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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ExamService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();
    private static final String ACTIVE_INDEX =
            "uq_exams_one_active_per_user_license";
    private static final int LEGACY_REQUIRED_CORRECT = 21;
    private static final int LEADERBOARD_TOP_N = 20;
    private static final int LEADERBOARD_WINDOW = 500;

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final ObjectMapper objectMapper;
    private final WrongQuestionProgressService wrongQuestionProgressService;
    private final ExamProgressReceiptService examProgressReceiptService;
    // ==================== START EXAM ====================

    public ExamSessionResponse startExam(
            JwtUserDetails user,
            StartExamRequest request
    ) {
        String mode = request.mode() != null
                ? request.mode().trim().toUpperCase(Locale.ROOT)
                : "EXAM";

        if (!mode.equals("EXAM") && !mode.equals("PRACTICE")) {
            throw BusinessException.badRequest(
                    "Mode must be EXAM or PRACTICE"
            );
        }

        String licenseClass = request.examType()
                .trim()
                .toUpperCase(Locale.ROOT);

        ExamMatrix.Rule rule = ExamMatrix.forClass(licenseClass);

        // Không tạo đề khác ma trận trong khi giao diện vẫn hiển thị
        // số câu hoặc thời gian do người dùng chọn.
        if ((request.totalQuestions() != null
                && request.totalQuestions() != rule.totalQuestions())
                || (request.durationMinutes() != null
                && request.durationMinutes() != rule.durationMinutes())) {
            throw BusinessException.badRequest(
                    "Hạng " + licenseClass + " cần "
                            + rule.totalQuestions() + " câu và "
                            + rule.durationMinutes() + " phút."
            );
        }

        boolean hasActiveExam = examRepository
                .existsByUserIdAndExamTypeAndStatusIn(
                        user.userId(),
                        licenseClass,
                        List.of(ExamStatus.IN_PROGRESS, ExamStatus.PAUSED)
                );

        if (hasActiveExam) {
            throw activeExamExists(licenseClass);
        }

        List<Question> criticalQuestions =
                questionRepository.findRandomExamCritical(
                        CURRENT_BANK,
                        licenseClass
                );

        if (criticalQuestions.size() != 1) {
            throw BusinessException.conflict(
                    "Bộ câu hỏi chưa đủ câu điểm liệt cho hạng "
                            + licenseClass
            );
        }

        int criticalQuestionId = criticalQuestions.get(0).getId();
        List<Integer> selectedIds = new ArrayList<>();
        selectedIds.add(criticalQuestionId);

        for (Map.Entry<Integer, Integer> quota
                : rule.ordinaryByChapter().entrySet()) {
            List<Question> ordinary =
                    questionRepository.findRandomExamOrdinary(
                            CURRENT_BANK,
                            licenseClass,
                            quota.getKey(),
                            quota.getValue()
                    );

            if (ordinary.size() != quota.getValue()) {
                throw BusinessException.conflict(
                        "Bộ câu hỏi hạng " + licenseClass
                                + " thiếu câu thường ở chương "
                                + quota.getKey()
                );
            }

            ordinary.forEach(q -> selectedIds.add(q.getId()));
        }

        if (selectedIds.size() != rule.totalQuestions()
                || new HashSet<>(selectedIds).size()
                        != selectedIds.size()) {
            throw BusinessException.conflict(
                    "Không thể tạo đề đủ câu hỏi không trùng lặp."
            );
        }

        Collections.shuffle(selectedIds);

        Instant now = Instant.now();

        Exam exam = Exam.builder()
                .userId(user.userId())
                .username(user.username())
                .fullName(
                        request.fullName() != null
                                && !request.fullName().isBlank()
                                ? request.fullName()
                                : user.username()
                )
                .examType(licenseClass)
                .bankVersion(CURRENT_BANK)
                .criticalQuestionId(criticalQuestionId)
                .questionIds(selectedIds)
                .currentQuestionId(selectedIds.get(0))
                .totalQuestions(rule.totalQuestions())
                .durationMinutes(rule.durationMinutes())
                .expiresAt(
                        now.plus(
                                rule.durationMinutes(),
                                ChronoUnit.MINUTES
                        )
                )
                .mode(mode)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .build();

        try {
            exam = examRepository.save(exam);
            examRepository.flush();
        } catch (DataIntegrityViolationException error) {
            if (isActiveIndexViolation(error)) {
                throw activeExamExists(licenseClass);
            }
            throw error;
        }

        for (Integer questionId : selectedIds) {
            examAnswerRepository.save(
                    ExamAnswer.builder()
                            .exam(exam)
                            .questionId(questionId)
                            .build()
            );
        }

        List<Question> questions =
                questionRepository.findAllById(selectedIds);

        Map<Integer, Question> byId = questions.stream()
                .collect(
                        Collectors.toMap(
                                Question::getId,
                                question -> question
                        )
                );

        Exam savedExam = exam;

        List<QuestionResponse> responses = selectedIds.stream()
                .map(id -> toSessionQuestion(savedExam, byId.get(id)))
                .toList();

        return new ExamSessionResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                0,
                exam.getDurationMinutes(),
                exam.getExpiresAt(),
                exam.getMode(),
                responses,
                exam.getStartedAt(),
                Map.of(),
                remainingTimeMs(exam),
                exam.getCurrentQuestionId()
        );
    }

    private BusinessException activeExamExists(String licenseClass) {
        return BusinessException.of(
                "ACTIVE_EXAM_EXISTS",
                "Bạn đang có một bài " + licenseClass + " chưa hoàn thành.",
                HttpStatus.CONFLICT
        );
    }

    private boolean isActiveIndexViolation(Throwable error) {
        Throwable cause = error;

        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException
                    constraint
                    && ACTIVE_INDEX.equals(constraint.getConstraintName())) {
                return true;
            }
            if (cause.getMessage() != null
                    && cause.getMessage().contains(ACTIVE_INDEX)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    // ==================== GET SESSION ====================

    public ExamSessionResponse getExamSession(
            UUID examId,
            UUID userId
    ) {
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() == ExamStatus.IN_PROGRESS
                && isExpired(exam)) {
            finishTimeout(exam);
        }

        return buildSession(exam);
    }

    @Transactional
    public ExamSessionResponse pauseExam(
            UUID examId,
            UUID userId,
            Integer currentQuestionId
    ) {
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() == ExamStatus.PAUSED) {
            return buildSession(exam);
        }
        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.of(
                    "EXAM_NOT_ACTIVE",
                    "Exam cannot be paused in status " + exam.getStatus(),
                    HttpStatus.CONFLICT
            );
        }

        Instant now = Instant.now();
        if (exam.getExpiresAt() == null) {
            throw BusinessException.conflict("Exam expiration is unavailable");
        }
        if (!now.isBefore(exam.getExpiresAt())) {
            finishTimeout(exam);
            return buildSession(exam);
        }

        if (currentQuestionId == null) {
            throw BusinessException.badRequest("currentQuestionId is required");
        }
        if (!exam.getQuestionIds().contains(currentQuestionId)) {
            throw BusinessException.of(
                    "INVALID_EXAM_QUESTION",
                    "Current question is not in this exam",
                    HttpStatus.BAD_REQUEST
            );
        }

        long remaining = Duration.between(now, exam.getExpiresAt()).toMillis();
        if (remaining <= 0) {
            finishTimeout(exam);
            return buildSession(exam);
        }

        exam.setCurrentQuestionId(currentQuestionId);
        exam.setPausedAt(now);
        exam.setRemainingTimeMs(remaining);
        exam.setExpiresAt(null);
        exam.setStatus(ExamStatus.PAUSED);
        examRepository.save(exam);
        return buildSession(exam);
    }

    @Transactional
    public ExamSessionResponse resumeExam(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getStatus() == ExamStatus.IN_PROGRESS) {
            if (isExpired(exam)) {
                finishTimeout(exam);
            }
            return buildSession(exam);
        }
        if (exam.getStatus() != ExamStatus.PAUSED) {
            throw BusinessException.of(
                    "EXAM_NOT_PAUSED",
                    "Exam cannot be resumed in status " + exam.getStatus(),
                    HttpStatus.CONFLICT
            );
        }

        Long remaining = exam.getRemainingTimeMs();
        if (remaining == null || remaining <= 0) {
            throw BusinessException.conflict("Paused exam has no remaining time");
        }

        exam.setExpiresAt(Instant.now().plusMillis(remaining));
        exam.setRemainingTimeMs(null);
        exam.setPausedAt(null);
        exam.setStatus(ExamStatus.IN_PROGRESS);
        examRepository.save(exam);
        return buildSession(exam);
    }

    private ExamSessionResponse buildSession(Exam exam) {
        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(
                        exam.getId()
                );

        Map<Integer, String> selectedAnswers = new LinkedHashMap<>();

        for (ExamAnswer answer : answers) {
            if (answer.getSelectedAnswer() != null) {
                selectedAnswers.put(
                        answer.getQuestionId(),
                        answer.getSelectedAnswer()
                );
            }
        }

        List<Question> questions =
                questionRepository.findAllById(
                        exam.getQuestionIds()
                );

        Map<Integer, Question> byId = questions.stream()
                .collect(
                        Collectors.toMap(
                                Question::getId,
                                question -> question
                        )
                );

        List<QuestionResponse> responses =
                exam.getQuestionIds().stream()
                        .map(id ->
                                toSessionQuestion(
                                        exam,
                                        byId.get(id)
                                )
                        )
                        .toList();

        return new ExamSessionResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                selectedAnswers.size(),
                exam.getDurationMinutes(),
                exam.getExpiresAt(),
                exam.getMode(),
                responses,
                exam.getStartedAt(),
                selectedAnswers,
                remainingTimeMs(exam),
                exam.getCurrentQuestionId()
        );
    }

    // ==================== SUBMIT ANSWER ====================

    @Transactional(noRollbackFor = BusinessException.class)
    public SubmitAnswerResponse submitAnswer(
            UUID examId,
            UUID userId,
            SubmitAnswerRequest request
    ) {
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() == ExamStatus.TIMEOUT) {
            throw BusinessException.of(
                    "EXAM_TIME_EXPIRED",
                    "Exam time has expired",
                    HttpStatus.CONFLICT
            );
        }

        if (exam.getStatus() == ExamStatus.PAUSED) {
            throw BusinessException.of(
                    "EXAM_PAUSED",
                    "Resume the exam before answering",
                    HttpStatus.CONFLICT
            );
        }

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest(
                    "Exam is already " + exam.getStatus()
            );
        }

        if (isExpired(exam)) {
            finishTimeout(exam);
            // noRollbackFor giữ trạng thái TIMEOUT khi response là 409.
            throw BusinessException.of(
                    "EXAM_TIME_EXPIRED",
                    "Exam time has expired",
                    HttpStatus.CONFLICT
            );
        }

        Integer questionId;
        try {
            questionId = Integer.valueOf(request.questionId());
        } catch (NumberFormatException error) {
            throw BusinessException.of(
                    "INVALID_QUESTION_ID",
                    "Question ID must be an integer",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(
                        examId
                );

        ExamAnswer answer = answers.stream()
                .filter(item ->
                        item.getQuestionId().equals(questionId))
                .findFirst()
                .orElseThrow(
                        () -> BusinessException.badRequest(
                                "Question not in this exam"
                        )
                );

        Question question =
                questionRepository.findById(questionId)
                        .orElseThrow(
                                () -> BusinessException.notFound(
                                        "Question not found"
                                )
                        );

        validateAnswerOption(question, request.selectedAnswer());

        Instant answeredAt = Instant.now();
        if ("PRACTICE".equals(exam.getMode())
                && CURRENT_BANK.equals(exam.getBankVersion())) {
            wrongQuestionProgressService.recordAnswer(
                    userId,
                    exam.getExamType(),
                    exam.getBankVersion(),
                    questionId,
                    request.selectedAnswer(),
                    answeredAt
            );
        }

        answer.setSelectedAnswer(request.selectedAnswer());
        answer.setIsCorrect(
                question.getCorrectAnswer()
                        .equals(request.selectedAnswer())
        );
        answer.setAnsweredAt(answeredAt);
        examAnswerRepository.save(answer);

        if ("PRACTICE".equals(exam.getMode())) {
            return new SubmitAnswerResponse(
                    "answered",
                    answer.getIsCorrect(),
                    question.getCorrectAnswer(),
                    question.getExplanation() != null
                            ? question.getExplanation()
                            : ""
            );
        }

        return new SubmitAnswerResponse(
                "answered",
                null,
                null,
                null
        );
    }

    // ==================== FINISH EXAM ====================

        @CacheEvict(value = "leaderboard", allEntries = true)
        public ExamResultResponse finishExam(
                UUID examId,
                UUID userId
        ) {
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound("Exam not found")
                );

        if (exam.getStatus() == ExamStatus.TIMEOUT
                || exam.getStatus() == ExamStatus.COMPLETED) {
                List<ExamAnswer> previous =
                        examAnswerRepository.findByExamIdOrderByQuestionId(examId);

                ExamResultResponse result =
                        buildResultResponse(exam, previous);

                examProgressReceiptService.record(
                        exam,
                        previous,
                        result.passed()
                );
                return result;
        }

        if (exam.getStatus() == ExamStatus.PAUSED) {
                throw BusinessException.of(
                        "EXAM_PAUSED",
                        "Resume the exam before finishing",
                        HttpStatus.CONFLICT
                );
        }

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
                throw BusinessException.badRequest(
                        "Exam is already " + exam.getStatus()
                );
        }

        if (isExpired(exam)) {
                finishTimeout(exam);
                return buildResultResponse(
                        exam,
                        examAnswerRepository.findByExamIdOrderByQuestionId(examId)
                );
        }

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(examId);

        int correct = (int) answers.stream()
                .filter(answer ->
                        Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        int wrong = answers.size() - correct;

        exam.setStatus(ExamStatus.COMPLETED);
        exam.setCorrectCount(correct);
        exam.setWrongCount(wrong);
        exam.setScore(
                calculateScore(correct, exam.getTotalQuestions())
        );
        exam.setCompletedAt(Instant.now());

        examRepository.save(exam);
        syncWrongQuestionProgress(exam, answers);

        ExamResultResponse result =
                buildResultResponse(exam, answers);

        examProgressReceiptService.record(
                exam,
                answers,
                result.passed()
        );

        return result;
        }

    // ==================== GET RESULT ====================

    @Transactional
    public ExamResultResponse getExamResult(
            UUID examId,
            UUID userId
    ) {
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() == ExamStatus.IN_PROGRESS) {
            if (isExpired(exam)) {
                finishTimeout(exam);
            } else {
                throw BusinessException.badRequest(
                        "Exam is still in progress"
                );
            }
        }

        if (exam.getStatus() == ExamStatus.PAUSED) {
            throw BusinessException.of(
                    "EXAM_PAUSED",
                    "Resume the exam before viewing the result",
                    HttpStatus.CONFLICT
            );
        }

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(
                        examId
                );

        return buildResultResponse(exam, answers);
    }

    // ==================== HISTORY ====================

    @Transactional(readOnly = true)
    public Page<ExamHistoryResponse> getExamHistory(
            UUID userId,
            Pageable pageable
    ) {
        return examRepository
                .findByUserIdOrderByStartedAtDesc(
                        userId,
                        pageable
                )
                .map(exam -> {
                    ExamHistoryResponse history =
                            examMapper.toHistoryResponse(exam);

                    if (exam.getStatus() == ExamStatus.IN_PROGRESS
                            || exam.getStatus() == ExamStatus.PAUSED) {
                        return history;
                    }

                    List<ExamAnswer> answers =
                            examAnswerRepository
                                    .findByExamIdOrderByQuestionId(
                                            exam.getId()
                                    );

                    List<Question> questions =
                            questionRepository.findAllById(
                                    exam.getQuestionIds()
                            );

                    Map<Integer, Question> byId =
                            questions.stream()
                                    .collect(
                                            Collectors.toMap(
                                                    Question::getId,
                                                    question -> question
                                            )
                                    );

                    return new ExamHistoryResponse(
                            history.examId(),
                            history.examType(),
                            history.status(),
                            history.totalQuestions(),
                            history.correctCount(),
                            history.wrongCount(),
                            history.score(),
                            passed(exam, answers, byId),
                            history.mode(),
                            history.durationMinutes(),
                            history.startedAt(),
                            history.completedAt()
                    );
                });
    }

    // ==================== LEADERBOARD ====================

    @Transactional(readOnly = true)
    @Cacheable(
            value = "leaderboard",
            key = "'lb:' + (#examType != null ? "
                    + "#examType : 'all') + ':' + "
                    + "(#period != null ? #period : 'all')"
    )
    public List<LeaderboardEntry> getLeaderboard(
            String examType,
            String period
    ) {
        Instant since = switch (
                period != null
                        ? period.toLowerCase()
                        : "all"
        ) {
            case "week" ->
                    Instant.now().minus(7, ChronoUnit.DAYS);
            case "month" ->
                    Instant.now().minus(30, ChronoUnit.DAYS);
            default -> Instant.EPOCH;
        };

        List<Exam> candidates =
                examRepository.findLeaderboard(
                        ExamStatus.COMPLETED,
                        "EXAM",
                        examType != null && !examType.isBlank()
                                ? examType
                                : null,
                        since,
                        PageRequest.of(
                                0,
                                LEADERBOARD_WINDOW
                        )
                );

        List<LeaderboardEntry> result =
                new ArrayList<>();

        Set<UUID> seen = new HashSet<>();

        for (Exam candidate : candidates) {
            if (seen.add(candidate.getUserId())) {
                result.add(
                        toLeaderboardEntry(candidate)
                );

                if (result.size()
                        >= LEADERBOARD_TOP_N) {
                    break;
                }
            }
        }

        return result;
    }

    private LeaderboardEntry toLeaderboardEntry(
            Exam exam
    ) {
        String displayName =
                exam.getFullName() != null
                        && !exam.getFullName().isBlank()
                        ? exam.getFullName()
                        : exam.getUsername() != null
                                && !exam.getUsername().isBlank()
                                ? exam.getUsername()
                                : exam.getUserId() != null
                                        ? "user_"
                                                + exam.getUserId()
                                                        .toString()
                                                        .substring(0, 6)
                                        : "Thí sinh";

        return new LeaderboardEntry(
                exam.getUserId(),
                exam.getUsername(),
                displayName,
                exam.getExamType(),
                exam.getScore(),
                exam.getCorrectCount(),
                exam.getTotalQuestions(),
                exam.getCompletedAt()
        );
    }

    // ==================== AUTO FINISH ====================

    @Scheduled(
            fixedRateString =
                    "${exam.auto-finish-interval-ms:30000}"
    )
    @Transactional
    public void autoFinishExpiredExams() {
        List<Exam> expired =
                examRepository
                        .findByStatusAndExpiresAtBefore(
                                ExamStatus.IN_PROGRESS,
                                Instant.now()
                        );

        for (Exam exam : expired) {
            finishTimeout(exam);
            log.info(
                    "Auto-finished expired exam: "
                            + "id={}, userId={}",
                    exam.getId(),
                    exam.getUserId()
            );
        }
    }

    // ==================== HELPERS ====================

    private void syncWrongQuestionProgress(
            Exam exam,
            List<ExamAnswer> answers
    ) {
        if (!CURRENT_BANK.equals(exam.getBankVersion())) {
            return;
        }

        for (ExamAnswer answer : answers) {
            if (answer.getSelectedAnswer() == null
                    || answer.getSelectedAnswer().isBlank()) {
                continue;
            }

            Instant answeredAt = answer.getAnsweredAt() != null
                    ? answer.getAnsweredAt()
                    : exam.getCompletedAt();

            wrongQuestionProgressService.recordAnswer(
                    exam.getUserId(),
                    exam.getExamType(),
                    exam.getBankVersion(),
                    answer.getQuestionId(),
                    answer.getSelectedAnswer(),
                    answeredAt
            );
        }
    }

    private void validateAnswerOption(
            Question question,
            String selectedAnswer
    ) {
        if (selectedAnswer == null || selectedAnswer.isBlank()) {
            throw BusinessException.of(
                    "INVALID_ANSWER_OPTION",
                    "Selected answer is not an option for this question",
                    HttpStatus.BAD_REQUEST
            );
        }

        JsonNode options;
        try {
            options = objectMapper.readTree(question.getOptions());
        } catch (JsonProcessingException | IllegalArgumentException error) {
            throw BusinessException.conflict(
                    "Question options are invalid: " + question.getId()
            );
        }

        if (options == null || !options.isArray()) {
            throw BusinessException.conflict(
                    "Question options are invalid: " + question.getId()
            );
        }

        for (JsonNode option : options) {
            JsonNode label = option.get("label");
            if (label != null && label.isTextual()
                    && selectedAnswer.equals(label.asText())) {
                return;
            }
        }

        throw BusinessException.of(
                "INVALID_ANSWER_OPTION",
                "Selected answer is not an option for this question",
                HttpStatus.BAD_REQUEST
        );
    }

    private Long remainingTimeMs(Exam exam) {
        if (exam.getStatus() == ExamStatus.PAUSED) {
            return exam.getRemainingTimeMs();
        }
        if (exam.getStatus() == ExamStatus.IN_PROGRESS
                && exam.getExpiresAt() != null) {
            return Math.max(
                    0L,
                    Duration.between(Instant.now(), exam.getExpiresAt()).toMillis()
            );
        }
        return 0L;
    }

    private boolean isExpired(Exam exam) {
        return exam.getExpiresAt() != null
                && !Instant.now().isBefore(exam.getExpiresAt());
    }

    private void finishTimeout(Exam exam) {
        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(
                        exam.getId()
                );

        int correct = (int) answers.stream()
                .filter(answer ->
                        Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        exam.setStatus(ExamStatus.TIMEOUT);
        exam.setCorrectCount(correct);
        exam.setWrongCount(answers.size() - correct);
        exam.setScore(
                calculateScore(correct, exam.getTotalQuestions())
        );
        exam.setCompletedAt(exam.getExpiresAt());

        examRepository.save(exam);
        syncWrongQuestionProgress(exam, answers);
        examProgressReceiptService.record(
                exam,
                answers,
                passedForProgress(exam, answers)
        );
    }

    private boolean passedForProgress(
            Exam exam,
            List<ExamAnswer> answers
    ) {
        if (exam.getCorrectCount() == null) {
            return false;
        }

        if (CURRENT_BANK.equals(exam.getBankVersion())) {
            return passed(exam, answers, Map.of());
        }

        if (exam.getCorrectCount() < LEGACY_REQUIRED_CORRECT) {
            return false;
        }

        Map<Integer, Question> byId = questionRepository
                .findAllById(exam.getQuestionIds())
                .stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        question -> question
                ));

        return passed(exam, answers, byId);
    }

    private QuestionResponse toSessionQuestion(
            Exam exam,
            Question question
    ) {
        if (question == null) {
            throw BusinessException.notFound(
                    "Exam question not found"
            );
        }

        QuestionResponse response =
                questionMapper.toResponse(question);

        boolean critical =
                CURRENT_BANK.equals(
                        exam.getBankVersion()
                )
                        ? Objects.equals(
                                exam.getCriticalQuestionId(),
                                question.getId()
                        )
                        : Boolean.TRUE.equals(
                                question.getIsCritical()
                        );

        boolean reveal = exam.getStatus() == ExamStatus.COMPLETED
                || exam.getStatus() == ExamStatus.TIMEOUT;

        return new QuestionResponse(
                response.id(),
                response.chapter(),
                response.questionText(),
                response.options(),
                critical,
                response.imageUrl(),
                response.mediaFileId(),
                reveal
                        ? response.correctAnswer()
                        : null,
                reveal
                        ? response.explanation()
                        : null
        );
    }

    private int calculateScore(
            int correct,
            int total
    ) {
        return (int) Math.round(
                (double) correct / total * 100
        );
    }

    private ExamResultResponse buildResultResponse(
            Exam exam,
            List<ExamAnswer> answers
    ) {
        List<Question> questions =
                questionRepository.findAllById(
                        exam.getQuestionIds()
                );

        Map<Integer, Question> byId =
                questions.stream()
                        .collect(
                                Collectors.toMap(
                                        Question::getId,
                                        question -> question
                                )
                        );

        Map<Integer, ExamAnswer> answersById = answers.stream()
                .collect(Collectors.toMap(
                        ExamAnswer::getQuestionId,
                        answer -> answer
                ));

        List<Map<String, Object>> answerDetails = new ArrayList<>();
        int answeredWrongCount = 0;
        int unansweredCount = 0;

        for (Integer questionId : exam.getQuestionIds()) {
            ExamAnswer answer = answersById.get(questionId);
            Question question = byId.get(questionId);

            if (answer == null || question == null) {
                throw BusinessException.conflict(
                        "Exam result is missing question " + questionId
                );
            }

            boolean unanswered = answer.getSelectedAnswer() == null
                    || answer.getSelectedAnswer().isBlank();
            if (unanswered) {
                unansweredCount++;
            } else if (!Boolean.TRUE.equals(answer.getIsCorrect())) {
                answeredWrongCount++;
            }

            Object options;

            try {
                options = objectMapper.readValue(
                        question.getOptions(),
                        Object.class
                );
            } catch (JsonProcessingException error) {
                options = question.getOptions();
            }

            Map<String, Object> detail =
                    new LinkedHashMap<>();

            detail.put(
                    "questionId",
                    question.getId()
            );
            detail.put(
                    "questionText",
                    question.getQuestionText()
            );
            detail.put(
                    "options",
                    options
            );
            detail.put(
                    "imageUrl",
                    question.getImageUrl()
            );
            detail.put(
                    "mediaFileId",
                    question.getMediaFileId()
            );
            detail.put(
                    "correctAnswer",
                    question.getCorrectAnswer()
            );
            detail.put(
                    "selectedAnswer",
                    answer.getSelectedAnswer() != null
                            ? answer.getSelectedAnswer()
                            : ""
            );
            detail.put(
                    "isCorrect",
                    answer.getIsCorrect() != null
                            ? answer.getIsCorrect()
                            : false
            );
            detail.put(
                    "isCritical",
                    CURRENT_BANK.equals(
                            exam.getBankVersion()
                    )
                            ? Objects.equals(
                                    exam.getCriticalQuestionId(),
                                    question.getId()
                            )
                            : Boolean.TRUE.equals(
                                    question.getIsCritical()
                            )
            );
            detail.put(
                    "explanation",
                    question.getExplanation() != null
                            ? question.getExplanation()
                            : ""
            );

            answerDetails.add(detail);
        }

        if (answerDetails.size() != exam.getTotalQuestions()
                || exam.getCorrectCount() == null
                || exam.getCorrectCount() + answeredWrongCount
                        + unansweredCount != exam.getTotalQuestions()) {
            throw BusinessException.conflict(
                    "Exam result counts do not match its questions"
            );
        }

        boolean passed =
                passed(exam, answers, byId);

        return new ExamResultResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                exam.getCorrectCount(),
                exam.getWrongCount(),
                exam.getScore(),
                passed,
                exam.getMode(),
                exam.getDurationMinutes(),
                answerDetails,
                exam.getStartedAt(),
                exam.getCompletedAt(),
                answeredWrongCount,
                unansweredCount
        );
    }

    private boolean passed(
            Exam exam,
            List<ExamAnswer> answers,
            Map<Integer, Question> byId
    ) {
        int required =
                CURRENT_BANK.equals(
                        exam.getBankVersion()
                )
                        ? ExamMatrix.forClass(
                                exam.getExamType()
                        ).requiredCorrect()
                        : LEGACY_REQUIRED_CORRECT;

        return exam.getCorrectCount() != null
                && exam.getCorrectCount() >= required
                && !hasWrongCritical(
                        exam,
                        answers,
                        byId
                );
    }

    private boolean hasWrongCritical(
            Exam exam,
            List<ExamAnswer> answers,
            Map<Integer, Question> byId
    ) {
        if (CURRENT_BANK.equals(
                exam.getBankVersion()
        )) {
            return answers.stream()
                    .anyMatch(answer ->
                            Objects.equals(
                                    answer.getQuestionId(),
                                    exam.getCriticalQuestionId()
                            )
                                    && !Boolean.TRUE.equals(
                                            answer.getIsCorrect()
                                    )
                    );
        }

        return answers.stream()
                .anyMatch(answer -> {
                    Question question =
                            byId.get(
                                    answer.getQuestionId()
                            );

                    return question != null
                            && Boolean.TRUE.equals(
                                    question.getIsCritical()
                            )
                            && !Boolean.TRUE.equals(
                                    answer.getIsCorrect()
                            );
                });
    }

    public record LeaderboardEntry(
            UUID userId,
            String username,
            String fullName,
            String examType,
            Integer score,
            Integer correctCount,
            Integer totalQuestions,
            Instant completedAt
    ) {}
}
