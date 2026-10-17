package com.dts.practice.repository;

import com.dts.practice.service.PracticeExpOutboxWorker;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** The real V1..V30 chain, graded-answer triggers and durable publisher in an owned database. */
@Testcontainers
@EnabledIfEnvironmentVariable(named="DTS_SCREEN14_16_POSTGRES_TEST",matches="true")
class Screen14QuestionExpPostgresTest {
    @Container static final PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("screen14_question_test").withUsername("test").withPassword("test");
    static JdbcTemplate jdbc;
    static TransactionTemplate transaction;
    static Map<String,String> before;
    static String oldHistory;
    static int oldRank,question;
    static final String BANK="screen14-question-test";
    static final UUID OLD=UUID.fromString("14000000-0000-0000-0000-000000000001");
    UUID user;

    static Flyway flyway(String target) {
        var builder=Flyway.configure().dataSource(postgres.getJdbcUrl(),"test","test").locations("classpath:db/migration");
        if(target!=null) builder.target(target);
        return builder.load();
    }
    @BeforeAll static void upgrade() {
        flyway("29").migrate();
        var data=new DriverManagerDataSource(postgres.getJdbcUrl(),"test","test");
        jdbc=new JdbcTemplate(data);transaction=new TransactionTemplate(new DataSourceTransactionManager(data));
        question=jdbc.queryForObject("""
            INSERT INTO questions(chapter,question_text,options,correct_answer,bank_version,
              source_question_id,applicable_licenses,critical_licenses)
            VALUES(1,'Owned EXP test','{"A":"yes","B":"no"}'::jsonb,'A',?,140001,'["A1","B"]','["A1"]') RETURNING id
            """,Integer.class,BANK);
        chapter(OLD,true,Instant.parse("2020-01-01T00:00:00Z"));
        before=new TreeMap<>();
        for(String table:jdbc.queryForList("SELECT tablename FROM pg_tables WHERE schemaname='public' AND tablename<>'flyway_schema_history' ORDER BY tablename",String.class))
            before.put(table,digest(table));
        oldRank=jdbc.queryForObject("SELECT max(installed_rank) FROM flyway_schema_history",Integer.class);
        oldHistory=history();
        flyway(null).migrate();flyway(null).validate();flyway(null).migrate();
        // Verify immediately, before individual tests create their own new answer fixtures.
        before.forEach((table,hash)->assertEquals(hash,digest(table),"V30 rewrote "+table));
        assertEquals(oldHistory,history());
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM practice_exp_outbox",Long.class));
    }
    static String digest(String table) {
        if(!table.matches("[a-z_]+")) throw new IllegalArgumentException("Unexpected fixture table");
        return jdbc.queryForObject("SELECT md5(COALESCE(jsonb_agg(to_jsonb(t) ORDER BY to_jsonb(t)::text)::text,'[]')) FROM "+table+" t",String.class);
    }
    static String history() {return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE installed_rank<=?",String.class,oldRank);}
    @BeforeEach void account() {user=UUID.randomUUID();}
    static void chapter(UUID who,boolean correct,Instant when) {
        jdbc.update("""
            INSERT INTO chapter_question_progress(user_id,license_class,bank_version,question_id,chapter_id,
              selected_answer,is_correct,last_answered_at) VALUES(?,'A1',?,?,1,?,?,?)
            ON CONFLICT(user_id,license_class,bank_version,question_id) DO UPDATE
            SET selected_answer=EXCLUDED.selected_answer,is_correct=EXCLUDED.is_correct,last_answered_at=EXCLUDED.last_answered_at
            """,who,BANK,question,correct?"A":"B",correct,Timestamp.from(when));
    }
    long count() {return jdbc.queryForObject("SELECT count(*) FROM practice_exp_outbox WHERE user_id=?",Long.class,user);}
    static Instant now() {return jdbc.queryForObject("SELECT clock_timestamp()",Timestamp.class).toInstant();}

    @Test void migrationIsAdditiveRepeatableAndDoesNotInventHistoricalCorrectAnswers() {
        assertEquals(oldHistory,history());
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE version='30' AND success",Long.class));
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM practice_exp_outbox WHERE user_id=?",Long.class,OLD));
        assertEquals(5L,jdbc.queryForObject("SELECT count(*) FROM pg_trigger WHERE tgname IN ('trg_chapter_correct_exp','trg_practice_mode_correct_exp','trg_chapter_review_correct_exp','trg_critical_correct_exp','trg_wrong_review_correct_exp') AND NOT tgisinternal",Long.class));
    }
    @Test void wrongThenCorrectQueuesOnlyCorrectEvidenceAndNoOpRetryKeepsEventId() throws Exception {
        var when=now();chapter(user,false,when);assertEquals(0,count());
        chapter(user,true,when.plusSeconds(1));assertEquals(1,count());
        String event=jdbc.queryForObject("SELECT payload::text FROM practice_exp_outbox WHERE user_id=?",String.class,user);
        chapter(user,true,when.plusSeconds(1));assertEquals(1,count());
        assertEquals(event,jdbc.queryForObject("SELECT payload::text FROM practice_exp_outbox WHERE user_id=?",String.class,user));
        var node=new ObjectMapper().readTree(event);
        assertEquals("QUESTION_CORRECT_RECORDED",node.path("eventType").asText());
        assertEquals(question,node.path("metadata").path("questionId").asInt());
        assertEquals("CHAPTER_LEARNING",node.path("metadata").path("context").asText());
        assertFalse(node.has("correctAnswer"));assertFalse(node.has("selectedAnswer"));
    }
    @Test void allFiveGradedContextsEmitEvidenceButRandomExamAnswersDoNot() {
        chapter(user,true,now());
        session("chapter_review_sessions","chapter_review_session_questions","chapter_review_session_answers",false);
        session("critical_question_sessions","critical_question_session_questions","critical_question_session_answers",true);
        session("wrong_question_review_sessions","wrong_question_review_session_questions","wrong_question_review_answers",false);
        exam("PRACTICE");exam("EXAM");
        assertEquals(5,count());
        assertEquals(Set.of("CHAPTER_LEARNING","CHAPTER_REVIEW","CRITICAL_PRACTICE","WRONG_REVIEW","PRACTICE_MODE"),
            new HashSet<>(jdbc.queryForList("SELECT payload->'metadata'->>'context' FROM practice_exp_outbox WHERE user_id=?",String.class,user)));
    }
    void session(String sessions,String questions,String answers,boolean critical) {
        UUID id=UUID.randomUUID();
        String extra=sessions.startsWith("chapter")?",chapter_id":critical?",mode":"";
        String values=sessions.startsWith("chapter")?",1":critical?",'REVIEW'":"";
        jdbc.update("INSERT INTO "+sessions+"(id,user_id,license_class,bank_version,total_questions,current_question_id"+extra+") VALUES(?,?,'A1',?,1,?"+values+")",id,user,BANK,question);
        jdbc.update("INSERT INTO "+questions+"(session_id,question_id,question_order) VALUES(?,?,1)",id,question);
        jdbc.update("INSERT INTO "+answers+"(session_id,question_id,selected_answer,is_correct,answered_at) VALUES(?,?,'A',TRUE,clock_timestamp())",id,question);
    }
    void exam(String mode) {
        UUID id=UUID.randomUUID();
        jdbc.update("INSERT INTO exams(id,user_id,exam_type,bank_version,mode,question_ids,total_questions,status) VALUES(?,?,'A1',?,?,ARRAY[?]::integer[],1,'COMPLETED')",id,user,BANK,mode,question);
        jdbc.update("INSERT INTO exam_answers(exam_id,question_id,selected_answer,is_correct,answered_at) VALUES(?,?,'A',TRUE,clock_timestamp())",id,question);
    }
    @Test void answerRollbackAlsoRollsBackBothIndependentOutboxes() {
        transaction.executeWithoutResult(status->{
            chapter(user,true,now());assertEquals(1,count());
            assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM learning_progress_outbox WHERE user_id=?",Long.class,user));
            status.setRollbackOnly();
        });
        assertEquals(0,count());
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM learning_progress_outbox WHERE user_id=?",Long.class,user));
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM chapter_question_progress WHERE user_id=?",Long.class,user));
    }
    @Test void oldTimestampOrInvalidNamespaceIsNotCredited() {
        chapter(user,true,Instant.parse("2020-01-01T00:00:00Z"));assertEquals(0,count());
        jdbc.queryForObject("SELECT dts_record_correct_question(?,'A1','wrong-bank',?,clock_timestamp(),'CHAPTER_LEARNING','invalid')::text",String.class,user,question);
        assertEquals(0,count());
    }
    @Test void expPublisherRetainsFailedEventAndRecoversWithoutRewritingProgress() {
        chapter(user,true,now());
        String progress=jdbc.queryForObject("SELECT jsonb_agg(payload ORDER BY event_sequence)::text FROM learning_progress_outbox WHERE user_id=?",String.class,user);
        @SuppressWarnings("unchecked") KafkaTemplate<String,String> kafka=mock(KafkaTemplate.class);
        when(kafka.send(anyString(),anyString(),anyString())).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("owned offline test")));
        var worker=new PracticeExpOutboxWorker(jdbc,kafka,new ObjectMapper(),"learning-results");
        transaction.executeWithoutResult(status->worker.publishPending());
        assertTrue(jdbc.queryForObject("SELECT published_at IS NULL AND attempts=1 AND last_error IS NOT NULL FROM practice_exp_outbox WHERE user_id=?",Boolean.class,user));
        when(kafka.send(anyString(),anyString(),anyString())).thenReturn(CompletableFuture.completedFuture(null));
        jdbc.update("UPDATE practice_exp_outbox SET next_attempt_at=NOW() WHERE published_at IS NULL");
        for(int i=0;i<5;i++) transaction.executeWithoutResult(status->worker.publishPending());
        assertTrue(jdbc.queryForObject("SELECT published_at IS NOT NULL AND last_error IS NULL FROM practice_exp_outbox WHERE user_id=?",Boolean.class,user));
        assertEquals(progress,jdbc.queryForObject("SELECT jsonb_agg(payload ORDER BY event_sequence)::text FROM learning_progress_outbox WHERE user_id=?",String.class,user));
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM practice_exp_outbox WHERE event_type='PRACTICE_EXP_SOURCE_READY'",Long.class));
        assertEquals(25,jdbc.queryForObject("SELECT (payload->'metadata'->'licenseQuestionCounts'->>'A1')::integer FROM practice_exp_outbox WHERE event_type='PRACTICE_EXP_SOURCE_READY'",Integer.class));
    }
}
