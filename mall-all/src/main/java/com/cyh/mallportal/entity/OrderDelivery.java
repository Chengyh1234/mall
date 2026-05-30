package com.cyh.mallportal.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单发货记录实体类
 * 对应数据库表 order_delivery
 * 支持一个订单多次发货的场景
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "order_delivery", autoResultMap = true)
public class OrderDelivery implements Serializable {

    /**
     * 发货记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID（关联订单表）
     */
    @TableField(value = "order_id")
    private Long orderId;

    /**
     * 物流公司（如：顺丰、中通）
     */
    @TableField(value = "delivery_company")
    private String deliveryCompany;

    /**
     * 物流单号
     */
    @TableField(value = "delivery_no")
    private String deliveryNo;

    /**
     * 发货状态：1-已发货 2-已签收 3-物流异常
     */
    @TableField(value = "delivery_status")
    private Integer deliveryStatus;

    /**
     * 发货人（操作员）
     */
    @TableField(value = "sender")
    private String sender;

    /**
     * 发货人ID（关联用户表）
     */
    @TableField(value = "sender_id")
    private Long senderId;

    /**
     * 实际发货时间
     */
    @TableField(value = "delivery_time")
    private LocalDateTime deliveryTime;

    /**
     * 包裹数量
     */
    @TableField(value = "package_count")
    private Integer packageCount;

    /**
     * 包裹重量(kg)
     */
    @TableField(value = "weight")
    private BigDecimal weight;

    /**
     * 收货人姓名
     */
    @TableField(value = "receiver_name")
    private String receiverName;

    /**
     * 收货人电话
     */
    @TableField(value = "receiver_phone")
    private String receiverPhone;

    /**
     * 签收时间
     */
    @TableField(value = "sign_time")
    private LocalDateTime signTime;

    /**
     * 签收人
     */
    @TableField(value = "signer")
    private String signer;

    /**
     * 异常原因
     */
    @TableField(value = "exception_reason")
    private String exceptionReason;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 记录创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}