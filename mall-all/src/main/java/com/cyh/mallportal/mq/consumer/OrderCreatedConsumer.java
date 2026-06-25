package com.cyh.mallportal.mq.consumer;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.mq.event.OrderCreatedEvent;
import com.cyh.mallportal.service.CartItemService;
import com.cyh.mallportal.service.InventoryRedisService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 订单创建事件消费者
 * <p>
 * 异步处理订单创建后的后置操作：
 * 1. 将 Redis 库存同步回 MySQL
 * 2. 清空购物车已选中商品（仅购物车结算场景）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final InventoryRedisService inventoryRedisService;
    private final CartItemService cartItemService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //消息的唯一编号。每个通道（Channel）内的每条消息都有一个单调递增的 deliveryTag，用来告诉 Broker 你确认的是哪一条具体消息。
        try {
            log.info("消费订单创建事件, orderId: {}, orderNo: {}, skuIds: {}",
                    event.getOrderId(), event.getOrderNo(), event.getSkuIds());

            // 1. 批量同步 Redis 库存到 MySQL
            if (event.getSkuIds() != null) {
                for (Long skuId : event.getSkuIds()) {
                    inventoryRedisService.syncStockToDb(skuId);
                }
                log.info("库存同步完成, orderId: {}, sku 数量: {}", event.getOrderId(), event.getSkuIds().size());
            }

            // 2. 清空购物车已选中商品（仅购物车结算场景）
            if (Boolean.TRUE.equals(event.getFromCart()) && event.getUserId() != null) {
                cartItemService.clearSelected(event.getUserId());
                log.info("购物车已清空, userId: {}", event.getUserId());
            }

            // 手动 ACK basicAck：代表“我成功了，你删了吧”。
            //false：仅确认当前这条 deliveryTag 的消息。
            //true：一次性确认当前通道中所有未确认的、且 deliveryTag 小于等于当前值的消息。
            // 你这里传 false 是最稳妥、最精确的做法，表示“我只认这一条”。
            channel.basicAck(deliveryTag, false);
            log.info("订单创建事件消费完成, orderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("消费订单创建事件失败, orderId: {}, 异常: {}", event.getOrderId(), e.getMessage(), e);
            // 重试耗尽后自动进入死信队列（配置了 default-requeue-rejected: false）
            //basicNack（否定确认）：代表“我失败了”。
            //参数2：false：不重新入队，直接拒绝。
            //参数3：false:不重新放回原队列。
            channel.basicNack(deliveryTag, false, false);
        }
    }
}