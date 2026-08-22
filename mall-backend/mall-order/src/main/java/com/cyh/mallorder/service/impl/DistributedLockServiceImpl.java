package com.cyh.mallorder.service.impl;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallorder.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁服务实现类
 * <p>
 * 基于 Redisson 实现，支持：
 * <ul>
 *   <li>WatchDog 自动续期：leaseTime=-1 时，持有锁的线程还在运行，Redisson 自动每10秒续期一次</li>
 *   <li>可重入：同一线程可重复获取同一把锁</li>
 *   <li>服务宕机自动释放：不会死锁</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockServiceImpl implements DistributedLockService {

    private final RedissonClient redissonClient;

    /**
     * 执行带分布式锁的任务
     * <p>
     * 基于 Redisson 实现，支持 WatchDog 自动续期（leaseTime=-1 时生效）、可重入、服务宕机自动释放。
     * 获取锁失败时抛出 BusinessException，避免线程阻塞等待。
     * 锁释放时校验当前线程是否仍持有锁，防止误释放。
     *
     * @param key       锁的 Redis Key
     * @param waitTime  获取锁的最大等待时间
     * @param leaseTime 锁持有时间（-1 表示启用 WatchDog 自动续期）
     * @param unit      时间单位
     * @param supplier  加锁后执行的业务逻辑
     * @param <T>       返回值类型
     * @return 业务逻辑执行结果
     */
    @Override
    public <T> T executeWithLock(String key, long waitTime, long leaseTime,
                                 TimeUnit unit, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        try {
            // 尝试获取锁
            acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("获取分布式锁失败, key: {}, waitTime: {} {}", key, waitTime, unit);
                throw new BusinessException("操作太频繁，请稍后再试");
            }

            log.debug("获取分布式锁成功, key: {}", key);
            // 执行业务逻辑
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("分布式锁线程中断, key: {}", key, e);
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁, key: {}", key);
            }
        }
    }
}