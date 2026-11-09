package com.dts.practice.integration;

import com.dts.practice.service.ExamMatrix;
import com.dts.practice.simulation.SimulationBankClient;
import com.dts.practice.simulation.SimulationBank;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Separate ordered simulation topic; no changes to theory producers or video services. */
@Service
@ConditionalOnProperty(name="simulation.performance.enabled",havingValue="true",matchIfMissing=true)
public class SimulationPerformanceWorker {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String,String> kafka;
    private final ObjectMapper json;
    private final SimulationBankClient banks;
    private final TransactionTemplate transaction;
    private final String topic;
    private final boolean learningEnabled;
    private volatile Instant nextRefresh=Instant.EPOCH;
    private volatile SimulationBank.Snapshot current;
    private volatile SimulationBank.Snapshot historical;
    private volatile boolean bankReady;
    public SimulationPerformanceWorker(JdbcTemplate jdbc,
            @Qualifier("practiceResultKafkaTemplate") KafkaTemplate<String,String> kafka,
            ObjectMapper json,SimulationBankClient banks,PlatformTransactionManager manager,
            @Value("${simulation.performance.topic:simulation-performance-v1}") String topic,
            @Value("${simulation.learning-enabled:true}") boolean enabled) {
        this.jdbc=jdbc;this.kafka=kafka;this.json=json;this.banks=banks;
        this.transaction=new TransactionTemplate(manager);this.topic=topic;this.learningEnabled=enabled;
    }
    @Scheduled(scheduler="simulationPerformanceScheduler",fixedDelayString="${simulation.performance.poll-ms:5000}")
    public void tick() {
        if(Instant.now().isAfter(nextRefresh)) {
            try {current=banks.current();bankReady=learningEnabled;}
            catch(com.dts.practice.exception.BusinessException unavailable) {bankReady=false;}
            historical=null;
            var missing=jdbc.queryForList("""
                SELECT k.bank_version FROM (
                  SELECT bank_version FROM simulation_attempts WHERE status='COMPLETED'
                  UNION SELECT bank_version FROM simulation_bookmarks
                  UNION SELECT bank_version FROM simulation_exam_sessions WHERE status='COMPLETED'
                )k WHERE NOT EXISTS(SELECT 1 FROM simulation_performance_catalogs c WHERE c.bank_version=k.bank_version)
                ORDER BY k.bank_version LIMIT 1
                """,String.class);
            if(!missing.isEmpty() && (current==null || !missing.getFirst().equals(current.bankVersion()))) {
                try {historical=banks.load(missing.getFirst());}
                catch(com.dts.practice.exception.BusinessException unavailable) { /* retain facts; denominator stays explicitly pending */ }
            }
            nextRefresh=Instant.now().plusSeconds(30);
        }
        // Release the source coordinator before waiting for Kafka. A broker outage must not
        // hold the transaction lock needed by a newly completed video or bookmark change.
        transaction.executeWithoutResult(status -> metadata());
        transaction.executeWithoutResult(status -> publish());
    }
    void metadata() {
        jdbc.queryForList("SELECT pg_advisory_xact_lock(773504,1)");
        var counts=new TreeMap<String,Integer>();
        for(String license:ExamMatrix.supportedClasses())
            if(!Set.of("A1","A","B1").contains(license)) counts.put(license,ExamMatrix.forClass(license).totalQuestions());
        String encoded=encode(counts);
        int changed=jdbc.update("UPDATE simulation_performance_source SET question_counts=?::jsonb,policy_revision=policy_revision+1 WHERE singleton AND question_counts<>?::jsonb",encoded,encoded);
        if(changed==1) emit("SIMULATION_POLICY_CHANGED",Map.of("expPolicyVersion","SIMULATION_EXP_V1","questionCounts",counts));
        if(current!=null) registerCatalog(current,true);
        if(historical!=null) registerCatalog(historical,false);
        Boolean due=jdbc.queryForObject("SELECT NOT EXISTS(SELECT 1 FROM simulation_performance_outbox WHERE event_type='SIMULATION_SYNC_MARKER' AND occurred_at>clock_timestamp()-INTERVAL '30 seconds')",Boolean.class);
        if(Boolean.TRUE.equals(due)) {
            String version=jdbc.queryForObject("SELECT current_bank_version FROM simulation_performance_source WHERE singleton",String.class);
            var marker=new LinkedHashMap<String,Object>();marker.put("bankReady",bankReady);marker.put("currentBankVersion",version);
            marker.put("expPolicyVersion","SIMULATION_EXP_V1");emit("SIMULATION_SYNC_MARKER",marker);
        }
    }
    private void registerCatalog(SimulationBank.Snapshot bank,boolean makeCurrent) {
            var catalog=new LinkedHashMap<String,Object>();
            catalog.put("bankVersion",bank.bankVersion());catalog.put("manifestSha256",bank.manifestSha256());
            catalog.put("learningScope","USER_BANK_VERSION_SHARED_LICENSES");
            catalog.put("achievementScope","DTS_SIMULATION_120_V1");
            catalog.put("chapters",bank.chapters());
            catalog.put("scenarios",bank.scenarios().stream().map(s->Map.of("scenarioId",s.scenarioId(),"scenarioNumber",s.scenarioNumber(),"chapterId",s.chapterId())).toList());
            String body=encode(catalog),hash=sha(body);
            var existing=jdbc.queryForList("SELECT catalog_sha256 FROM simulation_performance_catalogs WHERE bank_version=?",String.class,bank.bankVersion());
            if(!existing.isEmpty() && !existing.getFirst().equals(hash)) throw new IllegalStateException("Published simulation catalog changed under the same bankVersion");
            if(existing.isEmpty()) {
                jdbc.update("INSERT INTO simulation_performance_catalogs(bank_version,manifest_sha256,catalog_sha256,catalog) VALUES(?,?,?,?::jsonb)",bank.bankVersion(),bank.manifestSha256(),hash,body);
                emit("SIMULATION_CATALOG_PUBLISHED",catalog);
            }
        if(makeCurrent) jdbc.update("UPDATE simulation_performance_source SET current_bank_version=?,current_catalog_sha256=? WHERE singleton",bank.bankVersion(),hash);
    }
    void publish() {
        if(!Boolean.TRUE.equals(jdbc.queryForObject("SELECT pg_try_advisory_xact_lock(773504,2)",Boolean.class))) return;
        var batch=jdbc.query("SELECT event_sequence,payload::text FROM simulation_performance_outbox WHERE published_at IS NULL ORDER BY event_sequence LIMIT 100 FOR UPDATE",
                (r,n)->new Pending(r.getLong(1),r.getString(2)));
        for(var row:batch) {
            Boolean ready=jdbc.queryForObject("SELECT next_attempt_at<=clock_timestamp() FROM simulation_performance_outbox WHERE event_sequence=?",Boolean.class,row.sequence());
            if(!Boolean.TRUE.equals(ready)) break;
            try {
                // One stable key and one publisher transaction, preserving total source order.
                kafka.send(topic,"dts-simulation-performance-v1",row.body()).get(10,TimeUnit.SECONDS);
                jdbc.update("UPDATE simulation_performance_outbox SET published_at=clock_timestamp(),last_error=NULL WHERE event_sequence=?",row.sequence());
            } catch(Exception failure) {
                if(failure instanceof InterruptedException) Thread.currentThread().interrupt();
                jdbc.update("UPDATE simulation_performance_outbox SET attempts=attempts+1,last_error=?,next_attempt_at=clock_timestamp()+INTERVAL '10 seconds' WHERE event_sequence=?",failure.getClass().getSimpleName(),row.sequence());
                break; // Never publish higher sequences around a failed lower one.
            }
        }
    }
    private void emit(String type,Object data) {jdbc.queryForObject("SELECT dts_simulation_emit(?,clock_timestamp(),?::jsonb)",Long.class,type,encode(data));}
    private String encode(Object value) {try{return json.writer().with(com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS).writeValueAsString(value);}catch(Exception e){throw new IllegalStateException("Cannot encode simulation integration evidence");}}
    private static String sha(String value) {try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    private record Pending(long sequence,String body) {}
}
