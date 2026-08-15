import request from '@/utils/request'

export interface LogisticsCompany {
  id: number
  name: string
  code: string
  status: number
  sort: number
}

export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

// 分页获取物流公司列表
export function getLogisticsPage(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<PageResult<LogisticsCompany>> {
  return request({
    url: '/logistics/page',
    method: 'get',
    params
  })
}