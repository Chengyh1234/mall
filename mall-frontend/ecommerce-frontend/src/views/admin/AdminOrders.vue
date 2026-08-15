<template>
  <div class="admin-orders">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">订单管理</h1>
          <p class="welcome-desc">全平台订单一览，查看所有用户订单信息。</p>
        </div>
      </div>
    </section>

    <!-- 筛选工具栏 -->
    <section class="list-card">
      <div class="filter-body">
        <div class="status-filter-section">
          <span class="status-filter-label">订单状态</span>
          <div class="status-pills">
            <button
              class="status-pill"
              :class="{ active: filters.status === undefined }"
              @click="filters.status = undefined; handleSearch()"
            >
              全部
            </button>
            <button
              v-for="s in statusList"
              :key="s.value"
              class="status-pill"
              :class="{ active: filters.status === s.value }"
              @click="filters.status = s.value; handleSearch()"
            >
              <span class="status-dot" :class="'status-dot--' + s.tag" />
              {{ s.text }}
            </button>
          </div>
        </div>

        <div class="filter-divider" />

        <div class="filter-row">
          <div class="filter-field">
            <label>用户ID</label>
            <div class="filter-input-wrap">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              <input
                v-model="filters.userId"
                type="text"
                placeholder="精确匹配"
                @keyup.enter="handleSearch"
              />
            </div>
          </div>

          <div class="filter-field filter-field--grow">
            <label>订单号</label>
            <div class="filter-input-wrap">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              <input
                v-model="filters.orderNo"
                type="text"
                placeholder="模糊搜索订单号"
                @keyup.enter="handleSearch"
              />
            </div>
          </div>

          <div class="filter-actions">
            <button class="primary-btn primary-btn--gold" @click="handleSearch">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              查询
            </button>
            <button class="secondary-btn" @click="handleReset">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="23 4 23 10 17 10"/>
                <polyline points="1 20 1 14 7 14"/>
                <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
              </svg>
              重置
            </button>
          </div>
        </div>

        <div class="filter-divider" />

        <div class="filter-row filter-row--time">
          <div class="filter-field">
            <label>支付时间</label>
            <el-date-picker
              v-model="payTimeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="起始"
              end-placeholder="结束"
              class="time-picker"
              :teleported="false"
              @change="handleSearch"
            />
          </div>
          <div class="filter-field">
            <label>发货时间</label>
            <el-date-picker
              v-model="deliveryTimeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="起始"
              end-placeholder="结束"
              class="time-picker"
              :teleported="false"
              @change="handleSearch"
            />
          </div>
          <div class="filter-field">
            <label>收货时间</label>
            <el-date-picker
              v-model="receiveTimeRange"
              type="datetimerange"
              value-format="YYYY-MM-DD HH:mm:ss"
              range-separator="至"
              start-placeholder="起始"
              end-placeholder="结束"
              class="time-picker"
              :teleported="false"
              @change="handleSearch"
            />
          </div>
        </div>
      </div>
    </section>

    <!-- 订单表格 -->
    <section class="list-card">
      <div class="list-card__header">
        <div class="list-card__title">
          <h2>订单列表</h2>
          <span>共 {{ pagination.total }} 单</span>
        </div>
        <button class="icon-btn" title="刷新" @click="loadOrders">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
        </button>
      </div>

      <div class="table-container" v-loading="loading">
        <el-table
          :data="orders"
          style="width: 100%"
          :header-cell-style="headerCellStyle"
          :cell-style="cellStyle"
          row-class-name="order-row"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="orderNo" label="订单号" width="180" align="center" />
          <el-table-column prop="userId" label="用户ID" width="80" align="center" />
          <el-table-column label="总金额" width="100" align="center">
            <template #default="scope">
              <span class="amount-text">¥{{ scope.row.totalAmount?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="支付金额" width="100" align="center">
            <template #default="scope">
              <span class="pay-text">¥{{ scope.row.payAmount?.toFixed(2) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="订单状态" width="100" align="center">
            <template #default="scope">
              <span class="status-badge" :class="'status-badge--' + statusMap[scope.row.status]?.tag">
                <span class="status-dot" :class="'status-dot--' + statusMap[scope.row.status]?.tag" />
                {{ statusText(scope.row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="90" align="center">
            <template #default="scope">
              <span class="pay-status" :class="scope.row.payStatus === 1 ? 'pay-status--paid' : 'pay-status--unpaid'">
                {{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="receiverName" label="收货人" width="100" align="center" />
          <el-table-column prop="receiverPhone" label="联系电话" width="130" align="center" />
          <el-table-column label="收货地址" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <span class="address-text">{{ scope.row.receiverAddress }}</span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="155" align="center">
            <template #default="scope">
              <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90" align="center" fixed="right">
            <template #default="scope">
              <button class="action-icon-btn action-icon-btn--view" title="查看详情" @click="showDetail(scope.row)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                详情
              </button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </section>

    <!-- 订单详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="'订单详情 - ' + (currentOrder?.orderNo || '')"
      width="680px"
      destroy-on-close
      class="order-detail-dialog"
    >
      <div v-if="currentOrder" class="detail-body">
        <div class="detail-hero">
          <div class="detail-status-ring" :class="'detail-status-ring--' + statusMap[currentOrder.status]?.tag">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
          </div>
          <div class="detail-hero-info">
            <div class="detail-hero-status">
              <span class="status-badge" :class="'status-badge--' + statusMap[currentOrder.status]?.tag">
                <span class="status-dot" :class="'status-dot--' + statusMap[currentOrder.status]?.tag" />
                {{ statusText(currentOrder.status) }}
              </span>
              <span class="pay-status" :class="currentOrder.payStatus === 1 ? 'pay-status--paid' : 'pay-status--unpaid'">
                {{ currentOrder.payStatus === 1 ? '已支付' : '未支付' }}
              </span>
            </div>
            <div class="detail-hero-amount">
              <span class="detail-amount-label">支付金额</span>
              <span class="detail-amount-value">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="detail-section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
            订单信息
          </h3>
          <div class="detail-grid">
            <div class="detail-card">
              <span class="detail-card__label">订单号</span>
              <span class="detail-card__value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">用户ID</span>
              <span class="detail-card__value">{{ currentOrder.userId }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">总金额</span>
              <span class="detail-card__value">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">支付金额</span>
              <span class="detail-card__value amount">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">创建时间</span>
              <span class="detail-card__value">{{ formatTime(currentOrder.createdAt) }}</span>
            </div>
            <div class="detail-card detail-card--wide" v-if="currentOrder.remark">
              <span class="detail-card__label">买家备注</span>
              <span class="detail-card__value">{{ currentOrder.remark }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="detail-section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            收货信息
          </h3>
          <div class="detail-grid">
            <div class="detail-card">
              <span class="detail-card__label">收货人</span>
              <span class="detail-card__value">{{ currentOrder.receiverName }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">联系电话</span>
              <span class="detail-card__value">{{ currentOrder.receiverPhone }}</span>
            </div>
            <div class="detail-card detail-card--wide">
              <span class="detail-card__label">收货地址</span>
              <span class="detail-card__value">{{ currentOrder.receiverAddress }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-skeleton :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAdminOrderList, type Order } from '@/api/order'

const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const filters = reactive({
  status: undefined as number | undefined,
  userId: '',
  orderNo: ''
})
const payTimeRange = ref<[string, string] | null>(null)
const deliveryTimeRange = ref<[string, string] | null>(null)
const receiveTimeRange = ref<[string, string] | null>(null)

const statusMap: Record<number, { text: string; tag: string }> = {
  1: { text: '待付款', tag: 'warning' },
  2: { text: '待发货', tag: 'danger' },
  3: { text: '待收货', tag: 'primary' },
  4: { text: '已完成', tag: 'success' },
  5: { text: '已取消', tag: 'neutral' },
  6: { text: '退款中', tag: 'danger' },
  7: { text: '已退款', tag: 'success' },
  8: { text: '已拒绝', tag: 'danger' }
}

const statusList = Object.entries(statusMap).map(([value, item]) => ({
  value: Number(value),
  ...item
}))

const statusText = (status: number) => statusMap[status]?.text || '未知'

const loadOrders = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (filters.status !== undefined) params.status = filters.status
    if (filters.userId) params.userId = Number(filters.userId)
    if (filters.orderNo) params.orderNo = filters.orderNo
    if (payTimeRange.value) {
      params.payTimeStart = payTimeRange.value[0]
      params.payTimeEnd = payTimeRange.value[1]
    }
    if (deliveryTimeRange.value) {
      params.deliveryTimeStart = deliveryTimeRange.value[0]
      params.deliveryTimeEnd = deliveryTimeRange.value[1]
    }
    if (receiveTimeRange.value) {
      params.receiveTimeStart = receiveTimeRange.value[0]
      params.receiveTimeEnd = receiveTimeRange.value[1]
    }
    const result = await getAdminOrderList(params)
    orders.value = result.list || []
    pagination.total = result.total || 0
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadOrders()
}

const handleReset = () => {
  filters.status = undefined
  filters.userId = ''
  filters.orderNo = ''
  payTimeRange.value = null
  deliveryTimeRange.value = null
  receiveTimeRange.value = null
  pagination.page = 1
  loadOrders()
}

const showDetail = (row: Order) => {
  currentOrder.value = row
  detailVisible.value = true
}

const formatTime = (time: string | undefined) => {
  if (!time) return '-'
  try {
    const d = new Date(time)
    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return time
  }
}

const headerCellStyle = () => ({
  background: '#F7F7F5',
  color: '#1C1C1E',
  fontWeight: 700,
  fontSize: '12px',
  borderBottom: '1px solid #E8E8E6',
  padding: '14px 0'
})

const cellStyle = () => ({
  borderBottom: '1px solid #F0F0EE',
  padding: '14px 0'
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-orders {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 24px;
  color: #1C1C1E;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===========================
   Bento 顶部
   =========================== */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px;
}

.welcome-card {
  grid-column: span 12;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px 28px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.welcome-desc {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.status-filter-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.status-filter-label {
  font-size: 12px;
  font-weight: 700;
  color: #6B6B6E;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  padding-top: 9px;
  white-space: nowrap;
}

.status-pills {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: #4B4B4E;
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  padding: 8px 14px;
  border-radius: 22px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.status-pill:hover {
  border-color: #C8A464;
  color: #1C1C1E;
  background: #FDFCFA;
  box-shadow: 0 2px 6px rgba(200, 164, 100, 0.1);
}

.status-pill.active {
  background: rgba(200, 164, 100, 0.12);
  border-color: rgba(200, 164, 100, 0.5);
  color: #7A5C22;
  box-shadow: 0 2px 6px rgba(200, 164, 100, 0.12);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot--warning { background: #f59e0b; }
.status-dot--danger  { background: #ef4444; }
.status-dot--primary { background: #3B6E6E; }
.status-dot--success { background: #22c55e; }
.status-dot--neutral { background: #9CA3AF; }

/* ===========================
   列表卡片
   =========================== */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.list-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #F0F0EE;
  flex-wrap: wrap;
}

.list-card__title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.list-card__title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.list-card__title span {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

/* ===========================
   筛选区
   =========================== */
.filter-body {
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-row--time {
  align-items: flex-start;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-field--grow {
  flex: 1;
  min-width: 180px;
}

.filter-field label {
  font-size: 11px;
  font-weight: 700;
  color: #6B6B6E;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.filter-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.filter-input-wrap > svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #A1A1AA;
  pointer-events: none;
}

.filter-input-wrap input {
  width: 100%;
  min-width: 160px;
  height: 36px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  padding: 0 12px 0 36px;
  font-size: 13px;
  color: #1C1C1E;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: #FFFFFF;
}

.filter-input-wrap input:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.1);
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary-btn--gold {
  background: linear-gradient(135deg, #C8A464 0%, #B08B45 100%);
  color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(200, 164, 100, 0.25);
}

.primary-btn--gold:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(200, 164, 100, 0.35);
}

.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  background: #FFFFFF;
  color: #6B6B6E;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

.filter-divider {
  height: 1px;
  background: linear-gradient(to right, #F0F0EE 0%, transparent 100%);
}

.time-picker {
  width: 280px;
}

:deep(.time-picker .el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E8E8E6 inset;
}

:deep(.time-picker .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #C8A464 inset, 0 0 0 3px rgba(200, 164, 100, 0.1);
}

:deep(.time-picker .el-range-input) {
  font-size: 12px;
  color: #1C1C1E;
}

/* ===========================
   表格
   =========================== */
.table-container {
  padding: 0 24px;
}

.amount-text {
  color: #3B6E6E;
  font-weight: 700;
  font-size: 13px;
}

.pay-text {
  color: #C8A464;
  font-weight: 700;
  font-size: 13px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.status-badge--warning { color: #b45309; background: rgba(245, 158, 11, 0.12); }
.status-badge--danger  { color: #b91c1c; background: rgba(239, 68, 68, 0.1); }
.status-badge--primary { color: #1e4d4d; background: rgba(59, 110, 110, 0.12); }
.status-badge--success { color: #15803d; background: rgba(34, 197, 94, 0.12); }
.status-badge--neutral { color: #4b5563; background: rgba(156, 163, 175, 0.14); }

.pay-status {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.pay-status--paid {
  color: #15803d;
  background: rgba(34, 197, 94, 0.12);
}

.pay-status--unpaid {
  color: #6B7280;
  background: #F3F4F6;
}

.address-text {
  font-size: 13px;
  color: #4B4B4E;
}

.time-text {
  font-size: 12px;
  color: #909399;
}

.action-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 30px;
  padding: 0 10px;
  border-radius: 15px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-icon-btn--view {
  color: #3B6E6E;
  border-color: rgba(59, 110, 110, 0.3);
  background: rgba(59, 110, 110, 0.06);
}

.action-icon-btn--view:hover {
  background: rgba(59, 110, 110, 0.12);
  border-color: rgba(59, 110, 110, 0.5);
}

:deep(.order-row:hover > td) {
  background: #FAFAF9 !important;
}

/* ===========================
   分页
   =========================== */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 18px 24px;
  border-top: 1px solid #F0F0EE;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  color: #6B6B6E;
  font-weight: 500;
  border-radius: 8px;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #3B6E6E;
  border-color: #3B6E6E;
  color: #FFFFFF;
}

/* ===========================
   详情弹窗
   =========================== */
.order-detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.order-detail-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.order-detail-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 24px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  background: #FAFAF9;
  border-radius: 16px;
  border: 1px solid #F0F0EE;
}

.detail-status-ring {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #FFFFFF;
}

.detail-status-ring--warning { background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%); }
.detail-status-ring--danger  { background: linear-gradient(135deg, #f87171 0%, #ef4444 100%); }
.detail-status-ring--primary { background: linear-gradient(135deg, #4a8a8a 0%, #3B6E6E 100%); }
.detail-status-ring--success { background: linear-gradient(135deg, #4ade80 0%, #22c55e 100%); }
.detail-status-ring--neutral { background: linear-gradient(135deg, #9CA3AF 0%, #6B7280 100%); }

.detail-hero-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.detail-hero-status {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-hero-amount {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.detail-amount-label {
  font-size: 12px;
  color: #6B6B6E;
  font-weight: 500;
}

.detail-amount-value {
  font-size: 24px;
  font-weight: 700;
  color: #C8A464;
}

.detail-section {
  border-top: 1px solid #F0F0EE;
  padding-top: 20px;
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 700;
  color: #1C1C1E;
}

.detail-section-title svg {
  color: #C8A464;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  background: #FAFAF9;
  border-radius: 12px;
  border: 1px solid #F0F0EE;
}

.detail-card--wide {
  grid-column: 1 / -1;
}

.detail-card__label {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.detail-card__value {
  font-size: 13px;
  color: #1C1C1E;
  font-weight: 600;
  line-height: 1.5;
}

.detail-card__value.amount {
  color: #C8A464;
  font-weight: 700;
}

.empty-detail {
  padding: 40px 0;
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 1100px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .status-filter-section {
    flex-direction: column;
    gap: 10px;
  }

  .status-filter-label {
    padding-top: 0;
  }

  .filter-row {
    width: 100%;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
  }

  .filter-actions .primary-btn,
  .filter-actions .secondary-btn {
    flex: 1;
  }

  .filter-row--time {
    flex-direction: column;
  }

  .time-picker {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .list-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-container {
    padding: 0 16px;
    overflow-x: auto;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .pagination-bar {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .status-pills {
    gap: 6px;
  }

  .status-pill {
    padding: 5px 10px;
    font-size: 11px;
  }

  .filter-body {
    padding: 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .status-pill,
  .primary-btn,
  .secondary-btn,
  .icon-btn,
  .action-icon-btn {
    transition: none;
  }
}
</style>
