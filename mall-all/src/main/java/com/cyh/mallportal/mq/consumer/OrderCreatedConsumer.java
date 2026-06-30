package com.cyh.mallportal.mq.consumer;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.mq.event.OrderCreatedEvent;
import com.cyh.mallportal.service.CartItemService;
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
 * 异步处理订单创建后的后置操作（清空购物车）。
 * 库存同步已统一迁移至 {@link com.cyh.mallportal.mq.consumer.StockSyncConsumer}。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final CartItemService cartItemService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费订单创建事件, orderId: {}, orderNo: {}", event.getOrderId(), event.getOrderNo());

            if (Boolean.TRUE.equals(event.getFromCart()) && event.getUserId() != null) {
                cartItemService.clearSelected(event.getUserId());
                log.info("购物车已清空, userId: {}", event.getUserId());
            }

            channel.basicAck(deliveryTag, false);
            log.info("订单创建事件消费完成, orderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("消费订单创建事件失败, orderId: {}, 异常: {}", event.getOrderId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}