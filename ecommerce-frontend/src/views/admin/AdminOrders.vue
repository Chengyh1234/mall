<template>
  <div class="admin-orders">
    <div class="page-header">
      <div class="header-left">
        <h1>订单管理</h1>
        <p class="header-desc">全平台订单一览，查看所有用户订单信息</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-main">
        <div class="search-field-group">
          <div class="field-item">
            <label class="field-label">状态</label>
            <el-select v-model="filters.status" placeholder="全部状态" clearable class="field-select" @change="handleSearch">
              <el-option label="待付款" :value="1" />
              <el-option label="待发货" :value="2" />
              <el-option label="待收货" :value="3" />
              <el-option label="已完成" :value="4" />
              <el-option label="已取消" :value="5" />
              <el-option label="退款中" :value="6" />
              <el-option label="已退款" :value="7" />
              <el-option label="已拒绝" :value="8" />
            </el-select>
          </div>
          <div class="field-item">
            <label class="field-label">用户ID</label>
            <el-input v-model="filters.userId" placeholder="精确匹配" clearable class="field-input-sm" @keyup.enter="handleSearch" />
          </div>
          <div class="field-item">
            <label class="field-label">订单号</label>
            <el-input v-model="filters.orderNo" placeholder="模糊搜索" clearable class="field-input" @keyup.enter="handleSearch" />
          </div>
        </div>
        <div class="search-actions">
          <el-button type="primary" size="default" @click="handleSearch">
            <el-icon style="margin-right:4px"><Search /></el-icon>查询
          </el-button>
          <el-button size="default" @click="handleReset">重置</el-button>
        </div>
      </div>
      <div class="search-divider" />
      <div class="search-time">
        <span class="time-section-label">时间筛选</span>
        <div class="time-pickers">
          <el-date-picker
            v-model="payTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="支付起始"
            end-placeholder="支付结束"
            class="time-picker"
            :teleported="false"
            @change="handleSearch"
          />
          <el-date-picker
            v-model="deliveryTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="发货起始"
            end-placeholder="发货结束"
            class="time-picker"
            :teleported="false"
            @change="handleSearch"
          />
          <el-date-picker
            v-model="receiveTimeRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            range-separator="至"
            start-placeholder="收货起始"
            end-placeholder="收货结束"
            class="time-picker"
            :teleported="false"
            @change="handleSearch"
          />
        </div>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table
        :data="orders"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#333', fontWeight: 600 }"
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
            <span class="amount-text">¥{{ scope.row.payAmount?.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" width="100" align="center" />
        <el-table-column prop="receiverPhone" label="联系电话" width="130" align="center" />
        <el-table-column prop="receiverAddress" label="收货地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="创建时间" width="165" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="showDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
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

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="'订单详情 - ' + (currentOrder?.orderNo || '')"
      width="700px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="currentOrder" class="detail-content">
        <div class="detail-section">
          <h3 class="section-title">订单信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">订单号</span>
              <span class="detail-value">{{ currentOrder.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">用户ID</span>
              <span class="detail-value">{{ currentOrder.userId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">总金额</span>
              <span class="detail-value amount">¥{{ currentOrder.totalAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">支付金额</span>
              <span class="detail-value amount">¥{{ currentOrder.payAmount?.toFixed(2) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">订单状态</span>
              <span class="detail-value">
                <el-tag :type="statusTagType(currentOrder.status)" size="small">
                  {{ statusText(currentOrder.status) }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">支付状态</span>
              <span class="detail-value">
                <el-tag :type="currentOrder.payStatus === 1 ? 'success' : 'info'" size="small">
                  {{ currentOrder.payStatus === 1 ? '已支付' : '未支付' }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item" v-if="currentOrder.remark">
              <span class="detail-label">买家备注</span>
              <span class="detail-value">{{ currentOrder.remark }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatTime(currentOrder.createdAt) }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="section-title">收货信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">收货人</span>
              <span class="detail-value">{{ currentOrder.receiverName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">联系电话</span>
              <span class="detail-value">{{ currentOrder.receiverPhone }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="detail-label">收货地址</span>
              <span class="detail-value">{{ currentOrder.receiverAddress }}</span>
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
import { Search } from '@element-plus/icons-vue'
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

// 筛选条件
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
  5: { text: '已取消', tag: 'info' },
  6: { text: '退款中', tag: 'danger' },
  7: { text: '已退款', tag: 'success' },
  8: { text: '已拒绝', tag: 'danger' }
}

const statusText = (status: number) => statusMap[status]?.text || '未知'
const statusTagType = (status: number) => (statusMap[status]?.tag as any) || 'info'

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

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.admin-orders {
  padding: 0;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-left h1 {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px 0;
  letter-spacing: 0.3px;
}

.header-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.search-bar {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 18px 20px 16px 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s;
}

.search-bar:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.search-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  flex-wrap: wrap;
}

.search-field-group {
  display: flex;
  gap: 20px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.field-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.field-label {
  font-size: 12px;
  font-weight: 500;
  color: #606266;
  line-height: 1;
}

.field-select {
  width: 140px;
}

.field-input-sm {
  width: 120px;
}

.field-input {
  width: 200px;
}

.search-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

.search-divider {
  height: 1px;
  background: linear-gradient(to right, #e4e7ed 0%, transparent 100%);
  margin: 14px 0 12px 0;
}

.search-time {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.time-section-label {
  font-size: 12px;
  font-weight: 500;
  color: #909399;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.time-pickers {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.time-picker {
  width: 260px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.amount-text {
  color: #ff4400;
  font-weight: 600;
  font-size: 13px;
}

.time-text {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 0;
}

.detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #ebeef5;
}

.detail-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 14px 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.detail-value.amount {
  color: #ff4400;
  font-weight: 600;
}

.empty-detail {
  padding: 40px 0;
}

@media screen and (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .search-main {
    flex-direction: column;
    align-items: stretch;
  }

  .search-field-group {
    gap: 12px;
  }

  .field-select,
  .field-input-sm,
  .field-input {
    width: 100%;
  }

  .search-actions {
    justify-content: flex-end;
  }

  .search-time {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .time-picker {
    width: 100%;
  }
}
</style>