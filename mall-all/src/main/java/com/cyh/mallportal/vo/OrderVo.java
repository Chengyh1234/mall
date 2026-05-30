package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.entity.OrderDelivery;
import com.cyh.mallportal.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 订单详情VO
 * 包含订单主信息、订单明细列表和发货记录列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {

    /**
     * 订单主信息
     */
    private Order order;

    /**
     * 订单明细列表
     */
    private List<OrderItem> items;

    /**
     * 发货记录列表（支持一个订单多次发货）
     */
    private List<OrderDelivery> deliveries;

    /**
     * 订单状态描述
     */
    private String statusDesc;

    /**
     * 支付状态描述
     */
    private String payStatusDesc;

    /**
     * 支付方式描述
     */
    private String payTypeDesc;
}