import request from '@/utils/request'

/**
 * 购物车项VO
 */
export interface CartItemVo {
  id: number
  skuId: number
  spuId: number
  quantity: number
  selected: number // 1-选中 0-未选中
  productName: string
  productImage: string
  skuSpecs: string // JSON格式的规格
  price: number
  subtotal: number
  stock: number
}

/**
 * 购物车概要
 */
export interface CartSummary {
  totalCount: number
  selectedCount: number
  totalPrice: number
}

// 获取购物车列表
export function getCartList(): Promise<CartItemVo[]> {
  return request({
    url: '/cart/list',
    method: 'get'
  })
}

// 添加商品到购物车
export function addToCart(data: {
  skuId: number
  quantity?: number
  productImage?: string
  skuSpecs?: string
  notes?: string
}) {
  return request({
    url: '/cart/add',
    method: 'post',
    data
  })
}

// 更新购物车商品数量 (通过Query参数)
export function updateCartQuantity(skuId: number | string, quantity: number) {
  return request({
    url: `/cart/quantity/${skuId}`,
    method: 'put',
    params: { quantity }
  })
}

// 设置商品选中状态 (通过Query参数)
export function setCartSelected(skuId: number | string, selected: number) {
  return request({
    url: `/cart/selected/${skuId}`,
    method: 'put',
    params: { selected }
  })
}

// 全选/取消全选 (通过Query参数)
export function setCartSelectedAll(selected: number) {
  return request({
    url: '/cart/selected-all',
    method: 'put',
    params: { selected }
  })
}

// 删除购物车商品
export function removeCartItem(skuId: number | string) {
  return request({
    url: `/cart/remove/${skuId}`,
    method: 'delete'
  })
}

// 清空购物车
export function clearCart() {
  return request({
    url: '/cart/clear',
    method: 'delete'
  })
}

// 清空已选商品
export function clearSelectedCart() {
  return request({
    url: '/cart/clear-selected',
    method: 'delete'
  })
}

// 获取已选商品
export function getSelectedCart(): Promise<CartItemVo[]> {
  return request({
    url: '/cart/selected',
    method: 'get'
  })
}

// 获取购物车概要
export function getCartSummary(): Promise<CartSummary> {
  return request({
    url: '/cart/summary',
    method: 'get'
  })
}