package com.dts.practice.simulation;

import com.dts.practice.config.JwtValidationProperties;
import com.dts.practice.config.SecurityConfig;
import com.dts.practice.controller.LicenseCatalogController;
import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.exception.GlobalExceptionHandler;
import com.dts.practice.security.JwtValidationFilter;
import com.dts.practice.service.LicenseCatalogService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.time.OffsetDateTime;
import java.util.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes={SecurityConfig.class,JwtValidationFilter.class,GlobalExceptionHandler.class,SimulationCatalogController.class,
        LicenseCatalogController.class,SimulationCatalogService.class,SimulationAccess.class,SimulationTokens.class,SimulationLicenseCapabilities.class})
@Import(SimulationCatalogApiTest.Props.class)
@TestPropertySource(properties="simulation.learning-enabled=true")
class SimulationCatalogApiTest {
    @TestConfiguration static class Props {@Bean JwtValidationProperties jwtValidationProperties() {return SimulationFixtures.PROPS;}}
    @Autowired MockMvc mvc;
    @MockBean SimulationBankClient banks;
    @MockBean SimulationThumbnailClient thumbnails;
    @MockBean LicenseCatalogService theory;
    final SimulationBank.Snapshot bank=SimulationFixtures.published().snapshot();
    final UUID id=bank.scenarios().getFirst().scenarioId();
    @BeforeEach void fixtures() {
        when(banks.current()).thenReturn(bank);when(banks.load(any())).thenReturn(bank);
        when(thumbnails.resolve(any(),any(),any())).thenAnswer(a->{SimulationBank.Scenario s=a.getArgument(2);
            return new SimulationThumbnailClient.Thumbnail(s.thumbnailMediaId(),s.scenarioId(),bank.bankVersion(),"http://localhost:9000/fixture/1.webp?signature=fixture",OffsetDateTime.now().plusSeconds(300));});
        when(theory.getCatalog()).thenReturn(List.of(item("A1"),item("B1"),item("B"),item("CE")));
    }
    static LicenseCatalogItemResponse item(String code) {return new LicenseCatalogItemResponse(code,"fixture","Hạng "+code,"fixture","theory-v1",600,60,30,20,27,1,true,true);}
    String auth(UUID user,String...rights) {return "Bearer "+SimulationFixtures.access(user,rights);}
    @ParameterizedTest @ValueSource(strings={"/api/v1/simulations/catalog?licenseClass=B","/api/v1/simulations/scenarios/00000000-0000-4000-8000-000000000001/thumbnail?licenseClass=B"})
    void missingJwtStopsBeforeDependencies(String path) throws Exception {mvc.perform(get(path)).andExpect(status().isUnauthorized());verifyNoInteractions(banks,thumbnails);}
    @Test void forgedSignatureStopsBeforeDependencies() throws Exception {
        mvc.perform(get("/api/v1/simulations/catalog?licenseClass=B").header("Authorization","Bearer e30.e30.invalid")).andExpect(status().isUnauthorized());verifyNoInteractions(banks,thumbnails);
    }
    @Test void roleOrTheoryPermissionDoesNotGrantSimulationRead() throws Exception {
        mvc.perform(get("/api/v1/simulations/catalog?licenseClass=B").header("Authorization",auth(SimulationFixtures.USER,"practice:read")))
                .andExpect(status().isForbidden());verifyNoInteractions(banks,thumbnails);
    }
    @Test void validLearnerSeesOnlySanitizedPublishedCatalogue() throws Exception {
        mvc.perform(get("/api/v1/simulations/catalog?licenseClass=B").header("Authorization",auth(SimulationFixtures.USER,"simulations:read")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
                .andExpect(jsonPath("data.userId").value(SimulationFixtures.USER.toString()))
                .andExpect(jsonPath("data.totalScenarios").value(120)).andExpect(jsonPath("data.chapters.length()").value(6))
                .andExpect(jsonPath("data.scenarios.length()").value(120)).andExpect(jsonPath("data.bankReady").value(true))
                .andExpect(jsonPath("data.scenarios[0].scoringTimeline").doesNotExist()).andExpect(jsonPath("data.scenarios[0].hazardSign").doesNotExist())
                .andExpect(jsonPath("data.scenarios[0].videoMediaId").doesNotExist()).andExpect(jsonPath("data.scenarios[0].videoUrl").doesNotExist())
                .andExpect(jsonPath("data.scenarios[0].isPracticed").doesNotExist());
        verifyNoInteractions(thumbnails);
    }
    @ParameterizedTest @ValueSource(strings={"A1","A","B1","UNKNOWN"})
    void unsupportedLicenseStopsBeforeBankLookup(String license) throws Exception {
        mvc.perform(get("/api/v1/simulations/catalog").param("licenseClass",license).header("Authorization",auth(SimulationFixtures.USER,"simulations:read")))
                .andExpect(status().isForbidden());verifyNoInteractions(banks,thumbnails);
    }
    @Test void missingLicenseAndMalformedUuidAreBadRequests() throws Exception {
        mvc.perform(get("/api/v1/simulations/catalog").header("Authorization",auth(SimulationFixtures.USER,"simulations:read")))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/simulations/scenarios/not-a-uuid/thumbnail?licenseClass=B").header("Authorization",auth(SimulationFixtures.USER,"simulations:read","media:read")))
                .andExpect(status().isBadRequest());verifyNoInteractions(banks,thumbnails);
    }
    @Test void thumbnailRequiresMediaReadInAdditionToSimulationRead() throws Exception {
        mvc.perform(get("/api/v1/simulations/scenarios/"+id+"/thumbnail?licenseClass=B").header("Authorization",auth(SimulationFixtures.USER,"simulations:read")))
                .andExpect(status().isForbidden());verifyNoInteractions(banks,thumbnails);
    }
    @Test void learnerThumbnailBindingComesFromPublishedSnapshotAndJwtSubject() throws Exception {
        UUID other=UUID.randomUUID();
        mvc.perform(get("/api/v1/simulations/scenarios/"+id+"/thumbnail").param("licenseClass","CE").param("bankVersion",bank.bankVersion())
                .param("userId",other.toString()).param("mediaId",UUID.randomUUID().toString()).header("X-User-Id",other)
                .header("Authorization",auth(SimulationFixtures.USER,"simulations:read","media:read")))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control","no-store"))
                .andExpect(jsonPath("data.scenarioId").value(id.toString())).andExpect(jsonPath("data.mediaId").value(bank.scenarios().getFirst().thumbnailMediaId().toString()));
        verify(thumbnails).resolve(argThat(a->a.userId().equals(SimulationFixtures.USER)),eq(bank),eq(bank.scenarios().getFirst()));
    }
    @Test void unknownScenarioCannotRequestArbitraryMedia() throws Exception {
        mvc.perform(get("/api/v1/simulations/scenarios/"+UUID.randomUUID()+"/thumbnail?licenseClass=B").header("Authorization",auth(SimulationFixtures.USER,"simulations:read","media:read")))
                .andExpect(status().isNotFound());verifyNoInteractions(thumbnails);
    }
    @Test void identityJwtWithoutExpiryIsNotAcceptedForSimulation() throws Exception {
        String jwt=Jwts.builder().issuer(SimulationFixtures.ISSUER).subject(SimulationFixtures.USER.toString()).issuedAt(new Date())
                .claim("permissions",List.of("simulations:read")).signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(SimulationFixtures.SECRET))).compact();
        mvc.perform(get("/api/v1/simulations/catalog?licenseClass=B").header("Authorization","Bearer "+jwt)).andExpect(status().isUnauthorized());verifyNoInteractions(banks,thumbnails);
    }
    @Test void theoryCatalogueKeepsOldFieldsAndAddsSimulationCapabilities() throws Exception {
        mvc.perform(get("/api/v1/exams/license-catalog").header("Authorization",auth(SimulationFixtures.USER,"practice:read")))
                .andExpect(status().isOk()).andExpect(jsonPath("data[0].simulationEnabled").value(false)).andExpect(jsonPath("data[1].simulationEnabled").value(false))
                .andExpect(jsonPath("data[2].simulationEnabled").value(true)).andExpect(jsonPath("data[2].simulationReady").value(true))
                .andExpect(jsonPath("data[2].simulationBankVersion").value(bank.bankVersion())).andExpect(jsonPath("data[3].simulationBankVersion").value(bank.bankVersion()))
                .andExpect(jsonPath("data[2].bankVersion").value("theory-v1")).andExpect(jsonPath("data[2].examAvailable").value(true));
    }
    @Test void sourceOutageDoesNotTakeTheoryCatalogueDown() throws Exception {
        when(banks.current()).thenThrow(SimulationFailure.unavailable());
        mvc.perform(get("/api/v1/exams/license-catalog").header("Authorization",auth(SimulationFixtures.USER,"practice:read")))
                .andExpect(status().isOk()).andExpect(jsonPath("data[2].simulationReady").value(false)).andExpect(jsonPath("data[2].simulationEnabled").value(true))
                .andExpect(jsonPath("data[2].examAvailable").value(true));
        mvc.perform(get("/api/v1/simulations/catalog?licenseClass=B").header("Authorization",auth(SimulationFixtures.USER,"simulations:read")))
                .andExpect(status().isServiceUnavailable());
    }
}
