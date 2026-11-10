package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.dts.practice.simulation.SimulationBookmarkModel.*;
import static com.dts.practice.simulation.SimulationReviewModel.CompletedStats;

class SimulationBookmarkServiceTest {
    final SimulationBank.Snapshot bank = SimulationFixtures.published().snapshot();
    final SimulationAccess access = mock(SimulationAccess.class);
    final SimulationBankClient banks = mock(SimulationBankClient.class);
    final SimulationReviewRepository repo = mock(SimulationReviewRepository.class);
    final SimulationThumbnailBatch thumbnails = mock(SimulationThumbnailBatch.class);
    final Map<UUID, CompletedStats> stats = new HashMap<>();
    final Set<UUID> marks = new HashSet<>();
    final SimulationBookmarkService service = new SimulationBookmarkService(access, banks, repo, thumbnails);

    @BeforeEach void setup() {
        when(access.read(anyBoolean())).thenReturn(new SimulationAccess.Actor(SimulationFixtures.USER, "fixture"));
        when(banks.load(any())).thenReturn(bank);
        when(repo.completed(any(), any())).thenReturn(stats);
        when(repo.bookmarks(any(), any())).thenReturn(marks);
        when(thumbnails.resolve(any(), any(), any())).thenAnswer(call -> {
            List<SimulationBank.Scenario> selected = call.getArgument(2);
            var urls = new HashMap<UUID, SimulationThumbnailClient.Thumbnail>();
            for (var s : selected) urls.put(s.scenarioId(), new SimulationThumbnailClient.Thumbnail(
                    s.thumbnailMediaId(), s.scenarioId(), bank.bankVersion(),
                    "http://localhost/fixture.webp?signature=fixture", OffsetDateTime.now().plusSeconds(300)));
            return urls;
        });
    }
    UUID mark(int n) {UUID id=bank.scenarios().get(n-1).scenarioId();marks.add(id);return id;}
    BookmarkPage page(Integer chapter, int page, int size, String expected) {
        return service.list("B", bank.bankVersion(), chapter, "SCENARIO_ASC", page, size, expected);
    }
    @Test void overviewUsesPublishedMembershipSixChaptersAndDoesNotRequireMedia() {
        mark(1);mark(30);marks.add(UUID.randomUUID());
        var o=service.overview("B",bank.bankVersion());
        assertEquals(2,o.totalBookmarked());assertEquals(6,o.chapters().size());
        assertEquals(2,o.chapters().stream().mapToInt(BookmarkChapter::bookmarkedCount).sum());
        verify(access).read(false);verifyNoInteractions(thumbnails);
    }
    @Test void bookmarkListRetainsNullZeroAndFiveRatherThanReviewFiltering() {
        mark(1);stats.put(mark(2),new CompletedStats(1,0));stats.put(mark(3),new CompletedStats(4,5));
        var p=page(null,0,12,null);assertEquals(3,p.totalBookmarked());
        assertNull(p.items().get(0).bestScore());assertFalse(p.items().get(0).isPracticed());assertEquals(0,p.items().get(0).completedAttemptCount());
        assertEquals(0,p.items().get(1).bestScore());assertTrue(p.items().get(1).isPracticed());
        assertEquals(5,p.items().get(2).bestScore());assertTrue(p.items().get(2).isPracticed());
        assertTrue(p.items().stream().allMatch(i->i.isBookmarked() && i.maxScore()==5));
    }
    @Test void onlyRequestedPageResolvesImagesAndScenarioNumberOrderIsStable() {
        for(int n=29;n>=1;n--)mark(n);
        var p=page(null,1,12,null);
        assertEquals(java.util.stream.IntStream.rangeClosed(13,24).boxed().toList(),p.items().stream().map(BookmarkItem::scenarioNumber).toList());
        assertEquals(29,p.filteredCount());assertEquals(3,p.page().totalPages());
        verify(thumbnails).resolve(any(),eq(bank),argThat(rows->rows.size()==12));verify(access).read(true);
    }
    @Test void filteredEmptyRetainsGlobalCountsAndSixChapterMetadata() {
        mark(1);mark(30);var p=page(3,0,12,null);
        assertEquals(2,p.totalBookmarked());assertEquals(0,p.filteredCount());assertTrue(p.items().isEmpty());assertEquals(0,p.page().totalPages());
        assertEquals(6,p.chapters().size());assertEquals(2,p.chapters().stream().mapToInt(BookmarkChapter::bookmarkedCount).sum());
    }
    @Test void sharedLicenseUsesSameVersionAndUnrelatedPracticeDoesNotInvalidateMarks() {
        mark(1);var a=service.overview("CE",bank.bankVersion());
        stats.put(bank.scenarios().get(1).scenarioId(),new CompletedStats(1,4));
        var b=page(null,0,12,a.datasetVersion());
        assertEquals(a.datasetVersion(),b.datasetVersion());assertEquals("CE",a.scope().licenseClass());assertEquals("B",b.scope().licenseClass());
    }
    @Test void changingMarkedScoreOrMembershipInvalidatesPaginationBeforePrivateMedia() {
        UUID id=mark(1);String first=service.overview("B",bank.bankVersion()).datasetVersion();
        stats.put(id,new CompletedStats(1,0));
        var error=assertThrows(BusinessException.class,()->page(null,1,12,first));
        assertEquals("SIMULATION_BOOKMARKS_CHANGED",error.getErrorCode());verifyNoInteractions(thumbnails);
        String next=service.overview("B",bank.bankVersion()).datasetVersion();marks.clear();
        assertEquals("SIMULATION_BOOKMARKS_CHANGED",assertThrows(BusinessException.class,()->page(null,0,12,next)).getErrorCode());
    }
    @Test void freshImageExpiryDoesNotChangeDatasetAndEmptyIsValid() {
        assertEquals(0,page(null,0,12,null).totalBookmarked());mark(1);
        var a=page(null,0,12,null);var b=page(null,0,12,a.datasetVersion());
        assertEquals(a.datasetVersion(),b.datasetVersion());assertEquals(1,b.items().size());
    }
    @Test void missingMediaOrUnpublishedBankIsAnErrorNotSuccessfulEmptyData() {
        mark(1);doReturn(Map.of()).when(thumbnails).resolve(any(),any(),any());
        assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE",assertThrows(BusinessException.class,()->page(null,0,12,null)).getErrorCode());
        doThrow(SimulationFailure.notFound()).when(banks).load(any());
        assertEquals("SIMULATION_NOT_FOUND",assertThrows(BusinessException.class,()->service.overview("B",null)).getErrorCode());
    }
    @Test void invalidQueriesAndUnsupportedLicenseFailClearly() {
        for(int chapter:new int[]{0,7})assertThrows(BusinessException.class,()->page(chapter,0,12,null));
        assertThrows(BusinessException.class,()->service.list("B",null,null,"BEST_SCORE_ASC",0,12,null));
        assertThrows(BusinessException.class,()->page(null,-1,12,null));
        assertThrows(BusinessException.class,()->page(null,10001,12,null));
        assertThrows(BusinessException.class,()->page(null,0,0,null));
        assertThrows(BusinessException.class,()->page(null,0,13,null));
        assertThrows(BusinessException.class,()->page(null,0,12,"bad"));
        assertEquals("SIMULATION_LICENSE_NOT_SUPPORTED",assertThrows(BusinessException.class,()->service.overview("A1",null)).getErrorCode());
        verifyNoInteractions(thumbnails);
    }
}
