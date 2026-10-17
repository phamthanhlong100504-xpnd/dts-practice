package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.LearningBankCatalogResponse;
import com.dts.practice.service.LearningBankCatalogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/questions/learning-catalog")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
public class LearningBankCatalogController {

    private final LearningBankCatalogService service;

    @GetMapping
    public ApiResponse<LearningBankCatalogResponse> get(
            @RequestParam String licenseClass) {
        return ApiResponse.ok(service.get(licenseClass));
    }
}
