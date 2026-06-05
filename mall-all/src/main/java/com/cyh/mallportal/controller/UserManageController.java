package com.cyh.mallportal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.UserManageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    /**
     * 分页查询用户列表
     * <p>
     * 权限：SUPER_ADMIN、ADMIN
     * 功能：
     * 1. 分页返回用户基本信息（ID、用户名、真实姓名、邮箱、手机号、头像、状态、创建时间）
     * 2. 每个用户附带角色列表
     * 3. 支持多条件筛选：关键词、用户状态、角色编码、注册时间范围
     *
     * @param page              页码，默认第1页
     * @param pageSize          每页数量，默认10条
     * @param keyword           搜索关键词（可选，模糊匹配用户名/手机号/邮箱）
     * @param status            用户状态（可选，1-启用 0-禁用）
     * @param roleCode          角色编码（可选，如 USER / SELLER / ADMIN）
     * @param registerStartTime 注册时间起点（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @param registerEndTime   注册时间终点（可选，格式 yyyy-MM-dd HH:mm:ss）
     * @return 分页用户数据
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<IPage<User>> pageUsers(@RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status,
                                         @RequestParam(required = false) String roleCode,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime registerStartTime,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime registerEndTime) {
        IPage<User> userPage = userManageService.pageUsers(page, pageSize, keyword, status, roleCode, registerStartTime, registerEndTime);
        return Result.success(userPage);
    }

    /**
     * 启用或禁用用户
     * <p>
     * 权限：SUPER_ADMIN、ADMIN
     * 功能：
     * 1. status=1 启用用户，用户可正常登录
     * 2. status=0 禁用用户，用户无法登录系统
     *
     * @param userId 用户ID
     * @param status 目标状态：1-启用 0-禁用
     * @return 操作结果
     */
    @PutMapping("/status/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public Result<Void> setUserStatus(@PathVariable Long userId,
                                      @RequestParam Integer status) {
        userManageService.setUserStatus(userId, status);
        String msg = status == 1 ? "用户已启用" : "用户已禁用";
        return Result.success(msg, null);
    }
}