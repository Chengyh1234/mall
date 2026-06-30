<template>
  <div class="admin-products">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">商品管理</h1>
          <p class="welcome-desc">管理全平台商品信息，支持上架、下架与详情查看。</p>
        </div>
        <div class="welcome-meta">
          <span class="meta-pill">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
            共 {{ pagination.total }} 件商品
          </span>
        </div>
      </div>
    </section>

    <!-- 工具栏 -->
    <section class="list-card">
      <div class="list-card__header">
        <div class="list-card__title">
          <h2>商品列表</h2>
          <span>{{ products.length }} / {{ pagination.total }} 件</span>
        </div>
        <div class="list-card__toolbar">
          <div class="product-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索商品名称..."
              @keyup.enter="handleSearch"
            />
            <button v-if="searchKeyword" class="search-clear" @click="clearSearch">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
          <div class="filter-select-wrap">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/>
            </svg>
            <select v-model="statusFilter" @change="handleSearch">
              <option value="">全部状态</option>
              <option :value="1">已上架</option>
              <option :value="0">已下架</option>
            </select>
          </div>
          <button class="icon-btn" title="刷新" @click="loadProducts">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 商品表格 -->
      <div class="table-container" v-loading="loading">
        <el-table
          :data="products"
          style="width: 100%"
          :header-cell-style="headerCellStyle"
          :cell-style="cellStyle"
          row-class-name="product-row"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="商品主图" width="88" align="center">
            <template #default="scope">
              <div v-if="scope.row.mainImage" class="product-thumb-wrap">
                <img :src="getImageUrl(scope.row.mainImage)" alt="" class="product-thumb" loading="lazy" />
              </div>
              <span v-else class="no-img">-</span>
            </template>
          </el-table-column>
          <el-table-column label="商品名称" min-width="180">
            <template #default="scope">
              <div class="product-name-cell">
                <span class="name-text" :title="scope.row.name">{{ scope.row.name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="110" align="center" />
          <el-table-column prop="brandName" label="品牌" width="100" align="center" />
          <el-table-column prop="unit" label="单位" width="70" align="center" />
          <el-table-column prop="sellerId" label="商家ID" width="80" align="center" />
          <el-table-column label="价格" width="120" align="center">
            <template #default="scope">
              <span class="price-text">¥{{ scope.row.minPrice?.toFixed(2) || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sales" label="销量" width="90" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <span
                class="status-badge"
                :class="scope.row.status === 1 ? 'status-badge--on' : 'status-badge--off'"
              >
                <span class="status-dot" />
                {{ scope.row.status === 1 ? '上架' : '下架' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="160" align="center">
            <template #default="scope">
              <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center" fixed="right">
            <template #default="scope">
              <div class="action-group">
                <button class="action-icon-btn action-icon-btn--view" title="查看详情" @click="showDetail(scope.row)">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                    <circle cx="12" cy="12" r="3"/>
                  </svg>
                </button>
                <button
                  class="action-icon-btn"
                  :class="scope.row.status === 1 ? 'action-icon-btn--off' : 'action-icon-btn--on'"
                  :title="scope.row.status === 1 ? '下架' : '上架'"
                  :disabled="scope.row._statusLoading"
                  @click="toggleStatus(scope.row, scope.row.status !== 1)"
                >
                  <svg v-if="!scope.row._statusLoading" width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path v-if="scope.row.status === 1" d="M18.36 6.64a9 9 0 1 1-12.73 0"/>
                    <line v-if="scope.row.status === 1" x1="12" y1="2" x2="12" y2="12"/>
                    <polyline v-else points="22 12 18 12 15 21 9 3 6 12 2 12"/>
                  </svg>
                  <span v-else class="btn-spinner" />
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </section>

    <!-- 商品详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="'商品详情'"
      width="720px"
      :close-on-click-modal="false"
      class="product-detail-dialog"
      destroy-on-close
    >
      <div v-if="currentProduct" class="detail-body">
        <div class="detail-hero">
          <div class="detail-image-main">
            <img
              v-if="currentProduct.mainImage"
              :src="getImageUrl(currentProduct.mainImage)"
              alt="商品主图"
              loading="lazy"
            />
            <div v-else class="detail-image-empty">
              <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
              <span>暂无主图</span>
            </div>
          </div>
          <div class="detail-info">
            <h2 class="detail-name">{{ currentProduct.name }}</h2>
            <div class="detail-tags">
              <span
                class="status-badge"
                :class="currentProduct.status === 1 ? 'status-badge--on' : 'status-badge--off'"
              >
                <span class="status-dot" />
                {{ currentProduct.status === 1 ? '上架中' : '已下架' }}
              </span>
              <span v-if="currentProduct.categoryName" class="detail-tag">{{ currentProduct.categoryName }}</span>
              <span v-if="currentProduct.brandName" class="detail-tag">{{ currentProduct.brandName }}</span>
            </div>
            <div class="detail-price-row">
              <span class="detail-price">¥{{ currentProduct.minPrice?.toFixed(2) || '-' }}</span>
              <span class="detail-sales">销量 {{ currentProduct.sales || 0 }}</span>
            </div>
            <div class="detail-meta-grid">
              <div class="detail-meta-item">
                <span class="detail-meta-label">商品ID</span>
                <span class="detail-meta-value">{{ currentProduct.id }}</span>
              </div>
              <div class="detail-meta-item">
                <span class="detail-meta-label">商家ID</span>
                <span class="detail-meta-value">{{ currentProduct.sellerId }}</span>
              </div>
              <div class="detail-meta-item">
                <span class="detail-meta-label">单位</span>
                <span class="detail-meta-value">{{ currentProduct.unit || '-' }}</span>
              </div>
              <div class="detail-meta-item">
                <span class="detail-meta-label">创建时间</span>
                <span class="detail-meta-value">{{ formatTime(currentProduct.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="imageList.length > 0" class="detail-section">
          <h3 class="detail-section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
            商品图片集
          </h3>
          <div class="detail-gallery">
            <div v-for="(img, idx) in imageList" :key="idx" class="detail-gallery-item">
              <img :src="getImageUrl(img)" alt="商品图片" loading="lazy" />
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="detail-section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="4" y1="9" x2="20" y2="9"/>
              <line x1="4" y1="15" x2="20" y2="15"/>
              <line x1="10" y1="3" x2="8" y2="21"/>
              <line x1="16" y1="3" x2="14" y2="21"/>
            </svg>
            商品描述
          </h3>
          <p class="detail-description">{{ currentProduct.description || '暂无描述' }}</p>
        </div>
      </div>
      <div v-else class="empty-detail">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </div>
        <p>加载中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSpuPageAll, getSpuManageDetailForAdmin, onShelfSpu, offShelfSpu, type SpuAdminVO } from '@/api/spu'
import { getSpuImageUrl } from '@/utils/resource'

const searchKeyword = ref('')
const statusFilter = ref<number | ''>('')
const loading = ref(false)
const products = ref<SpuAdminVO[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentProduct = ref<SpuAdminVO | null>(null)

// 解析商品图片集
const imageList = computed(() => {
  if (!currentProduct.value?.images) return []
  try {
    const parsed = JSON.parse(currentProduct.value.images)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const loadProducts = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const result = await getSpuPageAll(params)
    products.value = (result.list || []).map((item: SpuAdminVO) => ({
      ...item,
      _statusLoading: false
    }))
    pagination.total = result.total || 0
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadProducts()
}

const clearSearch = () => {
  searchKeyword.value = ''
  handleSearch()
}

const toggleStatus = async (row: any, newStatus: boolean) => {
  const targetStatus = newStatus ? 1 : 0
  if (row.status === targetStatus) return

  row._statusLoading = true
  try {
    if (targetStatus === 1) {
      await onShelfSpu(row.id)
    } else {
      await offShelfSpu(row.id)
    }
    row.status = targetStatus
    ElMessage.success(targetStatus === 1 ? '商品已上架' : '商品已下架')
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._statusLoading = false
  }
}

const showDetail = async (row: SpuAdminVO) => {
  currentProduct.value = null
  detailVisible.value = true
  try {
    const detail = await getSpuManageDetailForAdmin(row.id)
    currentProduct.value = detail
  } catch {
    // 拦截器已处理后端错误提示
  }
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

const getImageUrl = (path?: string | null) => {
  if (!path) return ''
  return getSpuImageUrl(String(path))
}

// Element Plus 表格样式注入
const headerCellStyle = () => ({
  background: '#F7F7F5',
  color: '#1C1C1E',
  fontWeight: 700,
  fontSize: '12px',
  borderBottom: '1px solid #E8E8E6',
  padding: '14px 0'
})

const cellStyle = () => ({
  borderBottom: '1px solid #F0F0EE',
  padding: '14px 0'
})

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.admin-products {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
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

.welcome-card {
  grid-column: span 12;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px 28px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.welcome-desc {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.welcome-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #3B6E6E;
  background: rgba(59, 110, 110, 0.08);
  padding: 8px 14px;
  border-radius: 20px;
}

/* ===========================
   列表卡片
   =========================== */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.list-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #F0F0EE;
  flex-wrap: wrap;
}

.list-card__title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.list-card__title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.list-card__title span {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.list-card__toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-search {
  position: relative;
}

.product-search > svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #A1A1AA;
  pointer-events: none;
}

.product-search input {
  width: 240px;
  height: 36px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  padding: 0 34px;
  font-size: 13px;
  color: #1C1C1E;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: #FFFFFF;
}

.product-search input:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.1);
}

.search-clear {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #A1A1AA;
  cursor: pointer;
  border-radius: 50%;
  padding: 0;
}

.search-clear:hover {
  background: #F0F0EE;
  color: #6B6B6E;
}

.filter-select-wrap {
  position: relative;
}

.filter-select-wrap > svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #A1A1AA;
  pointer-events: none;
}

.filter-select-wrap select {
  width: 130px;
  height: 36px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  padding: 0 12px 0 34px;
  font-size: 13px;
  color: #1C1C1E;
  outline: none;
  background: #FFFFFF;
  cursor: pointer;
  appearance: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.filter-select-wrap select:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.1);
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

/* ===========================
   表格
   =========================== */
.table-container {
  padding: 0 24px;
}

.product-thumb-wrap {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  overflow: hidden;
  background: #F5F5F4;
  border: 1px solid #E8E8E6;
  margin: 0 auto;
}

.product-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.25s ease;
}

.product-thumb-wrap:hover .product-thumb {
  transform: scale(1.08);
}

.no-img {
  color: #C0C4CC;
  font-size: 12px;
}

.product-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-size: 13px;
  font-weight: 600;
  color: #1C1C1E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-text {
  color: #3B6E6E;
  font-weight: 700;
  font-size: 13px;
}

.time-text {
  font-size: 12px;
  color: #909399;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.status-badge--on {
  color: #15803d;
  background: rgba(34, 197, 94, 0.12);
}

.status-badge--off {
  color: #b91c1c;
  background: rgba(239, 68, 68, 0.1);
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
}

.action-group {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.action-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-icon-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.action-icon-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.action-icon-btn--view {
  color: #3B6E6E;
  border-color: rgba(59, 110, 110, 0.3);
  background: rgba(59, 110, 110, 0.06);
}

.action-icon-btn--view:hover:not(:disabled) {
  background: rgba(59, 110, 110, 0.12);
  border-color: rgba(59, 110, 110, 0.5);
}

.action-icon-btn--on {
  color: #15803d;
  border-color: rgba(34, 197, 94, 0.35);
  background: rgba(34, 197, 94, 0.08);
}

.action-icon-btn--on:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.14);
  border-color: rgba(34, 197, 94, 0.55);
}

.action-icon-btn--off {
  color: #b91c1c;
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.06);
}

.action-icon-btn--off:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.5);
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 行 hover 效果 */
:deep(.product-row:hover > td) {
  background: #FAFAF9 !important;
}

:deep(.el-table__body-wrapper) {
  scrollbar-width: thin;
}

/* ===========================
   分页
   =========================== */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 18px 24px;
  border-top: 1px solid #F0F0EE;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  color: #6B6B6E;
  font-weight: 500;
  border-radius: 8px;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #3B6E6E;
  border-color: #3B6E6E;
  color: #FFFFFF;
}

:deep(.el-pagination__total),
:deep(.el-pagination__sizes) {
  color: #6B6B6E;
  font-weight: 500;
}

/* ===========================
   详情弹窗
   =========================== */
.product-detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.product-detail-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.product-detail-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 24px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-hero {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 24px;
}

.detail-image-main {
  width: 200px;
  height: 200px;
  border-radius: 16px;
  overflow: hidden;
  background: #F5F5F4;
  border: 1px solid #E8E8E6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-image-main img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-image-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #A1A1AA;
  font-size: 12px;
  font-weight: 500;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.detail-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1C1C1E;
  line-height: 1.3;
}

.detail-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-tag {
  font-size: 11px;
  font-weight: 600;
  color: #6B6B6E;
  background: #F5F5F4;
  padding: 4px 10px;
  border-radius: 20px;
}

.detail-price-row {
  display: flex;
  align-items: baseline;
  gap: 16px;
}

.detail-price {
  font-size: 24px;
  font-weight: 700;
  color: #3B6E6E;
}

.detail-sales {
  font-size: 13px;
  color: #6B6B6E;
  font-weight: 500;
}

.detail-meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-top: auto;
}

.detail-meta-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.detail-meta-label {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 500;
}

.detail-meta-value {
  font-size: 13px;
  color: #1C1C1E;
  font-weight: 600;
}

.detail-section {
  border-top: 1px solid #F0F0EE;
  padding-top: 20px;
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 700;
  color: #1C1C1E;
}

.detail-section-title svg {
  color: #C8A464;
}

.detail-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
  gap: 12px;
}

.detail-gallery-item {
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  background: #F5F5F4;
  border: 1px solid #E8E8E6;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.detail-gallery-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.detail-gallery-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-description {
  margin: 0;
  font-size: 13px;
  color: #4B4B4E;
  line-height: 1.7;
  padding: 14px 16px;
  background: #FAFAF9;
  border-radius: 12px;
  border: 1px solid #F0F0EE;
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 20px;
  text-align: center;
  color: #6B6B6E;
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #F5F5F4;
  color: #A1A1AA;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-icon svg {
  width: 28px;
  height: 28px;
}

.empty-detail p {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1C1C1E;
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 900px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .list-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-card__toolbar {
    width: 100%;
    justify-content: space-between;
  }

  .product-search input {
    width: 100%;
  }

  .detail-hero {
    grid-template-columns: 1fr;
  }

  .detail-image-main {
    width: 100%;
    height: 240px;
  }

  .detail-meta-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .list-card__toolbar {
    flex-wrap: wrap;
  }

  .product-search,
  .filter-select-wrap {
    flex: 1;
    min-width: 140px;
  }

  .product-search input,
  .filter-select-wrap select {
    width: 100%;
  }

  .table-container {
    padding: 0 16px;
    overflow-x: auto;
  }

  .pagination-bar {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .detail-meta-grid {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .icon-btn,
  .action-icon-btn,
  .product-thumb,
  .detail-gallery-item,
  .btn-spinner {
    transition: none;
    animation: none;
  }
}
</style>
