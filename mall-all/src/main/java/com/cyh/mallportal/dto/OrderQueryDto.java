package com.cyh.mallportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单查询DTO
 * 参数说明：
 * - userId：用户ID
 * - status：订单状态（可选）
 * - orderNo：订单号（可选）
 * - page：页码
 * - pageSize：每页条数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderQueryDto {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页条数
     */
    private Integer pageSize;
}