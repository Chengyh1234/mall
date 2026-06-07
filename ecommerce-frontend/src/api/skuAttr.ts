import request from '@/utils/request'

export interface BindSkuAttrRequest {
  skuId: number
  attrValueIds: number[]
}

export function bindSkuAttr(data: BindSkuAttrRequest): Promise<void> {
  return request({
    url: '/sku/attr/bind',
    method: 'post',
    data
  })
}

export interface BatchBindSkuAttrItem {
  skuId: number
  attrValueIds: number[]
}

export function batchBindSkuAttr(data: BatchBindSkuAttrItem[]): Promise<{
  totalBindCount: number
  skuCount: number
}> {
  return request({
    url: '/sku/attr/batch-bind',
    method: 'post',
    data
  })
}

export function updateSkuAttr(skuId: number, data: BindSkuAttrRequest): Promise<void> {
  return request({
    url: `/sku/attr/update/${skuId}`,
    method: 'put',
    data
  })
}

export function getSkuAttrDetail(skuId: number): Promise<{
  skuId: number
  skuCode: string
  spuId: number
  spuName: string
  price: number
  stock: number
  image: string
  saleAttrs: Array<{
    id: number
    attrId: number
    attrName: string
    attrValueId: number
    attrValue: string
    imageUrl: string | null
  }>
}> {
  return request({
    url: `/sku/attr/detail/${skuId}`,
    method: 'get'
  })
}

export function getSkuAvailableAttrs(spuId: number): Promise<{
  spuId: number
  spuName: string
  saleAttrs: Array<{
    attrId: number
    attrName: string
    attrType: number
    values: Array<{
      valueId: number
      value: string
      imageUrl: string | null
      sort: number
    }>
  }>
}> {
  return request({
    url: `/sku/attr/available/${spuId}`,
    method: 'get'
  })
}

export interface UpdateSkuCombinedRequest {
  id: number
  price?: number
  marketPrice?: number
  costPrice?: number
  stock?: number
  warnStock?: number
  image?: string
  weight?: number
  status?: number
}

export function updateSkuCombined(data: UpdateSkuCombinedRequest): Promise<void> {
  return request({
    url: '/sku/attr/update-combined',
    method: 'put',
    data
  })
}

export function batchUpdateSku(data: UpdateSkuCombinedRequest[]): Promise<{
  successCount: number
  totalCount: number
}> {
  return request({
    url: '/sku/attr/batch-update',
    method: 'put',
    data
  })
}

export interface CreateSkuWithAttrRequest {
  spuId: number
  price?: number
  marketPrice?: number
  costPrice?: number
  stock?: number
  warnStock?: number
  image?: string
  weight?: number
  status?: number
  attrValueIds: number[]
}

export function createSkuWithAttr(data: CreateSkuWithAttrRequest): Promise<{
  skuId: number
}> {
  return request({
    url: '/sku/attr/create',
    method: 'post',
    data
  })
}

export function batchCreateSkuWithAttr(data: CreateSkuWithAttrRequest[]): Promise<{
  successCount: number
  totalCount: number
  failedList?: Array<{ attrValueIds: number[]; reason: string }>
}> {
  return request({
    url: '/sku/attr/batch-create',
    method: 'post',
    data
  })
}