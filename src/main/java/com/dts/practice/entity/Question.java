package com.dts.practice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "practice_question_ids")
    @SequenceGenerator(
        name = "practice_question_ids",
        sequenceName = "practice_question_bank_id_seq",
        allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private Integer chapter;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "JSONB")
    private String options;

    @Column(name = "correct_answer", nullable = false, length = 1)
    private String correctAnswer;

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "bank_version", nullable = false, length = 64)
    private String bankVersion;

    @Column(name = "source_question_id", nullable = false)
    private Integer sourceQuestionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicable_licenses", columnDefinition = "JSONB")
    private List<String> applicableLicenses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "critical_licenses", columnDefinition = "JSONB")
    private List<String> criticalLicenses;

    @Column(name = "content_builder_question_id")
    private UUID contentBuilderQuestionId;

    @Column(name = "media_file_id")
    private UUID mediaFileId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}