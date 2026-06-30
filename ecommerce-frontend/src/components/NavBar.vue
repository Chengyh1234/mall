<template>
  <el-header class="header">
    <div class="header-content">
      <div class="header-left">
        <button v-if="showBack" class="back-btn" @click="handleBack">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
          <span>返回</span>
        </button>
        <div class="logo" @click="goHome">
          <span class="logo-text">电商商城</span>
        </div>
      </div>

      <!-- 移动端菜单切换 -->
      <button class="mobile-menu-toggle" @click="mobileMenuOpen = !mobileMenuOpen" aria-label="菜单">
        <!-- 汉堡图标 -->
        <svg v-if="!mobileMenuOpen" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <line x1="3" y1="6" x2="21" y2="6"/>
          <line x1="3" y1="12" x2="21" y2="12"/>
          <line x1="3" y1="18" x2="21" y2="18"/>
        </svg>
        <!-- 关闭图标 -->
        <svg v-else viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <line x1="6" y1="6" x2="18" y2="18"/>
          <line x1="18" y1="6" x2="6" y2="18"/>
        </svg>
      </button>

      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        @select="handleMenuSelect"
        class="nav-menu"
        :class="{ 'mobile-open': mobileMenuOpen }"
      >
        <el-menu-item index="home">首页</el-menu-item>
        <el-menu-item index="seller">卖家中心</el-menu-item>
        <el-menu-item index="shop-apply">免费开店</el-menu-item>
        <el-menu-item index="cart">购物车</el-menu-item>
        <el-menu-item index="profile">个人中心</el-menu-item>
        <el-menu-item v-if="isLoggedIn && !isAdmin" index="logout">退出登录</el-menu-item>
        <el-menu-item v-if="!isLoggedIn || isAdmin" index="login">登录</el-menu-item>
      </el-menu>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  showBack?: boolean
}>()

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const mobileMenuOpen = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)

const activeMenu = computed(() => {
  const path = route.path
  if (path === '/home' || path === '/') return 'home'
  if (path === '/cart') return 'cart'
  if (path === '/profile') return 'profile'
  if (path.startsWith('/admin')) return 'admin'
  if (path.startsWith('/seller')) return 'seller'
  return ''
})

const goHome = () => {
  router.push('/home')
  mobileMenuOpen.value = false
}

const handleBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/products?searchType=shop')
  }
}

const handleMenuSelect = async (index: string) => {
  mobileMenuOpen.value = false
  switch (index) {
    case 'home':
      router.push('/home')
      break
    case 'admin':
      window.location.href = '/admin/login'
      break
    case 'seller':
      if (!isLoggedIn.value) { router.push('/login'); return }
      // 确保用户信息已加载，再判断是否有卖家身份
      await userStore.fetchUserInfo()
      if (!userStore.isSeller) {
        ElMessage.warning('您尚未开通卖家身份，请先申请开店')
        router.push('/shop/apply')
        return
      }
      router.push({ name: 'seller' })
      break
    case 'cart':
      if (!isLoggedIn.value) { router.push('/login'); return }
      router.push('/cart')
      break
    case 'profile':
      if (!isLoggedIn.value) { router.push('/login'); return }
      if (isAdmin.value) { router.push('/forbidden'); return }
      router.push('/profile')
      break
    case 'shop-apply':
      if (!isLoggedIn.value) { router.push('/login'); return }
      router.push('/shop/apply')
      break
    case 'login':
      router.push('/login')
      break
    case 'logout':
      try {
        await userStore.logout()
        ElMessage.success('已安全退出')
        router.push('/login')
      } catch { /* ignore */ }
      break
  }
}
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: var(--z-sticky);
  background: var(--surface);
  box-shadow: var(--shadow-sm);
  padding: 0;
  width: 100%;
  height: var(--header-height);
  border-bottom: 1px solid var(--border-light);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-8);
  width: 100%;
  height: 100%;
}

/* ---------- 左侧 ---------- */
.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  height: 100%;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  border: none;
  background: transparent;
  color: var(--ink-muted);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  line-height: 1;
}

.back-btn:hover {
  background: var(--surface-muted);
  color: var(--ink);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  padding: var(--space-1) 0;
  color: var(--color-brand-500);
  transition: opacity var(--transition-fast);
}

.logo:hover {
  opacity: 0.85;
}

.logo-text {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.02em;
}

.logo-icon {
  color: var(--color-brand-500);
  flex-shrink: 0;
}

/* ---------- 导航菜单 ---------- */
.nav-menu {
  border-bottom: none;
  margin-left: auto;
  background: transparent;
}

.nav-menu :deep(.el-menu-item) {
  padding: 0 var(--space-5);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--ink-muted);
  height: var(--header-height);
  line-height: var(--header-height);
  transition: color var(--transition-fast);
  border-bottom: 2px solid transparent;
}

.nav-menu :deep(.el-menu-item:hover) {
  color: var(--ink);
  background: transparent;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: var(--color-brand-500);
  border-bottom-color: var(--color-brand-500);
}

/* ---------- 移动端菜单切换 ---------- */
.mobile-menu-toggle {
  display: none;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--ink);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.mobile-menu-toggle:hover {
  background: var(--surface-muted);
}

/* ---------- 响应式 ---------- */
@media (max-width: 768px) {
  .header-content {
    padding: 0 var(--space-4);
  }

  .mobile-menu-toggle {
    display: inline-flex;
  }

  .nav-menu {
    display: none;
    position: fixed;
    top: var(--header-height);
    left: 0;
    right: 0;
    background: var(--surface);
    border-bottom: 1px solid var(--border);
    box-shadow: var(--shadow-md);
    flex-direction: column;
    z-index: var(--z-overlay);
  }

  .nav-menu.mobile-open {
    display: flex;
  }

  .nav-menu :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    border-bottom: 1px solid var(--border-light);
    padding: 0 var(--space-6);
  }

  .nav-menu :deep(.el-menu-item.is-active) {
    border-bottom: 1px solid var(--border-light);
    color: var(--color-brand-500);
    background: var(--color-brand-50);
  }

  .logo-text {
    font-size: var(--text-lg);
  }
}
</style>