<template>
  <div class="order-list">
    <div v-if="orders.length === 0" class="empty-order">
      <el-empty description="暂无订单" />
    </div>
    
    <div v-else class="order-items">
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="scope">
            ¥{{ scope.row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="viewOrderDetail(scope.row.id)">
              查看详情
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="primary"
              @click="payOrder(scope.row.id)"
            >
              支付
            </el-button>
            <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="danger"
              @click="cancelOrder(scope.row.id)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, cancelOrder as cancelOrderApi, payOrder as payOrderApi } from '@/api/order'

interface Order {
  id: number
  orderNo: string
  createTime: string
  totalAmount: number
  status: number
}

const props = defineProps<{
  status?: number | null
}>()

const orders = ref<Order[]>([])
const loading = ref(false)

// 获取订单列表
const fetchOrders = async () => {
  try {
    loading.value = true
    const params = props.status ? { status: props.status } : {}
    const result = await getOrderList(params)
    // getOrderList 返回 PageResult<Order> = { list, page, pageSize, total }，需提取 list 数组
    const rawList = (result as any)?.list || (result as any)?.records || result
    orders.value = Array.isArray(rawList) ? rawList : []
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status: number) => {
  switch (status) {
    case 1: return 'warning' // 待付款
    case 2: return 'info'    // 待发货
    case 3: return 'primary'  // 待收货
    case 4: return 'success'  // 已完成
    case 5: return 'danger'   // 已取消
    default: return ''
  }
}

// 获取状态文本
const getStatusText = (status: number) => {
  switch (status) {
    case 1: return '待付款'
    case 2: return '待发货'
    case 3: return '待收货'
    case 4: return '已完成'
    case 5: return '已取消'
    default: return '未知状态'
  }
}

// 查看订单详情
const viewOrderDetail = (id: number) => {
  ElMessage.info('查看订单详情功能开发中')
}

// 支付订单
const payOrder = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认支付该订单吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await payOrderApi(id)
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  }
}

// 取消订单
const cancelOrder = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrderApi(id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.order-list {
  padding: 20px 0;
}

.empty-order {
  padding: 100px 0;
  text-align: center;
}

.order-items {
  margin-top: 20px;
}
</style>