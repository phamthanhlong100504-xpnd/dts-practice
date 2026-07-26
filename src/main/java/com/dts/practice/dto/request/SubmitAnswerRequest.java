package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SubmitAnswerRequest(
        @NotBlank String questionId,
        @NotBlank String selectedAnswer
) {}
