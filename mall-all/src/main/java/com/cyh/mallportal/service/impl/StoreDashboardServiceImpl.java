package com.cyh.mallportal.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallportal.mapper.OrderItemMapper;
import com.cyh.mallportal.mapper.OrderMapper;
import com.cyh.mallportal.service.StoreDashboardService;
import com.cyh.mallportal.vo.StoreDashboardVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商家仪表盘服务实现类
 * 聚合订单、订单明细数据，提供销售 KPI、趋势和商品排行功能
 * <p>
 * 【缓存策略】
 * - 所有统计结果缓存 5 分钟，延迟可接受，大幅减少数据库聚合查询压力
 * - 缓存 Key 按 sellerId 隔离，不同商家互不干扰
 */
@Slf4j
@Service
public class StoreDashboardServiceImpl implements StoreDashboardService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 获取销售 KPI 总览
     * 返回今日、近7天、本月、本年四个时间维度的销售额和利润
     * 利润 = 销售额 - 订单商品总成本（quantity * cost_price）
     *
     * @param sellerId 商家用户ID
     * @return KPI 总览数据（含销售额和利润）
     */
    @Override
    public StoreDashboardVo.KpiOverview getSalesKpiOverview(Long sellerId) {
        String key = "dashboard:store:kpi:" + sellerId;

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardVo.KpiOverview.class);
            } catch (JsonProcessingException e) {
                // 反序列化失败，删除脏缓存，重新查库
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，查数据库（原有逻辑）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime yearStart = LocalDateTime.of(LocalDate.now().withDayOfYear(1), LocalTime.MIN);

        BigDecimal today = orderMapper.sumCompletedSalesByTimeRange(sellerId, todayStart);
        BigDecimal last7Days = orderMapper.sumCompletedSalesByTimeRange(sellerId, weekStart);
        BigDecimal thisMonth = orderMapper.sumCompletedSalesByTimeRange(sellerId, monthStart);
        BigDecimal thisYear = orderMapper.sumCompletedSalesByTimeRange(sellerId, yearStart);

        // 利润 = 销售额 - 成本
        BigDecimal todayCost = orderMapper.sumCompletedCostByTimeRange(sellerId, todayStart);
        BigDecimal last7DaysCost = orderMapper.sumCompletedCostByTimeRange(sellerId, weekStart);
        BigDecimal thisMonthCost = orderMapper.sumCompletedCostByTimeRange(sellerId, monthStart);
        BigDecimal thisYearCost = orderMapper.sumCompletedCostByTimeRange(sellerId, yearStart);

        System.out.println("todayCost: " + todayCost);
        System.out.println("last7DaysCost: " + last7DaysCost);
        System.out.println("thisMonthCost: " + thisMonthCost);
        System.out.println("thisYearCost: " + thisYearCost);

        StoreDashboardVo.KpiOverview result = new StoreDashboardVo.KpiOverview(
                today != null ? today : BigDecimal.ZERO,
                last7Days != null ? last7Days : BigDecimal.ZERO,
                thisMonth != null ? thisMonth : BigDecimal.ZERO,
                thisYear != null ? thisYear : BigDecimal.ZERO,
                today != null ? today.subtract(todayCost != null ? todayCost : BigDecimal.ZERO) : BigDecimal.ZERO,
                last7Days != null ? last7Days.subtract(last7DaysCost != null ? last7DaysCost : BigDecimal.ZERO) : BigDecimal.ZERO,
                thisMonth != null ? thisMonth.subtract(thisMonthCost != null ? thisMonthCost : BigDecimal.ZERO) : BigDecimal.ZERO,
                thisYear != null ? thisYear.subtract(thisYearCost != null ? thisYearCost : BigDecimal.ZERO) : BigDecimal.ZERO
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

    /**
     * 获取销售趋势数据
     * 返回最近7天每天的销售额，无数据的日期值为0，用于折线图展示
     *
     * @param sellerId 商家用户ID
     * @return 销售趋势数据
     */
    @Override
    public StoreDashboardVo.SalesTrend getSalesTrend(Long sellerId) {
        String key = "dashboard:store:trend:" + sellerId;

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardVo.SalesTrend.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，查数据库（原有逻辑）
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);

        Map<String, Map<String, Object>> resultMap = orderMapper.selectDailySalesBySellerId(sellerId, weekStart);

        Map<String, BigDecimal> dateAmountMap = new LinkedHashMap<>();
        if (resultMap != null) {
            for (Map.Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
                String date = entry.getKey();
                Map<String, Object> row = entry.getValue();
                BigDecimal amount = row.get("amount") != null
                        ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
                dateAmountMap.put(date, amount);
            }
        }

        List<String> dates = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            String dateStr = day.toString();
            dates.add(dateStr);
            values.add(dateAmountMap.getOrDefault(dateStr, BigDecimal.ZERO));
        }

        StoreDashboardVo.SalesTrend result = new StoreDashboardVo.SalesTrend(dates, values);

        // Step3: 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    /**
     * 获取商品销售排行数据
     * 按销售额降序排列，支持时间段参数，计算每个商品的销售额占比和销量占比
     *
     * @param sellerId 商家用户ID
     * @param period   时间段：today | last7Days | thisMonth | thisYear，默认 last7Days
     * @return 商品销售排行列表
     */
    @Override
    public List<StoreDashboardVo.ProductRankItem> getProductRanking(Long sellerId, String period) {
        if (period == null || period.isEmpty()) {
            period = "last7Days";
        }
        String key = "dashboard:store:ranking:" + sellerId + ":" + period;

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StoreDashboardVo.ProductRankItem.class));
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，查数据库（原有逻辑）
        LocalDateTime startTime = getPeriodStartTime(period);

        Map<String, Map<String, Object>> resultMap = orderItemMapper.selectProductSalesRanking(sellerId, startTime);
        if (resultMap == null || resultMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<StoreDashboardVo.ProductRankItem> result = resultMap.values().stream().map(row -> {
            StoreDashboardVo.ProductRankItem item = new StoreDashboardVo.ProductRankItem();
            item.setName(row.get("name") != null ? row.get("name").toString() : "未知商品");
            item.setSalesAmount(row.get("salesAmount") != null
                    ? new BigDecimal(row.get("salesAmount").toString()) : BigDecimal.ZERO);
            item.setSalesCount(row.get("salesCount") != null
                    ? ((Number) row.get("salesCount")).intValue() : 0);
            return item;
        }).collect(Collectors.toList());

        result.sort((a, b) -> b.getSalesAmount().compareTo(a.getSalesAmount()));

        BigDecimal totalSales = result.stream()
                .map(StoreDashboardVo.ProductRankItem::getSalesAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalCount = result.stream()
                .mapToInt(StoreDashboardVo.ProductRankItem::getSalesCount)
                .sum();

        if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
            for (StoreDashboardVo.ProductRankItem item : result) {
                BigDecimal percent = item.getSalesAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(totalSales, 1, RoundingMode.HALF_UP);
                item.setPercentOfTotal(percent);
            }
        } else {
            for (StoreDashboardVo.ProductRankItem item : result) {
                item.setPercentOfTotal(BigDecimal.ZERO);
            }
        }

        if (totalCount > 0) {
            for (StoreDashboardVo.ProductRankItem item : result) {
                BigDecimal countPercent = new BigDecimal(item.getSalesCount())
                        .multiply(new BigDecimal("100"))
                        .divide(new BigDecimal(totalCount), 1, RoundingMode.HALF_UP);
                item.setCountPercentOfTotal(countPercent);
            }
        } else {
            for (StoreDashboardVo.ProductRankItem item : result) {
                item.setCountPercentOfTotal(BigDecimal.ZERO);
            }
        }

        // Step3: 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    /**
     * 根据 period 参数计算时间范围起点
     *
     * @param period 时间段标识
     * @return 时间范围起点
     */
    private LocalDateTime getPeriodStartTime(String period) {
        LocalDate today = LocalDate.now();
        switch (period) {
            case "today":
                return LocalDateTime.of(today, LocalTime.MIN);
            case "last7Days":
                return LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
            case "thisMonth":
                return LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
            case "thisYear":
                return LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);
            default:
                return LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
        }
    }
}