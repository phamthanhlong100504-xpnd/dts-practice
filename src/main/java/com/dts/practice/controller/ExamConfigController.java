package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamConfigResponse;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.service.ExamMatrix;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/exams")
@Tag(name = "Exams")
@SecurityRequirement(name = "BearerAuth")
public class ExamConfigController {

    @GetMapping("/config")
    @Operation(summary = "Get the canonical exam rules for a license class")
    public ApiResponse<ExamConfigResponse> getConfig(
            @RequestParam("examType") String examType
    ) {
        if (examType.isBlank()) {
            throw BusinessException.badRequest("Exam type is required");
        }

        String licenseClass = examType.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.Rule rule = ExamMatrix.forClass(licenseClass);

        int ordinaryQuestions = rule.ordinaryByChapter().values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        return ApiResponse.ok(new ExamConfigResponse(
                licenseClass,
                rule.totalQuestions(),
                rule.durationMinutes(),
                rule.requiredCorrect(),
                rule.totalQuestions() - ordinaryQuestions
        ));
    }
}