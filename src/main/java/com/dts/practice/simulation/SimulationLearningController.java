package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static com.dts.practice.simulation.SimulationLearningModel.*;

@RestController
@RequestMapping("/api/v1/simulations")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_simulations:read')")
@SecurityRequirement(name="BearerAuth")
@Tag(name="Simulation learning")
public class SimulationLearningController {
    private final SimulationLearningService service;private final SimulationLearningReads reads;
    private static final String LEARN="hasAuthority('PERM_simulations:read') and hasAuthority('PERM_simulations:learn')";
    private static final String MEDIA="hasAuthority('PERM_simulations:read') and hasAuthority('PERM_media:read')";
    @GetMapping("/overview") public ResponseEntity<?> overview(@RequestParam(required=false) String licenseClass,@RequestParam(required=false) String bankVersion) {return ok(reads.overview(licenseClass,bankVersion));}
    @GetMapping("/chapters/{chapterId}/scenarios") @PreAuthorize(MEDIA)
    public ResponseEntity<?> chapter(@PathVariable int chapterId,@RequestParam(required=false) String licenseClass,@RequestParam(required=false) String bankVersion,
            @RequestParam(defaultValue="ALL") String filter,@RequestParam(defaultValue="SCENARIO_ASC") String sort,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="12") int size) {
        return ok(reads.chapter(chapterId,licenseClass,bankVersion,filter,sort,page,size));
    }
    @GetMapping("/scenarios/{scenarioId}") @PreAuthorize(MEDIA)
    public ResponseEntity<?> scenario(@PathVariable String scenarioId,@RequestParam(required=false) String licenseClass,@RequestParam(required=false) String bankVersion) {return ok(reads.scenario(uuid(scenarioId),licenseClass,bankVersion));}
    @PostMapping("/attempts") @PreAuthorize(LEARN) public ResponseEntity<?> create(@Valid @RequestBody Create request) {return ok(service.create(request));}
    @GetMapping("/attempts/{attemptId}") public ResponseEntity<?> get(@PathVariable String attemptId) {return ok(service.get(uuid(attemptId)));}
    @GetMapping("/attempts/{attemptId}/result") public ResponseEntity<?> result(@PathVariable String attemptId) {return ok(service.result(uuid(attemptId)));}
    @GetMapping("/attempts/{attemptId}/playback") @PreAuthorize(LEARN+" and hasAuthority('PERM_media:read')")
    public ResponseEntity<?> playback(@PathVariable String attemptId) {return ok(service.playback(uuid(attemptId)));}
    @PostMapping("/attempts/{attemptId}/start") @PreAuthorize(LEARN) public ResponseEntity<?> start(@PathVariable String attemptId,@Valid @RequestBody Event request) {return ok(service.start(uuid(attemptId),request));}
    @PostMapping("/attempts/{attemptId}/heartbeat") @PreAuthorize(LEARN) public ResponseEntity<?> heartbeat(@PathVariable String attemptId,@Valid @RequestBody Event request) {return ok(service.heartbeat(uuid(attemptId),request));}
    @PutMapping("/attempts/{attemptId}/response") @PreAuthorize(LEARN) public ResponseEntity<?> response(@PathVariable String attemptId,@Valid @RequestBody Event request) {return ok(service.response(uuid(attemptId),request));}
    @PostMapping("/attempts/{attemptId}/complete") @PreAuthorize(LEARN) public ResponseEntity<?> complete(@PathVariable String attemptId,@Valid @RequestBody Event request) {return ok(service.complete(uuid(attemptId),request));}
    @PostMapping("/attempts/{attemptId}/interrupt") @PreAuthorize(LEARN) public ResponseEntity<?> interrupt(@PathVariable String attemptId,@Valid @RequestBody Interrupt request) {return ok(service.interrupt(uuid(attemptId),request));}
    @PostMapping("/attempts/{attemptId}/cancel") @PreAuthorize(LEARN) public ResponseEntity<?> cancel(@PathVariable String attemptId,@Valid @RequestBody Cancel request) {return ok(service.cancel(uuid(attemptId),request));}
    @PutMapping("/bookmarks/{scenarioId}") @PreAuthorize(LEARN) public ResponseEntity<?> bookmark(@PathVariable String scenarioId,@Valid @RequestBody Bookmark request) {return ok(service.bookmark(uuid(scenarioId),request));}
    @PutMapping("/chapters/{chapterId}/cursor") @PreAuthorize(LEARN) public ResponseEntity<?> cursor(@PathVariable int chapterId,@Valid @RequestBody CursorCommand request) {return ok(service.cursor(chapterId,request));}
    static UUID uuid(String text) {try {return SimulationTokens.canonical(text);} catch(Exception e) {throw SimulationFailure.invalid("Use a canonical UUID");}}
    private static <T> ResponseEntity<ApiResponse<T>> ok(T value) {return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));}
}
