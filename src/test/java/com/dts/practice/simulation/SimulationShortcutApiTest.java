package com.dts.practice.simulation;
import com.dts.practice.config.*;
import com.dts.practice.exception.GlobalExceptionHandler;
import com.dts.practice.security.JwtValidationFilter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import java.util.*;
import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class,JwtValidationFilter.class,GlobalExceptionHandler.class,SimulationLearningShortcutsController.class,SimulationExamShortcutsController.class,SimulationShortcutAdvice.class})
@Import(SimulationShortcutApiTest.Props.class)
class SimulationShortcutApiTest {
    @TestConfiguration static class Props{@Bean JwtValidationProperties jwtValidationProperties(){return SimulationFixtures.PROPS;}}
    @Autowired MockMvc mvc;@MockBean SimulationShortcutService service;
    static final String ID="00000000-0000-4000-8000-000000000001";
    static final String L="/api/v1/simulations/attempts/"+ID+"/shortcuts",E="/api/v1/simulation-exams/sessions/"+ID+"/shortcuts";
    static final String ENABLE="{\"eventId\":\""+ID+"\",\"position\":1,\"playbackEventId\":\""+ID+"\"}";
    static final String SKIP=ENABLE.replace("}",",\"mediaTimeMs\":3000}");
    static final String NEXT=ENABLE.replace("}",",\"reactionAcknowledgement\":\""+"0".repeat(64)+"\"}");
    static Stream<Arguments> routes(){return Stream.of(Arguments.of("GET",L,"","simulations:learn"),Arguments.of("POST",L+"/enable",ENABLE,"simulations:learn"),Arguments.of("POST",L+"/skip-intro",SKIP,"simulations:learn"),Arguments.of("GET",E,"","simulations:exam"),Arguments.of("POST",E+"/enable",ENABLE,"simulations:exam"),Arguments.of("POST",E+"/skip-intro",SKIP,"simulations:exam"),Arguments.of("POST",E+"/advance-after-reaction",NEXT,"simulations:exam"));}
    MockHttpServletRequestBuilder request(String method,String path,String body){return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request(org.springframework.http.HttpMethod.valueOf(method),path+"?licenseClass=B").contentType("application/json").content(body);}
    String auth(String right){return "Bearer "+SimulationFixtures.access(SimulationFixtures.USER,"simulations:read",right);}
    @ParameterizedTest @MethodSource("routes") void anonymousDenied(String method,String path,String body,String right)throws Exception{mvc.perform(request(method,path,body)).andExpect(status().isUnauthorized());verifyNoInteractions(service);}
    @ParameterizedTest @MethodSource("routes") void wrongRightDenied(String method,String path,String body,String right)throws Exception{mvc.perform(request(method,path,body).header("Authorization",auth(right.equals("simulations:learn")?"simulations:exam":"simulations:learn"))).andExpect(status().isForbidden());verifyNoInteractions(service);}
    @ParameterizedTest @MethodSource("routes") void forgedJwtDenied(String method,String path,String body,String right)throws Exception{String a=auth(right);a=a.substring(0,a.lastIndexOf('.')+1)+"A".repeat(86);mvc.perform(request(method,path,body).header("Authorization",a)).andExpect(status().isUnauthorized());verifyNoInteractions(service);}
    @ParameterizedTest @MethodSource("routes") void exactRightReachesStrictMapping(String method,String path,String body,String right)throws Exception{mvc.perform(request(method,path,body).header("Authorization",auth(right))).andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));}
    @ParameterizedTest @ValueSource(strings={"durationMs","score","userId","targetMs","ended"}) void clientCannotSpoofShortcut(String field)throws Exception{for(String base:List.of(L,E))mvc.perform(request("POST",base+"/skip-intro",SKIP.replace("}",",\""+field+"\":1}")).header("Authorization",auth(base.equals(L)?"simulations:learn":"simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);}
    @Test void nextRequiresConfirmationShape()throws Exception{mvc.perform(request("POST",E+"/advance-after-reaction",ENABLE).header("Authorization",auth("simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);}
    @Test void noDefaultLicense()throws Exception{mvc.perform(get(E).header("Authorization",auth("simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);}
    @Test void unsupportedLicenseBeforeService()throws Exception{mvc.perform(get(E+"?licenseClass=A1").header("Authorization",auth("simulations:exam"))).andExpect(status().isForbidden());verifyNoInteractions(service);}
    @Test void canonicalNonceRequired()throws Exception{mvc.perform(request("POST",E+"/enable",ENABLE.replace(ID,"NOT-UUID")).header("Authorization",auth("simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);}
    @Test void fractionalMediaTimeRejected()throws Exception{mvc.perform(request("POST",E+"/skip-intro",SKIP.replace("3000","3.5")).header("Authorization",auth("simulations:exam"))).andExpect(status().isBadRequest());verifyNoInteractions(service);}
    @Test void storedReactionConfirmationFailureIsExplicit()throws Exception{when(service.advance(any(),any(),any())).thenThrow(SimulationShortcutFailure.conflict("REACTION_ACK_REQUIRED"));mvc.perform(request("POST",E+"/advance-after-reaction",NEXT).header("Authorization",auth("simulations:exam"))).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SIMULATION_SHORTCUT_REACTION_ACK_REQUIRED"));}
}
