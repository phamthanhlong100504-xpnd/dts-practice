package com.dts.practice.simulation;

import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimulationExamMigrationToolPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("dts_practice");
    static DriverManagerDataSource source;static JdbcTemplate jdbc;static String oldHistory;
    @BeforeAll static void init() {
        source=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(source);
        SimulationExamMigrationTool.flyway(source,"31").migrate();oldHistory=history();
    }
    static String history(){return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE version<>'32'",String.class);}
    @Test @Order(1) void readOnlyCheckValidatesV31WithoutApplyingV32()throws Exception {
        var r=SimulationExamMigrationTool.execute(source,"check");assertEquals(true,r.get("Passed"));assertEquals(false,r.get("Version32Recorded"));assertEquals(32,r.get("HistoryRowsAfter"));assertEquals(0,r.get("MigrationsExecuted"));assertEquals(oldHistory,history());
    }
    @Test @Order(2) void migrateRunsOnlyV32AndPreservesAllPriorHistoryAndLearning()throws Exception {
        var r=SimulationExamMigrationTool.execute(source,"migrate");assertEquals(1,r.get("MigrationsExecuted"));assertEquals(true,r.get("Version32Recorded"));assertEquals(33,r.get("HistoryRowsAfter"));assertEquals(true,r.get("ExistingTheoryAndLearningRowsUnchanged"));assertEquals(oldHistory,history());
        assertEquals(4,jdbc.queryForObject("SELECT count(*) FROM pg_tables WHERE schemaname='public' AND tablename LIKE 'simulation_exam_%'",Integer.class));
    }
    @Test @Order(3) void repeatedCheckAndMigrationAreIdempotent()throws Exception {
        assertEquals(true,SimulationExamMigrationTool.execute(source,"check").get("Version32Recorded"));assertEquals(0,SimulationExamMigrationTool.execute(source,"migrate").get("MigrationsExecuted"));assertEquals(oldHistory,history());
    }
    @Test @Order(4) void previousChecksumDriftBlocksMigration() {
        jdbc.update("UPDATE flyway_schema_history SET checksum=checksum+1 WHERE version='31'");
        try{assertThrows(Exception.class,()->SimulationExamMigrationTool.execute(source,"migrate"));}finally{jdbc.update("UPDATE flyway_schema_history SET checksum=checksum-1 WHERE version='31'");}
    }
    @Test @Order(5) void missingV32HistoryWithPartialTablesIsRejected() {
        var saved=jdbc.queryForMap("SELECT * FROM flyway_schema_history WHERE version='32'");jdbc.update("DELETE FROM flyway_schema_history WHERE version='32'");
        try{assertThrows(Exception.class,()->SimulationExamMigrationTool.execute(source,"check"));}finally{jdbc.update("INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,installed_on,execution_time,success) VALUES(?,?,?,?,?,?,?,?,?,?)",saved.get("installed_rank"),saved.get("version"),saved.get("description"),saved.get("type"),saved.get("script"),saved.get("checksum"),saved.get("installed_by"),saved.get("installed_on"),saved.get("execution_time"),saved.get("success"));}
    }
}
