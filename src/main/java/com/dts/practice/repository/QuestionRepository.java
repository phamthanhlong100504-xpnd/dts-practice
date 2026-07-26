package com.dts.practice.repository;

import com.dts.practice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findByChapter(Integer chapter);

    List<Question> findByIsCriticalTrue();

    @Query(value = "SELECT * FROM questions WHERE is_critical = TRUE ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomCriticalQuestions(@Param("limit") int limit);

    @Query(value = "SELECT * FROM questions WHERE chapter = :chapter AND is_critical = FALSE ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomNonCriticalByChapter(@Param("chapter") Integer chapter, @Param("limit") int limit);

    @Query(value = "SELECT * FROM questions WHERE is_critical = FALSE ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Question> findRandomNonCritical(@Param("limit") int limit);

    long countByChapter(Integer chapter);

    long countByIsCriticalTrue();
}
