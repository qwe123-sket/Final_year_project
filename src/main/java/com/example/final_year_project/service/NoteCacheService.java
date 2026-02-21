package com.example.final_year_project.service;

import com.example.final_year_project.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 用 Redis 做笔记浏览量计数，到一定阈值后同步回数据库。
 * 这样可以减少频繁写库的压力。
 */
@Service
@RequiredArgsConstructor
public class NoteCacheService {

    private static final String VIEW_KEY_PREFIX = "note:view:";
    // 每累计 10 次写一次 DB，这个数可以根据实际流量调
    private static final long SYNC_THRESHOLD = 10;

    private final RedisTemplate<String, Long> redisTemplateLong;
    private final NoteRepository noteRepository;

    public void incrementViewCount(Long noteId) {
        String key = VIEW_KEY_PREFIX + noteId;
        Long cnt = redisTemplateLong.opsForValue().increment(key);
        if (cnt != null && cnt % SYNC_THRESHOLD == 0) {
            syncToDb(noteId, cnt);
        }
    }

    public Long getViewCount(Long noteId) {
        String key = VIEW_KEY_PREFIX + noteId;
        Long cached = redisTemplateLong.opsForValue().get(key);
        if (cached != null) return cached;
        // 回源
        return noteRepository.findById(noteId).map(n -> n.getViewCount()).orElse(0L);
    }

    public void evictNote(Long noteId) {
        redisTemplateLong.delete(VIEW_KEY_PREFIX + noteId);
    }

    private void syncToDb(Long noteId, long redisCount) {
        noteRepository.findById(noteId).ifPresent(n -> {
            // 取较大的值，防止并发写覆盖
            n.setViewCount(Math.max(n.getViewCount(), redisCount));
            noteRepository.save(n);
        });
    }
}
