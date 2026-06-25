import request from '@/utils/request'

export interface KpiOverview {
  today: number
  last7Days: number
  thisMonth: number
  thisYear: number
  todayProfit: number
  last7DaysProfit: number
  thisMonthProfit: number
  thisYearProfit: number
}

export interface SalesTrend {
  dates: string[]
  values: number[]
}

export interface ProductRankItem {
  name: string
  salesAmount: number
  salesCount: number
  countPercentOfTotal: number
  percentOfTotal: number
}

/* ========== 商家仪表盘 (StoreDashboard) ========== */

export function getSalesKpiOverview(): Promise<KpiOverview> {
  return request({
    url: '/store-admin/dashboard/sales/kpi',
    method: 'get'
  })
}

export function getSalesTrend(): Promise<SalesTrend> {
  return request({
    url: '/store-admin/dashboard/sales/trend',
    method: 'get'
  })
}

export function getProductRanking(period: string = 'last7Days'): Promise<ProductRankItem[]> {
  return request({
    url: '/store-admin/dashboard/sales/product-ranking',
    method: 'get',
    params: { period }
  })
}

/* ========== 管理员仪表盘 (AdminDashboard) ========== */

/** 平台运营概览：总用户数、今日订单数、商品总数、今日销售额、入驻卖家数 */
export interface DashboardOverview {
  totalUsers: number
  todayOrders: number
  totalProducts: number
  todaySales: number
  settledSellers: number
}

/** 平台销售额统计：今日、近7日、本月、本年 */
export interface SalesStatistics {
  today: number
  last7Days: number
  thisMonth: number
  thisYear: number
}

/** 获取平台运营概览 */
export function getAdminDashboardOverview(): Promise<DashboardOverview> {
  return request({
    url: '/admin/dashboard/overview',
    method: 'get'
  })
}

/** 获取平台销售额统计 */
export function getAdminSalesStatistics(): Promise<SalesStatistics> {
  return request({
    url: '/admin/dashboard/sales',
    method: 'get'
  })
}