package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@Tag(name="Simulation catalogue")
@SecurityRequirement(name="BearerAuth")
public class SimulationCatalogController {
    private final SimulationCatalogService service;
    @GetMapping("/catalog")
    @PreAuthorize("hasAuthority('PERM_simulations:read')")
    @Operation(summary="Read the published six-chapter simulation catalogue without learning progress or scoring hints")
    public ResponseEntity<ApiResponse<SimulationCatalogService.Catalog>> catalog(@RequestParam(required=false) String licenseClass) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(service.catalog(licenseClass)));
    }
    @GetMapping("/scenarios/{scenarioId}/thumbnail")
    @PreAuthorize("hasAuthority('PERM_simulations:read') and hasAuthority('PERM_media:read')")
    @Operation(summary="Resolve a private published-bank thumbnail before creating a learning attempt")
    public ResponseEntity<ApiResponse<SimulationThumbnailClient.Thumbnail>> thumbnail(@PathVariable String scenarioId,@RequestParam(required=false) String licenseClass,
                                                                                  @RequestParam(required=false) String bankVersion) {
        UUID id;
        try {id=SimulationTokens.canonical(scenarioId);} catch(Exception e) {throw SimulationFailure.invalid("scenarioId must be a canonical UUID");}
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(service.thumbnail(licenseClass,bankVersion,id)));
    }
}
