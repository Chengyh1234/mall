package com.cyh.mallstore.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家仪表盘视图对象
 * 聚合店铺经营概览、销售 KPI、趋势、时间序列、商品排行等数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreDashboardVo {

    /** 店铺基本信息 */
    private StoreInfo storeInfo;
    /** 经营概览（今日/总计订单与销售额） */
    private BusinessOverview businessOverview;
    /** 各状态订单分类统计 */
    private OrderStats orderStats;
    /** 商品上下架统计 */
    private ProductStats productStats;

    /**
     * 店铺基本信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreInfo {
        private Long id;
        private String name;
        private String logo;
        private Integer status;
    }

    /**
     * 经营概览
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessOverview {
        private Integer todayOrders;
        private BigDecimal todaySales;
        private Integer totalOrders;
        private BigDecimal totalSales;
    }

    /**
     * 订单分类统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStats {
        private Integer pendingPayment;
        private Integer pendingDelivery;
        private Integer pendingReceive;
        private Integer completed;
        private Integer cancelled;
    }

    /**
     * 商品上下架统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStats {
        private Integer totalProducts;
        private Integer onlineProducts;
        private Integer offlineProducts;
    }

    /**
     * 近期订单简要项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderItem {
        private Long id;
        private String orderNo;
        private BigDecimal payAmount;
        private Integer status;
        private String statusDesc;
        private String receiverName;
        private String createdAt;
    }

    /**
     * KPI 总览
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiOverview {
        private BigDecimal today;
        private BigDecimal last7Days;
        private BigDecimal thisMonth;
        private BigDecimal thisYear;
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
    public static class SalesTrend {
        private List<String> dates;
        private List<BigDecimal> values;
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
     * 商品销售排行项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRankItem {
        private String name;
        private BigDecimal salesAmount;
        private Integer salesCount;
        private BigDecimal percentOfTotal;
        private BigDecimal countPercentOfTotal;
    }
}