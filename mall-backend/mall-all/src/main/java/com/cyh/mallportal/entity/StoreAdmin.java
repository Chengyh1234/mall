package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 店铺管理员实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "store_admins", autoResultMap = true)
public class StoreAdmin implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 店铺ID
     */
    @TableField(value = "store_id")
    private Long storeId;

    /**
     * 管理员用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 角色: 1-店长 2-管理员 3-客服 4-财务
     */
    @TableField(value = "role")
    private Integer role;

    /**
     * 状态: 1-启用 0-禁用
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}