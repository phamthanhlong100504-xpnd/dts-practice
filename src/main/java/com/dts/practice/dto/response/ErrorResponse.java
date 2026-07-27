package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details,
        String traceId,
        Instant timestamp
) {
    public static ErrorResponse of(String code, String message, String traceId) {
        return new ErrorResponse(code, message, List.of(), traceId, Instant.now());
    }

    public static ErrorResponse of(String code, String message, List<String> details, String traceId) {
        return new ErrorResponse(code, message, details, traceId, Instant.now());
    }
}
