package com.cyh.mallproduct.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SPU销售属性自定义值实体
 * 商家为 SPU 的销售属性添加的自定义值（如：蓝色、金色）
 * 生命周期跟随 SPU，删除 SPU 时级联删除
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "spu_sale_attr_custom_values", autoResultMap = true)
public class SpuSaleAttrCustomValue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SPU ID
     */
    @TableField(value = "spu_id")
    private Long spuId;

    /**
     * 销售属性ID
     */
    @TableField(value = "attr_id")
    private Long attrId;

    /**
     * 自定义属性值（如：蓝色、金色）
     */
    @TableField(value = "value")
    private String value;

    /**
     * 图片（如颜色色块）
     */
    @TableField(value = "image_url")
    private String imageUrl;

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
}