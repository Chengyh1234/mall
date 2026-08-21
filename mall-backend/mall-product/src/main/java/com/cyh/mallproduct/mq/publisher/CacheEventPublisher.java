package com.cyh.mallproduct.mq.publisher;

import com.cyh.mallproduct.config.RabbitMQConfig;
import com.cyh.mallproduct.mq.event.CacheInvalidateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 缓存事件消息发送者
 * <p>
 * 业务数据变更后，将缓存失效事件投递到 RabbitMQ 供消费者异步清除 Redis 缓存
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishInvalidate(CacheInvalidateEvent event) {
        log.info("发布缓存失效事件, domain: {}, exactKeys: {}, scanPatterns: {}",
                event.getDomain(), event.getExactKeys(), event.getScanPatterns());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CACHE_EXCHANGE,
                RabbitMQConfig.CACHE_INVALIDATE_ROUTING_KEY,
                event
        );
    }
}