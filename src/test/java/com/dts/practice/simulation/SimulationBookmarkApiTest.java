package com.dts.practice.simulation;

import com.dts.practice.config.JwtValidationProperties;
import com.dts.practice.config.SecurityConfig;
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
import org.springframework.http.MediaType;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class,JwtValidationFilter.class,GlobalExceptionHandler.class,
        SimulationBookmarkController.class,SimulationBookmarkAdvice.class,SimulationLearningController.class})
@Import(SimulationBookmarkApiTest.Props.class)
class SimulationBookmarkApiTest {
    @TestConfiguration static class Props {
        @Bean JwtValidationProperties jwtValidationProperties() {return SimulationFixtures.PROPS;}
    }
    @Autowired MockMvc mvc;
    @MockBean SimulationBookmarkService bookmarks;
    @MockBean SimulationLearningService learning;
    @MockBean SimulationLearningReads reads;
    static final String PREFIX="/api/v1/simulations",ID="00000000-0000-4000-8000-000000000001";
    String bearer(String... rights) {return "Bearer "+SimulationFixtures.access(SimulationFixtures.USER,rights);}

    @ParameterizedTest @ValueSource(strings={"/bookmarks/overview?licenseClass=B","/bookmarks?licenseClass=B"})
    void missingOrTheoryOnlyJwtCannotReadBookmarks(String path) throws Exception {
        mvc.perform(get(PREFIX+path)).andExpect(status().isUnauthorized());
        mvc.perform(get(PREFIX+path).header("Authorization",bearer("practice:read","practice:learn"))).andExpect(status().isForbidden());
        verifyNoInteractions(bookmarks,learning,reads);
    }
    @ParameterizedTest @ValueSource(strings={"/bookmarks/overview?licenseClass=B","/bookmarks?licenseClass=B"})
    void forgedExpiredAndWrongIssuerAreRejected(String path) throws Exception {
        String[] parts=SimulationFixtures.access(SimulationFixtures.USER,"simulations:read","media:read").split("\\.");
        String forged=parts[0]+"."+parts[1]+"."+(parts[2].charAt(0)=='A'?"B":"A")+parts[2].substring(1);
        mvc.perform(get(PREFIX+path).header("Authorization","Bearer "+forged)).andExpect(status().isUnauthorized());
        var key=io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(SimulationFixtures.SECRET));
        for(String issuer:List.of(SimulationFixtures.ISSUER,"wrong-issuer")) {
            var now=java.time.Instant.now();String token=io.jsonwebtoken.Jwts.builder().issuer(issuer).subject(ID)
                    .issuedAt(Date.from(now.minusSeconds(600)))
                    .expiration(Date.from(issuer.equals(SimulationFixtures.ISSUER)?now.minusSeconds(2):now.plusSeconds(300)))
                    .claim("permissions",List.of("simulations:read","media:read")).signWith(key).compact();
            mvc.perform(get(PREFIX+path).header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(bookmarks,learning,reads);
    }
    @Test void overviewNeedsOnlyReadAndHasNoStoreHeader() throws Exception {
        mvc.perform(get(PREFIX+"/bookmarks/overview?licenseClass=B").header("Authorization",bearer("simulations:read")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
        verify(bookmarks).overview("B",null);
    }
    @Test void listRequiresMediaButNotLearnAndBindsTheDatasetQuery() throws Exception {
        mvc.perform(get(PREFIX+"/bookmarks?licenseClass=B").header("Authorization",bearer("simulations:read"))).andExpect(status().isForbidden());
        mvc.perform(get(PREFIX+"/bookmarks?licenseClass=CE&bankVersion=bank-v1&chapterId=2&page=3&size=4&sort=SCENARIO_ASC&expectedDatasetVersion="+"a".repeat(64))
                .header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
        verify(bookmarks).list("CE","bank-v1",2,"SCENARIO_ASC",3,4,"a".repeat(64));
    }
    @ParameterizedTest @ValueSource(strings={"chapterId=bad","page=bad","size=bad"})
    void invalidNumericQueryReturns400(String query) throws Exception {
        mvc.perform(get(PREFIX+"/bookmarks?licenseClass=B&"+query).header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SIMULATION_INVALID_REQUEST"));
        verifyNoInteractions(bookmarks);
    }
    @Test void listErrorsRetainStatusAndCodeInsteadOfEmptySuccess() throws Exception {
        doThrow(SimulationLearningRules.conflict("SIMULATION_BOOKMARKS_CHANGED","refresh")).when(bookmarks).list(any(),any(),any(),any(),anyInt(),anyInt(),any());
        mvc.perform(get(PREFIX+"/bookmarks?licenseClass=B").header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SIMULATION_BOOKMARKS_CHANGED"));
        doThrow(SimulationFailure.unavailable()).when(bookmarks).list(any(),any(),any(),any(),anyInt(),anyInt(),any());
        mvc.perform(get(PREFIX+"/bookmarks?licenseClass=B").header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("SIMULATION_DEPENDENCY_UNAVAILABLE"));
    }
    @Test void existingPutStillNeedsLearnAndFullScopedBody() throws Exception {
        String body="{\"licenseClass\":\"B\",\"bankVersion\":\"bank-v1\",\"bookmarked\":false}";
        mvc.perform(put(PREFIX+"/bookmarks/"+ID).contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization",bearer("simulations:read","media:read"))).andExpect(status().isForbidden());
        doReturn(Map.of("scenarioId",ID,"bankVersion","bank-v1","bookmarked",false)).when(learning).bookmark(any(),any());
        mvc.perform(put(PREFIX+"/bookmarks/"+ID).contentType(MediaType.APPLICATION_JSON).content(body)
                .header("Authorization",bearer("simulations:read","simulations:learn")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.bookmarked").value(false));
        verify(learning).bookmark(eq(UUID.fromString(ID)),argThat(r->r.licenseClass().equals("B")&&r.bankVersion().equals("bank-v1")&&!r.bookmarked()));
    }
    @Test void oldPutFailureIsNotReportedAsSuccessfulUnbookmark() throws Exception {
        doThrow(SimulationFailure.unavailable()).when(learning).bookmark(any(),any());
        mvc.perform(put(PREFIX+"/bookmarks/"+ID).contentType(MediaType.APPLICATION_JSON)
                .content("{\"licenseClass\":\"B\",\"bankVersion\":\"bank-v1\",\"bookmarked\":false}")
                .header("Authorization",bearer("simulations:read","simulations:learn")))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("SIMULATION_DEPENDENCY_UNAVAILABLE"));
    }
}
