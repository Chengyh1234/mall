package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.mapper.StoreMapper;
import com.cyh.mallportal.mapper.UserMapper;
import com.cyh.mallportal.service.AdminDashboardService;
import com.cyh.mallportal.vo.AdminDashboardVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * 管理员仪表盘服务实现类
 * 聚合用户、订单、商品、店铺等多维度数据，提供平台级运营概览
 * <p>
 * 【缓存策略】
 * - 所有统计结果缓存 5 分钟，延迟可接受，大幅减少数据库聚合查询压力
 * - 运营概览和销售统计分别独立缓存，互不影响
 */
@Slf4j
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

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public AdminDashboardVo.DashboardOverview getDashboardOverview() {
        String key = "dashboard:admin:overview";

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AdminDashboardVo.DashboardOverview.class);
            } catch (JsonProcessingException e) {
                // 反序列化失败，删除脏缓存，重新查库
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，查数据库（原有逻辑）
        Long totalUsers = userMapper.selectCount(null);

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayOrders = orderMapper.countTodayOrders(todayStart);

        Long totalProducts = spuMapper.selectCount(new LambdaQueryWrapper<Spu>()
                .eq(Spu::getIsDeleted, false));

        BigDecimal todaySales = orderMapper.sumAdminCompletedSalesByTimeRange(todayStart);
        if (todaySales == null) {
            todaySales = BigDecimal.ZERO;
        }

        Long settledSellers = storeMapper.selectCount(new LambdaQueryWrapper<Store>()
                .eq(Store::getStatus, 1));

        AdminDashboardVo.DashboardOverview result = new AdminDashboardVo.DashboardOverview(
                totalUsers,
                todayOrders != null ? todayOrders : 0L,
                totalProducts,
                todaySales,
                settledSellers
        );

        // Step3: 写入缓存，5分钟过期
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    @Override
    public AdminDashboardVo.SalesStatistics getSalesStatistics() {
        String key = "dashboard:admin:sales-stats";

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AdminDashboardVo.SalesStatistics.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，查数据库（原有逻辑）
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime yearStart = LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);

        BigDecimal todaySales = orderMapper.sumAdminCompletedSalesByTimeRange(todayStart);
        BigDecimal last7DaysSales = orderMapper.sumAdminCompletedSalesByTimeRange(weekStart);
        BigDecimal thisMonthSales = orderMapper.sumAdminCompletedSalesByTimeRange(monthStart);
        BigDecimal thisYearSales = orderMapper.sumAdminCompletedSalesByTimeRange(yearStart);

        AdminDashboardVo.SalesStatistics result = new AdminDashboardVo.SalesStatistics(
                todaySales != null ? todaySales : BigDecimal.ZERO,
                last7DaysSales != null ? last7DaysSales : BigDecimal.ZERO,
                thisMonthSales != null ? thisMonthSales : BigDecimal.ZERO,
                thisYearSales != null ? thisYearSales : BigDecimal.ZERO
        );

        // Step3: 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }
}