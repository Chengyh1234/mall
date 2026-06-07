<template>
  <div class="cs-home">
    <div class="cs-header">
      <h1>客服中心</h1>
      <p>欢迎回来，{{ userStore.userInfo?.realName || '客服' }}</p>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon pending">📋</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingTickets }}</div>
          <div class="stat-label">待处理工单</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon today">✅</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayHandled }}</div>
          <div class="stat-label">今日处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon rating">⭐</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.customerRating }}</div>
          <div class="stat-label">客户评分</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon avg">️</div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.avgResponse }}</div>
          <div class="stat-label">平均响应</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="goTo('/cs/orders')">
          <div class="action-icon">📦</div>
          <span>订单查询</span>
        </div>
        <div class="action-card">
          <div class="action-icon">💬</div>
          <span>在线客服</span>
        </div>
        <div class="action-card">
          <div class="action-icon">📝</div>
          <span>处理工单</span>
        </div>
      </div>
    </div>

    <div class="recent-work">
      <h2>最近处理</h2>
      <el-table :data="recentCases" border>
        <el-table-column prop="caseNo" label="工单号" />
        <el-table-column prop="customerName" label="客户" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="content" label="问题描述" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'resolved' ? 'success' : 'warning'">
              {{ scope.row.status === 'resolved' ? '已解决' : '处理中' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  pendingTickets: 0,
  todayHandled: 0,
  customerRating: '-',
  avgResponse: '-'
})

const recentCases = ref<any[]>([])

const goTo = (path: string) => {
  router.push(path)
}

onMounted(() => {
})
</script>

<style scoped>
.cs-home {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.cs-header {
  margin-bottom: 30px;
}

.cs-header h1 {
  font-size: 28px;
  margin: 0 0 10px 0;
  color: #333;
}

.cs-header p {
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

.stat-icon.pending { background: #fff7e6; }
.stat-icon.today { background: #f6ffed; }
.stat-icon.rating { background: #f9f0ff; }
.stat-icon.avg { background: #e6f7ff; }

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

.recent-work {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.recent-work h2 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #333;
}
</style>
