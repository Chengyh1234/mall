package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户基本信息 DTO（内部服务间通信）
 * <p>
 * 供 mall-product 等下游服务通过 Feign 调用 mall-user 获取用户基础信息，
 * 不包含密码、邮箱等敏感字段。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVo {

    private Long id;
    private String username;
    private String avatar;
    private String realName;
    private String phone;
}