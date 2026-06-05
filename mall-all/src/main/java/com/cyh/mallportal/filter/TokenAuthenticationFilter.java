package com.cyh.mallportal.filter;

import cn.hutool.json.JSONUtil;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.utils.Result;
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
import org.springframework.util.StringUtils;
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
     * 路径匹配器
     */
    private final org.springframework.util.AntPathMatcher pathMatcher = new org.springframework.util.AntPathMatcher();

    /**
     * 公开 GET 路径列表（与 SecurityConfig 保持一致）
     */
    private static final java.util.Set<String> PUBLIC_GET_PATHS = java.util.Set.of(
            "/captcha",
            "/attribute/category/**",
            "/attribute/sales/**",
            "/attribute/basic/**",
            "/attribute/spu/**",
            "/banner/active",
            "/brand/**",
            "/category/detail/**",
            "/category/list",
            "/category/page",
            "/category/tree",
            "/category/level1",
            "/category/children/**",
            "/delivery/list/**",
            "/delivery/detail/**",
            "/delivery/status-desc/**",
            "/logistics/list",
            "/logistics/detail/**",
            "/logistics/code/**",
            "/order/status-desc/**",
            "/order/pay-status-desc/**",
            "/sku/detail/**",
            "/sku/list",
            "/sku/page",
            "/sku/min-price/**",
            "/sku/total-stock/**",
            "/sku/list-with-attributes",
            "/sku/detail-with-attributes/**",
            "/spu/detail/**",
            "/spu/list",
            "/spu/page",
            "/spu/by-store/**",
            "/store/detail/**",
            "/store/page"
    );

    /**
     * 公开 POST 路径列表（与 SecurityConfig 保持一致）
     */
    private static final java.util.Set<String> PUBLIC_POST_PATHS = java.util.Set.of(
            "/auth/login", "/auth/register", "/auth/register/send-email-code",
            "/auth/admin/login", "/auth/login/send-email-code", "/auth/login/email-code",
            "/auth/reset-password/send-code", "/auth/reset-password/reset"
    );

    /**
     * 是否需要跳过此请求
     * <p>
     * 返回 true 则 doFilterInternal 不会执行，直接放行。
     * 规则与 {@code SecurityConfig.filterChain()} 中的公开路径保持一致。
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();
        //String requestURI = request.getRequestURI();
        //log.info("path: {}, method: {}, uri: {}", path, method, requestURI);

        // 静态资源公开（不限方法）
        if (pathMatcher.match("/uploads/images/**", path)) {
            return true;
        }

        // 公开的 GET 路径
        if ("GET".equalsIgnoreCase(method)) {
            return PUBLIC_GET_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
        }

        // 公开的 POST 路径
        if ("POST".equalsIgnoreCase(method)) {
            return PUBLIC_POST_PATHS.contains(path);
        }

        return false;
    }

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
            // 无Token，返回未登录
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> error = Result.error(ErrorCode.NOT_LOGGED_IN.getBusinessCode(), ErrorCode.NOT_LOGGED_IN.getMessage());
            response.getWriter().write(JSONUtil.toJsonStr(error));
            return;
        }

        // 3. 提取Token（去掉"Bearer "前缀）
        token = authHeader.substring(7);

        // 4. 从Redis获取用户信息
        Object userInfoObj = redisTemplate.opsForValue().get(MyConstants.TOKEN_PREFIX + token);


        // 5. 如果Token存在但Redis中无用户信息，说明登录已失效
        if (userInfoObj == null && StringUtils.hasText(token)) {
            log.warn("Token已过期或无效，token: {}", token.substring(0, Math.min(10, token.length())) + "...");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> error = Result.error(ErrorCode.LOGIN_EXPIRED.getHttpStatus(), ErrorCode.LOGIN_EXPIRED.getMessage());
            response.getWriter().write(JSONUtil.toJsonStr(error));
            return;
        }

        // 6. 如果用户信息存在且当前未认证，则设置认证
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
                //log.warn("用户 {} 的会话已失效（在其他设备登录）", userId);
                //response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                //response.getWriter().write("{\"code\": 401, \"message\": \"账号已在其他设备登录\"}");
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
     * 从Redis中的roles构建角色权限（添加 ROLE_ 前缀）
     * 不再依赖permissions字段，仅基于角色控制
     *
     * @param userInfo Redis中的用户信息
     * @return 权限列表
     */
    private List<GrantedAuthority> buildAuthorities(Map<String, Object> userInfo) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 从 roles 构建权限（角色编码 + ROLE_ 前缀）
        Object rolesObj = userInfo.get("roles");
        if (rolesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> rolesMap = (Map<String, String>) rolesObj;
            for (String roleCode : rolesMap.keySet()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));
            }
        }

        return authorities;
    }
}
