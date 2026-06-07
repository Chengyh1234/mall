import request from '@/utils/request'

/**
 * 规格模板接口
 */
export interface SpecTemplate {
  id: number
  categoryId: number
  name: string
  status?: number
  createdAt?: string
  updatedAt?: string
}

/**
 * 规格值接口
 */
export interface SpecValue {
  id: number
  templateId: number
  value: string
}

/**
 * 规格模板详情(含规格值)
 */
export interface SpecTemplateDetail {
  specTemplate: SpecTemplate
  values: SpecValue[]
}

/**
 * 分类规格模板及规格值
 */
export interface CategorySpecWithValues {
  templateId: number
  templateName: string
  values: SpecValue[]
}

// 新增规格模板
export function addSpecTemplate(data: {
  categoryId: number
  name: string
  status?: number
}): Promise<{ id: number }> {
  return request({
    url: '/spec-template/add',
    method: 'post',
    data
  })
}

// 更新规格模板
export function updateSpecTemplate(data: {
  id: number
  categoryId?: number
  name?: string
  status?: number
}): Promise<void> {
  return request({
    url: '/spec-template/update',
    method: 'put',
    data
  })
}

// 删除规格模板
export function deleteSpecTemplate(id: number): Promise<void> {
  return request({
    url: `/spec-template/delete/${id}`,
    method: 'delete'
  })
}

// 获取规格模板详情(含规格值)
export function getSpecTemplateDetail(id: number): Promise<SpecTemplateDetail> {
  return request({
    url: `/spec-template/detail/${id}`,
    method: 'get'
  })
}

// 获取规格模板列表
export function getSpecTemplateList(params?: {
  categoryId?: number
  status?: number
}): Promise<SpecTemplate[]> {
  return request({
    url: '/spec-template/list',
    method: 'get',
    params
  })
}

// 获取分类规格模板及规格值
export function getCategorySpecWithValues(categoryId: number): Promise<CategorySpecWithValues[]> {
  return request({
    url: `/spec-template/with-values/${categoryId}`,
    method: 'get'
  })
}

// 新增规格值
export function addSpecValue(data: {
  templateId: number
  value: string
}): Promise<{ id: number }> {
  return request({
    url: '/spec-value/add',
    method: 'post',
    data
  })
}

// 批量新增规格值
export function batchAddSpecValue(data: {
  templateId: number
  values: string[]
}): Promise<void> {
  return request({
    url: '/spec-value/batch-add',
    method: 'post',
    data
  })
}

// 更新规格值
export function updateSpecValue(data: {
  id: number
  templateId?: number
  value?: string
}): Promise<void> {
  return request({
    url: '/spec-value/update',
    method: 'put',
    data
  })
}

// 删除规格值
export function deleteSpecValue(id: number): Promise<void> {
  return request({
    url: `/spec-value/delete/${id}`,
    method: 'delete'
  })
}

// 获取规格模板的规格值列表
export function getSpecValueList(templateId: number): Promise<SpecValue[]> {
  return request({
    url: '/spec-value/list',
    method: 'get',
    params: { templateId }
  })
}

// 获取所有规格值
export function getAllSpecValues(): Promise<SpecValue[]> {
  return request({
    url: '/spec-value/all',
    method: 'get'
  })
}