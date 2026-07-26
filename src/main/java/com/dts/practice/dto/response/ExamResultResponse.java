package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
        List<Map<String, Object>> answers,
        Instant startedAt,
        Instant completedAt
) {}
