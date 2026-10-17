package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitChapterReviewAnswerRequest(
        @NotNull Integer questionId,
        @NotBlank String selectedAnswer
) {}