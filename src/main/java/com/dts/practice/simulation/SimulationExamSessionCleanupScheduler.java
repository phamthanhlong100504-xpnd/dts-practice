package com.dts.practice.simulation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Scheduled worker that scans simulation exam sessions in IN_PROGRESS or READY_TO_SUBMIT
 * and transitions stale sessions (abandoned beyond the configured timeout)
 * to CANCELLED state with cancelled_at = NOW(), adhering to PostgreSQL check constraints.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "simulation.exam.cleanup-enabled", havingValue = "true", matchIfMissing = true)
public class SimulationExamSessionCleanupScheduler {

    private final SimulationExamRepository repo;

    @Value("${simulation.exam.session-timeout-hours:2}")
    private long sessionTimeoutHours = 2;

    @Scheduled(fixedDelayString = "${simulation.exam.cleanup-poll-ms:300000}")
    public void cleanupStaleSessions() {
        try {
            Instant threshold = Instant.now().minus(Duration.ofHours(sessionTimeoutHours));
            int cleaned = repo.cancelStaleSessions(threshold);
            if (cleaned > 0) {
                log.info("Simulation exam auto-cleanup job cancelled {} stale session(s) older than {} hours (before {})",
                        cleaned, sessionTimeoutHours, threshold);
            }
        } catch (Exception e) {
            log.error("Failed to run simulation exam session auto-cleanup job", e);
        }
    }
}
