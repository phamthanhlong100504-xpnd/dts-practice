package com.dts.practice.service;

import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WrongQuestionProgressService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void recordAnswer(UUID userId, String licenseClass, String bankVersion,
                             Integer questionId, String selectedAnswer, Instant answeredAt) {
        if (userId == null || bankVersion == null || bankVersion.isBlank()
                || questionId == null || answeredAt == null) {
            throw BusinessException.badRequest("Incomplete question answer");
        }

        String license = normalizeLicense(licenseClass);
        String answer = normalizeAnswer(selectedAnswer);

        List<AnswerCheck> checks = jdbcTemplate.query("""
                SELECT q.correct_answer,
                       EXISTS (
                           SELECT 1 FROM jsonb_array_elements(q.options) option
                           WHERE upper(btrim(option->>'label')) = ?
                       ) AS valid_option
                FROM questions q
                WHERE q.id = ? AND q.bank_version = ?
                  AND jsonb_exists(q.applicable_licenses, ?)
                """, (rs, rowNum) -> new AnswerCheck(
                rs.getString("correct_answer"),
                rs.getBoolean("valid_option")
        ), answer, questionId, bankVersion, license);

        if (checks.size() != 1) {
            throw BusinessException.badRequest(
                    "Question does not belong to this bank and license"
            );
        }
        if (!checks.get(0).validOption()) {
            throw BusinessException.of(
                    "INVALID_ANSWER_OPTION",
                    "Answer option does not belong to this question",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean correct = answer.equalsIgnoreCase(checks.get(0).correctAnswer());
        Timestamp time = Timestamp.from(answeredAt);

        jdbcTemplate.update("""
                INSERT INTO wrong_question_progress (
                    user_id, license_class, bank_version, question_id,
                    needs_review, last_correct, last_selected_answer,
                    last_wrong_answer, last_wrong_at, last_answered_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id, license_class, bank_version, question_id)
                DO UPDATE SET
                    needs_review = CASE
                        WHEN EXCLUDED.last_answered_at >= wrong_question_progress.last_answered_at
                        THEN EXCLUDED.needs_review
                        ELSE wrong_question_progress.needs_review END,
                    last_correct = CASE
                        WHEN EXCLUDED.last_answered_at >= wrong_question_progress.last_answered_at
                        THEN EXCLUDED.last_correct
                        ELSE wrong_question_progress.last_correct END,
                    last_selected_answer = CASE
                        WHEN EXCLUDED.last_answered_at >= wrong_question_progress.last_answered_at
                        THEN EXCLUDED.last_selected_answer
                        ELSE wrong_question_progress.last_selected_answer END,
                    last_wrong_answer = CASE
                        WHEN EXCLUDED.needs_review AND
                             (wrong_question_progress.last_wrong_at IS NULL OR
                              EXCLUDED.last_answered_at >= wrong_question_progress.last_wrong_at)
                        THEN EXCLUDED.last_wrong_answer
                        ELSE wrong_question_progress.last_wrong_answer END,
                    last_wrong_at = CASE
                        WHEN EXCLUDED.needs_review AND
                             (wrong_question_progress.last_wrong_at IS NULL OR
                              EXCLUDED.last_answered_at >= wrong_question_progress.last_wrong_at)
                        THEN EXCLUDED.last_wrong_at
                        ELSE wrong_question_progress.last_wrong_at END,
                    last_answered_at = GREATEST(
                        wrong_question_progress.last_answered_at,
                        EXCLUDED.last_answered_at
                    ),
                    updated_at = now()
                """,
                userId, license, bankVersion, questionId,
                !correct, correct, answer,
                correct ? null : answer,
                correct ? null : time,
                time
        );
    }

    @Transactional(readOnly = true)
    public long countNeedsReview(
            UUID userId,
            String licenseClass,
            String bankVersion
    ) {
        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM wrong_question_progress p
                JOIN questions q ON q.id = p.question_id
                WHERE p.user_id = ?
                  AND p.license_class = ?
                  AND p.bank_version = ?
                  AND p.needs_review
                  AND q.bank_version = p.bank_version
                  AND jsonb_exists(q.applicable_licenses, p.license_class)
                """,
                Long.class,
                userId,
                normalizeLicense(licenseClass),
                bankVersion
        );

        return count == null ? 0L : count;
    }

    @Transactional(readOnly = true)
    public List<ReviewState> findNeedsReview(
            UUID userId,
            String licenseClass,
            String bankVersion
    ) {
        return jdbcTemplate.query("""
                SELECT p.question_id, p.last_wrong_answer, p.last_wrong_at
                FROM wrong_question_progress p
                JOIN questions q ON q.id = p.question_id
                WHERE p.user_id = ?
                  AND p.license_class = ?
                  AND p.bank_version = ?
                  AND p.needs_review
                  AND q.bank_version = p.bank_version
                  AND jsonb_exists(q.applicable_licenses, p.license_class)
                ORDER BY p.last_wrong_at DESC NULLS LAST, p.question_id ASC
                """,
                (rs, rowNum) -> new ReviewState(
                        rs.getInt("question_id"),
                        rs.getString("last_wrong_answer"),
                        rs.getTimestamp("last_wrong_at") == null
                                ? null
                                : rs.getTimestamp("last_wrong_at").toInstant()
                ),
                userId,
                normalizeLicense(licenseClass),
                bankVersion
        );
    }

    private static String normalizeLicense(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest("License class is required");
        }

        String license = value.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(license);
        return license;
    }

    private static String normalizeAnswer(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest("Selected answer is required");
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private record AnswerCheck(
            String correctAnswer,
            boolean validOption
    ) {}

    public record ReviewState(
            Integer questionId,
            String previousWrongAnswer,
            Instant lastWrongAt
    ) {}
}