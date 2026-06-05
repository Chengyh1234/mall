package com.cyh.mallportal.service;

import java.util.Map;

/**
 * 用户管理 Service 接口（后台管理用）
 *
 * 提供运营管理员和超级管理员对用户的管理功能，
 * 如将普通用户升级为商家
 */
public interface UserManageService {

    /**
     * 将普通用户升级为商家
     *
     * 操作为：
     * 1. 为用户添加 SELLER 角色
     * 2. 自动创建默认店铺
     *
     * @param userId 用户ID
     * @return 操作结果（含商家角色ID和店铺ID）
     */
    Map<String, Object> promoteToSeller(Long userId);
}