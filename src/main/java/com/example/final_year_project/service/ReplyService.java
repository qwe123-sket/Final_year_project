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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReplyVO create(Long userId, Long noteId, ReplyCreateRequest req) {
        if (noteRepository.findById(noteId).map(n -> n.getStatus() != NoteStatus.APPROVED).orElse(true)) {
            throw new BusinessException("Note not found or not approved; cannot reply");
        }
        Reply reply = Reply.builder()
                .noteId(noteId)
                .userId(userId)
                .content(req.getContent().trim())
                .build();
        reply = replyRepository.save(reply);
        return toVO(reply);
    }

    public List<ReplyVO> listByNote(Long noteId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Reply> list = replyRepository.findByNoteIdOrderByCreatedAtDesc(noteId, pageable).getContent();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    private ReplyVO toVO(Reply r) {
        ReplyVO vo = new ReplyVO();
        vo.setId(r.getId());
        vo.setNoteId(r.getNoteId());
        vo.setUserId(r.getUserId());
        vo.setContent(r.getContent());
        vo.setCreatedAt(r.getCreatedAt());
        userRepository.findById(r.getUserId()).ifPresent(u -> vo.setUsername(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }
}
