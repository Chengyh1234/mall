package com.cyh.mallportal.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * SPU 销量累加事件消息体
 * <p>
 * 确认收货后发送到 MQ，消费者自行查询订单明细并异步累加 SPU 销量。
 */
@Data
@Accessors(chain = true)
public class SalesIncreaseEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
}