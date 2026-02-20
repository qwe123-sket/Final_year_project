package com.example.final_year_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 笔记回复
 */
@Entity
@Table(name = "reply", indexes = {
    @Index(name = "idx_reply_note_id", columnList = "note_id"),
    @Index(name = "idx_reply_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
