
package com.dts.practice.repository;

import com.dts.practice.entity.MemoryTip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemoryTipRepository
        extends JpaRepository<MemoryTip, UUID> {

    boolean existsByBankVersion(String bankVersion);

    long countByBankVersion(String bankVersion);

    long countByBankVersionAndStatus(
            String bankVersion,
            String status
    );

    Optional<MemoryTip> findByBankVersionAndSourceTipId(
            String bankVersion,
            Integer sourceTipId
    );

    boolean existsByContentBuilderMemoryTipId(
            UUID contentBuilderMemoryTipId
    );

    List<MemoryTip>
    findByBankVersionAndStatusOrderBySortOrderAsc(
            String bankVersion,
            String status
    );
}
