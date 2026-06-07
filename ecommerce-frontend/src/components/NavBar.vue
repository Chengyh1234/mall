<template>
  <el-header class="header">
    <div class="header-content">
      <div class="logo" @click="goHome">
        <h2>电商商城</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        mode="horizontal"
        @select="handleMenuSelect"
        class="nav-menu"
      >
        <el-menu-item index="home">首页</el-menu-item>
        
        <template v-if="isLoggedIn">
          
          <el-menu-item v-if="userStore.isSeller" index="seller">卖家中心</el-menu-item>
          <el-menu-item v-if="userStore.hasRole('USER')" index="cart">购物车</el-menu-item>
          <el-menu-item index="profile">个人中心</el-menu-item>
          <el-menu-item index="logout">退出</el-menu-item>
        </template>
        
        <el-menu-item v-else index="login">登录</el-menu-item>
      </el-menu>
    </div>
  </el-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)

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
}

const handleMenuSelect = async (index: string) => {
  switch (index) {
    case 'home':
      router.push('/home')
      break
    case 'admin':
      window.location.href = '/admin/login'
      break
    case 'seller':
      router.push({ name: 'seller' })
      break
    case 'cart':
      router.push('/cart')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'login':
      router.push('/login')
      break
    case 'logout':
      try {
        await userStore.logout()
        ElMessage.success('退出登录成功')
        router.push('/login')
      } catch {
      }
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
  z-index: 999;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 0;
  width: 100%;
  height: 60px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  width: 100%;
  height: 100%;
}

.logo {
  flex-shrink: 0;
  cursor: pointer;
}

.logo h2 {
  margin: 0;
  color: #ff4400;
  font-size: 26px;
  font-weight: 700;
}

.nav-menu {
  border-bottom: none;
  margin-left: auto;
  display: flex;
  gap: 15px;
}

.nav-menu :deep(.el-menu-item) {
  padding: 0 20px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #ff4400;
  border-bottom: 2px solid #ff4400;
}
</style>