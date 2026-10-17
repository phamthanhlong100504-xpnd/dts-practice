package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecordChapterAnswerRequest(
        @NotBlank String licenseClass,
        @NotNull @Positive Integer questionId,
        @NotBlank String selectedAnswer
) {}