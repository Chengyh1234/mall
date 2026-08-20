package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户认证信息 DTO（mall-auth ↔ mall-user 内部通信）
 * <p>
 * 用于 mall-auth 通过 Feign 调用 mall-user 时传递用户认证数据，
 * 包含用户基础信息和角色列表，不包含密码等敏感字段（密码单独字段用于认证）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {

    private Long id;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private String realName;
    private Integer status;
    private List<RoleDTO> roles;
}