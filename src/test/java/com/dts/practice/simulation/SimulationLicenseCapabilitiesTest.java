package com.dts.practice.simulation;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulationLicenseCapabilitiesTest {
    @Test void publishedBankDoesNotAdvertiseLearningUntilLearningApisAreEnabled() {
        var banks=mock(SimulationBankClient.class);var bank=SimulationFixtures.published().snapshot();when(banks.current()).thenReturn(bank);
        var caps=new SimulationLicenseCapabilities(banks,false);var original=SimulationCatalogApiTest.item("B");var row=caps.enrich(List.of(original)).getFirst();
        assertTrue(row.simulationEnabled());assertFalse(row.simulationReady());assertEquals(bank.bankVersion(),row.simulationBankVersion());
        assertEquals(original.bankVersion(),row.bankVersion());assertEquals(original.totalStudyQuestions(),row.totalStudyQuestions());assertEquals(original.examAvailable(),row.examAvailable());
    }
    @Test void multipleLicensesResolveCurrentBankOnlyOncePerCatalogueRequest() {
        var banks=mock(SimulationBankClient.class);when(banks.current()).thenReturn(SimulationFixtures.published().snapshot());
        var result=new SimulationLicenseCapabilities(banks,true).enrich(List.of(SimulationCatalogApiTest.item("B"),SimulationCatalogApiTest.item("CE")));
        assertTrue(result.stream().allMatch(i->i.simulationReady()));assertEquals(result.getFirst().simulationBankVersion(),result.getLast().simulationBankVersion());verify(banks,times(1)).current();
    }
}
