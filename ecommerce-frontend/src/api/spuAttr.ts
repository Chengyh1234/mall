import request from '@/utils/request'

/**
 * SPU基本属性绑定信息
 */
export interface SpuBasicAttrBind {
  spuId: number
  attrId: number
  attrValueId?: number
  manualValue?: string
}

/**
 * SPU销售属性绑定信息
 */
export interface SpuSaleAttrBind {
  spuId: number
  attrId: number
  selectedValueIds: number[]
  /** 新增的自定义值（文本），后端会创建并返回对应的 valueId */
  customValues?: string[]
}

/**
 * SPU基本属性绑定记录
 */
export interface SpuBasicAttr {
  id: number
  spuId: number
  attrId: number
  attrValueId: number
  manualValue?: string
  createdAt: string
}

/**
 * SPU销售属性绑定记录
 */
export interface SpuSaleAttr {
  id: number
  spuId: number
  attrId: number
  selectedValues: string
  createdAt: string
}

/**
 * SPU完整属性信息
 */
export interface SpuAllAttrs {
  spuId: number
  spuName: string
  basicAttrs: SpuBasicAttrDetail[]
  saleAttrs: SpuSaleAttrDetail[]
}

export interface SpuBasicAttrDetail {
  id: number
  attrId: number
  attrName: string
  attrType: number
  attrValueId: number
  attrValue: string
  manualValue?: string
}

export interface SpuSaleAttrDetail {
  id: number
  attrId: number
  attrName: string
  selectedValues: SpuAttrValue[]
}

export interface SpuAttrValue {
  valueId: number
  value: string
  imageUrl?: string
  /** true=卖家自定义，false=平台预设 */
  custom?: boolean
}

/**
 * SPU可绑定属性
 */
export interface SpuAvailableAttrs {
  spuId: number
  categoryId: number
  basicAttrs: SpuAvailableBasicAttr[]
  saleAttrs: SpuAvailableSaleAttr[]
}

export interface SpuAvailableBasicAttr {
  attrId: number
  attrName: string
  attrType: number
  bound: boolean
  boundId?: number
  values: AttrValueInfo[]
  currentValues?: {
    valueId?: number
    value: string
    imageUrl?: string
  }[]
}

export interface SpuAvailableSaleAttr {
  attrId: number
  attrName: string
  attrType: number
  bound: boolean
  boundId?: number
  values: AttrValueInfo[]
  currentValues?: {
    valueId?: number
    value: string
    imageUrl?: string
  }[]
}

export interface AttrValueInfo {
  valueId: number
  value: string
  imageUrl?: string
  sort?: number
}

/**
 * 批量绑定SPU基本属性
 * @param data 绑定信息列表
 */
export function batchBindSpuBasicAttr(data: SpuBasicAttrBind[]): Promise<{ successCount: number; totalCount: number }> {
  return request({
    url: '/spu/attr/basic/batch-bind',
    method: 'post',
    data
  })
}

/**
 * 绑定SPU基本属性
 */
export function bindSpuBasicAttr(data: SpuBasicAttrBind): Promise<{ id: number; message: string }> {
  return request({
    url: '/spu/attr/basic/bind',
    method: 'post',
    data
  })
}

/**
 * 更新SPU基本属性绑定
 */
export function updateSpuBasicAttr(id: number, data: SpuBasicAttrBind): Promise<void> {
  return request({
    url: `/spu/attr/basic/update/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除SPU基本属性绑定
 */
export function deleteSpuBasicAttr(id: number): Promise<void> {
  return request({
    url: `/spu/attr/basic/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 获取SPU基本属性列表
 */
export function getSpuBasicAttrList(spuId: number): Promise<SpuBasicAttr[]> {
  return request({
    url: `/spu/attr/basic/list/${spuId}`,
    method: 'get'
  })
}

/**
 * 批量绑定SPU销售属性
 */
export function batchBindSpuSaleAttr(data: SpuSaleAttrBind[]): Promise<{ successCount: number; totalCount: number }> {
  return request({
    url: '/spu/attr/sale/batch-bind',
    method: 'post',
    data
  })
}

/**
 * 绑定SPU销售属性
 */
export function bindSpuSaleAttr(data: SpuSaleAttrBind): Promise<{ id: number; message: string }> {
  return request({
    url: '/spu/attr/sale/bind',
    method: 'post',
    data
  })
}

/**
 * 更新SPU销售属性绑定
 */
export function updateSpuSaleAttr(id: number, data: SpuSaleAttrBind): Promise<void> {
  return request({
    url: `/spu/attr/sale/update/${id}`,
    method: 'put',
    data
  })
}

export interface BatchUpdateBasicAttrItem {
  id: number
  spuId: number
  attrId: number
  attrValueId?: number
  manualValue?: string
}

export interface BatchUpdateSaleAttrItem {
  id: number
  spuId: number
  attrId: number
  selectedValueIds: number[]
  /** 新增的自定义值（文本），后端会创建并返回对应的 valueId */
  customValues?: string[]
}

/**
 * 批量更新SPU基本属性绑定
 */
export function batchUpdateSpuBasicAttr(data: BatchUpdateBasicAttrItem[]): Promise<{ successCount: number; totalCount: number }> {
  return request({
    url: '/spu/attr/basic/batch-update',
    method: 'put',
    data
  })
}

/**
 * 批量更新SPU销售属性绑定
 */
export function batchUpdateSpuSaleAttr(data: BatchUpdateSaleAttrItem[]): Promise<{ successCount: number; totalCount: number }> {
  return request({
    url: '/spu/attr/sale/batch-update',
    method: 'put',
    data
  })
}

/**
 * 删除SPU销售属性绑定
 */
export function deleteSpuSaleAttr(id: number): Promise<void> {
  return request({
    url: `/spu/attr/sale/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 获取SPU销售属性列表
 */
export function getSpuSaleAttrList(spuId: number): Promise<SpuSaleAttr[]> {
  return request({
    url: `/spu/attr/sale/list/${spuId}`,
    method: 'get'
  })
}

/**
 * 获取SPU所有属性（基本+销售）
 */
export function getSpuAllAttrs(spuId: number): Promise<SpuAllAttrs> {
  return request({
    url: `/spu/attr/all/${spuId}`,
    method: 'get'
  })
}

/**
 * 获取SPU可绑定的属性列表
 */
export function getSpuAvailableAttrs(spuId: number): Promise<SpuAvailableAttrs> {
  return request({
    url: `/spu/attr/available/${spuId}`,
    method: 'get'
  })
}
