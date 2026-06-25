<template>
  <div class="admin-users">
    <div class="page-header">
      <h1>用户管理</h1>
    </div>

    <!-- 搜索栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名 / 昵称 / 邮箱"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部角色" clearable style="width: 150px">
            <el-option label="全部" value="" />
            <el-option label="普通用户" value="USER" />
            <el-option label="卖家" value="SELLER" />
            <el-option label="运营管理员" value="OPERATOR" />
            <el-option label="超级用户" value="ADMIN" />
            <el-option label="客服" value="CUSTOMER_SERVICE" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="全部" :value="undefined" />
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        stripe
        style="width: 100%"
        empty-text="暂无用户数据"
      >
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column prop="realName" label="昵称" width="130" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="角色" width="160">
          <template #default="scope">
            <el-tag
              v-for="role in scope.row.roles"
              :key="role"
              :type="getRoleTagType(role)"
              size="small"
              style="margin-right: 4px; margin-bottom: 2px"
            >
              {{ getRoleText(role) }}
            </el-tag>
            <span v-if="!scope.row.roles || scope.row.roles.length === 0">-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ scope.row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column prop="lastLoginTime" label="最近登录" width="180" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
              :type="scope.row.status === 1 ? 'danger' : 'success'"
              size="small"
              :loading="scope.row._statusLoading"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getManageUserPage,
  enableUser,
  disableUser,
  type ManageUserItem
} from '@/api/user'

// ---------- 搜索表单 ----------
const searchForm = reactive({
  keyword: '',
  role: '',
  status: undefined as number | undefined
})

// ---------- 表格数据 ----------
const loading = ref(false)
const userList = ref<ManageUserItem[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

// ---------- 角色映射 ----------
const roleTextMap: Record<string, string> = {
  USER: '普通用户',
  SELLER: '卖家',
  OPERATOR: '运营管理员',
  ADMIN: '超级用户',
  CUSTOMER_SERVICE: '客服'
}

const roleTagTypeMap: Record<string, string> = {
  USER: 'info',
  SELLER: 'warning',
  OPERATOR: 'primary',
  ADMIN: 'danger',
  CUSTOMER_SERVICE: 'success'
}

function getRoleText(role: string): string {
  return roleTextMap[role] || role
}

function getRoleTagType(role: string): string {
  return roleTagTypeMap[role] || 'info'
}

// ---------- 数据加载 ----------
async function fetchUserList() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.role) params.role = searchForm.role
    if (searchForm.status !== undefined) params.status = searchForm.status

    const res = await getManageUserPage(params as any)
    userList.value = (res.records || []).map(item => ({
      ...item,
      _statusLoading: false
    }))
    pagination.total = res.total
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    loading.value = false
  }
}

// ---------- 事件处理 ----------
function handleSearch() {
  pagination.page = 1
  fetchUserList()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = undefined
  pagination.page = 1
  fetchUserList()
}

function handlePageChange() {
  fetchUserList()
}

async function handleToggleStatus(row: ManageUserItem & { _statusLoading?: boolean }) {
  const newStatus = row.status === 1 ? 0 : 1
  const actionText = newStatus === 1 ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(
      `确定要${actionText}用户「${row.username}」吗？`,
      '操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return // 取消操作
  }

  // 给当前行加 loading（在额外字段上标记，不影响 el-table 原有数据）
  row._statusLoading = true
  try {
    if (newStatus === 1) {
      await enableUser(row.id)
    } else {
      await disableUser(row.id)
    }
    ElMessage.success(`用户已${actionText}`)
    // 更新本地状态
    row.status = newStatus
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._statusLoading = false
  }
}

// ---------- 初始化 ----------
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.admin-users {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h1 {
  font-size: 22px;
  margin: 0;
  color: #303133;
}

.search-card {
  margin-bottom: 16px;
}

.search-card :deep(.el-card__body) {
  padding: 16px 20px 0 20px;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
}
</style>