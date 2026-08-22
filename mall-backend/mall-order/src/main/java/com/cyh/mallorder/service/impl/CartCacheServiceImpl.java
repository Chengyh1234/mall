package com.cyh.mallorder.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallorder.entity.CartItem;
import com.cyh.mallorder.mapper.CartItemMapper;
import com.cyh.mallorder.service.CartCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 购物车 Redis 缓存服务实现类
 * <p>
 * 基于 Redis Hash 实现购物车缓存，采用 Write-Through 策略：
 * 写操作同步更新 Redis + MySQL，读操作优先走 Redis。
 * TTL 为 7 天，每次操作续期，缓存未命中时从 MySQL 回源。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartCacheServiceImpl implements CartCacheService {

    private final RedisUtils redisUtils;
    private final CartItemMapper cartItemMapper;

    private String getCartKey(Long userId) {
        return RedisConstants.CART_KEY_PREFIX + userId;
    }

    /**
     * 获取用户购物车列表（优先读 Redis 缓存，未命中时从 MySQL 回源）
     * <p>
     * 缓存基于 Redis Hash 结构，以 SKU ID 为 field，CartItem JSON 为 value。
     * 返回结果按更新时间降序排列，每次读取重置 TTL 为 7 天。
     * 若缓存中有反序列化失败的条目，自动移除无效缓存。
     */
    @Override
    public List<CartItem> getCart(Long userId) {
        String key = getCartKey(userId);
        Map<String, String> entries = redisUtils.hGetAll(key);

        if (entries.isEmpty()) {
            log.debug("缓存未命中，从 MySQL 回源, userId: {}", userId);
            return loadCartFromDb(userId);
        }

        List<CartItem> items = new ArrayList<>(entries.size());
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            CartItem item = redisUtils.fromJson(entry.getValue(), CartItem.class);
            if (item != null) {
                items.add(item);
            } else {
                log.warn("反序列化失败，移除无效缓存, skuId: {}", entry.getKey());
                redisUtils.hDelete(key, entry.getKey());
            }
        }

        items.sort((a, b) -> {
            if (a.getUpdatedAt() == null) return 1;
            if (b.getUpdatedAt() == null) return -1;
            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
        });

        redisUtils.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        log.debug("从缓存获取购物车列表成功, userId: {}, 商品数: {}", userId, items.size());
        return items;
    }

    /**
     * 从缓存获取购物车中指定 SKU 的商品信息
     * 缓存未命中时返回 null（不自动回源）。
     */
    @Override
    public CartItem getCartItem(Long userId, Long skuId) {
        String key = getCartKey(userId);
        String json = redisUtils.hGet(key, String.valueOf(skuId));

        if (json == null) {
            return null;
        }

        CartItem item = redisUtils.fromJson(json, CartItem.class);
        if (item != null) {
            redisUtils.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        }
        return item;
    }

    /**
     * 添加或更新购物车项缓存（Write-Through 策略）
     * 将 CartItem 序列化为 JSON 后写入 Redis Hash，并重置 TTL。
     */
    @Override
    public void addOrUpdateItem(Long userId, CartItem item) {
        String key = getCartKey(userId);
        String json = redisUtils.toJson(item);
        if (json == null) {
            log.error("添加购物车缓存失败，序列化异常, userId: {}, skuId: {}", userId, item.getSkuId());
            return;
        }
        redisUtils.hSet(key, String.valueOf(item.getSkuId()), json);
        redisUtils.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        log.debug("缓存购物车项成功, userId: {}, skuId: {}", userId, item.getSkuId());
    }

    /**
     * 从缓存移除单个购物车项
     */
    @Override
    public void removeItem(Long userId, Long skuId) {
        String key = getCartKey(userId);
        redisUtils.hDelete(key, String.valueOf(skuId));
        log.debug("从缓存移除购物车项成功, userId: {}, skuId: {}", userId, skuId);
    }

    /**
     * 从缓存批量移除多个购物车项
     */
    @Override
    public void removeItems(Long userId, List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        String key = getCartKey(userId);
        String[] fields = skuIds.stream().map(String::valueOf).toArray(String[]::new);
        redisUtils.hDelete(key, fields);
        log.debug("批量移除购物车项成功, userId: {}, 数量: {}", userId, fields.length);
    }

    /**
     * 清空用户购物车缓存
     */
    @Override
    public void clearCart(Long userId) {
        String key = getCartKey(userId);
        redisUtils.delete(key);
        log.debug("清空购物车缓存成功, userId: {}", userId);
    }

    /**
     * 获取购物车商品数量（优先从 Redis Hash 的 size 获取，未命中时回源 MySQL）
     */
    @Override
    public int getCartCount(Long userId) {
        String key = getCartKey(userId);
        Long size = redisUtils.hSize(key);

        if (size == null || size == 0) {
            List<CartItem> items = loadCartFromDb(userId);
            return items.size();
        }

        redisUtils.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        return size.intValue();
    }

    /**
     * 获取购物车中已选中的商品列表
     * 从缓存中全量读取后按 selected 字段过滤。
     */
    @Override
    public List<CartItem> getSelectedItems(Long userId) {
        return getCart(userId).stream()
                .filter(item -> item.getSelected() != null && item.getSelected() == 1)
                .collect(Collectors.toList());
    }

    /**
     * 获取购物车中已选中商品的数量
     */
    @Override
    public int getSelectedCount(Long userId) {
        return getSelectedItems(userId).size();
    }

    /**
     * 计算已选中商品的总价（单价 × 数量之和）
     */
    @Override
    public BigDecimal getSelectedTotalPrice(Long userId) {
        List<CartItem> selectedItems = getSelectedItems(userId);
        return selectedItems.stream()
                .filter(item -> item.getPrice() != null && item.getQuantity() != null)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 从 MySQL 加载购物车数据到 Redis 缓存（回源操作）
     * 缓存未命中时调用，将数据库中该用户的购物车记录批量写入 Redis Hash 并设置 TTL。
     */
    @Override
    public List<CartItem> loadCartFromDb(Long userId) {
        List<CartItem> items = cartItemMapper.selectByUserId(userId);

        if (items.isEmpty()) {
            log.debug("MySQL 购物车为空, userId: {}", userId);
            return items;
        }

        String key = getCartKey(userId);
        Map<String, String> hashEntries = new HashMap<>(items.size());

        for (CartItem item : items) {
            String json = redisUtils.toJson(item);
            if (json != null) {
                hashEntries.put(String.valueOf(item.getSkuId()), json);
            }
        }

        redisUtils.hPutAll(key, hashEntries);
        redisUtils.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);

        log.info("从 MySQL 加载购物车到 Redis 完成, userId: {}, 商品数: {}", userId, items.size());
        return items;
    }
}