package com.cyh.mallcommon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家仪表盘数据传输对象（Feign 内部通信）
 * 用于 mall-store 通过 Feign 调用 mall-order 获取店铺经营数据
 */
@Data
public class StoreDashboardDataDto {

    /**
     * 商家 KPI 总览（销售额 + 利润）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiData {
        private BigDecimal todaySales;
        private BigDecimal last7DaysSales;
        private BigDecimal thisMonthSales;
        private BigDecimal thisYearSales;
        private BigDecimal todayProfit;
        private BigDecimal last7DaysProfit;
        private BigDecimal thisMonthProfit;
        private BigDecimal thisYearProfit;
    }

    /**
     * 销售趋势
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTrendData {
        private List<String> dates;
        private List<BigDecimal> values;
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

    /**
     * 商品排行项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRankItem {
        private String name;
        private BigDecimal salesAmount;
        private Integer salesCount;
    }
}