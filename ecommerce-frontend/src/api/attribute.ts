import request from '@/utils/request'

/** 属性值对象 */
export interface AttributeValue {
  valueId: number
  attrId: number
  value: string
  imageUrl?: string
  sort: number
  createdAt: string
  updatedAt: string
}

/** 属性对象 */
export interface AttributeItem {
  id: number
  name: string
  attrType: number // 1-销售属性 2-基本属性
  sort: number
  createdAt: string
  updatedAt: string
  values: AttributeValue[]
}

/** 新增/修改属性参数 */
export interface AttributeForm {
  name: string
  attrType: number
  sort?: number
}

/** 新增/修改属性值参数 */
export interface AttributeValueForm {
  attrId: number
  value: string
  imageUrl?: string
  sort?: number
}

// 新增属性
export function addAttribute(data: AttributeForm) {
  return request({
    url: '/attribute/manage/add',
    method: 'post',
    data
  })
}

// 修改属性
export function updateAttribute(id: number, data: AttributeForm) {
  return request({
    url: `/attribute/manage/update/${id}`,
    method: 'put',
    data
  })
}

// 删除属性
export function deleteAttribute(id: number) {
  return request({
    url: `/attribute/manage/delete/${id}`,
    method: 'delete'
  })
}

// 查询属性列表
export function getAttributeList(): Promise<AttributeItem[]> {
  return request<AttributeItem[]>({
    url: '/attribute/manage/list',
    method: 'get'
  }) as Promise<AttributeItem[]>
}

// 查询属性详情
export function getAttributeDetail(id: number) {
  return request<AttributeItem>({
    url: `/attribute/manage/${id}`,
    method: 'get'
  })
}

// 新增属性值
export function addAttributeValue(data: AttributeValueForm) {
  return request({
    url: '/attribute/manage/value/add',
    method: 'post',
    data
  })
}

// 修改属性值
export function updateAttributeValue(id: number, data: AttributeValueForm) {
  return request({
    url: `/attribute/manage/value/update/${id}`,
    method: 'put',
    data
  })
}

// 删除属性值
export function deleteAttributeValue(id: number) {
  return request({
    url: `/attribute/manage/value/delete/${id}`,
    method: 'delete'
  })
}

// 查询属性值列表
export function getAttributeValueList(attrId: number) {
  return request<AttributeValue[]>({
    url: `/attribute/manage/value/list/${attrId}`,
    method: 'get'
  })
}

// ===== 分类-属性绑定管理 =====

/** 分类已绑定的属性记录 */
export interface CategoryBoundAttr {
  id: number
  categoryId: number
  attrId: number
  attrName: string
  attrType: number
  sort: number
  hasSpuUsage: boolean
}

/** 分类可绑定的属性 */
export interface CategoryAvailableAttr {
  id: number
  name: string
  attrType: number
  sort: number
  createdAt: string
  updatedAt: string
}

// 获取分类已绑定的属性列表
export function getCategoryBoundAttrs(categoryId: number): Promise<CategoryBoundAttr[]> {
  return request<CategoryBoundAttr[]>({
    url: `/category/attribute/manage/bound/${categoryId}`,
    method: 'get'
  }) as Promise<CategoryBoundAttr[]>
}

// 获取分类可绑定的属性列表
export function getCategoryAvailableAttrs(categoryId: number): Promise<CategoryAvailableAttr[]> {
  return request<CategoryAvailableAttr[]>({
    url: `/category/attribute/manage/available/${categoryId}`,
    method: 'get'
  }) as Promise<CategoryAvailableAttr[]>
}

// 绑定属性到分类
export function bindAttrToCategory(data: {
  categoryId: number
  attrId: number
  sort?: number
}) {
  return request({
    url: '/category/attribute/manage/bind',
    method: 'post',
    data
  })
}

// 修改绑定排序
export function updateCategoryAttrSort(id: number, sort: number) {
  return request({
    url: `/category/attribute/manage/update-sort/${id}`,
    method: 'put',
    data: { sort }
  })
}

// 解绑属性
export function unbindCategoryAttr(id: number) {
  return request({
    url: `/category/attribute/manage/unbind/${id}`,
    method: 'delete'
  })
}