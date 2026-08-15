package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.entity.Permission;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现类
 * 用于加载用户信息和权限
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    //private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String account) {
        User user = userMapper.selectByAccount(account);
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在: " + account);
        }

        // 校验用户状态：被禁用的用户拒绝登录
        //if (user.getStatus() == null || user.getStatus() != 1) {
        //    throw new BusinessException("账号已被禁用，无法登录");//无法进行使用，需要使用security体系的异常
        //    //throw new DisabledException("账号已被禁用，无法登录");
        //}

        List<Role> roles = roleMapper.selectByUserId(user.getId());
        user.setRoles(roles);

        if (roles != null && !roles.isEmpty()) {

            List<GrantedAuthority> authorities = new ArrayList<>();

            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            }
            user.setAuthorities(authorities);
        }

        return user;
    }
}
