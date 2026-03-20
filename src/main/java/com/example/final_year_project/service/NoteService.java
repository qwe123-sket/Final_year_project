package com.example.final_year_project.service;

import com.example.final_year_project.dto.note.NoteCreateRequest;
import com.example.final_year_project.dto.note.NoteUpdateRequest;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.NoteTag;
import com.example.final_year_project.entity.Tag;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.*;
import com.example.final_year_project.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final NoteCacheService noteCacheService;
    private final NoteLikeService noteLikeService;
    private final TagRepository tagRepository;
    private final NoteTagRepository noteTagRepository;

    // 热度衰减指数，值越大衰减越快，目前 1.5 效果还行
    private static final double DECAY_FACTOR = 1.5;
    // 列表场景返回摘要，避免把大字段 content 全量下发导致慢查询与大响应体
    private static final int SUMMARY_LEN = 200;

    @Transactional
    public NoteVO create(Long userId, NoteCreateRequest req) {
        Note note = Note.builder()
                .userId(userId)
                .title(req.getTitle().trim())
                .content(req.getContent() != null ? req.getContent().trim() : null)
                .coverImage(req.getCoverImage())
                .status(NoteStatus.PENDING)
                .build();
        note = noteRepository.save(note);

        // 保存标签关联
        var savedTags = saveTags(note.getId(), req.getTags());
        NoteVO vo = toVOSingle(note, userId);
        vo.setTags(savedTags);
        return vo;
    }

    @Transactional
    public NoteVO update(Long userId, Long noteId, NoteUpdateRequest req) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("Note not found"));

        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("No permission to edit this note");
        }
        if (req.getTitle() != null) note.setTitle(req.getTitle().trim());
        if (req.getContent() != null) note.setContent(req.getContent().trim());
        if (req.getCoverImage() != null) note.setCoverImage(req.getCoverImage());
        note = noteRepository.save(note);
        noteCacheService.evictNote(noteId);

        // 标签有变化就先删再加
        if (req.getTags() != null) {
            noteTagRepository.deleteByNoteId(noteId);
            saveTags(noteId, req.getTags());
        }
        return toVOSingle(note, userId);
    }

    @Transactional
    public void delete(Long userId, Long noteId) {
        var note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("Note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("You can only delete your own notes");
        }
        noteTagRepository.deleteByNoteId(noteId);
        noteRepository.delete(note);
        noteCacheService.evictNote(noteId);
    }

    public NoteVO getById(Long noteId, Long currentUserId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("Note does not exist"));
        // 未审核通过的笔记只有作者自己能看
        boolean isOwner = currentUserId != null && note.getUserId().equals(currentUserId);
        if (note.getStatus() != NoteStatus.APPROVED && !isOwner) {
            throw new BusinessException("This note is not available");
        }
        return toVOSingle(note, currentUserId);
    }

    public Result.PageData<NoteVO> listApproved(int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> result = noteRepository.findByStatus(NoteStatus.APPROVED, pg);
        List<NoteVO> voList = toBatchVO(result.getContent(), null);
        return Result.PageData.of(voList, result.getTotalElements(), page, size);
    }

    public Result.PageData<NoteVO> listByUser(Long userId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> p = noteRepository.findByUserId(userId, pg);
        return Result.PageData.of(toBatchVO(p.getContent(), userId), p.getTotalElements(), page, size);
    }

    /**
     * 查询某用户已通过笔记列表，用于公开个人页。
     * viewerId 用于计算当前查看者的 like/favorite 状态（可选）。
     */
    public Result.PageData<NoteVO> listApprovedByAuthor(Long authorId, Long viewerId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> p = noteRepository.findApprovedByUserId(authorId, pg);
        return Result.PageData.of(toBatchVO(p.getContent(), viewerId), p.getTotalElements(), page, size);
    }

    public Result.PageData<NoteVO> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return listApproved(page, size);
        }
        Pageable pg = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // 优化搜索：避免对大字段 content 做全量 LIKE 扫描，显著降低慢查询概率
        Page<Note> p = noteRepository.searchByKeywordAndStatusFast(keyword.trim(), NoteStatus.APPROVED, pg);
        return Result.PageData.of(toBatchVO(p.getContent(), null), p.getTotalElements(), page, size);
    }

    /**
     * 热门排行，支持按 day/week/month 筛选时间范围
     */
    public Result.PageData<NoteVO> getTrending(String period, int page, int size) {
        Instant since;
        switch (period) {
            case "day":   since = Instant.now().minus(Duration.ofDays(1)); break;
            case "week":  since = Instant.now().minus(Duration.ofDays(7)); break;
            default:      since = Instant.now().minus(Duration.ofDays(30)); break;
        }

        List<Note> candidates = noteRepository.findApprovedSince(since);
        if (candidates.isEmpty()) {
            return Result.PageData.of(Collections.emptyList(), 0, page, size);
        }

        var noteIds = candidates.stream().map(Note::getId).toList();
        Map<Long, Long> likeCntMap = noteLikeService.getLikeCounts(noteIds);
        Map<Long, Long> favCntMap = batchFavoriteCounts(noteIds);

        // 按热度排序
        List<Note> sorted = candidates.stream()
                .sorted((a, b) -> Double.compare(
                        calcHotScore(b, likeCntMap, favCntMap),
                        calcHotScore(a, likeCntMap, favCntMap)))
                .toList();

        // 手动分页
        int start = Math.min(page * size, sorted.size());
        int end = Math.min(start + size, sorted.size());
        List<Note> pageNotes = sorted.subList(start, end);

        return Result.PageData.of(toBatchVO(pageNotes, null), sorted.size(), page, size);
    }

    /* 热门标签，按使用次数降序 */
    public List<Map<String, Object>> getHotTags(int limit) {
        List<Object[]> rows = noteTagRepository.findHotTags();
        var tagIds = rows.stream().map(r -> (Long) r[0]).limit(limit).toList();
        if (tagIds.isEmpty()) return Collections.emptyList();

        Map<Long, String> nameMap = tagRepository.findByIdIn(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long tagId = (Long) row[0];
            String name = nameMap.get(tagId);
            if (name == null) continue;
            result.add(Map.of("name", name, "count", (Long) row[1]));
            if (result.size() >= limit) break;
        }
        return result;
    }

    public void incrementViewCount(Long noteId) {
        noteCacheService.incrementViewCount(noteId);
    }

    // ========== private helpers ==========

    /**
     * 批量构建 VO，一次性查出 user / tags / likes，避免 N+1
     */
    private List<NoteVO> toBatchVO(List<Note> notes, Long currentUserId) {
        if (notes.isEmpty()) return Collections.emptyList();

        List<Long> noteIds = notes.stream().map(Note::getId).toList();
        Set<Long> authorIds = notes.stream().map(Note::getUserId).collect(Collectors.toSet());

        // 批量加载关联数据
        Map<Long, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Long> likeMap = noteLikeService.getLikeCounts(noteIds);
        Set<Long> likedSet = noteLikeService.getLikedNoteIds(currentUserId, noteIds);

        // 收藏状态
        Set<Long> favSet;
        if (currentUserId != null) {
            var allFavIds = favoriteRepository.findNoteIdsByUserId(currentUserId);
            favSet = allFavIds.stream().filter(noteIds::contains).collect(Collectors.toSet());
        } else {
            favSet = Collections.emptySet();
        }

        Map<Long, List<String>> tagsMap = batchLoadTags(noteIds);

        return notes.stream().map(n -> {
            NoteVO vo = new NoteVO();
            vo.setId(n.getId());
            vo.setUserId(n.getUserId());
            vo.setTitle(n.getTitle());
            // 列表/搜索/推荐卡片只返回摘要，详情页使用 toVOSingle 返回全文
            String content = n.getContent();
            if (content != null && content.length() > SUMMARY_LEN) {
                content = content.substring(0, SUMMARY_LEN) + "...";
            }
            vo.setContent(content);
            vo.setCoverImage(n.getCoverImage());
            vo.setStatus(n.getStatus());
            vo.setRejectReason(n.getRejectReason());
            vo.setViewCount(noteCacheService.getViewCount(n.getId()));
            vo.setLikeCount(likeMap.getOrDefault(n.getId(), 0L));
            vo.setLiked(likedSet.contains(n.getId()));
            vo.setFavorited(favSet.contains(n.getId()));
            vo.setTags(tagsMap.getOrDefault(n.getId(), Collections.emptyList()));
            vo.setCreatedAt(n.getCreatedAt());
            vo.setUpdatedAt(n.getUpdatedAt());

            User author = userMap.get(n.getUserId());
            if (author != null) {
                vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            }
            return vo;
        }).toList();
    }

    // 单条笔记转 VO (详情页用)
    private NoteVO toVOSingle(Note n, Long currentUserId) {
        NoteVO vo = new NoteVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setCoverImage(n.getCoverImage());
        vo.setStatus(n.getStatus());
        vo.setRejectReason(n.getRejectReason());
        vo.setViewCount(noteCacheService.getViewCount(n.getId()));
        vo.setLikeCount(noteLikeService.getLikeCount(n.getId()));

        // 当前用户是否已点赞 / 收藏
        if (currentUserId != null) {
            vo.setLiked(noteLikeService.getLikedNoteIds(currentUserId, List.of(n.getId())).contains(n.getId()));
            vo.setFavorited(favoriteRepository.existsByUserIdAndNoteId(currentUserId, n.getId()));
        } else {
            vo.setLiked(false);
            vo.setFavorited(false);
        }

        vo.setTags(loadTagsForNote(n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());

        userRepository.findById(n.getUserId()).ifPresent(u ->
                vo.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }

    /** 保存标签，不存在的自动创建 */
    private List<String> saveTags(Long noteId, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return Collections.emptyList();

        // 去重、去空白、最多5个
        List<String> cleaned = tagNames.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(5)
                .toList();
        if (cleaned.isEmpty()) return Collections.emptyList();

        var existing = tagRepository.findByNameIn(cleaned);
        Map<String, Tag> nameMap = existing.stream()
                .collect(Collectors.toMap(Tag::getName, t -> t));

        List<Tag> allTags = new ArrayList<>();
        for (String name : cleaned) {
            Tag t = nameMap.get(name);
            if (t == null) {
                t = tagRepository.save(Tag.builder().name(name).build());
            }
            allTags.add(t);
        }

        // 写入关联表
        for (Tag tag : allTags) {
            noteTagRepository.save(NoteTag.builder().noteId(noteId).tagId(tag.getId()).build());
        }
        return allTags.stream().map(Tag::getName).toList();
    }

    private List<String> loadTagsForNote(Long noteId) {
        var noteTags = noteTagRepository.findByNoteId(noteId);
        if (noteTags.isEmpty()) return Collections.emptyList();
        List<Long> ids = noteTags.stream().map(NoteTag::getTagId).toList();
        return tagRepository.findByIdIn(ids).stream().map(Tag::getName).toList();
    }

    private Map<Long, List<String>> batchLoadTags(List<Long> noteIds) {
        var allNoteTags = noteTagRepository.findByNoteIdIn(noteIds);
        if (allNoteTags.isEmpty()) return Collections.emptyMap();

        Set<Long> tagIds = allNoteTags.stream().map(NoteTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> tagNameMap = tagRepository.findByIdIn(new ArrayList<>(tagIds)).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));

        return allNoteTags.stream().collect(Collectors.groupingBy(
                NoteTag::getNoteId,
                Collectors.mapping(nt -> tagNameMap.getOrDefault(nt.getTagId(), ""), Collectors.toList())
        ));
    }

    private Map<Long, Long> batchFavoriteCounts(List<Long> noteIds) {
        var rows = favoriteRepository.countByNoteIds(noteIds);
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put((Long) r[0], (Long) r[1]);
        }
        // 没有收藏记录的补零
        for (Long id : noteIds) {
            map.putIfAbsent(id, 0L);
        }
        return map;
    }

    // TODO: 后续可以考虑把热度分数缓存到 Redis，不用每次实时计算
    private double calcHotScore(Note n, Map<Long, Long> likeCntMap, Map<Long, Long> favCntMap) {
        long views = noteCacheService.getViewCount(n.getId());
        long likes = likeCntMap.getOrDefault(n.getId(), 0L);
        long favs = favCntMap.getOrDefault(n.getId(), 0L);
        double age = Duration.between(n.getCreatedAt(), Instant.now()).toHours();
        // 加权公式：浏览*1 + 点赞*2 + 收藏*3，再除以时间衰减
        return (views + likes * 2 + favs * 3) / Math.pow(age + 2, DECAY_FACTOR);
    }
}
