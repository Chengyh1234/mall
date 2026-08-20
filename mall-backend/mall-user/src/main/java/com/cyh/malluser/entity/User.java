package com.cyh.malluser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "users", autoResultMap = true)
public class User implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
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
}