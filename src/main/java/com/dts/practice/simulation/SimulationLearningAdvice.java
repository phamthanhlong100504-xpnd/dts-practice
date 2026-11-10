package com.dts.practice.simulation;
import com.dts.practice.dto.response.ErrorResponse;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
@RestControllerAdvice(assignableTypes=SimulationLearningController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimulationLearningAdvice {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> binding() {return ResponseEntity.badRequest().body(ErrorResponse.of("SIMULATION_INVALID_REQUEST","Invalid path or query value",(String)null));}
}
