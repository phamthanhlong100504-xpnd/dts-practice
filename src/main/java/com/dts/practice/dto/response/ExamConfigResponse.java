package com.dts.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ExamConfigResponse(
        String examType,
        Integer totalQuestions,
        Integer durationMinutes,
        @Schema(description = "Minimum number of correct answers required to pass")
        Integer passingScore,
        Integer criticalQuestionCount
) {}