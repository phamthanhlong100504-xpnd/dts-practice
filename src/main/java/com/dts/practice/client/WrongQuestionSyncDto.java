package com.dts.practice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WrongQuestionSyncDto(
        Integer questionId,
        Integer chapterId,
        Boolean isCritical,
        String lastWrongAnswer,
        Instant lastWrongAt,
        Instant lastAnsweredAt
) {}
