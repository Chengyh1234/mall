package com.cyh.mallstore.service;

import com.cyh.mallstore.vo.StoreDashboardVo;

import java.util.List;

/**
 * 商家仪表盘服务接口
 * 定义商家店铺经营数据的统计与查询方法
 */
public interface StoreDashboardService {

    /**
     * 获取商家销售 KPI 总览
     */
    StoreDashboardVo.KpiOverview getSalesKpiOverview(Long sellerId);

    /**
     * 获取商家销售趋势数据
     */
    StoreDashboardVo.SalesTrend getSalesTrend(Long sellerId);

    /**
     * 获取销售时间序列数据
     */
    StoreDashboardVo.SalesTimeSeries getSalesTimeSeries(Long sellerId, String period);

    /**
     * 获取商品销售排行数据
     */
    List<StoreDashboardVo.ProductRankItem> getProductRanking(Long sellerId, String period);
}