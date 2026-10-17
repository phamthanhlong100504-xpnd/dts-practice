package com.dts.practice.service;

import com.dts.practice.dto.response.ActiveExamResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActiveExamService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();

    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;

    public ActiveExamResponse getActiveExam(UUID userId, String examType) {
        if (examType == null || examType.isBlank()) {
            throw BusinessException.badRequest("Chưa chọn hạng GPLX.");
        }

        String licenseClass = examType.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(licenseClass);

        Exam exam = examRepository
                .findFirstByUserIdAndExamTypeAndBankVersionAndStatusInOrderByStartedAtDesc(
                        userId,
                        licenseClass,
                        CURRENT_BANK,
                        List.of(ExamStatus.IN_PROGRESS, ExamStatus.PAUSED)
                )
                .orElse(null);

        if (exam == null) {
            return null;
        }

        Instant now = Instant.now();
        Long remainingTimeMs;

        if (exam.getStatus() == ExamStatus.PAUSED) {
            remainingTimeMs = exam.getRemainingTimeMs();
        } else {
            if (exam.getExpiresAt() == null
                    || !now.isBefore(exam.getExpiresAt())) {
                return null;
            }

            remainingTimeMs = Math.max(
                    0L,
                    Duration.between(now, exam.getExpiresAt()).toMillis()
            );
        }

        int answeredCount = (int) examAnswerRepository
                .findByExamIdOrderByQuestionId(exam.getId())
                .stream()
                .filter(answer -> answer.getSelectedAnswer() != null)
                .count();

        return new ActiveExamResponse(
                exam.getId(),
                exam.getExamType(),
                exam.getMode(),
                exam.getStatus().name(),
                exam.getTotalQuestions(),
                answeredCount,
                exam.getDurationMinutes(),
                exam.getStartedAt(),
                exam.getExpiresAt(),
                remainingTimeMs,
                exam.getCurrentQuestionId()
        );
    }
}