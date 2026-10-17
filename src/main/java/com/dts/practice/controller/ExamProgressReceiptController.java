package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamProgressReceiptResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ExamProgressReceiptService;
import com.dts.practice.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams")
@SecurityRequirement(name = "BearerAuth")
public class ExamProgressReceiptController {

    private final ExamProgressReceiptService receiptService;
    private final ExamService examService;

    @GetMapping("/{examId}/progress")
    @Operation(
            summary = "Get the canonical progress receipt for a finished exam"
    )
    public ApiResponse<ExamProgressReceiptResponse> getProgress(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId
    ) {
        Boolean passed = examService
                .getExamResult(examId, user.userId())
                .passed();

        return ApiResponse.ok(
                receiptService.getReceipt(
                        examId,
                        user.userId(),
                        passed
                )
        );
    }
}