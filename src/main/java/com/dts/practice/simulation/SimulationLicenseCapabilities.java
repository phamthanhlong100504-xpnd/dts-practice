package com.dts.practice.simulation;

import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SimulationLicenseCapabilities {
    private final SimulationBankClient banks;
    private final boolean learningEnabled;
    public SimulationLicenseCapabilities(SimulationBankClient banks,@Value("${simulation.learning-enabled:true}") boolean learningEnabled) {
        this.banks=banks;this.learningEnabled=learningEnabled;
    }
    public List<LicenseCatalogItemResponse> enrich(List<LicenseCatalogItemResponse> catalog) {
        if(catalog==null || catalog.isEmpty()) return List.of();
        String currentVersion=null;
        try {currentVersion=banks.current().bankVersion();} catch(BusinessException unavailable) {
            // Simulation outages must not hide the existing theory catalogue.
        }
        String version=currentVersion;
        return catalog.stream().map(item->{
            boolean enabled=SimulationLicensePolicy.enabled(item.code());
            return item.withSimulation(enabled,enabled && learningEnabled && version!=null,enabled?version:null);
        }).toList();
    }
}
