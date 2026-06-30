<template>
  <div class="admin-dashboard" v-loading="loading">
    <!-- 顶部 Bento：欢迎 + 今日销售高亮（仅此处展示今日销售额） -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <div class="welcome-meta">
            <span class="live-badge">
              <span class="live-dot" />
              实时数据
            </span>
            <span class="welcome-date">{{ currentDate }}</span>
          </div>
          <h1 class="welcome-title">运营概览</h1>
          <p class="welcome-desc">平台核心数据一览，掌握全局运营态势。</p>
        </div>
        <div class="welcome-decoration">
          <div class="deco-orb orb-1" />
          <div class="deco-orb orb-2" />
          <div class="deco-ring" />
        </div>
      </div>

      <div class="bento-card sales-highlight">
        <div class="sales-highlight__label">
          <span>今日销售额</span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="1" x2="12" y2="23"/>
            <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
          </svg>
        </div>
        <div class="sales-highlight__value">¥{{ formatMoney(overview.todaySales) }}</div>
        <div class="sales-highlight__meta">
          <span class="meta-pill">平台实时成交额</span>
        </div>
        <div class="sales-shimmer" />
      </div>
    </section>

    <!-- KPI 指标卡（不含重复的今日销售额） -->
    <section class="kpi-section">
      <div
        class="kpi-card"
        v-for="item in kpiCards"
        :key="item.key"
        :class="`kpi-card--${item.key}`"
      >
        <div class="kpi-card__accent" />
        <div class="kpi-card__icon" v-html="item.icon"></div>
        <div class="kpi-card__body">
          <span class="kpi-card__label">{{ item.label }}</span>
          <span class="kpi-card__value">{{ item.formatted }}</span>
        </div>
      </div>
    </section>

    <!-- 销售数据时序：时段切换 + 指标切换 + 汇总 + 图表 -->
    <section class="timeseries-section">
      <div class="section-header">
        <div class="section-header__left">
          <h2 class="section-title">销售数据时序</h2>
          <span class="section-desc">切换时段和指标查看趋势</span>
        </div>
        <div class="section-header__right">
          <div class="period-switcher">
            <button
              v-for="p in periodOptions"
              :key="p.value"
              class="period-btn"
              :class="{ active: selectedPeriod === p.value }"
              @click="switchPeriod(p.value)"
            >{{ p.label }}</button>
          </div>
          <button class="header-action-btn header-action-btn--primary" :disabled="loading" @click="loadAllData">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
            刷新
          </button>
        </div>
      </div>

      <!-- 指标切换横栏 -->
      <div class="metric-toolbar">
        <div class="metric-switcher">
          <button
            v-for="m in metricOptions"
            :key="m.value"
            class="metric-btn"
            :class="{ active: selectedMetric === m.value }"
            @click="switchMetric(m.value)"
          >
            <span class="metric-dot" :style="{ background: m.color }" />
            {{ m.label }}
          </button>
        </div>
        <div class="metric-summary-chip">
          <span class="chip-label">{{ activeMetricConfig.label }} · 时段合计</span>
          <span class="chip-value" :style="{ color: activeMetricConfig.color }">
            <template v-if="selectedMetric === 'sales'">¥{{ formatMoney(summaryStatsValue) }}</template>
            <template v-else-if="selectedMetric === 'orders'">{{ formatInt(summaryStatsValue) }} <small>单</small></template>
            <template v-else>{{ formatInt(summaryStatsValue) }} <small>件</small></template>
          </span>
        </div>
      </div>

      <!-- 图表 -->
      <div class="ts-chart-card">
        <div ref="chartRef" class="chart-area chart-area--main"></div>
      </div>
    </section>

    <!-- 快捷操作 -->
    <section class="quick-actions">
      <div class="quick-actions__header">
        <h3 class="section-title">快捷入口</h3>
        <span class="section-desc">常用运营功能一键直达</span>
      </div>
      <div class="action-grid">
        <div class="action-card" @click="goTo('/admin/orders')">
          <div class="action-card__icon action-card__icon--orders">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <path d="M16 10a4 4 0 0 1-8 0"/>
            </svg>
          </div>
          <div class="action-card__body">
            <strong>订单管理</strong>
            <span>处理订单与售后</span>
          </div>
          <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="action-card" @click="goTo('/admin/products')">
          <div class="action-card__icon action-card__icon--products">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
              <line x1="8" y1="21" x2="16" y2="21"/>
              <line x1="12" y1="17" x2="12" y2="21"/>
            </svg>
          </div>
          <div class="action-card__body">
            <strong>商品管理</strong>
            <span>审核与管理商品</span>
          </div>
          <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="action-card" @click="goTo('/admin/shops')">
          <div class="action-card__icon action-card__icon--shops">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </div>
          <div class="action-card__body">
            <strong>店铺管理</strong>
            <span>审核入驻与运营</span>
          </div>
          <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>

        <div class="action-card" @click="goTo('/admin/users')">
          <div class="action-card__icon action-card__icon--users">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
          <div class="action-card__body">
            <strong>用户管理</strong>
            <span>查看平台用户数据</span>
          </div>
          <svg class="action-arrow" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { UniversalTransition } from 'echarts/features'
import type { ECharts } from 'echarts/core'
import {
  getAdminDashboardOverview,
  getAdminSalesTimeSeries,
  type DashboardOverview,
  type AdminDataPoint
} from '@/api/dashboard'

// 按需注册 ECharts 组件
echarts.use([
  LineChart, BarChart,
  GridComponent, TooltipComponent, LegendComponent, TitleComponent,
  CanvasRenderer, UniversalTransition
])

const router = useRouter()
const loading = ref(false)

// ====== 概览数据 ======
const overview = reactive<DashboardOverview>({
  totalUsers: 0,
  todayOrders: 0,
  totalProducts: 0,
  todaySales: 0,
  settledSellers: 0
})

// ====== 时序数据 ======
const selectedPeriod = ref('last7Days')
const selectedMetric = ref<'sales' | 'orders' | 'volume'>('sales')
const dataPoints = ref<AdminDataPoint[]>([])

const periodOptions = [
  { value: 'last24h', label: '最近24小时' },
  { value: 'last7Days', label: '近7日' },
  { value: 'thisMonth', label: '本月' },
  { value: 'last90Days', label: '近90日' },
  { value: 'thisYear', label: '本年' }
]

const metricOptions = [
  { value: 'sales',  label: '销售额', color: '#ff4400' },
  { value: 'orders', label: '订单量', color: '#3B6E6E' },
  { value: 'volume', label: '销量',   color: '#C8A464' }
] as const

const activeMetricConfig = computed(() =>
  metricOptions.find(m => m.value === selectedMetric.value)!
)

const summaryStatsValue = computed(() => {
  const dp = dataPoints.value
  const m = selectedMetric.value
  if (m === 'sales')  return dp.reduce((s, d) => s + (d.salesAmount ?? 0), 0)
  if (m === 'orders') return dp.reduce((s, d) => s + (d.orderCount ?? 0), 0)
  return dp.reduce((s, d) => s + (d.salesVolume ?? 0), 0)
})

// ====== 格式化 ======
/** #RRGGBB → rgba(r, g, b, a) */
const hexToRgba = (hex: string, alpha: number): string => {
  const c = hex.replace('#', '')
  const r = parseInt(c.substring(0, 2), 16)
  const g = parseInt(c.substring(2, 4), 16)
  const b = parseInt(c.substring(4, 6), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const formatMoney = (val: number): string => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatLarge = (val: number): string => {
  if (val == null) return '0'
  const n = Number(val)
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toLocaleString()
}

const formatLargeMoney = (val: number): string => {
  if (val == null) return '0'
  const n = Number(val)
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return n.toFixed(2)
}

const formatInt = (val: number): string => {
  if (val == null) return '0'
  return Number(val).toLocaleString()
}

// ====== 当前日期 ======
const currentDate = computed(() => {
  const d = new Date()
  const week = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · ${week[d.getDay()]}`
})

// ====== 配色板 ======
const palette = {
  brand: '#ff4400',
  brandLight: '#ff8f1a',
  teal: '#3B6E6E',
  tealLight: '#5A8F8F',
  gold: '#C8A464',
  goldLight: '#D4B87A',
  indigo: '#6366F1',
  primary: '#1C1C1E',
  secondary: '#6B6B6E',
  muted: '#A1A1AA',
  border: '#E8E8E6',
  grid: '#F0F0EE'
}

// ====== KPI 卡片配置（仅4项，不含今日销售额） ======
const kpiCards = computed(() => [
  {
    key: 'totalUsers',
    label: '总用户数',
    formatted: formatLarge(overview.totalUsers),
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
      <circle cx="9" cy="7" r="4"/>
      <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
      <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
    </svg>`
  },
  {
    key: 'todayOrders',
    label: '今日订单数',
    formatted: formatLarge(overview.todayOrders),
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
      <line x1="3" y1="6" x2="21" y2="6"/>
      <path d="M16 10a4 4 0 0 1-8 0"/>
    </svg>`
  },
  {
    key: 'totalProducts',
    label: '商品总数',
    formatted: formatLarge(overview.totalProducts),
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
      <line x1="8" y1="21" x2="16" y2="21"/>
      <line x1="12" y1="17" x2="12" y2="21"/>
    </svg>`
  },
  {
    key: 'settledSellers',
    label: '入驻卖家数',
    formatted: formatLarge(overview.settledSellers),
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
      <polyline points="9 22 9 12 15 12 15 22"/>
    </svg>`
  }
])

// ====== ECharts refs ======
const chartRef = ref<HTMLDivElement | null>(null)
let chart: ECharts | null = null

// ====== 时段切换 ======
const switchPeriod = (period: string) => {
  selectedPeriod.value = period
  loadTimeSeries()
}

// ====== 指标切换 ======
const switchMetric = (metric: 'sales' | 'orders' | 'volume') => {
  selectedMetric.value = metric
  initChart()
}

// ====== 加载概览 ======
const loadOverview = async () => {
  try {
    const data = await getAdminDashboardOverview()
    overview.totalUsers = data.totalUsers ?? 0
    overview.todayOrders = data.todayOrders ?? 0
    overview.totalProducts = data.totalProducts ?? 0
    overview.todaySales = data.todaySales ?? 0
    overview.settledSellers = data.settledSellers ?? 0
  } catch {
    // 拦截器已处理
  }
}

// ====== 加载时序数据 ======
const loadTimeSeries = async () => {
  try {
    const res = await getAdminSalesTimeSeries(selectedPeriod.value)
    dataPoints.value = res.dataPoints ?? []
  } catch {
    dataPoints.value = []
  }
  await nextTick()
  initChart()
}

const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadTimeSeries()
    ])
  } finally {
    loading.value = false
    await nextTick()
    initChart()
  }
}

// ====== 图表渲染：根据当前指标渲染折线图或柱状图 ======
const initChart = () => {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }

  const dp = dataPoints.value
  if (dp.length === 0) {
    chart.clear()
    chart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: { color: palette.muted, fontSize: 14, fontWeight: 500 }
      }
    })
    return
  }

  const labels = dp.map(d => d.label)
  const metric = selectedMetric.value
  const cfg = activeMetricConfig.value

  // 根据指标选取数据
  let data: number[]
  let valueFormatter: (v: number) => string

  if (metric === 'sales') {
    data = dp.map(d => d.salesAmount ?? 0)
    valueFormatter = (v: number) => '¥' + Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2 })
  } else if (metric === 'orders') {
    data = dp.map(d => d.orderCount ?? 0)
    valueFormatter = (v: number) => formatInt(v) + ' 单'
  } else {
    data = dp.map(d => d.salesVolume ?? 0)
    valueFormatter = (v: number) => formatInt(v) + ' 件'
  }

  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: palette.border,
      borderWidth: 1,
      textStyle: { color: palette.primary, fontSize: 13 },
      formatter: (params: any) => {
        const p = Array.isArray(params) ? params[0] : params
        return `<b>${p.axisValue}</b><br/>${cfg.label}：<b style="color:${cfg.color}">${valueFormatter(p.value)}</b>`
      }
    },
    grid: { top: 20, right: 20, bottom: 28, left: 60 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: palette.border } },
      axisTick: { show: false },
      axisLabel: {
        color: palette.secondary,
        fontSize: 11,
        rotate: labels.length > 12 ? 45 : 0
      }
    },
    yAxis: {
      type: 'value',
      name: metric === 'sales' ? '销售额' : '单/件',
      nameTextStyle: { color: palette.muted, fontSize: 11 },
      axisLabel: {
        color: palette.muted,
        fontSize: 11,
        formatter: (v: number) => {
          if (metric === 'sales') {
            if (v >= 10000) return (v / 10000).toFixed(0) + '万'
            return v.toFixed(0)
          }
          return v >= 1000 ? (v / 1000).toFixed(0) + 'k' : v.toFixed(0)
        }
      },
      splitLine: { lineStyle: { color: palette.grid } }
    },
    series: [{
      type: 'line',
      data,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: cfg.color },
          { offset: 1, color: cfg.color }
        ])
      },
      itemStyle: {
        color: '#fff',
        borderColor: cfg.color,
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: hexToRgba(cfg.color, 0.12) },
          { offset: 1, color: hexToRgba(cfg.color, 0.02) }
        ])
      }
    }]
  }, true)
}

// ====== 窗口 resize（防抖） ======
let resizeTimer: ReturnType<typeof setTimeout> | null = null

const handleResize = () => {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    chart?.resize()
  }, 150)
}

// ====== 导出报表 ======
const handleExport = () => {
  const data = {
    exportTime: new Date().toLocaleString('zh-CN'),
    overview: { ...overview },
    timeseries: {
      period: selectedPeriod.value,
      dataPoints: dataPoints.value
    }
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `dashboard-report-${new Date().toISOString().slice(0, 10)}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('报表已导出')
}

// ====== 路由跳转 ======
const goTo = (path: string) => {
  router.push(path)
}

// ====== 清理 ======
const cleanup = () => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
  chart = null
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadAllData()
})

onUnmounted(() => {
  cleanup()
})
</script>

<style scoped>
/* ===========================
   布局
   =========================== */
.admin-dashboard {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.bento-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 28px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.bento-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
  border-color: #DDDCD8;
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

.welcome-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.live-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #3B6E6E;
  background: rgba(59, 110, 110, 0.08);
  padding: 4px 10px;
  border-radius: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.live-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #3B6E6E;
  animation: pulse 2s ease-in-out infinite;
}

.welcome-date {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.welcome-title {
  margin: 0 0 10px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: #1C1C1E;
}

.welcome-desc {
  margin: 0;
  font-size: 14px;
  color: #6B6B6E;
  line-height: 1.5;
  max-width: 420px;
}

.welcome-decoration {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.deco-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(24px);
  opacity: 0.55;
}

.orb-1 {
  width: 70px;
  height: 70px;
  top: 10px;
  right: 10px;
  background: #C8A464;
  animation: float 6s ease-in-out infinite;
}

.orb-2 {
  width: 44px;
  height: 44px;
  bottom: 16px;
  left: 20px;
  background: #ff8f1a;
  animation: float 7s ease-in-out infinite reverse;
}

.deco-ring {
  position: absolute;
  inset: 18px;
  border-radius: 50%;
  border: 1.5px solid #E5E5E0;
}

.deco-ring::before {
  content: '';
  position: absolute;
  inset: 16px;
  border-radius: 50%;
  border: 1.5px solid #DDDCD8;
}

/* 销售高亮卡（唯一今日销售额展示） */
.sales-highlight {
  grid-column: span 4;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #1C1C1E;
  border-color: #1C1C1E;
  color: #FFFFFF;
  position: relative;
  overflow: hidden;
}

.sales-highlight:hover {
  border-color: #2C2C2E;
  box-shadow: 0 16px 36px rgba(0, 0, 0, 0.12);
}

.sales-highlight__label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
  margin-bottom: 12px;
}

.sales-highlight__value {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.03em;
  margin-bottom: 14px;
  background: linear-gradient(135deg, #FFFFFF 0%, #C8A464 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sales-highlight__meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta-pill {
  font-size: 11px;
  color: #FFFFFF;
  background: rgba(255, 255, 255, 0.12);
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.sales-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.06), transparent);
  animation: shimmer 5s ease-in-out infinite;
  pointer-events: none;
}

/* ===========================
   KPI 卡片（4列）
   =========================== */
.kpi-section {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.kpi-card {
  background: #FFFFFF;
  border-radius: 18px;
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
  overflow: hidden;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.05);
}

.kpi-card__accent {
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  border-radius: 18px 0 0 18px;
}

.kpi-card--totalUsers .kpi-card__accent { background: #6366F1; }
.kpi-card--todayOrders .kpi-card__accent { background: #ff4400; }
.kpi-card--totalProducts .kpi-card__accent { background: #C8A464; }
.kpi-card--settledSellers .kpi-card__accent { background: #7A9E7A; }

.kpi-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #F5F5F4;
  color: #6B6B6E;
  transition: background 0.2s ease, color 0.2s ease;
}

.kpi-card:hover .kpi-card__icon {
  background: #1C1C1E;
  color: #FFFFFF;
}

.kpi-card__icon svg {
  width: 22px;
  height: 22px;
}

.kpi-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.kpi-card__label {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.kpi-card__value {
  font-size: 20px;
  font-weight: 700;
  color: #1C1C1E;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

/* ===========================
   销售数据时序区
   =========================== */
.timeseries-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.section-header__left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.section-header__right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.header-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  padding: 8px 16px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.header-action-btn:hover:not(:disabled) {
  border-color: #1C1C1E;
  color: #1C1C1E;
  background: #FAFAF9;
}

.header-action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.header-action-btn--primary {
  border-color: #1C1C1E;
  background: #1C1C1E;
  color: #FFFFFF;
}

.header-action-btn--primary:hover:not(:disabled) {
  background: #2C2C2E;
  border-color: #2C2C2E;
  color: #FFFFFF;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1C1C1E;
  white-space: nowrap;
}

.section-desc {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
  white-space: nowrap;
}

/* 时段切换按钮组 */
.period-switcher {
  display: flex;
  gap: 1px;
  background: #EFEFEF;
  border-radius: 10px;
  padding: 3px;
}

.period-btn {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.period-btn:hover {
  color: #1C1C1E;
}

.period-btn.active {
  background: #FFFFFF;
  color: #1C1C1E;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* 指标切换横栏 */
.metric-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.metric-switcher {
  display: flex;
  gap: 6px;
}

.metric-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  padding: 8px 18px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.metric-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
}

.metric-btn.active {
  border-color: #1C1C1E;
  background: #FAFAF9;
  color: #1C1C1E;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.metric-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.metric-btn.active .metric-dot {
  transform: scale(1.25);
}

.metric-summary-chip {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 8px 18px;
  background: #F5F5F4;
  border-radius: 12px;
  white-space: nowrap;
}

.chip-label {
  font-size: 11px;
  font-weight: 500;
  color: #A1A1AA;
}

.chip-value {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.chip-value small {
  font-size: 12px;
  font-weight: 500;
  color: #A1A1AA;
}

/* 组合图卡片 */
.ts-chart-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.ts-chart-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.06);
  border-color: #DDDCD8;
}

.chart-area {
  width: 100%;
  height: 320px;
}

.chart-area--main {
  height: 380px;
}

/* ===========================
   快捷操作
   =========================== */
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-actions__header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  background: #FFFFFF;
  border-radius: 18px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.05);
  border-color: #DDDCD8;
}

.action-card__icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #FFFFFF;
  transition: transform 0.2s ease;
}

.action-card:hover .action-card__icon {
  transform: scale(1.08);
}

.action-card__icon--orders { background: linear-gradient(135deg, #ff8f1a, #ff4400); }
.action-card__icon--products { background: linear-gradient(135deg, #D4B87A, #C8A464); }
.action-card__icon--shops { background: linear-gradient(135deg, #5A8F8F, #3B6E6E); }
.action-card__icon--users { background: linear-gradient(135deg, #818CF8, #6366F1); }

.action-card__icon svg {
  width: 20px;
  height: 20px;
}

.action-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.action-card__body strong {
  font-size: 14px;
  color: #1C1C1E;
  font-weight: 600;
}

.action-card__body span {
  font-size: 12px;
  color: #A1A1AA;
}

.action-arrow {
  width: 16px;
  height: 16px;
  color: #A1A1AA;
  transition: transform 0.2s ease, color 0.2s ease;
}

.action-card:hover .action-arrow {
  transform: translateX(3px);
  color: #1C1C1E;
}

/* ===========================
   动画
   =========================== */
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.2); }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1.05); }
}

@keyframes shimmer {
  0% { left: -100%; }
  50%, 100% { left: 150%; }
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 1100px) {
  .welcome-card,
  .sales-highlight {
    grid-column: span 6;
  }

  .welcome-decoration {
    display: none;
  }

  .kpi-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .action-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .admin-dashboard {
    gap: 18px;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .welcome-card,
  .sales-highlight {
    grid-column: span 1;
  }

  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .kpi-section {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .kpi-card {
    padding: 18px;
  }

  .metric-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .metric-switcher {
    justify-content: center;
  }

  .metric-summary-chip {
    justify-content: center;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .section-header__right {
    width: 100%;
  }

  .header-action-btn {
    flex: 1;
    justify-content: center;
  }

  .period-switcher {
    width: 100%;
    flex: 1;
  }

  .period-btn {
    flex: 1;
    justify-content: center;
    padding: 6px 8px;
    font-size: 11px;
  }

  .chart-area,
  .chart-area--main {
    height: 280px;
  }
}

@media (max-width: 480px) {
  .kpi-section {
    grid-template-columns: 1fr;
  }

  .kpi-card {
    flex-direction: row;
  }

  .metric-btn {
    flex: 1;
    justify-content: center;
    padding: 8px 12px;
    font-size: 12px;
  }

  .metric-summary-chip {
    padding: 6px 14px;
  }

  .chip-value {
    font-size: 16px;
  }
}

/* 减少动画偏好 */
@media (prefers-reduced-motion: reduce) {
  .live-dot,
  .deco-orb,
  .sales-shimmer {
    animation: none;
  }

  .bento-card,
  .kpi-card,
  .ts-chart-card,
  .action-card,
  .metric-btn,
  .metric-dot {
    transition: none;
  }
}
</style>