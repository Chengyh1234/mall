<template>
  <div class="admin-shops">
    <!-- ====== 导航 Tabs ====== -->
    <div class="tabs-nav">
      <button
        :class="['tab-btn', { active: activeTab === 'shops' }]"
        @click="activeTab = 'shops'"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
          <polyline points="9 22 9 12 15 12 15 22"/>
        </svg>
        店铺管理
      </button>
      <button
        :class="['tab-btn', { active: activeTab === 'audit' }]"
        @click="activeTab = 'audit'"
      >
        <svg class="tab-icon" viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 11l3 3L22 4"/>
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
        </svg>
        开店审核
      </button>
    </div>

    <!-- ====== Tab 1: 店铺管理 ====== -->
    <div v-show="activeTab === 'shops'" class="tab-panel">
      <div class="panel-header">
        <div class="panel-header-left">
          <h2 class="panel-title">店铺管理</h2>
          <p class="panel-desc">查看和管理所有店铺信息，支持搜索、筛选和编辑</p>
        </div>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-input-group">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索店铺名称..."
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch" class="search-btn">搜索</el-button>
        </div>
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="handleSearch" class="filter-select">
          <el-option label="全部" value="" />
          <el-option label="营业中" :value="1" />
          <el-option label="已关闭" :value="0" />
        </el-select>
        <el-input
          v-model="searchSellerId"
          placeholder="商家ID"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          class="filter-input-sm"
        />
        <el-input
          v-model="searchPhone"
          placeholder="联系电话"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          class="filter-input-sm"
        />
      </div>

      <!-- 店铺表格 -->
      <div class="table-card">
        <el-table
          :data="shops"
          v-loading="loading"
          border
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f8f9fc', color: '#1e293b', fontWeight: 600 }"
          empty-text="暂无店铺数据"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="Logo" width="72" align="center">
            <template #default="scope">
              <div v-if="scope.row.logo" class="logo-cell">
                <img :src="getImageUrl(scope.row.logo)" alt="" class="logo-img" />
              </div>
              <div v-else class="logo-placeholder">{{ scope.row.name?.[0] || '店' }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="店铺名称" min-width="150">
            <template #default="scope">
              <span class="cell-name">{{ scope.row.name || '未命名' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sellerId" label="商家ID" width="80" align="center" />
          <el-table-column prop="phone" label="联系电话" width="130" align="center">
            <template #default="scope">
              <span class="cell-muted">{{ scope.row.phone || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <div class="status-toggle-cell">
                <el-switch
                  :model-value="scope.row.status === 1"
                  :loading="scope.row._statusLoading"
                  active-text="营业"
                  inactive-text="关闭"
                  inline-prompt
                  @change="(val: boolean) => toggleStatus(scope.row, val)"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="165" align="center">
            <template #default="scope">
              <span class="cell-muted">{{ formatTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" align="center" fixed="right">
            <template #default="scope">
              <div class="action-cell">
                <el-button size="small" text @click="showDetail(scope.row)">查看</el-button>
                <el-button size="small" text type="primary" @click="openEdit(scope.row)">编辑</el-button>
                <el-button
                  size="small"
                  :type="scope.row.status === 1 ? 'danger' : 'success'"
                  :loading="scope.row._statusLoading"
                  @click="toggleStatus(scope.row, scope.row.status !== 1)"
                >
                  {{ scope.row.status === 1 ? '关闭' : '开启' }}
                </el-button>
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
          @size-change="loadShops"
          @current-change="loadShops"
        />
      </div>
    </div>

    <!-- ====== Tab 2: 开店审核 ====== -->
    <div v-show="activeTab === 'audit'" class="tab-panel">
      <div class="panel-header">
        <div class="panel-header-left">
          <h2 class="panel-title">开店审核</h2>
          <p class="panel-desc">审核用户提交的开店申请，确认信息无误后可审核通过</p>
        </div>
        <el-button
          type="primary"
          plain
          :loading="auditLoading"
          @click="loadPendingApps"
          class="refresh-btn"
        >
          <svg class="btn-icon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          刷新
        </el-button>
      </div>

      <!-- 待审核表格 -->
      <div class="table-card">
        <el-table
          :data="pendingApps"
          v-loading="auditLoading"
          border
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f8f9fc', color: '#1e293b', fontWeight: 600 }"
          empty-text="暂无待审核申请"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="id" label="申请ID" width="80" align="center" />
          <el-table-column prop="name" label="店铺名称" min-width="140">
            <template #default="scope">
              <span class="cell-name">{{ scope.row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sellerId" label="申请人ID" width="90" align="center" />
          <el-table-column prop="phone" label="联系电话" width="130" align="center">
            <template #default="scope">
              <span class="cell-muted">{{ scope.row.phone || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="address" label="店铺地址" min-width="150">
            <template #default="scope">
              <span class="cell-muted">{{ scope.row.address || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="申请时间" width="165" align="center">
            <template #default="scope">
              <span class="cell-muted">{{ formatTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="210" align="center" fixed="right">
            <template #default="scope">
              <div class="action-cell">
                <el-button
                  size="small"
                  type="success"
                  plain
                  :loading="scope.row._approving"
                  @click="handleApprove(scope.row)"
                  class="action-approve"
                >
                  <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="margin-right:2px">
                    <polyline points="20 6 9 17 4 12"/>
                  </svg>
                  通过
                </el-button>
                <el-button
                  size="small"
                  type="danger"
                  plain
                  :loading="scope.row._rejecting"
                  @click="openRejectDialog(scope.row)"
                  class="action-reject"
                >
                  <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="margin-right:2px">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                  驳回
                </el-button>
                <el-button size="small" text @click="showApplyDetail(scope.row)">
                  <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="margin-right:2px">
                    <circle cx="12" cy="12" r="10"/>
                    <path d="M12 16v-4"/>
                    <path d="M12 8h.01"/>
                  </svg>
                  详情
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页 -->
      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="auditPagination.page"
          v-model:page-size="auditPagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="auditPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="loadPendingApps"
          @current-change="loadPendingApps"
        />
      </div>
    </div>

    <!-- ====== 店铺详情对话框 ====== -->
    <el-dialog
      v-model="detailVisible"
      :title="'店铺详情 — ' + (currentShop?.name || '')"
      width="620px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="currentShop" class="detail-body">
        <div class="detail-grid">
          <div class="detail-field">
            <span class="field-label">店铺 ID</span>
            <span class="field-value">#{{ currentShop.id }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">店铺名称</span>
            <span class="field-value">{{ currentShop.name || '未命名' }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">商家 ID</span>
            <span class="field-value">#{{ currentShop.sellerId }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">联系电话</span>
            <span class="field-value">{{ currentShop.phone || '—' }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">店铺状态</span>
            <span class="field-value">
              <span :class="['status-dot', currentShop.status === 1 ? 'active' : 'inactive']" />
              {{ currentShop.status === 1 ? '营业中' : '已关闭' }}
            </span>
          </div>
          <div class="detail-field">
            <span class="field-label">排序权重</span>
            <span class="field-value">{{ currentShop.sort ?? 0 }}</span>
          </div>
          <div v-if="currentShop.logo" class="detail-field span-2">
            <span class="field-label">店铺 Logo</span>
            <div class="detail-logo-wrap">
              <img :src="getImageUrl(currentShop.logo)" class="detail-logo" />
            </div>
          </div>
          <div class="detail-field span-2">
            <span class="field-label">店铺地址</span>
            <span class="field-value">{{ currentShop.address || '—' }}</span>
          </div>
          <div class="detail-field span-2">
            <span class="field-label">店铺描述</span>
            <p class="field-desc">{{ currentShop.description || '暂无描述' }}</p>
          </div>
        </div>
      </div>
      <div v-else class="detail-empty">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#d1d5db" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 16v-4"/>
          <path d="M12 8h.01"/>
        </svg>
        <p>暂无数据</p>
      </div>
    </el-dialog>

    <!-- ====== 编辑店铺对话框 ====== -->
    <el-dialog
      v-model="editVisible"
      title="编辑店铺信息"
      width="520px"
      destroy-on-close
      class="edit-dialog"
    >
      <el-form :model="editForm" label-width="90px" class="edit-form">
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
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入店铺描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- ====== 申请详情对话框 ====== -->
    <el-dialog
      v-model="applyDetailVisible"
      :title="'申请详情 — ' + (applyDetail?.name || '')"
      width="560px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-if="applyDetail" class="detail-body">
        <div class="detail-grid">
          <div class="detail-field">
            <span class="field-label">申请 ID</span>
            <span class="field-value">#{{ applyDetail.id }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">店铺名称</span>
            <span class="field-value">{{ applyDetail.name }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">申请人 ID</span>
            <span class="field-value">#{{ applyDetail.sellerId }}</span>
          </div>
          <div class="detail-field">
            <span class="field-label">联系电话</span>
            <span class="field-value">{{ applyDetail.phone || '—' }}</span>
          </div>
          <div class="detail-field span-2">
            <span class="field-label">店铺描述</span>
            <p class="field-desc">{{ applyDetail.description || '暂无描述' }}</p>
          </div>
          <div class="detail-field span-2">
            <span class="field-label">店铺地址</span>
            <span class="field-value">{{ applyDetail.address || '—' }}</span>
          </div>
          <div class="detail-field span-2">
            <span class="field-label">申请时间</span>
            <span class="field-value">{{ formatTime(applyDetail.createdAt) }}</span>
          </div>
        </div>
      </div>
      <div v-else class="detail-empty">
        <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#d1d5db" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"/>
          <path d="M12 16v-4"/>
          <path d="M12 8h.01"/>
        </svg>
        <p>暂无数据</p>
      </div>
      <template #footer>
        <el-button @click="applyDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ====== 驳回原因对话框 ====== -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回开店申请"
      width="480px"
      destroy-on-close
      class="reject-dialog"
    >
      <div class="reject-body">
        <div class="reject-warning">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="#ef4444" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>确定要驳回 <strong>{{ rejectingShop?.name }}</strong> 的开店申请吗？</span>
        </div>
        <p class="reject-hint">请填写驳回原因，以便申请人了解问题并修改后重新提交：</p>
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          placeholder="请输入驳回原因，如：店铺名称不合规、信息不完整等"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { getAdminStorePage, getAdminStoreDetail, updateStore, updateStoreStatus, type Store } from '@/api/shop'
import { getPendingApplyPage, approveStoreApply, rejectStoreApply } from '@/api/shop'
import { getStoreLogoUrl } from '@/utils/resource'

const activeTab = ref('shops')

// ====== 店铺管理 ======
const searchKeyword = ref('')
const statusFilter = ref<number | ''>('')
const searchSellerId = ref('')
const searchPhone = ref('')
const loading = ref(false)
const shops = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const detailVisible = ref(false)
const currentShop = ref<Store | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const editingShop = ref<any>(null)
const editForm = reactive({ name: '', phone: '', address: '', description: '' })

const loadShops = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (statusFilter.value !== '') params.status = statusFilter.value
    if (searchSellerId.value) params.sellerId = Number(searchSellerId.value)
    if (searchPhone.value) params.phone = searchPhone.value
    const result = await getAdminStorePage(params)
    shops.value = (result.list || []).map((item: Store) => ({ ...item, _statusLoading: false }))
    pagination.total = result.total || 0
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; loadShops() }

const toggleStatus = async (row: any, newStatus: boolean) => {
  const targetStatus = newStatus ? 1 : 0
  if (row.status === targetStatus) return
  row._statusLoading = true
  try {
    await updateStoreStatus(row.id, targetStatus)
    row.status = targetStatus
    ElMessage.success(targetStatus === 1 ? '店铺已开启营业' : '店铺已关闭')
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._statusLoading = false
  }
}

const showDetail = async (row: Store) => {
  currentShop.value = null
  detailVisible.value = true
  try {
    const detail = await getAdminStoreDetail(row.id)
    currentShop.value = detail
  } catch {
    // 拦截器已处理后端错误提示
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
  if (!editingShop.value?.id) { ElMessage.warning('未定位到店铺'); return }
  saving.value = true
  try {
    await updateStore({ id: editingShop.value.id, name: editForm.name, phone: editForm.phone, address: editForm.address, description: editForm.description })
    ElMessage.success('店铺信息已更新')
    editVisible.value = false
    loadShops()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    saving.value = false
  }
}

// ====== 开店审核 ======
const auditLoading = ref(false)
const pendingApps = ref<any[]>([])
const auditPagination = reactive({ page: 1, pageSize: 10, total: 0 })

const applyDetailVisible = ref(false)
const applyDetail = ref<any>(null)

const rejectDialogVisible = ref(false)
const rejectingShop = ref<any>(null)
const rejectReason = ref('')
const rejecting = ref(false)

const loadPendingApps = async () => {
  auditLoading.value = true
  try {
    const result = await getPendingApplyPage({ page: auditPagination.page, pageSize: auditPagination.pageSize })
    pendingApps.value = (result.list || []).map((item: any) => ({ ...item, _approving: false, _rejecting: false }))
    auditPagination.total = result.total || 0
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    auditLoading.value = false
  }
}

const handleApprove = async (row: any) => {
  row._approving = true
  try {
    await approveStoreApply(row.id)
    ElMessage.success('审核通过，店铺已开通')
    loadPendingApps()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._approving = false
  }
}

const openRejectDialog = (row: any) => {
  rejectingShop.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入驳回原因'); return }
  rejecting.value = true
  try {
    await rejectStoreApply(rejectingShop.value.id, rejectReason.value.trim())
    ElMessage.success('已驳回该开店申请')
    rejectDialogVisible.value = false
    loadPendingApps()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    rejecting.value = false
  }
}

const showApplyDetail = async (row: any) => {
  applyDetail.value = null
  applyDetailVisible.value = true
  try {
    const detail = await getAdminStoreDetail(row.id)
    applyDetail.value = detail
  } catch {
    // 拦截器已处理后端错误提示
  }
}

// ====== 工具函数 ======
const formatTime = (time: string | undefined) => {
  if (!time) return '—'
  try {
    const d = new Date(time)
    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch { return time }
}

const getImageUrl = (path?: string | null) => {
  if (!path) return ''
  return getStoreLogoUrl(String(path))
}

onMounted(() => { loadShops() })
</script>

<style scoped>
/* ===========================
   Design Tokens
   =========================== */
.admin-shops {
  --bg-page: #f0f2f5;
  --bg-card: #ffffff;
  --bg-surface: #f8f9fc;
  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-muted: #94a3b8;
  --border-light: #e2e8f0;
  --border-hover: #cbd5e1;
  --brand: #409eff;
  --brand-light: #ecf5ff;
  --brand-hover: #66b1ff;
  --green: #22c55e;
  --green-bg: #f0fdf4;
  --red: #ef4444;
  --red-bg: #fef2f2;
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 14px;
  --shadow-card: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.06);
  --shadow-elevated: 0 4px 16px rgba(0, 0, 0, 0.08);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* ===========================
   Tabs Navigation
   =========================== */
.tabs-nav {
  display: flex;
  gap: 3px;
  margin-bottom: 28px;
  padding: 3px;
  background: #e8eaef;
  border-radius: var(--radius-md);
  width: fit-content;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 22px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.tab-btn:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.6);
}

.tab-btn.active {
  background: #fff;
  color: var(--brand);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.tab-icon {
  flex-shrink: 0;
}

/* ===========================
   Panel Header
   =========================== */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 22px;
}

.panel-header-left {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.panel-title {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: -0.02em;
  line-height: 1.3;
}

.panel-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* ===========================
   Search / Filter Bar
   =========================== */
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 22px;
  align-items: center;
  flex-wrap: wrap;
  background: var(--bg-surface);
  padding: 14px 18px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.search-input-group {
  display: flex;
  gap: 0;
  align-items: center;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px 0 0 8px;
  box-shadow: 0 0 0 1px var(--border-light) inset;
}

.search-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--border-hover) inset;
}

.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand) inset;
}

.search-input {
  width: 260px;
}

.search-btn {
  border-radius: 0 8px 8px 0;
  margin-left: -1px;
}

.filter-select {
  width: 120px;
}

.filter-input-sm {
  width: 120px;
}

.filter-input-sm :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px var(--border-light) inset;
}

.filter-input-sm :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--border-hover) inset;
}

.filter-input-sm :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--brand) inset;
}

/* ===========================
   Table Card
   =========================== */
.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  overflow: hidden;
  border: 1px solid var(--border-light);
}

.table-card :deep(.el-table) {
  border: none !important;
}

.table-card :deep(.el-table th.el-table__cell) {
  border-bottom: 1px solid var(--border-light);
  padding: 12px 0;
}

.table-card :deep(.el-table__body tr:hover > td) {
  background-color: #f8fafc;
}

.table-card :deep(.el-table__row > td) {
  padding: 10px 0;
}

/* ===========================
   Table Cell Elements
   =========================== */
.logo-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-img {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  border: 1px solid var(--border-light);
}

.logo-placeholder {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cell-name {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.cell-muted {
  color: var(--text-secondary);
  font-size: 13px;
}

.status-toggle-cell {
  display: flex;
  justify-content: center;
}

.action-cell {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: nowrap;
}

/* ===========================
   Pagination
   =========================== */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding: 10px 4px 4px;
}

/* ===========================
   Refresh Button
   =========================== */
.refresh-btn {
  border-radius: 8px;
}

.btn-icon {
  flex-shrink: 0;
}

/* ===========================
   Detail Dialog
   =========================== */
.detail-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  margin: 0;
  border-bottom: 1px solid var(--border-light);
}

.detail-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.detail-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.detail-body {
  min-height: 100px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}

.detail-field {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-field.span-2 {
  grid-column: 1 / -1;
}

.field-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
  letter-spacing: 0.3px;
}

.field-value {
  font-size: 14px;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.field-desc {
  margin: 0;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.active {
  background: var(--green);
}

.status-dot.inactive {
  background: var(--text-muted);
}

.detail-logo-wrap {
  margin-top: 4px;
}

.detail-logo {
  max-width: 100px;
  max-height: 100px;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid var(--border-light);
}

.detail-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
  color: var(--text-muted);
  font-size: 14px;
}

/* ===========================
   Edit Dialog
   =========================== */
.edit-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  margin: 0;
  border-bottom: 1px solid var(--border-light);
}

.edit-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.edit-form {
  max-width: 100%;
}

.edit-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--text-secondary);
}

/* ===========================
   Reject Dialog
   =========================== */
.reject-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  margin: 0;
  border-bottom: 1px solid var(--border-light);
}

.reject-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.reject-dialog :deep(.el-dialog__body) {
  padding: 24px;
}

.reject-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reject-warning {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 16px;
  background: var(--red-bg);
  border: 1px solid #fecaca;
  border-radius: 8px;
  font-size: 14px;
  color: #991b1b;
  line-height: 1.5;
}

.reject-warning svg {
  flex-shrink: 0;
  margin-top: 1px;
}

.reject-warning strong {
  color: var(--red);
}

.reject-hint {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* ===========================
   Responsive
   =========================== */
@media screen and (max-width: 1024px) {
  .search-bar {
    padding: 12px 14px;
  }
  .search-input {
    width: 200px;
  }
}

@media screen and (max-width: 900px) {
  .tabs-nav {
    width: 100%;
  }
  .tab-btn {
    flex: 1;
    justify-content: center;
  }
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .panel-header {
    flex-direction: column;
    gap: 12px;
  }
  .refresh-btn {
    align-self: flex-start;
  }
}

@media screen and (max-width: 640px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
    padding: 12px;
  }
  .search-input-group {
    width: 100%;
  }
  .search-input {
    width: 100%;
  }
  .filter-select {
    width: 100%;
  }
  .filter-input-sm {
    width: 100%;
  }
  .action-cell {
    flex-direction: column;
    gap: 4px;
  }
  .pagination-bar :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>