package com.dts.practice.simulation;

import com.dts.practice.exception.BusinessException;
import org.springframework.http.HttpStatus;
import java.util.Locale;
import java.util.Set;

public final class SimulationLicensePolicy {
    private static final Set<String> SUPPORTED=Set.of("B","C1","C","D1","D2","D","BE","C1E","CE","D1E","D2E","DE");
    private SimulationLicensePolicy() {}
    public static boolean enabled(String code) {return code!=null && SUPPORTED.contains(code);}
    static String require(String code) {
        if(code==null || code.isBlank()) throw SimulationFailure.invalid("licenseClass is required");
        String value=code.strip().toUpperCase(Locale.ROOT);
        if(!SUPPORTED.contains(value)) throw BusinessException.of("SIMULATION_LICENSE_NOT_SUPPORTED","Simulation is not enabled for this license class",HttpStatus.FORBIDDEN);
        return value;
    }
}
