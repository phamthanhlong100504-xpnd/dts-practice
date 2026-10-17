
package com.dts.practice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "traffic_signs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrafficSign {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "bank_version", nullable = false, length = 64)
    private String bankVersion;

    @Column(name = "source_sign_id", nullable = false)
    private Integer sourceSignId;

    @Column(
        name = "content_builder_traffic_sign_id",
        nullable = false
    )
    private UUID contentBuilderTrafficSignId;

    @Column(name = "code", nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "meaning", nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "quick_tip", nullable = false, columnDefinition = "TEXT")
    private String quickTip;

    @Column(name = "source_image", nullable = false, length = 255)
    private String sourceImage;

    @Column(name = "media_file_id", nullable = false)
    private UUID mediaFileId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
        name = "applicable_licenses",
        nullable = false,
        columnDefinition = "jsonb"
    )
    private List<String> applicableLicenses;

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
