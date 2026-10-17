package com.dts.practice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "critical_question_progress")
@Getter
@Setter
@NoArgsConstructor
public class CriticalQuestionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "license_class", nullable = false, length = 10)
    private String licenseClass;

    @Column(name = "bank_version", nullable = false, length = 64)
    private String bankVersion;

    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "learned", nullable = false)
    private boolean learned;

    @Column(name = "last_studied_at", nullable = false)
    private Instant lastStudiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}