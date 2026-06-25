import request from '@/utils/request'

export interface LoginResult {
  token: string
}

export interface UserProfile {
  id: number
  username: string
  email: string
  phone: string
  avatar: string
  realName: string
  status: number
  lastLoginTime: string
  createdAt: string
}

export interface UpdateProfileRequest {
  username?: string
  realName?: string
  email?: string
  phone?: string
  password?: string
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface UpdatePasswordResponse {
  code: number
  msg: string
  data: null
}

export interface UpdateProfileResponse {
  code?: number
  msg?: string
  message?: string
  id?: number
  username?: string
  email?: string
  phone?: string
  realName?: string
  avatar?: string
  status?: number
}

export interface UploadAvatarResponse {
  avatar: string
  avatarUrl: string
}

export function login(data: Record<string, any>): Promise<LoginResult> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function register(data: Record<string, any>) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

export function getUserInfo(): Promise<UserProfile> {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

export function getUserProfile(): Promise<UserProfile> {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

export function updateUserProfile(data: UpdateProfileRequest): Promise<UpdateProfileResponse> {
  return request({
    url: '/user/profile',
    method: 'put',
    params: data
  })
}

export function updatePassword(data: UpdatePasswordRequest): Promise<UpdatePasswordResponse> {
  return request({
    url: '/user/password',
    method: 'put',
    params: data
  })
}

export function uploadAvatar(file: File): Promise<UploadAvatarResponse> {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ===== 运营管理 - 用户管理 =====

export interface ManageUserPageRequest {
  page: number
  pageSize: number
  keyword?: string
  role?: string
  status?: number
}

export interface ManageUserItem {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  avatar: string
  status: number
  roles: string[]
  createdAt: string
  lastLoginTime: string
}

export interface ManageUserPageResult {
  records: ManageUserItem[]
  total: number
  page: number
  pageSize: number
  pages: number
}

/** 分页查询用户列表 */
export function getManageUserPage(params: ManageUserPageRequest): Promise<ManageUserPageResult> {
  return request({
    url: '/user/manage/page',
    method: 'get',
    params
  })
}

/** 启用用户 */
export function enableUser(userId: number): Promise<void> {
  return request({
    url: `/user/manage/enable/${userId}`,
    method: 'put'
  })
}

/** 禁用用户 */
export function disableUser(userId: number): Promise<void> {
  return request({
    url: `/user/manage/disable/${userId}`,
    method: 'put'
  })
}