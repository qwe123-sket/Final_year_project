package com.example.final_year_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 用户收藏
 */
@Entity
@Table(name = "favorite", indexes = {
    @Index(name = "idx_favorite_user_id", columnList = "user_id"),
    @Index(name = "idx_favorite_note_id", columnList = "note_id"),
    @Index(name = "uk_favorite_user_note", columnList = "user_id, note_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
