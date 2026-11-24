package com.dts.practice.service;

import com.dts.practice.dto.response.ExamProgressReceiptResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamProgressReceiptService {

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final QuestionRepository questionRepository;
    private final RestClient progressClient;
    private final String internalKey;

    public ExamProgressReceiptService(
            ExamRepository examRepository,
            ExamAnswerRepository examAnswerRepository,
            QuestionRepository questionRepository,
            RestClient.Builder builder,
            @Value("${exam.progress-outbox.progress-url:${PROGRESS_SERVICE_URL:http://dts-progress:8083}}")
            String progressUrl,
            @Value("${exam.progress-outbox.internal-key:${EXAM_PROGRESS_INTERNAL_KEY:dts-internal-secret-progress-2024}}")
            String internalKey
    ) {
        this.examRepository = examRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.questionRepository = questionRepository;
        this.internalKey = internalKey;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);

        this.progressClient = builder != null
                ? builder.baseUrl(progressUrl != null ? progressUrl : "http://dts-progress:8083")
                         .requestFactory(factory)
                         .build()
                : null;
    }

    // Gọi trong cùng transaction kết thúc bài thi.
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            Exam exam,
            List<ExamAnswer> answers,
            Boolean passed
    ) {
        if (exam.getStatus() != ExamStatus.COMPLETED
                && exam.getStatus() != ExamStatus.TIMEOUT) {
            throw BusinessException.conflict("Exam is not finished");
        }

        if (exam.getCompletedAt() == null
                || exam.getStartedAt() == null
                || exam.getCorrectCount() == null
                || exam.getWrongCount() == null
                || exam.getTotalQuestions() == null
                || passed == null) {
            throw BusinessException.conflict(
                    "Exam progress is incomplete"
            );
        }

        int answeredWrong = (int) answers.stream()
                .filter(answer ->
                        answer.getSelectedAnswer() != null
                                && !answer.getSelectedAnswer().isBlank()
                                && !Boolean.TRUE.equals(
                                        answer.getIsCorrect()
                                )
                )
                .count();

        int unanswered = exam.getTotalQuestions()
                - exam.getCorrectCount()
                - answeredWrong;

        if (unanswered < 0) {
            throw BusinessException.conflict(
                    "Exam progress counts do not match"
            );
        }

        syncToProgressService(exam, answers, answeredWrong, unanswered, passed);
    }

    private void syncToProgressService(
            Exam exam,
            List<ExamAnswer> answers,
            int answeredWrong,
            int unanswered,
            Boolean passed
    ) {
        if (progressClient == null || internalKey == null || internalKey.isBlank()) {
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("eventId", exam.getId());
        payload.put("eventType", "EXAM_FINISHED");
        payload.put("schemaVersion", 1);
        payload.put("source", "dts-practice");
        payload.put("userId", exam.getUserId());
        payload.put("examId", exam.getId());
        payload.put("licenseClass", exam.getExamType());
        payload.put("bankVersion", exam.getBankVersion());
        payload.put("mode", exam.getMode());
        payload.put("status", exam.getStatus().name());
        payload.put("totalQuestions", exam.getTotalQuestions());
        payload.put("correctCount", exam.getCorrectCount());
        payload.put("wrongCountLegacy", exam.getWrongCount());
        payload.put("answeredWrongCount", answeredWrong);
        payload.put("unansweredCount", unanswered);
        payload.put("score", score(exam));
        payload.put("passed", passed);
        payload.put("durationSeconds", durationSeconds(exam));
        payload.put("startedAt", exam.getStartedAt());
        payload.put("completedAt", exam.getCompletedAt());

        try {
            progressClient.post()
                    .uri("/internal/v1/practice/exam-results")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-DTS-Internal-Key", internalKey)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully synced exam progress to dts-progress for exam {}", exam.getId());
        } catch (Exception ex) {
            log.warn("Non-fatal: could not sync exam progress to dts-progress for exam {}: {}",
                    exam.getId(), ex.getMessage());
        }

        // Sync individual wrong questions into user_question_progress with needs_review = true
        if (answers != null && !answers.isEmpty()) {
            List<Integer> wrongQuestionIds = answers.stream()
                    .filter(a -> a.getSelectedAnswer() != null && !a.getSelectedAnswer().isBlank() && !Boolean.TRUE.equals(a.getIsCorrect()))
                    .map(ExamAnswer::getQuestionId)
                    .toList();

            if (!wrongQuestionIds.isEmpty()) {
                try {
                    Map<Integer, Question> questionMap = questionRepository.findAllById(wrongQuestionIds).stream()
                            .collect(Collectors.toMap(Question::getId, q -> q));

                    for (ExamAnswer wrongAns : answers) {
                        if (wrongAns.getSelectedAnswer() == null || wrongAns.getSelectedAnswer().isBlank() || Boolean.TRUE.equals(wrongAns.getIsCorrect())) {
                            continue;
                        }
                        Question q = questionMap.get(wrongAns.getQuestionId());
                        int chapterId = (q != null && q.getChapter() != null) ? q.getChapter() : 1;
                        boolean isCritical = q != null && Boolean.TRUE.equals(q.getIsCritical());

                        Map<String, Object> qPayload = new HashMap<>();
                        qPayload.put("eventId", UUID.randomUUID());
                        qPayload.put("eventType", "QUESTION_PROGRESS_CHANGED");
                        qPayload.put("schemaVersion", 1);
                        qPayload.put("source", "dts-practice");
                        qPayload.put("eventSequence", System.currentTimeMillis());
                        qPayload.put("userId", exam.getUserId());
                        qPayload.put("licenseClass", exam.getExamType());
                        qPayload.put("bankVersion", exam.getBankVersion() != null ? exam.getBankVersion() : "dts-2026-600-v1");
                        qPayload.put("questionId", wrongAns.getQuestionId());
                        qPayload.put("chapterId", chapterId);
                        qPayload.put("critical", isCritical);
                        qPayload.put("dimension", "REVIEW");
                        qPayload.put("needsReview", true);
                        qPayload.put("studiedAt", exam.getCompletedAt() != null ? exam.getCompletedAt() : Instant.now());

                        progressClient.post()
                                .uri("/internal/v1/practice/question-progress")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-DTS-Internal-Key", internalKey)
                                .body(qPayload)
                                .retrieve()
                                .toBodilessEntity();
                    }
                    log.info("Successfully synced {} wrong questions to dts-progress for exam {}", wrongQuestionIds.size(), exam.getId());
                } catch (Exception ex) {
                    log.warn("Non-fatal: could not sync wrong questions to dts-progress: {}", ex.getMessage());
                }
            }
        }
    }

    // Cho phép lấy receipt của bài đã hoàn thành.
    @Transactional
    public ExamProgressReceiptResponse getReceipt(
            UUID examId,
            UUID userId,
            Boolean passed
    ) {
        Exam exam = examRepository
                .findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound(
                                "Exam not found"
                        )
                );

        if (exam.getStatus() != ExamStatus.COMPLETED
                && exam.getStatus() != ExamStatus.TIMEOUT) {
            throw BusinessException.conflict(
                    "Exam is not finished"
            );
        }

        List<ExamAnswer> answers = examAnswerRepository
                .findByExamIdOrderByQuestionId(examId);

        int answeredWrong = (int) answers.stream()
                .filter(answer ->
                        answer.getSelectedAnswer() != null
                                && !answer.getSelectedAnswer().isBlank()
                                && !Boolean.TRUE.equals(answer.getIsCorrect())
                )
                .count();

        int unanswered = Math.max(0, exam.getTotalQuestions() - exam.getCorrectCount() - answeredWrong);

        return new ExamProgressReceiptResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getBankVersion(),
                exam.getMode(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                exam.getCorrectCount(),
                exam.getWrongCount(),
                answeredWrong,
                unanswered,
                score(exam),
                passed,
                durationSeconds(exam),
                exam.getStartedAt(),
                exam.getCompletedAt()
        );
    }

    private int durationSeconds(Exam exam) {
        long allowedMs = exam.getDurationMinutes() == null
                ? Long.MAX_VALUE
                : Duration.ofMinutes(
                        exam.getDurationMinutes()
                ).toMillis();

        long usedMs;

        if (exam.getExpiresAt() != null
                && exam.getDurationMinutes() != null) {
            long remainingMs = Math.max(
                    0L,
                    Duration.between(
                            exam.getCompletedAt(),
                            exam.getExpiresAt()
                    ).toMillis()
            );

            usedMs = Math.max(
                    0L,
                    allowedMs - remainingMs
            );
        } else {
            usedMs = Math.max(
                    0L,
                    Duration.between(
                            exam.getStartedAt(),
                            exam.getCompletedAt()
                    ).toMillis()
            );
        }

        long seconds = Math.min(
                allowedMs,
                usedMs
        ) / 1000L;

        return (int) Math.min(
                Integer.MAX_VALUE,
                seconds
        );
    }

    private int score(Exam exam) {
        if (exam.getScore() != null) {
            return exam.getScore();
        }

        return exam.getTotalQuestions() == 0
                ? 0
                : (int) Math.round(
                        (double) exam.getCorrectCount()
                                / exam.getTotalQuestions()
                                * 100
                );
    }
}
