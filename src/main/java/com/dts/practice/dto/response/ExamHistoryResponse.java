package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ExamHistoryResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer score,
        Boolean passed,
        String mode,
        Integer durationMinutes,
        Instant startedAt,
        Instant completedAt
) {}
