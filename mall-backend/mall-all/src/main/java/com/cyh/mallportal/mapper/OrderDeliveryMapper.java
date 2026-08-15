package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.OrderDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单发货记录Mapper接口
 * 提供发货记录数据访问操作
 */
@Mapper
public interface OrderDeliveryMapper extends BaseMapper<OrderDelivery> {

    /**
     * 根据订单ID查询发货记录列表
     *
     * @param orderId 订单ID
     * @return 发货记录列表
     */
    List<OrderDelivery> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据物流单号查询发货记录
     *
     * @param deliveryNo 物流单号
     * @return 发货记录
     */
    OrderDelivery selectByDeliveryNo(@Param("deliveryNo") String deliveryNo);

    /**
     * 根据状态查询发货记录列表
     *
     * @param deliveryStatus 发货状态
     * @return 发货记录列表
     */
    List<OrderDelivery> selectByStatus(@Param("deliveryStatus") Integer deliveryStatus);

    /**
     * 查询订单的发货记录数量
     *
     * @param orderId 订单ID
     * @return 发货记录数量
     */
    int countByOrderId(@Param("orderId") Long orderId);
}