package com.dts.practice.service;

import com.dts.practice.dto.event.learning.ExamAttemptSubmittedKafkaEvent;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.Question;
import com.dts.practice.repository.QuestionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LearningProgressEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final String topic;
    private final boolean enabled;

    public LearningProgressEventPublisher(
            @Qualifier("practiceResultKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            QuestionRepository questionRepository,
            ObjectMapper objectMapper,
            @Value("${practice.learning-progress.topic:dts.practice.learning-progress.v1}") String topic,
            @Value("${practice.learning-progress.enabled:true}") boolean enabled
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper != null ? objectMapper.copy().findAndRegisterModules() : new ObjectMapper().findAndRegisterModules();
        this.topic = topic;
        this.enabled = enabled;
    }

    /**
     * 1. Lý thuyết (Theory Exam Attempt Submitted)
     */
    public void publishTheoryExamAttempt(Exam exam, List<ExamAnswer> answers) {
        if (!enabled || exam == null || exam.getUserId() == null) {
            return;
        }

        try {
            List<Integer> qIds = answers != null ? answers.stream()
                    .map(ExamAnswer::getQuestionId)
                    .filter(Objects::nonNull)
                    .toList() : Collections.emptyList();

            Map<Integer, Question> qMap = qIds.isEmpty() ? Collections.emptyMap() :
                    questionRepository.findAllById(qIds).stream()
                            .collect(Collectors.toMap(Question::getId, Function.identity(), (a, b) -> a));

            String sessionPrefix = exam.getId().toString().substring(0, 8);
            Instant occurredAt = exam.getCompletedAt() != null ? exam.getCompletedAt() : Instant.now();

            List<ExamAttemptSubmittedKafkaEvent.Item> items = new ArrayList<>();
            if (answers != null) {
                for (ExamAnswer a : answers) {
                    Question q = qMap.get(a.getQuestionId());
                    UUID cbId = q != null ? q.getContentBuilderQuestionId() : null;

                    String outcome;
                    if (a.getSelectedAnswer() == null || a.getSelectedAnswer().isBlank()) {
                        outcome = "UNANSWERED";
                    } else if (Boolean.TRUE.equals(a.getIsCorrect())) {
                        outcome = "CORRECT";
                    } else {
                        outcome = "INCORRECT";
                    }

                    items.add(ExamAttemptSubmittedKafkaEvent.Item.builder()
                            .sourceOutcomeKey("exam-" + sessionPrefix + "-q-" + a.getQuestionId())
                            .practiceQuestionId(a.getQuestionId())
                            .contentBuilderQuestionId(cbId)
                            .outcome(outcome)
                            .score(null)
                            .firstResponseMs(null)
                            .durationMs(null)
                            .answeredAt(a.getAnsweredAt() != null ? a.getAnsweredAt() : occurredAt)
                            .build());
                }
            }

            ExamAttemptSubmittedKafkaEvent event = ExamAttemptSubmittedKafkaEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType("EXAM_ATTEMPT_SUBMITTED")
                    .schemaVersion(1)
                    .sourceService("dts-practice")
                    .occurredAt(occurredAt)
                    .userId(exam.getUserId())
                    .licenseClass(exam.getExamType())
                    .bankVersion(exam.getBankVersion() != null ? exam.getBankVersion() : "dts-2026-600-v1")
                    .activityType("THEORY_EXAM")
                    .sessionId(exam.getId())
                    .items(items)
                    .build();

            sendEvent(event, exam.getUserId());
        } catch (Exception e) {
            log.warn("Lỗi tạo event THEORY_EXAM cho examId={}: {}", exam.getId(), e.getMessage());
        }
    }

    /**
     * 2. Trường hợp A: Nộp bài thi thử mô phỏng (Bộ 10 tình huống)
     */
    public void publishSimulationExam(UUID sessionId, UUID userId, String licenseClass, String bankVersion,
                                      Instant completedAt, List<ExamAttemptSubmittedKafkaEvent.Item> items) {
        if (!enabled || userId == null) {
            return;
        }

        try {
            ExamAttemptSubmittedKafkaEvent event = ExamAttemptSubmittedKafkaEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType("EXAM_ATTEMPT_SUBMITTED")
                    .schemaVersion(1)
                    .sourceService("dts-simulation-service")
                    .occurredAt(completedAt != null ? completedAt : Instant.now())
                    .userId(userId)
                    .licenseClass(licenseClass)
                    .bankVersion(bankVersion)
                    .activityType("SIMULATION")
                    .sessionId(sessionId)
                    .items(items != null ? items : Collections.emptyList())
                    .build();

            sendEvent(event, userId);
        } catch (Exception e) {
            log.warn("Lỗi tạo event SIMULATION (exam) cho sessionId={}: {}", sessionId, e.getMessage());
        }
    }

    /**
     * 3. Trường hợp B: Luyện tập đơn lẻ từng tình huống mô phỏng (Space reaction)
     */
    public void publishSimulationPractice(UUID attemptId, UUID userId, String licenseClass, String bankVersion,
                                          int scenarioNumber, int score, Integer firstResponseMs, int durationMs,
                                          Instant answeredAt) {
        if (!enabled || userId == null) {
            return;
        }

        try {
            long epoch = answeredAt != null ? answeredAt.getEpochSecond() : Instant.now().getEpochSecond();
            String userPrefix = userId.toString().substring(0, 8);
            String outcome = score > 0 ? "CORRECT" : "INCORRECT";

            ExamAttemptSubmittedKafkaEvent.Item item = ExamAttemptSubmittedKafkaEvent.Item.builder()
                    .sourceOutcomeKey("sim-practice-" + userPrefix + "-scenario-" + scenarioNumber + "-" + epoch)
                    .scenarioNumber(scenarioNumber)
                    .score(score)
                    .firstResponseMs(firstResponseMs)
                    .durationMs(durationMs)
                    .outcome(outcome)
                    .answeredAt(answeredAt != null ? answeredAt : Instant.now())
                    .build();

            ExamAttemptSubmittedKafkaEvent event = ExamAttemptSubmittedKafkaEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventType("EXAM_ATTEMPT_SUBMITTED")
                    .schemaVersion(1)
                    .sourceService("dts-simulation-service")
                    .occurredAt(answeredAt != null ? answeredAt : Instant.now())
                    .userId(userId)
                    .licenseClass(licenseClass)
                    .bankVersion(bankVersion)
                    .activityType("SIMULATION")
                    .sessionId(attemptId)
                    .items(List.of(item))
                    .build();

            sendEvent(event, userId);
        } catch (Exception e) {
            log.warn("Lỗi tạo event SIMULATION (practice) cho attemptId={}: {}", attemptId, e.getMessage());
        }
    }

    private void sendEvent(ExamAttemptSubmittedKafkaEvent event, UUID userId) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, userId.toString(), json);
            log.info("Đã gửi event sang Kafka topic={} (activityType={}, userId={}, sessionId={}, items={})",
                    topic, event.getActivityType(), userId, event.getSessionId(), event.getItems() != null ? event.getItems().size() : 0);
        } catch (Exception e) {
            log.warn("Không thể gửi event sang Kafka topic {}: {}", topic, e.getMessage());
        }
    }
}
