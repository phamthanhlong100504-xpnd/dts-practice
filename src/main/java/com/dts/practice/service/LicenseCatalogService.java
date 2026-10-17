package com.dts.practice.service;

import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LicenseCatalogService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();
    private static final String MOTORCYCLE = "Mô tô";
    private static final String CAR = "Ô tô";
    private static final String TRAILER = "Rơ moóc";

    private static final List<Integer> MOTORCYCLE_CHAPTERS =
            List.of(100, 10, 15, 0, 90, 35);
    private static final List<Integer> B1_CHAPTERS =
            List.of(110, 10, 15, 2, 128, 35);
    private static final List<Integer> OTHER_CHAPTERS =
            List.of(180, 25, 58, 37, 185, 115);

    private static final List<Definition> CATALOG = List.of(
            new Definition("A1", MOTORCYCLE,
                    "Lái xe mô tô hai bánh có dung tích xi-lanh đến 125 cm³ hoặc công suất động cơ điện đến 11 kW.",
                    MOTORCYCLE_CHAPTERS, 20),
            new Definition("A", MOTORCYCLE,
                    "Lái xe mô tô hai bánh có dung tích xi-lanh trên 125 cm³ hoặc công suất động cơ điện trên 11 kW và xe hạng A1.",
                    MOTORCYCLE_CHAPTERS, 20),
            new Definition("B1", MOTORCYCLE,
                    "Lái xe mô tô ba bánh và các loại xe hạng A1.",
                    B1_CHAPTERS, 30),
            new Definition("B", CAR,
                    "Lái xe ô tô đến 08 chỗ (không kể lái xe); xe ô tô tải và ô tô chuyên dùng đến 3.500 kg; xe hạng B kéo rơ moóc đến 750 kg.",
                    OTHER_CHAPTERS, 60),
            new Definition("C1", CAR,
                    "Lái xe ô tô tải và ô tô chuyên dùng trên 3.500 kg đến 7.500 kg; xe hạng C1 kéo rơ moóc đến 750 kg; các loại xe hạng B.",
                    OTHER_CHAPTERS, 60),
            new Definition("C", CAR,
                    "Lái xe ô tô tải và ô tô chuyên dùng trên 7.500 kg; xe hạng C kéo rơ moóc đến 750 kg; các loại xe hạng B và C1.",
                    OTHER_CHAPTERS, 60),
            new Definition("D1", CAR,
                    "Lái xe ô tô trên 08 chỗ đến 16 chỗ (không kể lái xe); xe hạng D1 kéo rơ moóc đến 750 kg; các loại xe hạng B, C1, C.",
                    OTHER_CHAPTERS, 60),
            new Definition("D2", CAR,
                    "Lái xe ô tô chở người (kể cả xe buýt) trên 16 chỗ đến 29 chỗ (không kể lái xe); xe hạng D2 kéo rơ moóc đến 750 kg; các loại xe hạng B, C1, C, D1.",
                    OTHER_CHAPTERS, 60),
            new Definition("D", CAR,
                    "Lái xe ô tô chở người (kể cả xe buýt) trên 29 chỗ (không kể lái xe); xe giường nằm; xe hạng D kéo rơ moóc đến 750 kg; các loại xe hạng B, C1, C, D1, D2.",
                    OTHER_CHAPTERS, 60),
            new Definition("BE", TRAILER,
                    "Lái xe hạng B kéo rơ moóc trên 750 kg.",
                    OTHER_CHAPTERS, 60),
            new Definition("C1E", TRAILER,
                    "Lái xe hạng C1 kéo rơ moóc trên 750 kg.",
                    OTHER_CHAPTERS, 60),
            new Definition("CE", TRAILER,
                    "Lái xe hạng C kéo rơ moóc trên 750 kg; xe ô tô đầu kéo kéo sơ mi rơ moóc.",
                    OTHER_CHAPTERS, 60),
            new Definition("D1E", TRAILER,
                    "Lái xe hạng D1 kéo rơ moóc trên 750 kg.",
                    OTHER_CHAPTERS, 60),
            new Definition("D2E", TRAILER,
                    "Lái xe hạng D2 kéo rơ moóc trên 750 kg.",
                    OTHER_CHAPTERS, 60),
            new Definition("DE", TRAILER,
                    "Lái xe hạng D kéo rơ moóc trên 750 kg; xe ô tô chở khách nối toa.",
                    OTHER_CHAPTERS, 60)
    );

    private final QuestionRepository questionRepository;

    public List<LicenseCatalogItemResponse> getCatalog() {
        return CATALOG.stream().map(this::toItem).toList();
    }

    private LicenseCatalogItemResponse toItem(Definition definition) {
        String license = definition.code();
        ExamMatrix.Rule rule = ExamMatrix.forClass(license);

        long actualTotal = questionRepository
                .countByBankVersionAndLicenseClass(CURRENT_BANK, license);

        long[] actualByChapter = new long[7];
        for (int chapter = 1; chapter <= 6; chapter++) {
            actualByChapter[chapter] = questionRepository
                    .countByBankVersionAndChapterAndLicenseClass(
                            CURRENT_BANK, chapter, license
                    );
        }

        List<Question> criticalQuestions = questionRepository
                .findCriticalByBankVersionAndLicenseClass(
                        CURRENT_BANK, license
                )
                .stream()
                .filter(question ->
                        question.getApplicableLicenses() != null
                                && question.getApplicableLicenses()
                                        .contains(license))
                .toList();

        long[] criticalByChapter = new long[7];
        boolean criticalChaptersValid = true;

        for (Question question : criticalQuestions) {
            Integer chapter = question.getChapter();
            if (chapter == null || chapter < 1 || chapter > 6) {
                criticalChaptersValid = false;
            } else {
                criticalByChapter[chapter]++;
            }
        }

        boolean chapterCountsMatch = criticalChaptersValid;
        for (int chapter = 1; chapter <= 6; chapter++) {
            if (actualByChapter[chapter]
                    != definition.studyQuestionsByChapter()
                            .get(chapter - 1)) {
                chapterCountsMatch = false;
            }
        }

        boolean examAvailable =
                !criticalQuestions.isEmpty() && criticalChaptersValid;

        for (Map.Entry<Integer, Integer> quota
                : rule.ordinaryByChapter().entrySet()) {
            int chapter = quota.getKey();

            if (actualByChapter[chapter] - criticalByChapter[chapter]
                    < quota.getValue()) {
                examAvailable = false;
            }
        }

        long expectedTotal = definition.studyQuestionsByChapter()
                .stream()
                .mapToLong(Integer::longValue)
                .sum();

        boolean bankCountsMatch =
                actualTotal == expectedTotal
                        && criticalQuestions.size()
                                == definition.expectedCriticalQuestions()
                        && chapterCountsMatch
                        && examAvailable;

        int criticalPerExam = rule.totalQuestions()
                - rule.ordinaryByChapter().values().stream()
                        .mapToInt(Integer::intValue)
                        .sum();

        return new LicenseCatalogItemResponse(
                license,
                definition.group(),
                "Hạng " + license,
                definition.description(),
                CURRENT_BANK,
                actualTotal,
                criticalQuestions.size(),
                rule.totalQuestions(),
                rule.durationMinutes(),
                rule.requiredCorrect(),
                criticalPerExam,
                bankCountsMatch,
                examAvailable
        );
    }

    private record Definition(
            String code,
            String group,
            String description,
            List<Integer> studyQuestionsByChapter,
            int expectedCriticalQuestions
    ) {}
}