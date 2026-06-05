package com.cyh.mallportal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyh.mallportal.entity.User;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户管理 Service 接口（后台管理用）
 * <p>
 * 提供运营管理员和超级管理员对用户的管理功能，
 * 如将普通用户升级为商家、分页查询用户、启用/禁用用户等
 */
public interface UserManageService {

    /**
     * 将普通用户升级为商家
     * <p>
     * 操作为：
     * 1. 为用户添加 SELLER 角色
     * 2. 自动创建默认店铺
     *
     * @param userId 用户ID
     * @return 操作结果（含商家角色ID和店铺ID）
     */
    Map<String, Object> promoteToSeller(Long userId);

    /**
     * 分页查询用户列表
     * 支持多种筛选条件，结果附带用户角色信息
     *
     * @param page              页码
     * @param pageSize          每页数量
     * @param keyword           搜索关键词（可选，模糊匹配用户名/手机号/邮箱）
     * @param status            用户状态（可选，1-启用 0-禁用）
     * @param roleCode          角色编码（可选，按角色筛选）
     * @param registerStartTime 注册时间起点（可选）
     * @param registerEndTime   注册时间终点（可选）
     * @return 分页结果
     */
    IPage<User> pageUsers(Integer page, Integer pageSize, String keyword,
                          Integer status, String roleCode,
                          LocalDateTime registerStartTime, LocalDateTime registerEndTime);

    /**
     * 启用或禁用用户
     * status=1 启用，status=0 禁用
     * 禁用后用户无法登录系统
     *
     * @param userId 用户ID
     * @param status 目标状态：1-启用 0-禁用
     */
    void setUserStatus(Long userId, Integer status);
}