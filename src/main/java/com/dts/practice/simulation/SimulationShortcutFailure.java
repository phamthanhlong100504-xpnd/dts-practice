package com.dts.practice.simulation;

import org.springframework.http.HttpStatus;
final class SimulationShortcutFailure extends RuntimeException {
    final HttpStatus status;final String code;
    SimulationShortcutFailure(HttpStatus status,String code,String message){super(message);this.status=status;this.code=code;}
    static SimulationShortcutFailure conflict(String code){return new SimulationShortcutFailure(HttpStatus.CONFLICT,"SIMULATION_SHORTCUT_"+code,code);}
    static SimulationShortcutFailure invalid(){return new SimulationShortcutFailure(HttpStatus.BAD_REQUEST,"SIMULATION_SHORTCUT_INVALID_REQUEST","Invalid shortcut request");}
    static SimulationShortcutFailure unavailable(){return new SimulationShortcutFailure(HttpStatus.SERVICE_UNAVAILABLE,"SIMULATION_SHORTCUT_UNAVAILABLE","Shortcut migration/configuration is not ready");}
}
