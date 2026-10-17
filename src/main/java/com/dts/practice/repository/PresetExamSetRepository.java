package com.dts.practice.repository;

import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.enums.PresetExamSetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PresetExamSetRepository
        extends JpaRepository<PresetExamSet, UUID> {

    List<PresetExamSet>
    findByLicenseClassAndBankVersionAndStatusOrderByDisplayOrderAscCodeAsc(
            String licenseClass,
            String bankVersion,
            PresetExamSetStatus status
    );
}