import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, logout as logoutApi, type Role } from '@/api/auth'

// 用户角色枚举 - 保持向后兼容
export enum UserRole {
  USER = 'USER',                           // 普通用户
  SELLER = 'SELLER',                       // 商家
  ADMIN = 'ADMIN',                         // 管理员
  SUPER_ADMIN = 'SUPER_ADMIN',             // 超级管理员
  OPERATOR = 'OPERATOR',                   // 运营管理员
  CUSTOMER_SERVICE = 'CUSTOMER_SERVICE',   // 客服
  // 保持旧的角色名称向后兼容
  ROLE_USER = 'ROLE_USER',
  ROLE_SELLER = 'ROLE_SELLER',
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_OPERATOR = 'ROLE_OPERATOR',
  ROLE_CUSTOMER_SERVICE = 'ROLE_CUSTOMER_SERVICE'
}

export interface UserInfo {
  id?: number
  username?: string
  realName?: string
  phone?: string
  email?: string
  avatar?: string
  status?: number
  lastLoginTime?: string
  createdAt?: string
  roles?: Role[]
  permissions?: string[]
}

// 缓存有效期（5分钟）
const CACHE_EXPIRE_MS = 5 * 60 * 1000

// localStorage 中角色信息存储 Key
const ROLES_STORAGE_KEY = 'user_roles'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)
  
  // 缓存相关
  const lastFetchTime = ref<number>(0)
  const isFetching = ref(false)

  // 设置token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 清除token
  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem(ROLES_STORAGE_KEY)
    userInfo.value = null
    lastFetchTime.value = 0
  }

  // 设置用户信息
  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info
    lastFetchTime.value = Date.now()
    // 持久化角色信息，刷新页面后恢复
    if (info.roles && info.roles.length > 0) {
      localStorage.setItem(ROLES_STORAGE_KEY, JSON.stringify(info.roles))
    } else {
      localStorage.removeItem(ROLES_STORAGE_KEY)
    }
  }

  // 判断是否需要刷新用户信息
  const needRefresh = (): boolean => {
    if (!token.value) return false
    if (!userInfo.value) return true
    const now = Date.now()
    return now - lastFetchTime.value > CACHE_EXPIRE_MS
  }

  // 获取用户信息（带缓存机制）
  const fetchUserInfo = async (forceRefresh = false) => {
    // 如果正在请求，等待之前的请求完成
    if (isFetching.value && !forceRefresh) {
      return userInfo.value
    }
    
    // 如果不需要刷新且已有用户信息，直接返回缓存
    if (!forceRefresh && !needRefresh() && userInfo.value) {
      return userInfo.value
    }
    
    try {
      isFetching.value = true
      const data = await getUserInfo()
      const userData = data as UserInfo
      
      // 尝试从 localStorage 恢复角色信息（登录时持久化的）
      const savedRoles = localStorage.getItem(ROLES_STORAGE_KEY)
      if (savedRoles) {
        try {
          const parsed = JSON.parse(savedRoles)
          if (Array.isArray(parsed) && parsed.length > 0) {
            userData.roles = parsed
          }
        } catch {
          // JSON 解析失败，忽略
        }
      }
      
      // 如果仍然没有角色信息，默认设置为普通用户角色
      if (!userData.roles || userData.roles.length === 0) {
        userData.roles = [{
          id: null,
          name: '普通用户',
          code: UserRole.USER,
          description: null,
          status: null,
          createdAt: null,
          updatedAt: null
        }]
      }
      
      userInfo.value = userData
      lastFetchTime.value = Date.now()
      return data
    } catch (error) {
      throw error
    } finally {
      isFetching.value = false
    }
  }

  // 退出登录
  const logout = async () => {
    try {
      await logoutApi()
    } catch {
    } finally {
      // 无论接口是否成功，都清除本地token
      clearToken()
    }
  }

  // 检查是否有指定角色（同时检查带ROLE_前缀和不带前缀的版本）
  const hasRole = (role: string): boolean => {
    if (!userInfo.value?.roles) return false
    const roleCode = role.toUpperCase()
    // 检查原始角色和带ROLE_前缀的角色
    return userInfo.value.roles.some(r => {
      // 防御性检查：确保 r.code 存在且为字符串
      if (!r || typeof r.code !== 'string') return false
      const rCode = r.code.toUpperCase()
      return rCode === roleCode || rCode === `ROLE_${roleCode}` || rCode.replace('ROLE_', '') === roleCode
    })
  }

  // 检查是否有任意指定角色（使用hasRole确保前缀匹配）
  const hasAnyRole = (roles: string[]): boolean => {
    if (!userInfo.value?.roles) return false
    return roles.some(role => hasRole(role))
  }

  // 是否是管理员（超级用户或管理员或运营）
  const isAdmin = computed(() => {
    return hasRole(UserRole.SUPER_ADMIN) || hasRole(UserRole.ADMIN) || hasRole(UserRole.OPERATOR) ||
           hasRole(UserRole.ROLE_ADMIN) || hasRole(UserRole.ROLE_OPERATOR)
  })

  // 是否是卖家（增加用户名判断作为备选）
  const isSeller = computed(() => {
    const hasSellerRole = hasRole(UserRole.SELLER) || hasRole(UserRole.ROLE_SELLER)
    const username = userInfo.value?.username?.toLowerCase() || ''
    const isSellerUsername = username.includes('seller')
    return hasSellerRole || isSellerUsername
  })

  // 是否是客服
  const isCustomerService = computed(() => {
    return hasRole(UserRole.CUSTOMER_SERVICE) || hasRole(UserRole.ROLE_CUSTOMER_SERVICE)
  })

  // 是否是普通用户（所有登录用户都拥有普通用户功能）
  const isNormalUser = computed(() => {
    return isLoggedIn.value
  })

  // 是否仅为普通用户（只有用户角色，没有其他角色）
  const isOnlyNormalUser = computed(() => {
    if (!userInfo.value?.roles || userInfo.value.roles.length === 0) return false
    const firstRole = userInfo.value.roles[0]
    if (!firstRole || typeof firstRole.code !== 'string') return false
    return userInfo.value.roles.length === 1 && 
           (firstRole.code === UserRole.USER || firstRole.code === UserRole.ROLE_USER)
  })

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isSeller,
    isCustomerService,
    isNormalUser,
    isOnlyNormalUser,
    setToken,
    clearToken,
    setUserInfo,
    fetchUserInfo,
    logout,
    hasRole,
    hasAnyRole,
    needRefresh
  }
})
