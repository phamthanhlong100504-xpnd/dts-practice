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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private static final String CURRENT_BANK = "dts-2026-600-v1";
    private static final int LEGACY_REQUIRED_CORRECT = 21;
    private static final int LEADERBOARD_TOP_N = 20;
    private static final int LEADERBOARD_WINDOW = 500;

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final ExamMapper examMapper;
    private final ObjectMapper objectMapper;

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

        exam = examRepository.save(exam);

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
                exam.getStartedAt()
        );
    }

    // ==================== GET SESSION ====================

    public ExamSessionResponse getExamSession(
            UUID examId,
            UUID userId
    ) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() == ExamStatus.IN_PROGRESS
                && isExpired(exam)) {
            finishTimeout(exam);
        }

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(
                        examId
                );

        long answeredCount = answers.stream()
                .filter(answer ->
                        answer.getSelectedAnswer() != null)
                .count();

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
                (int) answeredCount,
                exam.getDurationMinutes(),
                exam.getExpiresAt(),
                exam.getMode(),
                responses,
                exam.getStartedAt()
        );
    }

    // ==================== SUBMIT ANSWER ====================

    public SubmitAnswerResponse submitAnswer(
            UUID examId,
            UUID userId,
            SubmitAnswerRequest request
    ) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest(
                    "Exam is already " + exam.getStatus()
            );
        }

        if (isExpired(exam)) {
            // Worker hoặc getExamSession sẽ kết thúc lượt thi
            // trong một transaction riêng.
            throw BusinessException.badRequest(
                    "Exam time has expired"
            );
        }

        Integer questionId =
                Integer.valueOf(request.questionId());

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

        answer.setSelectedAnswer(request.selectedAnswer());
        answer.setIsCorrect(
                question.getCorrectAnswer()
                        .equals(request.selectedAnswer())
        );
        answer.setAnsweredAt(Instant.now());
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
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest(
                    "Exam is already " + exam.getStatus()
            );
        }

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(
                        examId
                );

        int correct = (int) answers.stream()
                .filter(answer ->
                        Boolean.TRUE.equals(answer.getIsCorrect()))
                .count();

        int wrong = answers.size() - correct;

        exam.setStatus(ExamStatus.COMPLETED);
        exam.setCorrectCount(correct);
        exam.setWrongCount(wrong);
        exam.setScore(
                calculateScore(
                        correct,
                        exam.getTotalQuestions()
                )
        );
        exam.setCompletedAt(Instant.now());
        examRepository.save(exam);

        return buildResultResponse(exam, answers);
    }

    // ==================== GET RESULT ====================

    @Transactional(readOnly = true)
    public ExamResultResponse getExamResult(
            UUID examId,
            UUID userId
    ) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() == ExamStatus.IN_PROGRESS) {
            throw BusinessException.badRequest(
                    "Exam is still in progress"
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

                    if (exam.getStatus()
                            == ExamStatus.IN_PROGRESS) {
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

    private boolean isExpired(Exam exam) {
        return exam.getExpiresAt() != null
                && Instant.now()
                        .isAfter(exam.getExpiresAt());
    }

    private void finishTimeout(Exam exam) {
        List<ExamAnswer> answers =
                examAnswerRepository
                        .findByExamIdOrderByQuestionId(
                                exam.getId()
                        );

        int correct = (int) answers.stream()
                .filter(answer ->
                        Boolean.TRUE.equals(
                                answer.getIsCorrect()
                        )
                )
                .count();

        exam.setStatus(ExamStatus.TIMEOUT);
        exam.setCorrectCount(correct);
        exam.setWrongCount(
                answers.size() - correct
        );
        exam.setScore(
                calculateScore(
                        correct,
                        exam.getTotalQuestions()
                )
        );
        exam.setCompletedAt(exam.getExpiresAt());

        examRepository.save(exam);
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

        boolean reveal =
                exam.getStatus()
                        != ExamStatus.IN_PROGRESS;

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

        List<Map<String, Object>> answerDetails =
                new ArrayList<>();

        for (ExamAnswer answer : answers) {
            Question question =
                    byId.get(answer.getQuestionId());

            if (question == null) {
                continue;
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
                exam.getCompletedAt()
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