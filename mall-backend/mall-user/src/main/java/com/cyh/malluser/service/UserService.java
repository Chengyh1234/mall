package com.cyh.malluser.service;

import com.cyh.malluser.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 更新用户基本信息
     */
    User updateProfile(Long userId, String username, String realName, String email, String phone, String password);

    /**
     * 修改密码
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新头像
     */
    boolean updateAvatar(Long userId, String avatarUrl);

    /**
     * 根据ID获取用户信息
     */
    User getById(Long userId);
}