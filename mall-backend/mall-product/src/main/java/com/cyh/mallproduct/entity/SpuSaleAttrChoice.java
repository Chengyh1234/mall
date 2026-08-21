package com.cyh.mallproduct.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SPU销售属性选择实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "spu_sale_attr_choice", autoResultMap = true)
public class SpuSaleAttrChoice implements Serializable {

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
     * 该SPU启用的属性值ID列表，JSON数组格式，如[101,102]
     */
    @TableField(value = "selected_values")
    private String selectedValues;

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