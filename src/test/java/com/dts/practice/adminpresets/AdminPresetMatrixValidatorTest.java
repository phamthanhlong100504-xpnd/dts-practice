package com.dts.practice.adminpresets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.entity.PresetExamSetQuestion;
import com.dts.practice.enums.PresetExamSetStatus;
import com.dts.practice.exception.BusinessException;
import com.dts.practice.repository.PresetExamSetQuestionRepository;
import com.dts.practice.repository.QuestionRepository;
import com.dts.practice.service.ExamMatrix;
import com.dts.practice.service.CurrentQuestionBank;
import com.dts.practice.service.PresetExamSetValidator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AdminPresetMatrixValidatorTest {
    @Test void draftValidatorReusesPublishedRuleButNeverAcceptsIncompleteContent() {
        var presetRepo=mock(PresetExamSetQuestionRepository.class);
        var questionRepo=mock(QuestionRepository.class);
        var validator=new PresetExamSetValidator(presetRepo,questionRepo);
        var bank=CurrentQuestionBank.currentVersion();
        var draft=PresetExamSet.builder().id(UUID.randomUUID()).code("SET-A1")
            .licenseClass("A1").bankVersion(bank).totalQuestions(25)
            .durationMinutes(19).status(PresetExamSetStatus.DRAFT).build();
        assertThrows(BusinessException.class,()->validator.validate(draft));
        assertThrows(BusinessException.class,()->validator.validateDraft(draft,List.of()));
        verifyNoInteractions(questionRepo);
        assertEquals(25,ExamMatrix.forClass("A1").totalQuestions());
        assertEquals(1+ExamMatrix.forClass("A1").ordinaryByChapter().values().stream()
            .mapToInt(Integer::intValue).sum(),25);
    }
    @Test void aPublishedSetCannotBypassDraftMethod() {
        var validator=new PresetExamSetValidator(mock(PresetExamSetQuestionRepository.class),mock(QuestionRepository.class));
        var published=PresetExamSet.builder().status(PresetExamSetStatus.PUBLISHED).build();
        assertThrows(BusinessException.class,()->validator.validateDraft(published,List.<PresetExamSetQuestion>of()));
    }
}
