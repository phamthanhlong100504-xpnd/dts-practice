package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.springframework.core.*;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.Instant;

@RestControllerAdvice(assignableTypes={SimulationLearningShortcutsController.class,SimulationExamShortcutsController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimulationShortcutAdvice {
    public record Error(boolean success,String code,String message,Instant timestamp) {}
    private ResponseEntity<Error> error(HttpStatus status,String code,String message){return ResponseEntity.status(status).cacheControl(CacheControl.noStore()).body(new Error(false,code,message,Instant.now()));}
    @ExceptionHandler(SimulationShortcutFailure.class)public ResponseEntity<Error> failure(SimulationShortcutFailure e){return error(e.status,e.code,e.getMessage());}
    @ExceptionHandler(SimulationExamFailure.class)public ResponseEntity<Error> exam(SimulationExamFailure e){return error(e.status,e.code,e.getMessage());}
    @ExceptionHandler(BusinessException.class)public ResponseEntity<Error> learning(BusinessException e){return error(e.getStatus(),e.getErrorCode(),e.getMessage());}
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,MissingServletRequestParameterException.class})public ResponseEntity<Error> invalid(){return failure(SimulationShortcutFailure.invalid());}
    @ExceptionHandler(DataAccessException.class)public ResponseEntity<Error> dependency(){return failure(SimulationShortcutFailure.unavailable());}
}
