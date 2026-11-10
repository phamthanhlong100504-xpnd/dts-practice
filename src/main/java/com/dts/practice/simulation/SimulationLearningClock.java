package com.dts.practice.simulation;
import org.springframework.stereotype.Component;
import java.time.Instant;
@Component
public class SimulationLearningClock {Instant now() {return Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);}}
