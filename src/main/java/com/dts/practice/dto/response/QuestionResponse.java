package com.dts.practice.dto.response;

import java.util.UUID;

public record QuestionResponse(
        Integer id,
        Integer chapter,
        String questionText,
        Object options,
        Boolean isCritical,
        String imageUrl,
        UUID mediaFileId,
        String correctAnswer,
        String explanation
) {}