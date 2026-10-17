package com.dts.practice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty(name="exam.result-outbox.enabled",havingValue="true",matchIfMissing=true)
public class PracticeResultOutboxWorker {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String,String> kafka;
    private final String topic;
    public PracticeResultOutboxWorker(JdbcTemplate jdbc,
            @Qualifier("practiceResultKafkaTemplate") KafkaTemplate<String,String> kafka,
            @Value("${exam.result-outbox.topic:learning-results}") String topic) {
        this.jdbc=jdbc;this.kafka=kafka;this.topic=topic;
    }
    @Scheduled(scheduler="practiceHistoryScheduler",fixedDelayString="${exam.result-outbox.poll-ms:5000}")
    @Transactional
    public void publishPending() {
        var batch=jdbc.query("""
            SELECT event_id,user_id,payload::text AS payload,attempts FROM practice_result_outbox
            WHERE published_at IS NULL AND next_attempt_at<=NOW()
            ORDER BY created_at,event_id LIMIT 10 FOR UPDATE SKIP LOCKED
            """,(rs,row)->new Pending(rs.getObject("event_id",UUID.class),rs.getObject("user_id",UUID.class),
                rs.getString("payload"),rs.getInt("attempts")));
        for(var event:batch) {
            try {
                kafka.send(topic,event.userId().toString(),event.payload()).get(10,TimeUnit.SECONDS);
                jdbc.update("UPDATE practice_result_outbox SET published_at=NOW(),last_error=NULL WHERE event_id=?",event.id());
            } catch(Exception failure) {
                if(failure instanceof InterruptedException) Thread.currentThread().interrupt();
                int attempts=event.attempts()+1;
                jdbc.update("""
                    UPDATE practice_result_outbox SET attempts=?,last_error=?,
                    next_attempt_at=NOW()+(? * INTERVAL '1 second') WHERE event_id=?
                    """,attempts,failure.getClass().getSimpleName(),Math.min(300,1L<<Math.min(attempts,8)),event.id());
                log.warn("Practice result outbox deferred: event={}, attempts={}",event.id(),attempts);
                if(Thread.currentThread().isInterrupted()) break;
            }
        }
    }
    private record Pending(UUID id,UUID userId,String payload,int attempts) {}
}
