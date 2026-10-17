package com.dts.practice.service;

import com.dts.practice.dto.response.LicenseCatalogItemResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.repository.QuestionRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LicenseCatalogServiceTest {

    private final QuestionRepository repository =
            mock(QuestionRepository.class);

    private final LicenseCatalogService service =
            new LicenseCatalogService(repository);

    @Test
    void listsAllClassesWithActualBankCountsAndCanonicalExamRules() {
        Map<String, List<Integer>> chapters = Map.of(
                "A1", List.of(100, 10, 15, 0, 90, 35),
                "A", List.of(100, 10, 15, 0, 90, 35),
                "B1", List.of(110, 10, 15, 2, 128, 35)
        );

        when(repository.countByBankVersionAndLicenseClass(
                anyString(), anyString()
        )).thenAnswer(call -> {
            String license = call.getArgument(1);
            return chapters.containsKey(license)
                    ? ("B1".equals(license) ? 300L : 250L)
                    : 600L;
        });

        when(repository.countByBankVersionAndChapterAndLicenseClass(
                anyString(), anyInt(), anyString()
        )).thenAnswer(call -> {
            int chapter = call.getArgument(1);
            String license = call.getArgument(2);

            return (long) chapters.getOrDefault(
                    license,
                    List.of(180, 25, 58, 37, 185, 115)
            ).get(chapter - 1);
        });

        when(repository.findCriticalByBankVersionAndLicenseClass(
                anyString(), anyString()
        )).thenAnswer(call -> {
            String license = call.getArgument(1);
            int count = "B1".equals(license)
                    ? 30
                    : ("A1".equals(license) || "A".equals(license)
                            ? 20
                            : 60);

            List<Question> result = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                Question question = new Question();
                question.setChapter(1);
                question.setIsCritical(true);
                question.setApplicableLicenses(List.of(license));
                result.add(question);
            }

            return result;
        });

        List<LicenseCatalogItemResponse> catalog =
                service.getCatalog();

        assertEquals(15, catalog.size());

        assertEquals(
                List.of(
                        "A1", "A", "B1", "B", "C1", "C",
                        "D1", "D2", "D", "BE", "C1E", "CE",
                        "D1E", "D2E", "DE"
                ),
                catalog.stream()
                        .map(LicenseCatalogItemResponse::code)
                        .toList()
        );

        assertTrue(catalog.stream().allMatch(
                LicenseCatalogItemResponse::bankCountsMatch
        ));
        assertTrue(catalog.stream().allMatch(
                LicenseCatalogItemResponse::examAvailable
        ));

        assertEquals(20L, catalog.get(0).criticalStudyQuestions());
        assertEquals(30L, catalog.get(2).criticalStudyQuestions());
        assertEquals(60L, catalog.get(3).criticalStudyQuestions());

        assertEquals(21, catalog.get(0).passingScore());
        assertEquals(23, catalog.get(1).passingScore());
        assertEquals(41, catalog.get(14).passingScore());
    }

    @Test
    void neverReportsExpectedCountsAsIfTheyWerePresentInTheDatabase() {
        when(repository.countByBankVersionAndLicenseClass(
                anyString(), anyString()
        )).thenReturn(0L);

        when(repository.countByBankVersionAndChapterAndLicenseClass(
                anyString(), anyInt(), anyString()
        )).thenReturn(0L);

        when(repository.findCriticalByBankVersionAndLicenseClass(
                anyString(), anyString()
        )).thenReturn(List.of());

        LicenseCatalogItemResponse a1 =
                service.getCatalog().get(0);

        assertEquals(0L, a1.totalStudyQuestions());
        assertEquals(0L, a1.criticalStudyQuestions());
        assertFalse(a1.bankCountsMatch());
        assertFalse(a1.examAvailable());

        // Quy tắc thi vẫn đến từ ExamMatrix.
        assertEquals(25, a1.totalQuestions());
    }
}