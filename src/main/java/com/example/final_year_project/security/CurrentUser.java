package com.example.final_year_project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 获取当前登录用户 ID（JWT 解析出的 ClaimsPrincipal）
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long getUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof ClaimsPrincipal)) {
            return null;
        }
        return ((ClaimsPrincipal) auth.getPrincipal()).getUserId();
    }

    public static Long getUserId() {
        Long id = getUserIdOrNull();
        if (id == null) {
            throw new com.example.final_year_project.exception.BusinessException(401, "Please sign in first");
        }
        return id;
    }
}
