import request from '@/utils/request'

/**
 * 品牌接口
 */
export interface Brand {
  id: number
  name: string
  logo?: string
  description?: string
  sort?: number
  status?: number
  createdAt?: string
}

/**
 * 分页响应
 */
export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

// 获取品牌列表
export function getBrandList(params?: {
  status?: number
}): Promise<Brand[]> {
  return request({
    url: '/brand/list',
    method: 'get',
    params
  })
}

// 分页获取品牌列表
export function getBrandPage(params?: {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}): Promise<PageResult<Brand>> {
  return request({
    url: '/brand/page',
    method: 'get',
    params
  })
}

// 搜索品牌
export function searchBrand(keyword: string): Promise<Brand[]> {
  return request({
    url: '/brand/search',
    method: 'get',
    params: { keyword }
  })
}

// 获取品牌详情
export function getBrandDetail(id: number): Promise<Brand> {
  return request({
    url: `/brand/detail/${id}`,
    method: 'get'
  })
}

// 根据状态获取品牌
export function getBrandByStatus(status: number): Promise<Brand[]> {
  return request({
    url: `/brand/status/${status}`,
    method: 'get'
  })
}

// 新增品牌
export function addBrand(data: {
  name: string
  logo?: string
  description?: string
  sort?: number
  status?: number
}): Promise<{ id: number }> {
  return request({
    url: '/brand/add',
    method: 'post',
    data
  })
}

// 更新品牌
export function updateBrand(data: {
  id: number
  name?: string
  logo?: string
  description?: string
  sort?: number
  status?: number
}): Promise<void> {
  return request({
    url: '/brand/update',
    method: 'put',
    data
  })
}

// 删除品牌
export function deleteBrand(id: number): Promise<void> {
  return request({
    url: `/brand/delete/${id}`,
    method: 'delete'
  })
}