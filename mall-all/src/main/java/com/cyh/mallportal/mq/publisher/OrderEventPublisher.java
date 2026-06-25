package com.cyh.mallportal.mq.publisher;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.mq.event.OrderCreatedEvent;
import com.cyh.mallportal.mq.event.OrderExpireEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 订单事件消息发送者
 * <p>
 * 负责在订单创建完成后，将事件投递到 RabbitMQ 供消费者异步处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 支付超时时间（分钟），从配置文件读取
     */
    @Value("${mall.order.pay-expire-minutes:30}")
    private int payExpireMinutes;

    /**
     * 发布订单创建事件
     *
     * @param event 订单创建事件
     */
    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("发布订单创建事件, orderId: {}, orderNo: {}", event.getOrderId(), event.getOrderNo());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                event
        );
    }

    /**
     * 发布订单超时延迟消息
     * <p>
     * 将消息发送到延迟队列，TTL 到期后自动转入超时处理队列。
     * 使用消息级别 TTL，精确控制每笔订单的超时时间。
     *
     * @param event 订单超时事件
     */
    public void publishOrderExpire(OrderExpireEvent event) {
        log.info("发布订单超时延迟消息, orderId: {}, orderNo: {}, 延迟: {}分钟",
                event.getOrderId(), event.getOrderNo(), payExpireMinutes);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_DELAY_ROUTING_KEY,
                event,
                message -> {
                    // 消息级别 TTL（毫秒），精准控制每笔订单的超时时间
                    message.getMessageProperties().setExpiration(
                            String.valueOf(payExpireMinutes * 60 * 1000L)
                    );
                    return message;
                }
        );
    }
}