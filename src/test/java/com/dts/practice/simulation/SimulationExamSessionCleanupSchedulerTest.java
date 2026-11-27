package com.dts.practice.simulation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationExamSessionCleanupSchedulerTest {

    @Mock
    private SimulationExamRepository repo;

    @InjectMocks
    private SimulationExamSessionCleanupScheduler scheduler;

    @Test
    @DisplayName("cleanupStaleSessions should call repo.cancelStaleSessions with threshold 2 hours ago")
    void testCleanupStaleSessionsSuccess() {
        ReflectionTestUtils.setField(scheduler, "sessionTimeoutHours", 2L);
        when(repo.cancelStaleSessions(any(Instant.class))).thenReturn(3);

        Instant before = Instant.now().minus(Duration.ofHours(2)).minusSeconds(1);
        scheduler.cleanupStaleSessions();
        Instant after = Instant.now().minus(Duration.ofHours(2)).plusSeconds(1);

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(repo, times(1)).cancelStaleSessions(captor.capture());

        Instant threshold = captor.getValue();
        assertTrue(!threshold.isBefore(before) && !threshold.isAfter(after),
                "Threshold should be approximately 2 hours ago");
    }

    @Test
    @DisplayName("cleanupStaleSessions should handle repository exceptions gracefully without rethrowing")
    void testCleanupStaleSessionsHandlesException() {
        ReflectionTestUtils.setField(scheduler, "sessionTimeoutHours", 2L);
        when(repo.cancelStaleSessions(any(Instant.class))).thenThrow(new RuntimeException("DB Connection timeout"));

        // Should not throw
        scheduler.cleanupStaleSessions();

        verify(repo, times(1)).cancelStaleSessions(any(Instant.class));
    }
}
