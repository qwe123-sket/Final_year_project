package com.example.final_year_project.controller;

import com.example.final_year_project.common.Result;
import com.example.final_year_project.dto.auth.LoginRequest;
import com.example.final_year_project.dto.auth.LoginResponse;
import com.example.final_year_project.dto.auth.RegisterRequest;
import com.example.final_year_project.service.AuthService;
import com.example.final_year_project.service.CaptchaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    /** register with captcha */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @GetMapping("/captcha")
    public Result<Map<String, String>> getCaptcha() {
        return Result.ok(captchaService.generate());
    }
}
