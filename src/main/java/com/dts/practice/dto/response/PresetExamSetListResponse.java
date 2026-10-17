package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PresetExamSetListResponse(
        String licenseClass,
        int total,
        List<Item> items
) {
    public record Item(
            UUID id,
            String code,
            String title,
            String licenseClass,
            int totalQuestions,
            int durationSeconds,
            String status,
            int order,
            String userStatus,
            int attemptCount,
            LatestAttempt latestAttempt
    ) {}

    public record LatestAttempt(
            UUID sessionId,
            String status,
            Integer correctCount,
            Integer totalQuestions,
            Boolean passed,
            Instant startedAt,
            Instant completedAt
    ) {}
}