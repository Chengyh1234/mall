package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.entity.CartItem;
import com.cyh.mallportal.mapper.CartItemMapper;
import com.cyh.mallportal.service.CartCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.cyh.mallcommon.constant.RedisConstants;

/**
 * 购物车 Redis 缓存服务实现类
 * <p>
 * 【设计要点】
 * - 数据结构：Redis Hash
 *   Key: cart:user:{userId}
 *   Field: {skuId}
 *   Value: CartItem JSON（使用 StringRedisTemplate + ObjectMapper 手动序列化）
 * - TTL 策略：7 天，每次操作自动续期
 * - 缓存策略：Write-Through（写操作同步更新 Redis + MySQL）
 * - 回源策略：缓存未命中时从 MySQL 加载并填充缓存
 * <p>
 * 【为什么手动序列化】
 * Jackson2JsonRedisSerializer<Object> 反序列化时不知道具体类型，会将对象转成 LinkedHashMap。
 * GenericJackson2JsonRedisSerializer 虽然可以嵌入 @class，但存在类型注入安全风险。
 * 手动序列化的方式类型由代码显式控制，安全性最高，且数据是纯 JSON，不绑定 Java 类名。
 * <p>
 * 【优势】
 * - 读操作：O(1) ~ O(n)，直接从 Redis 获取，响应时间 < 3ms
 * - 写操作：同步更新 MySQL + Redis，保证数据一致性
 * - 内存友好：7天自动过期，避免僵尸数据占用内存
 * - 类型安全：手动序列化控制，不存在 Jackson 类型注入风险
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartCacheServiceImpl implements CartCacheService {

    /** StringRedisTemplate，所有 value 均为纯字符串，不存在类型注入风险 */
    private final StringRedisTemplate stringRedisTemplate;

    /** Jackson ObjectMapper，用于手动序列化/反序列化 CartItem */
    private final ObjectMapper objectMapper;

    /** 购物车数据访问层，用于缓存回源 */
    private final CartItemMapper cartItemMapper;

    /**
     * 构建购物车 Redis Key
     *
     * @param userId 用户ID
     * @return Redis Key，格式：cart:user:{userId}
     */
    private String getCartKey(Long userId) {
        return RedisConstants.CART_KEY_PREFIX + userId;
    }

    /**
     * 将 CartItem 序列化为 JSON 字符串
     *
     * @param item 购物车项
     * @return JSON 字符串，异常时返回 null
     */
    private String serialize(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            log.error("序列化 CartItem 失败, skuId: {}", item.getSkuId(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为 CartItem
     *
     * @param json JSON 字符串
     * @return CartItem 对象，异常时返回 null
     */
    private CartItem deserialize(String json) {
        try {
            return objectMapper.readValue(json, CartItem.class);
        } catch (JsonProcessingException e) {
            log.error("反序列化 CartItem 失败, json: {}", json, e);
            return null;
        }
    }

    /**
     * 获取用户完整购物车列表
     * <p>
     * 【执行流程】
     * Step1: 从 Redis Hash 获取所有购物车项
     * Step2: 缓存未命中 → 从 MySQL 回源并填充缓存
     * Step3: 按更新时间倒序排序（最近操作的在前）
     * Step4: 续期 TTL
     *
     * @param userId 用户ID
     * @return 购物车项列表（按更新时间倒序）
     */
    @Override
    public List<CartItem> getCart(Long userId) {
        String key = getCartKey(userId);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);

        // 缓存未命中，触发回源逻辑
        if (entries.isEmpty()) {
            log.debug("缓存未命中，从 MySQL 回源, userId: {}", userId);
            return loadCartFromDb(userId);
        }

        // 手动反序列化为 CartItem 列表
        List<CartItem> items = new ArrayList<>(entries.size());
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            // entry.getValue() 为 StringRedisTemplate 返回的 JSON 字符串
            String json = (String) entry.getValue();
            CartItem item = deserialize(json);
            if (item != null) {
                items.add(item);
            } else {
                log.warn("反序列化失败，移除无效缓存, skuId: {}", entry.getKey());
                // 清除无效数据，避免阻塞后续操作
                stringRedisTemplate.opsForHash().delete(key, entry.getKey());
            }
        }

        // 按更新时间倒序排列，保证最近添加的商品在前面
        items.sort((a, b) -> {
            if (a.getUpdatedAt() == null) return 1;
            if (b.getUpdatedAt() == null) return -1;
            return b.getUpdatedAt().compareTo(a.getUpdatedAt());
        });

        // 续期 TTL，延长用户活跃期
        stringRedisTemplate.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);

        log.debug("从缓存获取购物车列表成功, userId: {}, 商品数: {}", userId, items.size());
        return items;
    }

    /**
     * 获取单个购物车项
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 购物车项（不存在或反序列化失败返回 null）
     */
    @Override
    public CartItem getCartItem(Long userId, Long skuId) {
        String key = getCartKey(userId);
        String json = (String) stringRedisTemplate.opsForHash().get(key, String.valueOf(skuId));

        if (json == null) {
            return null;
        }

        CartItem item = deserialize(json);
        if (item != null) {
            stringRedisTemplate.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        }
        return item;
    }

    /**
     * 添加或更新购物车项到缓存
     * <p>
     * 【注意】此方法只更新缓存，需配合 MySQL 事务使用
     *
     * @param userId 用户ID
     * @param item   购物车项
     */
    @Override
    public void addOrUpdateItem(Long userId, CartItem item) {
        String key = getCartKey(userId);
        String json = serialize(item);
        if (json == null) {
            log.error("添加购物车缓存失败，序列化异常, userId: {}, skuId: {}", userId, item.getSkuId());
            return;
        }
        // 使用 skuId 作为 Hash Field，保证同 SKU 唯一
        stringRedisTemplate.opsForHash().put(key, String.valueOf(item.getSkuId()), json);
        // 续期 TTL，保持用户活跃状态
        stringRedisTemplate.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        log.debug("缓存购物车项成功, userId: {}, skuId: {}", userId, item.getSkuId());
    }

    /**
     * 从缓存移除单个购物车项
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     */
    @Override
    public void removeItem(Long userId, Long skuId) {
        String key = getCartKey(userId);
        stringRedisTemplate.opsForHash().delete(key, String.valueOf(skuId));
        log.debug("从缓存移除购物车项成功, userId: {}, skuId: {}", userId, skuId);
    }

    /**
     * 批量从缓存移除购物车项
     * <p>
     * 【适用场景】订单结算成功后，批量删除已选中商品
     *
     * @param userId 用户ID
     * @param skuIds SKU ID 列表
     */
    @Override
    public void removeItems(Long userId, List<Long> skuIds) {
        // 边界处理：空列表直接返回
        if (skuIds == null || skuIds.isEmpty()) {
            return;
        }
        String key = getCartKey(userId);
        String[] fields = skuIds.stream().map(String::valueOf).toArray(String[]::new);
        stringRedisTemplate.opsForHash().delete(key, (Object[]) fields);
        log.debug("批量移除购物车项成功, userId: {}, 数量: {}", userId, fields.length);
    }

    /**
     * 清空用户购物车缓存
     *
     * @param userId 用户ID
     */
    @Override
    public void clearCart(Long userId) {
        String key = getCartKey(userId);
        stringRedisTemplate.delete(key);
        log.debug("清空购物车缓存成功, userId: {}", userId);
    }

    /**
     * 获取购物车商品种数
     * <p>
     * 【性能优化】使用 HLEN 命令，O(1) 复杂度
     *
     * @param userId 用户ID
     * @return 商品种数
     */
    @Override
    public int getCartCount(Long userId) {
        String key = getCartKey(userId);
        Long size = stringRedisTemplate.opsForHash().size(key);

        // 缓存未命中，触发回源
        if (size == null || size == 0) {
            List<CartItem> items = loadCartFromDb(userId);
            return items.size();
        }

        stringRedisTemplate.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);
        return size.intValue();
    }

    /**
     * 获取已选中的购物车商品
     *
     * @param userId 用户ID
     * @return 已选中的购物车项列表
     */
    @Override
    public List<CartItem> getSelectedItems(Long userId) {
        return getCart(userId).stream()
                // 过滤选中状态为 1 的商品（1=选中，0=未选中）
                .filter(item -> item.getSelected() != null && item.getSelected() == 1)
                .collect(Collectors.toList());
    }

    /**
     * 获取已选中商品数量
     *
     * @param userId 用户ID
     * @return 已选中商品种数
     */
    @Override
    public int getSelectedCount(Long userId) {
        return getSelectedItems(userId).size();
    }

    /**
     * 计算已选中商品总价
     *
     * @param userId 用户ID
     * @return 已选中商品总价
     */
    @Override
    public BigDecimal getSelectedTotalPrice(Long userId) {
        List<CartItem> selectedItems = getSelectedItems(userId);
        return selectedItems.stream()
                // 过滤有效数据（价格和数量不为空）
                .filter(item -> item.getPrice() != null && item.getQuantity() != null)
                // 计算单商品总价：价格 * 数量
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                // 累加所有商品总价
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 从 MySQL 加载购物车数据到 Redis（缓存回源）
     * <p>
     * 【触发条件】
     * - 用户首次访问购物车
     * - 缓存过期后首次访问
     * - 缓存被手动清除后访问
     *
     * @param userId 用户ID
     * @return 加载的购物车列表
     */
    @Override
    public List<CartItem> loadCartFromDb(Long userId) {
        // Step1: 从 MySQL 查询购物车数据
        List<CartItem> items = cartItemMapper.selectByUserId(userId);

        // 边界处理：空数据直接返回
        if (items.isEmpty()) {
            log.debug("MySQL 购物车为空, userId: {}", userId);
            return items;
        }

        // Step2: 批量序列化并写入 Redis
        String key = getCartKey(userId);
        Map<String, String> hashEntries = new HashMap<>(items.size());

        for (CartItem item : items) {
            String json = serialize(item);
            if (json != null) {
                hashEntries.put(String.valueOf(item.getSkuId()), json);
            }
        }

        // 使用 putAll 批量写入，减少网络往返
        stringRedisTemplate.opsForHash().putAll(key, hashEntries);
        stringRedisTemplate.expire(key, RedisConstants.CART_TTL_DAYS, TimeUnit.DAYS);

        log.info("从 MySQL 加载购物车到 Redis 完成, userId: {}, 商品数: {}", userId, items.size());
        return items;
    }
}