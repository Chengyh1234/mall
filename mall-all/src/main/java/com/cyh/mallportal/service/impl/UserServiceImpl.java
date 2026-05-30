package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.mapper.UserMapper;
import com.cyh.mallportal.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 提供用户信息管理的业务逻辑具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 更新用户基本信息（真实姓名、邮箱、手机号）
     * 校验邮箱和手机号唯一性，更新敏感信息时需要验证密码
     *
     * @param userId   用户ID
     * @param realName 真实姓名
     * @param email    邮箱
     * @param phone    手机号
     * @param password 当前密码（用于验证身份）
     * @return 更新后的用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateProfile(Long userId, String realName, String email, String phone, String password) {
        log.info("更新用户信息, 用户ID: {}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在, {}", userId);
            throw new BusinessException("用户不存在");
        }

        // 邮箱格式校验
        if (email != null && !email.isEmpty()) {
            if (!isValidEmail(email)) {
                log.warn("邮箱格式错误: {}", email);
                throw new BusinessException("邮箱格式不正确");
            }
        }

        // 手机号格式校验
        if (phone != null && !phone.isEmpty()) {
            if (!isValidPhone(phone)) {
                log.warn("手机号格式错误, {}", phone);
                throw new BusinessException("手机号格式不正确，必须是11位数字");
            }
        }

        boolean updateSensitiveInfo = (email != null && !email.isEmpty() && !email.equals(user.getEmail())) || 
                                      (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone()));

        if (updateSensitiveInfo) {
            if (password == null || password.isEmpty()) {
                log.warn("更新敏感信息需要验证密码, 用户ID: {}", userId);
                throw new BusinessException("更新邮箱或手机号需要输入密码进行验证");
            }
            
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("密码验证失败, 用户ID: {}", userId);
                throw new BusinessException("密码验证失败");
            }
        }

        if (realName != null && !realName.isEmpty()) {
            user.setRealName(realName);
        }

        // 更新邮箱（校验唯一性）
        if (email != null && !email.isEmpty() && !email.equals(user.getEmail())) {
            QueryWrapper<User> emailQuery = new QueryWrapper<>();
            emailQuery.eq("email", email);
            User existingByEmail = userMapper.selectOne(emailQuery);
            if (existingByEmail != null) {
                log.warn("邮箱已被使用: {}", email);
                throw new BusinessException("该邮箱已被其他用户使用");
            }
            user.setEmail(email);
        }

        // 更新手机号（校验唯一性）
        if (phone != null && !phone.isEmpty() && !phone.equals(user.getPhone())) {
            QueryWrapper<User> phoneQuery = new QueryWrapper<>();
            phoneQuery.eq("phone", phone);
            User existingByPhone = userMapper.selectOne(phoneQuery);
            if (existingByPhone != null) {
                log.warn("手机号已被使用, {}", phone);
                throw new BusinessException("该手机号已被其他用户使用");
            }
            user.setPhone(phone);
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户信息更新成功, 用户ID: {}", userId);
        // 清除敏感信息后返回
        user.setPassword(null);
        return user;
    }

    /**
     * 邮箱格式校验
     *
     * @param email 邮箱地址
     * @return 是否有效
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    /**
     * 手机号格式校验
     *
     * @param phone 手机号
     * @return 是否有效
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        String phoneRegex = "^1[3-9]\\d{9}$";
        return phone.matches(phoneRegex);
    }

    /**
     * 修改密码
     * 校验旧密码是否正确，密码加密存储
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码, 用户ID: {}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在, {}", userId);
            throw new BusinessException("用户不存在");
        }

        // 校验旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("旧密码错误, 用户ID: {}", userId);
            throw new BusinessException("旧密码错误");
        }

        // 新密码加密存储
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("密码修改成功, 用户ID: {}", userId);
        return true;
    }

    /**
     * 更新头像
     *
     * @param userId    用户ID
     * @param avatarUrl 头像URL
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAvatar(Long userId, String avatarUrl) {
        log.info("更新头像, 用户ID: {}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在, {}", userId);
            throw new BusinessException("用户不存在");
        }

        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("头像更新成功, 用户ID: {}", userId);
        return true;
    }

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（不含密码）
     */
    @Override
    public User getById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
        }
        return user;
    }
}