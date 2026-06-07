import request from '@/utils/request'

/**
 * 商品SKU接口
 */
export interface Sku {
  id: number
  spuId: number
  skuCode: string
  price: number
  marketPrice?: number
  stock: number
  warnStock?: number
  status: number
  image?: string
  specValues?: string // JSON格式
  sales?: number
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

// 新增SKU
export function addSku(data: {
  skuDto: string // JSON字符串
  imageFile?: File
}): Promise<{
  id: number
  image?: string
}> {
  const formData = new FormData()
  formData.append('skuDto', data.skuDto)
  if (data.imageFile) {
    formData.append('imageFile', data.imageFile)
  }
  return request({
    url: '/sku/add',
    method: 'post',
    data: formData
  })
}

// 批量新增SKU
export function batchAddSku(spuId: number, skuList: Array<{
  skuCode: string
  price: number
  marketPrice?: number
  stock: number
  specs?: string
  image?: string
}>): Promise<void> {
  return request({
    url: '/sku/batch-add',
    method: 'post',
    params: {
      spuId,
      skus: JSON.stringify(skuList)
    }
  })
}

// 更新SKU
export function updateSku(data: {
  skuDto: string // JSON字符串
  imageFile?: File
}): Promise<{
  id: number
  image?: string
}> {
  const formData = new FormData()
  formData.append('skuDto', data.skuDto)
  if (data.imageFile) {
    formData.append('imageFile', data.imageFile)
  }
  return request({
    url: '/sku/update',
    method: 'put',
    data: formData
  })
}

// 删除SKU
export function deleteSku(id: number): Promise<void> {
  return request({
    url: `/sku/delete/${id}`,
    method: 'delete'
  })
}

// 删除SPU下所有SKU
export function deleteSkuBySpu(spuId: number): Promise<void> {
  return request({
    url: `/sku/delete-by-spu/${spuId}`,
    method: 'delete'
  })
}

// 获取SKU详情
export function getSkuDetail(id: number): Promise<Sku> {
  return request({
    url: `/sku/detail/${id}`,
    method: 'get'
  })
}

// 获取SKU详情(含属性)
export function getSkuDetailWithAttrs(id: number): Promise<Sku> {
  return request({
    url: `/sku/detail-with-attributes/${id}`,
    method: 'get'
  })
}

// 根据编码获取SKU
export function getSkuByCode(skuCode: string): Promise<Sku> {
  return request({
    url: `/sku/by-code/${skuCode}`,
    method: 'get'
  })
}

// 获取SKU列表
export function getSkuList(params?: {
  spuId?: number
  status?: number
}): Promise<Sku[]> {
  return request({
    url: '/sku/list',
    method: 'get',
    params
  })
}

// 分页获取SKU列表
export function getSkuPage(params?: {
  spuId?: number
  status?: number
  keyword?: string
  page?: number
  pageSize?: number
}): Promise<PageResult<Sku>> {
  return request({
    url: '/sku/page',
    method: 'get',
    params
  })
}

// 获取SPU最低价
export function getSkuMinPrice(spuId: number): Promise<{ price: number }> {
  return request({
    url: `/sku/min-price/${spuId}`,
    method: 'get'
  })
}

// 获取SPU总库存
export function getSkuTotalStock(spuId: number): Promise<{ stock: number }> {
  return request({
    url: `/sku/total-stock/${spuId}`,
    method: 'get'
  })
}

// 更新SKU库存
export function updateSkuStock(data: {
  skuId: number
  stock: number
}): Promise<void> {
  return request({
    url: '/sku/update-stock',
    method: 'put',
    data
  })
}

// 扣减SKU库存
export function decreaseSkuStock(data: {
  skuId: number
  quantity: number
}): Promise<void> {
  return request({
    url: '/sku/decrease-stock',
    method: 'put',
    data
  })
}

// 启用SKU
export function enableSku(id: number): Promise<void> {
  return request({
    url: `/sku/enable/${id}`,
    method: 'put'
  })
}

// 禁用SKU
export function disableSku(id: number): Promise<void> {
  return request({
    url: `/sku/disable/${id}`,
    method: 'put'
  })
}