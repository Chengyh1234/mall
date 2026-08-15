package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车 Redis 缓存服务接口
 * <p>
 * 基于 Redis Hash 实现购物车缓存，采用 Write-Through 策略：
 * 写操作同步更新 Redis + MySQL，读操作优先走 Redis。
 * TTL 为 7 天，每次操作续期，缓存未命中时从 MySQL 回源。
 */
public interface CartCacheService {

    /**
     * 获取用户购物车列表（按更新时间倒序）
     */
    List<CartItem> getCart(Long userId);

    /**
     * 获取单个购物车项
     */
    CartItem getCartItem(Long userId, Long skuId);

    /**
     * 添加或更新购物车项到缓存
     */
    void addOrUpdateItem(Long userId, CartItem item);

    /**
     * 从缓存移除购物车项
     */
    void removeItem(Long userId, Long skuId);

    /**
     * 批量从缓存移除购物车项
     */
    void removeItems(Long userId, List<Long> skuIds);

    /**
     * 清空用户购物车缓存
     */
    void clearCart(Long userId);

    /**
     * 获取购物车商品种数
     */
    int getCartCount(Long userId);

    /**
     * 获取已选中的购物车商品
     */
    List<CartItem> getSelectedItems(Long userId);

    /**
     * 获取已选中商品数量
     */
    int getSelectedCount(Long userId);

    /**
     * 获取已选中商品总价
     */
    BigDecimal getSelectedTotalPrice(Long userId);

    /**
     * 从 MySQL 加载购物车数据到 Redis（缓存回源）
     *
     * @return 加载的购物车列表
     */
    List<CartItem> loadCartFromDb(Long userId);
}