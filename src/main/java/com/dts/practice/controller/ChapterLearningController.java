package com.dts.practice.controller;

import com.dts.practice.dto.request.RecordChapterAnswerRequest;
import com.dts.practice.dto.request.SaveChapterCursorRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ChapterLearningAnswerResponse;
import com.dts.practice.dto.response.ChapterLearningCursorResponse;
import com.dts.practice.dto.response.ChapterLearningOverviewResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ChapterLearningService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapter learning", description = "Study progress in the current question bank")
@SecurityRequirement(name = "BearerAuth")
public class ChapterLearningController {

    private final ChapterLearningService chapterLearningService;

    @GetMapping
    public ApiResponse<ChapterLearningOverviewResponse> overview(
            @AuthenticationPrincipal JwtUserDetails user,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                chapterLearningService.overview(user.userId(), licenseClass)
        );
    }

    @PostMapping("/{chapterId}/answers")
    public ApiResponse<ChapterLearningAnswerResponse> answer(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @Valid @RequestBody RecordChapterAnswerRequest request
    ) {
        return ApiResponse.ok(
                chapterLearningService.recordAnswer(
                        user.userId(), chapterId, request
                )
        );
    }

    @PutMapping("/{chapterId}/cursor")
    public ApiResponse<ChapterLearningCursorResponse> saveCursor(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @Valid @RequestBody SaveChapterCursorRequest request
    ) {
        return ApiResponse.ok(
                chapterLearningService.saveCursor(
                        user.userId(), chapterId, request
                )
        );
    }
}