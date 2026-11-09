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
class SimulationMigrationToolPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("dts_practice");
    static DriverManagerDataSource source;static JdbcTemplate jdbc;static String oldRows;
    @BeforeAll static void init() {
        source=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(source);
        SimulationMigrationTool.flyway(source,"30").migrate();oldRows=questions();
    }
    static String questions(){return jdbc.queryForObject("SELECT md5(coalesce(string_agg(to_jsonb(q)::text,E'\\n' ORDER BY to_jsonb(q)::text),'')) FROM questions q",String.class);}
    @Test @Order(1) void checksCompleteLegacyHistoryWithoutApplyingV31()throws Exception {
        var result=SimulationMigrationTool.execute(source,"check");assertEquals(true,result.get("Passed"));assertEquals(false,result.get("Version31Recorded"));assertEquals(31,result.get("HistoryRowsAfter"));assertEquals(oldRows,questions());
    }
    @Test @Order(2) void appliesOnlyV31AndPreservesLegacyQuestionsAndHistory()throws Exception {
        var result=SimulationMigrationTool.execute(source,"migrate");assertEquals(1,result.get("MigrationsExecuted"));assertEquals(true,result.get("ExistingHistoryRowsUnchanged"));assertEquals(32,result.get("HistoryRowsAfter"));assertEquals(oldRows,questions());
    }
    @Test @Order(3) void repeatedCheckAndMigrateDoNotExecuteAgain()throws Exception {
        assertEquals(true,SimulationMigrationTool.execute(source,"check").get("Version31Recorded"));assertEquals(0,SimulationMigrationTool.execute(source,"migrate").get("MigrationsExecuted"));
    }
    @Test @Order(4) void checksumDriftFailsBeforeAnyMigration() {
        jdbc.update("UPDATE flyway_schema_history SET checksum=checksum+1 WHERE version='30'");
        try {assertThrows(Exception.class,()->SimulationMigrationTool.execute(source,"check"));}
        finally {jdbc.update("UPDATE flyway_schema_history SET checksum=checksum-1 WHERE version='30'");}
    }
}
