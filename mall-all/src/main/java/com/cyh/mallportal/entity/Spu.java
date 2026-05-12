package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SPU实体类
 * 商品标准单元（Standard Product Unit）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "spu", autoResultMap = true)
public class Spu implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    private Long categoryId;

    /**
     * 品牌ID
     */
    @TableField(value = "brand_id")
    private Long brandId;

    /**
     * 商品描述
     */
    @TableField(value = "description")
    private String description;

    /**
     * 主图路径（格式：yyyy/MM/dd/uuid_原名_后缀）
     */
    @TableField(value = "main_image")
    private String mainImage;

    /**
     * 图片集（JSON数组格式）
     */
    @TableField(value = "images")
    private String images;

    /**
     * 单位（如：件、个、台）
     */
    @TableField(value = "unit")
    private String unit;

    /**
     * 关键词（逗号分隔）
     */
    @TableField(value = "keywords")
    private String keywords;

    /**
     * 销量
     */
    @TableField(value = "sales")
    private Integer sales;

    /**
     * 状态（1-上架 0-下架）
     * 逻辑删除字段
     */
    @TableField(value = "status")
    @TableLogic
    private Integer status;

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
