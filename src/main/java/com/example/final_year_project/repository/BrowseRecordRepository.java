package com.example.final_year_project.repository;

import com.example.final_year_project.entity.BrowseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BrowseRecordRepository extends JpaRepository<BrowseRecord, Long> {

    Optional<BrowseRecord> findByUserIdAndNoteId(Long userId, Long noteId);

    Page<BrowseRecord> findByUserIdOrderByLastBrowseAtDesc(Long userId, Pageable pageable);

    @Query("SELECT br.noteId FROM BrowseRecord br WHERE br.userId = :userId ORDER BY br.browseDurationSeconds DESC, br.lastBrowseAt DESC")
    List<Long> findNoteIdsByUserIdOrderByEngagement(@Param("userId") Long userId);

    @Query("SELECT br.userId FROM BrowseRecord br WHERE br.noteId IN :noteIds AND br.userId <> :excludeUserId AND br.browseDurationSeconds > 10")
    List<Long> findEngagedUsersByNoteIds(@Param("noteIds") List<Long> noteIds, @Param("excludeUserId") Long excludeUserId);
}
