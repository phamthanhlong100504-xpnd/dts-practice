package com.dts.practice.dto.response;

public record QuestionResponse(
        Integer id,
        Integer chapter,
        String questionText,
        Object options,
        Boolean isCritical,
        String imageUrl,
        String correctAnswer,
        String explanation
) {}
