package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LicenseCatalogItemResponse(
        String code,
        String group,
        String title,
        String description,
        String bankVersion,
        @Schema(description = "Actual number of applicable questions in the current bank")
        long totalStudyQuestions,
        @Schema(description = "Actual number of critical questions for this license in the current bank")
        long criticalStudyQuestions,
        int totalQuestions,
        int durationMinutes,
        int passingScore,
        int criticalQuestionCount,
        @Schema(description = "Study counts match the curriculum and exam quotas can be filled")
        boolean bankCountsMatch,
        @Schema(description = "The current bank has enough eligible questions to generate an exam")
        boolean examAvailable
) {}