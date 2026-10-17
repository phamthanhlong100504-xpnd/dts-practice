package com.dts.practice.service;

import com.dts.practice.dto.request.StartExamRequest;
import com.dts.practice.dto.request.SubmitAnswerRequest;
import com.dts.practice.dto.response.ExamHistoryResponse;
import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.dto.response.SubmitAnswerResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.mapper.ExamMapper;
import com.dts.practice.mapper.QuestionMapper;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.QuestionRepository;
import com.dts.practice.security.JwtUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    private static final String BANK = "dts-2026-600-v1";

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamAnswerRepository examAnswerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private ExamMapper examMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ExamProgressReceiptService examProgressReceiptService;

    @Mock
    private WrongQuestionProgressService wrongQuestionProgressService;

    @InjectMocks
    private ExamService examService;

    private JwtUserDetails userDetails;
    private Exam exam;

    @BeforeEach
    void setUp() {
        userDetails = new JwtUserDetails(
                UUID.randomUUID(),
                "testuser",
                List.of()
        );

        exam = Exam.builder()
                .id(UUID.randomUUID())
                .userId(userDetails.userId())
                .username(userDetails.username())
                .examType("A1")
                .bankVersion(BANK)
                .criticalQuestionId(1)
                .totalQuestions(25)
                .durationMinutes(19)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .expiresAt(
                        Instant.now().plus(19, ChronoUnit.MINUTES)
                )
                .mode("EXAM")
                .questionIds(List.of(1, 2, 3))
                .build();
    }

    @Test
    void testStartExamSuccess() {
        StartExamRequest request =
                new StartExamRequest("A1", 25, 19, "EXAM", "Test User");

        Question critical = question(1, 1, true);
        List<Question> selected = new ArrayList<>();
        selected.add(critical);

        when(questionRepository.findRandomExamCritical(BANK, "A1"))
                .thenReturn(List.of(critical));

        when(questionRepository.findRandomExamOrdinary(
                eq(BANK), eq("A1"), anyInt(), anyInt()
        )).thenAnswer(invocation -> {
            int chapter = invocation.getArgument(2);
            int limit = invocation.getArgument(3);
            List<Question> chapterQuestions = new ArrayList<>();

            for (int i = 1; i <= limit; i++) {
                Question item =
                        question(chapter * 100 + i, chapter, false);
                chapterQuestions.add(item);
                selected.add(item);
            }

            return chapterQuestions;
        });

        when(examRepository.save(any(Exam.class)))
                .thenAnswer(invocation -> {
                    Exam saved = invocation.getArgument(0);
                    saved.setId(UUID.randomUUID());
                    return saved;
                });

        when(questionRepository.findAllById(anyList()))
                .thenAnswer(invocation -> selected);

        when(questionMapper.toResponse(any(Question.class)))
                .thenAnswer(invocation ->
                        responseFor(invocation.getArgument(0))
                );

        ExamSessionResponse response =
                examService.startExam(userDetails, request);

        assertNotNull(response);
        assertEquals("IN_PROGRESS", response.status());
        assertEquals("A1", response.examType());
        assertEquals(25, response.totalQuestions());
        assertEquals(19, response.durationMinutes());
        assertEquals(25, response.questions().size());
        assertNotNull(response.answers());
        assertTrue(response.answers().isEmpty());

        // Khi bài đang làm, đáp án đúng không được lộ ra.
        assertTrue(response.questions().stream()
                .allMatch(item -> item.correctAnswer() == null));

        verify(examAnswerRepository, times(25))
                .save(any(ExamAnswer.class));
    }

    @Test
    void testStartExamInvalidMode() {
        StartExamRequest request =
                new StartExamRequest("A1", 25, 19, "INVALID", "Test User");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> examService.startExam(userDetails, request)
        );

        assertTrue(exception.getMessage()
                .contains("Mode must be EXAM or PRACTICE"));
    }

    @Test
    void testStartExamRejectsWrongMatrix() {
        StartExamRequest request =
                new StartExamRequest("A1", 20, 19, "EXAM", "Test User");

        assertThrows(
                BusinessException.class,
                () -> examService.startExam(userDetails, request)
        );

        verifyNoInteractions(examRepository);
    }

    @Test
    void testStartExamRejectsPausedSessionForSameLicense() {
        when(examRepository.existsByUserIdAndExamTypeAndStatusIn(
                userDetails.userId(),
                "A1",
                List.of(ExamStatus.IN_PROGRESS, ExamStatus.PAUSED)
        )).thenReturn(true);

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> examService.startExam(
                        userDetails,
                        new StartExamRequest("A1", 25, 19, "EXAM", "Test User")
                )
        );

        assertEquals("ACTIVE_EXAM_EXISTS", error.getErrorCode());
        verifyNoInteractions(questionRepository);
    }

    @Test
    void testGetExamSessionSuccess() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        answer.setSelectedAnswer("A");

        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));
        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(questionsForExam());
        when(questionMapper.toResponse(any(Question.class)))
                .thenAnswer(invocation ->
                        responseFor(invocation.getArgument(0))
                );

        ExamSessionResponse response =
                examService.getExamSession(
                        exam.getId(),
                        userDetails.userId()
                );

        assertEquals(1, response.answeredCount());
        assertEquals("A", response.answers().get(1));
        assertEquals(1, response.answers().size());
        assertEquals("IN_PROGRESS", response.status());
        assertEquals(3, response.questions().size());

        // Khôi phục lựa chọn đã gửi nhưng vẫn giấu đáp án đúng khi đang thi.
        assertTrue(response.questions().stream()
                .allMatch(item -> item.correctAnswer() == null));
    }

    @Test
    void testGetExamSessionTimeout() {
        exam.setExpiresAt(
                Instant.now().minus(1, ChronoUnit.MINUTES)
        );

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));
        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of());
        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(questionsForExam());
        when(questionMapper.toResponse(any(Question.class)))
                .thenAnswer(invocation ->
                        responseFor(invocation.getArgument(0))
                );

        ExamSessionResponse response =
                examService.getExamSession(
                        exam.getId(),
                        userDetails.userId()
                );

        assertEquals("TIMEOUT", response.status());
        assertEquals("A", response.questions().get(0).correctAnswer());
        verify(examRepository).save(exam);
        verify(examProgressReceiptService).record(
                eq(exam), eq(List.of()), eq(false)
        );
    }

    @Test
    void testPauseFreezesTimerKeepsAnswersAndHidesSolutions() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        answer.setSelectedAnswer("B");

        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));
        List<Question> questions = questionsForExam();
        questions.get(0).setExplanation("Secret explanation");
        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(questions);
        when(questionMapper.toResponse(any(Question.class)))
                .thenAnswer(invocation -> responseFor(invocation.getArgument(0)));

        ExamSessionResponse paused = examService.pauseExam(
                exam.getId(), userDetails.userId(), 2
        );

        assertEquals("PAUSED", paused.status());
        assertNull(paused.expiresAt());
        assertNotNull(exam.getPausedAt());
        assertTrue(paused.remainingTimeMs() > 0);
        assertEquals(2, paused.currentQuestionId());
        assertEquals("B", paused.answers().get(1));
        assertEquals(1, paused.answeredCount());
        assertTrue(paused.questions().stream().allMatch(
                question -> question.correctAnswer() == null
                        && question.explanation() == null
        ));

        ExamSessionResponse afterReload = examService.getExamSession(
                exam.getId(), userDetails.userId()
        );
        assertEquals(paused.remainingTimeMs(), afterReload.remainingTimeMs());
        assertEquals(ExamStatus.PAUSED, exam.getStatus());
        verify(examRepository).save(exam);
    }

    @Test
    void testPauseRejectsQuestionOutsideExam() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> examService.pauseExam(
                        exam.getId(), userDetails.userId(), 99
                )
        );

        assertEquals("INVALID_EXAM_QUESTION", error.getErrorCode());
        assertEquals(ExamStatus.IN_PROGRESS, exam.getStatus());
        verify(examRepository, never()).save(any(Exam.class));
    }

    @Test
    void testPauseAfterDeadlineTimesOut() {
        exam.setExpiresAt(Instant.now().minusSeconds(1));

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of());
        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(questionsForExam());
        when(questionMapper.toResponse(any(Question.class)))
                .thenAnswer(invocation -> responseFor(invocation.getArgument(0)));

        ExamSessionResponse response = examService.pauseExam(
                exam.getId(), userDetails.userId(), 2
        );

        assertEquals("TIMEOUT", response.status());
        assertEquals(0L, response.remainingTimeMs());
        assertNull(exam.getPausedAt());
        assertEquals(ExamStatus.TIMEOUT, exam.getStatus());
        verify(examRepository).save(exam);
    }

    @Test
    void testResumeRestoresExactRemainingTimeAndCursor() {
        exam.setStatus(ExamStatus.PAUSED);
        exam.setExpiresAt(null);
        exam.setPausedAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        exam.setRemainingTimeMs(90_000L);
        exam.setCurrentQuestionId(2);

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of());
        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(questionsForExam());
        when(questionMapper.toResponse(any(Question.class)))
                .thenAnswer(invocation -> responseFor(invocation.getArgument(0)));

        Instant before = Instant.now();
        ExamSessionResponse resumed = examService.resumeExam(
                exam.getId(), userDetails.userId()
        );
        Instant after = Instant.now();

        assertEquals("IN_PROGRESS", resumed.status());
        assertEquals(2, resumed.currentQuestionId());
        assertFalse(exam.getExpiresAt().isBefore(before.plusMillis(90_000)));
        assertFalse(exam.getExpiresAt().isAfter(after.plusMillis(90_000)));
        assertNull(exam.getRemainingTimeMs());
        assertNull(exam.getPausedAt());
        assertTrue(resumed.remainingTimeMs() > 0);
        assertTrue(resumed.remainingTimeMs() <= 90_000);
        assertTrue(resumed.questions().stream().allMatch(
                question -> question.correctAnswer() == null
        ));
        verify(examRepository).save(exam);
    }

    @Test
    void testPausedSessionRejectsAnswerFinishAndResult() {
        exam.setStatus(ExamStatus.PAUSED);
        exam.setExpiresAt(null);
        exam.setRemainingTimeMs(90_000L);
        exam.setPausedAt(Instant.now());

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        BusinessException answer = assertThrows(
                BusinessException.class,
                () -> examService.submitAnswer(
                        exam.getId(), userDetails.userId(),
                        new SubmitAnswerRequest("1", "A")
                )
        );
        BusinessException finish = assertThrows(
                BusinessException.class,
                () -> examService.finishExam(
                        exam.getId(), userDetails.userId()
                )
        );
        BusinessException result = assertThrows(
                BusinessException.class,
                () -> examService.getExamResult(
                        exam.getId(), userDetails.userId()
                )
        );

        assertEquals("EXAM_PAUSED", answer.getErrorCode());
        assertEquals("EXAM_PAUSED", finish.getErrorCode());
        assertEquals("EXAM_PAUSED", result.getErrorCode());
        verifyNoInteractions(examAnswerRepository);
        verify(examRepository, never()).save(any(Exam.class));
    }

    @Test
    void testSubmitAnswerSuccess() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);

        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));

        Question question = question(1, 1, true);
        question.setCorrectAnswer("B");
        when(questionRepository.findById(1))
                .thenReturn(Optional.of(question));

        SubmitAnswerResponse response = examService.submitAnswer(
                exam.getId(),
                userDetails.userId(),
                new SubmitAnswerRequest("1", "B")
        );

        assertEquals("answered", response.status());
        assertTrue(answer.getIsCorrect());
        assertEquals("B", answer.getSelectedAnswer());
        assertNull(response.correctAnswer());
        verify(examAnswerRepository).save(answer);
    }

    @Test
    void testSubmitAnswerPracticeMode() {
        exam.setMode("PRACTICE");

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));

        Question question = question(1, 1, true);
        question.setCorrectAnswer("B");
        question.setExplanation("Explanation");
        when(questionRepository.findById(1))
                .thenReturn(Optional.of(question));

        SubmitAnswerResponse response = examService.submitAnswer(
                exam.getId(),
                userDetails.userId(),
                new SubmitAnswerRequest("1", "A")
        );

        assertFalse(response.isCorrect());
        assertEquals("B", response.correctAnswer());
        assertEquals("Explanation", response.explanation());
    }

    @Test
    void testSubmitAnswerRejectsInvalidOption() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));

        when(questionRepository.findById(1))
                .thenReturn(Optional.of(question(1, 1, true)));

        assertThrows(
                BusinessException.class,
                () -> examService.submitAnswer(
                        exam.getId(),
                        userDetails.userId(),
                        new SubmitAnswerRequest("1", "Z")
                )
        );

        assertNull(answer.getSelectedAnswer());
        verify(examAnswerRepository, never()).save(any(ExamAnswer.class));
    }

    @Test
    void testSubmitAnswerRejectsInvalidQuestionId() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        assertThrows(
                BusinessException.class,
                () -> examService.submitAnswer(
                        exam.getId(),
                        userDetails.userId(),
                        new SubmitAnswerRequest("not-a-number", "A")
                )
        );

        verify(examAnswerRepository, never()).save(any(ExamAnswer.class));
    }

    @Test
    void testSubmitAnswerAlreadyCompleted() {
        exam.setStatus(ExamStatus.COMPLETED);

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        assertThrows(
                BusinessException.class,
                () -> examService.submitAnswer(
                        exam.getId(),
                        userDetails.userId(),
                        new SubmitAnswerRequest("1", "B")
                )
        );
    }

    @Test
    void testFinishExamSuccess() {
        exam.setQuestionIds(List.of(1, 2));
        exam.setTotalQuestions(2);

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        ExamAnswer answer1 = new ExamAnswer();
        answer1.setQuestionId(1);
        answer1.setSelectedAnswer("A");
        answer1.setIsCorrect(true);

        ExamAnswer answer2 = new ExamAnswer();
        answer2.setQuestionId(2);
        answer2.setSelectedAnswer("B");
        answer2.setIsCorrect(false);

        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer1, answer2));

        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(List.of(
                        question(1, 1, true),
                        question(2, 1, false)
                ));

        ExamResultResponse response = examService.finishExam(
                exam.getId(),
                userDetails.userId()
        );

        assertEquals("COMPLETED", response.status());
        assertEquals(1, response.correctCount());
        assertEquals(1, response.wrongCount());
        assertEquals(1, response.answeredWrongCount());
        assertEquals(0, response.unansweredCount());
        assertFalse(response.passed());
        verify(examRepository).save(exam);
        verify(examProgressReceiptService).record(
                eq(exam), eq(List.of(answer1, answer2)), eq(false)
        );
    }

    @Test
    void testResultPreservesExamOrderAndSeparatesUnanswered() {
        List<Integer> examOrder = new ArrayList<>();
        List<ExamAnswer> answersById = new ArrayList<>();
        List<Question> questionsById = new ArrayList<>();

        for (int id = 1; id <= 25; id++) {
            examOrder.add(id);

            ExamAnswer answer = new ExamAnswer();
            answer.setQuestionId(id);
            if (id <= 4) {
                answer.setSelectedAnswer("A");
                answer.setIsCorrect(true);
            } else if (id == 5) {
                answer.setSelectedAnswer("B");
                answer.setIsCorrect(false);
            }
            answersById.add(answer);

            Question question = question(id, 1, id == 1);
            question.setExplanation("Explanation " + id);
            questionsById.add(question);
        }

        Collections.reverse(examOrder);
        exam.setQuestionIds(examOrder);

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(answersById);
        when(questionRepository.findAllById(examOrder))
                .thenReturn(questionsById);

        ExamResultResponse finished = examService.finishExam(
                exam.getId(), userDetails.userId()
        );
        ExamResultResponse result = examService.getExamResult(
                exam.getId(), userDetails.userId()
        );

        assertEquals("COMPLETED", finished.status());
        assertEquals(4, finished.correctCount());
        assertEquals(21, finished.wrongCount());
        assertEquals(1, finished.answeredWrongCount());
        assertEquals(20, finished.unansweredCount());
        assertEquals(examOrder, finished.answers().stream()
                .map(detail -> (Integer) detail.get("questionId"))
                .toList());

        List<Integer> resultOrder = result.answers().stream()
                .map(detail -> (Integer) detail.get("questionId"))
                .toList();

        assertEquals(examOrder, resultOrder);
        assertEquals(4, result.correctCount());
        assertEquals(21, result.wrongCount());
        assertEquals(1, result.answeredWrongCount());
        assertEquals(20, result.unansweredCount());
        assertEquals(25, result.correctCount()
                + result.answeredWrongCount() + result.unansweredCount());
        assertEquals("A", result.answers().get(24).get("correctAnswer"));
        assertEquals("Explanation 1", result.answers().get(24).get("explanation"));
        assertEquals("", result.answers().get(0).get("selectedAnswer"));
        verify(examRepository).save(exam);
    }

    @Test
    void testGetResultRejectsInProgress() {
        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));

        assertThrows(
                BusinessException.class,
                () -> examService.getExamResult(
                        exam.getId(), userDetails.userId()
                )
        );

        verifyNoInteractions(examAnswerRepository, questionRepository);
    }

    @Test
    void testCriticalWrongFailsDespiteEnoughCorrectAnswers() {
        List<Integer> ids = new ArrayList<>();
        List<ExamAnswer> answers = new ArrayList<>();
        List<Question> questions = new ArrayList<>();

        for (int id = 1; id <= 25; id++) {
            ids.add(id);
            ExamAnswer answer = new ExamAnswer();
            answer.setQuestionId(id);
            answer.setSelectedAnswer(id == 1 ? "B" : "A");
            answer.setIsCorrect(id != 1);
            answers.add(answer);
            questions.add(question(id, 1, id == 1));
        }

        exam.setQuestionIds(ids);
        exam.setStatus(ExamStatus.COMPLETED);
        exam.setCorrectCount(24);
        exam.setWrongCount(1);
        exam.setScore(96);
        exam.setCompletedAt(Instant.now());

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(answers);
        when(questionRepository.findAllById(ids))
                .thenReturn(questions);

        ExamResultResponse result = examService.getExamResult(
                exam.getId(), userDetails.userId()
        );

        assertFalse(result.passed());
        assertEquals(24, result.correctCount());
        assertEquals(1, result.answeredWrongCount());
        assertEquals(0, result.unansweredCount());
        assertEquals(true, result.answers().get(0).get("isCritical"));
        assertEquals(false, result.answers().get(0).get("isCorrect"));
    }

    @Test
    void testTimeoutResultRevealsAnswerAndExplanation() {
        exam.setQuestionIds(List.of(1));
        exam.setTotalQuestions(1);
        exam.setStatus(ExamStatus.TIMEOUT);
        exam.setCorrectCount(0);
        exam.setWrongCount(1);
        exam.setScore(0);
        exam.setCompletedAt(Instant.now());

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);

        Question question = question(1, 1, true);
        question.setExplanation("Explanation after timeout");

        when(examRepository.findByIdAndUserIdForUpdate(
                exam.getId(), userDetails.userId()
        )).thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));
        when(questionRepository.findAllById(exam.getQuestionIds()))
                .thenReturn(List.of(question));

        ExamResultResponse result = examService.getExamResult(
                exam.getId(), userDetails.userId()
        );

        assertEquals("TIMEOUT", result.status());
        assertEquals(1, result.unansweredCount());
        assertEquals("A", result.answers().get(0).get("correctAnswer"));
        assertEquals("Explanation after timeout",
                result.answers().get(0).get("explanation"));
    }

    @Test
    void testGetExamHistory() {
        Page<Exam> page = new PageImpl<>(List.of(exam));

        when(examRepository.findByUserIdOrderByStartedAtDesc(
                eq(userDetails.userId()),
                any(PageRequest.class)
        )).thenReturn(page);

        when(examMapper.toHistoryResponse(exam))
                .thenReturn(new ExamHistoryResponse(
                        exam.getId(),
                        "A1",
                        "IN_PROGRESS",
                        25,
                        0,
                        0,
                        null,
                        null,
                        "EXAM",
                        19,
                        exam.getStartedAt(),
                        null
                ));

        Page<ExamHistoryResponse> history =
                examService.getExamHistory(
                        userDetails.userId(),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, history.getTotalElements());
        assertEquals("A1", history.getContent().get(0).examType());
    }

    @Test
    void testGetLeaderboard() {
        Exam secondUser = new Exam();
        secondUser.setUserId(UUID.randomUUID());
        secondUser.setUsername("user2");
        secondUser.setScore(100);

        when(examRepository.findLeaderboard(
                any(), any(), any(), any(), any()
        )).thenReturn(List.of(exam, exam, secondUser));

        List<ExamService.LeaderboardEntry> leaderboard =
                examService.getLeaderboard(null, "week");

        assertEquals(2, leaderboard.size());
    }

    @Test
    void testAutoFinishExpiredExams() {
        exam.setExpiresAt(
                Instant.now().minus(1, ChronoUnit.MINUTES)
        );

        when(examRepository.findByStatusAndExpiresAtBefore(
                eq(ExamStatus.IN_PROGRESS),
                any(Instant.class)
        )).thenReturn(List.of(exam));

        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        answer.setIsCorrect(true);

        when(examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId()))
                .thenReturn(List.of(answer));

        examService.autoFinishExpiredExams();

        assertEquals(ExamStatus.TIMEOUT, exam.getStatus());
        verify(examRepository).save(exam);
    }

    private static Question question(
            int id,
            int chapter,
            boolean critical
    ) {
        Question item = new Question();
        item.setId(id);
        item.setChapter(chapter);
        item.setQuestionText("Question " + id);
        item.setOptions(
                "[{\"label\":\"A\",\"text\":\"Option A\"},"
                        + "{\"label\":\"B\",\"text\":\"Option B\"}]"
        );
        item.setCorrectAnswer("A");
        item.setIsCritical(critical);
        return item;
    }

    private static List<Question> questionsForExam() {
        return List.of(
                question(1, 1, true),
                question(2, 1, false),
                question(3, 1, false)
        );
    }

    private static QuestionResponse responseFor(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getChapter(),
                question.getQuestionText(),
                question.getOptions(),
                question.getIsCritical(),
                null,
                null,
                question.getCorrectAnswer(),
                question.getExplanation()
        );
    }
}