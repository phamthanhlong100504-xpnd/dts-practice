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
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminPresetPostgresTest {
    @Container static final PostgreSQLContainer<?> pg=new PostgreSQLContainer<>("postgres:16-alpine");
    private JdbcTemplate jdbc;
    private AdminPresetService service;
    private TransactionTemplate tx;
    private final UUID actor=UUID.randomUUID();
    private final UUID published=UUID.randomUUID();

    @BeforeAll void init(){
        var ds=new DriverManagerDataSource(pg.getJdbcUrl(),pg.getUsername(),pg.getPassword());
        jdbc=new JdbcTemplate(ds);
        jdbc.execute("CREATE TABLE questions (id INTEGER PRIMARY KEY, source_question_id INTEGER NOT NULL,bank_version VARCHAR(64) NOT NULL,applicable_licenses JSONB, chapter INT DEFAULT 1, question_text TEXT DEFAULT 'sample',is_critical BOOLEAN DEFAULT FALSE,critical_licenses JSONB,media_file_id UUID)");
        jdbc.execute("CREATE TABLE preset_exam_sets (id UUID PRIMARY KEY,code VARCHAR(32) NOT NULL,title VARCHAR(200) NOT NULL,license_class VARCHAR(10) NOT NULL,bank_version VARCHAR(64) NOT NULL,total_questions INTEGER NOT NULL,duration_minutes INTEGER NOT NULL,status VARCHAR(20) NOT NULL,display_order INTEGER NOT NULL,updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),CONSTRAINT uq_preset_exam_sets_code UNIQUE(license_class,bank_version,code))");
        jdbc.execute("CREATE TABLE preset_exam_set_questions (preset_exam_set_id UUID NOT NULL REFERENCES preset_exam_sets(id),question_order INTEGER NOT NULL,question_id INTEGER NOT NULL REFERENCES questions(id),PRIMARY KEY(preset_exam_set_id,question_order))");
        jdbc.update("INSERT INTO questions VALUES (501,11,'dts-2026-600-v1','[\"A1\",\"A\"]'::jsonb),(502,12,'dts-2026-600-v1','[\"B\"]'::jsonb)");
        jdbc.update("INSERT INTO preset_exam_sets(id,code,title,license_class,bank_version,total_questions,duration_minutes,status,display_order) VALUES (?,?,?,'A1','dts-2026-600-v1',25,19,'PUBLISHED',1)",published,"OFFICIAL-1","Published 1");
        jdbc.update("INSERT INTO preset_exam_set_questions VALUES (?,?,?)",published,1,501);
        Flyway.configure().dataSource(ds).locations("classpath:db/migration")
            .baselineVersion("36").baselineOnMigrate(true).target("38")
            .validateOnMigrate(true).cleanDisabled(true).load().migrate();
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE version='37' AND success",Long.class).longValue());
        var validator=mock(PresetExamSetValidator.class);
        when(validator.validateDraft(any(),anyList())).thenReturn(
            new PresetExamSetValidator.ValidatedPreset(List.of(Question.builder().id(501).build()),11));
        service=new AdminPresetService(jdbc,validator);
        tx=new TransactionTemplate(new DataSourceTransactionManager(ds));
    }
    @Test void publishedRowsAndQuestionsCannotBeRewritten(){
        assertThrows(Exception.class,()->jdbc.update("UPDATE preset_exam_sets SET title='Changed' WHERE id=?",published));
        assertThrows(Exception.class,()->jdbc.update("DELETE FROM preset_exam_set_questions WHERE preset_exam_set_id=?",published));
        assertThrows(Exception.class,()->jdbc.update("INSERT INTO preset_exam_set_questions VALUES (?,?,?)",published,2,502));
        assertEquals("Published 1",jdbc.queryForObject("SELECT title FROM preset_exam_sets WHERE id=?",String.class,published));
    }
    @Test void cloneEditSourceMappingRevisionAndImmutableAudit(){
        var clone=tx.execute(status->service.cloneVersion(published,actor));
        assertNotNull(clone);
        UUID id=clone.metadata().id();
        assertEquals(published,clone.metadata().logicalSetId());
        assertEquals(2,clone.metadata().versionNumber());
        assertEquals(List.of(11),clone.sourceQuestionIds());
        assertEquals("DRAFT",clone.metadata().status());
        assertFalse(clone.metadata().canPublish());
        assertThrows(Exception.class,()->tx.execute(status->service.questions(id,new Questions(1,List.of(12)),actor)));
        var changed=tx.execute(status->service.questions(id,new Questions(1,List.of(11)),actor));
        assertEquals(2,changed.metadata().revision());
        assertThrows(Exception.class,()->tx.execute(status->service.edit(id,new Edit(1,"OFF-2","Wrong",2),actor)));
        var validated=tx.execute(status->service.validate(id,actor));
        assertTrue(validated.valid());assertFalse(validated.canPublish());
        assertEquals("VALID",service.detail(id).metadata().validationStatus());
        var edited=tx.execute(status->service.edit(id,new Edit(3,"OFF-2","Edited",2),actor));
        assertEquals("NEEDS_VALIDATION",edited.metadata().validationStatus());
        assertEquals(4,edited.metadata().revision());
        assertThrows(Exception.class,()->jdbc.update("UPDATE admin_preset_draft_audit SET action='FORGED' WHERE draft_id=?",id));
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM preset_exam_sets WHERE id=? AND status='PUBLISHED'",Long.class,published).longValue());
    }
    @Test void optionalQuestionCatalogFiltersWorkWithNullAndConcreteValues(){
        var unfiltered=service.availableQuestions("A1",null,null,null,20,0);
        assertEquals(1L,unfiltered.total());
        assertEquals(11,unfiltered.items().getFirst().sourceQuestionId());
        assertEquals(1L,service.availableQuestions("A1",1,null,"sample",20,0).total());
        assertEquals(0L,service.availableQuestions("A1",2,null,"",20,0).total());
        assertEquals(1L,service.availableQuestions("A1",null,11,"",20,0).total());
        assertEquals(0L,service.availableQuestions("A1",null,12,"",20,0).total());
        assertEquals(0L,service.availableQuestions("A1",null,null,"not-in-bank",20,0).total());
        assertEquals(0L,service.availableQuestions("B",null,11,"",20,0).total());
    }
    @Test void adminListFiltersHandleAllNullsWithoutPostgresUnknownType(){
        var all=service.list(null,null,null,null,100,0);
        assertTrue(all.items().stream().anyMatch(item->item.id().equals(published)));
        var publishedA1=service.list("A1","dts-2026-600-v1","PUBLISHED","Published",100,0);
        assertEquals(1L,publishedA1.total());
        assertEquals(published,publishedA1.items().getFirst().id());
        assertEquals(0L,service.list("B",null,"PUBLISHED","Published",100,0).total());
        assertEquals(0L,service.list(null,"not-a-bank","PUBLISHED","Published",100,0).total());
        assertEquals(0L,service.list(null,null,"ARCHIVED","Published",100,0).total());
    }
    @Test void freshDraftKeepsLearnerBankAndMatrixUntouched(){
        var created=tx.execute(status->service.create(new Create("NEW-A1","Set A1","A1",5),actor));
        assertNotNull(created);
        assertEquals(25,created.metadata().totalQuestions());
        assertEquals(19,created.metadata().durationMinutes());
        assertEquals(0,created.sourceQuestionIds().size());
        var policy=service.matrix("A1");
        assertEquals(25,policy.totalQuestions());
        assertNull(policy.matrixPolicyVersion());
        assertFalse(policy.editable());
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM preset_exam_sets",Long.class).longValue());
        var catalog=service.availableQuestions("A1",null,null,"",20,0);
        assertEquals(1,catalog.total());
        assertEquals(11,catalog.items().getFirst().sourceQuestionId());
    }
}
