package com.dts.practice.dto.response;

public record WrongQuestionOverviewResponse(
        String licenseClass,
        long totalWrongQuestions,
        boolean hasWrongQuestions
) {}