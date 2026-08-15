import request from '@/utils/request'

/**
 * 登录请求
 */
export interface LoginRequest {
  account: string
  password: string
  captchaKey?: string
  captcha?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  token: string
  userId: number
  username: string
  roles: string[]
  permissions: string[]
}

/**
 * 验证码响应
 */
export interface CaptchaResponse {
  captchaKey: string
  captchaImage: string
}

/**
 * 注册请求
 */
export interface RegisterRequest {
  username: string
  password: string
  email: string
  emailCode: string
  phone?: string
  realName?: string
}

/**
 * 注册响应
 */
export interface RegisterResponse {
  id: number
  username: string
}

/**
 * 邮箱验证码登录请求
 */
export interface EmailCodeLoginRequest {
  email: string
  code: string
}

/**
 * 重置密码请求
 */
export interface ResetPasswordRequest {
  email: string
  code: string
  newPassword: string
}

/**
 * 角色信息
 */
export interface Role {
  id: number | null
  name: string
  code: string
  description: string | null
  status: string | null
  createdAt: string | null
  updatedAt: string | null
  permissions?: string[] | null
}

/**
 * 用户信息
 */
export interface UserInfo {
  id: number
  username: string
  realName?: string
  phone?: string
  email?: string
  avatar?: string
  status?: number
  lastLoginTime?: string
  createdAt?: string
  roles: Role[]
  permissions: string[]
}

// 获取验证码
export function getCaptcha(): Promise<CaptchaResponse> {
  return request({
    url: '/captcha',
    method: 'get'
  })
}

// 用户登录（支持用户名/手机号/邮箱 + 密码）
export function login(data: LoginRequest): Promise<LoginResponse> {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 管理员登录（仅限 ADMIN / SUPER_ADMIN 角色）
export function adminLogin(data: LoginRequest): Promise<LoginResponse> {
  return request({
    url: '/auth/admin/login',
    method: 'post',
    data
  })
}

// 发送邮箱登录验证码
export function sendEmailLoginCode(email: string, captchaKey: string, captcha: string): Promise<void> {
  return request({
    url: '/auth/login/send-email-code',
    method: 'post',
    data: { email, captchaKey, captcha }
  })
}

// 邮箱验证码登录
export function emailCodeLogin(data: EmailCodeLoginRequest): Promise<LoginResponse> {
  return request({
    url: '/auth/login/email-code',
    method: 'post',
    data
  })
}

// 发送重置密码验证码
export function sendResetPasswordCode(email: string): Promise<void> {
  return request({
    url: '/auth/reset-password/send-code',
    method: 'post',
    data: { email }
  })
}

// 重置密码（通过邮箱验证码）
export function resetPassword(data: ResetPasswordRequest): Promise<void> {
  return request({
    url: '/auth/reset-password/reset',
    method: 'post',
    data
  })
}

// 发送注册邮箱验证码（需先通过图形验证码校验）
export function sendRegisterEmailCode(data: { email: string; captchaKey: string; captcha: string }): Promise<void> {
  return request({
    url: '/auth/register/send-email-code',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data: RegisterRequest): Promise<RegisterResponse> {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 用户登出
export function logout(): Promise<void> {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

// 获取当前用户信息
export function getUserInfo(): Promise<UserInfo> {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}