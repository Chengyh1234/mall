package com.cyh.mallorder.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallorder.mapper.OrderItemMapper;
import com.cyh.mallorder.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单仪表盘内部接口控制器（供其他微服务 Feign 调用）
 * <p>
 * 路径统一使用 /internal/order/ 前缀，区别于对外公开接口。
 * 提供商家店铺经营数据的统计查询，供 mall-store 的仪表盘使用。
 */
@RestController
@RequestMapping("/internal/order")
@RequiredArgsConstructor
public class OrderDashboardInternalController {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    /**
     * 统计商家指定时间范围内已完成订单的销售总额
     */
    @GetMapping("/dashboard/store/sales")
    public Result<BigDecimal> getSumCompletedSales(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime) {
        return Result.success(orderMapper.sumCompletedSalesByTimeRange(sellerId, startTime));
    }

    /**
     * 统计商家指定时间范围内已完成订单的商品总成本
     */
    @GetMapping("/dashboard/store/cost")
    public Result<BigDecimal> getSumCompletedCost(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime) {
        return Result.success(orderMapper.sumCompletedCostByTimeRange(sellerId, startTime));
    }

    /**
     * 查询商家近7天每日销售额明细
     */
    @GetMapping("/dashboard/store/daily-sales")
    public Result<Map<String, Map<String, Object>>> getDailySales(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime) {
        return Result.success(orderMapper.selectDailySalesBySellerId(sellerId, startTime));
    }

    /**
     * 统计商家各时段/每日的销售额、订单量、销量
     */
    @GetMapping("/dashboard/store/time-series")
    public Result<Map<String, Map<String, Object>>> getSalesTimeSeries(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime,
            @RequestParam boolean isHourly) {
        return Result.success(orderMapper.selectSalesTimeSeries(sellerId, startTime, isHourly));
    }

    /**
     * 查询商家指定时间范围内各商品的销售汇总数据
     */
    @GetMapping("/dashboard/store/product-ranking")
    public Result<Map<String, Map<String, Object>>> getProductRanking(
            @RequestParam Long sellerId,
            @RequestParam LocalDateTime startTime) {
        return Result.success(orderItemMapper.selectProductSalesRanking(sellerId, startTime));
    }

    // ==================== 管理员仪表盘 ====================

    /**
     * 统计平台各时段/每日的销售额、订单量、销量（不区分商家）
     */
    @GetMapping("/dashboard/admin/time-series")
    public Result<Map<String, Map<String, Object>>> getAdminSalesTimeSeries(
            @RequestParam LocalDateTime startTime,
            @RequestParam boolean isHourly) {
        return Result.success(orderMapper.selectAdminSalesTimeSeries(startTime, isHourly));
    }

    /**
     * 统计今日订单数
     */
    @GetMapping("/dashboard/admin/today-orders")
    public Result<Long> getTodayOrders(@RequestParam LocalDateTime todayStart) {
        return Result.success(orderMapper.countTodayOrders(todayStart));
    }

    /**
     * 统计平台指定时间范围内已完成订单的销售总额
     */
    @GetMapping("/dashboard/admin/completed-sales")
    public Result<BigDecimal> getAdminCompletedSales(@RequestParam LocalDateTime startTime) {
        return Result.success(orderMapper.sumAdminCompletedSalesByTimeRange(startTime));
    }
}