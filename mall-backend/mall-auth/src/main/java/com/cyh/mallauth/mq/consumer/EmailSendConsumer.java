package com.cyh.mallauth.mq.consumer;

import com.cyh.mallauth.config.RabbitMQConfig;
import com.cyh.mallauth.mq.event.EmailSendEvent;
import com.cyh.mallauth.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 邮件发送消费者
 * <p>
 * 异步消费邮件队列，调用 EmailService 实际发送 SMTP 邮件。
 * 发送成功后手动 ACK，失败后 NACK 触发 Spring 重试（最多 3 次），
 * 重试耗尽后自动进入 email.send.dlq 死信队列。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_SEND_QUEUE)
    public void handle(EmailSendEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("消费邮件发送事件, to: {}, type: {}", event.getTo(), event.getType());

            emailService.sendByType(event.getTo(), event.getCode(), event.getType());

            channel.basicAck(deliveryTag, false);
            log.info("邮件发送成功, to: {}, type: {}", event.getTo(), event.getType());

        } catch (Exception e) {
            log.error("邮件发送失败, to: {}, type: {}, 将重试或转入死信", event.getTo(), event.getType(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}