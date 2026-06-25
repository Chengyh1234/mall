<template>
  <div class="admin-layout">
    <div class="admin-body">
      <aside class="admin-sidebar">
        <div class="sidebar-header">运营管理</div>
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
          background-color="#1e1e2d"
          text-color="#a2a3b7"
          active-text-color="#fff"
        >
          <el-menu-item index="/admin">
            <span class="menu-icon">📊</span>
            <span>概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/banners">
            <span class="menu-icon">🖼️</span>
            <span>轮播图管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/attributes">
            <span class="menu-icon">🏷️</span>
            <span>属性管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category-attributes">
            <span class="menu-icon">🔗</span>
            <span>分类属性绑定</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <span class="menu-icon">📂</span>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/products">
            <span class="menu-icon">📱</span>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/orders">
            <span class="menu-icon">📦</span>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/shops">
            <span class="menu-icon">🏪</span>
            <span>店铺管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <span class="menu-icon">👥</span>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
        <div class="sidebar-footer">
          <el-button class="logout-btn" @click="handleLogout">退出登录</el-button>
        </div>
      </aside>
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return route.path
})

const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch {
    // ignore
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f0f2f5;
}

.admin-body {
  display: flex;
  flex: 1;
}

.admin-sidebar {
  width: 220px;
  height: 100vh;
  background: #1e1e2d;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  z-index: 10;
}

.sidebar-header {
  height: 50px;
  line-height: 50px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.05);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.sidebar-menu {
  border-right: none !important;
  flex: 1;
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
}

.sidebar-menu .el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.06) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.1) !important;
  border-right: 3px solid #409eff;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.logout-btn {
  width: 100%;
  color: #a2a3b7;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn:hover {
  color: #fff;
  background: rgba(255, 77, 79, 0.2);
  border-color: rgba(255, 77, 79, 0.4);
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}

.admin-content {
  flex: 1;
  padding: 20px;
  margin-left: 220px;
  overflow-x: hidden;
}
</style>