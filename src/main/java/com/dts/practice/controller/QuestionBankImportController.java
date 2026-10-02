package com.dts.practice.controller;

import com.dts.practice.dto.request.ImportQuestionBankRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.service.QuestionBankImportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions/admin")
@RequiredArgsConstructor
@Tag(name = "Question bank import")
@SecurityRequirement(name = "BearerAuth")
public class QuestionBankImportController {

    private final QuestionBankImportService questionBankImportService;

    @PostMapping("/import-bank")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> importBank(
            @Valid @RequestBody ImportQuestionBankRequest request) {
        int imported = questionBankImportService.importBank(request);

        return ApiResponse.ok(Map.of(
                "bankVersion", request.bankVersion(),
                "imported", imported
        ));
    }
}