package com.dts.practice.repository;

import com.dts.practice.entity.Exam;
import com.dts.practice.enums.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    Page<Exam> findByUserIdOrderByStartedAtDesc(UUID userId, Pageable pageable);

    List<Exam> findByUserIdAndStatus(UUID userId, ExamStatus status);

    Optional<Exam> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, ExamStatus status);
}
