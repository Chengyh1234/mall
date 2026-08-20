package com.cyh.mallauth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用户实体类（仅用于Feign数据映射，不再关联数据库）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable, UserDetails {

    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String avatar;

    private String realName;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    private List<Role> roles;

    private List<GrantedAuthority> authorities = new ArrayList<>();

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = new ArrayList<>(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }



    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /*
    authenticationManager.authenticate()会检测isEnabled值，为false抛出 DisabledException
     */
    @Override
    public boolean isEnabled() {
        //return status != null && status == 1;
        return true;
    }
}