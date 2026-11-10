package com.dts.practice.adminpresets;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.security.JwtUserDetails;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Separate permission for every transition; client never supplies an actor ID. */
@RestController
@RequestMapping("/api/v1/exams/admin/presets")
public class PresetPublicationController {
    private final PresetPublicationService service;
    public PresetPublicationController(PresetPublicationService service){this.service=service;}
    public record Submit(long expectedRevision) {}
    public record Reject(String reason) {}

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:submit')")
    public ResponseEntity<ApiResponse<PresetPublicationService.Review>> submit(
            @PathVariable UUID id,@RequestBody Submit input,@AuthenticationPrincipal JwtUserDetails actor) {
        return noStore(service.submit(id,input.expectedRevision(),actor.userId()));
    }
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:approve')")
    public ResponseEntity<ApiResponse<PresetPublicationService.Review>> approve(
            @PathVariable UUID id,@AuthenticationPrincipal JwtUserDetails actor) {
        return noStore(service.approve(id,actor.userId()));
    }
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:approve')")
    public ResponseEntity<ApiResponse<PresetPublicationService.Review>> reject(
            @PathVariable UUID id,@RequestBody Reject input,@AuthenticationPrincipal JwtUserDetails actor) {
        return noStore(service.reject(id,actor.userId(),input.reason()));
    }
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:publish')")
    public ResponseEntity<ApiResponse<PresetPublicationService.Receipt>> publish(
            @PathVariable UUID id,@AuthenticationPrincipal JwtUserDetails actor) {
        return noStore(service.publish(id,actor.userId()));
    }
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:archive')")
    public ResponseEntity<ApiResponse<PresetPublicationService.Receipt>> archive(
            @PathVariable UUID id,@AuthenticationPrincipal JwtUserDetails actor) {
        return noStore(service.archive(id,actor.userId()));
    }
    @GetMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:read')")
    public ResponseEntity<ApiResponse<PresetPublicationService.Review>> review(@PathVariable UUID id){
        return noStore(service.review(id));
    }
    @GetMapping("/logical/{logicalId}/versions")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_practice-exam-sets:read')")
    public ResponseEntity<ApiResponse<List<PresetPublicationService.Version>>> versions(@PathVariable UUID logicalId){
        return noStore(service.versions(logicalId));
    }
    private static <T> ResponseEntity<ApiResponse<T>> noStore(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponse.ok(body));
    }
}
