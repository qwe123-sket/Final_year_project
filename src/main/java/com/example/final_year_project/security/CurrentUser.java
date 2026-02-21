package com.example.final_year_project.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 工具类，从 SecurityContext 拿当前登录用户的 ID。
 * 没有登录的话 getUserId() 会抛异常，getUserIdOrNull() 返回 null。
 */
public final class CurrentUser {

    private CurrentUser() {}

    public static Long getUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        if (!(auth.getPrincipal() instanceof ClaimsPrincipal cp)) return null;
        return cp.getUserId();
    }

    public static Long getUserId() {
        Long id = getUserIdOrNull();
        if (id == null) {
            throw new com.example.final_year_project.exception.BusinessException(401, "Please sign in first");
        }
        return id;
    }
}
