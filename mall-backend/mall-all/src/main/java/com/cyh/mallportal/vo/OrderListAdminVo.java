package com.cyh.mallportal.vo;

import com.cyh.mallportal.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单管理列表 VO（管理员后台）
 *
 * 运营管理员/超级管理员查询全部订单列表使用，排除内部字段
 * 对应接口：
 *   GET /order/admin/list
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderListAdminVo {

    /** 订单ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单状态: 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消 6-退款中 7-已退款 8-已拒绝 */
    private Integer status;

    /** 支付状态: 0-未支付 1-已支付 2-已退款 */
    private Integer payStatus;

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

    /** 收货人 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 备注 */
    private String remark;

    /** 下单时间 */
    private LocalDateTime createdAt;

    /** 取消原因 */
    private String cancelReason;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String refundReason;

    /** 拒绝原因 */
    private String rejectReason;

    /** 拒绝时间 */
    private LocalDateTime rejectedAt;

    /** 物流公司 */
    private String deliveryCompany;

    /** 物流单号 */
    private String deliveryNo;

    /** 发货时间 */
    private LocalDateTime deliveryTime;

    /** 收货时间 */
    private LocalDateTime receiveTime;

    public static OrderListAdminVo fromOrder(Order order) {
        OrderListAdminVo vo = new OrderListAdminVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setStatus(order.getStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setPayAmount(order.getPayAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayTime(order.getPayTime());
        vo.setExpireTime(order.getExpireTime());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCancelReason(order.getCancelReason());
        vo.setRefundAmount(order.getRefundAmount());
        vo.setRefundReason(order.getRefundReason());
        vo.setRejectReason(order.getRejectReason());
        vo.setRejectedAt(order.getRejectedAt());
        vo.setDeliveryCompany(order.getDeliveryCompany());
        vo.setDeliveryNo(order.getDeliveryNo());
        vo.setDeliveryTime(order.getDeliveryTime());
        vo.setReceiveTime(order.getReceiveTime());
        return vo;
    }
}