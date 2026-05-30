package com.cyh.mallportal.service;

import com.cyh.mallportal.vo.StoreDashboardVo;

import java.util.List;

/**
 * 商家仪表盘服务接口
 * 定义商家店铺经营数据的统计与查询方法，包含销售数据等维度
 */
public interface StoreDashboardService {

    /**
     * 获取商家销售 KPI 总览
     * 返回今日、近7天、本月、本年四个时间维度的销售总额
     *
     * @param sellerId 商家用户ID
     * @return KPI 总览数据
     */
    StoreDashboardVo.KpiOverview getSalesKpiOverview(Long sellerId);

    /**
     * 获取商家销售趋势数据
     * 返回最近7天每天的销售额，用于折线图展示
     *
     * @param sellerId 商家用户ID
     * @return 销售趋势数据（日期数组 + 销售额数组）
     */
    StoreDashboardVo.SalesTrend getSalesTrend(Long sellerId);

    /**
     * 获取商品销售排行数据
     * 按销售额降序排列，支持时间段参数
     *
     * @param sellerId 商家用户ID
     * @param period   时间段：today | last7Days | thisMonth | thisYear，默认 last7Days
     * @return 商品销售排行列表
     */
    List<StoreDashboardVo.ProductRankItem> getProductRanking(Long sellerId, String period);
}