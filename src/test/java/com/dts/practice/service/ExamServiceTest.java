package com.dts.practice.service;

import com.dts.practice.dto.request.StartExamRequest;
import com.dts.practice.dto.request.SubmitAnswerRequest;
import com.dts.practice.dto.response.*;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

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

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ExamService examService;

    private JwtUserDetails userDetails;
    private Exam exam;

    @BeforeEach
    void setUp() {
        userDetails = new JwtUserDetails(UUID.randomUUID(), "testuser", List.of());
        exam = Exam.builder()
                .id(UUID.randomUUID())
                .userId(userDetails.userId())
                .username(userDetails.username())
                .examType("B2")
                .totalQuestions(25)
                .durationMinutes(20)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .expiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
                .mode("EXAM")
                .questionIds(List.of(1, 2, 3))
                .build();
    }

    @Test
    void testStartExam_Success() {
        StartExamRequest request = new StartExamRequest("B2", 25, 20, "EXAM", "Test User");

        Question critical = new Question();
        critical.setId(1);
        critical.setIsCritical(true);

        Question normal1 = new Question();
        normal1.setId(2);
        Question normal2 = new Question();
        normal2.setId(3);

        when(questionRepository.findRandomCriticalQuestions(1)).thenReturn(List.of(critical));
        when(questionRepository.findRandomNonCritical(24)).thenReturn(List.of(normal1, normal2));
        when(examRepository.save(any(Exam.class))).thenAnswer(inv -> {
            Exam e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });
        when(questionRepository.findAllById(anyList())).thenReturn(List.of(critical, normal1, normal2));
        when(questionMapper.toResponse(any())).thenReturn(new QuestionResponse(1, 1, "Text", "options", false, "img", "A", "explain"));

        ExamSessionResponse response = examService.startExam(userDetails, request);

        assertNotNull(response);
        assertEquals("IN_PROGRESS", response.status());
        verify(examAnswerRepository, times(3)).save(any(ExamAnswer.class));
    }

    @Test
    void testStartExam_InvalidMode() {
        StartExamRequest request = new StartExamRequest("B2", 25, 20, "INVALID", "Test User");

        BusinessException exception = assertThrows(BusinessException.class, () -> examService.startExam(userDetails, request));
        assertTrue(exception.getMessage().contains("Mode must be EXAM or PRACTICE"));
    }

    @Test
    void testGetExamSession_Success() {
        when(examRepository.findByIdAndUserId(exam.getId(), userDetails.userId())).thenReturn(Optional.of(exam));
        
        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        answer.setSelectedAnswer("A");
        
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId())).thenReturn(List.of(answer));
        when(questionRepository.findAllById(exam.getQuestionIds())).thenReturn(Collections.emptyList());

        ExamSessionResponse response = examService.getExamSession(exam.getId(), userDetails.userId());

        assertNotNull(response);
        assertEquals(1, response.answeredCount());
        assertEquals("IN_PROGRESS", response.status());
    }

    @Test
    void testGetExamSession_Timeout() {
        exam.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(examRepository.findByIdAndUserId(exam.getId(), userDetails.userId())).thenReturn(Optional.of(exam));
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId())).thenReturn(Collections.emptyList());
        when(questionRepository.findAllById(exam.getQuestionIds())).thenReturn(Collections.emptyList());

        ExamSessionResponse response = examService.getExamSession(exam.getId(), userDetails.userId());

        assertEquals("TIMEOUT", response.status());
        verify(examRepository, times(1)).save(exam);
    }

    @Test
    void testSubmitAnswer_Success() {
        when(examRepository.findByIdAndUserId(exam.getId(), userDetails.userId())).thenReturn(Optional.of(exam));
        
        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId())).thenReturn(List.of(answer));
        
        Question question = new Question();
        question.setId(1);
        question.setCorrectAnswer("B");
        
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));

        SubmitAnswerRequest request = new SubmitAnswerRequest("1", "B");
        SubmitAnswerResponse response = examService.submitAnswer(exam.getId(), userDetails.userId(), request);

        assertEquals("answered", response.status());
        assertTrue(answer.getIsCorrect());
        assertEquals("B", answer.getSelectedAnswer());
        verify(examAnswerRepository).save(answer);
    }

    @Test
    void testSubmitAnswer_PracticeMode() {
        exam.setMode("PRACTICE");
        when(examRepository.findByIdAndUserId(exam.getId(), userDetails.userId())).thenReturn(Optional.of(exam));
        
        ExamAnswer answer = new ExamAnswer();
        answer.setQuestionId(1);
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId())).thenReturn(List.of(answer));
        
        Question question = new Question();
        question.setId(1);
        question.setCorrectAnswer("B");
        question.setExplanation("Explanation");
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));

        SubmitAnswerRequest request = new SubmitAnswerRequest("1", "A");
        SubmitAnswerResponse response = examService.submitAnswer(exam.getId(), userDetails.userId(), request);

        assertFalse(response.isCorrect());
        assertEquals("B", response.correctAnswer());
        assertEquals("Explanation", response.explanation());
    }

    @Test
    void testSubmitAnswer_AlreadyCompleted() {
        exam.setStatus(ExamStatus.COMPLETED);
        when(examRepository.findByIdAndUserId(exam.getId(), userDetails.userId())).thenReturn(Optional.of(exam));

        SubmitAnswerRequest request = new SubmitAnswerRequest("1", "B");
        assertThrows(BusinessException.class, () -> examService.submitAnswer(exam.getId(), userDetails.userId(), request));
    }

    @Test
    void testFinishExam_Success() {
        when(examRepository.findByIdAndUserId(exam.getId(), userDetails.userId())).thenReturn(Optional.of(exam));
        
        ExamAnswer answer1 = new ExamAnswer();
        answer1.setQuestionId(1);
        answer1.setIsCorrect(true);
        ExamAnswer answer2 = new ExamAnswer();
        answer2.setQuestionId(2);
        answer2.setIsCorrect(false);

        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId())).thenReturn(List.of(answer1, answer2));
        
        Question q1 = new Question();
        q1.setId(1);
        q1.setOptions("[]");
        Question q2 = new Question();
        q2.setId(2);
        q2.setOptions("[]");
        
        when(questionRepository.findAllById(exam.getQuestionIds())).thenReturn(List.of(q1, q2));

        ExamResultResponse response = examService.finishExam(exam.getId(), userDetails.userId());

        assertNotNull(response);
        assertEquals("COMPLETED", response.status());
        assertEquals(1, response.correctCount());
        assertEquals(1, response.wrongCount());
        verify(examRepository).save(exam);
    }

    @Test
    void testGetExamHistory() {
        Page<Exam> page = new PageImpl<>(List.of(exam));
        when(examRepository.findByUserIdOrderByStartedAtDesc(eq(userDetails.userId()), any(PageRequest.class))).thenReturn(page);
        when(examMapper.toHistoryResponse(exam)).thenReturn(new ExamHistoryResponse(exam.getId(), "B2", "COMPLETED", 25, 20, 5, 80, true, "EXAM", 20, Instant.now(), Instant.now()));

        Page<ExamHistoryResponse> history = examService.getExamHistory(userDetails.userId(), PageRequest.of(0, 10));
        assertEquals(1, history.getTotalElements());
    }

    @Test
    void testGetLeaderboard() {
        Exam e2 = new Exam();
        e2.setUserId(UUID.randomUUID());
        e2.setUsername("user2");
        e2.setScore(100);

        when(examRepository.findLeaderboard(any(), any(), any(), any(), any())).thenReturn(List.of(exam, exam, e2));
        
        List<ExamService.LeaderboardEntry> leaderboard = examService.getLeaderboard(null, "week");
        
        assertEquals(2, leaderboard.size());
    }

    @Test
    void testAutoFinishExpiredExams() {
        exam.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
        when(examRepository.findByStatusAndExpiresAtBefore(eq(ExamStatus.IN_PROGRESS), any(Instant.class))).thenReturn(List.of(exam));
        
        ExamAnswer answer1 = new ExamAnswer();
        answer1.setQuestionId(1);
        answer1.setIsCorrect(true);
        when(examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId())).thenReturn(List.of(answer1));
        
        examService.autoFinishExpiredExams();
        
        assertEquals(ExamStatus.TIMEOUT, exam.getStatus());
        verify(examRepository, times(1)).save(exam);
    }
}

