package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 对应数据库表 cart_items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "cart_items", autoResultMap = true)
public class CartItem implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * SKU ID
     */
    @TableField(value = "sku_id")
    private Long skuId;

    /**
     * 数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 是否选中: 1-选中 0-未选
     */
    @TableField(value = "selected")
    private Integer selected;

    /**
     * 商品名称快照
     */
    @TableField(value = "product_name")
    private String productName;

    /**
     * 商品图片快照
     */
    @TableField(value = "product_image")
    private String productImage;

    /**
     * SKU规格快照
     */
    @TableField(value = "sku_specs")
    private String skuSpecs;

    /**
     * 单价快照（加购物车时的价格）
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 失效时间
     */
    @TableField(value = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    @TableField(value = "notes")
    private String notes;

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