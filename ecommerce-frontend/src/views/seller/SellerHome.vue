<template>
  <div class="seller-home" v-loading="loading">
    <div class="seller-header">
      <h1>卖家中心</h1>
      <p>欢迎回来，{{ userStore.userInfo?.realName || userStore.userInfo?.username }} · 销售仪表盘</p>
    </div>

    <div class="kpi-grid">
      <div class="kpi-card today">
        <div class="kpi-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
            <line x1="16" y1="2" x2="16" y2="6"/>
            <line x1="8" y1="2" x2="8" y2="6"/>
            <line x1="3" y1="10" x2="21" y2="10"/>
          </svg>
        </div>
        <div class="kpi-body">
          <span class="kpi-label">今日销售额</span>
          <span class="kpi-value">¥{{ formatMoney(kpi.today) }}</span>
          <span class="kpi-profit-label">利润</span>
          <span class="kpi-profit">¥{{ formatMoney(kpi.todayProfit) }}</span>
        </div>
        <div class="kpi-badge">当日</div>
      </div>
      <div class="kpi-card week">
        <div class="kpi-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"/>
            <polyline points="17 6 23 6 23 12"/>
          </svg>
        </div>
        <div class="kpi-body">
          <span class="kpi-label">近七天销售额</span>
          <span class="kpi-value">¥{{ formatMoney(kpi.last7Days) }}</span>
          <span class="kpi-profit-label">利润</span>
          <span class="kpi-profit">¥{{ formatMoney(kpi.last7DaysProfit) }}</span>
        </div>
        <div class="kpi-badge">7天</div>
      </div>
      <div class="kpi-card month">
        <div class="kpi-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 20V10"/>
            <path d="M18 20V4"/>
            <path d="M6 20v-4"/>
          </svg>
        </div>
        <div class="kpi-body">
          <span class="kpi-label">本月销售额</span>
          <span class="kpi-value">¥{{ formatMoney(kpi.thisMonth) }}</span>
          <span class="kpi-profit-label">利润</span>
          <span class="kpi-profit">¥{{ formatMoney(kpi.thisMonthProfit) }}</span>
        </div>
        <div class="kpi-badge">当月</div>
      </div>
      <div class="kpi-card year">
        <div class="kpi-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="kpi-body">
          <span class="kpi-label">本年销售额</span>
          <span class="kpi-value">¥{{ formatMoney(kpi.thisYear) }}</span>
          <span class="kpi-profit-label">利润</span>
          <span class="kpi-profit">¥{{ formatMoney(kpi.thisYearProfit) }}</span>
        </div>
        <div class="kpi-badge">年度</div>
      </div>
    </div>

    <div class="chart-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3>销售趋势</h3>
          <span class="chart-subtitle">近7天每日销售额变化</span>
        </div>
        <div ref="trendChartRef" class="chart-body"></div>
      </div>
    </div>

    <div class="section-header">
      <h2>商品销售分析</h2>
      <el-radio-group v-model="rankingPeriod" @change="loadProductRanking" size="small">
        <el-radio-button value="today">今日</el-radio-button>
        <el-radio-button value="last7Days">近7天</el-radio-button>
        <el-radio-button value="thisMonth">本月</el-radio-button>
        <el-radio-button value="thisYear">本年</el-radio-button>
      </el-radio-group>
    </div>

    <div class="chart-row chart-row--dual">
      <div class="chart-card">
        <div class="chart-header">
          <h3>商品销售占比</h3>
          <span class="chart-subtitle">销售数量 &amp; 占比分布</span>
        </div>
        <div ref="roseChartRef" class="chart-body"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>商品销售额对比</h3>
          <span class="chart-subtitle">各商品总销售额排行</span>
        </div>
        <div ref="barChartRef" class="chart-body"></div>
      </div>
    </div>

    <div class="quick-actions">
      <h2>快捷操作</h2>
      <div class="action-grid">
        <div class="action-card" @click="goTo('/seller/products')">
          <div class="action-icon">📤</div>
          <span>商品管理</span>
        </div>
        <div class="action-card" @click="goTo('/seller/orders')">
          <div class="action-icon">📋</div>
          <span>订单管理</span>
        </div>
        <div class="action-card" @click="goTo('/seller/profile')">
          <div class="action-icon">⚙️</div>
          <span>店铺设置</span>
        </div>
      </div>
    </div>

    <!-- ====== 店铺管理（含注销） ====== -->
    <div class="store-section">
      <div class="store-section-header">
        <h2>店铺管理</h2>
        <span class="store-section-desc">查看店铺信息和管理店铺状态</span>
      </div>

      <div class="store-info-card" v-if="storeInfo">
        <div class="store-info-grid">
          <div class="store-info-field">
            <span class="store-info-label">店铺 ID</span>
            <span class="store-info-value">#{{ storeInfo.id }}</span>
          </div>
          <div class="store-info-field">
            <span class="store-info-label">店铺名称</span>
            <span class="store-info-value">{{ storeInfo.name }}</span>
          </div>
          <div class="store-info-field">
            <span class="store-info-label">店铺状态</span>
            <span class="store-info-value">
              <span class="store-status-badge" :class="storeInfo.status === 1 ? 'active' : 'inactive'">
                <span class="store-status-dot" />
                {{ storeInfo.status === 1 ? '营业中' : '已关闭' }}
              </span>
            </span>
          </div>
          <div class="store-info-field">
            <span class="store-info-label">创建时间</span>
            <span class="store-info-value">{{ storeInfo.createdAt || '—' }}</span>
          </div>
        </div>
      </div>

      <!-- 危险区域：注销店铺 -->
      <div class="danger-zone">
        <div class="danger-zone-header">
          <svg class="danger-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <div class="danger-zone-text">
            <strong>危险操作</strong>
            <p>注销店铺后，店铺将无法正常营业，所有商品将下架。如有进行中的订单，将无法注销。</p>
          </div>
        </div>
        <el-button
          type="danger"
          :loading="deactivating"
          :disabled="!storeInfo || storeInfo.status === 0"
          @click="openDeactivateDialog"
          class="deactivate-btn"
        >
          <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right:4px">
            <circle cx="12" cy="12" r="10"/>
            <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
          </svg>
          注销店铺
        </el-button>
      </div>
    </div>

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
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
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
            <span>店铺下所有订单已完结（已完成/已取消/已退款）</span>
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
          <p class="confirm-label">请输入 <strong style="color:#ef4444">确认注销</strong> 以确认操作：</p>
          <el-input
            v-model="deactivateConfirmText"
            placeholder="请输入「确认注销」"
            maxlength="10"
            @input="handleConfirmInput"
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

    <div class="ranking-table" v-if="productRanking.length > 0">
      <h3>商品销售排行明细</h3>
      <el-table :data="productRanking" border stripe>
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="name" label="商品名称" min-width="160" />
        <el-table-column label="销售额" width="140" sortable prop="salesAmount">
          <template #default="{ row }">¥{{ formatMoney(row.salesAmount) }}</template>
        </el-table-column>
        <el-table-column label="销量" width="100" sortable prop="salesCount">
          <template #default="{ row }">{{ row.salesCount }} 件</template>
        </el-table-column>
        <el-table-column label="销售额占比" width="110" sortable prop="percentOfTotal">
          <template #default="{ row }">{{ row.percentOfTotal }}%</template>
        </el-table-column>
        <el-table-column label="销量占比" width="100" sortable prop="countPercentOfTotal">
          <template #default="{ row }">{{ row.countPercentOfTotal }}%</template>
        </el-table-column>
        <el-table-column label="占比进度" min-width="160">
          <template #default="{ row }">
            <div class="percent-bar">
              <div class="percent-bar__fill" :style="{ width: row.percentOfTotal + '%' }"></div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'
import {
  getSalesKpiOverview,
  getSalesTrend,
  getProductRanking,
  type KpiOverview,
  type SalesTrend,
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
const productRanking = ref<ProductRankItem[]>([])
const rankingPeriod = ref('last7Days')

// ====== 店铺注销 ======
const storeInfo = ref<Store | null>(null)
const deactivating = ref(false)
const deactivateDialogVisible = ref(false)
const deactivateConfirmText = ref('')

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

const handleConfirmInput = () => {
  // 仅用于驱动响应式更新
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
    // 更新本地状态
    storeInfo.value.status = 0

    // 清除本地角色缓存 + 强制从后端拉取最新用户信息
    localStorage.removeItem('user_roles')
    await userStore.fetchUserInfo(true)

    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      router.push('/')
    }, 1500)
  } catch (err: any) {
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
    const data = await getSalesTrend()
    salesTrend.value = data
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const loadProductRanking = async () => {
  try {
    productRanking.value = await getProductRanking(rankingPeriod.value)
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

const renderTrendChart = () => {
  if (!trendChart) return
  const { dates, values } = salesTrend.value
  if (!dates || dates.length === 0) return

  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        const p = params[0]
        return `${p.axisValue}<br/>销售额：<b>¥${Number(p.value).toFixed(2)}</b>`
      }
    },
    grid: { top: 20, right: 20, bottom: 30, left: 60 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#ddd' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (v: number) => '¥' + formatLargeMoney(v) },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      type: 'line',
      data: values,
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: {
        width: 3,
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#667eea' },
          { offset: 1, color: '#764ba2' }
        ])
      },
      itemStyle: {
        color: '#667eea',
        borderColor: '#fff',
        borderWidth: 2
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(102,126,234,0.3)' },
          { offset: 1, color: 'rgba(102,126,234,0.02)' }
        ])
      }
    }]
  })
}

const renderRoseChart = () => {
  if (!roseChart) return
  const data = productRanking.value
  if (!data || data.length === 0) {
    roseChart.clear()
    roseChart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } } })
    return
  }

  roseChart.setOption({
    title: { show: false, text: '' },
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => {
        return `${params.name}<br/>销量：<b>${params.value} 件</b><br/>占比：<b>${data[params.dataIndex]?.countPercentOfTotal}%</b>`
      }
    },
    legend: {
      type: 'scroll',
      bottom: 0,
      textStyle: { fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['30%', '70%'],
      center: ['50%', '45%'],
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%',
        fontSize: 10
      },
      emphasis: {
        label: { fontSize: 14, fontWeight: 'bold' }
      },
      data: data.map(item => ({
        name: item.name,
        value: item.salesCount
      }))
    }]
  })
}

const renderBarChart = () => {
  if (!barChart) return
  const data = productRanking.value
  if (!data || data.length === 0) {
    barChart.clear()
    barChart.setOption({ title: { text: '暂无数据', left: 'center', top: 'center', textStyle: { color: '#999', fontSize: 14 } } })
    return
  }

  const names = data.map(item => item.name)
  const amounts = data.map(item => item.salesAmount)

  barChart.setOption({
    title: { show: false, text: '' },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = params[0]
        const item = data[p.dataIndex]
        const pct = item?.percentOfTotal != null ? ` 占比：<b>${item.percentOfTotal}%</b>` : ''
        return `${p.name}<br/>销售额：<b>¥${Number(p.value).toFixed(2)}</b>${pct}`
      }
    },
    grid: { top: 10, right: 20, bottom: data.length > 5 ? 80 : 40, left: 60 },
    xAxis: {
      type: 'category',
      data: names,
      axisLabel: {
        rotate: data.length > 5 ? 35 : 0,
        fontSize: 11,
        interval: 0
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      axisLabel: { formatter: (v: number) => '¥' + formatLargeMoney(v) },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      data: amounts.map((val, idx) => ({
        value: val,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: idx % 2 === 0 ? '#667eea' : '#f093fb' },
            { offset: 1, color: idx % 2 === 0 ? '#764ba2' : '#f5576c' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      })),
      barWidth: data.length > 5 ? '50%' : '40%'
    }]
  })
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
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.seller-header {
  margin-bottom: 28px;
}

.seller-header h1 {
  font-size: 26px;
  margin: 0 0 6px;
  color: #1a1a2e;
  font-weight: 700;
}

.seller-header p {
  margin: 0;
  color: #888;
  font-size: 14px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.kpi-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  position: relative;
  overflow: hidden;
}

.kpi-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
}

.kpi-card::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 100px;
  height: 100%;
  opacity: 0.04;
  pointer-events: none;
}

.kpi-card.today::after { background: linear-gradient(135deg, transparent, #3b82f6); }
.kpi-card.week::after { background: linear-gradient(135deg, transparent, #8b5cf6); }
.kpi-card.month::after { background: linear-gradient(135deg, transparent, #10b981); }
.kpi-card.year::after { background: linear-gradient(135deg, transparent, #f59e0b); }

.kpi-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.kpi-icon svg {
  width: 24px;
  height: 24px;
}

.kpi-card.today .kpi-icon { background: #eff6ff; color: #3b82f6; }
.kpi-card.week .kpi-icon { background: #f5f3ff; color: #8b5cf6; }
.kpi-card.month .kpi-icon { background: #ecfdf5; color: #10b981; }
.kpi-card.year .kpi-icon { background: #fffbeb; color: #f59e0b; }

.kpi-body {
  flex: 1;
  min-width: 0;
}

.kpi-label {
  display: block;
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.kpi-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  letter-spacing: -0.3px;
}

.kpi-profit-label {
  display: block;
  font-size: 11px;
  color: #bbb;
  margin-top: 6px;
  margin-bottom: 2px;
}

.kpi-profit {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #10b981;
}

.kpi-badge {
  position: absolute;
  top: 10px;
  right: 12px;
  font-size: 11px;
  color: #fff;
  padding: 2px 8px;
  border-radius: 20px;
  font-weight: 500;
}

.kpi-card.today .kpi-badge { background: #3b82f6; }
.kpi-card.week .kpi-badge { background: #8b5cf6; }
.kpi-card.month .kpi-badge { background: #10b981; }
.kpi-card.year .kpi-badge { background: #f59e0b; }

.chart-row {
  margin-bottom: 24px;
}

.chart-row--dual {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.chart-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.04);
}

.chart-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1a1a2e;
  font-weight: 600;
}

.chart-subtitle {
  font-size: 12px;
  color: #bbb;
}

.chart-body {
  width: 100%;
  height: 320px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1a1a2e;
  font-weight: 600;
}

.quick-actions {
  margin: 24px 0;
}

.quick-actions h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #1a1a2e;
  font-weight: 600;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.action-card {
  background: #fff;
  padding: 25px;
  border-radius: 14px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.08);
}

.action-icon {
  font-size: 32px;
}

.action-card span {
  font-size: 14px;
  color: #666;
}

.ranking-table {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.04);
}

.ranking-table h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #1a1a2e;
  font-weight: 600;
}

.percent-bar {
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.percent-bar__fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 3px;
  transition: width 0.6s ease;
}

/* ===========================
   店铺管理（含注销）
   =========================== */
.store-section {
  margin: 28px 0 24px;
}

.store-section-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.store-section-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1a1a2e;
  font-weight: 600;
}

.store-section-desc {
  font-size: 12px;
  color: #bbb;
}

.store-info-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.04);
  margin-bottom: 20px;
}

.store-info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.store-info-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.store-info-label {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.store-info-value {
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
}

.store-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.store-status-badge.active {
  background: #ecfdf5;
  color: #10b981;
}

.store-status-badge.inactive {
  background: #f1f5f9;
  color: #64748b;
}

.store-status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.store-status-badge.active .store-status-dot {
  background: #10b981;
}

.store-status-badge.inactive .store-status-dot {
  background: #94a3b8;
}

/* Danger Zone */
.danger-zone {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  box-shadow: 0 2px 14px rgba(0, 0, 0, 0.04);
  border: 1px solid #fecaca;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.danger-zone-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex: 1;
}

.danger-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.danger-zone-text strong {
  display: block;
  font-size: 15px;
  color: #dc2626;
  margin-bottom: 4px;
}

.danger-zone-text p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.deactivate-btn {
  flex-shrink: 0;
  border-radius: 8px;
  padding: 9px 20px;
  font-weight: 500;
}

/* 注销对话框 */
.deactivate-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  margin: 0;
  border-bottom: 1px solid #e2e8f0;
}

.deactivate-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.deactivate-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.deactivate-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.deactivate-warning-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 10px;
}

.deactivate-warning-banner strong {
  display: block;
  font-size: 14px;
  color: #dc2626;
  margin-bottom: 2px;
}

.deactivate-warning-banner p {
  margin: 0;
  font-size: 13px;
  color: #991b1b;
  line-height: 1.5;
}

.deactivate-warning-banner svg {
  flex-shrink: 0;
  margin-top: 1px;
}

.deactivate-info {
  padding: 10px 16px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 14px;
  color: #475569;
}

.deactivate-info p {
  margin: 0;
}

.deactivate-info strong {
  color: #1e293b;
}

.deactivate-checklist {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  background: #fffbeb;
  border: 1px solid #fde68a;
  border-radius: 10px;
}

.checklist-title {
  margin: 0 0 2px;
  font-size: 13px;
  font-weight: 600;
  color: #92400e;
}

.checklist-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #78350f;
  line-height: 1.5;
  cursor: default;
}

.checklist-marker {
  flex-shrink: 0;
  font-style: normal;
}

.deactivate-confirm-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.confirm-label {
  margin: 0;
  font-size: 13px;
  color: #475569;
}

@media (max-width: 1024px) {
  .kpi-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .chart-row--dual {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
  .seller-home {
    padding: 16px;
  }
}
</style>