package com.cyh.malluser.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.malluser.service.UserManageService;
import com.cyh.malluser.vo.PromoteToSellerVo;
import com.cyh.malluser.vo.UserManageVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户管理控制器（后台管理用）
 * <p>
 * 提供超级管理员对用户的管理功能，包括：
 * 1. 将普通用户升级为商家（自动创建默认店铺）
 * 2. 分页查询用户列表
 * 3. 启用/禁用用户
 * <p>
 * 所有接口要求 SUPER_ADMIN 角色权限。
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/user/manage")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class UserManageController {

    private final UserManageService userManageService;

    /**
     * 将普通用户升级为商家
     * <p>
     * 操作为：
     * 1. 为用户添加 SELLER 角色
     * 2. 通过 Feign 调用 mall-store 自动创建默认店铺
     *
     * @param userId 用户ID
     * @return 升级结果（含商家角色ID、店铺ID等）
     */
    @PutMapping("/promote-to-seller/{userId}")
    public Result<PromoteToSellerVo> promoteToSeller(@PathVariable Long userId) {
        PromoteToSellerVo vo = userManageService.promoteToSeller(userId);
        return Result.success(vo);
    }

    /**
     * 分页查询用户列表（后台管理）
     * <p>
     * 支持按关键词（用户名/手机号/邮箱）、用户状态、角色编码、
     * 注册时间范围筛选，结果附带用户角色信息。
     *
     * @param page              页码（默认 1）
     * @param pageSize          每页数量（默认 10）
     * @param keyword           搜索关键词（可选）
     * @param status            用户状态（可选，1-启用 0-禁用）
     * @param roleCode          角色编码（可选）
     * @param registerStartTime 注册时间起点（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param registerEndTime   注册时间终点（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @return 分页用户列表
     */
    @GetMapping("/page")
    public Result<IPage<UserManageVo>> pageUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime registerStartTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime registerEndTime) {
        IPage<UserManageVo> result = userManageService.pageUsers(
                page, pageSize, keyword, status, roleCode, registerStartTime, registerEndTime);
        return Result.success(result);
    }

    /**
     * 启用用户
     * <p>
     * 将用户状态设为 1-启用，用户可正常登录
     *
     * @param userId 用户ID
     */
    @PutMapping("/enable/{userId}")
    public Result<Void> enableUser(@PathVariable Long userId) {
        userManageService.enableUser(userId);
        return Result.success("用户已启用", null);
    }

    /**
     * 禁用用户
     * <p>
     * 将用户状态设为 0-禁用，禁用后用户无法登录系统，
     * 同时清除 Redis 中的 Token 缓存使其立即下线
     *
     * @param userId 用户ID
     */
    @PutMapping("/disable/{userId}")
    public Result<Void> disableUser(@PathVariable Long userId) {
        userManageService.disableUser(userId);
        return Result.success("用户已禁用", null);
    }
}