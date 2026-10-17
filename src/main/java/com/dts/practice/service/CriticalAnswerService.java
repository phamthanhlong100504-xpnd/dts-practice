package com.dts.practice.service;

import com.dts.practice.dto.request.SubmitCriticalAnswerRequest;
import com.dts.practice.dto.response.CriticalAnswerResponse;
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
public class CriticalAnswerService {

    private static final String CURRENT_BANK =
            CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public CriticalAnswerResponse submitAnswer(
            UUID userId,
            SubmitCriticalAnswerRequest request
    ) {
        String license = normalizeLicense(request.licenseClass());
        String answer = request.selectedAnswer()
                .trim()
                .toUpperCase(Locale.ROOT);

        // Khóa phiên để answer và việc kết thúc phiên không ghi đè nhau.
        List<Session> sessions = jdbcTemplate.query("""
                SELECT license_class, bank_version, status
                FROM critical_question_sessions
                WHERE id = ? AND user_id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new Session(
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getString("status")
                ),
                request.sessionId(),
                userId
        );

        if (sessions.size() != 1) {
            throw BusinessException.notFound(
                    "Critical-question session not found"
            );
        }

        Session session = sessions.get(0);

        if (!license.equals(session.licenseClass())) {
            throw BusinessException.badRequest(
                    "License class does not match the session"
            );
        }

        if (!CURRENT_BANK.equals(session.bankVersion())) {
            throw BusinessException.conflict(
                    "Question bank version changed"
            );
        }

        if ("SUPERSEDED".equals(session.status())) {
            throw BusinessException.conflict(
                    "Critical-question session was superseded"
            );
        }

        List<QuestionCheck> questions = jdbcTemplate.query("""
                SELECT q.correct_answer,
                       q.explanation,
                       EXISTS (
                           SELECT 1
                           FROM jsonb_array_elements(q.options) option
                           WHERE upper(btrim(option->>'label')) = ?
                       ) AS valid_option
                FROM critical_question_session_questions sq
                JOIN questions q ON q.id = sq.question_id
                WHERE sq.session_id = ?
                  AND sq.question_id = ?
                  AND q.bank_version = ?
                  AND q.is_critical = TRUE
                  AND jsonb_exists(q.critical_licenses, ?)
                  AND jsonb_exists(q.applicable_licenses, ?)
                """,
                (rs, rowNum) -> new QuestionCheck(
                        rs.getString("correct_answer"),
                        rs.getString("explanation"),
                        rs.getBoolean("valid_option")
                ),
                answer,
                request.sessionId(),
                request.questionId(),
                CURRENT_BANK,
                license,
                license
        );

        if (questions.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not a critical question in this session"
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

        List<SavedAnswer> previous = jdbcTemplate.query("""
                SELECT selected_answer, is_correct, answered_at
                FROM critical_question_session_answers
                WHERE session_id = ? AND question_id = ?
                """,
                (rs, rowNum) -> new SavedAnswer(
                        rs.getString("selected_answer"),
                        rs.getBoolean("is_correct"),
                        rs.getTimestamp("answered_at").toInstant()
                ),
                request.sessionId(),
                request.questionId()
        );

        // Gửi lại cùng đáp án giữ nguyên thời điểm đã lưu, kể cả sau
        // khi phiên COMPLETED. SUPERSEDED đã bị chặn ở phía trên.
        if (!previous.isEmpty()
                && previous.get(0).selectedAnswer().equals(answer)) {
            SavedAnswer saved = previous.get(0);

            return new CriticalAnswerResponse(
                    request.questionId(),
                    answer,
                    saved.isCorrect(),
                    question.correctAnswer(),
                    question.explanation(),
                    saved.answeredAt()
            );
        }

        if (!"IN_PROGRESS".equals(session.status())) {
            throw BusinessException.conflict(
                    "Critical-question session is completed"
            );
        }

        boolean correct =
                answer.equalsIgnoreCase(question.correctAnswer());

        jdbcTemplate.update("""
                INSERT INTO critical_question_session_answers
                    (session_id, question_id, selected_answer,
                     is_correct, answered_at)
                VALUES (?, ?, ?, ?, clock_timestamp())
                ON CONFLICT (session_id, question_id)
                DO UPDATE
                SET selected_answer = EXCLUDED.selected_answer,
                    is_correct = EXCLUDED.is_correct,
                    answered_at = EXCLUDED.answered_at
                """,
                request.sessionId(),
                request.questionId(),
                answer,
                correct
        );

        // Đọc đúng timestamp PostgreSQL đã lưu ở độ chính xác microsecond.
        Timestamp savedAt = jdbcTemplate.queryForObject("""
                SELECT answered_at
                FROM critical_question_session_answers
                WHERE session_id = ? AND question_id = ?
                """,
                Timestamp.class,
                request.sessionId(),
                request.questionId()
        );

        // Đáp án vòng ôn và tiến độ lâu dài cùng commit một transaction.
        jdbcTemplate.update("""
                INSERT INTO critical_question_progress
                    (id, user_id, license_class, bank_version,
                     question_id, learned, selected_answer, is_correct,
                     learned_at, last_answered_at, last_studied_at,
                     created_at, updated_at)
                VALUES (
                    gen_random_uuid(), ?, ?, ?, ?, TRUE, ?, ?, ?, ?, ?,
                    NOW(), NOW()
                )
                ON CONFLICT ON CONSTRAINT uq_critical_question_progress_scope
                DO UPDATE
                SET learned = TRUE,
                    selected_answer = EXCLUDED.selected_answer,
                    is_correct = EXCLUDED.is_correct,
                    learned_at = COALESCE(
                        critical_question_progress.learned_at,
                        EXCLUDED.learned_at
                    ),
                    last_answered_at = EXCLUDED.last_answered_at,
                    last_studied_at = EXCLUDED.last_studied_at,
                    updated_at = NOW()
                """,
                userId,
                license,
                CURRENT_BANK,
                request.questionId(),
                answer,
                correct,
                savedAt,
                savedAt,
                savedAt
        );

        Long unanswered = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM critical_question_session_questions sq
                WHERE sq.session_id = ?
                  AND sq.initial_learned = FALSE
                  AND NOT EXISTS (
                      SELECT 1
                      FROM critical_question_session_answers a
                      WHERE a.session_id = sq.session_id
                        AND a.question_id = sq.question_id
                  )
                """,
                Long.class,
                request.sessionId()
        );

        if (unanswered != null && unanswered == 0) {
            jdbcTemplate.update("""
                    UPDATE critical_question_sessions
                    SET status = 'COMPLETED',
                        completed_at = ?,
                        updated_at = NOW()
                    WHERE id = ?
                      AND user_id = ?
                      AND status = 'IN_PROGRESS'
                    """,
                    savedAt,
                    request.sessionId(),
                    userId
            );
        }

        return new CriticalAnswerResponse(
                request.questionId(),
                answer,
                correct,
                question.correctAnswer(),
                question.explanation(),
                savedAt.toInstant()
        );
    }

    private static String normalizeLicense(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String license =
                value.trim().toUpperCase(Locale.ROOT);

        ExamMatrix.forClass(license);
        return license;
    }

    private record Session(
            String licenseClass,
            String bankVersion,
            String status
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