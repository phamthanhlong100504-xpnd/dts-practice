package com.dts.practice.service;

import com.dts.practice.dto.request.StartPresetExamRequest;
import com.dts.practice.dto.response.ExamResultResponse;
import com.dts.practice.dto.response.ExamSessionResponse;
import com.dts.practice.dto.response.PresetExamSetListResponse;
import com.dts.practice.entity.Exam;
import com.dts.practice.entity.ExamAnswer;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.ExamStatus;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.exception.ActiveExamExistsException;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.ExamAnswerRepository;
import com.dts.practice.repository.ExamRepository;
import com.dts.practice.repository.PresetExamAttemptRepository;
import com.dts.practice.repository.PresetExamSetRepository;
import com.dts.practice.security.JwtUserDetails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PresetExamSetService {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();
    private static final String ACTIVE_INDEX =
            "uq_exams_one_active_per_user_license";

    private final PresetExamSetRepository presetExamSetRepository;
    private final PresetExamAttemptRepository presetExamAttemptRepository;
    private final PresetExamSetValidator presetExamSetValidator;
    private final ExamRepository examRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamService examService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public PresetExamSetListResponse getPublishedSets(
            UUID userId,
            String licenseClass
    ) {
        String license = normalizeLicenseClass(licenseClass);

        List<PresetExamSet> sets = presetExamSetRepository
                .findByLicenseClassAndBankVersionAndStatusOrderByDisplayOrderAscCodeAsc(
                        license,
                        CURRENT_BANK,
                        PresetExamSetStatus.PUBLISHED
                );

        if (sets.isEmpty()) {
            return new PresetExamSetListResponse(license, 0, List.of());
        }

        List<UUID> setIds = sets.stream()
                .map(PresetExamSet::getId)
                .toList();

        List<Exam> attempts = presetExamAttemptRepository
                .findByUserIdAndPresetExamSetIdInAndBankVersionOrderByStartedAtDesc(
                        userId,
                        setIds,
                        CURRENT_BANK
                );

        Map<UUID, List<Exam>> attemptsBySet = new HashMap<>();
        for (Exam attempt : attempts) {
            attemptsBySet
                    .computeIfAbsent(
                            attempt.getPresetExamSetId(),
                            ignored -> new ArrayList<>()
                    )
                    .add(attempt);
        }

        List<PresetExamSetListResponse.Item> items = sets.stream()
                .map(set -> toItem(
                        userId,
                        set,
                        attemptsBySet.getOrDefault(set.getId(), List.of())
                ))
                .toList();

        return new PresetExamSetListResponse(
                license,
                items.size(),
                items
        );
    }

    @Transactional
    public ExamSessionResponse startSession(
            JwtUserDetails user,
            UUID presetSetId,
            StartPresetExamRequest request
    ) {
        PresetExamSet set = presetExamSetRepository
                .findById(presetSetId)
                .filter(item ->
                        item.getStatus() == PresetExamSetStatus.PUBLISHED
                                && CURRENT_BANK.equals(item.getBankVersion())
                )
                .orElseThrow(() ->
                        BusinessException.notFound(
                                "Preset exam set not found"
                        )
                );

        if (request != null
                && request.licenseClass() != null
                && !normalizeLicenseClass(request.licenseClass())
                        .equals(set.getLicenseClass())) {
            throw BusinessException.badRequest(
                    "License class does not match preset exam set"
            );
        }

        String mode = request == null || request.mode() == null
                ? "EXAM"
                : request.mode().trim().toUpperCase(Locale.ROOT);

        if (!"EXAM".equals(mode) && !"PRACTICE".equals(mode)) {
            throw BusinessException.badRequest(
                    "Mode must be EXAM or PRACTICE"
            );
        }

        // Xử lý tuần tự các POST cùng user và hạng GPLX.
        lockUserLicense(user.userId(), set.getLicenseClass());

        // Cùng preset và mode thì tiếp tục phiên hiện có.
        List<Exam> sameSetAttempts = presetExamAttemptRepository
                .findByUserIdAndPresetExamSetIdInAndBankVersionOrderByStartedAtDesc(
                        user.userId(),
                        List.of(set.getId()),
                        CURRENT_BANK
                );

        for (Exam attempt : sameSetAttempts) {
            if (attempt.getStatus() != ExamStatus.IN_PROGRESS
                    && attempt.getStatus() != ExamStatus.PAUSED) {
                continue;
            }

            // Đồng bộ TIMEOUT nếu scheduler chưa kịp xử lý.
            ExamSessionResponse current = examService.getExamSession(
                    attempt.getId(),
                    user.userId()
            );

            if ("IN_PROGRESS".equals(current.status())
                    || "PAUSED".equals(current.status())) {
                if (!mode.equals(current.mode())) {
                    throw activeExamExists(
                            set.getLicenseClass(),
                            attempt,
                            current.status()
                    );
                }
                return current;
            }
        }

        // Unique index áp dụng cho user + hạng, không giới hạn bank version.
        // Bài random hoặc preset khác cũng phải được trả về trong lỗi 409.
        Optional<Exam> activeExam = examRepository
                .findFirstByUserIdAndExamTypeAndStatusInOrderByStartedAtDesc(
                        user.userId(),
                        set.getLicenseClass(),
                        List.of(ExamStatus.IN_PROGRESS, ExamStatus.PAUSED)
                );

        if (activeExam.isPresent()) {
            Exam active = activeExam.get();
            ExamSessionResponse current = examService.getExamSession(
                    active.getId(),
                    user.userId()
            );

            if ("IN_PROGRESS".equals(current.status())
                    || "PAUSED".equals(current.status())) {
                throw activeExamExists(
                        set.getLicenseClass(),
                        active,
                        current.status()
                );
            }
        }

        PresetExamSetValidator.ValidatedPreset validated =
                presetExamSetValidator.validate(set);

        List<Integer> questionIds = validated.questions()
                .stream()
                .map(Question::getId)
                .toList();

        Instant now = Instant.now();

        String fullName = request != null
                && request.fullName() != null
                && !request.fullName().isBlank()
                ? request.fullName()
                : user.username();

        Exam exam = Exam.builder()
                .userId(user.userId())
                .username(user.username())
                .fullName(fullName)
                .examType(set.getLicenseClass())
                .bankVersion(CURRENT_BANK)
                .presetExamSetId(set.getId())
                .criticalQuestionId(validated.criticalQuestionId())
                .questionIds(questionIds)
                .currentQuestionId(questionIds.get(0))
                .totalQuestions(set.getTotalQuestions())
                .durationMinutes(set.getDurationMinutes())
                .expiresAt(now.plus(
                        set.getDurationMinutes(),
                        ChronoUnit.MINUTES
                ))
                .mode(mode)
                .status(ExamStatus.IN_PROGRESS)
                .startedAt(now)
                .build();

        try {
            // Flush để unique index bắt request tạo bài cùng hạng.
            exam = examRepository.saveAndFlush(exam);
        } catch (DataIntegrityViolationException error) {
            if (isActiveIndexViolation(error)) {
                throw activeExamExists(set.getLicenseClass());
            }
            throw error;
        }

        for (Integer questionId : questionIds) {
            examAnswerRepository.save(
                    ExamAnswer.builder()
                            .exam(exam)
                            .questionId(questionId)
                            .build()
            );
        }

        return examService.getExamSession(
                exam.getId(),
                user.userId()
        );
    }

    private ActiveExamExistsException activeExamExists(String licenseClass) {
        return new ActiveExamExistsException(
                licenseClass,
                null,
                null,
                null
        );
    }

    private ActiveExamExistsException activeExamExists(
            String licenseClass,
            Exam active,
            String status
    ) {
        return new ActiveExamExistsException(
                licenseClass,
                active.getId(),
                active.getPresetExamSetId(),
                status
        );
    }

    private void lockUserLicense(UUID userId, String licenseClass) {
        long key = userId.getMostSignificantBits()
                ^ Long.rotateLeft(userId.getLeastSignificantBits(), 17)
                ^ Long.rotateLeft((long) licenseClass.hashCode(), 32);

        entityManager.unwrap(Session.class).doWork(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT pg_advisory_xact_lock(?)"
            )) {
                statement.setLong(1, key);
                statement.execute();
            }
        });
    }

    private boolean isActiveIndexViolation(Throwable error) {
        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException
                    constraint
                    && ACTIVE_INDEX.equals(constraint.getConstraintName())) {
                return true;
            }
            cause = cause.getCause();
        }

        return error.getMessage() != null
                && error.getMessage().contains(ACTIVE_INDEX);
    }

    private PresetExamSetListResponse.Item toItem(
            UUID userId,
            PresetExamSet set,
            List<Exam> attempts
    ) {
        Exam latest = attempts.isEmpty() ? null : attempts.get(0);
        ExamStatus latestStatus = latest == null ? null : latest.getStatus();

        if (latestStatus == ExamStatus.IN_PROGRESS
                && latest.getExpiresAt() != null
                && !Instant.now().isBefore(latest.getExpiresAt())) {
            ExamSessionResponse synchronizedSession =
                    examService.getExamSession(latest.getId(), userId);
            latestStatus = ExamStatus.valueOf(synchronizedSession.status());
        }

        ExamResultResponse latestResult = null;
        if (latestStatus == ExamStatus.COMPLETED
                || latestStatus == ExamStatus.TIMEOUT) {
            latestResult = examService.getExamResult(latest.getId(), userId);
        }

        Boolean latestPassed = latestResult == null
                ? null
                : latestResult.passed();

        String userStatus;
        if (latest == null) {
            userStatus = "NOT_STARTED";
        } else if (latestStatus == ExamStatus.IN_PROGRESS
                || latestStatus == ExamStatus.PAUSED) {
            userStatus = "IN_PROGRESS";
        } else if (Boolean.TRUE.equals(latestPassed)) {
            userStatus = "PASSED";
        } else {
            userStatus = "FAILED";
        }

        PresetExamSetListResponse.LatestAttempt latestAttempt =
                latest == null
                        ? null
                        : new PresetExamSetListResponse.LatestAttempt(
                                latest.getId(),
                                latestStatus.name(),
                                latestResult == null
                                        ? latest.getCorrectCount()
                                        : latestResult.correctCount(),
                                latest.getTotalQuestions(),
                                latestPassed,
                                latest.getStartedAt(),
                                latestResult == null
                                        ? latest.getCompletedAt()
                                        : latestResult.completedAt()
                        );

        return new PresetExamSetListResponse.Item(
                set.getId(),
                set.getCode(),
                set.getTitle(),
                set.getLicenseClass(),
                set.getTotalQuestions(),
                set.getDurationMinutes() * 60,
                set.getStatus().name(),
                set.getDisplayOrder(),
                userStatus,
                attempts.size(),
                latestAttempt
        );
    }

    private String normalizeLicenseClass(String licenseClass) {
        if (licenseClass == null || licenseClass.isBlank()) {
            throw BusinessException.badRequest(
                    "License class is required"
            );
        }

        String normalized = licenseClass
                .trim()
                .toUpperCase(Locale.ROOT);

        ExamMatrix.forClass(normalized);
        return normalized;
    }
}