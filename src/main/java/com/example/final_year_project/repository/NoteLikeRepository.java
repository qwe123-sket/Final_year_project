package com.example.final_year_project.repository;

import com.example.final_year_project.entity.NoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteLikeRepository extends JpaRepository<NoteLike, Long> {

    Optional<NoteLike> findByUserIdAndNoteId(Long userId, Long noteId);

    boolean existsByUserIdAndNoteId(Long userId, Long noteId);

    void deleteByUserIdAndNoteId(Long userId, Long noteId);

    long countByNoteId(Long noteId);

    @Query("SELECT nl.noteId, COUNT(nl) FROM NoteLike nl WHERE nl.noteId IN :noteIds GROUP BY nl.noteId")
    List<Object[]> countByNoteIds(@Param("noteIds") List<Long> noteIds);

    @Query("SELECT nl.noteId FROM NoteLike nl WHERE nl.userId = :userId AND nl.noteId IN :noteIds")
    List<Long> findLikedNoteIds(@Param("userId") Long userId, @Param("noteIds") List<Long> noteIds);

    @Query("SELECT COUNT(nl) FROM NoteLike nl WHERE nl.noteId IN (SELECT n.id FROM Note n WHERE n.userId = :userId)")
    long countLikesOnUserNotes(@Param("userId") Long userId);
}
