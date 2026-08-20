package com.cyh.malluser.util;

import com.cyh.mallcommon.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 权限校验工具类
 * <p>
 * 微服务模式下，网关负责身份认证（Token校验），
 * 本工具类负责角色权限校验，读取网关注入的请求头 X-User-Roles。
 * X-User-Roles 格式为逗号分隔的角色编码，如 "USER,SELLER"。
 */
@Slf4j
public final class AuthUtils {

    private AuthUtils() {
    }

    /**
     * 校验当前用户是否拥有指定角色，若无则抛出 BusinessException
     *
     * @param xUserRoles 网关注入的 X-User-Roles 请求头
     * @param roleCode   所需角色编码，如 "USER"
     */
    public static void requireRole(String xUserRoles, String roleCode) {
        if (!hasRole(xUserRoles, roleCode)) {
            log.warn("权限不足，需要角色: {}, 用户角色: {}", roleCode, xUserRoles);
            throw new BusinessException("权限不足，需要" + roleCode + "角色");
        }
    }

    /**
     * 判断当前用户是否拥有指定角色
     *
     * @param xUserRoles 网关注入的 X-User-Roles 请求头
     * @param roleCode   角色编码，如 "USER"
     * @return true 拥有该角色
     */
    public static boolean hasRole(String xUserRoles, String roleCode) {
        if (!StringUtils.hasText(xUserRoles) || !StringUtils.hasText(roleCode)) {
            return false;
        }
        String[] roles = xUserRoles.split(",");
        for (String role : roles) {
            if (roleCode.equals(role.trim())) {
                return true;
            }
        }
        return false;
    }
}