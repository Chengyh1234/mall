package com.cyh.mallorder.service.impl;

import com.cyh.mallcommon.dto.SkuInfoDto;
import com.cyh.mallcommon.dto.SpuInfoDto;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallorder.dto.CartItemDto;
import com.cyh.mallorder.entity.CartItem;
import com.cyh.mallorder.feign.SkuClient;
import com.cyh.mallorder.mapper.CartItemMapper;
import com.cyh.mallorder.service.CartCacheService;
import com.cyh.mallorder.service.CartItemService;
import com.cyh.mallorder.vo.CartItemVo;
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
 * 通过 Feign 调用 mall-product 获取 SKU/SPU 数据，替代直连数据库。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemMapper cartItemMapper;
    private final SkuClient skuClient;
    private final CartCacheService cartCacheService;

    /**
     * 添加商品到购物车
     * <p>
     * 通过 Feign 校验 SKU 上架状态和库存，若购物车已存在该商品则累加数量（不超过库存上限）。
     * 同步更新 MySQL 和 Redis 缓存（Write-Through 策略）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addToCart(Long userId, CartItemDto cartItemDto) {
        log.info("添加商品到购物车, 用户ID: {}, SKU ID: {}, 数量: {}",
                userId, cartItemDto.getSkuId(), cartItemDto.getQuantity());

        // 通过 Feign 查询 SKU 信息
        Result<SkuInfoDto> skuResult = skuClient.getSkuDetail(cartItemDto.getSkuId());
        if (skuResult == null || skuResult.getData() == null) {
            log.warn("SKU不存在: {}", cartItemDto.getSkuId());
            return false;
        }
        SkuInfoDto sku = skuResult.getData();

        // 检查SKU是否上架
        if (sku.getStatus() != 1) {
            log.warn("SKU已下架: {}", cartItemDto.getSkuId());
            return false;
        }

        // 检查库存
        if (sku.getStock() <= 0) {
            log.warn("SKU库存不足: {}", cartItemDto.getSkuId());
            return false;
        }

        // 查询 SPU 信息（用于商品名称）
        Result<SpuInfoDto> spuResult = skuClient.getSpuDetail(sku.getSpuId());
        SpuInfoDto spu = (spuResult != null) ? spuResult.getData() : null;

        // 查询购物车是否已存在该商品
        CartItem existItem = cartItemMapper.selectByUserIdAndSkuId(userId, cartItemDto.getSkuId());

        if (existItem != null) {
            int newQuantity = existItem.getQuantity() + (cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            if (newQuantity > sku.getStock()) {
                log.warn("购物车中商品数量超过库存: SKU {}, 库存: {}, 尝试数量: {}",
                        cartItemDto.getSkuId(), sku.getStock(), newQuantity);
                return false;
            }
            existItem.setQuantity(newQuantity);
            existItem.setUpdatedAt(LocalDateTime.now());
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
            cartCacheService.addOrUpdateItem(userId, existItem);
            log.info("更新购物车商品数量成功, 购物车项ID: {}, 新数量: {}", existItem.getId(), newQuantity);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setSkuId(cartItemDto.getSkuId());
            cartItem.setQuantity(cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            cartItem.setSelected(1);
            cartItem.setSpuId(sku.getSpuId());
            if (spu != null) {
                cartItem.setProductName(spu.getName());
            }
            cartItem.setProductImage(
                    cartItemDto.getProductImage() != null ? cartItemDto.getProductImage() : sku.getImage());
            cartItem.setSkuSpecs(cartItemDto.getSkuSpecs());
            cartItem.setPrice(sku.getPrice());
            cartItem.setNotes(cartItemDto.getNotes());
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());

            cartItemMapper.insert(cartItem);
            cartCacheService.addOrUpdateItem(userId, cartItem);
            log.info("添加购物车商品成功, ID: {}", cartItem.getId());
        }

        return true;
    }

    /**
     * 更新购物车商品数量
     * 通过 Feign 校验库存，数量 ≤ 0 时自动移除商品。同步更新 MySQL 和 Redis 缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuantity(Long userId, Long skuId, Integer quantity) {
        log.info("更新购物车商品数量, 用户ID: {}, SKU ID: {}, 新数量: {}", userId, skuId, quantity);

        CartItem cartItem = cartItemMapper.selectByUserIdAndSkuId(userId, skuId);
        if (cartItem == null) {
            log.warn("购物车商品不存在");
            return false;
        }

        if (quantity <= 0) {
            return removeFromCart(userId, skuId);
        }

        // 通过 Feign 校验库存
        Result<SkuInfoDto> skuResult = skuClient.getSkuDetail(skuId);
        SkuInfoDto sku = (skuResult != null) ? skuResult.getData() : null;
        if (sku == null || quantity > sku.getStock()) {
            log.warn("库存不足, SKU: {}, 库存: {}, 尝试数量: {}", skuId, sku != null ? sku.getStock() : 0, quantity);
            return false;
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);
        cartCacheService.addOrUpdateItem(userId, cartItem);

        log.info("更新购物车商品数量成功");
        return true;
    }

    /**
     * 设置购物车商品选中状态
     * 同步更新 MySQL 和 Redis 缓存。
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
        cartCacheService.addOrUpdateItem(userId, cartItem);

        return true;
    }

    /**
     * 设置购物车全选/取消全选
     * 遍历用户所有购物车项，统一更新选中状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setAllSelected(Long userId, Integer selected) {
        log.info("设置购物车全选状态, 用户ID: {}, selected: {}", userId, selected);

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
     * 从购物车移除商品
     * 同步删除 MySQL 记录和 Redis 缓存中的购物车项。
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

        cartItemMapper.deleteById(cartItem.getId());
        cartCacheService.removeItem(userId, skuId);

        log.info("移除购物车商品成功");
        return true;
    }

    /**
     * 清空购物车
     * 同步删除该用户所有 MySQL 购物车记录和 Redis 缓存。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearCart(Long userId) {
        log.info("清空购物车, 用户ID: {}", userId);

        int count = cartItemMapper.deleteByUserId(userId);
        cartCacheService.clearCart(userId);

        log.info("清空购物车成功, 删除数量: {}", count);
        return true;
    }

    /**
     * 清空购物车中已选中的商品（下单后调用）
     * 同步删除 MySQL 和 Redis 中已选中的购物车项。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearSelected(Long userId) {
        log.info("清空已选中的购物车商品, 用户ID: {}", userId);

        List<CartItem> selectedItems = cartCacheService.getSelectedItems(userId);
        if (selectedItems.isEmpty()) {
            return true;
        }

        int count = cartItemMapper.deleteSelectedByUserId(userId);

        List<Long> skuIds = selectedItems.stream()
                .map(CartItem::getSkuId)
                .collect(Collectors.toList());
        cartCacheService.removeItems(userId, skuIds);

        log.info("清空选中商品成功, 删除数量: {}", count);
        return true;
    }

    /**
     * 获取购物车列表（从 Redis 缓存读取）
     */
    @Override
    public List<CartItem> getCartList(Long userId) {
        log.info("获取购物车列表, 用户ID: {}", userId);
        return cartCacheService.getCart(userId);
    }

    /**
     * 获取已选中的购物车商品
     */
    @Override
    public List<CartItem> getSelectedItems(Long userId) {
        log.info("获取已选中的购物车商品, 用户ID: {}", userId);
        return cartCacheService.getSelectedItems(userId);
    }

    /**
     * 获取购物车商品总数
     */
    @Override
    public int getCartCount(Long userId) {
        return cartCacheService.getCartCount(userId);
    }

    /**
     * 获取购物车中已选中商品的数量
     */
    @Override
    public int getSelectedCount(Long userId) {
        return cartCacheService.getSelectedCount(userId);
    }

    /**
     * 计算已选中商品的总价
     */
    @Override
    public BigDecimal getSelectedTotalPrice(Long userId) {
        return cartCacheService.getSelectedTotalPrice(userId);
    }

    /**
     * 获取购物车中指定 SKU 的商品信息
     */
    @Override
    public CartItem getCartItem(Long userId, Long skuId) {
        return cartCacheService.getCartItem(userId, skuId);
    }

    /**
     * 将购物车实体列表转为 VO 列表（补充实时库存）
     * 通过 Feign 获取每个 SKU 的实时库存数据，组装到 VO 中返回。
     */
    @Override
    public List<CartItemVo> toCartItemVoList(List<CartItem> items) {
        return items.stream().map(item -> {
            CartItemVo vo = CartItemVo.fromCartItem(item);

            // 通过 Feign 获取实时库存
            try {
                Result<SkuInfoDto> skuResult = skuClient.getSkuDetail(item.getSkuId());
                if (skuResult != null && skuResult.getData() != null) {
                    vo.setStock(skuResult.getData().getStock());
                }
            } catch (Exception e) {
                log.warn("获取SKU实时库存失败, skuId: {}", item.getSkuId(), e);
            }

            return vo;
        }).collect(Collectors.toList());
    }
}