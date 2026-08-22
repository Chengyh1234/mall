package com.cyh.mallorder.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 控制器基类
 * 提供获取当前登录用户 ID 和判断管理员权限的通用方法
 */
@Slf4j
public class BaseController {

    /**
     * 获取当前登录用户 ID
     * 从网关透传的 X-User-Id 请求头中提取，由 GatewayHeaderAuthenticationFilter 注入 SecurityContext
     *
     * @return 用户 ID，未登录或解析失败时返回 null
     */
    protected Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                try {
                    return Long.valueOf((String) principal);
                } catch (NumberFormatException e) {
                    log.warn("解析用户 ID 失败, principal: {}", principal);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 判断当前用户是否是管理员或商家
     * 包含 SUPER_ADMIN（超级管理员）、SELLER（商家）、STORE_ADMIN（店铺管理员）
     *
     * @return true 表示具有管理员/商家权限
     */
    protected boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().contains("SUPER_ADMIN") ||
                                   a.getAuthority().contains("SELLER") ||
                                   a.getAuthority().contains("STORE_ADMIN"));
        }
        return false;
    }
}