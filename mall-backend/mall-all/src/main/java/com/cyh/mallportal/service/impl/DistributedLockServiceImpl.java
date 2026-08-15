package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.service.DistributedLockService;
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
        } catch (BusinessException e) {
            // 业务异常直接抛出，不包装
            throw e;
        } catch (Exception e) {
            log.error("分布式锁执行异常, key: {}", key, e);
            throw new BusinessException("系统繁忙，请稍后重试");
        } finally {
            // 仅当当前线程仍持有锁时才释放
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁, key: {}", key);
            }
        }
    }
}