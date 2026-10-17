package com.dts.practice.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ActiveExamExistsHandler {

    @ExceptionHandler(ActiveExamExistsException.class)
    public ResponseEntity<ActiveExamConflictResponse> handle(
            ActiveExamExistsException exception
    ) {
        ActiveExamConflictResponse body = new ActiveExamConflictResponse(
                exception.getErrorCode(),
                exception.getMessage(),
                List.of(exception.getMessage()),
                MDC.get("traceId"),
                Instant.now(),
                exception.getActiveExamId(),
                exception.getActivePresetExamSetId(),
                exception.getActiveStatus()
        );

        return ResponseEntity.status(exception.getStatus()).body(body);
    }

    public record ActiveExamConflictResponse(
            String code,
            String message,
            List<String> details,
            String traceId,
            Instant timestamp,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            UUID activeExamId,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            UUID activePresetExamSetId,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            String status
    ) {}
}