<template>
  <div class="order-list-container">
    <NavBar />
    
    <div class="main-content">
      <div class="order-header">
        <h2>我的订单</h2>
        <el-tabs v-model="activeTab" type="card">
          <el-tab-pane label="全部" name="all">全部</el-tab-pane>
          <el-tab-pane label="待付款" name="pending_pay">待付款</el-tab-pane>
          <el-tab-pane label="待发货" name="pending_ship">待发货</el-tab-pane>
          <el-tab-pane label="待收货" name="pending_receive">待收货</el-tab-pane>
          <el-tab-pane label="已完成" name="completed">已完成</el-tab-pane>
          <el-tab-pane label="已取消" name="cancelled">已取消</el-tab-pane>
        </el-tabs>
      </div>
      
      <div v-if="orders.length > 0" class="order-list">
        <div 
          v-for="order in orders" 
          :key="order.id" 
          class="order-card"
        >
          <div class="order-header-bar">
            <span class="order-no">订单号：{{ order.orderNo }}</span>
            <span class="order-status" :class="getStatusClass(order.status)">
              {{ getStatusText(order.status) }}
            </span>
          </div>
          
          <div class="order-address">
            <div class="address-info">
              <span class="address-label">收货信息：</span>
              <span>{{ order.receiverName }} {{ order.receiverPhone }}</span>
              <span class="address-detail">{{ order.receiverAddress }}</span>
            </div>
          </div>
          
          <div class="order-footer">
            <div class="order-total">
              合计：
              <span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="order-actions">
              <template v-if="order.status === 1">
                <el-button type="primary" @click="handlePay(order.id)">立即支付</el-button>
                <el-button type="text" @click="showCancelDialog(order)">取消订单</el-button>
              </template>
              <template v-else-if="order.status === 3">
                <el-button type="primary" @click="handleReceive(order.id)">确认收货</el-button>
              </template>
              <template v-else-if="order.status === 2">
                <span class="waiting-text">等待卖家发货...</span>
              </template>
              <template v-else-if="order.status === 5">
                <span class="cancelled-text">{{ order.cancelReason || '订单已取消' }}</span>
              </template>
              <template v-else-if="order.status === 4">
                <span class="completed-text">订单已完成</span>
              </template>
            </div>
          </div>

          <!-- 发货信息 -->
          <div v-if="order.status === 3 && order.deliveryCompany" class="shipping-info">
            <div class="shipping-header">
              <span class="shipping-icon">📦</span>
              <span>物流信息</span>
            </div>
            <div class="shipping-detail">
              <span>快递公司：{{ order.deliveryCompany }}</span>
              <span>运单号：{{ order.deliveryNo }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-orders">
        <el-empty description="暂无订单">
          <el-button type="primary" @click="goHome">去购物</el-button>
        </el-empty>
      </div>
      
      <!-- 取消订单弹窗 -->
      <el-dialog title="取消订单" v-model="cancelDialogVisible">
        <div class="cancel-reason">
          <span>取消原因：</span>
          <el-select v-model="cancelReason" placeholder="请选择取消原因">
            <el-option label="不想买了" value="不想买了" />
            <el-option label="商品价格太贵" value="商品价格太贵" />
            <el-option label="等待时间太长" value="等待时间太长" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </div>
        <template #footer>
          <el-button @click="cancelDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmCancel">确认取消</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { getOrderList, payOrder, cancelOrder, confirmReceive, type Order } from '@/api/order'

const router = useRouter()

const activeTab = ref('all')
const orders = ref<Order[]>([])
const loading = ref(false)

// 取消订单弹窗
const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const currentOrder = ref<Order | null>(null)

// 状态映射
const statusMap: Record<number, string> = {
  1: '待付款',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款'
}

// 状态样式类
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    1: 'status-pending',
    2: 'status-processing',
    3: 'status-processing',
    4: 'status-success',
    5: 'status-cancelled',
    6: 'status-warning',
    7: 'status-info'
  }
  return classMap[status] || ''
}

// 获取状态文本
const getStatusText = (status: number) => {
  return statusMap[status] || String(status)
}

// tab标签到状态的映射
const tabStatusMap: Record<string, number> = {
  all: 0,
  pending_pay: 1,
  pending_ship: 2,
  pending_receive: 3,
  completed: 4,
  cancelled: 5
}

// 根据标签过滤订单
const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orders.value
  }
  const targetStatus = tabStatusMap[activeTab.value] || 0
  return orders.value.filter(order => order.status === targetStatus)
})

// 加载订单列表
const loadOrders = async (status?: number) => {
  loading.value = true
  try {
    const params: { page?: number; pageSize?: number; status?: number } = {
      page: 1,
      pageSize: 20
    }
    if (status !== undefined && status !== 0) {
      params.status = status
    }
    const data = await getOrderList(params)
    orders.value = data || []
  } catch {
    ElMessage.error('获取订单列表失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

// 支付订单
const handlePay = async (orderId: number) => {
  try {
    await payOrder(orderId)
    ElMessage.success('支付成功')
    loadOrders()
  } catch {
    ElMessage.error('支付失败')
  }
}

// 显示取消订单弹窗
const showCancelDialog = (order: Order) => {
  currentOrder.value = order
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请选择取消原因')
    return
  }
  
  if (!currentOrder.value) return
  
  try {
    await cancelOrder(currentOrder.value.id)
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch {
    ElMessage.error('取消订单失败')
  }
}

// 确认收货
const handleReceive = async (orderId: number) => {
  try {
    await confirmReceive(orderId)
    ElMessage.success('收货成功')
    loadOrders()
  } catch {
    ElMessage.error('收货失败')
  }
}

// 跳转到商品详情
const goProductDetail = (spuId?: number) => {
  if (spuId) {
    router.push(`/product/${spuId}`)
  }
}

// 跳转到首页
const goHome = () => {
  router.push('/home')
}

// 监听标签切换
watch(activeTab, () => {
  const targetStatus = tabStatusMap[activeTab.value] || 0
  loadOrders(targetStatus === 0 ? undefined : targetStatus)
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.main-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
}

.order-header {
  margin-bottom: 20px;
}

.order-header h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.order-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-bottom: 1px solid #eee;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-status {
  font-size: 14px;
  font-weight: 600;
}

.status-pending {
  color: #ff4400;
}

.status-processing {
  color: #409eff;
}

.status-success {
  color: #67c23a;
}

.status-cancelled {
  color: #999;
}

.status-warning {
  color: #e6a23c;
}

.status-info {
  color: #909399;
}

.order-address {
  padding: 15px 20px;
}

.address-info {
  font-size: 14px;
  color: #666;
}

.address-label {
  font-weight: 500;
  color: #333;
}

.address-detail {
  display: block;
  margin-top: 5px;
  color: #666;
}

.completed-text {
  color: #67c23a;
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.order-total .total-price {
  color: #ff4400;
  font-size: 18px;
  font-weight: 700;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.waiting-text {
  color: #999;
  font-size: 14px;
}

.cancelled-text {
  color: #999;
  font-size: 14px;
}

.shipping-info {
  padding: 15px 20px;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.shipping-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #333;
}

.shipping-icon {
  font-size: 18px;
}

.shipping-detail {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.empty-orders {
  padding: 60px 0;
  text-align: center;
}

/* 取消订单弹窗样式 */
.cancel-reason {
  display: flex;
  align-items: center;
  gap: 15px;
}

.cancel-reason span {
  font-weight: 500;
}

.cancel-reason .el-select {
  width: 200px;
}
</style>