import request from '@/utils/request'

/**
 * 轮播图对象
 */
export interface BannerItem {
  id: number
  title: string
  imageUrl: string
  linkUrl: string
  sort: number
  status: number
  createdAt: string
  updatedAt: string
}

/**
 * 新增/编辑轮播图响应
 */
export interface BannerSaveResponse {
  id: number
  imageUrl: string
}

// 获取轮播图列表（管理后台）
export function getBannerList(status?: number): Promise<BannerItem[]> {
  return request({
    url: '/banner/list',
    method: 'get',
    params: status !== undefined ? { status } : {}
  })
}

// 新增轮播图
export function addBanner(data: FormData): Promise<BannerSaveResponse> {
  return request({
    url: '/banner/add',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 编辑轮播图
export function updateBanner(data: FormData): Promise<BannerSaveResponse> {
  return request({
    url: '/banner/update',
    method: 'put',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 删除轮播图
export function deleteBanner(id: number): Promise<void> {
  return request({
    url: `/banner/delete/${id}`,
    method: 'delete'
  })
}

// 获取启用的轮播图列表（首页展示）
export function getActiveBanners(): Promise<BannerItem[]> {
  return request({
    url: '/banner/active',
    method: 'get'
  })
}

// 更新轮播图状态
export function updateBannerStatus(id: number, status: number): Promise<void> {
  const url = status === 1 ? `/banner/enable/${id}` : `/banner/disable/${id}`
  return request({
    url,
    method: 'put'
  })
}