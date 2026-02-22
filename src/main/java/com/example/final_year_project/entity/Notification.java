package com.example.final_year_project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_notif_user_id", columnList = "user_id"),
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(nullable = false, length = 512)
    private String content;

    @Column(name = "related_note_id")
    private Long relatedNoteId;

    @Column(name = "from_user_id")
    private Long fromUserId;

    @Column(name = "from_username", length = 64)
    private String fromUsername;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
