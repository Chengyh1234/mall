<template>
  <div class="admin-home">
    <div class="admin-header">
      <h1>管理后台</h1>
      <p>欢迎回来，{{ userStore.userInfo?.realName || '管理员' }}</p>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon users">👥</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orders">📦</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayOrders }}</div>
          <div class="stat-label">今日订单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon products">📱</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalProducts }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon sales">💰</div>
        <div class="stat-info">
          <div class="stat-value">¥{{ stats.todaySales }}</div>
          <div class="stat-label">今日销售额</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon sellers">🏪</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalSellers }}</div>
          <div class="stat-label">入驻卖家</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon cs">💬</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.onlineCs }}</div>
          <div class="stat-label">在线客服</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="goTo('/admin/users')">
          <div class="action-icon">👥</div>
          <span>用户管理</span>
        </div>
        <div class="action-card" @click="goTo('/admin/products')">
          <div class="action-icon">📱</div>
          <span>商品管理</span>
        </div>
        <div class="action-card" @click="goTo('/admin/orders')">
          <div class="action-icon">📋</div>
          <span>订单管理</span>
        </div>
        <div class="action-card" @click="goTo('/admin/banners')">
          <div class="action-icon">🖼️</div>
          <span>轮播图管理</span>
        </div>
        <div class="action-card" @click="goTo('/admin/attributes')">
          <div class="action-icon">🏷️</div>
          <span>属性管理</span>
        </div>
        <div class="action-card" @click="goTo('/admin/category-attributes')">
          <div class="action-icon">🔗</div>
          <span>分类属性绑定</span>
        </div>
        <div class="action-card" @click="goTo('/admin/categories')">
          <div class="action-icon">📂</div>
          <span>分类管理</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  totalUsers: 0,
  todayOrders: 0,
  totalProducts: 0,
  todaySales: '0.00',
  totalSellers: 0,
  onlineCs: 0
})

const goTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
})
</script>

<style scoped>
.admin-home {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.admin-header {
  margin-bottom: 30px;
}

.admin-header h1 {
  font-size: 28px;
  margin: 0 0 10px 0;
  color: #333;
}

.admin-header p {
  margin: 0;
  color: #666;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 36px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.stat-icon.users { background: #e6f7ff; }
.stat-icon.orders { background: #f6ffed; }
.stat-icon.products { background: #fff7e6; }
.stat-icon.sales { background: #f9f0ff; }
.stat-icon.sellers { background: #fff0f6; }
.stat-icon.cs { background: #e6fffb; }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.quick-actions {
  margin-bottom: 30px;
}

.quick-actions h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #333;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.action-card {
  background: white;
  padding: 25px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
}

.action-icon {
  font-size: 32px;
}

.action-card span {
  font-size: 14px;
  color: #666;
}
</style>
