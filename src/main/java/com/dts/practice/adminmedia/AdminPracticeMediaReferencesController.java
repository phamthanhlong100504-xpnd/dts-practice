package com.dts.practice.adminmedia;

import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only Practice reference report. Not an authorization to delete Media. */
@RestController
@RequestMapping("/api/v1/questions/admin/media-references")
public class AdminPracticeMediaReferencesController {
    private final AdminPracticeMediaReferences service;

    public AdminPracticeMediaReferencesController(AdminPracticeMediaReferences service) {
        this.service = service;
    }

    @GetMapping("/{mediaId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('PERM_media:read')")
    public ResponseEntity<AdminPracticeMediaReferences.Report> get(@PathVariable UUID mediaId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.inspect(mediaId));
    }
}
