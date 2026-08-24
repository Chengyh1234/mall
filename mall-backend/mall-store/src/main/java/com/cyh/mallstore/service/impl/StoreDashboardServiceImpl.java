package com.cyh.mallstore.service.impl;

import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallstore.feign.OrderDashboardClient;
import com.cyh.mallstore.service.StoreDashboardService;
import com.cyh.mallstore.vo.StoreDashboardVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * 通过 Feign 调用 mall-order 获取订单统计数据进行聚合
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreDashboardServiceImpl implements StoreDashboardService {

    private final OrderDashboardClient orderDashboardClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public StoreDashboardVo.KpiOverview getSalesKpiOverview(Long sellerId) {
        String key = "dashboard:store:kpi:" + sellerId;

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardVo.KpiOverview.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        // Step2: 缓存未命中，通过 Feign 调用 mall-order 获取数据
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime yearStart = LocalDateTime.of(LocalDate.now().withDayOfYear(1), LocalTime.MIN);

        BigDecimal today = unwrap(orderDashboardClient.getSumCompletedSales(sellerId, todayStart));
        BigDecimal last7Days = unwrap(orderDashboardClient.getSumCompletedSales(sellerId, weekStart));
        BigDecimal thisMonth = unwrap(orderDashboardClient.getSumCompletedSales(sellerId, monthStart));
        BigDecimal thisYear = unwrap(orderDashboardClient.getSumCompletedSales(sellerId, yearStart));

        BigDecimal todayCost = unwrap(orderDashboardClient.getSumCompletedCost(sellerId, todayStart));
        BigDecimal last7DaysCost = unwrap(orderDashboardClient.getSumCompletedCost(sellerId, weekStart));
        BigDecimal thisMonthCost = unwrap(orderDashboardClient.getSumCompletedCost(sellerId, monthStart));
        BigDecimal thisYearCost = unwrap(orderDashboardClient.getSumCompletedCost(sellerId, yearStart));

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

        // Step3: 写入缓存
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    @Override
    public StoreDashboardVo.SalesTrend getSalesTrend(Long sellerId) {
        String key = "dashboard:store:trend:" + sellerId;

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardVo.SalesTrend.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(6), LocalTime.MIN);
        Map<String, Map<String, Object>> resultMap = unwrap(orderDashboardClient.getDailySales(sellerId, weekStart));

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

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    @Override
    public StoreDashboardVo.SalesTimeSeries getSalesTimeSeries(Long sellerId, String period) {
        if (period == null || period.isEmpty()) {
            period = "last7Days";
        }
        String key = "dashboard:store:timeseries:" + sellerId + ":" + period;

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, StoreDashboardVo.SalesTimeSeries.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        LocalDateTime startTime;
        boolean isHourly;
        LocalDate today = LocalDate.now();

        switch (period) {
            case "last24h":
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

        Map<String, Map<String, Object>> resultMap = unwrap(
                orderDashboardClient.getSalesTimeSeries(sellerId, startTime, isHourly));

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
                        formatLabel(label, isHourly), salesAmount, orderCount, salesVolume));
            }
        }

        List<StoreDashboardVo.TimeSeriesPoint> dataPoints = new ArrayList<>();

        if (isHourly) {
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

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    @Override
    public List<StoreDashboardVo.ProductRankItem> getProductRanking(Long sellerId, String period) {
        if (period == null || period.isEmpty()) {
            period = "last7Days";
        }
        String key = "dashboard:store:ranking:" + sellerId + ":" + period;

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StoreDashboardVo.ProductRankItem.class));
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        LocalDateTime startTime = getPeriodStartTime(period);
        Map<String, Map<String, Object>> resultMap = unwrap(
                orderDashboardClient.getProductRanking(sellerId, startTime));

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

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

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
            return fullLabel;
        }
    }

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

    /**
     * 从 Feign 调用的 Result 中解包数据
     */
    private <T> T unwrap(com.cyh.mallcommon.utils.Result<T> result) {
        if (result != null && result.getData() != null) {
            return result.getData();
        }
        return null;
    }
}