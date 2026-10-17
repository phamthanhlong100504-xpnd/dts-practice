package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.WrongQuestionOverviewResponse;
import com.dts.practice.dto.response.WrongQuestionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.WrongQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/questions/wrong")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Browse driving license questions")
@SecurityRequirement(name = "BearerAuth")
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping("/overview")
    @Operation(summary = "Count distinct questions answered incorrectly")
    public ApiResponse<WrongQuestionOverviewResponse> getOverview(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                wrongQuestionService.getOverview(
                        user.userId(),
                        licenseClass
                )
        );
    }

    @GetMapping
    @Operation(summary = "List distinct questions answered incorrectly")
    public ApiResponse<List<WrongQuestionResponse>> getWrongQuestions(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                wrongQuestionService.getWrongQuestions(
                        user.userId(),
                        licenseClass
                )
        );
    }
}