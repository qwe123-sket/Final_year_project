package com.example.final_year_project.service;

import com.example.final_year_project.dto.recommend.RecommendItemVO;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推荐模块：预留接口，算法由外部实现后对接。
 * 当前返回空列表或按时间倒序的已通过笔记作为占位。
 */
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteCacheService noteCacheService;

    /**
     * 获取当前用户的个性化推荐列表。
     * 算法侧完成后可改为：调用算法服务获取 noteId 列表，再组装 VO。
     */
    public List<RecommendItemVO> getRecommendList(Long userId, int page, int size) {
        // 占位：返回最近通过的笔记，算法接口对接后替换为算法返回的 ID 列表
        List<Note> notes = noteRepository.findByStatus(NoteStatus.APPROVED,
                org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                .getContent();
        return notes.stream().map(this::toRecommendVO).collect(Collectors.toList());
    }

    /**
     * 根据笔记 ID 列表组装推荐项（供算法侧返回 ID 后调用）
     */
    public List<RecommendItemVO> getRecommendListByNoteIds(List<Long> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) return Collections.emptyList();
        List<Note> notes = noteRepository.findByIdInAndApproved(noteIds);
        return notes.stream().map(this::toRecommendVO).collect(Collectors.toList());
    }

    private RecommendItemVO toRecommendVO(Note n) {
        RecommendItemVO vo = new RecommendItemVO();
        vo.setNoteId(n.getId());
        vo.setTitle(n.getTitle());
        vo.setViewCount(noteCacheService.getViewCount(n.getId()));
        userRepository.findById(n.getUserId()).ifPresent(u -> vo.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }
}
