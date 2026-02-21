package com.example.final_year_project.repository;

import com.example.final_year_project.entity.NoteTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteTagRepository extends JpaRepository<NoteTag, Long> {

    List<NoteTag> findByNoteId(Long noteId);

    List<NoteTag> findByNoteIdIn(List<Long> noteIds);

    void deleteByNoteId(Long noteId);

    @Query("SELECT nt.tagId, COUNT(nt) as cnt FROM NoteTag nt GROUP BY nt.tagId ORDER BY cnt DESC")
    List<Object[]> findHotTags();

    @Query("SELECT DISTINCT nt.noteId FROM NoteTag nt WHERE nt.tagId IN :tagIds AND nt.noteId <> :excludeNoteId")
    List<Long> findNoteIdsByTagIdsExcluding(@Param("tagIds") List<Long> tagIds, @Param("excludeNoteId") Long excludeNoteId);

    @Query("SELECT DISTINCT nt.noteId FROM NoteTag nt WHERE nt.tagId IN :tagIds")
    List<Long> findNoteIdsByTagIds(@Param("tagIds") List<Long> tagIds);
}
