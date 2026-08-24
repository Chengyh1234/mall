package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员仪表盘数据传输对象（Feign 内部通信）
 * 用于 mall-auth 通过 Feign 调用各服务获取平台级运营数据
 */
@Data
public class AdminDashboardDataDto {

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
     * 销售额统计
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
     * 时间序列
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesData {
        private String period;
        private List<TimeSeriesPoint> dataPoints;
    }
}