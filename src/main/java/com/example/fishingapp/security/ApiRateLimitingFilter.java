// src/main/java/com/example/fishingapp/security/ApiRateLimitingFilter.java
package com.example.fishingapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiRateLimitingFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private RateLimiter rateLimiter;

    private record Rule(String method, String pattern, String name, int maxAttempts, long windowSeconds) {
    }

    private final List<Rule> rules = List.of(
            new Rule("POST", "/api/comments/catch/*", "comment", 20, 60),
            new Rule("POST", "/api/likes/catch/*/toggle", "like-toggle", 60, 60),
            new Rule("POST", "/api/catches/with-photo", "catch-with-photo", 10, 60),
            new Rule("POST", "/api/catches/*/photo", "catch-photo", 10, 60),
            new Rule("GET", "/api/catches/nearby", "catches-nearby", 30, 30),
            new Rule("GET", "/api/search/users", "search-users", 20, 30)
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        for (Rule rule : rules) {
            if (rule.method().equalsIgnoreCase(method) && pathMatcher.match(rule.pattern(), path)) {
                String identity = resolveIdentity();
                String key = rule.name() + ":" + identity;

                if (!rateLimiter.tryAcquire(key, rule.maxAttempts(), rule.windowSeconds())) {
                    response.setStatus(429);
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"error\":\"Слишком много запросов. Попробуйте позже.\"}");
                    return;
                }
                break;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return "user:" + authentication.getName();
        }
        return "anonymous";
    }
}