package com.dts.practice.service;

import com.dts.practice.dto.response.ExamFlagsResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamQuestionFlagService {

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;

    @Transactional(readOnly = true)
    public ExamFlagsResponse getFlags(UUID examId, UUID userId) {
        Exam exam = examRepository.findByIdAndUserId(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(examId);

        return toResponse(exam, answers);
    }

    @Transactional
    public ExamFlagsResponse updateFlag(
            UUID examId,
            UUID userId,
            Integer questionId,
            boolean flagged
    ) {
        // Cùng khóa đang dùng khi nộp đáp án/pause để tránh ghi đè đồng thời.
        Exam exam = examRepository.findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (exam.getQuestionIds() == null
                || !exam.getQuestionIds().contains(questionId)) {
            throw BusinessException.badRequest("Question not in this exam");
        }

        List<ExamAnswer> answers =
                examAnswerRepository.findByExamIdOrderByQuestionId(examId);

        ExamAnswer answer = answers.stream()
                .filter(item -> questionId.equals(item.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> BusinessException.conflict(
                        "Exam answer record is missing"
                ));

        answer.setFlagged(flagged);
        examAnswerRepository.save(answer);

        return toResponse(exam, answers);
    }

    private ExamFlagsResponse toResponse(
            Exam exam,
            List<ExamAnswer> answers
    ) {
        Set<Integer> flaggedIds = answers.stream()
                .filter(ExamAnswer::isFlagged)
                .map(ExamAnswer::getQuestionId)
                .collect(Collectors.toSet());

        // Trả theo thứ tự câu trong đề để frontend dựng lại navigator.
        List<Integer> orderedIds = exam.getQuestionIds().stream()
                .filter(flaggedIds::contains)
                .toList();

        return new ExamFlagsResponse(exam.getId(), orderedIds);
    }
}