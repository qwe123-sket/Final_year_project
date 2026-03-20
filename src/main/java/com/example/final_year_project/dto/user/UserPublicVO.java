package com.example.final_year_project.dto.user;

import com.example.final_year_project.entity.enums.UserRole;
import com.example.final_year_project.entity.enums.UserStatus;
import lombok.Data;

import java.time.Instant;

/**
 * 对外展示的用户信息（用于公开个人页与搜索建议）。
 * 不暴露邮箱等敏感字段。
 */
@Data
public class UserPublicVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private UserRole role;
    private UserStatus status;
    private Instant createdAt;
}

