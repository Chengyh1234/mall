package com.cyh.mallportal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列、交换机、死信队列配置
 * <p>
 * 架构：
 * ┌────────────────────────────────────────────────────┐
 * │   order.direct (Direct Exchange)                  │
 * │   routing keys: order.created, order.expire.delay │
 * │        ↓                    ↓                     │
 * │   order.created.queue   order.expire.delay.queue  │
 * │   (业务消费)             (TTL 队列,无消费者)        │
 * │        ↓                    ↓ (TTL 到期)           │
 * │        └──→ order.dead ←───┘                     │
 * │            (Dead Letter Exchange)                 │
 * │        ↓                    ↓                     │
 * │   order.created.dead.queue  order.expire.queue    │
 * │   (死信队列)               (超时取消消费)           │
 * └────────────────────────────────────────────────────┘
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /** 业务交换机 */
    public static final String ORDER_EXCHANGE = "order.direct";
    /** 订单创建队列 */
    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    /** 订单创建路由键 */
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";

    /** 死信交换机 */
    public static final String DEAD_EXCHANGE = "order.dead";
    /** 死信队列（订单创建消费失败） */
    public static final String DEAD_CREATED_QUEUE = "order.created.dead.queue";

    // ==================== 订单超时取消 - 延迟队列 ====================

    /** 延迟队列（消息 TTL 到期后转死信） 无消费者监听 */
    public static final String ORDER_DELAY_QUEUE = "order.expire.delay.queue";
    /** 延迟路由键 */
    public static final String ORDER_DELAY_ROUTING_KEY = "order.expire.delay";

    /** 死信队列 订单超时处理队列（消费延迟到期的消息） */
    public static final String ORDER_EXPIRE_QUEUE = "order.expire.queue";
    /** 死信交换机和死信队列绑定 订单超时路由键 */
    public static final String ORDER_EXPIRE_ROUTING_KEY = "order.expire";

    // ==================== 业务交换机 & 队列 ====================

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE)
                // 绑定死信交换机
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(ORDER_CREATED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    // ==================== 订单超时取消 - 延迟队列 & 超时队列 ====================

    /**
     * 延迟队列：消息在此等待 TTL 到期后转入死信交换机
     * 无消费者监听，仅作为 TTL 缓冲
     */
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                // 绑定死信交换机
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

    /**
     * 订单超时处理队列：监听延迟到期的消息
     * 消费者检查订单是否仍为"待付款"，是则自动取消并释放库存
     */
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

    // ==================== 死信交换机 & 队列 ====================

    @Bean
    public DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue deadCreatedQueue() {
        return QueueBuilder.durable(DEAD_CREATED_QUEUE).build();
    }

    @Bean
    public Binding deadCreatedBinding() {
        return BindingBuilder.bind(deadCreatedQueue())
                .to(deadExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    // ==================== 消息序列化 ====================

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 自定义 RabbitTemplate，设置 JSON 序列化
     */
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