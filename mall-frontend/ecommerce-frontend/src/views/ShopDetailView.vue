<template>
  <div class="shop-detail-page">
    <NavBar showBack />

    <!-- 加载状态 -->
    <div v-if="pageLoading" class="page-loading">
      <div class="loading-indicator">
        <svg class="loading-spinner" viewBox="0 0 50 50">
          <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="4"></circle>
        </svg>
        <span class="loading-text">加载中...</span>
      </div>
    </div>

    <!-- 内容主体 -->
    <template v-else-if="shop">
      <!-- 店铺横幅区 -->
      <div class="shop-hero" :style="{ backgroundImage: shop.banner ? `url(${getStoreBannerUrl(shop.banner)})` : 'none' }">
        <div class="hero-overlay" />
        <div class="hero-content">
          <div class="hero-info">
            <el-avatar :size="72" class="hero-logo">
              <img :src="shop.logo ? getStoreLogoUrl(shop.logo) : ''" :alt="shop.name" />
            </el-avatar>
            <div class="hero-text">
              <h1 class="hero-title">{{ shop.name }}</h1>
              <p class="hero-desc">{{ shop.description || '暂无描述' }}</p>
              <div class="hero-meta">
                <span class="hero-meta-item" v-if="shop.phone">
                  <svg viewBox="0 0 16 16" width="14" height="14" fill="currentColor"><path d="M3.654 1.328a.678.678 0 00-1.015-.063L1.605 2.3c-.483.484-.661 1.169-.45 1.77a17.568 17.568 0 004.168 6.608 17.569 17.569 0 006.608 4.168c.601.211 1.286.033 1.77-.45l1.034-1.034a.678.678 0 00-.063-1.015l-2.307-1.794a.678.678 0 00-.58-.122l-2.19.547a1.745 1.745 0 01-1.657-.459L5.482 8.062a1.745 1.745 0 01-.46-1.657l.548-2.19a.678.678 0 00-.122-.58L3.654 1.328z"/></svg>
                  {{ shop.phone }}
                </span>
                <span class="hero-meta-item" v-if="shop.address">
                  <svg viewBox="0 0 16 16" width="14" height="14" fill="currentColor"><path d="M8 16s6-5.686 6-10A6 6 0 002 6c0 4.314 6 10 6 10zm0-7a3 3 0 110-6 3 3 0 010 6z"/></svg>
                  {{ shop.address }}
                </span>
                <span class="hero-meta-item" v-if="shop.createdAt">
                  <svg viewBox="0 0 16 16" width="14" height="14" fill="currentColor"><path d="M8 3.5a.5.5 0 00-1 0V9a.5.5 0 00.252.434l3.5 2a.5.5 0 00.496-.868L8 8.71V3.5z"/><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm7-8A7 7 0 111 8a7 7 0 0114 0z"/></svg>
                  {{ shop.createdAt }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 主体内容 -->
      <div class="shop-main">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <div class="filter-row filter-row-top">
            <div class="filter-left">
              <div class="search-input-group">
                <el-input
                  v-model="keyword"
                  placeholder="搜索店内商品..."
                  class="filter-search"
                  clearable
                  @keyup.enter="handleSearch"
                >
                  <template #prefix>
                    <svg viewBox="0 0 16 16" width="14" height="14" fill="#9ca3af"><path d="M11.742 10.344a6.5 6.5 0 10-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 001.415-1.414l-3.85-3.85a1.007 1.007 0 00-.115-.1zM12 6.5a5.5 5.5 0 11-11 0 5.5 5.5 0 0111 0z"/></svg>
                  </template>
                </el-input>
                <div class="filter-group filter-price-group">
                  <span class="filter-group-label">价格</span>
                  <el-input-number
                    v-model="minPrice"
                    :min="0"
                    :max="999999"
                    placeholder="最低价"
                    class="filter-price-input"
                    controls-position="right"
                    size="small"
                  />
                  <span class="price-separator">—</span>
                  <el-input-number
                    v-model="maxPrice"
                    :min="0"
                    :max="999999"
                    placeholder="最高价"
                    class="filter-price-input"
                    controls-position="right"
                    size="small"
                  />
                </div>
                <el-button type="primary" class="search-btn" @click="handleSearch">
                  <svg viewBox="0 0 16 16" width="14" height="14" fill="currentColor"><path d="M11.742 10.344a6.5 6.5 0 10-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 001.415-1.414l-3.85-3.85a1.007 1.007 0 00-.115-.1zM12 6.5a5.5 5.5 0 11-11 0 5.5 5.5 0 0111 0z"/></svg>
                  搜索
                </el-button>
              </div>
            </div>
            <div class="filter-right">
              <span class="sort-label">排序：</span>
              <div class="sort-tabs">
                <button
                  v-for="opt in sortOptions"
                  :key="opt.value"
                  :class="['sort-tab', { active: sortBy === opt.value }]"
                  @click="handleSortChange(opt.value)"
                >
                  {{ opt.label }}
                  <span v-if="sortBy === opt.value" class="sort-arrow">
                    <svg :class="{ rotated: sortOrder === 'asc' }" viewBox="0 0 12 12" width="10" height="10" fill="currentColor"><path d="M6 3l4 5H2l4-5z"/></svg>
                  </span>
                </button>
              </div>
            </div>
          </div>

          <div class="filter-row filter-row-bottom">
            <div class="filter-group">
              <span class="filter-group-label">分类</span>
              <el-cascader
                v-model="selectedCategoryPath"
                :options="categoryTree"
                :props="cascaderProps"
                placeholder="全部分类"
                class="filter-cascader"
                clearable
                filterable
                @change="handleFilterChange"
              >
                <template #default="{ data }">
                  <span class="cascader-option">
                    <svg class="cascader-folder" viewBox="0 0 16 16" width="14" height="14" fill="currentColor">
                      <path v-if="data.children?.length" d="M1 2.5A1.5 1.5 0 012.5 1h3.672a1.5 1.5 0 011.06.44l.586.586a1.5 1.5 0 001.06.44H13.5A1.5 1.5 0 0115 4v1.5H1V2.5z"/>
                      <path v-else d="M2 2.5A1.5 1.5 0 013.5 1h3.172a1.5 1.5 0 011.06.44l.586.586a1.5 1.5 0 001.06.44H13.5A1.5 1.5 0 0115 4v8.5a1.5 1.5 0 01-1.5 1.5h-10A1.5 1.5 0 012 12.5V2.5z"/>
                    </svg>
                    <span>{{ data.name }}</span>
                  </span>
                </template>
              </el-cascader>
            </div>

            <el-button
              class="filter-reset-btn"
              size="small"
              @click="resetAllFilters"
            >
              清除筛选
            </el-button>
          </div>
        </div>

        <!-- 商品区域 -->
        <div class="products-area">
          <div class="products-header">
            <span class="products-title">店内商品</span>
            <span class="products-count" v-if="total > 0">共 {{ total }} 件</span>
          </div>

          <!-- 加载中 -->
          <div v-if="productsLoading" class="loading-container">
            <div class="loading-indicator">
              <svg class="loading-spinner" viewBox="0 0 50 50">
                <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="4"></circle>
              </svg>
              <span class="loading-text">加载商品中...</span>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else-if="products.length === 0" class="empty-container">
            <div class="empty-icon">
              <svg viewBox="0 0 80 80" width="80" height="80" fill="none">
                <rect x="10" y="20" width="60" height="50" rx="6" stroke="#d1d5db" stroke-width="2" fill="#f9fafb"/>
                <line x1="25" y1="38" x2="55" y2="38" stroke="#e5e7eb" stroke-width="2" stroke-linecap="round"/>
                <line x1="25" y1="46" x2="50" y2="46" stroke="#e5e7eb" stroke-width="2" stroke-linecap="round"/>
                <line x1="25" y1="54" x2="45" y2="54" stroke="#e5e7eb" stroke-width="2" stroke-linecap="round"/>
                <path d="M40 10l-8 10h16l-8-10z" fill="#f3f4f6" stroke="#d1d5db" stroke-width="2"/>
              </svg>
            </div>
            <p class="empty-text">{{ hasActiveFilters ? '未搜到相关商品' : '该店铺暂无在售商品' }}</p>
            <el-button v-if="hasActiveFilters" size="small" class="empty-reset-btn" @click="resetAllFilters">清除筛选条件</el-button>
          </div>

          <!-- 商品网格 -->
          <div v-else class="product-grid">
            <div
              v-for="item in products"
              :key="item.id"
              class="product-card"
              @click="goToProduct(item.id)"
            >
              <div class="product-image-wrap">
                <img :src="item.mainImage ? getSpuImageUrl(item.mainImage) : '/images/default-product.png'" :alt="item.name" class="product-image" loading="lazy" />
                <div class="product-hover-overlay">
                  <span>查看详情</span>
                </div>
              </div>
              <div class="product-body">
                <h3 class="product-name">{{ item.name }}</h3>
                <div class="product-tags">
                  <span class="product-tag" v-if="item.categoryName">{{ item.categoryName }}</span>
                  <span class="product-tag brand-tag" v-if="item.brandName">{{ item.brandName }}</span>
                </div>
                <div class="product-footer">
                  <span class="product-price">¥{{ item.minPrice }}</span>
                  <span class="product-sales">已售 {{ item.sales > 999 ? (item.sales / 1000).toFixed(1) + 'k' : item.sales }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              @current-change="handlePageChange"
              :current-page="currentPage"
              :page-size="pageSize"
              layout="prev, pager, next, total"
              :total="total"
              :page-count="Math.ceil(total / pageSize)"
              background
            />
            <span class="pagination-info">共 {{ Math.ceil(total / pageSize) }} 页</span>
          </div>
        </div>
      </div>
    </template>

    <!-- 加载失败 -->
    <div v-else class="error-container">
      <el-empty description="店铺不存在或已关闭" />
      <el-button type="primary" @click="router.back()" class="back-btn">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStoreDetail, getStoreSpuPage, type Store, type StoreSpuItem } from '@/api/shop'
import { getCategoryTree, type Category } from '@/api/product'
import NavBar from '@/components/NavBar.vue'
import { getSpuImageUrl, getStoreLogoUrl, getStoreBannerUrl } from '@/utils/resource'

const router = useRouter()
const route = useRoute()

const shopId = Number(route.params.id)
const pageLoading = ref(true)
const shop = ref<Store | null>(null)

// 商品相关
const products = ref<StoreSpuItem[]>([])
const productsLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 筛选
const keyword = ref('')
const sortBy = ref<string>('created_at')
const sortOrder = ref<string>('desc')

// 分类级联选择器
const selectedCategoryPath = ref<number[]>([])
const categoryTree = ref<Category[]>([])
const cascaderProps = {
  value: 'id',
  label: 'name',
  children: 'children',
  emitPath: false,
  checkStrictly: false,
}

// 品牌 & 价格
const minPrice = ref<number | undefined>(undefined)
const maxPrice = ref<number | undefined>(undefined)

const hasActiveFilters = computed(() => {
  return !!(keyword.value.trim()
    || selectedCategoryPath.value.length > 0
    || minPrice.value !== undefined
    || maxPrice.value !== undefined)
})

const sortOptions = [
  { value: 'created_at', label: '最新' },
  { value: 'sales', label: '销量' },
  { value: 'price', label: '价格' },
]

// 获取店铺详情
const fetchShopDetail = async () => {
  try {
    shop.value = await getStoreDetail(shopId)
  } catch {
    shop.value = null
  } finally {
    pageLoading.value = false
  }
}

// 获取分类选项
const fetchFilterOptions = async () => {
  try {
    categoryTree.value = await getCategoryTree()
  } catch {
    // 静默失败，不影响主流程
  }
}

// 获取店铺商品
const fetchProducts = async (page = 1) => {
  productsLoading.value = true
  try {
    const result = await getStoreSpuPage(shopId, {
      page,
      pageSize: pageSize.value,
      keyword: keyword.value.trim() || undefined,
      categoryId: selectedCategoryPath.value.length > 0 ? selectedCategoryPath.value[selectedCategoryPath.value.length - 1] : undefined,
      minPrice: minPrice.value,
      maxPrice: maxPrice.value,
      sortBy: sortBy.value as any,
      sortOrder: sortOrder.value as any,
    })
    if (result) {
      products.value = result.list || []
      currentPage.value = result.page || page
      total.value = result.total || 0
    }
  } catch {
    ElMessage.error('获取商品列表失败')
    products.value = []
    total.value = 0
  } finally {
    productsLoading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchProducts(1)
}

// 筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1
  fetchProducts(1)
}

// 重置所有筛选
const resetAllFilters = () => {
  keyword.value = ''
  selectedCategoryPath.value = []
  minPrice.value = undefined
  maxPrice.value = undefined
  sortBy.value = 'created_at'
  sortOrder.value = 'desc'
  handleFilterChange()
}

// 排序切换
const handleSortChange = (value: string) => {
  if (sortBy.value === value) {
    sortOrder.value = sortOrder.value === 'desc' ? 'asc' : 'desc'
  } else {
    sortBy.value = value
    sortOrder.value = 'desc'
  }
  currentPage.value = 1
  fetchProducts(1)
}

// 分页
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchProducts(page)
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

// 跳转商品详情
const goToProduct = (id: number) => {
  router.push(`/product/${id}`)
}

onMounted(() => {
  Promise.all([fetchShopDetail(), fetchFilterOptions()]).then(() => {
    if (shop.value) {
      fetchProducts(1)
    }
  })
})
</script>

<style scoped>
/* ========== 页面基础 ========== */
.shop-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

/* ========== 加载 / 错误 ========== */
.page-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.loading-spinner {
  width: 34px;
  height: 34px;
  animation: spin 1s linear infinite;
}

.loading-spinner .path {
  stroke: #1e1e2d;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }
@keyframes dash {
  0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; }
  50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; }
  100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; }
}

.loading-text {
  color: #9ca3af;
  font-size: 14px;
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 20px;
}

.back-btn {
  background: #1e1e2d;
  border-color: #1e1e2d;
}

.back-btn:hover {
  background: #2d2d44;
  border-color: #2d2d44;
}

/* ========== 店铺横幅 ========== */
.shop-hero {
  position: relative;
  height: 280px;
  background-size: cover;
  background-position: center;
  background-color: #2d2d44;
  overflow: hidden;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(30, 30, 45, 0.85) 0%, rgba(30, 30, 45, 0.6) 50%, rgba(30, 30, 45, 0.4) 100%);
}

.hero-content {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 100%;
  display: flex;
  align-items: flex-end;
}

.hero-info {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-bottom: 36px;
}

.hero-logo {
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 14px;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  background: #fff;
}

.hero-text {
  color: #fff;
  min-width: 0;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 6px;
  line-height: 1.25;
  text-wrap: balance;
}

.hero-desc {
  font-size: 15px;
  margin: 0 0 14px;
  opacity: 0.85;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 20px;
}

.hero-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  opacity: 0.75;
  color: #fff;
  line-height: 1.4;
}

.hero-meta-item svg {
  flex-shrink: 0;
}

/* ========== 主体内容 ========== */
.shop-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 60px;
  margin-top: -20px;
  position: relative;
}

/* ========== 筛选栏 ========== */
.filter-bar {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 12px rgba(0, 0, 0, 0.03);
  margin-bottom: 24px;
  overflow: hidden;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  flex-wrap: wrap;
}

.filter-row-top {
  border-bottom: 1px solid #f0f1f3;
}

.filter-row-bottom {
  padding: 10px 20px;
}

.filter-left {
  flex: 1;
  min-width: 200px;
}

.search-input-group {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input-group .filter-search {
  flex: 1;
  min-width: 140px;
  max-width: 220px;
  --el-input-border-radius: 8px;
  --el-input-height: 34px;
}

.search-input-group .filter-price-group {
  flex-shrink: 0;
  gap: 4px;
}

.search-input-group .filter-price-input {
  width: 90px;
}

.search-input-group .filter-price-input .el-input-number__increase,
.search-input-group .filter-price-input .el-input-number__decrease {
  display: none;
}

.search-input-group .price-separator {
  color: #d1d5db;
  font-size: 14px;
  flex-shrink: 0;
}

.search-btn {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 34px;
  padding: 0 14px;
  background: #1e1e2d;
  border-color: #1e1e2d;
  font-size: 13px;
  font-weight: 500;
  border-radius: 8px;
}

.search-btn:hover {
  background: #2d2d44;
  border-color: #2d2d44;
}

.filter-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-label {
  font-size: 13px;
  color: #9ca3af;
  white-space: nowrap;
}

.sort-tabs {
  display: flex;
  gap: 4px;
}

.sort-tab {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #6b7280;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  line-height: 1;
  white-space: nowrap;
}

.sort-tab:hover {
  border-color: #d1d5db;
  color: #374151;
}

.sort-tab.active {
  background: #1e1e2d;
  border-color: #1e1e2d;
  color: #fff;
}

.sort-arrow {
  display: inline-flex;
  transition: transform 0.2s;
}

.sort-arrow svg.rotated {
  transform: rotate(180deg);
}

/* 筛选条件组 */
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group-label {
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  font-weight: 500;
  min-width: 28px;
}

.filter-select {
  width: 150px;
  --el-select-input-font-size: 13px;
}

/* 分类级联选择器 */
.filter-cascader {
  width: 165px;
  --el-cascader-menu-item-font-size: 13px;
}

.filter-cascader :deep(.el-cascader__placeholder) {
  color: #9ca3af;
  font-size: 13px;
}

.filter-cascader :deep(.el-cascader__search-input) {
  font-size: 13px;
}

.filter-cascader :deep(.el-cascader-node__label) {
  font-size: 13px;
}

.cascader-option {
  display: flex;
  align-items: center;
  gap: 6px;
}

.cascader-folder {
  flex-shrink: 0;
  color: #9ca3af;
}

.el-cascader-node.is-active .cascader-folder,
.el-cascader-node.in-active-path .cascader-folder {
  color: #1e1e2d;
}

.filter-price-group {
  gap: 4px;
}

.filter-price-input {
  width: 100px;
}

.filter-price-input .el-input-number__increase,
.filter-price-input .el-input-number__decrease {
  display: none;
}

.price-separator {
  color: #d1d5db;
  font-size: 14px;
}

.filter-reset-btn {
  margin-left: auto;
  color: #6b7280;
  border-color: #e5e7eb;
  font-size: 12px;
}

.filter-reset-btn:hover {
  color: #ef4444;
  border-color: #fca5a5;
  background: #fef2f2;
}

/* ========== 商品区域 ========== */
.products-area {
  background: #fff;
  border-radius: 14px;
  padding: 24px 28px 28px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.03);
}

.products-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eaecf0;
}

.products-title {
  font-size: 18px;
  font-weight: 650;
  color: #1a1a2e;
  position: relative;
  padding-left: 12px;
}

.products-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: #1e1e2d;
  border-radius: 2px;
}

.products-count {
  font-size: 13px;
  color: #9ca3af;
  padding: 4px 12px;
  background: #f4f5f8;
  border-radius: 20px;
  font-weight: 500;
}

/* ========== 加载 / 空状态 ========== */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 80px 0;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  gap: 16px;
}

.empty-icon {
  opacity: 0.6;
}

.empty-text {
  color: #9ca3af;
  font-size: 15px;
  margin: 0;
}

.empty-reset-btn {
  background: #1e1e2d;
  border-color: #1e1e2d;
  color: #fff;
}

.empty-reset-btn:hover {
  background: #2d2d44;
  border-color: #2d2d44;
}

/* ========== 商品网格 ========== */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(190px, 1fr));
  gap: 16px;
}

.product-card {
  background: #fff;
  border: 1px solid #f0f1f3;
  border-radius: 11px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.2s, border-color 0.25s;
  display: flex;
  flex-direction: column;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.07);
  transform: translateY(-3px);
  border-color: #e0e2e6;
}

.product-image-wrap {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: #f9fafb;
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.product-card:hover .product-image {
  transform: scale(1.08);
}

.product-hover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(30, 30, 45, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
}

.product-card:hover .product-hover-overlay {
  opacity: 1;
}

.product-hover-overlay span {
  color: #fff;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 16px;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

.product-body {
  padding: 13px 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
}

.product-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.product-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.product-tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #f3f4f6;
  color: #6b7280;
  font-weight: 500;
}

.product-tag.brand-tag {
  background: #fef3c7;
  color: #b45309;
}

.product-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: auto;
  gap: 8px;
}

.product-price {
  font-size: 18px;
  font-weight: 700;
  color: #ef4444;
  line-height: 1;
}

.product-sales {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
}

/* ========== 分页 ========== */
.pagination-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid #eaecf0;
  gap: 14px;
}

.pagination-info {
  font-size: 13px;
  color: #9ca3af;
}

.pagination-wrap :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #1e1e2d;
}

/* ========== 响应式 ========== */
@media screen and (max-width: 768px) {
  .shop-hero {
    height: 200px;
  }

  .hero-info {
    gap: 14px;
    padding-bottom: 24px;
  }

  .hero-logo {
    width: 52px;
    height: 52px;
  }

  .hero-title {
    font-size: 20px;
  }

  .hero-desc {
    font-size: 13px;
    -webkit-line-clamp: 1;
  }

  .hero-meta {
    gap: 6px 14px;
  }

  .hero-meta-item {
    font-size: 12px;
  }

  .shop-main {
    padding: 0 12px 40px;
    margin-top: -16px;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    padding: 0;
  }

  .filter-row {
    padding: 10px 16px;
    gap: 10px;
  }

  .filter-left {
    max-width: none;
  }

  .filter-right {
    justify-content: space-between;
  }

  .sort-tab {
    padding: 5px 10px;
    font-size: 12px;
  }

  .filter-row-bottom {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }

  .filter-group {
    flex-wrap: wrap;
  }

  .filter-select {
    flex: 1;
    min-width: 120px;
  }

  .filter-cascader {
    flex: 1;
    min-width: 120px;
  }

  .filter-price-group {
    flex-wrap: nowrap;
  }

  .filter-price-input {
    flex: 1;
    width: auto;
  }

  .filter-reset-btn {
    margin-left: 0;
    width: 100%;
  }

  .products-area {
    padding: 16px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .product-body {
    padding: 10px;
  }

  .product-name {
    font-size: 13px;
    min-height: 36px;
  }

  .product-price {
    font-size: 16px;
  }

  .pagination-wrap {
    flex-wrap: wrap;
  }
}

@media screen and (min-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (prefers-reduced-motion: reduce) {
  .product-card,
  .product-image,
  .product-hover-overlay,
  .sort-tab {
    transition: none;
  }

  .loading-spinner {
    animation: none;
  }
}
</style>