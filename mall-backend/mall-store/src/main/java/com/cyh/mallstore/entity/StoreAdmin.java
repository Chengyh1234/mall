package com.cyh.mallstore.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺管理员实体
 * 对应数据库 store_admins 表，记录店铺的管理员/员工账号及其角色权限
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "store_admins", autoResultMap = true)
public class StoreAdmin implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "store_id")
    private Long storeId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "role")
    private Integer role;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}