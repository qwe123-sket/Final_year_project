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

    /** 管理员注册密钥：与配置 app.admin-register-secret 一致时注册为管理员，不传或错误则为普通用户 */
    private String adminSecret;
}
