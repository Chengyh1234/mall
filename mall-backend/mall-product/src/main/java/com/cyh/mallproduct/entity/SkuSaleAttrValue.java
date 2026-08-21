package com.cyh.mallproduct.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SKU与销售属性值关联实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sku_sale_attr_values", autoResultMap = true)
public class SkuSaleAttrValue implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    @TableField(value = "sku_id")
    private Long skuId;

    /**
     * 属性值ID（指向attribute_values）
     */
    @TableField(value = "attr_value_id")
    private Long attrValueId;

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