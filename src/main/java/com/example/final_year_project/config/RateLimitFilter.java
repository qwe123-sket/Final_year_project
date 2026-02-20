package com.example.final_year_project.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limit for /api/auth (login/register) to reduce brute-force risk.
 * Allows 15 requests per minute per IP for auth endpoints.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT = 15;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Window> authCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path != null && path.startsWith("/api/auth/")) {
            String key = clientKey(request);
            Window w = authCounts.compute(key, (k, v) -> {
                long now = System.currentTimeMillis();
                if (v == null || now - v.start > WINDOW_MS) {
                    return new Window(now, new AtomicInteger(1));
                }
                v.count.incrementAndGet();
                return v;
            });
            if (w.count.get() > AUTH_LIMIT) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"code\":429,\"message\":\"Too many attempts. Try again later.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Window {
        final long start;
        final AtomicInteger count;

        Window(long start, AtomicInteger count) {
            this.start = start;
            this.count = count;
        }
    }
}
