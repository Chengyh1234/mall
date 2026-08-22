package com.cyh.mallorder.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5);
        return Redisson.create(config);
    }
}