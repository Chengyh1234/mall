<template>
  <div class="order-page">
    <NavBar />

    <div class="page-body">
      <!-- 页面标题 -->
      <div class="page-title">
        <h2>我的订单</h2>
        <p class="title-desc">查看和管理您的全部订单</p>
      </div>

      <!-- 状态标签 -->
      <div class="status-tabs">
        <el-tabs v-model="activeTab" @tab-change="onTabChange">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="待付款" name="pending_pay" />
          <el-tab-pane label="待发货" name="pending_ship" />
          <el-tab-pane label="待收货" name="pending_receive" />
          <el-tab-pane label="已完成" name="completed" />
          <el-tab-pane label="已取消" name="cancelled" />
        </el-tabs>
      </div>

      <!-- 订单列表 -->
      <div class="order-list-wrap">
        <!-- 加载中 -->
        <div v-if="loading" class="loading-state">
          <el-icon class="loading-icon" :size="32"><Loading /></el-icon>
          <p>加载中...</p>
        </div>

        <!-- 订单卡片 -->
        <template v-else-if="orders.length > 0">
          <div class="order-cards">
            <div v-for="order in orders" :key="order.id" class="order-card" @click="viewDetail(order)">
              <!-- 卡片头部 -->
              <div class="card-header">
                <div class="header-left">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-time">{{ formatTime(order.createdAt) }}</span>
                </div>
                <div class="header-right">
                  <el-tag :type="statusTag(order.status)" size="large" effect="dark" class="status-tag">
                    {{ statusText(order.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 商品列表 -->
              <div class="card-items">
                <template v-if="order.items && order.items.length > 0">
                  <div class="item" v-for="item in order.items" :key="item.id">
                    <div class="item-img">
                      <img :src="item.image || '/placeholder.png'" :alt="item.name" />
                    </div>
                    <div class="item-info">
                      <div class="item-name">{{ item.name }}</div>
                      <div class="item-specs" v-if="item.specs">{{ item.specs }}</div>
                    </div>
                    <div class="item-price">
                      <span class="price">¥{{ item.price?.toFixed(2) }}</span>
                    </div>
                    <div class="item-qty">x{{ item.quantity }}</div>
                  </div>
                </template>
                <!-- 无 items 时展示摘要 -->
                <div class="item" v-else>
                  <div class="item-info">
                    <div class="item-name">共 {{ order.itemCount || order.totalQuantity || '-' }} 件商品</div>
                    <div class="item-specs" v-if="order.remark">备注：{{ order.remark }}</div>
                  </div>
                </div>
              </div>

              <!-- 卡片底部 -->
              <div class="card-footer">
                <div class="footer-left">
                  <span v-if="order.deliveryCompany && order.deliveryNo" class="delivery-info">
                    📦 {{ order.deliveryCompany }}：{{ order.deliveryNo }}
                  </span>
                </div>
                <div class="footer-right">
                  <div class="total-amount">
                    合计：<span class="amount">¥{{ (order.payAmount || order.totalAmount)?.toFixed(2) }}</span>
                  </div>
                  <div class="actions" @click.stop>
                    <!-- 待付款：支付 + 取消 -->
                    <template v-if="order.status === 1">
                      <el-button size="small" plain @click="handleCancel(order)">取消</el-button>
                      <el-button size="small" type="primary" @click="handlePay(order)">立即支付</el-button>
                    </template>

                    <!-- 待发货 -->
                    <template v-else-if="order.status === 2">
                      <span class="status-tip">⏳ 等待卖家发货</span>
                      <el-button size="small" plain @click="handleRefund(order)">申请退款</el-button>
                    </template>

                    <!-- 待收货：确认收货 -->
                    <template v-else-if="order.status === 3">
                      <el-button size="small" type="success" @click="handleReceive(order)">确认收货</el-button>
                      <el-button size="small" plain @click="handleRefund(order)">申请退款</el-button>
                    </template>

                    <!-- 已完成：删除 + 退款 -->
                    <template v-else-if="order.status === 4">
                      <el-button size="small" plain @click="handleRefund(order)">申请退款</el-button>
                      <el-button size="small" plain type="danger" @click="handleDelete(order)">删除订单</el-button>
                    </template>

                    <!-- 已取消：删除 -->
                    <template v-else-if="order.status === 5">
                      <span class="status-tip" v-if="order.cancelReason">取消原因：{{ order.cancelReason }}</span>
                      <el-button size="small" plain type="danger" @click="handleDelete(order)">删除订单</el-button>
                    </template>

                    <!-- 退款中：可取消退款 -->
                    <template v-else-if="order.status === 6">
                      <el-button size="small" plain @click="handleCancelRefund(order)">取消退款</el-button>
                      <span class="status-tip">⏳ 退款审核中...</span>
                    </template>

                    <!-- 已退款 -->
                    <template v-else-if="order.status === 7">
                      <span class="status-tip refund-done">✅ 已退款</span>
                    </template>

                    <!-- 已拒绝：可取消退款 -->
                    <template v-else-if="order.status === 8">
                      <el-button size="small" plain @click="handleCancelRefund(order)">取消退款</el-button>
                      <span class="status-tip" v-if="order.refundRejectReason">拒绝原因：{{ order.refundRejectReason }}</span>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-bar" v-if="pagination.total > 0">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :total="pagination.total"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="loadOrders"
              @current-change="loadOrders"
            />
          </div>
        </template>

        <!-- 空状态 -->
        <el-empty v-else description="暂无订单，快去挑选心仪的商品吧！" :image-size="160" />
      </div>
    </div>

    <!-- ===== 取消订单弹窗 ===== -->
    <el-dialog v-model="cancelVisible" title="取消订单" width="420px" :close-on-click-modal="false">
      <div class="dialog-body">
        <p class="dialog-tip">请选择取消该订单的原因：</p>
        <el-select v-model="cancelReason" placeholder="请选择取消原因" class="full-select">
          <el-option label="不想买了" value="不想买了" />
          <el-option label="商品价格太贵" value="商品价格太贵" />
          <el-option label="等待时间太长" value="等待时间太长" />
          <el-option label="商品信息有误" value="商品信息有误" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="cancelVisible = false">返回</el-button>
        <el-button type="primary" @click="confirmCancel" :loading="submitting">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- ===== 删除确认弹窗 ===== -->
    <el-dialog v-model="deleteVisible" title="删除订单" width="400px">
      <div class="dialog-body">
        <p class="dialog-tip warn">⚠️ 删除后不可恢复，确认删除该订单吗？</p>
      </div>
      <template #footer>
        <el-button @click="deleteVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="submitting">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- ===== 申请退款弹窗 ===== -->
    <el-dialog v-model="refundVisible" title="申请退款" width="420px" :close-on-click-modal="false">
      <div class="dialog-body">
        <p class="dialog-tip">请选择退款原因：</p>
        <el-select v-model="refundReason" placeholder="请选择退款原因" class="full-select">
          <el-option label="商品质量问题" value="商品质量问题" />
          <el-option label="商品与描述不符" value="商品与描述不符" />
          <el-option label="发错货" value="发错货" />
          <el-option label="不想要了" value="不想要了" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="refundVisible = false">返回</el-button>
        <el-button type="warning" @click="confirmRefund" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- ===== 订单详情弹窗 ===== -->
    <el-dialog
      v-model="detailVisible"
      :title="'订单详情 - ' + (detailOrder?.orderNo || '')"
      width="720px"
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" class="detail-wrap">
        <template v-if="orderDetail">
          <!-- 订单状态 -->
          <div class="detail-status">
            <el-tag :type="statusTag(orderDetail.order.status)" size="large" effect="dark">
              {{ orderDetail.statusDesc }}
            </el-tag>
            <el-tag
              v-if="orderDetail.order.status >= 6"
              :type="statusTag(orderDetail.order.status)"
              size="large"
              style="margin-left: 8px"
            >
              {{ statusText(orderDetail.order.status) }}
            </el-tag>
            <span class="pay-info" v-if="orderDetail.payTypeDesc">
              {{ orderDetail.payStatusDesc }} · {{ orderDetail.payTypeDesc }}
            </span>
          </div>

          <!-- 商品明细 -->
          <div class="detail-section">
            <h3>商品信息</h3>
            <div class="detail-items">
              <div class="detail-item" v-for="item in orderDetail.items" :key="item.id">
                <img class="d-item-img" :src="item.image || '/placeholder.png'" />
                <div class="d-item-info">
                  <div class="d-item-name">{{ item.name }}</div>
                  <div class="d-item-specs" v-if="item.specs">{{ item.specs }}</div>
                </div>
                <div class="d-item-price">¥{{ item.price?.toFixed(2) }}</div>
                <div class="d-item-qty">x{{ item.quantity }}</div>
                <div class="d-item-subtotal">¥{{ (item.price * item.quantity)?.toFixed(2) }}</div>
              </div>
            </div>
          </div>

          <!-- 订单信息 -->
          <div class="detail-section">
            <h3>订单信息</h3>
            <div class="info-grid">
              <div class="info-row">
                <span class="label">订单号</span>
                <span class="value">{{ orderDetail.order.orderNo }}</span>
              </div>
              <div class="info-row">
                <span class="label">下单时间</span>
                <span class="value">{{ orderDetail.order.createdAt }}</span>
              </div>
              <div class="info-row" v-if="orderDetail.order.payTime">
                <span class="label">支付时间</span>
                <span class="value">{{ orderDetail.order.payTime }}</span>
              </div>
              <div class="info-row">
                <span class="label">商品金额</span>
                <span class="value">¥{{ orderDetail.order.totalAmount?.toFixed(2) }}</span>
              </div>
              <div class="info-row" v-if="orderDetail.order.freightAmount">
                <span class="label">运费</span>
                <span class="value">¥{{ orderDetail.order.freightAmount?.toFixed(2) }}</span>
              </div>
              <div class="info-row" v-if="orderDetail.order.discountAmount">
                <span class="label">优惠</span>
                <span class="value discount">-¥{{ orderDetail.order.discountAmount?.toFixed(2) }}</span>
              </div>
              <div class="info-row total-row">
                <span class="label">实付金额</span>
                <span class="value pay-amount">¥{{ orderDetail.order.payAmount?.toFixed(2) }}</span>
              </div>
              <div class="info-row" v-if="orderDetail.order.remark">
                <span class="label">备注</span>
                <span class="value">{{ orderDetail.order.remark }}</span>
              </div>
              <div
                class="info-row"
                v-if="orderDetail.order.status === 8 && orderDetail.order.refundRejectReason"
              >
                <span class="label">拒绝原因</span>
                <span class="value" style="color:#c2413a">{{ orderDetail.order.refundRejectReason }}</span>
              </div>
              <div class="info-row" v-if="orderDetail.order.refundRejectTime">
                <span class="label">拒绝时间</span>
                <span class="value">{{ orderDetail.order.refundRejectTime }}</span>
              </div>
            </div>
          </div>

          <!-- 收货信息 -->
          <div class="detail-section">
            <h3>收货信息</h3>
            <div class="info-grid">
              <div class="info-row">
                <span class="label">收货人</span>
                <span class="value">{{ orderDetail.order.receiverName }}</span>
              </div>
              <div class="info-row">
                <span class="label">联系电话</span>
                <span class="value">{{ orderDetail.order.receiverPhone }}</span>
              </div>
              <div class="info-row">
                <span class="label">收货地址</span>
                <span class="value">{{ orderDetail.order.receiverAddress }}</span>
              </div>
            </div>
          </div>

          <!-- 发货记录 -->
          <div class="detail-section" v-if="orderDetail.deliveries && orderDetail.deliveries.length > 0">
            <h3>发货记录</h3>
            <div class="deliveries">
              <div class="delivery-row" v-for="(del, idx) in orderDetail.deliveries" :key="idx">
                <span class="del-icon">📦</span>
                <span>{{ del.deliveryCompany || del.company }}{{ del.deliveryNo || del.trackingNo ? '：' + (del.deliveryNo || del.trackingNo) : '' }}</span>
              </div>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!detailLoading" description="暂无订单详情" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
console.log('===== OrderListView 组件已加载 =====')

import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import {
  getOrderList,
  payOrder,
  cancelOrder,
  confirmReceive,
  deleteOrder,
  applyRefund,
  cancelRefund,
  getOrderDetail,
  type Order,
  type OrderDetail,
  type PageResult
} from '@/api/order'

const router = useRouter()

// ----- 状态 -----
const activeTab = ref('all')
const loading = ref(false)
const orders = ref<Order[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 取消弹窗
const cancelVisible = ref(false)
const cancelReason = ref('')
const cancelTarget = ref<Order | null>(null)

// 删除弹窗
const deleteVisible = ref(false)
const deleteTarget = ref<Order | null>(null)

// 退款弹窗
const refundVisible = ref(false)
const refundReason = ref('')
const refundTarget = ref<Order | null>(null)

// 详情弹窗
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailOrder = ref<Order | null>(null)
const orderDetail = ref<OrderDetail | null>(null)

const submitting = ref(false)

// ----- 常量映射 -----
const tabStatusMap: Record<string, number | undefined> = {
  all: undefined,
  pending_pay: 1,
  pending_ship: 2,
  pending_receive: 3,
  completed: 4,
  cancelled: 5
}

const statusMap: Record<number, { text: string; tag: string }> = {
  1: { text: '待付款', tag: 'warning' },
  2: { text: '待发货', tag: 'info' },
  3: { text: '待收货', tag: 'primary' },
  4: { text: '已完成', tag: 'success' },
  5: { text: '已取消', tag: 'danger' },
  6: { text: '退款中', tag: 'warning' },
  7: { text: '已退款', tag: 'success' },
  8: { text: '已拒绝', tag: 'danger' }
}

const statusText = (s: number) => statusMap[s]?.text || '未知'
const statusTag = (s: number) => (statusMap[s]?.tag as any) || 'info'

// ----- 数据加载 -----
const loadOrders = async () => {
  loading.value = true
  try {
    const status = tabStatusMap[activeTab.value]
    console.log('[订单] 请求参数:', { status, page: pagination.page, pageSize: pagination.pageSize })
    const result = await getOrderList({
      status,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    console.log('[订单] 响应数据:', result)
    // 响应经过拦截器已提取 res.data = { list, page, pageSize, total }
    const rawList = result.list || result.records || []
    orders.value = Array.isArray(rawList) ? rawList : []
    pagination.total = result.total || orders.value.length || 0
    console.log('[订单] 加载完成:', { count: orders.value.length, total: pagination.total })
  } catch (e: any) {
    console.error('[订单] 加载失败:', e?.message || e)
    orders.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  pagination.page = 1
  loadOrders()
}

// ----- 操作 -----
// 支付
const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认支付订单 ${order.orderNo} 吗？`, '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
    submitting.value = true
    await payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '支付失败')
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = (order: Order) => {
  cancelTarget.value = order
  cancelReason.value = ''
  cancelVisible.value = true
}

const confirmCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请选择取消原因')
    return
  }
  try {
    submitting.value = true
    await cancelOrder(cancelTarget.value!.id)
    ElMessage.success('订单已取消')
    cancelVisible.value = false
    loadOrders()
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

// 确认收货
const handleReceive = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认已收到 ${order.orderNo} 的商品吗？`, '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '再想想',
      type: 'success'
    })
    submitting.value = true
    await confirmReceive(order.id)
    ElMessage.success('收货成功，感谢您的购买！')
    loadOrders()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '收货失败')
  } finally {
    submitting.value = false
  }
}

// 删除
const handleDelete = (order: Order) => {
  deleteTarget.value = order
  deleteVisible.value = true
}

const confirmDelete = async () => {
  try {
    submitting.value = true
    await deleteOrder(deleteTarget.value!.id)
    ElMessage.success('订单已删除')
    deleteVisible.value = false
    loadOrders()
 } catch {
    /* 错误已由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

// 退款
const handleRefund = (order: Order) => {
  refundTarget.value = order
  refundReason.value = ''
  refundVisible.value = true
}

const confirmRefund = async () => {
  if (!refundReason.value) {
    ElMessage.warning('请选择退款原因')
    return
  }
  try {
    submitting.value = true
    await applyRefund(refundTarget.value!.id, refundReason.value)
    ElMessage.success('退款申请已提交，请等待审核')
    refundVisible.value = false
    loadOrders()
 } catch {
    /* 错误已由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

// 取消退款
const handleCancelRefund = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确定要取消订单 ${order.orderNo} 的退款申请吗？`, '取消退款', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    submitting.value = true
    await cancelRefund(order.id)
    ElMessage.success('退款申请已取消，订单已恢复')
    loadOrders()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '取消退款失败')
  } finally {
    submitting.value = false
  }
}

// 查看详情
const viewDetail = async (order: Order) => {
  detailOrder.value = order
  detailVisible.value = true
  detailLoading.value = true
  orderDetail.value = null
  try {
    const detail = await getOrderDetail(order.orderNo)
    orderDetail.value = detail
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    detailLoading.value = false
  }
}

// ----- 工具 -----
const formatTime = (t?: string) => {
  if (!t) return '-'
  try {
    const d = new Date(t)
    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return t
  }
}

const goHome = () => router.push('/home')

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
/* === 页面布局 === */
.order-page {
  min-height: 100vh;
  background: #f4f6f9;
  padding-top: 60px;
}

.page-body {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 20px 60px;
}

/* === 标题 === */
.page-title {
  margin-bottom: 20px;
}

.page-title h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 4px;
}

.title-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

/* === 标签 === */
.status-tabs {
  background: #fff;
  border-radius: 10px;
  padding: 0 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.status-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.status-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

/* === 空状态 === */
.empty-state {
  padding: 80px 0;
  text-align: center;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* === 加载状态 === */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  color: #909399;
  font-size: 14px;
}

.loading-state .loading-icon {
  margin-bottom: 12px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* === 列表容器 === */
.order-list-wrap {
  min-height: 200px;
}

/* === 卡片列表 === */
.order-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.order-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.15s;
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #fafbfc;
  border-bottom: 1px solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-no {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.order-time {
  font-size: 12px;
  color: #aaa;
}

.status-tag {
  font-size: 13px;
  font-weight: 600;
}

/* 商品列表 */
.card-items {
  padding: 12px 20px;
}

.item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.item + .item {
  border-top: 1px solid #f5f5f5;
}

.item-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f5f5f5;
}

.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-specs {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
}

.item-price {
  text-align: right;
  flex-shrink: 0;
}

.price {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.item-qty {
  font-size: 13px;
  color: #909399;
  width: 40px;
  text-align: right;
  flex-shrink: 0;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafbfc;
}

.footer-left {
  flex: 1;
  min-width: 0;
}

.delivery-info {
  font-size: 12px;
  color: #909399;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.total-amount {
  font-size: 13px;
  color: #666;
}

.amount {
  font-size: 18px;
  font-weight: 700;
  color: #ff4400;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.status-tip {
  font-size: 14px;
  color: #909399;
}

.status-tip.refund-done {
  color: #67c23a;
}

/* === 分页 === */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

/* === 弹窗 === */
.dialog-body {
  padding: 8px 0;
}

.dialog-tip {
  font-size: 14px;
  color: #606266;
  margin: 0 0 16px;
}

.dialog-tip.warn {
  color: #e6a23c;
}

.full-select {
  width: 100%;
}

/* === 详情弹窗 === */
.detail-wrap {
  min-height: 200px;
}

.detail-status {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 16px;
  margin-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.pay-info {
  font-size: 13px;
  color: #909399;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.detail-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f8f9fb;
  border-radius: 8px;
}

.d-item-img {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  object-fit: cover;
  background: #eee;
  flex-shrink: 0;
}

.d-item-info {
  flex: 1;
  min-width: 0;
}

.d-item-name {
  font-size: 13px;
  color: #303133;
  font-weight: 500;
}

.d-item-specs {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.d-item-price,
.d-item-qty {
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.d-item-subtotal {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: baseline;
}

.info-row .label {
  width: 80px;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.info-row .value {
  font-size: 13px;
  color: #303133;
  flex: 1;
}

.info-row.total-row {
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}

.info-row .value.pay-amount {
  font-size: 16px;
  font-weight: 700;
  color: #ff4400;
}

.info-row .value.discount {
  color: #67c23a;
}

.deliveries {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.delivery-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.del-icon {
  font-size: 16px;
}

/* === 响应式 === */
@media screen and (max-width: 640px) {
  .page-body {
    padding: 12px 10px 40px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .card-footer {
    flex-direction: column;
    align-items: flex-end;
    gap: 10px;
  }

  .footer-right {
    flex-direction: column;
    align-items: flex-end;
  }

  .item {
    flex-wrap: wrap;
  }

  .detail-item {
    flex-wrap: wrap;
  }

  .d-item-subtotal {
    width: auto;
  }
}
</style>