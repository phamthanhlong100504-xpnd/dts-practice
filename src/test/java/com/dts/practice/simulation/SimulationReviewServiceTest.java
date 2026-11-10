package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static com.dts.practice.simulation.SimulationReviewModel.*;

class SimulationReviewServiceTest {
    final SimulationBank.Snapshot bank = SimulationFixtures.published().snapshot();
    final SimulationAccess access = mock(SimulationAccess.class);
    final SimulationBankClient banks = mock(SimulationBankClient.class);
    final SimulationReviewRepository repo = mock(SimulationReviewRepository.class);
    final SimulationThumbnailBatch thumbnails = mock(SimulationThumbnailBatch.class);
    final Map<UUID, CompletedStats> stats = new HashMap<>();
    final Set<UUID> marks = new HashSet<>();
    final SimulationReviewService service = new SimulationReviewService(access, banks, repo, thumbnails);

    @BeforeEach void setup() {
        when(access.read(anyBoolean())).thenReturn(new SimulationAccess.Actor(SimulationFixtures.USER, "fixture"));
        when(banks.load(any())).thenReturn(bank);
        when(repo.completed(any(), any())).thenReturn(stats);
        when(repo.bookmarks(any(), any())).thenReturn(marks);
        when(thumbnails.resolve(any(), any(), any())).thenAnswer(call -> {
            List<SimulationBank.Scenario> selected = call.getArgument(2);
            var result = new HashMap<UUID, SimulationThumbnailClient.Thumbnail>();
            for (var s : selected) result.put(s.scenarioId(), new SimulationThumbnailClient.Thumbnail(
                    s.thumbnailMediaId(), s.scenarioId(), bank.bankVersion(),
                    "http://localhost/fixture.webp?signature=fixture", OffsetDateTime.now().plusSeconds(300)));
            return result;
        });
    }
    void stat(int n, int count, int best) {stats.put(bank.scenarios().get(n-1).scenarioId(), new CompletedStats(count, best));}
    ReviewPage page(int page, int size, String expected) {return service.list("B", bank.bankVersion(), null, "BEST_SCORE_ASC", page, size, expected);}

    @Test void overviewDoesNotRequirePrivateMediaOrCountUnknownPublishedIds() {
        stat(1, 4, 4); stat(2, 2, 5); stats.put(UUID.randomUUID(), new CompletedStats(8, 0));
        var overview = service.overview("B", bank.bankVersion());
        assertEquals(2, overview.practicedScenarios()); assertEquals(1, overview.totalNeedReview());
        assertEquals(6, overview.chapters().size());
        assertEquals(1, overview.chapters().stream().mapToInt(ReviewChapter::needReviewCount).sum());
        verify(access).read(false); verifyNoInteractions(thumbnails);
    }
    @Test void onlySelectedPageResolvesPrivateImagesAndMetadataMatchesItsBank() {
        for (int n=1; n<=29; n++) stat(n, 1, n%5);
        var page = page(1, 12, null);
        assertEquals(12, page.items().size()); assertEquals(29, page.filteredCount());
        assertTrue(page.items().stream().allMatch(i -> i.isPracticed() && i.maxScore()==5 && i.chapterName().equals("Chương 1")));
        verify(thumbnails).resolve(any(), eq(bank), argThat(rows -> rows.size()==12));
        verify(access).read(true);
    }
    @Test void noCompletedRowsAndAllMasteredHaveDifferentOverviewStates() {
        assertEquals(0, service.overview("B", bank.bankVersion()).practicedScenarios());
        stat(1, 3, 5);
        var overview = service.overview("B", bank.bankVersion());
        assertEquals(1, overview.practicedScenarios()); assertEquals(0, overview.totalNeedReview());
        assertTrue(page(0, 12, null).items().isEmpty());
    }
    @Test void expiredUrlRenewalDoesNotInvalidateTheDatasetButBookmarkChangesDo() {
        stat(1, 1, 0);
        var first = page(0, 12, null); var repeated = page(0, 12, first.datasetVersion());
        assertEquals(first.datasetVersion(), repeated.datasetVersion());
        marks.add(bank.scenarios().getFirst().scenarioId());
        var error = assertThrows(BusinessException.class, () -> page(0, 12, first.datasetVersion()));
        assertEquals("SIMULATION_REVIEW_CHANGED", error.getErrorCode());
        assertNotEquals(first.datasetVersion(), page(0, 12, null).datasetVersion());
    }
    @Test void staleDatasetRejectedBeforeRequestingPrivateImages() {
        stat(1, 1, 2);
        assertThrows(BusinessException.class, () -> page(0, 12, "a".repeat(64)));
        verifyNoInteractions(thumbnails);
    }
    @Test void missingOrFailedMediaIsAnErrorRatherThanSuccessfulEmptyData() {
        stat(1, 1, 0); doReturn(Map.of()).when(thumbnails).resolve(any(), any(), any());
        var error = assertThrows(BusinessException.class, () -> page(0, 12, null));
        assertEquals("SIMULATION_DEPENDENCY_UNAVAILABLE", error.getErrorCode());
    }
    @Test void summaryAndListVersionMatchAcrossSupportedLicenses() {
        stat(1, 2, 4);
        var overview = service.overview("CE", bank.bankVersion()); var page = page(0, 12, null);
        assertEquals(overview.datasetVersion(), page.datasetVersion());
        assertEquals("CE", overview.scope().licenseClass()); assertEquals("B", page.scope().licenseClass());
    }
    @Test void invalidParametersAndUnsupportedLicenseDoNotBecomeEmptySuccess() {
        assertThrows(BusinessException.class, () -> service.list("B", null, 7, "BEST_SCORE_ASC", 0, 12, null));
        assertThrows(BusinessException.class, () -> service.list("B", null, null, "UNKNOWN", 0, 12, null));
        assertThrows(BusinessException.class, () -> service.list("B", null, null, "BEST_SCORE_ASC", -1, 12, null));
        assertThrows(BusinessException.class, () -> service.list("B", null, null, "BEST_SCORE_ASC", 0, 13, null));
        assertThrows(BusinessException.class, () -> page(0, 12, "not-a-version"));
        assertEquals("SIMULATION_LICENSE_NOT_SUPPORTED", assertThrows(BusinessException.class,
                () -> service.overview("B1", null)).getErrorCode());
        verifyNoInteractions(thumbnails);
    }
}
