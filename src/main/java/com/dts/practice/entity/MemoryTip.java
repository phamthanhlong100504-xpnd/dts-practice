
package com.dts.practice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "memory_tips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemoryTip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "bank_version", nullable = false, length = 64)
    private String bankVersion;

    @Column(name = "source_tip_id", nullable = false)
    private Integer sourceTipId;

    @Column(
        name = "content_builder_memory_tip_id",
        nullable = false
    )
    private UUID contentBuilderMemoryTipId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "quick_rule", nullable = false, columnDefinition = "TEXT")
    private String quickRule;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
