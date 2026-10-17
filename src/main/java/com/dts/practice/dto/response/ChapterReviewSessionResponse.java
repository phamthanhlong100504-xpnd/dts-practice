package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ChapterReviewSessionResponse(
        UUID reviewSessionId,
        String licenseClass,
        String bankVersion,
        int chapterId,
        String status,
        int totalQuestions,
        int answeredQuestions,
        int currentPosition,
        Integer currentQuestionId,
        List<Question> questions,
        Map<Integer, Answer> answers,
        Instant startedAt,
        Instant completedAt
) {
    public record Question(
            Integer questionId,
            int position,
            String questionText,
            Object options,
            boolean isCritical,
            String imageUrl,
            UUID mediaFileId,
            boolean bookmarked
    ) {}

    public record Answer(
            Integer questionId,
            String selectedAnswer,
            boolean isCorrect,
            String correctAnswer,
            String explanation,
            Instant answeredAt
    ) {}
}