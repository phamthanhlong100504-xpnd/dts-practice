package com.dts.practice.service;

import com.dts.practice.dto.request.StartPresetExamRequest;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.exception.ActiveExamExistsException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.PresetExamAttemptRepository;
import com.dts.practice.repository.PresetExamSetRepository;
import com.dts.practice.security.JwtUserDetails;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresetExamStartServiceTest {

    private static final String BANK = "dts-2026-600-v1";

    @Mock private PresetExamSetRepository presetExamSetRepository;
    @Mock private PresetExamAttemptRepository presetExamAttemptRepository;
    @Mock private PresetExamSetValidator presetExamSetValidator;
    @Mock private ExamRepository examRepository;
    @Mock private ExamAnswerRepository examAnswerRepository;
    @Mock private ExamService examService;
    @Mock private EntityManager entityManager;
    @Mock private Session hibernateSession;

    @InjectMocks private PresetExamSetService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID setId = UUID.randomUUID();
    private final JwtUserDetails user =
            new JwtUserDetails(userId, "testuser", List.of());

    @BeforeEach
    void injectEntityManager() {
        ReflectionTestUtils.setField(service, "entityManager", entityManager);
    }

    @Test
    void repeatedPostForSameRunningPresetReturnsSameSession() {
        PresetExamSet set = givenPublishedSet();
        Exam active = existingAttempt(ExamStatus.IN_PROGRESS);
        givenSameSetAttempts(set, List.of(active));
        ExamSessionResponse current =
                givenSession(active, "IN_PROGRESS", "EXAM");

        ExamSessionResponse first =
                service.startSession(user, setId, null);
        ExamSessionResponse second =
                service.startSession(user, setId, null);

        assertSame(current, first);
        assertSame(current, second);
        verify(hibernateSession, times(2)).doWork(any(Work.class));
        verify(examService, times(2))
                .getExamSession(active.getId(), userId);
        verify(examRepository, never())
                .saveAndFlush(any(Exam.class));
        verifyNoInteractions(
                presetExamSetValidator,
                examAnswerRepository
        );
    }

    @Test
    void pausedPresetReturnsPausedSessionWithoutNewAttempt() {
        PresetExamSet set = givenPublishedSet();
        Exam paused = existingAttempt(ExamStatus.PAUSED);
        givenSameSetAttempts(set, List.of(paused));
        ExamSessionResponse current =
                givenSession(paused, "PAUSED", "PRACTICE");

        StartPresetExamRequest request =
                mock(StartPresetExamRequest.class);
        when(request.mode()).thenReturn("PRACTICE");

        ExamSessionResponse response =
                service.startSession(user, setId, request);

        assertSame(current, response);
        verify(examRepository, never())
                .saveAndFlush(any(Exam.class));
        verifyNoInteractions(
                presetExamSetValidator,
                examAnswerRepository
        );
    }

    @Test
    void changingModeWhilePresetIsActiveReturnsConflict() {
        PresetExamSet set = givenPublishedSet();
        Exam active = existingAttempt(ExamStatus.IN_PROGRESS);
        when(active.getPresetExamSetId()).thenReturn(setId);
        givenSameSetAttempts(set, List.of(active));
        givenSession(active, "IN_PROGRESS", "EXAM");

        StartPresetExamRequest request =
                mock(StartPresetExamRequest.class);
        when(request.mode()).thenReturn("PRACTICE");

        ActiveExamExistsException error = assertThrows(
                ActiveExamExistsException.class,
                () -> service.startSession(user, setId, request)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("ACTIVE_EXAM_EXISTS", error.getErrorCode());
        assertEquals(active.getId(), error.getActiveExamId());
        assertEquals(setId, error.getActivePresetExamSetId());
        assertEquals("IN_PROGRESS", error.getActiveStatus());
        verify(examRepository, never())
                .saveAndFlush(any(Exam.class));
    }

    @Test
    void anotherActiveExamForTheLicenseReturnsConflict() {
        PresetExamSet set = givenPublishedSet();
        givenSameSetAttempts(set, List.of());

        UUID otherSetId = UUID.randomUUID();
        Exam other = Exam.builder()
                .id(UUID.randomUUID())
                .presetExamSetId(otherSetId)
                .build();

        when(examRepository
                .findFirstByUserIdAndExamTypeAndStatusInOrderByStartedAtDesc(
                        userId,
                        "A1",
                        List.of(
                                ExamStatus.IN_PROGRESS,
                                ExamStatus.PAUSED
                        )
                ))
                .thenReturn(Optional.of(other));

        ExamSessionResponse otherSession =
                mock(ExamSessionResponse.class);
        when(otherSession.status()).thenReturn("PAUSED");
        when(examService.getExamSession(other.getId(), userId))
                .thenReturn(otherSession);

        ActiveExamExistsException error = assertThrows(
                ActiveExamExistsException.class,
                () -> service.startSession(user, setId, null)
        );

        assertEquals("ACTIVE_EXAM_EXISTS", error.getErrorCode());
        assertEquals(other.getId(), error.getActiveExamId());
        assertEquals(
                otherSetId,
                error.getActivePresetExamSetId()
        );
        assertEquals("PAUSED", error.getActiveStatus());
        verify(examRepository, never())
                .saveAndFlush(any(Exam.class));
        verifyNoInteractions(
                presetExamSetValidator,
                examAnswerRepository
        );
    }

    @Test
    void activeRandomExamReturnsIdWithoutPresetId() {
        PresetExamSet set = givenPublishedSet();
        givenSameSetAttempts(set, List.of());

        Exam random = Exam.builder()
                .id(UUID.randomUUID())
                .build();

        when(examRepository
                .findFirstByUserIdAndExamTypeAndStatusInOrderByStartedAtDesc(
                        userId,
                        "A1",
                        List.of(
                                ExamStatus.IN_PROGRESS,
                                ExamStatus.PAUSED
                        )
                ))
                .thenReturn(Optional.of(random));

        ExamSessionResponse current =
                mock(ExamSessionResponse.class);
        when(current.status()).thenReturn("IN_PROGRESS");
        when(examService.getExamSession(random.getId(), userId))
                .thenReturn(current);

        ActiveExamExistsException error = assertThrows(
                ActiveExamExistsException.class,
                () -> service.startSession(user, setId, null)
        );

        assertEquals(random.getId(), error.getActiveExamId());
        assertNull(error.getActivePresetExamSetId());
        assertEquals("IN_PROGRESS", error.getActiveStatus());
        verify(examRepository, never())
                .saveAndFlush(any(Exam.class));
    }

    @Test
    void transactionLockExecutesPreparedStatement()
            throws Exception {
        PresetExamSet set = givenPublishedSet();
        Exam active = existingAttempt(ExamStatus.IN_PROGRESS);
        givenSameSetAttempts(set, List.of(active));
        givenSession(active, "IN_PROGRESS", "EXAM");

        Connection connection = mock(Connection.class);
        PreparedStatement statement =
                mock(PreparedStatement.class);

        when(connection.prepareStatement(
                "SELECT pg_advisory_xact_lock(?)"
        )).thenReturn(statement);

        doAnswer(invocation -> {
            Work work = invocation.getArgument(0);
            work.execute(connection);
            return null;
        }).when(hibernateSession).doWork(any(Work.class));

        service.startSession(user, setId, null);

        verify(statement).setLong(eq(1), anyLong());
        verify(statement).execute();
        verify(statement).close();
    }

    @Test
    void expiredAttemptCanStartNewCleanPresetSession() {
        PresetExamSet set = givenPublishedSet();
        Exam expired = existingAttempt(ExamStatus.IN_PROGRESS);
        givenSameSetAttempts(set, List.of(expired));

        ExamSessionResponse timedOut =
                mock(ExamSessionResponse.class);
        when(timedOut.status()).thenReturn("TIMEOUT");
        when(examService.getExamSession(
                expired.getId(),
                userId
        )).thenReturn(timedOut);

        List<Question> questions = new ArrayList<>();
        for (int id = 1; id <= 25; id++) {
            Question question = mock(Question.class);
            when(question.getId()).thenReturn(id);
            questions.add(question);
        }

        when(presetExamSetValidator.validate(set)).thenReturn(
                new PresetExamSetValidator.ValidatedPreset(
                        questions,
                        1
                )
        );
        when(set.getTotalQuestions()).thenReturn(25);
        when(set.getDurationMinutes()).thenReturn(19);

        UUID createdId = UUID.randomUUID();
        when(examRepository.saveAndFlush(any(Exam.class)))
                .thenAnswer(invocation -> {
                    Exam exam = invocation.getArgument(0);
                    exam.setId(createdId);
                    return exam;
                });

        ExamSessionResponse created =
                mock(ExamSessionResponse.class);
        when(examService.getExamSession(
                createdId,
                userId
        )).thenReturn(created);

        ExamSessionResponse response =
                service.startSession(user, setId, null);

        assertSame(created, response);

        ArgumentCaptor<Exam> captor =
                ArgumentCaptor.forClass(Exam.class);
        verify(examRepository).saveAndFlush(captor.capture());

        Exam saved = captor.getValue();
        assertEquals(setId, saved.getPresetExamSetId());
        assertEquals(25, saved.getQuestionIds().size());
        assertEquals(
                saved.getQuestionIds().get(0),
                saved.getCurrentQuestionId()
        );
        assertEquals(
                ExamStatus.IN_PROGRESS,
                saved.getStatus()
        );
        assertTrue(saved.getExpiresAt().isAfter(Instant.now()));

        verify(examAnswerRepository, times(25))
                .save(any(ExamAnswer.class));
    }

    private PresetExamSet givenPublishedSet() {
        PresetExamSet set = mock(PresetExamSet.class);
        when(set.getId()).thenReturn(setId);
        when(set.getStatus())
                .thenReturn(PresetExamSetStatus.PUBLISHED);
        when(set.getBankVersion()).thenReturn(BANK);
        when(set.getLicenseClass()).thenReturn("A1");
        when(presetExamSetRepository.findById(setId))
                .thenReturn(Optional.of(set));
        when(entityManager.unwrap(Session.class))
                .thenReturn(hibernateSession);
        return set;
    }

    private void givenSameSetAttempts(
            PresetExamSet set,
            List<Exam> attempts
    ) {
        when(presetExamAttemptRepository
                .findByUserIdAndPresetExamSetIdInAndBankVersionOrderByStartedAtDesc(
                        userId,
                        List.of(set.getId()),
                        BANK
                ))
                .thenReturn(attempts);
    }

    private Exam existingAttempt(ExamStatus status) {
        Exam attempt = mock(Exam.class);
        when(attempt.getStatus()).thenReturn(status);
        when(attempt.getId()).thenReturn(UUID.randomUUID());
        return attempt;
    }

    private ExamSessionResponse givenSession(
            Exam attempt,
            String status,
            String mode
    ) {
        ExamSessionResponse response =
                mock(ExamSessionResponse.class);
        when(response.status()).thenReturn(status);
        when(response.mode()).thenReturn(mode);
        when(examService.getExamSession(
                attempt.getId(),
                userId
        )).thenReturn(response);
        return response;
    }
}