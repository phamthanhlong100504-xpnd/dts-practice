package com.dts.practice.service;

import com.dts.practice.dto.response.ChapterReviewSessionResponse;
import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChapterReviewSessionService {

    private static final String CURRENT_BANK =
            CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChapterReviewSessionResponse start(
            UUID userId,
            int chapterId,
            String licenseClass
    ) {
        String license = normalizeLicense(licenseClass);
        validateChapter(chapterId);

        // Hai request start đồng thời phải trỏ về cùng một phiên đang dở.
        String lockKey = "chapter-review:" + userId + ":" + license
                + ":" + CURRENT_BANK + ":" + chapterId;

        jdbcTemplate.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?::text, 0))",
                (RowCallbackHandler) rs -> {},
                lockKey
        );

        List<UUID> active = jdbcTemplate.query("""
                SELECT id
                FROM chapter_review_sessions
                WHERE user_id = ?
                  AND license_class = ?
                  AND bank_version = ?
                  AND chapter_id = ?
                  AND status = 'IN_PROGRESS'
                FOR UPDATE
                """,
                (rs, rowNum) -> rs.getObject("id", UUID.class),
                userId, license, CURRENT_BANK, chapterId
        );

        if (!active.isEmpty()) {
            return get(active.get(0), userId, chapterId);
        }

        List<SnapshotQuestion> snapshot = jdbcTemplate.query("""
                SELECT q.id
                FROM questions q
                WHERE q.bank_version = ?
                  AND q.chapter = ?
                  AND jsonb_exists(q.applicable_licenses, ?)
                ORDER BY q.source_question_id, q.id
                """,
                (rs, rowNum) -> new SnapshotQuestion(
                        rs.getInt("id"),
                        rowNum + 1
                ),
                CURRENT_BANK, chapterId, license
        );

        if (snapshot.isEmpty()) {
            throw BusinessException.badRequest(
                    "Chapter has no questions for this license class"
            );
        }

        Long learned = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM chapter_question_progress p
                JOIN questions q ON q.id = p.question_id
                WHERE p.user_id = ?
                  AND p.license_class = ?
                  AND p.bank_version = ?
                  AND p.chapter_id = ?
                  AND q.bank_version = p.bank_version
                  AND q.chapter = p.chapter_id
                  AND jsonb_exists(
                      q.applicable_licenses,
                      p.license_class
                  )
                """,
                Long.class,
                userId, license, CURRENT_BANK, chapterId
        );

        if (learned == null || learned != snapshot.size()) {
            throw BusinessException.of(
                    "CHAPTER_LEARNING_INCOMPLETE",
                    "Complete the chapter before starting review",
                    HttpStatus.CONFLICT
            );
        }

        UUID sessionId = jdbcTemplate.queryForObject("""
                INSERT INTO chapter_review_sessions
                    (
                        user_id,
                        license_class,
                        bank_version,
                        chapter_id,
                        current_question_id,
                        total_questions
                    )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                (rs, rowNum) -> rs.getObject(1, UUID.class),
                userId,
                license,
                CURRENT_BANK,
                chapterId,
                snapshot.get(0).questionId(),
                snapshot.size()
        );

        jdbcTemplate.batchUpdate("""
                INSERT INTO chapter_review_session_questions
                    (session_id, question_id, question_order)
                VALUES (?, ?, ?)
                """,
                snapshot,
                snapshot.size(),
                (statement, question) -> {
                    statement.setObject(1, sessionId);
                    statement.setInt(2, question.questionId());
                    statement.setInt(3, question.position());
                }
        );

        return get(sessionId, userId, chapterId);
    }

    @Transactional(readOnly = true)
    public ChapterReviewSessionResponse get(
            UUID sessionId,
            UUID userId,
            int chapterId
    ) {
        List<Session> rows = jdbcTemplate.query("""
                SELECT id,
                       license_class,
                       bank_version,
                       chapter_id,
                       status,
                       current_question_id,
                       total_questions,
                       started_at,
                       completed_at
                FROM chapter_review_sessions
                WHERE id = ?
                  AND user_id = ?
                  AND chapter_id = ?
                """,
                (rs, rowNum) -> new Session(
                        rs.getObject("id", UUID.class),
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getInt("chapter_id"),
                        rs.getString("status"),
                        rs.getInt("current_question_id"),
                        rs.getInt("total_questions"),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("completed_at") == null
                                ? null
                                : rs.getTimestamp("completed_at")
                                        .toInstant()
                ),
                sessionId, userId, chapterId
        );

        if (rows.size() != 1) {
            throw BusinessException.notFound(
                    "Chapter review session not found"
            );
        }

        Session session = rows.get(0);

        List<ChapterReviewSessionResponse.Question> questions =
                jdbcTemplate.query("""
                        SELECT sq.question_id,
                               sq.question_order,
                               q.question_text,
                               q.options::text AS options,
                               q.image_url,
                               q.media_file_id,
                               (
                                   q.is_critical
                                   AND jsonb_exists(
                                       COALESCE(
                                           q.critical_licenses,
                                           '[]'::jsonb
                                       ),
                                       ?
                                   )
                               ) AS critical_for_license,
                               EXISTS (
                                   SELECT 1
                                   FROM chapter_question_bookmarks b
                                   WHERE b.user_id = ?
                                     AND b.license_class = ?
                                     AND b.bank_version = ?
                                     AND b.question_id = q.id
                               ) AS bookmarked
                        FROM chapter_review_session_questions sq
                        JOIN questions q
                          ON q.id = sq.question_id
                        WHERE sq.session_id = ?
                          AND q.bank_version = ?
                          AND q.chapter = ?
                          AND jsonb_exists(
                              q.applicable_licenses,
                              ?
                          )
                        ORDER BY sq.question_order
                        """,
                        (rs, rowNum) ->
                                new ChapterReviewSessionResponse.Question(
                                        rs.getInt("question_id"),
                                        rs.getInt("question_order"),
                                        rs.getString("question_text"),
                                        parseOptions(
                                                rs.getString("options")
                                        ),
                                        rs.getBoolean(
                                                "critical_for_license"
                                        ),
                                        rs.getString("image_url"),
                                        rs.getObject(
                                                "media_file_id",
                                                UUID.class
                                        ),
                                        rs.getBoolean("bookmarked")
                                ),
                        session.licenseClass(),
                        userId,
                        session.licenseClass(),
                        session.bankVersion(),
                        sessionId,
                        session.bankVersion(),
                        chapterId,
                        session.licenseClass()
                );

        if (questions.size() != session.totalQuestions()) {
            throw BusinessException.conflict(
                    "Chapter review snapshot is incomplete"
            );
        }

        Map<Integer, ChapterReviewSessionResponse.Answer> answers =
                new LinkedHashMap<>();

        jdbcTemplate.query("""
                SELECT a.question_id,
                       a.selected_answer,
                       a.is_correct,
                       a.answered_at,
                       q.correct_answer,
                       q.explanation
                FROM chapter_review_session_answers a
                JOIN chapter_review_session_questions sq
                  ON sq.session_id = a.session_id
                 AND sq.question_id = a.question_id
                JOIN questions q
                  ON q.id = a.question_id
                WHERE a.session_id = ?
                ORDER BY sq.question_order
                """,
                (RowCallbackHandler) rs -> answers.put(
                        rs.getInt("question_id"),
                        new ChapterReviewSessionResponse.Answer(
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

        int currentPosition = questions.stream()
                .filter(question ->
                        question.questionId().equals(
                                session.currentQuestionId()
                        )
                )
                .mapToInt(
                        ChapterReviewSessionResponse.Question::position
                )
                .findFirst()
                .orElseThrow(() -> BusinessException.conflict(
                        "Chapter review cursor is invalid"
                ));

        return new ChapterReviewSessionResponse(
                session.id(),
                session.licenseClass(),
                session.bankVersion(),
                session.chapterId(),
                session.status(),
                session.totalQuestions(),
                answers.size(),
                currentPosition,
                session.currentQuestionId(),
                List.copyOf(questions),
                Collections.unmodifiableMap(answers),
                session.startedAt(),
                session.completedAt()
        );
    }

    @Transactional
    public ChapterReviewSessionResponse answer(
            UUID sessionId,
            UUID userId,
            int chapterId,
            Integer questionId,
            String selectedAnswer
    ) {
        Session session = lockSession(
                sessionId, userId, chapterId
        );

        if (questionId == null
                || selectedAnswer == null
                || selectedAnswer.isBlank()) {
            throw BusinessException.badRequest(
                    "Question and answer are required"
            );
        }

        String answer = selectedAnswer
                .trim()
                .toUpperCase(Locale.ROOT);

        List<QuestionCheck> checks = jdbcTemplate.query("""
                SELECT q.correct_answer,
                       q.explanation,
                       EXISTS (
                           SELECT 1
                           FROM jsonb_array_elements(q.options) opt
                           WHERE upper(
                               btrim(opt->>'label')
                           ) = ?
                       ) AS valid_option
                FROM chapter_review_session_questions sq
                JOIN questions q
                  ON q.id = sq.question_id
                WHERE sq.session_id = ?
                  AND sq.question_id = ?
                  AND q.bank_version = ?
                  AND q.chapter = ?
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
                chapterId,
                session.licenseClass()
        );

        if (checks.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not in this review session"
            );
        }

        if (!checks.get(0).validOption()) {
            throw BusinessException.of(
                    "INVALID_ANSWER_OPTION",
                    "Answer option does not belong to this question",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<String> previous = jdbcTemplate.query("""
                SELECT selected_answer
                FROM chapter_review_session_answers
                WHERE session_id = ?
                  AND question_id = ?
                """,
                (rs, rowNum) -> rs.getString(1),
                sessionId,
                questionId
        );

        // Retry cùng đáp án giữ nguyên bản ghi, kể cả khi phiên đã xong.
        if (!previous.isEmpty()
                && previous.get(0).equals(answer)) {
            return get(sessionId, userId, chapterId);
        }

        requireInProgress(session);

        jdbcTemplate.update("""
                INSERT INTO chapter_review_session_answers
                    (
                        session_id,
                        question_id,
                        selected_answer,
                        is_correct,
                        answered_at
                    )
                VALUES (?, ?, ?, ?, clock_timestamp())
                ON CONFLICT (session_id, question_id)
                DO UPDATE SET
                    selected_answer = EXCLUDED.selected_answer,
                    is_correct = EXCLUDED.is_correct,
                    answered_at = EXCLUDED.answered_at
                """,
                sessionId,
                questionId,
                answer,
                answer.equalsIgnoreCase(
                        checks.get(0).correctAnswer()
                )
        );

        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM chapter_review_session_answers
                WHERE session_id = ?
                """,
                Long.class,
                sessionId
        );

        if (count != null
                && count == session.totalQuestions()) {
            jdbcTemplate.update("""
                    UPDATE chapter_review_sessions
                    SET status = 'COMPLETED',
                        completed_at = clock_timestamp()
                    WHERE id = ?
                      AND user_id = ?
                      AND status = 'IN_PROGRESS'
                    """,
                    sessionId,
                    userId
            );
        }

        return get(sessionId, userId, chapterId);
    }

    @Transactional
    public ChapterReviewSessionResponse saveCursor(
            UUID sessionId,
            UUID userId,
            int chapterId,
            Integer questionId
    ) {
        Session session = lockSession(
                sessionId, userId, chapterId
        );
        requireInProgress(session);

        List<Integer> matches = jdbcTemplate.query("""
                SELECT sq.question_id
                FROM chapter_review_session_questions sq
                JOIN questions q
                  ON q.id = sq.question_id
                WHERE sq.session_id = ?
                  AND sq.question_id = ?
                  AND q.bank_version = ?
                  AND q.chapter = ?
                  AND jsonb_exists(
                      q.applicable_licenses,
                      ?
                  )
                """,
                (rs, rowNum) -> rs.getInt(1),
                sessionId,
                questionId,
                session.bankVersion(),
                chapterId,
                session.licenseClass()
        );

        if (matches.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not in this review session"
            );
        }

        if (!questionId.equals(
                session.currentQuestionId()
        )) {
            jdbcTemplate.update("""
                    UPDATE chapter_review_sessions
                    SET current_question_id = ?
                    WHERE id = ?
                      AND user_id = ?
                    """,
                    questionId,
                    sessionId,
                    userId
            );
        }

        return get(sessionId, userId, chapterId);
    }

    @Transactional
    public ChapterReviewSessionResponse finish(
            UUID sessionId,
            UUID userId,
            int chapterId
    ) {
        Session session = lockSession(
                sessionId, userId, chapterId
        );

        if ("COMPLETED".equals(session.status())) {
            return get(sessionId, userId, chapterId);
        }

        Long count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM chapter_review_session_answers
                WHERE session_id = ?
                """,
                Long.class,
                sessionId
        );

        if (count == null
                || count != session.totalQuestions()) {
            throw BusinessException.of(
                    "CHAPTER_REVIEW_INCOMPLETE",
                    "Answer all chapter questions before finishing",
                    HttpStatus.CONFLICT
            );
        }

        jdbcTemplate.update("""
                UPDATE chapter_review_sessions
                SET status = 'COMPLETED',
                    completed_at = clock_timestamp()
                WHERE id = ?
                  AND user_id = ?
                  AND status = 'IN_PROGRESS'
                """,
                sessionId,
                userId
        );

        return get(sessionId, userId, chapterId);
    }

    private Session lockSession(
            UUID sessionId,
            UUID userId,
            int chapterId
    ) {
        List<Session> rows = jdbcTemplate.query("""
                SELECT id,
                       license_class,
                       bank_version,
                       chapter_id,
                       status,
                       current_question_id,
                       total_questions,
                       started_at,
                       completed_at
                FROM chapter_review_sessions
                WHERE id = ?
                  AND user_id = ?
                  AND chapter_id = ?
                FOR UPDATE
                """,
                (rs, rowNum) -> new Session(
                        rs.getObject("id", UUID.class),
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getInt("chapter_id"),
                        rs.getString("status"),
                        rs.getInt("current_question_id"),
                        rs.getInt("total_questions"),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("completed_at") == null
                                ? null
                                : rs.getTimestamp("completed_at")
                                        .toInstant()
                ),
                sessionId,
                userId,
                chapterId
        );

        if (rows.size() != 1) {
            throw BusinessException.notFound(
                    "Chapter review session not found"
            );
        }

        Session session = rows.get(0);

        if (!CURRENT_BANK.equals(
                session.bankVersion()
        )) {
            throw BusinessException.conflict(
                    "Question bank version changed"
            );
        }

        return session;
    }

    private Object parseOptions(String json) {
        try {
            return objectMapper.readValue(
                    json,
                    Object.class
            );
        } catch (JsonProcessingException error) {
            throw BusinessException.conflict(
                    "Question options are invalid"
            );
        }
    }

    private static void requireInProgress(
            Session session
    ) {
        if (!"IN_PROGRESS".equals(
                session.status()
        )) {
            throw BusinessException.conflict(
                    "Chapter review is completed"
            );
        }
    }

    private static void validateChapter(
            int chapterId
    ) {
        if (chapterId < 1 || chapterId > 6) {
            throw BusinessException.badRequest(
                    "Invalid chapterId: " + chapterId
            );
        }
    }

    private static String normalizeLicense(
            String value
    ) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String license = value
                .trim()
                .toUpperCase(Locale.ROOT);

        ExamMatrix.forClass(license);

        return license;
    }

    private record Session(
            UUID id,
            String licenseClass,
            String bankVersion,
            int chapterId,
            String status,
            int currentQuestionId,
            int totalQuestions,
            Instant startedAt,
            Instant completedAt
    ) {}

    private record QuestionCheck(
            String correctAnswer,
            String explanation,
            boolean validOption
    ) {}

    private record SnapshotQuestion(
            int questionId,
            int position
    ) {}
}