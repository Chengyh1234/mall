package com.cyh.mallportal.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 订单创建事件消息体
 * <p>
 * 订单创建完成后发送到 MQ，由消费者异步处理
 * （库存同步到 DB、清空购物车等后置操作）
 */
@Data
@Accessors(chain = true)
public class OrderCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 涉及的 SKU ID 列表（用于批量同步库存到 DB） */
    private List<Long> skuIds;

    /**
     * 是否来自购物车结算
     * true  → 需要异步清空购物车已选中商品
     * false → 直接下单，无需清空购物车
     */
    private Boolean fromCart;
}