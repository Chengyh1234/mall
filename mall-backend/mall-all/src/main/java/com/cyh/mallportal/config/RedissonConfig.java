package com.cyh.mallportal.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置类
 * <p>
 * 基于 Redis 配置自动创建 RedissonClient，
 * 用于实现分布式锁，防止订单重复提交、重复支付等并发问题。
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    /**
     * 创建 Redisson 客户端
     * <p>
     * 使用单节点模式连接 Redis，
     * 配置连接池参数确保高并发下的性能。
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5)
                .setConnectTimeout(5000)
                .setTimeout(3000);

        log.info("Redisson 客户端初始化完成, Redis 地址: redis://{}:{}", host, port);
        return Redisson.create(config);
    }
}