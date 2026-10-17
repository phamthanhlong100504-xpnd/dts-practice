package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Feedback for a question already answered in PRACTICE mode")
public record ExamAnswerFeedbackResponse(
        String selectedAnswer,
        Boolean isCorrect,
        String correctAnswer,
        String explanation
) {}