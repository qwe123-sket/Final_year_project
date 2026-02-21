package com.example.final_year_project.service;

import com.example.final_year_project.dto.recommend.RecommendItemVO;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.NoteTag;
import com.example.final_year_project.entity.Tag;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.NoteTagRepository;
import com.example.final_year_project.repository.TagRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务 —— 目前只做数据组装层，实际推荐算法由组内另一位同学负责实现。
 *
 * 对接方式：
 * 1) 算法模块产出一组 noteId -> 调用 getRecommendListByNoteIds
 * 2) 如果算法还能给出分数 -> 调用 getRecommendListByNoteIdsWithScores
 * 3) 默认的 getRecommendList 是 fallback，按发布时间倒序返回
 */
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteCacheService cacheService;
    private final NoteLikeService likeService;
    private final NoteTagRepository noteTagRepo;
    private final TagRepository tagRepo;

    // 内容摘要截取长度
    private static final int SUMMARY_LEN = 200;

    /**
     * 默认推荐列表 —— fallback 实现。
     * 等算法对接好了替换这里的逻辑就行。
     */
    public List<RecommendItemVO> getRecommendList(Long userId, int page, int size) {
        // FIXME: userId 目前没用到，接入算法后需要拿来做个性化
        var notes = noteRepository.findByStatus(NoteStatus.APPROVED,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
        return buildBatchVO(notes);
    }

    /**
     * 根据算法给出的笔记 ID 列表，组装前端需要的 VO。
     * 保持输入顺序。
     */
    public List<RecommendItemVO> getRecommendListByNoteIds(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) return Collections.emptyList();

        List<Note> notes = noteRepository.findByIdInAndApproved(noteIds);
        // 按传入的 id 顺序排列
        Map<Long, Note> map = notes.stream().collect(Collectors.toMap(Note::getId, n -> n));
        var ordered = noteIds.stream().map(map::get).filter(Objects::nonNull).toList();
        return buildBatchVO(ordered);
    }

    /**
     * 带分数的推荐结果组装，按 score 降序排列。
     * 算法那边如果能输出每个笔记的推荐分数，就调这个方法。
     */
    public List<RecommendItemVO> getRecommendListByNoteIdsWithScores(Map<Long, Double> scoreMap) {
        if (scoreMap == null || scoreMap.isEmpty()) return Collections.emptyList();

        List<Note> notes = noteRepository.findByIdInAndApproved(new ArrayList<>(scoreMap.keySet()));
        // 按分数降序
        notes.sort((a, b) -> Double.compare(
                scoreMap.getOrDefault(b.getId(), 0.0),
                scoreMap.getOrDefault(a.getId(), 0.0)));

        List<RecommendItemVO> voList = buildBatchVO(notes);
        // 把分数填进去
        for (var vo : voList) {
            vo.setScore(scoreMap.getOrDefault(vo.getNoteId(), 0.0));
        }
        return voList;
    }

    // ---------- 内部方法 ----------

    private List<RecommendItemVO> buildBatchVO(List<Note> notes) {
        if (notes.isEmpty()) return Collections.emptyList();

        var noteIds = notes.stream().map(Note::getId).toList();
        Set<Long> authorIds = notes.stream().map(Note::getUserId).collect(Collectors.toSet());

        // 一次查出所有作者
        Map<Long, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Long> likeCountMap = likeService.getLikeCounts(noteIds);
        Map<Long, List<String>> tagsMap = loadTagsBatch(noteIds);

        return notes.stream().map(n -> {
            var vo = new RecommendItemVO();
            vo.setNoteId(n.getId());
            vo.setTitle(n.getTitle());
            // 内容截取前 200 字做摘要
            String content = n.getContent();
            if (content != null && content.length() > SUMMARY_LEN) {
                content = content.substring(0, SUMMARY_LEN) + "...";
            }
            vo.setContent(content);
            vo.setViewCount(cacheService.getViewCount(n.getId()));
            vo.setLikeCount(likeCountMap.getOrDefault(n.getId(), 0L));
            vo.setTags(tagsMap.getOrDefault(n.getId(), Collections.emptyList()));

            User author = userMap.get(n.getUserId());
            if (author != null) {
                vo.setAuthorName(author.getNickname() != null ? author.getNickname() : author.getUsername());
            }
            return vo;
        }).toList();
    }

    private Map<Long, List<String>> loadTagsBatch(List<Long> noteIds) {
        var noteTags = noteTagRepo.findByNoteIdIn(noteIds);
        if (noteTags.isEmpty()) return Collections.emptyMap();

        Set<Long> tagIds = noteTags.stream().map(NoteTag::getTagId).collect(Collectors.toSet());
        Map<Long, String> nameMap = tagRepo.findByIdIn(new ArrayList<>(tagIds)).stream()
                .collect(Collectors.toMap(Tag::getId, Tag::getName));

        return noteTags.stream().collect(Collectors.groupingBy(
                NoteTag::getNoteId,
                Collectors.mapping(nt -> nameMap.getOrDefault(nt.getTagId(), ""), Collectors.toList())
        ));
    }
}
