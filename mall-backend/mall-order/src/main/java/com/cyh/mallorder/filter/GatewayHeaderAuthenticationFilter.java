package com.cyh.mallorder.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网关信任头认证过滤器
 * 从网关透传的请求头中提取用户信息，构建 SecurityContext
 * 下游服务信任网关，不做 Token 校验
 */
@Slf4j
@Component
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader(USER_ID_HEADER);
        String roles = request.getHeader(USER_ROLES_HEADER);

        if (StringUtils.hasText(userId)) {
            List<SimpleGrantedAuthority> authorities = Collections.emptyList();
            if (StringUtils.hasText(roles)) {
                authorities = Arrays.stream(roles.split(","))
                        .filter(StringUtils::hasText)
                        .map(role -> "ROLE_" + role.trim())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("网关认证注入成功, userId: {}, roles: {}", userId, roles);
        }

        filterChain.doFilter(request, response);
    }
}