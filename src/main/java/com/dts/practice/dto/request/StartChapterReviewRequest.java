package com.dts.practice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record StartChapterReviewRequest(
        @NotBlank String licenseClass
) {}