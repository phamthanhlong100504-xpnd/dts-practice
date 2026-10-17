package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitWrongReviewAnswerRequest(
        @NotNull Integer questionId,
        @NotBlank String selectedAnswer
) {}