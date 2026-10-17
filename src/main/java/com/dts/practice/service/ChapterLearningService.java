package com.dts.practice.service;

import com.dts.practice.dto.request.RecordChapterAnswerRequest;
import com.dts.practice.dto.request.SaveChapterCursorRequest;
import com.dts.practice.dto.response.ChapterLearningAnswerResponse;
import com.dts.practice.dto.response.ChapterLearningCursorResponse;
import com.dts.practice.dto.response.ChapterLearningOverviewResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChapterLearningService {

    private static final int FIRST_CHAPTER = 1;
    private static final int LAST_CHAPTER = 6;

    private final QuestionService questionService;
    private final JdbcTemplate jdbcTemplate;
    private final WrongQuestionProgressService wrongQuestionProgressService;

    @Transactional(readOnly = true)
    public ChapterLearningOverviewResponse overview(
            UUID userId,
            String licenseClass
    ) {
        String license = normalizeLicense(licenseClass);
        Map<Integer, List<QuestionResponse>> questionsByChapter =
                new HashMap<>();
        String currentBank = null;

        // QuestionService filters by the current bank and license.
        for (int chapterId = FIRST_CHAPTER;
             chapterId <= LAST_CHAPTER;
             chapterId++) {

            List<QuestionResponse> questions =
                    questionService.getByChapter(chapterId, license);

            questionsByChapter.put(chapterId, questions);

            if (!questions.isEmpty()) {
                String version = bankVersionOf(questions.get(0).id());

                if (currentBank != null
                        && !currentBank.equals(version)) {
                    throw BusinessException.conflict(
                            "Question bank version changed during request"
                    );
                }

                currentBank = version;
            }
        }

        if (currentBank == null) {
            throw BusinessException.conflict(
                    "Current question bank has no questions for " + license
            );
        }

        Map<Integer, Integer> learnedByChapter = new HashMap<>();

        jdbcTemplate.query("""
                SELECT p.chapter_id, COUNT(*) AS learned
                FROM chapter_question_progress p
                JOIN questions q ON q.id = p.question_id
                WHERE p.user_id = ?
                  AND p.license_class = ?
                  AND p.bank_version = ?
                  AND q.bank_version = p.bank_version
                  AND q.chapter = p.chapter_id
                  AND jsonb_exists(
                      q.applicable_licenses,
                      p.license_class
                  )
                GROUP BY p.chapter_id
                """,
                (RowCallbackHandler) rs -> {
                    learnedByChapter.put(
                            rs.getInt("chapter_id"),
                            rs.getInt("learned")
                    );
                },
                userId,
                license,
                currentBank
        );

        Map<Integer, ChapterLearningCursorResponse> cursors =
                new HashMap<>();

        jdbcTemplate.query("""
                SELECT chapter_id, last_question_id, last_studied_at
                FROM chapter_learning_cursor
                WHERE user_id = ?
                  AND license_class = ?
                  AND bank_version = ?
                """,
                rs -> {
                    int chapterId = rs.getInt("chapter_id");
                    int questionId = rs.getInt("last_question_id");

                    boolean stillInChapter = questionsByChapter
                            .getOrDefault(chapterId, List.of())
                            .stream()
                            .anyMatch(q ->
                                    Objects.equals(
                                            q.id(),
                                            questionId
                                    )
                            );

                    if (stillInChapter) {
                        cursors.put(
                                chapterId,
                                new ChapterLearningCursorResponse(
                                        chapterId,
                                        questionId,
                                        questionId,
                                        rs.getTimestamp(
                                                "last_studied_at"
                                        ).toInstant()
                                )
                        );
                    }
                },
                userId,
                license,
                currentBank
        );

        int total = 0;
        int learnedTotal = 0;

        List<ChapterLearningOverviewResponse.Chapter> chapters =
                new java.util.ArrayList<>();

        for (int chapterId = FIRST_CHAPTER;
             chapterId <= LAST_CHAPTER;
             chapterId++) {

            int chapterTotal =
                    questionsByChapter.get(chapterId).size();

            int learned =
                    learnedByChapter.getOrDefault(chapterId, 0);

            if (learned > chapterTotal) {
                throw BusinessException.conflict(
                        "Chapter progress exceeds current question count"
                );
            }

            ChapterLearningCursorResponse cursor =
                    cursors.get(chapterId);

            String status =
                    chapterTotal > 0 && learned == chapterTotal
                            ? "COMPLETED"
                            : learned > 0 || cursor != null
                                    ? "IN_PROGRESS"
                                    : "NOT_STARTED";

            chapters.add(
                    new ChapterLearningOverviewResponse.Chapter(
                            chapterId,
                            chapterTotal,
                            learned,
                            percent(learned, chapterTotal),
                            status,
                            cursor == null
                                    ? null
                                    : cursor.lastQuestionId(),
                            cursor == null
                                    ? null
                                    : cursor.resumeQuestionId(),
                            cursor == null
                                    ? null
                                    : cursor.lastStudiedAt()
                    )
            );

            total += chapterTotal;
            learnedTotal += learned;
        }

        return new ChapterLearningOverviewResponse(
                license,
                currentBank,
                total,
                learnedTotal,
                percent(learnedTotal, total),
                List.copyOf(chapters)
        );
    }

    @Transactional
    public ChapterLearningAnswerResponse recordAnswer(
            UUID userId,
            int chapterId,
            RecordChapterAnswerRequest request
    ) {
        String license = normalizeLicense(request.licenseClass());

        QuestionResponse question = inCurrentChapter(
                chapterId,
                license,
                request.questionId()
        );

        String bankVersion = bankVersionOf(question.id());
        String answer = normalizeAnswer(request.selectedAnswer());

        List<OptionCheck> checks = jdbcTemplate.query("""
                SELECT q.correct_answer,
                       EXISTS (
                           SELECT 1
                           FROM jsonb_array_elements(q.options) opt
                           WHERE upper(btrim(opt->>'label')) = ?
                       ) AS valid_option
                FROM questions q
                WHERE q.id = ?
                  AND q.chapter = ?
                  AND q.bank_version = ?
                  AND jsonb_exists(
                      q.applicable_licenses,
                      ?
                  )
                """,
                (rs, rowNum) -> new OptionCheck(
                        rs.getString("correct_answer"),
                        rs.getBoolean("valid_option")
                ),
                answer,
                question.id(),
                chapterId,
                bankVersion,
                license
        );

        if (checks.size() != 1) {
            throw BusinessException.badRequest(
                    "Question is not in the current bank and license"
            );
        }

        OptionCheck checked = checks.get(0);

        if (!checked.validOption()) {
            throw BusinessException.of(
                    "INVALID_ANSWER_OPTION",
                    "Answer option does not belong to this question",
                    org.springframework.http.HttpStatus.BAD_REQUEST
            );
        }

        boolean correct =
                answer.equalsIgnoreCase(checked.correctAnswer());

        Instant answeredAt = jdbcTemplate.queryForObject("""
                INSERT INTO chapter_question_progress
                    (
                        user_id,
                        license_class,
                        bank_version,
                        question_id,
                        chapter_id,
                        selected_answer,
                        is_correct
                    )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (
                    user_id,
                    license_class,
                    bank_version,
                    question_id
                )
                DO UPDATE SET
                    selected_answer = EXCLUDED.selected_answer,
                    is_correct = EXCLUDED.is_correct,
                    last_answered_at = now()
                RETURNING last_answered_at
                """,
                (rs, rowNum) ->
                        rs.getTimestamp(1).toInstant(),
                userId,
                license,
                bankVersion,
                question.id(),
                chapterId,
                answer,
                correct
        );

        wrongQuestionProgressService.recordAnswer(
                userId,
                license,
                bankVersion,
                question.id(),
                answer,
                answeredAt
        );

        upsertCursor(
                userId,
                license,
                bankVersion,
                chapterId,
                question.id()
        );

        return new ChapterLearningAnswerResponse(
                question.id(),
                answer,
                correct,
                checked.correctAnswer(),
                question.explanation(),
                answeredAt
        );
    }

    @Transactional
    public ChapterLearningCursorResponse saveCursor(
            UUID userId,
            int chapterId,
            SaveChapterCursorRequest request
    ) {
        String license = normalizeLicense(request.licenseClass());

        QuestionResponse question = inCurrentChapter(
                chapterId,
                license,
                request.questionId()
        );

        String bankVersion = bankVersionOf(question.id());

        Boolean eligible = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM questions q
                    WHERE q.id = ?
                      AND q.chapter = ?
                      AND q.bank_version = ?
                      AND jsonb_exists(
                          q.applicable_licenses,
                          ?
                      )
                )
                """,
                Boolean.class,
                question.id(),
                chapterId,
                bankVersion,
                license
        );

        if (!Boolean.TRUE.equals(eligible)) {
            throw BusinessException.badRequest(
                    "Question is not in the current bank and license"
            );
        }

        Instant studiedAt = upsertCursor(
                userId,
                license,
                bankVersion,
                chapterId,
                question.id()
        );

        return new ChapterLearningCursorResponse(
                chapterId,
                question.id(),
                question.id(),
                studiedAt
        );
    }

    private Instant upsertCursor(
            UUID userId,
            String license,
            String bankVersion,
            int chapterId,
            int questionId
    ) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO chapter_learning_cursor
                    (
                        user_id,
                        license_class,
                        bank_version,
                        chapter_id,
                        last_question_id
                    )
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (
                    user_id,
                    license_class,
                    bank_version,
                    chapter_id
                )
                DO UPDATE SET
                    last_question_id =
                        EXCLUDED.last_question_id,
                    last_studied_at = now()
                RETURNING last_studied_at
                """,
                (rs, rowNum) ->
                        rs.getTimestamp(1).toInstant(),
                userId,
                license,
                bankVersion,
                chapterId,
                questionId
        );
    }

    private QuestionResponse inCurrentChapter(
            int chapterId,
            String license,
            Integer questionId
    ) {
        if (chapterId < FIRST_CHAPTER
                || chapterId > LAST_CHAPTER) {
            throw BusinessException.badRequest(
                    "Invalid chapterId: " + chapterId
            );
        }

        return questionService
                .getByChapter(chapterId, license)
                .stream()
                .filter(q ->
                        Objects.equals(q.id(), questionId)
                )
                .findFirst()
                .orElseThrow(() ->
                        BusinessException.badRequest(
                                "Question does not belong to this "
                                        + "chapter, bank and license"
                        )
                );
    }

    private String bankVersionOf(int questionId) {
        List<String> versions = jdbcTemplate.query(
                "SELECT bank_version FROM questions WHERE id = ?",
                (rs, rowNum) -> rs.getString(1),
                questionId
        );

        if (versions.size() != 1
                || versions.get(0) == null) {
            throw BusinessException.conflict(
                    "Current question is missing its bank version"
            );
        }

        return versions.get(0);
    }

    private static String normalizeLicense(
            String licenseClass
    ) {
        if (licenseClass == null
                || licenseClass.isBlank()) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String normalized = licenseClass
                .trim()
                .toUpperCase(Locale.ROOT);

        ExamMatrix.forClass(normalized);

        return normalized;
    }

    private static String normalizeAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw BusinessException.badRequest(
                    "Selected answer is required"
            );
        }

        return answer.trim().toUpperCase(Locale.ROOT);
    }

    private static int percent(int learned, int total) {
        return total == 0
                ? 0
                : Math.round(100f * learned / total);
    }

    private record OptionCheck(
            String correctAnswer,
            boolean validOption
    ) {}
}