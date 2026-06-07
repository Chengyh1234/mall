import request from '@/utils/request'

/**
 * 订单项接口
 */
export interface OrderItem {
  id: number
  spuId: number
  name: string
  image: string
  specs: string
  price: number
  quantity: number
}

/**
 * 发货信息接口
 */
export interface ShippingInfo {
  company: string
  trackingNo: string
}

/**
 * 订单接口
 */
export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  payAmount: number
  discountAmount: number
  freightAmount: number
  status: number // 1-待付款, 2-待发货, 3-待收货, 4-已完成, 5-已取消
  payStatus: number
  payTime: string | null
  payType: string | null
  deliveryType: string | null
  deliveryCompany: string | null
  deliveryNo: string | null
  deliveryTime: string | null
  receiveTime: string | null
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  createdAt: string
  updatedAt: string
  cancelReason: string | null
  version: number
  statusDesc?: string
  totalQuantity?: number
  items?: OrderItem[]
  shippingInfo?: {
    company: string
    trackingNo: string
  }
}

/**
 * 订单详情响应
 */
export interface OrderDetail {
  order: Order
  items: OrderItem[]
  deliveries: any[]
  statusDesc: string
  payStatusDesc: string
  payTypeDesc: string
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

/**
 * 创建订单请求
 */
export interface CreateOrderRequest {
  addressId?: number
  receiverName?: string
  receiverPhone?: string
  receiverAddress?: string
  totalAmount: number
  discountAmount?: number
  freightAmount?: number
  payAmount: number
  remark?: string
  items: Array<{
    skuId: number
    quantity: number
    productName: string
    productImage?: string
    skuSpecs?: string
    price: number
    giftFlag?: number
  }>
}

/**
 * 从购物车创建订单请求
 */
export interface CreateOrderFromCartRequest {
  addressId: number
  payType: string
  buyerMessage?: string
}

/**
 * 创建订单响应
 */
export interface CreateOrderResponse {
  orderId: number
  orderNo: string
  payAmount: number
  totalAmount?: number
  order?: {
    id: number
    orderNo: string
    totalAmount?: number
    payAmount?: number
  }
}

// 创建订单
export function createOrder(data: CreateOrderRequest): Promise<CreateOrderResponse> {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

// 从购物车结算创建订单
export function createOrderFromCart(data: CreateOrderFromCartRequest): Promise<CreateOrderResponse> {
  return request({
    url: '/order/create-from-cart',
    method: 'post',
    params: {
      addressId: data.addressId,
      payType: data.payType,
      buyerMessage: data.buyerMessage
    }
  })
}

// 获取订单详情
export function getOrderDetail(orderNo: string): Promise<OrderDetail> {
  return request({
    url: `/order/detail/${orderNo}`,
    method: 'get'
  })
}

// 获取订单列表
export function getOrderList(params?: { 
  status?: number, 
  page?: number, 
  pageSize?: number 
}): Promise<Order[]> {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

// 获取订单明细
export function getOrderItems(orderId: number): Promise<OrderItem[]> {
  return request({
    url: `/order/items/${orderId}`,
    method: 'get'
  })
}

// 商家查询店铺订单列表
export function getSellerOrderList(params?: {
  status?: number
}): Promise<Order[] | { list: Order[]; total: number }> {
  return request({
    url: '/order/seller/list',
    method: 'get',
    params
  })
}

// 商家查询订单详情
export function getSellerOrderDetail(orderNo: string): Promise<OrderDetail> {
  return request({
    url: `/order/seller/detail/${orderNo}`,
    method: 'get'
  })
}

// 取消订单
export function cancelOrder(orderId: number): Promise<void> {
  return request({
    url: `/order/cancel/${orderId}`,
    method: 'put'
  })
}

// 支付订单
export function payOrder(orderId: number, payType?: string): Promise<void> {
  return request({
    url: `/order/pay/${orderId}`,
    method: 'put',
    params: payType ? { payType } : undefined
  })
}

// 订单发货
export function deliverOrder(orderId: number, data: { 
  deliveryCompany: string, 
  deliveryNo: string 
}): Promise<void> {
  return request({
    url: `/order/deliver/${orderId}`,
    method: 'put',
    params: data
  })
}

// 确认收货
export function confirmReceive(orderId: number): Promise<void> {
  return request({
    url: `/order/confirm/${orderId}`,
    method: 'put'
  })
}

// 删除订单
export function deleteOrder(orderId: number): Promise<void> {
  return request({
    url: `/order/delete/${orderId}`,
    method: 'delete'
  })
}

// 获取订单状态描述
export function getStatusDesc(status: number): Promise<{ statusDesc: string }> {
  return request({
    url: `/order/status-desc/${status}`,
    method: 'get'
  })
}

// 获取支付状态描述
export function getPayStatusDesc(payStatus: number): Promise<{ payStatusDesc: string }> {
  return request({
    url: `/order/pay-status-desc/${payStatus}`,
    method: 'get'
  })
}