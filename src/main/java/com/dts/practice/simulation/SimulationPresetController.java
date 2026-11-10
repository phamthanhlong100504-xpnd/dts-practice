package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulation-exams/presets")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_simulations:read') and hasAuthority('PERM_simulations:exam')")
public class SimulationPresetController {
    private final SimulationPresetService service;
    @GetMapping public ResponseEntity<ApiResponse<SimulationPresets.ListView>> list(@RequestParam String licenseClass){return ok(service.list(SimulationLicensePolicy.require(licenseClass)));}
    @GetMapping("/{presetSetId}")public ResponseEntity<ApiResponse<SimulationPresets.Detail>> detail(@PathVariable String presetSetId,@RequestParam String licenseClass){return ok(service.detail(id(presetSetId),SimulationLicensePolicy.require(licenseClass)));}
    private static UUID id(String value){try{return SimulationTokens.canonical(value);}catch(Exception e){throw SimulationExamFailure.invalid("A canonical preset set UUID is required");}}
    private static<T>ResponseEntity<ApiResponse<T>> ok(T value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));}
}
