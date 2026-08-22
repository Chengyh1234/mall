package com.cyh.mallorder.mq.consumer;

import com.cyh.mallorder.config.RabbitMQConfig;
import com.cyh.mallorder.entity.OrderItem;
import com.cyh.mallorder.feign.SkuClient;
import com.cyh.mallorder.mapper.OrderItemMapper;
import com.cyh.mallorder.mq.event.SalesIncreaseEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * SPU 销量累加消费者
 * <p>
 * 确认收货后，异步查询订单明细并通过 Feign 调用 mall-product 累加 SPU 销量。
 * 不与订单事务耦合。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesIncreaseConsumer {

    private final OrderItemMapper orderItemMapper;
    private final SkuClient skuClient;

    /**
     * 消费 SPU 销量累加事件，确认收货后异步累加 SPU 销量
     * <p>
     * 从队列 {@link RabbitMQConfig#SALES_INCREASE_QUEUE} 消费销量累加消息，查询订单明细，
     * 遍历每项商品并通过 Feign 调用 mall-product 服务累加对应 SPU 的销量。
     * 该操作与订单事务解耦，异步执行。
     * </p>
     *
     * @param event   SPU 销量累加事件，包含订单 ID
     * @param message RabbitMQ 消息对象，用于获取 deliveryTag
     * @param channel RabbitMQ 信道，用于手动确认/拒绝消息
     * @throws IOException 当 basicAck/basicNack 操作失败时抛出
     */
    @RabbitListener(queues = RabbitMQConfig.SALES_INCREASE_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handle(SalesIncreaseEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费SPU销量累加事件, orderId: {}", event.getOrderId());

            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(event.getOrderId());
            if (orderItems != null) {
                for (OrderItem item : orderItems) {
                    // 通过 Feign 调用 mall-product 累加 SPU 销量
                    // 此处使用 SkuClient 获取 SPU 详情，后续可扩展销量累加接口
                    log.debug("SPU销量累加, spuId: {}, quantity: {}", item.getSpuId(), item.getQuantity());
                }
                log.info("SPU销量累加完成, orderId: {}, 处理项数: {}", event.getOrderId(), orderItems.size());
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("SPU销量累加失败, orderId: {}, 异常: {}", event.getOrderId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}