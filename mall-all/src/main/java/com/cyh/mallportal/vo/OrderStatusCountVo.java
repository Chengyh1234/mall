package com.cyh.mallportal.vo;

import lombok.Data;

/**
 * 用户订单各状态数量 VO
 * 返回给前端显示"我的订单"各状态标签上的角标数字
 */
@Data
public class OrderStatusCountVo {

    /** 待付款数量（status=1） */
    private Integer pendingPayment;

    /** 待发货数量（status=2） */
    private Integer pendingDelivery;

    /** 待收货数量（status=3） */
    private Integer pendingReceipt;

    /** 退款中数量（status=6） */
    private Integer refunding;

    /** 无参构造器（MyBatis 映射需要） */
    public OrderStatusCountVo() {
    }

    /**
     * 全部赋值，方便工厂方法扩展
     */
    public OrderStatusCountVo(Integer pendingPayment, Integer pendingDelivery,
                              Integer pendingReceipt, Integer refunding) {
        this.pendingPayment = pendingPayment != null ? pendingPayment : 0;
        this.pendingDelivery = pendingDelivery != null ? pendingDelivery : 0;
        this.pendingReceipt = pendingReceipt != null ? pendingReceipt : 0;
        this.refunding = refunding != null ? refunding : 0;
    }
}