package com.dts.practice.simulation;

import com.dts.practice.config.*;
import com.dts.practice.exception.GlobalExceptionHandler;
import com.dts.practice.security.JwtValidationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class,JwtValidationFilter.class,GlobalExceptionHandler.class,SimulationPresetController.class,SimulationExamController.class,SimulationExamAdvice.class})
@Import(SimulationPresetApiTest.Props.class)
class SimulationPresetApiTest {
    @TestConfiguration static class Props {@Bean JwtValidationProperties jwtValidationProperties(){return SimulationFixtures.PROPS;}}
    @Autowired MockMvc mvc;@MockBean SimulationPresetService presets;@MockBean SimulationExamService exams;
    static final String ROOT="/api/v1/simulation-exams",ID="11111111-1111-4111-8111-111111111111";
    String jwt(String...rights){return "Bearer "+SimulationFixtures.access(SimulationFixtures.USER,rights);}
    @ParameterizedTest @ValueSource(strings={"/presets","/presets/"+ID}) void anonymousCannotReadPresets(String path)throws Exception{mvc.perform(get(ROOT+path+"?licenseClass=B")).andExpect(status().isUnauthorized());verifyNoInteractions(presets);}
    @ParameterizedTest @ValueSource(strings={"/presets","/presets/"+ID}) void theoryAndLearningRightsDoNotGrantPresetExam(String path)throws Exception{mvc.perform(get(ROOT+path+"?licenseClass=B").header("Authorization",jwt("practice:exam","simulations:read","simulations:learn"))).andExpect(status().isForbidden());verifyNoInteractions(presets);}
    @ParameterizedTest @ValueSource(strings={"/presets","/presets/"+ID}) void correctRightsUseNoStoreReadMapping(String path)throws Exception{mvc.perform(get(ROOT+path+"?licenseClass=B").header("Authorization",jwt("simulations:read","simulations:exam"))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));verifyNoInteractions(exams);}
    @ParameterizedTest @ValueSource(strings={"/presets","/presets/"+ID}) void forgedSignatureRejected(String path)throws Exception{String t=jwt("simulations:read","simulations:exam");t=t.substring(0,t.lastIndexOf('.')+1)+"A".repeat(86);mvc.perform(get(ROOT+path+"?licenseClass=B").header("Authorization",t)).andExpect(status().isUnauthorized());verifyNoInteractions(presets);}
    @ParameterizedTest @ValueSource(strings={"/presets","/presets/"+ID}) void motorcycleDeniedBeforeService(String path)throws Exception{mvc.perform(get(ROOT+path+"?licenseClass=A1").header("Authorization",jwt("simulations:read","simulations:exam"))).andExpect(status().isForbidden());verifyNoInteractions(presets);}
    String body(){return "{\"mode\":\"PRESET\",\"licenseClass\":\"B\",\"presetSetId\":\""+ID+"\",\"presetVersionId\":\""+ID+"\"}";}
    @Test void presetCreateRequiresMediaPermission()throws Exception{mvc.perform(post(ROOT+"/sessions").header("Idempotency-Key",ID).header("Authorization",jwt("simulations:read","simulations:exam")).contentType("application/json").content(body())).andExpect(status().isForbidden());verifyNoInteractions(exams);}
    @Test void presetCreateReachesSharedServiceWithExactReferences()throws Exception{mvc.perform(post(ROOT+"/sessions").header("Idempotency-Key",ID).header("Authorization",jwt("simulations:read","simulations:exam","media:read")).contentType("application/json").content(body())).andExpect(status().isOk());verify(exams).create(UUID.fromString(ID),new SimulationExamModel.Create("PRESET","B",null,UUID.fromString(ID),UUID.fromString(ID)));}
    @ParameterizedTest @ValueSource(strings={"presetSetId","presetVersionId","licenseClass"}) void presetCreateNeedsCompleteSelection(String field)throws Exception{var mapper=new com.fasterxml.jackson.databind.ObjectMapper();var node=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(body());node.remove(field);mvc.perform(post(ROOT+"/sessions").header("Idempotency-Key",ID).header("Authorization",jwt("simulations:read","simulations:exam","media:read")).contentType("application/json").content(node.toString())).andExpect(status().isBadRequest());verifyNoInteractions(exams);}
    @ParameterizedTest @ValueSource(strings={"scenarioIds","order","score","userId"}) void callerCannotChooseAnswersActorOrOrder(String field)throws Exception{var mapper=new com.fasterxml.jackson.databind.ObjectMapper();var node=(com.fasterxml.jackson.databind.node.ObjectNode)mapper.readTree(body());node.put(field,"spoof");mvc.perform(post(ROOT+"/sessions").header("Idempotency-Key",ID).header("Authorization",jwt("simulations:read","simulations:exam","media:read")).contentType("application/json").content(node.toString())).andExpect(status().isBadRequest());verifyNoInteractions(exams);}
    @Test void unknownSetAndInvalidIdHaveDistinctResponses()throws Exception{when(presets.detail(any(),eq("B"))).thenThrow(SimulationExamFailure.missing());mvc.perform(get(ROOT+"/presets/"+ID+"?licenseClass=B").header("Authorization",jwt("simulations:read","simulations:exam"))).andExpect(status().isNotFound());mvc.perform(get(ROOT+"/presets/not-a-uuid?licenseClass=B").header("Authorization",jwt("simulations:read","simulations:exam"))).andExpect(status().isBadRequest());}
}
