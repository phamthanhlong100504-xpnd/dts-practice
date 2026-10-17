package com.dts.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(name = "exam.progress-outbox.enabled", havingValue = "true")
public class LearningProgressOutboxWorker {

    private final JdbcTemplate jdbcTemplate;
    private final RestClient progressClient;
    private final String internalKey;

    public LearningProgressOutboxWorker(
            JdbcTemplate jdbcTemplate,
            RestClient.Builder builder,
            @Value("${exam.progress-outbox.progress-url}") String progressUrl,
            @Value("${exam.progress-outbox.internal-key}") String internalKey
    ) {
        if (internalKey == null || internalKey.isBlank()) {
            throw new IllegalStateException(
                    "EXAM_PROGRESS_INTERNAL_KEY is required when outbox is enabled");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        this.jdbcTemplate = jdbcTemplate;
        this.internalKey = internalKey;
        this.progressClient = builder.baseUrl(progressUrl)
                .requestFactory(factory).build();
    }

    @Scheduled(fixedDelayString = "${exam.progress-outbox.poll-ms:5000}")
    @Transactional
    public void publishPending() {
        List<PendingEvent> events = jdbcTemplate.query("""
                SELECT event_sequence, payload::text AS body, attempts
                FROM learning_progress_outbox
                WHERE published_at IS NULL
                  AND next_attempt_at <= NOW()
                ORDER BY event_sequence
                LIMIT 100
                FOR UPDATE SKIP LOCKED
                """,
                (rs, row) -> new PendingEvent(
                        rs.getLong("event_sequence"),
                        rs.getString("body"),
                        rs.getInt("attempts")));

        for (PendingEvent event : events) {
            try {
                progressClient.post()
                        .uri("/internal/v1/practice/question-progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-DTS-Internal-Key", internalKey)
                        .body(event.body())
                        .retrieve()
                        .toBodilessEntity();

                jdbcTemplate.update("""
                        UPDATE learning_progress_outbox
                        SET published_at = NOW(), last_error = NULL
                        WHERE event_sequence = ? AND published_at IS NULL
                        """, event.sequence());
            } catch (Exception error) {
                int attempts = event.attempts() + 1;
                long delay = Math.min(300L, 1L << Math.min(attempts, 8));
                jdbcTemplate.update("""
                        UPDATE learning_progress_outbox
                        SET attempts = ?,
                            next_attempt_at = NOW() + (? * INTERVAL '1 second'),
                            last_error = ?
                        WHERE event_sequence = ? AND published_at IS NULL
                        """, attempts, delay, error.getClass().getSimpleName(),
                        event.sequence());
                log.warn("Question progress delivery deferred, sequence={}, attempts={}",
                        event.sequence(), attempts);
            }
        }
    }

    private record PendingEvent(long sequence, String body, int attempts) {}
}
