import request from '@/utils/request'

/**
 * 店铺信息接口
 */
export interface Store {
  id: number
  name: string
  sellerId: number
  logo: string
  banner: string
  description: string
  phone: string
  address: string
  businessLicense: string
  status: number
  sort: number
  createdAt: string
}

/**
 * 分页结果
 */
export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

/**
 * 获取当前用户的店铺
 */
export function getMyStore(): Promise<Store> {
  return request({
    url: '/store/my-store',
    method: 'get'
  })
}

/**
 * 分页获取店铺列表
 */
export function getStorePage(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<PageResult<Store>> {
  return request({
    url: '/store/page',
    method: 'get',
    params
  })
}

/**
 * 更新店铺信息
 */
export interface UpdateStoreRequest {
  id: number
  name?: string
  description?: string
  phone?: string
  address?: string
  businessLicense?: string
  sort?: number
}

export function updateStore(data: UpdateStoreRequest, logoFile?: File, bannerFile?: File): Promise<{ id: number; logo: string; banner: string }> {
  const formData = new FormData()
  formData.append('storeDto', JSON.stringify(data))
  
  if (logoFile) {
    formData.append('logoFile', logoFile)
  }
  
  if (bannerFile) {
    formData.append('bannerFile', bannerFile)
  }
  
  return request({
    url: '/store/update',
    method: 'put',
    data: formData
  })
}

/**
 * 获取店铺详情
 */
export function getStoreDetail(id: number): Promise<Store> {
  return request({
    url: `/store/detail/${id}`,
    method: 'get'
  })
}

/**
 * 更新店铺状态
 */
export function updateStoreStatus(id: number, status: number): Promise<void> {
  return request({
    url: `/store/status/${id}`,
    method: 'put',
    params: { status }
  })
}