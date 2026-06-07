import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

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
      if (config.headers) {
        config.headers['Content-Type'] = 'application/json;charset=UTF-8'
      }
    }
    
    return config
  },
  (error: any) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 处理统一响应格式 { code, msg, data }
    const res = response.data

    // 如果不是标准响应格式，直接返回
    if (!res || typeof res.code === 'undefined') {
      return res
    }

    // 处理401未登录
    if (res.code === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error(res.msg || '登录已过期，请重新登录')
      return Promise.reject(new Error(res.msg || 'Error'))
    }

    // 处理403权限不足
    if (res.code === 403) {
      ElMessage.error(res.msg || '权限不足')
      return Promise.reject(new Error(res.msg || 'Error'))
    }

    // 处理其他错误
    if (res.code !== 200) {
      ElMessage.warning(res.msg || res.message || '请求失败')
      return Promise.reject(new Error(res.msg || res.message || 'Error'))
    }

    // 返回data字段
    return res.data
  },
  (error: any) => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem('token')
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      } else if (error.response.status === 403) {
        ElMessage.error('权限不足')
      } else {
        // 处理其他HTTP错误状态码
        const errorMsg = error.response.data?.msg || error.response.data?.message || '操作失败'
        ElMessage.warning(errorMsg)
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.warning(error.message || '操作失败')
    }
    return Promise.reject(error)
  }
)

export default request