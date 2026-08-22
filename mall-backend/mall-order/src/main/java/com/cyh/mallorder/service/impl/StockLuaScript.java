package com.cyh.mallorder.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 库存 Lua 脚本加载器
 * 预加载 Lua 脚本，执行时传入 KEYS 和 ARGV
 */
@Slf4j
@Component
public class StockLuaScript {

    private final RedisUtils redisUtils;
    private final DefaultRedisScript<Long> freezeStockScript;
    private final DefaultRedisScript<Long> confirmStockScript;
    private final DefaultRedisScript<Long> releaseStockScript;
    private final DefaultRedisScript<Long> cancelStockScript;

    public StockLuaScript(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
        this.freezeStockScript = loadScript("lua/freeze_stock.lua");
        this.confirmStockScript = loadScript("lua/confirm_stock.lua");
        this.releaseStockScript = loadScript("lua/release_stock.lua");
        this.cancelStockScript = loadScript("lua/cancel_stock.lua");
        log.info("库存 Lua 脚本加载完成");
    }

    private DefaultRedisScript<Long> loadScript(String path) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource(path));
        script.setResultType(Long.class);
        return script;
    }

    /**
     * 冻结库存
     *
     * @param skuId    SKU ID
     * @param quantity 冻结数量
     * @return true=成功, false=库存不足
     */
    public boolean freezeStock(Long skuId, int quantity) {
        List<String> keys = Arrays.asList(
                RedisConstants.STOCK_PREFIX + skuId,
                RedisConstants.FROZEN_PREFIX + skuId
        );
        Long result = redisUtils.execute(freezeStockScript, keys, String.valueOf(quantity));
        return result != null && result == 1;
    }

    /**
     * 确认扣除库存（支付成功）
     *
     * @param skuId    SKU ID
     * @param quantity 扣除数量
     */
    public void confirmStock(Long skuId, int quantity) {
        List<String> keys = Arrays.asList(
                RedisConstants.STOCK_PREFIX + skuId,
                RedisConstants.FROZEN_PREFIX + skuId
        );
        redisUtils.execute(confirmStockScript, keys, String.valueOf(quantity));
    }

    /**
     * 释放冻结库存（超时/取消）
     *
     * @param skuId    SKU ID
     * @param quantity 释放数量
     */
    public void releaseStock(Long skuId, int quantity) {
        List<String> keys = Arrays.asList(
                RedisConstants.FROZEN_PREFIX + skuId
        );
        redisUtils.execute(releaseStockScript, keys, String.valueOf(quantity));
    }

    /**
     * 已经支付的订单取消订单，库存放回（订单取消）
     *
     * @param skuId    SKU ID
     * @param quantity 取消数量
     */
    public void cancelStock(Long skuId, int quantity) {
        List<String> keys = Arrays.asList(
                RedisConstants.STOCK_PREFIX + skuId
        );
        redisUtils.execute(cancelStockScript, keys, String.valueOf(quantity));
    }
}