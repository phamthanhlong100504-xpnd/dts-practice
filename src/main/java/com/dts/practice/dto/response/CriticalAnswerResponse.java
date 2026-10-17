package com.dts.practice.dto.response;

import java.time.Instant;

public record CriticalAnswerResponse(
        Integer questionId,
        String selectedAnswer,
        Boolean isCorrect,
        String correctAnswer,
        String explanation,
        Instant answeredAt
) {}