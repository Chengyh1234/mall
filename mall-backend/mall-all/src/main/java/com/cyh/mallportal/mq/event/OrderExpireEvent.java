package com.cyh.mallportal.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 订单超时到期事件消息体
 * <p>
 * 订单创建后发送到延迟队列，TTL 到期后由消费者处理超时取消逻辑。
 * 仅携带必要字段，不做全量订单查询后发消息。
 */
@Data
@Accessors(chain = true)
public class OrderExpireEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;
}