package com.cyh.mallorder.service;

import com.cyh.mallorder.dto.CartItemDto;
import com.cyh.mallorder.entity.CartItem;
import com.cyh.mallorder.vo.CartItemVo;

import java.util.List;

/**
 * 购物车服务接口
 * 提供购物车业务逻辑操作
 */
public interface CartItemService {

    /**
     * 添加商品到购物车
     * 如果商品已存在，则增加数量
     *
     * @param userId    用户ID
     * @param cartItemDto 购物车项DTO
     * @return 是否添加成功
     */
    boolean addToCart(Long userId, CartItemDto cartItemDto);

    /**
     * 更新购物车商品数量
     *
     * @param userId    用户ID
     * @param skuId     SKU ID
     * @param quantity  新的数量
     * @return 是否更新成功
     */
    boolean updateQuantity(Long userId, Long skuId, Integer quantity);

    /**
     * 设置商品选中状态
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param selected 是否选中（1-选中 0-未选）
     * @return 是否更新成功
     */
    boolean setSelected(Long userId, Long skuId, Integer selected);

    /**
     * 全选/取消全选
     *
     * @param userId   用户ID
     * @param selected 是否全选（1-全选 0-取消全选）
     * @return 是否更新成功
     */
    boolean setAllSelected(Long userId, Integer selected);

    /**
     * 从购物车移除商品
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 是否删除成功
     */
    boolean removeFromCart(Long userId, Long skuId);

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 是否清空成功
     */
    boolean clearCart(Long userId);

    /**
     * 清空已选中的商品
     *
     * @param userId 用户ID
     * @return 是否清空成功
     */
    boolean clearSelected(Long userId);

    /**
     * 获取用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表（实体）
     */
    List<CartItem> getCartList(Long userId);

    /**
     * 获取用户已选中的购物车商品
     *
     * @param userId 用户ID
     * @return 已选中的购物车列表（实体，用于结算）
     */
    List<CartItem> getSelectedItems(Long userId);

    /**
     * 获取购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品种类的数量
     */
    int getCartCount(Long userId);

    /**
     * 获取已选中商品数量
     *
     * @param userId 用户ID
     * @return 已选中商品数量
     */
    int getSelectedCount(Long userId);

    /**
     * 获取已选中商品的总价
     *
     * @param userId 用户ID
     * @return 已选中商品的总价
     */
    java.math.BigDecimal getSelectedTotalPrice(Long userId);

    /**
     * 获取用户购物车中的商品项
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 购物车项实体
     */
    CartItem getCartItem(Long userId, Long skuId);

    /**
     * 将购物车实体列表转换为VO列表（补充实时库存信息）
     *
     * @param items 购物车实体列表
     * @return 购物车VO列表
     */
    List<CartItemVo> toCartItemVoList(List<CartItem> items);
}