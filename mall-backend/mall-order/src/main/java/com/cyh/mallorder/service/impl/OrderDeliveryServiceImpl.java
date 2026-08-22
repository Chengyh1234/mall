package com.cyh.mallorder.service.impl;

import com.cyh.mallorder.dto.OrderDeliveryDto;
import com.cyh.mallorder.entity.OrderDelivery;
import com.cyh.mallorder.mapper.OrderDeliveryMapper;
import com.cyh.mallorder.service.OrderDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单发货记录服务实现类
 * 提供发货记录业务逻辑的具体实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDeliveryServiceImpl implements OrderDeliveryService {

    private final OrderDeliveryMapper orderDeliveryMapper;

    /**
     * 创建发货记录
     * 当订单发货时调用，记录物流公司、运单号、收件人等信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDelivery(OrderDeliveryDto deliveryDto) {
        log.info("创建发货记录, 订单ID: {}", deliveryDto.getOrderId());

        OrderDelivery delivery = new OrderDelivery();
        delivery.setOrderId(deliveryDto.getOrderId());
        delivery.setDeliveryCompany(deliveryDto.getDeliveryCompany());
        delivery.setDeliveryNo(deliveryDto.getDeliveryNo());
        delivery.setDeliveryStatus(1);
        delivery.setSender(deliveryDto.getSender());
        delivery.setSenderId(deliveryDto.getSenderId());
        delivery.setDeliveryTime(LocalDateTime.now());
        delivery.setPackageCount(deliveryDto.getPackageCount() != null ? deliveryDto.getPackageCount() : 1);
        delivery.setWeight(deliveryDto.getWeight());
        delivery.setReceiverName(deliveryDto.getReceiverName());
        delivery.setReceiverPhone(deliveryDto.getReceiverPhone());
        delivery.setRemark(deliveryDto.getRemark());
        delivery.setCreatedAt(LocalDateTime.now());
        delivery.setUpdatedAt(LocalDateTime.now());

        orderDeliveryMapper.insert(delivery);
        log.info("发货记录创建成功, ID: {}", delivery.getId());
        return delivery.getId();
    }

    /**
     * 签收发货记录
     * 更新物流状态为"已签收"，记录签收人和签收时间。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean signDelivery(String deliveryNo, String signer) {
        log.info("签收发货记录, 物流单号: {}", deliveryNo);

        OrderDelivery delivery = orderDeliveryMapper.selectByDeliveryNo(deliveryNo);
        if (delivery == null) {
            log.warn("发货记录不存在: {}", deliveryNo);
            return false;
        }

        delivery.setDeliveryStatus(2);
        delivery.setSignTime(LocalDateTime.now());
        delivery.setSigner(signer);
        delivery.setUpdatedAt(LocalDateTime.now());

        orderDeliveryMapper.updateById(delivery);
        log.info("发货记录签收成功, 物流单号: {}", deliveryNo);
        return true;
    }

    /**
     * 标记物流异常
     * 更新物流状态为"物流异常"，记录异常原因。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exceptionDelivery(String deliveryNo, String exceptionReason) {
        log.info("物流异常处理, 物流单号: {}", deliveryNo);

        OrderDelivery delivery = orderDeliveryMapper.selectByDeliveryNo(deliveryNo);
        if (delivery == null) {
            log.warn("发货记录不存在: {}", deliveryNo);
            return false;
        }

        delivery.setDeliveryStatus(3);
        delivery.setExceptionReason(exceptionReason);
        delivery.setUpdatedAt(LocalDateTime.now());

        orderDeliveryMapper.updateById(delivery);
        log.info("物流异常处理成功, 物流单号: {}", deliveryNo);
        return true;
    }

    /**
     * 根据 ID 查询发货记录
     */
    @Override
    public OrderDelivery getById(Long id) {
        return orderDeliveryMapper.selectById(id);
    }

    /**
     * 根据订单 ID 查询发货记录列表
     */
    @Override
    public List<OrderDelivery> getByOrderId(Long orderId) {
        return orderDeliveryMapper.selectByOrderId(orderId);
    }

    /**
     * 根据物流单号查询发货记录
     */
    @Override
    public OrderDelivery getByDeliveryNo(String deliveryNo) {
        return orderDeliveryMapper.selectByDeliveryNo(deliveryNo);
    }

    /**
     * 根据物流状态查询发货记录列表
     */
    @Override
    public List<OrderDelivery> getByStatus(Integer deliveryStatus) {
        return orderDeliveryMapper.selectByStatus(deliveryStatus);
    }

    /**
     * 统计指定订单的发货记录数
     */
    @Override
    public int countByOrderId(Long orderId) {
        return orderDeliveryMapper.countByOrderId(orderId);
    }

    /**
     * 获取物流状态的中文描述
     */
    @Override
    public String getStatusDesc(Integer deliveryStatus) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(1, "已发货");
        statusMap.put(2, "已签收");
        statusMap.put(3, "物流异常");
        return statusMap.getOrDefault(deliveryStatus, "未知状态");
    }
}