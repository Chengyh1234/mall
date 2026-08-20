package com.cyh.mallauth.config;

import com.cyh.mallauth.filter.GatewayHeaderAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * 1. 配置请求授权规则：无须认证
 * 2. 启用方法级安全：支持@PreAuthorize注解进行权限控制
 * 4. 密码加密：使用BCrypt加密
 * 5. CORS配置：支持跨域请求
 *
 * @author cyh
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 网关信任头认证过滤器
     * 读取网关注入的请求头，设置 SecurityContext
     */
    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

    /**
     * 用户详情服务
     * 用于加载用户信息和权限
     */
    private final UserDetailsService userDetailsService;

    /**
     * 安全过滤器链配置
     * <p>
     * 配置说明：
     * - 认证由 mall-gateway 统一处理，下游服务不做认证判断
     * - 所有请求放行，权限控制由 @PreAuthorize 在 Controller 层处理
     * - 禁用CSRF：前后端分离架构无需CSRF保护
     * - 启用CORS：支持跨域请求
     * - 无状态Session：不创建HttpSession
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
                // 所有请求放行，认证由网关统一处理，授权由 @PreAuthorize 控制
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // 配置认证提供者
                .authenticationProvider(authenticationProvider())
                // 添加网关信任头过滤器（在用户名密码认证过滤器之前执行）
                .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用表单登录（前后端分离使用Token方式）
                .formLogin(form -> form.disable())
                // 禁用HTTP Basic认证
                .httpBasic(basic -> basic.disable());

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