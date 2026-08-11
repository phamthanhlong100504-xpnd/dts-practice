package com.dts.practice.service;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.mapper.QuestionMapper;
import com.dts.practice.repository.QuestionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestionService")
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionService questionService;

    @Nested
    @DisplayName("getById")
    class GetById {

        @Test
        @DisplayName("should return question response when question exists")
        void whenExists_shouldReturnQuestionResponse() {
            Question question = Question.builder().id(1).chapter(1).questionText("Q1").build();
            QuestionResponse response = new QuestionResponse(1, 1, "Q1", null, false, null, null, null);

            given(questionRepository.findById(1)).willReturn(Optional.of(question));
            given(questionMapper.toResponse(question)).willReturn(response);

            QuestionResponse result = questionService.getById(1);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1);
            then(questionRepository).should().findById(1);
        }

        @Test
        @DisplayName("should throw not found exception when question does not exist")
        void whenNotExists_shouldThrowNotFound() {
            given(questionRepository.findById(999)).willReturn(Optional.empty());

            assertThatThrownBy(() -> questionService.getById(999))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Question not found");
        }
    }

    @Nested
    @DisplayName("getByChapter")
    class GetByChapter {

        @Test
        @DisplayName("should return list of question responses")
        void shouldReturnQuestionList() {
            Question q1 = Question.builder().id(1).chapter(1).build();
            Question q2 = Question.builder().id(2).chapter(1).build();
            QuestionResponse r1 = new QuestionResponse(1, 1, "Q1", null, false, null, null, null);
            QuestionResponse r2 = new QuestionResponse(2, 1, "Q2", null, false, null, null, null);

            given(questionRepository.findByChapter(1)).willReturn(List.of(q1, q2));
            given(questionMapper.toResponseList(List.of(q1, q2))).willReturn(List.of(r1, r2));

            List<QuestionResponse> result = questionService.getByChapter(1);

            assertThat(result).hasSize(2);
            then(questionRepository).should().findByChapter(1);
            then(questionMapper).should().toResponseList(List.of(q1, q2));
        }
    }

    @Nested
    @DisplayName("getCriticalQuestions")
    class GetCriticalQuestions {

        @Test
        @DisplayName("should return critical questions only")
        void shouldReturnCriticalQuestions() {
            Question q = Question.builder().id(5).isCritical(true).build();
            QuestionResponse r = new QuestionResponse(5, 1, "Q5", null, true, null, null, null);

            given(questionRepository.findByIsCriticalTrue()).willReturn(List.of(q));
            given(questionMapper.toResponseList(List.of(q))).willReturn(List.of(r));

            List<QuestionResponse> result = questionService.getCriticalQuestions();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).isCritical()).isTrue();
        }
    }

    @Nested
    @DisplayName("countAll")
    class CountAll {

        @Test
        @DisplayName("should return total question count")
        void shouldReturnTotalCount() {
            given(questionRepository.count()).willReturn(600L);

            long result = questionService.countAll();

            assertThat(result).isEqualTo(600L);
        }
    }

    @Nested
    @DisplayName("countByChapter")
    class CountByChapter {

        @Test
        @DisplayName("should return chapter question count")
        void shouldReturnChapterCount() {
            given(questionRepository.countByChapter(1)).willReturn(100L);

            long result = questionService.countByChapter(1);

            assertThat(result).isEqualTo(100L);
        }
    }
}
