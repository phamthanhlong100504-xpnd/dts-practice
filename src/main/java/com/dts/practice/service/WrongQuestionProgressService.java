package com.dts.practice.service;

import com.dts.practice.client.ProgressSyncClient;
import com.dts.practice.client.WrongQuestionSyncDto;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WrongQuestionProgressService {

    private final ProgressSyncClient progressSyncClient;
    private final QuestionRepository questionRepository;

    @Transactional
    public void recordAnswer(UUID userId, String licenseClass, String bankVersion,
                             Integer questionId, String selectedAnswer, Instant answeredAt) {
        if (userId == null || questionId == null || selectedAnswer == null) {
            return;
        }

        Question question = questionRepository.findById(questionId).orElse(null);
        int chapterId = (question != null && question.getChapter() != null) ? question.getChapter() : 1;
        boolean isCritical = question != null && Boolean.TRUE.equals(question.getIsCritical());
        boolean isCorrect = question != null && selectedAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer());

        // When answered correctly, needsReview becomes false. When wrong, needsReview remains true.
        progressSyncClient.syncQuestionProgress(
                userId,
                licenseClass,
                bankVersion,
                questionId,
                chapterId,
                isCritical,
                "REVIEW",
                null,
                !isCorrect,
                answeredAt != null ? answeredAt : Instant.now()
        );
    }

    @Transactional(readOnly = true)
    public long countNeedsReview(
            UUID userId,
            String licenseClass,
            String bankVersion
    ) {
        return findNeedsReview(userId, licenseClass, bankVersion).size();
    }

    @Transactional(readOnly = true)
    public List<ReviewState> findNeedsReview(
            UUID userId,
            String licenseClass,
            String bankVersion
    ) {
        String license = normalizeLicense(licenseClass);
        List<WrongQuestionSyncDto> wrongItems = progressSyncClient.getWrongQuestions(userId, license);
        if (wrongItems == null || wrongItems.isEmpty()) {
            return List.of();
        }

        return wrongItems.stream()
                .map(item -> new ReviewState(
                        item.questionId(),
                        item.lastWrongAnswer(),
                        item.lastWrongAt() != null ? item.lastWrongAt() : item.lastAnsweredAt()
                ))
                .toList();
    }

    private static String normalizeLicense(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest("License class is required");
        }
        String license = value.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(license);
        return license;
    }

    public record ReviewState(
            Integer questionId,
            String previousWrongAnswer,
            Instant lastWrongAt
    ) {}
}
