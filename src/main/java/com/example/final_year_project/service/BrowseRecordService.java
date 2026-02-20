package com.example.final_year_project.service;

import com.example.final_year_project.dto.browse.BrowseRecordRequest;
import com.example.final_year_project.entity.BrowseRecord;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.BrowseRecordRepository;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 浏览记录（供推荐算法使用）
 */
@Service
@RequiredArgsConstructor
public class BrowseRecordService {

    private final BrowseRecordRepository browseRecordRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public void record(Long userId, BrowseRecordRequest req) {
        if (noteRepository.findById(req.getNoteId()).isEmpty()) {
            throw new BusinessException("Note not found");
        }
        Instant now = Instant.now();
        browseRecordRepository.findByUserIdAndNoteId(userId, req.getNoteId())
                .ifPresentOrElse(
                        r -> {
                            r.setBrowseDurationSeconds(r.getBrowseDurationSeconds() + req.getBrowseDurationSeconds());
                            r.setLastBrowseAt(now);
                            r.setUpdatedAt(now);
                            browseRecordRepository.save(r);
                        },
                        () -> {
                            BrowseRecord r = BrowseRecord.builder()
                                    .userId(userId)
                                    .noteId(req.getNoteId())
                                    .browseDurationSeconds(req.getBrowseDurationSeconds())
                                    .lastBrowseAt(now)
                                    .build();
                            browseRecordRepository.save(r);
                        }
                );
    }
}
