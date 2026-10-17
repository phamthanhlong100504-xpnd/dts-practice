package com.dts.practice.controller;

import com.dts.practice.dto.request.StartCriticalSessionRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.CriticalSessionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.CriticalSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions/critical/sessions")
@RequiredArgsConstructor
@Tag(name = "Critical questions", description = "Critical-question learning and review rounds")
@SecurityRequirement(name = "BearerAuth")
public class CriticalSessionController {

    private final CriticalSessionService criticalSessionService;

    @PostMapping
    public ApiResponse<CriticalSessionResponse> start(
            @AuthenticationPrincipal JwtUserDetails user,
            @Valid @RequestBody StartCriticalSessionRequest request
    ) {
        return ApiResponse.ok(criticalSessionService.start(
                user.userId(), request.licenseClass(), request.mode(),
                Boolean.TRUE.equals(request.restart())));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<CriticalSessionResponse> get(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.ok(criticalSessionService.get(sessionId, user.userId()));
    }
}