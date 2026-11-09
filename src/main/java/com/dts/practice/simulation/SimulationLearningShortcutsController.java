package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static com.dts.practice.simulation.SimulationShortcutRequests.*;

@RestController
@RequestMapping("/api/v1/simulations/attempts/{attemptId}/shortcuts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_simulations:read') and hasAuthority('PERM_simulations:learn')")
public class SimulationLearningShortcutsController {
    private final SimulationShortcutService service;
    @GetMapping public ResponseEntity<?> get(@PathVariable String attemptId,@RequestParam String licenseClass){return ok(service.state("LEARNING",uuid(attemptId),SimulationLicensePolicy.require(licenseClass)));}
    @PostMapping("/enable") public ResponseEntity<?> enable(@PathVariable String attemptId,@RequestParam String licenseClass,@RequestBody JsonNode body){return ok(service.enable("LEARNING",uuid(attemptId),parse(body,"ENABLE"),SimulationLicensePolicy.require(licenseClass)));}
    @PostMapping("/skip-intro") public ResponseEntity<?> skip(@PathVariable String attemptId,@RequestParam String licenseClass,@RequestBody JsonNode body){return ok(service.skip("LEARNING",uuid(attemptId),parse(body,"SKIP_INTRO"),SimulationLicensePolicy.require(licenseClass)));}
    private static <T>ResponseEntity<ApiResponse<T>> ok(T value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));}
}
