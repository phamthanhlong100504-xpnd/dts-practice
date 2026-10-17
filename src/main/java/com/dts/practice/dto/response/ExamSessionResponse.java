package com.dts.practice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ExamSessionResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer answeredCount,
        Integer durationMinutes,
        Instant expiresAt,
        String mode,
        List<QuestionResponse> questions,
        Instant startedAt,
        @Schema(description = "Selected options keyed by question ID")
        Map<Integer, String> answers,
        @Schema(description = "Server-calculated remaining time; frozen while PAUSED")
        Long remainingTimeMs,
        @Schema(description = "Saved question cursor within this exam")
        Integer currentQuestionId,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Feedback for answered PRACTICE questions only; absent in EXAM mode")
        Map<Integer, ExamAnswerFeedbackResponse> answerFeedback,
        @Schema(description = "Server timestamp for synchronizing the countdown")
        Instant serverTime
) {
    public ExamSessionResponse(
            UUID examId,
            String examType,
            String status,
            Integer totalQuestions,
            Integer answeredCount,
            Integer durationMinutes,
            Instant expiresAt,
            String mode,
            List<QuestionResponse> questions,
            Instant startedAt,
            Map<Integer, String> answers,
            Long remainingTimeMs,
            Integer currentQuestionId
    ) {
        this(
                examId, examType, status, totalQuestions, answeredCount,
                durationMinutes, expiresAt, mode, questions, startedAt,
                answers, remainingTimeMs, currentQuestionId,
                Map.of(), Instant.now()
        );
    }

    public ExamSessionResponse(
            UUID examId,
            String examType,
            String status,
            Integer totalQuestions,
            Integer answeredCount,
            Integer durationMinutes,
            Instant expiresAt,
            String mode,
            List<QuestionResponse> questions,
            Instant startedAt,
            Map<Integer, String> answers,
            Long remainingTimeMs,
            Integer currentQuestionId,
            Map<Integer, ExamAnswerFeedbackResponse> answerFeedback
    ) {
        this(
                examId, examType, status, totalQuestions, answeredCount,
                durationMinutes, expiresAt, mode, questions, startedAt,
                answers, remainingTimeMs, currentQuestionId,
                answerFeedback, Instant.now()
        );
    }

    // Giữ tương thích với những chỗ đang gọi constructor cũ.
    public ExamSessionResponse(
            UUID examId,
            String examType,
            String status,
            Integer totalQuestions,
            Integer answeredCount,
            Integer durationMinutes,
            Instant expiresAt,
            String mode,
            List<QuestionResponse> questions,
            Instant startedAt,
            Map<Integer, String> answers
    ) {
        this(
                examId, examType, status, totalQuestions, answeredCount,
                durationMinutes, expiresAt, mode, questions, startedAt,
                answers, null, null
        );
    }
}