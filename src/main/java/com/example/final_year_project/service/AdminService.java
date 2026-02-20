package com.example.final_year_project.service;

import com.example.final_year_project.dto.admin.AdminUserVO;
import com.example.final_year_project.dto.admin.NoteAuditRequest;
import com.example.final_year_project.dto.admin.UserStatusRequest;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.entity.Note;
import com.example.final_year_project.entity.User;
import com.example.final_year_project.entity.enums.NoteStatus;
import com.example.final_year_project.entity.enums.UserStatus;
import com.example.final_year_project.exception.BusinessException;
import com.example.final_year_project.repository.NoteRepository;
import com.example.final_year_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 后台管理：笔记审核、用户状态管理
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NoteCacheService noteCacheService;

    @Transactional
    public NoteVO auditNote(Long noteId, NoteAuditRequest req) {
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new BusinessException("Note not found"));
        note.setStatus(req.getStatus());
        note.setRejectReason(req.getStatus() == NoteStatus.REJECTED ? req.getRejectReason() : null);
        note = noteRepository.save(note);
        noteCacheService.evictNote(noteId);
        return toNoteVO(note);
    }

    public com.example.final_year_project.common.Result.PageData<NoteVO> listPendingNotes(int page, int size) {
        int safeSize = com.example.final_year_project.common.PageRequest.clampSize(size);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Note> p = noteRepository.findByStatus(NoteStatus.PENDING, pageable);
        List<NoteVO> list = p.getContent().stream().map(this::toNoteVO).collect(Collectors.toList());
        return com.example.final_year_project.common.Result.PageData.of(list, p.getTotalElements(), page, safeSize);
    }

    /** 管理后台统计数据 */
    public AdminStats getStats() {
        AdminStats s = new AdminStats();
        s.setUserCount(userRepository.count());
        s.setNoteCount(noteRepository.count());
        s.setPendingNoteCount(noteRepository.countByStatus(NoteStatus.PENDING));
        s.setApprovedNoteCount(noteRepository.countByStatus(NoteStatus.APPROVED));
        return s;
    }

    @Transactional
    public void updateUserStatus(Long targetUserId, UserStatusRequest req) {
        User user = userRepository.findById(targetUserId).orElseThrow(() -> new BusinessException("User not found"));
        user.setStatus(req.getStatus());
        userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Long targetUserId, com.example.final_year_project.entity.enums.UserRole role) {
        User user = userRepository.findById(targetUserId).orElseThrow(() -> new BusinessException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public com.example.final_year_project.common.Result.PageData<AdminUserVO> listUsers(int page, int size, UserStatus statusFilter) {
        int safeSize = com.example.final_year_project.common.PageRequest.clampSize(size);
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> p = statusFilter != null
                ? userRepository.findByStatus(statusFilter, pageable)
                : userRepository.findAll(pageable);
        List<AdminUserVO> list = p.getContent().stream().map(this::toAdminUserVO).collect(Collectors.toList());
        return com.example.final_year_project.common.Result.PageData.of(list, p.getTotalElements(), page, safeSize);
    }

    @lombok.Data
    public static class AdminStats {
        private long userCount;
        private long noteCount;
        private long pendingNoteCount;
        private long approvedNoteCount;
    }

    private NoteVO toNoteVO(Note n) {
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
        userRepository.findById(n.getUserId()).ifPresent(u -> vo.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        return vo;
    }

    private AdminUserVO toAdminUserVO(User u) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setEmail(u.getEmail());
        vo.setNickname(u.getNickname());
        vo.setRole(u.getRole());
        vo.setStatus(u.getStatus());
        vo.setCreatedAt(u.getCreatedAt());
        vo.setNoteCount(noteRepository.countByUserId(u.getId()));
        return vo;
    }
}
