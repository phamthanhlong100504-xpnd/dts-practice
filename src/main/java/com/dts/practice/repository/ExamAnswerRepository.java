package com.dts.practice.repository;

import com.dts.practice.entity.ExamAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExamAnswerRepository extends JpaRepository<ExamAnswer, UUID> {

    List<ExamAnswer> findByExamIdOrderByQuestionId(UUID examId);

    long countByExamIdAndIsCorrectTrue(UUID examId);

    long countByExamIdAndIsCorrectFalse(UUID examId);

    void deleteByExamId(UUID examId);
}
