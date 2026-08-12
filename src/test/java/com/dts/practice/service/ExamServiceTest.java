package com.dts.practice.service;

import com.dts.practice.dto.request.StartExamRequest;
import com.dts.practice.dto.request.SubmitAnswerRequest;
import com.dts.practice.dto.response.ExamHistoryResponse;
import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExamService")
class ExamServiceTest {

    @Mock private ExamRepository examRepository;
    @Mock private ExamAnswerRepository examAnswerRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private QuestionMapper questionMapper;
    @Mock private ExamMapper examMapper;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ExamService examService;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID EXAM_ID = UUID.randomUUID();
    private static final JwtUserDetails USER = new JwtUserDetails(USER_ID, "testuser", List.of());

    private Exam exam;
    private Question question;

    @BeforeEach
    void setUp() {
        question = Question.builder()
                .id(1).chapter(1).questionText("Test question?")
                .options("{\"A\":\"Option A\"}").correctAnswer("A")
                .isCritical(false).explanation("Explanation").build();

        exam = Exam.builder()
                .id(EXAM_ID).userId(USER_ID).examType("A1")
                .questionIds(List.of(1, 2, 3)).totalQuestions(3)
                .durationMinutes(20).mode("EXAM")
                .status(ExamStatus.IN_PROGRESS).startedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(1200))
                .correctCount(0).wrongCount(0).score(0)
                .build();
    }

    @Nested
    @DisplayName("startExam")
    class StartExam {

        @Test
        @DisplayName("should create new exam session successfully")
        void shouldStartNewExam() {
            StartExamRequest request = new StartExamRequest("A1", 3, 20, "EXAM", "Nguyen Van A");

            given(questionRepository.findRandomCriticalQuestions(1)).willReturn(List.of(question));
            given(questionRepository.findRandomNonCritical(2)).willReturn(List.of(
                    Question.builder().id(2).chapter(1).build(),
                    Question.builder().id(3).chapter(2).build()));
            given(examRepository.save(any(Exam.class))).willReturn(exam);
            given(questionRepository.findAllById(any())).willReturn(List.of(question));
            given(questionMapper.toResponse(any())).willReturn(null);

            ExamSessionResponse result = examService.startExam(USER, request);

            assertThat(result).isNotNull();
            assertThat(result.examId()).isEqualTo(EXAM_ID);
            assertThat(result.status()).isEqualTo("IN_PROGRESS");
            assertThat(result.totalQuestions()).isEqualTo(3);
            then(examRepository).should().save(any(Exam.class));
            then(examAnswerRepository).should(times(3)).save(any(ExamAnswer.class));
        }

        @Test
        @DisplayName("should denormalize full name into exam")
        void shouldStoreFullName() {
            StartExamRequest request = new StartExamRequest("A1", 3, 20, "EXAM", "Nguyen Van A");

            given(questionRepository.findRandomCriticalQuestions(1)).willReturn(List.of(question));
            given(questionRepository.findRandomNonCritical(2)).willReturn(List.of(
                    Question.builder().id(2).chapter(1).build(),
                    Question.builder().id(3).chapter(2).build()));
            given(examRepository.save(any(Exam.class))).willReturn(exam);
            given(questionRepository.findAllById(any())).willReturn(List.of(question));
            given(questionMapper.toResponse(any())).willReturn(null);

            examService.startExam(USER, request);

            then(examRepository).should().save(argThat(e -> "Nguyen Van A".equals(e.getFullName())));
        }

        @Test
        @DisplayName("should reject invalid mode")
        void shouldRejectInvalidMode() {
            StartExamRequest request = new StartExamRequest("A1", 3, 20, "INVALID", null);

            assertThatThrownBy(() -> examService.startExam(USER, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Mode must be EXAM or PRACTICE");
        }
    }

    @Nested
    @DisplayName("getExamSession")
    class GetExamSession {

        @Test
        @DisplayName("should return exam session when exam exists")
        void whenExamExists_shouldReturnSession() {
            given(examRepository.findByIdAndUserId(EXAM_ID, USER_ID)).willReturn(Optional.of(exam));
            given(examAnswerRepository.findByExamIdOrderByQuestionId(EXAM_ID)).willReturn(List.of());
            given(questionRepository.findAllById(any())).willReturn(List.of());
            given(questionMapper.toResponse(any())).willReturn(null);

            ExamSessionResponse result = examService.getExamSession(EXAM_ID, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.examId()).isEqualTo(EXAM_ID);
        }

        @Test
        @DisplayName("should throw not found when exam does not exist")
        void whenExamNotExists_shouldThrowNotFound() {
            given(examRepository.findByIdAndUserId(EXAM_ID, USER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> examService.getExamSession(EXAM_ID, USER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Exam not found");
        }
    }

    @Nested
    @DisplayName("submitAnswer")
    class SubmitAnswer {

        @Test
        @DisplayName("should accept valid answer submission")
        void shouldAcceptValidAnswer() {
            SubmitAnswerRequest request = new SubmitAnswerRequest("1", "A");
            ExamAnswer answer = ExamAnswer.builder()
                    .exam(exam).questionId(1).build();

            given(examRepository.findByIdAndUserId(EXAM_ID, USER_ID)).willReturn(Optional.of(exam));
            given(examAnswerRepository.findByExamIdOrderByQuestionId(EXAM_ID))
                    .willReturn(List.of(answer));
            given(questionRepository.findById(1)).willReturn(Optional.of(question));

            SubmitAnswerResponse result = examService.submitAnswer(EXAM_ID, USER_ID, request);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo("answered");
            then(examAnswerRepository).should().save(answer);
        }

        @Test
        @DisplayName("should reject when exam is already completed")
        void whenExamCompleted_shouldReject() {
            exam.setStatus(ExamStatus.COMPLETED);
            SubmitAnswerRequest request = new SubmitAnswerRequest("1", "A");

            given(examRepository.findByIdAndUserId(EXAM_ID, USER_ID)).willReturn(Optional.of(exam));

            assertThatThrownBy(() -> examService.submitAnswer(EXAM_ID, USER_ID, request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already COMPLETED");
        }
    }

    @Nested
    @DisplayName("finishExam")
    class FinishExam {

        @Test
        @DisplayName("should complete exam and return results")
        void shouldCompleteExam() {
            ExamAnswer answer = ExamAnswer.builder()
                    .exam(exam).questionId(1).selectedAnswer("A")
                    .isCorrect(true).answeredAt(Instant.now()).build();

            given(examRepository.findByIdAndUserId(EXAM_ID, USER_ID)).willReturn(Optional.of(exam));
            given(examAnswerRepository.findByExamIdOrderByQuestionId(EXAM_ID))
                    .willReturn(List.of(answer));

            ExamResultResponse result = examService.finishExam(EXAM_ID, USER_ID);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo("COMPLETED");
            then(examRepository).should().save(exam);
        }
    }

    @Nested
    @DisplayName("getExamHistory")
    class GetExamHistory {

        @Test
        @DisplayName("should return paginated history as DTOs")
        void shouldReturnHistoryDtos() {
            PageRequest pageable = PageRequest.of(0, 10);
            ExamHistoryResponse dto = new ExamHistoryResponse(
                    EXAM_ID, "A1", "COMPLETED", 25, 20, 5, 80, true, "EXAM", 20,
                    Instant.now(), Instant.now());

            given(examRepository.findByUserIdOrderByStartedAtDesc(USER_ID, pageable))
                    .willReturn(new PageImpl<>(List.of(exam)));
            given(examMapper.toHistoryResponse(exam)).willReturn(dto);

            Page<ExamHistoryResponse> result = examService.getExamHistory(USER_ID, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isInstanceOf(ExamHistoryResponse.class);
            then(examMapper).should().toHistoryResponse(exam);
        }
    }

    @Nested
    @DisplayName("getLeaderboard")
    class GetLeaderboard {

        @Test
        @DisplayName("should return top entries with full name")
        void shouldReturnLeaderboard() {
            exam.setStatus(ExamStatus.COMPLETED);
            exam.setScore(95);
            exam.setCorrectCount(23);
            exam.setFullName("Nguyen Van A");

            given(examRepository.findLeaderboard(
                    eq(ExamStatus.COMPLETED), eq("EXAM"), eq("A1"), any(Instant.class), any()))
                    .willReturn(List.of(exam));

            List<ExamService.LeaderboardEntry> result = examService.getLeaderboard("A1", "all");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).score()).isEqualTo(95);
            assertThat(result.get(0).fullName()).isEqualTo("Nguyen Van A");
            then(examRepository).should().findLeaderboard(
                    eq(ExamStatus.COMPLETED), eq("EXAM"), eq("A1"), any(Instant.class), any());
        }

        @Test
        @DisplayName("should fallback to username when full name is blank")
        void shouldFallbackToUsername() {
            exam.setStatus(ExamStatus.COMPLETED);
            exam.setScore(80);
            exam.setUsername("testuser");
            exam.setFullName(null);

            given(examRepository.findLeaderboard(
                    eq(ExamStatus.COMPLETED), eq("EXAM"), eq("B2"), any(Instant.class), any()))
                    .willReturn(List.of(exam));

            List<ExamService.LeaderboardEntry> result = examService.getLeaderboard("B2", "all");

            assertThat(result.get(0).fullName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should dedup: one entry per user, keep best attempt")
        void shouldDedupByUser() {
            UUID other = UUID.randomUUID();
            Exam best = Exam.builder()
                    .id(UUID.randomUUID()).userId(USER_ID).username("u1").fullName("User One")
                    .examType("B2").mode("EXAM").status(ExamStatus.COMPLETED)
                    .score(90).correctCount(22).totalQuestions(25).completedAt(Instant.parse("2026-08-12T10:00:00Z"))
                    .build();
            Exam worse = Exam.builder()
                    .id(UUID.randomUUID()).userId(USER_ID).username("u1").fullName("User One")
                    .examType("B2").mode("EXAM").status(ExamStatus.COMPLETED)
                    .score(70).correctCount(18).totalQuestions(25).completedAt(Instant.parse("2026-08-12T11:00:00Z"))
                    .build();
            Exam otherBest = Exam.builder()
                    .id(UUID.randomUUID()).userId(other).username("u2").fullName("User Two")
                    .examType("B2").mode("EXAM").status(ExamStatus.COMPLETED)
                    .score(85).correctCount(21).totalQuestions(25).completedAt(Instant.parse("2026-08-12T09:00:00Z"))
                    .build();

            // Sorted as the query returns: score desc, correctCount desc, completedAt asc
            given(examRepository.findLeaderboard(
                    eq(ExamStatus.COMPLETED), eq("EXAM"), eq("B2"), any(Instant.class), any()))
                    .willReturn(List.of(best, otherBest, worse));

            List<ExamService.LeaderboardEntry> result = examService.getLeaderboard("B2", "all");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ExamService.LeaderboardEntry::userId)
                    .containsExactly(USER_ID, other);
            assertThat(result.get(0).score()).isEqualTo(90); // best of USER_ID kept, worse dropped
        }
    }
}
