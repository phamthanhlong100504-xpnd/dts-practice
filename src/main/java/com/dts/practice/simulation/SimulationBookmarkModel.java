package com.dts.practice.simulation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SimulationBookmarkModel {
    private SimulationBookmarkModel() {}

    public record BookmarkChapter(int chapterId, int chapterNumber, String chapterName, int bookmarkedCount) {}
    public record BookmarkOverview(SimulationLearningReads.Scope scope, String scoringVersion,
                                   int totalBookmarked, List<BookmarkChapter> chapters, String datasetVersion) {}
    public record BookmarkItem(UUID scenarioId, int scenarioNumber, int chapterId, int chapterNumber,
                               String chapterName, String title, int durationMs, String thumbnailUrl,
                               OffsetDateTime thumbnailExpiresAt, boolean isPracticed, Integer bestScore,
                               int maxScore, boolean isBookmarked, int completedAttemptCount) {}
    public record BookmarkPage(SimulationLearningReads.Scope scope, String scoringVersion,
                               int totalBookmarked, List<BookmarkChapter> chapters, Integer chapterId,
                               String sort, int filteredCount, List<BookmarkItem> items,
                               SimulationReviewModel.Pagination page, String datasetVersion) {}
}
