package com.dts.practice.service;

import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExamRetryService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();
    private static final String ACTIVE_INDEX =
            "uq_exams_one_active_per_user_license";

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamService examService;

    @Transactional
    public ExamSessionResponse retryExam(UUID sourceExamId, JwtUserDetails user) {
        Exam source = examRepository
                .findByIdAndUserId(sourceExamId, user.userId())
                .orElseThrow(() -> BusinessException.notFound("Exam not found"));

        if (source.getStatus() != ExamStatus.COMPLETED
                && source.getStatus() != ExamStatus.TIMEOUT) {
            throw BusinessException.of(
                    "EXAM_NOT_FINISHED",
                    "Finish the exam before retrying it",
                    HttpStatus.CONFLICT
            );
        }

        // Lịch sử legacy vẫn xem được, nhưng không đưa câu cũ vào bài mới.
        if (!CURRENT_BANK.equals(source.getBankVersion())) {
            throw BusinessException.of(
                    "RETRY_BANK_UNAVAILABLE",
                    "This question bank is no longer available for retry",
                    HttpStatus.CONFLICT
            );
        }

        List<Integer> questionIds = source.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()
                || new HashSet<>(questionIds).size() != questionIds.size()
                || !Objects.equals(source.getTotalQuestions(), questionIds.size())
                || source.getDurationMinutes() == null
                || source.getDurationMinutes() <= 0
                || source.getMode() == null
                || (!"EXAM".equals(source.getMode())
                    && !"PRACTICE".equals(source.getMode()))) {
            throw BusinessException.conflict("Original exam cannot be retried");
        }

        if (examRepository.existsByUserIdAndExamTypeAndStatusIn(
                user.userId(),
                source.getExamType(),
                List.of(ExamStatus.IN_PROGRESS, ExamStatus.PAUSED)
        )) {
            throw activeExamExists(source.getExamType());
        }

        Instant now = Instant.now();
        Exam retry = Exam.builder()
                .userId(user.userId())
                .username(user.username())
                .fullName(source.getFullName())
                .examType(source.getExamType())
                .bankVersion(source.getBankVersion())
                .criticalQuestionId(source.getCriticalQuestionId())
                .presetExamSetId(source.getPresetExamSetId())
                .questionIds(List.copyOf(questionIds))
                .currentQuestionId(questionIds.get(0))
                .totalQuestions(source.getTotalQuestions())
                .durationMinutes(source.getDurationMinutes())
                .expiresAt(now.plus(source.getDurationMinutes(), ChronoUnit.MINUTES))
                .mode(source.getMode())
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .build();

        try {
            retry = examRepository.saveAndFlush(retry);
        } catch (DataIntegrityViolationException error) {
            if (isActiveIndexViolation(error)) {
                throw activeExamExists(source.getExamType());
            }
            throw error;
        }

        Exam savedRetry = retry;
        List<ExamAnswer> newAnswers = questionIds.stream()
                .map(questionId -> ExamAnswer.builder()
                        .exam(savedRetry)
                        .questionId(questionId)
                        .flagged(false)
                        .build())
                .toList();

        examAnswerRepository.saveAllAndFlush(newAnswers);
        return examService.getExamSession(savedRetry.getId(), user.userId());
    }

    private BusinessException activeExamExists(String licenseClass) {
        return BusinessException.of(
                "ACTIVE_EXAM_EXISTS",
                "Bạn đang có một bài " + licenseClass + " chưa hoàn thành.",
                HttpStatus.CONFLICT
        );
    }

    private boolean isActiveIndexViolation(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException
                    constraint
                    && ACTIVE_INDEX.equals(constraint.getConstraintName())) {
                return true;
            }
            if (cause.getMessage() != null
                    && cause.getMessage().contains(ACTIVE_INDEX)) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
