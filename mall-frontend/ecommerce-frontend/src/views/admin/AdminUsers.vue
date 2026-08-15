<template>
  <div class="admin-users">
    <section class="hero-panel">
      <div class="hero-panel__content">
        <span class="hero-kicker">账号治理台</span>
        <h1>用户管理</h1>
        <p>统一查看平台账号身份、联系方式与启停状态，快速定位异常账户。</p>
      </div>
      <div class="hero-metrics" aria-label="用户概览">
        <div class="hero-metric">
          <span>平台用户</span>
          <strong>{{ formatNumber(pagination.total) }}</strong>
          <small>{{ activeFilterCount > 0 ? '当前筛选命中' : '全量用户池' }}</small>
        </div>
        <div class="hero-metric">
          <span>当前页正常</span>
          <strong>{{ pageActiveCount }}</strong>
          <small>{{ activeRatio }}% 可用</small>
        </div>
        <div class="hero-metric">
          <span>当前页禁用</span>
          <strong>{{ pageDisabledCount }}</strong>
          <small>需持续关注</small>
        </div>
      </div>
    </section>

    <section class="list-card">
      <div class="filter-body">
        <div class="status-filter-section">
          <span class="status-filter-label">账号状态</span>
          <div class="status-pills" role="group" aria-label="账号状态筛选">
            <button
              v-for="item in statusOptions"
              :key="item.label"
              type="button"
              class="status-pill"
              :class="[
                `status-pill--${item.tone}`,
                { active: searchForm.status === item.value }
              ]"
              @click="setStatus(item.value)"
            >
              <span v-if="item.tone !== 'all'" class="status-dot" :class="`status-dot--${item.tone}`" />
              {{ item.label }}
            </button>
          </div>
        </div>

        <div class="filter-divider" />

        <div class="filter-row">
          <div class="filter-field filter-field--keyword">
            <label for="user-keyword">关键词</label>
            <div class="filter-input-wrap">
              <el-icon><Search /></el-icon>
              <input
                id="user-keyword"
                v-model="searchForm.keyword"
                type="text"
                placeholder="用户名 / 昵称 / 邮箱"
                @keyup.enter="handleSearch"
              />
              <button
                v-if="searchForm.keyword"
                type="button"
                class="input-clear"
                aria-label="清空关键词"
                @click="clearKeyword"
              >
                <el-icon><Close /></el-icon>
              </button>
            </div>
          </div>

          <div class="filter-field">
            <label for="user-role">角色</label>
            <div class="select-wrap">
              <select id="user-role" v-model="searchForm.role" @change="handleSearch">
                <option v-for="role in roleOptions" :key="role.value" :value="role.value">
                  {{ role.label }}
                </option>
              </select>
            </div>
          </div>

          <div class="filter-actions">
            <button type="button" class="primary-btn primary-btn--gold" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </button>
            <button type="button" class="secondary-btn" @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </button>
          </div>
        </div>
      </div>
    </section>

    <section class="list-card">
      <div class="list-card__header">
        <div class="list-card__title">
          <h2>用户列表</h2>
          <span>{{ tableSummary }}</span>
        </div>
        <div class="table-actions">
          <span v-if="activeFilterCount > 0" class="filter-chip">
            已启用 {{ activeFilterCount }} 个筛选
          </span>
          <button
            type="button"
            class="icon-btn"
            :disabled="loading"
            title="刷新"
            aria-label="刷新用户列表"
            @click="fetchUserList"
          >
            <el-icon :class="{ 'btn-spinner': loading }"><Refresh /></el-icon>
          </button>
        </div>
      </div>

      <div class="table-container" v-loading="loading">
        <el-table
          :data="userList"
          :header-cell-style="headerCellStyle"
          :cell-style="cellStyle"
          row-key="id"
          row-class-name="user-row"
          style="width: 100%"
          empty-text="暂无用户数据"
        >
          <el-table-column type="index" label="序号" width="64" align="center" />
          <el-table-column label="用户" min-width="220">
            <template #default="scope">
              <div class="user-cell">
                <div class="user-avatar" :class="getAvatarTone(scope.row)">
                  <img
                    v-if="scope.row.avatar"
                    :src="getAvatarSrc(scope.row)"
                    alt=""
                    loading="lazy"
                  />
                  <span v-else>{{ getAvatarInitial(scope.row) }}</span>
                </div>
                <div class="user-identity">
                  <strong>{{ scope.row.username || '未命名用户' }}</strong>
                  <span>ID #{{ scope.row.id }} · {{ scope.row.realName || '未设置昵称' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="联系方式" min-width="240">
            <template #default="scope">
              <div class="contact-cell">
                <span>{{ scope.row.email || '未绑定邮箱' }}</span>
                <small>{{ scope.row.phone || '未绑定手机号' }}</small>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="角色" min-width="180">
            <template #default="scope">
              <div v-if="scope.row.roles?.length" class="role-tags">
                <span
                  v-for="role in scope.row.roles"
                  :key="role"
                  class="role-tag"
                  :class="`role-tag--${getRoleTone(role)}`"
                >
                  {{ getRoleText(role) }}
                </span>
              </div>
              <span v-else class="cell-empty">未分配</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="116" align="center">
            <template #default="scope">
              <span
                class="status-badge"
                :class="scope.row.status === 1 ? 'status-badge--active' : 'status-badge--inactive'"
              >
                <span
                  class="status-dot"
                  :class="scope.row.status === 1 ? 'status-dot--active' : 'status-dot--inactive'"
                />
                {{ scope.row.status === 1 ? '正常' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="注册时间" width="160" align="center">
            <template #default="scope">
              <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="最近登录" width="160" align="center">
            <template #default="scope">
              <span class="time-text">{{ formatTime(scope.row.lastLoginTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="118" align="center" fixed="right">
            <template #default="scope">
              <button
                type="button"
                class="action-btn"
                :class="scope.row.status === 1 ? 'action-btn--danger' : 'action-btn--success'"
                :disabled="scope.row._statusLoading"
                @click="handleToggleStatus(scope.row)"
              >
                <el-icon v-if="scope.row._statusLoading" class="btn-spinner"><Refresh /></el-icon>
                <el-icon v-else-if="scope.row.status === 1"><Lock /></el-icon>
                <el-icon v-else><Unlock /></el-icon>
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </button>
            </template>
          </el-table-column>

          <template #empty>
            <div class="empty-state">
              <div class="empty-state__icon">
                <el-icon><UserFilled /></el-icon>
              </div>
              <strong>暂无匹配用户</strong>
              <span>调整关键词、角色或状态后再试</span>
            </div>
          </template>
        </el-table>
      </div>

      <div class="pagination-bar">
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
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Close, Lock, Refresh, Search, Unlock, UserFilled } from '@element-plus/icons-vue'
import { getAvatarUrl } from '@/utils/resource'
import {
  getManageUserPage,
  enableUser,
  disableUser,
  type ManageUserItem,
  type ManageUserPageRequest
} from '@/api/user'

type ManageUserRow = ManageUserItem & {
  _statusLoading?: boolean
}

const searchForm = reactive({
  keyword: '',
  role: '',
  status: undefined as number | undefined
})

const loading = ref(false)
const userList = ref<ManageUserRow[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

const statusOptions: Array<{ label: string; value: number | undefined; tone: string }> = [
  { label: '全部', value: undefined, tone: 'all' },
  { label: '正常', value: 1, tone: 'active' },
  { label: '禁用', value: 0, tone: 'inactive' }
]

const roleOptions = [
  { label: '全部角色', value: '' },
  { label: '普通用户', value: 'USER' },
  { label: '卖家', value: 'SELLER' },
  { label: '运营管理员', value: 'OPERATOR' },
  { label: '超级用户', value: 'ADMIN' },
  { label: '客服', value: 'CUSTOMER_SERVICE' }
]

const roleTextMap: Record<string, string> = {
  USER: '普通用户',
  ROLE_USER: '普通用户',
  SELLER: '卖家',
  ROLE_SELLER: '卖家',
  OPERATOR: '运营管理员',
  ROLE_OPERATOR: '运营管理员',
  ADMIN: '超级用户',
  SUPER_ADMIN: '超级用户',
  ROLE_ADMIN: '超级用户',
  ROLE_SUPER_ADMIN: '超级用户',
  CUSTOMER_SERVICE: '客服',
  ROLE_CUSTOMER_SERVICE: '客服'
}

const roleToneMap: Record<string, string> = {
  USER: 'user',
  ROLE_USER: 'user',
  SELLER: 'seller',
  ROLE_SELLER: 'seller',
  OPERATOR: 'operator',
  ROLE_OPERATOR: 'operator',
  ADMIN: 'admin',
  SUPER_ADMIN: 'admin',
  ROLE_ADMIN: 'admin',
  ROLE_SUPER_ADMIN: 'admin',
  CUSTOMER_SERVICE: 'service',
  ROLE_CUSTOMER_SERVICE: 'service'
}

const avatarTones = ['amber', 'teal', 'indigo', 'rose', 'slate'] as const

const pageActiveCount = computed(() => userList.value.filter(item => item.status === 1).length)
const pageDisabledCount = computed(() => userList.value.filter(item => item.status !== 1).length)

const activeRatio = computed(() => {
  if (!userList.value.length) return 0
  return Math.round((pageActiveCount.value / userList.value.length) * 100)
})

const activeFilterCount = computed(() => {
  let count = 0
  if (searchForm.keyword.trim()) count += 1
  if (searchForm.role) count += 1
  if (searchForm.status !== undefined) count += 1
  return count
})

const tableSummary = computed(() => {
  if (loading.value) return '正在同步用户数据'
  return `当前页 ${userList.value.length} 位 / 共 ${formatNumber(pagination.total)} 位`
})

function getRoleText(role: string): string {
  return roleTextMap[role] || role
}

function getRoleTone(role: string): string {
  return roleToneMap[role] || 'user'
}

function getAvatarTone(row: ManageUserRow): string {
  const tone = avatarTones[row.id % avatarTones.length] ?? 'slate'
  return `user-avatar--${tone}`
}

function getAvatarInitial(row: ManageUserRow): string {
  const source = (row.realName || row.username || '?').trim()
  return source.slice(0, 1).toUpperCase()
}

function getAvatarSrc(row: ManageUserRow): string {
  return row.avatar ? getAvatarUrl(row.avatar) : ''
}

function formatNumber(value: number): string {
  return new Intl.NumberFormat('zh-CN').format(value || 0)
}

function formatTime(time: string | undefined): string {
  if (!time) return '—'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time
  const pad = (value: number) => value.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function fetchUserList() {
  loading.value = true
  try {
    const params: ManageUserPageRequest = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    const keyword = searchForm.keyword.trim()
    if (keyword) params.keyword = keyword
    if (searchForm.role) params.role = searchForm.role
    if (searchForm.status !== undefined) params.status = searchForm.status

    const res = await getManageUserPage(params)
    userList.value = (res.records || []).map(item => ({
      ...item,
      _statusLoading: false
    }))
    pagination.total = res.total || 0
  } catch {
    userList.value = []
  } finally {
    loading.value = false
  }
}

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

function clearKeyword() {
  searchForm.keyword = ''
  handleSearch()
}

function setStatus(status: number | undefined) {
  searchForm.status = status
  handleSearch()
}

function handlePageChange() {
  fetchUserList()
}

async function handleToggleStatus(row: ManageUserRow) {
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
    return
  }

  row._statusLoading = true
  try {
    if (newStatus === 1) {
      await enableUser(row.id)
    } else {
      await disableUser(row.id)
    }
    row.status = newStatus
    ElMessage.success(`用户已${actionText}`)
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._statusLoading = false
  }
}

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
  fetchUserList()
})
</script>

<style scoped>
.admin-users {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 24px;
  color: #1C1C1E;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.hero-panel {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 520px);
  gap: 24px;
  overflow: hidden;
  padding: 26px 28px;
  border: 1px solid rgba(28, 25, 23, 0.08);
  border-radius: 20px;
  background:
    linear-gradient(135deg, rgba(28, 25, 23, 0.96), rgba(63, 55, 46, 0.92)),
    #1C1917;
  box-shadow: 0 18px 45px rgba(28, 25, 23, 0.12);
}

.hero-panel::after {
  content: '';
  position: absolute;
  inset: auto -80px -120px auto;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(200, 164, 100, 0.28), transparent 62%);
  pointer-events: none;
}

.hero-panel__content,
.hero-metrics {
  position: relative;
  z-index: 1;
}

.hero-panel__content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
}

.hero-kicker {
  width: fit-content;
  margin-bottom: 10px;
  padding: 5px 10px;
  border: 1px solid rgba(200, 164, 100, 0.26);
  border-radius: 999px;
  background: rgba(200, 164, 100, 0.1);
  color: #E7C777;
  font-size: 12px;
  font-weight: 700;
}

.hero-panel h1 {
  margin: 0;
  color: #FFFFFF;
  font-size: 26px;
  font-weight: 800;
  letter-spacing: 0;
}

.hero-panel p {
  max-width: 580px;
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.72);
  font-size: 14px;
  line-height: 1.7;
}

.hero-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: stretch;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.08);
}

.hero-metric {
  display: flex;
  min-width: 0;
  flex-direction: column;
  justify-content: center;
  gap: 6px;
  padding: 18px;
}

.hero-metric + .hero-metric {
  border-left: 1px solid rgba(255, 255, 255, 0.12);
}

.hero-metric span {
  color: rgba(255, 255, 255, 0.64);
  font-size: 12px;
  font-weight: 600;
}

.hero-metric strong {
  color: #FFFFFF;
  font-size: 28px;
  font-weight: 800;
  line-height: 1;
}

.hero-metric small {
  color: rgba(231, 199, 119, 0.9);
  font-size: 12px;
  line-height: 1.4;
}

.list-card {
  overflow: hidden;
  border: 1px solid #E8E8E6;
  border-radius: 20px;
  background: #FFFFFF;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.filter-body {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px 24px;
}

.status-filter-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.status-filter-label {
  padding-top: 9px;
  color: #6B6B6E;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.status-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 36px;
  padding: 0 14px;
  border: 1px solid #E8E8E6;
  border-radius: 999px;
  background: #FFFFFF;
  color: #4B4B4E;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.status-pill:hover,
.status-pill:focus-visible {
  border-color: rgba(200, 164, 100, 0.55);
  background: #FDFCFA;
  color: #1C1C1E;
  outline: none;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.12);
}

.status-pill.active {
  border-color: rgba(200, 164, 100, 0.5);
  background: rgba(200, 164, 100, 0.12);
  color: #7A5C22;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.status-dot--active {
  background: #22C55E;
}

.status-dot--inactive {
  background: #9CA3AF;
}

.filter-divider {
  height: 1px;
  background: linear-gradient(to right, #F0F0EE 0%, transparent 100%);
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  flex-wrap: wrap;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 180px;
}

.filter-field--keyword {
  flex: 1;
  min-width: 260px;
}

.filter-field label {
  color: #6B6B6E;
  font-size: 11px;
  font-weight: 700;
}

.filter-input-wrap,
.select-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.filter-input-wrap > .el-icon {
  position: absolute;
  left: 12px;
  color: #A1A1AA;
  pointer-events: none;
}

.filter-input-wrap input,
.select-wrap select {
  width: 100%;
  height: 38px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  background: #FFFFFF;
  color: #1C1C1E;
  font-size: 13px;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.filter-input-wrap input {
  padding: 0 36px;
}

.select-wrap select {
  min-width: 164px;
  padding: 0 34px 0 12px;
  cursor: pointer;
  appearance: none;
}

.select-wrap::after {
  content: '';
  position: absolute;
  right: 13px;
  width: 7px;
  height: 7px;
  border-right: 1.5px solid #A1A1AA;
  border-bottom: 1.5px solid #A1A1AA;
  transform: rotate(45deg) translateY(-2px);
  pointer-events: none;
}

.filter-input-wrap input:focus,
.select-wrap select:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.12);
}

.input-clear {
  position: absolute;
  right: 9px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: #E8E8E6;
  color: #6B6B6E;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.input-clear:hover,
.input-clear:focus-visible {
  background: #D4D4D2;
  color: #1C1C1E;
  outline: none;
}

.filter-actions,
.table-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-actions {
  margin-left: auto;
}

.primary-btn,
.secondary-btn,
.icon-btn,
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.2s ease, background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.primary-btn,
.secondary-btn {
  min-height: 38px;
  padding: 0 16px;
}

.primary-btn {
  border: none;
}

.primary-btn--gold {
  background: linear-gradient(135deg, #C8A464 0%, #B08B45 100%);
  color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(200, 164, 100, 0.25);
}

.primary-btn--gold:hover,
.primary-btn--gold:focus-visible {
  outline: none;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(200, 164, 100, 0.28);
}

.secondary-btn {
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
}

.secondary-btn:hover,
.secondary-btn:focus-visible {
  border-color: #D4D4D2;
  background: #FAFAF9;
  color: #1C1C1E;
  outline: none;
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
  color: #1C1C1E;
  font-size: 16px;
  font-weight: 800;
}

.list-card__title span {
  color: #A1A1AA;
  font-size: 12px;
  font-weight: 600;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(59, 110, 110, 0.1);
  color: #235A5A;
  font-size: 12px;
  font-weight: 700;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
}

.icon-btn:hover,
.icon-btn:focus-visible {
  border-color: #D4D4D2;
  background: #FAFAF9;
  color: #1C1C1E;
  outline: none;
}

.icon-btn:disabled,
.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.62;
  transform: none;
}

.table-container {
  padding: 0 24px;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.user-avatar {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.66);
  border-radius: 14px;
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 800;
  flex-shrink: 0;
  box-shadow: 0 8px 20px rgba(28, 25, 23, 0.12);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar--amber {
  background: linear-gradient(135deg, #D6B05A, #9A6B1F);
}

.user-avatar--teal {
  background: linear-gradient(135deg, #4B8F8F, #235A5A);
}

.user-avatar--indigo {
  background: linear-gradient(135deg, #6976D9, #3D4D99);
}

.user-avatar--rose {
  background: linear-gradient(135deg, #D76C82, #9C2F47);
}

.user-avatar--slate {
  background: linear-gradient(135deg, #64748B, #334155);
}

.user-identity,
.contact-cell {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.user-identity strong {
  overflow: hidden;
  color: #1C1C1E;
  font-size: 14px;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-identity span,
.contact-cell small,
.cell-empty {
  color: #A1A1AA;
  font-size: 12px;
  font-weight: 600;
}

.contact-cell span {
  overflow: hidden;
  color: #4B4B4E;
  font-size: 13px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.role-tag--user {
  background: #F3F4F6;
  color: #4B5563;
}

.role-tag--seller {
  background: rgba(200, 164, 100, 0.14);
  color: #7A5C22;
}

.role-tag--operator {
  background: rgba(59, 110, 110, 0.12);
  color: #235A5A;
}

.role-tag--admin {
  background: rgba(239, 68, 68, 0.1);
  color: #B91C1C;
}

.role-tag--service {
  background: rgba(34, 197, 94, 0.12);
  color: #15803D;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.status-badge--active {
  background: rgba(34, 197, 94, 0.12);
  color: #15803D;
}

.status-badge--inactive {
  background: #F3F4F6;
  color: #6B7280;
}

.time-text {
  color: #909399;
  font-size: 12px;
  font-weight: 600;
}

.action-btn {
  min-height: 32px;
  padding: 0 12px;
  border: 1px solid;
}

.action-btn--success {
  border-color: rgba(34, 197, 94, 0.35);
  background: rgba(34, 197, 94, 0.08);
  color: #15803D;
}

.action-btn--success:hover,
.action-btn--success:focus-visible {
  border-color: #22C55E;
  background: #22C55E;
  color: #FFFFFF;
  outline: none;
}

.action-btn--danger {
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.06);
  color: #B91C1C;
}

.action-btn--danger:hover,
.action-btn--danger:focus-visible {
  border-color: #EF4444;
  background: #EF4444;
  color: #FFFFFF;
  outline: none;
}

.btn-spinner {
  animation: spin 1s linear infinite;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 8px;
  min-height: 220px;
  color: #A1A1AA;
}

.empty-state__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: #F5F5F4;
  color: #C8A464;
  font-size: 22px;
}

.empty-state strong {
  color: #1C1C1E;
  font-size: 15px;
}

.empty-state span {
  color: #A1A1AA;
  font-size: 13px;
}

:deep(.user-row:hover > td) {
  background: #FAFAF9 !important;
}

:deep(.el-table__fixed-right::before),
:deep(.el-table__fixed::before) {
  background: #F0F0EE;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 18px 24px;
  border-top: 1px solid #F0F0EE;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  border: 1px solid #E8E8E6;
  border-radius: 8px;
  background: #FFFFFF;
  color: #6B6B6E;
  font-weight: 600;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  border-color: #3B6E6E;
  background: #3B6E6E;
  color: #FFFFFF;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1180px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .hero-metrics {
    max-width: 680px;
  }
}

@media (max-width: 900px) {
  .hero-panel {
    padding: 22px;
  }

  .status-filter-section {
    flex-direction: column;
    gap: 10px;
  }

  .status-filter-label {
    padding-top: 0;
  }

  .filter-actions {
    width: 100%;
    margin-left: 0;
  }

  .filter-actions .primary-btn,
  .filter-actions .secondary-btn {
    flex: 1;
  }

  .filter-field,
  .filter-field--keyword {
    width: 100%;
    min-width: 0;
  }
}

@media (max-width: 768px) {
  .hero-metrics {
    grid-template-columns: 1fr;
  }

  .hero-metric + .hero-metric {
    border-top: 1px solid rgba(255, 255, 255, 0.12);
    border-left: none;
  }

  .list-card__header {
    align-items: flex-start;
    flex-direction: column;
  }

  .table-actions {
    width: 100%;
    justify-content: space-between;
  }

  .table-container {
    overflow-x: auto;
    padding: 0 16px;
  }

  .pagination-bar {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .admin-users {
    gap: 16px;
  }

  .hero-panel,
  .filter-body {
    padding: 18px;
  }

  .hero-panel h1 {
    font-size: 22px;
  }

  .status-pills {
    gap: 8px;
  }

  .status-pill {
    min-height: 34px;
    padding: 0 11px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .status-pill,
  .primary-btn,
  .secondary-btn,
  .icon-btn,
  .action-btn,
  .filter-input-wrap input,
  .select-wrap select,
  .input-clear {
    transition: none;
  }

  .btn-spinner {
    animation: none;
  }
}
</style>
