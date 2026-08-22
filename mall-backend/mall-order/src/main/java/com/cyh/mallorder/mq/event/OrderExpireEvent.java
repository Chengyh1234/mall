package com.cyh.mallorder.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * 订单超时取消事件消息
 * <p>
 * 当订单超过支付期限未支付时，发送该消息到延迟队列，
 * 消费者收到后执行订单取消及库存回滚操作。
 */
@Data
@Accessors(chain = true)
public class OrderExpireEvent implements Serializable {
    private Long orderId;
    private String orderNo;
}