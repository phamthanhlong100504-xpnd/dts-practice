package com.dts.practice.repository;

import com.dts.practice.entity.PresetExamSet;
import com.dts.practice.enums.PresetExamSetStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PresetExamSetRepository
        extends JpaRepository<PresetExamSet, UUID> {

    default List<PresetExamSet> findByLicenseClassAndBankVersionAndStatusOrderByDisplayOrderAscCodeAsc(
            String licenseClass, String bankVersion, PresetExamSetStatus status) {
        return findActivePublishedVersions(licenseClass,bankVersion,status.name());
    }

    @Query(value = "SELECT p.* FROM preset_exam_sets p " +
            "JOIN admin_preset_active_versions a ON a.logical_set_id=p.logical_set_id " +
            "AND a.active_version_id=p.id " +
            "WHERE p.license_class=:licenseClass AND p.bank_version=:bankVersion " +
            "AND p.status=:status ORDER BY p.display_order ASC,p.code ASC", nativeQuery=true)
    List<PresetExamSet>
    findActivePublishedVersions(
            @Param("licenseClass") String licenseClass,
            @Param("bankVersion") String bankVersion,
            @Param("status") String status
    );
}