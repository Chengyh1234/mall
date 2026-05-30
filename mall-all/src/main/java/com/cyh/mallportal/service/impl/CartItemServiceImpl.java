package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.dto.CartItemDto;
import com.cyh.mallportal.entity.CartItem;
import com.cyh.mallportal.entity.Sku;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.mapper.CartItemMapper;
import com.cyh.mallportal.mapper.SkuMapper;
import com.cyh.mallportal.mapper.SpuMapper;
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
 * 提供购物车业务逻辑的具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemMapper cartItemMapper;
    private final SkuMapper skuMapper;
    private final SpuMapper spuMapper;

    /**
     * 添加商品到购物车
     * 如果商品已存在，则增加数量
     *
     * @param userId      用户ID
     * @param cartItemDto 购物车项DTO
     * @return 是否添加成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addToCart(Long userId, CartItemDto cartItemDto) {
        log.info("添加商品到购物车, 用户ID: {}, SKU ID: {}, 数量: {}",
                userId, cartItemDto.getSkuId(), cartItemDto.getQuantity());

        // 1. 查询SKU信息
        Sku sku = skuMapper.selectById(cartItemDto.getSkuId());
        if (sku == null) {
            log.warn("SKU不存在: {}", cartItemDto.getSkuId());
            return false;
        }

        // 2. 检查SKU是否上架
        if (sku.getStatus() != 1) {
            log.warn("SKU已下架: {}", cartItemDto.getSkuId());
            return false;
        }

        // 3. 检查库存
        if (sku.getStock() <= 0) {
            log.warn("SKU库存不足: {}", cartItemDto.getSkuId());
            return false;
        }

        // 4. 查询SPU信息（用于快照）
        Spu spu = spuMapper.selectById(sku.getSpuId());

        // 5. 查询是否已存在于购物车
        CartItem existItem = cartItemMapper.selectByUserIdAndSkuId(userId, cartItemDto.getSkuId());

        if (existItem != null) {
            // 6. 已存在，增加数量
            int newQuantity = existItem.getQuantity() + (cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            // 检查库存
            if (newQuantity > sku.getStock()) {
                log.warn("购物车中商品数量超过库存: SKU {}, 库存: {}, 尝试数量: {}",
                        cartItemDto.getSkuId(), sku.getStock(), newQuantity);
                return false;
            }
            existItem.setQuantity(newQuantity);
            existItem.setUpdatedAt(LocalDateTime.now());
            // 如果传入了主图或规格，更新快照
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
            log.info("更新购物车商品数量成功, 购物车项ID: {}, 新数量: {}", existItem.getId(), newQuantity);
        } else {
            // 7. 不存在，新增购物车项
            CartItem cartItem = new CartItem();
            cartItem.setUserId(userId);
            cartItem.setSkuId(cartItemDto.getSkuId());
            cartItem.setQuantity(cartItemDto.getQuantity() != null ? cartItemDto.getQuantity() : 1);
            cartItem.setSelected(1); // 默认选中
            if (spu != null) {
                cartItem.setProductName(spu.getName());
            }
            // 主图：优先使用前端传入，否则回退到 SKU 的图片
            cartItem.setProductImage(
                    cartItemDto.getProductImage() != null ? cartItemDto.getProductImage() : sku.getImage());
            // 规格：优先使用前端传入的快照
            cartItem.setSkuSpecs(cartItemDto.getSkuSpecs());
            cartItem.setPrice(sku.getPrice());
            cartItem.setNotes(cartItemDto.getNotes());
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());

            cartItemMapper.insert(cartItem);
            log.info("添加购物车商品成功, ID: {}", cartItem.getId());
        }

        return true;
    }

    /**
     * 更新购物车商品数量
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param quantity 新的数量
     * @return 是否更新成功
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
            // 数量小于等于0时，移除商品
            return removeFromCart(userId, skuId);
        }

        // 检查库存
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || quantity > sku.getStock()) {
            log.warn("库存不足, SKU: {}, 库存: {}, 尝试数量: {}", skuId, sku != null ? sku.getStock() : 0, quantity);
            return false;
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemMapper.updateById(cartItem);

        log.info("更新购物车商品数量成功");
        return true;
    }

    /**
     * 设置商品选中状态
     *
     * @param userId   用户ID
     * @param skuId    SKU ID
     * @param selected 是否选中（1-选中 0-未选）
     * @return 是否更新成功
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

        return true;
    }

    /**
     * 全选/取消全选
     *
     * @param userId   用户ID
     * @param selected 是否全选（1-全选 0-取消全选）
     * @return 是否更新成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setAllSelected(Long userId, Integer selected) {
        log.info("设置购物车全选状态, 用户ID: {}, selected: {}", userId, selected);

        LambdaQueryWrapper<CartItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CartItem::getUserId, userId);

        List<CartItem> items = cartItemMapper.selectList(wrapper);
        for (CartItem item : items) {
            item.setSelected(selected);
            item.setUpdatedAt(LocalDateTime.now());
            cartItemMapper.updateById(item);
        }

        log.info("全选状态更新成功, 更新数量: {}", items.size());
        return true;
    }

    /**
     * 从购物车移除商品
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 是否删除成功
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
        log.info("移除购物车商品成功");
        return true;
    }

    /**
     * 清空购物车
     *
     * @param userId 用户ID
     * @return 是否清空成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearCart(Long userId) {
        log.info("清空购物车, 用户ID: {}", userId);
        int count = cartItemMapper.deleteByUserId(userId);
        log.info("清空购物车成功, 删除数量: {}", count);
        return true;
    }

    /**
     * 清空已选中的商品
     *
     * @param userId 用户ID
     * @return 是否清空成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean clearSelected(Long userId) {
        log.info("清空已选中的购物车商品, 用户ID: {}", userId);
        int count = cartItemMapper.deleteSelectedByUserId(userId);
        log.info("清空选中商品成功, 删除数量: {}", count);
        return true;
    }

    /**
     * 获取用户购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表（包含商品详情）
     */
    @Override
    public List<CartItemVo> getCartList(Long userId) {
        log.info("获取购物车列表, 用户ID: {}", userId);

        List<CartItem> items = cartItemMapper.selectByUserId(userId);
        return convertToVoList(items);
    }

    /**
     * 获取用户已选中的购物车商品
     *
     * @param userId 用户ID
     * @return 已选中的购物车列表（用于结算）
     */
    @Override
    public List<CartItemVo> getSelectedItems(Long userId) {
        log.info("获取已选中的购物车商品, 用户ID: {}", userId);

        List<CartItem> items = cartItemMapper.selectSelectedByUserId(userId);
        return convertToVoList(items);
    }

    /**
     * 获取购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品种类的数量
     */
    @Override
    public int getCartCount(Long userId) {
        return cartItemMapper.countByUserId(userId);
    }

    /**
     * 获取已选中商品数量
     *
     * @param userId 用户ID
     * @return 已选中商品数量
     */
    @Override
    public int getSelectedCount(Long userId) {
        return cartItemMapper.countSelectedByUserId(userId);
    }

    /**
     * 获取已选中商品的总价
     *
     * @param userId 用户ID
     * @return 已选中商品的总价
     */
    @Override
    public BigDecimal getSelectedTotalPrice(Long userId) {
        List<CartItem> items = cartItemMapper.selectSelectedByUserId(userId);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            if (item.getPrice() != null && item.getQuantity() != null) {
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        return total;
    }

    /**
     * 获取用户购物车中的商品项
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 购物车项VO
     */
    @Override
    public CartItemVo getCartItem(Long userId, Long skuId) {
        CartItem cartItem = cartItemMapper.selectByUserIdAndSkuId(userId, skuId);
        if (cartItem == null) {
            return null;
        }
        List<CartItemVo> voList = convertToVoList(List.of(cartItem));
        return voList.isEmpty() ? null : voList.get(0);
    }

    /**
     * 将购物车项列表转换为VO列表，并补充实时库存等信息
     *
     * @param items 购物车项列表
     * @return 购物车项VO列表
     */
    private List<CartItemVo> convertToVoList(List<CartItem> items) {
        return items.stream().map(item -> {
            CartItemVo vo = CartItemVo.fromCartItem(item);

            // 补充实时库存信息
            Sku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null) {
                vo.setStock(sku.getStock());
                vo.setSkuStatus(sku.getStatus());
                vo.setSpuId(sku.getSpuId());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}