package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.UUID;

public record WrongQuestionResponse(
        Integer questionId,
        Integer sourceQuestionId,
        Integer chapterId,
        String questionText,
        Object options,
        String selectedAnswer,
        String correctAnswer,
        Boolean isCritical,
        String explanation,
        String imageUrl,
        UUID mediaFileId,
        Instant lastWrongAt
) {}