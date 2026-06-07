<template>
  <div class="admin-shops">
    <div class="page-header">
      <div class="header-left">
        <h1>店铺管理</h1>
        <p class="header-desc">查看和管理所有店铺信息，支持搜索、筛选和编辑</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-input-wrapper">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索店铺名称..."
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
        <el-option label="营业中" :value="1" />
        <el-option label="已关闭" :value="0" />
      </el-select>
    </div>

    <!-- 店铺表格 -->
    <div class="table-container">
      <el-table
        :data="shops"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#333', fontWeight: 600 }"
      >
        <el-table-column type="index" label="序号" width="65" align="center" />
        <el-table-column prop="id" label="店铺ID" width="85" align="center" />
        <el-table-column label="店铺Logo" width="80" align="center">
          <template #default="scope">
            <div v-if="scope.row.logo" class="shop-logo-wrapper">
              <img :src="getImageUrl(scope.row.logo)" alt="" class="shop-logo" />
            </div>
            <el-avatar v-else :size="40" shape="square">
              {{ scope.row.name?.[0] || '店' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="店铺名称" min-width="160">
          <template #default="scope">
            <span class="shop-name-text">{{ scope.row.name || '未命名' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sellerId" label="商家ID" width="85" align="center" />
        <el-table-column prop="phone" label="联系电话" width="130" align="center">
          <template #default="scope">
            <span>{{ scope.row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="scope">
            <el-switch
              :model-value="scope.row.status === 1"
              :loading="scope.row._statusLoading"
              active-text="营业"
              inactive-text="关闭"
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
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="showDetail(scope.row)">查看</el-button>
            <el-button size="small" type="warning" plain @click="openEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              :loading="scope.row._statusLoading"
              @click="toggleStatus(scope.row, scope.row.status !== 1)"
            >
              {{ scope.row.status === 1 ? '关闭' : '开启' }}
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
        @size-change="loadShops"
        @current-change="loadShops"
      />
    </div>

    <!-- 店铺详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="'店铺详情 - ' + (currentShop?.name || '')"
      width="650px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="currentShop" class="detail-content">
        <div class="detail-grid">
          <div class="detail-item">
            <span class="detail-label">店铺ID</span>
            <span class="detail-value">{{ currentShop.id }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">店铺名称</span>
            <span class="detail-value">{{ currentShop.name || '未命名' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">商家ID</span>
            <span class="detail-value">{{ currentShop.sellerId }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">联系电话</span>
            <span class="detail-value">{{ currentShop.phone || '-' }}</span>
          </div>
          <div class="detail-item">
            <span class="detail-label">店铺状态</span>
            <span class="detail-value">
              <el-tag :type="currentShop.status === 1 ? 'success' : 'warning'" size="small">
                {{ currentShop.status === 1 ? '营业中' : '已关闭' }}
              </el-tag>
            </span>
          </div>
          <div class="detail-item">
            <span class="detail-label">排序</span>
            <span class="detail-value">{{ currentShop.sort ?? 0 }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">店铺Logo</span>
            <div v-if="currentShop.logo" class="detail-image-wrap">
              <img :src="getImageUrl(currentShop.logo)" class="detail-logo" />
            </div>
            <span v-else class="detail-value">暂无</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">店铺地址</span>
            <span class="detail-value">{{ currentShop.address || '-' }}</span>
          </div>
          <div class="detail-item full-width">
            <span class="detail-label">店铺描述</span>
            <span class="detail-value">{{ currentShop.description || '暂无描述' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-empty description="暂无数据" />
      </div>
    </el-dialog>

    <!-- 编辑店铺对话框 -->
    <el-dialog
      v-model="editVisible"
      title="编辑店铺信息"
      width="550px"
      destroy-on-close
      class="edit-dialog"
    >
      <el-form :model="editForm" label-width="100px" class="shop-form">
        <el-form-item label="店铺名称">
          <el-input v-model="editForm.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="editForm.address" placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入店铺描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getStorePage, getStoreDetail, updateStore, updateStoreStatus, type Store } from '@/api/shop'
import { getStoreLogoUrl } from '@/utils/resource'

const searchKeyword = ref('')
const statusFilter = ref<number | ''>('')
const loading = ref(false)
const shops = ref<any[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const detailVisible = ref(false)
const currentShop = ref<Store | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const editingShop = ref<any>(null)
const editForm = reactive({
  name: '',
  phone: '',
  address: '',
  description: ''
})

const loadShops = async () => {
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
    const result = await getStorePage(params)
    shops.value = (result.list || []).map((item: Store) => ({
      ...item,
      _statusLoading: false
    }))
    pagination.total = result.total || 0
  } catch {
    ElMessage.error('获取店铺列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadShops()
}

const toggleStatus = async (row: any, newStatus: boolean) => {
  const targetStatus = newStatus ? 1 : 0
  if (row.status === targetStatus) return

  row._statusLoading = true
  try {
    await updateStoreStatus(row.id, targetStatus)
    row.status = targetStatus
    ElMessage.success(targetStatus === 1 ? '店铺已开启营业' : '店铺已关闭')
  } catch {
    ElMessage.error('操作失败')
  } finally {
    row._statusLoading = false
  }
}

const showDetail = async (row: Store) => {
  currentShop.value = null
  detailVisible.value = true
  try {
    const detail = await getStoreDetail(row.id)
    currentShop.value = detail
  } catch {
    ElMessage.error('获取店铺详情失败')
  }
}

const openEdit = (row: any) => {
  editingShop.value = row
  editForm.name = row.name || ''
  editForm.phone = row.phone || ''
  editForm.address = row.address || ''
  editForm.description = row.description || ''
  editVisible.value = true
}

const handleSave = async () => {
  if (!editingShop.value?.id) {
    ElMessage.warning('未定位到店铺')
    return
  }
  saving.value = true
  try {
    await updateStore({
      id: editingShop.value.id,
      name: editForm.name,
      phone: editForm.phone,
      address: editForm.address,
      description: editForm.description
    })
    ElMessage.success('店铺信息已更新')
    editVisible.value = false
    // 刷新列表
    loadShops()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
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
  return getStoreLogoUrl(String(path))
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.admin-shops {
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

.shop-logo-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.shop-logo {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
}

.shop-name-text {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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

.detail-image-wrap {
  margin-top: 4px;
}

.detail-logo {
  max-width: 120px;
  max-height: 120px;
  object-fit: contain;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.empty-detail {
  padding: 40px 0;
}

/* 编辑对话框 */
.edit-dialog :deep(.el-dialog__header) {
  padding: 20px 24px;
  margin: 0;
  border-bottom: 1px solid #ebeef5;
}

.shop-form {
  max-width: 100%;
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