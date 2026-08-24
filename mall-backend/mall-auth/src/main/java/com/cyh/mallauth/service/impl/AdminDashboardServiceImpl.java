package com.cyh.mallauth.service.impl;

import com.cyh.mallauth.feign.OrderAdminClient;
import com.cyh.mallauth.feign.ProductAdminCountClient;
import com.cyh.mallauth.feign.StoreAdminCountClient;
import com.cyh.mallauth.feign.UserAdminCountClient;
import com.cyh.mallauth.service.AdminDashboardService;
import com.cyh.mallauth.vo.DashboardAdminVo;
import com.cyh.mallcommon.constant.RedisConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 管理员仪表盘服务实现类
 * 通过 Feign 调用各服务获取平台级运营数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserAdminCountClient userAdminCountClient;
    private final OrderAdminClient orderAdminClient;
    private final ProductAdminCountClient productAdminCountClient;
    private final StoreAdminCountClient storeAdminCountClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public DashboardAdminVo.DashboardOverview getDashboardOverview() {
        String key = "dashboard:admin:overview";

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, DashboardAdminVo.DashboardOverview.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        Long totalUsers = unwrap(userAdminCountClient.getUserCount());
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Long todayOrders = unwrap(orderAdminClient.getTodayOrders(todayStart));
        Long totalProducts = unwrap(productAdminCountClient.getProductCount());
        BigDecimal todaySales = unwrap(orderAdminClient.getAdminCompletedSales(todayStart));
        if (todaySales == null) {
            todaySales = BigDecimal.ZERO;
        }
        Long settledSellers = unwrap(storeAdminCountClient.getSettledSellerCount());

        DashboardAdminVo.DashboardOverview result = new DashboardAdminVo.DashboardOverview(
                totalUsers != null ? totalUsers : 0L,
                todayOrders != null ? todayOrders : 0L,
                totalProducts != null ? totalProducts : 0L,
                todaySales,
                settledSellers != null ? settledSellers : 0L
        );

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    @Override
    public DashboardAdminVo.SalesTimeSeries getSalesTimeSeries(String period) {
        if (period == null || period.isEmpty()) {
            period = "last7Days";
        }
        String key = "dashboard:admin:timeseries:" + period;

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, DashboardAdminVo.SalesTimeSeries.class);
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
                orderAdminClient.getAdminSalesTimeSeries(startTime, isHourly));

        Map<String, DashboardAdminVo.TimeSeriesPoint> dataMap = new LinkedHashMap<>();
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

                dataMap.put(label, new DashboardAdminVo.TimeSeriesPoint(
                        formatLabel(label, isHourly), salesAmount, orderCount, salesVolume));
            }
        }

        List<DashboardAdminVo.TimeSeriesPoint> dataPoints = new ArrayList<>();

        if (isHourly) {
            LocalDateTime hourCursor = startTime;
            for (int i = 0; i < 24; i++) {
                String dbKey = hourCursor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String displayLabel = hourCursor.format(DateTimeFormatter.ofPattern("HH:mm"));
                DashboardAdminVo.TimeSeriesPoint point = dataMap.containsKey(dbKey)
                        ? dataMap.get(dbKey)
                        : new DashboardAdminVo.TimeSeriesPoint(displayLabel, BigDecimal.ZERO, 0, 0);
                dataPoints.add(point);
                hourCursor = hourCursor.plusHours(1);
            }
        } else {
            LocalDate dateCursor = startTime.toLocalDate();
            LocalDate endDate = today;
            while (!dateCursor.isAfter(endDate)) {
                String dbKey = dateCursor.toString();
                String displayLabel = dateCursor.format(DateTimeFormatter.ofPattern("MM-dd"));
                DashboardAdminVo.TimeSeriesPoint point = dataMap.containsKey(dbKey)
                        ? dataMap.get(dbKey)
                        : new DashboardAdminVo.TimeSeriesPoint(displayLabel, BigDecimal.ZERO, 0, 0);
                dataPoints.add(point);
                dateCursor = dateCursor.plusDays(1);
            }
        }

        DashboardAdminVo.SalesTimeSeries result = new DashboardAdminVo.SalesTimeSeries(period, dataPoints);

        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result),
                    RedisConstants.DASHBOARD_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.error("序列化缓存数据失败, key: {}", key, e);
        }

        return result;
    }

    @Override
    public DashboardAdminVo.SalesStatistics getSalesStatistics() {
        String key = "dashboard:admin:sales-stats";

        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, DashboardAdminVo.SalesStatistics.class);
            } catch (JsonProcessingException e) {
                stringRedisTemplate.delete(key);
            }
        }

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(today.withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime yearStart = LocalDateTime.of(today.withDayOfYear(1), LocalTime.MIN);

        BigDecimal todaySales = unwrap(orderAdminClient.getAdminCompletedSales(todayStart));
        BigDecimal last7DaysSales = unwrap(orderAdminClient.getAdminCompletedSales(weekStart));
        BigDecimal thisMonthSales = unwrap(orderAdminClient.getAdminCompletedSales(monthStart));
        BigDecimal thisYearSales = unwrap(orderAdminClient.getAdminCompletedSales(yearStart));

        DashboardAdminVo.SalesStatistics result = new DashboardAdminVo.SalesStatistics(
                todaySales != null ? todaySales : BigDecimal.ZERO,
                last7DaysSales != null ? last7DaysSales : BigDecimal.ZERO,
                thisMonthSales != null ? thisMonthSales : BigDecimal.ZERO,
                thisYearSales != null ? thisYearSales : BigDecimal.ZERO
        );

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

    private <T> T unwrap(com.cyh.mallcommon.utils.Result<T> result) {
        if (result != null && result.getData() != null) {
            return result.getData();
        }
        return null;
    }
}