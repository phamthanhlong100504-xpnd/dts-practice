package com.dts.practice.repository;

import com.dts.practice.entity.Exam;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PresetExamAttemptRepository extends Repository<Exam, UUID> {

    List<Exam>
    findByUserIdAndPresetExamSetIdInAndBankVersionOrderByStartedAtDesc(
            UUID userId,
            Collection<UUID> presetExamSetIds,
            String bankVersion
    );
}