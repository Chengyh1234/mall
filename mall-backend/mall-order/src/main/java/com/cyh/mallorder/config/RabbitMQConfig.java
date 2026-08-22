package com.cyh.mallorder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列、交换机、死信队列配置（订单服务）
 * <p>
 * 包含：订单超时取消（延迟队列）、库存同步、SPU 销量累加
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /** 业务交换机 */
    public static final String ORDER_EXCHANGE = "order.direct";

    /** 死信交换机 */
    public static final String DEAD_EXCHANGE = "order.dead";

    // ==================== 订单超时取消 - 延迟队列 ====================
    /**
     * 生产者 → 主交换机(order.direct) → (路由：order.expire.delay) → 延迟队列(队列：order.expire.delay.queue)
     * （无消费者，等TTL）→ TTL到期 → 死信交换机（order.dead） → (路由:order.expire) → 超时处理队列（队列：order.expire.queue）。
     */
    /** 延迟队列（消息 TTL 到期后转死信） */
    public static final String ORDER_DELAY_QUEUE = "order.expire.delay.queue";
    /** 延迟路由键 */
    public static final String ORDER_DELAY_ROUTING_KEY = "order.expire.delay";

    /** 订单超时处理队列（消费延迟到期的消息） */
    public static final String ORDER_EXPIRE_QUEUE = "order.expire.queue";
    /** 订单超时路由键 */
    public static final String ORDER_EXPIRE_ROUTING_KEY = "order.expire";

    // ==================== 库存同步 ====================

    /** 库存同步队列 */
    public static final String STOCK_SYNC_QUEUE = "stock.sync.queue";
    /** 库存同步死信队列 */
    public static final String STOCK_SYNC_DLQ = "stock.sync.dlq";
    /** 库存同步路由键 */
    public static final String STOCK_SYNC_ROUTING_KEY = "stock.sync";

    // ==================== SPU 销量累加 ====================

    /** SPU 销量累加队列 */
    public static final String SALES_INCREASE_QUEUE = "sales.increase.queue";
    /** SPU 销量累加死信队列 */
    public static final String SALES_INCREASE_DLQ = "sales.increase.dlq";
    /** SPU 销量累加路由键 */
    public static final String SALES_INCREASE_ROUTING_KEY = "sales.increase";

    // ==================== 业务交换机 ====================

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 订单超时取消 ====================

    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(ORDER_EXPIRE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderExchange())
                .with(ORDER_DELAY_ROUTING_KEY);
    }

    @Bean
    public Queue orderExpireQueue() {
        return QueueBuilder.durable(ORDER_EXPIRE_QUEUE).build();
    }

    @Bean
    public Binding orderExpireBinding() {
        return BindingBuilder.bind(orderExpireQueue())
                .to(deadExchange())
                .with(ORDER_EXPIRE_ROUTING_KEY);
    }

    // ==================== 库存同步 ====================

    @Bean
    public Queue stockSyncQueue() {
        return QueueBuilder.durable(STOCK_SYNC_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(STOCK_SYNC_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding stockSyncBinding() {
        return BindingBuilder.bind(stockSyncQueue())
                .to(orderExchange())
                .with(STOCK_SYNC_ROUTING_KEY);
    }

    @Bean
    public Queue stockSyncDlq() {
        return QueueBuilder.durable(STOCK_SYNC_DLQ).build();
    }

    @Bean
    public Binding stockSyncDlqBinding() {
        return BindingBuilder.bind(stockSyncDlq())
                .to(deadExchange())
                .with(STOCK_SYNC_ROUTING_KEY);
    }

    // ==================== SPU 销量累加 ====================

    @Bean
    public Queue salesIncreaseQueue() {
        return QueueBuilder.durable(SALES_INCREASE_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(SALES_INCREASE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding salesIncreaseBinding() {
        return BindingBuilder.bind(salesIncreaseQueue())
                .to(orderExchange())
                .with(SALES_INCREASE_ROUTING_KEY);
    }

    @Bean
    public Queue salesIncreaseDlq() {
        return QueueBuilder.durable(SALES_INCREASE_DLQ).build();
    }

    @Bean
    public Binding salesIncreaseDlqBinding() {
        return BindingBuilder.bind(salesIncreaseDlq())
                .to(deadExchange())
                .with(SALES_INCREASE_ROUTING_KEY);
    }

    // ==================== 消息序列化 ====================

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        template.setMandatory(true);
        template.setReturnsCallback(returned -> log.warn("消息发送失败, exchange: {}, routingKey: {}, replyCode: {}, replyText: {}",
                returned.getExchange(), returned.getRoutingKey(),
                returned.getReplyCode(), returned.getReplyText()));
        return template;
    }
}