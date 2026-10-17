package com.dts.practice.service;

import com.dts.practice.dto.response.QuestionResponse;
import com.dts.practice.dto.response.WrongQuestionOverviewResponse;
import com.dts.practice.dto.response.WrongQuestionResponse;
import com.dts.practice.entity.Question;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.mapper.QuestionMapper;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WrongQuestionService {

    private static final String CURRENT_BANK_VERSION =
            CurrentQuestionBank.currentVersion();

    private final WrongQuestionProgressService wrongQuestionProgressService;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    public WrongQuestionOverviewResponse getOverview(
            UUID userId,
            String licenseClass
    ) {
        String license = normalizeLicenseClass(licenseClass);

        long total = wrongQuestionProgressService.countNeedsReview(
                userId,
                license,
                CURRENT_BANK_VERSION
        );

        return new WrongQuestionOverviewResponse(
                license,
                total,
                total > 0
        );
    }

    public List<WrongQuestionResponse> getWrongQuestions(
            UUID userId,
            String licenseClass
    ) {
        String license = normalizeLicenseClass(licenseClass);

        List<WrongQuestionProgressService.ReviewState> states =
                wrongQuestionProgressService.findNeedsReview(
                        userId,
                        license,
                        CURRENT_BANK_VERSION
                );

        if (states.isEmpty()) {
            return List.of();
        }

        List<Integer> questionIds = states.stream()
                .map(WrongQuestionProgressService.ReviewState::questionId)
                .toList();

        Map<Integer, Question> questionsById =
                questionRepository.findAllById(questionIds)
                        .stream()
                        .collect(Collectors.toMap(
                                Question::getId,
                                question -> question
                        ));

        return states.stream()
                .map(state -> {
                    Question question = questionsById.get(state.questionId());

                    if (question == null) {
                        throw BusinessException.notFound(
                                "Question not found: " + state.questionId()
                        );
                    }

                    QuestionResponse response =
                            questionMapper.toResponse(question);

                    boolean isCritical =
                            Boolean.TRUE.equals(question.getIsCritical())
                                    && question.getCriticalLicenses() != null
                                    && question.getCriticalLicenses()
                                            .contains(license);

                    return new WrongQuestionResponse(
                            question.getId(),
                            question.getSourceQuestionId(),
                            question.getChapter(),
                            response.questionText(),
                            response.options(),
                            state.previousWrongAnswer(),
                            response.correctAnswer(),
                            isCritical,
                            response.explanation(),
                            response.imageUrl(),
                            response.mediaFileId(),
                            state.lastWrongAt()
                    );
                })
                .toList();
    }

    private String normalizeLicenseClass(String licenseClass) {
        if (licenseClass == null || licenseClass.isBlank()) {
            throw BusinessException.badRequest("License class is required");
        }

        String normalized = licenseClass.trim().toUpperCase(Locale.ROOT);
        ExamMatrix.forClass(normalized);
        return normalized;
    }
}