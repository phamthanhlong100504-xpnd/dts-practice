package com.dts.practice.dto.response;

import java.time.Instant;

public record ChapterLearningCursorResponse(
        int chapterId,
        Integer lastQuestionId,
        Integer resumeQuestionId,
        Instant lastStudiedAt
) {}