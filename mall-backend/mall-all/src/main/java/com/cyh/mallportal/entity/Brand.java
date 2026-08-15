package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 品牌实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "brands", autoResultMap = true)
public class Brand implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 品牌名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 品牌Logo路径
     */
    @TableField(value = "logo")
    private String logo;

    /**
     * 品牌描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 品牌官网
     */
    @TableField(value = "website")
    private String website;

    /**
     * 排序号
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态（1-启用 0-禁用）
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 逻辑删除（0-未删除 1-已删除）
     */
    @TableField(value = "is_deleted")
    @TableLogic(value = "0", delval = "1")
    private Boolean isDeleted;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
