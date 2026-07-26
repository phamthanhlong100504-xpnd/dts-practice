package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ExamSessionResponse(
        UUID examId,
        String examType,
        String status,
        Integer totalQuestions,
        Integer answeredCount,
        List<QuestionResponse> questions,
        Instant startedAt
) {}
