package com.cyh.mallorder.mq.consumer;

import com.cyh.mallorder.config.RabbitMQConfig;
import com.cyh.mallorder.mq.event.StockSyncEvent;
import com.cyh.mallorder.service.InventoryRedisService;
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

    /**
     * 消费库存同步事件，将 Redis 中的库存变更同步回 MySQL
     * <p>
     * 从队列 {@link RabbitMQConfig#STOCK_SYNC_QUEUE} 消费库存同步消息，遍历事件中的 SKU ID 列表，
     * 调用 {@link com.cyh.mallorder.service.InventoryRedisService#syncStockToDb} 将每个 SKU 的 Redis 库存同步到数据库。
     * 覆盖下单、支付、取消、退款等所有需要同步库存的场景。
     * </p>
     *
     * @param event   库存同步事件，包含需要同步的 SKU ID 列表
     * @param message RabbitMQ 消息对象，用于获取 deliveryTag
     * @param channel RabbitMQ 信道，用于手动确认/拒绝消息
     * @throws IOException 当 basicAck/basicNack 操作失败时抛出
     */
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