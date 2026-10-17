package com.dts.practice.controller;

import com.dts.practice.dto.request.UpdateExamFlagRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamFlagsResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ExamQuestionFlagService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams/{examId}")
@RequiredArgsConstructor
@Tag(name = "Exam question flags", description = "Saved flags for questions in an exam")
@SecurityRequirement(name = "BearerAuth")
public class ExamQuestionFlagController {

    private final ExamQuestionFlagService flagService;

    @GetMapping("/flags")
    public ApiResponse<ExamFlagsResponse> getFlags(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId
    ) {
        return ApiResponse.ok(
                flagService.getFlags(examId, user.userId())
        );
    }

    @PutMapping("/questions/{questionId}/flag")
    public ApiResponse<ExamFlagsResponse> updateFlag(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId,
            @PathVariable Integer questionId,
            @Valid @RequestBody UpdateExamFlagRequest request
    ) {
        return ApiResponse.ok(
                flagService.updateFlag(
                        examId,
                        user.userId(),
                        questionId,
                        request.flagged()
                )
        );
    }
}