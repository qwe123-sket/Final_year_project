package com.example.final_year_project.repository;

import com.example.final_year_project.entity.BrowseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrowseRecordRepository extends JpaRepository<BrowseRecord, Long> {

    Optional<BrowseRecord> findByUserIdAndNoteId(Long userId, Long noteId);

    Page<BrowseRecord> findByUserIdOrderByLastBrowseAtDesc(Long userId, Pageable pageable);
}
