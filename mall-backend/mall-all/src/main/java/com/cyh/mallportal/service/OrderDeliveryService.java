package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.OrderDeliveryDto;
import com.cyh.mallportal.entity.OrderDelivery;

import java.util.List;

/**
 * 订单发货记录服务接口
 * 提供发货记录业务逻辑操作
 */
public interface OrderDeliveryService {

    /**
     * 创建发货记录
     *
     * @param deliveryDto 发货记录DTO
     * @return 发货记录ID
     */
    Long createDelivery(OrderDeliveryDto deliveryDto);

    /**
     * 更新发货状态为已签收
     *
     * @param deliveryNo 物流单号
     * @param signer     签收人
     * @return 是否更新成功
     */
    boolean signDelivery(String deliveryNo, String signer);

    /**
     * 更新发货状态为物流异常
     *
     * @param deliveryNo      物流单号
     * @param exceptionReason 异常原因
     * @return 是否更新成功
     */
    boolean exceptionDelivery(String deliveryNo, String exceptionReason);

    /**
     * 根据ID获取发货记录
     *
     * @param id 发货记录ID
     * @return 发货记录
     */
    OrderDelivery getById(Long id);

    /**
     * 根据订单ID获取发货记录列表
     *
     * @param orderId 订单ID
     * @return 发货记录列表
     */
    List<OrderDelivery> getByOrderId(Long orderId);

    /**
     * 根据物流单号获取发货记录
     *
     * @param deliveryNo 物流单号
     * @return 发货记录
     */
    OrderDelivery getByDeliveryNo(String deliveryNo);

    /**
     * 根据状态获取发货记录列表
     *
     * @param deliveryStatus 发货状态
     * @return 发货记录列表
     */
    List<OrderDelivery> getByStatus(Integer deliveryStatus);

    /**
     * 获取订单的发货记录数量
     *
     * @param orderId 订单ID
     * @return 发货记录数量
     */
    int countByOrderId(Long orderId);

    /**
     * 获取发货状态描述
     *
     * @param deliveryStatus 发货状态
     * @return 状态描述
     */
    String getStatusDesc(Integer deliveryStatus);
}