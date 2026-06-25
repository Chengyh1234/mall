<template>
  <div class="admin-products">
    <div class="page-header">
      <div class="header-left">
        <h1>商品管理</h1>
        <p class="header-desc">管理所有商家的商品信息，支持上下架操作</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrapper">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品名称..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch" class="filter-select">
        <el-option label="全部" value="" />
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
    </div>

    <!-- 商品表格 -->
    <div class="table-container">
      <el-table
        :data="products"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#333', fontWeight: 600 }"
      >
        <el-table-column type="index" label="序号" width="65" align="center" />
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column label="商品主图" width="80" align="center">
          <template #default="scope">
            <div v-if="scope.row.mainImage" class="product-img-wrapper">
              <img :src="getImageUrl(scope.row.mainImage)" alt="" class="product-thumb" />
            </div>
            <span v-else class="no-img">-</span>
          </template>
        </el-table-column>
        <el-table-column label="商品名称" min-width="180">
          <template #default="scope">
            <div class="product-name-cell">
              <span class="name-text">{{ scope.row.name }}</span>
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
            <el-switch
              :model-value="scope.row.status === 1"
              :loading="scope.row._statusLoading"
              active-text="上架"
              inactive-text="下架"
              inline-prompt
              @change="(val: boolean) => toggleStatus(scope.row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170" align="center">
          <template #default="scope">
            <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="showDetail(scope.row)">查看</el-button>
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              :loading="scope.row._statusLoading"
              @click="toggleStatus(scope.row, scope.row.status !== 1)"
            >
              {{ scope.row.status === 1 ? '下架' : '上架' }}
            </el-button>
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
        @size-change="loadProducts"
        @current-change="loadProducts"
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
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">商品ID</span>
            <span class="detail-value">{{ currentProduct.id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">商品名称</span>
            <span class="detail-value">{{ currentProduct.name }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">所属分类</span>
            <span class="detail-value">{{ currentProduct.categoryName || '未分类' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">商家ID</span>
            <span class="detail-value">{{ currentProduct.sellerId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">最低价格</span>
            <span class="detail-value price">¥{{ currentProduct.minPrice?.toFixed(2) || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">商品状态</span>
            <span class="detail-value">
              <el-tag :type="currentProduct.status === 1 ? 'success' : 'warning'" size="small">
                {{ currentProduct.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">品牌</span>
            <span class="detail-value">{{ currentProduct.brandName || '无' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">销量</span>
            <span class="detail-value">{{ currentProduct.sales || 0 }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">商品主图</span>
            <div v-if="currentProduct.mainImage" class="detail-image-wrap">
              <img :src="getImageUrl(currentProduct.mainImage)" class="detail-main-img" />
            </div>
            <span v-else class="detail-value">暂无主图</span>
          </div>
          <div class="detail-item full-width" v-if="imageList.length > 0">
            <span class="detail-label">商品图片集</span>
            <div class="detail-images-gallery">
              <img
                v-for="(img, idx) in imageList"
                :key="idx"
                :src="getImageUrl(img)"
                class="detail-gallery-img"
              />
            </div>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">商品描述</span>
            <span class="detail-value">{{ currentProduct.description || '暂无描述' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-empty description="暂无数据" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
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
    // 后端直接返回 SpuAdminDetailVO 扁平对象
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

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.admin-products {
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

.search-input-wrapper {
  width: 320px;
}

.filter-select {
  width: 140px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.product-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-img-wrapper {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-thumb {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.no-img {
  color: #c0c4cc;
  font-size: 12px;
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

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
}

.detail-value.price {
  color: #ff4400;
  font-weight: 600;
  font-size: 16px;
}

.empty-detail {
  padding: 40px 0;
}

/* 详情图片 */
.detail-image-wrap {
  margin-top: 4px;
}

.detail-main-img {
  max-width: 220px;
  max-height: 220px;
  object-fit: contain;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.detail-images-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 4px;
}

.detail-gallery-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #ebeef5;
  cursor: pointer;
  transition: transform 0.2s;
}

.detail-gallery-img:hover {
  transform: scale(1.05);
}

@media screen and (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .search-input-wrapper {
    width: 100%;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-select {
    width: 100%;
  }
}
</style>