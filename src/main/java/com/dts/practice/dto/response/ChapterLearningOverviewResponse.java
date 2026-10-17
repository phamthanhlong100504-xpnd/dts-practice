package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;

public record ChapterLearningOverviewResponse(
        String licenseClass,
        String bankVersion,
        int totalQuestions,
        int learnedQuestions,
        int completionPercent,
        List<Chapter> chapters
) {
    public record Chapter(
            int chapterId,
            int totalQuestions,
            int learnedQuestions,
            int completionPercent,
            String status,
            Integer lastQuestionId,
            Integer resumeQuestionId,
            Instant lastStudiedAt
    ) {}
}