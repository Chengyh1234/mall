package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.mapper.StoreMapper;
import com.cyh.mallportal.mapper.UserMapper;
import com.cyh.mallportal.service.AdminDashboardService;
import com.cyh.mallportal.vo.AdminDashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 管理员仪表盘服务实现类
 * 聚合用户、订单、商品、店铺等多维度数据，提供平台级运营概览
 */
@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Override
    public AdminDashboardVo.DashboardOverview getDashboardOverview() {
        // 总用户数
        Long totalUsers = userMapper.selectCount(null);

        // 今日订单数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayOrders = orderMapper.countTodayOrders(todayStart);

        // 商品总数（未逻辑删除的 SPU）
        Long totalProducts = spuMapper.selectCount(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getIsDeleted, false));

        // 今日销售额（已完成订单实付金额）
        BigDecimal todaySales = orderMapper.sumAdminCompletedSalesByTimeRange(todayStart);
        if (todaySales == null) {
            todaySales = BigDecimal.ZERO;
        }

        // 入驻卖家数（状态为正常的店铺数）
        Long settledSellers = storeMapper.selectCount(new LambdaQueryWrapper<Store>()
                .eq(Store::getStatus, 1));

        return new AdminDashboardVo.DashboardOverview(
                totalUsers,
                todayOrders != null ? todayOrders : 0L,
                totalProducts,
                todaySales,
                settledSellers
        );
    }

    @Override
    public AdminDashboardVo.SalesStatistics getSalesStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime yearStart = LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);

        BigDecimal todaySales = orderMapper.sumAdminCompletedSalesByTimeRange(todayStart);
        BigDecimal last7DaysSales = orderMapper.sumAdminCompletedSalesByTimeRange(weekStart);
        BigDecimal thisMonthSales = orderMapper.sumAdminCompletedSalesByTimeRange(monthStart);
        BigDecimal thisYearSales = orderMapper.sumAdminCompletedSalesByTimeRange(yearStart);

        return new AdminDashboardVo.SalesStatistics(
                todaySales != null ? todaySales : BigDecimal.ZERO,
                last7DaysSales != null ? last7DaysSales : BigDecimal.ZERO,
                thisMonthSales != null ? thisMonthSales : BigDecimal.ZERO,
                thisYearSales != null ? thisYearSales : BigDecimal.ZERO
        );
    }
}