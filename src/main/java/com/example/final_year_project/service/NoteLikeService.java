package com.example.final_year_project.service;

import com.example.final_year_project.entity.NoteLike;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.NoteLikeRepository;
import com.example.final_year_project.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteLikeService {

    private static final String LIKE_COUNT_KEY = "note:like:";

    private final NoteLikeRepository likeRepo;
    private final NoteRepository noteRepo;
    private final RedisTemplate<String, Long> redisTemplateLong;

    @Transactional
    public void like(Long userId, Long noteId) {
        // 只能点赞已审核通过的笔记
        noteRepo.findById(noteId)
                .filter(n -> n.getStatus() == NoteStatus.APPROVED)
                .orElseThrow(() -> new BusinessException("Note not found"));

        if (likeRepo.existsByUserIdAndNoteId(userId, noteId)) {
            throw new BusinessException("Already liked");
        }
        likeRepo.save(NoteLike.builder().userId(userId).noteId(noteId).build());
        redisTemplateLong.opsForValue().increment(LIKE_COUNT_KEY + noteId);
    }

    @Transactional
    public void unlike(Long userId, Long noteId) {
        likeRepo.findByUserIdAndNoteId(userId, noteId)
                .orElseThrow(() -> new BusinessException("Haven't liked this note"));
        likeRepo.deleteByUserIdAndNoteId(userId, noteId);

        // FIXME: 这里有并发问题，极端情况下可能变成负数，后续考虑用 lua 脚本
        String key = LIKE_COUNT_KEY + noteId;
        Long val = redisTemplateLong.opsForValue().get(key);
        if (val != null && val > 0) {
            redisTemplateLong.opsForValue().decrement(key);
        }
    }

    public long getLikeCount(Long noteId) {
        String key = LIKE_COUNT_KEY + noteId;
        Long cached = redisTemplateLong.opsForValue().get(key);
        if (cached != null) return cached;

        // 缓存 miss，从 DB 查然后写回
        long count = likeRepo.countByNoteId(noteId);
        redisTemplateLong.opsForValue().set(key, count);
        return count;
    }

    /**
     * 批量获取点赞数，先查 Redis 再查 DB 补缺
     */
    public Map<Long, Long> getLikeCounts(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) return Collections.emptyMap();

        Map<Long, Long> result = new HashMap<>();
        List<Long> missIds = new ArrayList<>();

        for (Long id : noteIds) {
            Long cached = redisTemplateLong.opsForValue().get(LIKE_COUNT_KEY + id);
            if (cached != null) {
                result.put(id, cached);
            } else {
                missIds.add(id);
            }
        }

        if (!missIds.isEmpty()) {
            // 批量从 DB 查
            List<Object[]> dbRows = likeRepo.countByNoteIds(missIds);
            for (Object[] row : dbRows) {
                Long nid = (Long) row[0];
                Long cnt = (Long) row[1];
                result.put(nid, cnt);
                redisTemplateLong.opsForValue().set(LIKE_COUNT_KEY + nid, cnt);
            }
            // 没记录的补 0
            for (Long id : missIds) {
                result.putIfAbsent(id, 0L);
            }
        }
        return result;
    }

    public Set<Long> getLikedNoteIds(Long userId, List<Long> noteIds) {
        if (userId == null || noteIds == null || noteIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(likeRepo.findLikedNoteIds(userId, noteIds));
    }
}
