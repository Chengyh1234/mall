package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.service.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器（后台管理用）
 *
 * 提供运营管理员和超级管理员对用户的管理功能
 * 权限：仅限 SUPER_ADMIN、ADMIN 角色访问
 *
 * API 前缀：/user/manage
 */
@Slf4j
@RestController
@RequestMapping("/user/manage")
@RequiredArgsConstructor
public class UserManageController {

    private final UserManageService userManageService;

    /**
     * 将普通用户升级为商家
     *
     * 权限：SUPER_ADMIN、ADMIN
     * 功能：
     * 1. 为用户添加 SELLER 角色
     * 2. 自动创建默认店铺（店铺名为"用户姓名/用户名的店铺"）
     *    如果用户已有店铺则跳过创建
     *
     * @param userId 待升级的用户ID
     * @return 升级结果（含商家角色ID和店铺ID）
     */
    @PutMapping("/promote-to-seller/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<Map<String, Object>> promoteToSeller(@PathVariable Long userId) {
        Map<String, Object> result = userManageService.promoteToSeller(userId);
        return Result.success("用户升级为商家成功", result);
    }
}