package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色信息 DTO（mall-auth ↔ mall-user 内部通信）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    private String code;
    private String name;
}