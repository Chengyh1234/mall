import request from '@/utils/request'

/**
 * 订单项接口（订单详情用）
 */
export interface OrderItem {
  productName: string
  productImage: string
  skuSpecs: string
  price: number
  quantity: number
  totalAmount: number
}

/**
 * 发货记录接口
 */
export interface Delivery {
  deliveryCompany: string
  deliveryNo: string
  deliveryStatus: number
  deliveryTime: string
}

/**
 * 订单详情响应（扁平结构，新 API 格式）
 */
export interface OrderDetail {
  id: number
  orderNo: string
  totalAmount: number
  payAmount: number
  discountAmount: number
  freightAmount: number
  status: number
  payStatus: number
  statusDesc: string
  payStatusDesc: string
  payTypeDesc: string
  payTime: string | null
  expireTime: string | null
  receiveTime: string | null
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
  createdAt: string
  cancelReason: string | null
  refundReason: string | null
  refundAmount: number | null
  rejectReason: string | null
  rejectedAt: string | null
  items: OrderItem[]
  deliveries: Delivery[]
}

/**
 * 订单接口（订单列表用）
 */
export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  payAmount: number
  discountAmount: number
  freightAmount: number
  status: number
  payStatus: number
  payTime: string | null
  payType: string | null
  deliveryCompany: string | null
  deliveryNo: string | null
  deliveryTime: string | null
  receiveTime: string | null
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  expireTime: string | null
  refundAmount: number | null
  remark: string
  createdAt: string
  updatedAt: string
  cancelReason: string | null
  rejectReason?: string
  rejectedAt?: string
  version: number
  statusDesc?: string
  itemCount?: number
  totalQuantity?: number
  items?: OrderItem[]
  shippingInfo?: {
    company: string
    trackingNo: string
  }
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
    spuId: number
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

// 从购物车结算创建订单（后端使用 @RequestParam，返回 List<Long> 即 [orderId]）
export function createOrderFromCart(data: CreateOrderFromCartRequest): Promise<number[]> {
  const params: Record<string, any> = { addressId: data.addressId }
  if (data.buyerMessage) params.buyerMessage = data.buyerMessage
  return request({
    url: '/order/create-from-cart',
    method: 'post',
    params
  })
}

// 获取订单详情
export function getOrderDetail(orderNo: string): Promise<OrderDetail> {
  return request({
    url: `/order/detail/${orderNo}`,
    method: 'get'
  })
}

// 获取订单列表（分页，带商品明细）
export function getOrderListWithItems(params?: {
  page?: number
  pageSize?: number
  status?: number
}): Promise<PageResult<Order>> {
  const cleanParams: Record<string, any> = {}
  if (params) {
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null) cleanParams[key] = val
    })
  }
  return request({
    url: '/order/list-with-items',
    method: 'get',
    params: cleanParams
  })
}

// 获取订单列表（分页）
export function getOrderList(params?: { 
  status?: number, 
  page?: number, 
  pageSize?: number 
}): Promise<PageResult<Order>> {
  // 清理 undefined 参数，防止 Axios 将其序列化为 "undefined" 字符串
  const cleanParams: Record<string, any> = {}
  if (params) {
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null) {
        cleanParams[key] = val
      }
    })
  }
  return request({
    url: '/order/list',
    method: 'get',
    params: cleanParams
  })
}

// 获取订单明细
export function getOrderItems(orderId: number): Promise<OrderItem[]> {
  return request({
    url: `/order/items/${orderId}`,
    method: 'get'
  })
}

// 管理员分页查询全部订单列表
export function getAdminOrderList(params?: {
  status?: number
  userId?: number
  orderNo?: string
  payTimeStart?: string
  payTimeEnd?: string
  deliveryTimeStart?: string
  deliveryTimeEnd?: string
  receiveTimeStart?: string
  receiveTimeEnd?: string
  page?: number
  pageSize?: number
}): Promise<PageResult<Order>> {
  // 清理 undefined 参数
  const cleanParams: Record<string, any> = {}
  if (params) {
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null) {
        cleanParams[key] = val
      }
    })
  }
  return request({
    url: '/order/admin/list',
    method: 'get',
    params: cleanParams
  })
}

// 商家查询店铺订单列表
export function getSellerOrderList(params?: {
  status?: number
  page?: number
  pageSize?: number
  userId?: number
  orderNo?: string
  payTimeStart?: string
  payTimeEnd?: string
  deliveryTimeStart?: string
  deliveryTimeEnd?: string
  receiveTimeStart?: string
  receiveTimeEnd?: string
}): Promise<{ list: any[]; total: number; page: number; pageSize: number }> {
  const cleanParams: Record<string, any> = {}
  if (params) {
    Object.entries(params).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        cleanParams[key] = val
      }
    })
  }
  return request({
    url: '/order/seller/list',
    method: 'get',
    params: cleanParams
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

// 批量付款（从购物车结算后调用）
export interface BatchPayRequest {
  orderIds: number[]
  payType?: string
}

export interface BatchPayFailItem {
  orderId: number
  reason: string
}

export interface BatchPayResponse {
  success: number[]
  fail: BatchPayFailItem[]
  totalCount: number
  successCount: number
  failCount: number
}

export function batchPay(data: BatchPayRequest): Promise<BatchPayResponse> {
  return request({
    url: '/order/batch-pay',
    method: 'post',
    data
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

// 取消退款申请
export function cancelRefund(orderId: number): Promise<void> {
  return request({
    url: `/order/refund/cancel/${orderId}`,
    method: 'put'
  })
}

// 用户申请退款
export function applyRefund(orderId: number, refundReason?: string): Promise<void> {
  return request({
    url: `/order/refund/apply/${orderId}`,
    method: 'post',
    params: refundReason ? { refundReason } : undefined
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

// 管理员审核退款
export function refundReview(orderId: number, reviewResult: number, rejectReason?: string): Promise<void> {
  return request({
    url: `/order/refund/review/${orderId}`,
    method: 'put',
    params: { reviewResult, ...(rejectReason ? { rejectReason } : {}) }
  })
}

// 同意退款
export function refundApprove(orderId: number): Promise<void> {
  return request({
    url: `/order/refund/approve/${orderId}`,
    method: 'put'
  })
}

// 拒绝退款
export function refundReject(orderId: number, rejectReason: string): Promise<void> {
  return request({
    url: `/order/refund/reject/${orderId}`,
    method: 'put',
    params: { rejectReason }
  })
}

// 订单状态统计接口
export interface OrderStatusCount {
  pendingPayment: number    // 待付款
  pendingDelivery: number   // 待发货
  pendingReceipt: number    // 待收货
  refunding: number         // 退款中
}

// 获取订单状态统计（用户个人中心）
export function getOrderStatusCount(): Promise<OrderStatusCount> {
  return request({
    url: '/order/count-status',
    method: 'get'
  })
}