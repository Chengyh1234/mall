package com.cyh.mallorder.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;

/**
 * SPU 销量累加事件消息
 * <p>
 * 当订单支付成功后，发送该消息异步累加对应 SPU 的销量，
 * 降低对订单支付流程的延迟影响。
 */
@Data
@Accessors(chain = true)
public class SalesIncreaseEvent implements Serializable {
    private Long orderId;
}