package com.cyh.mallauth.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置（精简版 — 仅邮件发送相关）
 * <p>
 * 架构：
 * email.direct (Direct Exchange)
 *   routing key: email.send
 *        ↓
 *   email.send.queue
 *   (异步削峰, Consumer 调用 EmailService)
 *        ↓ (重试耗尽)
 *   email.send.dlq (死信队列, 人工排查)
 */
@Slf4j
@Configuration
public class RabbitMQConfig {

    /** 死信交换机（与 mall-all 共用同一个死信交换机） */
    public static final String DEAD_EXCHANGE = "order.dead";

    // ==================== 邮件发送 - 异步削峰 ====================

    /** 邮件交换机 */
    public static final String EMAIL_EXCHANGE = "email.direct";
    /** 邮件发送队列 */
    public static final String EMAIL_SEND_QUEUE = "email.send.queue";
    /** 邮件发送死信队列 */
    public static final String EMAIL_SEND_DLQ = "email.send.dlq";
    /** 邮件发送路由键 */
    public static final String EMAIL_SEND_ROUTING_KEY = "email.send";

    // ==================== 邮件发送 - 异步削峰 ====================

    @Bean
    public DirectExchange emailExchange() {
        return ExchangeBuilder.directExchange(EMAIL_EXCHANGE)
                .durable(true)
                .build();
    }

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

    // ==================== 死信交换机 ====================

    @Bean
    public DirectExchange deadExchange() {
        return ExchangeBuilder.directExchange(DEAD_EXCHANGE)
                .durable(true)
                .build();
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