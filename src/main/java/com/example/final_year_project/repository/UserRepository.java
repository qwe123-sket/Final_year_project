package com.example.final_year_project.repository;

import com.example.final_year_project.entity.User;
import com.example.final_year_project.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE u.status = :status
              AND (u.username LIKE %:keyword% OR u.nickname LIKE %:keyword%)
            ORDER BY u.createdAt DESC
            """)
    Page<User> searchPublicUsers(
            @Param("keyword") String keyword,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}
