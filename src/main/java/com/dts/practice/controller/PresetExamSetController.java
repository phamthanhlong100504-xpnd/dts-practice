package com.dts.practice.controller;

import com.dts.practice.dto.request.StartPresetExamRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.dto.response.PresetExamSetListResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.PresetExamSetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams/presets")
@RequiredArgsConstructor
@Tag(name = "Preset exams", description = "Published fixed practice exam sets")
@SecurityRequirement(name = "BearerAuth")
public class PresetExamSetController {

    private final PresetExamSetService presetExamSetService;

    @GetMapping
    public ApiResponse<PresetExamSetListResponse> list(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                presetExamSetService.getPublishedSets(
                        user.userId(),
                        licenseClass
                )
        );
    }

    @PostMapping("/{presetSetId}/sessions")
    public ApiResponse<ExamSessionResponse> startSession(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID presetSetId,
            @RequestBody(required = false) StartPresetExamRequest request
    ) {
        return ApiResponse.ok(
                presetExamSetService.startSession(
                        user,
                        presetSetId,
                        request
                )
        );
    }
}