package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import static com.dts.practice.simulation.SimulationLearningModel.*;

@Testcontainers
class SimulationLearningPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("simulation_learning_test");
    static JdbcTemplate jdbc;static TransactionTemplate tx;static String oldHistory;static Map<String,String> oldData;
    final UUID user=UUID.randomUUID();final SimulationBank.Snapshot bank=SimulationFixtures.published().snapshot();
    final AtomicReference<Instant> now=new AtomicReference<>(Instant.parse("2026-11-02T01:00:00Z"));
    SimulationLearningRepository repo;SimulationLearningService service;SimulationLearningReads reads;
    SimulationAccess access;SimulationBankClient banks;SimulationPlaybackClient media;SimulationThumbnailBatch thumbnails;
    @BeforeAll static void migrate() {
        var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(ds);tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
        flyway("30").migrate();oldHistory=history();oldData=fingerprints();
        var result=flyway("31").migrate();assertEquals(1,result.migrationsExecuted);assertEquals("31",result.migrations.getFirst().version);
        assertEquals(oldHistory,history());assertEquals(oldData,fingerprints());assertEquals(0,flyway("31").migrate().migrationsExecuted);
    }
    static Flyway flyway(String target) {return Flyway.configure().dataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword()).locations("classpath:db/migration").target(target).load();}
    static String history() {return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE version<>'31'",String.class);}
    static Map<String,String> fingerprints() {
        var result=new TreeMap<String,String>();
        for(String table:jdbc.queryForList("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename NOT LIKE 'simulation_%' AND tablename<>'flyway_schema_history'",String.class)) {
            String name="\""+table.replace("\"","\"\"")+"\"";
            result.put(table,jdbc.queryForObject("SELECT count(*)::text||':'||md5(coalesce(string_agg(to_jsonb(t)::text,E'\\n' ORDER BY to_jsonb(t)::text),'')) FROM "+name+" t",String.class));
        }
        return result;
    }
    @BeforeEach void setup() {
        repo=new SimulationLearningRepository(jdbc,new ObjectMapper().findAndRegisterModules());access=mock(SimulationAccess.class);banks=mock(SimulationBankClient.class);media=mock(SimulationPlaybackClient.class);thumbnails=mock(SimulationThumbnailBatch.class);
        var actor=new SimulationAccess.Actor(user,"fixture-access");when(access.learn()).thenReturn(actor);when(access.read(anyBoolean())).thenReturn(actor);when(banks.load(any())).thenReturn(bank);when(banks.current()).thenReturn(bank);
        when(media.resolve(any(),any())).thenAnswer(a->{Row r=a.getArgument(1);return new SimulationPlaybackClient.Response(r.snapshot.videoMediaId(),"http://localhost:9000/fixture.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(300),r.bank);});
        when(thumbnails.resolve(any(),any(),any())).thenAnswer(a->{List<SimulationBank.Scenario> rows=a.getArgument(2);var result=new HashMap<UUID,SimulationThumbnailClient.Thumbnail>();for(var s:rows)result.put(s.scenarioId(),new SimulationThumbnailClient.Thumbnail(s.thumbnailMediaId(),s.scenarioId(),bank.bankVersion(),"http://localhost:9000/fixture.webp?signature=fixture",OffsetDateTime.now().plusSeconds(300)));return result;});
        var clock=mock(SimulationLearningClock.class);when(clock.now()).thenAnswer(a->now.get());service=new SimulationLearningService(access,banks,repo,media,clock);reads=new SimulationLearningReads(access,banks,repo,thumbnails,clock);
    }
    <T> T call(Supplier<T> value) {return tx.execute(s->value.get());}
    Attempt create(int number) {return call(()->service.create(new Create(UUID.randomUUID(),bank.scenarios().get(number-1).scenarioId(),"B",bank.bankVersion())));}
    Event event(Attempt a,int position) {return new Event(UUID.randomUUID(),a.playbackEventId(),position);}
    Attempt start(Attempt a) {call(()->service.playback(a.attemptId()));return call(()->service.start(a.attemptId(),event(a,a.resumeAtMs())));}
    Attempt finish(Attempt a,Integer reaction) {
        for(int p=a.resumeAtMs()+3000;p<60000;p+=3000) {
            now.set(now.get().plusMillis(3000));int position=p;
            if(reaction!=null&&reaction>=p-3000&&reaction<p) {
                int delta=p-reaction;now.set(now.get().minusMillis(delta));call(()->service.response(a.attemptId(),event(a,reaction)));now.set(now.get().plusMillis(delta));
            }
            call(()->service.heartbeat(a.attemptId(),event(a,position)));
        }
        now.set(now.get().plusSeconds(3));return call(()->service.complete(a.attemptId(),event(a,60000)));
    }
    @Test void migrationPreservesTheoryAndExecutedHistory() {assertEquals(oldHistory,history());assertEquals(oldData,fingerprints());}
    @Test void createRetryIsIdempotentAndConflictingEventCannotTargetAnotherScenario() {
        var req=new Create(UUID.randomUUID(),bank.scenarios().getFirst().scenarioId(),"B",bank.bankVersion());var a=call(()->service.create(req));
        assertEquals(a.attemptId(),call(()->service.create(req)).attemptId());
        assertThrows(BusinessException.class,()->call(()->service.create(new Create(req.eventId(),bank.scenarios().get(1).scenarioId(),"B",bank.bankVersion()))));
        assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM simulation_attempts WHERE user_id=?",Integer.class,user));
    }
    @Test void startRequiresVerifiedMediaGrantAndDoesNotRestartOnRetry() {
        var a=create(1);var e=event(a,0);assertThrows(BusinessException.class,()->call(()->service.start(a.attemptId(),e)));
        call(()->service.playback(a.attemptId()));var first=call(()->service.start(a.attemptId(),e));now.set(now.get().plusSeconds(1));
        assertEquals(first.startedAt(),call(()->service.start(a.attemptId(),e)).startedAt());
    }
    @Test void realClockTimestampsRemainIdenticalAfterPostgresRoundTripAndRetry() {
        var real=new SimulationLearningService(access,banks,repo,media,new SimulationLearningClock());
        var created=call(()->real.create(new Create(UUID.randomUUID(),bank.scenarios().getFirst().scenarioId(),"B",bank.bankVersion())));
        assertEquals(created.createdAt(),call(()->real.get(created.attemptId())).createdAt());
        call(()->real.playback(created.attemptId()));var event=event(created,0);var first=call(()->real.start(created.attemptId(),event));
        assertEquals(first.startedAt(),call(()->real.start(created.attemptId(),event)).startedAt());
    }
    @ParameterizedTest @CsvSource({"10000,5","11000,4","12000,3","13000,2","14000,1","16000,0"})
    void completePersistsScoreFromFrozenFirstMediaResponse(int position,int score) {
        var a=start(create(1));var done=finish(a,position);assertEquals(score,done.result().score());assertEquals(position,done.result().firstResponseMs());
        assertEquals(done.result(),call(()->service.result(a.attemptId())));assertEquals(1,call(()->reads.overview("CE",bank.bankVersion())).practicedScenarios());
    }
    @Test void noResponseZeroCountsPracticedAndRetryDoesNotDoubleCount() {
        var first=finish(start(create(1)),null);assertEquals(0,first.result().score());assertFalse(first.responseRecorded());
        var second=finish(start(create(1)),null);assertNotEquals(first.attemptId(),second.attemptId());
        var overview=call(()->reads.overview("C1",bank.bankVersion()));assertEquals(1,overview.practicedScenarios());
        var page=call(()->reads.chapter(1,"B",bank.bankVersion(),"PRACTICED","SCENARIO_ASC",0,12));
        assertEquals(1,page.filteredCount());assertEquals(0,page.items().getFirst().bestScore());assertEquals(2,page.items().getFirst().completedAttemptCount());
    }
    @Test void repeatedSpaceKeepsFirstResponseIncludingConcurrentRequests() throws Exception {
        var a=start(create(1));now.set(now.get().plusSeconds(10));
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var f1=pool.submit(()->call(()->service.response(a.attemptId(),event(a,10000))));var f2=pool.submit(()->call(()->service.response(a.attemptId(),event(a,10500))));
            var one=f1.get(10,TimeUnit.SECONDS);var two=f2.get(10,TimeUnit.SECONDS);assertEquals(one.firstResponseMs(),two.firstResponseMs());
        }
    }
    @Test void completionCannotBeForgedByEndedOrByWaitingWithoutHeartbeats() {
        var a=start(create(1));assertThrows(BusinessException.class,()->call(()->service.complete(a.attemptId(),event(a,60000))));
        now.set(now.get().plusSeconds(60));assertThrows(BusinessException.class,()->call(()->service.complete(a.attemptId(),event(a,60000))));
        assertEquals(0,call(()->reads.overview("B",bank.bankVersion())).practicedScenarios());
    }
    @Test void interruptionRequiresNewPlaybackNonceAndPreservesFirstResponse() {
        var a=start(create(1));now.set(now.get().plusSeconds(10));var responded=call(()->service.response(a.attemptId(),event(a,10000)));
        var stop=call(()->service.interrupt(a.attemptId(),new Interrupt(UUID.randomUUID(),a.playbackEventId(),"VIDEO_ERROR")));
        assertEquals("INTERRUPTED",stop.status());assertEquals(10000,stop.firstResponseMs());assertNotEquals(a.playbackEventId(),stop.playbackEventId());
        assertThrows(BusinessException.class,()->call(()->service.heartbeat(a.attemptId(),event(a,10000))));
        now.set(now.get().plusSeconds(20));var resumed=start(stop);assertEquals(10000,resumed.resumeAtMs());assertEquals(responded.firstResponseMs(),resumed.firstResponseMs());
        assertEquals(0,call(()->reads.overview("B",bank.bankVersion())).practicedScenarios());
    }
    @Test void crossUserCannotReadMutateOrIssuePlayback() {
        var a=create(1);var other=new SimulationAccess.Actor(UUID.randomUUID(),"other");when(access.read(anyBoolean())).thenReturn(other);when(access.learn()).thenReturn(other);
        assertThrows(BusinessException.class,()->call(()->service.get(a.attemptId())));assertThrows(BusinessException.class,()->call(()->service.cancel(a.attemptId(),new Cancel(UUID.randomUUID()))));
        assertThrows(BusinessException.class,()->call(()->service.playback(a.attemptId())));verifyNoInteractions(media);
    }
    @Test void completeRetryPreservesTerminalRowAndResult() {
        var a=finish(start(create(1)),null);String before=jdbc.queryForObject("SELECT to_jsonb(t)::text FROM simulation_attempts t WHERE id=?",String.class,a.attemptId());
        var same=call(()->service.complete(a.attemptId(),event(a,60000)));assertEquals(a.result(),same.result());
        assertEquals(before,jdbc.queryForObject("SELECT to_jsonb(t)::text FROM simulation_attempts t WHERE id=?",String.class,a.attemptId()));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_attempts SET score=5 WHERE id=?",a.attemptId()));
    }
    @Test void bookmarksAreSharedAcrossLicensesAndCursorUsesCompareAndSwap() {
        var id=bank.scenarios().getFirst().scenarioId();var req=new Bookmark("B",bank.bankVersion(),true);call(()->service.bookmark(id,req));call(()->service.bookmark(id,req));
        assertEquals(1,call(()->reads.chapter(1,"CE",bank.bankVersion(),"BOOKMARKED","SCENARIO_ASC",0,12)).filteredCount());
        var cursor=new CursorCommand(UUID.randomUUID(),"B",bank.bankVersion(),id,0);assertEquals(1,call(()->service.cursor(1,cursor)).revision());assertEquals(1,call(()->service.cursor(1,cursor)).revision());
        assertThrows(BusinessException.class,()->call(()->service.cursor(1,new CursorCommand(UUID.randomUUID(),"B",bank.bankVersion(),id,0))));
    }
    @Test void newerAttemptCursorIsNotOverwrittenByOldCompletion() {
        var old=start(create(1));var newer=start(create(2));finish(old,null);
        assertEquals(newer.scenarioId(),repo.cursor(user,bank.bankVersion(),1).scenarioId());
    }
    @Test void snapshotsEventsAndFirstResponsesAreImmutableAtDatabaseBoundary() {
        var a=start(create(1));now.set(now.get().plusSeconds(10));call(()->service.response(a.attemptId(),event(a,10000)));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_attempts SET first_response_ms=11000 WHERE id=?",a.attemptId()));
        assertThrows(Exception.class,()->jdbc.update("UPDATE simulation_attempts SET scenario_snapshot='{}'::jsonb WHERE id=?",a.attemptId()));
        assertThrows(Exception.class,()->jdbc.update("DELETE FROM simulation_attempt_events WHERE user_id=?",user));
    }
}
