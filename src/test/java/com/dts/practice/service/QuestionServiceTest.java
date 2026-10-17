package com.dts.practice.service;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.mapper.QuestionMapper;
import com.dts.practice.repository.QuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    private static final String BANK = "dts-2026-600-v1";

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    private Question question;
    private QuestionResponse questionResponse;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setId(1);
        question.setChapter(1);
        question.setIsCritical(true);

        questionResponse = new QuestionResponse(
                1,
                1,
                "Question text",
                "options",
                true,
                "imageUrl",
                null,
                "C",
                "explain"
        );
    }

    @Test
    void testGetByChapter() {
        when(questionRepository
                .findByBankVersionAndChapterOrderBySourceQuestionIdAsc(
                        BANK, 1
                ))
                .thenReturn(List.of(question));
        when(questionMapper.toResponseList(List.of(question)))
                .thenReturn(List.of(questionResponse));

        List<QuestionResponse> result = questionService.getByChapter(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).id());
    }

    @Test
    void testGetByChapterForLicenseUsesLicenseCriticalFlag() {
        when(questionRepository
                .findByBankVersionAndChapterAndLicenseClass(
                        BANK, 1, "A1"
                ))
                .thenReturn(List.of(question));
        when(questionMapper.toResponseList(List.of(question)))
                .thenReturn(List.of(questionResponse));
        when(questionRepository
                .findCriticalByBankVersionAndLicenseClass(
                        BANK, "A1"
                ))
                .thenReturn(List.of());

        List<QuestionResponse> result =
                questionService.getByChapter(1, "a1");

        assertEquals(1, result.size());
        assertFalse(result.get(0).isCritical());
    }

    @Test
    void testGetByIdSuccess() {
        when(questionRepository.findById(1))
                .thenReturn(Optional.of(question));
        when(questionMapper.toResponse(question))
                .thenReturn(questionResponse);

        QuestionResponse result = questionService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.id());
    }

    @Test
    void testGetByIdNotFound() {
        when(questionRepository.findById(2))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> questionService.getById(2)
        );
    }

    @Test
    void testGetByIdNotApplicableToLicense() {
        when(questionRepository.findById(1))
                .thenReturn(Optional.of(question));
        when(questionMapper.toResponse(question))
                .thenReturn(questionResponse);
        when(questionRepository.findCriticalFlagForLicense(
                1, BANK, "A1"
        )).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> questionService.getById(1, "A1")
        );
    }

    @Test
    void testGetCriticalQuestions() {
        when(questionRepository
                .findByBankVersionAndIsCriticalTrueOrderBySourceQuestionIdAsc(
                        BANK
                ))
                .thenReturn(List.of(question));
        when(questionMapper.toResponseList(List.of(question)))
                .thenReturn(List.of(questionResponse));

        List<QuestionResponse> result =
                questionService.getCriticalQuestions();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isCritical());
    }

    @Test
    void testCountAll() {
        when(questionRepository.countByBankVersion(BANK))
                .thenReturn(600L);

        assertEquals(600L, questionService.countAll());
    }

    @Test
    void testCountByChapter() {
        when(questionRepository.countByBankVersionAndChapter(BANK, 1))
                .thenReturn(180L);

        assertEquals(180L, questionService.countByChapter(1));
    }

    @Test
    void testInvalidLicenseClass() {
        assertThrows(
                BusinessException.class,
                () -> questionService.countAll("B2")
        );
    }
}