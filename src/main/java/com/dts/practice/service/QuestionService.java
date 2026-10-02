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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private static final String CURRENT_BANK_VERSION = "dts-2026-600-v1";

    private static final Set<String> LICENSE_CLASSES = Set.of(
            "A1", "A", "B1", "B", "C1", "C", "D1", "D2", "D",
            "BE", "C1E", "CE", "D1E", "D2E", "DE"
    );

    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    @Cacheable(
            value = "questions",
            key = "'dts-2026-600-v1:chapter:' + #chapterId + ':license:' + "
                    + "(#licenseClass == null ? 'ALL' : #licenseClass.trim().toUpperCase())"
    )
    public List<QuestionResponse> getByChapter(
            Integer chapterId,
            String licenseClass
    ) {
        String license = normalizeLicenseClass(licenseClass);

        List<Question> questions = license == null
                ? questionRepository
                    .findByBankVersionAndChapterOrderBySourceQuestionIdAsc(
                            CURRENT_BANK_VERSION, chapterId
                    )
                : questionRepository
                    .findByBankVersionAndChapterAndLicenseClass(
                            CURRENT_BANK_VERSION, chapterId, license
                    );

        List<QuestionResponse> responses =
                questionMapper.toResponseList(questions);

        if (license == null) {
            return responses;
        }

        Set<Integer> criticalIds = questionRepository
                .findCriticalByBankVersionAndLicenseClass(
                        CURRENT_BANK_VERSION, license
                )
                .stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        return responses.stream()
                .map(response -> withCriticalFlag(
                        response,
                        criticalIds.contains(response.id())
                ))
                .toList();
    }

    public List<QuestionResponse> getByChapter(Integer chapterId) {
        return getByChapter(chapterId, null);
    }

    public QuestionResponse getById(Integer id, String licenseClass) {
        String license = normalizeLicenseClass(licenseClass);

        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                        BusinessException.notFound("Question not found: " + id)
                );

        QuestionResponse response = questionMapper.toResponse(question);

        if (license == null) {
            return response;
        }

        Boolean isCritical = questionRepository.findCriticalFlagForLicense(
                id,
                CURRENT_BANK_VERSION,
                license
        ).orElseThrow(() ->
                BusinessException.notFound("Question not found: " + id)
        );

        return withCriticalFlag(response, isCritical);
    }

    public QuestionResponse getById(Integer id) {
        return getById(id, null);
    }

    public List<QuestionResponse> getCriticalQuestions(String licenseClass) {
        String license = normalizeLicenseClass(licenseClass);

        List<Question> questions = license == null
                ? questionRepository
                    .findByBankVersionAndIsCriticalTrueOrderBySourceQuestionIdAsc(
                            CURRENT_BANK_VERSION
                    )
                : questionRepository
                    .findCriticalByBankVersionAndLicenseClass(
                            CURRENT_BANK_VERSION, license
                    );

        return questionMapper.toResponseList(questions);
    }

    public List<QuestionResponse> getCriticalQuestions() {
        return getCriticalQuestions(null);
    }

    public long countAll(String licenseClass) {
        String license = normalizeLicenseClass(licenseClass);

        return license == null
                ? questionRepository.countByBankVersion(CURRENT_BANK_VERSION)
                : questionRepository.countByBankVersionAndLicenseClass(
                        CURRENT_BANK_VERSION, license
                );
    }

    public long countAll() {
        return countAll(null);
    }

    public long countByChapter(Integer chapter, String licenseClass) {
        String license = normalizeLicenseClass(licenseClass);

        return license == null
                ? questionRepository.countByBankVersionAndChapter(
                        CURRENT_BANK_VERSION, chapter
                )
                : questionRepository
                    .countByBankVersionAndChapterAndLicenseClass(
                            CURRENT_BANK_VERSION, chapter, license
                    );
    }

    public long countByChapter(Integer chapter) {
        return countByChapter(chapter, null);
    }

    private QuestionResponse withCriticalFlag(
            QuestionResponse response,
            boolean isCritical
    ) {
        return new QuestionResponse(
                response.id(),
                response.chapter(),
                response.questionText(),
                response.options(),
                isCritical,
                response.imageUrl(),
                response.mediaFileId(),
                response.correctAnswer(),
                response.explanation()
        );
    }

    private String normalizeLicenseClass(String licenseClass) {
        if (licenseClass == null) {
            return null;
        }

        String normalized = licenseClass.trim().toUpperCase(Locale.ROOT);
        if (!LICENSE_CLASSES.contains(normalized)) {
            throw BusinessException.badRequest(
                    "Invalid license class: " + licenseClass
            );
        }

        return normalized;
    }
}