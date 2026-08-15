<template>
  <div class="seller-profile">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">店铺设置</h1>
        <p class="page-desc">管理你的店铺信息、Logo 和横幅展示</p>
      </div>
      <div class="header-right">
        <el-button
          type="primary"
          :loading="saving"
          class="save-btn"
          @click="saveProfile"
        >
          <template #icon>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z"/>
              <polyline points="17 21 17 13 7 13 7 21"/>
              <polyline points="7 3 7 8 15 8"/>
            </svg>
          </template>
          保存设置
        </el-button>
      </div>
    </div>

    <div v-loading="loading" class="profile-body">
      <!-- 横幅区域 -->
      <section class="setting-section">
        <div class="section-head">
          <div class="section-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <circle cx="8.5" cy="8.5" r="1.5"/>
              <polyline points="21 15 16 10 5 21"/>
            </svg>
          </div>
          <div class="section-text">
            <h2>店铺横幅</h2>
            <p>建议尺寸 1200 × 400px，展示在店铺顶部</p>
          </div>
        </div>
        <div class="section-body">
          <div
            class="banner-uploader"
            :class="{ 'is-dragover': bannerDragover }"
            @dragover.prevent="bannerDragover = true"
            @dragleave.prevent="bannerDragover = false"
            @drop.prevent="handleBannerDrop"
          >
            <div v-if="bannerPreviewUrl" class="banner-preview">
              <img :src="bannerPreviewUrl" alt="店铺横幅" />
              <div class="banner-overlay">
                <div class="overlay-actions">
                  <el-button size="small" round @click="triggerBannerUpload">
                    <template #icon>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                        <polyline points="17 8 12 3 7 8"/>
                        <line x1="12" y1="3" x2="12" y2="15"/>
                      </svg>
                    </template>
                    更换图片
                  </el-button>
                  <el-button size="small" round plain @click="removeBanner">
                    <template #icon>
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3 6 5 6 21 6"/>
                        <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
                      </svg>
                    </template>
                    移除
                  </el-button>
                </div>
              </div>
            </div>
            <div v-else class="banner-placeholder" @click="triggerBannerUpload">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
              <span class="placeholder-title">上传店铺横幅</span>
              <span class="placeholder-desc">拖拽图片到此处，或点击选择文件</span>
              <span class="placeholder-hint">支持 JPG / PNG，建议尺寸 1200 × 400px</span>
            </div>
            <input
              ref="bannerInputRef"
              type="file"
              accept="image/*"
              class="file-input-hidden"
              @change="handleBannerFileChange"
            />
          </div>
        </div>
      </section>

      <!-- Logo 区域 -->
      <section class="setting-section">
        <div class="section-head">
          <div class="section-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
            </svg>
          </div>
          <div class="section-text">
            <h2>店铺 Logo</h2>
            <p>建议尺寸 200 × 200px，方形图片效果最佳</p>
          </div>
        </div>
        <div class="section-body">
          <div class="logo-upload-area">
            <div class="logo-preview-wrap">
              <div class="logo-preview" @click="triggerLogoUpload">
                <img v-if="logoPreviewUrl" :src="logoPreviewUrl" alt="店铺Logo" />
                <div v-else class="logo-placeholder">
                  <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20.84 4.61a5.5 5.5 0 00-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 00-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 000-7.78z"/>
                  </svg>
                  <span>上传 Logo</span>
                </div>
              </div>
            </div>
            <div class="logo-actions">
              <input
                ref="logoInputRef"
                type="file"
                accept="image/*"
                class="file-input-hidden"
                @change="handleLogoFileChange"
              />
              <el-button size="small" @click="triggerLogoUpload">
                <template #icon>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/>
                    <polyline points="17 8 12 3 7 8"/>
                    <line x1="12" y1="3" x2="12" y2="15"/>
                  </svg>
                </template>
                选择文件
              </el-button>
              <span v-if="logoFileName" class="file-name">{{ logoFileName }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 基本信息区域 -->
      <section class="setting-section">
        <div class="section-head">
          <div class="section-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
          </div>
          <div class="section-text">
            <h2>基本信息</h2>
            <p>完善店铺资料有助于买家更好地了解你的店铺</p>
          </div>
        </div>
        <div class="section-body">
          <el-form
            ref="formRef"
            :model="profileForm"
            :rules="formRules"
            label-position="top"
            class="setting-form"
          >
            <div class="form-grid">
              <el-form-item label="店铺名称" prop="name" class="grid-full">
                <el-input
                  v-model="profileForm.name"
                  placeholder="请输入店铺名称"
                  size="large"
                  maxlength="50"
                  show-word-limit
                >
                  <template #prefix>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>
                      <polyline points="9 22 9 12 15 12 15 22"/>
                    </svg>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="联系电话" prop="phone">
                <el-input
                  v-model="profileForm.phone"
                  placeholder="请输入联系电话"
                  size="large"
                  maxlength="20"
                >
                  <template #prefix>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z"/>
                    </svg>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="店铺地址" prop="address">
                <el-input
                  v-model="profileForm.address"
                  placeholder="请输入店铺地址"
                  size="large"
                  maxlength="100"
                >
                  <template #prefix>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/>
                      <circle cx="12" cy="10" r="3"/>
                    </svg>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            <el-form-item label="店铺描述" prop="description" class="grid-full">
              <el-input
                v-model="profileForm.description"
                type="textarea"
                :rows="4"
                placeholder="介绍一下你的店铺，让买家更了解你..."
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </section>

      <!-- 底部操作栏 -->
      <div class="form-footer">
        <el-button
          type="primary"
          :loading="saving"
          size="large"
          class="submit-btn"
          @click="saveProfile"
        >
          <template #icon>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z"/>
              <polyline points="17 21 17 13 7 13 7 21"/>
              <polyline points="7 3 7 8 15 8"/>
            </svg>
          </template>
          保存设置
        </el-button>
        <span class="footer-hint">修改后记得保存哦</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyStore, updateStore, type UpdateStoreRequest } from '@/api/shop'
import { getStoreLogoUrl, getStoreBannerUrl } from '@/utils/resource'

const saving = ref(false)
const loading = ref(true)
const storeId = ref(0)
const bannerDragover = ref(false)

const formRef = ref()

// 表单数据
const profileForm = reactive({
  name: '',
  description: '',
  phone: '',
  address: ''
})

// 表单校验
const formRules = {
  name: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^[\d\-()+\s]{6,20}$/, message: '请输入有效的联系电话', trigger: 'blur' }
  ]
}

// --- Logo 相关 ---
const logoInputRef = ref<HTMLInputElement>()
const logoFile = ref<File | null>(null)
const shopLogoDbPath = ref('')
const logoFileName = ref('')

const logoPreviewUrl = computed(() => {
  if (logoFile.value) {
    return URL.createObjectURL(logoFile.value)
  }
  if (shopLogoDbPath.value) {
    return getStoreLogoUrl(shopLogoDbPath.value)
  }
  return ''
})

const triggerLogoUpload = () => {
  logoInputRef.value?.click()
}

const handleLogoFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  logoFile.value = file
  logoFileName.value = file.name
  input.value = ''
}

// --- Banner 相关 ---
const bannerInputRef = ref<HTMLInputElement>()
const bannerFile = ref<File | null>(null)
const shopBannerDbPath = ref('')

const bannerPreviewUrl = computed(() => {
  if (bannerFile.value) {
    return URL.createObjectURL(bannerFile.value)
  }
  if (shopBannerDbPath.value) {
    return getStoreBannerUrl(shopBannerDbPath.value)
  }
  return ''
})

const triggerBannerUpload = () => {
  bannerInputRef.value?.click()
}

const handleBannerFileChange = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  bannerFile.value = file
  input.value = ''
}

const handleBannerDrop = (e: DragEvent) => {
  bannerDragover.value = false
  const file = e.dataTransfer?.files?.[0]
  if (!file || !file.type.startsWith('image/')) {
    ElMessage.warning('请拖入图片文件')
    return
  }
  bannerFile.value = file
}

const removeBanner = () => {
  bannerFile.value = null
  shopBannerDbPath.value = ''
}

// --- 加载店铺信息 ---
const loadShopInfo = async () => {
  try {
    loading.value = true
    const store = await getMyStore()
    storeId.value = store.id
    profileForm.name = store.name || ''
    profileForm.description = store.description || ''
    profileForm.phone = store.phone || ''
    profileForm.address = store.address || ''
    shopLogoDbPath.value = store.logo || ''
    shopBannerDbPath.value = store.banner || ''
  } catch {
    ElMessage.error('获取店铺信息失败')
  } finally {
    loading.value = false
  }
}

// --- 保存 ---
const saveProfile = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善店铺信息')
    return
  }

  try {
    saving.value = true
    const updateData: UpdateStoreRequest = {
      id: storeId.value,
      name: profileForm.name,
      description: profileForm.description,
      phone: profileForm.phone,
      address: profileForm.address
    }
    await updateStore(
      updateData,
      logoFile.value || undefined,
      bannerFile.value || undefined
    )
    ElMessage.success('店铺设置已保存')

    // 更新本地路径
    if (logoFile.value) {
      shopLogoDbPath.value = URL.createObjectURL(logoFile.value)
    }
    if (bannerFile.value) {
      shopBannerDbPath.value = URL.createObjectURL(bannerFile.value)
    }
    logoFile.value = null
    bannerFile.value = null
    logoFileName.value = ''
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShopInfo()
})
</script>

<style scoped>
/* ============================================
   店铺设置 — 完整样式
   ============================================ */
.seller-profile {
  max-width: 860px;
  margin: 0 auto;
  padding: 12px 8px 40px;
}

/* --- 页面头部 --- */
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 28px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaecf0;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
  letter-spacing: -0.02em;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}

.save-btn {
  padding: 10px 24px;
  font-weight: 600;
  font-size: 14px;
  border-radius: 8px;
  background: #1e1e2d;
  border-color: #1e1e2d;
  transition: all 0.2s;
}
.save-btn:hover {
  background: #2d2d44;
  border-color: #2d2d44;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 30, 45, 0.25);
}

/* --- 设置区域通用 --- */
.setting-section {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eaecf0;
  margin-bottom: 20px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.setting-section:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #eaecf0;
}

.section-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #eaecf0;
  color: #1e1e2d;
  flex-shrink: 0;
}

.section-text {
  flex: 1;
}
.section-text h2 {
  margin: 0 0 2px;
  font-size: 16px;
  font-weight: 650;
  color: #1a1a2e;
}
.section-text p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.section-body {
  padding: 24px;
}

/* --- 横幅上传 --- */
.banner-uploader {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  background: #f9fafb;
  border: 2px dashed #d1d5db;
  min-height: 200px;
}
.banner-uploader.is-dragover {
  border-color: #1e1e2d;
  background: #f3f4f6;
}

.banner-preview {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 1;
  overflow: hidden;
}
.banner-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.banner-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
}
.banner-preview:hover .banner-overlay {
  opacity: 1;
}
.overlay-actions {
  display: flex;
  gap: 10px;
}

.banner-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 48px 20px;
  cursor: pointer;
  transition: background 0.2s;
  min-height: 200px;
}
.banner-placeholder:hover {
  background: #f3f4f6;
}
.placeholder-title {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
}
.placeholder-desc {
  font-size: 13px;
  color: #9ca3af;
}
.placeholder-hint {
  font-size: 12px;
  color: #c4c8cf;
  margin-top: 4px;
}

.file-input-hidden {
  display: none;
}

/* --- Logo 上传 --- */
.logo-upload-area {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo-preview-wrap {
  flex-shrink: 0;
}

.logo-preview {
  width: 100px;
  height: 100px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #eaecf0;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f9fafb;
}
.logo-preview:hover {
  border-color: #1e1e2d;
  box-shadow: 0 0 0 3px rgba(30, 30, 45, 0.08);
}
.logo-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.logo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #9ca3af;
  font-size: 12px;
}

.logo-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}
.file-name {
  font-size: 13px;
  color: #6b7280;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* --- 表单 --- */
.setting-form {
  width: 100%;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 20px;
}
.grid-full {
  grid-column: 1 / -1;
}

.setting-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  padding-bottom: 6px;
}

.setting-form :deep(.el-input__wrapper),
.setting-form :deep(.el-textarea__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d1d5db inset;
  transition: box-shadow 0.2s;
}
.setting-form :deep(.el-input__wrapper:hover),
.setting-form :deep(.el-textarea__wrapper:hover) {
  box-shadow: 0 0 0 1px #9ca3af inset;
}
.setting-form :deep(.el-input__wrapper.is-focus),
.setting-form :deep(.el-textarea__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #1e1e2d inset;
}

.setting-form :deep(.el-input__inner) {
  height: 42px;
  font-size: 14px;
}

.setting-form :deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
}

.setting-form :deep(.el-input__count),
.setting-form :deep(.el-textarea__count) {
  color: #9ca3af;
  font-size: 12px;
}

/* --- 底部 --- */
.form-footer {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-top: 8px;
}

.submit-btn {
  padding: 12px 32px;
  font-weight: 600;
  font-size: 15px;
  border-radius: 8px;
  background: #1e1e2d;
  border-color: #1e1e2d;
  transition: all 0.2s;
}
.submit-btn:hover {
  background: #2d2d44;
  border-color: #2d2d44;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 30, 45, 0.25);
}

.footer-hint {
  font-size: 13px;
  color: #9ca3af;
}

/* --- 加载状态 --- */
.profile-body:deep(.el-loading-mask) {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.7);
}

/* --- 响应式 --- */
@media (max-width: 768px) {
  .seller-profile {
    padding: 8px 4px 24px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
  }
  .header-right {
    width: 100%;
  }
  .save-btn {
    width: 100%;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .section-body {
    padding: 16px;
  }

  .banner-placeholder {
    padding: 32px 16px;
    min-height: 140px;
  }

  .logo-upload-area {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-footer {
    flex-direction: column;
    align-items: stretch;
  }
  .submit-btn {
    width: 100%;
  }
  .footer-hint {
    text-align: center;
  }
}
</style>