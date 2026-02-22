package com.example.final_year_project.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 64)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 32)
    private String password;

    @Size(max = 128)
    private String email;

    @Size(max = 64)
    private String nickname;

    /** 图形验证码 key（从 /api/auth/captcha 获取） */
    @NotBlank(message = "Captcha key is required")
    private String captchaKey;

    /** 用户输入的图形验证码 */
    @NotBlank(message = "Captcha code is required")
    private String captchaCode;

    /** 管理员注册密钥 */
    private String adminSecret;
}
