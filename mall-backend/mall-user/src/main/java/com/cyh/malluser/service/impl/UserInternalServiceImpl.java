package com.cyh.malluser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyh.mallcommon.dto.RoleDTO;
import com.cyh.mallcommon.dto.UserAuthDTO;
import com.cyh.malluser.entity.Role;
import com.cyh.malluser.entity.User;
import com.cyh.malluser.entity.UserRole;
import com.cyh.malluser.mapper.RoleMapper;
import com.cyh.malluser.mapper.UserMapper;
import com.cyh.malluser.mapper.UserRoleMapper;
import com.cyh.malluser.service.UserInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户内部服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserInternalServiceImpl implements UserInternalService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserAuthDTO loadUserByAccount(String account) {
        User user = userMapper.selectByAccount(account);
        if (user == null) {
            return null;
        }
        return toUserAuthDTO(user);
    }

    @Override
    public UserAuthDTO loadUserByEmail(String email) {
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            return null;
        }
        return toUserAuthDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAuthDTO registerUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 自动分配 USER 角色
        Role userRole = roleMapper.selectByCode("USER");
        if (userRole != null) {
            UserRole userRoleEntity = new UserRole();
            userRoleEntity.setUserId(user.getId());
            userRoleEntity.setRoleId(userRole.getId());
            userRoleMapper.insert(userRoleEntity);
            log.info("用户 {} 注册成功，已自动分配 USER 角色", user.getUsername());
        } else {
            log.warn("USER 角色不存在，请检查数据库 roles 表");
        }

        return toUserAuthDTO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String encodedPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }
        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLogin(Long userId, String ip) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userMapper.updateById(user);
        }
    }

    @Override
    public boolean isEmailExists(String email) {
        return userMapper.selectByEmail(email) != null;
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    @Override
    public boolean isPhoneExists(String phone) {
        return userMapper.selectByPhone(phone) != null;
    }

    /**
     * 将 User 实体转换为 UserAuthDTO
     */
    private UserAuthDTO toUserAuthDTO(User user) {
        UserAuthDTO dto = new UserAuthDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setRealName(user.getRealName());
        dto.setStatus(user.getStatus());

        List<Role> roles = roleMapper.selectByUserId(user.getId());
        if (roles != null) {
            dto.setRoles(roles.stream()
                    .map(r -> new RoleDTO(r.getId(), r.getCode(), r.getName()))
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}