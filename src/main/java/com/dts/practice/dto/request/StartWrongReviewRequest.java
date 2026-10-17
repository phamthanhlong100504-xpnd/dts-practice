package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StartWrongReviewRequest(@NotBlank String licenseClass) {}