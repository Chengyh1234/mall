package com.cyh.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallorder.entity.OrderDelivery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单发货记录数据访问层
 */
@Mapper
public interface OrderDeliveryMapper extends BaseMapper<OrderDelivery> {

    /**
     * 根据订单ID查询发货记录列表
     */
    List<OrderDelivery> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据物流单号查询发货记录
     */
    OrderDelivery selectByDeliveryNo(@Param("deliveryNo") String deliveryNo);

    /**
     * 根据状态查询发货记录列表
     */
    List<OrderDelivery> selectByStatus(@Param("deliveryStatus") Integer deliveryStatus);

    /**
     * 统计订单的发货记录数量
     */
    int countByOrderId(@Param("orderId") Long orderId);
}