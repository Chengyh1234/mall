package com.cyh.malluser.service;

import com.cyh.mallcommon.dto.UserAuthDTO;
import com.cyh.malluser.entity.User;

/**
 * 用户内部服务接口（供其他微服务通过 Feign 调用）
 * <p>
 * 职责：提供通用的用户数据操作，包括用户查询、注册、密码更新等，不对外暴露。
 */
public interface UserInternalService {

    /**
     * 根据账号（用户名/手机号/邮箱）加载用户认证信息
     */
    UserAuthDTO loadUserByAccount(String account);

    /**
     * 根据邮箱加载用户认证信息
     */
    UserAuthDTO loadUserByEmail(String email);

    /**
     * 注册用户
     *
     * @return 注册后的用户认证信息
     */
    UserAuthDTO registerUser(User user);

    /**
     * 更新密码
     */
    boolean updatePassword(Long userId, String encodedPassword);

    /**
     * 更新最后登录信息
     */
    void updateLastLogin(Long userId, String ip);

    /**
     * 检查邮箱是否已被注册
     */
    boolean isEmailExists(String email);

    /**
     * 检查用户名是否已被使用
     */
    boolean isUsernameExists(String username);

    /**
     * 检查手机号是否已被注册
     */
    boolean isPhoneExists(String phone);
}