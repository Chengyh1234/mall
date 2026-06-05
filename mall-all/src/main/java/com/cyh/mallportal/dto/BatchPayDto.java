package com.cyh.mallportal.dto;

import lombok.Data;

import java.util.List;

/**
 * 批量付款请求 DTO
 * 封装用户选择批量付款的订单ID列表和支付方式
 */
@Data
public class BatchPayDto {

    /**
     * 待付款的订单ID列表（至少一个）
     */
    private List<Long> orderIds;

    /**
     * 支付方式：alipay-支付宝  wechat-微信（默认 alipay）
     */
    private String payType;
}