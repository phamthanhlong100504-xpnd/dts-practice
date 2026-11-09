package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.dts.practice.simulation.SimulationExamModel.*;

@Testcontainers
class SimulationExamPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("dts_practice");
    static JdbcTemplate jdbc;static TransactionTemplate tx;static String oldHistory;static Map<String,String> oldData;
    final UUID user=UUID.randomUUID();final SimulationBank.Snapshot bank=SimulationFixtures.published().snapshot();
    final AtomicReference<Instant> now=new AtomicReference<>(Instant.parse("2026-11-03T01:00:00Z"));
    SimulationExamRepository repo;SimulationExamService service;SimulationExamAccess access;SimulationBankClient banks;SimulationExamMediaClient media;SimulationExamRules rules;
    @BeforeAll static void migrate() {
        var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        flyway("31").migrate();oldHistory=history();oldData=fingerprints();var result=flyway("32").migrate();assertEquals(1,result.migrationsExecuted);assertEquals("32",result.migrations.getFirst().version);
        assertEquals(oldHistory,history());assertEquals(oldData,fingerprints());assertEquals(0,flyway("32").migrate().migrationsExecuted);
    }
    static Flyway flyway(String target){return Flyway.configure().dataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword()).locations("classpath:db/migration").target(target).load();}
    static String history(){return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE version<>'32'",String.class);}
    static Map<String,String> fingerprints() {
        var result=new TreeMap<String,String>();
        for(String table:jdbc.queryForList("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename NOT LIKE 'simulation_exam_%' AND tablename<>'flyway_schema_history'",String.class)) {
            assertTrue(table.matches("[a-z_]+"));result.put(table,jdbc.queryForObject("SELECT count(*)::text||':'||md5(coalesce(string_agg(to_jsonb(t)::text,E'\\n' ORDER BY to_jsonb(t)::text),'')) FROM "+table+" t",String.class));
        }
        return result;
    }
    @BeforeEach void setup() {
        repo=new SimulationExamRepository(jdbc,new ObjectMapper().findAndRegisterModules());access=mock(SimulationExamAccess.class);banks=mock(SimulationBankClient.class);media=mock(SimulationExamMediaClient.class);rules=spy(new SimulationExamRules());
        when(access.require(anyBoolean())).thenReturn(new SimulationAccess.Actor(user,"fixture-access"));when(banks.current()).thenReturn(bank);
        when(media.eligibility(any(),any(),any())).thenAnswer(a->{List<SimulationBank.Scenario> rows=a.getArgument(2);return rows.stream().map(s->new SimulationExamMediaClient.Eligible(s.videoMediaId(),true,s.durationMs(),s.video().videoTrackDurationMs(),"fixtureetag","READY")).toList();});
        when(media.playback(any(),any(),any(),anyBoolean())).thenAnswer(a->{Row r=a.getArgument(1);Frame f=a.getArgument(2);boolean review=a.getArgument(3);return new SimulationExamMediaClient.Video(r.id,f.position,f.playbackId,r.bank,r.manifest,f.snapshot.videoMediaId(),"http://localhost:9000/media/uploads/2026/11/03/"+f.snapshot.videoMediaId()+".mp4?fixture=only",OffsetDateTime.now().plusSeconds(300),"video/mp4",f.duration,review?"REVIEW":"CURRENT");});
        var clock=mock(SimulationLearningClock.class);when(clock.now()).thenAnswer(a->now.get());service=new SimulationExamService(access,banks,repo,media,rules,clock);
    }
    @AfterEach void preserved(){assertEquals(oldHistory,history());assertEquals(oldData,fingerprints());}
    <T>T call(Supplier<T> action){return tx.execute(s->action.get());}
    Session create(){return call(()->service.create(UUID.randomUUID(),new Create("RANDOM","B",null)));}
    Event event(Session s,int ms){return new Event(UUID.randomUUID(),s.currentPosition(),s.playbackEventId(),ms);}
    Session play(Session s){call(()->service.playback(s.sessionId(),"B"));return call(()->service.start(s.sessionId(),event(s,0),"B"));}
    Session finish(Session initial,Integer response) {
        var s=play(initial);Instant start=now.get();int duration=repo.frame(s.sessionId(),s.currentPosition()).duration;
        if(response!=null&&response==0)call(()->service.reaction(s.sessionId(),event(s,0),"B"));
        for(int current=0;current<duration;) {
            int next=Math.min(duration,current+3000);
            if(response!=null&&response>current&&response<=next){now.set(start.plusMillis(response));call(()->service.reaction(s.sessionId(),event(s,response),"B"));}
            now.set(start.plusMillis(next));int ms=next;
            if(next<duration)call(()->service.heartbeat(s.sessionId(),event(s,ms),"B"));else return call(()->service.complete(s.sessionId(),event(s,ms),"B"));
            current=next;
        }
        throw new AssertionError();
    }
    Session ready(Integer...responses){var s=create();for(Integer response:responses)s=finish(s,response);return s;}
    Session submit(Session s){return call(()->service.submit(s.sessionId(),new Command(UUID.randomUUID()),"B"));}
    String rows(String table,UUID session){return jdbc.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY to_jsonb(t)::text),'[]'::jsonb)::text FROM "+table+" t WHERE "+(table.equals("simulation_exam_sessions")?"id":"session_id")+"=?",String.class,session);}
    void code(String expected,Runnable action){var error=assertThrows(SimulationExamFailure.class,action::run);assertEquals(expected,error.code);}

    @Test @DisplayName("C05-T01 fixed ten unique random positions survive repository round trip") void fixedRandomSnapshot() {
        var s=create();var f=repo.frames(s.sessionId());assertEquals(10,f.size());assertEquals(10,f.stream().map(x->x.scenarioId).distinct().count());assertEquals(s,call(()->service.get(s.sessionId(),"B")));
        assertEquals(List.of(1,2,3,4,5,6,7,8,9,10),f.stream().map(x->x.position).toList());assertEquals(10,s.totalScenarios());
    }
    @Test @DisplayName("C05-T02 new completed exams keep separate snapshots") void newExamDoesNotReuseCompletedSession() {
        var done=submit(ready(new Integer[10]));var old=rows("simulation_exam_positions",done.sessionId());var next=create();assertNotEquals(done.sessionId(),next.sessionId());assertEquals(old,rows("simulation_exam_positions",done.sessionId()));
    }
    @Test @DisplayName("C05-T03 same Idempotency-Key is stable and conflicting request is rejected") void idempotentCreate() {
        UUID key=UUID.randomUUID();var request=new Create("RANDOM","B",null);var first=call(()->service.create(key,request));assertEquals(first,call(()->service.create(key,request)));
        code("SIMULATION_EXAM_EVENT_CONFLICT",()->call(()->service.create(key,new Create("RANDOM","CE",null))));assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM simulation_exam_sessions WHERE user_id=?",Integer.class,user));
    }
    @Test @DisplayName("C05-T04 active includes READY_TO_SUBMIT and is unique across licenses") void activeSessionConflict() {
        var s=create();var e=assertThrows(SimulationExamFailure.class,()->call(()->service.create(UUID.randomUUID(),new Create("RANDOM","CE",null))));assertEquals("ACTIVE_SESSION_EXISTS",e.code);assertEquals(s.sessionId(),e.activeSessionId);
        var scoped=call(()->service.active("CE"));assertEquals(s.sessionId(),scoped.session().sessionId());assertEquals("CE",scoped.session().scope().licenseClass());
    }
    @Test @DisplayName("C05-T05/T23 reload and restart preserve first reaction and saved order") void restartRetainsReaction() {
        var s=play(create());now.set(now.get().plusSeconds(10));var response=call(()->service.reaction(s.sessionId(),event(s,10000),"B"));String original=repo.encode(repo.frames(s.sessionId()).stream().map(f->f.scenarioId).toList());
        var restarted=call(()->service.restart(s.sessionId(),new Restart(UUID.randomUUID(),s.currentPosition(),s.playbackEventId()),"B"));assertTrue(restarted.responseRecorded());assertNotEquals(s.playbackEventId(),restarted.playbackEventId());
        assertEquals(10000,repo.frame(s.sessionId(),1).responseMs);assertEquals(0,repo.frame(s.sessionId(),1).lastMs);assertEquals(original,repo.encode(repo.frames(s.sessionId()).stream().map(f->f.scenarioId).toList()));
        code("SIMULATION_EXAM_PLAYBACK_CHANGED",()->call(()->service.heartbeat(s.sessionId(),event(s,10000),"B")));assertEquals(response.responseRecorded(),call(()->service.get(s.sessionId(),"B")).responseRecorded());
    }
    @Test @DisplayName("C05-T06 insufficient verified READY media creates no session") void insufficientReadySource() {
        doAnswer(a->{List<SimulationBank.Scenario> batch=a.getArgument(2);return batch.stream().map(s->new SimulationExamMediaClient.Eligible(s.videoMediaId(),false,s.durationMs(),s.video().videoTrackDurationMs(),null,"NOT_READY")).toList();}).when(media).eligibility(any(),any(),any());
        code("SIMULATION_EXAM_NOT_ENOUGH_MEDIA",this::create);assertTrue(repo.active(user).isEmpty());assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM simulation_exam_sessions WHERE user_id=?",Integer.class,user));
    }
    @Test @DisplayName("C05-T07 active session projection exposes no source or scoring fields") void activeProjectionHidesAnswers() {
        var s=create();var tree=new ObjectMapper().findAndRegisterModules().valueToTree(s);
        for(String key:List.of("scenarioId","scenarioNumber","title","chapterName","hazardSign","recognitionTip","score","totalScore","passed","scoringTimeline","scoringSegments"))assertNull(tree.get(key));
        for(var p:tree.get("positions")){Set<String> keys=new HashSet<>();p.fieldNames().forEachRemaining(keys::add);assertEquals(Set.of("position","status"),keys);}
    }
    @Test @DisplayName("C05-T08 first valid response is stored without score disclosure") void firstReaction() {
        var s=play(create());now.set(now.get().plusSeconds(10));var r=call(()->service.reaction(s.sessionId(),event(s,10000),"B"));assertTrue(r.responseRecorded());assertEquals(10000,repo.frame(s.sessionId(),1).responseMs);assertNull(repo.frame(s.sessionId(),1).score);
    }
    @Test @DisplayName("C05-T09 concurrent Space and button never replace the first response") void concurrentReactions()throws Exception {
        var s=play(create());now.set(now.get().plusSeconds(10));try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var one=pool.submit(()->call(()->service.reaction(s.sessionId(),event(s,10000),"B")));var two=pool.submit(()->call(()->service.reaction(s.sessionId(),event(s,10500),"B")));
            assertTrue(one.get(10,TimeUnit.SECONDS).responseRecorded());assertTrue(two.get(10,TimeUnit.SECONDS).responseRecorded());
        }
        assertTrue(Set.of(10000,10500).contains(repo.frame(s.sessionId(),1).responseMs));
    }
    @ParameterizedTest @CsvSource({"9999,0","10000,5","10999,5","11000,4","12000,3","13000,2","14000,1","15000,1","15001,0"})
    @DisplayName("C05-T10/T12 shared Card01 timeline and boundary scoring") void scoreBoundaries(int ms,int expected){var s=finish(create(),ms);assertEquals(expected,repo.frame(s.sessionId(),1).score);assertEquals(ms,repo.frame(s.sessionId(),1).responseMs);}
    @Test @DisplayName("C05-T11 valid full playback without reaction scores zero") void noReaction(){var s=finish(create(),null);var f=repo.frame(s.sessionId(),1);assertEquals(0,f.score);assertNull(f.responseMs);assertTrue(f.complete);}
    @Test @DisplayName("C05-T13 forged ended, seeking and lost heartbeat cannot complete") void incompletePlaybackCannotFinish() {
        var s=play(create());code("SIMULATION_EXAM_VIDEO_NOT_FINISHED",()->call(()->service.complete(s.sessionId(),event(s,0),"B")));
        assertThrows(BusinessException.class,()->call(()->service.complete(s.sessionId(),event(s,60000),"B")));
        now.set(now.get().plusSeconds(60));assertThrows(BusinessException.class,()->call(()->service.complete(s.sessionId(),event(s,60000),"B")));assertEquals(0,repo.get(user,s.sessionId(),false).completedCount);
        assertTrue(call(()->service.get(s.sessionId(),"B")).requiresRestart());
    }
    @Test @DisplayName("C05-T14 future position cannot start or get a reaction") void futurePositionDenied() {
        var s=play(create());var future=repo.frame(s.sessionId(),2);code("SIMULATION_EXAM_POSITION_CHANGED",()->call(()->service.start(s.sessionId(),new Event(UUID.randomUUID(),2,future.playbackId,0),"B")));
        assertNull(future.score);assertFalse(future.complete);
    }
    @Test @DisplayName("C05-T15 completion repeats do not advance twice") void completionRetry() {
        var initial=create();var done=finish(initial,null);var event=new Event(UUID.randomUUID(),1,initial.playbackEventId(),60000);
        assertEquals(1,call(()->service.complete(initial.sessionId(),event,"B")).completedScenarios());assertEquals(1,call(()->service.complete(initial.sessionId(),event,"B")).completedScenarios());
        assertEquals(1,call(()->service.get(done.sessionId(),"B")).completedScenarios());
    }
    @Test @DisplayName("C05-T16 early submit is rejected") void earlySubmit(){var s=create();code("SIMULATION_EXAM_INCOMPLETE",()->submit(s));assertThrows(SimulationExamFailure.class,()->call(()->service.result(s.sessionId(),"B")));}
    @ParameterizedTest @CsvSource({"34,false,FAILED","35,true,PASSED","50,true,PASSED","0,false,FAILED"})
    @DisplayName("C05-T17/T29/T30/T31/T32 total and 35/50 threshold") void totalAndPassingBoundary(int total,boolean expected,String label) {
        Integer[] responses=new Integer[10];int left=total;
        for(int i=0;i<10;i++){int score=Math.min(5,left);left-=score;responses[i]=score==0?null:15000-score*1000;}
        var r=submit(ready(responses));var result=call(()->service.result(r.sessionId(),"B"));assertEquals(total,result.totalScore());assertEquals(35,result.passingScore());assertEquals(expected,result.passed());assertEquals(label,result.resultStatus());
        assertEquals(total,result.items().stream().mapToInt(ResultItem::score).sum());assertEquals(10,result.items().size());
    }
    @Test @DisplayName("C05-T18/T19/T34 repeat submit and lost response preserve one immutable result") void submitRetryAndRecovery() {
        var s=ready(new Integer[10]);var request=new Command(UUID.randomUUID());var first=call(()->service.submit(s.sessionId(),request,"B"));String result=rows("simulation_exam_results",s.sessionId());
        assertEquals(first,call(()->service.submit(s.sessionId(),request,"B")));assertEquals(first,submit(first));assertEquals(result,rows("simulation_exam_results",s.sessionId()));assertEquals(0,call(()->service.result(s.sessionId(),"B")).totalScore());
    }
    @Test @DisplayName("C05-T20/T33 unfinished/cancelled sessions do not expose result or review") void incompleteAndCancelledHaveNoPassFail() {
        var s=create();code("NOT_COMPLETED",()->call(()->service.review(s.sessionId(),"B")));code("NOT_COMPLETED",()->call(()->service.result(s.sessionId(),"B")));
        call(()->service.cancel(s.sessionId(),new Command(UUID.randomUUID()),"B"));code("NOT_COMPLETED",()->call(()->service.result(s.sessionId(),"B")));code("NOT_COMPLETED",()->call(()->service.reviewPlayback(s.sessionId(),1,"B")));
    }
    @Test @DisplayName("C05-T21 review uses all ten frozen snapshots without creating learning attempts") void completedReview() {
        var s=submit(ready(10000,11000,12000,13000,14000,null,null,null,null,null));var review=call(()->service.review(s.sessionId(),"B"));assertEquals(10,review.items().size());
        for(var item:review.items()){var frame=repo.frame(s.sessionId(),item.position());assertEquals(frame.snapshot.title(),item.title());assertEquals(frame.snapshot.hazardSign(),item.hazardSign());assertEquals(SimulationLearningRules.segments(frame.snapshot.scoringTimeline()),item.scoringSegments());}
        String before=rows("simulation_exam_positions",s.sessionId());call(()->service.reviewPlayback(s.sessionId(),1,"B"));call(()->service.reviewPlayback(s.sessionId(),10,"B"));assertEquals(before,rows("simulation_exam_positions",s.sessionId()));
    }
    @Test @DisplayName("C05-T22 cancel is terminal and completed results cannot be cancelled") void cancelIsTerminal() {
        var s=create();var c=call(()->service.cancel(s.sessionId(),new Command(UUID.randomUUID()),"B"));assertEquals("CANCELLED",c.status());assertTrue(repo.active(user).isEmpty());
        code("SIMULATION_EXAM_TERMINAL",()->call(()->service.playback(s.sessionId(),"B")));code("SIMULATION_EXAM_INCOMPLETE",()->submit(c));
        var done=submit(ready(new Integer[10]));code("SIMULATION_EXAM_TERMINAL",()->call(()->service.cancel(done.sessionId(),new Command(UUID.randomUUID()),"B")));
    }
    @Test @DisplayName("C05-T24 URL renewal keeps nonce, response and position") void urlRenewalDoesNotRestart() {
        var s=play(create());now.set(now.get().plusSeconds(3));call(()->service.heartbeat(s.sessionId(),event(s,3000),"B"));
        var first=call(()->service.playback(s.sessionId(),"B"));var second=call(()->service.playback(s.sessionId(),"B"));assertEquals(first.playbackEventId(),second.playbackEventId());assertEquals(3000,repo.frame(s.sessionId(),1).lastMs);
    }
    @Test @DisplayName("C05-T25 another owner cannot read mutate or issue current/review playback") void crossOwner() {
        var s=create();doReturn(new SimulationAccess.Actor(UUID.randomUUID(),"other")).when(access).require(anyBoolean());
        code("SIMULATION_EXAM_NOT_FOUND",()->call(()->service.get(s.sessionId(),"B")));code("SIMULATION_EXAM_NOT_FOUND",()->call(()->service.playback(s.sessionId(),"B")));code("SIMULATION_EXAM_NOT_FOUND",()->call(()->service.review(s.sessionId(),"B")));
        code("SIMULATION_EXAM_NOT_FOUND",()->call(()->service.cancel(s.sessionId(),new Command(UUID.randomUUID()),"B")));
    }
    @Test @DisplayName("C05-T26 bank changes cannot rewrite active playback or result snapshots") void changedBankDoesNotAffectPinnedExam() {
        var s=create();doThrow(SimulationFailure.unavailable()).when(banks).current();var done=s;for(int i=0;i<10;i++)done=finish(done,10000);submit(done);
        var result=call(()->service.result(s.sessionId(),"CE"));assertEquals(bank.bankVersion(),result.bankVersion());assertEquals(bank.scoringVersion(),result.scoringVersion());assertEquals(50,result.totalScore());assertEquals("CE",result.scope().licenseClass());
    }
    @Test @DisplayName("C05-T28 migration leaves theory Card01-03 rows and old history unchanged") void migrationAndLearningIsolation(){assertEquals(oldData,fingerprints());assertEquals(oldHistory,history());}
    @Test @DisplayName("C05-T35 policy changes never alter historical results") void policyVersionIsPinned() {
        var done=submit(ready(10000,10000,10000,10000,10000,10000,10000,null,null,null));var original=call(()->service.result(done.sessionId(),"B"));
        doReturn(new Policy(10,5,50,40,"future-policy-fixture")).when(rules).currentPolicy();assertEquals(original,call(()->service.result(done.sessionId(),"B")));
        var next=create();assertEquals(40,next.policy().passingScore());assertEquals(35,original.passingScore());
    }
    @Test void concurrentCreateRespectsDatabaseUniqueness()throws Exception {
        UUID key=UUID.randomUUID();try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var one=pool.submit(()->call(()->service.create(key,new Create("RANDOM","B",null))));var two=pool.submit(()->call(()->service.create(key,new Create("RANDOM","B",null))));
            assertEquals(one.get(20,TimeUnit.SECONDS).sessionId(),two.get(20,TimeUnit.SECONDS).sessionId());
        }
        var s=repo.active(user).orElseThrow();assertThrows(Exception.class,()->jdbc.update("INSERT INTO simulation_exam_sessions SELECT gen_random_uuid(),user_id,mode,license_class,bank_version,scoring_version,manifest_sha256,policy_version,passing_score,status,completed_count,current_position,gen_random_uuid(),create_sha256,created_at,started_at,updated_at,completed_at,cancelled_at FROM simulation_exam_sessions WHERE id=?",s.id));
    }
    @Test void failedEventRollsBackRowsAndReceiptsAtomically() {
        var s=play(create());var event=event(s,3000);now.set(now.get().plusSeconds(3));String before=rows("simulation_exam_positions",s.sessionId());
        assertThrows(IllegalStateException.class,()->call(()->{service.heartbeat(s.sessionId(),event,"B");throw new IllegalStateException("fixture commit failure");}));
        assertEquals(before,rows("simulation_exam_positions",s.sessionId()));assertTrue(repo.receipt(user,event.eventId()).isEmpty());
    }
    @Test void frozenSnapshotsFirstReactionAndResultsCannotBeUpdatedThroughSql() {
        var s=play(create());now.set(now.get().plusSeconds(10));call(()->service.reaction(s.sessionId(),event(s,10000),"B"));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_exam_positions SET first_response_ms=11000 WHERE session_id=? AND position=1",s.sessionId()));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_exam_positions SET scenario_snapshot='{}'::jsonb WHERE session_id=? AND position=1",s.sessionId()));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_exam_sessions SET passing_score=0 WHERE id=?",s.sessionId()));
    }

    @Test void concurrentSubmitProducesExactlyOneImmutableResult()throws Exception {
        var s=ready(new Integer[10]);try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var one=pool.submit(()->submit(s));var two=pool.submit(()->submit(s));
            assertEquals(one.get(20,TimeUnit.SECONDS),two.get(20,TimeUnit.SECONDS));
        }
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM simulation_exam_results WHERE session_id=?",Integer.class,s.sessionId()));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_exam_results SET total_score=50 WHERE session_id=?",s.sessionId()));
        assertThrows(Exception.class,()->jdbc.update("DELETE FROM simulation_exam_results WHERE session_id=?",s.sessionId()));
    }
    @Test void failedSubmitRollsBackResultSessionAndReceiptThenCanRetry() {
        var s=ready(new Integer[10]);var command=new Command(UUID.randomUUID());String before=rows("simulation_exam_sessions",s.sessionId());
        assertThrows(IllegalStateException.class,()->call(()->{service.submit(s.sessionId(),command,"B");throw new IllegalStateException("fixture rollback");}));
        assertEquals(before,rows("simulation_exam_sessions",s.sessionId()));assertEquals("[]",rows("simulation_exam_results",s.sessionId()));assertTrue(repo.receipt(user,command.eventId()).isEmpty());
        assertEquals("COMPLETED",call(()->service.submit(s.sessionId(),command,"B")).status());
    }
    @Test void reopeningServiceAndReadOnlyReviewDoNotMutateExamTables() {
        var s=submit(ready(new Integer[10]));Map<String,String> before=new TreeMap<>();
        for(String table:List.of("simulation_exam_sessions","simulation_exam_positions","simulation_exam_results","simulation_exam_events"))before.put(table,rows(table,s.sessionId()));
        setup();var readonly=new TransactionTemplate(tx.getTransactionManager());readonly.setReadOnly(true);
        assertEquals(s,readonly.execute(t->service.get(s.sessionId(),"B")));
        assertEquals(10,readonly.execute(t->service.review(s.sessionId(),"B")).items().size());
        readonly.execute(t->service.reviewPlayback(s.sessionId(),10,"B"));
        before.forEach((table,value)->assertEquals(value,rows(table,s.sessionId())));
    }
    @Test void mediaFailureNeverAdvancesOrScoresCurrentPosition() {
        var s=create();String before=rows("simulation_exam_positions",s.sessionId());doThrow(SimulationExamFailure.unavailable()).when(media).playback(any(),any(),any(),anyBoolean());
        assertThrows(SimulationExamFailure.class,()->call(()->service.playback(s.sessionId(),"B")));assertEquals(before,rows("simulation_exam_positions",s.sessionId()));assertEquals(0,repo.get(user,s.sessionId(),false).completedCount);
    }
    @Test void readyToSubmitStillBlocksAnotherActiveSession() {
        var s=ready(new Integer[10]);assertEquals("READY_TO_SUBMIT",s.status());assertEquals(s.sessionId(),call(()->service.active("B")).session().sessionId());
        code("ACTIVE_SESSION_EXISTS",this::create);code("NOT_COMPLETED",()->call(()->service.result(s.sessionId(),"B")));
    }
    @Test void eventIdCannotBeReusedWithDifferentPayload() {
        var s=play(create());now.set(now.get().plusSeconds(3));var event=event(s,3000);var ack=call(()->service.heartbeat(s.sessionId(),event,"B"));
        assertEquals(ack,call(()->service.heartbeat(s.sessionId(),event,"B")));code("SIMULATION_EXAM_EVENT_CONFLICT",()->call(()->service.heartbeat(s.sessionId(),new Event(event.eventId(),1,s.playbackEventId(),3001),"B")));
    }
    @Test void presetAndStalePreparationBankCannotCreateAnySession() {
        code("SIMULATION_EXAM_INVALID_REQUEST",()->call(()->service.create(UUID.randomUUID(),new Create("PRESET","B",null))));
        code("SIMULATION_EXAM_BANK_CHANGED",()->call(()->service.create(UUID.randomUUID(),new Create("RANDOM","B","other-bank"))));assertTrue(repo.active(user).isEmpty());
    }
}
