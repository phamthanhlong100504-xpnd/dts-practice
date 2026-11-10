package com.dts.practice.simulation;

import java.time.Instant;

/** Reuses the exact learner bank structural validator, without publishing content. */
public final class AdminPreparedBankValidator {
    private AdminPreparedBankValidator() {}
    public static void validate(SimulationBank.Snapshot bank,String version) {
        SimulationBank.version(version);
        SimulationBank.validate(new SimulationBank.Published(bank,"PUBLISHED",true,Instant.now()),version);
    }
}
