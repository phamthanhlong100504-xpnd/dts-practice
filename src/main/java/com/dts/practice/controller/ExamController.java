package com.dts.practice.controller;

import com.dts.practice.dto.request.StartExamRequest;
import com.dts.practice.dto.request.SubmitAnswerRequest;
import com.dts.practice.dto.response.*;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@Tag(name = "Exams", description = "Start, take, and review driving license practice exams")
@SecurityRequirement(name = "BearerAuth")
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start a new exam or practice session")
    public ApiResponse<ExamSessionResponse> startExam(
            @AuthenticationPrincipal JwtUserDetails user,
            @Valid @RequestBody StartExamRequest request) {
        return ApiResponse.ok(examService.startExam(user, request));
    }

    @GetMapping("/{examId}")
    @Operation(summary = "Get exam session details")
    public ApiResponse<ExamSessionResponse> getSession(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId) {
        return ApiResponse.ok(examService.getExamSession(examId, user.userId()));
    }

    @PostMapping("/{examId}/answers")
    @Operation(summary = "Submit an answer for a question (in practice mode, returns explanation)")
    public ApiResponse<SubmitAnswerResponse> submitAnswer(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        return ApiResponse.ok(examService.submitAnswer(examId, user.userId(), request));
    }

    @PostMapping("/{examId}/finish")
    @Operation(summary = "Finish exam and get results")
    public ApiResponse<ExamResultResponse> finishExam(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId) {
        return ApiResponse.ok(examService.finishExam(examId, user.userId()));
    }

    @GetMapping("/{examId}/result")
    @Operation(summary = "Get exam result (must be completed)")
    public ApiResponse<ExamResultResponse> getResult(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId) {
        return ApiResponse.ok(examService.getExamResult(examId, user.userId()));
    }

    @GetMapping("/history")
    @Operation(summary = "Get exam history for current user")
    public ApiResponse<Page<ExamHistoryResponse>> getHistory(
            @AuthenticationPrincipal JwtUserDetails user,
            Pageable pageable) {
        return ApiResponse.ok(examService.getExamHistory(user.userId(), pageable));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get top scores leaderboard")
    public ApiResponse<List<ExamService.LeaderboardEntry>> getLeaderboard(
            @RequestParam(required = false) String examType,
            @RequestParam(required = false, defaultValue = "all") String period) {
        return ApiResponse.ok(examService.getLeaderboard(examType, period));
    }
}
