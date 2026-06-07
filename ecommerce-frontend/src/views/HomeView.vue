<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <NavBar />

    <!-- 分类和轮播图区域 -->
    <div class="main-banner">
      <!-- 左侧分类导航 -->
      <div class="category-nav">
        <div class="nav-title">全部商品分类</div>
        <div
          v-for="category in firstLevelCategories"
          :key="category.id"
          :class="['nav-item', { active: hoverCategoryId === category.id || selectedFirstCategoryId === category.id }]"
          @mouseenter="hoverCategoryId = category.id"
          @mouseleave="hoverCategoryId = null"
          @click="handleCategoryClick(category)"
        >
          <span class="nav-icon">☰</span>
          <span class="nav-name">{{ category.name }}</span>
          <span class="nav-arrow">›</span>
          
          <!-- 子分类弹窗 -->
          <div 
            v-show="hoverCategoryId === category.id && category.children && category.children.length > 0" 
            class="sub-category-popup"
          >
            <div v-for="secondLevel in category.children" :key="secondLevel.id" class="popup-column">
              <div class="popup-title">{{ secondLevel.name }}</div>
              <div v-for="thirdLevel in (secondLevel.children || [])" :key="thirdLevel.id" 
                class="popup-item"
                :class="{ active: selectedCategoryId === thirdLevel.id }"
                @click.stop="handleSubCategoryClick(thirdLevel)"
              >
                {{ thirdLevel.name }}
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧轮播图 -->
      <div class="carousel-wrapper">
        <el-carousel :interval="3000" arrow="always" height="100%">
          <el-carousel-item v-for="item in carouselItems" :key="item.id">
            <div class="carousel-slide">
              <a v-if="item.linkUrl" :href="item.linkUrl" target="_blank">
                <img :src="getBannerImageUrl(item.imageUrl)" :alt="item.title" class="carousel-image" />
              </a>
              <img v-else :src="getBannerImageUrl(item.imageUrl)" :alt="item.title" class="carousel-image" />
              <div class="carousel-title">{{ item.title }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>
    
    <!-- 清除筛选按钮 -->
    <div class="clear-filter" v-if="selectedCategoryId !== null">
      <el-button size="small" @click="clearCategory">清除筛选</el-button>
    </div>

    <!-- 热门商品推荐 -->
    <div class="product-section">
      <div class="section-header">
        <h2>热门商品推荐</h2>
        <div class="section-actions">
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索商品..."
              class="search-input"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #append>
                <el-button :icon="Search" @click="handleSearch" />
              </template>
            </el-input>
          </div>
          <el-link type="primary" @click="goToProducts">查看更多</el-link>
          <span class="product-count">共 {{ filteredProducts.length }} 件商品</span>
        </div>
      </div>
      
      <div v-loading="loading" class="product-list">
        <el-empty v-if="filteredProducts.length === 0" description="暂无商品" />
        
        <div v-else class="product-grid">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="product-grid-item"
          >
            <el-card class="product-card" shadow="hover" @click="goToProductDetail(product.id)">
              <div class="product-image-container">
                <img :src="product.image" :alt="product.name" class="product-image" />
                <el-tag v-if="product.stock < 10" type="danger" class="stock-tag">
                  仅剩{{ product.stock }}件
                </el-tag>
                <el-tag v-else-if="product.stock < 50" type="warning" class="stock-tag">
                  库存紧张
                </el-tag>
              </div>
              <div class="product-info">
                <div class="product-category">
                  <el-tag size="small" type="info">{{ product.category }}</el-tag>
                </div>
                <h3 class="product-name">{{ product.name }}</h3>
                <p class="product-description">{{ product.description }}</p>
                <div class="product-price">
                  <span class="current-price">¥{{ product.price.toLocaleString() }}</span>
                  <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice.toLocaleString() }}</span>
                  <span class="discount" v-if="product.originalPrice">
                    {{ Math.round((1 - product.price / product.originalPrice) * 100) }}折
                  </span>
                </div>
                <div class="product-sales">
                  <span>已售 {{ product.sales }}件</span>
                </div>
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 滚动加载提示 -->
        <div v-if="hasMore" class="loading-more">
          <div v-if="isLoadingMore" class="loading-indicator">
            <svg class="loading-spinner" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="4"></circle>
            </svg>
            <span class="loading-text">加载中...</span>
          </div>
          <span v-else class="load-more-text">下拉加载更多</span>
        </div>
        <div v-else-if="products.length >= maxProducts" class="loading-more">
          <span class="no-more-text">已展示全部商品（最多200个）</span>
        </div>
        

      </div>
    </div>

    <!-- 页脚 -->
    <el-footer class="footer">
      <div class="footer-content">
        <p>© 2024 电商商城 版权所有</p>
        <p>联系我们: service@ecommerce.com | 客服热线: 400-123-4567</p>
      </div>
    </el-footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getProductPage, type ProductPageResult, getCategoryTree, type Category } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'
import { getSpuImageUrl } from '@/utils/resource'
import { getActiveBanners, type BannerItem } from '@/api/banner'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const products = ref<any[]>([])
const categoryTree = ref<Category[]>([])
const selectedCategoryId = ref<number | null>(null)
const selectedFirstCategoryId = ref<number | null>(null)
const hoverCategoryId = ref<number | null>(null)
const searchKeyword = ref('')

// 计算一级分类列表
const firstLevelCategories = computed(() => {
  return categoryTree.value.filter(item => item.level === 1)
})

// 当前选中的一级分类
const selectedFirstCategory = computed(() => {
  return categoryTree.value.find(item => item.id === selectedFirstCategoryId.value)
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 滚动加载相关
const isLoadingMore = ref(false)
const hasMore = ref(true)
const maxProducts = 200 // 最多展示200个商品

const carouselItems = ref<BannerItem[]>([])

// 轮播图图片URL
const getBannerImageUrl = (imageUrl: string) => {
  if (!imageUrl) return ''
  return `/api/file/preview?relativePath=${imageUrl}&subDir=banners`
}

// 获取轮播图数据
const fetchBanners = async () => {
  try {
    carouselItems.value = await getActiveBanners()
  } catch {
    // 拦截器已处理
  }
}

// 计算登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn)

// 过滤商品
const filteredProducts = computed(() => {
  if (!selectedCategoryId.value) {
    return products.value
  }
  return products.value.filter(product => product.categoryId === selectedCategoryId.value)
})

// 筛选商品
const filterProducts = () => {
  currentPage.value = 1
  fetchProducts(1, pageSize.value)
}

// 点击分类 - 跳转到商品列表页
const handleCategoryClick = (category: Category) => {
  router.push({
    name: 'products',
    query: { categoryId: category.id }
  })
}

// 点击子分类 - 跳转到商品列表页
const handleSubCategoryClick = (category: Category) => {
  router.push({
    name: 'products',
    query: { categoryId: category.id }
  })
}

// 清除分类筛选
const clearCategory = () => {
  selectedCategoryId.value = null
  selectedFirstCategoryId.value = null
  filterProducts()
}

const fetchCategories = async () => {
  try {
    const result = await getCategoryTree()
    if (result && result.length > 0) {
      categoryTree.value = result
    }
  } catch (error) {
  }
}

const fetchProducts = async (page = 1, size = 10, append = false) => {
  try {
    if (append) {
      isLoadingMore.value = true
    } else {
      loading.value = true
    }
    
    const params: Record<string, any> = { page, pageSize: size }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    
    const result: ProductPageResult = await getProductPage(params)
    
    if (result && result.list) {
      currentPage.value = result.page || page
      pageSize.value = result.pageSize || size
      total.value = result.total || result.list.length
      
      // 转换商品数据格式
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
      
      // 如果是追加模式，合并数据；否则替换数据
      if (append) {
        products.value = [...products.value, ...newProducts]
      } else {
        products.value = newProducts
      }
      
      // 检查是否还有更多数据
      if (products.value.length >= maxProducts || newProducts.length < size) {
        hasMore.value = false
      }
    } else {
      hasMore.value = false
    }
  } catch {
    ElMessage.error('获取商品失败，请稍后重试')
    if (!append) {
      products.value = []
      total.value = 0
    }
  } finally {
    loading.value = false
    isLoadingMore.value = false
  }
}

// 根据分类ID获取分类名称
const getCategoryName = (categoryId: number): string => {
  const categoryMap: Record<number, string> = {
    1: '手机',
    2: '电脑',
    3: '游戏',
    4: '家电',
    5: '手机',
    6: '耳机',
    7: '显示器',
    8: '鞋类',
    9: '服装'
  }
  return categoryMap[categoryId] || '其他'
}

// 滚动加载更多
const loadMore = () => {
  if (isLoadingMore.value || !hasMore.value || loading.value) {
    return
  }
  const nextPage = currentPage.value + 1
  fetchProducts(nextPage, pageSize.value, true)
}

// 滚动事件处理
const handleScroll = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  const windowHeight = window.innerHeight || document.documentElement.clientHeight
  const documentHeight = document.documentElement.scrollHeight
  
  // 当滚动到距离底部200px时触发加载
  if (scrollTop + windowHeight >= documentHeight - 200) {
    loadMore()
  }
}
// 跳转到商品列表页
const goToProducts = () => {
  if (searchKeyword.value) {
    router.push({
      path: '/products',
      query: { keyword: searchKeyword.value }
    })
  } else {
    router.push('/products')
  }
}

// 搜索商品
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/products',
      query: { keyword: searchKeyword.value }
    })
  } else {
    ElMessage.warning('请输入搜索关键字')
  }
}

// 跳转到商品详情页
const goToProductDetail = (id: number) => {
  router.push(`/product/${id}`)
}

// 加入购物车
const addToCart = async (product: any) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    await addToCartApi({
      skuId: product.id,
      quantity: 1,
      productImage: product.image
    })
    ElMessage.success('已加入购物车')
  } catch {
  }
}

// 立即购买
const buyNow = (product: any) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 跳转到订单确认页面
  router.push({
    path: '/order/confirm',
    query: { productId: product.id, quantity: 1 }
  })
}

// 组件挂载时获取数据
onMounted(() => {
  fetchCategories()
  fetchProducts()
  fetchBanners()
  window.addEventListener('scroll', handleScroll)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.carousel-section {
  margin-bottom: 40px;
  width: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.carousel-slide:hover .carousel-image {
  transform: scale(1.03);
}

.carousel-slide {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.carousel-slide::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0,0,0,0.15) 0%, transparent 50%);
  pointer-events: none;
}

.carousel-title {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 24px;
  background: linear-gradient(transparent 0%, rgba(0, 0, 0, 0.75) 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  text-align: left;
  letter-spacing: 0.5px;
}

.carousel-caption {
  position: absolute;
  bottom: 40px;
  left: 40px;
  color: white;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.5);
}

.carousel-caption h3 {
  margin: 0 0 10px 0;
  font-size: 32px;
}

.carousel-caption p {
  margin: 0;
  font-size: 18px;
}

.category-section {
  max-width: 1200px;
  margin: 0 auto 40px;
  padding: 0 20px;
  width: 100%;
}

.main-banner {
  display: flex;
  margin: 80px auto 0;
  gap: 12px;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
  max-width: 1400px;
  padding: 0 20px;
}

.category-nav {
  width: 220px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4400 100%);
  color: #fff;
  position: relative;
  z-index: 100;
  border-radius: 8px 0 0 8px;
  box-shadow: 0 4px 12px rgba(255, 68, 0, 0.15);
}

.nav-title {
  padding: 16px 20px;
  font-weight: bold;
  font-size: 16px;
  background: rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-title::before {
  content: '☰';
  font-size: 18px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 14px 18px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  position: relative;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background: #fff;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.nav-item:hover::before, .nav-item.active::before {
  opacity: 1;
}

.nav-item:hover, .nav-item.active {
  background-color: #fff;
  color: #ff4400;
  padding-left: 22px;
  font-weight: 500;
}

.nav-icon {
  margin-right: 10px;
  font-size: 14px;
  opacity: 0.8;
}

.nav-name {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-arrow {
  font-size: 18px;
  opacity: 0.6;
  transition: transform 0.3s ease;
}

.nav-item:hover .nav-arrow {
  transform: translateX(3px);
  opacity: 1;
}

.sub-category-popup {
  position: absolute;
  left: 220px;
  top: 0;
  width: 680px;
  min-height: 420px;
  max-height: 500px;
  background-color: #fff;
  border: none;
  border-radius: 0 8px 8px 0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-wrap: wrap;
  padding: 20px;
  z-index: 1000;
  overflow-y: auto;
}

.popup-column {
  width: 33.33%;
  padding: 0 12px;
  margin-bottom: 15px;
}

.popup-title {
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #ff4400;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.popup-title::before {
  content: '▸';
  color: #ff4400;
  margin-right: 6px;
  font-size: 12px;
}

.popup-item {
  display: inline-block;
  padding: 6px 12px;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 4px;
  margin: 2px;
  background-color: #f8f9fa;
}

.popup-item:hover, .popup-item.active {
  color: #fff;
  background-color: #ff4400;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.3);
}

.carousel-wrapper {
  flex: 1;
  border-radius: 0 8px 8px 0;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  aspect-ratio: 2.8 / 1;
  min-height: 240px;
  max-height: 420px;
}

.carousel-wrapper :deep(.el-carousel) {
  height: 100% !important;
  border-radius: 0 8px 8px 0;
}

.carousel-wrapper :deep(.el-carousel__container) {
  height: 100% !important;
}

.clear-filter {
  margin-top: 15px;
  text-align: right;
  padding: 0 10px;
}

.sub-category-group {
  margin-bottom: 20px;
}

.sub-category-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #333;
  margin-bottom: 10px;
  font-size: 14px;
}

.title-arrow {
  color: #ff4400;
  margin-right: 5px;
}

.sub-category-items {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.sub-item {
  padding: 4px 12px;
  background-color: #f5f5f5;
  border-radius: 3px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.sub-item:hover {
  background-color: #fff;
  color: #ff4400;
  border: 1px solid #ff4400;
}

.sub-item.active {
  background-color: #ff4400;
  color: #fff;
}

.product-section {
  flex: 1;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
  gap: 15px;
  padding: 20px 25px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header h2 {
  margin: 0;
  color: #1a1a1a;
  font-size: 24px;
  font-weight: 700;
  position: relative;
  padding-left: 20px;
  letter-spacing: 0.5px;
}

.section-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4400 100%);
  border-radius: 2px;
}

.section-header::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e8e8e8, transparent);
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.search-box {
  flex-shrink: 0;
}

.search-input {
  width: 220px;
  border-radius: 20px;
  border-color: #e8e8e8;
  transition: all 0.3s ease;
}

.search-input:focus {
  box-shadow: 0 0 0 3px rgba(255, 68, 0, 0.1);
  border-color: #ff4400;
}

@media screen and (max-width: 768px) {
  .search-input {
    width: 160px;
  }
}

.product-count {
  color: #888;
  font-size: 13px;
  padding: 6px 14px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef0f3 100%);
  border-radius: 20px;
  font-weight: 500;
}

.product-list {
  margin-bottom: 40px;
  width: 100%;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  width: 100%;
}

.product-grid-item {
  width: 100%;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
  
  .product-card {
    margin-bottom: 10px;
  }
}

@media screen and (min-width: 769px) and (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}

@media screen and (min-width: 993px) and (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 14px;
  }
}

@media screen and (min-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 15px;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 35px;
  gap: 20px;
  padding: 25px 30px;
  background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
}

.page-info {
  color: #666;
  font-size: 14px;
  font-weight: 500;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #eef0f3 100%);
  border-radius: 20px;
}

.loading-more {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 30px 0;
  margin-top: 20px;
}

.load-more-text {
  color: #999;
  font-size: 14px;
}

.no-more-text {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  animation: spin 1s linear infinite;
}

.loading-spinner .path {
  stroke: #ff4400;
  stroke-linecap: round;
  animation: dash 1.5s ease-in-out infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes dash {
  0% {
    stroke-dasharray: 1, 150;
    stroke-dashoffset: 0;
  }
  50% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -35;
  }
  100% {
    stroke-dasharray: 90, 150;
    stroke-dashoffset: -124;
  }
}

.loading-text {
  color: #999;
  font-size: 14px;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  border: none;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #ff6b6b, #ff4400, #ffaa00);
  transform: scaleX(0);
  transition: transform 0.35s ease;
}

.product-card:hover::before {
  transform: scaleX(1);
}

.product-card:hover {
  box-shadow: 0 12px 32px rgba(255, 68, 0, 0.15);
  transform: translateY(-6px);
}

.product-image-container {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  border-radius: 12px 12px 0 0;
}

.product-image-container::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(0,0,0,0.03) 100%);
  pointer-events: none;
}

.product-image {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 90%;
  height: 90%;
  object-fit: contain;
  transform: translate(-50%, -50%);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover .product-image {
  transform: translate(-50%, -50%) scale(1.08);
}

.stock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
  border-radius: 4px;
  padding: 2px 8px;
  font-size: 11px;
}

.product-info {
  padding: 15px;
  background-color: #fff;
}

.product-category {
  display: none;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #1a1a1a;
  font-weight: 500;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.25s ease;
}

.product-card:hover .product-name {
  color: #ff4400;
}

.product-description {
  display: none;
}

.product-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 8px;
}

.current-price {
  font-size: 24px;
  color: #ff4400;
  font-weight: 700;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: -0.5px;
}

.original-price {
  display: none;
}

.discount {
  display: none;
}

.product-stock {
  display: none;
}

.product-sales {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #f0f0f0;
}

.product-actions {
  display: none;
}

.footer {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: rgba(255, 255, 255, 0.85);
  text-align: center;
  padding: 30px 20px;
  margin-top: 40px;
  width: 100%;
  border-top: 3px solid #ff4400;
}

.footer-content p {
  margin: 6px 0;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.footer-content p:first-child {
  font-weight: 600;
  font-size: 15px;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .header-content {
    padding: 0 15px;
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .nav-menu {
    width: 100%;
    overflow-x: auto;
  }
  
  .carousel-section {
    margin-bottom: 30px;
  }
  
  .carousel-caption {
    bottom: 20px;
    left: 20px;
  }
  
  .carousel-caption h3 {
    font-size: 20px;
  }
  
  .carousel-caption p {
    font-size: 14px;
  }
  
  .category-section,
  .product-section {
    padding: 0 15px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .section-header h2 {
    font-size: 22px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: space-between;
  }
  
  .product-image {
    height: 150px;
  }
  
  .product-name {
    font-size: 14px;
  }
  
  .current-price {
    font-size: 18px;
  }
  
  .product-actions {
    flex-direction: column;
  }
  
  .product-actions .el-button {
    width: 100%;
  }
}

/* 平板端适配 */
@media (min-width: 768px) and (max-width: 1023px) {
  .header-content {
    padding: 0 30px;
  }
  
  .carousel-caption h3 {
    font-size: 28px;
  }
  
  .carousel-caption p {
    font-size: 16px;
  }
  
  .category-section,
  .product-section {
    padding: 0 30px;
  }
  
  .product-image {
    height: 180px;
  }
}

/* PC端适配 */
@media (min-width: 1024px) {
  .header-content {
    padding: 0 40px;
  }
  
  .category-section,
  .product-section {
    padding: 0 40px;
  }
  
  .carousel-section {
    max-width: 1200px;
    margin: 0 auto 40px;
  }
}
</style>