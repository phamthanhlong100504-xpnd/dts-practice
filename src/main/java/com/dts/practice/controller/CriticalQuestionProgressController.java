package com.dts.practice.controller;

import com.dts.practice.dto.request.UpdateCriticalProgressRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.CriticalProgressOverviewResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.CriticalQuestionProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions/critical")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Browse driving license questions")
@SecurityRequirement(name = "BearerAuth")
public class CriticalQuestionProgressController {

    private final CriticalQuestionProgressService progressService;

    @GetMapping("/overview")
    @Operation(summary = "Get critical question learning progress")
    public ApiResponse<CriticalProgressOverviewResponse> getOverview(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                progressService.getOverview(user.userId(), licenseClass)
        );
    }

    @PutMapping("/{questionId}/progress")
    @Operation(summary = "Mark a critical question as learned or not learned")
    public ApiResponse<CriticalProgressOverviewResponse> updateProgress(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable Integer questionId,
            @Valid @RequestBody UpdateCriticalProgressRequest request
    ) {
        return ApiResponse.ok(
                progressService.updateProgress(
                        user.userId(),
                        questionId,
                        request
                )
        );
    }
}