package com.dts.practice.repository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises the actual Flyway chain and V28 triggers; no application or Kafka starts. */
@Testcontainers
class Screen13PracticePostgresTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("screen13_practice_test")
            .withUsername("test").withPassword("test");

    private static final UUID OLD_USER = UUID.fromString("13000000-0000-0000-0000-000000000001");
    private static final UUID OLD_RANDOM_1 = UUID.fromString("13000000-0000-0000-0000-000000000011");
    private static final UUID OLD_RANDOM_2 = UUID.fromString("13000000-0000-0000-0000-000000000012");
    private static final UUID OLD_PRESET = UUID.fromString("13000000-0000-0000-0000-000000000013");
    private static final UUID OLD_PRACTICE = UUID.fromString("13000000-0000-0000-0000-000000000014");
    private static final UUID PRESET = UUID.fromString("13000000-0000-0000-0000-000000000020");
    private static final UUID PROGRESS_EVENT = UUID.fromString("13000000-0000-0000-0000-000000000021");
    private static final UUID LEARNING_EVENT = UUID.fromString("13000000-0000-0000-0000-000000000022");
    private static String oldExams, oldReceipt, oldProgress, oldLearning, oldMigrationHistory;
    private static int oldMigrationRank;

    @BeforeAll
    static void migrateExistingV27Database() throws Exception {
        flyway("27").migrate();
        try (var connection = connect()) {
            update(connection, """
                    INSERT INTO preset_exam_sets(id,code,title,license_class,bank_version,
                        total_questions,duration_minutes,status,display_order)
                    VALUES(?,'SCREEN13-TEST','Preset source title','A1','screen13-test',5,20,'PUBLISHED',1)
                    """, PRESET);
            insertExam(connection, OLD_RANDOM_1, OLD_USER, "EXAM", null);
            insertExam(connection, OLD_RANDOM_2, OLD_USER, "EXAM", null);
            insertExam(connection, OLD_PRESET, OLD_USER, "EXAM", PRESET);
            insertExam(connection, OLD_PRACTICE, OLD_USER, "PRACTICE", null);
            // The persisted receipt must win over older mutable exam grade fields.
            update(connection, "UPDATE exams SET score=20,correct_count=1,wrong_count=4 WHERE id=?", OLD_RANDOM_1);
            insertReceipt(connection, OLD_RANDOM_1);
            insertProgressEvent(connection, OLD_RANDOM_1, OLD_USER, PROGRESS_EVENT);
            update(connection, """
                    INSERT INTO learning_progress_outbox(event_sequence,event_id,user_id,license_class,
                        bank_version,question_id,payload)
                    VALUES(nextval('learning_progress_event_sequence'),?,?,'A1','screen13-test',1,
                        '{"eventType":"QUESTION_PROGRESS_CHANGED","keep":"original"}'::jsonb)
                    """, LEARNING_EVENT, OLD_USER);
            oldExams = originalExamRows(connection);
            oldReceipt = rowJson(connection, "exam_progress_receipts", "exam_id", OLD_RANDOM_1);
            oldProgress = rowJson(connection, "exam_progress_outbox", "event_id", PROGRESS_EVENT);
            oldLearning = rowJson(connection, "learning_progress_outbox", "event_id", LEARNING_EVENT);
            oldMigrationRank = scalar(connection, "SELECT max(installed_rank) FROM flyway_schema_history", Integer.class);
            oldMigrationHistory = scalar(connection, """
                    SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text
                    FROM flyway_schema_history h WHERE installed_rank<=?
                    """, String.class, oldMigrationRank);
        }
        flyway("27.1").migrate();
        // Normal updates must still be audited if the application writes between versions.
        try (var connection = connect()) {
            connection.setAutoCommit(false);
            try {
                var source = UUID.randomUUID();
                insertExam(connection, source, UUID.randomUUID(), "EXAM", null);
                update(connection, "UPDATE exams SET score=60,updated_at='2000-01-01T00:00:00Z' WHERE id=?", source);
                assertTrue(scalar(connection, "SELECT updated_at=CURRENT_TIMESTAMP FROM exams WHERE id=?", Boolean.class, source));
            } finally {
                connection.rollback();
            }
        }
        flyway(null).migrate();
        flyway(null).validate();
        // Repeating the command must skip the complete ordered upgrade and preserve history.
        flyway(null).migrate();
    }

    @Test
    void upgradePreservesExistingRecordsProgressAndExecutedMigrationHistory() throws Exception {
        try (var connection = connect()) {
            assertEquals(oldExams, originalExamRows(connection));
            assertEquals(oldReceipt, rowJson(connection, "exam_progress_receipts", "exam_id", OLD_RANDOM_1));
            assertEquals(oldProgress, rowJson(connection, "exam_progress_outbox", "event_id", PROGRESS_EVENT));
            assertEquals(oldLearning, rowJson(connection, "learning_progress_outbox", "event_id", LEARNING_EVENT));
            assertEquals(oldMigrationHistory, scalar(connection, """
                    SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text
                    FROM flyway_schema_history h WHERE installed_rank<=?
                    """, String.class, oldMigrationRank));
            assertEquals(1L, scalar(connection, "SELECT count(*) FROM flyway_schema_history WHERE version='27.1' AND success", Long.class));
            assertEquals(1L, scalar(connection, "SELECT count(*) FROM flyway_schema_history WHERE version='28' AND success", Long.class));
            assertEquals(1L, scalar(connection, "SELECT count(*) FROM flyway_schema_history WHERE version='29' AND success", Long.class));
            assertEquals(1, scalar(connection, "SELECT history_attempt_no FROM exams WHERE id=?", Integer.class, OLD_RANDOM_1));
            assertEquals(2, scalar(connection, "SELECT history_attempt_no FROM exams WHERE id=?", Integer.class, OLD_RANDOM_2));
            assertEquals(1, scalar(connection, "SELECT history_attempt_no FROM exams WHERE id=?", Integer.class, OLD_PRESET));
            assertEquals(PRESET, scalar(connection, "SELECT history_target_id FROM exams WHERE id=?", UUID.class, OLD_PRESET));
            assertNull(scalar(connection, "SELECT history_attempt_no FROM exams WHERE id=?", Integer.class, OLD_PRACTICE));
            var next = UUID.randomUUID();
            insertExam(connection, next, OLD_USER, "EXAM", null);
            assertEquals(3, scalar(connection, "SELECT history_attempt_no FROM exams WHERE id=?", Integer.class, next));
        }
    }

    @Test
    void originalExamAndAnswerAuditTriggersAreRestoredForNormalBusinessUpdates() throws Exception {
        try (var connection = connect()) {
            assertEquals("update_updated_at_column", scalar(connection, """
                    SELECT p.proname FROM pg_trigger t JOIN pg_proc p ON p.oid=t.tgfoid
                    WHERE t.tgrelid='public.exams'::regclass AND t.tgname='set_exams_updated_at'
                      AND NOT t.tgisinternal AND t.tgenabled='O'
                    """, String.class));
            assertNull(scalar(connection,
                    "SELECT to_regprocedure('public.dts_preserve_exam_timestamp_during_history_upgrade()')::text", String.class));
            var source = UUID.randomUUID();
            insertExam(connection, source, UUID.randomUUID(), "EXAM", null);
            var answer = UUID.randomUUID();
            update(connection, """
                    INSERT INTO exam_answers(id,exam_id,question_id,selected_answer)
                    VALUES(?,?,(SELECT id FROM questions ORDER BY id LIMIT 1),'A')
                    """, answer, source);
            connection.setAutoCommit(false);
            try {
                update(connection, "UPDATE exams SET score=60,updated_at='2000-01-01T00:00:00Z' WHERE id=?", source);
                update(connection, "UPDATE exam_answers SET selected_answer='B',updated_at='2000-01-01T00:00:00Z' WHERE id=?", answer);
                assertTrue(scalar(connection, "SELECT updated_at=CURRENT_TIMESTAMP FROM exams WHERE id=?", Boolean.class, source));
                assertTrue(scalar(connection, "SELECT updated_at=CURRENT_TIMESTAMP FROM exam_answers WHERE id=?", Boolean.class, answer));
                connection.commit();
            } catch (Exception failure) {
                connection.rollback();
                throw failure;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    @Test
    void randomAndPresetReceiptsQueueTypedSnapshotsUsingSourceIdsAndCounts() throws Exception {
        try (var connection = connect()) {
            var user = UUID.randomUUID();
            var random = UUID.randomUUID();
            var preset = UUID.randomUUID();
            insertExam(connection, random, user, "EXAM", null);
            insertExam(connection, preset, user, "EXAM", PRESET);
            insertReceipt(connection, random);
            insertReceipt(connection, preset);
            for (var source : Map.of(random, "RANDOM_EXAM", preset, "PRESET_EXAM").entrySet()) {
                assertEquals(source.getKey().toString(), payload(connection, source.getKey(), "eventId"));
                assertEquals(source.getKey().toString(), payload(connection, source.getKey(), "sourceId"));
                assertEquals(user.toString(), payload(connection, source.getKey(), "userId"));
                assertEquals("PRACTICE_SESSION", payload(connection, source.getKey(), "sourceType"));
                assertEquals(source.getValue(), payload(connection, source.getKey(), "activityType"));
                assertEquals("2", payload(connection, source.getKey(), "eventVersion"));
                assertEquals("40", payload(connection, source.getKey(), "score"));
                assertEquals("100", payload(connection, source.getKey(), "maxScore"));
                assertEquals("2", payload(connection, source.getKey(), "correctCount"));
                assertEquals("5", payload(connection, source.getKey(), "questionCount"));
                assertEquals("300", payload(connection, source.getKey(), "durationSeconds"));
                assertEquals("FAILED", payload(connection, source.getKey(), "result"));
                assertEquals("true", payload(connection, source.getKey(), "resultAvailable"));
                assertEquals("1", payload(connection, source.getKey(), "attemptNo"));
            }
            assertEquals(PRESET.toString(), payload(connection, preset, "targetId"));
            assertEquals("Preset source title", payload(connection, preset, "displayTitle"));
            assertEquals(scalar(connection, "SELECT history_target_id::text FROM exams WHERE id=?", String.class, random), payload(connection, random, "targetId"));
        }
    }

    @Test
    void practiceReceiptRemainsStoredWithoutCreatingExamHistory() throws Exception {
        try (var connection = connect()) {
            var source = UUID.randomUUID();
            insertExam(connection, source, UUID.randomUUID(), "PRACTICE", null);
            insertReceipt(connection, source);
            assertEquals(1L, count(connection, "exam_progress_receipts", source));
            assertEquals(0L, count(connection, "practice_result_outbox", source));
            assertNull(scalar(connection, "SELECT history_target_id FROM exams WHERE id=?", UUID.class, source));
            assertFalse(scalar(connection, "SELECT dts_enqueue_practice_result(?,false)", Boolean.class, source));
        }
    }

    @Test
    void rolledBackReceiptLeavesNeitherResultNorProgressOutbox() throws Exception {
        var source = UUID.randomUUID();
        var user = UUID.randomUUID();
        try (var connection = connect()) {
            insertExam(connection, source, user, "EXAM", null);
            connection.setAutoCommit(false);
            try {
                insertReceipt(connection, source);
                insertProgressEvent(connection, source, user, UUID.randomUUID());
                assertEquals(1L, count(connection, "practice_result_outbox", source));
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
            assertEquals(0L, count(connection, "exam_progress_receipts", source));
            assertEquals(0L, count(connection, "exam_progress_outbox", source));
            assertEquals(0L, count(connection, "practice_result_outbox", source));
        }
    }

    @Test
    void invalidHistorySnapshotDoesNotAbortReceiptAndProgressTransaction() throws Exception {
        try (var connection = connect()) {
            var source = UUID.randomUUID();
            var user = UUID.randomUUID();
            insertExam(connection, source, user, "EXAM", null);
            update(connection, "UPDATE exams SET history_target_id=NULL WHERE id=?", source);
            connection.setAutoCommit(false);
            try {
                insertReceipt(connection, source);
                insertProgressEvent(connection, source, user, UUID.randomUUID());
                connection.commit();
            } catch (Exception failure) {
                connection.rollback();
                throw failure;
            } finally {
                connection.setAutoCommit(true);
            }
            assertEquals(1L, count(connection, "exam_progress_receipts", source));
            assertEquals(1L, count(connection, "exam_progress_outbox", source));
            assertEquals(0L, count(connection, "practice_result_outbox", source));
            assertEquals("HISTORY_SNAPSHOT_INVALID", scalar(connection,
                    "SELECT error_code FROM practice_result_backfill_failures WHERE exam_id=?", String.class, source));
        }
    }

    @Test
    void concurrentBackfillAndRetriesProduceOneEventAndDoNotCreateProgressReceipts() throws Exception {
        var source = UUID.randomUUID();
        try (var connection = connect()) {
            insertExam(connection, source, UUID.randomUUID(), "EXAM", null);
        }
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(8);
        var futures = new ArrayList<Future<Boolean>>();
        try {
            for (int i = 0; i < 8; i++) futures.add(pool.submit(() -> {
                start.await();
                try (var connection = connect()) {
                    return scalar(connection, "SELECT dts_enqueue_practice_result(?,false)", Boolean.class, source);
                }
            }));
            start.countDown();
            int inserted = 0;
            for (var future : futures) if (future.get(30, TimeUnit.SECONDS)) inserted++;
            assertEquals(1, inserted);
        } finally {
            pool.shutdownNow();
        }
        try (var connection = connect()) {
            assertEquals(1L, count(connection, "practice_result_outbox", source));
            assertEquals(0L, count(connection, "exam_progress_receipts", source));
            assertEquals(0L, count(connection, "exam_progress_outbox", source));
            assertFalse(scalar(connection, "SELECT dts_enqueue_practice_result(?,false)", Boolean.class, source));
            assertEquals(source.toString(), payload(connection, source, "eventId"));
        }
    }

    @Test
    void concurrentAttemptsReserveDistinctNumbersAtTheSource() throws Exception {
        var user = UUID.randomUUID();
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(8);
        var futures = new ArrayList<Future<Integer>>();
        try {
            for (int i = 0; i < 8; i++) futures.add(pool.submit(() -> {
                start.await();
                try (var connection = connect()) {
                    var source = UUID.randomUUID();
                    insertExam(connection, source, user, "EXAM", null);
                    return scalar(connection, "SELECT history_attempt_no FROM exams WHERE id=?", Integer.class, source);
                }
            }));
            start.countDown();
            var numbers = new HashSet<Integer>();
            for (var future : futures) numbers.add(future.get(30, TimeUnit.SECONDS));
            assertEquals(java.util.Set.of(1, 2, 3, 4, 5, 6, 7, 8), numbers);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void existingReceiptIsAuthoritativeForBackfillAndDoesNotCreateAnotherProgressEvent() throws Exception {
        try (var connection = connect()) {
            assertTrue(scalar(connection, "SELECT dts_enqueue_practice_result(?)", Boolean.class, OLD_RANDOM_1));
            assertEquals("40", payload(connection, OLD_RANDOM_1, "score"));
            assertEquals("2", payload(connection, OLD_RANDOM_1, "correctCount"));
            assertEquals("5", payload(connection, OLD_RANDOM_1, "questionCount"));
            assertEquals("300", payload(connection, OLD_RANDOM_1, "durationSeconds"));
            assertEquals(oldReceipt, rowJson(connection, "exam_progress_receipts", "exam_id", OLD_RANDOM_1));
            assertEquals(oldProgress, rowJson(connection, "exam_progress_outbox", "event_id", PROGRESS_EVENT));
            assertEquals(1L, count(connection, "exam_progress_outbox", OLD_RANDOM_1));
            assertFalse(scalar(connection, "SELECT dts_enqueue_practice_result(?)", Boolean.class, OLD_RANDOM_1));
        }
    }

    private static Flyway flyway(String target) {
        var configuration = Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration");
        if (target != null) configuration.target(target);
        return configuration.load();
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private static void insertExam(Connection connection, UUID id, UUID user, String mode, UUID preset) throws SQLException {
        update(connection, """
                INSERT INTO exams(id,user_id,exam_type,bank_version,mode,preset_exam_set_id,
                    question_ids,total_questions,status,score,correct_count,wrong_count,
                    started_at,completed_at,duration_minutes,expires_at)
                VALUES(?,?,'A1','screen13-test',?,?,ARRAY[1,2,3,4,5],5,'COMPLETED',40,2,3,
                    '2026-10-01T00:00:00Z','2026-10-01T00:05:00Z',20,'2026-10-01T00:20:00Z')
                """, id, user, mode, preset);
    }

    private static void insertReceipt(Connection connection, UUID source) throws SQLException {
        update(connection, """
                INSERT INTO exam_progress_receipts(user_id,exam_id,exam_type,bank_version,mode,status,
                    total_questions,correct_count,wrong_count,answered_wrong_count,unanswered_count,
                    score,passed,duration_seconds,started_at,completed_at)
                SELECT user_id,id,exam_type,bank_version,mode,status,5,2,3,3,0,40,false,300,started_at,completed_at
                FROM exams WHERE id=?
                """, source);
    }

    private static void insertProgressEvent(Connection connection, UUID source, UUID user, UUID event) throws SQLException {
        update(connection, """
                INSERT INTO exam_progress_outbox(event_id,user_id,exam_id,event_type,schema_version,payload)
                VALUES(?,?,?,'EXAM_COMPLETED',1,'{"keep":"original-progress"}'::jsonb)
                """, event, user, source);
    }

    private static String originalExamRows(Connection connection) throws SQLException {
        return scalar(connection, """
                SELECT jsonb_agg(to_jsonb(e)-'history_target_id'-'history_attempt_no' ORDER BY id)::text
                FROM exams e WHERE user_id=? AND id IN (?,?,?,?)
                """, String.class, OLD_USER, OLD_RANDOM_1, OLD_RANDOM_2, OLD_PRESET, OLD_PRACTICE);
    }

    private static String rowJson(Connection connection, String table, String key, UUID id) throws SQLException {
        return scalar(connection, "SELECT to_jsonb(r)::text FROM " + table + " r WHERE " + key + "=?", String.class, id);
    }

    private static long count(Connection connection, String table, UUID source) throws SQLException {
        return scalar(connection, "SELECT count(*) FROM " + table + " WHERE exam_id=?", Long.class, source);
    }

    private static String payload(Connection connection, UUID source, String field) throws SQLException {
        return scalar(connection, "SELECT payload->>? FROM practice_result_outbox WHERE exam_id=?", String.class, field, source);
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object... parameters) throws SQLException {
        var statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) statement.setObject(i + 1, parameters[i]);
        return statement;
    }

    private static void update(Connection connection, String sql, Object... parameters) throws SQLException {
        try (var statement = prepare(connection, sql, parameters)) { statement.executeUpdate(); }
    }

    private static <T> T scalar(Connection connection, String sql, Class<T> type, Object... parameters) throws SQLException {
        try (var statement = prepare(connection, sql, parameters); var rows = statement.executeQuery()) {
            assertTrue(rows.next(), "Expected one SQL row");
            return rows.getObject(1, type);
        }
    }
}
