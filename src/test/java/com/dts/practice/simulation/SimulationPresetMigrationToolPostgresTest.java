package com.dts.practice.simulation;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimulationPresetMigrationToolPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("dts_practice");
    static DriverManagerDataSource source;static JdbcTemplate jdbc;static String history,oldSession,oldResult,oldReceipt;static UUID session,user;
    @BeforeAll static void init(){
        source=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(source);SimulationPresetMigrationTool.flyway(source,"32").migrate();history=history();session=UUID.randomUUID();user=UUID.randomUUID();
        jdbc.update("INSERT INTO simulation_exam_sessions(id,user_id,mode,license_class,bank_version,scoring_version,manifest_sha256,policy_version,passing_score,status,completed_count,current_position,create_key,create_sha256,created_at,started_at,updated_at,completed_at) VALUES(?,?,'RANDOM','B','fixture-v1','scoring-v1',?,'random-fixture',35,'COMPLETED',10,10,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",session,user,"a".repeat(64),UUID.randomUUID(),"b".repeat(64));
        jdbc.update("INSERT INTO simulation_exam_results(session_id,total_score,passing_score,policy_version,passed,result_status,result_json,completed_at) VALUES(?,30,35,'random-fixture',FALSE,'FAILED','{\"mode\":\"RANDOM\",\"fixture\":\"old-result\"}',CURRENT_TIMESTAMP)",session);
        jdbc.update("INSERT INTO simulation_exam_events(user_id,event_id,session_id,kind,request_sha256,response_json,created_at) VALUES(?,?,?,'SUBMIT',?,'{\"mode\":\"RANDOM\",\"fixture\":\"old-receipt\"}',CURRENT_TIMESTAMP)",user,UUID.randomUUID(),session,"c".repeat(64));
        oldSession=sessionRows();oldResult=resultRows();oldReceipt=receiptRows();
    }
    static String history(){return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE version<>'33'",String.class);}
    static String sessionRows(){return jdbc.queryForObject("SELECT (to_jsonb(s)-ARRAY['preset_set_id','preset_version_id','preset_snapshot'])::text FROM simulation_exam_sessions s WHERE id=?",String.class,session);}
    static String resultRows(){return jdbc.queryForObject("SELECT to_jsonb(r)::text FROM simulation_exam_results r WHERE session_id=?",String.class,session);}
    static String receiptRows(){return jdbc.queryForObject("SELECT to_jsonb(r)::text FROM simulation_exam_events r WHERE session_id=?",String.class,session);}
    @AfterEach void oldRowsUnchanged(){assertEquals(history,history());assertEquals(oldSession,sessionRows());assertEquals(oldResult,resultRows());assertEquals(oldReceipt,receiptRows());}
    @Test @Order(1) void readOnlyCheckValidatesAllOldMigrationsWithoutInstallingV33()throws Exception{var r=SimulationPresetMigrationTool.execute(source,"check");assertEquals(true,r.get("Passed"));assertEquals(false,r.get("Version33Recorded"));assertEquals(33,r.get("HistoryRowsAfter"));assertEquals(0,r.get("MigrationsExecuted"));}
    @Test @Order(2) void migrationRunsOnlyV33AndPreservesPriorExamResultsReceiptsAndTheoryLearning()throws Exception{var r=SimulationPresetMigrationTool.execute(source,"migrate");assertEquals(1,r.get("MigrationsExecuted"));assertEquals(true,r.get("Version33Recorded"));assertEquals(34,r.get("HistoryRowsAfter"));assertEquals(true,r.get("ExistingTheoryLearningAndExamRowsUnchanged"));assertEquals(3,jdbc.queryForObject("SELECT count(*) FROM information_schema.columns WHERE table_schema='public' AND table_name='simulation_exam_sessions' AND column_name IN ('preset_set_id','preset_version_id','preset_snapshot')",Integer.class));}
    @Test @Order(3) void repeatCheckAndMigrateExecuteNothing()throws Exception{assertEquals(true,SimulationPresetMigrationTool.execute(source,"check").get("Version33Recorded"));assertEquals(0,SimulationPresetMigrationTool.execute(source,"migrate").get("MigrationsExecuted"));}
    @Test @Order(4) void previousChecksumDriftBlocksUpgrade(){jdbc.update("UPDATE flyway_schema_history SET checksum=checksum+1 WHERE version='32'");try{assertThrows(Exception.class,()->SimulationPresetMigrationTool.execute(source,"migrate"));}finally{jdbc.update("UPDATE flyway_schema_history SET checksum=checksum-1 WHERE version='32'");}}
    @Test @Order(5) void partialPresetColumnsWithoutHistoryAreRejected(){var saved=jdbc.queryForMap("SELECT * FROM flyway_schema_history WHERE version='33'");jdbc.update("DELETE FROM flyway_schema_history WHERE version='33'");try{assertThrows(Exception.class,()->SimulationPresetMigrationTool.execute(source,"check"));}finally{jdbc.update("INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success) VALUES(?,?,?,?,?,?,?,?,?,?)",saved.get("installed_rank"),saved.get("version"),saved.get("description"),saved.get("type"),saved.get("script"),saved.get("checksum"),saved.get("installed_by"),saved.get("installed_on"),saved.get("execution_time"),saved.get("success"));}}
    @Test @Order(6) void existingOneActiveConstraintIsRetained(){assertNotNull(jdbc.queryForObject("SELECT indexdef FROM pg_indexes WHERE schemaname='public' AND indexname='uq_simulation_exam_one_active'",String.class));}
    @Test @Order(7) void noNewTheoryOrLearningTablesAreIntroduced(){assertEquals(4,jdbc.queryForObject("SELECT count(*) FROM pg_tables WHERE schemaname='public' AND tablename LIKE 'simulation_exam_%'",Integer.class));assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM pg_tables WHERE schemaname='public' AND tablename LIKE 'simulation_preset_%'",Integer.class));}
    @Test @Order(8) void runnerNeverStartsSpringOrRepairsHistory()throws Exception{var r=SimulationPresetMigrationTool.execute(source,"check");assertEquals(false,r.get("SpringApplicationStarted"));assertEquals(false,r.get("BaselineRepairOrCleanRun"));assertEquals(false,r.get("OlderMigrationsExecuted"));assertEquals(true,r.get("ReadOnlyValidationConnection"));}
}
