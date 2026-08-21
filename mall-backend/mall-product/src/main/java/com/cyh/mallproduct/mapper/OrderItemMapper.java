package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细 Mapper 接口
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单明细列表
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
}