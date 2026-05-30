package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 发货记录DTO
 * 参数说明：
 * - 基本信息：orderId, deliveryCompany, deliveryNo
 * - 发货信息：sender, senderId, packageCount, weight
 * - 收货信息：receiverName, receiverPhone
 * - 其他：remark
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeliveryDto {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 物流公司
     */
    private String deliveryCompany;

    /**
     * 物流单号
     */
    private String deliveryNo;

    /**
     * 发货人
     */
    private String sender;

    /**
     * 发货人ID
     */
    private Long senderId;

    /**
     * 包裹数量
     */
    private Integer packageCount;

    /**
     * 包裹重量(kg)
     */
    private BigDecimal weight;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 备注
     */
    private String remark;
}