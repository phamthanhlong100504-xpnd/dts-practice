package com.dts.practice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CriticalOverviewSyncDto(
        String licenseClass,
        Integer totalQuestions,
        Integer learnedQuestions,
        List<CriticalItemSyncDto> questions
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CriticalItemSyncDto(
            Integer questionId,
            Integer chapterId,
            Boolean learned,
            String selectedAnswer,
            Boolean isCorrect,
            String lastStudiedAt
    ) {}
}
