package com.dts.practice.service;

import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamCursorService {

    private final ExamRepository examRepository;
    private final ExamService examService;
    private final ExamSessionFeedbackService examSessionFeedbackService;

    @Transactional(noRollbackFor = BusinessException.class)
    public ExamSessionResponse updateCursor(
            UUID examId,
            UUID userId,
            Integer currentQuestionId
    ) {
        Exam exam = examRepository
                .findByIdAndUserIdForUpdate(examId, userId)
                .orElseThrow(
                        () -> BusinessException.notFound("Exam not found")
                );

        if (exam.getStatus() != ExamStatus.IN_PROGRESS) {
            throw BusinessException.of(
                    "EXAM_NOT_ACTIVE",
                    "Exam is not in progress",
                    HttpStatus.CONFLICT
            );
        }

        if (exam.getExpiresAt() == null) {
            throw BusinessException.conflict(
                    "Exam expiration is unavailable"
            );
        }

        if (!Instant.now().isBefore(exam.getExpiresAt())) {
            examService.getExamSession(examId, userId);
            throw BusinessException.of(
                    "EXAM_TIME_EXPIRED",
                    "Exam time has expired",
                    HttpStatus.CONFLICT
            );
        }

        if (currentQuestionId == null
                || exam.getQuestionIds() == null
                || !exam.getQuestionIds().contains(currentQuestionId)) {
            throw BusinessException.of(
                    "INVALID_EXAM_QUESTION",
                    "Current question is not in this exam",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!Objects.equals(
                currentQuestionId,
                exam.getCurrentQuestionId()
        )) {
            exam.setCurrentQuestionId(currentQuestionId);
            examRepository.save(exam);
        }

        return examSessionFeedbackService.getSession(
                examId,
                userId
        );
    }
}