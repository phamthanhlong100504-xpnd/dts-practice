package com.dts.practice.controller;

import com.dts.practice.dto.request.SaveChapterReviewCursorRequest;
import com.dts.practice.dto.request.StartChapterReviewRequest;
import com.dts.practice.dto.request.SubmitChapterReviewAnswerRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ChapterReviewSessionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ChapterReviewSessionService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/learning/chapters/{chapterId}/review-sessions")
@RequiredArgsConstructor
@Tag(
        name = "Chapter learning",
        description = "Persistent chapter review rounds"
)
@SecurityRequirement(name = "BearerAuth")
public class ChapterReviewSessionController {

    private final ChapterReviewSessionService service;

    @PostMapping
    public ApiResponse<ChapterReviewSessionResponse> start(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @Valid @RequestBody StartChapterReviewRequest request
    ) {
        return ApiResponse.ok(
                service.start(
                        user.userId(),
                        chapterId,
                        request.licenseClass()
                )
        );
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<ChapterReviewSessionResponse> get(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.ok(
                service.get(
                        sessionId,
                        user.userId(),
                        chapterId
                )
        );
    }

    @PostMapping("/{sessionId}/answers")
    public ApiResponse<ChapterReviewSessionResponse> answer(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitChapterReviewAnswerRequest request
    ) {
        return ApiResponse.ok(
                service.answer(
                        sessionId,
                        user.userId(),
                        chapterId,
                        request.questionId(),
                        request.selectedAnswer()
                )
        );
    }

    @PutMapping("/{sessionId}/cursor")
    public ApiResponse<ChapterReviewSessionResponse> cursor(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SaveChapterReviewCursorRequest request
    ) {
        return ApiResponse.ok(
                service.saveCursor(
                        sessionId,
                        user.userId(),
                        chapterId,
                        request.currentQuestionId()
                )
        );
    }

    @PostMapping("/{sessionId}/finish")
    public ApiResponse<ChapterReviewSessionResponse> finish(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable int chapterId,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.ok(
                service.finish(
                        sessionId,
                        user.userId(),
                        chapterId
                )
        );
    }
}