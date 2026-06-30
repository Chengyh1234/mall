package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.entity.Order;
import com.cyh.mallportal.vo.OrderStatusCountVo;
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
     * 分页查询用户订单列表
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<Order> selectByUserIdPaged(Page<Order> page, @Param("userId") Long userId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 批量查询用户订单（用于批量付款/批量操作校验）
     * 自动过滤 is_deleted = 0，仅返回属于该用户的订单
     *
     * @param ids    订单ID列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> selectByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /**
     * 根据用户ID和状态查询订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> selectByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 分页查询用户订单列表（按状态筛选）
     *
     * @param page   分页对象
     * @param userId 用户ID
     * @param status 订单状态
     * @return 分页结果
     */
    IPage<Order> selectByUserIdAndStatusPaged(Page<Order> page, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户指定状态订单数量
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单数量
     */
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 批量统计用户各状态订单数量
     * 一次性返回待付款/待发货/待收货/退款中的数量，用于前端"我的订单"各标签角标
     *
     * @param userId 用户ID
     * @return 各状态订单数量 VO
     */
    OrderStatusCountVo countOrderStatusByUserId(@Param("userId") Long userId);

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
     * 统计商家指定时间范围内已完成订单的商品总成本
     * 通过 order_items 关联 sku 表，计算 SUM(quantity * cost_price)
     * 用于仪表盘利润计算：利润 = 销售额 - 成本
     *
     * @param sellerId  商家用户ID
     * @param startTime 时间范围起点
     * @return 商品总成本
     */
    BigDecimal sumCompletedCostByTimeRange(@Param("sellerId") Long sellerId, @Param("startTime") LocalDateTime startTime);

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
     * 统计商家店铺进行中/未完结的订单数量
     * 用于校验店铺能否注销：仅当进行中订单数量为0时才允许注销
     * 进行中状态：1-待付款 2-待发货 3-待收货 6-退款中 8-已拒绝
     *
     * @param sellerId 商家用户ID
     * @return 进行中的订单总数
     */
    int countActiveBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 分页查询商家店铺订单列表（多条件筛选）
     * 支持按状态、用户ID、订单号、支付/发货/收货时间范围筛选
     *
     * @param page      分页对象
     * @param sellerId  商家用户ID
     * @param status    订单状态（可选）
     * @param userId    用户ID（可选）
     * @param orderNo   订单号（可选，模糊匹配）
     * @param payTimeStart    支付时间范围-起始（可选）
     * @param payTimeEnd      支付时间范围-结束（可选）
     * @param deliveryTimeStart 发货时间范围-起始（可选）
     * @param deliveryTimeEnd   发货时间范围-结束（可选）
     * @param receiveTimeStart 收货时间范围-起始（可选）
     * @param receiveTimeEnd   收货时间范围-结束（可选）
     * @return 分页结果
     */
    IPage<Order> selectBySellerIdWithFilters(Page<Order> page,
                                             @Param("sellerId") Long sellerId,
                                             @Param("status") Integer status,
                                             @Param("userId") Long userId,
                                             @Param("orderNo") String orderNo,
                                             @Param("payTimeStart") LocalDateTime payTimeStart,
                                             @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                             @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                             @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                             @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                             @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    /**
     * 统计商家店铺订单总数（多条件筛选）
     * 与 selectBySellerIdWithFilters 条件完全一致，用于分页总记录数
     */
    int countBySellerIdWithFilters(@Param("sellerId") Long sellerId,
                                   @Param("status") Integer status,
                                   @Param("userId") Long userId,
                                   @Param("orderNo") String orderNo,
                                   @Param("payTimeStart") LocalDateTime payTimeStart,
                                   @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                   @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                   @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                   @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                   @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    /**
     * 根据订单号和商家用户ID查询订单
     * 用于校验订单是否属于该商家，通过 order_items → spu 关联
     *
     * @param orderNo  订单号
     * @param sellerId 商家用户ID
     * @return 订单实体，不属于该商家时返回 null
     */
    Order selectByOrderNoAndSellerId(@Param("orderNo") String orderNo, @Param("sellerId") Long sellerId);

    /**
     * 查询过期未支付的订单列表（status=1 且 expire_time < 当前时间）
     *
     * @param now 当前时间
     * @return 过期订单列表
     */
    List<Order> selectExpiredUnpaidOrders(@Param("now") LocalDateTime now);

    // ==================== 全局订单查询（运营管理员/超级管理员） ====================

    /**
     * 分页查询全部订单（运营管理员/超级管理员使用）
     * 查询所有订单，按创建时间倒序排列
     *
     * @param page 分页对象
     * @return 分页结果
     */
    IPage<Order> selectAllOrders(Page<Order> page);

    /**
     * 分页查询全部订单（按状态筛选）
     *
     * @param page   分页对象
     * @param status 订单状态
     * @return 分页结果
     */
    IPage<Order> selectAllOrdersByStatus(Page<Order> page, @Param("status") Integer status);

    /**
     * 统计全部订单总数
     *
     * @return 订单总数
     */
    int countAllOrders();

    /**
     * 统计指定状态的订单总数
     *
     * @param status 订单状态
     * @return 订单总数
     */
    int countAllOrdersByStatus(@Param("status") Integer status);

    /**
     * 分页查询全部订单（多条件筛选）
     * 支持按状态、用户ID、订单号、支付/发货/收货时间范围筛选
     */
    IPage<Order> selectAllOrdersWithFilters(Page<Order> page,
                                            @Param("status") Integer status,
                                            @Param("userId") Long userId,
                                            @Param("orderNo") String orderNo,
                                            @Param("payTimeStart") LocalDateTime payTimeStart,
                                            @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                            @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                            @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                            @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                            @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    /**
     * 统计全部订单总数（多条件筛选）
     * 与 selectAllOrdersWithFilters 条件完全一致
     */
    int countAllOrdersWithFilters(@Param("status") Integer status,
                                  @Param("userId") Long userId,
                                  @Param("orderNo") String orderNo,
                                  @Param("payTimeStart") LocalDateTime payTimeStart,
                                  @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                  @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                  @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                  @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                  @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    // ==================== 管理员订单详情查询（不过滤 is_deleted） ====================

    /**
     * 管理员根据订单号查询订单详情（不过滤 is_deleted，可查看所有订单包括已删除的）
     *
     * @param orderNo 订单号
     * @return 订单实体
     */
    Order selectByOrderNoForAdmin(@Param("orderNo") String orderNo);

    // ==================== 商家仪表盘时间序列统计 ====================

    /**
     * 统计商家指定时间范围内各时段/每日的销售额、订单量、销量
     * 通过 order_items 关联 spu 过滤商家，distinct 去重保证订单金额不重复求和
     * 用于仪表盘时间序列折线图展示
     *
     * @param sellerId  商家用户ID
     * @param startTime 时间范围起点
     * @param isHourly  是否按小时分组（true=按小时，false=按天）
     * @return key=时间标签, value={label=标签, salesAmount=销售额, orderCount=订单量, salesVolume=销量}
     */
    @MapKey("label")
    Map<String, Map<String, Object>> selectSalesTimeSeries(
            @Param("sellerId") Long sellerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("isHourly") boolean isHourly);

    // ==================== 管理员仪表盘时间序列统计 ====================

    /**
     * 统计平台指定时间范围内各时段/每日的销售额、订单量、销量
     * 不区分商家，统计全部已完成订单（status=4）
     * 用于管理员仪表盘时间序列折线图展示
     *
     * @param startTime 时间范围起点
     * @param isHourly  是否按小时分组（true=按小时，false=按天）
     * @return key=时间标签, value={label=标签, salesAmount=销售额, orderCount=订单量, salesVolume=销量}
     */
    @MapKey("label")
    Map<String, Map<String, Object>> selectAdminSalesTimeSeries(
            @Param("startTime") LocalDateTime startTime,
            @Param("isHourly") boolean isHourly);

    // ==================== 平台仪表盘统计（超级管理员） ====================

    /**
     * 统计今日订单数
     * 统计当日 00:00:00 至今创建的订单总数
     *
     * @param todayStart 今日零点
     * @return 今日订单数
     */
    Long countTodayOrders(@Param("todayStart") LocalDateTime todayStart);

    /**
     * 统计平台指定时间范围内已完成订单的销售总额
     * 不区分商家，统计全部已完成订单（status=4）
     *
     * @param startTime 时间范围起点
     * @return 销售总额
     */
    BigDecimal sumAdminCompletedSalesByTimeRange(@Param("startTime") LocalDateTime startTime);
}