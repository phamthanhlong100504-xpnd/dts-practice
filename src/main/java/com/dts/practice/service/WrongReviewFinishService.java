package com.dts.practice.service;

import com.dts.practice.dto.response.WrongReviewSessionResponse;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WrongReviewFinishService {

    private static final String CURRENT_BANK =
            CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;
    private final WrongQuestionProgressService wrongQuestionProgressService;
    private final WrongReviewSessionService wrongReviewSessionService;

    @Transactional
    public WrongReviewSessionResponse finish(
            UUID sessionId,
            UUID userId
    ) {
        List<Session> sessions = jdbcTemplate.query("""
                SELECT license_class,
                       bank_version,
                       status,
                       total_questions
                FROM wrong_question_review_sessions
                WHERE id = ?
                  AND user_id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new Session(
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getString("status"),
                        rs.getInt("total_questions")
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

        // Gọi nộp lần nữa chỉ đọc kết quả đã lưu.
        if ("COMPLETED".equals(session.status())) {
            return wrongReviewSessionService.get(
                    sessionId,
                    userId
            );
        }

        if (!"IN_PROGRESS".equals(session.status())) {
            throw BusinessException.conflict(
                    "Review session cannot be finished"
            );
        }

        AnswerCounts counts = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) AS answered,
                       COUNT(*) FILTER (
                           WHERE is_correct
                       ) AS correct
                FROM wrong_question_review_answers
                WHERE session_id = ?
                """,
                (rs, rowNum) -> new AnswerCounts(
                        rs.getInt("answered"),
                        rs.getInt("correct")
                ),
                sessionId
        );

        int answered = counts.answered();
        int correct = counts.correct();
        int wrong = answered - correct;
        int unanswered = session.totalQuestions() - answered;

        if (unanswered < 0) {
            throw BusinessException.conflict(
                    "Review answer count is invalid"
            );
        }

        // Đáp án đã cập nhật needsReview lúc submit.
        // Nộp phiên không đánh dấu các câu chưa trả lời là đúng.
        int remaining = Math.toIntExact(
                wrongQuestionProgressService.countNeedsReview(
                        userId,
                        session.licenseClass(),
                        session.bankVersion()
                )
        );

        int changed = jdbcTemplate.update("""
                UPDATE wrong_question_review_sessions
                SET status = 'COMPLETED',
                    answered_questions = ?,
                    correct_count = ?,
                    wrong_count = ?,
                    unanswered_count = ?,
                    remaining_count = ?,
                    completed_at = ?
                WHERE id = ?
                  AND user_id = ?
                  AND status = 'IN_PROGRESS'
                """,
                answered,
                correct,
                wrong,
                unanswered,
                remaining,
                Timestamp.from(Instant.now()),
                sessionId,
                userId
        );

        if (changed != 1) {
            throw BusinessException.conflict(
                    "Review session could not be finished"
            );
        }

        return wrongReviewSessionService.get(
                sessionId,
                userId
        );
    }

    private record Session(
            String licenseClass,
            String bankVersion,
            String status,
            int totalQuestions
    ) {}

    private record AnswerCounts(
            int answered,
            int correct
    ) {}
}