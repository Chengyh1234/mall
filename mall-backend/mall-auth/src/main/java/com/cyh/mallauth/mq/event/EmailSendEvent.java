package com.cyh.mallauth.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 邮件发送事件消息体
 * <p>
 * Controller 将邮件请求入队即返回，Consumer 异步调用 SMTP 发送，
 * 实现请求毫秒级响应 + MQ 削峰缓冲。
 */
@Data
@Accessors(chain = true)
public class EmailSendEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 收件人邮箱 */
    private String to;

    /** 验证码 */
    private String code;

    /** 邮件类型 */
    private EmailType type;

    public enum EmailType {
        LOGIN,//登录邮件
        REGISTER,//注册邮件
        RESET_PASSWORD//重置密码邮件
    }
}