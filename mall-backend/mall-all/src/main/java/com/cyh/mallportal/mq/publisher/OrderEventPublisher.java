package com.cyh.mallportal.mq.publisher;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.mq.event.OrderExpireEvent;
import com.cyh.mallportal.mq.event.SalesIncreaseEvent;
import com.cyh.mallportal.mq.event.StockSyncEvent;
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

    /**
     * 支付超时时间（分钟），从配置文件读取
     */
    @Value("${mall.order.pay-expire-minutes:30}")
    private int payExpireMinutes;

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
                    message.getMessageProperties().setExpiration(
                            String.valueOf(payExpireMinutes * 60 * 1000L)
                    );
                    return message;
                }
        );
    }

    /**
     * 发布库存同步事件
     * <p>
     * Redis 库存变更后，异步同步到 MySQL。统一入口，下单、支付、取消、退款均走此方法。
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
     * <p>
     * 确认收货后，异步累加 SPU 销量。消费者自行查询订单明细，不与订单事务耦合。
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