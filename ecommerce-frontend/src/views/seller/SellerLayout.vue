<template>
  <div class="seller-layout">
    <NavBar />
    <div class="seller-body">
      <aside class="seller-sidebar">
        <div class="sidebar-header">卖家中心</div>
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
          background-color="#1e1e2d"
          text-color="#a2a3b7"
          active-text-color="#fff"
        >
          <el-menu-item index="/seller">
            <span class="menu-icon">📊</span>
            <span>卖家中心</span>
          </el-menu-item>
          <el-menu-item index="/seller/products">
            <span class="menu-icon">📱</span>
            <span>商品管理</span>
          </el-menu-item>
          <el-menu-item index="/seller/orders">
            <span class="menu-icon">📋</span>
            <span>订单管理</span>
          </el-menu-item>
          <el-menu-item index="/seller/profile">
            <span class="menu-icon">⚙️</span>
            <span>店铺设置</span>
          </el-menu-item>
        </el-menu>
      </aside>
      <main class="seller-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from '@/components/NavBar.vue'

const route = useRoute()

const activeMenu = computed(() => {
  return route.path
})
</script>

<style scoped>
.seller-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f0f2f5;
}

.seller-body {
  display: flex;
  flex: 1;
  margin-top: 60px;
}

.seller-sidebar {
  width: 220px;
  min-height: calc(100vh - 60px);
  background: #1e1e2d;
  flex-shrink: 0;
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
}

.sidebar-menu {
  border-right: none !important;
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

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}

.seller-content {
  flex: 1;
  padding: 20px;
  overflow-x: hidden;
}
</style>