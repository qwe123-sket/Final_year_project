package com.example.final_year_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 浏览记录（供推荐算法使用）
 */
@Entity
@Table(name = "browse_record", indexes = {
    @Index(name = "idx_browse_user_id", columnList = "user_id"),
    @Index(name = "idx_browse_note_id", columnList = "note_id"),
    @Index(name = "idx_browse_last_at", columnList = "last_browse_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrowseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    /** 本次/累计浏览时长（秒） */
    @Column(name = "browse_duration_seconds", nullable = false)
    @Builder.Default
    private Long browseDurationSeconds = 0L;

    @Column(name = "last_browse_at", nullable = false)
    private Instant lastBrowseAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (lastBrowseAt == null) lastBrowseAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
