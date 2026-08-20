package com.cyh.malluser.config;

import com.cyh.malluser.filter.GatewayHeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 安全配置
 * <p>
 * 认证鉴权由 mall-gateway 统一处理，网关将认证结果通过请求头传递给下游。
 * 本配置仅启用 SecurityContext，使 @PreAuthorize 注解可用于角色级权限控制。
 * <p>
 * 所有请求放行，下游服务不做认证判断，授权由 @PreAuthorize 在 Controller 层控制。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final GatewayHeaderAuthenticationFilter gatewayHeaderAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离架构）
                .csrf(csrf -> csrf.disable())
                // 无状态 Session（不创建 HttpSession）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 所有请求放行，认证由网关统一处理，授权由 @PreAuthorize 控制
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // 注册网关信任头过滤器，在 UsernamePasswordAuthenticationFilter 之前执行
                .addFilterBefore(gatewayHeaderAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用表单登录和HTTP Basic认证（纯REST API，不需要）
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
        ;

        return http.build();
    }
}