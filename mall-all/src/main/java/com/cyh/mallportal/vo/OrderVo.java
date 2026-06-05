package com.cyh.mallportal.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 * 包含订单主信息、订单明细列表和发货记录列表
 * 仅包含前端展示需要的字段，不暴露内部实体字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {

    /** 订单ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 订单状态: 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消 6-退款中 7-已退款 8-已拒绝 */
    private Integer status;

    /** 支付状态: 0-未支付 1-已支付 2-已退款 */
    private Integer payStatus;

    /** 订单状态描述 */
    private String statusDesc;

    /** 支付状态描述 */
    private String payStatusDesc;

    /** 支付方式描述 */
    private String payTypeDesc;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

    /** 收货时间 */
    private LocalDateTime receiveTime;

    /** 收货人 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 备注 */
    private String remark;

    /** 创建时间（下单时间） */
    private LocalDateTime createdAt;

    /** 取消原因 */
    private String cancelReason;

    /** 退款原因 */
    private String refundReason;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 拒绝原因 */
    private String rejectReason;

    /** 拒绝时间 */
    private LocalDateTime rejectedAt;

    /** 用户ID（仅用于内部权限校验，不序列化到前端） */
    @JsonIgnore
    private Long userId;

    /** 订单商品明细列表 */
    private List<ItemVo> items;

    /** 发货记录列表 */
    private List<DeliveryVo> deliveries;

    /**
     * 订单商品明细VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemVo {

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

    /**
     * 订单发货记录VO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryVo {

        /** 物流公司 */
        private String deliveryCompany;

        /** 物流单号 */
        private String deliveryNo;

        /** 发货状态：1-已发货 2-已签收 3-物流异常 */
        private Integer deliveryStatus;

        /** 实际发货时间 */
        private LocalDateTime deliveryTime;
    }
}