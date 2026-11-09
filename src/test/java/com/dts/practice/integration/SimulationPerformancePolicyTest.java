package com.dts.practice.integration;

import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.service.ExamMatrix;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimulationPerformancePolicyTest {
    @Test void catalogModeFollowsSupportEvenDuringBankOutage()throws Exception {
        var item=new LicenseCatalogItemResponse("B","cars","B","description","theory",600,60,30,20,27,1,true,true).withSimulation(true,false,null);
        var json=new ObjectMapper().valueToTree(item);assertEquals("COMBINED",json.path("performanceMode").asText());assertFalse(json.path("simulationReady").asBoolean());assertTrue(json.path("simulationBankVersion").isNull());assertEquals(30,json.path("totalQuestions").intValue());
    }
    @Test void legacyCatalogFieldsAndPolicyRemainUnchanged() {
        var item=new LicenseCatalogItemResponse("A1","motorcycles","A1","description","theory",250,20,25,19,21,1,true,true);
        var json=new ObjectMapper().valueToTree(item);assertEquals("LEGACY_THEORY",json.path("performanceMode").asText());assertEquals("EXP_2026_01",json.path("performancePolicyVersion").asText());assertEquals(1,json.path("learningModules").size());
    }
    @Test void migrationNSeedExactlyMatchesTheExistingSourceExamMatrix()throws Exception {
        String sql=Files.readString(Path.of("src/main/resources/db/migration/V35__simulation_performance_source.sql"));
        var matcher=java.util.regex.Pattern.compile("question_counts JSONB NOT NULL DEFAULT '(\\{[^']+})'::jsonb").matcher(sql);assertTrue(matcher.find());
        var seed=new ObjectMapper().readTree(matcher.group(1));int count=0;
        for(String license:ExamMatrix.supportedClasses())if(!Set.of("A1","A","B1").contains(license)){assertEquals(ExamMatrix.forClass(license).totalQuestions(),seed.path(license).intValue());count++;}
        assertEquals(count,seed.size());assertEquals(30,seed.path("B").intValue());assertEquals(35,seed.path("C1").intValue());assertEquals(45,seed.path("DE").intValue());
    }
    @Test void sourceMigrationIsAdditiveAndNeverRewritesTheoryOrGradeSnapshots()throws Exception {
        String sql=Files.readString(Path.of("src/main/resources/db/migration/V35__simulation_performance_source.sql"));
        for(String forbidden:List.of("UPDATE simulation_attempts","UPDATE simulation_exam_results","UPDATE simulation_exam_sessions","DELETE FROM simulation_attempts","UPDATE exams ","UPDATE questions ","CREATE OR REPLACE FUNCTION dts_enqueue_practice_result"))assertFalse(sql.contains(forbidden),forbidden);
        assertTrue(sql.contains("AFTER INSERT OR DELETE ON simulation_bookmarks"));assertTrue(sql.contains("dts_simulation_emit_exam(item.id,FALSE)"));
    }
}
