package com.cyh.mallproduct.mq.publisher;

import com.cyh.mallproduct.config.RabbitMQConfig;
import com.cyh.mallproduct.mq.event.SpuSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * SPU 同步事件消息发送者
 * <p>
 * SPU 增删改后，将同步事件投递到 RabbitMQ 供消费者异步更新 ES 索引。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpuEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布 SPU 同步事件
     *
     * @param spuId SPU ID
     * @param type  事件类型（CREATE / UPDATE / DELETE）
     */
    public void publishSync(Long spuId, String type) {
        log.info("发布 SPU 同步事件, spuId: {}, type: {}", spuId, type);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SPU_SYNC_EXCHANGE,
                RabbitMQConfig.SPU_SYNC_ROUTING_KEY,
                new SpuSyncEvent().setSpuId(spuId).setType(type)
        );
    }
}