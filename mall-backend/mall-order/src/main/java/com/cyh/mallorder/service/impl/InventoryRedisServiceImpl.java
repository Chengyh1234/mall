package com.cyh.mallorder.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.dto.SkuStockDto;
import com.cyh.mallcommon.utils.RedisUtils;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallorder.feign.SkuClient;
import com.cyh.mallorder.service.InventoryRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 库存 Redis 缓存服务实现
 * 通过 Feign 调用 mall-product 同步库存到 MySQL
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryRedisServiceImpl implements InventoryRedisService {

    private final RedisUtils redisUtils;
    private final SkuClient skuClient;

    /**
     * 启动时从 MySQL 加载所有 SKU 库存到 Redis
     * 通过 Feign 调用 mall-product 获取全量 SKU 库存数据
     */
    @Override
    public void loadAllStockToRedis() {
        log.info("开始从 MySQL 加载 SKU 库存到 Redis...");
        Result<List<SkuStockDto>> result = skuClient.getAllSkuStock();
        if (result == null || result.getData() == null) {
            log.warn("获取 SKU 库存数据失败，result 为空");
            return;
        }
        List<SkuStockDto> skuList = result.getData();
        int count = 0;
        for (SkuStockDto sku : skuList) {
            redisUtils.set(RedisConstants.STOCK_PREFIX + sku.getId(),
                    String.valueOf(sku.getStock() != null ? sku.getStock() : 0));
            redisUtils.set(RedisConstants.FROZEN_PREFIX + sku.getId(),
                    String.valueOf(sku.getFrozenStock() != null ? sku.getFrozenStock() : 0));
            count++;
        }
        log.info("库存加载完成，共加载 {} 个 SKU", count);
    }

    /**
     * 获取 SKU 可售库存（Redis 中 stock - frozen_stock 的差值）
     * 从 Redis 读取总库存和冻结库存，返回可售数量。
     */
    @Override
    public int getAvailableStock(Long skuId) {
        String stockStr = redisUtils.get(RedisConstants.STOCK_PREFIX + skuId);
        String frozenStr = redisUtils.get(RedisConstants.FROZEN_PREFIX + skuId);
        int stock = StringUtils.hasText(stockStr) ? Integer.parseInt(stockStr) : 0;
        int frozen = StringUtils.hasText(frozenStr) ? Integer.parseInt(frozenStr) : 0;
        return stock - frozen;
    }

    /**
     * 将 Redis 中的库存同步回 MySQL（通过 Feign 调用 mall-product）
     * 读取 Redis 中的总库存和冻结库存，通过 Feign 接口写入 MySQL。
     */
    @Override
    public void syncStockToDb(Long skuId) {
        String stockStr = redisUtils.get(RedisConstants.STOCK_PREFIX + skuId);
        String frozenStr = redisUtils.get(RedisConstants.FROZEN_PREFIX + skuId);
        int stock = StringUtils.hasText(stockStr) ? Integer.parseInt(stockStr) : 0;
        int frozen = StringUtils.hasText(frozenStr) ? Integer.parseInt(frozenStr) : 0;

        // 通过 Feign 调用 mall-product 同步库存
        skuClient.syncStock(skuId, stock, frozen);
        log.debug("同步库存到 MySQL 成功，SKU ID: {}, stock: {}, frozen: {}", skuId, stock, frozen);
    }
}