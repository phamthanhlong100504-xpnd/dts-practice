package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ExamRetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams")
@SecurityRequirement(name = "BearerAuth")
public class ExamRetryController {

    private final ExamRetryService examRetryService;

    @PostMapping("/{examId}/retry")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Retry a completed exam with the same questions and order")
    public ApiResponse<ExamSessionResponse> retryExam(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId
    ) {
        return ApiResponse.ok(examRetryService.retryExam(examId, user));
    }
}