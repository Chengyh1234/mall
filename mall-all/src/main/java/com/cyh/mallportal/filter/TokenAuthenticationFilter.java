package com.cyh.mallportal.filter;

import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Token认证过滤器
 * <p>
 * 功能说明：
 * 1. 从请求头中获取Token（Authorization: Bearer xxx）
 * 2. 从Redis中查询用户信息
 * 3. 解析用户信息和权限
 * 4. 设置Spring Security上下文
 * <p>
 * 工作流程：
 * - 请求进入 → 检查是否有Token → 有Token则查询Redis → 获取用户信息 → 设置认证 → 放行
 * - 无Token或Redis中不存在 → 直接放行（由SecurityConfig判断是否需要认证）
 *
 * @author cyh
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Redis模板
     * 用于从Redis中获取用户信息
     */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 核心过滤方法
     * <p>
     * 处理流程：
     * 1. 从请求头获取Token
     * 2. 如果没有Token或格式不正确，直接放行
     * 3. 从Redis查询用户信息
     * 4. 如果用户信息存在且未设置认证，则设置认证
     * 5. 放行请求
     *
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头获取Token
        final String authHeader = request.getHeader(MyConstants.AUTH_HEADER);
        final String token;

        // 2. 检查Token是否存在且格式正确（Bearer xxx）
        if (authHeader == null || !authHeader.startsWith(MyConstants.BEARER_PREFIX)) {
            // 无Token，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取Token（去掉"Bearer "前缀）
        token = authHeader.substring(7);

        try {
            // 4. 从Redis获取用户信息
            Object userInfoObj = redisTemplate.opsForValue().get(MyConstants.TOKEN_PREFIX + token);

            // 5. 如果用户信息存在且当前未认证，则设置认证
            if (userInfoObj != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 强制类型转换
                @SuppressWarnings("unchecked")
                Map<String, Object> userInfo = (Map<String, Object>) userInfoObj;

                // 单点登录验证：检查sessionId是否匹配
                String tokenSessionId = (String) userInfo.get("sessionId");
                Long userId = ((Number) userInfo.get("userId")).longValue();
                String currentSessionId = (String) redisTemplate.opsForValue().get(MyConstants.USER_CURRENT_SESSION_PREFIX + userId);

                if (tokenSessionId == null || !tokenSessionId.equals(currentSessionId)) {
                    // 5.1. sessionId不匹配，说明用户已在其他设备登录，但是之前在redis中还记录，需要删除
                    redisTemplate.delete(MyConstants.TOKEN_PREFIX + token);
                    // sessionId不匹配，说明用户已在其他设备登录
                    log.warn("用户 {} 的会话已失效（在其他设备登录）", userId);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"code\": 401, \"message\": \"账号已在其他设备登录\"}");
                    return;
                }

                // 构建User对象
                User user = buildUser(userInfo);

                // 构建权限列表
                List<GrantedAuthority> authorities = buildAuthorities(userInfo);

                // 设置权限到User对象
                user.setAuthorities(authorities);

                // 6. 创建认证Token并设置到SecurityContext
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,          // 用户对象
                        null,          // 凭证（已验证，不需要）
                        user.getAuthorities()  // 权限列表
                );

                // 设置请求详情（IP地址等）
                authToken.setDetails(
                        new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                // 设置到SecurityContext，后续可以通过SecurityContextHolder获取
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // 刷新Token过期时间：用户每次请求成功验证后，重置Redis中token的过期时间
                // 这样可以实现"用户活跃时会话永不过期"的效果
                redisTemplate.expire(
                        MyConstants.TOKEN_PREFIX + token,
                        MyConstants.TOKEN_EXPIRATION,
                        java.util.concurrent.TimeUnit.SECONDS
                );
                log.debug("刷新Token过期时间成功，token: {}, 过期时间: {}秒", 
                        token.substring(0, Math.min(10, token.length())) + "...", 
                        MyConstants.TOKEN_EXPIRATION);
                //刷新用户当前会话ID
                redisTemplate.expire(
                        MyConstants.USER_CURRENT_SESSION_PREFIX + userId,
                        MyConstants.TOKEN_EXPIRATION,
                        java.util.concurrent.TimeUnit.SECONDS
                );
            }
        } catch (Exception e) {
            // 出现异常时记录日志，但不放行请求（让后续的认证检查处理）
            logger.error("设置用户认证失败: " + e.getMessage());
        }

        // 7. 放行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 从用户信息Map构建User对象
     *
     * @param userInfo Redis中的用户信息
     * @return User对象
     */
    private User buildUser(Map<String, Object> userInfo) {
        User user = new User();
        user.setId(((Number) userInfo.get("userId")).longValue());
        user.setUsername((String) userInfo.get("username"));
        user.setRealName((String) userInfo.get("realName"));
        user.setEmail((String) userInfo.get("email"));
        user.setPhone((String) userInfo.get("phone"));
        user.setStatus(1);

        // 解析角色信息
        Object rolesObj = userInfo.get("roles");
        if (rolesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> rolesMap = (Map<String, String>) rolesObj;
            List<Role> roles = new ArrayList<>();
            for (Map.Entry<String, String> entry : rolesMap.entrySet()) {
                Role role = new Role();
                role.setCode(entry.getKey());
                role.setName(entry.getValue());
                roles.add(role);
            }
            user.setRoles(roles);
        }

        return user;
    }

    /**
     * 从用户信息Map构建权限列表
     * 直接从Redis中存储的permissions构建，无需从roles再次构建
     *
     * @param userInfo Redis中的用户信息
     * @return 权限列表
     */
    private List<GrantedAuthority> buildAuthorities(Map<String, Object> userInfo) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 直接从permissions中添加权限（包含角色权限和具体权限）
        Object permissionsObj = userInfo.get("permissions");
        if (permissionsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Boolean> permissionsMap = (Map<String, Boolean>) permissionsObj;
            for (String perm : permissionsMap.keySet()) {
                authorities.add(new SimpleGrantedAuthority(perm));
            }
        }

        return authorities;
    }
}
