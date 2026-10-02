package com.dts.practice.controller;

import com.dts.practice.dto.response.ApiResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.service.QuestionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
@Tag(name = "Questions", description = "Browse driving license questions by chapter")
@SecurityRequirement(name = "BearerAuth")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/chapter/{chapter}")
    public ApiResponse<List<QuestionResponse>> getByChapter(
            @PathVariable Integer chapter,
            @RequestParam(required = false) String licenseClass
    ) {
        return ApiResponse.ok(
                questionService.getByChapter(chapter, licenseClass)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getById(
            @PathVariable Integer id,
            @RequestParam(required = false) String licenseClass
    ) {
        return ApiResponse.ok(
                questionService.getById(id, licenseClass)
        );
    }

    @GetMapping("/critical")
    public ApiResponse<List<QuestionResponse>> getCritical(
            @RequestParam(required = false) String licenseClass
    ) {
        return ApiResponse.ok(
                questionService.getCriticalQuestions(licenseClass)
        );
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats(
            @RequestParam(required = false) String licenseClass
    ) {
        return ApiResponse.ok(Map.of(
                "total", questionService.countAll(licenseClass),
                "byChapter", Map.of(
                        1, questionService.countByChapter(1, licenseClass),
                        2, questionService.countByChapter(2, licenseClass),
                        3, questionService.countByChapter(3, licenseClass),
                        4, questionService.countByChapter(4, licenseClass),
                        5, questionService.countByChapter(5, licenseClass),
                        6, questionService.countByChapter(6, licenseClass)
                )
        ));
    }
}