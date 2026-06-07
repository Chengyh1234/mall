<template>
  <div class="seller-orders">
    <div class="page-header">
      <h1>订单管理</h1>
    </div>

    <div class="status-tabs">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待付款" name="pending" />
        <el-tab-pane label="待发货" name="unshipped" />
        <el-tab-pane label="已发货" name="shipped" />
        <el-tab-pane label="已完成" name="completed" />
        <el-tab-pane label="已取消" name="cancelled" />
      </el-tabs>
    </div>

    <el-table :data="orders" border v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="买家" width="120">
        <template #default="scope">
          <div>{{ scope.row.receiverName || scope.row.buyerName || '--' }}</div>
          <div class="phone-text">{{ scope.row.receiverPhone || scope.row.buyerPhone || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="金额（元）" width="160">
        <template #default="scope">
          <div class="amount-col">
            <div>实付：<span class="pay-amount">¥{{ scope.row.payAmount?.toFixed(2) }}</span></div>
            <div class="total-amount">总额 ¥{{ scope.row.totalAmount?.toFixed(2) }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.statusDesc || getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="160">
        <template #default="scope">
          {{ scope.row.createdAt }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="viewOrder(scope.row)">查看详情</el-button>
          <el-button
            v-if="scope.row.status === 2"
            size="small"
            type="primary"
            @click="showShipModal(scope.row)"
          >
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadOrders"
        @size-change="loadOrders"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="showDetailModal" width="700px">
      <div v-if="orderDetail" class="order-detail" v-loading="detailLoading">
        <div class="detail-header">
          <div class="order-no">订单号：{{ orderDetail.order.orderNo }}</div>
          <div class="order-status">
            <el-tag :type="getStatusType(orderDetail.order.status)">
              {{ orderDetail.statusDesc }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section">
          <h4>买家信息</h4>
          <div class="info-row">
            <span class="label">姓名：</span>
            <span>{{ orderDetail.order.receiverName }}</span>
          </div>
          <div class="info-row">
            <span class="label">电话：</span>
            <span>{{ orderDetail.order.receiverPhone }}</span>
          </div>
          <div class="info-row">
            <span class="label">地址：</span>
            <span>{{ orderDetail.order.receiverAddress }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>支付信息</h4>
          <div class="info-row">
            <span class="label">支付状态：</span>
            <span>{{ orderDetail.payStatusDesc }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付方式：</span>
            <span>{{ orderDetail.payTypeDesc }}</span>
          </div>
          <div class="info-row">
            <span class="label">订单金额：</span>
            <span>¥{{ orderDetail.order.totalAmount?.toFixed(2) }}</span>
          </div>
          <div class="info-row">
            <span class="label">实付金额：</span>
            <span class="pay-amount-text">¥{{ orderDetail.order.payAmount?.toFixed(2) }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>商品信息</h4>
          <el-table :data="orderDetail.items" border size="small">
            <el-table-column label="商品" min-width="200">
              <template #default="scope">
                <div class="product-info">
                  <el-image v-if="scope.row.productImage" :src="getProductImageUrl(scope.row.productImage)" fit="cover" class="product-img" />
                  <div class="product-text">
                    <div class="product-name">{{ scope.row.productName }}</div>
                    <div class="product-specs" v-if="scope.row.skuSpecs">{{ scope.row.skuSpecs }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="80">
              <template #default="scope">
                ¥{{ scope.row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="60" />
            <el-table-column label="小计" width="100">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section" v-if="orderDetail.deliveries && orderDetail.deliveries.length > 0">
          <h4>发货记录</h4>
          <el-table :data="orderDetail.deliveries" border size="small">
            <el-table-column prop="deliveryCompany" label="快递公司" />
            <el-table-column prop="deliveryNo" label="运单号" />
            <el-table-column prop="createdAt" label="发货时间" />
          </el-table>
        </div>

        <div class="detail-footer">
          <div class="time-row">
            <span class="label">下单时间：</span>
            <span>{{ orderDetail.order.createdAt }}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog title="发货" v-model="showShipDialog" width="400px">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="快递公司" required>
          <el-select v-model="shipForm.company" placeholder="请选择快递公司">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="申通快递" value="申通快递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="EMS" value="EMS" />
          </el-select>
        </el-form-item>
        <el-form-item label="运单号" required>
          <el-input v-model="shipForm.trackingNo" placeholder="请输入运单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showShipDialog = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="confirmShip">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getSellerOrderList, getSellerOrderDetail, deliverOrder } from '@/api/order'
import { getSpuImageUrl } from '@/utils/resource'

const activeTab = ref('all')
const showDetailModal = ref(false)
const showShipDialog = ref(false)
const loading = ref(false)
const detailLoading = ref(false)
const shipLoading = ref(false)

const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const shipForm = reactive({
  company: '',
  trackingNo: ''
})

const orders = ref<any[]>([])
const orderDetail = ref<any>(null)
const detailOrderId = ref<number>(0)

const statusTabMap: Record<string, number | undefined> = {
  all: undefined,
  pending: 1,
  unshipped: 2,
  shipped: 3,
  completed: 4,
  cancelled: 5
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    1: 'warning',
    2: 'primary',
    3: 'info',
    4: 'success',
    5: 'danger'
  }
  return types[status] || 'default'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    1: '待付款',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消'
  }
  return texts[status] || '未知'
}

const getProductImageUrl = (path?: string) => {
  if (!path) return ''
  return getSpuImageUrl(path)
}

const loadOrders = async () => {
  loading.value = true
  try {
    const res = await getSellerOrderList({
      status: statusTabMap[activeTab.value]
    })

    if (Array.isArray(res)) {
      orders.value = res
    } else if (res && Array.isArray(res.list)) {
      orders.value = res.list
      total.value = res.total ?? res.list.length
    } else {
      orders.value = []
    }
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

const loadOrderDetail = async (orderNo: string) => {
  detailLoading.value = true
  try {
    const data = await getSellerOrderDetail(orderNo)
    orderDetail.value = data
  } catch {
    ElMessage.error('获取订单详情失败')
    orderDetail.value = null
  } finally {
    detailLoading.value = false
  }
}

const viewOrder = (row: any) => {
  detailOrderId.value = row.id
  showDetailModal.value = true
  loadOrderDetail(row.orderNo)
}

watch(activeTab, () => {
  loadOrders()
})

const showShipModal = (row: any) => {
  detailOrderId.value = row.id
  shipForm.company = ''
  shipForm.trackingNo = ''
  showShipDialog.value = true
}

const confirmShip = async () => {
  if (!shipForm.company || !shipForm.trackingNo) {
    ElMessage.warning('请填写完整的发货信息')
    return
  }

  shipLoading.value = true
  try {
    await deliverOrder(detailOrderId.value, {
      deliveryCompany: shipForm.company,
      deliveryNo: shipForm.trackingNo
    })

    ElMessage.success('发货成功')
    showShipDialog.value = false
    loadOrders()
  } catch {
    ElMessage.error('发货失败')
  } finally {
    shipLoading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.seller-orders {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.status-tabs {
  margin-bottom: 20px;
}

.phone-text {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.product-img {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  flex-shrink: 0;
}

.product-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-text {
  flex: 1;
  min-width: 0;
}

.product-specs {
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}

.more-text {
  font-size: 12px;
  color: #999;
}

.amount-col {
  text-align: right;
}

.pay-amount {
  font-weight: 600;
  color: #ff4400;
}

.total-amount {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
}

.pagination-wrap {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 订单详情弹窗样式 */
.order-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}

.order-no {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.detail-section {
  margin-bottom: 15px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 14px;
  padding-bottom: 6px;
  border-bottom: 1px solid #f0f0f0;
}

.info-row {
  display: flex;
  margin-bottom: 5px;
  line-height: 1.8;
}

.info-row .label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}

.pay-amount-text {
  font-weight: 600;
  color: #ff4400;
}

.detail-footer {
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.time-row {
  display: flex;
  color: #666;
  font-size: 14px;
}

.time-row .label {
  width: 80px;
}
</style>