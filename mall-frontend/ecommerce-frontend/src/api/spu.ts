import request from '@/utils/request'

// ===== 公开（前台）商品 VO =====
export interface SpuPageVO {
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

// ===== 商家端商品 VO =====
export interface SpuSellerVO extends SpuPageVO {
  sellerId: number
  storeId?: number
  status: number
  createdAt: string
  updatedAt?: string
}

// ===== 管理员端商品 VO =====
export interface SpuAdminVO extends SpuSellerVO {
  isDeleted?: boolean
}

// ===== 公开详情 VO（含卖家信息）=====
export interface SpuDetailVO extends SpuPageVO {
  sellerId?: number
  sellerUsername?: string
  sellerAvatar?: string
}

// ===== 商家端管理详情 VO =====
export interface SpuSellerDetailVO extends SpuSellerVO {
  sellerUsername?: string
  sellerAvatar?: string
  sellerRealName?: string
  sellerPhone?: string
}

// ===== 管理员端管理详情 VO =====
export interface SpuAdminDetailVO extends SpuAdminVO {
  sellerUsername?: string
  sellerAvatar?: string
  sellerRealName?: string
  sellerPhone?: string
}

// ===== 分页结果 =====
export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

// ===== 新增 / 更新 =====

export function addSpu(data: {
  spuDto: string
  imageFiles?: File[]
}): Promise<{
  id: number
  mainImage?: string
  images?: string
}> {
  const formData = new FormData()
  formData.append('spuDto', data.spuDto)
  if (data.imageFiles) {
    data.imageFiles.forEach(file => {
      formData.append('imageFiles', file)
    })
  }
  return request({
    url: '/spu/add',
    method: 'post',
    data: formData
  })
}

export function updateSpu(data: {
  spuDto: string
  imageFiles?: File[]
}): Promise<{
  id: number
  mainImage?: string
  images?: string
}> {
  const formData = new FormData()
  formData.append('spuDto', data.spuDto)
  if (data.imageFiles && data.imageFiles.length > 0) {
    data.imageFiles.forEach(file => {
      formData.append('imageFiles', file)
    })
  }
  return request({
    url: '/spu/update',
    method: 'put',
    data: formData
  })
}

export function deleteSpu(id: number): Promise<void> {
  return request({
    url: `/spu/delete/${id}`,
    method: 'delete'
  })
}

// ===== 公开详情（前台商品详情页）=====
export function getSpuDetail(id: number): Promise<SpuDetailVO> {
  return request({
    url: `/spu/detail/${id}`,
    method: 'get'
  })
}

// ===== 商家端管理详情 =====
export function getSpuManageDetailForSeller(id: number): Promise<SpuSellerDetailVO> {
  return request({
    url: `/spu/manage-detail/seller/${id}`,
    method: 'get'
  })
}

// ===== 管理员端管理详情 =====
export function getSpuManageDetailForAdmin(id: number): Promise<SpuAdminDetailVO> {
  return request({
    url: `/spu/manage-detail/admin/${id}`,
    method: 'get'
  })
}

// ===== 公开分页（首页、商品列表页）=====
export function getSpuList(params: {
  page?: number
  pageSize?: number
  keyword?: string
  categoryId?: number
  brandId?: number
}): Promise<PageResult<SpuPageVO>> {
  return request({
    url: '/spu/page',
    method: 'get',
    params
  })
}

// ===== ES 搜索建议（自动补全）=====
export function suggestSpu(keyword: string, size = 5): Promise<string[]> {
  return request({
    url: '/spu/suggest',
    method: 'get',
    params: { keyword, size }
  })
}

// ===== ES 商品搜索 VO =====
export interface SpuSearchVO {
  id: number
  name: string
  description?: string
  keywords?: string
  categoryId: number
  categoryName?: string
  brandId?: number
  brandName?: string
  storeId?: number
  storeName?: string
  minPrice?: number
  sales: number
  mainImage?: string
  status: number
  createdAt: string
}

// ===== ES 商品搜索（关键词 + 筛选 + 排序 + 分页）=====
export function searchSpu(params: {
  keyword?: string
  categoryId?: number
  brandId?: number
  minPrice?: number
  maxPrice?: number
  sortBy?: 'sales' | 'price' | 'created_at'
  sortOrder?: 'asc' | 'desc'
  page?: number
  pageSize?: number
}): Promise<PageResult<SpuSearchVO>> {
  return request({
    url: '/spu/search',
    method: 'get',
    params
  })
}

// ===== 管理员分页（全平台全部商品）=====
export function getSpuPageAll(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<PageResult<SpuAdminVO>> {
  return request({
    url: '/spu/page-all',
    method: 'get',
    params
  })
}

// ===== 商家分页 =====
export function getSpuPageBySeller(sellerId: number, params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<PageResult<SpuSellerVO>> {
  return request({
    url: `/spu/page-by-seller/${sellerId}`,
    method: 'get',
    params
  })
}

export function updateSpuStatus(id: number, status: number): Promise<void> {
  return request({
    url: `/spu/status/${id}`,
    method: 'put',
    params: { status }
  })
}

export function onShelfSpu(id: number): Promise<void> {
  return request({
    url: `/spu/on-shelf/${id}`,
    method: 'put'
  })
}

export function offShelfSpu(id: number): Promise<void> {
  return request({
    url: `/spu/off-shelf/${id}`,
    method: 'put'
  })
}