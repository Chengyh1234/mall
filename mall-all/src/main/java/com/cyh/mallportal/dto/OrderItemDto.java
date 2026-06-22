package com.cyh.mallportal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项DTO
 * 用于订单创建时的商品项信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    /** SKU ID */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /** 数量 */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String productImage;

    /** SKU规格（JSON格式） */
    private String skuSpecs;

    /** 单价 */
    private BigDecimal price;

    /** 是否赠品 */
    private Integer giftFlag;
}