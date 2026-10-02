package com.dts.practice.repository;

import com.dts.practice.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    // Giữ các truy vấn cũ để không ảnh hưởng những nơi còn sử dụng chúng.
    List<Question> findByChapter(Integer chapter);

    List<Question> findByIsCriticalTrue();

    @Query(
        value = "SELECT * FROM questions "
              + "WHERE is_critical = TRUE ORDER BY RANDOM() LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomCriticalQuestions(@Param("limit") int limit);

    @Query(
        value = "SELECT * FROM questions "
              + "WHERE chapter = :chapter AND is_critical = FALSE "
              + "ORDER BY RANDOM() LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomNonCriticalByChapter(
            @Param("chapter") Integer chapter,
            @Param("limit") int limit
    );

    @Query(
        value = "SELECT * FROM questions "
              + "WHERE is_critical = FALSE ORDER BY RANDOM() LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomNonCritical(@Param("limit") int limit);

    long countByChapter(Integer chapter);

    long countByIsCriticalTrue();

    List<Question> findByBankVersionAndChapterOrderBySourceQuestionIdAsc(
            String bankVersion,
            Integer chapter
    );

    List<Question> findByBankVersionAndIsCriticalTrueOrderBySourceQuestionIdAsc(
            String bankVersion
    );

    long countByBankVersion(String bankVersion);

    long countByBankVersionAndChapter(
            String bankVersion,
            Integer chapter
    );

    @Query(
        value = "SELECT * FROM questions "
              + "WHERE bank_version = :bankVersion "
              + "AND chapter = :chapter "
              + "AND jsonb_exists(applicable_licenses, :licenseClass) "
              + "ORDER BY source_question_id",
        nativeQuery = true
    )
    List<Question> findByBankVersionAndChapterAndLicenseClass(
            @Param("bankVersion") String bankVersion,
            @Param("chapter") Integer chapter,
            @Param("licenseClass") String licenseClass
    );

    @Query(
        value = "SELECT * FROM questions "
              + "WHERE bank_version = :bankVersion "
              + "AND is_critical = TRUE "
              + "AND jsonb_exists(critical_licenses, :licenseClass) "
              + "ORDER BY source_question_id",
        nativeQuery = true
    )
    List<Question> findCriticalByBankVersionAndLicenseClass(
            @Param("bankVersion") String bankVersion,
            @Param("licenseClass") String licenseClass
    );

    // Không có dòng kết quả nếu câu không thuộc bộ hiện hành hoặc hạng đã chọn.
    // Có dòng kết quả với giá trị false nếu câu thuộc hạng nhưng không phải điểm liệt.
    @Query(
        value = "SELECT COALESCE("
              + "q.is_critical AND jsonb_exists(q.critical_licenses, :licenseClass), "
              + "FALSE) "
              + "FROM questions q "
              + "WHERE q.id = :questionId "
              + "AND q.bank_version = :bankVersion "
              + "AND jsonb_exists(q.applicable_licenses, :licenseClass)",
        nativeQuery = true
    )
    Optional<Boolean> findCriticalFlagForLicense(
            @Param("questionId") Integer questionId,
            @Param("bankVersion") String bankVersion,
            @Param("licenseClass") String licenseClass
    );

    @Query(
        value = "SELECT COUNT(*) FROM questions "
              + "WHERE bank_version = :bankVersion "
              + "AND jsonb_exists(applicable_licenses, :licenseClass)",
        nativeQuery = true
    )
    long countByBankVersionAndLicenseClass(
            @Param("bankVersion") String bankVersion,
            @Param("licenseClass") String licenseClass
    );

    @Query(
        value = "SELECT COUNT(*) FROM questions "
              + "WHERE bank_version = :bankVersion "
              + "AND chapter = :chapter "
              + "AND jsonb_exists(applicable_licenses, :licenseClass)",
        nativeQuery = true
    )
    long countByBankVersionAndChapterAndLicenseClass(
            @Param("bankVersion") String bankVersion,
            @Param("chapter") Integer chapter,
            @Param("licenseClass") String licenseClass
    );

    @Query(
        value = "SELECT * FROM questions "
            + "WHERE bank_version = :bankVersion "
            + "AND jsonb_exists(applicable_licenses, :licenseClass) "
            + "AND is_critical = TRUE "
            + "AND jsonb_exists(critical_licenses, :licenseClass) "
            + "ORDER BY RANDOM() LIMIT 1",
        nativeQuery = true
    )
    List<Question> findRandomExamCritical(
            @Param("bankVersion") String bankVersion,
            @Param("licenseClass") String licenseClass
    );

    @Query(
        value = "SELECT * FROM questions "
            + "WHERE bank_version = :bankVersion "
            + "AND chapter = :chapter "
            + "AND jsonb_exists(applicable_licenses, :licenseClass) "
            + "AND NOT jsonb_exists("
            + "COALESCE(critical_licenses, '[]'::jsonb), :licenseClass"
            + ") "
            + "ORDER BY RANDOM() LIMIT :limit",
        nativeQuery = true
    )
    List<Question> findRandomExamOrdinary(
            @Param("bankVersion") String bankVersion,
            @Param("licenseClass") String licenseClass,
            @Param("chapter") int chapter,
            @Param("limit") int limit
    );
}