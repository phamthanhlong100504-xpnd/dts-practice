package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.service.LicenseCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams")
@SecurityRequirement(name = "BearerAuth")
public class LicenseCatalogController {

    private final LicenseCatalogService licenseCatalogService;

    @GetMapping("/license-catalog")
    @Operation(summary = "List license classes, canonical exam rules and current bank counts")
    public ApiResponse<List<LicenseCatalogItemResponse>> getLicenseCatalog() {
        return ApiResponse.ok(licenseCatalogService.getCatalog());
    }
}