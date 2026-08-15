import request from '@/utils/request'

// SPU相关接口

export interface ProductImage {
  url: string
}

export interface SpuForm {
  id?: number
  name: string
  categoryId: number
  brandId: number
  price: number
  description?: string
  unit?: string
  keywords?: string
  mainImage?: string
  images?: ProductImage[]
}

// 创建SPU
export function createSpu(data: SpuForm) {
  return request({
    url: '/spu/add',
    method: 'post',
    data
  })
}

// 更新SPU
export function updateSpu(data: {
  spuDto: string
  imageFiles?: File[]
}) {
  const formData = new FormData()
  formData.append('spuDto', data.spuDto)
  if (data.imageFiles) {
    data.imageFiles.forEach(file => {
      formData.append('imageFiles', file)
    })
  }
  return request({
    url: '/spu/update',
    method: 'put',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除SPU
export function deleteSpu(id: number | string) {
  return request({
    url: `/spu/delete/${id}`,
    method: 'delete'
  })
}

// 获取卖家SPU列表
export function getSellerSpuList(params: Record<string, any>): Promise<{ list: any[] }> {
  return request({
    url: '/spu/page',
    method: 'get',
    params
  })
}

// SKU相关接口

export interface SkuForm {
  id?: number
  spuId: number
  skuCode?: string
  price: number
  marketPrice?: number
  stock: number
  warnStock?: number
  specs: Record<string, string>
  weight?: number
  image?: string
}

// 创建SKU
export function createSku(data: SkuForm) {
  return request({
    url: '/sku/add',
    method: 'post',
    data
  })
}

// 批量创建SKU
export function batchCreateSku(data: SkuForm[]) {
  return request({
    url: '/sku/batch-add',
    method: 'post',
    data
  })
}

// 更新SKU
export function updateSku(data: SkuForm) {
  return request({
    url: '/sku/update',
    method: 'put',
    data
  })
}

// 删除SKU
export function deleteSku(id: number | string) {
  return request({
    url: `/sku/delete/${id}`,
    method: 'delete'
  })
}

// 删除SPU下所有SKU
export function deleteSkuBySpu(spuId: number | string) {
  return request({
    url: `/sku/delete-by-spu/${spuId}`,
    method: 'delete'
  })
}

// 更新SKU库存
export function updateSkuStock(data: { id: number; stock: number }) {
  return request({
    url: '/sku/update-stock',
    method: 'put',
    data
  })
}

// 扣减SKU库存
export function decreaseSkuStock(data: { id: number; quantity: number }) {
  return request({
    url: '/sku/decrease-stock',
    method: 'put',
    data
  })
}

// 获取分类树
export function getCategoryTree() {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

// 获取品牌列表
export function getBrandList() {
  return request({
    url: '/brand/list',
    method: 'get'
  })
}

// 分类属性响应类型（用于商品编辑时的属性选择）
export interface CategoryAttrItem {
  attrId: number
  attrName: string
  type: number
  values: {
    valueId: number
    value: string
    imageUrl?: string
  }[]
}

// 获取分类属性（销售属性）
export function getSalesAttributes(categoryId: number) {
  return request({
    url: `/attribute/sales/${categoryId}`,
    method: 'get'
  }) as Promise<CategoryAttrItem[]>
}

// 获取分类基本属性
export function getBasicAttributes(categoryId: number) {
  return request({
    url: `/attribute/basic/${categoryId}`,
    method: 'get'
  }) as Promise<CategoryAttrItem[]>
}

// 获取SPU所有属性
export function getSpuAllAttributes(spuId: number) {
  return request({
    url: `/attribute/spu/${spuId}/all`,
    method: 'get'
  })
}