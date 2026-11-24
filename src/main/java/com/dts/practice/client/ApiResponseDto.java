package com.dts.practice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiResponseDto<T>(
        Boolean success,
        String message,
        T data
) {}
