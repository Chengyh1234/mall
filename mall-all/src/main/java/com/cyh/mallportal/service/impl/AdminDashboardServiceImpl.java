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
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    /**
     * 获取平台销售时间序列数据，聚合展示销售额、订单量、销量三个指标
     * 根据 period 参数自动切换按小时/按日分组，缺失时段自动补 0
     *
     * @param period 时间段：last24h | last7Days | thisMonth | last90Days | thisYear
     * @return 时间序列数据
     */
    @Override
    public AdminDashboardVo.SalesTimeSeries getSalesTimeSeries(String period) {
        if (period == null || period.isEmpty()) {
            period = "last7Days";
        }
        String key = "dashboard:admin:timeseries:" + period;

        // Step1: 尝试从缓存读取
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, AdminDashboardVo.SalesTimeSeries.class);
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

        // Step3: 查询数据库（不区分商家，全平台统计）
        Map<String, Map<String, Object>> resultMap = orderMapper.selectAdminSalesTimeSeries(startTime, isHourly);

        // Step4: 将 DB 结果转换为 label → 数据点的映射，便于补零
        Map<String, AdminDashboardVo.TimeSeriesPoint> dataMap = new LinkedHashMap<>();
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

                dataMap.put(label, new AdminDashboardVo.TimeSeriesPoint(
                        formatLabel(label, isHourly),
                        salesAmount, orderCount, salesVolume));
            }
        }

        // Step5: 生成完整时间序列，缺失时段补 0
        List<AdminDashboardVo.TimeSeriesPoint> dataPoints = new ArrayList<>();

        if (isHourly) {
            // 最近24小时，逐个填充小时，缺失时段补 0
            LocalDateTime hourCursor = startTime;
            for (int i = 0; i < 24; i++) {
                String dbKey = hourCursor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String displayLabel = hourCursor.format(DateTimeFormatter.ofPattern("HH:mm"));
                AdminDashboardVo.TimeSeriesPoint point = dataMap.containsKey(dbKey)
                        ? dataMap.get(dbKey)
                        : new AdminDashboardVo.TimeSeriesPoint(displayLabel, BigDecimal.ZERO, 0, 0);
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
                AdminDashboardVo.TimeSeriesPoint point = dataMap.containsKey(dbKey)
                        ? dataMap.get(dbKey)
                        : new AdminDashboardVo.TimeSeriesPoint(displayLabel, BigDecimal.ZERO, 0, 0);
                dataPoints.add(point);
                dateCursor = dateCursor.plusDays(1);
            }
        }

        AdminDashboardVo.SalesTimeSeries result = new AdminDashboardVo.SalesTimeSeries(period, dataPoints);

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
     * @return 展示格式标签（HH:mm 或 MM-dd）
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
            return fullLabel;
        }
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