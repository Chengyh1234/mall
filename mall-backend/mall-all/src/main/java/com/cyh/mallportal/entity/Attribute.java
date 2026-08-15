package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 属性定义实体类
 * 属性类型：1=销售属性，2=基本属性
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "attributes", autoResultMap = true)
public class Attribute implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 属性名称（颜色、内存、电池容量等）
     */
    @TableField(value = "name")
    private String name;

    /**
     * 属性类型：1=销售属性，2=基本属性
     */
    @TableField(value = "attr_type")
    private Integer attrType;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

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

    /**
     * 属性类型常量
     */
    public static final int TYPE_SALES = 1;      // 销售属性
    public static final int TYPE_BASIC = 2;       // 基本属性
}
