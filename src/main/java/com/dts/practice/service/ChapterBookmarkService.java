package com.dts.practice.service;

import com.dts.practice.dto.request.SetChapterBookmarkRequest;
import com.dts.practice.dto.response.ChapterBookmarkResponse;
import com.dts.practice.dto.response.ChapterBookmarksResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterBookmarkService {

    private final QuestionService questionService;
    private final JdbcTemplate jdbcTemplate;

    public ChapterBookmarksResponse list(
            UUID userId,
            int chapterId,
            String licenseClass
    ) {
        checkChapter(chapterId);
        String license = normalizeLicense(licenseClass);

        List<QuestionResponse> currentQuestions =
                questionService.getByChapter(chapterId, license);

        if (currentQuestions.isEmpty()) {
            return new ChapterBookmarksResponse(
                    chapterId, license, List.of()
            );
        }

        String bankVersion =
                bankVersionOf(currentQuestions.get(0).id());

        List<Integer> ids = jdbcTemplate.query("""
                SELECT b.question_id
                FROM chapter_question_bookmarks b
                JOIN questions q ON q.id = b.question_id
                WHERE b.user_id = ?
                  AND b.license_class = ?
                  AND b.bank_version = ?
                  AND b.chapter_id = ?
                  AND q.bank_version = b.bank_version
                  AND q.chapter = b.chapter_id
                  AND jsonb_exists(
                      q.applicable_licenses,
                      b.license_class
                  )
                ORDER BY b.question_id
                """,
                (rs, rowNum) ->
                        rs.getInt("question_id"),
                userId,
                license,
                bankVersion,
                chapterId
        );

        return new ChapterBookmarksResponse(
                chapterId,
                license,
                List.copyOf(ids)
        );
    }

    @Transactional
    public ChapterBookmarkResponse set(
            UUID userId,
            int chapterId,
            Integer questionId,
            SetChapterBookmarkRequest request
    ) {
        checkChapter(chapterId);
        String license = normalizeLicense(request.licenseClass());

        List<QuestionResponse> currentQuestions =
                questionService.getByChapter(chapterId, license);

        boolean present = currentQuestions.stream()
                .anyMatch(q ->
                        Objects.equals(q.id(), questionId)
                );

        if (!present) {
            throw BusinessException.badRequest(
                    "Question does not belong to this "
                            + "chapter, bank and license"
            );
        }

        List<String> versions = jdbcTemplate.query("""
                SELECT q.bank_version
                FROM questions q
                WHERE q.id = ?
                  AND q.chapter = ?
                  AND jsonb_exists(
                      q.applicable_licenses,
                      ?
                  )
                """,
                (rs, rowNum) ->
                        rs.getString(1),
                questionId,
                chapterId,
                license
        );

        if (versions.size() != 1 || versions.get(0) == null) {
            throw BusinessException.badRequest(
                    "Question does not belong to this "
                            + "chapter, bank and license"
            );
        }

        String bankVersion = versions.get(0);

        if (request.bookmarked()) {
            jdbcTemplate.update("""
                    INSERT INTO chapter_question_bookmarks
                        (
                            user_id,
                            license_class,
                            bank_version,
                            question_id,
                            chapter_id
                        )
                    VALUES (?, ?, ?, ?, ?)
                    ON CONFLICT (
                        user_id,
                        license_class,
                        bank_version,
                        question_id
                    )
                    DO NOTHING
                    """,
                    userId,
                    license,
                    bankVersion,
                    questionId,
                    chapterId
            );
        } else {
            jdbcTemplate.update("""
                    DELETE FROM chapter_question_bookmarks
                    WHERE user_id = ?
                      AND license_class = ?
                      AND bank_version = ?
                      AND question_id = ?
                      AND chapter_id = ?
                    """,
                    userId,
                    license,
                    bankVersion,
                    questionId,
                    chapterId
            );
        }

        return new ChapterBookmarkResponse(
                chapterId,
                questionId,
                license,
                request.bookmarked()
        );
    }

    private String bankVersionOf(Integer questionId) {
        List<String> versions = jdbcTemplate.query(
                "SELECT bank_version FROM questions WHERE id = ?",
                (rs, rowNum) ->
                        rs.getString(1),
                questionId
        );

        if (versions.size() != 1 || versions.get(0) == null) {
            throw BusinessException.conflict(
                    "Current question is missing its bank version"
            );
        }

        return versions.get(0);
    }

    private static void checkChapter(int chapterId) {
        if (chapterId < 1 || chapterId > 6) {
            throw BusinessException.badRequest(
                    "Invalid chapterId: " + chapterId
            );
        }
    }

    private static String normalizeLicense(
            String licenseClass
    ) {
        if (licenseClass == null || licenseClass.isBlank()) {
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
}