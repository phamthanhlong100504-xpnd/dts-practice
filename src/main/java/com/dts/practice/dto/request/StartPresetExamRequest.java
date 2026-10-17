package com.dts.practice.dto.request;

public record StartPresetExamRequest(
        String licenseClass,
        String mode,
        String fullName
) {}