package com.cyh.mallportal.mq.consumer;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.mq.event.CacheInvalidateEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * 缓存失效消费者
 * <p>
 * 统一入口：业务数据变更后，异步清除 Redis 缓存。
 * 精确 key 直接 DEL，列表/分页类缓存通过 SCAN 命令扫描删除（替代 KEYS）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidateConsumer {

    private final StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMQConfig.CACHE_INVALIDATE_QUEUE)
    public void handleCacheInvalidate(CacheInvalidateEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费缓存失效事件, domain: {}, exactKeys: {}, scanPatterns: {}",
                    event.getDomain(), event.getExactKeys(), event.getScanPatterns());

            // 1. 精确 key 删除
            if (event.getExactKeys() != null && !event.getExactKeys().isEmpty()) {
                stringRedisTemplate.delete(event.getExactKeys());
                log.info("精确删除缓存完成, key 数量: {}", event.getExactKeys().size());
            }

            // 2. SCAN 模式删除（非阻塞，替代 KEYS）
            if (event.getScanPatterns() != null && !event.getScanPatterns().isEmpty()) {
                for (String pattern : event.getScanPatterns()) {
                    scanAndDelete(pattern);
                }
                log.info("SCAN 删除缓存完成, pattern 数量: {}", event.getScanPatterns().size());
            }

            channel.basicAck(deliveryTag, false);
            log.info("缓存失效事件消费完成, domain: {}", event.getDomain());

        } catch (Exception e) {
            log.error("消费缓存失效事件失败, domain: {}, 异常: {}", event.getDomain(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * 使用 SCAN 命令扫描并删除匹配的 key（非阻塞，替代 KEYS）
     */
    private void scanAndDelete(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();
        Set<String> keysToDelete = new HashSet<>();
        try {
            Cursor<String> cursor = stringRedisTemplate.scan(options);
            while (cursor.hasNext()) {
                keysToDelete.add(cursor.next());
            }
            if (!keysToDelete.isEmpty()) {
                stringRedisTemplate.delete(keysToDelete);
                log.debug("SCAN 删除缓存, pattern: {}, 删除数量: {}", pattern, keysToDelete.size());
            }
        } catch (Exception e) {
            log.error("SCAN 删除缓存失败, pattern: {}", pattern, e);
        }
    }
}