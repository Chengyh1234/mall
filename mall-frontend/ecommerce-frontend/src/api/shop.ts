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
  rejectReason?: string | null
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
 * 管理员获取店铺详情
 */
export interface StoreAdminVo extends Store {
  rejectReason: string | null
  updatedAt: string
}

export function getAdminStoreDetail(id: number): Promise<StoreAdminVo> {
  return request({
    url: `/store/admin/detail/${id}`,
    method: 'get'
  })
}

/**
 * 店铺SPU商品接口（对接后端 SpuVO）
 */
export interface StoreSpuItem {
  id: number
  name: string
  categoryId: number
  categoryName?: string
  brandId?: number
  brandName?: string
  description?: string
  mainImage?: string
  images?: string
  unit?: string
  keywords?: string
  sales: number
  minPrice?: number
}

/**
 * 分页查询店铺下SPU列表
 */
export function getStoreSpuPage(storeId: number, params: {
  keyword?: string
  categoryId?: number
  brandId?: number
  minPrice?: number
  maxPrice?: number
  sortBy?: 'sales' | 'price' | 'created_at'
  sortOrder?: 'asc' | 'desc'
  page?: number
  pageSize?: number
}): Promise<PageResult<StoreSpuItem>> {
  return request({
    url: `/spu/by-store/${storeId}/page`,
    method: 'get',
    params
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

/**
 * 开店申请接口
 */

/** 提交开店申请 */
export interface StoreApplyRequest {
  name: string
  description?: string
  phone?: string
  address?: string
}

export function submitStoreApply(data: StoreApplyRequest): Promise<Store> {
  return request({
    url: '/store/apply',
    method: 'post',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    data: new URLSearchParams(
      Object.entries(data).filter(([_, v]) => v !== undefined && v !== null) as [string, string][]
    ).toString()
  })
}

/** 查看开店申请状态 */
export function getStoreApplyStatus(): Promise<Store> {
  return request({
    url: '/store/apply/status',
    method: 'get'
  })
}

/** 重新提交开店申请 */
export interface ReSubmitStoreApplyRequest {
  storeId: number
  name?: string
  description?: string
  phone?: string
  address?: string
}

export function resubmitStoreApply(data: ReSubmitStoreApplyRequest): Promise<void> {
  return request({
    url: '/store/apply/re-submit',
    method: 'post',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    data: new URLSearchParams(
      Object.entries(data).filter(([_, v]) => v !== undefined && v !== null) as [string, string][]
    ).toString()
  })
}

/** 已注销店铺重新申请开店 */
export interface ReopenStoreApplyRequest {
  storeId: number
  name?: string
  description?: string
  phone?: string
  address?: string
}

export function reopenStoreApply(data: ReopenStoreApplyRequest): Promise<void> {
  return request({
    url: '/store/apply/reopen',
    method: 'post',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    data: new URLSearchParams(
      Object.entries(data).filter(([_, v]) => v !== undefined && v !== null) as [string, string][]
    ).toString()
  })
}

/**
 * 管理员 - 分页获取全部店铺列表
 */
export function getAdminStorePage(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
  sellerId?: number
  phone?: string
}): Promise<PageResult<Store>> {
  return request({
    url: '/store/admin/page',
    method: 'get',
    params
  })
}

/**
 * 管理员 - 开店审核接口
 */

/** 分页查询待审核列表 */
export function getPendingApplyPage(params: {
  page?: number
  pageSize?: number
}): Promise<PageResult<Store>> {
  return request({
    url: '/admin/store/apply/pending',
    method: 'get',
    params
  })
}

/** 分页查询全部开店申请（支持按状态筛选：2 审核中 / 3 审核失败） */
export function getAdminApplyPage(params: {
  page?: number
  pageSize?: number
  status?: number
}): Promise<PageResult<StoreAdminVo>> {
  return request({
    url: '/admin/store/apply/pending',
    method: 'get',
    params
  })
}

/** 审核通过 */
export function approveStoreApply(id: number): Promise<void> {
  return request({
    url: `/admin/store/apply/approve/${id}`,
    method: 'put'
  })
}

/** 驳回开店申请 */
export function rejectStoreApply(id: number, rejectReason: string): Promise<void> {
  return request({
    url: `/admin/store/apply/reject/${id}`,
    method: 'put',
    params: { rejectReason }
  })
}

/**
 * 注销店铺
 * 商家注销自己的店铺，需要所有订单为完结状态
 */
export function deactivateStore(storeId: number): Promise<void> {
  return request({
    url: '/store/deactivate',
    method: 'post',
    params: { storeId }
  })
}