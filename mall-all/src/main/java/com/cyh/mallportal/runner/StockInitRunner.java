package com.cyh.mallportal.runner;

import com.cyh.mallportal.service.InventoryRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 库存初始化 Runner
 * 应用启动时将 MySQL 中的库存数据加载到 Redis
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockInitRunner implements ApplicationRunner {

    private final InventoryRedisService inventoryRedisService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("应用启动，开始加载库存到 Redis...");
        inventoryRedisService.loadAllStockToRedis();
    }
}