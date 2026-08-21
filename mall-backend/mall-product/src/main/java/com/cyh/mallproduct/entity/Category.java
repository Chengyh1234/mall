package com.cyh.mallproduct.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 * 支持三级分类结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "categories", autoResultMap = true)
public class Category implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分类名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 父分类ID（0表示一级分类）
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 分类级别（1-一级分类，2-二级分类，3-三级分类）
     */
    @TableField(value = "level")
    private Integer level;

    /**
     * 分类图标
     */
    @TableField(value = "icon")
    private String icon;

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