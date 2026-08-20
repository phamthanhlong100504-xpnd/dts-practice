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
        
        questionResponse = new QuestionResponse(1, 1, "Question text", "options", true, "imageUrl", "C", "explain");
    }

    @Test
    void testGetByChapter() {
        when(questionRepository.findByChapter(1)).thenReturn(List.of(question));
        when(questionMapper.toResponseList(List.of(question))).thenReturn(List.of(questionResponse));

        List<QuestionResponse> result = questionService.getByChapter(1);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).id());
    }

    @Test
    void testGetById_Success() {
        when(questionRepository.findById(1)).thenReturn(Optional.of(question));
        when(questionMapper.toResponse(question)).thenReturn(questionResponse);

        QuestionResponse result = questionService.getById(1);

        assertNotNull(result);
        assertEquals(1, result.id());
    }

    @Test
    void testGetById_NotFound() {
        when(questionRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> questionService.getById(2));
    }

    @Test
    void testGetCriticalQuestions() {
        when(questionRepository.findByIsCriticalTrue()).thenReturn(List.of(question));
        when(questionMapper.toResponseList(List.of(question))).thenReturn(List.of(questionResponse));

        List<QuestionResponse> result = questionService.getCriticalQuestions();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isCritical());
    }

    @Test
    void testCountAll() {
        when(questionRepository.count()).thenReturn(100L);
        assertEquals(100L, questionService.countAll());
    }

    @Test
    void testCountByChapter() {
        when(questionRepository.countByChapter(1)).thenReturn(20L);
        assertEquals(20L, questionService.countByChapter(1));
    }
}

