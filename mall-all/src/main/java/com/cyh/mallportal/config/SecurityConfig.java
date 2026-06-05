package com.cyh.mallportal.config;

import com.cyh.mallportal.filter.TokenAuthenticationFilter;
import com.cyh.mallportal.handler.CustomAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security安全配置类
 * <p>
 * 功能说明：
 * 1. 配置请求授权规则：GET请求公开，其他请求需要登录认证
 * 2. 启用方法级安全：支持@PreAuthorize注解进行权限控制
 * 3. 配置Token认证：使用Redis存储Token和用户信息
 * 4. 密码加密：使用BCrypt加密
 * 5. CORS配置：支持跨域请求
 *
 * @author cyh
 * @since 2024-01-01
 */
@Configuration
/**
 * 启用Web安全配置
 * 启用后Spring Security会自动配置安全过滤器链
 */
@EnableWebSecurity
/**
 * 启用方法级安全控制
 * 支持@PreAuthorize、@PostAuthorize、@Secured注解进行方法级权限控制
 */
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Token认证过滤器
     * 负责从Redis中获取用户信息并设置用户认证
     */
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    /**
     * 未登录认证处理器
     * 访问受保护接口时未携带Token → 返回统一的JSON 401
     */
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * 用户详情服务
     * 用于加载用户信息和权限
     */
    private final UserDetailsService userDetailsService;

    /**
     * 安全过滤器链配置
     * <p>
     * 配置详情：
     * - 禁用CSRF：前后端分离架构无需CSRF保护
     * - 启用CORS：支持跨域请求
     * - 无状态Session：不创建HttpSession
     * - 公开接口：/auth/login、/auth/register、所有GET请求
     * - 认证接口：其他所有POST/PUT/DELETE请求需要登录
     *
     * @param http HttpSecurity对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（前后端分离架构无需CSRF令牌）
                .csrf(csrf -> csrf.disable())
                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 配置无状态Session（不创建HttpSession，适合REST API）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许获取验证码无需认证
                        .requestMatchers(HttpMethod.GET, "/captcha").permitAll()
                        // 允许登录/注册/密码重置接口无需认证
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register",
                                "/auth/register/send-email-code", "/auth/admin/login",
                                "/auth/login/send-email-code", "/auth/login/email-code",
                                "/auth/reset-password/send-code", "/auth/reset-password/reset").permitAll()
                        // 允许静态资源访问
                        .requestMatchers("/uploads/images/**").permitAll()
                        // 公开浏览接口（产品浏览、品牌、分类等，无需登录）
                        .requestMatchers(HttpMethod.GET,
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
                        ).permitAll()
                        // 兜底：其他请求（含未列出的GET、所有POST/PUT/DELETE）都需要认证
                        .anyRequest().authenticated()
                )
                // 配置认证提供者
                .authenticationProvider(authenticationProvider())
                // 添加Token过滤器（在用户名密码认证过滤器之前执行）
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用表单登录（前后端分离使用Token方式）
                .formLogin(form -> form.disable())
                // 禁用HTTP Basic认证
                .httpBasic(basic -> basic.disable())
                // 异常处理：接管 Spring Security 过滤器链中的认证异常
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );

        return http.build();
    }

    /**
     * CORS跨域配置
     * <p>
     * 配置说明：
     * - 允许所有来源：*
     * - 允许的HTTP方法：GET、POST、PUT、DELETE、OPTIONS
     * - 允许所有请求头：*
     * - 预检请求缓存时间：3600秒
     *
     * @return CorsConfigurationSource CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 认证提供者配置
     * <p>
     * 使用DaoAuthenticationProvider：
     * - 从数据库加载用户信息
     * - 使用BCryptPasswordEncoder验证密码
     *
     * @return AuthenticationProvider 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     * <p>
     * 用于处理认证请求，在登录时调用
     *
     * @param config 认证配置
     * @return AuthenticationManager 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 密码编码器
     * <p>
     * 使用BCrypt加密算法：
     * - 单向加密，不可逆
     * - 每次加密结果都不同（因为有随机盐）
     * - 验证时自动比较
     *
     * @return PasswordEncoder 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
