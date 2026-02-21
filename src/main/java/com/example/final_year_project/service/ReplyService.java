package com.example.final_year_project.service;

import com.example.final_year_project.dto.reply.ReplyCreateRequest;
import com.example.final_year_project.dto.reply.ReplyVO;
import com.example.final_year_project.entity.Reply;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.ReplyRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReplyVO create(Long userId, Long noteId, ReplyCreateRequest req) {
        // 只能评论已过审的笔记
        var note = noteRepository.findById(noteId);
        if (note.isEmpty() || note.get().getStatus() != NoteStatus.APPROVED) {
            throw new BusinessException("Cannot reply to this note");
        }

        Reply reply = Reply.builder()
                .noteId(noteId)
                .userId(userId)
                .content(req.getContent().trim())
                .build();
        reply = replyRepository.save(reply);
        return toVO(reply);
    }

    // TODO: 后续可以做评论的分页加载和嵌套回复
    public List<ReplyVO> listByNote(Long noteId, int page, int size) {
        Pageable pg = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var replies = replyRepository.findByNoteIdOrderByCreatedAtDesc(noteId, pg).getContent();
        return replies.stream().map(this::toVO).toList();
    }

    private ReplyVO toVO(Reply r) {
        ReplyVO vo = new ReplyVO();
        vo.setId(r.getId());
        vo.setNoteId(r.getNoteId());
        vo.setUserId(r.getUserId());
        vo.setContent(r.getContent());
        vo.setCreatedAt(r.getCreatedAt());
        // 拿昵称，没设置昵称就用用户名
        userRepository.findById(r.getUserId()).ifPresent(u ->
                vo.setUsername(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }
}
