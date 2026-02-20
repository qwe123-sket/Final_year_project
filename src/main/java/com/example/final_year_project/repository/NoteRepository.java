package com.example.final_year_project.repository;

import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.enums.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Page<Note> findByUserId(Long userId, Pageable pageable);

    Page<Note> findByStatus(NoteStatus status, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.status = :status AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    Page<Note> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") NoteStatus status, Pageable pageable);

    long countByUserId(Long userId);

    long countByStatus(NoteStatus status);

    /** 供推荐模块：根据 ID 列表按顺序返回 */
    @Query("SELECT n FROM Note n WHERE n.id IN :ids AND n.status = 'APPROVED'")
    List<Note> findByIdInAndApproved(@Param("ids") List<Long> ids);
}
