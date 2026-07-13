package com.cyh.mallportal.vo;

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
     * 用于展示总用户数、今日订单、商品总数、今日销售额、入驻卖家数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardOverview {
        /** 总用户数 */
        private Long totalUsers;
        /** 今日订单数 */
        private Long todayOrders;
        /** 商品总数（未删除的 SPU） */
        private Long totalProducts;
        /** 今日销售额（已完成订单实付金额） */
        private BigDecimal todaySales;
        /** 入驻卖家数（状态为正常的店铺数） */
        private Long settledSellers;
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
        /** 时间标签：最近24小时为 HH:mm，其余为 MM-dd */
        private String label;
        /** 该时段销售额（元），无数据为 0 */
        private BigDecimal salesAmount;
        /** 该时段订单量（单），无数据为 0 */
        private Integer orderCount;
        /** 该时段销量（件），无数据为 0 */
        private Integer salesVolume;
    }

    /**
     * 平台销售额统计
     * 用于展示今日、近7日、本月、本年四个时间维度的销售额
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesStatistics {
        /** 今日销售额（元） */
        private BigDecimal today;
        /** 近7日销售额（元） */
        private BigDecimal last7Days;
        /** 本月销售额（元） */
        private BigDecimal thisMonth;
        /** 本年销售额（元） */
        private BigDecimal thisYear;
    }
}