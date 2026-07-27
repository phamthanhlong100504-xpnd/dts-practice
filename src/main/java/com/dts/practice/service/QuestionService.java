package com.dts.practice.service;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.mapper.QuestionMapper;
import com.dts.practice.repository.QuestionRepository;
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
    private final QuestionMapper questionMapper;

    public List<QuestionResponse> getByChapter(Integer chapter) {
        return questionMapper.toResponseList(questionRepository.findByChapter(chapter));
    }

    public QuestionResponse getById(Integer id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Question not found: " + id));
        return questionMapper.toResponse(q);
    }

    public List<QuestionResponse> getCriticalQuestions() {
        return questionMapper.toResponseList(questionRepository.findByIsCriticalTrue());
    }

    public long countAll() {
        return questionRepository.count();
    }

    public long countByChapter(Integer chapter) {
        return questionRepository.countByChapter(chapter);
    }
}
