package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.user.PasswordChangeRequest;
import com.example.final_year_project.dto.user.UserUpdateRequest;
import com.example.final_year_project.dto.user.UserVO;
import com.example.final_year_project.security.CurrentUser;
import com.example.final_year_project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        Long userId = CurrentUser.getUserId();
        userService.changePassword(userId, req);
        return Result.ok();
    }
}
