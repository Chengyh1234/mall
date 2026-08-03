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
 * │   routing keys: order.expire.delay                │
 * │                    ↓                              │
 * │   order.expire.delay.queue                        │
 * │   (TTL 队列,无消费者)                              │
 * │                    ↓ (TTL 到期)                    │
 * │              order.dead                            │
 * │           (Dead Letter Exchange)                  │
 * │                    ↓                              │
 * │   order.expire.queue                              │
 * │   (超时取消消费)                                    │
 * └────────────────────────────────────────────────────┘
 * <p>
 * ┌────────────────────────────────────────────────────┐
 * │   email.direct (Direct Exchange)                  │
 * │   routing key: email.send                         │
 * │        ↓                                          │
 * │   email.send.queue                                │
 * │   (异步削峰, Consumer 调用 EmailService)           │
 * │        ↓ (重试耗尽)                                │
 * │   email.send.dlq (死信队列, 人工排查)               │
 * └────────────────────────────────────────────────────┘
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /** 业务交换机 */
    public static final String ORDER_EXCHANGE = "order.direct";

    /** 死信交换机 */
    public static final String DEAD_EXCHANGE = "order.dead";

    // ==================== 库存同步 - 异步削峰 ====================

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

    // ==================== 业务交换机 & 队列 ====================

    /** 延迟队列（消息 TTL 到期后转死信） 无消费者监听 */
    public static final String ORDER_DELAY_QUEUE = "order.expire.delay.queue";
    /** 延迟路由键 */
    public static final String ORDER_DELAY_ROUTING_KEY = "order.expire.delay";

    /** 死信队列 订单超时处理队列（消费延迟到期的消息） */
    public static final String ORDER_EXPIRE_QUEUE = "order.expire.queue";
    /** 死信交换机和死信队列绑定 订单超时路由键 */
    public static final String ORDER_EXPIRE_ROUTING_KEY = "order.expire";

    // ==================== 邮件发送 - 异步削峰 ====================

    /** 邮件交换机 */
    public static final String EMAIL_EXCHANGE = "email.direct";
    /** 邮件发送队列 */
    public static final String EMAIL_SEND_QUEUE = "email.send.queue";
    /** 邮件发送死信队列 */
    public static final String EMAIL_SEND_DLQ = "email.send.dlq";
    /** 邮件发送路由键 */
    public static final String EMAIL_SEND_ROUTING_KEY = "email.send";


    // ==================== 业务交换机 & 队列 ====================

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 订单超时取消 - 延迟队列 & 超时队列 ====================

    /**
     * 延迟队列：消息在此等待 TTL 到期后转入死信交换机
     * 无消费者监听，仅作为 TTL 缓冲
     * 重点：订单超时取消队列，消息 TTL 到期后转入死信交换机，由死信队列监听 ttl为超时时间，这里为30分钟
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

    // ==================== 邮件发送 - 异步削峰 ====================

    /**
     * 邮件业务交换机
     */
    @Bean
    public DirectExchange emailExchange() {
        return ExchangeBuilder.directExchange(EMAIL_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 邮件发送队列：消息入队后由 Consumer 异步发送 SMTP
     * 消费失败转入 order.dead 死信交换机，路由键 email.send
     */
    @Bean
    public Queue emailSendQueue() {
        return QueueBuilder.durable(EMAIL_SEND_QUEUE)
                .deadLetterExchange(DEAD_EXCHANGE)
                .deadLetterRoutingKey(EMAIL_SEND_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding emailSendBinding() {
        return BindingBuilder.bind(emailSendQueue())
                .to(emailExchange())
                .with(EMAIL_SEND_ROUTING_KEY);
    }

    /**
     * 邮件发送死信队列：重试耗尽后落入此队列，人工排查
     */
    @Bean
    public Queue emailSendDlq() {
        return QueueBuilder.durable(EMAIL_SEND_DLQ).build();
    }

    @Bean
    public Binding emailSendDlqBinding() {
        return BindingBuilder.bind(emailSendDlq())
                .to(deadExchange())
                .with(EMAIL_SEND_ROUTING_KEY);
    }

    // ==================== 库存同步 - 异步削峰 ====================

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