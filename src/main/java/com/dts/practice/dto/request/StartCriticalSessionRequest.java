package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StartCriticalSessionRequest(
        @NotBlank String licenseClass,
        @NotBlank String mode,
        Boolean restart
) {
    public StartCriticalSessionRequest(String licenseClass, String mode) {
        this(licenseClass, mode, false);
    }
}