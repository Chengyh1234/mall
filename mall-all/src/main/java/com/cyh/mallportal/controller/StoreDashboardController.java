package com.cyh.mallportal.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallportal.entity.User;
import com.cyh.mallportal.service.StoreDashboardService;
import com.cyh.mallportal.service.StoreService;
import com.cyh.mallportal.vo.StoreDashboardVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商家仪表盘控制器
 * 为商家用户提供店铺经营数据的可视化展示接口
 * 包含销售 KPI、趋势、商品排行等功能
 */
@RestController
@RequestMapping("/store-admin/dashboard")
public class StoreDashboardController {

    @Autowired
    private StoreDashboardService storeDashboardService;

    @Autowired
    private StoreService storeService;

    /**
     * 获取销售 KPI 总览
     * 返回今日、近7天、本月、本年四个时间维度的销售额和利润，用于仪表盘核心指标卡片
     *
     * @return KPI 总览数据
     */
    @GetMapping("/sales/kpi")
    @PreAuthorize("hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreDashboardVo.KpiOverview> getSalesKpiOverview() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        Long storeId = storeService.getBySellerId(currentUserId) != null
                ? storeService.getBySellerId(currentUserId).getId() : null;
        if (storeId == null) {
            return Result.error("您还没有店铺");
        }

        if (!storeService.hasStorePermission(storeId, currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        StoreDashboardVo.KpiOverview data = storeDashboardService.getSalesKpiOverview(currentUserId);
        return Result.success(data);
    }

    /**
     * 获取销售趋势数据（折线图）
     * 返回最近7天每天的销售额，dates 和 values 一一对应，无销售数据的日期值为 0
     *
     * @return 销售趋势数据
     */
    @GetMapping("/sales/trend")
    @PreAuthorize("hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreDashboardVo.SalesTrend> getSalesTrend() {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        Long storeId = storeService.getBySellerId(currentUserId) != null
                ? storeService.getBySellerId(currentUserId).getId() : null;
        if (storeId == null) {
            return Result.error("您还没有店铺");
        }

        if (!storeService.hasStorePermission(storeId, currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        StoreDashboardVo.SalesTrend data = storeDashboardService.getSalesTrend(currentUserId);
        return Result.success(data);
    }

    /**
     * 获取商品销售排行
     * 按销售额降序排列，支持时间段切换，可用于条形图和南丁格尔玫瑰图
     *
     * @param period 时间段：today | last7Days | thisMonth | thisYear，默认 last7Days
     * @return 商品销售排行列表
     */
    @GetMapping("/sales/product-ranking")
    @PreAuthorize("hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<List<StoreDashboardVo.ProductRankItem>> getProductRanking(
            @RequestParam(defaultValue = "last7Days") String period) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        Long storeId = storeService.getBySellerId(currentUserId) != null
                ? storeService.getBySellerId(currentUserId).getId() : null;
        if (storeId == null) {
            return Result.error("您还没有店铺");
        }

        if (!storeService.hasStorePermission(storeId, currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        List<StoreDashboardVo.ProductRankItem> data = storeDashboardService.getProductRanking(currentUserId, period);
        return Result.success(data);
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }
}