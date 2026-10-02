package com.dts.practice.service;

import com.dts.practice.dto.request.ImportQuestionBankRequest;
import com.dts.practice.dto.request.ImportQuestionBankRequest.BankQuestion;
import com.dts.practice.dto.request.ImportQuestionBankRequest.BankOption;
import com.dts.practice.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionBankImportService {

    private static final String BANK_VERSION = "dts-2026-600-v1";

    private static final Set<String> LICENSES = Set.of(
            "A1", "A", "B1", "B", "C1", "C", "D1", "D2", "D",
            "BE", "C1E", "CE", "D1E", "D2E", "DE"
    );

    private static final Set<Integer> A1_CRITICAL = Set.of(
            19, 20, 21, 22, 24, 26, 27, 28, 30, 47,
            48, 52, 53, 63, 64, 65, 68, 70, 71, 72
    );

    private static final Set<Integer> B1_CRITICAL = Set.of(
            19, 20, 21, 22, 24, 26, 27, 28, 30, 47,
            48, 52, 53, 63, 64, 65, 68, 70, 71, 72,
            73, 74, 87, 89, 90, 91, 92, 215, 254, 255
    );

    private static final Set<Integer> WITHOUT_EXPLANATION =
            Set.of(466, 474, 475);

    private static final int[] CHAPTER_COUNTS =
            {0, 180, 25, 58, 37, 185, 115};

    private static final String INSERT_SQL = """
            INSERT INTO questions (
                chapter, question_text, options, correct_answer,
                is_critical, explanation, bank_version, source_question_id,
                applicable_licenses, critical_licenses,
                content_builder_question_id, media_file_id
            )
            VALUES (
                ?, ?, CAST(? AS jsonb), ?,
                ?, ?, ?, ?,
                CAST(? AS jsonb), CAST(? AS jsonb),
                ?, ?
            )
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public int importBank(ImportQuestionBankRequest request) {
        validateBank(request);

        Long existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM questions WHERE bank_version = ?",
                Long.class,
                BANK_VERSION
        );

        if (existing != null && existing > 0) {
            throw BusinessException.conflict(
                    "Question bank " + BANK_VERSION + " has already been imported"
            );
        }

        List<ImportRow> rows = new ArrayList<>(600);
        for (BankQuestion question : request.questions()) {
            rows.add(new ImportRow(
                    question,
                    toJson(question.options()),
                    toJson(question.applicableLicenses()),
                    toJson(question.criticalLicenses())
            ));
        }

        jdbcTemplate.batchUpdate(INSERT_SQL, rows, 100, (statement, row) -> {
            BankQuestion question = row.question();

            statement.setInt(1, question.chapter());
            statement.setString(2, question.questionText());
            statement.setString(3, row.optionsJson());
            statement.setString(4, question.correctAnswer());
            statement.setBoolean(5, question.isCritical());
            statement.setString(6, question.explanation());
            statement.setString(7, BANK_VERSION);
            statement.setInt(8, question.sourceQuestionId());
            statement.setString(9, row.applicableLicensesJson());
            statement.setString(10, row.criticalLicensesJson());
            statement.setObject(11, question.contentBuilderQuestionId());

            if (question.mediaFileId() == null) {
                statement.setNull(12, Types.OTHER);
            } else {
                statement.setObject(12, question.mediaFileId());
            }
        });

        return rows.size();
    }

    private void validateBank(ImportQuestionBankRequest request) {
        if (request == null
                || !BANK_VERSION.equals(request.bankVersion())
                || request.questions() == null
                || request.questions().size() != 600) {
            throw BusinessException.badRequest("Expected the complete 2026 question bank");
        }

        Set<Integer> sourceIds = new HashSet<>();
        Set<UUID> contentBuilderIds = new HashSet<>();
        Map<String, Integer> licenseCounts = new HashMap<>();
        Map<String, Set<Integer>> criticalIds = new HashMap<>();
        int[] chapterCounts = new int[7];
        int globalCriticalCount = 0;
        int mediaCount = 0;

        for (String license : LICENSES) {
            criticalIds.put(license, new HashSet<>());
        }

        for (BankQuestion question : request.questions()) {
            Integer sourceId = question.sourceQuestionId();

            if (sourceId == null || sourceId < 1 || sourceId > 600
                    || !sourceIds.add(sourceId)) {
                throw BusinessException.badRequest(
                        "Missing, repeated or invalid source question ID"
                );
            }

            if (question.contentBuilderQuestionId() == null
                    || !contentBuilderIds.add(question.contentBuilderQuestionId())) {
                throw BusinessException.badRequest(
                        "Missing or repeated Content Builder question ID"
                );
            }

            if (question.chapter() == null
                    || question.chapter() != chapterFor(sourceId)) {
                throw BusinessException.badRequest(
                        "Question " + sourceId + " belongs to an incorrect chapter"
                );
            }
            chapterCounts[question.chapter()]++;

            if (question.questionText() == null
                    || question.questionText().isBlank()) {
                throw BusinessException.badRequest(
                        "Question " + sourceId + " has no content"
                );
            }

            validateOptions(question);

            if ((question.explanation() == null
                    || question.explanation().isBlank())
                    && !WITHOUT_EXPLANATION.contains(sourceId)) {
                throw BusinessException.badRequest(
                        "Question " + sourceId + " has no explanation"
                );
            }

            if (question.applicableLicenses() == null
                    || question.criticalLicenses() == null
                    || question.isCritical() == null) {
                throw BusinessException.badRequest(
                        "Question " + sourceId + " has incomplete license metadata"
                );
            }

            Set<String> applicable =
                    new HashSet<>(question.applicableLicenses());
            Set<String> critical =
                    new HashSet<>(question.criticalLicenses());

            if (applicable.isEmpty()
                    || applicable.size() != question.applicableLicenses().size()
                    || critical.size() != question.criticalLicenses().size()
                    || !LICENSES.containsAll(applicable)
                    || !applicable.containsAll(critical)
                    || (!question.isCritical() && !critical.isEmpty())
                    || (question.isCritical() && critical.isEmpty())) {
                throw BusinessException.badRequest(
                        "Question " + sourceId + " has invalid license metadata"
                );
            }

            for (String license : applicable) {
                licenseCounts.merge(license, 1, Integer::sum);
            }
            for (String license : critical) {
                criticalIds.get(license).add(sourceId);
            }

            if (question.isCritical()) {
                globalCriticalCount++;
            }
            if (question.mediaFileId() != null) {
                mediaCount++;
            }
        }

        for (int chapter = 1; chapter <= 6; chapter++) {
            if (chapterCounts[chapter] != CHAPTER_COUNTS[chapter]) {
                throw BusinessException.badRequest(
                        "Incorrect question count in chapter " + chapter
                );
            }
        }

        if (sourceIds.size() != 600
                || globalCriticalCount != 60
                || mediaCount != 318) {
            throw BusinessException.badRequest(
                    "Question bank totals do not match the validated source"
            );
        }

        for (String license : LICENSES) {
            int expectedTotal = switch (license) {
                case "A1", "A" -> 250;
                case "B1" -> 300;
                default -> 600;
            };
            int expectedCritical = switch (license) {
                case "A1", "A" -> 20;
                case "B1" -> 30;
                default -> 60;
            };

            if (licenseCounts.getOrDefault(license, 0) != expectedTotal
                    || criticalIds.get(license).size() != expectedCritical) {
                throw BusinessException.badRequest(
                        "Question counts do not match license " + license
                );
            }
        }

        if (!criticalIds.get("A1").equals(A1_CRITICAL)
                || !criticalIds.get("A").equals(A1_CRITICAL)
                || !criticalIds.get("B1").equals(B1_CRITICAL)) {
            throw BusinessException.badRequest(
                    "Critical question IDs do not match A1, A or B1"
            );
        }
    }

    private void validateOptions(BankQuestion question) {
        List<BankOption> options = question.options();
        if (options == null || options.size() < 2 || options.size() > 4
                || question.correctAnswer() == null) {
            throw BusinessException.badRequest(
                    "Question " + question.sourceQuestionId() + " has invalid options"
            );
        }

        boolean correctAnswerExists = false;
        for (int index = 0; index < options.size(); index++) {
            BankOption option = options.get(index);
            String expectedLabel = String.valueOf((char) ('A' + index));

            if (option == null
                    || !expectedLabel.equals(option.label())
                    || option.text() == null
                    || option.text().isBlank()) {
                throw BusinessException.badRequest(
                        "Question " + question.sourceQuestionId()
                                + " has invalid option order"
                );
            }

            if (expectedLabel.equals(question.correctAnswer())) {
                correctAnswerExists = true;
            }
        }

        if (!correctAnswerExists) {
            throw BusinessException.badRequest(
                    "Question " + question.sourceQuestionId()
                            + " has no valid correct answer"
            );
        }
    }

    private int chapterFor(int sourceId) {
        if (sourceId <= 180) return 1;
        if (sourceId <= 205) return 2;
        if (sourceId <= 263) return 3;
        if (sourceId <= 300) return 4;
        if (sourceId <= 485) return 5;
        return 6;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Cannot serialize validated question bank", exception
            );
        }
    }

    private record ImportRow(
            BankQuestion question,
            String optionsJson,
            String applicableLicensesJson,
            String criticalLicensesJson
    ) {}
}