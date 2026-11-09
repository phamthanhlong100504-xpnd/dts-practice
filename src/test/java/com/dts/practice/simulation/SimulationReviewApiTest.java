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
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class, JwtValidationFilter.class, GlobalExceptionHandler.class,
        SimulationReviewController.class, SimulationReviewAdvice.class})
@Import(SimulationReviewApiTest.Props.class)
class SimulationReviewApiTest {
    @TestConfiguration static class Props {
        @Bean JwtValidationProperties jwtValidationProperties() {return SimulationFixtures.PROPS;}
    }
    @Autowired MockMvc mvc;
    @MockBean SimulationReviewService reviews;
    @MockBean SimulationReplayService replay;
    static final String PREFIX="/api/v1/simulations", ID="00000000-0000-4000-8000-000000000001";
    String bearer(String... rights) {return "Bearer "+SimulationFixtures.access(SimulationFixtures.USER, rights);}
    static final String OVERVIEW="/review-needed/overview?licenseClass=B";

    @ParameterizedTest @ValueSource(strings={"/review-needed/overview?licenseClass=B", "/review-needed?licenseClass=B", "/attempts/00000000-0000-4000-8000-000000000001/replay"})
    void nativeMappingsRejectAnonymousAndTheoryOnlyRights(String path) throws Exception {
        mvc.perform(get(PREFIX+path)).andExpect(status().isUnauthorized());
        mvc.perform(get(PREFIX+path).header("Authorization", bearer("practice:read", "practice:learn"))).andExpect(status().isForbidden());
        verifyNoInteractions(reviews, replay);
    }
    @ParameterizedTest @ValueSource(strings={"/review-needed/overview?licenseClass=B", "/review-needed?licenseClass=B", "/attempts/00000000-0000-4000-8000-000000000001/replay"})
    void forgedExpiredAndWrongIssuerTokensFailBeforeServices(String path) throws Exception {
        String valid=SimulationFixtures.access(SimulationFixtures.USER,"simulations:read","media:read");
        String[] parts=valid.split("\\."); String signature=parts[2];
        String forged=parts[0]+"."+parts[1]+"."+(signature.charAt(0)=='A'?"B":"A")+signature.substring(1);
        mvc.perform(get(PREFIX+path).header("Authorization","Bearer "+forged)).andExpect(status().isUnauthorized());
        var key=io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(SimulationFixtures.SECRET));
        for (String issuer:List.of(SimulationFixtures.ISSUER,"wrong-issuer")) {
            var now=java.time.Instant.now();
            String token=io.jsonwebtoken.Jwts.builder().issuer(issuer).subject(ID).issuedAt(Date.from(now.minusSeconds(600)))
                    .expiration(Date.from(issuer.equals(SimulationFixtures.ISSUER)?now.minusSeconds(5):now.plusSeconds(300)))
                    .claim("permissions",List.of("simulations:read","media:read")).signWith(key).compact();
            mvc.perform(get(PREFIX+path).header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(reviews,replay);
    }
    @Test void overviewNeedsReadButDoesNotRequireLearningOrMediaPermission() throws Exception {
        mvc.perform(get(PREFIX+OVERVIEW).header("Authorization",bearer("simulations:read")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
        verify(reviews).overview("B",null);
    }
    @ParameterizedTest @ValueSource(strings={"/review-needed?licenseClass=B", "/attempts/00000000-0000-4000-8000-000000000001/replay"})
    void privateReadRequiresMediaButNotLearn(String path) throws Exception {
        mvc.perform(get(PREFIX+path).header("Authorization",bearer("simulations:read"))).andExpect(status().isForbidden());
        verifyNoInteractions(reviews,replay);
        mvc.perform(get(PREFIX+path).header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"));
    }
    @Test void listBindsAllPagingAndScopeParameters() throws Exception {
        mvc.perform(get(PREFIX+"/review-needed?licenseClass=CE&bankVersion=bank-v1&chapterId=2&page=3&size=4&sort=BEST_SCORE_ASC&expectedDatasetVersion="+"a".repeat(64))
                .header("Authorization",bearer("simulations:read","media:read"))).andExpect(status().isOk());
        verify(reviews).list("CE","bank-v1",2,"BEST_SCORE_ASC",3,4,"a".repeat(64));
    }
    @ParameterizedTest @ValueSource(strings={"/review-needed?licenseClass=B&chapterId=bad", "/review-needed?licenseClass=B&page=bad", "/review-needed?licenseClass=B&size=bad", "/attempts/not-uuid/replay"})
    void invalidBindingOrUuidReturns400(String path) throws Exception {
        mvc.perform(get(PREFIX+path).header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("SIMULATION_INVALID_REQUEST"));
        verifyNoInteractions(reviews,replay);
    }
    @Test void serviceConflictsAndNotFoundAreNotConvertedIntoEmpty200() throws Exception {
        when(reviews.list(any(),any(),any(),any(),anyInt(),anyInt(),any())).thenThrow(
                SimulationLearningRules.conflict("SIMULATION_REVIEW_CHANGED","refresh"));
        mvc.perform(get(PREFIX+"/review-needed?licenseClass=B").header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("SIMULATION_REVIEW_CHANGED"));
        when(replay.replay(any())).thenThrow(SimulationFailure.notFound());
        mvc.perform(get(PREFIX+"/attempts/"+ID+"/replay").header("Authorization",bearer("simulations:read","media:read")))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("SIMULATION_NOT_FOUND"));
    }
}
