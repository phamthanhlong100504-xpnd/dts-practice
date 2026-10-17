package com.dts.practice.service;

import com.dts.practice.dto.response.WrongReviewAnswerResponse;
import com.dts.practice.dto.response.WrongReviewSessionResponse;
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
public class WrongReviewInteractionService {

    private static final String CURRENT_BANK =
            CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;
    private final WrongQuestionProgressService wrongQuestionProgressService;
    private final WrongReviewSessionService wrongReviewSessionService;

    @Transactional
    public WrongReviewAnswerResponse submitAnswer(
            UUID sessionId,
            UUID userId,
            Integer questionId,
            String selectedAnswer
    ) {
        Session session = lockSession(sessionId, userId);
        requireInProgress(session);

        if (questionId == null
                || selectedAnswer == null
                || selectedAnswer.isBlank()) {
            throw BusinessException.badRequest(
                    "Question and selected answer are required"
            );
        }

        String answer = selectedAnswer
                .trim()
                .toUpperCase(Locale.ROOT);

        List<QuestionCheck> questions = jdbcTemplate.query("""
                SELECT q.correct_answer,
                       q.explanation,
                       EXISTS (
                           SELECT 1
                           FROM jsonb_array_elements(q.options) option
                           WHERE upper(btrim(option->>'label')) = ?
                       ) AS valid_option
                FROM wrong_question_review_session_questions sq
                JOIN questions q
                  ON q.id = sq.question_id
                WHERE sq.session_id = ?
                  AND sq.question_id = ?
                  AND q.bank_version = ?
                  AND jsonb_exists(
                      q.applicable_licenses,
                      ?
                  )
                """,
                (rs, rowNum) -> new QuestionCheck(
                        rs.getString("correct_answer"),
                        rs.getString("explanation"),
                        rs.getBoolean("valid_option")
                ),
                answer,
                sessionId,
                questionId,
                session.bankVersion(),
                session.licenseClass()
        );

        if (questions.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not in this review session"
            );
        }

        QuestionCheck question = questions.get(0);

        if (!question.validOption()) {
            throw BusinessException.of(
                    "INVALID_ANSWER_OPTION",
                    "Answer option does not belong to this question",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<SavedAnswer> existing = jdbcTemplate.query("""
                SELECT selected_answer,
                       is_correct,
                       answered_at
                FROM wrong_question_review_answers
                WHERE session_id = ?
                  AND question_id = ?
                """,
                (rs, rowNum) -> new SavedAnswer(
                        rs.getString("selected_answer"),
                        rs.getBoolean("is_correct"),
                        rs.getTimestamp("answered_at").toInstant()
                ),
                sessionId,
                questionId
        );

        // Request gửi lại cùng đáp án không ghi lại progress hoặc answered_at.
        if (!existing.isEmpty()
                && existing.get(0).selectedAnswer().equals(answer)) {
            SavedAnswer saved = existing.get(0);

            return new WrongReviewAnswerResponse(
                    questionId,
                    saved.selectedAnswer(),
                    saved.isCorrect(),
                    question.correctAnswer(),
                    question.explanation(),
                    saved.answeredAt()
            );
        }

        boolean correct = answer.equalsIgnoreCase(
                question.correctAnswer()
        );

        Timestamp storedTime = jdbcTemplate.queryForObject("""
                INSERT INTO wrong_question_review_answers
                    (session_id, question_id, selected_answer,
                     is_correct, answered_at)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (session_id, question_id)
                DO UPDATE SET
                    selected_answer = EXCLUDED.selected_answer,
                    is_correct = EXCLUDED.is_correct,
                    answered_at = EXCLUDED.answered_at
                RETURNING answered_at
                """,
                (rs, rowNum) -> rs.getTimestamp(1),
                sessionId,
                questionId,
                answer,
                correct,
                Timestamp.from(Instant.now())
        );

        Instant answeredAt = storedTime.toInstant();

        // Đáp án của session và needsReview commit cùng một transaction.
        wrongQuestionProgressService.recordAnswer(
                userId,
                session.licenseClass(),
                session.bankVersion(),
                questionId,
                answer,
                answeredAt
        );

        return new WrongReviewAnswerResponse(
                questionId,
                answer,
                correct,
                question.correctAnswer(),
                question.explanation(),
                answeredAt
        );
    }

    @Transactional
    public WrongReviewSessionResponse saveCursor(
            UUID sessionId,
            UUID userId,
            Integer questionId
    ) {
        Session session = lockSession(sessionId, userId);
        requireInProgress(session);

        if (questionId == null) {
            throw BusinessException.badRequest(
                    "Question ID is required"
            );
        }

        List<Integer> positions = jdbcTemplate.query("""
                SELECT sq.question_order
                FROM wrong_question_review_session_questions sq
                JOIN questions q
                  ON q.id = sq.question_id
                WHERE sq.session_id = ?
                  AND sq.question_id = ?
                  AND q.bank_version = ?
                  AND jsonb_exists(
                      q.applicable_licenses,
                      ?
                  )
                """,
                (rs, rowNum) -> rs.getInt("question_order"),
                sessionId,
                questionId,
                session.bankVersion(),
                session.licenseClass()
        );

        if (positions.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not in this review session"
            );
        }

        if (!questionId.equals(session.currentQuestionId())) {
            jdbcTemplate.update("""
                    UPDATE wrong_question_review_sessions
                    SET current_question_id = ?
                    WHERE id = ?
                      AND user_id = ?
                      AND status = 'IN_PROGRESS'
                    """,
                    questionId,
                    sessionId,
                    userId
            );
        }

        return wrongReviewSessionService.get(
                sessionId,
                userId
        );
    }

    private Session lockSession(
            UUID sessionId,
            UUID userId
    ) {
        List<Session> sessions = jdbcTemplate.query("""
                SELECT license_class,
                       bank_version,
                       status,
                       current_question_id
                FROM wrong_question_review_sessions
                WHERE id = ?
                  AND user_id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new Session(
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getString("status"),
                        rs.getInt("current_question_id")
                ),
                sessionId,
                userId
        );

        if (sessions.size() != 1) {
            throw BusinessException.notFound(
                    "Wrong-question review session not found"
            );
        }

        Session session = sessions.get(0);

        if (!CURRENT_BANK.equals(session.bankVersion())) {
            throw BusinessException.conflict(
                    "Question bank version changed; start a new review"
            );
        }

        return session;
    }

    private static void requireInProgress(Session session) {
        if (!"IN_PROGRESS".equals(session.status())) {
            throw BusinessException.conflict(
                    "Review session is already completed"
            );
        }
    }

    private record Session(
            String licenseClass,
            String bankVersion,
            String status,
            Integer currentQuestionId
    ) {}

    private record QuestionCheck(
            String correctAnswer,
            String explanation,
            boolean validOption
    ) {}

    private record SavedAnswer(
            String selectedAnswer,
            boolean isCorrect,
            Instant answeredAt
    ) {}
}