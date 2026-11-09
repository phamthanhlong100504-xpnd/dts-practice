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
import static com.dts.practice.simulation.SimulationBookmarkModel.*;

@Testcontainers
class SimulationBookmarkPostgresTest {
    @Container static final PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("simulation_bookmark_test");
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
    static SimulationBookmarkService bookmarks;
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
        @Bean SimulationBookmarkService bookmarks() {return new SimulationBookmarkService(access,banks,reviewRepo,thumbnails);}
    }
    @BeforeAll static void prepareDatabaseAndRealTransactionalServices() {
        source = new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        jdbc = new JdbcTemplate(source);
        Flyway.configure().dataSource(source).locations("classpath:db/migration").target("31").load().migrate();
        historyBefore = history(); theoryBefore = fingerprints(false);
        access=mock(SimulationAccess.class);banks=mock(SimulationBankClient.class);
        media=mock(SimulationPlaybackClient.class);thumbnails=mock(SimulationThumbnailBatch.class);
        clock=mock(SimulationLearningClock.class);
        attempts=spy(new SimulationLearningRepository(jdbc,new ObjectMapper().findAndRegisterModules()));
        reviewRepo=spy(new SimulationReviewRepository(jdbc));
        context=new AnnotationConfigApplicationContext();
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("review-fixture",
                Map.of("simulation.learning-enabled","true")));
        context.register(Config.class);context.refresh();
        learning=context.getBean(SimulationLearningService.class);learningReads=context.getBean(SimulationLearningReads.class);
        reviews=context.getBean(SimulationReviewService.class);replays=context.getBean(SimulationReplayService.class);bookmarks=context.getBean(SimulationBookmarkService.class);
    }
    @AfterAll static void closeContext() {if(context!=null)context.close();}
    @BeforeEach void resetActorAndDependencies() {
        reset(access,banks,media,thumbnails,clock,reviewRepo,attempts);user=UUID.randomUUID();actor(user);
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
    BookmarkOverview overview() {return bookmarks.overview("B",bank.bankVersion());}
    BookmarkPage page(Integer chapter,int page,int size,String version) {return bookmarks.list("B",bank.bankVersion(),chapter,"SCENARIO_ASC",page,size,version);}
    BookmarkPage page() {return page(null,0,12,null);}
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

    UUID id(int number) {return bank.scenarios().get(number-1).scenarioId();}
    void mark(int number,boolean value) {learning.bookmark(id(number),new Bookmark("B",bank.bankVersion(),value));}
    static Map<String,String> withoutBookmarks() {
        var rows=fingerprints(true);rows.remove("simulation_bookmarks");return rows;
    }

    @Test void c03t01NoBookmarksIsAValidEmptyDataset() {
        var o=overview();var p=page();
        assertEquals(0,o.totalBookmarked());assertEquals(6,o.chapters().size());assertTrue(p.items().isEmpty());
        assertEquals(0,p.filteredCount());assertEquals(0,p.page().totalPages());assertEquals(o.datasetVersion(),p.datasetVersion());
    }
    @Test void c03t02UnpracticedScenarioCanBeBookmarkedWithoutCreatingAnAttempt() {
        var before=withoutBookmarks();mark(1,true);var p=page();
        assertEquals(1,p.totalBookmarked());assertFalse(p.items().getFirst().isPracticed());assertNull(p.items().getFirst().bestScore());
        assertEquals(0,p.items().getFirst().completedAttemptCount());assertTrue(learningReads.scenario(id(1),"B",bank.bankVersion()).scenario().isBookmarked());
        assertEquals(before,withoutBookmarks());
    }
    @Test void c03t03CompletedZeroIsPracticedAndStillBookmarked() {
        completed(1,0);mark(1,true);var item=page().items().getFirst();
        assertTrue(item.isPracticed());assertEquals(0,item.bestScore());assertEquals(1,item.completedAttemptCount());
    }
    @Test void c03t04PerfectScoreRemainsInBookmarkList() {
        completed(1,5);mark(1,true);assertEquals(5,page().items().getFirst().bestScore());assertEquals(1,overview().totalBookmarked());
        assertEquals(0,reviews.overview("B",bank.bankVersion()).totalNeedReview());
    }
    @Test void c03t05RepeatedTrueDoesNotDuplicateOrRewriteTheExistingBookmark() {
        mark(1,true);var before=fingerprints(true);String version=overview().datasetVersion();now.set(now.get().plusSeconds(1));mark(1,true);
        assertEquals(1,overview().totalBookmarked());assertEquals(version,overview().datasetVersion());assertEquals(before,fingerprints(true));
    }
    @Test void c03t06ChapterFilterPreservesWholeBankCountsAndAllSixChapters() {
        mark(1,true);mark(30,true);var p=page(1,0,12,null);
        assertEquals(2,p.totalBookmarked());assertEquals(1,p.filteredCount());assertEquals(6,p.chapters().size());
        assertEquals(2,p.chapters().stream().mapToInt(BookmarkChapter::bookmarkedCount).sum());assertEquals(1,p.items().getFirst().chapterId());
    }
    @Test void c03t07EmptyFilteredChapterIsNotAnEmptyBank() {
        mark(1,true);var p=page(6,0,12,null);
        assertEquals(1,p.totalBookmarked());assertEquals(0,p.filteredCount());assertTrue(p.items().isEmpty());
    }
    @Test void c03t08OrderingAndPaginationAreStableWithoutMissingOrDuplicatedIds() {
        for(int n=29;n>=1;n--)mark(n,true);
        var all=new ArrayList<BookmarkItem>();String version=overview().datasetVersion();
        for(int p=0;p<3;p++)all.addAll(page(null,p,12,version).items());
        assertEquals(java.util.stream.IntStream.rangeClosed(1,29).boxed().toList(),all.stream().map(BookmarkItem::scenarioNumber).toList());
        assertEquals(29,new HashSet<>(all.stream().map(BookmarkItem::scenarioId).toList()).size());
        var beyond=page(null,10000,12,version);assertTrue(beyond.items().isEmpty());assertEquals(29,beyond.filteredCount());assertEquals(3,beyond.page().totalPages());
    }
    @Test void c03t09UnbookmarkRemovesOnlyTheSelectedScenarioAndUpdatesCounts() {
        mark(1,true);mark(30,true);mark(1,false);var p=page();
        assertEquals(List.of(30),p.items().stream().map(BookmarkItem::scenarioNumber).toList());assertEquals(1,p.totalBookmarked());
        assertEquals(0,p.chapters().getFirst().bookmarkedCount());assertFalse(learningReads.scenario(id(1),"B",bank.bankVersion()).scenario().isBookmarked());
    }
    @Test void c03t10RemovingLastBookmarkCreatesAValidEmptyList() {
        mark(1,true);mark(1,false);assertEquals(0,page().totalBookmarked());assertTrue(page().items().isEmpty());
    }
    @Test void c03t11FailureAfterTheActualDeleteRollsBackTheBookmark() {
        mark(1,true);var before=fingerprints(true);
        doAnswer(call->{call.callRealMethod();throw SimulationFailure.unavailable();})
                .when(attempts).bookmark(eq(user),eq(bank.bankVersion()),eq(id(1)),eq(false),any());
        assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE",assertThrows(BusinessException.class,()->mark(1,false)).getErrorCode());
        assertEquals(before,fingerprints(true));assertEquals(1,overview().totalBookmarked());
    }
    @Test void c03t12TrueAndFalseRetriesAreIdempotentAndLeaveScoresHistoryAndCursorAlone() {
        completed(1,4);var before=withoutBookmarks();
        for(int i=0;i<3;i++)mark(1,true);assertEquals(1,overview().totalBookmarked());
        for(int i=0;i<3;i++)mark(1,false);assertEquals(0,overview().totalBookmarked());
        assertEquals(before,withoutBookmarks());assertEquals(4,learningReads.scenario(id(1),"B",bank.bankVersion()).scenario().bestScore());
    }
    @Test void c03t13AllSimulationReadSurfacesShareTheSameBookmarkStore() {
        completed(1,2);mark(1,true);
        assertTrue(learningReads.scenario(id(1),"B",bank.bankVersion()).scenario().isBookmarked());
        assertTrue(learningReads.chapter(1,"B",bank.bankVersion(),"BOOKMARKED","SCENARIO_ASC",0,12).items().getFirst().isBookmarked());
        assertTrue(reviews.list("B",bank.bankVersion(),null,"BEST_SCORE_ASC",0,12,null).items().getFirst().isBookmarked());
        assertTrue(page().items().getFirst().isBookmarked());
        mark(1,false);assertTrue(page().items().isEmpty());assertFalse(reviews.list("B",bank.bankVersion(),null,"BEST_SCORE_ASC",0,12,null).items().getFirst().isBookmarked());
    }
    @Test void c03t14NewLearningResultsUpdateMaximumWithoutRemovingBookmark() {
        mark(1,true);completed(1,2);completed(1,5);completed(1,1);
        var item=page().items().getFirst();assertEquals(5,item.bestScore());assertEquals(3,item.completedAttemptCount());assertTrue(item.isBookmarked());
        assertEquals(1,overview().totalBookmarked());assertEquals(5,learningReads.scenario(id(1),"B",bank.bankVersion()).scenario().bestScore());
    }
    @Test void c03t15UsersHaveIndependentBookmarksAndOneCannotDeleteTheOthersMark() {
        mark(1,true);String original=overview().datasetVersion();UUID other=UUID.randomUUID();actor(other);
        assertEquals(0,overview().totalBookmarked());mark(1,false);assertTrue(page().items().isEmpty());
        mark(1,true);mark(30,true);assertEquals(2,overview().totalBookmarked());actor(user);
        assertEquals(1,overview().totalBookmarked());assertEquals(original,overview().datasetVersion());
    }
    @Test void c03t16ConcurrentDuplicateWritesAndIndependentDeviceReloadsAreConsistent() throws Exception {
        try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {
            var gate=new CountDownLatch(1);
            var a=pool.submit(()->{gate.await();mark(1,true);return true;});
            var b=pool.submit(()->{gate.await();mark(1,true);return true;});gate.countDown();
            assertTrue(a.get(10,TimeUnit.SECONDS));assertTrue(b.get(10,TimeUnit.SECONDS));
            assertEquals(1,overview().totalBookmarked());
            pool.submit(()->mark(1,false)).get(10,TimeUnit.SECONDS);assertEquals(0,overview().totalBookmarked());
            pool.submit(()->mark(1,true)).get(10,TimeUnit.SECONDS);assertEquals(1,overview().totalBookmarked());
        }
    }
    @Test void c03t17UnknownScenarioCannotCreateOrRemovePublishedBookmarks() {
        mark(1,true);var before=fingerprints(true);
        assertEquals("SIMULATION_NOT_FOUND",assertThrows(BusinessException.class,()->learning.bookmark(UUID.randomUUID(),new Bookmark("B",bank.bankVersion(),true))).getErrorCode());
        assertEquals(before,fingerprints(true));assertEquals(1,overview().totalBookmarked());
    }
    @Test void c03t18UnpublishedBankAndUnavailableMediaAreNotSuccessfulEmptyData() {
        mark(1,true);doThrow(SimulationFailure.notFound()).when(banks).load(any());
        assertEquals("SIMULATION_NOT_FOUND",assertThrows(BusinessException.class,this::overview).getErrorCode());
        doReturn(bank).when(banks).load(any());doThrow(SimulationFailure.unavailable()).when(thumbnails).resolve(any(),any(),any());
        assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE",assertThrows(BusinessException.class,this::page).getErrorCode());assertEquals(1,overview().totalBookmarked());
    }
    @Test void c03t19ExistingMigrationHistoryAndTheoryDataAreUntouchedBySimulationBookmarks() {
        var before=fingerprints(false);mark(1,true);completed(1,4);mark(1,false);page();
        assertEquals(before,fingerprints(false));assertEquals(theoryBefore,fingerprints(false));assertEquals(historyBefore,history());
        assertEquals(32L,jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history",Long.class));
    }
    @Test void paginationVersionChangesForBookmarkMembershipAndMarkedBestScore() {
        mark(1,true);String first=overview().datasetVersion();mark(30,true);
        assertEquals("SIMULATION_BOOKMARKS_CHANGED",assertThrows(BusinessException.class,()->page(null,1,12,first)).getErrorCode());
        String second=overview().datasetVersion();completed(1,0);
        assertEquals("SIMULATION_BOOKMARKS_CHANGED",assertThrows(BusinessException.class,()->page(null,0,12,second)).getErrorCode());
    }
    @Test void sharedLicenseButDifferentBankAndScoringStayInTheirDefinedScopes() {
        completed(1,4);mark(1,true);assertEquals(overview().datasetVersion(),bookmarks.overview("CE",bank.bankVersion()).datasetVersion());
        learning.bookmark(id(1),new Bookmark("CE",bank.bankVersion(),false));assertEquals(0,overview().totalBookmarked());mark(1,true);
        var changed=identity(bank.bankVersion()+"-new",bank.scoringVersion(),bank.manifestSha256());
        doReturn(changed).when(banks).load(any());assertEquals(0,bookmarks.overview("B",changed.bankVersion()).totalBookmarked());
        learning.bookmark(id(1),new Bookmark("B",changed.bankVersion(),true));assertNull(bookmarks.list("B",changed.bankVersion(),null,"SCENARIO_ASC",0,12,null).items().getFirst().bestScore());
        doReturn(identity(bank.bankVersion(),"scoring-changed",bank.manifestSha256())).when(banks).load(any());
        assertNull(page().items().getFirst().bestScore());doReturn(bank).when(banks).load(any());assertEquals(4,page().items().getFirst().bestScore());
    }
    @Test void actualReadOnlyRepeatableReadAvoidsMixedStatsAndConcurrentBookmarkMembership() throws Exception {
        mark(1,true);completed(1,0);
        doAnswer(call->{
            Object rows=call.callRealMethod();assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
            assertEquals("on",jdbc.queryForObject("SHOW transaction_read_only",String.class));
            try(var pool=Executors.newVirtualThreadPerTaskExecutor()) {pool.submit(()->mark(1,false)).get(10,TimeUnit.SECONDS);}
            return rows;
        }).when(reviewRepo).completed(any(),any());
        var read=page();assertEquals(1,read.totalBookmarked());assertTrue(read.items().getFirst().isBookmarked());
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM simulation_bookmarks WHERE user_id=?",Long.class,user));
    }
    @Test void repeatedReadAndThumbnailRenewalDoNotWriteProgressOrAnySimulationRow() {
        mark(1,true);completed(1,4);var before=fingerprints(true);
        var first=page();var second=page(null,0,12,first.datasetVersion());
        assertEquals(first.datasetVersion(),second.datasetVersion());assertEquals(before,fingerprints(true));
    }
    @Test void legacyTheoryBookmarkServiceStillWritesAndReadsItsOwnTableOnly() {
        var before=fingerprints(false);var simulation=fingerprints(true);
        var transaction=new org.springframework.transaction.support.TransactionTemplate(new DataSourceTransactionManager(source));
        transaction.executeWithoutResult(status->{
            int question=jdbc.queryForObject("""
                    INSERT INTO questions(chapter,question_text,options,correct_answer,bank_version,source_question_id,applicable_licenses,critical_licenses)
                    VALUES(1,'Card03 theory regression','{"A":"yes","B":"no"}'::jsonb,'A','card03-theory-regression',190003,'["B"]','[]') RETURNING id
                    """,Integer.class);
            var questions=mock(com.dts.practice.service.QuestionService.class);
            doReturn(List.of(new com.dts.practice.dto.response.QuestionResponse(question,1,"Theory",Map.of("A","yes"),false,null,null,"A",null)))
                    .when(questions).getByChapter(1,"B");
            var theory=new com.dts.practice.service.ChapterBookmarkService(questions,jdbc);
            var yes=new com.dts.practice.dto.request.SetChapterBookmarkRequest("B",true);
            theory.set(user,1,question,yes);theory.set(user,1,question,yes);
            assertEquals(List.of(question),theory.list(user,1,"B").questionIds());
            theory.set(user,1,question,new com.dts.practice.dto.request.SetChapterBookmarkRequest("B",false));
            assertTrue(theory.list(user,1,"B").questionIds().isEmpty());assertEquals(simulation,fingerprints(true));status.setRollbackOnly();
        });
        assertEquals(before,fingerprints(false));assertEquals(historyBefore,history());
    }
}
