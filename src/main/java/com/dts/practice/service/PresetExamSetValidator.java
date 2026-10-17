package com.dts.practice.service;

import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.entity.PresetExamSetQuestion;
import com.dts.practice.entity.Question;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.PresetExamSetQuestionRepository;
import com.dts.practice.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresetExamSetValidator {

    private static final String CURRENT_BANK = CurrentQuestionBank.currentVersion();

    private final PresetExamSetQuestionRepository presetQuestionRepository;
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public ValidatedPreset validate(PresetExamSet set) {
        if (set.getStatus() != PresetExamSetStatus.PUBLISHED) {
            throw BusinessException.notFound("Preset exam set not found");
        }

        if (!CURRENT_BANK.equals(set.getBankVersion())) {
            throw invalid(set, "không thuộc bộ câu hỏi hiện hành");
        }

        ExamMatrix.Rule rule = ExamMatrix.forClass(
                set.getLicenseClass()
        );

        if (!Objects.equals(
                set.getTotalQuestions(),
                rule.totalQuestions()
        ) || !Objects.equals(
                set.getDurationMinutes(),
                rule.durationMinutes()
        )) {
            throw invalid(set, "số câu hoặc thời gian không đúng ma trận");
        }

        List<PresetExamSetQuestion> entries =
                presetQuestionRepository
                        .findByPresetExamSetIdOrderByQuestionOrderAsc(
                                set.getId()
                        );

        if (entries.size() != rule.totalQuestions()) {
            throw invalid(set, "không đủ số câu hỏi");
        }

        List<Integer> questionIds = new ArrayList<>();
        Set<Integer> uniqueIds = new HashSet<>();

        for (int index = 0; index < entries.size(); index++) {
            PresetExamSetQuestion entry = entries.get(index);

            if (!Objects.equals(
                    entry.getQuestionOrder(),
                    index + 1
            )) {
                throw invalid(set, "thứ tự câu hỏi không liên tục");
            }

            if (!uniqueIds.add(entry.getQuestionId())) {
                throw invalid(set, "có câu hỏi trùng lặp");
            }

            questionIds.add(entry.getQuestionId());
        }

        Map<Integer, Question> questionsById = questionRepository
                .findAllById(questionIds)
                .stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        question -> question
                ));

        List<Question> orderedQuestions = new ArrayList<>();
        Map<Integer, Integer> ordinaryByChapter = new HashMap<>();
        Integer criticalQuestionId = null;

        for (Integer questionId : questionIds) {
            Question question = questionsById.get(questionId);

            if (question == null
                    || !CURRENT_BANK.equals(
                            question.getBankVersion()
                    )
                    || question.getApplicableLicenses() == null
                    || !question.getApplicableLicenses()
                            .contains(set.getLicenseClass())) {
                throw invalid(
                        set,
                        "câu hỏi không thuộc phiên bản hoặc hạng bằng"
                );
            }

            boolean criticalForLicense =
                    question.getCriticalLicenses() != null
                            && question.getCriticalLicenses()
                                    .contains(set.getLicenseClass());

            if (criticalForLicense
                    && !Boolean.TRUE.equals(
                            question.getIsCritical()
                    )) {
                throw invalid(
                        set,
                        "nhãn câu điểm liệt không hợp lệ"
                );
            }

            if (criticalForLicense) {
                if (criticalQuestionId != null) {
                    throw invalid(set, "có nhiều hơn một câu điểm liệt");
                }

                criticalQuestionId = questionId;
            } else {
                ordinaryByChapter.merge(
                        question.getChapter(),
                        1,
                        Integer::sum
                );
            }

            orderedQuestions.add(question);
        }

        if (criticalQuestionId == null) {
            throw invalid(set, "thiếu câu điểm liệt");
        }

        if (!rule.ordinaryByChapter().equals(
                ordinaryByChapter
        )) {
            throw invalid(
                    set,
                    "số câu thường theo chương không đúng ma trận"
            );
        }

        return new ValidatedPreset(
                List.copyOf(orderedQuestions),
                criticalQuestionId
        );
    }

    private BusinessException invalid(
            PresetExamSet set,
            String reason
    ) {
        return BusinessException.conflict(
                "Bộ đề " + set.getCode() + ": " + reason
        );
    }

    public record ValidatedPreset(
            List<Question> questions,
            Integer criticalQuestionId
    ) {}
}