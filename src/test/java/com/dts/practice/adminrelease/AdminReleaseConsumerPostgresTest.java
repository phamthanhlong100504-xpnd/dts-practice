package com.dts.practice.adminrelease;

import static org.junit.jupiter.api.Assertions.*;
import com.dts.practice.simulation.SimulationBank;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Real V36 Flyway against an isolated database. Never uses a DTS instance. */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminReleaseConsumerPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine");
    private final ObjectMapper json=new ObjectMapper();
    private JdbcTemplate jdbc;
    private AdminReleaseConsumerService service;
    @BeforeAll void init() {
        var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        jdbc=new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE migration_fixture(id INT PRIMARY KEY)");
        Flyway.configure().dataSource(ds).locations("classpath:db/migration")
            .baselineVersion("35").baselineOnMigrate(true).target("36")
            .cleanDisabled(true).validateOnMigrate(true).load().migrate();
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE version='36' AND success",Long.class));
        service=new AdminReleaseConsumerService(jdbc,json);
    }
    private AdminReleaseConsumerService.Prepare valid(String version,UUID releaseId) throws Exception {
        int[] perChapter={29,14,20,10,17,30};
        List<SimulationBank.Chapter> chapters=new ArrayList<>();
        List<SimulationBank.Scenario> scenarios=new ArrayList<>();
        for(int i=0;i<6;i++)chapters.add(new SimulationBank.Chapter(i+1,"Chapter "+(i+1),perChapter[i]));
        int number=0;
        for(int chapter=1;chapter<=6;chapter++) for(int i=0;i<perChapter[chapter-1];i++) {
            number++;
            var video=new SimulationBank.Asset("video.mp4","video/mp4","a".repeat(64),10000,10000);
            var image=new SimulationBank.Asset("image.webp","image/webp","b".repeat(64),null,null);
            scenarios.add(new SimulationBank.Scenario(UUID.randomUUID(),number,chapter,"Scenario "+number,10000,
                UUID.randomUUID(),UUID.randomUUID(),"Hazard","Recognition",
                new SimulationBank.Timeline(100,200,300,400,500,600),video,image));
        }
        var snapshot=new SimulationBank.Snapshot(version,"source-v1","asset-v1","score-v1",
            "a".repeat(64),"b".repeat(64),"c".repeat(64),chapters,scenarios);
        String body=json.writeValueAsString(snapshot);
        String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
        return new AdminReleaseConsumerService.Prepare(releaseId,version,sha,"c".repeat(64),body);
    }
    @Test void sameExactReleaseIsIdempotentAndImmutable() throws Exception {
        UUID id=UUID.randomUUID();var request=valid("release-"+id,id);
        var first=service.prepare(request);var second=service.prepare(request);
        assertEquals(first,second);
        assertEquals("PREPARED",first.status());assertTrue(first.checksumVerified());
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM admin_simulation_release_preparations WHERE release_id=?",Long.class,id));
        assertThrows(Exception.class,()->jdbc.update("DELETE FROM admin_simulation_release_preparations WHERE release_id=?",id));
        assertThrows(Exception.class,()->jdbc.update("UPDATE admin_simulation_release_preparations SET snapshot_sha256=? WHERE release_id=?","d".repeat(64),id));
    }
    @Test void reusedVersionWithDifferentReleaseIsDenied() throws Exception {
        UUID id=UUID.randomUUID();var request=valid("release-"+id,id);
        service.prepare(request);
        var wrong=new AdminReleaseConsumerService.Prepare(UUID.randomUUID(),request.bankVersion(),
            request.snapshotSha256(),request.manifestSha256(),request.snapshotJson());
        assertThrows(IllegalStateException.class,()->service.prepare(wrong));
        assertEquals(1L,jdbc.queryForObject("SELECT count(*) FROM admin_simulation_release_preparations WHERE bank_version=?",Long.class,request.bankVersion()));
    }
    @Test void changedPayloadWithSameShaCannotBePrepared() throws Exception {
        var request=valid("release-"+UUID.randomUUID(),UUID.randomUUID());
        var changed=new AdminReleaseConsumerService.Prepare(request.releaseId(),request.bankVersion(),
            request.snapshotSha256(),request.manifestSha256(),request.snapshotJson()+" ");
        assertThrows(IllegalArgumentException.class,()->service.prepare(changed));
        assertEquals(0L,jdbc.queryForObject("SELECT count(*) FROM admin_simulation_release_preparations WHERE release_id=?",Long.class,request.releaseId()));
    }
}
