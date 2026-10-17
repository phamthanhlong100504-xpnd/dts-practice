
package com.dts.practice.repository;

import com.dts.practice.entity.TrafficSign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrafficSignRepository
        extends JpaRepository<TrafficSign, UUID> {

    boolean existsByBankVersion(String bankVersion);

    long countByBankVersion(String bankVersion);

    long countByBankVersionAndStatus(
            String bankVersion,
            String status
    );

    Optional<TrafficSign> findByBankVersionAndSourceSignId(
            String bankVersion,
            Integer sourceSignId
    );

    boolean existsByContentBuilderTrafficSignId(
            UUID contentBuilderTrafficSignId
    );

    List<TrafficSign>
    findByBankVersionAndStatusOrderByCategoryAscSourceSignIdAsc(
            String bankVersion,
            String status
    );
}
