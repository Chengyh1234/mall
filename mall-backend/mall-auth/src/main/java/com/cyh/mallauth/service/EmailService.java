package com.cyh.mallauth.service;

import com.cyh.mallauth.mq.event.EmailSendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务
 * <p>
 * 负责发送登录验证码、重置密码验证码等邮件。
 * 使用 Spring Mail + QQ邮箱 SMTP 服务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 根据邮件类型发送验证码（供 Consumer/MQ 异步调用）
     */
    public void sendByType(String to, String code, EmailSendEvent.EmailType type) {
        String subject;
        String text;

        switch (type) {
            case LOGIN:
                subject = "【cyh电商平台】登录验证码";
                text = "您的登录验证码为：" + code + "，5分钟内有效，请勿泄露给他人。";
                break;
            case REGISTER:
                subject = "【cyh电商平台】注册验证码";
                text = "欢迎注册电商平台！您的注册验证码为：" + code + "，5分钟内有效，请勿泄露给他人。";
                break;
            case RESET_PASSWORD:
                subject = "【cyh的电商平台】重置密码验证码";
                text = "您正在重置密码，验证码为：" + code + "，5分钟内有效。如非本人操作，请忽略此邮件。";
                break;
            default:
                throw new IllegalArgumentException("不支持的邮件类型: " + type);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        log.info("邮件已发送至: {}, 类型: {}", to, type);
    }
}