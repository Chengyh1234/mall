package com.cyh.mallorder.service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务接口
 * <p>
 * 基于 Redisson 实现分布式锁，提供通用加锁模板，
 * 适用于订单创建、支付、退款等需要防重复操作的场景。
 */
public interface DistributedLockService {

    /**
     * 带回调的锁执行（推荐使用）
     * <p>
     * 自动获取锁 → 执行业务 → 释放锁
     * 获取锁失败时抛出 BusinessException
     *
     * @param key      锁 Key
     * @param waitTime 等待锁的超时时间
     * @param leaseTime 锁持有时间，-1 表示启用 WatchDog 自动续期
     * @param unit     时间单位
     * @param supplier 业务回调
     * @param <T>      返回值类型
     * @return 业务执行结果
     */
    <T> T executeWithLock(String key, long waitTime, long leaseTime,
                          TimeUnit unit, Supplier<T> supplier);
}