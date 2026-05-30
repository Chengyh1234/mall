package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 购物车项DTO
 * 用于添加/更新购物车时传递参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {

    /**
     * SKU ID（添加购物车时必填）
     */
    private Long skuId;

    /**
     * 数量（默认为1）
     */
    private Integer quantity;

    /**
     * 商品主图路径（前端传入，作为快照保存）
     */
    private String productImage;

    /**
     * SKU规格描述快照（如："颜色:黑色 / 尺寸:XL"）
     */
    private String skuSpecs;

    /**
     * 备注（如：送礼需要精美包装）
     */
    private String notes;
}