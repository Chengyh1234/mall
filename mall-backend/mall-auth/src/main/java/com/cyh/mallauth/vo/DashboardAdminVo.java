package com.cyh.mallauth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员仪表盘视图对象
 * 聚合平台级运营概览、销售统计数据，用于超级管理员后台首页展示
 */
@Data
public class DashboardAdminVo {

    /**
     * 平台运营概览
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardOverview {
        private Long totalUsers;
        private Long todayOrders;
        private Long totalProducts;
        private BigDecimal todaySales;
        private Long settledSellers;
    }

    /**
     * 销售时间序列
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTimeSeries {
        private String period;
        private List<TimeSeriesPoint> dataPoints;
    }

    /**
     * 时间序列数据点
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        private String label;
        private BigDecimal salesAmount;
        private Integer orderCount;
        private Integer salesVolume;
    }

    /**
     * 平台销售额统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesStatistics {
        private BigDecimal today;
        private BigDecimal last7Days;
        private BigDecimal thisMonth;
        private BigDecimal thisYear;
    }
}