package com.cyh.mallauth.service.impl;

import com.cyh.mallcommon.dto.RoleDTO;
import com.cyh.mallcommon.dto.UserAuthDTO;
import com.cyh.mallauth.entity.User;
import com.cyh.mallauth.feign.UserServiceClient;
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
 * 通过 Feign 调用 mall-user 获取用户数据，不再直连数据库。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    @Override
    public UserDetails loadUserByUsername(String account) {
        UserAuthDTO userAuth = userServiceClient.loadByAccount(account);
        if (userAuth == null) {
            throw new UsernameNotFoundException("账号不存在: " + account);
        }

        User user = new User();
        user.setId(userAuth.getId());
        user.setUsername(userAuth.getUsername());
        user.setPassword(userAuth.getPassword());
        user.setEmail(userAuth.getEmail());
        user.setPhone(userAuth.getPhone());
        user.setAvatar(userAuth.getAvatar());
        user.setRealName(userAuth.getRealName());
        user.setStatus(userAuth.getStatus());

        if (userAuth.getRoles() != null && !userAuth.getRoles().isEmpty()) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            for (RoleDTO role : userAuth.getRoles()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            }
            user.setAuthorities(authorities);
        }

        // 将角色列表转成 mall-auth 的 Role 实体，供后续角色校验
        if (userAuth.getRoles() != null) {
            user.setRoles(userAuth.getRoles().stream()
                    .map(r -> {
                        com.cyh.mallauth.entity.Role role = new com.cyh.mallauth.entity.Role();
                        role.setId(r.getId());
                        role.setCode(r.getCode());
                        role.setName(r.getName());
                        return role;
                    })
                    .collect(Collectors.toList()));
        }

        return user;
    }
}