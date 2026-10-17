package com.dts.practice.controller;

import com.dts.practice.dto.request.SubmitCriticalAnswerRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.CriticalAnswerResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.CriticalAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions/critical/answers")
@RequiredArgsConstructor
@Tag(name = "Critical questions", description = "Critical-question learning answers")
@SecurityRequirement(name = "BearerAuth")
public class CriticalAnswerController {

    private final CriticalAnswerService criticalAnswerService;

    @PostMapping
    @Operation(summary = "Answer a question in the current critical-question round")
    public ApiResponse<CriticalAnswerResponse> submitAnswer(
            @AuthenticationPrincipal JwtUserDetails user,
            @Valid @RequestBody SubmitCriticalAnswerRequest request
    ) {
        return ApiResponse.ok(criticalAnswerService.submitAnswer(
                user.userId(), request));
    }
}