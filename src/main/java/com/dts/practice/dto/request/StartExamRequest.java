package com.dts.practice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StartExamRequest(
        @NotBlank String examType,
        @Min(1) @Max(60) Integer totalQuestions,
        @Min(5) @Max(120) Integer durationMinutes,
        String mode
) {}
