package com.cyh.mallauth.controller;

import com.cyh.mallauth.service.AdminDashboardService;
import com.cyh.mallauth.vo.DashboardAdminVo;
import com.cyh.mallcommon.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员仪表盘控制器
 * 为超级管理员提供平台级运营数据的可视化展示接口
 */
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 获取平台运营概览
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DashboardAdminVo.DashboardOverview> getDashboardOverview() {
        return Result.success(adminDashboardService.getDashboardOverview());
    }

    /**
     * 获取平台销售时间序列数据
     */
    @GetMapping("/sales/timeseries")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DashboardAdminVo.SalesTimeSeries> getSalesTimeSeries(
            @RequestParam(defaultValue = "last7Days") String period) {
        return Result.success(adminDashboardService.getSalesTimeSeries(period));
    }

    /**
     * 获取平台销售额统计
     */
    @GetMapping("/sales")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public Result<DashboardAdminVo.SalesStatistics> getSalesStatistics() {
        return Result.success(adminDashboardService.getSalesStatistics());
    }
}