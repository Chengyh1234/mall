package com.cyh.mallportal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * <p>
 * 功能说明：
 * 配置RedisTemplate使用Jackson序列化，解决乱码问题
 *
 * @author cyh
 * @since 2024-01-01
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate
     * <p>
     * 使用Jackson2JsonRedisSerializer进行序列化：
     * - key使用StringRedisSerializer
     * - value使用Jackson2JsonRedisSerializer转化为json字符串存储
     * <p>
     * 注意：购物车缓存(CartCacheService)直接使用StringRedisTemplate手动序列化，
     * 避免Jackson默认类型反序列化问题，同时杜绝类型注入安全风险。
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate Redis模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}