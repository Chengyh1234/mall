import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import { BusinessError } from './business-error'
import { ErrorCode, ERROR_CATEGORY_MAP, ErrorCategory, ERROR_DEFAULT_MSG } from './error-codes'

/**
 * API统一响应格式
 */
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

// 创建axios实例
const request = axios.create({
  baseURL: '/api', // 基础URL，配合Vite代理
  timeout: 10000 // 请求超时时间
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      // 设置Authorization头为Bearer + token格式
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 如果是 FormData 类型，不设置 Content-Type，让浏览器自动处理
    if (!(config.data instanceof FormData)) {
      if (config.headers && !config.headers['Content-Type']) {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8'
      }
    }
    
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// ========== 响应拦截器（统一错误处理） ==========

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 非标准格式（无 code 字段），直接透传
    if (!res || typeof res.code === 'undefined') {
      return res
    }

    // 成功（HTTP 200 + code 200）
    if (res.code === 200) {
      return res.data
    }

    // ────── 业务错误处理 ──────

    const category = (ERROR_CATEGORY_MAP[res.code] || ErrorCategory.NOTIFY) as ErrorCategory
    const defaultMsg = ERROR_DEFAULT_MSG[res.code] || '操作失败'
    const errMsg = res.msg || res.message || defaultMsg

    switch (category) {
      case ErrorCategory.AUTH:
        // 认证失效 → 清除 Pinia 与 localStorage 登录态，跳转登录页
        useUserStore().clearToken()
        router.push('/login')
        ElMessage.error({ message: errMsg, showClose: true, duration: 5000 })
        break

      case ErrorCategory.USER_ACTION:
        // 用户可修正的错误（参数错误、业务规则等）→ 仅弹提示
        ElMessage.warning({ message: errMsg, showClose: true, duration: 5000 })
        break

      case ErrorCategory.SYSTEM:
        // 系统异常 → 弹错误提示
        ElMessage.error({ message: errMsg, showClose: true, duration: 5000 })
        // TODO: 可扩展错误上报
        break

      default:
        ElMessage.info({ message: errMsg, showClose: true, duration: 5000 })
    }

    return Promise.reject(new BusinessError(res.code, errMsg))
  },
  (error: any) => {
    // ────── HTTP 层面错误（网络断开、HTTP 状态码错误等） ──────

    if (error.response) {
      const httpStatus = error.response.status
      const body = error.response.data // 可能为 ApiResponse 格式

      if (httpStatus === 401) {
        // 根据业务码区分：40101=未登录，40102=密码错误，40103=登录失效
        const bizCode = body?.code
        const userStore = useUserStore()

        if (bizCode === ErrorCode.NOT_LOGGED_IN) {
          // 未登录 → 清登录态，静默跳转登录页，不弹提示
          userStore.clearToken()
          router.push('/login')
        } else if (bizCode === ErrorCode.AUTH_FAILED) {
          // 用户名或密码错误 → 清登录态，跳转登录页
          userStore.clearToken()
          router.push('/login')
          ElMessage.warning({ message: body?.msg || body?.message || ERROR_DEFAULT_MSG[ErrorCode.AUTH_FAILED], showClose: true, duration: 5000 })
        } else if (bizCode === ErrorCode.LOGIN_EXPIRED) {
          // 登录已失效 → 清登录态，跳转登录页
          userStore.clearToken()
          router.push('/login')
          ElMessage.error({ message: body?.msg || body?.message || ERROR_DEFAULT_MSG[ErrorCode.LOGIN_EXPIRED], showClose: true, duration: 5000 })
        } else if (bizCode === ErrorCode.CAPTCHA_EXPIRED) {
          // 验证码过期/无效 → 不清除登录态，不跳转，由调用方自行处理
          return Promise.reject(new BusinessError(bizCode, body?.msg || body?.message || ERROR_DEFAULT_MSG[ErrorCode.CAPTCHA_EXPIRED]))
        } else {
          // 未知业务码的 401 → 清登录态，静默跳转
          userStore.clearToken()
          router.push('/login')
        }
      } else if (httpStatus === 403) {
        // 权限不足 → 跳转 403 页面
        ElMessage.warning({ message: body?.msg || body?.message || ERROR_DEFAULT_MSG[ErrorCode.FORBIDDEN], showClose: true, duration: 5000 })
        router.push('/forbidden')
      } else if (httpStatus === 404) {
        // 资源不存在 → 跳转 404 页面，展示后端错误信息
        const errMsg = body?.msg || body?.message || ERROR_DEFAULT_MSG[ErrorCode.NOT_FOUND]
        router.push({ name: 'not-found', query: { message: errMsg } })
      } else if (httpStatus >= 500) {
        ElMessage.error({ message: body?.msg || body?.message || ERROR_DEFAULT_MSG[ErrorCode.SYSTEM_ERROR], showClose: true, duration: 5000 })
      } else {
        const errMsg = body?.msg || body?.message || `请求失败 (${httpStatus})`
        ElMessage.warning({ message: errMsg, showClose: true, duration: 5000 })
      }
    } else if (error.request) {
      // 请求发出但无响应（网络断开/超时）
      ElMessage.error({ message: '网络错误，请检查网络连接', showClose: true, duration: 5000 })
    } else {
      ElMessage.warning({ message: error.message || '操作失败', showClose: true, duration: 5000 })
    }

    return Promise.reject(error)
  }
)

export default request