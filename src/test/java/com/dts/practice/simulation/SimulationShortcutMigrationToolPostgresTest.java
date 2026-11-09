package com.dts.practice.simulation;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimulationShortcutMigrationToolPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("dts_practice");
    static DataSource ds;static JdbcTemplate jdbc;static String history;
    @BeforeAll static void setup(){ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());jdbc=new JdbcTemplate(ds);Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("33").load().migrate();history=oldHistory();}
    static String oldHistory(){return jdbc.queryForObject("SELECT jsonb_agg(to_jsonb(h) ORDER BY installed_rank)::text FROM flyway_schema_history h WHERE version<>'34'",String.class);}
    @Test @Order(0) void unreviewedEnqueueDefinitionIsRefusedBeforeMigration()throws Exception{
        String original=jdbc.queryForObject("SELECT pg_get_functiondef('public.dts_enqueue_practice_result(uuid,boolean)'::regprocedure)",String.class);
        String changed=original.replace("BEGIN\n","BEGIN\n    -- owned drift fixture\n");assertNotEquals(original,changed);
        try{jdbc.execute(changed);assertThrows(IllegalStateException.class,()->SimulationShortcutMigrationTool.execute(ds,"check"));assertEquals(history,oldHistory());}
        finally{jdbc.execute(original);}
    }
    @Test @Order(1) void partialUnrecordedSchemaRefusedWithoutAnyRepair()throws Exception{jdbc.execute("CREATE TABLE simulation_video_shortcut_state(fixture integer)");try{assertThrows(IllegalStateException.class,()->SimulationShortcutMigrationTool.execute(ds,"check"));assertEquals(history,oldHistory());}finally{jdbc.execute("DROP TABLE simulation_video_shortcut_state");}}
    @Test @Order(2) void readOnlyCheckBeforeMigrationChangesNothing()throws Exception{var r=SimulationShortcutMigrationTool.execute(ds,"check");assertEquals(true,r.get("Passed"));assertEquals(false,r.get("Version34Recorded"));assertEquals(0,r.get("MigrationsExecuted"));assertEquals(true,r.get("ReadOnlyValidationConnection"));assertEquals(history,oldHistory());}
    @Test @Order(3) void onlyV34RunsAndExistingHistoryStaysIdentical()throws Exception{var r=SimulationShortcutMigrationTool.execute(ds,"migrate");assertEquals(true,r.get("Version34Recorded"));assertEquals(1,r.get("MigrationsExecuted"));assertEquals(true,r.get("ExistingTheoryLearningAndExamRowsUnchanged"));assertEquals(history,oldHistory());}
    @Test @Order(4) void repeatedMigrationIsANoop()throws Exception{var r=SimulationShortcutMigrationTool.execute(ds,"migrate");assertEquals(0,r.get("MigrationsExecuted"));assertEquals(history,oldHistory());}
    @Test @Order(5) void installedCheckKeepsDataAndHistory()throws Exception{var r=SimulationShortcutMigrationTool.execute(ds,"check");assertEquals(true,r.get("Version34Recorded"));assertEquals(0,r.get("MigrationsExecuted"));assertEquals(history,oldHistory());}
    @Test @Order(6) void nonPublicSchemaRefusedBeforeFlyway()throws Exception{DataSource wrong=new AbstractDataSource(){@Override public Connection getConnection()throws SQLException{var c=ds.getConnection();c.setSchema("pg_catalog");return c;}@Override public Connection getConnection(String u,String p)throws SQLException{return getConnection();}};assertThrows(IllegalStateException.class,()->SimulationShortcutMigrationTool.execute(wrong,"check"));assertEquals(history,oldHistory());}
    static UUID completedTheoryExam(){
        var id=UUID.randomUUID();jdbc.update("""
            INSERT INTO exams(id,user_id,exam_type,bank_version,mode,question_ids,total_questions,
                status,score,correct_count,wrong_count,started_at,completed_at,duration_minutes,expires_at)
            VALUES(?,?,'A1','shortcut-outbox-regression','EXAM',ARRAY[1,2,3,4,5],5,
                'COMPLETED',40,2,3,'2026-10-01T00:00:00Z','2026-10-01T00:05:00Z',20,'2026-10-01T00:20:00Z')
            """,id,UUID.randomUUID());return id;
    }
    @Test @Order(7) void enqueueAcquiresSourceExamLockBeforeOutboxCheck()throws Exception{
        var id=completedTheoryExam();var pool=Executors.newSingleThreadExecutor();
        try(var lock=ds.getConnection()){
            lock.setAutoCommit(false);try(var s=lock.prepareStatement("SELECT id FROM exams WHERE id=? FOR UPDATE")){s.setObject(1,id);try(var r=s.executeQuery()){assertTrue(r.next());}}
            try{
                var result=pool.submit(()->{try(var c=ds.getConnection();var s=c.createStatement()){
                    s.execute("SET lock_timeout='250ms'");try(var q=c.prepareStatement("SELECT dts_enqueue_practice_result(?,false)")){q.setObject(1,id);q.execute();return "NO_LOCK";}
                }catch(SQLException e){return e.getSQLState();}});
                assertEquals("55P03",result.get(10,TimeUnit.SECONDS));
                assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM practice_result_outbox WHERE exam_id=?",Long.class,id));
            }finally{lock.rollback();}
        }finally{pool.shutdownNow();}
        assertEquals(history,oldHistory());
    }
    @Test @Order(8) void concurrentBackfillsHaveOneWinnerAndImmutableEventWithoutProgressWrites()throws Exception{
        var id=completedTheoryExam();var start=new CountDownLatch(1);var pool=Executors.newFixedThreadPool(12);var futures=new ArrayList<Future<Boolean>>();
        try{
            for(int i=0;i<12;i++)futures.add(pool.submit(()->{start.await();return jdbc.queryForObject("SELECT dts_enqueue_practice_result(?,false)",Boolean.class,id);}));
            start.countDown();int winners=0;for(var f:futures)if(f.get(30,TimeUnit.SECONDS))winners++;assertEquals(1,winners);
        }finally{pool.shutdownNow();}
        String event=jdbc.queryForObject("SELECT to_jsonb(t)::text FROM practice_result_outbox t WHERE exam_id=?",String.class,id);
        assertFalse(jdbc.queryForObject("SELECT dts_enqueue_practice_result(?,true)",Boolean.class,id));
        assertEquals(event,jdbc.queryForObject("SELECT to_jsonb(t)::text FROM practice_result_outbox t WHERE exam_id=?",String.class,id));
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM practice_result_outbox WHERE event_id=? AND exam_id=?",Long.class,id,id));
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM exam_progress_receipts WHERE exam_id=?",Long.class,id));
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM exam_progress_outbox WHERE exam_id=?",Long.class,id));assertEquals(history,oldHistory());
    }
}
