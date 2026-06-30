<template>
  <div class="banner-manage" v-loading="loading">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">轮播图管理</h1>
          <p class="welcome-desc">配置首页展示轮播图，支持排序、启用/禁用和跳转链接。</p>
        </div>
        <button class="primary-btn primary-btn--gold" @click="openAddDialog">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新增轮播图
        </button>
      </div>
    </section>

    <!-- 统计卡 -->
    <section class="stats-row">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">轮播图总数</span>
          <span class="stat-card__value">{{ stats.total }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">已启用</span>
          <span class="stat-card__value">{{ stats.active }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--inactive">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="8" y1="12" x2="16" y2="12"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">已禁用</span>
          <span class="stat-card__value">{{ stats.inactive }}</span>
        </div>
      </div>
    </section>

    <!-- 列表卡片 -->
    <section class="list-card">
      <div class="list-card__header">
        <div class="list-card__title">
          <h2>轮播图列表</h2>
          <span>共 {{ bannerList.length }} 条记录</span>
        </div>
        <div class="list-card__toolbar">
          <div class="filter-tabs">
            <button
              v-for="f in statusTabs"
              :key="String(f.value)"
              class="filter-tab"
              :class="{ active: statusFilter === f.value }"
              @click="setStatusFilter(f.value)"
            >{{ f.label }}</button>
          </div>
          <button class="icon-btn" title="刷新" @click="loadList">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="banner-list">
        <div
          v-for="banner in bannerList"
          :key="banner.id"
          class="banner-item"
          :class="{ 'banner-item--disabled': banner.status !== 1 }"
        >
          <div class="banner-item__thumb">
            <el-image
              :src="getImageUrl(banner.imageUrl)"
              fit="cover"
              loading="lazy"
              :preview-src-list="[getImageUrl(banner.imageUrl)]"
              hide-on-click-modal
            />
            <span class="status-badge" :class="banner.status === 1 ? 'status-badge--active' : 'status-badge--inactive'">
              {{ banner.status === 1 ? '启用' : '禁用' }}
            </span>
          </div>

          <div class="banner-item__info">
            <div class="info-row info-row--title">
              <strong>{{ banner.title || '未命名' }}</strong>
              <span class="info-id">#{{ banner.id }}</span>
            </div>
            <div class="info-row info-row--link" v-if="banner.linkUrl">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
              <span>{{ banner.linkUrl }}</span>
            </div>
            <div class="info-row info-row--meta">
              <span>排序 {{ banner.sort }}</span>
              <span>创建于 {{ banner.createdAt }}</span>
            </div>
          </div>

          <div class="banner-item__actions">
            <button class="action-btn action-btn--edit" @click="openEditDialog(banner)">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
              编辑
            </button>
            <button
              class="action-btn"
              :class="banner.status === 1 ? 'action-btn--disable' : 'action-btn--enable'"
              @click="handleToggleStatus(banner)"
            >
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path v-if="banner.status === 1" d="M18.36 6.64a9 9 0 1 1-12.73 0"/>
                <path v-else d="M5 12h14"/>
                <line v-if="banner.status === 1" x1="12" y1="2" x2="12" y2="12"/>
              </svg>
              {{ banner.status === 1 ? '禁用' : '启用' }}
            </button>
            <button class="action-btn action-btn--delete" @click="handleDelete(banner)">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
              删除
            </button>
          </div>
        </div>
      </div>

      <div v-if="bannerList.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </div>
        <p>暂无轮播图</p>
        <span>点击右上角「新增轮播图」按钮开始配置</span>
      </div>
    </section>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="520px"
      :close-on-click-modal="false"
      class="banner-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="82px" label-position="left">
          <el-form-item label="标题" prop="title">
            <el-input v-model="form.title" placeholder="请输入轮播图标题" maxlength="50" />
          </el-form-item>
          <el-form-item label="跳转链接" prop="linkUrl">
            <el-input v-model="form.linkUrl" placeholder="可选，点击轮播图跳转的链接" />
          </el-form-item>
          <el-form-item label="轮播图" prop="imageFile">
            <div class="upload-area" @click="triggerUpload">
              <input
                ref="fileInputRef"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                class="upload-input"
                @change="handleFileChange"
              />
              <template v-if="form.previewUrl">
                <img :src="form.previewUrl" alt="预览" class="upload-preview" />
                <div class="upload-overlay">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.3"/>
                  </svg>
                  <span>更换图片</span>
                </div>
              </template>
              <template v-else>
                <div class="upload-placeholder">
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                    <circle cx="8.5" cy="8.5" r="1.5"/>
                    <polyline points="21 15 16 10 5 21"/>
                  </svg>
                  <strong>点击上传图片</strong>
                  <span>支持 jpg、png、gif、webp，建议尺寸 1920×600</span>
                </div>
              </template>
            </div>
            <p v-if="form.previewUrl && form.imageFile" class="upload-hint">已选择：{{ form.imageFile.name }}</p>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="dialogVisible = false">取消</button>
          <button class="primary-btn" :disabled="saving" @click="handleSave">
            <span v-if="saving" class="btn-spinner" />
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  getBannerList,
  addBanner,
  updateBanner,
  deleteBanner,
  updateBannerStatus,
  type BannerItem
} from '@/api/banner'
import { getBannerUrl } from '@/utils/resource'

const loading = ref(false)
const saving = ref(false)
const bannerList = ref<BannerItem[]>([])
const statusFilter = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement | null>(null)

const form = ref({
  title: '',
  linkUrl: '',
  imageFile: null as File | null,
  previewUrl: ''
})

const statusTabs = [
  { label: '全部', value: undefined },
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 }
]

const stats = computed(() => {
  const total = bannerList.value.length
  const active = bannerList.value.filter(b => b.status === 1).length
  const inactive = total - active
  return { total, active, inactive }
})

const formRules: FormRules = {
  title: [{ required: true, message: '请输入轮播图标题', trigger: 'blur' }],
  imageFile: [
    {
      validator: (_rule: any, _value: any, callback: any) => {
        if (!isEdit.value && !form.value.imageFile) {
          callback(new Error('请选择轮播图图片'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const getImageUrl = (imageUrl: string) => getBannerUrl(imageUrl)

const setStatusFilter = (value?: number) => {
  statusFilter.value = value
  loadList()
}

const loadList = async () => {
  loading.value = true
  try {
    bannerList.value = await getBannerList(statusFilter.value)
  } catch {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.value = { title: '', linkUrl: '', imageFile: null, previewUrl: '' }
  if (fileInputRef.value) fileInputRef.value.value = ''
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (row: BannerItem) => {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    title: row.title,
    linkUrl: row.linkUrl || '',
    imageFile: null,
    previewUrl: getImageUrl(row.imageUrl)
  }
  if (fileInputRef.value) fileInputRef.value.value = ''
  dialogVisible.value = true
}

const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 jpg、png、gif、webp 格式')
    if (fileInputRef.value) fileInputRef.value.value = ''
    return
  }

  // 释放旧的 blob URL
  if (form.value.previewUrl && form.value.previewUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.previewUrl)
  }

  form.value.imageFile = file
  form.value.previewUrl = URL.createObjectURL(file)
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (!form.value.title) return

  saving.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.value.title)
    if (form.value.linkUrl) fd.append('linkUrl', form.value.linkUrl)

    if (isEdit.value && editId.value !== null) {
      fd.append('id', String(editId.value))
      if (form.value.imageFile) fd.append('imageFile', form.value.imageFile)
      await updateBanner(fd)
      ElMessage.success('更新成功')
    } else {
      if (!form.value.imageFile) {
        ElMessage.warning('请选择轮播图图片')
        return
      }
      fd.append('imageFile', form.value.imageFile)
      await addBanner(fd)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (row: BannerItem) => {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}该轮播图吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateBannerStatus(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    await loadList()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 拦截器已处理
    }
  }
}

const handleDelete = async (row: BannerItem) => {
  try {
    await ElMessageBox.confirm(`确定删除轮播图「${row.title}」吗？删除后不可恢复。`, '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 拦截器已处理
    }
  }
}

onMounted(() => {
  loadList()
})

onUnmounted(() => {
  if (form.value.previewUrl && form.value.previewUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.value.previewUrl)
  }
})
</script>

<style scoped>
.banner-manage {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 24px;
  color: #1C1C1E;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===========================
   Bento 顶部
   =========================== */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px;
}

.welcome-card {
  grid-column: span 12;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px 28px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.welcome-desc {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 12px;
  padding: 10px 20px;
  border: 1px solid #1C1C1E;
  background: #1C1C1E;
  color: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.primary-btn:hover:not(:disabled) {
  background: #2C2C2E;
  border-color: #2C2C2E;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.primary-btn--gold {
  background: linear-gradient(135deg, #C8A464 0%, #ff8f1a 100%);
  border: none;
  color: #FFFFFF;
}

.primary-btn--gold:hover:not(:disabled) {
  background: linear-gradient(135deg, #D4B87A 0%, #ff9f2e 100%);
  box-shadow: 0 8px 20px rgba(200, 164, 100, 0.28);
}

.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 12px;
  padding: 10px 20px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

/* ===========================
   统计卡
   =========================== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border-radius: 18px;
  padding: 20px 24px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.05);
}

.stat-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card__icon svg {
  width: 22px;
  height: 22px;
}

.stat-card__icon--total { background: #F5F5F4; color: #C8A464; }
.stat-card__icon--active { background: rgba(34, 197, 94, 0.08); color: #22c55e; }
.stat-card__icon--inactive { background: rgba(239, 68, 68, 0.08); color: #ef4444; }

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card__label {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.stat-card__value {
  font-size: 22px;
  font-weight: 700;
  color: #1C1C1E;
  line-height: 1;
}

/* ===========================
   列表卡片
   =========================== */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
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
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.list-card__title span {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.list-card__toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-tabs {
  display: flex;
  gap: 2px;
  background: #F0F0EE;
  border-radius: 10px;
  padding: 3px;
}

.filter-tab {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.filter-tab:hover {
  color: #1C1C1E;
}

.filter-tab.active {
  background: #FFFFFF;
  color: #1C1C1E;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* ===========================
   列表项
   =========================== */
.banner-list {
  display: flex;
  flex-direction: column;
}

.banner-item {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px 24px;
  border-bottom: 1px solid #F0F0EE;
  transition: background 0.15s ease;
}

.banner-item:last-child {
  border-bottom: none;
}

.banner-item:hover {
  background: #FAFAF9;
}

.banner-item--disabled {
  opacity: 0.65;
}

.banner-item--disabled .banner-item__thumb img {
  filter: grayscale(80%);
}

.banner-item__thumb {
  position: relative;
  width: 140px;
  height: 72px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  background: #F0F0EE;
  border: 1px solid #E8E8E6;
  cursor: pointer;
}

.banner-item__thumb img,
.banner-item__thumb :deep(.el-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.banner-item__thumb:hover img,
.banner-item__thumb:hover :deep(.el-image img) {
  transform: scale(1.05);
}

.status-badge {
  position: absolute;
  top: 6px;
  left: 6px;
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 20px;
  color: #FFFFFF;
  letter-spacing: 0.5px;
}

.status-badge--active { background: #22c55e; }
.status-badge--inactive { background: #ef4444; }

.banner-item__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row--title {
  justify-content: space-between;
  gap: 12px;
}

.info-row--title strong {
  font-size: 15px;
  font-weight: 600;
  color: #1C1C1E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-id {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 500;
  flex-shrink: 0;
}

.info-row--link {
  color: #6B6B6E;
  font-size: 12px;
}

.info-row--link span {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-row--meta {
  gap: 16px;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.banner-item__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.action-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

.action-btn--enable {
  color: #22c55e;
  border-color: rgba(34, 197, 94, 0.3);
  background: rgba(34, 197, 94, 0.06);
}

.action-btn--enable:hover {
  background: rgba(34, 197, 94, 0.12);
  border-color: rgba(34, 197, 94, 0.5);
}

.action-btn--disable {
  color: #f59e0b;
  border-color: rgba(245, 158, 11, 0.3);
  background: rgba(245, 158, 11, 0.06);
}

.action-btn--disable:hover {
  background: rgba(245, 158, 11, 0.12);
  border-color: rgba(245, 158, 11, 0.5);
}

.action-btn--delete {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.06);
}

.action-btn--delete:hover {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.5);
}

/* ===========================
   空状态
   =========================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  text-align: center;
  color: #6B6B6E;
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #F5F5F4;
  color: #A1A1AA;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-icon svg {
  width: 28px;
  height: 28px;
}

.empty-state p {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: #1C1C1E;
}

.empty-state span {
  font-size: 12px;
  color: #A1A1AA;
}

/* ===========================
   弹窗表单
   =========================== */
.banner-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.banner-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.banner-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 10px;
}

.banner-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
  border-top: none;
}

.banner-dialog :deep(.el-form-item__label) {
  color: #6B6B6E;
  font-weight: 500;
}

.banner-dialog :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E8E8E6 inset;
  border-radius: 10px;
  padding: 0 12px;
}

.banner-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #C8A464 inset;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.upload-area {
  position: relative;
  width: 100%;
  height: 180px;
  border-radius: 14px;
  border: 2px dashed #E8E8E6;
  background: #FAFAF9;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease;
}

.upload-area:hover {
  border-color: #C8A464;
  background: #FDFCFA;
}

.upload-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  z-index: 2;
}

.upload-preview {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  background: rgba(28, 28, 30, 0.55);
  color: #FFFFFF;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 3;
  font-size: 13px;
  font-weight: 600;
}

.upload-area:hover .upload-overlay {
  opacity: 1;
}

.upload-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #A1A1AA;
  z-index: 1;
}

.upload-placeholder svg {
  color: #C8A464;
}

.upload-placeholder strong {
  font-size: 14px;
  font-weight: 600;
  color: #1C1C1E;
}

.upload-placeholder span {
  font-size: 12px;
  color: #A1A1AA;
}

.upload-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #6B6B6E;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 900px) {
  .banner-item {
    flex-wrap: wrap;
  }

  .banner-item__actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #F0F0EE;
  }
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .stat-card {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }

  .list-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-card__toolbar {
    width: 100%;
    justify-content: space-between;
  }

  .banner-item__thumb {
    width: 100%;
    height: 140px;
  }

  .banner-item__info {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .banner-item__actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-btn {
    justify-content: center;
  }
}

@media (prefers-reduced-motion: reduce) {
  .primary-btn,
  .secondary-btn,
  .icon-btn,
  .stat-card,
  .banner-item,
  .banner-item__thumb img,
  .action-btn,
  .upload-area,
  .upload-overlay {
    transition: none;
  }

  .banner-item__thumb:hover img,
  .banner-item__thumb:hover :deep(.el-image img) {
    transform: none;
  }

  .btn-spinner {
    animation: none;
  }
}
</style>