package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotNull;

public record PauseExamRequest(
        @NotNull Integer currentQuestionId
) {}