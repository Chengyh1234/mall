import request from '@/utils/request'

/**
 * 地址接口
 */
export interface Address {
  id: number
  userId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  zipCode?: string
  isDefault: number // 1-默认 0-非默认
}

// 添加收货地址
export function addAddress(data: {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  zipCode?: string
  isDefault?: number
}): Promise<{ id: number }> {
  return request({
    url: '/address/add',
    method: 'post',
    data
  })
}

// 更新收货地址
export function updateAddress(data: {
  id: number
  receiverName?: string
  receiverPhone?: string
  province?: string
  city?: string
  district?: string
  detailAddress?: string
  zipCode?: string
  isDefault?: number
}): Promise<{ id: number }> {
  return request({
    url: '/address/update',
    method: 'put',
    data
  })
}

// 删除收货地址
export function deleteAddress(addressId: number): Promise<void> {
  return request({
    url: `/address/delete/${addressId}`,
    method: 'delete'
  })
}

// 获取地址详情
export function getAddressDetail(addressId: number): Promise<Address> {
  return request({
    url: `/address/detail/${addressId}`,
    method: 'get'
  })
}

// 获取地址列表
export function getAddressList(): Promise<Address[]> {
  return request({
    url: '/address/list',
    method: 'get'
  })
}

// 获取默认地址
export function getDefaultAddress(): Promise<Address> {
  return request({
    url: '/address/default',
    method: 'get'
  })
}

// 设置默认地址
export function setDefaultAddress(addressId: number): Promise<void> {
  return request({
    url: `/address/set-default/${addressId}`,
    method: 'put'
  })
}