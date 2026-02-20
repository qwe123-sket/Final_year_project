package com.example.final_year_project.entity;

import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * 用户实体
 */
@Entity
@Table(name = "sys_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(unique = true, length = 128)
    private String email;

    @Column(length = 64)
    private String nickname;

    @Column(length = 256)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private UserStatus status = UserStatus.NORMAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
