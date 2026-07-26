package com.dts.practice.service;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.QuestionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    public List<QuestionResponse> getByChapter(Integer chapter) {
        return questionRepository.findByChapter(chapter).stream()
                .map(this::toResponse)
                .toList();
    }

    public QuestionResponse getById(Integer id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Question not found: " + id));
        return toResponse(q);
    }

    public List<QuestionResponse> getCriticalQuestions() {
        return questionRepository.findByIsCriticalTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public long countAll() {
        return questionRepository.count();
    }

    public long countByChapter(Integer chapter) {
        return questionRepository.countByChapter(chapter);
    }

    private QuestionResponse toResponse(Question q) {
        Object optionsObj;
        try {
            optionsObj = objectMapper.readValue(q.getOptions(), Object.class);
        } catch (JsonProcessingException e) {
            optionsObj = q.getOptions();
        }
        return new QuestionResponse(
                q.getId(),
                q.getChapter(),
                q.getQuestionText(),
                optionsObj,
                q.getIsCritical(),
                q.getImageUrl()
        );
    }
}
