package com.dts.practice.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record WrongReviewSessionResponse(
        UUID sessionId,
        String licenseClass,
        String bankVersion,
        String status,
        Integer totalQuestions,
        Integer currentPosition,
        Integer currentQuestionId,
        List<Question> questions,
        Map<Integer, Answer> answers,
        Integer answeredQuestions,
        Integer correctCount,
        Integer wrongCount,
        Integer unansweredCount,
        Long remainingCount,
        Instant startedAt,
        Instant completedAt
) {
    public record Question(
            Integer questionId,
            Integer position,
            Integer chapterId,
            String chapterTitle,
            String questionText,
            Object options,
            Boolean isCritical,
            String correctAnswer,
            String explanation,
            String imageUrl,
            UUID mediaFileId,
            String previousWrongAnswer,
            Instant lastWrongAt,
            Boolean bookmarked
    ) {}

    public record Answer(
            String selectedAnswer,
            Boolean isCorrect,
            Instant answeredAt
    ) {}
}