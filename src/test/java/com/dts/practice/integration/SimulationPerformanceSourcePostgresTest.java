package com.dts.practice.integration;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named="DTS_SIMULATION_INTEGRATION_POSTGRES_TEST",matches="true")
class SimulationPerformanceSourcePostgresTest {
    static SimulationSourceTestDatabase db;static JdbcTemplate jdbc;static UUID historicalUser,historicalAttempt;static String originalRows,enqueue;
    UUID user;
    @BeforeAll static void schema()throws Exception{
        db=new SimulationSourceTestDatabase();jdbc=new JdbcTemplate(db.source);historicalUser=UUID.randomUUID();
        historicalAttempt=attempt(historicalUser,1,5,"B","bank-v1",Instant.now().minusSeconds(86400));
        attempt(historicalUser,2,0,"B","bank-v1",Instant.now().minusSeconds(86000));
        originalRows=jdbc.queryForObject("SELECT md5(string_agg(to_jsonb(a)::text,',' ORDER BY id))FROM simulation_attempts a",String.class);
        enqueue=jdbc.queryForObject("SELECT md5(prosrc)FROM pg_proc WHERE proname='dts_enqueue_practice_result'",String.class);
        var migrated=org.flywaydb.core.Flyway.configure().dataSource(db.source).locations("classpath:db/migration").target("35").load().migrate();assertEquals(1,migrated.migrationsExecuted);
    }
    @AfterAll static void done(){if(db!=null)db.close();}
    @BeforeEach void ownUser(){user=UUID.randomUUID();}
    static UUID attempt(UUID owner,int number,int score,String license,String bank,Instant at){
        UUID id=UUID.randomUUID();jdbc.update("""
            INSERT INTO simulation_attempts(id,user_id,create_event_id,bank_version,scoring_version,manifest_sha256,scenario_id,scenario_number,chapter_id,license_class,duration_ms,scenario_snapshot,status,playback_event_id,created_at,updated_at,completed_at,score)
            VALUES(?,?,?,?,?,?,?,?,?,?,30000,'{}','COMPLETED',?,?,?,?,?)
            """,id,owner,UUID.randomUUID(),bank,"scoring-v1","a".repeat(64),new UUID(0,number),number,1,license,UUID.randomUUID(),Timestamp.from(at),Timestamp.from(at),Timestamp.from(at),score);return id;
    }
    long sequence(){return jdbc.queryForObject("SELECT last_sequence FROM simulation_performance_source",Long.class);}
    @Test void migrationPreservesHistoricalRowsAndLegacyTheoryFunction(){
        assertEquals(originalRows,jdbc.queryForObject("SELECT md5(string_agg(to_jsonb(a)::text,',' ORDER BY id))FROM simulation_attempts a WHERE user_id=? AND completed_at<(SELECT capture_started_at FROM simulation_performance_source)",String.class,historicalUser));
        assertEquals(enqueue,jdbc.queryForObject("SELECT md5(prosrc)FROM pg_proc WHERE proname='dts_enqueue_practice_result'",String.class));
    }
    @Test void historicalFirstPerfectIsExcludedAndRepublishDoesNotAwardIt(){
        assertFalse(jdbc.queryForObject("SELECT exp_eligible FROM simulation_performance_first_perfect WHERE user_id=? AND scenario_number=1",Boolean.class,historicalUser));
        UUID id=attempt(historicalUser,1,5,"C1","republished-bank",Instant.now());
        assertFalse(jdbc.queryForObject("SELECT(payload->'data'->>'firstPerfectEligible')::boolean FROM simulation_performance_outbox WHERE payload->'data'->>'sourceId'=?",Boolean.class,id.toString()));
    }
    @Test void zeroCompletionIsExportedWithoutGrantingPerfectExp(){
        UUID id=attempt(user,3,0,"B","bank-v1",Instant.now());
        assertEquals(0,jdbc.queryForObject("SELECT(payload->'data'->>'score')::integer FROM simulation_performance_outbox WHERE payload->'data'->>'sourceId'=?",Integer.class,id.toString()));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM simulation_performance_first_perfect WHERE user_id=?",Integer.class,user));
    }
    @Test void firstPerfectIsOnceAcrossLicenseAndBankPublication(){
        for(String license:List.of("B","C1","C"))attempt(user,4,5,license,"bank-"+license,Instant.now());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*)FROM simulation_performance_outbox WHERE(payload->'data'->>'userId')::uuid=? AND(payload->'data'->>'firstPerfectEligible')::boolean",Integer.class,user));
        assertEquals("B",jdbc.queryForObject("SELECT license_class FROM simulation_performance_first_perfect WHERE user_id=?",String.class,user));
    }
    @Test void unmarkHasAnOrderedTombstone(){
        UUID scenario=new UUID(0,5);jdbc.update("INSERT INTO simulation_bookmarks(user_id,bank_version,scenario_id,updated_at)VALUES(?,'bank-v1',?,clock_timestamp())",user,scenario);
        jdbc.update("DELETE FROM simulation_bookmarks WHERE user_id=?",user);
        assertFalse(jdbc.queryForObject("SELECT(payload->'data'->>'marked')::boolean FROM simulation_performance_outbox WHERE(payload->'data'->>'userId')::uuid=? ORDER BY event_sequence DESC LIMIT 1",Boolean.class,user));
    }
    @Test void businessRollbackRollsBackEvidenceAndDoesNotLeaveSequenceGap(){
        long before=sequence();assertThrows(RuntimeException.class,()->db.transaction.executeWithoutResult(tx->{attempt(user,6,5,"B","bank-v1",Instant.now());throw new RuntimeException("owned rollback fixture");}));
        assertEquals(before,sequence());assertEquals(0,jdbc.queryForObject("SELECT COUNT(*)FROM simulation_attempts WHERE user_id=?",Integer.class,user));
    }
    @Test void immutableOutboxRejectsEvidenceReplacement(){
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE simulation_performance_outbox SET payload='{}' WHERE event_sequence=1"));
        assertEquals("SIMULATION_SYNC_BEGIN",jdbc.queryForObject("SELECT event_type FROM simulation_performance_outbox WHERE event_sequence=1",String.class));
    }
    @Test void terminalSourceGuardRejectsRewriteAndCannotPublishAnotherCompletion(){
        UUID id=attempt(user,7,5,"B","bank-v1",Instant.now());long before=sequence();
        assertThrows(org.springframework.dao.DataAccessException.class,()->jdbc.update("UPDATE simulation_attempts SET updated_at=clock_timestamp() WHERE id=?",id));assertEquals(before,sequence());
    }
}
