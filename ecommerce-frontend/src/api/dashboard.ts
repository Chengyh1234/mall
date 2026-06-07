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