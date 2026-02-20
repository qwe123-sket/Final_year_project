package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.admin.AdminUserVO;
import com.example.final_year_project.dto.admin.NoteAuditRequest;
import com.example.final_year_project.dto.admin.UserStatusRequest;
import com.example.final_year_project.dto.note.NoteVO;
import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.entity.enums.UserStatus;
import com.example.final_year_project.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public Result<AdminService.AdminStats> getStats() {
        return Result.ok(adminService.getStats());
    }

    @PutMapping("/notes/{noteId}/audit")
    public Result<NoteVO> auditNote(@PathVariable Long noteId, @Valid @RequestBody NoteAuditRequest req) {
        return Result.ok(adminService.auditNote(noteId, req));
    }

    @GetMapping("/notes/pending")
    public Result<Result.PageData<NoteVO>> listPendingNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(adminService.listPendingNotes(page, size));
    }

    @PutMapping("/users/{userId}/status")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @Valid @RequestBody UserStatusRequest req) {
        adminService.updateUserStatus(userId, req);
        return Result.ok();
    }

    @PutMapping("/users/{userId}/role")
    public Result<Void> updateUserRole(@PathVariable Long userId, @RequestParam UserRole role) {
        adminService.updateUserRole(userId, role);
        return Result.ok();
    }

    @GetMapping("/users")
    public Result<Result.PageData<AdminUserVO>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UserStatus status) {
        return Result.ok(adminService.listUsers(page, size, status));
    }
}
