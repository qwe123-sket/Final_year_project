package com.example.final_year_project.repository;

import com.example.final_year_project.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    Page<Reply> findByNoteIdOrderByCreatedAtDesc(Long noteId, Pageable pageable);

    long countByNoteId(Long noteId);
}
