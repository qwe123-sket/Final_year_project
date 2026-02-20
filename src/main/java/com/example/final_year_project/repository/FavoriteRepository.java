package com.example.final_year_project.repository;

import com.example.final_year_project.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndNoteId(Long userId, Long noteId);

    boolean existsByUserIdAndNoteId(Long userId, Long noteId);

    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndNoteId(Long userId, Long noteId);
}
