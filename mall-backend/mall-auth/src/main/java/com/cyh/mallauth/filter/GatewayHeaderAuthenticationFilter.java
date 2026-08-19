package com.cyh.mallauth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 网关信任头认证过滤器
 * <p>
 * 功能说明：
 * 网关 AuthGlobalFilter 已完成 Token 校验、单点登录校验、用户状态校验、TTL 续期，
 * 并将认证结果注入请求头（X-User-Id、X-User-Name、X-User-Roles）。
 * 本过滤器仅读取这些请求头，设置 Spring SecurityContext，使 @PreAuthorize 可用。
 * 不再自行查询 Redis。
 */
@Slf4j
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String userName = request.getHeader("X-User-Name");
        String roles = request.getHeader("X-User-Roles");

        if (StringUtils.hasText(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (StringUtils.hasText(roles)) {
                for (String role : roles.split(",")) {
                    String trimmed = role.trim();
                    if (StringUtils.hasText(trimmed)) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + trimmed));
                    }
                }
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userName, null, authorities);
            authToken.setDetails(
                    new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("网关信任头认证成功, userId: {}, userName: {}, roles: {}", userId, userName, roles);
        }

        filterChain.doFilter(request, response);
    }
}