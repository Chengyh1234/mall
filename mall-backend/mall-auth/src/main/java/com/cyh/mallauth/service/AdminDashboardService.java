package com.cyh.mallauth.service;

import com.cyh.mallauth.vo.DashboardAdminVo;

/**
 * 管理员仪表盘服务接口
 * 定义平台级运营数据的统计与查询方法
 */
public interface AdminDashboardService {

    /**
     * 获取平台销售时间序列数据
     */
    DashboardAdminVo.SalesTimeSeries getSalesTimeSeries(String period);

    /**
     * 获取平台运营概览
     */
    DashboardAdminVo.DashboardOverview getDashboardOverview();

    /**
     * 获取平台销售额统计
     */
    DashboardAdminVo.SalesStatistics getSalesStatistics();
}