import request from '@/utils/request'

/**
 * 分类接口
 */
export interface Category {
  id: number
  name: string
  parentId?: number
  level?: number
  icon?: string
  sort?: number
  status?: number
  createdAt?: string
  children?: Category[]
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

// 获取分类列表
export function getCategoryList(params?: {
  parentId?: number
  level?: number
  status?: number
}): Promise<Category[]> {
  return request({
    url: '/category/list',
    method: 'get',
    params
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
export function getCategoryChildren(parentId: number): Promise<Category[]> {
  return request({
    url: `/category/children/${parentId}`,
    method: 'get'
  })
}

// 获取分类树形结构
export function getCategoryTree(): Promise<Category[]> {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

// 分页获取分类列表
export function getCategoryPage(params?: {
  keyword?: string
  status?: number
  page?: number
  pageSize?: number
}): Promise<PageResult<Category>> {
  return request({
    url: '/category/page',
    method: 'get',
    params
  })
}

// 获取分类详情
export function getCategoryDetail(id: number): Promise<Category> {
  return request({
    url: `/category/detail/${id}`,
    method: 'get'
  })
}

// 新增分类（支持图标上传）
export function addCategory(data: {
  name: string
  parentId?: number
  level?: number
  sort?: number
  status?: number
  iconFile?: File
}): Promise<{ id: number; level: number; icon: string }> {
  const formData = new FormData()
  const dto: Record<string, any> = { name: data.name }
  if (data.parentId !== undefined) dto.parentId = data.parentId
  if (data.level !== undefined) dto.level = data.level
  if (data.sort !== undefined) dto.sort = data.sort
  if (data.status !== undefined) dto.status = data.status
  formData.append('categoryDto', JSON.stringify(dto))
  if (data.iconFile) {
    formData.append('iconFile', data.iconFile)
  }
  return request({
    url: '/category/add',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 更新分类（支持图标上传）
export function updateCategory(data: {
  id: number
  name?: string
  parentId?: number
  sort?: number
  status?: number
  iconFile?: File
}): Promise<{ id: number; icon: string }> {
  const formData = new FormData()
  const dto: Record<string, any> = { id: data.id }
  if (data.name !== undefined) dto.name = data.name
  if (data.parentId !== undefined) dto.parentId = data.parentId
  if (data.sort !== undefined) dto.sort = data.sort
  if (data.status !== undefined) dto.status = data.status
  formData.append('categoryDto', JSON.stringify(dto))
  if (data.iconFile) {
    formData.append('iconFile', data.iconFile)
  }
  return request({
    url: '/category/update',
    method: 'put',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取分类图标预览地址
export function getCategoryIconUrl(iconPath: string): string {
  if (!iconPath) return ''
  if (iconPath.startsWith('http')) return iconPath
  return `/api/file/preview?relativePath=${iconPath}&subDir=icons`
}

// 删除分类
export function deleteCategory(id: number): Promise<void> {
  return request({
    url: `/category/delete/${id}`,
    method: 'delete'
  })
}