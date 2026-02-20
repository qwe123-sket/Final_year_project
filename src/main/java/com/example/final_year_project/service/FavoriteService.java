package com.example.final_year_project.service;

import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.entity.Favorite;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.FavoriteRepository;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteCacheService noteCacheService;

    @Transactional
    public void add(Long userId, Long noteId) {
        if (noteRepository.findById(noteId).map(n -> n.getStatus() != NoteStatus.APPROVED).orElse(true)) {
            throw new BusinessException("Note not found or not approved");
        }
        if (favoriteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new BusinessException("Already favorited this note");
        }
        Favorite f = Favorite.builder().userId(userId).noteId(noteId).build();
        favoriteRepository.save(f);
    }

    @Transactional
    public void remove(Long userId, Long noteId) {
        favoriteRepository.deleteByUserIdAndNoteId(userId, noteId);
    }

    public boolean isFavorited(Long userId, Long noteId) {
        return userId != null && favoriteRepository.existsByUserIdAndNoteId(userId, noteId);
    }

    public com.example.final_year_project.common.Result.PageData<NoteVO> listMyFavorites(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var p = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<Long> noteIds = p.getContent().stream().map(Favorite::getNoteId).toList();
        if (noteIds.isEmpty()) {
            return com.example.final_year_project.common.Result.PageData.of(List.of(), 0, page, size);
        }
        List<Note> notes = noteRepository.findAllById(noteIds);
        Map<Long, Note> noteMap = notes.stream().collect(Collectors.toMap(Note::getId, n -> n));
        List<NoteVO> list = p.getContent().stream()
                .map(f -> noteMap.get(f.getNoteId()))
                .filter(n -> n != null && n.getStatus() == NoteStatus.APPROVED)
                .map(n -> toNoteVO(n, true))
                .toList();
        return com.example.final_year_project.common.Result.PageData.of(list, p.getTotalElements(), page, size);
    }

    private NoteVO toNoteVO(Note n, boolean favorited) {
        NoteVO vo = new NoteVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setStatus(n.getStatus());
        vo.setViewCount(noteCacheService.getViewCount(n.getId()));
        vo.setCreatedAt(n.getCreatedAt());
        vo.setUpdatedAt(n.getUpdatedAt());
        vo.setFavorited(favorited);
        userRepository.findById(n.getUserId()).ifPresent(u -> vo.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }
}
