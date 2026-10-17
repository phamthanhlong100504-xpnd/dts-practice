package com.dts.practice.dto.response;

import java.time.Instant;

public record ChapterLearnedAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        boolean isCorrect,
        Instant learnedAt,
        Instant lastAnsweredAt
) {}