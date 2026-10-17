package com.dts.practice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ActiveExamResponse(
        UUID examId,
        String examType,
        String mode,
        String status,
        Integer totalQuestions,
        Integer answeredCount,
        Integer durationMinutes,
        Instant startedAt,
        Instant expiresAt,
        Long remainingTimeMs,
        Integer currentQuestionId
) {
    // Giữ tương thích với chỗ nào còn dùng constructor cũ.
    public ActiveExamResponse(
            UUID examId,
            String examType,
            String mode,
            String status,
            Integer totalQuestions,
            Integer answeredCount,
            Integer durationMinutes,
            Instant startedAt,
            Instant expiresAt
    ) {
        this(
                examId,
                examType,
                mode,
                status,
                totalQuestions,
                answeredCount,
                durationMinutes,
                startedAt,
                expiresAt,
                null,
                null
        );
    }

    @JsonProperty("serverTime")
    public Instant serverTime() {
        return Instant.now();
    }
}