package com.dts.practice.simulation;

import com.dts.practice.service.LicenseCatalogService;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulationAdminPolicyControllerTest {
    @Test void adminPoliciesAreRealPracticePoliciesAndRandomHasNoInventedChapterQuotas() {
        var banks=mock(SimulationBankClient.class);
        var catalog=mock(LicenseCatalogService.class);
        when(banks.current()).thenReturn(SimulationFixtures.published().snapshot());
        var controller=new SimulationAdminPolicyController(new SimulationExamRules(),banks,catalog,true);
        var policies=controller.policies().getBody().data();
        assertEquals(2,policies.size());
        var random=policies.getFirst();var preset=policies.getLast();
        assertEquals("RANDOM",random.mode());assertNull(random.chapterQuotas());
        assertEquals("PRESET",preset.mode());assertEquals(List.of(2,1,2,1,2,2),preset.chapterQuotas());
        assertEquals(10,random.totalScenarios());assertEquals(50,preset.maxTotalScore());
        assertEquals(35,random.passingScore());
        assertEquals("READY",random.dataStatus());
        verify(banks,times(1)).current();
    }
    @Test void unavailableBankIsNotPresentedAsAvailable() {
        var banks=mock(SimulationBankClient.class);
        when(banks.current()).thenThrow(new IllegalStateException("unavailable"));
        var catalog=mock(LicenseCatalogService.class);
        when(catalog.getCatalog()).thenReturn(List.of(SimulationCatalogApiTest.item("A1"),SimulationCatalogApiTest.item("B")));
        var controller=new SimulationAdminPolicyController(new SimulationExamRules(),banks,catalog,true);
        var licenses=controller.capabilities().getBody().data();
        assertFalse(licenses.getFirst().simulationEnabled());
        assertEquals("LICENSE_NOT_SUPPORTED",licenses.getFirst().readinessReason());
        assertTrue(licenses.getLast().simulationEnabled());
        assertFalse(licenses.getLast().simulationReady());
        assertEquals("UNAVAILABLE",licenses.getLast().dataStatus());
        assertEquals("BANK_UNAVAILABLE",licenses.getLast().readinessReason());
    }
}
