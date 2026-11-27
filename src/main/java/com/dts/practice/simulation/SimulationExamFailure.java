package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public class SimulationExamFailure extends BusinessException {
    public final HttpStatus status;
    public final String code;
    public final UUID activeSessionId;

    public SimulationExamFailure(HttpStatus status, String code, String message, UUID activeSessionId) {
        super(code, message, status);
        this.status = status;
        this.code = code;
        this.activeSessionId = activeSessionId;
    }

    public static SimulationExamFailure invalid(String message) {
        return new SimulationExamFailure(HttpStatus.BAD_REQUEST, "SIMULATION_EXAM_INVALID_REQUEST", message, null);
    }

    public static SimulationExamFailure conflict(String code, String message) {
        return new SimulationExamFailure(HttpStatus.CONFLICT, code, message, null);
    }

    public static SimulationExamFailure active(UUID id) {
        return new SimulationExamFailure(HttpStatus.CONFLICT, "ACTIVE_SESSION_EXISTS", "Continue or cancel the existing simulation exam", id);
    }

    public static SimulationExamFailure missing() {
        return new SimulationExamFailure(HttpStatus.NOT_FOUND, "SIMULATION_EXAM_NOT_FOUND", "Simulation exam not found", null);
    }

    public static SimulationExamFailure unavailable() {
        return new SimulationExamFailure(HttpStatus.SERVICE_UNAVAILABLE, "SIMULATION_EXAM_UNAVAILABLE", "Simulation exam service temporarily unavailable", null);
    }
}

