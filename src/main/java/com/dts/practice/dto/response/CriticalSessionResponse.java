package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record CriticalSessionResponse(
        UUID sessionId,
        String licenseClass,
        String bankVersion,
        String mode,
        String status,
        Integer totalQuestions,
        Integer answeredQuestions,
        Integer currentPosition,
        Integer currentQuestionId,
        List<Question> questions,
        Map<Integer, Answer> answers,
        Instant startedAt,
        Instant completedAt
) {
    public record Question(
            Integer questionId,
            Integer position,
            Integer chapterId,
            String questionText,
            Object options,
            Boolean isCritical,
            String imageUrl,
            UUID mediaFileId,
            Boolean bookmarked,
            Boolean learnedBeforeSession
    ) {}

    public record Answer(
            Integer questionId,
            String selectedAnswer,
            Boolean isCorrect,
            String correctAnswer,
            String explanation,
            Instant answeredAt
    ) {}
}