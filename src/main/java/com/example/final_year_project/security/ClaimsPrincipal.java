package com.example.final_year_project.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * 从 JWT 解析出的主体（userId + role，不查库）
 */
@Getter
public class ClaimsPrincipal {

    private final Long userId;
    private final String role;

    public ClaimsPrincipal(Long userId, String role) {
        this.userId = userId;
        this.role = role != null ? role : "USER";
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}
