<template>
  <div class="home">
    <!-- 顶部导航栏 -->
    <NavBar />

    <!-- 主横幅区域：分类导航 + 轮播图 -->
    <section class="hero-section">
      <div class="hero-layout">
        <!-- 左侧分类导航 -->
        <nav class="category-sidebar" aria-label="商品分类">
          <div class="category-header">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
              <line x1="3" y1="6" x2="21" y2="6"/>
              <line x1="3" y1="12" x2="21" y2="12"/>
              <line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
            <span>全部商品分类</span>
          </div>
          <div
            v-for="category in firstLevelCategories"
            :key="category.id"
            :class="['category-item', { active: hoverCategoryId === category.id }]"
            @mouseenter="hoverCategoryId = category.id"
            @mouseleave="hoverCategoryId = null"
            @click="handleCategoryClick(category)"
          >
            <span class="category-name">{{ category.name }}</span>
            <svg v-if="category.children?.length" class="category-arrow" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>

            <!-- 子分类弹窗 -->
            <div
              v-if="hoverCategoryId === category.id && category.children?.length"
              class="sub-panel"
              @click.stop
            >
              <div v-for="secondLevel in category.children" :key="secondLevel.id" class="sub-group">
                <div class="sub-group-title">{{ secondLevel.name }}</div>
                <div class="sub-group-items">
                  <span
                    v-for="thirdLevel in (secondLevel.children || [])"
                    :key="thirdLevel.id"
                    class="sub-tag"
                    @click.stop="handleSubCategoryClick(thirdLevel)"
                  >
                    {{ thirdLevel.name }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </nav>

        <!-- 右侧轮播图 -->
        <div class="carousel-area">
          <el-carousel :interval="4000" arrow="always" height="100%" indicator-position="inside">
            <el-carousel-item v-for="item in carouselItems" :key="item.id">
              <a v-if="item.linkUrl" :href="item.linkUrl" target="_blank" class="carousel-link">
                <img :src="getBannerImageUrl(item.imageUrl)" :alt="item.title" class="carousel-img" loading="lazy" />
              </a>
              <img v-else :src="getBannerImageUrl(item.imageUrl)" :alt="item.title" class="carousel-img" loading="lazy" />
              <div class="carousel-overlay">
                <span class="carousel-label">{{ item.title }}</span>
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>
      </div>
    </section>

    <!-- 热门商品推荐 -->
    <section class="product-section" ref="productSectionRef">
      <div class="product-section-inner">
        <!-- 区域头部 -->
        <div class="section-header">
          <div class="section-header-left">
            <h2 class="section-title">热门推荐</h2>
            <p class="section-subtitle">精选好货，品质保障</p>
          </div>
          <div class="section-header-right">
            <!-- 搜索区 -->
            <div class="search-bar">
              <div class="search-type-switch">
                <button
                  :class="['search-type-btn', { active: searchType === 'product' }]"
                  @click="searchType = 'product'"
                >商品</button>
                <button
                  :class="['search-type-btn', { active: searchType === 'shop' }]"
                  @click="searchType = 'shop'"
                >店铺</button>
              </div>
              <div class="search-input-wrap">
                <svg class="search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input
                  v-model="searchKeyword"
                  :placeholder="searchType === 'product' ? '搜索商品名称...' : '搜索店铺名称...'"
                  class="search-input"
                  @keyup.enter="handleSearch"
                />
                <button class="search-btn" @click="handleSearch" aria-label="搜索">搜索</button>
              </div>
            </div>
            <button class="view-all-btn" @click="goToProducts">
              查看全部
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="9 18 15 12 9 6"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 商品列表 -->
        <div v-loading="loading" class="product-list">
          <el-empty v-if="!loading && filteredProducts.length === 0" description="暂无商品" />

          <div v-else class="product-grid">
            <div
              v-for="(product, index) in filteredProducts"
              :key="product.id"
              class="product-card"
              :style="{ '--delay': index * 60 + 'ms' }"
              @click="goToProductDetail(product.id)"
            >
              <div class="card-image-wrap">
                <img :src="product.image" :alt="product.name" loading="lazy" />
                <span v-if="product.stock < 10" class="card-badge danger">仅剩 {{ product.stock }} 件</span>
                <span v-else-if="product.stock < 50" class="card-badge warning">库存紧张</span>
              </div>
              <div class="card-body">
                <h3 class="card-title">{{ product.name }}</h3>
                <div class="card-price">
                  <span class="price-symbol">¥</span>
                  <span class="price-value">{{ product.price.toLocaleString() }}</span>
                  <span v-if="product.originalPrice" class="price-original">¥{{ product.originalPrice.toLocaleString() }}</span>
                </div>
                <div class="card-meta">
                  <span>已售 {{ product.sales || 0 }} 件</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="filteredProducts.length > 0" class="load-more-area">
            <div v-if="isLoadingMore" class="load-more-status">
              <svg class="spin" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="var(--color-brand-500)" stroke-width="2.5">
                <circle cx="12" cy="12" r="10" stroke-dasharray="31.4 31.4" stroke-linecap="round"/>
              </svg>
              <span>加载中...</span>
            </div>
            <button
              v-else-if="hasMore"
              class="load-more-btn"
              @click="loadMore"
            >
              加载更多商品
              <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </button>
            <span v-else class="load-end">已展示全部商品</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 页脚 -->
    <footer class="site-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <svg class="footer-logo" viewBox="0 0 32 32" width="24" height="24" fill="none">
            <rect x="2" y="10" width="28" height="20" rx="3" stroke="currentColor" stroke-width="2"/>
            <path d="M8 10V7a8 8 0 0 1 16 0v3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <span>电商商城</span>
        </div>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">联系我们</a>
          <a href="#">帮助中心</a>
          <a href="#">隐私政策</a>
        </div>
        <div class="footer-info">
          <span>© 2026 电商商城 版权所有</span>
          <span class="footer-divider">|</span>
          <span>客服热线: 400-123-4567</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductPage, type ProductPageResult, getCategoryTree, type Category } from '@/api/product'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'
import { getSpuImageUrl, getBannerUrl } from '@/utils/resource'
import { getActiveBanners, type BannerItem } from '@/api/banner'

const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const products = ref<any[]>([])
const categoryTree = ref<Category[]>([])
const selectedCategoryId = ref<number | null>(null)
const hoverCategoryId = ref<number | null>(null)
const searchKeyword = ref('')
const searchType = ref('product')

// 计算一级分类（parentId === 0 为顶级分类）
const firstLevelCategories = computed(() => {
  return categoryTree.value.filter(item => item.parentId === 0)
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 滚动加载
const isLoadingMore = ref(false)
const hasMore = ref(true)
const maxProducts = 200

const carouselItems = ref<BannerItem[]>([])

const getBannerImageUrl = (imageUrl: string) => getBannerUrl(imageUrl)

const fetchBanners = async () => {
  try {
    carouselItems.value = await getActiveBanners()
  } catch { /* ok */ }
}

const isLoggedIn = computed(() => userStore.isLoggedIn)

const filteredProducts = computed(() => {
  if (!selectedCategoryId.value) return products.value
  return products.value.filter(product => product.categoryId === selectedCategoryId.value)
})

const handleCategoryClick = (category: Category) => {
  router.push({ name: 'products', query: { categoryId: category.id } })
}

const handleSubCategoryClick = (category: Category) => {
  router.push({ name: 'products', query: { categoryId: category.id } })
}

const fetchCategories = async () => {
  try {
    const result = await getCategoryTree()
    if (result?.length) categoryTree.value = result
  } catch { /* ok */ }
}

const fetchProducts = async (page = 1, size = 10, append = false) => {
  try {
    if (append) isLoadingMore.value = true
    else loading.value = true

    const params: Record<string, any> = { page, pageSize: size }
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value

    const result: ProductPageResult = await getProductPage(params)

    if (result?.list) {
      currentPage.value = result.page || page
      pageSize.value = result.pageSize || size
      total.value = result.total || result.list.length

      const newProducts = result.list.map((item: any) => ({
        id: item.id,
        name: item.name,
        description: item.description,
        price: item.minPrice || item.price || 0,
        originalPrice: item.originalPrice || item.marketPrice || 0,
        image: item.mainImage ? getSpuImageUrl(item.mainImage) : '',
        images: item.images ? JSON.parse(item.images) : [],
        stock: item.stock || 100,
        category: getCategoryName(item.categoryId),
        categoryId: item.categoryId,
        sales: item.sales,
        unit: item.unit,
        keywords: item.keywords,
        status: item.status,
        createdAt: item.createdAt,
        updatedAt: item.updatedAt
      }))

      if (append) products.value = [...products.value, ...newProducts]
      else products.value = newProducts

      hasMore.value = products.value.length < maxProducts && newProducts.length >= size
    } else {
      hasMore.value = false
    }
  } catch {
    ElMessage.error('获取商品失败，请稍后重试')
    if (!append) { products.value = []; total.value = 0 }
  } finally {
    loading.value = false
    isLoadingMore.value = false
  }
}

const getCategoryName = (categoryId: number): string => {
  const map: Record<number, string> = {
    1: '手机', 2: '电脑', 3: '游戏', 4: '家电',
    5: '手机', 6: '耳机', 7: '显示器', 8: '鞋类', 9: '服装'
  }
  return map[categoryId] || '其他'
}

const loadMore = () => {
  if (isLoadingMore.value || !hasMore.value || loading.value) return
  fetchProducts(currentPage.value + 1, pageSize.value, true)
}

const handleScroll = () => {
  const st = window.scrollY || document.documentElement.scrollTop
  const wh = window.innerHeight || document.documentElement.clientHeight
  const dh = document.documentElement.scrollHeight
  if (st + wh >= dh - 200) loadMore()
}

const goToProducts = () => {
  router.push({ path: '/products', query: searchKeyword.value ? { keyword: searchKeyword.value } : undefined })
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/products', query: { keyword: searchKeyword.value, searchType: searchType.value } })
  } else {
    ElMessage.warning('请输入搜索关键字')
  }
}

const goToProductDetail = (id: number) => {
  router.push(`/product/${id}`)
}

// Intersection Observer for card animation
const productSectionRef = ref<HTMLElement | null>(null)

onMounted(() => {
  fetchCategories()
  fetchProducts()
  fetchBanners()
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.home {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

/* =============================================
   英雄区：分类侧边栏 + 轮播图
   ============================================= */
.hero-section {
  margin-top: var(--header-height);
  background: var(--bg);
  padding: var(--space-4) 0;
}

.hero-layout {
  display: flex;
  max-width: var(--max-width);
  margin: 0 auto;
  gap: var(--space-3);
}

/* --- 分类侧边栏 --- */
.category-sidebar {
  width: 220px;
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.category-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-4) var(--space-5);
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--surface);
  background: var(--color-brand-500);
  letter-spacing: 0.3px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 11px var(--space-5);
  font-size: var(--text-sm);
  color: var(--ink);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
  border-bottom: 1px solid var(--border-light);
}

.category-item:last-child {
  border-bottom: none;
}

.category-item:hover,
.category-item.active {
  background: var(--color-brand-50);
  color: var(--color-brand-600);
  padding-left: var(--space-6);
}

.category-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.category-arrow {
  flex-shrink: 0;
  opacity: 0;
  transition: all var(--transition-fast);
  color: var(--ink-faint);
}

.category-item:hover .category-arrow {
  opacity: 1;
  color: var(--color-brand-500);
}

/* --- 子分类弹窗 --- */
.sub-panel {
  position: absolute;
  left: 100%;
  top: 0;
  width: 640px;
  min-height: 100%;
  background: var(--surface);
  border-radius: 0 var(--radius-lg) var(--radius-lg) 0;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  padding: var(--space-6);
  gap: var(--space-5);
  z-index: var(--z-dropdown);
}

.sub-group {
  flex: 0 0 calc(50% - var(--space-3));
}

.sub-group-title {
  font-weight: 600;
  font-size: var(--text-sm);
  color: var(--ink);
  margin-bottom: var(--space-3);
  padding-bottom: var(--space-1);
  border-bottom: 2px solid var(--color-brand-500);
}

.sub-group-items {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.sub-tag {
  display: inline-block;
  padding: 4px 12px;
  font-size: var(--text-xs);
  color: var(--ink-muted);
  background: var(--surface-muted);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.sub-tag:hover {
  background: var(--color-brand-500);
  color: var(--surface);
  box-shadow: var(--shadow-brand-sm);
}

/* --- 轮播图 --- */
.carousel-area {
  flex: 1;
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  aspect-ratio: 2.8 / 1;
  min-height: 280px;
  max-height: 420px;
}

.carousel-area :deep(.el-carousel) {
  height: 100% !important;
}

.carousel-area :deep(.el-carousel__container) {
  height: 100% !important;
}

.carousel-area :deep(.el-carousel__indicator--active .el-carousel__button) {
  background: var(--color-brand-500);
}

.carousel-link {
  display: block;
  width: 100%;
  height: 100%;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.carousel-area:hover .carousel-img {
  transform: scale(1.04);
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: var(--space-4) var(--space-6);
  background: linear-gradient(transparent, rgba(0,0,0,0.6));
  pointer-events: none;
}

.carousel-label {
  color: var(--surface);
  font-size: var(--text-base);
  font-weight: 600;
  text-shadow: 0 1px 4px rgba(0,0,0,0.3);
}

/* =============================================
   商品区域
   ============================================= */
.product-section {
  flex: 1;
  padding: var(--space-2) 0 var(--space-10);
}

.product-section-inner {
  max-width: var(--max-width);
  margin: 0 auto;
}

/* --- 区域头部 --- */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  padding: var(--space-5) var(--space-6);
  background: var(--surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  flex-wrap: wrap;
  gap: var(--space-4);
}

.section-header-left {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.section-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.01em;
}

.section-subtitle {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.section-header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  background: var(--surface-muted);
  border-radius: var(--radius-full);
  padding: 3px;
  border: 1px solid var(--border);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.search-bar:focus-within {
  border-color: var(--color-brand-500);
  box-shadow: 0 0 0 3px rgba(255, 68, 0, 0.1);
}

.search-type-switch {
  display: flex;
  gap: 2px;
  padding: 2px;
  background: var(--border-light);
  border-radius: var(--radius-full);
}

.search-type-btn {
  padding: 4px 12px;
  border: none;
  background: transparent;
  font-size: var(--text-xs);
  color: var(--ink-muted);
  cursor: pointer;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  font-weight: 500;
}

.search-type-btn.active {
  background: var(--surface);
  color: var(--ink);
  box-shadow: var(--shadow-sm);
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-2);
}

.search-icon {
  color: var(--ink-faint);
  flex-shrink: 0;
}

.search-input {
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--ink);
  width: 180px;
  outline: none;
  font-family: inherit;
}

.search-input::placeholder {
  color: var(--ink-faint);
}

.search-btn {
  padding: 6px 16px;
  border: none;
  background: var(--color-brand-500);
  color: var(--surface);
  font-size: var(--text-xs);
  font-weight: 600;
  cursor: pointer;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  font-family: inherit;
}

.search-btn:hover {
  background: var(--color-brand-600);
  box-shadow: var(--shadow-brand-sm);
}

/* 查看全部按钮 */
.view-all-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: 0;
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--ink-muted);
  cursor: pointer;
  transition: color var(--transition-fast);
  font-family: inherit;
  font-weight: 500;
}

.view-all-btn:hover {
  color: var(--color-brand-500);
}

.view-all-btn svg {
  transition: transform var(--transition-fast);
}

.view-all-btn:hover svg {
  transform: translateX(3px);
}

/* --- 商品网格 --- */
.product-list {
  min-height: 200px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
}

.product-card {
  background: var(--surface);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-sm);
  animation: card-enter 0.4s ease backwards;
  animation-delay: var(--delay, 0ms);
}

@keyframes card-enter {
  from {
    opacity: 0;
    transform: translateY(16px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.product-card:hover {
  box-shadow: var(--shadow-brand-md);
  transform: translateY(-4px);
}

.card-image-wrap {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: var(--surface-soft);
}

.card-image-wrap img {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 88%;
  height: 88%;
  object-fit: contain;
  transform: translate(-50%, -50%);
  transition: transform var(--transition-base);
}

.product-card:hover .card-image-wrap img {
  transform: translate(-50%, -50%) scale(1.08);
}

.card-badge {
  position: absolute;
  top: var(--space-2);
  right: var(--space-2);
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--radius-sm);
  color: var(--surface);
  line-height: 1.4;
}

.card-badge.danger {
  background: var(--color-danger);
}

.card-badge.warning {
  background: var(--color-warning);
}

.card-body {
  padding: var(--space-3) var(--space-4) var(--space-4);
}

.card-title {
  margin: 0 0 var(--space-2);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--ink);
  line-height: 1.4;
  height: 2.8em;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color var(--transition-fast);
}

.product-card:hover .card-title {
  color: var(--color-brand-600);
}

.card-price {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.price-symbol {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--color-brand-500);
}

.price-value {
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-brand-500);
  letter-spacing: -0.02em;
}

.price-original {
  font-size: var(--text-xs);
  color: var(--ink-faint);
  text-decoration: line-through;
}

.card-meta {
  font-size: var(--text-xs);
  color: var(--ink-faint);
  padding-top: var(--space-2);
  border-top: 1px solid var(--border-light);
}

/* --- 加载更多 --- */
.load-more-area {
  display: flex;
  justify-content: center;
  padding: var(--space-10) 0 var(--space-6);
}

.load-more-status {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--ink-faint);
}

.spin {
  animation: rotate 0.8s linear infinite;
}

@keyframes rotate {
  to { transform: rotate(360deg); }
}

.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-3) var(--space-8);
  border: 1px solid var(--border);
  background: var(--surface);
  font-size: var(--text-sm);
  color: var(--ink-muted);
  cursor: pointer;
  border-radius: var(--radius-full);
  transition: all var(--transition-fast);
  font-family: inherit;
}

.load-more-btn:hover {
  border-color: var(--color-brand-500);
  color: var(--color-brand-500);
  box-shadow: var(--shadow-brand-sm);
}

.load-more-btn svg {
  transition: transform var(--transition-fast);
}

.load-more-btn:hover svg {
  transform: translateY(2px);
}

.load-end {
  font-size: var(--text-sm);
  color: var(--ink-faint);
}

/* =============================================
   页脚
   ============================================= */
.site-footer {
  margin-top: auto;
  background: var(--ink);
  color: rgba(255,255,255,0.8);
  padding: var(--space-10) var(--space-5);
  text-align: center;
}

.footer-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-4);
}

.footer-brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--surface);
}

.footer-logo {
  color: var(--color-brand-500);
}

.footer-links {
  display: flex;
  gap: var(--space-6);
  flex-wrap: wrap;
  justify-content: center;
}

.footer-links a {
  color: rgba(255,255,255,0.6);
  font-size: var(--text-sm);
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--surface);
}

.footer-info {
  font-size: var(--text-xs);
  color: rgba(255,255,255,0.4);
  display: flex;
  gap: var(--space-3);
  align-items: center;
  flex-wrap: wrap;
  justify-content: center;
}

.footer-divider {
  opacity: 0.3;
}

/* =============================================
   响应式
   ============================================= */
@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: var(--space-3) var(--space-3);
  }

  .hero-layout {
    flex-direction: column;
    gap: var(--space-3);
  }

  .category-sidebar {
    width: 100%;
    display: flex;
    flex-wrap: nowrap;
    overflow-x: auto;
    border-radius: var(--radius-lg);
    -webkit-overflow-scrolling: touch;
  }

  .category-header {
    display: none;
  }

  .category-item {
    flex: 0 0 auto;
    padding: var(--space-3) var(--space-4);
    border-bottom: none;
    border-right: 1px solid var(--border-light);
    white-space: nowrap;
    font-size: var(--text-xs);
  }

  .category-item:hover,
  .category-item.active {
    padding-left: var(--space-4);
    background: var(--color-brand-50);
  }

  .category-arrow,
  .sub-panel {
    display: none;
  }

  .carousel-area {
    min-height: 200px;
    max-height: 260px;
  }

  .product-section {
    padding: var(--space-1) var(--space-3) var(--space-8);
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
    padding: var(--space-4);
    gap: var(--space-3);
  }

  .section-header-right {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-3);
  }

  .search-bar {
    width: 100%;
  }

  .search-input {
    width: 100px;
  }

  .search-btn {
    padding: 6px 12px;
  }

  .view-all-btn {
    align-self: flex-end;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-3);
  }

  .card-body {
    padding: var(--space-2) var(--space-3) var(--space-3);
  }

  .price-value {
    font-size: var(--text-lg);
  }

  .site-footer {
    padding: var(--space-8) var(--space-4);
  }

  .footer-links {
    gap: var(--space-4);
  }
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-2);
  }
}
</style>