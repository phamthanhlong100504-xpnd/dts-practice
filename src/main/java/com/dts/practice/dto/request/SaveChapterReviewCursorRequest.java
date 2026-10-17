package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotNull;

public record SaveChapterReviewCursorRequest(
        @NotNull Integer currentQuestionId
) {}