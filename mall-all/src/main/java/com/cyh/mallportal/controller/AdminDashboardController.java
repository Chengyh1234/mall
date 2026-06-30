package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.service.AdminDashboardService;
import com.cyh.mallportal.vo.AdminDashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员仪表盘控制器
 * 为超级管理员提供平台级运营数据的可视化展示接口
 * 包含平台概览、销售额统计等功能
 */
@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    /**
     * 获取平台运营概览
     * 返回总用户数、今日订单数、商品总数、今日销售额、入驻卖家数
     * 用于超级管理员后台首页核心指标卡片展示
     *
     * @return 平台运营概览数据
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<AdminDashboardVo.DashboardOverview> getDashboardOverview() {
        AdminDashboardVo.DashboardOverview data = adminDashboardService.getDashboardOverview();
        return Result.success(data);
    }

    /**
     * 获取平台销售时间序列数据（折线图）只统计已完成的订单，即status=4
     * 统一接口，通过 period 参数切换不同时间维度
     * 返回各时段/每日的全平台销售额、订单量、销量三个指标
     *
     * @param period 时间段：last24h（最近24小时按小时）| last7Days | thisMonth | last90Days | thisYear，默认 last7Days
     * @return 时间序列数据
     */
    @GetMapping("/sales/timeseries")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<AdminDashboardVo.SalesTimeSeries> getSalesTimeSeries(
            @RequestParam(defaultValue = "last7Days") String period) {
        AdminDashboardVo.SalesTimeSeries data = adminDashboardService.getSalesTimeSeries(period);
        return Result.success(data);
    }

    /**
     * 获取平台销售额统计
     * 返回今日、近7日、本月、本年四个时间维度的销售额
     * 用于超级管理员后台首页销售额趋势卡片展示
     *
     * @return 销售额统计
     */
    @GetMapping("/sales")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<AdminDashboardVo.SalesStatistics> getSalesStatistics() {
        AdminDashboardVo.SalesStatistics data = adminDashboardService.getSalesStatistics();
        return Result.success(data);
    }
}