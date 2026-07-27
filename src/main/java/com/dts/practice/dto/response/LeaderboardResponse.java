package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record LeaderboardResponse(
        UUID userId,
        String username,
        String examType,
        Integer score,
        Integer correctCount,
        Integer totalQuestions,
        Instant completedAt
) {}
