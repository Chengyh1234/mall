package com.cyh.mallgateway.filter;
import com.cyh.mallcommon.constant.MyConstants;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.exception.ErrorCode;
import com.cyh.mallcommon.utils.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * 全局鉴权过滤器
 * <p>
 * 功能说明：
 * 1. 白名单路径直接放行（登录、注册、公开商品查询等）
 * 2. 非白名单路径需校验 Session Token（基于 Redis 存储）
 * 3. 从 Redis 读取 Token JSON 字符串，手动反序列化后校验
 * 4. 校验通过后将用户信息注入请求头，传递给下游服务
 * 5. 自动刷新 Token 过期时间
 *
 * @author cyh
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    //spring-boot-starter-data-redis-reactive引入这个依赖启动会自动进行创建
    private final ReactiveStringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 公开 GET 路径列表（无需鉴权）
     */
    private static final Set<String> PUBLIC_GET_PATHS = Set.of(
            "/api/captcha",
            "/api/attribute/category/**",
            "/api/attribute/sales/**",
            "/api/attribute/basic/**",
            "/api/banner/active",
            "/api/brand/**",
            "/api/category/detail/**",
            "/api/category/list",
            "/api/category/page",
            "/api/category/tree",
            "/api/category/level1",
            "/api/category/children/**",
            "/api/delivery/list/**",
            "/api/delivery/detail/**",
            "/api/delivery/status-desc/**",
            "/api/logistics/list",
            "/api/logistics/detail/**",
            "/api/logistics/code/**",
            "/api/order/pay-status-desc/**",
            "/api/sku/detail/**",
            "/api/sku/total-stock/**",
            "/api/sku/list-with-attributes",
            "/api/sku/detail-with-attributes/**",
            "/api/spu/detail/**",
            "/api/spu/page",
            "/api/spu/by-store/**",
            "/api/spu/*/basic-attributes",
            "/api/spu/search",
            "/api/spu/suggest",
            "/api/store/detail/**",
            "/api/store/page"
    );

    /**
     * 公开 POST 路径列表（无需鉴权）
     */
    private static final Set<String> PUBLIC_POST_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/register/send-email-code",
            "/api/auth/admin/login",
            "/api/auth/login/send-email-code",
            "/api/auth/login/email-code",
            "/api/auth/reset-password/send-code",
            "/api/auth/reset-password/reset"
    );

    /**
     * 认证错误信息存储 Key（通过 exchange 属性传递）
     */
    private static final String ATTR_AUTH_ERROR_CODE = "auth_error_code";
    private static final String ATTR_AUTH_ERROR_MSG = "auth_error_msg";

    /**
     * 核心过滤逻辑
     * <p>
     * 注意：认证结果通过 {@link #authenticate(ServerWebExchange, String)} 返回 {@code Mono<ServerHttpRequest>}
     * （会发射值），避免将 {@code Mono<Void>} 放入 flatMap + switchIfEmpty 链中，
     * 防止 chain.filter() 的 complete-only 信号被 switchIfEmpty 误判为"空"。
     *
     * @param exchange ServerWebExchange
     * @param chain    GatewayFilterChain
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // 1. 白名单直接放行
        if (isPublicPath(path, method)) {
            return chain.filter(exchange);
        }

        // 2. 获取 Token
        String authHeader = request.getHeaders().getFirst(MyConstants.AUTH_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(MyConstants.BEARER_PREFIX)) {
            return unauthorized(exchange.getResponse(), ErrorCode.NOT_LOGGED_IN.getBusinessCode(), ErrorCode.NOT_LOGGED_IN.getMessage());
        }

        String token = authHeader.substring(MyConstants.BEARER_PREFIX.length());

        // 3. 认证 Token
        //    chain.filter() 返回 Mono<Void>，它在 Reactor 中"完成但不发射值"，
        //    直接放在 flatMap 中，switchIfEmpty 会将 chain.filter() 的成功完成误判为"空流"并触发 fallback。
        //    因此用 .then(Mono.just(true)) 将成功信号转为有值的 Mono<Boolean>，
        //    确保 switchIfEmpty 仅在 authenticate() 返回空（认证失败）时触发。
        return authenticate(exchange, token)
                .flatMap(mutatedRequest ->
                        chain.filter(exchange.mutate().request(mutatedRequest).build())
                                .then(Mono.just(true)))
                .switchIfEmpty(Mono.defer(() -> {
                    Integer code = exchange.getAttribute(ATTR_AUTH_ERROR_CODE);
                    String msg = exchange.getAttribute(ATTR_AUTH_ERROR_MSG);
                    if (code == null) {
                        code = ErrorCode.LOGIN_EXPIRED.getBusinessCode();
                        msg = ErrorCode.LOGIN_EXPIRED.getMessage();
                    }
                    log.warn("认证失败, code={}, message={}", code, msg);
                    return unauthorized(exchange.getResponse(), code, msg)
                            .then(Mono.just(false));
                }))
                .then();
    }

    /**
     * 认证 Token 并构建注入用户信息的请求
     * <p>
     * 返回 {@code Mono<ServerHttpRequest>} 表示认证成功，
     * 返回 {@code Mono.empty()} 表示认证失败（由 switchIfEmpty 处理）。
     * 通过 exchange 属性传递具体错误码和消息。
     *
     * @param exchange ServerWebExchange
     * @param token    Session Token
     * @return 认证成功时发射注入用户信息的请求，失败时发射空
     */
    private Mono<ServerHttpRequest> authenticate(ServerWebExchange exchange, String token) {
        return stringRedisTemplate.opsForValue()
                .get(RedisConstants.TOKEN_PREFIX + token)
                .flatMap(jsonStr -> {
                    // 3.1 反序列化 Token JSON
                    Map<String, Object> userInfo;
                    try {
                        userInfo = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                    } catch (JsonProcessingException e) {
                        log.error("Token 反序列化失败: {}", jsonStr, e);
                        return Mono.empty();
                    }

                    if (userInfo == null || userInfo.isEmpty()) {
                        return Mono.empty();
                    }

                    // 3.2 单点登录校验：检查 sessionId 是否匹配
                    String tokenSessionId = (String) userInfo.get("sessionId");
                    Long userId = extractUserId(userInfo.get("userId"));
                    if (userId == null) {
                        return Mono.empty();
                    }

                    return stringRedisTemplate.opsForValue()
                            .get(RedisConstants.USER_CURRENT_SESSION_PREFIX + userId)
                            .flatMap(currentSessionId -> {
                                if (tokenSessionId == null || !tokenSessionId.equals(currentSessionId)) {
                                    // 会话不一致，删除旧 token 后返回空
                                    exchange.getAttributes().put(ATTR_AUTH_ERROR_CODE, ErrorCode.LOGIN_EXPIRED.getBusinessCode());
                                    exchange.getAttributes().put(ATTR_AUTH_ERROR_MSG, ErrorCode.LOGIN_EXPIRED.getMessage());
                                    return stringRedisTemplate.delete(RedisConstants.TOKEN_PREFIX + token)
                                            .then(Mono.empty());
                                }
                                // 3.3 校验用户状态
                                Integer status = userInfo.get("status") != null
                                        ? ((Number) userInfo.get("status")).intValue() : null;
                                if (status == null || status != 1) {
                                    exchange.getAttributes().put(ATTR_AUTH_ERROR_CODE, ErrorCode.ACCOUNT_DISABLED.getBusinessCode());
                                    exchange.getAttributes().put(ATTR_AUTH_ERROR_MSG, ErrorCode.ACCOUNT_DISABLED.getMessage());
                                    return Mono.empty();
                                }
                                // 3.4 构建注入用户信息的请求
                                String roles = extractRoles(userInfo);
                                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-User-Roles", roles)
                                        .build();
                                // 3.5 刷新 Token 过期时间，返回 mutatedRequest
                                return refreshTokenExpiration(token, userId)
                                        .then(Mono.just(mutatedRequest));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                // 检查是否已有错误属性（session 不匹配、用户禁用等），有则透传空
                                if (exchange.getAttribute(ATTR_AUTH_ERROR_CODE) != null) {
                                    return Mono.empty();
                                }
                                // 无当前 session 记录（首次登录或已过期），视为有效
                                String roles = extractRoles(userInfo);
                                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                        .header("X-User-Id", String.valueOf(userId))
                                        .header("X-User-Roles", roles)
                                        .build();
                                return refreshTokenExpiration(token, userId)
                                        .then(Mono.just(mutatedRequest));
                            }));
                });
    }

    /**
     * 从用户信息中提取角色编码，以逗号拼接
     *
     * @param userInfo 用户信息 Map
     * @return 角色编码字符串，如 "ADMIN,USER"
     */
    private String extractRoles(Map<String, Object> userInfo) {
        Object rolesObj = userInfo.get("roles");
        if (rolesObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> rolesMap = (Map<String, Object>) rolesObj;
            return String.join(",", rolesMap.keySet());
        }
        return "";
    }

    /**
     * 安全提取 userId（兼容 GenericJackson2JsonRedisSerializer 的数组格式）
     * <p>
     * mall-auth 使用 GenericJackson2JsonRedisSerializer 存储用户信息，
     * userId 在 Redis 中可能为 {@code ["java.lang.Long", 4]} 的数组格式，
     * 也可能是普通 Number，此方法统一处理两种格式。
     *
     * @param userIdObj userId 原始值
     * @return userId
     */
    private Long extractUserId(Object userIdObj) {
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        if (userIdObj instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) userIdObj;
            if (!list.isEmpty() && list.get(list.size() - 1) instanceof Number) {
                return ((Number) list.get(list.size() - 1)).longValue();
            }
        }
        log.warn("无法解析 userId: {}", userIdObj);
        return null;
    }

    /**
     * 判断是否为公开路径（无需鉴权）
     *
     * @param path   请求路径
     * @param method HTTP 方法
     * @return true-公开路径，false-需要鉴权
     */
    private boolean isPublicPath(String path, String method) {
        // 静态资源公开
        if (pathMatcher.match("/api/uploads/images/**", path)) {
            return true;
        }

        // 公开 GET 路径
        if ("GET".equalsIgnoreCase(method)) {
            return PUBLIC_GET_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
        }

        // 公开 POST 路径
        if ("POST".equalsIgnoreCase(method)) {
            return PUBLIC_POST_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
        }

        return false;
    }

    /**
     * 返回 401 未认证响应
     * <p>
     * 注意：Reactive 环境中，响应可能已被下游服务提交（如 chain.filter 返回的 Mono<Void>
     * 被 switchIfEmpty 误判为空），此时修改响应头会抛出 UnsupportedOperationException。
     * 因此先检查响应是否已提交，已提交则直接返回。
     *
     * @param response ServerHttpResponse
     * @param code     业务错误码
     * @param message  错误消息
     * @return Mono<Void>
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, int code, String message) {
        if (response.isCommitted()) {
            log.warn("响应已提交，跳过 401 设置: code={}, message={}", code, message);
            return Mono.empty();
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Result<Object> result = Result.error(code, message);
        try {
            String json = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化响应失败", e);
            String json = "{\"code\":" + code + ",\"message\":\"" + message + "\"}";
            DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        }
    }

    /**
     * 刷新 Token 及相关会话的过期时间
     * <p>
     * 三个 expire 操作并行执行，全部完成后返回 Mono<Void>
     *
     * @param token  Token 字符串
     * @param userId 用户ID
     * @return Mono<Void>
     */
    private Mono<Void> refreshTokenExpiration(String token, Long userId) {
        Duration duration = Duration.ofSeconds(RedisConstants.TOKEN_EXPIRATION);
        return Mono.when(
                stringRedisTemplate.expire(RedisConstants.TOKEN_PREFIX + token, duration),
                stringRedisTemplate.expire(RedisConstants.USER_CURRENT_SESSION_PREFIX + userId, duration),
                stringRedisTemplate.expire(RedisConstants.USER_ACTIVE_TOKEN_PREFIX + userId, duration)
        ).then();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
