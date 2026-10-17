package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ChapterLearnedAnswerResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ChapterLearnedAnswerQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/learning/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapter learning")
@SecurityRequirement(name = "BearerAuth")
public class ChapterLearnedAnswerQueryController {

    private final ChapterLearnedAnswerQueryService queryService;

    @GetMapping("/{chapterId}/answers")
    public ApiResponse<List<ChapterLearnedAnswerResponse>> getAnswers(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                queryService.getAnswers(
                        user.userId(),
                        chapterId,
                        licenseClass
                )
        );
    }
}