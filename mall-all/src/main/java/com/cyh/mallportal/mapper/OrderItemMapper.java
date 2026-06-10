package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.OrderItem;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单明细Mapper接口
 * 提供订单明细数据访问操作
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

    /**
     * 批量查询订单明细（用于分页列表批量加载）
     *
     * @param orderIds 订单ID列表
     * @return 订单明细列表
     */
    List<OrderItem> selectByOrderIds(@Param("orderIds") List<Long> orderIds);

    /**
     * 根据订单号查询订单明细列表
     *
     * @param orderNo 订单号
     * @return 订单明细列表
     */
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据SKU ID查询订单明细列表
     *
     * @param skuId SKU ID
     * @return 订单明细列表
     */
    List<OrderItem> selectBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据订单ID删除订单明细
     *
     * @param orderId 订单ID
     * @return 删除数量
     */
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询商家指定时间范围内各商品的销售汇总数据
     * 按商品分组统计销售额和销量，用于商品销售排行（条形图/南丁格尔玫瑰图）
     * 关联链：order_items → orders（过滤已完成+时间范围）→ spu（过滤商家）
     *
     * @param sellerId  商家用户ID
     * @param startTime 时间范围起点
     * @return key=商品名称, value={name=商品名, salesAmount=销售额, salesCount=销量}
     */
    @MapKey("name")
    Map<String, Map<String, Object>> selectProductSalesRanking(@Param("sellerId") Long sellerId, @Param("startTime") LocalDateTime startTime);
}