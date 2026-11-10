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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class,JwtValidationFilter.class,GlobalExceptionHandler.class,SimulationLearningController.class,SimulationLearningAdvice.class})
@Import(SimulationLearningApiTest.Props.class)
class SimulationLearningApiTest {
    @TestConfiguration static class Props {@Bean JwtValidationProperties jwtValidationProperties(){return SimulationFixtures.PROPS;}}
    @Autowired MockMvc mvc;
    @MockBean SimulationLearningService service;
    @MockBean SimulationLearningReads reads;
    static final String ID="00000000-0000-4000-8000-000000000001",PREFIX="/api/v1/simulations";
    static final String EVENT="{\"eventId\":\""+ID+"\",\"playbackEventId\":\""+ID+"\",\"mediaTimeMs\":0}";
    static Stream<Arguments> routes() {
        var read=List.of("simulations:read");var learn=List.of("simulations:read","simulations:learn");var media=List.of("simulations:read","media:read");
        return Stream.of(
            Arguments.of("GET","/overview?licenseClass=B","",read),Arguments.of("GET","/chapters/1/scenarios?licenseClass=B","",media),
            Arguments.of("GET","/scenarios/"+ID+"?licenseClass=B","",media),Arguments.of("GET","/attempts/"+ID,"",read),Arguments.of("GET","/attempts/"+ID+"/result","",read),
            Arguments.of("GET","/attempts/"+ID+"/playback","",List.of("simulations:read","simulations:learn","media:read")),
            Arguments.of("POST","/attempts","{\"eventId\":\""+ID+"\",\"scenarioId\":\""+ID+"\",\"licenseClass\":\"B\",\"bankVersion\":\"bank-v1\"}",learn),
            Arguments.of("POST","/attempts/"+ID+"/start",EVENT,learn),Arguments.of("POST","/attempts/"+ID+"/heartbeat",EVENT,learn),
            Arguments.of("PUT","/attempts/"+ID+"/response",EVENT,learn),Arguments.of("POST","/attempts/"+ID+"/complete",EVENT,learn),
            Arguments.of("POST","/attempts/"+ID+"/interrupt","{\"eventId\":\""+ID+"\",\"playbackEventId\":\""+ID+"\",\"reason\":\"NETWORK\"}",learn),
            Arguments.of("POST","/attempts/"+ID+"/cancel","{\"eventId\":\""+ID+"\"}",learn),
            Arguments.of("PUT","/bookmarks/"+ID,"{\"licenseClass\":\"B\",\"bankVersion\":\"bank-v1\",\"bookmarked\":true}",learn),
            Arguments.of("PUT","/chapters/1/cursor","{\"eventId\":\""+ID+"\",\"licenseClass\":\"B\",\"bankVersion\":\"bank-v1\",\"scenarioId\":\""+ID+"\",\"expectedRevision\":0}",learn));
    }
    MockHttpServletRequestBuilder request(String method,String path,String body) {return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),PREFIX+path).contentType("application/json").content(body);}
    String bearer(String...rights) {return "Bearer "+SimulationFixtures.access(SimulationFixtures.USER,rights);}
    @ParameterizedTest @MethodSource("routes") void allMappingsRequireSignedIdentityJwt(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body)).andExpect(status().isUnauthorized());verifyNoInteractions(service,reads);
    }
    @ParameterizedTest @MethodSource("routes") void theoryPermissionDoesNotOpenSimulation(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body).header("Authorization",bearer("practice:read","practice:learn","practice:exam"))).andExpect(status().isForbidden());verifyNoInteractions(service,reads);
    }
    @ParameterizedTest @MethodSource("routes") void matchingSimulationRightsReachController(String method,String path,String body,List<String> rights)throws Exception {
        mvc.perform(request(method,path,body).header("Authorization",bearer(rights.toArray(String[]::new)))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
    }
    @Test void readPermissionDoesNotAllowCreatingAttempt()throws Exception {
        mvc.perform(request("POST","/attempts","{\"eventId\":\""+ID+"\",\"scenarioId\":\""+ID+"\",\"licenseClass\":\"B\",\"bankVersion\":\"bank-v1\"}").header("Authorization",bearer("simulations:read"))).andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
    @Test void responseRejectsNegativeMediaTimeBeforeCallingService()throws Exception {
        String body="{\"eventId\":\""+ID+"\",\"playbackEventId\":\""+ID+"\",\"mediaTimeMs\":-1}";
        mvc.perform(request("PUT","/attempts/"+ID+"/response",body).header("Authorization",bearer("simulations:read","simulations:learn")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(service,reads);
    }
    @Test void responseRejectsMissingEventIdsBeforeCallingService()throws Exception {
        mvc.perform(request("PUT","/attempts/"+ID+"/response","{\"mediaTimeMs\":0}").header("Authorization",bearer("simulations:read","simulations:learn")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoInteractions(service,reads);
    }
    @Test void invalidAttemptUuidIsBadRequest()throws Exception {
        mvc.perform(get(PREFIX+"/attempts/not-uuid").header("Authorization",bearer("simulations:read")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SIMULATION_INVALID_REQUEST"));
        verifyNoInteractions(service,reads);
    }
    @Test void nonNumericChapterIdIsBadRequest()throws Exception {
        mvc.perform(get(PREFIX+"/chapters/bad/scenarios?licenseClass=B").header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SIMULATION_INVALID_REQUEST"));
        verifyNoInteractions(service,reads);
    }
}
