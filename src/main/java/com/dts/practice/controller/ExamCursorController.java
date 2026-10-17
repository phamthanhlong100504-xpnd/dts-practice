package com.dts.practice.controller;

import com.dts.practice.dto.request.UpdateExamCursorRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ExamCursorService;
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
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams")
@SecurityRequirement(name = "BearerAuth")
public class ExamCursorController {

    private final ExamCursorService examCursorService;

    @PutMapping("/{examId}/cursor")
    @Operation(summary = "Save the current question in an active exam")
    public ApiResponse<ExamSessionResponse> updateCursor(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId,
            @Valid @RequestBody UpdateExamCursorRequest request
    ) {
        return ApiResponse.ok(examCursorService.updateCursor(
                examId, user.userId(), request.currentQuestionId()));
    }
}