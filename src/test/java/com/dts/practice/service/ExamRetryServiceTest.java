package com.dts.practice.service;

import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.security.JwtUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamRetryServiceTest {

    private static final String BANK = "dts-2026-600-v1";

    @Mock private ExamRepository examRepository;
    @Mock private ExamAnswerRepository examAnswerRepository;
    @Mock private ExamService examService;

    @InjectMocks private ExamRetryService retryService;

    private JwtUserDetails user;
    private Exam source;
    private UUID newExamId;
    private List<ExamAnswer> savedAnswers;

    @BeforeEach
    void setUp() {
        user = new JwtUserDetails(UUID.randomUUID(), "testuser", List.of());
        newExamId = UUID.randomUUID();
        savedAnswers = new ArrayList<>();

        List<Integer> questionIds = new ArrayList<>();
        for (int id = 1; id <= 25; id++) {
            questionIds.add(id);
        }
        Collections.rotate(questionIds, 7);

        source = Exam.builder()
                .id(UUID.randomUUID())
                .userId(user.userId())
                .username(user.username())
                .fullName("Test User")
                .examType("A1")
                .bankVersion(BANK)
                .criticalQuestionId(questionIds.get(3))
                .presetExamSetId(UUID.randomUUID())
                .questionIds(List.copyOf(questionIds))
                .totalQuestions(25)
                .durationMinutes(19)
                .mode("PRACTICE")
                .status(ExamStatus.COMPLETED)
                .score(84)
                .correctCount(21)
                .wrongCount(4)
                .startedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .completedAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();
    }

    @Test
    void retryCopiesExactPresetOrderAndCreatesCleanAnswers() {
        stubSuccessfulRetry();

        Instant before = Instant.now();
        ExamSessionResponse response = retryService.retryExam(source.getId(), user);
        Instant after = Instant.now();

        ArgumentCaptor<Exam> examCaptor = ArgumentCaptor.forClass(Exam.class);
        verify(examRepository).saveAndFlush(examCaptor.capture());
        Exam retry = examCaptor.getValue();

        assertEquals(newExamId, response.examId());
        assertNotEquals(source.getId(), response.examId());
        assertEquals(source.getQuestionIds(), retry.getQuestionIds());
        assertEquals(source.getExamType(), retry.getExamType());
        assertEquals(source.getBankVersion(), retry.getBankVersion());
        assertEquals(source.getMode(), retry.getMode());
        assertEquals(source.getDurationMinutes(), retry.getDurationMinutes());
        assertEquals(source.getCriticalQuestionId(), retry.getCriticalQuestionId());
        assertEquals(source.getPresetExamSetId(), retry.getPresetExamSetId());
        assertEquals(source.getQuestionIds().get(0), retry.getCurrentQuestionId());
        assertEquals(ExamStatus.IN_PROGRESS, retry.getStatus());
        assertNull(retry.getScore());
        assertTrue(!retry.getStartedAt().isBefore(before)
                && !retry.getStartedAt().isAfter(after));
        assertEquals(retry.getStartedAt().plus(19, ChronoUnit.MINUTES),
                retry.getExpiresAt());

        assertEquals(source.getQuestionIds(), savedAnswers.stream()
                .map(ExamAnswer::getQuestionId).toList());
        assertTrue(savedAnswers.stream().allMatch(answer ->
                answer.getExam() == retry
                        && answer.getSelectedAnswer() == null
                        && answer.getIsCorrect() == null
                        && answer.getAnsweredAt() == null
                        && !answer.isFlagged()));
        verify(examAnswerRepository).saveAllAndFlush(anyList());
        verify(examService).getExamSession(newExamId, user.userId());
        assertTrue(response.answers().isEmpty());
    }

    @Test
    void timeoutExamCanAlsoBeRetried() {
        source.setStatus(ExamStatus.TIMEOUT);
        stubSuccessfulRetry();

        ExamSessionResponse response = retryService.retryExam(source.getId(), user);

        assertEquals(newExamId, response.examId());
        verify(examRepository).saveAndFlush(any(Exam.class));
        verify(examAnswerRepository).saveAllAndFlush(anyList());
    }

    @Test
    void randomExamRetryKeepsItsOwnQuestionsAndExamMode() {
        source.setPresetExamSetId(null);
        source.setMode("EXAM");
        stubSuccessfulRetry();

        ExamSessionResponse response = retryService.retryExam(source.getId(), user);

        ArgumentCaptor<Exam> captor = ArgumentCaptor.forClass(Exam.class);
        verify(examRepository).saveAndFlush(captor.capture());
        assertNull(captor.getValue().getPresetExamSetId());
        assertEquals("EXAM", captor.getValue().getMode());
        assertEquals("EXAM", response.mode());
        assertEquals(source.getQuestionIds(), captor.getValue().getQuestionIds());
    }

    @Test
    void anotherUserCannotRetrySource() {
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.empty());

        BusinessException error = assertThrows(BusinessException.class,
                () -> retryService.retryExam(source.getId(), user));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatus());
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
        verifyNoInteractions(examAnswerRepository, examService);
    }

    @Test
    void pausedExamCannotBeRetried() {
        source.setStatus(ExamStatus.PAUSED);
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.of(source));

        BusinessException error = assertThrows(BusinessException.class,
                () -> retryService.retryExam(source.getId(), user));

        assertEquals("EXAM_NOT_FINISHED", error.getErrorCode());
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
    }

    @Test
    void legacyBankCannotBeRetried() {
        source.setBankVersion("legacy-v1");
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.of(source));

        BusinessException error = assertThrows(BusinessException.class,
                () -> retryService.retryExam(source.getId(), user));

        assertEquals("RETRY_BANK_UNAVAILABLE", error.getErrorCode());
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
    }

    @Test
    void activeExamForSameLicenseBlocksRetry() {
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.of(source));
        when(examRepository.existsByUserIdAndExamTypeAndStatusIn(
                user.userId(), "A1",
                List.of(ExamStatus.IN_PROGRESS, ExamStatus.PAUSED)
        )).thenReturn(true);

        BusinessException error = assertThrows(BusinessException.class,
                () -> retryService.retryExam(source.getId(), user));

        assertEquals("ACTIVE_EXAM_EXISTS", error.getErrorCode());
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
        verifyNoInteractions(examAnswerRepository, examService);
    }

    @Test
    void databaseUniqueIndexAlsoBlocksConcurrentRetry() {
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.of(source));
        when(examRepository.saveAndFlush(any(Exam.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate key uq_exams_one_active_per_user_license"
                ));

        BusinessException error = assertThrows(BusinessException.class,
                () -> retryService.retryExam(source.getId(), user));

        assertEquals("ACTIVE_EXAM_EXISTS", error.getErrorCode());
        verifyNoInteractions(examAnswerRepository, examService);
    }

    @Test
    void corruptOriginalQuestionIdsAreRejected() {
        source.setQuestionIds(List.of(1, 1));
        source.setTotalQuestions(2);
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.of(source));

        BusinessException error = assertThrows(BusinessException.class,
                () -> retryService.retryExam(source.getId(), user));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(examRepository, never()).saveAndFlush(any(Exam.class));
    }

    private void stubSuccessfulRetry() {
        when(examRepository.findByIdAndUserId(source.getId(), user.userId()))
                .thenReturn(Optional.of(source));
        when(examRepository.saveAndFlush(any(Exam.class)))
                .thenAnswer(invocation -> {
                    Exam retry = invocation.getArgument(0);
                    retry.setId(newExamId);
                    return retry;
                });
        when(examAnswerRepository.saveAllAndFlush(any()))
                .thenAnswer(invocation -> {
                    Iterable<ExamAnswer> rows = invocation.getArgument(0);
                    rows.forEach(savedAnswers::add);
                    return savedAnswers;
                });
        when(examService.getExamSession(newExamId, user.userId()))
                .thenAnswer(invocation -> {
                    Instant now = Instant.now();
                    return new ExamSessionResponse(
                            newExamId, "A1", "IN_PROGRESS", 25, 0, 19,
                            now.plus(19, ChronoUnit.MINUTES), source.getMode(),
                            List.of(), now, Map.of(), 1_140_000L,
                            source.getQuestionIds().get(0)
                    );
                });
    }

}