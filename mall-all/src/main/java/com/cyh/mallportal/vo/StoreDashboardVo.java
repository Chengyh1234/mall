package com.cyh.mallportal.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商家仪表盘视图对象
 * 聚合店铺信息、经营概览、订单统计、商品统计、销售数据等多板块数据
 * 各内部类对应仪表盘上的一个功能卡片或图表
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
        /** 店铺ID */
        private Long id;
        /** 店铺名称 */
        private String name;
        /** 店铺Logo路径 */
        private String logo;
        /** 店铺状态：1-正常 0-禁用 2-审核中 3-审核失败 */
        private Integer status;
    }

    /**
     * 经营概览
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessOverview {
        /** 今日订单数 */
        private Integer todayOrders;
        /** 今日销售额（已完成订单实付金额） */
        private BigDecimal todaySales;
        /** 累计订单总数 */
        private Integer totalOrders;
        /** 累计总销售额（已完成订单实付金额） */
        private BigDecimal totalSales;
    }

    /**
     * 订单分类统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStats {
        /** 待付款订单数（status=1） */
        private Integer pendingPayment;
        /** 待发货订单数（status=2） */
        private Integer pendingDelivery;
        /** 待收货订单数（status=3） */
        private Integer pendingReceive;
        /** 已完成订单数（status=4） */
        private Integer completed;
        /** 已取消订单数（status=5） */
        private Integer cancelled;
    }

    /**
     * 商品上下架统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductStats {
        /** 商品总数 */
        private Integer totalProducts;
        /** 上架商品数（status=1） */
        private Integer onlineProducts;
        /** 下架商品数（status=0） */
        private Integer offlineProducts;
    }

    /**
     * 近期订单简要项
     * 用于仪表盘"最近订单"列表展示
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentOrderItem {
        /** 订单ID */
        private Long id;
        /** 订单号 */
        private String orderNo;
        /** 实付金额 */
        private BigDecimal payAmount;
        /** 订单状态码（1-待付款 2-待发货 3-待收货 4-已完成 5-已取消） */
        private Integer status;
        /** 订单状态中文描述 */
        private String statusDesc;
        /** 收货人姓名 */
        private String receiverName;
        /** 下单时间（格式：yyyy-MM-dd HH:mm:ss） */
        private String createdAt;
    }

    /**
     * KPI 总览
     * 用于展示今日、近7天、本月、本年四个时间维度的销售额和利润
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiOverview {
        /** 今日销售额（元） */
        private BigDecimal today;
        /** 近7天销售额（元） */
        private BigDecimal last7Days;
        /** 本月销售额（元） */
        private BigDecimal thisMonth;
        /** 本年销售额（元） */
        private BigDecimal thisYear;
        /** 今日利润（元），利润 = 销售额 - 订单商品总成本 */
        private BigDecimal todayProfit;
        /** 近7天利润（元） */
        private BigDecimal last7DaysProfit;
        /** 本月利润（元） */
        private BigDecimal thisMonthProfit;
        /** 本年利润（元） */
        private BigDecimal thisYearProfit;
    }

    /**
     * 销售趋势
     * 用于折线图展示，返回最近7天每日销售额
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTrend {
        /** 日期数组（格式 YYYY-MM-DD），固定7天 */
        private List<String> dates;
        /** 对应日期的销售额数组，与 dates 一一对应 */
        private List<BigDecimal> values;
    }

    /**
     * 销售时间序列
     * 用于折线图展示不同时间维度的销售额、订单量、销量趋势
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesTimeSeries {
        /** 时间段标识：last24h | last7Days | thisMonth | last90Days | thisYear */
        private String period;
        /** 时间序列数据点列表，按时间升序排列 */
        private List<TimeSeriesPoint> dataPoints;
    }

    /**
     * 时间序列数据点
     * 表示某个时间单位的销售汇总数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSeriesPoint {
        /** 时间标签：最近24小时为 HH:00，其余为 MM-DD */
        private String label;
        /** 该时段销售额（元），无数据为 0 */
        private BigDecimal salesAmount;
        /** 该时段订单量（单），无数据为 0 */
        private Integer orderCount;
        /** 该时段销量（件），无数据为 0 */
        private Integer salesVolume;
    }

    /**
     * 商品销售排行项
     * 用于条形图和南丁格尔玫瑰图展示商品销售数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRankItem {
        /** 商品名称 */
        private String name;
        /** 总销售额（元） */
        private BigDecimal salesAmount;
        /** 总销售数量（件） */
        private Integer salesCount;
        /** 该商品销售额占该时间段总销售额的百分比（如 27.1 代表 27.1%） */
        private BigDecimal percentOfTotal;
        /** 该商品销售数量占该时间段总销售数量的百分比（如 15.3 代表 15.3%） */
        private BigDecimal countPercentOfTotal;
    }
}