<template>
  <div class="shop-apply-page">
    <!-- 顶部导航栏（带返回键） -->
    <NavBar showBack />

    <!-- 页面 Hero -->
    <section class="apply-hero">
      <div class="hero-glow hero-glow-1" />
      <div class="hero-glow hero-glow-2" />
      <div class="hero-content">
        <h1 class="hero-title">免费开店</h1>
        <p class="hero-subtitle">开启您的专属店铺，触达更多顾客</p>
      </div>
    </section>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-ring">
        <span />
        <span />
        <span />
      </div>
      <p class="loading-text">正在检查申请状态...</p>
    </div>

    <!-- 内容区域 -->
    <main v-else class="apply-container">
      <div class="apply-card">

        <!-- ====== 状态 1: 已开店 ====== -->
        <template v-if="applyStatus === 'opened'">
          <div class="status-section success-section">
            <div class="status-icon-wrap success">
              <div class="icon-bg">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22 4 12 14.01 9 11.01"/>
                </svg>
              </div>
              <div class="pulse-ring" />
            </div>
            <h2 class="status-title">开店成功！</h2>
            <p class="status-desc">您已拥有正常店铺，欢迎进入卖家中心管理您的店铺</p>
            <button class="primary-button" @click="goSellerCenter">
              进入卖家中心
            </button>
          </div>
        </template>

        <!-- ====== 状态 2: 审核中 ====== -->
        <template v-else-if="applyStatus === 'pending'">
          <div class="status-section pending-section">
            <div class="status-icon-wrap pending">
              <div class="icon-bg">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
              </div>
              <div class="spin-ring" />
            </div>
            <h2 class="status-title">审核中</h2>
            <p class="status-desc">
              开店申请已提交，请耐心等待管理员审核
            </p>
            <div class="shop-name-chip">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 7l-8-4-8 4v10l8 4 8-4V7z" />
                <path d="M4 7l8 4 8-4" />
                <path d="M12 22V11" />
              </svg>
              <span>{{ storeInfo?.name }}</span>
            </div>
            <div class="pending-timeline">
              <div class="timeline-item active">
                <div class="timeline-node">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </div>
                <div class="timeline-content">
                  <span class="timeline-label">已提交申请</span>
                  <span class="timeline-time">{{ formatTime(storeInfo?.createdAt) }}</span>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-node" />
                <div class="timeline-content">
                  <span class="timeline-label">管理员审核</span>
                  <span class="timeline-time">预计 1-3 个工作日</span>
                </div>
              </div>
              <div class="timeline-item">
                <div class="timeline-node" />
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
              <div class="icon-bg">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="15" y1="9" x2="9" y2="15"/>
                  <line x1="9" y1="9" x2="15" y2="15"/>
                </svg>
              </div>
            </div>
            <h2 class="status-title">审核未通过</h2>
            <div class="reject-reason">
              <span class="reason-label">驳回原因：</span>
              <span class="reason-text">{{ storeInfo?.rejectReason || '未知原因' }}</span>
            </div>
            <p class="status-desc">请修改信息后重新提交申请</p>
            <button class="primary-button" @click="showReApplyForm = true">
              重新提交申请
            </button>
          </div>

          <!-- 重新提交表单 -->
          <div v-if="showReApplyForm" class="form-section">
            <div class="form-divider" />
            <h3 class="form-title">修改申请信息</h3>
            <el-form :model="reApplyForm" class="apply-form">
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 7l-8-4-8 4v10l8 4 8-4V7z" />
                    <path d="M4 7l8 4 8-4" />
                    <path d="M12 22V11" />
                  </svg>
                  店铺名称 <span class="required">*</span>
                </label>
                <el-input v-model="reApplyForm.name" placeholder="请输入店铺名称" maxlength="30" />
              </div>
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="17" y1="10" x2="3" y2="10" />
                    <line x1="21" y1="6" x2="3" y2="6" />
                    <line x1="21" y1="14" x2="3" y2="14" />
                    <line x1="17" y1="18" x2="3" y2="18" />
                  </svg>
                  店铺描述
                </label>
                <el-input v-model="reApplyForm.description" placeholder="可选，描述主营类目" maxlength="200" type="textarea" :rows="3" />
              </div>
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" />
                  </svg>
                  联系电话
                </label>
                <el-input v-model="reApplyForm.phone" placeholder="可选" maxlength="15" />
              </div>
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                    <circle cx="12" cy="10" r="3" />
                  </svg>
                  店铺地址
                </label>
                <el-input v-model="reApplyForm.address" placeholder="可选" maxlength="50" />
              </div>
              <div class="form-actions">
                <button class="secondary-button" @click="showReApplyForm = false">取消</button>
                <button class="primary-button" :disabled="submitting" @click="handleReSubmit">
                  提交申请
                </button>
              </div>
            </el-form>
          </div>
        </template>

        <!-- ====== 状态 4: 新申请表单 ====== -->
        <template v-else>
          <div class="form-section full-width">
            <!-- 已注销店铺恢复提示 -->
            <div v-if="storeInfo?.status === 0" class="restore-notice">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
              <span>检测到您已注销的店铺「<strong>{{ storeInfo.name }}</strong>」，可修改信息后重新提交审核</span>
            </div>

            <div class="apply-header">
              <div class="header-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 7l-8-4-8 4v10l8 4 8-4V7z" />
                  <path d="M4 7l8 4 8-4" />
                  <path d="M12 22V11" />
                </svg>
              </div>
              <h2 class="apply-title">创建您的店铺</h2>
              <p class="apply-subtitle">填写以下信息即可提交开店申请，管理员审核通过后即可成为商家</p>
            </div>

            <!-- 优势亮点 -->
            <div class="benefits-row">
              <div class="benefit-item">
                <div class="benefit-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10" />
                    <path d="M12 6v6l4 2" />
                  </svg>
                </div>
                <span>快速审核</span>
              </div>
              <div class="benefit-item">
                <div class="benefit-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  </svg>
                </div>
                <span>安全保障</span>
              </div>
              <div class="benefit-item">
                <div class="benefit-icon">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                    <circle cx="9" cy="7" r="4" />
                    <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                    <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                  </svg>
                </div>
                <span>海量用户</span>
              </div>
            </div>

            <el-form :model="applyForm" class="apply-form">
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 7l-8-4-8 4v10l8 4 8-4V7z" />
                    <path d="M4 7l8 4 8-4" />
                    <path d="M12 22V11" />
                  </svg>
                  店铺名称 <span class="required">*</span>
                </label>
                <el-input v-model="applyForm.name" placeholder="请输入店铺名称，如：XX数码旗舰店" maxlength="30" />
              </div>
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="17" y1="10" x2="3" y2="10" />
                    <line x1="21" y1="6" x2="3" y2="6" />
                    <line x1="21" y1="14" x2="3" y2="14" />
                    <line x1="17" y1="18" x2="3" y2="18" />
                  </svg>
                  店铺描述
                </label>
                <el-input v-model="applyForm.description" placeholder="可选，简单描述您的主营类目和特色" maxlength="200" type="textarea" :rows="3" />
              </div>
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z" />
                  </svg>
                  联系电话
                </label>
                <el-input v-model="applyForm.phone" placeholder="可选，方便管理员联系" maxlength="15" />
              </div>
              <div class="custom-field">
                <label class="field-label">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                    <circle cx="12" cy="10" r="3" />
                  </svg>
                  店铺地址
                </label>
                <el-input v-model="applyForm.address" placeholder="可选" maxlength="50" />
              </div>
              <button class="primary-button submit-btn" :disabled="submitting" @click="handleSubmit">
                提交开店申请
              </button>
            </el-form>
          </div>
        </template>

      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStoreApplyStatus, submitStoreApply, resubmitStoreApply, reopenStoreApply } from '@/api/shop'
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
      // 开店成功，刷新用户信息以更新 user_roles（获取 SELLER 角色）
      userStore.fetchUserInfo(true)
    } else if (data.status === 2) {
      applyStatus.value = 'pending'
    } else if (data.status === 3) {
      applyStatus.value = 'rejected'
      reApplyForm.name = data.name || ''
      reApplyForm.description = data.description || ''
      reApplyForm.phone = data.phone || ''
      reApplyForm.address = data.address || ''
    } else if (data.status === 0) {
      // 已注销店铺：进入新申请表单，询问是否使用原店铺信息
      applyStatus.value = 'none'
      loading.value = false
      askRestoreOldStore(data)
      return
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

// 询问是否使用已注销店铺信息
const askRestoreOldStore = async (data: Store) => {
  try {
    await ElMessageBox.confirm(
      `<div class="restore-message-content">
        <div class="restore-message-glow"></div>
        <div class="restore-message-icon-wrap">
          <div class="restore-message-pulse"></div>
          <div class="restore-message-pulse pulse-2"></div>
          <div class="restore-message-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
          </div>
        </div>
        <div class="restore-message-text">
          <span class="restore-message-eyebrow">店铺恢复</span>
          <h3 class="restore-message-title">检测到已注销店铺</h3>
          <p class="restore-message-shop">${data.name || ''}</p>
          <p class="restore-message-desc">
            是否使用原店铺信息并修改后重新提交审核？<br>
            <span>选择「重新填写」将清空表单，由您自行输入。</span>
          </p>
        </div>
      </div>`,
      '',
      {
        confirmButtonText: '使用原店铺',
        cancelButtonText: '重新填写',
        customClass: 'shop-restore-messagebox',
        dangerouslyUseHTMLString: true,
        closeOnClickModal: false,
        closeOnPressEscape: false,
        showClose: false
      }
    )
    // 选择「是」：回填原店铺信息
    applyForm.name = data.name || ''
    applyForm.description = data.description || ''
    applyForm.phone = data.phone || ''
    applyForm.address = data.address || ''
  } catch {
    // 选择「否」：清空表单，让用户自行输入
    applyForm.name = ''
    applyForm.description = ''
    applyForm.phone = ''
    applyForm.address = ''
  }
}

// 手机号校验（填了才校验）
const validatePhone = (phone: string): boolean => {
  if (!phone.trim()) return true
  return /^1[3-9]\d{9}$/.test(phone.trim())
}

// 提交新申请
const handleSubmit = async () => {
  if (!applyForm.name.trim()) {
    ElMessage.warning('请输入店铺名称')
    return
  }
  if (!validatePhone(applyForm.phone)) {
    ElMessage.warning('请输入正确的手机号格式')
    return
  }
  submitting.value = true
  try {
    if (storeInfo.value?.status === 0) {
      // 已注销店铺：重新提交开店申请
      await reopenStoreApply({
        storeId: storeInfo.value.id,
        name: applyForm.name.trim(),
        description: applyForm.description.trim() || undefined,
        phone: applyForm.phone.trim() || undefined,
        address: applyForm.address.trim() || undefined
      })
      ElMessage.success('开店申请已重新提交，请等待管理员审核')
    } else {
      // 全新开店申请（首次申请或非已注销店铺）
      await submitStoreApply({
        name: applyForm.name.trim(),
        description: applyForm.description.trim() || undefined,
        phone: applyForm.phone.trim() || undefined,
        address: applyForm.address.trim() || undefined
      })
      ElMessage.success('开店申请已提交，请等待管理员审核')
    }
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
  if (!validatePhone(reApplyForm.phone)) {
    ElMessage.warning('请输入正确的手机号格式')
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
/* ============================================
   免费开店页面 — 高级暖白金色主题
   ============================================ */
.shop-apply-page {
  min-height: 100vh;
  background: #FAFAF9;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: #1C1917;
}

/* ---------- Hero 区域 ---------- */
.apply-hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FAF7F2 0%, #F5F0E8 100%);
  padding: 56px 20px 72px;
  text-align: center;
  margin-top: 60px;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.4;
  pointer-events: none;
}

.hero-glow-1 {
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(202, 138, 4, 0.22) 0%, transparent 70%);
  top: -70px;
  left: 15%;
  animation: float 8s ease-in-out infinite;
}

.hero-glow-2 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.2) 0%, transparent 70%);
  bottom: -50px;
  right: 18%;
  animation: float 10s ease-in-out infinite reverse;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #1C1917;
}

.hero-subtitle {
  margin: 0;
  font-size: 15px;
  color: #78716C;
  font-weight: 400;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-16px) scale(1.05); }
}

/* ---------- 加载状态 ---------- */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 200px);
  gap: 20px;
}

.loading-ring {
  position: relative;
  width: 56px;
  height: 56px;
}

.loading-ring span {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: #D97706;
  animation: ringSpin 1.4s cubic-bezier(0.4, 0, 0.2, 1) infinite;
}

.loading-ring span:nth-child(2) {
  border-top-color: #F59E0B;
  animation-duration: 1.8s;
  width: 76%;
  height: 76%;
  top: 12%;
  left: 12%;
}

.loading-ring span:nth-child(3) {
  border-top-color: #FCD34D;
  animation-duration: 2.2s;
  width: 52%;
  height: 52%;
  top: 24%;
  left: 24%;
}

@keyframes ringSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  color: #78716C;
  font-size: 15px;
  margin: 0;
}

/* ---------- 卡片容器 ---------- */
.apply-container {
  display: flex;
  justify-content: center;
  padding: 0 16px 60px;
  margin-top: -32px;
  position: relative;
  z-index: 2;
}

.apply-card {
  width: 100%;
  max-width: 560px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 28px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 24px 48px -4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

/* ---------- 状态区（通用） ---------- */
.status-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 44px 32px 40px;
  text-align: center;
}

.status-icon-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  margin-bottom: 22px;
}

.icon-bg {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
}

.icon-bg svg {
  width: 40px;
  height: 40px;
}

.status-icon-wrap.success .icon-bg {
  background: linear-gradient(135deg, #D1FAE5 0%, #A7F3D0 100%);
  color: #059669;
  box-shadow: 0 8px 24px rgba(5, 150, 105, 0.2);
}

.status-icon-wrap.pending .icon-bg {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  color: #B45309;
  box-shadow: 0 8px 24px rgba(180, 83, 9, 0.2);
}

.status-icon-wrap.rejected .icon-bg {
  background: linear-gradient(135deg, #FEE2E2 0%, #FECACA 100%);
  color: #DC2626;
  box-shadow: 0 8px 24px rgba(220, 38, 38, 0.2);
}

.pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(5, 150, 105, 0.15);
  animation: pulse 2s ease-out infinite;
  z-index: 1;
}

.spin-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid transparent;
  border-top-color: #D97706;
  border-right-color: #F59E0B;
  animation: ringSpin 1.2s linear infinite;
  z-index: 1;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 0.6; }
  100% { transform: scale(1.4); opacity: 0; }
}

.status-title {
  font-size: 24px;
  font-weight: 700;
  color: #1C1917;
  margin: 0 0 10px;
}

.status-desc {
  font-size: 15px;
  color: #78716C;
  line-height: 1.6;
  margin: 0 0 24px;
  max-width: 360px;
}

/* 审核中店铺名 */
.shop-name-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #FFFBEB;
  border: 1px solid #FCD34D;
  border-radius: 30px;
  color: #B45309;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 24px;
}

.shop-name-chip svg {
  width: 16px;
  height: 16px;
}

/* ---------- 驳回原因 ---------- */
.reject-reason {
  width: 100%;
  max-width: 360px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 14px;
  padding: 14px 18px;
  margin-bottom: 14px;
  font-size: 14px;
  display: flex;
  align-items: flex-start;
  gap: 6px;
  text-align: left;
}

.reason-label {
  color: #991B1B;
  font-weight: 600;
  white-space: nowrap;
}

.reason-text {
  color: #DC2626;
  word-break: break-all;
}

/* ---------- 审核中时间线 ---------- */
.pending-timeline {
  display: flex;
  flex-direction: column;
  gap: 0;
  width: 100%;
  max-width: 280px;
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
  left: 13px;
  top: 28px;
  bottom: 0;
  width: 2px;
  background: #E7E5E4;
}

.timeline-item:last-child::before {
  display: none;
}

.timeline-item.active::before {
  background: linear-gradient(180deg, #D97706 0%, #FCD34D 100%);
}

.timeline-node {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #F5F5F4;
  border: 2px solid #E7E5E4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  position: relative;
  z-index: 1;
  transition: all 0.3s;
}

.timeline-node svg {
  width: 14px;
  height: 14px;
  color: #FFFFFF;
  transform: scale(0);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.timeline-item.active .timeline-node {
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  border-color: #B45309;
  box-shadow: 0 4px 12px rgba(180, 83, 9, 0.25);
}

.timeline-item.active .timeline-node svg {
  transform: scale(1);
}

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.timeline-label {
  font-size: 15px;
  font-weight: 600;
  color: #A8A29E;
}

.timeline-item.active .timeline-label {
  color: #1C1917;
}

.timeline-time {
  font-size: 13px;
  color: #A8A29E;
}

/* ---------- 表单区 ---------- */
.form-section {
  padding: 36px 32px;
}

.form-section.full-width {
  padding-top: 40px;
}

.restore-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  margin-bottom: 24px;
  background: #FFFBEB;
  border: 1px solid #FCD34D;
  border-radius: 14px;
  color: #92400E;
  font-size: 14px;
  line-height: 1.5;
}

.restore-notice svg {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  margin-top: 1px;
}

.restore-notice strong {
  font-weight: 600;
  color: #B45309;
}

.form-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #E7E5E4, transparent);
  margin-bottom: 32px;
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
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border-radius: 22px;
  margin-bottom: 16px;
  color: #B45309;
  box-shadow: 0 8px 24px rgba(251, 191, 36, 0.15);
}

.header-icon svg {
  width: 40px;
  height: 40px;
}

.apply-title {
  font-size: 24px;
  font-weight: 700;
  color: #1C1917;
  margin: 0 0 8px;
}

.apply-subtitle {
  font-size: 14px;
  color: #78716C;
  line-height: 1.6;
  margin: 0;
  max-width: 360px;
}

.form-title {
  font-size: 18px;
  font-weight: 700;
  color: #1C1917;
  margin: 0 0 22px;
}

/* 优势亮点 */
.benefits-row {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.benefit-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #FFFFFF;
  border: 1px solid #F5F5F4;
  border-radius: 30px;
  font-size: 13px;
  color: #57534E;
  font-weight: 500;
  transition: all 0.2s;
}

.benefit-item:hover {
  border-color: #FCD34D;
  background: #FFFBEB;
  color: #B45309;
  transform: translateY(-2px);
}

.benefit-icon {
  width: 22px;
  height: 22px;
  color: #D97706;
}

.benefit-icon svg {
  width: 100%;
  height: 100%;
}

/* 自定义字段 */
.apply-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.custom-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #44403C;
}

.field-label svg {
  width: 18px;
  height: 18px;
  color: #B45309;
}

.required {
  color: #DC2626;
  margin-left: 2px;
}

.apply-form :deep(.el-input__wrapper),
.apply-form :deep(.el-textarea__wrapper) {
  background: #FFFFFF;
  border-radius: 14px;
  box-shadow: 0 0 0 1px #E7E5E4 inset;
  transition: all 0.2s ease;
  padding: 0 16px;
  min-height: 54px;
}

.apply-form :deep(.el-textarea__wrapper) {
  padding: 12px 16px;
}

.apply-form :deep(.el-input__inner) {
  font-size: 15px;
  color: #1C1917;
}

.apply-form :deep(.el-input__wrapper:hover),
.apply-form :deep(.el-textarea__wrapper:hover) {
  box-shadow: 0 0 0 1px #D6D3D1 inset, 0 4px 12px rgba(0, 0, 0, 0.04);
}

.apply-form :deep(.el-input__wrapper.is-focus),
.apply-form :deep(.el-textarea__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #D97706 inset, 0 0 0 4px rgba(217, 119, 6, 0.1);
}

.apply-form :deep(.el-textarea__inner) {
  font-size: 15px;
  color: #1C1917;
}

/* 按钮 */
.primary-button {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 20px rgba(180, 83, 9, 0.25);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.primary-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(180, 83, 9, 0.32);
}

.primary-button:disabled {
  background: #E7E5E4;
  color: #A8A29E;
  cursor: not-allowed;
  box-shadow: none;
}

.secondary-button {
  flex: 1;
  height: 48px;
  border: 1.5px solid #E7E5E4;
  border-radius: 14px;
  background: #FFFFFF;
  color: #57534E;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.secondary-button:hover {
  border-color: #D97706;
  color: #B45309;
  background: #FFFBEB;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions .primary-button {
  flex: 2;
}

.submit-btn {
  margin-top: 6px;
}

/* ---------- 响应式 ---------- */
@media (max-width: 640px) {
  .apply-hero {
    padding: 44px 20px 56px;
  }

  .hero-title {
    font-size: 28px;
  }

  .apply-card {
    border-radius: 20px;
  }

  .status-section {
    padding: 36px 24px 32px;
  }

  .form-section {
    padding: 28px 22px;
  }

  .benefits-row {
    gap: 8px;
  }

  .benefit-item {
    padding: 8px 12px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .apply-container {
    padding: 0 12px 40px;
  }

  .apply-card {
    border-radius: 18px;
  }

  .status-icon-wrap {
    width: 88px;
    height: 88px;
  }

  .icon-bg {
    width: 68px;
    height: 68px;
  }

  .icon-bg svg {
    width: 34px;
    height: 34px;
  }
}

/* ---------- 恢复店铺确认弹窗（全局，因 teleport 到 body） ---------- */
:global(.shop-restore-messagebox) {
  width: 320px;
  max-width: 88vw;
  border-radius: 20px !important;
  background: linear-gradient(180deg, #FFFBEB 0%, #FFFFFF 100%) !important;
  border: 1px solid rgba(251, 191, 36, 0.2) !important;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 16px 40px -8px rgba(0, 0, 0, 0.12) !important;
  padding: 22px 20px 18px !important;
  animation: messageBoxEnter 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes messageBoxEnter {
  0% { opacity: 0; transform: scale(0.92) translateY(12px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}

:global(.shop-restore-messagebox .el-message-box__header) {
  display: none;
}

:global(.shop-restore-messagebox .el-message-box__content) {
  padding: 0 !important;
}

:global(.shop-restore-messagebox .el-message-box__message) {
  text-align: center;
}

:global(.restore-message-content) {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 2px 4px;
}

:global(.restore-message-glow) {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.16) 0%, transparent 70%);
  filter: blur(14px);
  pointer-events: none;
}

:global(.restore-message-icon-wrap) {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
}

:global(.restore-message-pulse) {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(251, 191, 36, 0.18);
  animation: restorePulse 2.2s ease-out infinite;
}

:global(.restore-message-pulse.pulse-2) {
  animation-delay: 1.1s;
}

@keyframes restorePulse {
  0% { transform: scale(0.7); opacity: 0.6; }
  100% { transform: scale(1.5); opacity: 0; }
}

:global(.restore-message-icon) {
  position: relative;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FCD34D 0%, #D97706 100%);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow:
    0 5px 14px rgba(217, 119, 6, 0.26),
    inset 0 2px 4px rgba(255, 255, 255, 0.3);
  z-index: 1;
}

:global(.restore-message-icon svg) {
  width: 22px;
  height: 22px;
}

:global(.restore-message-text) {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  position: relative;
  z-index: 1;
}

:global(.restore-message-eyebrow) {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  color: #D97706;
  background: rgba(251, 191, 36, 0.12);
  padding: 2px 8px;
  border-radius: 20px;
}

:global(.restore-message-title) {
  margin: 2px 0 0;
  font-size: 15px;
  font-weight: 700;
  color: #1C1917;
}

:global(.restore-message-shop) {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #B45309 0%, #D97706 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:global(.restore-message-desc) {
  margin: 4px 0 0;
  font-size: 12px;
  color: #78716C;
  line-height: 1.55;
}

:global(.restore-message-desc span) {
  display: block;
  margin-top: 2px;
  color: #A8A29E;
  font-size: 11px;
}

:global(.shop-restore-messagebox .el-message-box__btns) {
  display: flex;
  gap: 10px;
  padding: 16px 0 0 !important;
}

:global(.shop-restore-messagebox .el-button) {
  flex: 1;
  height: 36px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

:global(.shop-restore-messagebox .el-button--primary) {
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  border: none;
  box-shadow: 0 5px 14px rgba(180, 83, 9, 0.26);
}

:global(.shop-restore-messagebox .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 10px 22px rgba(180, 83, 9, 0.32);
}

:global(.shop-restore-messagebox .el-button--default) {
  border: 1.5px solid #E7E5E4;
  background: #FFFFFF;
  color: #57534E;
}

:global(.shop-restore-messagebox .el-button--default:hover) {
  border-color: #D97706;
  color: #B45309;
  background: #FFFBEB;
  transform: translateY(-2px);
}
</style>
