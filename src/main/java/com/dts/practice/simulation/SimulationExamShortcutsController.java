package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static com.dts.practice.simulation.SimulationShortcutRequests.*;

@RestController
@RequestMapping("/api/v1/simulation-exams/sessions/{sessionId}/shortcuts")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_simulations:read') and hasAuthority('PERM_simulations:exam')")
public class SimulationExamShortcutsController {
    private final SimulationShortcutService service;
    @GetMapping public ResponseEntity<?> get(@PathVariable String sessionId,@RequestParam String licenseClass){return ok(service.state("EXAM",uuid(sessionId),SimulationLicensePolicy.require(licenseClass)));}
    @PostMapping("/enable") public ResponseEntity<?> enable(@PathVariable String sessionId,@RequestParam String licenseClass,@RequestBody JsonNode body){return ok(service.enable("EXAM",uuid(sessionId),parse(body,"ENABLE"),SimulationLicensePolicy.require(licenseClass)));}
    @PostMapping("/skip-intro") public ResponseEntity<?> skip(@PathVariable String sessionId,@RequestParam String licenseClass,@RequestBody JsonNode body){return ok(service.skip("EXAM",uuid(sessionId),parse(body,"SKIP_INTRO"),SimulationLicensePolicy.require(licenseClass)));}
    @PostMapping("/advance-after-reaction") public ResponseEntity<?> advance(@PathVariable String sessionId,@RequestParam String licenseClass,@RequestBody JsonNode body){return ok(service.advance(uuid(sessionId),parse(body,"ADVANCE_AFTER_REACTION"),SimulationLicensePolicy.require(licenseClass)));}
    private static <T>ResponseEntity<ApiResponse<T>> ok(T value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));}
}
