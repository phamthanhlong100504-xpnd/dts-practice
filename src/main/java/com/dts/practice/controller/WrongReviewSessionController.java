package com.dts.practice.controller;

import com.dts.practice.dto.request.SaveWrongReviewCursorRequest;
import com.dts.practice.dto.request.StartWrongReviewRequest;
import com.dts.practice.dto.request.SubmitWrongReviewAnswerRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.WrongReviewAnswerResponse;
import com.dts.practice.dto.response.WrongReviewSessionResponse;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.WrongReviewFinishService;
import com.dts.practice.service.WrongReviewInteractionService;
import com.dts.practice.service.WrongReviewSessionService;
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
@RequestMapping("/api/v1/wrong-questions/sessions")
@RequiredArgsConstructor
@Tag(
        name = "Wrong-question review",
        description = "Review questions currently needing attention"
)
@SecurityRequirement(name = "BearerAuth")
public class WrongReviewSessionController {

    private final WrongReviewSessionService wrongReviewSessionService;
    private final WrongReviewInteractionService wrongReviewInteractionService;
    private final WrongReviewFinishService wrongReviewFinishService;

    @PostMapping
    public ApiResponse<WrongReviewSessionResponse> start(
            @AuthenticationPrincipal JwtUserDetails user,
            @Valid @RequestBody StartWrongReviewRequest request
    ) {
        return ApiResponse.ok(
                wrongReviewSessionService.start(
                        user.userId(),
                        request.licenseClass()
                )
        );
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<WrongReviewSessionResponse> get(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.ok(
                wrongReviewSessionService.get(
                        sessionId,
                        user.userId()
                )
        );
    }

    @PostMapping("/{sessionId}/answers")
    public ApiResponse<WrongReviewAnswerResponse> submitAnswer(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SubmitWrongReviewAnswerRequest request
    ) {
        return ApiResponse.ok(
                wrongReviewInteractionService.submitAnswer(
                        sessionId,
                        user.userId(),
                        request.questionId(),
                        request.selectedAnswer()
                )
        );
    }

    @PutMapping("/{sessionId}/cursor")
    public ApiResponse<WrongReviewSessionResponse> saveCursor(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID sessionId,
            @Valid @RequestBody SaveWrongReviewCursorRequest request
    ) {
        return ApiResponse.ok(
                wrongReviewInteractionService.saveCursor(
                        sessionId,
                        user.userId(),
                        request.questionId()
                )
        );
    }

    @PostMapping("/{sessionId}/finish")
    public ApiResponse<WrongReviewSessionResponse> finish(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID sessionId
    ) {
        return ApiResponse.ok(
                wrongReviewFinishService.finish(
                        sessionId,
                        user.userId()
                )
        );
    }
}