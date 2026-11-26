package com.dts.practice.dto.event.learning;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAttemptSubmittedKafkaEvent {
    private UUID eventId;
    @Builder.Default
    private String eventType = "EXAM_ATTEMPT_SUBMITTED";
    @Builder.Default
    private Integer schemaVersion = 1;
    private String sourceService;
    private Instant occurredAt;
    private UUID userId;
    private String licenseClass;
    private String bankVersion;
    private String activityType; // THEORY_EXAM, SIMULATION
    private UUID sessionId;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String sourceOutcomeKey;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Integer practiceQuestionId;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private UUID contentBuilderQuestionId;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Integer scenarioNumber;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private UUID scenarioId;

        private String outcome; // CORRECT, INCORRECT, UNANSWERED

        @JsonInclude(JsonInclude.Include.ALWAYS)
        private Integer score;

        @JsonInclude(JsonInclude.Include.ALWAYS)
        private Integer firstResponseMs;

        @JsonInclude(JsonInclude.Include.ALWAYS)
        private Integer durationMs;

        private Instant answeredAt;
    }
}
