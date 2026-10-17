package com.dts.practice.service;

import com.dts.practice.dto.response.LearningBankCatalogResponse;
import com.dts.practice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningBankCatalogService {

    private final JdbcTemplate jdbcTemplate;

    public LearningBankCatalogResponse get(String licenseClass) {
        if (licenseClass == null || licenseClass.isBlank()) {
            throw BusinessException.badRequest("License class is required");
        }

        String license = licenseClass.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(license);
        String bank = CurrentQuestionBank.currentVersion();

        List<LearningBankCatalogResponse.Chapter> chapters =
                jdbcTemplate.query("""
                        SELECT q.chapter AS chapter_id,
                               metadata.chapter_name,
                               COUNT(*)::integer AS total_questions,
                               COUNT(*) FILTER (
                                   WHERE q.is_critical
                                     AND jsonb_exists(q.critical_licenses, ?)
                               )::integer AS critical_questions
                        FROM questions q
                        LEFT JOIN question_bank_chapters metadata
                          ON metadata.bank_version = q.bank_version
                         AND metadata.chapter_id = q.chapter
                        WHERE q.bank_version = ?
                          AND jsonb_exists(q.applicable_licenses, ?)
                        GROUP BY q.chapter, metadata.chapter_name
                        ORDER BY q.chapter
                        """,
                        (rs, row) -> new LearningBankCatalogResponse.Chapter(
                                rs.getInt("chapter_id"),
                                rs.getString("chapter_name"),
                                rs.getInt("total_questions"),
                                rs.getInt("critical_questions")
                        ),
                        license, bank, license
                );

        if (chapters.stream().anyMatch(chapter ->
                chapter.chapterName() == null
                        || chapter.chapterName().isBlank())) {
            throw BusinessException.conflict(
                    "Missing chapter metadata for question bank " + bank
            );
        }

        int total = chapters.stream()
                .mapToInt(LearningBankCatalogResponse.Chapter::totalQuestions)
                .sum();

        int critical = chapters.stream()
                .mapToInt(LearningBankCatalogResponse.Chapter::criticalQuestions)
                .sum();

        if (total == 0) {
            throw BusinessException.conflict(
                    "Current question bank is empty for " + license
            );
        }

        return new LearningBankCatalogResponse(
                license,
                bank,
                total,
                critical,
                List.copyOf(chapters)
        );
    }
}