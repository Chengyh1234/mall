package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 对应数据库表 orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "orders", autoResultMap = true)
public class Order implements Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    @TableField(value = "order_no")
    private String orderNo;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 订单总金额
     */
    @TableField(value = "total_amount")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @TableField(value = "pay_amount")
    private BigDecimal payAmount;

    /**
     * 优惠金额
     */
    @TableField(value = "discount_amount")
    private BigDecimal discountAmount;

    /**
     * 运费
     */
    @TableField(value = "freight_amount")
    private BigDecimal freightAmount;

    /**
     * 订单状态: 1-待付款 2-待发货 3-待收货 4-已完成 5-已取消 6-退款中 7-已退款
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 支付状态: 0-未支付 1-已支付 2-已退款
     */
    @TableField(value = "pay_status")
    private Integer payStatus;

    /**
     * 支付时间
     */
    @TableField(value = "pay_time")
    private LocalDateTime payTime;

    /**
     * 支付方式: alipay-支付宝 wechat-微信
     */
    @TableField(value = "pay_type")
    private String payType;

    /**
     * 配送方式
     */
    @TableField(value = "delivery_type")
    private String deliveryType;

    /**
     * 物流公司
     */
    @TableField(value = "delivery_company")
    private String deliveryCompany;

    /**
     * 物流单号
     */
    @TableField(value = "delivery_no")
    private String deliveryNo;

    /**
     * 发货时间
     */
    @TableField(value = "delivery_time")
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    @TableField(value = "receive_time")
    private LocalDateTime receiveTime;

    /**
     * 收货人
     */
    @TableField(value = "receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField(value = "receiver_phone")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @TableField(value = "receiver_address")
    private String receiverAddress;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 取消原因
     */
    @TableField(value = "cancel_reason")
    private String cancelReason;

    /**
     * 乐观锁版本号
     */
    @Version
    @TableField(value = "version")
    private Integer version;
}