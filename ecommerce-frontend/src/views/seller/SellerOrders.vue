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
        <el-tab-pane label="退款中" name="refunding" />
        <el-tab-pane label="已退款" name="refunded" />
        <el-tab-pane label="已拒绝" name="rejected" />
      </el-tabs>
    </div>

    <!-- 筛选面板 -->
    <div class="search-bar">
      <div class="search-main">
        <div class="search-field-group">
          <div class="field-item">
            <label class="field-label">订单号</label>
            <el-input
              v-model="filters.orderNo"
              placeholder="模糊搜索"
              clearable
              class="field-input"
              @keyup.enter="doSearch"
            />
          </div>
          <div class="field-item">
            <label class="field-label">用户ID</label>
            <el-input
              v-model="filters.userIdStr"
              placeholder="精确匹配"
              clearable
              class="field-input-sm"
              @keyup.enter="doSearch"
            />
          </div>
        </div>
        <div class="search-actions">
          <el-button type="primary" :icon="Search" @click="doSearch">查询</el-button>
          <el-button :icon="Refresh" @click="resetFilters">重置</el-button>
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
            @change="onPayTimeChange"
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
            @change="onDeliveryTimeChange"
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
            @change="onReceiveTimeChange"
          />
        </div>
      </div>
    </div>

    <!-- 订单列表 -->
    <div class="order-table-wrap">
      <el-table
        :data="orders"
        stripe
        v-loading="loading"
        style="width: 100%"
        :row-class-name="orderRowClass"
        header-cell-class-name="order-table-header"
      >
        <!-- 订单号 + 状态 -->
        <el-table-column label="订单信息" min-width="200">
          <template #default="scope">
            <div class="oi-top">
              <span class="oi-no">{{ scope.row.orderNo }}</span>
              <el-tag
                :type="getStatusType(scope.row.status)"
                effect="dark"
                size="small"
                round
              >
                {{ scope.row.statusDesc || getStatusText(scope.row.status) }}
              </el-tag>
            </div>
            <div class="oi-time">{{ scope.row.createdAt }}</div>
          </template>
        </el-table-column>

        <!-- 商品展示 -->
        <el-table-column label="商品" min-width="350">
          <template #default="scope">
            <div class="items-wrap" v-if="scope.row.items && scope.row.items.length > 0">
              <div
                class="item-card"
                v-for="(item, idx) in scope.row.items"
                :key="idx"
              >
                <el-image
                  v-if="item.productImage || item.image"
                  :src="getProductImageUrl(item.productImage || item.image)"
                  fit="cover"
                  class="item-thumb"
                />
                <div class="item-thumb-placeholder" v-else>
                  <el-icon :size="16"><Picture /></el-icon>
                </div>
                <div class="item-body">
                  <span class="item-name">{{ item.productName || item.name }}</span>
                  <span class="item-specs" v-if="item.skuSpecs || item.specs">{{ item.skuSpecs || item.specs }}</span>
                </div>
                <div class="item-meta">
                  <span class="item-qty">×{{ item.quantity }}</span>
                  <span class="item-price">¥{{ (item.price || 0).toFixed(2) }}</span>
                </div>
              </div>
            </div>
            <span class="empty-text" v-else>暂无商品</span>
          </template>
        </el-table-column>

        <!-- 实付金额 -->
        <el-table-column label="实付金额" width="130" align="right">
          <template #default="scope">
            <div class="pay-col">
              <span class="pay-val">¥{{ scope.row.payAmount?.toFixed(2) }}</span>
              <span class="total-val" v-if="scope.row.totalAmount !== scope.row.payAmount">
                原价 ¥{{ scope.row.totalAmount?.toFixed(2) }}
              </span>
            </div>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="scope">
            <div class="action-group">
              <el-button size="small" text @click="viewOrder(scope.row)">详情</el-button>
              <template v-if="scope.row.status === 2">
                <el-divider direction="vertical" />
                <el-button size="small" type="primary" plain @click="showShipModal(scope.row)">发货</el-button>
              </template>
              <template v-if="scope.row.status === 6">
                <el-divider direction="vertical" />
                <el-button size="small" type="warning" plain @click="showReviewDialog(scope.row)">审核退款</el-button>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="loadOrders"
        @size-change="loadOrders"
      />
    </div>

    <!-- 订单详情弹窗 -->
    <el-dialog title="订单详情" v-model="showDetailModal" width="720px" top="4vh">
      <div v-if="orderDetail" class="order-detail" v-loading="detailLoading">
        <div class="detail-header">
          <div class="order-no">订单号：{{ orderDetail.orderNo }}</div>
          <div class="order-status">
            <el-tag :type="getStatusType(orderDetail.status)" size="large" effect="dark">
              {{ orderDetail.statusDesc }}
            </el-tag>
            <el-tag v-if="orderDetail.status >= 6" :type="getStatusType(orderDetail.status)" size="large" style="margin-left: 8px">
              {{ getStatusText(orderDetail.status) }}
            </el-tag>
          </div>
        </div>

        <div class="detail-grid">
          <div class="detail-section">
            <h4>买家信息</h4>
            <div class="info-list">
              <div class="info-row"><span class="label">姓名：</span><span>{{ orderDetail.receiverName }}</span></div>
              <div class="info-row"><span class="label">电话：</span><span>{{ orderDetail.receiverPhone }}</span></div>
              <div class="info-row"><span class="label">地址：</span><span>{{ orderDetail.receiverAddress }}</span></div>
            </div>
          </div>
          <div class="detail-section">
            <h4>支付信息</h4>
            <div class="info-list">
              <div class="info-row"><span class="label">支付状态：</span><span>{{ orderDetail.payStatusDesc }}</span></div>
              <div class="info-row"><span class="label">支付方式：</span><span>{{ orderDetail.payTypeDesc }}</span></div>
              <div class="info-row"><span class="label">订单金额：</span><span>¥{{ orderDetail.totalAmount?.toFixed(2) }}</span></div>
              <div class="info-row"><span class="label">实付金额：</span><span class="pay-amount-text">¥{{ orderDetail.payAmount?.toFixed(2) }}</span></div>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>商品信息</h4>
          <el-table :data="orderDetail.items" border size="small">
            <el-table-column label="商品" min-width="220">
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
            <el-table-column label="单价" width="80" align="right">
              <template #default="scope">¥{{ scope.row.price?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="60" align="center" />
            <el-table-column label="小计" width="100" align="right">
              <template #default="scope">¥{{ (scope.row.totalAmount || scope.row.price * scope.row.quantity).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section" v-if="orderDetail.deliveries && orderDetail.deliveries.length > 0">
          <h4>发货记录</h4>
          <el-table :data="orderDetail.deliveries" border size="small">
            <el-table-column prop="deliveryCompany" label="快递公司" />
            <el-table-column prop="deliveryNo" label="运单号" />
            <el-table-column prop="deliveryTime" label="发货时间" />
          </el-table>
        </div>

        <div class="detail-section" v-if="orderDetail.status >= 6 && orderDetail.rejectReason">
          <h4>拒绝信息</h4>
          <div class="info-list">
            <div class="info-row"><span class="label">拒绝原因：</span><span class="reject-text">{{ orderDetail.rejectReason }}</span></div>
            <div class="info-row" v-if="orderDetail.rejectedAt"><span class="label">拒绝时间：</span><span>{{ orderDetail.rejectedAt }}</span></div>
          </div>
        </div>

        <div class="detail-footer">
          <span class="label">下单时间：</span>
          <span>{{ orderDetail.createdAt }}</span>
          <span class="detail-footer-divider">|</span>
          <span class="label">订单号：</span>
          <span>{{ orderDetail.orderNo }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog title="发货" v-model="showShipDialog" width="420px" top="30vh">
      <el-form :model="shipForm" label-width="80px">
        <el-form-item label="快递公司" required>
          <el-select v-model="shipForm.company" placeholder="请选择快递公司" style="width: 100%">
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

    <!-- 退款审核弹窗 -->
    <el-dialog title="审核退款" v-model="showReviewDialogVisible" width="460px" top="30vh">
      <div v-if="reviewTarget" class="review-info">
        <div class="info-row"><span class="label">订单号</span><span style="font-family:monospace">{{ reviewTarget.orderNo }}</span></div>
        <div class="info-row"><span class="label">退款金额</span><span class="pay-amount-text">¥{{ reviewTarget.payAmount?.toFixed(2) }}</span></div>
        <div class="info-row" v-if="reviewTarget.refundReason"><span class="label">退款原因</span><span>{{ reviewTarget.refundReason }}</span></div>
        <div class="review-reason" v-if="reviewTarget.refundReason">
          <span class="reason-label">买家说明：</span>{{ reviewTarget.refundReason }}
        </div>
      </div>
      <el-input
        v-model="rejectReasonInput"
        type="textarea"
        :rows="3"
        placeholder="拒绝退款时请填写原因（必填）"
      />
      <div class="review-footer-hint">请核实买家退款原因后做出审核决定</div>
      <template #footer>
        <el-button @click="showReviewDialogVisible = false">取消</el-button>
        <el-button type="danger" plain :loading="reviewLoading" @click="doRejectReview">拒绝退款</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="doApproveReview">同意退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import { getSellerOrderList, getSellerOrderDetail, deliverOrder, refundApprove, refundReject } from '@/api/order'
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

// 退款审核相关
const showReviewDialogVisible = ref(false)
const reviewTarget = ref<any>(null)
const reviewLoading = ref(false)
const rejectReasonInput = ref('')

const orders = ref<any[]>([])
const orderDetail = ref<any>(null)
const detailOrderId = ref<number>(0)

// 筛选条件
const filters = reactive({
  orderNo: '',
  userIdStr: ''
})
const payTimeRange = ref<[string, string] | null>(null)
const deliveryTimeRange = ref<[string, string] | null>(null)
const receiveTimeRange = ref<[string, string] | null>(null)

const filterParams = reactive({
  payTimeStart: '',
  payTimeEnd: '',
  deliveryTimeStart: '',
  deliveryTimeEnd: '',
  receiveTimeStart: '',
  receiveTimeEnd: ''
})

const onPayTimeChange = (val: [string, string] | null) => {
  filterParams.payTimeStart = val ? val[0] : ''
  filterParams.payTimeEnd = val ? val[1] : ''
}
const onDeliveryTimeChange = (val: [string, string] | null) => {
  filterParams.deliveryTimeStart = val ? val[0] : ''
  filterParams.deliveryTimeEnd = val ? val[1] : ''
}
const onReceiveTimeChange = (val: [string, string] | null) => {
  filterParams.receiveTimeStart = val ? val[0] : ''
  filterParams.receiveTimeEnd = val ? val[1] : ''
}

const doSearch = () => {
  currentPage.value = 1
  loadOrders()
}

const resetFilters = () => {
  filters.orderNo = ''
  filters.userIdStr = ''
  payTimeRange.value = null
  deliveryTimeRange.value = null
  receiveTimeRange.value = null
  filterParams.payTimeStart = ''
  filterParams.payTimeEnd = ''
  filterParams.deliveryTimeStart = ''
  filterParams.deliveryTimeEnd = ''
  filterParams.receiveTimeStart = ''
  filterParams.receiveTimeEnd = ''
  currentPage.value = 1
  loadOrders()
}

const statusTabMap: Record<string, number | undefined> = {
  all: undefined,
  pending: 1,
  unshipped: 2,
  shipped: 3,
  completed: 4,
  cancelled: 5,
  refunding: 6,
  refunded: 7,
  rejected: 8
}

const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    1: 'warning',
    2: 'primary',
    3: 'info',
    4: 'success',
    5: 'danger',
    6: 'danger',
    7: 'success',
    8: 'danger'
  }
  return types[status] || 'default'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    1: '待付款',
    2: '待发货',
    3: '已发货',
    4: '已完成',
    5: '已取消',
    6: '退款中',
    7: '已退款',
    8: '已拒绝'
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
    const params: Record<string, any> = {
      status: statusTabMap[activeTab.value],
      page: currentPage.value,
      pageSize: pageSize.value
    }
    if (filters.orderNo) params.orderNo = filters.orderNo
    if (filters.userIdStr) params.userId = Number(filters.userIdStr)
    if (filterParams.payTimeStart) params.payTimeStart = filterParams.payTimeStart
    if (filterParams.payTimeEnd) params.payTimeEnd = filterParams.payTimeEnd
    if (filterParams.deliveryTimeStart) params.deliveryTimeStart = filterParams.deliveryTimeStart
    if (filterParams.deliveryTimeEnd) params.deliveryTimeEnd = filterParams.deliveryTimeEnd
    if (filterParams.receiveTimeStart) params.receiveTimeStart = filterParams.receiveTimeStart
    if (filterParams.receiveTimeEnd) params.receiveTimeEnd = filterParams.receiveTimeEnd

    const res = await getSellerOrderList(params)

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
    orderDetail.value = null
  } finally {
    detailLoading.value = false
  }
}

// 去除 Expand 相关回调
const onExpandChange = undefined

// 行样式：商品多的行自动增高
const orderRowClass = () => 'seller-order-row'

const viewOrder = (row: any) => {
  detailOrderId.value = row.id
  showDetailModal.value = true
  loadOrderDetail(row.orderNo)
}

watch(activeTab, () => {
  currentPage.value = 1
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
    // 拦截器已处理后端错误提示
  } finally {
    shipLoading.value = false
  }
}

// 退款审核
const showReviewDialog = (row: any) => {
  reviewTarget.value = row
  rejectReasonInput.value = ''
  showReviewDialogVisible.value = true
}

const doApproveReview = async () => {
  if (!reviewTarget.value) return
  reviewLoading.value = true
  try {
    await refundApprove(reviewTarget.value.id)
    ElMessage.success('退款审核通过，已完成退款')
    showReviewDialogVisible.value = false
    loadOrders()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    reviewLoading.value = false
  }
}

const doRejectReview = async () => {
  if (!reviewTarget.value) return
  if (!rejectReasonInput.value) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  reviewLoading.value = true
  try {
    await refundReject(reviewTarget.value.id, rejectReasonInput.value)
    ElMessage.success('已拒绝退款申请')
    showReviewDialogVisible.value = false
    loadOrders()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    reviewLoading.value = false
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.seller-orders {
  padding: 24px 32px;
  max-width: 1440px;
  margin: 0 auto;
  background: #f5f6fa;
  min-height: 100vh;
}

/* ---- 页面标题 ---- */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-header h1 {
  font-size: 22px;
  margin: 0;
  color: #1e1e2f;
  font-weight: 650;
  letter-spacing: -0.3px;
}

/* ---- Tabs ---- */
.status-tabs {
  margin-bottom: 16px;
}
.status-tabs :deep(.el-tabs__header) {
  margin: 0;
}
.status-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 0;
}
.status-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  font-weight: 500;
  padding: 0 16px;
  height: 36px;
  line-height: 36px;
  color: #555;
  transition: color 0.2s;
}
.status-tabs :deep(.el-tabs__item.is-active) {
  color: #4361ee;
  font-weight: 600;
}
.status-tabs :deep(.el-tabs__active-bar) {
  height: 2.5px;
  border-radius: 2px;
  background: #4361ee;
}

/* ---- 搜索栏 ---- */
.search-bar {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 18px 20px 16px 20px;
  margin-bottom: 16px;
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
.field-input {
  width: 200px;
}
.field-input-sm {
  width: 120px;
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

/* ---- 表格容器 ---- */
.order-table-wrap {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}
.order-table-header th {
  background: #f8f9fd !important;
  color: #555 !important;
  font-weight: 600 !important;
  font-size: 13px !important;
  padding: 12px 18px !important;
  border-bottom: 1px solid #e8ecf1 !important;
}

/* ---- 订单信息列 ---- */
.oi-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}
.oi-no {
  font-weight: 650;
  color: #1e1e2f;
  font-size: 14px;
  font-family: 'SF Mono', 'Consolas', 'Monaco', monospace;
  letter-spacing: 0.2px;
}
.oi-time {
  font-size: 12.5px;
  color: #888;
  padding-left: 1px;
}

/* ---- 商品卡片 ---- */
.items-wrap {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 2px 0;
}
.item-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 10px;
  border-radius: 8px;
  background: #f8f9fc;
  border: 1px solid #eef0f4;
  transition: background 0.18s, border-color 0.18s;
}
.item-card:hover {
  background: #f0f4ff;
  border-color: #d0d9f0;
}
.item-thumb {
  width: 38px;
  height: 38px;
  border-radius: 6px;
  flex-shrink: 0;
  border: 1px solid #e2e6ed;
  object-fit: cover;
}
.item-thumb-placeholder {
  width: 38px;
  height: 38px;
  border-radius: 6px;
  flex-shrink: 0;
  background: #eef0f4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c0c4cc;
}
.item-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.item-name {
  font-size: 13px;
  color: #1e1e2f;
  font-weight: 530;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.item-specs {
  font-size: 11.5px;
  color: #aaa;
}
.item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.item-qty {
  color: #888;
  font-size: 13px;
}
.item-price {
  color: #e74c3c;
  font-size: 13px;
  font-weight: 620;
  min-width: 62px;
  text-align: right;
}
.empty-text {
  color: #c0c4cc;
  font-size: 13px;
}

/* ---- 金额列 ---- */
.pay-col {
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.pay-val {
  font-size: 16px;
  font-weight: 700;
  color: #e74c3c;
  letter-spacing: -0.2px;
}
.total-val {
  font-size: 11.5px;
  color: #bbb;
  text-decoration: line-through;
}

/* ---- 操作列 ---- */
.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
}
.action-group .el-divider--vertical {
  height: 13px;
  margin: 0 3px;
}

/* ---- 行样式 ---- */
.seller-order-row td {
  vertical-align: top;
  padding: 13px 18px !important;
}

/* ---- 分页 ---- */
.pagination-wrap {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  padding: 4px 0;
}

/* ---- 订单详情弹窗 ---- */
.order-detail {
  padding: 6px 0;
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #eaeef2;
  margin-bottom: 16px;
}
.order-no {
  font-size: 15px;
  font-weight: 630;
  color: #1e1e2f;
  font-family: 'SF Mono', 'Consolas', monospace;
}
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.detail-section {
  margin-bottom: 16px;
}
.detail-section h4 {
  margin: 0 0 10px 0;
  color: #1e1e2f;
  font-size: 14px;
  font-weight: 600;
  padding-bottom: 6px;
  border-bottom: 1px solid #eef1f5;
}
.info-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.info-row {
  display: flex;
  margin-bottom: 4px;
  line-height: 1.8;
  font-size: 13px;
}
.info-row .label {
  width: 80px;
  color: #777;
  flex-shrink: 0;
}
.pay-amount-text {
  font-weight: 650;
  color: #e74c3c;
}
.reject-text {
  color: #c2413a;
  font-weight: 500;
}
.detail-footer {
  padding-top: 12px;
  border-top: 1px solid #eaeef2;
  color: #888;
  font-size: 12.5px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.detail-footer .label {
  color: #999;
}
.detail-footer-divider {
  margin: 0 6px;
  color: #ddd;
}
.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.product-img {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  flex-shrink: 0;
  border: 1px solid #eef0f4;
}
.product-name {
  font-size: 13px;
  color: #1e1e2f;
  font-weight: 530;
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

/* ---- 审核退款弹窗 ---- */
.review-info {
  background: #f8f9fc;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 12px;
}
.review-info .info-row {
  font-size: 13.5px;
  padding: 5px 0;
}
.review-info .info-row .label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}
.review-reason {
  margin-top: 10px;
  padding: 10px 14px;
  background: #fffbe6;
  border-radius: 8px;
  border-left: 3px solid #faad14;
  color: #333;
  font-size: 13px;
  line-height: 1.55;
}
.review-reason .reason-label {
  color: #d48806;
  font-weight: 550;
  margin-right: 4px;
}
.review-footer-hint {
  font-size: 12px;
  color: #bbb;
  text-align: center;
  margin-top: 10px;
}
</style>