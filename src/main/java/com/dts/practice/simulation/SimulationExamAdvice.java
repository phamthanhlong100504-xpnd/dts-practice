package com.dts.practice.simulation;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;
import java.util.UUID;

@RestControllerAdvice(assignableTypes={SimulationExamController.class,SimulationPresetController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimulationExamAdvice {
    public record Error(boolean success,String code,String message,UUID activeSessionId,Instant timestamp) {}
    @ExceptionHandler(SimulationExamFailure.class)public ResponseEntity<Error> failure(SimulationExamFailure e){return ResponseEntity.status(e.status).body(new Error(false,e.code,e.getMessage(),e.activeSessionId,Instant.now()));}
    @ExceptionHandler({MethodArgumentTypeMismatchException.class,HttpMessageNotReadableException.class})public ResponseEntity<Error> binding(){return failure(SimulationExamFailure.invalid("Invalid path, query or request body"));}
}
