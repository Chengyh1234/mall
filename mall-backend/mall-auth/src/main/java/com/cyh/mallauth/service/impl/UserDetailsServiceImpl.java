package com.cyh.mallauth.service.impl;

import com.cyh.mallcommon.exception.BusinessException;

import com.cyh.mallauth.entity.Role;
import com.cyh.mallauth.entity.User;
import com.cyh.mallauth.mapper.RoleMapper;
import com.cyh.mallauth.mapper.UserMapper;
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

    @Override
    public UserDetails loadUserByUsername(String account) {
        User user = userMapper.selectByAccount(account);
        if (user == null) {
            throw new UsernameNotFoundException("账号不存在: " + account);
        }

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