package com.dts.practice.service;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.mapper.QuestionMapper;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "questions", key = "#chapterId")
    public List<QuestionResponse> getByChapter(Integer chapterId) {
        return questionMapper.toResponseList(questionRepository.findByChapter(chapterId));
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
