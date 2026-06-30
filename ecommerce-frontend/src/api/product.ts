import request from '@/utils/request'
import type { SpuPageVO, SpuDetailVO } from '@/api/spu'

export interface Product {
  id: number
  name: string
  description: string
  categoryId: number
  brandId: number
  mainImage: string | null
  images: string | null
  price?: number
  minPrice?: number
  unit: string
  keywords: string
  sales: number
  status: number
  createdAt: string
  updatedAt: string
}

export interface ProductPageResult {
  page: number
  pageSize: number
  list: SpuPageVO[]
  total: number
}

// 商品分页查询
export function getProductPage(params: Record<string, any>): Promise<ProductPageResult> {
  return request({
    url: '/spu/page',
    method: 'get',
    params
  })
}

// SKU规格（JSON格式：{"颜色":"红色","尺寸":"L"}）
export interface SkuSpecs {
  [key: string]: string
}

// SKU销售属性项
export interface SkuSaleAttribute {
  valueId: number
  attrId: number
  imageUrl: string | null
  value: string
  attrName: string
}

// SKU
export interface Sku {
  id: number
  spuId: number
  skuCode?: string
  price: number
  marketPrice?: number
  originalPrice?: number
  costPrice?: number
  stock?: number
  inStock?: boolean
  warnStock?: number
  specs: string | SkuSpecs
  weight?: number
  image?: string
  status?: number
  createdAt?: string
  updatedAt?: string
  sales?: number
  saleAttributes?: SkuSaleAttribute[]
}

// SPU基本属性
export interface SpuBasicAttribute {
  id: number
  spuId: number
  templateId: number
  attrValue: string
}

// 规格值
export interface SpecValue {
  id: number
  value: string
}

// 规格模板
export interface SpecTemplate {
  templateId: number
  templateName: string
  templateType: number
  values: SpecValue[]
}

// 商品详情 — 后端直接返回 SpuDetailVO（扁平结构）
export function getProductDetail(id: number | string): Promise<SpuDetailVO> {
  return request({
    url: `/spu/detail/${id}`,
    method: 'get'
  })
}

// 获取SKU列表
export function getSkuList(spuId: number): Promise<Sku[]> {
  return request({
    url: '/sku/list',
    method: 'get',
    params: { spuId }
  })
}

// 获取带销售属性的SKU列表（前台公开）
export function getSkuListWithAttributes(spuId: number): Promise<Sku[]> {
  return request({
    url: '/sku/list-with-attributes',
    method: 'get',
    params: { spuId }
  })
}

// 获取带销售属性的SKU列表（卖家端，含库存等管理信息）
export function getStoreSkuListWithAttributes(spuId: number): Promise<Sku[]> {
  return request({
    url: '/sku/store/list-with-attributes',
    method: 'get',
    params: { spuId }
  })
}

// 获取SKU详情
export function getSkuDetail(id: number | string): Promise<Sku> {
  return request({
    url: `/sku/detail/${id}`,
    method: 'get'
  })
}

// 获取SKU详情（含属性）
export function getSkuDetailWithAttributes(id: number | string): Promise<Sku> {
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

// 获取SPU最低价
export function getSkuMinPrice(spuId: number): Promise<number> {
  return request({
    url: `/sku/min-price/${spuId}`,
    method: 'get'
  })
}

// 获取SPU总库存
export function getSkuTotalStock(spuId: number): Promise<number> {
  return request({
    url: `/sku/total-stock/${spuId}`,
    method: 'get'
  })
}

// 销售属性值
export interface SalesAttrValue {
  valueId: number
  imageUrl: string | null
  value: string
}

// 销售属性
export interface SpuSalesAttribute {
  attrId: number
  attrName: string
  values: SalesAttrValue[]
}

// 基本属性
export interface SpuBasicAttribute {
  attrId: number
  attrName: string
  valueId: number | null
  value: string
  imageUrl: string | null
}

// API响应格式
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

// 获取分类属性
export function getCategoryAttributes(categoryId: number): Promise<ApiResponse<any[]>> {
  return request({
    url: `/attribute/category/${categoryId}`,
    method: 'get'
  })
}

// 获取分类销售属性
export function getCategorySalesAttributes(categoryId: number): Promise<ApiResponse<SpuSalesAttribute[]>> {
  return request({
    url: `/attribute/sales/${categoryId}`,
    method: 'get'
  })
}

// 获取分类基本属性
export function getCategoryBasicAttributes(categoryId: number): Promise<SpuBasicAttribute[]> {
  return request({
    url: `/attribute/basic/${categoryId}`,
    method: 'get'
  })
}

// 获取指定类型属性
export function getCategoryAttributesByType(categoryId: number, type: number): Promise<ApiResponse<any[]>> {
  return request({
    url: `/attribute/category/${categoryId}/type/${type}`,
    method: 'get'
  })
}

// 获取SPU基本属性
export function getSpuBasicAttributes(spuId: number): Promise<ApiResponse<SpuBasicAttribute[]>> {
  return request({
    url: `/spu/${spuId}/basic-attributes`,
    method: 'get'
  })
}

// 获取SPU销售属性
export function getSpuSalesAttributes(spuId: number): Promise<SpuSalesAttribute[]> {
  return request({
    url: `/attribute/spu/${spuId}/sales`,
    method: 'get'
  })
}

// 获取SPU所有属性
export function getSpuAllAttributes(spuId: number): Promise<ApiResponse<any>> {
  return request({
    url: `/attribute/spu/${spuId}/all`,
    method: 'get'
  })
}

export interface Category {
  id: number
  name: string
  parentId: number
  level: number
  sort: number
  status: number
  icon: string | null
  children?: Category[]
}

// 获取分类列表
export function getCategoryList(): Promise<Category[]> {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 获取分类树
export function getCategoryTree(): Promise<Category[]> {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

// 获取一级分类
export function getCategoryLevel1(): Promise<Category[]> {
  return request({
    url: '/category/level1',
    method: 'get'
  })
}

// 获取子分类
export function getCategoryChildren(parentId: number | string): Promise<Category[]> {
  return request({
    url: `/category/children/${parentId}`,
    method: 'get'
  })
}

// 获取分类详情
export function getCategoryDetail(id: number | string): Promise<Category> {
  return request({
    url: `/category/detail/${id}`,
    method: 'get'
  })
}

export interface Brand {
  id: number
  name: string
  logo: string | null
  description: string
  status: number
}

// 获取品牌列表
export function getBrandList(): Promise<Brand[]> {
  return request({
    url: '/brand/list',
    method: 'get'
  })
}

// 获取品牌详情
export function getBrandDetail(id: number | string): Promise<Brand> {
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

// 搜索品牌
export function searchBrand(keyword: string): Promise<Brand[]> {
  return request({
    url: '/brand/search',
    method: 'get',
    params: { keyword }
  })
}