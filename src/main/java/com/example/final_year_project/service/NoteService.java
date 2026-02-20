package com.example.final_year_project.service;

import com.example.final_year_project.dto.note.NoteCreateRequest;
import com.example.final_year_project.dto.note.NoteUpdateRequest;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.FavoriteRepository;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.common.Result;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final NoteCacheService noteCacheService;

    @Transactional
    public NoteVO create(Long userId, NoteCreateRequest req) {
        Note note = Note.builder()
                .userId(userId)
                .title(req.getTitle().trim())
                .content(req.getContent() != null ? req.getContent().trim() : null)
                .status(NoteStatus.PENDING)
                .build();
        note = noteRepository.save(note);
        return toVO(note, userId, false);
    }

    @Transactional
    public NoteVO update(Long userId, Long noteId, NoteUpdateRequest req) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new BusinessException("Note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to edit this note");
        }
        if (req.getTitle() != null) note.setTitle(req.getTitle().trim());
        if (req.getContent() != null) note.setContent(req.getContent().trim());
        note = noteRepository.save(note);
        noteCacheService.evictNote(noteId);
        return toVO(note, userId, favoriteRepository.existsByUserIdAndNoteId(userId, noteId));
    }

    @Transactional
    public void delete(Long userId, Long noteId) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new BusinessException("Note not found"));
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("You do not have permission to delete this note");
        }
        noteRepository.delete(note);
        noteCacheService.evictNote(noteId);
    }

    public NoteVO getById(Long noteId, Long currentUserId) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new BusinessException("Note not found"));
        if (note.getStatus() != NoteStatus.APPROVED && (currentUserId == null || !note.getUserId().equals(currentUserId))) {
            throw new BusinessException("Note not found or not approved");
        }
        boolean favorited = currentUserId != null && favoriteRepository.existsByUserIdAndNoteId(currentUserId, noteId);
        return toVO(note, currentUserId, favorited);
    }

    /** 列表仅展示已通过审核的笔记 */
    public Result.PageData<NoteVO> listApproved(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> p = noteRepository.findByStatus(NoteStatus.APPROVED, pageable);
        List<NoteVO> list = p.getContent().stream()
                .map(n -> toVO(n, null, false))
                .toList();
        return Result.PageData.of(list, p.getTotalElements(), page, size);
    }

    public Result.PageData<NoteVO> listByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> p = noteRepository.findByUserId(userId, pageable);
        List<NoteVO> list = p.getContent().stream()
                .map(n -> toVO(n, userId, favoriteRepository.existsByUserIdAndNoteId(userId, n.getId())))
                .toList();
        return Result.PageData.of(list, p.getTotalElements(), page, size);
    }

    public Result.PageData<NoteVO> search(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return listApproved(page, size);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> p = noteRepository.searchByKeywordAndStatus(keyword.trim(), NoteStatus.APPROVED, pageable);
        List<NoteVO> list = p.getContent().stream()
                .map(n -> toVO(n, null, false))
                .toList();
        return Result.PageData.of(list, p.getTotalElements(), page, size);
    }

    public void incrementViewCount(Long noteId) {
        noteCacheService.incrementViewCount(noteId);
    }

    private NoteVO toVO(Note n, Long currentUserId, Boolean favorited) {
        NoteVO vo = new NoteVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setStatus(n.getStatus());
        vo.setRejectReason(n.getRejectReason());
        vo.setViewCount(noteCacheService.getViewCount(n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());
        vo.setFavorited(favorited != null ? favorited : Boolean.FALSE);
        userRepository.findById(n.getUserId()).ifPresent(u -> vo.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }
}
