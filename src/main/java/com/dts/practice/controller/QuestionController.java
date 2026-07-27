package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.service.QuestionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Browse 600 driving license test questions by chapter")
@SecurityRequirement(name = "BearerAuth")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/chapter/{chapter}")
    public ApiResponse<List<QuestionResponse>> getByChapter(@PathVariable Integer chapter) {
        return ApiResponse.ok(questionService.getByChapter(chapter));
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getById(@PathVariable Integer id) {
        return ApiResponse.ok(questionService.getById(id));
    }

    @GetMapping("/critical")
    public ApiResponse<List<QuestionResponse>> getCritical() {
        return ApiResponse.ok(questionService.getCriticalQuestions());
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.ok(Map.of(
            "total", questionService.countAll(),
            "byChapter", Map.of(
                1, questionService.countByChapter(1),
                2, questionService.countByChapter(2),
                3, questionService.countByChapter(3),
                4, questionService.countByChapter(4),
                5, questionService.countByChapter(5),
                6, questionService.countByChapter(6)
            )
        ));
    }
}
