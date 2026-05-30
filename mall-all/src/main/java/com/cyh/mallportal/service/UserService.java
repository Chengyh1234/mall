package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.User;

/**
 * 用户服务接口
 * 提供用户信息管理的业务逻辑操作
 */
public interface UserService {

    /**
     * 更新用户基本信息（真实姓名、邮箱、手机号）
     *
     * @param userId   用户ID
     * @param realName 真实姓名
     * @param email    邮箱
     * @param phone    手机号
     * @param password 当前密码（用于验证身份）
     * @return 更新后的用户信息
     */
    User updateProfile(Long userId, String realName, String email, String phone, String password);

    /**
     * 修改密码
     *
     * @param userId          用户ID
     * @param oldPassword     旧密码
     * @param newPassword     新密码
     * @return 是否修改成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新头像
     *
     * @param userId    用户ID
     * @param avatarUrl 头像URL
     * @return 是否更新成功
     */
    boolean updateAvatar(Long userId, String avatarUrl);

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getById(Long userId);
}