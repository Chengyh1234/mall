import request from '@/utils/request'

export interface Spu {
  id: number
  sellerId: number
  storeId?: number
  name: string
  categoryId: number
  brandId?: number
  description?: string
  mainImage?: string
  images?: string
  unit?: string
  keywords?: string
  sales: number
  status: number
  isDeleted?: boolean
  brandName?: string
  categoryName?: string
  minPrice?: number
  createdAt: string
  updatedAt?: string
}

export interface PageResult<T> {
  list: T[]
  page: number
  pageSize: number
  total: number
}

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

export function getSpuDetail(id: number): Promise<Spu> {
  return request({
    url: `/spu/detail/${id}`,
    method: 'get'
  })
}

export function getSpuList(params: {
  page?: number
  pageSize?: number
  keyword?: string
  categoryId?: number
  brandId?: number
  status?: number
}): Promise<PageResult<Spu>> {
  return request({
    url: '/spu/page',
    method: 'get',
    params
  })
}

// 运营管理员分页获取全平台全部商品（含上架/下架，含分类名/品牌名）
export function getSpuPageAll(params: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<PageResult<Spu>> {
  return request({
    url: '/spu/page-all',
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

export function getSpuPageBySeller(sellerId: number, params: {
  page?: number
  pageSize?: number
  keyword?: string
  categoryId?: number
  brandId?: number
  status?: number
}): Promise<PageResult<Spu>> {
  return request({
    url: `/spu/page-by-seller/${sellerId}`,
    method: 'get',
    params
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
