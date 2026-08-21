package com.cyh.mallproduct.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列、交换机、死信队列配置（商品服务）
 * <p>
 * 包含：SPU 同步（ES 索引）、SPU 销量累加、缓存失效
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /** 订单业务交换机（由 mall-order 声明，此处声明是幂等的） */
    public static final String ORDER_EXCHANGE = "order.direct";

    /** 死信交换机 */
    public static final String DEAD_EXCHANGE = "order.dead";

    // ==================== SPU 销量累加 ====================

    /** SPU 销量累加队列 */
    public static final String SALES_INCREASE_QUEUE = "sales.increase.queue";
    /** SPU 销量累加死信队列 */
    public static final String SALES_INCREASE_DLQ = "sales.increase.dlq";
    /** SPU 销量累加路由键 */
    public static final String SALES_INCREASE_ROUTING_KEY = "sales.increase";

    // ==================== SPU 同步 - 异步更新 ES 索引 ====================

    /** SPU 同步交换机 */
    public static final String SPU_SYNC_EXCHANGE = "spu.direct";
    /** SPU 同步队列 */
    public static final String SPU_SYNC_QUEUE = "spu.sync.queue";
    /** SPU 同步死信队列 */
    public static final String SPU_SYNC_DLQ = "spu.sync.dlq";
    /** SPU 同步路由键 */
    public static final String SPU_SYNC_ROUTING_KEY = "spu.sync";

    // ==================== 缓存失效 - 异步清除 ====================

    /** 缓存交换机 */
    public static final String CACHE_EXCHANGE = "cache.direct";
    /** 缓存失效队列 */
    public static final String CACHE_INVALIDATE_QUEUE = "cache.invalidate.queue";
    /** 缓存失效死信队列 */
    public static final String CACHE_INVALIDATE_DLQ = "cache.invalidate.dlq";
    /** 缓存失效路由键 */
    public static final String CACHE_INVALIDATE_ROUTING_KEY = "cache.invalidate";

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

    // ==================== SPU 同步 - 异步更新 ES 索引 ====================

    @Bean
    public DirectExchange spuSyncExchange() {
        return ExchangeBuilder.directExchange(SPU_SYNC_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue spuSyncQueue() {
        return QueueBuilder.durable(SPU_SYNC_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(SPU_SYNC_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding spuSyncBinding() {
        return BindingBuilder.bind(spuSyncQueue())
                .to(spuSyncExchange())
                .with(SPU_SYNC_ROUTING_KEY);
    }

    @Bean
    public Queue spuSyncDlq() {
        return QueueBuilder.durable(SPU_SYNC_DLQ).build();
    }

    @Bean
    public Binding spuSyncDlqBinding() {
        return BindingBuilder.bind(spuSyncDlq())
                .to(deadExchange())
                .with(SPU_SYNC_ROUTING_KEY);
    }

    // ==================== 缓存失效 - 异步清除 ====================

    @Bean
    public DirectExchange cacheExchange() {
        return ExchangeBuilder.directExchange(CACHE_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue cacheInvalidateQueue() {
        return QueueBuilder.durable(CACHE_INVALIDATE_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(CACHE_INVALIDATE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding cacheInvalidateBinding() {
        return BindingBuilder.bind(cacheInvalidateQueue())
                .to(cacheExchange())
                .with(CACHE_INVALIDATE_ROUTING_KEY);
    }

    @Bean
    public Queue cacheInvalidateDlq() {
        return QueueBuilder.durable(CACHE_INVALIDATE_DLQ).build();
    }

    @Bean
    public Binding cacheInvalidateDlqBinding() {
        return BindingBuilder.bind(cacheInvalidateDlq())
                .to(deadExchange())
                .with(CACHE_INVALIDATE_ROUTING_KEY);
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