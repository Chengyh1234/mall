<template>
  <div class="dashboard" v-loading="loading">
    <!-- 页面头部 -->
    <header class="dashboard-header">
      <div class="dashboard-header__left">
        <h1 class="dashboard-title">运营概览</h1>
        <p class="dashboard-desc">平台核心运营数据一览</p>
      </div>
      <div class="dashboard-header__actions">
        <el-button class="action-btn" @click="handleExport">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="7 10 12 15 17 10"/>
            <line x1="12" y1="15" x2="12" y2="3"/>
          </svg>
          导出报表
        </el-button>
        <el-button class="action-btn action-btn--refresh" :loading="loading" @click="loadAllData">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <polyline points="1 20 1 14 7 14"/>
            <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
          </svg>
          刷新
        </el-button>
      </div>
    </header>

    <!-- KPI 指标卡片 -->
    <section class="kpi-grid">
      <article class="kpi-card" v-for="item in kpiCards" :key="item.key">
        <div class="kpi-card__icon" :class="`kpi-card__icon--${item.key}`" v-html="item.icon"></div>
        <div class="kpi-card__body">
          <span class="kpi-card__label">{{ item.label }}</span>
          <span class="kpi-card__value">{{ item.key === 'todaySales' ? '¥' : '' }}{{ item.formatted }}</span>
          <span v-if="item.trend" class="kpi-card__trend" :class="item.trend > 0 ? 'up' : 'down'">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
              <polyline :points="item.trend > 0 ? '18 15 12 9 6 15' : '6 9 12 15 18 9'"/>
            </svg>
            {{ Math.abs(item.trend) }}%
          </span>
        </div>
      </article>
    </section>

    <!-- 销售额仪表盘 -->
    <section class="section">
      <div class="section-header">
        <div class="section-header__left">
          <h2 class="section-title">销售额仪表盘</h2>
          <span class="section-desc">四个时间维度的销售额对比</span>
        </div>
        <div class="view-switcher">
          <button
            class="view-btn"
            :class="{ active: chartView === 'metrics' }"
            @click="chartView = 'metrics'"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="20" x2="18" y2="10"/><line x1="12" y1="20" x2="12" y2="4"/><line x1="6" y1="20" x2="6" y2="14"/>
            </svg>
            概览
          </button>
          <button
            class="view-btn"
            :class="{ active: chartView === 'bar' }"
            @click="chartView = 'bar'"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="18"/><rect x="14" y="3" width="7" height="18"/>
            </svg>
            柱状
          </button>
        </div>
      </div>

      <!-- 指标概览视图 -->
      <div v-if="chartView === 'metrics'" class="dashboard-panel">
        <div class="metrics-body">
          <div
            class="metric-row"
            v-for="(m, idx) in metricsItems"
            :key="m.key"
            :style="{ '--row-delay': idx * 0.08 + 's' }"
          >
            <span class="metric-dot" :style="{ background: m.color }"></span>
            <span class="metric-label">{{ m.label }}</span>
            <span class="metric-value">¥{{ formatMoney(m.value) }}</span>
            <div class="metric-bar-track">
              <div
                class="metric-bar-fill"
                :style="{
                  width: m.percent + '%',
                  background: `linear-gradient(90deg, ${m.color}, ${m.color}dd)`
                }"
              ></div>
            </div>
            <span class="metric-pct">{{ m.percent }}%</span>
          </div>
        </div>
        <div class="metrics-footer">
          <span class="metrics-total-label">合计销售额</span>
          <span class="metrics-total-value">¥{{ formatMoney(salesTotal) }}</span>
        </div>
      </div>

      <!-- 柱状对比图 -->
      <div v-else class="dashboard-panel">
        <div ref="barChartRef" class="chart-body"></div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  getAdminDashboardOverview,
  getAdminSalesStatistics,
  type DashboardOverview,
  type SalesStatistics
} from '@/api/dashboard'

const loading = ref(false)

// ====== 概览数据 ======
const overview = reactive<DashboardOverview>({
  totalUsers: 0,
  todayOrders: 0,
  totalProducts: 0,
  todaySales: 0,
  settledSellers: 0
})

const sales = reactive<SalesStatistics>({
  today: 0,
  last7Days: 0,
  thisMonth: 0,
  thisYear: 0
})

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

// ====== KPI 卡片配置 ======
const kpiCards = computed(() => [
  {
    key: 'totalUsers',
    label: '总用户数',
    value: overview.totalUsers,
    formatted: formatLarge(overview.totalUsers),
    trend: null,
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
    value: overview.todayOrders,
    formatted: formatLarge(overview.todayOrders),
    trend: null,
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
      <line x1="3" y1="6" x2="21" y2="6"/>
      <path d="M16 10a4 4 0 0 1-8 0"/>
    </svg>`
  },
  {
    key: 'totalProducts',
    label: '商品总数',
    value: overview.totalProducts,
    formatted: formatLarge(overview.totalProducts),
    trend: null,
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
      <line x1="8" y1="21" x2="16" y2="21"/>
      <line x1="12" y1="17" x2="12" y2="21"/>
    </svg>`
  },
  {
    key: 'todaySales',
    label: '今日销售额',
    value: overview.todaySales,
    formatted: formatMoney(overview.todaySales),
    trend: null,
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <line x1="12" y1="1" x2="12" y2="23"/>
      <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
    </svg>`
  },
  {
    key: 'settledSellers',
    label: '入驻卖家数',
    value: overview.settledSellers,
    formatted: formatLarge(overview.settledSellers),
    trend: null,
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
      <polyline points="9 22 9 12 15 12 15 22"/>
    </svg>`
  }
])

// ====== 视图切换 ======
const chartView = ref<'metrics' | 'bar'>('metrics')

// ====== 销售额指标 ======
const salesTotal = computed(() =>
  sales.today + sales.last7Days + sales.thisMonth + sales.thisYear
)

const metricsItems = computed(() => {
  const maxVal = Math.max(sales.today, sales.last7Days, sales.thisMonth, sales.thisYear, 1)
  const total = salesTotal.value || 1
  return [
    { key: 'm-today', label: '今日', value: sales.today, color: '#ff4400', percent: +(sales.today / maxVal * 100).toFixed(1), pctOfTotal: +(sales.today / total * 100).toFixed(1) },
    { key: 'm-week', label: '近7日', value: sales.last7Days, color: '#8b5cf6', percent: +(sales.last7Days / maxVal * 100).toFixed(1), pctOfTotal: +(sales.last7Days / total * 100).toFixed(1) },
    { key: 'm-month', label: '本月', value: sales.thisMonth, color: '#10b981', percent: +(sales.thisMonth / maxVal * 100).toFixed(1), pctOfTotal: +(sales.thisMonth / total * 100).toFixed(1) },
    { key: 'm-year', label: '本年', value: sales.thisYear, color: '#f59e0b', percent: +(sales.thisYear / maxVal * 100).toFixed(1), pctOfTotal: +(sales.thisYear / total * 100).toFixed(1) }
  ]
})

// ====== ECharts 图表 refs ======
const barChartRef = ref<HTMLDivElement | null>(null)
let barChart: echarts.ECharts | null = null

// ====== 加载数据 ======
const loadOverview = async () => {
  try {
    const data = await getAdminDashboardOverview()
    overview.totalUsers = data.totalUsers ?? 0
    overview.todayOrders = data.todayOrders ?? 0
    overview.totalProducts = data.totalProducts ?? 0
    overview.todaySales = data.todaySales ?? 0
    overview.settledSellers = data.settledSellers ?? 0
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const loadSales = async () => {
  try {
    const data = await getAdminSalesStatistics()
    sales.today = data.today ?? 0
    sales.last7Days = data.last7Days ?? 0
    sales.thisMonth = data.thisMonth ?? 0
    sales.thisYear = data.thisYear ?? 0
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadSales()
    ])
  } finally {
    loading.value = false
    await nextTick()
    initCharts()
  }
}

// ====== 导出报表 ======
const handleExport = () => {
  const data = {
    exportTime: new Date().toLocaleString('zh-CN'),
    overview: {
      totalUsers: overview.totalUsers,
      todayOrders: overview.todayOrders,
      totalProducts: overview.totalProducts,
      todaySales: overview.todaySales,
      settledSellers: overview.settledSellers
    },
    salesStatistics: {
      today: sales.today,
      last7Days: sales.last7Days,
      thisMonth: sales.thisMonth,
      thisYear: sales.thisYear
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

// ====== 初始化图表 ======
const initCharts = () => {
  if (chartView.value === 'bar') {
    initBarChart()
  }
}

// ====== 柱状对比图 ======
const initBarChart = () => {
  if (!barChartRef.value) return
  if (!barChart) {
    barChart = echarts.init(barChartRef.value)
  }

  const labels = ['今日', '近7日', '本月', '本年']
  const values = [sales.today, sales.last7Days, sales.thisMonth, sales.thisYear]
  const colors = ['#ff4400', '#8b5cf6', '#10b981', '#f59e0b']
  const maxVal = Math.max(...values)
  const pad = maxVal * 0.15 || 1000

  barChart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255,255,255,0.95)',
      borderColor: 'var(--border)',
      borderWidth: 1,
      textStyle: { fontSize: 13, color: 'var(--ink)' },
      formatter: (params: any) => {
        const p = params[0]
        return `<b>${p.axisValue}</b><br/>销售额：<b>¥${Number(p.value).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}</b>`
      }
    },
    grid: { top: 24, right: 36, bottom: 32, left: 72 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: 'var(--border)' } },
      axisTick: { show: false },
      axisLabel: { color: 'var(--ink-muted)', fontSize: 13 }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: maxVal + pad,
      axisLabel: {
        color: 'var(--ink-faint)',
        fontSize: 12,
        formatter: (v: number) => {
          if (v >= 10000) return (v / 10000).toFixed(0) + '万'
          return v.toFixed(0)
        }
      },
      splitLine: { lineStyle: { color: 'var(--border-light)', type: 'dashed' } }
    },
    series: [{
      type: 'bar',
      data: values.map((v, i) => ({
        value: v,
        itemStyle: { color: colors[i], borderRadius: [6, 6, 0, 0] }
      })),
      barWidth: '52%',
      label: {
        show: true,
        position: 'top',
        color: 'var(--ink-muted)',
        fontSize: 12,
        formatter: (p: any) => '¥' + Number(p.value).toLocaleString('zh-CN', { minimumFractionDigits: 0 })
      },
      animationDuration: 800,
      animationEasing: 'cubicOut'
    }]
  }, true)
}

// ====== 窗口 resize ======
const handleResize = () => {
  barChart?.resize()
}

// ====== 监听视图切换 ======
watch(chartView, (val) => {
  nextTick(() => {
    if (val === 'bar') {
      initBarChart()
    }
  })
})

// ====== 清理 ======
const cleanup = () => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
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
.dashboard {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ===========================
   头部
   =========================== */
.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-4);
}

.dashboard-header__left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.dashboard-title {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.02em;
}

.dashboard-desc {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--ink-faint);
}

.dashboard-header__actions {
  display: flex;
  gap: var(--space-2);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-sm);
  border-radius: var(--radius-md);
  padding: 8px 16px;
  font-weight: 500;
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--ink-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  border-color: var(--color-brand-300);
  color: var(--color-brand-500);
  background: var(--color-brand-50);
}

.action-btn--refresh {
  border: none;
  background: var(--color-brand-500);
  color: #fff;
}

.action-btn--refresh:hover {
  background: var(--color-brand-600);
  color: #fff;
}

/* ===========================
   KPI 卡片网格
   =========================== */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
}

.kpi-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  padding: var(--space-5) var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  position: relative;
  overflow: hidden;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.kpi-card__icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-card__icon svg {
  width: 24px;
  height: 24px;
}

.kpi-card__icon--totalUsers { background: #eff6ff; color: #3b82f6; }
.kpi-card__icon--todayOrders { background: #f0fdf4; color: #22c55e; }
.kpi-card__icon--totalProducts { background: #fff7ed; color: #f97316; }
.kpi-card__icon--todaySales { background: #fef2f2; color: #ef4444; }
.kpi-card__icon--settledSellers { background: #f5f3ff; color: #8b5cf6; }

.kpi-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.kpi-card__label {
  font-size: var(--text-xs);
  color: var(--ink-faint);
  font-weight: 500;
}

.kpi-card__value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.kpi-card__trend {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: var(--text-xs);
  font-weight: 600;
  margin-top: 2px;
}

.kpi-card__trend.up { color: var(--color-success); }
.kpi-card__trend.down { color: var(--color-danger); }

/* ===========================
   Section 通用
   =========================== */
.section {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-3);
}

.section-header__left {
  display: flex;
  align-items: baseline;
  gap: var(--space-3);
}

.section-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--ink);
}

.section-desc {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.view-switcher {
  display: flex;
  gap: 1px;
  background: var(--surface-muted);
  border-radius: var(--radius-md);
  padding: 3px;
}

.view-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
  padding: 5px 12px;
  border: none;
  border-radius: 5px;
  background: transparent;
  color: var(--ink-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.view-btn:hover {
  color: var(--ink);
}

.view-btn.active {
  background: var(--surface);
  color: var(--ink);
  box-shadow: var(--shadow-sm);
}

/* ===========================
   仪表盘面板
   =========================== */
.dashboard-panel {
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
}

.metrics-body {
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.metric-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  animation: metricFadeIn 0.5s ease-out both;
  animation-delay: var(--row-delay, 0s);
}

@keyframes metricFadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.metric-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.metric-label {
  width: 48px;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--ink);
  flex-shrink: 0;
}

.metric-value {
  width: 120px;
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
  text-align: right;
  flex-shrink: 0;
}

.metric-bar-track {
  flex: 1;
  height: 8px;
  background: var(--surface-muted);
  border-radius: var(--radius-full);
  overflow: hidden;
  min-width: 80px;
}

.metric-bar-fill {
  height: 100%;
  border-radius: var(--radius-full);
  transition: width 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.metric-pct {
  width: 44px;
  font-size: var(--text-xs);
  color: var(--ink-faint);
  font-weight: 500;
  text-align: right;
  flex-shrink: 0;
  font-variant-numeric: tabular-nums;
}

.metrics-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-6);
  background: var(--surface-soft);
  border-top: 1px solid var(--border-light);
}

.metrics-total-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--ink-muted);
}

.metrics-total-value {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.chart-body {
  width: 100%;
  height: 320px;
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .dashboard-header__actions {
    width: 100%;
  }
  .action-btn {
    flex: 1;
    justify-content: center;
  }
  .chart-body {
    height: 240px;
  }
}

@media (max-width: 480px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  .dashboard {
    padding: 0;
  }
  .metrics-body {
    padding: var(--space-3) var(--space-4);
  }
  .metrics-footer {
    padding: var(--space-3) var(--space-4);
  }
  .metric-value {
    width: 80px;
    font-size: var(--text-xs);
  }
  .metric-label {
    width: 36px;
    font-size: var(--text-xs);
  }
  .metric-pct {
    width: 36px;
  }
}
</style>