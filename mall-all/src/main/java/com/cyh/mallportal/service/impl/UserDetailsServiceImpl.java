package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.entity.Permission;
import com.cyh.mallportal.entity.Role;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.mapper.PermissionMapper;
import com.cyh.mallportal.mapper.RoleMapper;
import com.cyh.mallportal.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
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
    private final PermissionMapper permissionMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<Role> roles = roleMapper.selectByUserId(user.getId());
        user.setRoles(roles);

        if (roles != null && !roles.isEmpty()) {
            List<Long> roleIds = roles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());

            List<Permission> permissions = permissionMapper.selectByRoleIds(roleIds);

            List<GrantedAuthority> authorities = new ArrayList<>();

            for (Role role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            }

            for (Permission permission : permissions) {
                if (permission.getCode() != null) {
                    authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                }
            }

            user.setAuthorities(authorities);
        }

        return user;
    }
}
