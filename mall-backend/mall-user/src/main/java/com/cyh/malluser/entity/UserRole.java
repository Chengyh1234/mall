package com.cyh.malluser.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "user_roles", autoResultMap = true)
public class UserRole implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "role_id")
    private Long roleId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}