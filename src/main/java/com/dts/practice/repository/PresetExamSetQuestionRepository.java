package com.dts.practice.repository;

import com.dts.practice.entity.PresetExamSetQuestion;
import com.dts.practice.entity.PresetExamSetQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PresetExamSetQuestionRepository
        extends JpaRepository<PresetExamSetQuestion, PresetExamSetQuestionId> {

    List<PresetExamSetQuestion>
    findByPresetExamSetIdOrderByQuestionOrderAsc(UUID presetExamSetId);
}