package com.example.final_year_project.repository;

import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.enums.NoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    Page<Note> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.status = 'APPROVED' AND n.userId = :userId")
    Page<Note> findApprovedByUserId(@Param("userId") Long userId, Pageable pageable);

    Page<Note> findByStatus(NoteStatus status, Pageable pageable);

    @Query("SELECT n FROM Note n WHERE n.status = :status AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    Page<Note> searchByKeywordAndStatus(@Param("keyword") String keyword, @Param("status") NoteStatus status, Pageable pageable);

    /**
     * 更偏工程可用的搜索：避免对大字段 content 做全量 LIKE 扫描。
     * 只在 title 或 content 前缀区域中查找关键词，以提升在大数据集下的响应速度。
     */
    @Query("SELECT n FROM Note n WHERE n.status = :status AND (n.title LIKE %:keyword% OR SUBSTRING(n.content, 1, 500) LIKE %:keyword%)")
    Page<Note> searchByKeywordAndStatusFast(@Param("keyword") String keyword, @Param("status") NoteStatus status, Pageable pageable);

    long countByUserId(Long userId);

    long countByStatus(NoteStatus status);

    @Query("SELECT n FROM Note n WHERE n.id IN :ids AND n.status = 'APPROVED'")
    List<Note> findByIdInAndApproved(@Param("ids") List<Long> ids);

    @Query("SELECT n FROM Note n WHERE n.status = 'APPROVED' AND n.createdAt > :since")
    List<Note> findApprovedSince(@Param("since") Instant since);

    @Query("SELECT n FROM Note n WHERE n.status = 'APPROVED' AND n.id NOT IN :excludeIds ORDER BY n.viewCount DESC")
    List<Note> findPopularExcluding(@Param("excludeIds") List<Long> excludeIds, Pageable pageable);

    @Query("SELECT COALESCE(SUM(n.viewCount), 0) FROM Note n WHERE n.userId = :userId")
    long sumViewCountByUserId(@Param("userId") Long userId);

    List<Note> findByUserIdAndCreatedAtAfter(Long userId, Instant since);
}
