package com.cyh.mallportal.service;

import com.cyh.mallportal.vo.AdminDashboardVo;

/**
 * 管理员仪表盘服务接口
 * 定义平台级运营数据的统计与查询方法，用于超级管理员后台首页展示
 */
public interface AdminDashboardService {

    /**
     * 获取平台销售时间序列数据
     * 按时间维度聚合展示全平台销售额、订单量、销量三个指标
     *
     * @param period 时间段：last24h | last7Days | thisMonth | last90Days | thisYear
     * @return 时间序列数据（含标签、销售额、订单量、销量）
     */
    AdminDashboardVo.SalesTimeSeries getSalesTimeSeries(String period);

    /**
     * 获取平台运营概览
     * 返回总用户数、今日订单数、商品总数、今日销售额、入驻卖家数
     *
     * @return 平台运营概览数据
     */
    AdminDashboardVo.DashboardOverview getDashboardOverview();

    /**
     * 获取平台销售额统计
     * 返回今日、近7日、本月、本年四个时间维度的销售额
     *
     * @return 销售额统计
     */
    AdminDashboardVo.SalesStatistics getSalesStatistics();
}