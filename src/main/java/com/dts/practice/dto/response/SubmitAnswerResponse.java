package com.dts.practice.dto.response;

public record SubmitAnswerResponse(
        String status,
        Boolean isCorrect,
        String correctAnswer,
        String explanation
) {}
