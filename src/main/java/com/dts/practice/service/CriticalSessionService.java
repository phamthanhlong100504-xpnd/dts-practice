package com.dts.practice.service;

import com.dts.practice.dto.response.CriticalSessionResponse;
import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CriticalSessionService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public CriticalSessionResponse start(
            UUID userId, String licenseClass, String requestedMode
    ) {
        return start(userId, licenseClass, requestedMode, false);
    }

    @Transactional
    public CriticalSessionResponse start(
            UUID userId, String licenseClass, String requestedMode,
            boolean restart
    ) {
        String license = normalizeLicense(licenseClass);
        String mode = normalizeMode(requestedMode);

        if (restart && !"REVIEW".equals(mode)) {
            throw BusinessException.badRequest(
                    "restart is only supported for REVIEW"
            );
        }

        String lockKey =
                "critical:" + userId + ":" + license + ":" + CURRENT_BANK;

        jdbcTemplate.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?::text, 0))",
                (RowCallbackHandler) rs -> {},
                lockKey
        );

        List<ActiveSession> active = jdbcTemplate.query("""
                SELECT id, mode
                FROM critical_question_sessions
                WHERE user_id = ?
                  AND license_class = ?
                  AND bank_version = ?
                  AND status = 'IN_PROGRESS'
                FOR UPDATE
                """,
                (rs, rowNum) -> new ActiveSession(
                        rs.getObject("id", UUID.class),
                        rs.getString("mode")
                ),
                userId, license, CURRENT_BANK
        );

        ActiveSession current = active.isEmpty() ? null : active.get(0);

        if (current != null) {
            if (!mode.equals(current.mode())) {
                throw BusinessException.of(
                        "ACTIVE_CRITICAL_SESSION_EXISTS",
                        "Another critical-question round is in progress",
                        HttpStatus.CONFLICT
                );
            }

            if (!restart) {
                return get(current.id(), userId);
            }
        }

        List<SnapshotQuestion> snapshot = jdbcTemplate.query("""
                SELECT q.id,
                       COALESCE(p.learned, FALSE) AS learned,
                       p.selected_answer,
                       p.is_correct,
                       p.last_answered_at
                FROM questions q
                LEFT JOIN critical_question_progress p
                  ON p.user_id = ?
                 AND p.license_class = ?
                 AND p.bank_version = ?
                 AND p.question_id = q.id
                WHERE q.bank_version = ?
                  AND q.is_critical = TRUE
                  AND jsonb_exists(q.critical_licenses, ?)
                ORDER BY q.source_question_id, q.id
                """,
                (rs, rowNum) -> new SnapshotQuestion(
                        rs.getInt("id"),
                        rowNum + 1,
                        rs.getBoolean("learned"),
                        "CONTINUE".equals(mode)
                                && rs.getBoolean("learned"),
                        rs.getString("selected_answer"),
                        rs.getObject("is_correct", Boolean.class),
                        rs.getTimestamp("last_answered_at") == null
                                ? null
                                : rs.getTimestamp("last_answered_at")
                                        .toInstant()
                ),
                userId, license, CURRENT_BANK, CURRENT_BANK, license
        );

        if (snapshot.isEmpty()) {
            throw BusinessException.conflict(
                    "No critical questions in the current bank"
            );
        }

        long alreadyLearned = snapshot.stream()
                .filter(SnapshotQuestion::canonicalLearned)
                .count();

        if ("REVIEW".equals(mode)
                && alreadyLearned != snapshot.size()) {
            throw BusinessException.of(
                    "CRITICAL_LEARNING_INCOMPLETE",
                    "Complete critical-question learning before review",
                    HttpStatus.CONFLICT
            );
        }

        if ("CONTINUE".equals(mode)
                && alreadyLearned == snapshot.size()) {
            throw BusinessException.of(
                    "CRITICAL_ALREADY_LEARNED",
                    "Critical-question learning is complete; start REVIEW",
                    HttpStatus.CONFLICT
            );
        }

        int cursor = snapshot.stream()
                .filter(question -> !question.initialLearned())
                .findFirst()
                .orElseThrow(() -> BusinessException.conflict(
                        "Review cursor is unavailable"
                ))
                .questionId();

        // Chỉ kết thúc vòng cũ sau khi đã validate bộ câu của vòng mới.
        // UPDATE và INSERT cùng transaction: INSERT lỗi thì UPDATE rollback.
        if (current != null) {
            int superseded = jdbcTemplate.update("""
                    UPDATE critical_question_sessions
                    SET status = 'SUPERSEDED',
                        completed_at = NOW(),
                        updated_at = NOW()
                    WHERE id = ?
                      AND user_id = ?
                      AND status = 'IN_PROGRESS'
                      AND mode = 'REVIEW'
                    """,
                    current.id(), userId
            );

            if (superseded != 1) {
                throw BusinessException.conflict(
                        "Review session changed while restarting"
                );
            }
        }

        UUID sessionId = jdbcTemplate.queryForObject("""
                INSERT INTO critical_question_sessions
                    (user_id, license_class, bank_version, mode,
                     current_question_id, total_questions)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                (rs, rowNum) -> rs.getObject(1, UUID.class),
                userId, license, CURRENT_BANK, mode,
                cursor, snapshot.size()
        );

        jdbcTemplate.batchUpdate("""
                INSERT INTO critical_question_session_questions
                    (session_id, question_id, question_order,
                     initial_learned)
                VALUES (?, ?, ?, ?)
                """,
                snapshot,
                snapshot.size(),
                (statement, question) -> {
                    statement.setObject(1, sessionId);
                    statement.setInt(2, question.questionId());
                    statement.setInt(3, question.position());
                    statement.setBoolean(4, question.initialLearned());
                }
        );

        // Bản ghi learned cũ có thể chưa lưu đáp án.
        // Chỉ khôi phục lựa chọn khi dữ liệu canonical thực sự có đáp án.
        List<SnapshotQuestion> knownAnswers = snapshot.stream()
                .filter(question -> question.initialLearned()
                        && question.selectedAnswer() != null
                        && question.isCorrect() != null
                        && question.lastAnsweredAt() != null)
                .toList();

        if (!knownAnswers.isEmpty()) {
            jdbcTemplate.batchUpdate("""
                    INSERT INTO critical_question_session_answers
                        (session_id, question_id, selected_answer,
                         is_correct, answered_at)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    knownAnswers,
                    knownAnswers.size(),
                    (statement, question) -> {
                        statement.setObject(1, sessionId);
                        statement.setInt(2, question.questionId());
                        statement.setString(
                                3, question.selectedAnswer()
                        );
                        statement.setBoolean(
                                4, question.isCorrect()
                        );
                        statement.setTimestamp(
                                5,
                                Timestamp.from(question.lastAnsweredAt())
                        );
                    }
            );
        }

        return get(sessionId, userId);
    }

    @Transactional(readOnly = true)
    public CriticalSessionResponse get(UUID sessionId, UUID userId) {
        List<SessionRow> sessions = jdbcTemplate.query("""
                SELECT id, license_class, bank_version, mode, status,
                       total_questions, current_question_id,
                       started_at, completed_at
                FROM critical_question_sessions
                WHERE id = ? AND user_id = ?
                """,
                (rs, rowNum) -> new SessionRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getString("mode"),
                        rs.getString("status"),
                        rs.getInt("total_questions"),
                        rs.getObject(
                                "current_question_id",
                                Integer.class
                        ),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("completed_at") == null
                                ? null
                                : rs.getTimestamp("completed_at")
                                        .toInstant()
                ),
                sessionId, userId
        );

        if (sessions.size() != 1) {
            throw BusinessException.notFound(
                    "Critical-question session not found"
            );
        }

        SessionRow session = sessions.get(0);

        List<CriticalSessionResponse.Question> questions =
                jdbcTemplate.query("""
                        SELECT sq.question_id,
                               sq.question_order,
                               sq.initial_learned,
                               q.chapter,
                               q.question_text,
                               q.options::text AS options,
                               q.image_url,
                               q.media_file_id,
                               EXISTS (
                                   SELECT 1
                                   FROM chapter_question_bookmarks b
                                   WHERE b.user_id = ?
                                     AND b.license_class = ?
                                     AND b.bank_version = ?
                                     AND b.question_id = sq.question_id
                               ) AS bookmarked
                        FROM critical_question_session_questions sq
                        JOIN questions q ON q.id = sq.question_id
                        WHERE sq.session_id = ?
                        ORDER BY sq.question_order
                        """,
                        (rs, rowNum) ->
                                new CriticalSessionResponse.Question(
                                        rs.getInt("question_id"),
                                        rs.getInt("question_order"),
                                        rs.getInt("chapter"),
                                        rs.getString("question_text"),
                                        parseOptions(
                                                rs.getString("options")
                                        ),
                                        true,
                                        rs.getString("image_url"),
                                        rs.getObject(
                                                "media_file_id",
                                                UUID.class
                                        ),
                                        rs.getBoolean("bookmarked"),
                                        rs.getBoolean("initial_learned")
                                ),
                        userId,
                        session.licenseClass(),
                        session.bankVersion(),
                        sessionId
                );

        if (questions.size() != session.totalQuestions()) {
            throw BusinessException.conflict(
                    "Critical-question snapshot is incomplete"
            );
        }

        Map<Integer, CriticalSessionResponse.Answer> answers =
                new LinkedHashMap<>();

        jdbcTemplate.query("""
                SELECT a.question_id,
                       a.selected_answer,
                       a.is_correct,
                       a.answered_at,
                       q.correct_answer,
                       q.explanation
                FROM critical_question_session_answers a
                JOIN critical_question_session_questions sq
                  ON sq.session_id = a.session_id
                 AND sq.question_id = a.question_id
                JOIN questions q ON q.id = a.question_id
                WHERE a.session_id = ?
                ORDER BY sq.question_order
                """,
                (RowCallbackHandler) rs -> answers.put(
                        rs.getInt("question_id"),
                        new CriticalSessionResponse.Answer(
                                rs.getInt("question_id"),
                                rs.getString("selected_answer"),
                                rs.getBoolean("is_correct"),
                                rs.getString("correct_answer"),
                                rs.getString("explanation"),
                                rs.getTimestamp("answered_at")
                                        .toInstant()
                        )
                ),
                sessionId
        );

        int answered = (int) questions.stream()
                .filter(question -> question.learnedBeforeSession()
                        || answers.containsKey(question.questionId()))
                .count();

        int currentPosition = questions.stream()
                .filter(question -> question.questionId()
                        .equals(session.currentQuestionId()))
                .map(CriticalSessionResponse.Question::position)
                .findFirst()
                .orElseThrow(() -> BusinessException.conflict(
                        "Critical-question cursor is invalid"
                ));

        return new CriticalSessionResponse(
                session.id(),
                session.licenseClass(),
                session.bankVersion(),
                session.mode(),
                session.status(),
                session.totalQuestions(),
                answered,
                currentPosition,
                session.currentQuestionId(),
                List.copyOf(questions),
                answers,
                session.startedAt(),
                session.completedAt()
        );
    }

    private Object parseOptions(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException error) {
            throw BusinessException.conflict(
                    "Critical-question options are invalid"
            );
        }
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

    private static String normalizeMode(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest(
                    "Mode is required"
            );
        }

        String mode =
                value.trim().toUpperCase(Locale.ROOT);

        if (!"CONTINUE".equals(mode)
                && !"REVIEW".equals(mode)) {
            throw BusinessException.badRequest(
                    "Mode must be CONTINUE or REVIEW"
            );
        }

        return mode;
    }

    private record ActiveSession(
            UUID id,
            String mode
    ) {}

    private record SnapshotQuestion(
            Integer questionId,
            int position,
            boolean canonicalLearned,
            boolean initialLearned,
            String selectedAnswer,
            Boolean isCorrect,
            Instant lastAnsweredAt
    ) {}

    private record SessionRow(
            UUID id,
            String licenseClass,
            String bankVersion,
            String mode,
            String status,
            int totalQuestions,
            Integer currentQuestionId,
            Instant startedAt,
            Instant completedAt
    ) {}
}