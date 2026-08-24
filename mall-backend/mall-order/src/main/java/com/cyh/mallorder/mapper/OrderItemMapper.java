package com.cyh.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallorder.entity.OrderItem;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单明细数据访问层
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单明细
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单明细
     */
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 批量查询订单明细
     */
    List<OrderItem> selectByOrderIds(@Param("list") List<Long> orderIds);

    /**
     * 根据SKU ID查询订单明细
     */
    List<OrderItem> selectBySkuId(@Param("skuId") Long skuId);

    // ==================== 商家仪表盘统计（Feign 内部调用） ====================

    /**
     * 查询商家指定时间范围内各商品的销售汇总数据
     * 用于商品销售排行
     */
    @MapKey("name")
    Map<String, Map<String, Object>> selectProductSalesRanking(@Param("sellerId") Long sellerId, @Param("startTime") LocalDateTime startTime);
}