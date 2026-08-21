package com.cyh.mallproduct.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU实体类
 * 商品库存单元（Stock Keeping Unit）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sku", autoResultMap = true)
public class Sku implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联SPU ID
     */
    @TableField(value = "spu_id")
    private Long spuId;

    /**
     * 销售价格
     */
    @TableField(value = "price")
    private BigDecimal price;

    /**
     * 市场价
     */
    @TableField(value = "market_price")
    private BigDecimal marketPrice;

    /**
     * 成本价
     */
    @TableField(value = "cost_price")
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    @TableField(value = "stock")
    private Integer stock;

    /**
     * 冻结库存（预扣，待支付）
     */
    @TableField(value = "frozen_stock")
    private Integer frozenStock;

    /**
     * 预警库存
     */
    @TableField(value = "warn_stock")
    private Integer warnStock;

    /**
     * 图片路径
     */
    @TableField(value = "image")
    private String image;

    /**
     * 规格（JSON格式）
     */
    //@TableField(value = "specs")
    //private String specs;

    /**
     * 重量（kg）
     */
    @TableField(value = "weight")
    private BigDecimal weight;

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