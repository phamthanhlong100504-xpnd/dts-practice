package com.dts.practice.repository;

import com.dts.practice.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, UUID> {

    List<ExamAnswer> findByExamIdOrderByQuestionId(UUID examId);

    long countByExamIdAndIsCorrectTrue(UUID examId);

    long countByExamIdAndIsCorrectFalse(UUID examId);

    void deleteByExamId(UUID examId);

    @Query(
        value = """
            SELECT COUNT(DISTINCT a.question_id)
            FROM exam_answers a
            JOIN exams e ON e.id = a.exam_id
            JOIN questions q ON q.id = a.question_id
            WHERE e.user_id = :userId
              AND e.exam_type = :licenseClass
              AND e.bank_version = :bankVersion
              AND e.status IN ('COMPLETED', 'TIMEOUT')
              AND a.is_correct = FALSE
              AND q.bank_version = :bankVersion
              AND jsonb_exists(q.applicable_licenses, :licenseClass)
            """,
        nativeQuery = true
    )
    long countDistinctWrongQuestions(
            @Param("userId") UUID userId,
            @Param("licenseClass") String licenseClass,
            @Param("bankVersion") String bankVersion
    );

    @Query(
        value = """
            SELECT DISTINCT ON (a.question_id) a.*
            FROM exam_answers a
            JOIN exams e ON e.id = a.exam_id
            JOIN questions q ON q.id = a.question_id
            WHERE e.user_id = :userId
              AND e.exam_type = :licenseClass
              AND e.bank_version = :bankVersion
              AND e.status IN ('COMPLETED', 'TIMEOUT')
              AND a.is_correct = FALSE
              AND q.bank_version = :bankVersion
              AND jsonb_exists(q.applicable_licenses, :licenseClass)
            ORDER BY a.question_id,
                     a.answered_at DESC NULLS LAST,
                     a.id DESC
            """,
        nativeQuery = true
    )
    List<ExamAnswer> findLatestWrongAnswers(
            @Param("userId") UUID userId,
            @Param("licenseClass") String licenseClass,
            @Param("bankVersion") String bankVersion
    );
}