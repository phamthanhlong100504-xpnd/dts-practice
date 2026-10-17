package com.dts.practice.service;

import com.dts.practice.dto.request.UpdateCriticalProgressRequest;
import com.dts.practice.dto.response.CriticalProgressOverviewResponse;
import com.dts.practice.entity.CriticalQuestionProgress;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.CriticalQuestionProgressRepository;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CriticalQuestionProgressService {

    private static final String CURRENT_BANK_VERSION =
        CurrentQuestionBank.currentVersion();

    private final CriticalQuestionProgressRepository progressRepository;
    private final QuestionRepository questionRepository;

    public CriticalProgressOverviewResponse getOverview(
            UUID userId,
            String licenseClass
    ) {
        String license = normalizeLicenseClass(licenseClass);

        // Repository này đã sắp câu theo source_question_id tăng dần.
        List<Question> questions =
                questionRepository.findCriticalByBankVersionAndLicenseClass(
                        CURRENT_BANK_VERSION,
                        license
                );

        Set<Integer> validQuestionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        List<CriticalQuestionProgress> progress =
                progressRepository.findByUserIdAndLicenseClassAndBankVersion(
                        userId,
                        license,
                        CURRENT_BANK_VERSION
                );

        Set<Integer> learnedIds = new HashSet<>();
        boolean hasProgress = false;
        Instant lastStudiedAt = null;

        for (CriticalQuestionProgress item : progress) {
            if (!validQuestionIds.contains(item.getQuestionId())) {
                continue;
            }

            hasProgress = true;

            if (item.isLearned()) {
                learnedIds.add(item.getQuestionId());
            }

            if (lastStudiedAt == null
                    || item.getLastStudiedAt().isAfter(lastStudiedAt)) {
                lastStudiedAt = item.getLastStudiedAt();
            }
        }

        int total = questions.size();
        int learned = learnedIds.size();
        int currentPosition = 0;

        for (int index = 0; index < total; index++) {
            if (!learnedIds.contains(questions.get(index).getId())) {
                currentPosition = index + 1;
                break;
            }
        }

        if (total > 0 && currentPosition == 0) {
            currentPosition = total;
        }

        int percent = total == 0
                ? 0
                : (int) Math.round(learned * 100.0 / total);

        return new CriticalProgressOverviewResponse(
                license,
                total,
                learned,
                percent,
                currentPosition,
                hasProgress,
                total > 0 && learned == total,
                lastStudiedAt
        );
    }

    public CriticalProgressOverviewResponse updateProgress(
            UUID userId,
            Integer questionId,
            UpdateCriticalProgressRequest request
    ) {
        throw BusinessException.of(
                "CRITICAL_PROGRESS_READ_ONLY",
                "Tiến độ câu điểm liệt được ghi khi trả lời câu hỏi trong phiên học hoặc ôn tập.",
                HttpStatus.CONFLICT
        );
    }

    private String normalizeLicenseClass(String licenseClass) {
        if (licenseClass == null) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String normalized =
                licenseClass.trim().toUpperCase(Locale.ROOT);

        ExamMatrix.forClass(normalized);
        return normalized;
    }
}