package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import javax.sql.DataSource;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.dts.practice.simulation.SimulationLearningModel.*;
import static com.dts.practice.simulation.SimulationReviewModel.*;

@Testcontainers
class SimulationReviewPostgresTest {
    @Container static final PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("simulation_review_test");
    static DataSource source;
    static JdbcTemplate jdbc;
    static AnnotationConfigApplicationContext context;
    static SimulationAccess access;
    static SimulationBankClient banks;
    static SimulationPlaybackClient media;
    static SimulationThumbnailBatch thumbnails;
    static SimulationLearningClock clock;
    static SimulationLearningRepository attempts;
    static SimulationReviewRepository reviewRepo;
    static SimulationLearningService learning;
    static SimulationLearningReads learningReads;
    static SimulationReviewService reviews;
    static SimulationReplayService replays;
    static Map<String,String> theoryBefore;
    static String historyBefore;
    static final AtomicReference<Instant> now = new AtomicReference<>();
    final SimulationBank.Snapshot bank = SimulationFixtures.published().snapshot();
    UUID user;

    @TestConfiguration @EnableTransactionManagement(proxyTargetClass=true)
    static class Config {
        @Bean DataSource dataSource() {return source;}
        @Bean JdbcTemplate jdbcTemplate() {return jdbc;}
        @Bean PlatformTransactionManager transactionManager() {return new DataSourceTransactionManager(source);}
        @Bean SimulationLearningService learningService() {return new SimulationLearningService(access,banks,attempts,media,clock);}
        @Bean SimulationLearningReads learningReads() {return new SimulationLearningReads(access,banks,attempts,thumbnails,clock);}
        @Bean SimulationReviewService reviews() {return new SimulationReviewService(access,banks,reviewRepo,thumbnails);}
        @Bean SimulationReplayService replays() {return new SimulationReplayService(access,attempts,banks,media);}
    }
    @BeforeAll static void prepareDatabaseAndRealTransactionalServices() {
        source = new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        jdbc = new JdbcTemplate(source);
        Flyway.configure().dataSource(source).locations("classpath:db/migration").target("31").load().migrate();
        historyBefore = history(); theoryBefore = fingerprints(false);
        access=mock(SimulationAccess.class);banks=mock(SimulationBankClient.class);
        media=mock(SimulationPlaybackClient.class);thumbnails=mock(SimulationThumbnailBatch.class);
        clock=mock(SimulationLearningClock.class);
        attempts=new SimulationLearningRepository(jdbc,new ObjectMapper().findAndRegisterModules());
        reviewRepo=spy(new SimulationReviewRepository(jdbc));
        context=new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("review-fixture",
                Map.of("simulation.learning-enabled","true")));
        context.register(Config.class);context.refresh();
        learning=context.getBean(SimulationLearningService.class);learningReads=context.getBean(SimulationLearningReads.class);
        reviews=context.getBean(SimulationReviewService.class);replays=context.getBean(SimulationReplayService.class);
    }
    @AfterAll static void closeContext() {if(context!=null)context.close();}
    @BeforeEach void resetActorAndDependencies() {
        reset(access,banks,media,thumbnails,clock,reviewRepo);user=UUID.randomUUID();actor(user);
        now.set(Instant.parse("2026-11-02T01:00:00Z"));when(clock.now()).thenAnswer(i->now.get());
        when(banks.load(any())).thenReturn(bank);when(banks.current()).thenReturn(bank);
        when(media.resolve(any(),any())).thenAnswer(i->{Row r=i.getArgument(1);return new SimulationPlaybackClient.Response(
                r.snapshot.videoMediaId(),"http://localhost/fixture.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(300),r.bank);});
        when(thumbnails.resolve(any(),any(),any())).thenAnswer(i->{
            List<SimulationBank.Scenario> selected=i.getArgument(2);var map=new HashMap<UUID,SimulationThumbnailClient.Thumbnail>();
            for(var s:selected)map.put(s.scenarioId(),new SimulationThumbnailClient.Thumbnail(s.thumbnailMediaId(),s.scenarioId(),bank.bankVersion(),
                    "http://localhost/fixture.webp?signature=fixture",OffsetDateTime.now().plusSeconds(300)));
            return map;
        });
    }
    void actor(UUID id) {var a=new SimulationAccess.Actor(id,"fixture-access");when(access.read(anyBoolean())).thenReturn(a);when(access.learn()).thenReturn(a);}
    Attempt create(int number) {return learning.create(new Create(UUID.randomUUID(),bank.scenarios().get(number-1).scenarioId(),"B",bank.bankVersion()));}
    Event event(Attempt attempt,int position) {return new Event(UUID.randomUUID(),attempt.playbackEventId(),position);}
    Attempt start(Attempt attempt) {
        learning.playback(attempt.attemptId());
        return learning.start(attempt.attemptId(),event(attempt,attempt.resumeAtMs()));
    }
    Attempt finish(Attempt attempt,Integer reaction) {
        int duration=attempt.videoDurationMs();
        for(int p=attempt.resumeAtMs()+3000;p<duration;p+=3000) {
            now.set(now.get().plusMillis(3000));
            if(reaction!=null && reaction>=p-3000 && reaction<p) {
                int delta=p-reaction;now.set(now.get().minusMillis(delta));
                learning.response(attempt.attemptId(),event(attempt,reaction));now.set(now.get().plusMillis(delta));
            }
            learning.heartbeat(attempt.attemptId(),event(attempt,p));
        }
        now.set(now.get().plusMillis(3000));
        return learning.complete(attempt.attemptId(),event(attempt,duration));
    }
    Attempt completed(int number,int score) {
        int reaction=switch(score) {case 5->10000;case 4->11000;case 3->12000;case 2->13000;case 1->14000;default->16000;};
        var result=finish(start(create(number)),reaction);assertEquals(score,result.result().score());return result;
    }
    ReviewOverview overview() {return reviews.overview("B",bank.bankVersion());}
    ReviewPage page(Integer chapter,int page,int size,String version) {return reviews.list("B",bank.bankVersion(),chapter,"BEST_SCORE_ASC",page,size,version);}
    ReviewPage page() {return page(null,0,12,null);}
    static String history() {return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(t) ORDER BY installed_rank)::text FROM flyway_schema_history t",String.class);}
    static Map<String,String> fingerprints(boolean simulation) {
        var result=new TreeMap<String,String>();
        for(var table:jdbc.queryForList("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename<>'flyway_schema_history'",String.class)) {
            if(table.startsWith("simulation_")!=simulation)continue;
            String name="\""+table.replace("\"","\"\"")+"\"";
            result.put(table,jdbc.queryForObject("SELECT count(*)::text||':'||md5(coalesce(string_agg(to_jsonb(t)::text,E'\\n' ORDER BY to_jsonb(t)::text),'')) FROM "+name+" t",String.class));
        }
        return result;
    }
    SimulationBank.Snapshot identity(String version,String scoring,String manifest) {
        return new SimulationBank.Snapshot(version,bank.contentVersion(),bank.assetVersion(),scoring,bank.sourceSha256(),
                bank.assetManifestSha256(),manifest,bank.chapters(),bank.scenarios());
    }

    @Test void c02t01NoHistoryHasZeroPracticedAndSixEmptyChapters() {
        var o=overview();var p=page();
        assertEquals(0,o.practicedScenarios());assertEquals(0,o.totalNeedReview());assertEquals(6,o.chapters().size());
        assertTrue(p.items().isEmpty());assertEquals(0,p.page().totalPages());assertEquals(o.datasetVersion(),p.datasetVersion());
    }
    @Test void c02t02PerfectHistoryHasNonzeroPracticeButNoReview() {
        completed(1,5);var o=overview();assertEquals(1,o.practicedScenarios());assertEquals(0,o.totalNeedReview());assertTrue(page().items().isEmpty());
    }
    @ParameterizedTest @ValueSource(ints={0,1,2,3,4})
    void c02t03ValidScoresBelowFiveAppearIncludingZero(int score) {
        completed(1,score);var p=page();assertEquals(1,p.totalNeedReview());assertEquals(score,p.items().getFirst().bestScore());
        assertTrue(p.items().getFirst().isPracticed());assertEquals(5,p.items().getFirst().maxScore());
    }
    @Test void c02t04UnpracticedAndBookmarkedOnlyDoNotBecomeReview() {
        learning.bookmark(bank.scenarios().getFirst().scenarioId(),new Bookmark("B",bank.bankVersion(),true));
        assertEquals(0,overview().practicedScenarios());assertTrue(page().items().isEmpty());
    }
    @Test void c02t05TwoThenFourUsesMaximumAndDistinctScenarioCount() {
        completed(1,2);completed(1,4);var p=page();
        assertEquals(1,p.practicedScenarios());assertEquals(1,p.totalNeedReview());assertEquals(4,p.items().getFirst().bestScore());assertEquals(2,p.items().getFirst().completedAttemptCount());
    }
    @Test void c02t06FiveThenOneStaysMastered() {
        var five=completed(1,5);completed(1,1);assertEquals(0,overview().totalNeedReview());
        assertEquals(5,learning.result(five.attemptId()).score());assertTrue(page().items().isEmpty());
    }
    @Test void c02t07CreatedPlayingInterruptedAndCancelledAreNotPractice() {
        create(1);start(create(2));var interrupted=start(create(3));
        learning.interrupt(interrupted.attemptId(),new Interrupt(UUID.randomUUID(),interrupted.playbackEventId(),"VIDEO_ERROR"));
        var cancelled=create(4);learning.cancel(cancelled.attemptId(),new Cancel(UUID.randomUUID()));
        assertEquals(0,overview().practicedScenarios());assertEquals(0,page().totalNeedReview());
    }
    @Test void c02t08ChapterFilterKeepsGlobalTotalsAndAllPublishedChapters() {
        completed(1,0);completed(30,2);var p=page(1,0,12,null);
        assertEquals(2,p.totalNeedReview());assertEquals(1,p.filteredCount());assertEquals(6,p.chapters().size());
        assertEquals(2,p.chapters().stream().mapToInt(ReviewChapter::needReviewCount).sum());assertEquals(1,p.items().getFirst().chapterId());
    }
    @Test void c02t09EmptyFilteredChapterIsNotEmptyGlobalHistory() {
        completed(1,4);var p=page(3,0,12,null);
        assertEquals(1,p.practicedScenarios());assertEquals(1,p.totalNeedReview());assertEquals(0,p.filteredCount());assertTrue(p.items().isEmpty());
    }
    @Test void c02t10And11ScoreThenNumberOrderingAndPaginationHaveNoGapsOrDuplicates() {
        for(int n=1;n<=15;n++)completed(n,n%5);
        var all=new ArrayList<ReviewItem>();String version=overview().datasetVersion();
        for(int page=0;page<4;page++)all.addAll(page(null,page,4,version).items());
        var expected=java.util.stream.IntStream.rangeClosed(1,15).boxed().sorted(Comparator.<Integer>comparingInt(n->n%5).thenComparingInt(n->n)).toList();
        assertEquals(expected,all.stream().map(ReviewItem::scenarioNumber).toList());assertEquals(15,new HashSet<>(all.stream().map(ReviewItem::scenarioId).toList()).size());
        var beyond=page(null,10000,4,version);assertTrue(beyond.items().isEmpty());assertEquals(15,beyond.filteredCount());
    }
    @Test void c02t12CompletionAtFiveRemovesReviewAndUpdatesChapterTotals() {
        completed(1,0);var old=page();completed(1,5);var p=page();var o=overview();
        assertEquals(0,p.totalNeedReview());assertTrue(p.items().isEmpty());assertEquals(o.totalNeedReview(),p.totalNeedReview());
        assertEquals(0,p.chapters().getFirst().needReviewCount());assertEquals(1,p.practicedScenarios());assertNotEquals(old.datasetVersion(),p.datasetVersion());
    }
    @Test void c02t13AnotherLowResultRetainsCorrectMaximum() {
        completed(1,4);completed(1,2);assertEquals(4,page().items().getFirst().bestScore());
    }
    @Test void c02t14UnconfirmedFiveReactionOrFailedSaveDoesNotRemoveReview() {
        completed(1,0);var active=start(create(1));now.set(now.get().plusSeconds(10));
        learning.response(active.attemptId(),event(active,10000));
        assertThrows(BusinessException.class,()->learning.complete(active.attemptId(),event(active,60000)));
        assertEquals(0,page().items().getFirst().bestScore());assertEquals(1,page().items().getFirst().completedAttemptCount());
    }
    @Test void c02t15BookmarkIsSharedIdempotentAndIndependentOfBestScore() {
        completed(1,4);var id=bank.scenarios().getFirst().scenarioId();var req=new Bookmark("B",bank.bankVersion(),true);
        learning.bookmark(id,req);learning.bookmark(id,req);
        assertTrue(page().items().getFirst().isBookmarked());assertEquals(4,page().items().getFirst().bestScore());
        assertEquals(1,learningReads.chapter(1,"CE",bank.bankVersion(),"BOOKMARKED","SCENARIO_ASC",0,12).filteredCount());
        learning.bookmark(id,new Bookmark("CE",bank.bankVersion(),false));assertFalse(page().items().getFirst().isBookmarked());assertEquals(1,page().totalNeedReview());
    }
    @Test void c02t16TwoUsersHaveIndependentMaximumsAndBookmarks() {
        completed(1,2);var first=page();UUID other=UUID.randomUUID();actor(other);
        assertEquals(0,page().practicedScenarios());completed(1,4);assertEquals(4,page().items().getFirst().bestScore());
        actor(user);assertEquals(2,page().items().getFirst().bestScore());assertEquals(first.datasetVersion(),page().datasetVersion());
    }
    @Test void c02t17DifferentBankScoringAndBindingAreNotMixedButSharedLicensesAre() {
        completed(1,2);
        for(var other:List.of(identity(bank.bankVersion()+"-old",bank.scoringVersion(),bank.manifestSha256()),
                identity(bank.bankVersion(),"scoring-other",bank.manifestSha256()),identity(bank.bankVersion(),bank.scoringVersion(),"f".repeat(64)))) {
            doReturn(other).when(banks).load(any());
            var historical=learning.create(new Create(UUID.randomUUID(),other.scenarios().getFirst().scenarioId(),"B",other.bankVersion()));
            assertEquals(5,finish(start(historical),10000).result().score());
        }
        doReturn(bank).when(banks).load(any());assertEquals(2,page().items().getFirst().bestScore());
        var trailer=reviews.overview("CE",bank.bankVersion());assertEquals(overview().datasetVersion(),trailer.datasetVersion());
    }
    @Test void c02t18BadQueryAndDependencyErrorsAreNotSuccessfulEmptyData() {
        assertThrows(BusinessException.class,()->reviews.list("B",null,7,"BEST_SCORE_ASC",0,12,null));
        assertThrows(BusinessException.class,()->reviews.list("B",null,null,"BEST_SCORE_ASC",0,0,null));
        doThrow(SimulationFailure.unavailable()).when(banks).load(any());
        assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE",assertThrows(BusinessException.class,this::overview).getErrorCode());
    }
    @Test void c02t19UnpublishedSourceAndWithdrawnMediaProduceErrors() {
        completed(1,2);doThrow(SimulationFailure.notFound()).when(banks).load(any());
        assertThrows(BusinessException.class,this::page);
        doReturn(bank).when(banks).load(any());doThrow(SimulationFailure.unavailable()).when(thumbnails).resolve(any(),any(),any());
        assertThrows(BusinessException.class,this::page);
    }
    @Test void c02t20TheoryDataAndMigrationHistoryRemainUnchanged() {
        assertEquals(theoryBefore,fingerprints(false));assertEquals(historyBefore,history());
    }
    @Test void paginationVersionDetectsConcurrentUpdatesAndRefreshRestoresConsistency() {
        completed(1,0);String version=page().datasetVersion();completed(1,5);
        var error=assertThrows(BusinessException.class,()->page(null,1,1,version));assertEquals("SIMULATION_REVIEW_CHANGED",error.getErrorCode());
        assertEquals(0,page().filteredCount());
    }
    @Test void listUsesActualReadOnlyRepeatableReadAcrossConcurrentBookmarkCommit() throws Exception {
        completed(1,0);UUID id=bank.scenarios().getFirst().scenarioId();
        doAnswer(invocation->{
            Object stats=invocation.callRealMethod();
            assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            assertEquals("on",jdbc.queryForObject("SHOW transaction_read_only",String.class));
            try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
                pool.submit(()->learning.bookmark(id,new Bookmark("B",bank.bankVersion(),true))).get(10,TimeUnit.SECONDS);
            }
            return stats;
        }).when(reviewRepo).completed(any(),any());
        assertFalse(page().items().getFirst().isBookmarked());
        assertTrue(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM simulation_bookmarks WHERE user_id=? AND scenario_id=?)",Boolean.class,user,id));
    }
    @Test void replayRenewalIsReadOnlyAndPreservesAllSimulationRowsAndSavedResult() {
        var done=completed(1,4);var before=fingerprints(true);var saved=learning.result(done.attemptId());clearInvocations(access);
        doAnswer(invocation->{
            assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            assertEquals("on",jdbc.queryForObject("SHOW transaction_read_only",String.class));
            Row row=invocation.getArgument(1);
            return new SimulationPlaybackClient.Response(row.snapshot.videoMediaId(),"http://localhost/replay.mp4?signature=fixture",OffsetDateTime.now().plusSeconds(300),row.bank);
        }).when(media).resolve(any(),any());
        for(int i=0;i<2;i++) {
            var replay=replays.replay(done.attemptId());assertTrue(replay.readOnly());assertTrue(replay.policy().allowSeek());assertTrue(replay.policy().allowPause());
            assertEquals(done.attemptId(),replay.attemptId());assertEquals(done.scenarioId(),replay.scenarioId());
        }
        assertEquals(before,fingerprints(true));assertEquals(saved,learning.result(done.attemptId()));verify(access,never()).learn();
        assertEquals("SIMULATION_TERMINAL",assertThrows(BusinessException.class,()->learning.playback(done.attemptId())).getErrorCode());
    }
    @Test void replayOtherUserIs404AndCannotResolveMedia() {
        var done=completed(1,4);actor(UUID.randomUUID());clearInvocations(media);
        assertEquals("SIMULATION_NOT_FOUND",assertThrows(BusinessException.class,()->replays.replay(done.attemptId())).getErrorCode());verifyNoInteractions(media);
    }
    @ParameterizedTest @ValueSource(strings={"CREATED","PLAYING","INTERRUPTED","CANCELLED"})
    void replayRejectsNoncompletedStatesWithoutGrantOrProgressChanges(String status) {
        var attempt=create(1);
        if(status.equals("PLAYING")||status.equals("INTERRUPTED"))attempt=start(attempt);
        if(status.equals("INTERRUPTED"))attempt=learning.interrupt(attempt.attemptId(),new Interrupt(UUID.randomUUID(),attempt.playbackEventId(),"NETWORK"));
        if(status.equals("CANCELLED"))attempt=learning.cancel(attempt.attemptId(),new Cancel(UUID.randomUUID()));
        UUID id=attempt.attemptId();var before=fingerprints(true);clearInvocations(media);
        assertEquals("SIMULATION_REPLAY_NOT_READY",assertThrows(BusinessException.class,()->replays.replay(id)).getErrorCode());
        assertEquals(before,fingerprints(true));verifyNoInteractions(media);
    }
    @Test void replayRequiresPublishedPinnedBindingAndActiveMedia() {
        var done=completed(1,4);
        doReturn(identity(bank.bankVersion(),"changed-scoring",bank.manifestSha256())).when(banks).load(any());
        assertEquals("SIMULATION_BANK_CHANGED",assertThrows(BusinessException.class,()->replays.replay(done.attemptId())).getErrorCode());
        doThrow(SimulationFailure.notFound()).when(banks).load(any());assertThrows(BusinessException.class,()->replays.replay(done.attemptId()));
        doReturn(bank).when(banks).load(any());doThrow(SimulationFailure.unavailable()).when(media).resolve(any(),any());
        assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE",assertThrows(BusinessException.class,()->replays.replay(done.attemptId())).getErrorCode());
    }
}
