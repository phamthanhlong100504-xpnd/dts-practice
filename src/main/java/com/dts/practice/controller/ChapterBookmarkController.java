package com.dts.practice.controller;

import com.dts.practice.dto.request.SetChapterBookmarkRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ChapterBookmarkResponse;
import com.dts.practice.dto.response.ChapterBookmarksResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ChapterBookmarkService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/learning/chapters")
@RequiredArgsConstructor
@Tag(name = "Chapter learning")
@SecurityRequirement(name = "BearerAuth")
public class ChapterBookmarkController {

    private final ChapterBookmarkService chapterBookmarkService;

    @GetMapping("/{chapterId}/bookmarks")
    public ApiResponse<ChapterBookmarksResponse> list(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @RequestParam String licenseClass
    ) {
        return ApiResponse.ok(
                chapterBookmarkService.list(
                        user.userId(),
                        chapterId,
                        licenseClass
                )
        );
    }

    @PutMapping("/{chapterId}/bookmarks/{questionId}")
    public ApiResponse<ChapterBookmarkResponse> set(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @PathVariable Integer questionId,
            @Valid @RequestBody SetChapterBookmarkRequest request
    ) {
        return ApiResponse.ok(
                chapterBookmarkService.set(
                        user.userId(),
                        chapterId,
                        questionId,
                        request
                )
        );
    }
}