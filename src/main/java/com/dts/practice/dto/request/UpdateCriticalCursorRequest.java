package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCriticalCursorRequest(
        @NotNull Integer questionId
) {}