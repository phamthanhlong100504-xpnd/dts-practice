package com.dts.practice.simulation;

import com.dts.practice.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.dts.practice.simulation.SimulationExamModel.*;

@RestController
@RequestMapping("/api/v1/simulation-exams")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('PERM_simulations:read') and hasAuthority('PERM_simulations:exam')")
@SecurityRequirement(name="BearerAuth")
@Tag(name="Simulation mock exams")
public class SimulationExamController {
    private static final String MEDIA="hasAuthority('PERM_simulations:read') and hasAuthority('PERM_simulations:exam') and hasAuthority('PERM_media:read')";
    private final SimulationExamService service;
    @GetMapping("/preparation") public ResponseEntity<ApiResponse<Preparation>> preparation(@RequestParam String licenseClass){return ok(service.preparation(license(licenseClass)));}
    @GetMapping("/sessions/active") public ResponseEntity<ApiResponse<Active>> active(@RequestParam(required=false)String licenseClass){return ok(service.active(license(licenseClass)));}
    @PostMapping("/sessions") @PreAuthorize(MEDIA)
    public ResponseEntity<ApiResponse<Session>> create(@RequestBody JsonNode body,HttpServletRequest http) {
        String mode=text(body,"mode");
        if("RANDOM".equals(mode))fields(body,Set.of("mode","licenseClass"),Set.of("bankVersion"));
        else if("PRESET".equals(mode))fields(body,Set.of("mode","licenseClass","presetSetId","presetVersionId"),Set.of("bankVersion"));
        else throw SimulationExamFailure.invalid("Mode must be RANDOM or PRESET");var keys=Collections.list(http.getHeaders("Idempotency-Key"));
        if(keys.size()!=1)throw SimulationExamFailure.invalid("Exactly one Idempotency-Key UUID header is required");
        return ok(service.create(uuid(keys.getFirst()),new Create(mode,text(body,"licenseClass"),body.has("bankVersion")?text(body,"bankVersion"):null,body.has("presetSetId")?uuid(text(body,"presetSetId")):null,body.has("presetVersionId")?uuid(text(body,"presetVersionId")):null)));
    }
    @GetMapping("/sessions/{sessionId}") public ResponseEntity<ApiResponse<Session>> get(@PathVariable String sessionId,@RequestParam(required=false)String licenseClass){return ok(service.get(uuid(sessionId),license(licenseClass)));}
    @GetMapping("/sessions/{sessionId}/current/playback") @PreAuthorize(MEDIA)
    public ResponseEntity<ApiResponse<Playback>> playback(@PathVariable String sessionId,@RequestParam(required=false)String licenseClass){return ok(service.playback(uuid(sessionId),license(licenseClass)));}
    @PostMapping("/sessions/{sessionId}/current/restart") public ResponseEntity<ApiResponse<Session>> restart(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass) {
        fields(body,Set.of("eventId","position","playbackEventId"),Set.of());return ok(service.restart(uuid(sessionId),new Restart(uuid(text(body,"eventId")),number(body,"position",1,10),uuid(text(body,"playbackEventId"))),license(licenseClass)));
    }
    @PostMapping("/sessions/{sessionId}/current/start") public ResponseEntity<ApiResponse<Session>> start(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass){return ok(service.start(uuid(sessionId),event(body),license(licenseClass)));}
    @PostMapping("/sessions/{sessionId}/current/heartbeat") public ResponseEntity<ApiResponse<Session>> heartbeat(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass){return ok(service.heartbeat(uuid(sessionId),event(body),license(licenseClass)));}
    @PutMapping("/sessions/{sessionId}/reaction") public ResponseEntity<ApiResponse<Session>> reaction(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass){return ok(service.reaction(uuid(sessionId),event(body),license(licenseClass)));}
    @PostMapping("/sessions/{sessionId}/complete-current") public ResponseEntity<ApiResponse<Session>> complete(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass){return ok(service.complete(uuid(sessionId),event(body),license(licenseClass)));}
    @PostMapping("/sessions/{sessionId}/submit") public ResponseEntity<ApiResponse<Session>> submit(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass){return ok(service.submit(uuid(sessionId),command(body),license(licenseClass)));}
    @PostMapping("/sessions/{sessionId}/cancel") public ResponseEntity<ApiResponse<Session>> cancel(@PathVariable String sessionId,@RequestBody JsonNode body,@RequestParam(required=false)String licenseClass){return ok(service.cancel(uuid(sessionId),command(body),license(licenseClass)));}
    @GetMapping("/sessions/{sessionId}/result") public ResponseEntity<ApiResponse<Result>> result(@PathVariable String sessionId,@RequestParam(required=false)String licenseClass){return ok(service.result(uuid(sessionId),license(licenseClass)));}
    @GetMapping("/sessions/{sessionId}/review") public ResponseEntity<ApiResponse<Review>> review(@PathVariable String sessionId,@RequestParam(required=false)String licenseClass){return ok(service.review(uuid(sessionId),license(licenseClass)));}
    @GetMapping("/sessions/{sessionId}/review/{position}/playback") @PreAuthorize(MEDIA)
    public ResponseEntity<ApiResponse<Playback>> reviewPlayback(@PathVariable String sessionId,@PathVariable int position,@RequestParam(required=false)String licenseClass){return ok(service.reviewPlayback(uuid(sessionId),position,license(licenseClass)));}
    private static Event event(JsonNode body){fields(body,Set.of("eventId","position","playbackEventId","mediaTimeMs"),Set.of());return new Event(uuid(text(body,"eventId")),number(body,"position",1,10),uuid(text(body,"playbackEventId")),number(body,"mediaTimeMs",0,Integer.MAX_VALUE));}
    private static Command command(JsonNode body){fields(body,Set.of("eventId"),Set.of());return new Command(uuid(text(body,"eventId")));}
    private static void fields(JsonNode body,Set<String> required,Set<String> optional) {
        if(body==null||!body.isObject())throw SimulationExamFailure.invalid("An object body is required");Set<String> keys=new HashSet<>();body.fieldNames().forEachRemaining(keys::add);Set<String> allowed=new HashSet<>(required);allowed.addAll(optional);
        if(!keys.containsAll(required)||!allowed.containsAll(keys))throw SimulationExamFailure.invalid("Missing or unsupported request fields");
    }
    private static String text(JsonNode body,String name){if(!body.path(name).isTextual()||body.path(name).asText().isBlank())throw SimulationExamFailure.invalid("A text field is required: "+name);return body.path(name).asText();}
    private static int number(JsonNode body,String name,int min,int max){var n=body.path(name);if(!n.isIntegralNumber()||!n.canConvertToInt()||n.asInt()<min||n.asInt()>max)throw SimulationExamFailure.invalid("Invalid integer field: "+name);return n.asInt();}
    private static UUID uuid(String text){try{return SimulationTokens.canonical(text);}catch(Exception e){throw SimulationExamFailure.invalid("A canonical UUID is required");}}
    private static String license(String value){return value==null?null:SimulationLicensePolicy.require(value);}
    private static<T>ResponseEntity<ApiResponse<T>> ok(T value){return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(value));}
}
