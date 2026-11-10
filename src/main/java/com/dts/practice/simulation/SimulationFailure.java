package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.springframework.http.HttpStatus;

final class SimulationFailure {
    private SimulationFailure() {}
    static BusinessException unavailable() {return BusinessException.of("SIMULATION_DEPENDENCY_UNAVAILABLE","Simulation content is temporarily unavailable",HttpStatus.SERVICE_UNAVAILABLE);}
    static BusinessException invalid(String message) {return BusinessException.of("SIMULATION_INVALID_REQUEST",message,HttpStatus.BAD_REQUEST);}
    static BusinessException notFound() {return BusinessException.of("SIMULATION_NOT_FOUND","Published simulation content not found",HttpStatus.NOT_FOUND);}
    static BusinessException unauthorized() {return BusinessException.of("SIMULATION_UNAUTHORIZED","A valid access token is required",HttpStatus.UNAUTHORIZED);}
    static BusinessException forbidden() {return BusinessException.of("SIMULATION_FORBIDDEN","Simulation permission required",HttpStatus.FORBIDDEN);}
}
