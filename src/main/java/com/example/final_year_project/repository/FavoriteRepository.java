package com.example.final_year_project.repository;

import com.example.final_year_project.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndNoteId(Long userId, Long noteId);

    boolean existsByUserIdAndNoteId(Long userId, Long noteId);

    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    void deleteByUserIdAndNoteId(Long userId, Long noteId);

    long countByUserId(Long userId);

    long countByNoteId(Long noteId);

    @Query("SELECT f.noteId, COUNT(f) FROM Favorite f WHERE f.noteId IN :noteIds GROUP BY f.noteId")
    List<Object[]> countByNoteIds(@Param("noteIds") List<Long> noteIds);

    @Query("SELECT f.noteId FROM Favorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Long> findNoteIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.userId FROM Favorite f WHERE f.noteId IN :noteIds AND f.userId <> :excludeUserId")
    List<Long> findUserIdsByNoteIds(@Param("noteIds") List<Long> noteIds, @Param("excludeUserId") Long excludeUserId);

    @Query("SELECT f.noteId FROM Favorite f WHERE f.userId IN :userIds AND f.noteId NOT IN :excludeNoteIds")
    List<Long> findNoteIdsByUserIds(@Param("userIds") List<Long> userIds, @Param("excludeNoteIds") List<Long> excludeNoteIds);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.noteId IN (SELECT n.id FROM Note n WHERE n.userId = :userId)")
    long countFavoritesOnUserNotes(@Param("userId") Long userId);
}
