package com.cyh.mallstore.controller;

import com.cyh.mallcommon.utils.Result;
import com.cyh.mallstore.service.StoreDashboardService;
import com.cyh.mallstore.service.StoreService;
import com.cyh.mallstore.vo.StoreDashboardVo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商家仪表盘控制器
 * 为商家用户提供店铺经营数据的可视化展示接口
 * 包含销售 KPI、趋势、商品排行等功能
 */
@RestController
@RequestMapping("/store-admin/dashboard")
@RequiredArgsConstructor
public class StoreDashboardController {

    private final StoreDashboardService storeDashboardService;
    private final StoreService storeService;

    /**
     * 获取销售 KPI 总览
     */
    @GetMapping("/sales/kpi")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreDashboardVo.KpiOverview> getSalesKpiOverview() {
        Long currentUserId = getCurrentUserId();

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
     */
    @GetMapping("/sales/trend")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreDashboardVo.SalesTrend> getSalesTrend() {
        Long currentUserId = getCurrentUserId();

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
     * 获取销售时间序列数据
     */
    @GetMapping("/sales/timeseries")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<StoreDashboardVo.SalesTimeSeries> getSalesTimeSeries(
            @RequestParam(defaultValue = "last7Days") String period) {
        Long currentUserId = getCurrentUserId();

        Long storeId = storeService.getBySellerId(currentUserId) != null
                ? storeService.getBySellerId(currentUserId).getId() : null;
        if (storeId == null) {
            return Result.error("您还没有店铺");
        }

        if (!storeService.hasStorePermission(storeId, currentUserId)) {
            return Result.error("无权操作此店铺");
        }

        StoreDashboardVo.SalesTimeSeries data = storeDashboardService.getSalesTimeSeries(currentUserId, period);
        return Result.success(data);
    }

    /**
     * 获取商品销售排行
     */
    @GetMapping("/sales/product-ranking")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SELLER')")
    public Result<List<StoreDashboardVo.ProductRankItem>> getProductRanking(
            @RequestParam(defaultValue = "last7Days") String period) {
        Long currentUserId = getCurrentUserId();

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
     * 从 SecurityContext 获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return Long.valueOf(authentication.getPrincipal().toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}