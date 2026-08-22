package com.cyh.mallorder.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 * 对应数据库表 order_items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "order_items", autoResultMap = true)
public class OrderItem implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 订单号
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * SKU ID
     */
    @TableField(value = "sku_id")
    private Long skuId;

    /**
     * SPU ID
     */
    @TableField(value = "spu_id")
    private Long spuId;

    /**
     * 商品名称
     */
    @TableField(value = "product_name")
    private String productName;

    /**
     * 商品图片
     */
    @TableField(value = "product_image")
    private String productImage;

    /**
     * SKU规格
     */
    @TableField(value = "sku_specs")
    private String skuSpecs;

    /**
     * 单价
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 数量
     */
    @TableField(value = "quantity")
    private Integer quantity;

    /**
     * 小计
     */
    @TableField(value = "total_amount")
    private BigDecimal totalAmount;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 是否赠品
     */
    @TableField(value = "gift_flag")
    private Integer giftFlag;
}