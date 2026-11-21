package com.dts.practice.service;

import com.dts.practice.entity.Exam;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.PresetExamSetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class PracticeResultEventPublisher {

    private final ExamRepository examRepository;
    private final PresetExamSetRepository presetExamSetRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PracticeResultEventPublisher(
            ExamRepository examRepository,
            PresetExamSetRepository presetExamSetRepository,
            @Qualifier("practiceResultKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${exam.result-outbox.topic:learning-results}") String topic
    ) {
        this.examRepository = examRepository;
        this.presetExamSetRepository = presetExamSetRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    /**
     * Publishes completed exam results to Result Service via Kafka.
     * Guarantees non-blocking, exception-safe behavior.
     */
    public void publishExamResult(Exam exam, boolean passed) {
        if (exam == null || exam.getId() == null || exam.getUserId() == null) {
            return;
        }

        try {
            UUID targetId = computeTargetId(exam);
            Integer attemptNo = exam.getHistoryAttemptNo();
            if (attemptNo == null || attemptNo < 1) {
                int previousAttempts = examRepository.countByUserIdAndHistoryTargetIdAndCompletedAtIsNotNull(
                        exam.getUserId(),
                        targetId
                );
                attemptNo = previousAttempts + 1;
            }

            exam.setHistoryTargetId(targetId);
            exam.setHistoryAttemptNo(attemptNo);
            examRepository.save(exam);

            String displayTitle = resolveDisplayTitle(exam);
            int durationSeconds = computeDurationSeconds(exam);
            Instant completedAt = exam.getCompletedAt() != null ? exam.getCompletedAt() : Instant.now();
            Instant startedAt = exam.getStartedAt() != null ? exam.getStartedAt() : completedAt;
            if (completedAt.isBefore(startedAt)) {
                completedAt = startedAt;
            }

            int correctCount = exam.getCorrectCount() != null ? exam.getCorrectCount() : 0;
            int totalQuestions = exam.getTotalQuestions() != null && exam.getTotalQuestions() > 0
                    ? exam.getTotalQuestions()
                    : Math.max(correctCount, 25);
            BigDecimal score = BigDecimal.valueOf(exam.getScore() != null ? exam.getScore() : 0);

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventId", exam.getId());
            payload.put("eventType", "LEARNING_RESULT_CREATED");
            payload.put("eventVersion", 2);
            payload.put("occurredAt", completedAt.toString());
            payload.put("userId", exam.getUserId());
            payload.put("sourceType", "PRACTICE_SESSION");
            payload.put("sourceId", exam.getId());
            payload.put("targetType", "EXAM");
            payload.put("targetId", targetId);
            payload.put("attemptNo", attemptNo);
            payload.put("activityType", exam.getPresetExamSetId() == null ? "RANDOM_EXAM" : "PRESET_EXAM");
            payload.put("displayTitle", displayTitle);
            payload.put("licenseClass", exam.getExamType());
            payload.put("result", passed ? "PASSED" : "FAILED");
            payload.put("score", score);
            payload.put("maxScore", BigDecimal.valueOf(100));
            payload.put("progress", 100.0);
            payload.put("correctCount", correctCount);
            payload.put("questionCount", totalQuestions);
            payload.put("durationSeconds", durationSeconds);
            payload.put("startedAt", startedAt.toString());
            payload.put("completedAt", completedAt.toString());
            payload.put("resultAvailable", true);
            payload.put("reviewAvailable", true);

            Map<String, Object> resultSnapshot = new LinkedHashMap<>();
            resultSnapshot.put("totalQuestions", totalQuestions);
            resultSnapshot.put("correctCount", correctCount);
            resultSnapshot.put("licenseClass", exam.getExamType());
            if (exam.getPresetExamSetId() != null) {
                resultSnapshot.put("presetExamSetId", exam.getPresetExamSetId());
            }
            if (exam.getBankVersion() != null) {
                resultSnapshot.put("bankVersion", exam.getBankVersion());
            }
            payload.put("resultSnapshot", resultSnapshot);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("mode", exam.getMode());
            if (exam.getBankVersion() != null) {
                metadata.put("bankVersion", exam.getBankVersion());
            }
            payload.put("metadata", metadata);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, exam.getUserId().toString(), jsonPayload);
            log.info("Published LearningResultEvent to Kafka topic={}: examId={}, userId={}, attemptNo={}",
                    topic, exam.getId(), exam.getUserId(), attemptNo);
        } catch (Exception e) {
            log.warn("Failed to publish LearningResultEvent for examId={}: {}", exam.getId(), e.getMessage());
        }
    }

    public UUID computeTargetId(Exam exam) {
        if (exam.getPresetExamSetId() != null) {
            return exam.getPresetExamSetId();
        }
        String bank = exam.getBankVersion() != null ? exam.getBankVersion() : "legacy";
        String seed = "dts-practice:random:" + bank + ":" + exam.getExamType();
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(seed.getBytes(StandardCharsets.UTF_8));
            ByteBuffer bb = ByteBuffer.wrap(md5);
            long mostSig = bb.getLong();
            long leastSig = bb.getLong();
            return new UUID(mostSig, leastSig);
        } catch (Exception e) {
            return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
        }
    }

    public int computeDurationSeconds(Exam exam) {
        if (exam.getStartedAt() == null || exam.getCompletedAt() == null) {
            return 0;
        }
        long seconds = Math.max(0, Duration.between(exam.getStartedAt(), exam.getCompletedAt()).toSeconds());
        if (exam.getDurationMinutes() != null && exam.getDurationMinutes() > 0) {
            long maxSeconds = exam.getDurationMinutes() * 60L;
            if (exam.getExpiresAt() != null) {
                long remaining = Math.max(0, Duration.between(exam.getCompletedAt(), exam.getExpiresAt()).toSeconds());
                seconds = Math.min(maxSeconds, Math.max(0, maxSeconds - remaining));
            } else {
                seconds = Math.min(maxSeconds, seconds);
            }
        }
        return (int) seconds;
    }

    private String resolveDisplayTitle(Exam exam) {
        if (exam.getPresetExamSetId() != null) {
            Optional<PresetExamSet> preset = presetExamSetRepository.findById(exam.getPresetExamSetId());
            if (preset.isPresent() && preset.get().getTitle() != null && !preset.get().getTitle().isBlank()) {
                return preset.get().getTitle();
            }
            return "Đề thi " + exam.getExamType();
        }
        return "Thi thử ngẫu nhiên - " + exam.getExamType();
    }

    /**
     * Backfill recent completed exams (last 7 days) that might have been finished
     * during the period when result outbox was disabled.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void backfillRecentCompletedExams() {
        try {
            Instant since = Instant.now().minus(7, ChronoUnit.DAYS);
            List<Exam> recent = examRepository.findByStatusInAndCompletedAtAfterOrderByCompletedAtAsc(
                    List.of(ExamStatus.COMPLETED, ExamStatus.TIMEOUT),
                    since
            );
            if (recent.isEmpty()) {
                return;
            }
            log.info("Checking {} recent completed exams for result synchronization", recent.size());
            for (Exam exam : recent) {
                boolean passed = exam.getScore() != null && exam.getScore() >= 80;
                publishExamResult(exam, passed);
            }
        } catch (Exception e) {
            log.warn("Recent exam result backfill skipped: {}", e.getMessage());
        }
    }
}
