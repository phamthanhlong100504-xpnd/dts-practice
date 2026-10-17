package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCriticalProgressRequest(
        @NotBlank String licenseClass,
        @NotNull Boolean learned
) {}