package com.dts.practice.repository;

import com.dts.practice.entity.Exam;
import com.dts.practice.enums.ExamStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    Page<Exam> findByUserIdOrderByStartedAtDesc(
            UUID userId,
            Pageable pageable
    );

    List<Exam> findByUserIdAndStatus(
            UUID userId,
            ExamStatus status
    );

    Optional<Exam> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT e FROM Exam e
            WHERE e.id = :examId AND e.userId = :userId
            """)
    Optional<Exam> findByIdAndUserIdForUpdate(
            @Param("examId") UUID examId,
            @Param("userId") UUID userId
    );

    boolean existsByUserIdAndExamTypeAndStatusIn(
            UUID userId,
            String examType,
            Collection<ExamStatus> statuses
    );

    Optional<Exam>
    findFirstByUserIdAndExamTypeAndStatusInOrderByStartedAtDesc(
            UUID userId,
            String examType,
            Collection<ExamStatus> statuses
    );

    Optional<Exam>
    findFirstByUserIdAndExamTypeAndBankVersionAndStatusInOrderByStartedAtDesc(
            UUID userId,
            String examType,
            String bankVersion,
            Collection<ExamStatus> statuses
    );

    Optional<Exam>
    findFirstByUserIdAndExamTypeAndBankVersionAndStatusAndExpiresAtAfterOrderByStartedAtDesc(
            UUID userId,
            String examType,
            String bankVersion,
            ExamStatus status,
            Instant now
    );

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(
            UUID userId,
            ExamStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Exam> findByStatusAndExpiresAtBefore(
            ExamStatus status,
            Instant now
    );

    // Leaderboard: exams in mode=EXAM, completed since a date,
    // optionally filtered by examType.
    // Ordering: score desc -> correctCount desc -> completedAt asc.
    @Query("SELECT e FROM Exam e WHERE e.status = :status AND e.mode = :mode " +
            "AND (:examType IS NULL OR e.examType = :examType) AND e.completedAt > :since " +
            "ORDER BY e.score DESC NULLS LAST, e.correctCount DESC NULLS LAST, e.completedAt ASC NULLS LAST")
    List<Exam> findLeaderboard(
            @Param("status") ExamStatus status,
            @Param("mode") String mode,
            @Param("examType") String examType,
            @Param("since") Instant since,
            Pageable pageable
    );
}