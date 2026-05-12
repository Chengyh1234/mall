package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * SPU基本属性值实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "spu_basic_attr_values", autoResultMap = true)
public class SpuBasicAttrValue implements Serializable {

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
     * 基本属性ID
     */
    @TableField(value = "attr_id")
    private Long attrId;

    /**
     * 当input_type为单选/多选时，关联attribute_values.id
     */
    @TableField(value = "attr_value_id")
    private Long attrValueId;

    /**
     * 当input_type为手动输入时，存储用户填写的值
     */
    @TableField(value = "manual_value")
    private String manualValue;

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
