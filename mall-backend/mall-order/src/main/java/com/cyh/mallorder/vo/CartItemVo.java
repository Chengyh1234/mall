package com.cyh.mallorder.vo;

/**
 * 购物车商品 VO
 * 返回给前端展示购物车列表，包含商品实时信息
 */
import com.cyh.mallorder.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 购物车项VO
 * 包含购物车信息和商品详情
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemVo {

    /**
     * 购物车项ID
     */
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否选中: 1-选中 0-未选
     */
    private Integer selected;

    /**
     * 商品名称（快照）
     */
    private String productName;

    /**
     * 商品图片（快照）
     */
    private String productImage;

    /**
     * SKU规格（快照）
     */
    private String skuSpecs;

    /**
     * 单价（快照）
     */
    private BigDecimal price;

    /**
     * 小计金额（数量 × 单价）
     */
    private BigDecimal subtotal;

    /**
     * 备注
     */
    private String notes;

    /**
     * 失效时间
     */
    private String expireTime;

    /** SKU库存（实时查询） */
    private Integer stock;

    /**
     * 从 CartItem 实体构建 CartItemVo
     */
    public static CartItemVo fromCartItem(CartItem cartItem) {
        CartItemVo vo = new CartItemVo();
        vo.setId(cartItem.getId());
        vo.setSkuId(cartItem.getSkuId());
        vo.setSpuId(cartItem.getSpuId());
        vo.setQuantity(cartItem.getQuantity());
        vo.setSelected(cartItem.getSelected());
        vo.setProductName(cartItem.getProductName());
        vo.setProductImage(cartItem.getProductImage());
        vo.setSkuSpecs(cartItem.getSkuSpecs());
        vo.setPrice(cartItem.getPrice());
        vo.setSubtotal(cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        vo.setNotes(cartItem.getNotes());
        vo.setExpireTime(cartItem.getExpireTime() != null ? cartItem.getExpireTime().toString() : null);
        return vo;
    }
}