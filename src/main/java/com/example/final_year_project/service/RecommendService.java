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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务 —— 对接 Python 算法模块。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteCacheService cacheService;
    private final NoteLikeService likeService;
    private final NoteTagRepository noteTagRepo;
    private final TagRepository tagRepo;

    // Python API URL
    private static final String ALGO_API_URL = "http://localhost:8000/recommend/{userId}?top_k={size}";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 内容摘要截取长度
    private static final int SUMMARY_LEN = 200;

    /**
     * 获取推荐列表。
     * 尝试调用 Python 算法服务，如果失败则降级为按时间倒序。
     */
    public List<RecommendItemVO> getRecommendList(Long userId, int page, int size) {
        // 如果 userId 为 0 (未登录) 或 page > 0 (翻页)，暂时走 fallback
        // 因为目前的算法 API 主要是针对第一页的个性化推荐
        if (userId == null || userId <= 0) {
            return getFallbackList(page, size);
        }

        try {
            // 调用算法 API
            String url = ALGO_API_URL.replace("{userId}", userId.toString())
                                     .replace("{size}", String.valueOf(size));
            
            log.info("Calling Algo API: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode recommendations = root.get("recommendations");
            
            if (recommendations != null && recommendations.isArray() && recommendations.size() > 0) {
                List<Long> noteIds = new ArrayList<>();
                Map<Long, Double> scoreMap = new HashMap<>();
                Map<Long, String> sourceMap = new HashMap<>();
                
                for (JsonNode node : recommendations) {
                    Long nid = node.get("note_id").asLong();
                    noteIds.add(nid);
                    if (node.has("score")) {
                        scoreMap.put(nid, node.get("score").asDouble());
                    }
                    if (node.has("recall_source")) {
                        sourceMap.put(nid, node.get("recall_source").asText());
                    }
                }
                
                // 根据 ID 查库并组装
                return buildBatchVOWithScoresAndSource(noteIds, scoreMap, sourceMap);
            }
        } catch (Exception e) {
            log.error("Failed to get recommendations from Algo API: {}", e.getMessage());
            // Fallback to default logic
        }

        return getFallbackList(page, size);
    }

    private List<RecommendItemVO> getFallbackList(int page, int size) {
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
        Map<Long, Note> map = notes.stream().collect(Collectors.toMap(Note::getId, n -> n));
        var ordered = noteIds.stream().map(map::get).filter(Objects::nonNull).toList();
        return buildBatchVO(ordered);
    }

    // ---------- 内部方法 ----------

    private List<RecommendItemVO> buildBatchVO(List<Note> notes) {
        return buildBatchVOWithScoresAndSource(
            notes.stream().map(Note::getId).toList(), 
            Collections.emptyMap(), 
            Collections.emptyMap()
        );
    }

    private List<RecommendItemVO> buildBatchVOWithScoresAndSource(
            List<Long> orderedNoteIds, 
            Map<Long, Double> scoreMap,
            Map<Long, String> sourceMap) {
            
        if (orderedNoteIds.isEmpty()) return Collections.emptyList();

        List<Note> notes = noteRepository.findByIdInAndApproved(orderedNoteIds);
        Map<Long, Note> noteMap = notes.stream().collect(Collectors.toMap(Note::getId, n -> n));
        
        // 保持 orderedNoteIds 的顺序
        List<Note> orderedNotes = orderedNoteIds.stream()
                .map(noteMap::get)
                .filter(Objects::nonNull)
                .toList();

        if (orderedNotes.isEmpty()) return Collections.emptyList();

        var noteIds = orderedNotes.stream().map(Note::getId).toList();
        Set<Long> authorIds = orderedNotes.stream().map(Note::getUserId).collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findAllById(authorIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Long> likeCountMap = likeService.getLikeCounts(noteIds);
        Map<Long, List<String>> tagsMap = loadTagsBatch(noteIds);

        return orderedNotes.stream().map(n -> {
            var vo = new RecommendItemVO();
            vo.setNoteId(n.getId());
            vo.setTitle(n.getTitle());
            
            String content = n.getContent();
            if (content != null && content.length() > SUMMARY_LEN) {
                content = content.substring(0, SUMMARY_LEN) + "...";
            }
            vo.setContent(content);
            vo.setCoverImage(n.getCoverImage());
            vo.setViewCount(cacheService.getViewCount(n.getId()));
            vo.setLikeCount(likeCountMap.getOrDefault(n.getId(), 0L));
            vo.setTags(tagsMap.getOrDefault(n.getId(), Collections.emptyList()));
            
            // Set Score and Source
            vo.setScore(scoreMap.getOrDefault(n.getId(), 0.0));
            vo.setRecallSource(sourceMap.getOrDefault(n.getId(), "default"));

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
