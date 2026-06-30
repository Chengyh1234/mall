package com.cyh.mallportal.mq.consumer;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.mq.event.StockSyncEvent;
import com.cyh.mallportal.service.InventoryRedisService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * 库存同步消费者
 * <p>
 * 统一入口：Redis 库存变更后，异步同步到 MySQL。
 * 下单、支付、取消、退款等所有场景的库存同步均通过此消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockSyncConsumer {

    private final InventoryRedisService inventoryRedisService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_SYNC_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleStockSync(StockSyncEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费库存同步事件, skuIds: {}", event.getSkuIds());

            if (event.getSkuIds() != null) {
                for (Long skuId : event.getSkuIds()) {
                    inventoryRedisService.syncStockToDb(skuId);
                }
                log.info("库存同步完成, sku 数量: {}", event.getSkuIds().size());
            }

            channel.basicAck(deliveryTag, false);
            log.info("库存同步事件消费完成");

        } catch (Exception e) {
            log.error("消费库存同步事件失败, skuIds: {}, 异常: {}", event.getSkuIds(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}