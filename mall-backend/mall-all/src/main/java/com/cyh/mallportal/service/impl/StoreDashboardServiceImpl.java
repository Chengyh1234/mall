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
import java.time.format.DateTimeFormatter;
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
     * 获取销售时间序列数据，聚合展示销售额、订单量、销量三个指标
     * 根据 period 参数自动切换按小时/按日分组，缺失时段自动补 0
     *
     * @param sellerId 商家用户ID
     * @param period   时间段：last24h | last7Days | thisMonth | last90Days | thisYear
     * @return 时间序列数据
     */
    @Override
    public StoreDashboardVo.SalesTimeSeries getSalesTimeSeries(Long sellerId, String period) {
        if (period == null || period.isEmpty()) {
            period = "last7Days";
        }
        String key = "dashboard:store:timeseries:" + sellerId + ":" + period;

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardVo.SalesTimeSeries.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 计算时间范围起点和分组模式
        LocalDateTime startTime;
        boolean isHourly;
        LocalDate today = LocalDate.now();

        switch (period) {
            case "last24h":
                // 最近24小时：从当前时间往前推23小时，取整到小时
                startTime = LocalDateTime.now().minusHours(23).withMinute(0).withSecond(0).withNano(0);
                isHourly = true;
                break;
            case "last7Days":
                startTime = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
                isHourly = false;
                break;
            case "thisMonth":
                startTime = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
                isHourly = false;
                break;
            case "last90Days":
                startTime = LocalDateTime.of(today.minusDays(89), LocalTime.MIN);
                isHourly = false;
                break;
            case "thisYear":
                startTime = LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);
                isHourly = false;
                break;
            default:
                startTime = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
                isHourly = false;
        }

        // Step3: 查询数据库
        Map<String, Map<String, Object>> resultMap = orderMapper.selectSalesTimeSeries(sellerId, startTime, isHourly);

        // Step4: 将 DB 结果转换为 label → 数据点的映射，便于补零
        Map<String, StoreDashboardVo.TimeSeriesPoint> dataMap = new LinkedHashMap<>();
        if (resultMap != null) {
            for (Map.Entry<String, Map<String, Object>> entry : resultMap.entrySet()) {
                Map<String, Object> row = entry.getValue();
                String label = row.get("label") != null ? row.get("label").toString() : "";
                BigDecimal salesAmount = row.get("salesAmount") != null
                        ? new BigDecimal(row.get("salesAmount").toString()) : BigDecimal.ZERO;
                int orderCount = row.get("orderCount") != null
                        ? ((Number) row.get("orderCount")).intValue() : 0;
                int salesVolume = row.get("salesVolume") != null
                        ? ((Number) row.get("salesVolume")).intValue() : 0;

                dataMap.put(label, new StoreDashboardVo.TimeSeriesPoint(
                        formatLabel(label, isHourly),
                        salesAmount, orderCount, salesVolume));
            }
        }

        // Step5: 生成完整时间序列，缺失时段补 0
        List<StoreDashboardVo.TimeSeriesPoint> dataPoints = new ArrayList<>();

        if (isHourly) {
            // 最近24小时，逐个填充小时，缺失时段补 0
            LocalDateTime hourCursor = startTime;
            for (int i = 0; i < 24; i++) {
                String dbKey = hourCursor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String displayLabel = hourCursor.format(DateTimeFormatter.ofPattern("HH:mm"));
                StoreDashboardVo.TimeSeriesPoint point = dataMap.containsKey(dbKey)
                        ? dataMap.get(dbKey)
                        : new StoreDashboardVo.TimeSeriesPoint(displayLabel, BigDecimal.ZERO, 0, 0);
                dataPoints.add(point);
                hourCursor = hourCursor.plusHours(1);
            }
        } else {
            // 按日填充，从 startTime 到 today
            LocalDate dateCursor = startTime.toLocalDate();
            LocalDate endDate = today;
            while (!dateCursor.isAfter(endDate)) {
                String dbKey = dateCursor.toString();
                String displayLabel = dateCursor.format(DateTimeFormatter.ofPattern("MM-dd"));
                StoreDashboardVo.TimeSeriesPoint point = dataMap.containsKey(dbKey)
                        ? dataMap.get(dbKey)
                        : new StoreDashboardVo.TimeSeriesPoint(displayLabel, BigDecimal.ZERO, 0, 0);
                dataPoints.add(point);
                dateCursor = dateCursor.plusDays(1);
            }
        }

        StoreDashboardVo.SalesTimeSeries result = new StoreDashboardVo.SalesTimeSeries(period, dataPoints);

        // Step6: 写入缓存，5 分钟过期
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    /**
     * 将数据库返回的完整时间标签转换为前端展示格式
     *
     * @param fullLabel 数据库返回的完整标签（如 2024-06-27 14:00:00 或 2024-06-27）
     * @param isHourly  是否按小时分组
     * @return 展示格式标签（HH:00 或 MM-DD）
     */
    private String formatLabel(String fullLabel, boolean isHourly) {
        if (fullLabel == null || fullLabel.isEmpty()) {
            return "";
        }
        try {
            if (isHourly) {
                LocalDateTime dt = LocalDateTime.parse(fullLabel, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                LocalDate d = LocalDate.parse(fullLabel, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                return d.format(DateTimeFormatter.ofPattern("MM-dd"));
            }
        } catch (Exception e) {
            // 解析失败时原样返回
            return fullLabel;
        }
    }

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