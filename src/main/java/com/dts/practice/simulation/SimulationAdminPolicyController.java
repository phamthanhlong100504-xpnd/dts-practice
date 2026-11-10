package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.service.LicenseCatalogService;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only Admin view of exactly the same policy and license catalogue used by learners. */
@RestController
@RequestMapping("/api/v1/simulations/admin")
public class SimulationAdminPolicyController {
    public record ExamPolicy(String mode,String bankVersion,String policyVersion,int totalScenarios,
            int maxScenarioScore,int maxTotalScore,int passingScore,List<Integer> chapterQuotas,
            Instant effectiveFrom,Instant effectiveUntil,String dataStatus,String sourceService) {}
    public record LicenseCapability(String licenseClass,boolean simulationEnabled,boolean simulationReady,
            String bankVersion,String readinessReason,String progressScope,String dataStatus) {}
    private final SimulationExamRules rules;
    private final SimulationBankClient banks;
    private final LicenseCatalogService catalog;
    private final boolean learningEnabled;
    public SimulationAdminPolicyController(SimulationExamRules rules,SimulationBankClient banks,
            LicenseCatalogService catalog,@Value("${simulation.learning-enabled:true}") boolean learningEnabled) {
        this.rules=rules;this.banks=banks;this.catalog=catalog;this.learningEnabled=learningEnabled;
    }
    /** A missing live bank is not represented as a usable policy. */
    private String currentBank() {
        try {return banks.current().bankVersion();}
        catch (RuntimeException inaccessible) {return null;}
    }
    @GetMapping("/exam-policies")
    @PreAuthorize("(hasAuthority('ROLE_ADMIN') or hasAuthority('PERM_admin-portal:enter')) and hasAuthority('PERM_simulation-exam-sets:read')")
    public ResponseEntity<ApiResponse<List<ExamPolicy>>> policies() {
        String bank=currentBank();String status=bank==null?"UNAVAILABLE":"READY";
        var random=rules.currentPolicy();var preset=SimulationExamRules.PRESET_POLICY;
        // The 2-1-2-1-2-2 quota belongs only to the V1 fixed presets, not RANDOM.
        List<ExamPolicy> result=List.of(
            new ExamPolicy("RANDOM",bank,random.policyVersion(),random.totalScenarios(),random.maxScenarioScore(),
                random.maxTotalScore(),random.passingScore(),null,null,null,status,"dts-practice"),
            new ExamPolicy("PRESET",bank,preset.policyVersion(),preset.totalScenarios(),preset.maxScenarioScore(),
                preset.maxTotalScore(),preset.passingScore(),List.of(2,1,2,1,2,2),null,null,status,"dts-practice"));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(result));
    }
    @GetMapping("/license-capabilities")
    @PreAuthorize("(hasAuthority('ROLE_ADMIN') or hasAuthority('PERM_admin-portal:enter')) and hasAuthority('PERM_simulation-scenarios:read')")
    public ResponseEntity<ApiResponse<List<LicenseCapability>>> capabilities() {
        String bank=currentBank();
        List<LicenseCapability> result=catalog.getCatalog().stream().map(item->capability(item,bank)).toList();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(result));
    }
    private LicenseCapability capability(LicenseCatalogItemResponse item,String bank) {
        boolean enabled=SimulationLicensePolicy.enabled(item.code());
        boolean ready=enabled && learningEnabled && bank!=null;
        String reason=!enabled?"LICENSE_NOT_SUPPORTED":!learningEnabled?"MODULE_DISABLED":bank==null?"BANK_UNAVAILABLE":null;
        String status=bank==null && enabled && learningEnabled?"UNAVAILABLE":"READY";
        return new LicenseCapability(item.code(),enabled,ready,enabled?bank:null,reason,
            enabled?"USER_BANK_VERSION_SHARED_LICENSES":null,status);
    }
}
