package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@Testcontainers
class SimulationShortcutRestartPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("dts_practice");
    static JdbcTemplate jdbc;static TransactionTemplate tx;static String oldHistory;
    final UUID user=UUID.randomUUID();final AtomicReference<Instant> now=new AtomicReference<>(Instant.parse("2026-11-03T14:00:00Z"));
    final ObjectMapper json=new ObjectMapper().findAndRegisterModules();
    SimulationBank.Snapshot bank;SimulationAccess access;SimulationExamAccess examAccess;SimulationBankClient banks;
    SimulationLearningRepository lessons;SimulationExamRepository exams;SimulationShortcutRepository store;
    SimulationLearningService learning;SimulationExamService engine;SimulationShortcutService shortcuts;SimulationPresetService presets;
    @BeforeAll static void migrate(){var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));flyway("33").migrate();oldHistory=history();assertEquals(1,flyway("34").migrate().migrationsExecuted);assertEquals(oldHistory,history());assertEquals(0,flyway("34").migrate().migrationsExecuted);}
    static Flyway flyway(String version){return Flyway.configure().dataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword()).locations("classpath:db/migration").target(version).load();}
    static String history(){return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE version<>'34'",String.class);}
    @BeforeEach void setup(){
        var original=SimulationFixtures.published().snapshot();var rows=original.scenarios().stream().map(s->new SimulationBank.Scenario(s.scenarioId(),s.scenarioNumber(),s.chapterId(),s.title(),s.durationMs(),s.videoMediaId(),s.thumbnailMediaId(),s.hazardSign(),s.recognitionTip(),new SimulationBank.Timeline(17000,18000,19000,20000,21000,22000),s.video(),s.thumbnail())).toList();
        bank=new SimulationBank.Snapshot(original.bankVersion(),original.contentVersion(),original.assetVersion(),"scoring-shortcuts-fixture",original.sourceSha256(),original.assetManifestSha256(),original.manifestSha256(),original.chapters(),rows);
        lessons=new SimulationLearningRepository(jdbc,json);exams=new SimulationExamRepository(jdbc,json);store=new SimulationShortcutRepository(jdbc,json);access=mock(SimulationAccess.class);examAccess=mock(SimulationExamAccess.class);banks=mock(SimulationBankClient.class);
        var actor=new SimulationAccess.Actor(user,"fixture");when(access.learn()).thenReturn(actor);when(access.read(anyBoolean())).thenReturn(actor);when(examAccess.require(anyBoolean())).thenReturn(actor);when(banks.current()).thenReturn(bank);when(banks.load(any())).thenReturn(bank);when(banks.presets()).thenReturn(SimulationPresetFixtures.catalogue(bank));when(banks.preset(any())).thenReturn(SimulationPresetFixtures.version(bank,1,"v1"));
        var clock=mock(SimulationLearningClock.class);when(clock.now()).thenAnswer(a->now.get());
        var playback=mock(SimulationPlaybackClient.class);when(playback.resolve(any(),any())).thenAnswer(a->{SimulationLearningModel.Row r=a.getArgument(1);return new SimulationPlaybackClient.Response(r.snapshot.videoMediaId(),"http://localhost:9000/fixture",OffsetDateTime.now().plusSeconds(300),r.bank);});
        learning=new SimulationLearningService(access,banks,lessons,playback,clock);
        var media=mock(SimulationExamMediaClient.class);when(media.eligibility(any(),any(),any())).thenAnswer(a->{List<SimulationBank.Scenario> list=a.getArgument(2);return list.stream().map(s->new SimulationExamMediaClient.Eligible(s.videoMediaId(),true,s.durationMs(),s.video().videoTrackDurationMs(),"fixtureetag","READY")).toList();});
        when(media.playback(any(),any(),any(),anyBoolean())).thenAnswer(a->{SimulationExamModel.Row r=a.getArgument(1);SimulationExamModel.Frame f=a.getArgument(2);return new SimulationExamMediaClient.Video(r.id,f.position,f.playbackId,r.bank,r.manifest,f.snapshot.videoMediaId(),"http://localhost:9000/fixture",OffsetDateTime.now().plusSeconds(300),"video/mp4",f.duration,"CURRENT");});
        engine=new SimulationExamService(examAccess,banks,exams,media,new SimulationExamRules(),clock);shortcuts=new SimulationShortcutService(access,examAccess,lessons,exams,store,clock);presets=new SimulationPresetService(examAccess,banks,exams,clock);
    }
    @AfterEach void historyPreserved(){assertEquals(oldHistory,history());}
    <T>T call(Supplier<T> f){return tx.execute(s->f.get());}
    SimulationExamModel.Session create(String mode){var p=SimulationPresetFixtures.version(bank,1,"v1");return call(()->engine.create(UUID.randomUUID(),new SimulationExamModel.Create(mode,"B",bank.bankVersion(),mode.equals("PRESET")?p.presetSetId():null,mode.equals("PRESET")?p.presetVersionId():null)));}
    SimulationShortcutModel.State state(SimulationExamModel.Session s){return call(()->shortcuts.state("EXAM",s.sessionId(),"B"));}
    SimulationShortcutModel.Request command(SimulationExamModel.Session s,Integer ms,String proof){return new SimulationShortcutModel.Request(UUID.randomUUID(),s.currentPosition(),s.playbackEventId(),ms,proof);}
    SimulationExamModel.Event event(SimulationExamModel.Session s,int ms){return new SimulationExamModel.Event(UUID.randomUUID(),s.currentPosition(),s.playbackEventId(),ms);}
    SimulationExamModel.Session enable(SimulationExamModel.Session s){return call(()->shortcuts.enable("EXAM",s.sessionId(),command(s,null,null),"B")).state().session();}
    SimulationExamModel.Session play(SimulationExamModel.Session s){var p=call(()->engine.playback(s.sessionId(),"B"));return call(()->engine.start(s.sessionId(),event(s,p.restartAtMs()),"B"));}
    SimulationExamModel.Session react(SimulationExamModel.Session s,int ms){int base=exams.frame(s.sessionId(),s.currentPosition()).segmentStart;Instant start=now.get();for(int t=base+3000;t<ms;t+=3000){now.set(start.plusMillis(t-base));int pos=t;call(()->engine.heartbeat(s.sessionId(),event(s,pos),"B"));}now.set(start.plusMillis(ms-base));return call(()->engine.reaction(s.sessionId(),event(s,ms),"B"));}
    SimulationShortcutModel.Ack next(SimulationExamModel.Session s){String proof=state(s).reactionAcknowledgement();return call(()->shortcuts.advance(s.sessionId(),command(s,null,proof),"B"));}
    SimulationExamModel.Session skipped(SimulationExamModel.Session s){return call(()->shortcuts.skip("EXAM",s.sessionId(),command(s,0,null),"B")).state().session();}
    SimulationLearningModel.Attempt lesson(){return call(()->learning.create(new SimulationLearningModel.Create(UUID.randomUUID(),bank.scenarios().getFirst().scenarioId(),"B",bank.bankVersion())));}
    SimulationLearningModel.Event levent(SimulationLearningModel.Attempt a,int ms){return new SimulationLearningModel.Event(UUID.randomUUID(),a.playbackEventId(),ms);}
    SimulationShortcutModel.Request lcommand(SimulationLearningModel.Attempt a,Integer ms){return new SimulationShortcutModel.Request(UUID.randomUUID(),1,a.playbackEventId(),ms,null);}
    SimulationLearningModel.Attempt lplay(SimulationLearningModel.Attempt a){call(()->learning.playback(a.attemptId()));return call(()->learning.start(a.attemptId(),levent(a,a.resumeAtMs())));}

    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"RANDOM","PRESET"})
    void unansweredRestartAllowsOneNewIntroAfterStart(String mode){
        var s=play(enable(create(mode)));var first=call(()->shortcuts.skip("EXAM",s.sessionId(),command(s,0,null),"B"));
        var started=play(first.state().session());assertFalse(state(started).skipIntro().available());
        var req=new SimulationExamModel.Restart(UUID.randomUUID(),1,started.playbackEventId());var restart=call(()->engine.restart(s.sessionId(),req,"B"));
        assertEquals("NOT_PLAYING",state(restart).skipIntro().reason());assertEquals(0,state(restart).startAtMs());
        var playing=play(restart);assertTrue(state(playing).skipIntro().available());var nextSkip=command(playing,0,null);
        var ack=call(()->shortcuts.skip("EXAM",s.sessionId(),nextSkip,"B"));
        assertEquals(ack.receipt(),call(()->shortcuts.skip("EXAM",s.sessionId(),nextSkip,"B")).receipt());
        assertEquals(restart,call(()->engine.restart(s.sessionId(),req,"B")));assertEquals(10000,state(restart).startAtMs());
        var resumed=play(ack.state().session());assertFalse(state(resumed).skipIntro().available());
        var reacted=react(resumed,17432);next(reacted);var frame=exams.frame(s.sessionId(),1);assertEquals(17432,frame.responseMs);assertEquals(5,frame.score);assertEquals(1,exams.get(user,s.sessionId(),false).completedCount);
    }
    @ParameterizedTest @CsvSource({"RANDOM,0","PRESET,0","RANDOM,17432","PRESET,17432"})
    void committedResponseCannotBeClearedOrRearmSkip(String mode,int ms){
        var s=play(enable(create(mode)));if(ms>0)s=play(skipped(s));var reacted=react(s,ms);var before=exams.frame(s.sessionId(),1);var firstEvent=before.responseEvent;var oldProof=state(reacted).reactionAcknowledgement();
        var restarted=call(()->engine.restart(reacted.sessionId(),new SimulationExamModel.Restart(UUID.randomUUID(),1,reacted.playbackEventId()),"B"));var current=play(restarted);
        assertEquals("REACTION_ALREADY_RECORDED",state(current).skipIntro().reason());assertNotEquals(oldProof,state(current).reactionAcknowledgement());
        assertThrows(SimulationShortcutFailure.class,()->call(()->shortcuts.skip("EXAM",current.sessionId(),command(current,0,null),"B")));
        assertThrows(SimulationShortcutFailure.class,()->call(()->shortcuts.advance(current.sessionId(),command(current,null,oldProof),"B")));
        next(current);var after=exams.frame(s.sessionId(),1);assertEquals(ms,after.responseMs);assertEquals(firstEvent,after.responseEvent);assertEquals(ms==0?0:5,after.score);
    }
    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"RANDOM","PRESET"})
    void oldSkipReceiptAfterRestartDoesNotRestoreCheckpoint(String mode){
        var s=play(enable(create(mode)));var request=command(s,0,null);var first=call(()->shortcuts.skip("EXAM",s.sessionId(),request,"B"));var skipped=first.state().session();
        var restarted=call(()->engine.restart(s.sessionId(),new SimulationExamModel.Restart(UUID.randomUUID(),1,skipped.playbackEventId()),"B"));
        var recovered=call(()->shortcuts.skip("EXAM",s.sessionId(),request,"B"));assertEquals(first.receipt(),recovered.receipt());assertEquals(restarted.playbackEventId(),recovered.state().playbackEventId());assertEquals(0,recovered.state().startAtMs());assertTrue(state(play(restarted)).skipIntro().available());
    }
    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"RANDOM","PRESET"})
    void concurrentRestartSameEventReturnsOneNonceAndCannotRearmAfterSkip(String mode)throws Exception{
        var s=skipped(play(enable(create(mode))));var request=new SimulationExamModel.Restart(UUID.randomUUID(),1,s.playbackEventId());
        SimulationExamModel.Session a,b;try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var x=pool.submit(()->call(()->engine.restart(s.sessionId(),request,"B")));var y=pool.submit(()->call(()->engine.restart(s.sessionId(),request,"B")));a=x.get(10,TimeUnit.SECONDS);b=y.get(10,TimeUnit.SECONDS);
        }assertEquals(a,b);var playing=play(a);var skipped=skipped(playing);var nonce=skipped.playbackEventId();
        call(()->engine.restart(s.sessionId(),request,"B"));assertEquals(nonce,state(skipped).playbackEventId());assertEquals(10000,state(skipped).startAtMs());assertFalse(state(play(skipped)).skipIntro().available());
    }
    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"RANDOM","PRESET"})
    void concurrentReactionZeroAndRestartNeverDropCommittedResponse(String mode)throws Exception{
        var s=play(enable(create(mode)));var restart=new SimulationExamModel.Restart(UUID.randomUUID(),1,s.playbackEventId());var response=event(s,0);
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()){
            var a=pool.submit(()->call(()->engine.restart(s.sessionId(),restart,"B")));var b=pool.submit(()->{try{return call(()->engine.reaction(s.sessionId(),response,"B"));}catch(SimulationExamFailure e){return null;}});a.get(10,TimeUnit.SECONDS);b.get(10,TimeUnit.SECONDS);
        }var f=exams.frame(s.sessionId(),1);assertNotEquals(s.playbackEventId(),f.playbackId);var current=play(call(()->engine.get(s.sessionId(),"B")));assertEquals(f.responseMs==null,state(current).skipIntro().available());if(f.responseMs!=null){assertEquals(0,f.responseMs);assertEquals(response.eventId(),f.responseEvent);}
    }
    @ParameterizedTest @org.junit.jupiter.params.provider.ValueSource(strings={"RANDOM","PRESET"})
    void rollbackRestartDoesNotRearmAndOtherPositionsRemainFrozen(String mode){
        var s=react(play(skipped(play(enable(create(mode))))),17432);next(s);String prior=jdbc.queryForObject("SELECT to_jsonb(p)::text FROM simulation_exam_positions p WHERE session_id=? AND position=1",String.class,s.sessionId());
        var second=skipped(play(call(()->engine.get(s.sessionId(),"B"))));var request=new SimulationExamModel.Restart(UUID.randomUUID(),2,second.playbackEventId());
        assertThrows(IllegalStateException.class,()->call(()->{engine.restart(second.sessionId(),request,"B");throw new IllegalStateException("isolated rollback fixture");}));
        assertEquals(second.playbackEventId(),state(second).playbackEventId());assertEquals(10000,state(second).startAtMs());assertEquals(prior,jdbc.queryForObject("SELECT to_jsonb(p)::text FROM simulation_exam_positions p WHERE session_id=? AND position=1",String.class,s.sessionId()));assertEquals(1,exams.get(user,s.sessionId(),false).completedCount);
    }
}
