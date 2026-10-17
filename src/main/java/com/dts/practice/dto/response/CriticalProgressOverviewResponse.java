package com.dts.practice.dto.response;

import java.time.Instant;

public record CriticalProgressOverviewResponse(
        String licenseClass,
        int totalQuestions,
        int learnedQuestions,
        int completionPercent,
        int currentPosition,
        boolean hasProgress,
        boolean completed,
        Instant lastStudiedAt
) {}