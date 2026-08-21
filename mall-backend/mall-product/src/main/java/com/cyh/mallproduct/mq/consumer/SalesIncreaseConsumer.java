package com.cyh.mallproduct.mq.consumer;

import com.cyh.mallproduct.config.RabbitMQConfig;
import com.cyh.mallproduct.entity.OrderItem;
import com.cyh.mallproduct.mapper.OrderItemMapper;
import com.cyh.mallproduct.mapper.SpuMapper;
import com.cyh.mallproduct.mq.event.SalesIncreaseEvent;
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
 * 确认收货后，异步查询订单明细并累加 SPU 销量，不与订单事务耦合。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesIncreaseConsumer {

    private final SpuMapper spuMapper;
    private final OrderItemMapper orderItemMapper;

    @RabbitListener(queues = RabbitMQConfig.SALES_INCREASE_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handle(SalesIncreaseEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费SPU销量累加事件, orderId: {}", event.getOrderId());

            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(event.getOrderId());
            if (orderItems != null) {
                for (OrderItem item : orderItems) {
                    spuMapper.increaseSales(item.getSpuId(), item.getQuantity());
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