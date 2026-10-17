package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitCriticalAnswerRequest(
        @NotBlank String licenseClass,
        @NotNull UUID sessionId,
        @NotNull Integer questionId,
        @NotBlank String selectedAnswer
) {}