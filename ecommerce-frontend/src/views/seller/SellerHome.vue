<template>
  <div class="seller-home" v-loading="loading">
    <!-- Bento 仪表盘 -->
    <section class="dashboard-bento">
      <!-- 欢迎卡 -->
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <p class="welcome-date">{{ todayDate }}</p>
          <h1>欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h1>
          <p class="welcome-sub">这是您今日的店铺概览。数据实时汇总，助您掌控经营全局。</p>
        </div>
        <div class="welcome-decoration">
          <div class="decoration-ring" />
          <div class="decoration-dot" />
        </div>
      </div>

      <!-- 今日销售高亮 -->
      <div class="bento-card highlight-card">
        <div class="highlight-label">今日销售额</div>
        <div class="highlight-value">¥{{ formatMoney(kpi.today) }}</div>
        <div class="highlight-meta">
          <span class="highlight-profit">利润 ¥{{ formatMoney(kpi.todayProfit) }}</span>
          <span class="highlight-tag">实时</span>
        </div>
      </div>

      <!-- 迷你 KPI -->
      <div class="bento-card kpi-mini week">
        <span class="kpi-mini-label">近7天销售</span>
        <span class="kpi-mini-value">¥{{ formatMoney(kpi.last7Days) }}</span>
        <span class="kpi-mini-profit">利润 ¥{{ formatMoney(kpi.last7DaysProfit) }}</span>
      </div>
      <div class="bento-card kpi-mini month">
        <span class="kpi-mini-label">本月销售</span>
        <span class="kpi-mini-value">¥{{ formatMoney(kpi.thisMonth) }}</span>
        <span class="kpi-mini-profit">利润 ¥{{ formatMoney(kpi.thisMonthProfit) }}</span>
      </div>
      <div class="bento-card kpi-mini year">
        <span class="kpi-mini-label">本年销售</span>
        <span class="kpi-mini-value">¥{{ formatMoney(kpi.thisYear) }}</span>
        <span class="kpi-mini-profit">利润 ¥{{ formatMoney(kpi.thisYearProfit) }}</span>
      </div>
      <div class="bento-card kpi-mini products">
        <span class="kpi-mini-label">在售商品</span>
        <span class="kpi-mini-value">{{ productRanking.length }}</span>
        <span class="kpi-mini-profit">产生过销售记录</span>
      </div>

      <!-- 商品销售分析标题 + 周期切换 -->
      <div class="analysis-header">
        <div class="analysis-title">
          <h2>商品销售分析</h2>
          <p>切换时间维度查看不同周期表现</p>
        </div>
        <el-radio-group v-model="rankingPeriod" @change="handlePeriodChange" size="small" class="period-radio">
          <el-radio-button value="today">今日</el-radio-button>
          <el-radio-button value="last7Days">近7日</el-radio-button>
          <el-radio-button value="thisMonth">本月</el-radio-button>
          <el-radio-button value="thisYear">本年</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 销售趋势 -->
      <div class="bento-card trend-card">
        <div class="card-header">
          <div>
            <h3>销售趋势</h3>
            <p>切换周期与指标查看经营走势</p>
          </div>
          <div class="trend-controls">
            <el-radio-group v-model="trendMetric" @change="handleTrendMetricChange" size="small" class="metric-radio">
              <el-radio-button value="salesAmount">销售额</el-radio-button>
              <el-radio-button value="orderCount">订单量</el-radio-button>
              <el-radio-button value="salesVolume">销量</el-radio-button>
            </el-radio-group>
            <el-select v-model="trendPeriod" @change="handleTrendPeriodChange" size="small" class="period-select">
              <el-option
                v-for="opt in trendPeriodOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-area"></div>
      </div>

      <!-- 热销排行 -->
      <div class="bento-card ranking-card">
        <div class="card-header">
          <div>
            <h3>热销排行</h3>
            <p>{{ rankBy === 'count' ? '按销量 TOP 商品' : '按销售额 TOP 商品' }}</p>
          </div>
          <button class="ranking-toggle" @click="toggleRankBy" title="切换排行维度">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
            </svg>
            <span>{{ rankBy === 'count' ? '按销量' : '按金额' }}</span>
          </button>
        </div>
        <div class="ranking-list" v-if="productRanking.length > 0">
          <div
            v-for="(item, index) in productRanking.slice(0, 6)"
            :key="item.productId"
            class="ranking-item"
          >
            <span class="ranking-index" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <div class="ranking-info">
              <div class="ranking-name" :title="item.name">{{ item.name }}</div>
              <div class="ranking-bar">
                <div
                  class="ranking-bar-fill"
                  :style="{ width: (rankBy === 'count' ? item.countPercentOfTotal : item.percentOfTotal) + '%' }"
                ></div>
              </div>
            </div>
            <span class="ranking-amount">
              {{ rankBy === 'count' ? `${item.salesCount} 件` : `¥${formatMoney(item.salesAmount)}` }}
            </span>
          </div>
        </div>
        <div v-else class="ranking-empty">暂无销售数据</div>
      </div>

      <!-- 销量占比 -->
      <div class="bento-card chart-half">
        <div class="card-header">
          <div>
            <h3>销量占比</h3>
            <p>按销量分布</p>
          </div>
        </div>
        <div ref="roseChartRef" class="chart-area"></div>
      </div>

      <!-- 商品销售对比 -->
      <div class="bento-card chart-half">
        <div class="card-header">
          <div>
            <h3>销售对比</h3>
            <p>各商品销售额</p>
          </div>
        </div>
        <div ref="barChartRef" class="chart-area"></div>
      </div>

      <!-- 店铺信息 -->
      <div class="bento-card store-card">
        <div class="card-header">
          <div>
            <h3>店铺信息</h3>
            <p>当前店铺状态与基本信息</p>
          </div>
        </div>
        <div class="store-fields" v-if="storeInfo">
          <div class="store-field">
            <span>店铺名称</span>
            <strong>{{ storeInfo.name }}</strong>
          </div>
          <div class="store-field">
            <span>店铺 ID</span>
            <strong>#{{ storeInfo.id }}</strong>
          </div>
          <div class="store-field">
            <span>店铺状态</span>
            <strong>
              <span class="store-status-badge" :class="storeInfo.status === 1 ? 'active' : 'inactive'">
                <span class="store-status-dot" />
                {{ storeInfo.status === 1 ? '营业中' : '已关闭' }}
              </span>
            </strong>
          </div>
          <div class="store-field">
            <span>创建时间</span>
            <strong>{{ formatDate(storeInfo.createdAt) }}</strong>
          </div>
        </div>
        <div class="store-row" v-if="storeInfo?.address">
          <span>店铺地址</span>
          <p>{{ storeInfo.address }}</p>
        </div>
        <div class="store-row" v-if="storeInfo?.description">
          <span>店铺描述</span>
          <p>{{ storeInfo.description }}</p>
        </div>
        <div v-else-if="!storeInfo" class="store-empty">暂无店铺信息</div>
      </div>

      <!-- 快捷操作 -->
      <div class="bento-card actions-card">
        <div class="card-header">
          <div>
            <h3>快捷操作</h3>
            <p>常用功能入口</p>
          </div>
        </div>
        <div class="action-list">
          <div class="action-item" @click="goTo('/seller/products')">
            <div class="action-item-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                <polyline points="3.27 6.96 12 12.01 20.73 6.96"/>
                <line x1="12" y1="22.08" x2="12" y2="12"/>
              </svg>
            </div>
            <div class="action-item-body">
              <strong>商品管理</strong>
              <span>管理在售商品与库存</span>
            </div>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
          <div class="action-item" @click="goTo('/seller/orders')">
            <div class="action-item-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </div>
            <div class="action-item-body">
              <strong>订单管理</strong>
              <span>查看订单与处理售后</span>
            </div>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
          <div class="action-item" @click="goTo('/seller/profile')">
            <div class="action-item-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <div class="action-item-body">
              <strong>店铺设置</strong>
              <span>完善资料与店铺形象</span>
            </div>
            <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </div>
        </div>
      </div>
    </section>

    <!-- 危险操作 -->
    <section class="danger-section">
      <div class="danger-content">
        <svg class="danger-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
          <line x1="12" y1="9" x2="12" y2="13"/>
          <line x1="12" y1="17" x2="12.01" y2="17"/>
        </svg>
        <div>
          <strong>注销店铺</strong>
          <p>注销后店铺将停止营业，所有商品下架。操作不可撤销，请谨慎处理。</p>
        </div>
      </div>
      <el-button
        type="danger"
        :loading="deactivating"
        :disabled="!storeInfo || storeInfo.status === 0"
        @click="openDeactivateDialog"
        class="deactivate-btn"
      >
        注销店铺
      </el-button>
    </section>

    <!-- 注销确认对话框 -->
    <el-dialog
      v-model="deactivateDialogVisible"
      title="注销店铺确认"
      width="480px"
      destroy-on-close
      class="deactivate-dialog"
    >
      <div class="deactivate-dialog-body">
        <div class="deactivate-warning-banner">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#B85C5C" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <div>
            <strong>此操作不可撤销</strong>
            <p>注销后店铺将无法正常使用，所有商品会下架处理。</p>
          </div>
        </div>

        <div class="deactivate-info">
          <p>即将注销：<strong>{{ storeInfo?.name || '—' }}</strong></p>
        </div>

        <div class="deactivate-checklist">
          <p class="checklist-title">注销前请确认：</p>
          <label class="checklist-item">
            <span class="checklist-marker">①</span>
            <span>店铺下所有订单已完结</span>
          </label>
          <label class="checklist-item">
            <span class="checklist-marker">②</span>
            <span>已处理完所有售后和退款</span>
          </label>
          <label class="checklist-item">
            <span class="checklist-marker">③</span>
            <span>确认不再需要使用该店铺</span>
          </label>
        </div>

        <div class="deactivate-confirm-input">
          <p class="confirm-label">请输入 <strong>确认注销</strong> 以继续：</p>
          <el-input
            v-model="deactivateConfirmText"
            placeholder="请输入「确认注销」"
            maxlength="10"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="deactivateDialogVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="deactivating"
          :disabled="deactivateConfirmText !== '确认注销'"
          @click="handleDeactivate"
        >
          确认注销店铺
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import {
  getSalesKpiOverview,
  getSalesTrend,
  getSalesTimeSeries,
  getProductRanking,
  type KpiOverview,
  type SalesTrend,
  type TimeSeriesData,
  type ProductRankItem
} from '@/api/dashboard'
import { getMyStore, deactivateStore, type Store } from '@/api/shop'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const kpi = reactive<KpiOverview>({
  today: 0,
  last7Days: 0,
  thisMonth: 0,
  thisYear: 0,
  todayProfit: 0,
  last7DaysProfit: 0,
  thisMonthProfit: 0,
  thisYearProfit: 0
})

const salesTrend = ref<SalesTrend>({ dates: [], values: [] })
const timeSeriesData = ref<TimeSeriesData>({ period: 'last7Days', dataPoints: [] })
const trendPeriod = ref('last7Days')
const trendMetric = ref<'salesAmount' | 'orderCount' | 'salesVolume'>('salesAmount')
const productRanking = ref<ProductRankItem[]>([])
const rankingPeriod = ref('last7Days')
const rankBy = ref<'count' | 'amount'>('count')

const trendPeriodOptions = [
  { value: 'last24h', label: '近24小时' },
  { value: 'last7Days', label: '近7日' },
  { value: 'thisMonth', label: '本月' },
  { value: 'last90Days', label: '近90天' },
  { value: 'thisYear', label: '本年' }
]

const trendMetricOptions = [
  { value: 'salesAmount', label: '销售额' },
  { value: 'orderCount', label: '订单量' },
  { value: 'salesVolume', label: '销量' }
]

const handlePeriodChange = async () => {
  await loadProductRanking()
}

const handleTrendPeriodChange = async () => {
  await loadSalesTrend()
  renderTrendChart()
}

const handleTrendMetricChange = () => {
  renderTrendChart()
}

const toggleRankBy = async () => {
  rankBy.value = rankBy.value === 'count' ? 'amount' : 'count'
  await loadProductRanking()
}

// ====== 店铺注销 ======
const storeInfo = ref<Store | null>(null)
const deactivating = ref(false)
const deactivateDialogVisible = ref(false)
const deactivateConfirmText = ref('')

const todayDate = computed(() => {
  const d = new Date()
  const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · ${week[d.getDay()]}`
})

const loadStoreInfo = async () => {
  try {
    storeInfo.value = await getMyStore()
  } catch {
    // 可能没有店铺
  }
}

const openDeactivateDialog = () => {
  deactivateConfirmText.value = ''
  deactivateDialogVisible.value = true
}

const handleDeactivate = async () => {
  if (deactivateConfirmText.value !== '确认注销') {
    ElMessage.warning('请输入"确认注销"以确认操作')
    return
  }
  if (!storeInfo.value?.id) {
    ElMessage.error('店铺信息异常')
    return
  }
  deactivating.value = true
  try {
    await deactivateStore(storeInfo.value.id)
    ElMessage.success('店铺已注销')
    deactivateDialogVisible.value = false
    storeInfo.value.status = 0

    // 清除本地角色缓存 + 强制从后端拉取最新用户信息
    localStorage.removeItem('user_roles')
    await userStore.fetchUserInfo(true)

    setTimeout(() => {
      router.push('/')
    }, 1500)
  } catch {
    // 错误已由拦截器处理
  } finally {
    deactivating.value = false
  }
}

const trendChartRef = ref<HTMLDivElement | null>(null)
const roseChartRef = ref<HTMLDivElement | null>(null)
const barChartRef = ref<HTMLDivElement | null>(null)

let trendChart: echarts.ECharts | null = null
let roseChart: echarts.ECharts | null = null
let barChart: echarts.ECharts | null = null

const formatMoney = (val: number): string => {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

const formatLargeMoney = (val: number): string => {
  if (val == null) return '0'
  const num = Number(val)
  if (num >= 100000000) return (num / 100000000).toFixed(1) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toFixed(2)
}

const formatDate = (val?: string): string => {
  if (!val) return '—'
  // 兼容 ISO 8601 格式，如 2024-01-01T10:30:00
  const d = new Date(val)
  if (isNaN(d.getTime())) return val
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const loadKpi = async () => {
  try {
    const data = await getSalesKpiOverview()
    kpi.today = data.today || 0
    kpi.last7Days = data.last7Days || 0
    kpi.thisMonth = data.thisMonth || 0
    kpi.thisYear = data.thisYear || 0
    kpi.todayProfit = data.todayProfit || 0
    kpi.last7DaysProfit = data.last7DaysProfit || 0
    kpi.thisMonthProfit = data.thisMonthProfit || 0
    kpi.thisYearProfit = data.thisYearProfit || 0
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const loadSalesTrend = async () => {
  try {
    const data = await getSalesTimeSeries(trendPeriod.value)
    timeSeriesData.value = data
    // 保持旧字段兼容（部分图表/逻辑可能仍依赖 salesTrend）
    salesTrend.value = {
      dates: data.dataPoints.map(p => p.label),
      values: data.dataPoints.map(p => p.salesAmount)
    }
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const loadProductRanking = async () => {
  try {
    productRanking.value = await getProductRanking(rankingPeriod.value, rankBy.value)
    await nextTick()
    initCharts()
    await nextTick()
    renderRoseChart()
    renderBarChart()
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const initCharts = () => {
  if (!trendChart && trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
  }
  if (!roseChart && roseChartRef.value) {
    roseChart = echarts.init(roseChartRef.value)
  }
  if (!barChart && barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
}

// 克制的高级配色：暖灰白底 + 青铜褐强调 + 低饱和辅助色
const palette = {
  bg: '#FAFAF9',
  surface: '#FFFFFF',
  border: '#E5E5E0',
  primary: '#1C1C1E',
  secondary: '#6B6B6E',
  muted: '#A1A1AA',
  accent: '#3B6E6E',
  accentLight: '#5A8F8F',
  accentPale: 'rgba(59, 110, 110, 0.08)',
  accentAreaTop: 'rgba(59, 110, 110, 0.16)',
  accentAreaBottom: 'rgba(59, 110, 110, 0.02)',
  clay: '#C67C5C',
  sand: '#C8A464',
  sage: '#7A9E7A',
  rose: ['#3B6E6E', '#C67C5C', '#C8A464', '#7A9E7A', '#5A8F8F', '#A98C7B', '#8FA8A8', '#6B8E8E', '#B8A58C', '#7E8E8E'],
  success: '#5A7D5A',
  danger: '#B85C5C',
  grid: '#F0F0EE'
}

const metricConfig = {
  salesAmount: {
    label: '销售额',
    unit: '¥',
    color: palette.accent,
    areaTop: palette.accentAreaTop,
    areaBottom: palette.accentAreaBottom,
    formatter: (v: number) => '¥' + formatLargeMoney(v)
  },
  orderCount: {
    label: '订单量',
    unit: '单',
    color: palette.clay,
    areaTop: 'rgba(198, 124, 92, 0.16)',
    areaBottom: 'rgba(198, 124, 92, 0.02)',
    formatter: (v: number) => v + ' 单'
  },
  salesVolume: {
    label: '销量',
    unit: '件',
    color: palette.sage,
    areaTop: 'rgba(122, 158, 122, 0.16)',
    areaBottom: 'rgba(122, 158, 122, 0.02)',
    formatter: (v: number) => v + ' 件'
  }
}

const renderTrendChart = () => {
  if (!trendChart) return
  const points = timeSeriesData.value.dataPoints
  if (!points || points.length === 0) return

  const config = metricConfig[trendMetric.value]
  const labels = points.map(p => p.label)
  const values = points.map(p => p[trendMetric.value])

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: palette.border,
      borderWidth: 1,
      textStyle: { color: palette.primary },
      formatter: (params: any) => {
        const p = params[0]
        const value = Number(p.value)
        const formatted = trendMetric.value === 'salesAmount' ? '¥' + formatMoney(value) : value + ' ' + config.unit.replace(' ', '')
        return `${p.axisValue}<br/>${config.label}：<b style="color:${config.color}">${formatted}</b>`
      }
    },
    grid: { top: 24, right: 24, bottom: 28, left: 66 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: palette.border } },
      axisLabel: { color: palette.secondary, fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (v: number) => {
          if (trendMetric.value === 'salesAmount') return '¥' + formatLargeMoney(v)
          return String(v)
        },
        color: palette.muted,
        fontSize: 11
      },
      splitLine: { lineStyle: { color: palette.grid } }
    },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 2.5,
        color: config.color
      },
      itemStyle: {
        color: config.color,
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: config.areaTop },
          { offset: 1, color: config.areaBottom }
        ])
      }
    }]
  }, true)
}

const renderRoseChart = () => {
  if (!roseChart) return
  const data = productRanking.value
  if (!data || data.length === 0) {
    roseChart.clear()
    roseChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: palette.muted, fontSize: 14, fontWeight: 500 }
      }
    })
    return
  }

  roseChart.setOption({
    title: { show: false },
    color: palette.rose,
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: palette.border,
      borderWidth: 1,
      textStyle: { color: palette.primary },
      formatter: (params: any) => {
        return `${params.name}<br/>销量：<b>${params.value} 件</b>`
      }
    },
    legend: {
      type: 'scroll',
      bottom: 0,
      textStyle: { fontSize: 10, color: palette.secondary },
      pageIconColor: palette.accent,
      pageTextStyle: { color: palette.secondary }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['50%', '44%'],
      itemStyle: {
        borderRadius: 5,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 10,
        color: palette.secondary
      },
      emphasis: {
        label: { fontSize: 12, fontWeight: 'bold', color: palette.primary },
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(59, 110, 110, 0.2)'
        }
      },
      data: data.map(item => ({
        name: item.name,
        value: item.salesCount
      }))
    }]
  }, true)
}

const renderBarChart = () => {
  if (!barChart) return
  const data = productRanking.value
  if (!data || data.length === 0) {
    barChart.clear()
    barChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: palette.muted, fontSize: 14, fontWeight: 500 }
      }
    })
    return
  }

  const names = data.map(item => item.name)
  const amounts = data.map(item => item.salesAmount)

  const barGradients = [
    ['#5A8F8F', '#3B6E6E'],
    ['#D08C6C', '#C67C5C'],
    ['#D8B474', '#C8A464'],
    ['#8AAE8A', '#7A9E7A'],
    ['#6A9F9F', '#5A8F8F'],
    ['#B99C8B', '#A98C7B'],
    ['#9FB8B8', '#8FA8A8'],
    ['#7B9E9E', '#6B8E8E']
  ]

  barChart.setOption({
    title: { show: false },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: palette.border,
      borderWidth: 1,
      textStyle: { color: palette.primary },
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(59, 110, 110, 0.06)' } },
      formatter: (params: any) => {
        const p = params[0]
        const idx = p.dataIndex % barGradients.length
        const color = barGradients[idx]![1]
        return `${p.name}<br/>销售额：<b style="color:${color}">¥${Number(p.value).toFixed(2)}</b>`
      }
    },
    grid: { top: 16, right: 20, bottom: data.length > 5 ? 80 : 40, left: 60 },
    xAxis: {
      type: 'category',
      data: names,
      axisLine: { lineStyle: { color: palette.border } },
      axisLabel: {
        rotate: data.length > 5 ? 35 : 0,
        fontSize: 10,
        interval: 0,
        color: palette.secondary
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (v: number) => '¥' + formatLargeMoney(v), color: palette.muted, fontSize: 10 },
      splitLine: { lineStyle: { color: palette.grid } }
    },
    series: [{
      type: 'bar',
      data: amounts.map((val, index) => {
        const grad = barGradients[index % barGradients.length]!
        return {
          value: val,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: grad[0]! },
              { offset: 1, color: grad[1]! }
            ]),
            borderRadius: [4, 4, 0, 0]
          }
        }
      }),
      barWidth: data.length > 5 ? '44%' : '32%',
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowColor: 'rgba(0, 0, 0, 0.12)'
        }
      }
    }]
  }, true)
}

const handleResize = () => {
  trendChart?.resize()
  roseChart?.resize()
  barChart?.resize()
}

const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadKpi(),
      loadSalesTrend(),
      loadProductRanking()
    ])
  } finally {
    loading.value = false
    await nextTick()
    initCharts()
    await nextTick()
    renderTrendChart()
    renderRoseChart()
    renderBarChart()
    window.dispatchEvent(new Event('resize'))
  }
}

const goTo = (path: string) => {
  router.push(path)
}

const cleanup = () => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  roseChart?.dispose()
  barChart?.dispose()
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadAllData()
  loadStoreInfo()
})

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
.seller-home {
  padding: 28px;
  max-width: 1400px;
  margin: 0 auto;
  background: #FAFAF9;
  min-height: 100vh;
  color: #1C1C1E;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ---------- Bento 网格 ---------- */
.dashboard-bento {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.analysis-header {
  grid-column: span 12;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 8px 4px 0;
  flex-wrap: wrap;
}

.analysis-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.analysis-title h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1C1C1E;
}

.analysis-title p {
  margin: 0;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.period-radio :deep(.el-radio-button__inner) {
  border-color: #E5E5E0;
  color: #6B6B6E;
  font-weight: 500;
  background: #FFFFFF;
}

.period-radio :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #3B6E6E;
  border-color: #3B6E6E;
  color: #FFFFFF;
  box-shadow: -1px 0 0 0 #3B6E6E;
}

.period-radio :deep(.el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px 0 0 8px;
}

.period-radio :deep(.el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 8px 8px 0;
}

.bento-card {
  background: #FFFFFF;
  border-radius: 18px;
  padding: 24px;
  border: 1px solid #E5E5E0;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.bento-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.05);
  border-color: #D8D8D2;
}

/* 欢迎卡 */
.welcome-card {
  grid-column: span 8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #FFFFFF 0%, #F5F5F4 100%);
  position: relative;
  overflow: hidden;
}

.welcome-date {
  margin: 0 0 8px;
  font-size: 12px;
  color: #3B6E6E;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.welcome-card h1 {
  margin: 0 0 10px;
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1C1C1E;
}

.welcome-sub {
  margin: 0;
  font-size: 14px;
  color: #6B6B6E;
  line-height: 1.5;
  max-width: 420px;
}

.welcome-decoration {
  position: relative;
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  margin-left: 24px;
}

.decoration-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 1.5px solid #E5E5E0;
}

.decoration-ring::before {
  content: '';
  position: absolute;
  inset: 14px;
  border-radius: 50%;
  border: 1.5px solid #D8D8D2;
}

.decoration-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 12px;
  height: 12px;
  margin: -6px 0 0 -6px;
  border-radius: 50%;
  background: #3B6E6E;
}

/* 高亮卡 */
.highlight-card {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #1C1C1E;
  border-color: #1C1C1E;
  color: #FFFFFF;
}

.highlight-card:hover {
  border-color: #2C2C2E;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.12);
}

.highlight-label {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
  margin-bottom: 10px;
}

.highlight-value {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin-bottom: 12px;
  color: #7FBFBF;
}

.highlight-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.highlight-profit {
  font-size: 13px;
  color: #C8A464;
}

.highlight-tag {
  font-size: 10px;
  color: #FFFFFF;
  background: #3B6E6E;
  padding: 3px 10px;
  border-radius: 20px;
  font-weight: 700;
}

.bento-card {
  position: relative;
}

/* 迷你 KPI */
.kpi-mini {
  grid-column: span 3;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kpi-mini::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  border-radius: 18px 0 0 18px;
}

.kpi-mini.week::before { background: #3B6E6E; }
.kpi-mini.month::before { background: #C67C5C; }
.kpi-mini.year::before { background: #C8A464; }
.kpi-mini.products::before { background: #7A9E7A; }

.kpi-mini-label {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.kpi-mini-value {
  font-size: 22px;
  font-weight: 700;
  color: #1C1C1E;
  letter-spacing: -0.02em;
}

.kpi-mini-profit {
  font-size: 12px;
  color: #6B6B6E;
}

.kpi-mini.week .kpi-mini-profit { color: #3B6E6E; }
.kpi-mini.month .kpi-mini-profit { color: #C67C5C; }
.kpi-mini.year .kpi-mini-profit { color: #C8A464; }
.kpi-mini.products .kpi-mini-profit { color: #7A9E7A; }

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 18px;
}

.card-header h3 {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 700;
  color: #1C1C1E;
}

.card-header p {
  margin: 0;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

/* 趋势图 */
.trend-card {
  grid-column: span 8;
}

.trend-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.metric-radio :deep(.el-radio-button__inner) {
  border-color: #E5E5E0;
  color: #6B6B6E;
  font-weight: 600;
  background: #FFFFFF;
}

.metric-radio :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: #3B6E6E;
  border-color: #3B6E6E;
  color: #FFFFFF;
  box-shadow: -1px 0 0 0 #3B6E6E;
}

.period-select {
  width: 110px;
}

.period-select :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E5E5E0 inset;
  border-radius: 8px;
}

.period-select :deep(.el-input__wrapper:hover),
.period-select :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #3B6E6E inset;
}

.chart-area {
  width: 100%;
  height: 300px;
}

/* 排行卡 */
.ranking-card {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
}

.ranking-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: 1px solid #E5E5E0;
  border-radius: 8px;
  background: #FFFFFF;
  color: #6B6B6E;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.ranking-toggle:hover {
  border-color: #3B6E6E;
  color: #3B6E6E;
  background: rgba(59, 110, 110, 0.06);
}

.ranking-toggle svg {
  width: 14px;
  height: 14px;
}

.ranking-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ranking-index {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #6B6B6E;
  background: #F5F5F4;
  flex-shrink: 0;
}

.ranking-index.top {
  background: #3B6E6E;
  color: #FFFFFF;
}

.ranking-info {
  flex: 1;
  min-width: 0;
}

.ranking-name {
  font-size: 13px;
  color: #1C1C1E;
  font-weight: 600;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-bar {
  height: 4px;
  background: #F0F0EE;
  border-radius: 2px;
  overflow: hidden;
}

.ranking-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #5A8F8F, #3B6E6E);
  border-radius: 2px;
  transition: width 0.6s ease;
}

.ranking-amount {
  font-size: 12px;
  color: #6B6B6E;
  font-weight: 600;
  flex-shrink: 0;
}

.ranking-empty {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: #A1A1AA;
}

/* 双图 */
.chart-half {
  grid-column: span 6;
}

/* 店铺信息 */
.store-card {
  grid-column: span 7;
}

.store-fields {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.store-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.store-field span {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.store-field strong {
  font-size: 14px;
  color: #1C1C1E;
  font-weight: 600;
}

.store-row {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.store-row span {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.store-row p {
  margin: 0;
  font-size: 14px;
  color: #1C1C1E;
  line-height: 1.6;
  word-break: break-word;
}

.store-empty {
  font-size: 13px;
  color: #A1A1AA;
  padding: 12px 0;
}

.store-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.store-status-badge.active {
  background: #F0F5F0;
  color: #5A7D5A;
}

.store-status-badge.inactive {
  background: #F5F0F0;
  color: #C67C5C;
}

.store-status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.store-status-badge.active .store-status-dot {
  background: #5A7D5A;
}

.store-status-badge.inactive .store-status-dot {
  background: #C67C5C;
}

/* 快捷操作 */
.actions-card {
  grid-column: span 5;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.action-item:hover {
  background: #FAFAF9;
  border-color: #E5E5E0;
}

.action-item-icon {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  background: #F5F5F4;
  color: #6B6B6E;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: background 0.2s ease, color 0.2s ease;
}

.action-item:hover .action-item-icon {
  background: #3B6E6E;
  color: #FFFFFF;
}

.action-item-icon svg {
  width: 18px;
  height: 18px;
}

.action-item-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.action-item-body strong {
  font-size: 14px;
  color: #1C1C1E;
  font-weight: 600;
}

.action-item-body span {
  font-size: 12px;
  color: #A1A1AA;
}

.action-arrow {
  width: 16px;
  height: 16px;
  color: #A1A1AA;
  transition: transform 0.2s ease, color 0.2s ease;
}

.action-item:hover .action-arrow {
  transform: translateX(3px);
  color: #3B6E6E;
}

/* ---------- 危险操作 ---------- */
.danger-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  background: #FFFFFF;
  border: 1px solid #E5E5E0;
  border-radius: 18px;
  padding: 22px 24px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.danger-content {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.danger-icon {
  width: 22px;
  height: 22px;
  color: #C67C5C;
  flex-shrink: 0;
  margin-top: 2px;
}

.danger-content strong {
  display: block;
  font-size: 14px;
  color: #1C1C1E;
  margin-bottom: 4px;
}

.danger-content p {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.deactivate-btn {
  flex-shrink: 0;
  border-radius: 10px;
  padding: 10px 22px;
  font-weight: 600;
}

/* ---------- 注销对话框 ---------- */
.deactivate-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  margin: 0;
  border-bottom: 1px solid #E5E5E0;
}

.deactivate-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.deactivate-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.deactivate-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.deactivate-warning-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #FDF2F2;
  border: 1px solid #FAD1D1;
  border-radius: 12px;
}

.deactivate-warning-banner strong {
  display: block;
  font-size: 14px;
  color: #C67C5C;
  margin-bottom: 2px;
}

.deactivate-warning-banner p {
  margin: 0;
  font-size: 13px;
  color: #9E5E46;
  line-height: 1.5;
}

.deactivate-warning-banner svg {
  flex-shrink: 0;
  margin-top: 1px;
}

.deactivate-info {
  padding: 10px 16px;
  background: #FAFAF9;
  border-radius: 10px;
  font-size: 14px;
  color: #6B6B6E;
}

.deactivate-info p {
  margin: 0;
}

.deactivate-info strong {
  color: #1C1C1E;
}

.deactivate-checklist {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.checklist-title {
  margin: 0 0 2px;
  font-size: 13px;
  font-weight: 700;
  color: #1C1C1E;
}

.checklist-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.checklist-marker {
  flex-shrink: 0;
  font-style: normal;
  color: #3B6E6E;
  font-weight: 700;
}

.deactivate-confirm-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.confirm-label {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
}

/* ---------- 响应式 ---------- */
@media (max-width: 1200px) {
  .welcome-card,
  .highlight-card,
  .trend-card,
  .ranking-card,
  .chart-half,
  .store-card,
  .actions-card {
    grid-column: span 6;
  }

  .kpi-mini {
    grid-column: span 6;
  }

  .store-fields {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .seller-home {
    padding: 16px;
  }

  .welcome-card,
  .highlight-card,
  .trend-card,
  .ranking-card,
  .chart-half,
  .store-card,
  .actions-card,
  .kpi-mini {
    grid-column: span 12;
  }

  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .welcome-decoration {
    display: none;
  }

  .store-fields {
    grid-template-columns: 1fr;
  }

  .danger-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .chart-area {
    height: 260px;
  }
}
</style>
