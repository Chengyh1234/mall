package com.cyh.mallauth.mq.publisher;

import com.cyh.mallauth.config.RabbitMQConfig;
import com.cyh.mallauth.mq.event.EmailSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 邮件发送事件发布者
 * <p>
 * 将邮件请求入队后立即返回，由 Consumer 异步调用 SMTP 发送。
 * 发送耗时从 1~5s 降为 <10ms，同时 MQ 队列提供削峰缓冲能力。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发布邮件发送事件
     *
     * @param event 邮件发送事件（收件人、验证码、邮件类型）
     */
    public void publish(EmailSendEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_SEND_ROUTING_KEY,
                event
        );
        log.info("邮件发送事件已入队, to: {}, type: {}", event.getTo(), event.getType());
    }
}