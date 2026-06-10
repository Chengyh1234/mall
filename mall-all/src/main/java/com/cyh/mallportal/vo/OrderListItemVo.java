package com.cyh.mallportal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单列表项VO（普通用户分页订单列表）
 * 包含订单摘要信息 + 商品明细列表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListItemVo {

    /** 订单ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

    /** 订单状态: 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消 6-退款中 7-已退款 8-已拒绝 */
    private Integer status;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 拒绝原因（拒接退款时填写） */
    private String rejectReason;

    /** 拒绝时间（拒接退款时记录） */
    private LocalDateTime rejectedAt;

    /** 订单商品明细列表 */
    private List<Item> items;

    /**
     * 订单商品明细（用户列表展示）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        /** 商品名称 */
        private String productName;

        /** 商品图片 */
        private String productImage;

        /** SKU规格 */
        private String skuSpecs;

        /** 单价 */
        private BigDecimal price;

        /** 数量 */
        private Integer quantity;

        /** 小计 */
        private BigDecimal totalAmount;
    }
}