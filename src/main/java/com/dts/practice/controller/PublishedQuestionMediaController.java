package com.dts.practice.controller;

import com.dts.practice.repository.QuestionRepository;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.CurrentQuestionBank;
import com.dts.practice.service.PublishedQuestionMediaTicket;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/questions/published-image-tickets")
@RequiredArgsConstructor
public class PublishedQuestionMediaController {
    private final QuestionRepository questions;
    private final PublishedQuestionMediaTicket tickets;

    @GetMapping("/{mediaId}")
    @PreAuthorize("hasAuthority('PERM_practice:read') and hasAuthority('PERM_media:read')")
    public ResponseEntity<Map<String, String>> issue(@PathVariable UUID mediaId) {
        // Proof must come from the published learner bank, not from client-supplied metadata.
        if (!questions.existsByBankVersionAndMediaFileId(CurrentQuestionBank.currentVersion(), mediaId))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Published question image not found");
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtUserDetails user))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(Map.of("ticket", tickets.issue(user.userId(), mediaId)));
    }
}
