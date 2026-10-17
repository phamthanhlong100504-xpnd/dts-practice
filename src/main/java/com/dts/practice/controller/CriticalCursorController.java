package com.dts.practice.controller;

import com.dts.practice.dto.request.UpdateCriticalCursorRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.CriticalSessionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.CriticalCursorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions/critical/sessions")
@RequiredArgsConstructor
@Tag(name = "Critical questions", description = "Critical-question learning cursor")
@SecurityRequirement(name = "BearerAuth")
public class CriticalCursorController {

    private final CriticalCursorService criticalCursorService;

    @PutMapping("/{sessionId}/cursor")
    @Operation(summary = "Save the current question in a critical-question round")
    public ApiResponse<CriticalSessionResponse> saveCursor(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateCriticalCursorRequest request
    ) {
        return ApiResponse.ok(criticalCursorService.saveCursor(
                sessionId, user.userId(), request.questionId()));
    }
}