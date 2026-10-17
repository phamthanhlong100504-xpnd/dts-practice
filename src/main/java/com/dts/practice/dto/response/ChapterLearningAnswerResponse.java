package com.dts.practice.dto.response;

import java.time.Instant;

public record ChapterLearningAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        boolean isCorrect,
        String correctAnswer,
        String explanation,
        Instant answeredAt
) {}