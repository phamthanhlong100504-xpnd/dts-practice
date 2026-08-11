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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamService {

    private static final int DEFAULT_TOTAL_QUESTIONS = 25;
    private static final int DEFAULT_DURATION_MINUTES = 20;
    private static final int REQUIRED_CORRECT_FOR_PASS = 21;
    private static final int CRITICAL_QUESTIONS_IN_EXAM = 1;
    private static final int LEADERBOARD_TOP_N = 20;

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final ObjectMapper objectMapper;

    // ==================== START EXAM ====================

    public ExamSessionResponse startExam(JwtUserDetails user, StartExamRequest request) {
        int totalQuestions = request.totalQuestions() != null ? request.totalQuestions() : DEFAULT_TOTAL_QUESTIONS;
        int durationMinutes = request.durationMinutes() != null ? request.durationMinutes() : DEFAULT_DURATION_MINUTES;
        String mode = request.mode() != null ? request.mode().toUpperCase() : "EXAM";

        if (!mode.equals("EXAM") && !mode.equals("PRACTICE")) {
            throw BusinessException.badRequest("Mode must be EXAM or PRACTICE");
        }

        List<Question> criticalQuestions = questionRepository.findRandomCriticalQuestions(CRITICAL_QUESTIONS_IN_EXAM);
        List<Integer> selectedIds = new ArrayList<>(criticalQuestions.stream().map(Question::getId).toList());

        int remaining = totalQuestions - selectedIds.size();
        List<Question> normalQuestions = questionRepository.findRandomNonCritical(remaining);
        selectedIds.addAll(normalQuestions.stream().map(Question::getId).toList());

        Collections.shuffle(selectedIds);

        Instant now = Instant.now();
        Exam exam = Exam.builder()
                .userId(user.userId())
                .username(user.username())
                .examType(request.examType())
                .questionIds(selectedIds)
                .totalQuestions(totalQuestions)
                .durationMinutes(durationMinutes)
                .expiresAt(now.plus(durationMinutes, ChronoUnit.MINUTES))
                .mode(mode)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .build();
        exam = examRepository.save(exam);

        for (Integer qId : selectedIds) {
            examAnswerRepository.save(ExamAnswer.builder()
                    .exam(exam)
                    .questionId(qId)
                    .build());
        }

        List<Question> questions = questionRepository.findAllById(selectedIds);
        Map<Integer, Question> qMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        List<QuestionResponse> questionResponses = selectedIds.stream()
                .map(qId -> questionMapper.toResponse(qMap.get(qId)))
                .filter(Objects::nonNull)
                .toList();

        return new ExamSessionResponse(
                exam.getId(), exam.getExamType(), exam.getStatus().name(),
                exam.getTotalQuestions(), 0,
                exam.getDurationMinutes(), exam.getExpiresAt(), exam.getMode(),
                questionResponses, exam.getStartedAt());
    }

    // ==================== GET SESSION ====================

    @Transactional(readOnly = true)
    public ExamSessionResponse getExamSession(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() == ExamStatus.IN_PROGRESS && isExpired(exam)) {
            exam.setStatus(ExamStatus.TIMEOUT);
            exam.setCompletedAt(exam.getExpiresAt());
            examRepository.save(exam);
        }

        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        long answeredCount = answers.stream().filter(a -> a.getSelectedAnswer() != null).count();

        List<Question> questions = questionRepository.findAllById(exam.getQuestionIds());
        Map<Integer, Question> qMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        List<QuestionResponse> questionResponses = exam.getQuestionIds().stream()
                .map(qId -> questionMapper.toResponse(qMap.get(qId)))
                .filter(Objects::nonNull)
                .toList();

        return new ExamSessionResponse(
                exam.getId(), exam.getExamType(), exam.getStatus().name(),
                exam.getTotalQuestions(), (int) answeredCount,
                exam.getDurationMinutes(), exam.getExpiresAt(), exam.getMode(),
                questionResponses, exam.getStartedAt());
    }

    // ==================== SUBMIT ANSWER ====================

    public SubmitAnswerResponse submitAnswer(UUID examId, UUID userId, SubmitAnswerRequest request) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest("Exam is already " + exam.getStatus());
        }

        if (isExpired(exam)) {
            exam.setStatus(ExamStatus.TIMEOUT);
            exam.setCompletedAt(exam.getExpiresAt());
            examRepository.save(exam);
            throw BusinessException.badRequest("Exam time has expired");
        }

        Integer questionId = Integer.valueOf(request.questionId());
        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        ExamAnswer answer = answers.stream()
                .filter(a -> a.getQuestionId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> BusinessException.badRequest("Question not in this exam"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> BusinessException.notFound("Question not found"));

        answer.setSelectedAnswer(request.selectedAnswer());
        answer.setIsCorrect(question.getCorrectAnswer().equals(request.selectedAnswer()));
        answer.setAnsweredAt(Instant.now());
        examAnswerRepository.save(answer);

        if ("PRACTICE".equals(exam.getMode())) {
            return new SubmitAnswerResponse(
                    "answered",
                    answer.getIsCorrect(),
                    question.getCorrectAnswer(),
                    question.getExplanation() != null ? question.getExplanation() : ""
            );
        }

        return new SubmitAnswerResponse("answered", null, null, null);
    }

    // ==================== FINISH EXAM ====================

    public ExamResultResponse finishExam(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest("Exam is already " + exam.getStatus());
        }

        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        int correct = (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
        int wrong = answers.size() - correct;

        exam.setStatus(ExamStatus.COMPLETED);
        exam.setCorrectCount(correct);
        exam.setWrongCount(wrong);
        exam.setScore(calculateScore(correct, exam.getTotalQuestions()));
        exam.setCompletedAt(Instant.now());
        examRepository.save(exam);

        return buildResultResponse(exam, answers);
    }

    // ==================== GET RESULT ====================

    @Transactional(readOnly = true)
    public ExamResultResponse getExamResult(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() == ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest("Exam is still in progress");
        }

        List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(examId);
        return buildResultResponse(exam, answers);
    }

    // ==================== HISTORY ====================

    @Transactional(readOnly = true)
    public Page<ExamHistoryResponse> getExamHistory(UUID userId, Pageable pageable) {
        return examRepository.findByUserIdOrderByStartedAtDesc(userId, pageable)
                .map(examMapper::toHistoryResponse);
    }

    // ==================== LEADERBOARD ====================

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard(String examType, String period) {
        Instant since = switch (period != null ? period.toLowerCase() : "all") {
            case "week" -> Instant.now().minus(7, ChronoUnit.DAYS);
            case "month" -> Instant.now().minus(30, ChronoUnit.DAYS);
            default -> Instant.EPOCH;
        };

        List<Exam> topExams = examRepository.findByStatusAndModeAndCompletedAtAfterOrderByScoreDescCorrectCountDesc(
                ExamStatus.COMPLETED, "EXAM", since, PageRequest.of(0, LEADERBOARD_TOP_N));

        return topExams.stream()
                .map(e -> new LeaderboardEntry(
                        e.getUserId(),
                        e.getUsername() != null && !e.getUsername().isBlank()
                                ? e.getUsername()
                                : (e.getUserId() != null ? "user_" + e.getUserId().toString().substring(0, 6) : "Thí sinh"),
                        e.getExamType(), e.getScore(),
                        e.getCorrectCount(), e.getTotalQuestions(), e.getCompletedAt()))
                .toList();
    }

    // ==================== AUTO-FINISH EXPIRED EXAMS ====================

    @Scheduled(fixedRateString = "${exam.auto-finish-interval-ms:30000}")
    @Transactional
    public void autoFinishExpiredExams() {
        List<Exam> expired = examRepository.findByStatusAndExpiresAtBefore(
                ExamStatus.IN_PROGRESS, Instant.now());

        for (Exam exam : expired) {
            List<ExamAnswer> answers = examAnswerRepository.findByExamIdOrderByQuestionId(exam.getId());
            int correct = (int) answers.stream().filter(a -> Boolean.TRUE.equals(a.getIsCorrect())).count();
            int wrong = answers.size() - correct;

            exam.setStatus(ExamStatus.TIMEOUT);
            exam.setCorrectCount(correct);
            exam.setWrongCount(wrong);
            exam.setScore(calculateScore(correct, exam.getTotalQuestions()));
            exam.setCompletedAt(exam.getExpiresAt());
            examRepository.save(exam);
            log.info("Auto-finished expired exam: id={}, userId={}", exam.getId(), exam.getUserId());
        }
    }

    // ==================== HELPERS ====================

    private boolean isExpired(Exam exam) {
        return exam.getExpiresAt() != null && Instant.now().isAfter(exam.getExpiresAt());
    }

    private int calculateScore(int correct, int total) {
        return (int) Math.round((double) correct / total * 100);
    }

    private ExamResultResponse buildResultResponse(Exam exam, List<ExamAnswer> answers) {
        List<Question> questions = questionRepository.findAllById(exam.getQuestionIds());
        Map<Integer, Question> qMap = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));

        List<Map<String, Object>> answerDetails = new ArrayList<>();
        for (ExamAnswer ans : answers) {
            Question q = qMap.get(ans.getQuestionId());
            if (q == null) continue;
            Object options;
            try {
                options = objectMapper.readValue(q.getOptions(), Object.class);
            } catch (JsonProcessingException e) {
                options = q.getOptions();
            }
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("questionId", q.getId());
            detail.put("questionText", q.getQuestionText());
            detail.put("options", options);
            detail.put("imageUrl", q.getImageUrl());
            detail.put("correctAnswer", q.getCorrectAnswer());
            detail.put("selectedAnswer", ans.getSelectedAnswer() != null ? ans.getSelectedAnswer() : "");
            detail.put("isCorrect", ans.getIsCorrect() != null ? ans.getIsCorrect() : false);
            detail.put("explanation", q.getExplanation() != null ? q.getExplanation() : "");
            answerDetails.add(detail);
        }

        boolean passed = exam.getCorrectCount() >= REQUIRED_CORRECT_FOR_PASS
                && !hasWrongCritical(answers, qMap);

        return new ExamResultResponse(
                exam.getId(), exam.getExamType(), exam.getStatus().name(),
                exam.getTotalQuestions(), exam.getCorrectCount(), exam.getWrongCount(),
                exam.getScore(), passed,
                exam.getMode(), exam.getDurationMinutes(),
                answerDetails, exam.getStartedAt(), exam.getCompletedAt());
    }

    private boolean hasWrongCritical(List<ExamAnswer> answers, Map<Integer, Question> qMap) {
        return answers.stream().anyMatch(a -> {
            Question q = qMap.get(a.getQuestionId());
            return q != null && Boolean.TRUE.equals(q.getIsCritical())
                    && !Boolean.TRUE.equals(a.getIsCorrect());
        });
    }

    // Inner record for leaderboard entry
    public record LeaderboardEntry(
            UUID userId, String username, String examType, Integer score,
            Integer correctCount, Integer totalQuestions, Instant completedAt) {}
}

