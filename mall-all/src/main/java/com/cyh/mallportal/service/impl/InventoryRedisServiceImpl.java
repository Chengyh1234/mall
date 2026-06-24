package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.Sku;
import com.cyh.mallportal.mapper.SkuMapper;
import com.cyh.mallportal.service.InventoryRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 库存 Redis 缓存服务实现
 * 启动时加载库存到 Redis，提供库存查询和同步功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryRedisServiceImpl implements InventoryRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SkuMapper skuMapper;

    /**
     * 启动时从 MySQL 加载所有 SKU 库存到 Redis
     */
    @Override
    public void loadAllStockToRedis() {
        log.info("开始从 MySQL 加载 SKU 库存到 Redis...");
        List<Sku> skuList = skuMapper.selectList(new LambdaQueryWrapper<>());
        int count = 0;
        for (Sku sku : skuList) {
            redisTemplate.opsForValue().set(RedisConstants.STOCK_PREFIX + sku.getId(), String.valueOf(sku.getStock()));
            redisTemplate.opsForValue().set(RedisConstants.FROZEN_PREFIX + sku.getId(), String.valueOf(sku.getFrozenStock() != null ? sku.getFrozenStock() : 0));
            count++;
        }
        log.info("库存加载完成，共加载 {} 个 SKU", count);
    }

    /**
     * 获取 SKU 可售库存（Redis 中 stock - frozen_stock）
     *
     * @param skuId SKU ID
     * @return 可售库存
     */
    @Override
    public int getAvailableStock(Long skuId) {
        String stockStr = redisTemplate.opsForValue().get(RedisConstants.STOCK_PREFIX + skuId);
        String frozenStr = redisTemplate.opsForValue().get(RedisConstants.FROZEN_PREFIX + skuId);
        int stock = StringUtils.hasText(stockStr) ? Integer.parseInt(stockStr) : 0;
        int frozen = StringUtils.hasText(frozenStr) ? Integer.parseInt(frozenStr) : 0;
        return stock - frozen;
    }

    /**
     * 将 Redis 中的库存同步回 MySQL
     *
     * @param skuId SKU ID
     */
    @Override
    public void syncStockToDb(Long skuId) {
        try {
            String stockStr = redisTemplate.opsForValue().get(RedisConstants.STOCK_PREFIX + skuId);
            String frozenStr = redisTemplate.opsForValue().get(RedisConstants.FROZEN_PREFIX + skuId);
            int stock = StringUtils.hasText(stockStr) ? Integer.parseInt(stockStr) : 0;
            int frozen = StringUtils.hasText(frozenStr) ? Integer.parseInt(frozenStr) : 0;

            Sku sku = new Sku();
            sku.setId(skuId);
            sku.setStock(stock);
            sku.setFrozenStock(frozen);
            skuMapper.updateById(sku);
            log.debug("同步库存到 MySQL 成功，SKU ID: {}, stock: {}, frozen: {}", skuId, stock, frozen);
        } catch (Exception e) {
            log.error("同步库存到 MySQL 失败，SKU ID: {}", skuId, e);
        }
    }
}