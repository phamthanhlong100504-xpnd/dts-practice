package com.dts.practice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WrongQuestionProgressService {

    @Transactional
    public void recordAnswer(UUID userId, String licenseClass, String bankVersion,
                             Integer questionId, String selectedAnswer, Instant answeredAt) {
        // Wrong question progress tracking has been consolidated to DTS_PROGRESS service (user_question_progress table).
        // This method is maintained as a safe no-op to decouple practice execution from progress tracking.
        log.debug("Skipping local wrong_question_progress record for user {}, question {}", userId, questionId);
    }

    @Transactional(readOnly = true)
    public long countNeedsReview(
            UUID userId,
            String licenseClass,
            String bankVersion
    ) {
        return 0L;
    }

    @Transactional(readOnly = true)
    public List<ReviewState> findNeedsReview(
            UUID userId,
            String licenseClass,
            String bankVersion
    ) {
        return List.of();
    }

    public record ReviewState(
            Integer questionId,
            String previousWrongAnswer,
            Instant lastWrongAt
    ) {}
}
