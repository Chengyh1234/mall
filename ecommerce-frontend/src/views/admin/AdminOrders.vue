<template>
  <div class="admin-orders">
    <div class="page-header">
      <div class="header-left">
        <h1>商品列表</h1>
        <p class="header-desc">全平台商品一览，查看所有商家的商品信息（含上架/下架）</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称"
        size="large"
        clearable
        class="search-input"
        @keyup.enter="handleSearch"
      />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable class="filter-select" @change="handleSearch">
        <el-option label="全部状态" value="" />
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" size="large" @click="handleSearch">查询</el-button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table
        :data="products"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#333', fontWeight: 600 }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="商品名称" min-width="200">
          <template #default="scope">
            <div class="product-cell">
              <el-image
                v-if="scope.row.mainImage"
                :src="getSpuImageUrl(scope.row.mainImage)"
                class="product-thumb"
                fit="cover"
                lazy
              >
                <template #error><div class="img-placeholder">📷</div></template>
              </el-image>
              <div class="product-info">
                <span class="product-name">{{ scope.row.name }}</span>
                <span class="product-keywords" v-if="scope.row.keywords">{{ scope.row.keywords }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="120" align="center" />
        <el-table-column prop="brandName" label="品牌" width="100" align="center" />
        <el-table-column label="价格" width="110" align="center">
          <template #default="scope">
            <span class="price-text">¥{{ scope.row.minPrice?.toFixed(2) || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sales" label="销量" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sellerId" label="商家ID" width="80" align="center" />
        <el-table-column prop="storeId" label="店铺ID" width="80" align="center" />
        <el-table-column label="创建时间" width="165" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="showDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="loadSpuList"
        @current-change="loadSpuList"
      />
    </div>

    <!-- 商品详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="'商品详情 - ' + (currentProduct?.name || '')"
      width="700px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="currentProduct" class="detail-content">
        <!-- 图片展示 -->
        <div class="detail-section image-section">
          <el-image
            v-if="currentProduct.mainImage"
            :src="getSpuImageUrl(currentProduct.mainImage)"
            class="detail-main-img"
            fit="contain"
          >
            <template #error><div class="img-placeholder-lg">📷</div></template>
          </el-image>
        </div>

        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label">商品名称</span>
              <span class="detail-value">{{ currentProduct.name }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">分类</span>
              <span class="detail-value">{{ currentProduct.categoryName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">品牌</span>
              <span class="detail-value">{{ currentProduct.brandName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">状态</span>
              <span class="detail-value">
                <el-tag :type="currentProduct.status === 1 ? 'success' : 'danger'" size="small">
                  {{ currentProduct.status === 1 ? '上架' : '下架' }}
                </el-tag>
              </span>
            </div>
            <div class="detail-item">
              <span class="detail-label">最低价格</span>
              <span class="detail-value price">¥{{ currentProduct.minPrice?.toFixed(2) || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">销量</span>
              <span class="detail-value">{{ currentProduct.sales ?? 0 }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">商家ID</span>
              <span class="detail-value">{{ currentProduct.sellerId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">店铺ID</span>
              <span class="detail-value">{{ currentProduct.storeId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">单位</span>
              <span class="detail-value">{{ currentProduct.unit || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">创建时间</span>
              <span class="detail-value">{{ formatTime(currentProduct.createdAt) }}</span>
            </div>
            <div class="detail-item full-width" v-if="currentProduct.keywords">
              <span class="detail-label">关键词</span>
              <span class="detail-value">{{ currentProduct.keywords }}</span>
            </div>
            <div class="detail-item full-width" v-if="currentProduct.description">
              <span class="detail-label">描述</span>
              <span class="detail-value">{{ currentProduct.description }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-skeleton :rows="6" animated />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSpuPageAll } from '@/api/spu'
import { getSpuImageUrl } from '@/utils/resource'

const keyword = ref('')
const statusFilter = ref<number | ''>('')
const loading = ref(false)
const products = ref<any[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentProduct = ref<any>(null)

const loadSpuList = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (keyword.value.trim()) {
      params.keyword = keyword.value.trim()
    }
    if (statusFilter.value !== '') {
      params.status = statusFilter.value
    }
    const result: any = await getSpuPageAll(params)
    products.value = result.list || []
    pagination.total = result.total || 0
  } catch {
    products.value = []
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadSpuList()
}

const showDetail = (row: any) => {
  currentProduct.value = row
  detailVisible.value = true
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

onMounted(() => {
  loadSpuList()
})
</script>

<style scoped>
.admin-orders {
  padding: 0;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-left h1 {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px 0;
  letter-spacing: 0.3px;
}

.header-desc {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 260px;
}

.filter-select {
  width: 150px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 4px 0;
}

.product-thumb {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  flex-shrink: 0;
  object-fit: cover;
  background: #f5f5f5;
}

.img-placeholder {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 20px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-keywords {
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-text {
  color: #ff4400;
  font-weight: 600;
  font-size: 13px;
}

.time-text {
  font-size: 12px;
  color: #909399;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 16px 0;
}

/* 详情对话框 */
.detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #ebeef5;
}

.detail-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.image-section {
  text-align: center;
}

.detail-main-img {
  max-width: 280px;
  max-height: 200px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.img-placeholder-lg {
  width: 200px;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 48px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 14px 0;
  padding-left: 10px;
  border-left: 3px solid #409eff;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.detail-value.price {
  color: #ff4400;
  font-weight: 600;
}

.empty-detail {
  padding: 40px 0;
}

@media screen and (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }
}
</style>