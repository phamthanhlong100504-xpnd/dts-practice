package com.dts.practice.adminpresets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.dts.practice.adminpresets.AdminPresetModels.*;
import com.dts.practice.entity.Question;
import com.dts.practice.service.PresetExamSetValidator;
import java.util.List;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Real PostgreSQL contracts: approval, immutable source, logical EXP identity and atomic cutover. */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PresetPublicationPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine");
    JdbcTemplate db;
    AdminPresetService editor;
    PresetPublicationService publication;
    TransactionTemplate tx;
    UUID author=UUID.randomUUID(),reviewer=UUID.randomUUID(),publisher=UUID.randomUUID();
    UUID legacy=UUID.randomUUID();
    UUID publishedNew;

    @BeforeAll void init() {
        var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        db=new JdbcTemplate(ds);
        db.execute("CREATE TABLE questions (id INTEGER PRIMARY KEY,source_question_id INTEGER NOT NULL,"+
            "bank_version VARCHAR(64) NOT NULL,applicable_licenses JSONB NOT NULL,chapter INT NOT NULL,"+
            "question_text TEXT NOT NULL,options JSONB NOT NULL,correct_answer VARCHAR(1) NOT NULL,"+
            "is_critical BOOLEAN NOT NULL,critical_licenses JSONB NOT NULL,explanation TEXT,"+
            "media_file_id UUID,image_url TEXT,updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW())");
        db.execute("CREATE TABLE preset_exam_sets (id UUID PRIMARY KEY,code VARCHAR(32) NOT NULL,"+
            "title VARCHAR(200) NOT NULL,license_class VARCHAR(10) NOT NULL,bank_version VARCHAR(64) NOT NULL,"+
            "total_questions INTEGER NOT NULL,duration_minutes INTEGER NOT NULL,status VARCHAR(20) NOT NULL,"+
            "display_order INTEGER NOT NULL,updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),"+
            "CONSTRAINT uq_preset_exam_sets_code UNIQUE(license_class,bank_version,code))");
        db.execute("CREATE TABLE preset_exam_set_questions (preset_exam_set_id UUID NOT NULL REFERENCES preset_exam_sets(id),"+
            "question_order INTEGER NOT NULL,question_id INTEGER NOT NULL REFERENCES questions(id),"+
            "PRIMARY KEY(preset_exam_set_id,question_order))");
        db.execute("CREATE TABLE exams (id UUID PRIMARY KEY,user_id UUID NOT NULL,mode VARCHAR(10) NOT NULL,"+
            "bank_version VARCHAR(64),exam_type VARCHAR(10) NOT NULL,preset_exam_set_id UUID REFERENCES preset_exam_sets(id),"+
            "history_target_id UUID,history_attempt_no INTEGER)");
        db.execute("CREATE TABLE practice_history_attempt_counters(user_id UUID NOT NULL,target_id UUID NOT NULL,"+
            "last_attempt_no INTEGER NOT NULL,PRIMARY KEY(user_id,target_id))");
        for(int n=1;n<=25;n++) db.update("INSERT INTO questions(id,source_question_id,bank_version,applicable_licenses,"+
            "chapter,question_text,options,correct_answer,is_critical,critical_licenses) "+
            "VALUES(?,?,'dts-2026-600-v1','[\"A1\"]'::jsonb,1,?, '[{\"key\":\"A\"}]'::jsonb,'A',FALSE,'[]'::jsonb)",
            1000+n,n,"Question "+n);
        db.update("INSERT INTO preset_exam_sets(id,code,title,license_class,bank_version,total_questions,"+
            "duration_minutes,status,display_order) VALUES (?, 'SET01','Original', 'A1','dts-2026-600-v1',"+
            "25,19,'PUBLISHED',1)",legacy);
        for(int n=1;n<=25;n++) db.update("INSERT INTO preset_exam_set_questions VALUES (?,?,?)",legacy,n,1000+n);
        Flyway.configure().dataSource(ds).locations("classpath:db/migration")
            .baselineVersion("36").baselineOnMigrate(true).target("38")
            .validateOnMigrate(true).cleanDisabled(true).load().migrate();
        db.execute("CREATE TRIGGER test_practice_history_attempt BEFORE INSERT ON exams "+
            "FOR EACH ROW EXECUTE FUNCTION dts_assign_practice_history_attempt()");
        var validation=mock(PresetExamSetValidator.class);
        when(validation.validateDraft(any(),anyList())).thenReturn(
            new PresetExamSetValidator.ValidatedPreset(List.of(Question.builder().id(1001).build()),1001));
        editor=new AdminPresetService(db,validation);
        publication=new PresetPublicationService(db,validation);
        tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
    }

    private UUID approvedClone(UUID source) {
        var draft=tx.execute(x->editor.cloneVersion(source,author));
        assertNotNull(draft);
        UUID id=draft.metadata().id();
        var validation=tx.execute(x->editor.validate(id,author));
        assertTrue(validation.valid());
        tx.executeWithoutResult(x->publication.submit(id,validation.revision(),author));
        tx.executeWithoutResult(x->publication.approve(id,reviewer));
        return id;
    }

    @Test @Order(1) void cannotPublishWithoutApprovalAndCannotSelfApprove() {
        var draft=tx.execute(x->editor.cloneVersion(legacy,author));
        UUID id=draft.metadata().id();
        assertThrows(Exception.class,()->tx.executeWithoutResult(x->publication.publish(id,publisher)));
        var result=tx.execute(x->editor.validate(id,author));
        tx.executeWithoutResult(x->publication.submit(id,result.revision(),author));
        assertThrows(Exception.class,()->tx.executeWithoutResult(x->publication.approve(id,author)));
        assertThrows(Exception.class,()->tx.executeWithoutResult(x->editor.edit(id,
            new Edit(result.revision(),"CHANGED","Unapproved edit",1),author)));
        assertEquals(legacy,db.queryForObject("SELECT active_version_id FROM admin_preset_active_versions "+
            "WHERE logical_set_id=?",UUID.class,legacy));
    }

    @Test @Order(2) void sourceMutationAfterReviewBlocksPublishAndPreservesCurrentVersion() {
        UUID id=approvedClone(legacy);
        db.update("UPDATE questions SET question_text='Modified by importer' WHERE id=1001");
        assertThrows(Exception.class,()->tx.executeWithoutResult(x->publication.publish(id,publisher)));
        assertEquals(legacy,db.queryForObject("SELECT active_version_id FROM admin_preset_active_versions "+
            "WHERE logical_set_id=?",UUID.class,legacy));
        db.update("UPDATE questions SET question_text='Question 1' WHERE id=1001");
    }

    @Test @Order(3) void versionedPublishPreservesOldPresetAndStableHistoryExpTarget() {
        UUID id=approvedClone(legacy);
        var receipt=tx.execute(x->publication.publish(id,publisher));
        publishedNew=id;
        assertEquals(id,tx.execute(x->publication.publish(id,publisher)).versionId());
        assertEquals(legacy,receipt.logicalSetId());
        assertEquals(id,receipt.versionId());
        assertEquals(id,db.queryForObject("SELECT active_version_id FROM admin_preset_active_versions "+
            "WHERE logical_set_id=?",UUID.class,legacy));
        assertEquals("Original",db.queryForObject("SELECT title FROM preset_exam_sets WHERE id=?",String.class,legacy));
        assertThrows(Exception.class,()->db.update("DELETE FROM preset_exam_sets WHERE id=?",legacy));
        UUID user=UUID.randomUUID();
        UUID oldSession=UUID.randomUUID(),newSession=UUID.randomUUID();
        db.update("INSERT INTO exams(id,user_id,mode,bank_version,exam_type,preset_exam_set_id) "+
            "VALUES (?,?,'EXAM','dts-2026-600-v1','A1',?)",oldSession,user,legacy);
        db.update("INSERT INTO exams(id,user_id,mode,bank_version,exam_type,preset_exam_set_id) "+
            "VALUES (?,?,'EXAM','dts-2026-600-v1','A1',?)",newSession,user,id);
        List<UUID> targets=db.query("SELECT history_target_id FROM exams WHERE id IN (?,?)",
            (r,i)->(UUID)r.getObject(1),oldSession,newSession);
        assertEquals(List.of(legacy,legacy),targets);
        assertEquals(2,db.queryForObject("SELECT MAX(history_attempt_no) FROM exams WHERE user_id=?",
            Integer.class,user).intValue());
    }

    @Test @Order(4) void archiveOnlyChangesActivationPointer() {
        UUID id=publishedNew;
        assertNotNull(id);
        var archived=tx.execute(x->publication.archive(id,publisher));
        assertEquals("ARCHIVED",archived.state());
        assertEquals("ARCHIVED",tx.execute(x->publication.archive(id,publisher)).state());
        assertNull(db.queryForObject("SELECT active_version_id FROM admin_preset_active_versions WHERE logical_set_id=?",
            UUID.class,legacy));
        assertEquals("PUBLISHED",db.queryForObject("SELECT status FROM preset_exam_sets WHERE id=?",String.class,id));
        assertEquals(1L,db.queryForObject("SELECT COUNT(*) FROM admin_preset_release_audit WHERE version_id=? " +
            "AND action='ARCHIVE'",Long.class,id));
    }
}
