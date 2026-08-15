package com.cyh.mallportal.service;

/**
 * 库存 Redis 缓存服务接口
 * 管理 SKU 库存缓存的初始化和同步
 */
public interface InventoryRedisService {

    /**
     * 启动时从 MySQL 加载所有 SKU 库存到 Redis
     */
    void loadAllStockToRedis();

    /**
     * 获取 SKU 可售库存（Redis 中 stock - frozen_stock）
     *
     * @param skuId SKU ID
     * @return 可售库存
     */
    int getAvailableStock(Long skuId);

    /**
     * 将 Redis 中的库存同步回 MySQL
     *
     * @param skuId SKU ID
     */
    void syncStockToDb(Long skuId);
}