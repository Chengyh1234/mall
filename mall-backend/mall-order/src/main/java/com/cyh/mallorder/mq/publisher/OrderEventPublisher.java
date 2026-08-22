package com.cyh.mallorder.mq.publisher;

import com.cyh.mallorder.config.RabbitMQConfig;
import com.cyh.mallorder.mq.event.OrderExpireEvent;
import com.cyh.mallorder.mq.event.SalesIncreaseEvent;
import com.cyh.mallorder.mq.event.StockSyncEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单事件消息发送者
 * <p>
 * 负责将订单域事件投递到 RabbitMQ 供消费者异步处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${mall.order.pay-expire-minutes:30}")
    private int payExpireMinutes;

    /**
     * 发布订单超时延迟消息
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
                    message.getMessageProperties().setExpiration(
                            String.valueOf(payExpireMinutes * 60 * 1000L)
                    );
                    return message;
                }
        );
    }

    /**
     * 发布库存同步事件
     *
     * @param skuIds 需要同步的 SKU ID 列表
     */
    public void publishStockSync(List<Long> skuIds) {
        log.info("发布库存同步事件, skuIds: {}", skuIds);
        StockSyncEvent event = new StockSyncEvent().setSkuIds(skuIds);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.STOCK_SYNC_ROUTING_KEY,
                event
        );
    }

    /**
     * 发布 SPU 销量累加事件
     *
     * @param orderId 订单ID
     */
    public void publishSalesIncrease(Long orderId) {
        log.info("发布SPU销量累加事件, orderId: {}", orderId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.SALES_INCREASE_ROUTING_KEY,
                new SalesIncreaseEvent().setOrderId(orderId)
        );
    }
}