<template>
  <div class="shop-apply-page">
    <!-- 顶部导航栏（带返回键） -->
    <NavBar showBack />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner" />
      <p class="loading-text">正在检查申请状态...</p>
    </div>

    <!-- 内容区域 -->
    <div v-else class="apply-container">
      <div class="apply-card">

        <!-- ====== 状态 1: 已开店 ====== -->
        <template v-if="applyStatus === 'opened'">
          <div class="status-section success-section">
            <div class="status-icon-wrap success">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#10b981" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <h2 class="status-title">开店成功！</h2>
            <p class="status-desc">您已拥有正常店铺，欢迎进入卖家中心管理您的店铺</p>
            <el-button type="primary" size="large" class="action-btn" @click="goSellerCenter">
              进入卖家中心
            </el-button>
          </div>
        </template>

        <!-- ====== 状态 2: 审核中 ====== -->
        <template v-else-if="applyStatus === 'pending'">
          <div class="status-section pending-section">
            <div class="status-icon-wrap pending">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#f59e0b" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
            </div>
            <h2 class="status-title">审核中</h2>
            <p class="status-desc">
              开店申请已提交，请耐心等待管理员审核<br>
              <span class="status-sub">店铺名称：{{ storeInfo?.name }}</span>
            </p>
            <div class="pending-timeline">
              <div class="timeline-item active">
                <div class="timeline-dot" />
                <div class="timeline-content">
                  <span class="timeline-label">已提交申请</span>
                  <span class="timeline-time">{{ formatTime(storeInfo?.createdAt) }}</span>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-dot" />
                <div class="timeline-content">
                  <span class="timeline-label">管理员审核</span>
                  <span class="timeline-time">预计 1-3 个工作日</span>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-dot" />
                <div class="timeline-content">
                  <span class="timeline-label">审核完成</span>
                  <span class="timeline-time">等待通知</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- ====== 状态 3: 审核失败 ====== -->
        <template v-else-if="applyStatus === 'rejected'">
          <div class="status-section rejected-section">
            <div class="status-icon-wrap rejected">
              <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="#ef4444" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
            </div>
            <h2 class="status-title">审核未通过</h2>
            <div class="reject-reason">
              <span class="reason-label">驳回原因：</span>
              <span class="reason-text">{{ storeInfo?.rejectReason || '未知原因' }}</span>
            </div>
            <p class="status-desc">请修改信息后重新提交申请</p>
            <el-button type="primary" size="large" class="action-btn" @click="showReApplyForm = true">
              重新提交申请
            </el-button>
          </div>

          <!-- 重新提交表单 -->
          <div v-if="showReApplyForm" class="form-section">
            <h3 class="form-title">修改申请信息</h3>
            <el-form :model="reApplyForm" label-width="90px" class="apply-form">
              <el-form-item label="店铺名称" required>
                <el-input v-model="reApplyForm.name" placeholder="输入店铺名称" maxlength="30" />
              </el-form-item>
              <el-form-item label="店铺描述">
                <el-input v-model="reApplyForm.description" placeholder="可选，描述主营类目" maxlength="200" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="reApplyForm.phone" placeholder="可选" maxlength="15" />
              </el-form-item>
              <el-form-item label="店铺地址">
                <el-input v-model="reApplyForm.address" placeholder="可选" maxlength="50" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="handleReSubmit">
                  提交申请
                </el-button>
                <el-button @click="showReApplyForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>

        <!-- ====== 状态 4: 新申请表单 ====== -->
        <template v-else>
          <div class="form-section full-width">
            <div class="apply-header">
              <div class="header-icon">
                <svg viewBox="0 0 24 24" width="40" height="40" fill="none" stroke="#ff4400" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="3"/>
                  <line x1="12" y1="8" x2="12" y2="16"/>
                  <line x1="8" y1="12" x2="16" y2="12"/>
                </svg>
              </div>
              <h2 class="apply-title">免费开店</h2>
              <p class="apply-subtitle">填写以下信息即可提交开店申请，管理员审核通过后即可成为商家</p>
            </div>
            <el-form :model="applyForm" label-width="90px" class="apply-form">
              <el-form-item label="店铺名称" required>
                <el-input v-model="applyForm.name" placeholder="请输入店铺名称，如：XX数码旗舰店" maxlength="30" />
              </el-form-item>
              <el-form-item label="店铺描述">
                <el-input v-model="applyForm.description" placeholder="可选，简单描述您的主营类目和特色" maxlength="200" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="applyForm.phone" placeholder="可选，方便管理员联系" maxlength="15" />
              </el-form-item>
              <el-form-item label="店铺地址">
                <el-input v-model="applyForm.address" placeholder="可选" maxlength="50" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit" class="submit-btn">
                  提交开店申请
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </template>

      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getStoreApplyStatus, submitStoreApply, resubmitStoreApply } from '@/api/shop'
import type { Store } from '@/api/shop'
import { useUserStore } from '@/stores/user'
import NavBar from '@/components/NavBar.vue'

const router = useRouter()
const userStore = useUserStore()

// 状态管理
const loading = ref(true)
const submitting = ref(false)
const applyStatus = ref<'none' | 'pending' | 'rejected' | 'opened'>('none')
const storeInfo = ref<Store | null>(null)
const showReApplyForm = ref(false)

// 新申请表单
const applyForm = reactive({
  name: '',
  description: '',
  phone: '',
  address: ''
})

// 重新提交表单
const reApplyForm = reactive({
  name: '',
  description: '',
  phone: '',
  address: ''
})

// 检查申请状态
const checkStatus = async () => {
  try {
    const data = await getStoreApplyStatus()
    storeInfo.value = data
    if (data.status === 1) {
      applyStatus.value = 'opened'
    } else if (data.status === 2) {
      applyStatus.value = 'pending'
    } else if (data.status === 3) {
      applyStatus.value = 'rejected'
      reApplyForm.name = data.name || ''
      reApplyForm.description = data.description || ''
      reApplyForm.phone = data.phone || ''
      reApplyForm.address = data.address || ''
    } else {
      applyStatus.value = 'none'
    }
  } catch {
    // 如果查询失败（如无申请记录），走新申请流程
    applyStatus.value = 'none'
  } finally {
    loading.value = false
  }
}

// 提交新申请
const handleSubmit = async () => {
  if (!applyForm.name.trim()) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  submitting.value = true
  try {
    await submitStoreApply({
      name: applyForm.name.trim(),
      description: applyForm.description.trim() || undefined,
      phone: applyForm.phone.trim() || undefined,
      address: applyForm.address.trim() || undefined
    })
    ElMessage.success('开店申请已提交，请等待管理员审核')
    await checkStatus()
  } catch {
    // 错误已由拦截器处理
  } finally {
    submitting.value = false
  }
}

// 重新提交
const handleReSubmit = async () => {
  if (!reApplyForm.name.trim()) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  if (!storeInfo.value?.id) {
    ElMessage.error('店铺信息异常，请刷新后重试')
    return
  }
  submitting.value = true
  try {
    await resubmitStoreApply({
      storeId: storeInfo.value.id,
      name: reApplyForm.name.trim() || undefined,
      description: reApplyForm.description.trim() || undefined,
      phone: reApplyForm.phone.trim() || undefined,
      address: reApplyForm.address.trim() || undefined
    })
    ElMessage.success('开店申请已重新提交，请等待管理员审核')
    showReApplyForm.value = false
    await checkStatus()
  } catch {
    // 错误已由拦截器处理
  } finally {
    submitting.value = false
  }
}

// 跳转到卖家中心
const goSellerCenter = () => {
  router.push('/seller')
}

// 格式化时间
const formatTime = (t?: string) => {
  if (!t) return ''
  return t.split(' ')[0] || t.slice(0, 10)
}

onMounted(() => {
  // 未登录跳转登录
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  checkStatus()
})
</script>

<style scoped>
/* ========== 页面容器 ========== */
.shop-apply-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
}

/* ========== 加载 ========== */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 60px);
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #e5e7eb;
  border-top-color: #ff4400;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-text {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

/* ========== 卡片容器 ========== */
.apply-container {
  display: flex;
  justify-content: center;
  padding: 40px 16px;
}

.apply-card {
  width: 100%;
  max-width: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* ========== 状态区（通用） ========== */
.status-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48px 32px;
  text-align: center;
}

.status-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  margin-bottom: 20px;
}

.status-icon-wrap.success {
  background: #ecfdf5;
}
.status-icon-wrap.pending {
  background: #fffbeb;
}
.status-icon-wrap.rejected {
  background: #fef2f2;
}

.status-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

.status-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
  margin: 0 0 24px;
}

.status-sub {
  font-size: 13px;
  color: #9ca3af;
}

.action-btn {
  width: 200px;
  height: 44px;
  font-size: 15px;
  border-radius: 10px;
}

/* ========== 驳回原因 ========== */
.reject-reason {
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 10px;
  padding: 12px 18px;
  margin-bottom: 12px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.reason-label {
  color: #991b1b;
  font-weight: 500;
  white-space: nowrap;
}

.reason-text {
  color: #dc2626;
  word-break: break-all;
}

/* ========== 审核中时间线 ========== */
.pending-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
  width: 100%;
  max-width: 260px;
  margin-top: 8px;
}

.timeline-item {
  display: flex;
  gap: 14px;
  position: relative;
  padding-bottom: 28px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 7px;
  top: 22px;
  bottom: 0;
  width: 2px;
  background: #e5e7eb;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-item.active::before {
  background: #f59e0b;
}

.timeline-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #e5e7eb;
  flex-shrink: 0;
  margin-top: 2px;
  position: relative;
  z-index: 1;
}

.timeline-item.active .timeline-dot {
  background: #f59e0b;
  box-shadow: 0 0 0 4px rgba(245, 158, 11, 0.2);
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.timeline-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.timeline-item.active .timeline-label {
  color: #b45309;
}

.timeline-time {
  font-size: 12px;
  color: #9ca3af;
}

/* ========== 表单区 ========== */
.form-section {
  padding: 32px;
  border-top: 1px solid #f3f4f6;
}

.form-section.full-width {
  border-top: none;
}

.apply-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 28px;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  background: #fff5f0;
  border-radius: 50%;
  margin-bottom: 12px;
}

.apply-title {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
}

.apply-subtitle {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
  margin: 0;
  max-width: 340px;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
  margin: 0 0 20px;
}

.apply-form {
  max-width: 400px;
  margin: 0 auto;
}

.apply-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.apply-form :deep(.el-form-item__label) {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.apply-form :deep(.el-input__wrapper),
.apply-form :deep(.el-textarea__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d1d5db inset;
  transition: box-shadow 0.2s;
}

.apply-form :deep(.el-input__wrapper:hover),
.apply-form :deep(.el-textarea__wrapper:hover) {
  box-shadow: 0 0 0 1px #9ca3af inset;
}

.apply-form :deep(.el-input__wrapper.is-focus),
.apply-form :deep(.el-textarea__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ff4400 inset;
}

.submit-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  border-radius: 10px;
  margin-top: 4px;
}
</style>