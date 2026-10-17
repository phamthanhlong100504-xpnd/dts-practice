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
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(
        name = "exam.progress-outbox.enabled",
        havingValue = "true"
)
public class ExamProgressOutboxWorker {

    private final JdbcTemplate jdbcTemplate;
    private final RestClient progressClient;
    private final String internalKey;

    public ExamProgressOutboxWorker(
            JdbcTemplate jdbcTemplate,
            RestClient.Builder builder,
            @Value("${exam.progress-outbox.progress-url}")
            String progressUrl,
            @Value("${exam.progress-outbox.internal-key}")
            String internalKey
    ) {
        if (internalKey == null || internalKey.isBlank()) {
            throw new IllegalStateException(
                    "EXAM_PROGRESS_INTERNAL_KEY is required when outbox is enabled"
            );
        }

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);

        this.jdbcTemplate = jdbcTemplate;
        this.internalKey = internalKey;
        this.progressClient = builder
                .baseUrl(progressUrl)
                .requestFactory(factory)
                .build();
    }

    @Scheduled(
            fixedDelayString = "${exam.progress-outbox.poll-ms:5000}"
    )
    @Transactional
    public void publishPending() {
        // Các instance Practice khác bỏ qua những hàng đã được khóa.
        List<PendingEvent> events = jdbcTemplate.query("""
                SELECT
                    event_id,
                    exam_id,
                    payload::text AS body,
                    attempts
                FROM exam_progress_outbox
                WHERE published_at IS NULL
                  AND next_attempt_at <= NOW()
                ORDER BY created_at, event_id
                LIMIT 10
                FOR UPDATE SKIP LOCKED
                """,
                (rs, row) -> new PendingEvent(
                        rs.getObject("event_id", UUID.class),
                        rs.getObject("exam_id", UUID.class),
                        rs.getString("body"),
                        rs.getInt("attempts")
                )
        );

        for (PendingEvent event : events) {
            try {
                progressClient.post()
                        .uri("/internal/v1/practice/exam-results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(
                                "X-DTS-Internal-Key",
                                internalKey
                        )
                        .body(event.body())
                        .retrieve()
                        .toBodilessEntity();

                jdbcTemplate.update("""
                        UPDATE exam_progress_outbox
                        SET published_at = NOW(),
                            last_error = NULL
                        WHERE event_id = ?
                          AND published_at IS NULL
                        """,
                        event.eventId()
                );
            } catch (Exception error) {
                int attempts = event.attempts() + 1;
                long delaySeconds = Math.min(
                        300L,
                        1L << Math.min(attempts, 8)
                );

                jdbcTemplate.update("""
                        UPDATE exam_progress_outbox
                        SET attempts = ?,
                            next_attempt_at =
                                NOW() + (? * INTERVAL '1 second'),
                            last_error = ?
                        WHERE event_id = ?
                          AND published_at IS NULL
                        """,
                        attempts,
                        delaySeconds,
                        safeMessage(error),
                        event.eventId()
                );

                log.warn(
                        "Progress outbox delivery deferred for examId={}, attempts={}",
                        event.examId(),
                        attempts
                );
            }
        }
    }

    private static String safeMessage(Exception error) {
        // Không lưu request body hoặc internal key vào DB/log.
        return error.getClass().getSimpleName();
    }

    private record PendingEvent(
            UUID eventId,
            UUID examId,
            String body,
            int attempts
    ) {}
}