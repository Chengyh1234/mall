package com.cyh.mallportal.mq.consumer;

import com.cyh.mallportal.config.RabbitMQConfig;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.entity.OrderItem;
import com.cyh.mallportal.mapper.OrderItemMapper;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.mq.event.OrderExpireEvent;
import com.cyh.mallportal.service.InventoryRedisService;
import com.cyh.mallportal.service.StockLuaScript;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时到期消费者
 * <p>
 * 消费延迟队列到期的消息，处理超时未支付订单：
 * 1. 检查订单是否仍为"待付款"(status=1)
 * 2. 是 → 取消订单 + 释放 Redis 冻结库存 + 同步库存到 DB
 * 3. 否（已支付/已取消）→ 直接忽略，ACK 确认
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireConsumer {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockLuaScript stockLuaScript;
    private final InventoryRedisService inventoryRedisService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_EXPIRE_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderExpire(OrderExpireEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费订单超时消息, orderId: {}, orderNo: {}", event.getOrderId(), event.getOrderNo());

            // 1. 查询订单当前状态
            Order order = orderMapper.selectById(event.getOrderId());

            if (order == null || order.getIsDeleted() == 1) {
                log.warn("订单不存在或已删除, 直接忽略, orderId: {}", event.getOrderId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 2. 仅处理仍为"待付款"的订单
            if (order.getStatus() != 1) {
                log.info("订单无需处理(当前状态: {}), 直接忽略, orderId: {}", order.getStatus(), event.getOrderId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 3. 释放 Redis 冻结库存
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            for (OrderItem item : items) {
                stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
                inventoryRedisService.syncStockToDb(item.getSkuId());
            }

            // 4. 更新订单状态为"已取消"
            order.setStatus(5);
            order.setCancelReason("支付超时自动取消");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("延迟队列 - 超时订单已自动取消, orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());

            channel.basicAck(deliveryTag, false);
            log.info("订单超时消息消费完成, orderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("消费订单超时消息失败, orderId: {}, 异常: {}", event.getOrderId(), e.getMessage(), e);
            // 重试耗尽后自动进入死信队列（default-requeue-rejected: false）
            channel.basicNack(deliveryTag, false, false);
        }
    }
}