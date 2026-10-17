package com.dts.practice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@ConditionalOnProperty(name="exam.exp-outbox.enabled",havingValue="true",matchIfMissing=true)
public class PracticeExpOutboxWorker {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper mapper;
    private final String topic;
    public PracticeExpOutboxWorker(JdbcTemplate jdbc,
            @Qualifier("practiceResultKafkaTemplate") KafkaTemplate<String,String> kafka,ObjectMapper mapper,
            @Value("${exam.result-outbox.topic:learning-results}") String topic) {
        this.jdbc=jdbc;this.kafka=kafka;this.mapper=mapper;this.topic=topic;
    }
    @Scheduled(scheduler="practiceExpScheduler",fixedDelayString="${exam.exp-outbox.poll-ms:5000}")
    @Transactional
    public void publishPending() {
        ensureSourcePolicy();
        var batch=jdbc.query("""
            SELECT event_id,user_id,payload::text body,attempts FROM practice_exp_outbox
            WHERE published_at IS NULL AND next_attempt_at<=NOW()
            ORDER BY (event_type='PRACTICE_EXP_SOURCE_READY') DESC,created_at,event_id
            LIMIT 10 FOR UPDATE SKIP LOCKED
            """,(rs,n)->new Pending(rs.getObject("event_id",UUID.class),rs.getObject("user_id",UUID.class),
                rs.getString("body"),rs.getInt("attempts")));
        for(var event:batch) {
            try {
                String key=event.user()==null?"dts-practice-exp-config":event.user().toString();
                kafka.send(topic,key,event.body()).get(10,TimeUnit.SECONDS);
                jdbc.update("UPDATE practice_exp_outbox SET published_at=NOW(),last_error=NULL WHERE event_id=?",event.id());
            } catch(Exception error) {
                if(error instanceof InterruptedException) Thread.currentThread().interrupt();
                int attempts=event.attempts()+1;
                jdbc.update("""
                    UPDATE practice_exp_outbox SET attempts=?,last_error=?,
                    next_attempt_at=NOW()+(?*INTERVAL '1 second') WHERE event_id=?
                    """,attempts,error.getClass().getSimpleName(),Math.min(300,1L<<Math.min(attempts,8)),event.id());
                log.warn("EXP source delivery deferred: event={}, attempts={}",event.id(),attempts);
                if(Thread.currentThread().isInterrupted()) break;
            }
        }
    }
    private void ensureSourcePolicy() {
        var state=jdbc.queryForObject("""
            SELECT capture_started_at,policy_revision,policy_hash,clock_timestamp() observed_at
            FROM practice_exp_capture_state WHERE singleton FOR UPDATE
            """,(rs,n)->new State(rs.getTimestamp(1).toInstant(),rs.getLong(2),rs.getString(3),rs.getTimestamp(4).toInstant()));
        if(state==null) throw new IllegalStateException("EXP source migration has not initialized capture");
        var counts=new TreeMap<String,Integer>();
        for(String license:ExamMatrix.supportedClasses()) counts.put(license,ExamMatrix.forClass(license).totalQuestions());
        String bank=CurrentQuestionBank.currentVersion();
        String hash=hash(bank+"\n"+json(counts));
        if(hash.equals(state.hash())) return;
        long revision=Math.addExact(state.revision(),1);
        UUID id=UUID.randomUUID();
        var payload=new LinkedHashMap<String,Object>();
        payload.put("eventId",id);payload.put("eventType","PRACTICE_EXP_SOURCE_READY");payload.put("eventVersion",1);
        payload.put("occurredAt",state.observed().toString());payload.put("sourceType","PRACTICE_CONFIG");
        payload.put("sourceId",id);payload.put("targetType","CONFIG");
        payload.put("metadata",Map.of("captureStartedAt",state.captured().toString(),"bankVersion",bank,
                "policyRevision",revision,"licenseQuestionCounts",counts));
        jdbc.update("""
            INSERT INTO practice_exp_outbox(event_id,event_type,payload)
            VALUES(?,'PRACTICE_EXP_SOURCE_READY',?::jsonb)
            """,id,json(payload));
        jdbc.update("UPDATE practice_exp_capture_state SET policy_revision=?,policy_hash=? WHERE singleton",revision,hash);
    }
    private String json(Object value) {
        try {return mapper.writeValueAsString(value);}
        catch(JsonProcessingException error) {throw new IllegalStateException("Cannot serialize EXP source evidence",error);}
    }
    private static String hash(String text) {
        try {return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException error) {throw new IllegalStateException(error);}
    }
    private record State(Instant captured,long revision,String hash,Instant observed) {}
    private record Pending(UUID id,UUID user,String body,int attempts) {}
}
