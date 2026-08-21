package com.cyh.mallstore.filter;

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
 * 信任网关注入的 X-User-Id、X-User-Roles 请求头
 * 解析后构建 Spring Security 的 Authentication 对象注入 SecurityContext
 * 下游服务无需自行校验 Token，完全信任网关
 */
@Slf4j
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 从请求头中提取 X-User-Id / X-User-Name / X-User-Roles，构建 UsernamePasswordAuthenticationToken
     * 注入 SecurityContextHolder，供下游 @PreAuthorize 和 getCurrentUserId() 使用
     * 如果请求头不存在，则放行（匿名访问），后续由 @PreAuthorize 控制权限
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
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
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            authToken.setDetails(
                    new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug("网关信任头认证成功, userId: {}, roles: {}", userId, roles);
        }

        filterChain.doFilter(request, response);
    }
}