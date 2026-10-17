package com.dts.practice.repository;

import com.dts.practice.entity.CriticalQuestionProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CriticalQuestionProgressRepository
        extends JpaRepository<CriticalQuestionProgress, UUID> {

    List<CriticalQuestionProgress>
    findByUserIdAndLicenseClassAndBankVersion(
            UUID userId,
            String licenseClass,
            String bankVersion
    );

    @Modifying
    @Query(
        value = """
            INSERT INTO critical_question_progress (
                id, user_id, license_class, bank_version,
                question_id, learned, last_studied_at,
                created_at, updated_at
            )
            VALUES (
                gen_random_uuid(), :userId, :licenseClass, :bankVersion,
                :questionId, :learned, NOW(), NOW(), NOW()
            )
            ON CONFLICT ON CONSTRAINT uq_critical_question_progress_scope
            DO UPDATE SET
                learned = EXCLUDED.learned,
                last_studied_at = EXCLUDED.last_studied_at,
                updated_at = NOW()
            """,
        nativeQuery = true
    )
    int upsert(
            @Param("userId") UUID userId,
            @Param("licenseClass") String licenseClass,
            @Param("bankVersion") String bankVersion,
            @Param("questionId") Integer questionId,
            @Param("learned") boolean learned
    );
}