package com.example.final_year_project.service;

import com.example.final_year_project.dto.browse.BrowseRecordRequest;
import com.example.final_year_project.entity.BrowseRecord;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.BrowseRecordRepository;
import com.example.final_year_project.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 浏览记录服务，主要是给推荐算法提供数据基础。
 * 记录用户看了哪些笔记、看了多久。
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
        var existing = browseRecordRepository.findByUserIdAndNoteId(userId, req.getNoteId());

        if (existing.isPresent()) {
            // 已有记录就累加时长
            var r = existing.get();
            r.setBrowseDurationSeconds(r.getBrowseDurationSeconds() + req.getBrowseDurationSeconds());
            r.setLastBrowseAt(now);
            r.setUpdatedAt(now);
            browseRecordRepository.save(r);
        } else {
            var r = BrowseRecord.builder()
                    .userId(userId)
                    .noteId(req.getNoteId())
                    .browseDurationSeconds(req.getBrowseDurationSeconds())
                    .lastBrowseAt(now)
                    .build();
            browseRecordRepository.save(r);
        }
    }
}
