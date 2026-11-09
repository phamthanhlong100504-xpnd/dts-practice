package com.dts.practice.simulation;

import com.dts.practice.config.JwtValidationProperties;
import com.dts.practice.config.SecurityConfig;
import com.dts.practice.exception.GlobalExceptionHandler;
import com.dts.practice.security.JwtValidationFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.*;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class,JwtValidationFilter.class,GlobalExceptionHandler.class,SimulationExamController.class,SimulationExamAdvice.class})
@Import(SimulationExamApiTest.Props.class)
class SimulationExamApiTest {
    @TestConfiguration static class Props {@Bean JwtValidationProperties jwtValidationProperties(){return SimulationFixtures.PROPS;}}
    @Autowired MockMvc mvc;
    @MockBean SimulationExamService service;
    static final String ID="00000000-0000-4000-8000-000000000001",PREFIX="/api/v1/simulation-exams";
    static final String COMMAND="{\"eventId\":\""+ID+"\"}";
    static final String RESTART="{\"eventId\":\""+ID+"\",\"position\":1,\"playbackEventId\":\""+ID+"\"}";
    static final String EVENT=RESTART.replace("}",",\"mediaTimeMs\":0}");
    static Stream<Arguments> routes() {
        var read=List.of("simulations:read","simulations:exam");var media=List.of("simulations:read","simulations:exam","media:read");
        return Stream.of(Arguments.of("GET","/preparation?licenseClass=B","",read),Arguments.of("GET","/sessions/active","",read),
            Arguments.of("POST","/sessions","{\"mode\":\"RANDOM\",\"licenseClass\":\"B\"}",media),Arguments.of("GET","/sessions/"+ID,"",read),
            Arguments.of("GET","/sessions/"+ID+"/current/playback","",media),Arguments.of("POST","/sessions/"+ID+"/current/restart",RESTART,read),
            Arguments.of("POST","/sessions/"+ID+"/current/start",EVENT,read),Arguments.of("POST","/sessions/"+ID+"/current/heartbeat",EVENT,read),
            Arguments.of("PUT","/sessions/"+ID+"/reaction",EVENT,read),Arguments.of("POST","/sessions/"+ID+"/complete-current",EVENT,read),
            Arguments.of("POST","/sessions/"+ID+"/submit",COMMAND,read),Arguments.of("POST","/sessions/"+ID+"/cancel",COMMAND,read),
            Arguments.of("GET","/sessions/"+ID+"/result","",read),Arguments.of("GET","/sessions/"+ID+"/review","",read),
            Arguments.of("GET","/sessions/"+ID+"/review/1/playback","",media));
    }
    MockHttpServletRequestBuilder request(String method,String path,String body){return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),PREFIX+path).contentType("application/json").content(body).header("Idempotency-Key",ID);}
    String bearer(String...rights){return "Bearer "+SimulationFixtures.access(SimulationFixtures.USER,rights);}
    @ParameterizedTest @MethodSource("routes") void anonymousDenied(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body)).andExpect(status().isUnauthorized());verifyNoInteractions(service);
    }
    @ParameterizedTest @MethodSource("routes") void theoryExamDoesNotGrantSimulationExam(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body).header("Authorization",bearer("practice:read","practice:exam"))).andExpect(status().isForbidden());verifyNoInteractions(service);
    }
    @ParameterizedTest @MethodSource("routes") void learningPermissionDoesNotGrantSimulationExam(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body).header("Authorization",bearer("simulations:read","simulations:learn","media:read"))).andExpect(status().isForbidden());verifyNoInteractions(service);
    }
    @ParameterizedTest @MethodSource("routes") void exactExamRightsReachMapping(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body).header("Authorization",bearer(rights.toArray(String[]::new)))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
    }
    @ParameterizedTest @MethodSource("routes") void forgedSignatureDenied(String method,String path,String body,List<String> rights)throws Exception {
        String token=bearer(rights.toArray(String[]::new));int dot=token.lastIndexOf('.');token=token.substring(0,dot+1)+"A".repeat(86);
        mvc.perform(request(method,path,body).header("Authorization",token)).andExpect(status().isUnauthorized());verifyNoInteractions(service);
    }
    @ParameterizedTest @ValueSource(strings={"userId","score","scenarioIds","order","passed"}) void clientCannotProvideActorAnswersOrOrder(String field)throws Exception {
        mvc.perform(request("POST","/sessions","{\"mode\":\"RANDOM\",\"licenseClass\":\"B\",\""+field+"\":\"spoof\"}").header("Authorization",bearer("simulations:read","simulations:exam","media:read")))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SIMULATION_EXAM_INVALID_REQUEST"));verifyNoInteractions(service);
    }
    @ParameterizedTest @ValueSource(strings={"mediaTimeMs","position","eventId","playbackEventId"}) void eventFieldsRequired(String field)throws Exception {
        var json=new com.fasterxml.jackson.databind.ObjectMapper();var body=(com.fasterxml.jackson.databind.node.ObjectNode)json.readTree(EVENT);body.remove(field);
        mvc.perform(request("PUT","/sessions/"+ID+"/reaction",body.toString()).header("Authorization",bearer("simulations:read","simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);
    }
    @Test void createNeedsExactlyOneCanonicalIdempotencyKey()throws Exception {
        mvc.perform(post(PREFIX+"/sessions").contentType("application/json").content("{\"mode\":\"RANDOM\",\"licenseClass\":\"B\"}").header("Authorization",bearer("simulations:read","simulations:exam","media:read"))).andExpect(status().isBadRequest());verifyNoInteractions(service);
    }
    @Test void privateVideoNeedsMediaPermission()throws Exception {
        mvc.perform(request("GET","/sessions/"+ID+"/current/playback","").header("Authorization",bearer("simulations:read","simulations:exam"))).andExpect(status().isForbidden());verifyNoInteractions(service);
    }
    @Test void motorcycleDeniedBeforeService()throws Exception {
        mvc.perform(request("GET","/preparation?licenseClass=A1","").header("Authorization",bearer("simulations:read","simulations:exam"))).andExpect(status().isForbidden());verifyNoInteractions(service);
    }
    @Test void activeConflictReturnsRecoverableSessionId()throws Exception {
        when(service.create(any(),any())).thenThrow(SimulationExamFailure.active(UUID.fromString(ID)));
        mvc.perform(request("POST","/sessions","{\"mode\":\"RANDOM\",\"licenseClass\":\"B\"}").header("Authorization",bearer("simulations:read","simulations:exam","media:read")))
            .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("ACTIVE_SESSION_EXISTS")).andExpect(jsonPath("$.activeSessionId").value(ID));
    }
    @Test void invalidUuidAndFractionalMediaTimeAreRejected()throws Exception {
        mvc.perform(request("GET","/sessions/not-uuid","").header("Authorization",bearer("simulations:read","simulations:exam"))).andExpect(status().isBadRequest());
        mvc.perform(request("PUT","/sessions/"+ID+"/reaction",EVENT.replace(":0",":0.5")).header("Authorization",bearer("simulations:read","simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);
    }
}
