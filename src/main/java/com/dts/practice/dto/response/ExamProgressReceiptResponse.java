package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ExamProgressReceiptResponse(
        UUID examId,
        String examType,
        String bankVersion,
        String mode,
        String status,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer answeredWrongCount,
        Integer unansweredCount,
        Integer score,
        Boolean passed,
        Integer durationSeconds,
        Instant startedAt,
        Instant completedAt
) {}