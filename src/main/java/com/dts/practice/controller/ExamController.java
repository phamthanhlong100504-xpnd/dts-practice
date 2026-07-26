package com.dts.practice.controller;

import com.dts.practice.dto.request.StartExamRequest;
import com.dts.practice.dto.request.SubmitAnswerRequest;
import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.security.JwtUserDetails;
import com.dts.practice.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ExamSessionResponse> startExam(
            @AuthenticationPrincipal JwtUserDetails user,
            @Valid @RequestBody StartExamRequest request) {
        return ApiResponse.ok(examService.startExam(user, request));
    }

    @GetMapping("/{examId}")
    public ApiResponse<ExamSessionResponse> getSession(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId) {
        return ApiResponse.ok(examService.getExamSession(examId, user.userId()));
    }

    @PostMapping("/{examId}/answers")
    public ApiResponse<Map<String, String>> submitAnswer(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId,
            @Valid @RequestBody SubmitAnswerRequest request) {
        examService.submitAnswer(examId, user.userId(), request);
        return ApiResponse.ok(Map.of("status", "answered"));
    }

    @PostMapping("/{examId}/finish")
    public ApiResponse<ExamResultResponse> finishExam(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId) {
        return ApiResponse.ok(examService.finishExam(examId, user.userId()));
    }

    @GetMapping("/{examId}/result")
    public ApiResponse<ExamResultResponse> getResult(
            @AuthenticationPrincipal JwtUserDetails user,
            @PathVariable UUID examId) {
        return ApiResponse.ok(examService.getExamResult(examId, user.userId()));
    }

    @GetMapping("/history")
    public ApiResponse<Page<Exam>> getHistory(
            @AuthenticationPrincipal JwtUserDetails user,
            Pageable pageable) {
        return ApiResponse.ok(examService.getExamHistory(user.userId(), pageable));
    }
}
