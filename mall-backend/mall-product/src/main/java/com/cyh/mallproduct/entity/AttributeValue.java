package com.cyh.mallproduct.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 属性值实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "attribute_values", autoResultMap = true)
public class AttributeValue implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 属性ID
     */
    @TableField(value = "attr_id")
    private Long attrId;

    /**
     * 属性值（黑色、8G、5000mAh等）
     */
    @TableField(value = "value")
    private String value;

    /**
     * 销售属性可配图片（如颜色色块）
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 卖家ID（NULL=平台预设值，非NULL=卖家自定义值）
     */
    @TableField(value = "seller_id")
    private Long sellerId;

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