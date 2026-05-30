package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.entity.Order;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单Mapper接口
 * 提供订单数据访问操作
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据用户ID查询订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 根据用户ID和状态查询订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 根据状态查询订单数量
     *
     * @param status 订单状态
     * @return 订单数量
     */
    int countByStatus(@Param("status") Integer status);

    /**
     * 统计商家指定时间范围内已完成订单的销售总额
     * 使用子查询去重，避免同一订单在 order_items 多行联表中重复求和
     *
     * @param sellerId  商家用户ID
     * @param startTime 时间范围起点
     * @return 销售总额
     */
    BigDecimal sumCompletedSalesByTimeRange(@Param("sellerId") Long sellerId, @Param("startTime") LocalDateTime startTime);

    /**
     * 查询商家近7天每日销售额明细
     * 按 DATE(created_at) 分组后以日期为 key 返回 Map，用于折线图展示
     *
     * @param sellerId  商家用户ID
     * @param startTime 7天前零点
     * @return key=日期字符串(YYYY-MM-DD), value={date=日期, amount=销售额}
     */
    @MapKey("date")
    Map<String, Map<String, Object>> selectDailySalesBySellerId(@Param("sellerId") Long sellerId, @Param("startTime") LocalDateTime startTime);

    // ==================== 商家订单查询 ====================

    /**
     * 分页查询商家店铺订单列表
     * 通过订单项 → SPU 关联到商家，使用 DISTINCT 去重避免同一订单多行联表产生重复
     *
     * @param page     分页对象
     * @param sellerId 商家用户ID（对应 spu.seller_id）
     * @return 分页结果
     */
    IPage<Order> selectBySellerId(Page<Order> page, @Param("sellerId") Long sellerId);

    /**
     * 分页查询商家店铺订单列表（按状态筛选）
     *
     * @param page     分页对象
     * @param sellerId 商家用户ID
     * @param status   订单状态
     * @return 分页结果
     */
    IPage<Order> selectBySellerIdAndStatus(Page<Order> page, @Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 统计商家店铺订单总数
     *
     * @param sellerId 商家用户ID
     * @return 订单总数
     */
    int countBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 统计商家店铺指定状态订单总数
     *
     * @param sellerId 商家用户ID
     * @param status   订单状态
     * @return 订单总数
     */
    int countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 根据订单号和商家用户ID查询订单
     * 用于校验订单是否属于该商家，通过 order_items → spu 关联
     *
     * @param orderNo  订单号
     * @param sellerId 商家用户ID
     * @return 订单实体，不属于该商家时返回 null
     */
    Order selectByOrderNoAndSellerId(@Param("orderNo") String orderNo, @Param("sellerId") Long sellerId);
}