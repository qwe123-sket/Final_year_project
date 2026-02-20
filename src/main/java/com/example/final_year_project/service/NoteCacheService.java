package com.example.final_year_project.service;

import com.example.final_year_project.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 笔记浏览量计数（Redis），定期同步回 DB
 */
@Service
@RequiredArgsConstructor
public class NoteCacheService {

    private static final String VIEW_COUNT_KEY = "note:view:";
    private static final long VIEW_COUNT_SYNC_THRESHOLD = 10;

    private final RedisTemplate<String, Long> redisTemplateLong;
    private final NoteRepository noteRepository;

    public void incrementViewCount(Long noteId) {
        String key = VIEW_COUNT_KEY + noteId;
        Long count = redisTemplateLong.opsForValue().increment(key);
        if (count != null && count % VIEW_COUNT_SYNC_THRESHOLD == 0) {
            syncViewCountToDb(noteId, count);
        }
    }

    public Long getViewCount(Long noteId) {
        String key = VIEW_COUNT_KEY + noteId;
        Long cached = redisTemplateLong.opsForValue().get(key);
        if (cached != null) return cached;
        return noteRepository.findById(noteId).map(n -> n.getViewCount()).orElse(0L);
    }

    public void evictNote(Long noteId) {
        redisTemplateLong.delete(VIEW_COUNT_KEY + noteId);
    }

    private void syncViewCountToDb(Long noteId, long redisCount) {
        noteRepository.findById(noteId).ifPresent(n -> {
            n.setViewCount(Math.max(n.getViewCount(), redisCount));
            noteRepository.save(n);
        });
    }
}
