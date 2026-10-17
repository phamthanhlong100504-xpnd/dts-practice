package com.dts.practice.service;

import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.dto.response.PresetExamSetListResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.PresetExamAttemptRepository;
import com.dts.practice.repository.PresetExamSetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresetExamSetServiceTest {

    private static final String BANK = "dts-2026-600-v1";

    @Mock private PresetExamSetRepository presetExamSetRepository;
    @Mock private PresetExamAttemptRepository presetExamAttemptRepository;
    @Mock private PresetExamSetValidator presetExamSetValidator;
    @Mock private ExamRepository examRepository;
    @Mock private ExamAnswerRepository examAnswerRepository;
    @Mock private ExamService examService;

    @InjectMocks private PresetExamSetService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID setId = UUID.randomUUID();

    @Test
    void emptyCatalogHasNoAttemptsAndTotalZero() {
        when(presetExamSetRepository
                .findByLicenseClassAndBankVersionAndStatusOrderByDisplayOrderAscCodeAsc(
                        "A1", BANK, PresetExamSetStatus.PUBLISHED
                )).thenReturn(List.of());

        PresetExamSetListResponse response =
                service.getPublishedSets(userId, " a1 ");

        assertEquals("A1", response.licenseClass());
        assertEquals(0, response.total());
        assertTrue(response.items().isEmpty());
        verifyNoInteractions(presetExamAttemptRepository, examService);
    }

    @Test
    void neverStartedHasNullLatestAttempt() {
        givenPublishedSet(List.of());

        PresetExamSetListResponse response =
                service.getPublishedSets(userId, "A1");
        PresetExamSetListResponse.Item item = response.items().get(0);

        assertEquals(1, response.total());
        assertEquals(response.items().size(), response.total());
        assertEquals("01", item.code());
        assertEquals("NOT_STARTED", item.userStatus());
        assertEquals(0, item.attemptCount());
        assertNull(item.latestAttempt());
        verifyNoInteractions(examService);
    }

    @Test
    void pausedAttemptIsResumableInCatalogWithoutBeingGraded() {
        Exam paused = attempt(
                ExamStatus.PAUSED,
                0,
                Instant.now().minusSeconds(60),
                null
        );
        givenPublishedSet(List.of(paused));

        PresetExamSetListResponse.Item item =
                service.getPublishedSets(userId, "A1").items().get(0);

        assertEquals("IN_PROGRESS", item.userStatus());
        assertEquals(1, item.attemptCount());
        assertEquals(paused.getId(), item.latestAttempt().sessionId());
        assertEquals("PAUSED", item.latestAttempt().status());
        assertNull(item.latestAttempt().passed());
        assertNull(item.latestAttempt().completedAt());
        verifyNoInteractions(examService);
    }

    @Test
    void unexpiredAttemptRemainsInProgressWithoutResultLookup() {
        Exam running = attempt(
                ExamStatus.IN_PROGRESS,
                0,
                Instant.now().minusSeconds(60),
                Instant.now().plusSeconds(600)
        );
        givenPublishedSet(List.of(running));

        PresetExamSetListResponse.Item item =
                service.getPublishedSets(userId, "A1").items().get(0);

        assertEquals("IN_PROGRESS", item.userStatus());
        assertEquals("IN_PROGRESS", item.latestAttempt().status());
        assertEquals(running.getId(), item.latestAttempt().sessionId());
        assertNull(item.latestAttempt().passed());
        verifyNoInteractions(examService);
    }

    @Test
    void latestFailedAttemptOverridesEarlierPass() {
        Instant completedAt = Instant.now().minusSeconds(30);
        Exam latest = attempt(
                ExamStatus.COMPLETED,
                10,
                Instant.now().minusSeconds(600),
                null
        );
        latest.setCompletedAt(completedAt);

        Exam olderPassed = attempt(
                ExamStatus.COMPLETED,
                22,
                Instant.now().minusSeconds(3600),
                null
        );

        givenPublishedSet(List.of(latest, olderPassed));
        givenResult(latest, false, 10, completedAt);

        PresetExamSetListResponse.Item item =
                service.getPublishedSets(userId, "A1").items().get(0);

        assertEquals("FAILED", item.userStatus());
        assertEquals(2, item.attemptCount());
        assertEquals(latest.getId(), item.latestAttempt().sessionId());
        assertEquals(10, item.latestAttempt().correctCount());
        assertEquals(Boolean.FALSE, item.latestAttempt().passed());
        verify(examService).getExamResult(latest.getId(), userId);
        verify(examService, never())
                .getExamResult(olderPassed.getId(), userId);
    }

    @Test
    void criticalFailureUsesCanonicalResultInsteadOfRawCorrectCount() {
        Exam latest = attempt(
                ExamStatus.COMPLETED,
                24,
                Instant.now().minusSeconds(600),
                null
        );
        Instant completedAt = Instant.now().minusSeconds(30);

        givenPublishedSet(List.of(latest));
        givenResult(latest, false, 24, completedAt);

        PresetExamSetListResponse.Item item =
                service.getPublishedSets(userId, "A1").items().get(0);

        assertEquals("FAILED", item.userStatus());
        assertEquals(24, item.latestAttempt().correctCount());
        assertEquals(Boolean.FALSE, item.latestAttempt().passed());
    }

    @Test
    void completedPassIsShownAsPassed() {
        Exam latest = attempt(
                ExamStatus.COMPLETED,
                22,
                Instant.now().minusSeconds(600),
                null
        );
        Instant completedAt = Instant.now().minusSeconds(30);

        givenPublishedSet(List.of(latest));
        givenResult(latest, true, 22, completedAt);

        PresetExamSetListResponse.Item item =
                service.getPublishedSets(userId, "A1").items().get(0);

        assertEquals("PASSED", item.userStatus());
        assertEquals(Boolean.TRUE, item.latestAttempt().passed());
        assertEquals(completedAt, item.latestAttempt().completedAt());
    }

    @Test
    void expiredAttemptIsSynchronizedBeforeCatalogStatus() {
        Exam expired = attempt(
                ExamStatus.IN_PROGRESS,
                0,
                Instant.now().minusSeconds(1800),
                Instant.now().minusSeconds(60)
        );
        Instant completedAt = Instant.now();

        givenPublishedSet(List.of(expired));

        ExamSessionResponse session = mock(ExamSessionResponse.class);
        when(session.status()).thenReturn("TIMEOUT");
        when(examService.getExamSession(expired.getId(), userId))
                .thenReturn(session);
        givenResult(expired, false, 3, completedAt);

        PresetExamSetListResponse.Item item =
                service.getPublishedSets(userId, "A1").items().get(0);

        assertEquals("FAILED", item.userStatus());
        assertEquals("TIMEOUT", item.latestAttempt().status());
        assertEquals(3, item.latestAttempt().correctCount());
        assertEquals(Boolean.FALSE, item.latestAttempt().passed());
        assertEquals(completedAt, item.latestAttempt().completedAt());
        verify(examService).getExamSession(expired.getId(), userId);
        verify(examService).getExamResult(expired.getId(), userId);
    }

    private Exam attempt(
            ExamStatus status,
            int correctCount,
            Instant startedAt,
            Instant expiresAt
    ) {
        return Exam.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .presetExamSetId(setId)
                .examType("A1")
                .bankVersion(BANK)
                .status(status)
                .correctCount(correctCount)
                .totalQuestions(25)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build();
    }

    private void givenPublishedSet(List<Exam> attempts) {
        PresetExamSet set = mock(PresetExamSet.class);
        when(set.getId()).thenReturn(setId);
        when(set.getCode()).thenReturn("01");
        when(set.getTitle()).thenReturn("Đề A1 01");
        when(set.getLicenseClass()).thenReturn("A1");
        when(set.getTotalQuestions()).thenReturn(25);
        when(set.getDurationMinutes()).thenReturn(19);
        when(set.getStatus()).thenReturn(PresetExamSetStatus.PUBLISHED);
        when(set.getDisplayOrder()).thenReturn(1);

        when(presetExamSetRepository
                .findByLicenseClassAndBankVersionAndStatusOrderByDisplayOrderAscCodeAsc(
                        "A1", BANK, PresetExamSetStatus.PUBLISHED
                )).thenReturn(List.of(set));

        when(presetExamAttemptRepository
                .findByUserIdAndPresetExamSetIdInAndBankVersionOrderByStartedAtDesc(
                        eq(userId),
                        eq(List.of(setId)),
                        eq(BANK)
                )).thenReturn(attempts);
    }

    private void givenResult(
            Exam exam,
            boolean passed,
            int correctCount,
            Instant completedAt
    ) {
        ExamResultResponse result = mock(ExamResultResponse.class);
        when(result.passed()).thenReturn(passed);
        when(result.correctCount()).thenReturn(correctCount);
        when(result.completedAt()).thenReturn(completedAt);
        when(examService.getExamResult(exam.getId(), userId))
                .thenReturn(result);
    }
}