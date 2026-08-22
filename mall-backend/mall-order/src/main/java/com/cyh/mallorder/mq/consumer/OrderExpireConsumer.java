package com.cyh.mallorder.mq.consumer;

import com.cyh.mallorder.config.RabbitMQConfig;
import com.cyh.mallorder.entity.Order;
import com.cyh.mallorder.entity.OrderItem;
import com.cyh.mallorder.mapper.OrderItemMapper;
import com.cyh.mallorder.mapper.OrderMapper;
import com.cyh.mallorder.mq.event.OrderExpireEvent;
import com.cyh.mallorder.mq.publisher.OrderEventPublisher;
import com.cyh.mallorder.service.impl.StockLuaScript;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单超时到期消费者
 * <p>
 * 消费延迟队列到期的消息，处理超时未支付订单。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpireConsumer {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final StockLuaScript stockLuaScript;
    private final OrderEventPublisher orderEventPublisher;

    /**
     * 消费订单超时消息，处理超时未支付订单
     * <p>
     * 从延迟队列 {@link RabbitMQConfig#ORDER_EXPIRE_QUEUE} 消费到期消息，执行以下操作：
     * <ol>
     *   <li>检查订单是否存在且未被删除，若不存在则直接确认</li>
     *   <li>检查订单状态是否为待支付（status=1），若非待支付则直接忽略</li>
     *   <li>遍历订单明细，通过 Lua 脚本释放 Redis 中冻结的库存</li>
     *   <li>将订单状态更新为已取消（status=5），记录取消原因"支付超时自动取消"</li>
     *   <li>事务提交后，发布库存同步事件，将 Redis 库存同步回 MySQL</li>
     * </ol>
     * </p>
     *
     * @param event   订单超时事件，包含订单 ID 和订单编号
     * @param message RabbitMQ 消息对象，用于获取 deliveryTag
     * @param channel RabbitMQ 信道，用于手动确认/拒绝消息
     * @throws IOException 当 basicAck/basicNack 操作失败时抛出
     */
    @RabbitListener(queues = RabbitMQConfig.ORDER_EXPIRE_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderExpire(OrderExpireEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费订单超时消息, orderId: {}, orderNo: {}", event.getOrderId(), event.getOrderNo());

            Order order = orderMapper.selectById(event.getOrderId());

            if (order == null || order.getIsDeleted() == 1) {
                log.warn("订单不存在或已删除, 直接忽略, orderId: {}", event.getOrderId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            if (order.getStatus() != 1) {
                log.info("订单无需处理(当前状态: {}), 直接忽略, orderId: {}", order.getStatus(), event.getOrderId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            List<Long> skuIds = new ArrayList<>();
            for (OrderItem item : items) {
                stockLuaScript.releaseStock(item.getSkuId(), item.getQuantity());
                skuIds.add(item.getSkuId());
            }

            order.setStatus(5);
            order.setCancelReason("支付超时自动取消");
            order.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("延迟队列 - 超时订单已自动取消, orderId: {}, orderNo: {}", order.getId(), order.getOrderNo());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    orderEventPublisher.publishStockSync(skuIds);
                }
            });

            channel.basicAck(deliveryTag, false);
            log.info("订单超时消息消费完成, orderId: {}", event.getOrderId());

        } catch (Exception e) {
            log.error("消费订单超时消息失败, orderId: {}, 异常: {}", event.getOrderId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}