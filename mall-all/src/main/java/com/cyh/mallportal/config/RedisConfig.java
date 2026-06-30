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
    /*
     * 我的看法：
     * 这个 RedisTemplate<String, Object> 可以不用。
     *
     * 序列化时：Jackson 将 value 按 JSON 格式写入 Redis。
     *   - String → JSON string     "hello"
     *   - Integer → JSON number     100
     *   - Map     → JSON object     {"a":1}
     *   - 自定义VO → JSON object    {"id":1,"name":"xxx"}
     *
     * 反序列化时：targetClass = Object.class，Jackson 按 JSON 原生类型映射：
     *   JSON string → String        ✅
     *   JSON number → Integer/Long  ✅（但可能跟预期类型不一致）
     *   JSON object → LinkedHashMap ⚠️（自定义对象和 Map 无法区分）
     *   JSON array  → ArrayList     ✅
     *
     * 结论：
     *   - 只适合存 String / Map / 简单 List 等 JSON 原生类型
     *   - 不适合存自定义业务对象（会丢成 LinkedHashMap）
     *   - 取 String 必须强转 (String)，多此一举
     * 解决方案：
     *   - 不换 GenericJackson2JsonRedisSerializer（@class 注入风险）
     *   - 不建多套专用 Bean（工程臃肿）
     *   - 自定义业务对象一律走 StringRedisTemplate + 手动序列化
     *
     *  存储数据（写入）的全过程
     * 当你执行 stringRedisTemplate.opsForValue().set("name", "张三") 时：
     * 1.Key 序列化：StringRedisSerializer 把字符串 "name" 按 UTF-8 编码转换成字节数组 [110, 97, 109, 101]。
     * 2.Value 序列化：StringRedisSerializer 把字符串 "张三" 按 UTF-8 编码转换成字节数组 。
     *
     *  读取数据（取出）的过程：将字节数组转换为字符串。
     *
     * 注意：Redis 服务器底层存的确实是字节，但你在 redis-cli 里看到的是 redis-cli 客户端对字节进行解码后的“格式化展示”，而不是存储本身
     *
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