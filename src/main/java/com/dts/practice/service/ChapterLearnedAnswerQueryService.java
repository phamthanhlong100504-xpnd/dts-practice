package com.dts.practice.service;

import com.dts.practice.dto.response.ChapterLearnedAnswerResponse;
import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterLearnedAnswerQueryService {

    private final QuestionService questionService;
    private final JdbcTemplate jdbcTemplate;

    public List<ChapterLearnedAnswerResponse> getAnswers(
            UUID userId,
            int chapterId,
            String licenseClass
    ) {
        if (chapterId < 1 || chapterId > 6) {
            throw BusinessException.badRequest(
                    "Invalid chapterId: " + chapterId
            );
        }

        if (licenseClass == null || licenseClass.isBlank()) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String license = licenseClass
                .trim()
                .toUpperCase(Locale.ROOT);

        ExamMatrix.forClass(license);

        // Danh sách này đã được QuestionService lọc theo bank hiện hành.
        List<QuestionResponse> currentQuestions =
                questionService.getByChapter(chapterId, license);

        if (currentQuestions.isEmpty()) {
            return List.of();
        }

        return jdbcTemplate.query("""
                SELECT
                    p.question_id,
                    p.selected_answer,
                    p.is_correct,
                    p.learned_at,
                    p.last_answered_at
                FROM chapter_question_progress p
                JOIN questions q ON q.id = p.question_id
                WHERE p.user_id = ?
                  AND p.license_class = ?
                  AND p.bank_version = (
                      SELECT bank_version
                      FROM questions
                      WHERE id = ?
                  )
                  AND p.chapter_id = ?
                  AND q.chapter = p.chapter_id
                  AND q.bank_version = p.bank_version
                  AND jsonb_exists(
                      q.applicable_licenses,
                      p.license_class
                  )
                ORDER BY p.question_id
                """,
                (rs, rowNum) ->
                        new ChapterLearnedAnswerResponse(
                                rs.getInt("question_id"),
                                rs.getString("selected_answer"),
                                rs.getBoolean("is_correct"),
                                rs.getTimestamp(
                                        "learned_at"
                                ).toInstant(),
                                rs.getTimestamp(
                                        "last_answered_at"
                                ).toInstant()
                        ),
                userId,
                license,
                currentQuestions.get(0).id(),
                chapterId
        );
    }
}