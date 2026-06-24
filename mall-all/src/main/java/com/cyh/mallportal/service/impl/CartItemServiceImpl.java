package com.cyh.mallportal.service.impl;

import com.cyh.mallportal.dto.CartItemDto;
import com.cyh.mallportal.entity.CartItem;
import com.cyh.mallportal.entity.Sku;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.mapper.CartItemMapper;
import com.cyh.mallportal.mapper.SkuMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.service.CartCacheService;
import com.cyh.mallportal.service.CartItemService;
import com.cyh.mallportal.vo.CartItemVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 * <p>
 * 【核心设计】
 * - 读操作：优先从 Redis 缓存获取（通过 CartCacheService），未命中自动回源 MySQL
 * - 写操作：同步更新 MySQL + Redis，保证数据一致性
 * - 事务管理：所有写操作使用 @Transactional 注解，确保原子性
 * <p>
 * 【数据流向】
 * 添加商品 → MySQL insert → Redis HSET
 * 修改数量 → MySQL update → Redis HSET
 * 删除商品 → MySQL delete → Redis HDEL
 * 查询列表 → Redis HGETALL（未命中→MySQL→Redis填充）
 * <p>
 * 【性能优化】
 * - 购物车列表、数量、选中状态等读操作走缓存，响应时间 < 3ms
 * - 库存信息实时从 MySQL 查询（不缓存，因为库存变化频繁）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    /** 购物车数据访问层 */
    private final CartItemMapper cartItemMapper;

    /** SKU 数据访问层（用于库存校验） */
    private final SkuMapper skuMapper;

    /** SPU 数据访问层（用于商品名称快照） */
    private final SpuMapper spuMapper;

    /** Redis 缓存服务（加速读操作） */
    private final CartCacheService cartCacheService;

    /**
     * 添加商品到购物车
     * <p>
     * 【执行流程】
     * Step1: 查询 SKU 信息（校验商品存在、上架状态、库存）
     * Step2: 查询 SPU 信息（用于商品名称快照）
     * Step3: 查询购物车是否已存在该商品
     * Step4a: 已存在 → 增加数量（校验库存）→ 更新 MySQL + Redis
     * Step4b: 不存在 → 新建购物车项 → 插入 MySQL + Redis
     *
     * @param userId       用户ID
     * @param cartItemDto  购物车项DTO（包含skuId、quantity等）
     * @return true=成功，false=失败（SKU不存在/已下架/库存不足）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addToCart(Long userId, CartItemDto cartItemDto) {
        log.info("添加商品到购物车, 用户ID: {}, SKU ID: {}, 数量: {}",
                userId, cartItemDto.getSkuId(), cartItemDto.getQuantity());

        // Step1: 查询SKU信息，校验商品有效性
        Sku sku = skuMapper.selectById(cartItemDto.getSkuId());
        if (sku == null) {
            log.warn("SKU不存在: {}", cartItemDto.getSkuId());
            return false;
        }

        // Step2: 检查SKU是否上架（status=1为上架状态）
        if (sku.getStatus() != 1) {
            log.warn("SKU已下架: {}", cartItemDto.getSkuId());
            return false;
        }

        // Step3: 检查库存是否充足
        if (sku.getStock() <= 0) {
            log.warn("SKU库存不足: {}", cartItemDto.getSkuId());
            return false;
        }

        // Step4: 查询SPU信息（用于商品名称快照，避免后续查询）
        Spu spu = spuMapper.selectById(sku.getSpuId());

        // Step5: 查询购物车是否已存在该商品
        CartItem existItem = cartItemMapper.selectByUserIdAndSkuId(userId, cartItemDto.getSkuId());

        if (existItem != null) {
            // Step6: 已存在，增加数量
            int newQuantity = existItem.getQuantity() + (cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            if (newQuantity > sku.getStock()) {
                log.warn("购物车中商品数量超过库存: SKU {}, 库存: {}, 尝试数量: {}",
                        cartItemDto.getSkuId(), sku.getStock(), newQuantity);
                return false;
            }
            existItem.setQuantity(newQuantity);
            existItem.setUpdatedAt(LocalDateTime.now());
            // 更新可选字段（图片、规格、备注）
            if (cartItemDto.getProductImage() != null) {
                existItem.setProductImage(cartItemDto.getProductImage());
            }
            if (cartItemDto.getSkuSpecs() != null) {
                existItem.setSkuSpecs(cartItemDto.getSkuSpecs());
            }
            if (cartItemDto.getNotes() != null) {
                existItem.setNotes(cartItemDto.getNotes());
            }
            cartItemMapper.updateById(existItem);

            // 同步更新 Redis 缓存
            cartCacheService.addOrUpdateItem(userId, existItem);

            log.info("更新购物车商品数量成功, 购物车项ID: {}, 新数量: {}", existItem.getId(), newQuantity);
        } else {
            // Step7: 不存在，新增购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setSkuId(cartItemDto.getSkuId());
            cartItem.setQuantity(cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            cartItem.setSelected(1); // 默认选中状态
            if (spu != null) {
                cartItem.setProductName(spu.getName());
            }
            // 优先使用DTO中的图片，否则使用SKU默认图片
            cartItem.setProductImage(
                    cartItemDto.getProductImage() != null ? cartItemDto.getProductImage() : sku.getImage());
            cartItem.setSkuSpecs(cartItemDto.getSkuSpecs());
            cartItem.setPrice(sku.getPrice());
            cartItem.setNotes(cartItemDto.getNotes());
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());

            cartItemMapper.insert(cartItem);

            // 同步更新 Redis 缓存
            cartCacheService.addOrUpdateItem(userId, cartItem);

            log.info("添加购物车商品成功, ID: {}", cartItem.getId());
        }

        return true;
    }

    /**
     * 更新购物车商品数量
     * <p>
     * 【特殊处理】
     * - quantity <= 0 时，自动调用 removeFromCart 删除该商品
     * - quantity > 库存时，返回失败
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param quantity 新数量
     * @return true=成功，false=失败（商品不存在/库存不足）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuantity(Long userId, Long skuId, Integer quantity) {
        log.info("更新购物车商品数量, 用户ID: {}, SKU ID: {}, 新数量: {}", userId, skuId, quantity);

        // 查询购物车项
        CartItem cartItem = cartItemMapper.selectByUserIdAndSkuId(userId, skuId);
        if (cartItem == null) {
            log.warn("购物车商品不存在");
            return false;
        }

        // 数量 <= 0 时，删除该商品
        if (quantity <= 0) {
            return removeFromCart(userId, skuId);
        }

        // 校验库存
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || quantity > sku.getStock()) {
            log.warn("库存不足, SKU: {}, 库存: {}, 尝试数量: {}", skuId, sku != null ? sku.getStock() : 0, quantity);
            return false;
        }

        // 更新数量
        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);

        // 同步更新 Redis 缓存
        cartCacheService.addOrUpdateItem(userId, cartItem);

        log.info("更新购物车商品数量成功");
        return true;
    }

    /**
     * 设置单个商品的选中状态
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param selected 选中状态（1=选中，0=未选中）
     * @return true=成功，false=失败（商品不存在）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setSelected(Long userId, Long skuId, Integer selected) {
        log.info("设置购物车商品选中状态, 用户ID: {}, SKU ID: {}, selected: {}", userId, skuId, selected);

        CartItem cartItem = cartItemMapper.selectByUserIdAndSkuId(userId, skuId);
        if (cartItem == null) {
            log.warn("购物车商品不存在");
            return false;
        }

        cartItem.setSelected(selected);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);

        // 同步更新 Redis 缓存
        cartCacheService.addOrUpdateItem(userId, cartItem);

        return true;
    }

    /**
     * 设置购物车全选/全不选状态
     * <p>
     * 【优化】从缓存获取购物车列表，避免 MySQL 查询
     *
     * @param userId   用户ID
     * @param selected 选中状态（1=全选，0=全不选）
     * @return true=成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setAllSelected(Long userId, Integer selected) {
        log.info("设置购物车全选状态, 用户ID: {}, selected: {}", userId, selected);

        // 从缓存获取购物车列表（性能优化）
        List<CartItem> items = cartCacheService.getCart(userId);
        for (CartItem item : items) {
            item.setSelected(selected);
            item.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.updateById(item);
            cartCacheService.addOrUpdateItem(userId, item);
        }

        log.info("全选状态更新成功, 更新数量: {}", items.size());
        return true;
    }

    /**
     * 从购物车移除单个商品
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return true=成功，false=失败（商品不存在）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeFromCart(Long userId, Long skuId) {
        log.info("从购物车移除商品, 用户ID: {}, SKU ID: {}", userId, skuId);

        CartItem cartItem = cartItemMapper.selectByUserIdAndSkuId(userId, skuId);
        if (cartItem == null) {
            log.warn("购物车商品不存在");
            return false;
        }

        // 删除 MySQL 记录
        cartItemMapper.deleteById(cartItem.getId());

        // 同步移除 Redis 缓存
        cartCacheService.removeItem(userId, skuId);

        log.info("移除购物车商品成功");
        return true;
    }

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return true=成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearCart(Long userId) {
        log.info("清空购物车, 用户ID: {}", userId);

        // 删除 MySQL 所有记录
        int count = cartItemMapper.deleteByUserId(userId);

        // 同步清空 Redis 缓存
        cartCacheService.clearCart(userId);

        log.info("清空购物车成功, 删除数量: {}", count);
        return true;
    }

    /**
     * 清空已选中的商品（订单结算后调用）
     *
     * @param userId 用户ID
     * @return true=成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearSelected(Long userId) {
        log.info("清空已选中的购物车商品, 用户ID: {}", userId);

        // 从缓存获取已选中列表（性能优化）
        List<CartItem> selectedItems = cartCacheService.getSelectedItems(userId);
        if (selectedItems.isEmpty()) {
            return true;
        }

        // 批量删除 MySQL 中已选中的记录
        int count = cartItemMapper.deleteSelectedByUserId(userId);

        // 同步从 Redis 批量移除
        List<Long> skuIds = selectedItems.stream()
                .map(CartItem::getSkuId)
                .collect(Collectors.toList());
        cartCacheService.removeItems(userId, skuIds);

        log.info("清空选中商品成功, 删除数量: {}", count);
        return true;
    }

    /**
     * 获取购物车列表（按更新时间倒序）
     * <p>
     * 【缓存策略】优先从 Redis 获取，未命中自动回源 MySQL
     *
     * @param userId 用户ID
     * @return 购物车项列表
     */
    @Override
    public List<CartItem> getCartList(Long userId) {
        log.info("获取购物车列表, 用户ID: {}", userId);
        return cartCacheService.getCart(userId);
    }

    /**
     * 获取已选中的购物车商品
     *
     * @param userId 用户ID
     * @return 已选中的购物车项列表
     */
    @Override
    public List<CartItem> getSelectedItems(Long userId) {
        log.info("获取已选中的购物车商品, 用户ID: {}", userId);
        return cartCacheService.getSelectedItems(userId);
    }

    /**
     * 获取购物车商品种数
     *
     * @param userId 用户ID
     * @return 商品种数
     */
    @Override
    public int getCartCount(Long userId) {
        return cartCacheService.getCartCount(userId);
    }

    /**
     * 获取已选中商品数量
     *
     * @param userId 用户ID
     * @return 已选中商品种数
     */
    @Override
    public int getSelectedCount(Long userId) {
        return cartCacheService.getSelectedCount(userId);
    }

    /**
     * 获取已选中商品总价
     *
     * @param userId 用户ID
     * @return 已选中商品总价
     */
    @Override
    public BigDecimal getSelectedTotalPrice(Long userId) {
        return cartCacheService.getSelectedTotalPrice(userId);
    }

    /**
     * 获取单个购物车项
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 购物车项（不存在返回 null）
     */
    @Override
    public CartItem getCartItem(Long userId, Long skuId) {
        return cartCacheService.getCartItem(userId, skuId);
    }

    /**
     * 将购物车项列表转换为 VO 列表（用于接口返回）
     * <p>
     * 【关键处理】
     * - 商品快照信息从缓存获取（CartItem）
     * - 实时库存从 MySQL 查询（不缓存，因为库存变化频繁）
     *
     * @param items 购物车项列表
     * @return VO 列表（包含实时库存）
     */
    @Override
    public List<CartItemVo> toCartItemVoList(List<CartItem> items) {
        return items.stream().map(item -> {
            // 从 CartItem 转换为 CartItemVo
            CartItemVo vo = CartItemVo.fromCartItem(item);

            // 实时库存从 DB 查询（不缓存）
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                vo.setStock(sku.getStock());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}