package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExamResultResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer score,
        Boolean passed,
        List<AnswerDetailResponse> answers,
        Instant startedAt,
        Instant completedAt
) {}

record AnswerDetailResponse(
        Integer questionId,
        String questionText,
        Object options,
        String correctAnswer,
        String selectedAnswer,
        Boolean isCorrect,
        String explanation
) {}
