package com.dts.practice.simulation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class SimulationReviewModel {
    private SimulationReviewModel() {}

    public record CompletedStats(int completedAttemptCount, int bestScore) {}
    public record ReviewChapter(int chapterId, int chapterNumber, String chapterName, int needReviewCount) {}
    public record ReviewOverview(SimulationLearningReads.Scope scope, String scoringVersion,
                                 int practicedScenarios, int totalNeedReview,
                                 List<ReviewChapter> chapters, String datasetVersion) {}
    public record ReviewItem(UUID scenarioId, int scenarioNumber, int chapterId, int chapterNumber,
                             String chapterName, String title, int durationMs,
                             String thumbnailUrl, OffsetDateTime thumbnailExpiresAt,
                             int bestScore, int maxScore, boolean isPracticed,
                             boolean isBookmarked, int completedAttemptCount) {}
    public record Pagination(int number, int size, int totalElements, int totalPages) {}
    public record ReviewPage(SimulationLearningReads.Scope scope, String scoringVersion,
                             int practicedScenarios, int totalNeedReview, List<ReviewChapter> chapters,
                             Integer chapterId, String sort, int filteredCount, List<ReviewItem> items,
                             Pagination page, String datasetVersion) {}
    public record ReplayPolicy(boolean allowPause, boolean allowSeek, boolean allowFullscreen) {}
    public record Replay(UUID attemptId, UUID scenarioId, String bankVersion, String scoringVersion,
                         String videoUrl, OffsetDateTime expiresAt, String mimeType, int durationMs,
                         boolean readOnly, ReplayPolicy policy) {}
}
