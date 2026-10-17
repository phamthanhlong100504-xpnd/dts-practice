package com.dts.practice.service;

import com.dts.practice.dto.response.WrongReviewSessionResponse;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WrongReviewSessionService {

    private static final String CURRENT_BANK =
            CurrentQuestionBank.currentVersion();

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final WrongQuestionProgressService wrongQuestionProgressService;

    @Transactional
    public WrongReviewSessionResponse start(
            UUID userId,
            String licenseClass
    ) {
        String license = normalizeLicense(licenseClass);

        // Serialize concurrent starts for the same user, license and bank.
        // The partial unique index is the second layer of protection.
        String lockKey = userId + ":" + license + ":" + CURRENT_BANK;
        jdbcTemplate.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?::text, 0))",
                (RowCallbackHandler) rs -> {},
                lockKey
        );

        List<UUID> active = jdbcTemplate.query("""
                SELECT id
                FROM wrong_question_review_sessions
                WHERE user_id = ?
                  AND license_class = ?
                  AND bank_version = ?
                  AND status = 'IN_PROGRESS'
                FOR UPDATE
                """,
                (rs, rowNum) -> rs.getObject("id", UUID.class),
                userId,
                license,
                CURRENT_BANK
        );

        if (!active.isEmpty()) {
            return get(active.get(0), userId);
        }

        List<WrongQuestionProgressService.ReviewState> snapshot =
                wrongQuestionProgressService.findNeedsReview(
                        userId,
                        license,
                        CURRENT_BANK
                );

        if (snapshot.isEmpty()) {
            throw BusinessException.of(
                    "NO_WRONG_QUESTIONS",
                    "No questions currently need review",
                    HttpStatus.CONFLICT
            );
        }

        UUID sessionId = jdbcTemplate.queryForObject("""
                INSERT INTO wrong_question_review_sessions
                    (user_id, license_class, bank_version, total_questions,
                     current_question_id, unanswered_count)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                (rs, rowNum) -> rs.getObject(1, UUID.class),
                userId,
                license,
                CURRENT_BANK,
                snapshot.size(),
                snapshot.get(0).questionId(),
                snapshot.size()
        );

        Map<Integer, Integer> orderByQuestionId = new HashMap<>();
        for (int index = 0; index < snapshot.size(); index++) {
            orderByQuestionId.put(
                    snapshot.get(index).questionId(),
                    index + 1
            );
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO wrong_question_review_session_questions
                    (session_id, question_id, question_order,
                     previous_wrong_answer, last_wrong_at)
                VALUES (?, ?, ?, ?, ?)
                """,
                snapshot,
                snapshot.size(),
                (statement, state) -> {
                    statement.setObject(1, sessionId);
                    statement.setInt(2, state.questionId());
                    statement.setInt(
                            3,
                            orderByQuestionId.get(state.questionId())
                    );
                    statement.setString(
                            4,
                            state.previousWrongAnswer()
                    );
                    statement.setTimestamp(
                            5,
                            state.lastWrongAt() == null
                                    ? null
                                    : Timestamp.from(state.lastWrongAt())
                    );
                }
        );

        return get(sessionId, userId);
    }

    @Transactional(readOnly = true)
    public WrongReviewSessionResponse get(
            UUID sessionId,
            UUID userId
    ) {
        List<SessionRow> sessions = jdbcTemplate.query("""
                SELECT id, license_class, bank_version, status,
                       total_questions, current_question_id,
                       answered_questions, correct_count, wrong_count,
                       unanswered_count, remaining_count,
                       started_at, completed_at
                FROM wrong_question_review_sessions
                WHERE id = ? AND user_id = ?
                """,
                (rs, rowNum) -> new SessionRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("license_class"),
                        rs.getString("bank_version"),
                        rs.getString("status"),
                        rs.getInt("total_questions"),
                        rs.getInt("current_question_id"),
                        rs.getInt("answered_questions"),
                        rs.getInt("correct_count"),
                        rs.getInt("wrong_count"),
                        rs.getInt("unanswered_count"),
                        rs.getObject("remaining_count") == null
                                ? null
                                : rs.getLong("remaining_count"),
                        rs.getTimestamp("started_at").toInstant(),
                        rs.getTimestamp("completed_at") == null
                                ? null
                                : rs.getTimestamp("completed_at").toInstant()
                ),
                sessionId,
                userId
        );

        if (sessions.size() != 1) {
            throw BusinessException.notFound(
                    "Wrong-question review session not found"
            );
        }

        SessionRow session = sessions.get(0);
        if (!CURRENT_BANK.equals(session.bankVersion())) {
            throw BusinessException.conflict(
                    "Question bank version changed; start a new review"
            );
        }

        List<WrongReviewSessionResponse.Question> questions =
                jdbcTemplate.query("""
                        SELECT sq.question_id,
                               sq.question_order,
                               sq.previous_wrong_answer,
                               sq.last_wrong_at,
                               q.chapter,
                               q.question_text,
                               q.options::text AS options,
                               q.correct_answer,
                               q.explanation,
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
                        FROM wrong_question_review_session_questions sq
                        JOIN questions q
                          ON q.id = sq.question_id
                        WHERE sq.session_id = ?
                          AND q.bank_version = ?
                          AND jsonb_exists(
                              q.applicable_licenses,
                              ?
                          )
                        ORDER BY sq.question_order
                        """,
                        (rs, rowNum) ->
                                new WrongReviewSessionResponse.Question(
                                        rs.getInt("question_id"),
                                        rs.getInt("question_order"),
                                        rs.getInt("chapter"),
                                        chapterTitle(
                                                rs.getInt("chapter")
                                        ),
                                        rs.getString("question_text"),
                                        parseOptions(
                                                rs.getString("options")
                                        ),
                                        rs.getBoolean(
                                                "critical_for_license"
                                        ),
                                        rs.getString("correct_answer"),
                                        rs.getString("explanation"),
                                        rs.getString("image_url"),
                                        rs.getObject(
                                                "media_file_id",
                                                UUID.class
                                        ),
                                        rs.getString(
                                                "previous_wrong_answer"
                                        ),
                                        rs.getTimestamp(
                                                "last_wrong_at"
                                        ) == null
                                                ? null
                                                : rs.getTimestamp(
                                                        "last_wrong_at"
                                                ).toInstant(),
                                        rs.getBoolean("bookmarked")
                                ),
                        session.licenseClass(),
                        userId,
                        session.licenseClass(),
                        session.bankVersion(),
                        sessionId,
                        session.bankVersion(),
                        session.licenseClass()
                );

        if (questions.size() != session.totalQuestions()) {
            throw BusinessException.conflict(
                    "Review snapshot is incomplete"
            );
        }

        Map<Integer, WrongReviewSessionResponse.Answer> answers =
                new LinkedHashMap<>();

        jdbcTemplate.query("""
                SELECT a.question_id,
                       a.selected_answer,
                       a.is_correct,
                       a.answered_at
                FROM wrong_question_review_answers a
                JOIN wrong_question_review_session_questions sq
                  ON sq.session_id = a.session_id
                 AND sq.question_id = a.question_id
                WHERE a.session_id = ?
                ORDER BY sq.question_order
                """,
                (RowCallbackHandler) rs -> answers.put(
                        rs.getInt("question_id"),
                        new WrongReviewSessionResponse.Answer(
                                rs.getString("selected_answer"),
                                rs.getBoolean("is_correct"),
                                rs.getTimestamp(
                                        "answered_at"
                                ).toInstant()
                        )
                ),
                sessionId
        );

        Integer currentPosition = questions.stream()
                .filter(question ->
                        question.questionId().equals(
                                session.currentQuestionId()
                        )
                )
                .map(
                        WrongReviewSessionResponse.Question::position
                )
                .findFirst()
                .orElseThrow(() -> BusinessException.conflict(
                        "Review cursor is invalid"
                ));

        boolean completed = "COMPLETED".equals(session.status());

        int answered = completed
                ? session.answeredQuestions()
                : answers.size();

        int correct = completed
                ? session.correctCount()
                : (int) answers.values().stream()
                        .filter(answer ->
                                Boolean.TRUE.equals(
                                        answer.isCorrect()
                                )
                        )
                        .count();

        int wrong = completed
                ? session.wrongCount()
                : answered - correct;

        int unanswered = completed
                ? session.unansweredCount()
                : session.totalQuestions() - answered;

        long remaining = completed
                ? session.remainingCount()
                : wrongQuestionProgressService.countNeedsReview(
                        userId,
                        session.licenseClass(),
                        session.bankVersion()
                );

        return new WrongReviewSessionResponse(
                session.id(),
                session.licenseClass(),
                session.bankVersion(),
                session.status(),
                session.totalQuestions(),
                currentPosition,
                session.currentQuestionId(),
                List.copyOf(questions),
                answers,
                answered,
                correct,
                wrong,
                unanswered,
                remaining,
                session.startedAt(),
                session.completedAt()
        );
    }

    private Object parseOptions(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException error) {
            throw BusinessException.conflict(
                    "Review question options are invalid"
            );
        }
    }

    private static String normalizeLicense(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String license = value.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(license);
        return license;
    }

    private static String chapterTitle(int chapter) {
        return switch (chapter) {
            case 1 ->
                    "Quy định chung và quy tắc giao thông đường bộ";
            case 2 ->
                    "Văn hóa giao thông, đạo đức người lái xe, "
                    + "kỹ năng PCCC và cứu hộ, cứu nạn";
            case 3 ->
                    "Kỹ thuật lái xe";
            case 4 ->
                    "Cấu tạo và sửa chữa";
            case 5 ->
                    "Báo hiệu đường bộ";
            case 6 ->
                    "Giải thế sa hình và kỹ năng xử lý "
                    + "tình huống giao thông";
            default -> throw BusinessException.conflict(
                    "Invalid question chapter"
            );
        };
    }

    private record SessionRow(
            UUID id,
            String licenseClass,
            String bankVersion,
            String status,
            int totalQuestions,
            int currentQuestionId,
            int answeredQuestions,
            int correctCount,
            int wrongCount,
            int unansweredCount,
            Long remainingCount,
            Instant startedAt,
            Instant completedAt
    ) {}
}