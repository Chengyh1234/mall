<template>
  <div class="product-list-container">
    <NavBar />

    <div class="main-content">
      <!-- 搜索和筛选栏 -->
      <div class="search-filter-bar">
        <!-- 搜索框 -->
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
        
        <!-- 分类选择 -->
        <div class="filter-select">
          <el-select
            v-model="selectedCategoryId"
            placeholder="选择分类"
            clearable
            class="filter-select-input"
            @change="handleFilterChange"
          >
            <el-option
              v-for="cat in flatCategories"
              :key="cat.id"
              :label="getCategoryLabel(cat)"
              :value="cat.id"
            />
          </el-select>
        </div>
        
        <!-- 品牌选择 -->
        <div class="filter-select">
          <el-select
            v-model="selectedBrandId"
            placeholder="选择品牌"
            clearable
            class="filter-select-input"
            @change="handleFilterChange"
          >
            <el-option
              v-for="brand in brandList"
              :key="brand.id"
              :label="brand.name"
              :value="brand.id"
            />
          </el-select>
        </div>
        
        <!-- 当前筛选标签 -->
        <div class="filter-tags" v-if="selectedCategoryId || selectedBrandId || searchKeyword">
          <el-tag v-if="selectedCategoryId" type="warning" closable @close="clearCategory">
            {{ getSelectedCategoryName }}
          </el-tag>
          <el-tag v-if="selectedBrandId" type="success" closable @close="clearBrand">
            {{ getSelectedBrandName }}
          </el-tag>
          <el-tag v-if="searchKeyword" type="primary" closable @close="clearSearch">
            关键字: {{ searchKeyword }}
          </el-tag>
          <el-button size="small" link @click="clearAll">清除全部</el-button>
        </div>
      </div>

      <!-- 商品列表 -->
      <div class="product-section">
        <div class="section-header">
          <h2>商品列表</h2>
          <span class="product-count">共 {{ total }} 件商品</span>
        </div>

        <div v-if="loading" class="loading-container">
          <div class="loading-indicator">
            <svg class="loading-spinner" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="4"></circle>
            </svg>
            <span class="loading-text">加载中...</span>
          </div>
        </div>

        <div v-else-if="products.length === 0" class="empty-container">
          <p>暂无商品</p>
        </div>

        <div v-else class="product-grid">
          <div
            v-for="item in products"
            :key="item.id"
            class="product-card"
            @click="goToProductDetail(item.id)"
          >
            <div class="product-image-container">
              <img :src="item.image" :alt="item.name" class="product-image" />
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ item.name }}</h3>
              <p class="product-sales">已售 {{ item.sales }} 件</p>
              <div class="product-price">
                <span class="price">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页组件 -->
        <div v-if="total > 0" class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-size="pageSize"
            layout="prev, pager, next, total"
            :total="total"
            :page-count="Math.ceil(total / pageSize)"
          >
          </el-pagination>
          <div class="page-info">共 {{ Math.ceil(total / pageSize) }} 页</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getProductPage, type ProductPageResult, getCategoryTree, type Category, getBrandList, type Brand } from '@/api/product'
import NavBar from '@/components/NavBar.vue'
import { getSpuImageUrl } from '@/utils/resource'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const products = ref<any[]>([])
const categoryTree = ref<Category[]>([])
const brandList = ref<Brand[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const selectedCategoryId = ref<number | null>(null)
const selectedBrandId = ref<number | null>(null)

// 扁平化分类树
interface CategoryWithLabel extends Category {
  _label?: string
}

const flatCategories = computed((): CategoryWithLabel[] => {
  const result: CategoryWithLabel[] = []
  const flatten = (categories: Category[], prefix = '') => {
    for (const cat of categories) {
      const newCat: CategoryWithLabel = Object.assign({}, cat)
      newCat._label = prefix + cat.name
      result.push(newCat)
      if (cat.children && cat.children.length > 0) {
        flatten(cat.children, prefix + cat.name + ' > ')
      }
    }
  }
  flatten(categoryTree.value)
  return result
})

// 获取分类标签
const getCategoryLabel = (cat: CategoryWithLabel) => {
  return cat._label || cat.name
}

// 获取选中的分类名称
const getSelectedCategoryName = computed(() => {
  if (!selectedCategoryId.value) return ''
  const cat = flatCategories.value.find(c => c.id === selectedCategoryId.value)
  return cat ? cat.name : ''
})

// 获取选中的品牌名称
const getSelectedBrandName = computed(() => {
  if (!selectedBrandId.value) return ''
  const brand = brandList.value.find(b => b.id === selectedBrandId.value)
  return brand ? brand.name : ''
})

// 获取商品列表
const fetchProducts = async (page = 1, size = 10) => {
  try {
    loading.value = true
    const params: Record<string, any> = { page, pageSize: size }
    if (selectedCategoryId.value) {
      params.categoryId = selectedCategoryId.value
    }
    if (selectedBrandId.value) {
      params.brandId = selectedBrandId.value
    }
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    const result: ProductPageResult = await getProductPage(params)
    
    if (result && result.list) {
      currentPage.value = result.page || page
      pageSize.value = result.pageSize || size
      total.value = result.total || result.list.length
      
      products.value = result.list.map((item: any) => ({
        id: item.id,
        name: item.name,
        description: item.description,
        price: item.price || item.skuPrice || 0,
        originalPrice: item.originalPrice || item.marketPrice || 0,
        image: item.mainImage ? getSpuImageUrl(item.mainImage) : 'https://via.placeholder.com/300x300/f5f5f5/999999?text=找不到图片',
        images: item.images ? JSON.parse(item.images) : [],
        stock: item.stock || 100,
        sales: item.sales,
        unit: item.unit,
        categoryId: item.categoryId
      }))
    }
  } catch {
    ElMessage.error('获取商品失败，请稍后重试')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索商品
const handleSearch = () => {
  currentPage.value = 1
  updateQuery()
  fetchProducts(1, pageSize.value)
}

// 筛选条件变化处理
const handleFilterChange = () => {
  currentPage.value = 1
  updateQuery()
  fetchProducts(1, pageSize.value)
}

// 更新URL参数
const updateQuery = () => {
  const query: Record<string, any> = {}
  if (selectedCategoryId.value) {
    query.categoryId = selectedCategoryId.value
  }
  if (selectedBrandId.value) {
    query.brandId = selectedBrandId.value
  }
  if (searchKeyword.value) {
    query.keyword = searchKeyword.value
  }
  router.replace({
    path: '/products',
    query
  })
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  updateQuery()
  fetchProducts(1, pageSize.value)
}

// 清除分类
const clearCategory = () => {
  selectedCategoryId.value = null
  currentPage.value = 1
  updateQuery()
  fetchProducts(1, pageSize.value)
}

// 清除品牌
const clearBrand = () => {
  selectedBrandId.value = null
  currentPage.value = 1
  updateQuery()
  fetchProducts(1, pageSize.value)
}

// 清除全部筛选
const clearAll = () => {
  searchKeyword.value = ''
  selectedCategoryId.value = null
  selectedBrandId.value = null
  currentPage.value = 1
  router.push('/products')
  fetchProducts(1, pageSize.value)
}

// 获取分类树
const fetchCategories = async () => {
  try {
    const result = await getCategoryTree()
    if (result && result.length > 0) {
      categoryTree.value = result
    }
  } catch {
  }
}

// 获取品牌列表
const fetchBrands = async () => {
  try {
    const result = await getBrandList()
    if (result && result.length > 0) {
      brandList.value = result
    }
  } catch {
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  fetchProducts(1, size)
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  fetchProducts(page, pageSize.value)
}

// 跳转到商品详情
const goToProductDetail = (id: number) => {
  router.push(`/product/${id}`)
}

const handleMenuSelect = (index: string) => {
  switch (index) {
    case 'home':
      router.push('/home')
      break
    case 'products':
      router.push('/products')
      break
    case 'cart':
      router.push('/cart')
      break
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      handleLogout()
      break
    case 'login':
      router.push('/login')
      break
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 监听URL参数变化
watch(() => route.query, (newQuery) => {
  currentPage.value = 1
  if (newQuery.keyword) {
    searchKeyword.value = String(newQuery.keyword)
  } else {
    searchKeyword.value = ''
  }
  if (newQuery.categoryId) {
    selectedCategoryId.value = Number(newQuery.categoryId)
  } else {
    selectedCategoryId.value = null
  }
  if (newQuery.brandId) {
    selectedBrandId.value = Number(newQuery.brandId)
  } else {
    selectedBrandId.value = null
  }
  fetchProducts(1, pageSize.value)
}, { immediate: false })

onMounted(() => {
  fetchCategories()
  fetchBrands()
  // 从URL初始化筛选条件
  if (route.query.keyword) {
    searchKeyword.value = String(route.query.keyword)
  }
  if (route.query.categoryId) {
    selectedCategoryId.value = Number(route.query.categoryId)
  }
  if (route.query.brandId) {
    selectedBrandId.value = Number(route.query.brandId)
  }
  fetchProducts(1, pageSize.value)
})
</script>

<style scoped>
.product-list-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;
  background-color: #fff;
  padding: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 30px;
  width: 100%;
}

.logo {
  flex-shrink: 0;
}

.logo h2 {
  margin: 0;
  color: #ff4400;
  font-size: 26px;
  font-weight: 700;
}

.nav-menu {
  border-bottom: none;
  margin-left: auto;
  display: flex;
  gap: 15px;
}

.nav-menu :deep(.el-menu-item) {
  padding: 0 20px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.nav-menu :deep(.el-menu-item.is-active) {
  color: #ff4400;
}

.main-content {
  padding: 80px 20px 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-filter-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
  padding: 15px 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex-wrap: wrap;
}

.search-box {
  flex-shrink: 0;
}

.search-input {
  width: 250px;
}

.filter-select {
  flex-shrink: 0;
}

.filter-select-input {
  width: 160px;
}

.filter-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  flex: 1;
}

.filter-label {
  color: #666;
  font-size: 14px;
  white-space: nowrap;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  padding: 12px 18px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border-radius: 8px;
  border-left: 4px solid #ff4400;
}

.category-label {
  color: #666;
  font-size: 14px;
}

.category-name {
  color: #ff4400;
  font-weight: 600;
  font-size: 15px;
}

.product-section {
  background-color: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
  font-weight: 600;
  position: relative;
  padding-left: 15px;
}

.section-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4400 100%);
  border-radius: 2px;
}

.product-count {
  color: #666;
  font-size: 14px;
  padding: 6px 12px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.loading-container, .empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
  color: #999;
}

.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-spinner {
  width: 32px;
  height: 32px;
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 15px;
  width: 100%;
}

.product-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  border: 1px solid #e8e8e8;
  position: relative;
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
  border-color: #ff4400;
}

.product-image-container {
  position: relative;
  width: 100%;
  padding-top: 100%;
  overflow: hidden;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

.product-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image {
  transform: scale(1.05);
}

.product-info {
  padding: 12px;
  background-color: #fff;
}

.product-name {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #333;
  font-weight: 500;
  line-height: 1.5;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.2s ease;
}

.product-card:hover .product-name {
  color: #ff4400;
}

.product-sales {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}

.product-price {
  margin-top: 8px;
}

.price {
  font-size: 22px;
  color: #ff4400;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 15px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.page-info {
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
  
  .search-filter-bar {
    padding: 10px;
    gap: 10px;
  }
  
  .search-box {
    width: 100%;
  }
  
  .search-input {
    width: 100%;
  }
  
  .filter-select {
    flex: 1;
  }
  
  .filter-select-input {
    width: 100%;
  }
  
  .filter-tags {
    width: 100%;
  }
  
  .filter-info {
    width: 100%;
  }
  
  .product-section {
    padding: 15px;
  }
  
  .product-grid {
    gap: 10px;
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
  
  .product-card {
    margin-bottom: 10px;
  }
  
  .category-info {
    padding: 10px 12px;
    flex-wrap: wrap;
  }
}

@media screen and (min-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(5, 1fr);
    max-width: 1200px;
    margin: 0 auto;
  }
}

@media screen and (min-width: 768px) and (max-width: 1199px) {
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>
