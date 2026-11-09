package com.dts.practice.simulation;

import org.springframework.http.HttpStatus;

final class SimulationExamFailure extends RuntimeException {
    final HttpStatus status;final String code;final java.util.UUID activeSessionId;
    SimulationExamFailure(HttpStatus status,String code,String message,java.util.UUID activeSessionId){super(message);this.status=status;this.code=code;this.activeSessionId=activeSessionId;}
    static SimulationExamFailure invalid(String message){return new SimulationExamFailure(HttpStatus.BAD_REQUEST,"SIMULATION_EXAM_INVALID_REQUEST",message,null);}
    static SimulationExamFailure conflict(String code,String message){return new SimulationExamFailure(HttpStatus.CONFLICT,code,message,null);}
    static SimulationExamFailure active(java.util.UUID id){return new SimulationExamFailure(HttpStatus.CONFLICT,"ACTIVE_SESSION_EXISTS","Continue or cancel the existing simulation exam",id);}
    static SimulationExamFailure missing(){return new SimulationExamFailure(HttpStatus.NOT_FOUND,"SIMULATION_EXAM_NOT_FOUND","Simulation exam not found",null);}
    static SimulationExamFailure unavailable(){return new SimulationExamFailure(HttpStatus.SERVICE_UNAVAILABLE,"SIMULATION_EXAM_UNAVAILABLE","Simulation exam service temporarily unavailable",null);}
}
