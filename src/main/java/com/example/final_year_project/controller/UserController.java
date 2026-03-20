package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.user.DashboardVO;
import com.example.final_year_project.dto.user.PasswordChangeRequest;
import com.example.final_year_project.dto.user.UserStatsVO;
import com.example.final_year_project.dto.user.UserUpdateRequest;
import com.example.final_year_project.dto.user.UserVO;
import com.example.final_year_project.dto.user.UserPublicVO;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.UserService;
import com.example.final_year_project.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final NoteService noteService;

    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        Long userId = CurrentUser.getUserId();
        return Result.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UserUpdateRequest req) {
        Long userId = CurrentUser.getUserId();
        return Result.ok(userService.updateProfile(userId, req));
    }

    @GetMapping("/stats")
    public Result<UserStatsVO> getStats() {
        Long userId = CurrentUser.getUserId();
        return Result.ok(userService.getStats(userId));
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        Long userId = CurrentUser.getUserId();
        return Result.ok(userService.getDashboard(userId));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        Long userId = CurrentUser.getUserId();
        userService.changePassword(userId, req);
        return Result.ok();
    }

    /**
     * 用户搜索（用户名/昵称模糊匹配），用于搜索栏下拉建议。
     */
    @GetMapping("/search")
    public Result<List<UserPublicVO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return Result.ok(userService.searchPublicUsers(keyword, limit));
    }

    /**
     * 公开个人页信息（不要求登录）。
     */
    @GetMapping("/public/{id}")
    public Result<UserPublicVO> getPublicProfile(@PathVariable Long id) {
        return Result.ok(userService.getPublicProfile(id));
    }

    /**
     * 公开个人页的已通过笔记列表（允许未登录查看）。
     */
    @GetMapping("/public/{id}/notes")
    public Result<Result.PageData<com.example.final_year_project.dto.note.NoteVO>> getPublicUserNotes(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Long viewerId = CurrentUser.getUserIdOrNull();
        // 复用 NoteService 的分页结果格式，content 由 VO 截断，减少列表接口体积
        return Result.ok(noteService.listApprovedByAuthor(id, viewerId, page, com.example.final_year_project.common.PageRequest.clampSize(size)));
    }
}
