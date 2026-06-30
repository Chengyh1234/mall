<template>
  <div class="login-page">
    <!-- 左侧品牌展示区 -->
    <div class="brand-panel">
      <div class="brand-bg"></div>
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-icon">🛍️</span>
        </div>
        <h1 class="brand-title">电商平台</h1>
        <p class="brand-desc">发现好物，畅享品质生活</p>

        <div class="features">
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">海量商品，一站购齐</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">品质保障，退换无忧</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">✓</span>
            <span class="feature-text">极速发货，安全支付</span>
          </div>
        </div>

        <div class="brand-footer">
          <p class="footer-slogan" @click="goToRegister">已有账户？</p>
          <p class="footer-welcome">欢迎回来</p>
        </div>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="form-panel">
      <div class="form-wrapper">
        <div class="form-header">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请选择登录方式</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="tab-switch">
          <button
            :class="['tab-btn', { active: activeTab === 'password' }]"
            @click="activeTab = 'password'"
          >
            <span class="tab-icon">🔑</span>
            密码登录
          </button>
          <button
            :class="['tab-btn', { active: activeTab === 'email' }]"
            @click="activeTab = 'email'; loadEmailCaptcha()"
          >
            <span class="tab-icon">📧</span>
            验证码登录
          </button>
        </div>

        <!-- 密码登录 -->
        <transition name="fade-slide" mode="out-in">
          <div v-if="activeTab === 'password'" key="password" class="tab-content">
            <el-form
              ref="pwdFormRef"
              :model="pwdForm"
              :rules="pwdRules"
              class="login-form"
              @submit.prevent="handlePwdLogin"
            >
              <el-form-item prop="account">
                <div class="input-wrap">
                  <el-input
                    v-model="pwdForm.account"
                    placeholder="请输入用户名 / 手机号 / 邮箱"
                    size="large"
                    :prefix-icon="UserIcon"
                  />
                </div>
              </el-form-item>
              <el-form-item prop="password">
                <div class="input-wrap">
                  <el-input
                    v-model="pwdForm.password"
                    type="password"
                    placeholder="请输入密码"
                    size="large"
                    :prefix-icon="LockIcon"
                    show-password
                  />
                </div>
              </el-form-item>
              <el-form-item prop="captcha">
                <div class="captcha-wrap">
                  <div class="captcha-input-wrap">
                    <el-input
                      v-model="pwdForm.captcha"
                      placeholder="图形验证码"
                      size="large"
                      maxlength="4"
                    />
                  </div>
                  <img
                    v-if="captchaImage"
                    :src="captchaImage"
                    class="captcha-img"
                    alt="验证码"
                    title="点击刷新"
                    @click="loadCaptcha"
                  />
                </div>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="submit-btn"
                  :loading="loading"
                  @click="handlePwdLogin"
                >
                  登 录
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <!-- 验证码登录 -->
          <div v-else key="email" class="tab-content">
            <el-form
              ref="emailFormRef"
              :model="emailForm"
              :rules="emailRules"
              class="login-form"
              @submit.prevent="handleEmailLogin"
            >
              <el-form-item prop="email">
                <div class="input-wrap">
                  <el-input
                    v-model="emailForm.email"
                    placeholder="请输入邮箱地址"
                    size="large"
                    :prefix-icon="MessageIcon"
                  />
                </div>
              </el-form-item>
              <el-form-item prop="captcha">
                <div class="captcha-wrap">
                  <div class="captcha-input-wrap">
                    <el-input
                      v-model="emailForm.captcha"
                      placeholder="图形验证码"
                      size="large"
                      maxlength="4"
                    />
                  </div>
                  <img
                    v-if="emailCaptchaImage"
                    :src="emailCaptchaImage"
                    class="captcha-img"
                    alt="验证码"
                    title="点击刷新"
                    @click="loadEmailCaptcha"
                  />
                </div>
              </el-form-item>
              <el-form-item prop="code">
                <div class="code-wrap">
                  <div class="code-input-wrap">
                    <el-input
                      v-model="emailForm.code"
                      placeholder="6 位验证码"
                      size="large"
                      maxlength="6"
                    />
                  </div>
                  <el-button
                    size="large"
                    :disabled="emailCodeSending || emailCodeCountdown > 0"
                    class="send-code-btn"
                    @click="sendEmailLoginCode"
                  >
                    <template v-if="emailCodeCountdown > 0">
                      重新发送 {{ emailCodeCountdown }}s
                    </template>
                    <template v-else>
                      {{ emailCodeSending ? '发送中...' : '获取验证码' }}
                    </template>
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  class="submit-btn"
                  :loading="emailLoading"
                  @click="handleEmailLogin"
                >
                  登 录
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </transition>

        <!-- 底部链接 -->
        <div class="form-footer">
          <el-button
            link
            type="primary"
            class="footer-link"
            @click="forgotPwdVisible = true"
          >
            忘记密码？
          </el-button>
          <span class="footer-sep">|</span>
          <el-button
            link
            type="primary"
            class="footer-link"
            @click="goToRegister"
          >
            注册新账户
          </el-button>
        </div>

        <div class="home-link">
          <span class="home-link-text" @click="goToHome">← 返回首页</span>
        </div>
      </div>
    </div>

    <!-- 忘记密码对话框 -->
    <el-dialog
      v-model="forgotPwdVisible"
      title="重置密码"
      width="420px"
      destroy-on-close
      class="forgot-dialog"
      :close-on-click-modal="false"
    >
      <div class="dialog-desc">
        请输入您注册时使用的邮箱，我们将发送验证码用于重置密码。
      </div>
      <el-form
        ref="resetFormRef"
        :model="resetForm"
        :rules="resetRules"
        label-width="0"
        class="reset-form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="resetForm.email"
            placeholder="请输入注册邮箱"
            size="large"
            :prefix-icon="MessageIcon"
          />
        </el-form-item>
        <el-form-item prop="code">
          <div class="code-wrap">
            <div class="code-input-wrap">
              <el-input
                v-model="resetForm.code"
                placeholder="6 位验证码"
                size="large"
                maxlength="6"
              />
            </div>
            <el-button
              size="large"
              :disabled="resetCodeSending || resetCodeCountdown > 0"
              class="send-code-btn"
              @click="sendResetCode"
            >
              <template v-if="resetCodeCountdown > 0">
                重新发送 {{ resetCodeCountdown }}s
              </template>
              <template v-else>
                {{ resetCodeSending ? '发送中...' : '获取验证码' }}
              </template>
            </el-button>
          </div>
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input
            v-model="resetForm.newPassword"
            type="password"
            placeholder="新密码（至少 6 位）"
            size="large"
            :prefix-icon="LockIcon"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="resetForm.confirmPassword"
            type="password"
            placeholder="确认新密码"
            size="large"
            :prefix-icon="LockIcon"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="submit-btn"
            :loading="resetting"
            @click="handleResetPassword"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!-- 注册对话框 -->
    <el-dialog
      v-model="registerVisible"
      title=""
      width="460px"
      destroy-on-close
      class="register-dialog"
      :close-on-click-modal="false"
      @opened="loadRegisterCaptcha"
    >
      <!-- 自定义关闭按钮 -->
      <button class="register-close" @click="registerVisible = false" title="关闭">
        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="3">
          <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>

      <div class="register-header">
        <div class="register-header-bg"></div>
        <!-- 装饰气泡 -->
        <div class="bubble b1"></div>
        <div class="bubble b2"></div>
        <div class="bubble b3"></div>
        <div class="register-header-text">
          <div class="register-logo">
            <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="#fff" stroke-width="1.8">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
            </svg>
          </div>
          <h2 class="register-title">创建账户</h2>
          <p class="register-subtitle">加入电商平台，开启购物之旅</p>
        </div>
      </div>

      <div class="register-body">
        <!-- 步骤条 -->
        <div class="step-bar">
          <div class="step-dot" :class="{ active: registerStep >= 1, done: registerStep > 1 }">
            <span class="step-num">1</span>
          </div>
          <div class="step-track">
            <div class="step-track-fill" :style="{ width: ((registerStep - 1) / 2) * 100 + '%' }"></div>
          </div>
          <div class="step-dot" :class="{ active: registerStep >= 2, done: registerStep > 2 }">
            <span class="step-num">2</span>
          </div>
          <div class="step-track">
            <div class="step-track-fill" :style="{ width: ((registerStep - 1) / 2) * 100 + '%' }"></div>
          </div>
          <div class="step-dot" :class="{ active: registerStep >= 3 }">
            <span class="step-num">3</span>
          </div>
        </div>

        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-width="0"
          class="register-form"
        >
          <Transition
            :name="'step-slide-' + (registerDirection === 'next' ? 'forward' : 'backward')"
            mode="out-in"
          >
            <!-- 第1步：账户信息 -->
            <div v-if="registerStep === 1" key="step1" class="step-panel">
              <div class="step-panel-icon">👤</div>
              <div class="step-panel-title">填写账户信息</div>
              <div class="step-panel-desc">创建您的专属账户标识</div>
              <div class="form-group-fields">
                <el-form-item prop="username" class="stagger-item" style="--order:0">
                  <el-input
                    v-model="registerForm.username"
                    placeholder="用户名"
                    size="large"
                    :prefix-icon="UserIcon"
                  />
                </el-form-item>
                <el-form-item prop="email" class="stagger-item" style="--order:1">
                  <el-input
                    v-model="registerForm.email"
                    placeholder="邮箱地址"
                    size="large"
                    :prefix-icon="MessageIcon"
                  />
                </el-form-item>
                <el-form-item prop="phone" class="stagger-item" style="--order:2">
                  <el-input
                    v-model="registerForm.phone"
                    placeholder="手机号（选填）"
                    size="large"
                    :prefix-icon="PhoneIcon"
                    maxlength="11"
                  />
                </el-form-item>
              </div>
            </div>

            <!-- 第2步：安全密码 -->
            <div v-else-if="registerStep === 2" key="step2" class="step-panel">
              <div class="step-panel-icon">🔒</div>
              <div class="step-panel-title">设置安全密码</div>
              <div class="step-panel-desc">保护您的账户安全</div>
              <div class="form-group-fields">
                <el-form-item prop="password" class="stagger-item" style="--order:0">
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="密码（至少 6 位）"
                    size="large"
                    :prefix-icon="LockIcon"
                    show-password
                  />
                </el-form-item>
                <el-form-item prop="confirmPassword" class="stagger-item" style="--order:1">
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="确认密码"
                    size="large"
                    :prefix-icon="LockIcon"
                    show-password
                  />
                </el-form-item>
              </div>
            </div>

            <!-- 第3步：邮箱验证 -->
            <div v-else key="step3" class="step-panel">
              <div class="step-panel-icon">📧</div>
              <div class="step-panel-title">完成邮箱验证</div>
              <div class="step-panel-desc">验证您的邮箱，确保账户可找回</div>
              <div class="form-group-fields">
                <el-form-item prop="registerCaptcha" class="stagger-item" style="--order:0">
                  <div class="captcha-wrap">
                    <div class="captcha-input-wrap">
                      <el-input
                        v-model="registerForm.registerCaptcha"
                        placeholder="图形验证码"
                        size="large"
                        maxlength="4"
                      />
                    </div>
                    <img
                      v-if="registerCaptchaImage"
                      :src="registerCaptchaImage"
                      class="captcha-img"
                      alt="验证码"
                      title="点击刷新"
                      @click="loadRegisterCaptcha"
                    />
                  </div>
                </el-form-item>
                <el-form-item prop="emailCode" class="stagger-item" style="--order:1">
                  <div class="code-wrap">
                    <div class="code-input-wrap">
                      <el-input
                        v-model="registerForm.emailCode"
                        placeholder="邮箱验证码"
                        size="large"
                        maxlength="6"
                      />
                    </div>
                    <el-button
                      size="large"
                      :disabled="registerCodeSending || registerCodeCountdown > 0"
                      class="send-code-btn"
                      @click="sendRegisterCode"
                    >
                      <template v-if="registerCodeCountdown > 0">
                        重新发送 {{ registerCodeCountdown }}s
                      </template>
                      <template v-else>
                        {{ registerCodeSending ? '发送中...' : '获取验证码' }}
                      </template>
                    </el-button>
                  </div>
                </el-form-item>
              </div>
            </div>
          </Transition>
        </el-form>

        <div class="register-footer">
          <div class="step-nav">
            <el-button
              v-if="registerStep > 1"
              size="large"
              class="step-prev-btn"
              @click="prevStep"
            >
              上一步
            </el-button>
            <el-button
              v-if="registerStep < 3"
              type="primary"
              size="large"
              class="step-next-btn"
              @click="nextStep"
            >
              下一步
            </el-button>
            <el-button
              v-if="registerStep === 3"
              type="primary"
              size="large"
              class="register-submit-btn"
              :loading="registering"
              @click="handleRegister"
            >
              创建账户
            </el-button>
          </div>
          <p class="register-agreement">
            点击"创建账户"即表示同意 <a href="javascript:void(0)">服务条款</a> 和 <a href="javascript:void(0)">隐私政策</a>
          </p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, h, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  login,
  getCaptcha,
  sendEmailLoginCode as apiSendEmailLoginCode,
  emailCodeLogin as apiEmailCodeLogin,
  sendResetPasswordCode as apiSendResetCode,
  resetPassword as apiResetPassword,
  sendRegisterEmailCode as apiSendRegisterCode,
  register as apiRegister
} from '@/api/auth'
import { useUserStore, UserRole } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// ---- SVG 图标组件（无三方依赖） ----
const UserIcon = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2, width: 18, height: 18 }, [
      h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
      h('circle', { cx: 12, cy: 7, r: 4 })
    ])
  }
}
const LockIcon = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2, width: 18, height: 18 }, [
      h('rect', { x: 3, y: 11, width: 18, height: 11, rx: 2, ry: 2 }),
      h('path', { d: 'M7 11V7a5 5 0 0 1 10 0v4' })
    ])
  }
}
const MessageIcon = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2, width: 18, height: 18 }, [
      h('path', { d: 'M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z' }),
      h('polyline', { points: '22,6 12,13 2,6' })
    ])
  }
}
const PhoneIcon = {
  render() {
    return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2, width: 18, height: 18 }, [
      h('path', { d: 'M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z' })
    ])
  }
}

// ---- State ----
const loading = ref(false)
const emailLoading = ref(false)
const activeTab = ref('password')

// ---- 图形验证码 ----
const captchaKey = ref('')
const captchaImage = ref('')

const loadCaptcha = async () => {
  try {
    const data = await getCaptcha()
    captchaKey.value = data.captchaKey
    captchaImage.value = data.captchaImage
  } catch { /* ignored */ }
}

// ===== 密码登录 =====
const pwdFormRef = ref<FormInstance>()
const pwdForm = reactive({ account: '', password: '', captcha: '' })
const pwdRules: FormRules = {
  account: [
    { required: true, message: '请输入用户名/手机号/邮箱', trigger: 'blur' },
    {
      validator: (_r: any, v: string, cb: (e?: Error) => void) => {
        if (!v) return cb()
        if (v.includes('@')) {
          if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) return cb(new Error('邮箱格式不正确'))
        } else if (/^\d+$/.test(v)) {
          if (!/^1\d{10}$/.test(v)) return cb(new Error('手机号格式不正确（11位数字）'))
        } else {
          if (/[!@#$%^&*(),.?":{}|<>]/g.test(v)) return cb(new Error('用户名不能包含特殊字符'))
        }
        cb()
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位', trigger: 'blur' }
  ]
}

const handleLoginSuccess = (result: { token: string; userId: number; username: string; roles: string[]; permissions: string[] }) => {
  userStore.setToken(result.token)
  // roles 可能是字符串数组 ['USER'] 也可能是对象数组 [{ code: 'USER', ... }]
  // 统一处理两种格式，避免 map 遍历时类型不匹配报错
  const roles = (result.roles || []).map((r: any) => {
    const code = typeof r === 'string' ? r : (typeof r.code === 'string' ? r.code : UserRole.USER)
    return {
      id: null, name: code, code,
      description: null, status: null, createdAt: null, updatedAt: null
    }
  })
  userStore.setUserInfo({
    id: result.userId,
    username: result.username,
    roles,
    permissions: result.permissions
  })
  ElMessage.success('登录成功')
  // push 是异步的，用 catch 兜底避免静默失败
  router.push('/home').catch(() => {})
}

const handlePwdLogin = async () => {
  if (!pwdFormRef.value) return
  try {
    await pwdFormRef.value.validate()
    loading.value = true
    const result = await login({
      account: pwdForm.account,
      password: pwdForm.password,
      captchaKey: captchaKey.value,
      captcha: pwdForm.captcha
    })
    handleLoginSuccess(result)
  } catch {
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

// ===== 邮箱验证码登录 =====
const emailFormRef = ref<FormInstance>()
const emailForm = reactive({ email: '', code: '', captcha: '' })
const emailRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' }
  ]
}

const emailCaptchaKey = ref('')
const emailCaptchaImage = ref('')
const emailCodeSending = ref(false)
const emailCodeCountdown = ref(0)
let emailTimer: number | null = null

const loadEmailCaptcha = async () => {
  try {
    const res = await getCaptcha()
    emailCaptchaKey.value = res.captchaKey
    emailCaptchaImage.value = res.captchaImage
  } catch { /* ignored */ }
}

const sendEmailLoginCode = async () => {
  if (!emailForm.email) { ElMessage.warning('请先输入邮箱地址'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.email)) { ElMessage.warning('邮箱格式不正确'); return }
  if (!emailForm.captcha) { ElMessage.warning('请先输入图形验证码'); return }
  emailCodeSending.value = true
  try {
    await apiSendEmailLoginCode(emailForm.email, emailCaptchaKey.value, emailForm.captcha)
    ElMessage.success('验证码已发送至邮箱')
    emailCodeCountdown.value = 60
    emailTimer = window.setInterval(() => {
      emailCodeCountdown.value--
      if (emailCodeCountdown.value <= 0 && emailTimer) {
        clearInterval(emailTimer); emailTimer = null
      }
    }, 1000)
  } catch { /* ignored */ }
  finally { emailCodeSending.value = false }
}

const handleEmailLogin = async () => {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
    emailLoading.value = true
    const result = await apiEmailCodeLogin({ email: emailForm.email, code: emailForm.code })
    handleLoginSuccess(result)
  } catch {
    loadEmailCaptcha()
  } finally {
    emailLoading.value = false
  }
}

// ===== 忘记密码 =====
const forgotPwdVisible = ref(false)
const resetFormRef = ref<FormInstance>()
const resetForm = reactive({ email: '', code: '', newPassword: '', confirmPassword: '' })
const resetRules: FormRules = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_r: any, v: string, cb: (e?: Error) => void) => v === resetForm.newPassword ? cb() : cb(new Error('两次输入的密码不一致')), trigger: 'blur' }
  ]
}

const resetCodeSending = ref(false)
const resetCodeCountdown = ref(0)
let resetTimer: number | null = null
const resetting = ref(false)

const sendResetCode = async () => {
  if (!resetForm.email) { ElMessage.warning('请先输入注册邮箱'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(resetForm.email)) { ElMessage.warning('邮箱格式不正确'); return }
  resetCodeSending.value = true
  try {
    await apiSendResetCode(resetForm.email)
    ElMessage.success('验证码已发送至邮箱')
    resetCodeCountdown.value = 60
    resetTimer = window.setInterval(() => {
      resetCodeCountdown.value--
      if (resetCodeCountdown.value <= 0 && resetTimer) {
        clearInterval(resetTimer); resetTimer = null
      }
    }, 1000)
  } catch { /* ignored */ }
  finally { resetCodeSending.value = false }
}

const handleResetPassword = async () => {
  if (!resetFormRef.value) return
  try {
    await resetFormRef.value.validate()
    resetting.value = true
    await apiResetPassword({ email: resetForm.email, code: resetForm.code, newPassword: resetForm.newPassword })
    ElMessage.success('密码重置成功，请使用新密码登录')
    forgotPwdVisible.value = false
    activeTab.value = 'password'
  } catch { /* ignored */ }
  finally { resetting.value = false }
}

// ===== 注册 =====
const registerVisible = ref(false)
const registerStep = ref(1)
const registerDirection = ref<'next' | 'prev'>('next')
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  username: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  registerCaptcha: '',
  emailCode: ''
})
const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度 2-20 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/, message: '用户名不能包含特殊字符（如 @）', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  phone: [
    {
      validator: (_r: any, v: string, cb: (e?: Error) => void) => {
        if (v && !/^1\d{10}$/.test(v)) {
          cb(new Error('手机号格式不正确'))
        } else { cb() }
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_r: any, v: string, cb: (e?: Error) => void) => v === registerForm.password ? cb() : cb(new Error('两次输入的密码不一致')), trigger: 'blur' }
  ],
  registerCaptcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { len: 4, message: '验证码为 4 位', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码为 6 位', trigger: 'blur' }
  ]
}

// 注册用的独立图形验证码
const registerCaptchaKey = ref('')
const registerCaptchaImage = ref('')
const loadRegisterCaptcha = async () => {
  try {
    const data = await getCaptcha()
    registerCaptchaKey.value = data.captchaKey
    registerCaptchaImage.value = data.captchaImage
    registerForm.registerCaptcha = ''
  } catch { /* ignored */ }
}

const registerCodeSending = ref(false)
const registerCodeCountdown = ref(0)
let registerTimer: number | null = null

const sendRegisterCode = async () => {
  if (!registerForm.email) { ElMessage.warning('请先输入邮箱地址'); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.email)) { ElMessage.warning('邮箱格式不正确'); return }
  if (!registerForm.registerCaptcha) { ElMessage.warning('请先输入图形验证码'); return }
  registerCodeSending.value = true
  try {
    await apiSendRegisterCode({
      email: registerForm.email,
      captchaKey: registerCaptchaKey.value,
      captcha: registerForm.registerCaptcha
    })
    ElMessage.success('验证码已发送至邮箱')
    registerCodeCountdown.value = 60
    registerTimer = window.setInterval(() => {
      registerCodeCountdown.value--
      if (registerCodeCountdown.value <= 0 && registerTimer) {
        clearInterval(registerTimer); registerTimer = null
      }
    }, 1000)
  } catch { /* ignored */ }
  finally { registerCodeSending.value = false }
}

const registering = ref(false)

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
    registering.value = true
    await apiRegister({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email,
      emailCode: registerForm.emailCode,
      phone: registerForm.phone || undefined
    })
    ElMessage.success('注册成功，请登录')
    registerVisible.value = false
    activeTab.value = 'password'
  } catch { /* ignored */ }
  finally { registering.value = false }
}

// 上一步
const prevStep = () => {
  if (registerStep.value > 1) {
    registerDirection.value = 'prev'
    registerStep.value--
  }
}

// 下一步（校验当前步骤字段）
const nextStep = () => {
  if (!registerFormRef.value) return
  const fieldsToValidate = (() => {
    if (registerStep.value === 1) return ['username', 'email', 'phone']
    if (registerStep.value === 2) return ['password', 'confirmPassword']
    return []
  })()
  registerFormRef.value.validateField(fieldsToValidate, (valid) => {
    if (valid && registerStep.value < 3) {
      registerDirection.value = 'next'
      registerStep.value++
    }
  })
}

// 重置注册对话框状态
const resetRegisterState = () => {
  registerStep.value = 1
  registerForm.username = ''
  registerForm.email = ''
  registerForm.phone = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.registerCaptcha = ''
  registerForm.emailCode = ''
  registerCodeCountdown.value = 0
  if (registerTimer) {
    clearInterval(registerTimer)
    registerTimer = null
  }
}

// ===== 生命周期 =====
onMounted(() => loadCaptcha())
onUnmounted(() => {
  if (emailTimer) clearInterval(emailTimer)
  if (resetTimer) clearInterval(resetTimer)
  if (registerTimer) clearInterval(registerTimer)
})

// ===== 导航 =====
const goToRegister = () => {
  resetRegisterState()
  registerVisible.value = true
}
const goToHome = () => {
  router.push('/home').catch(() => {
    // 如果 router.push 失败（例如路由未就绪），用 location.href 兜底
    window.location.href = '/home'
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* ===== 左侧品牌区 ===== */
.brand-panel {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 100vh;
}

.brand-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  z-index: 0;
}

.brand-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(79,172,254,0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 8s ease-in-out infinite;
}

.brand-bg::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(255,121,121,0.1) 0%, transparent 70%);
  border-radius: 50%;
  animation: float 10s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 48px;
  max-width: 420px;
}

.brand-logo {
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 64px;
  line-height: 1;
  display: inline-block;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 12px 0;
  letter-spacing: 2px;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255,255,255,0.7);
  margin: 0 0 48px 0;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 48px;
  padding: 0 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255,255,255,0.85);
  font-size: 15px;
}

.feature-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
  font-size: 13px;
  font-weight: bold;
  flex-shrink: 0;
}

.brand-footer {
  padding-top: 24px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.footer-slogan {
  margin: 0 0 4px 0;
  color: rgba(255,255,255,0.5);
  font-size: 14px;
  cursor: pointer;
  transition: color 0.25s ease;
}

.footer-slogan:hover {
  color: rgba(255,255,255,0.8);
}

.footer-welcome {
  margin: 0;
  color: rgba(255,255,255,0.9);
  font-size: 18px;
  font-weight: 600;
}

/* ===== 右侧表单区 ===== */
.form-panel {
  width: 520px;
  min-width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  padding: 40px;
}

.form-wrapper {
  width: 100%;
  max-width: 400px;
}

.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px 0;
}

.form-subtitle {
  font-size: 15px;
  color: #8c8c8c;
  margin: 0;
}

/* 自定义标签切换 */
.tab-switch {
  display: flex;
  gap: 8px;
  margin-bottom: 28px;
  background: #f0f2f5;
  border-radius: 10px;
  padding: 4px;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  background: transparent;
  color: #8c8c8c;
  transition: all 0.25s ease;
}

.tab-btn:hover {
  color: #595959;
}

.tab-btn.active {
  background: #fff;
  color: #1a1a2e;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.tab-icon {
  font-size: 16px;
}

/* 表单 - 修复输入框宽度 */
.tab-content {
  min-height: 260px;
}

.input-wrap {
  width: 100%;
}

.input-wrap :deep(.el-input__wrapper) {
  background: #f7f8fa;
  border: none;
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.25s ease;
  width: 100%;
}

.input-wrap :deep(.el-input__wrapper:hover) {
  background: #f0f2f5;
}

.input-wrap :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #409eff;
}

.input-wrap :deep(.el-input__inner) {
  height: 48px;
  font-size: 15px;
}

.input-wrap :deep(.el-input__prefix) {
  margin-right: 8px;
}

.input-wrap :deep(.el-input__prefix-inner) {
  color: #bfbfbf;
}

.captcha-wrap,
.code-wrap {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.captcha-input-wrap,
.code-input-wrap {
  flex: 1;
  min-width: 0;
}

.captcha-input-wrap :deep(.el-input__wrapper),
.code-input-wrap :deep(.el-input__wrapper) {
  background: #f7f8fa;
  border: none;
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.25s ease;
}

.captcha-input-wrap :deep(.el-input__wrapper:hover),
.code-input-wrap :deep(.el-input__wrapper:hover) {
  background: #f0f2f5;
}

.captcha-input-wrap :deep(.el-input__wrapper.is-focus),
.code-input-wrap :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #409eff;
}

.captcha-img {
  width: 140px;
  height: 44px;
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid #e8e8e8;
}

.send-code-btn {
  width: 140px;
  flex-shrink: 0;
  height: 44px;
  border-radius: 8px;
  font-size: 13px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  margin-top: 8px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
}

/* 底部链接 */
.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.footer-link {
  font-size: 14px;
}

.footer-sep {
  color: #d9d9d9;
  font-size: 12px;
}

.home-link {
  text-align: center;
  margin-top: 12px;
}

.home-link-text {
  color: #bfbfbf;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.25s ease;
}

.home-link-text:hover {
  color: #409eff;
}

/* ===== 忘记密码对话框 ===== */
.forgot-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  margin: 0;
  border-bottom: 1px solid #f0f0f0;
}

.forgot-dialog :deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
}

.dialog-desc {
  color: #8c8c8c;
  font-size: 13px;
  margin-bottom: 20px;
  line-height: 1.6;
}

.reset-form :deep(.el-input__wrapper) {
  background: #f7f8fa;
  border: none;
  box-shadow: none;
  border-radius: 8px;
  transition: all 0.25s ease;
}

.reset-form :deep(.el-input__wrapper:hover) {
  background: #f0f2f5;
}

.reset-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1px #409eff;
}

.reset-form .code-wrap {
  display: flex;
  gap: 12px;
  align-items: center;
  width: 100%;
}

.reset-form .code-input-wrap {
  flex: 1;
}

.reset-form .send-code-btn {
  width: 140px;
  flex-shrink: 0;
  height: 40px;
  border-radius: 8px;
  font-size: 13px;
}

.reset-form .submit-btn {
  margin-top: 0;
}

/* ===== 注册对话框 — 质感升级版 ===== */
.register-dialog {
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.2);
}

.register-dialog :deep(.el-dialog__header) {
  display: none;
}

.register-dialog :deep(.el-dialog__body) {
  padding: 0;
}

/* 自定义关闭按钮 — 高可见度 */
.register-close {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  width: 38px;
  height: 38px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.7);
  background: rgba(0,0,0,0.25);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s ease;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.register-close:hover {
  background: rgba(255,255,255,0.9);
  color: #333;
  transform: rotate(90deg) scale(1.1);
  border-color: #fff;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
}

/* 头部 */
.register-header {
  position: relative;
  padding: 32px 32px 24px;
  text-align: center;
  overflow: hidden;
}

.register-header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  z-index: 0;
}

/* 装饰气泡 */
.bubble {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
  z-index: 1;
  animation: bubbleFloat 6s ease-in-out infinite;
}

.b1 { width: 140px; height: 140px; top: -40px; left: -30px; animation-delay: 0s; }
.b2 { width: 100px; height: 100px; bottom: -30px; right: 10px; animation-delay: 2s; }
.b3 { width: 60px; height: 60px; top: 10px; right: 60px; animation-delay: 4s; }

@keyframes bubbleFloat {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-15px) scale(1.05); }
}

/* Logo 图标 */
.register-logo {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(255,255,255,0.18);
  backdrop-filter: blur(8px);
  margin-bottom: 14px;
  animation: logoReveal 0.6s ease-out;
}

@keyframes logoReveal {
  0% { opacity: 0; transform: scale(0.6) translateY(10px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}

.register-header-text {
  position: relative;
  z-index: 2;
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px 0;
  letter-spacing: 1px;
}

.register-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  margin: 0;
  font-weight: 400;
}

/* 主体 */
.register-body {
  padding: 0;
}

/* ===== 步骤条 ===== */
.step-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px 28px 16px;
}

.step-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #d9d9d9;
  background: #fff;
  transition: all 0.35s ease;
  flex-shrink: 0;
}

.step-dot.active {
  border-color: #4facfe;
  background: #eef7ff;
  box-shadow: 0 2px 8px rgba(79,172,254,0.25);
  animation: dotPulse 2s ease-in-out infinite;
}

.step-dot.done {
  border-color: #52c41a;
  background: #52c41a;
  animation: dotDone 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes dotPulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(79,172,254,0.25); }
  50% { box-shadow: 0 2px 16px rgba(79,172,254,0.45); }
}

@keyframes dotDone {
  0% { transform: scale(0.8); }
  50% { transform: scale(1.15); }
  100% { transform: scale(1); }
}

.step-num {
  font-size: 13px;
  font-weight: 700;
  color: #bfbfbf;
  transition: color 0.35s ease;
}

.step-dot.active .step-num {
  color: #4facfe;
}

.step-dot.done .step-num {
  color: #fff;
}

.step-track {
  flex: 1;
  height: 3px;
  background: #e8e8e8;
  margin: 0 10px;
  border-radius: 2px;
  overflow: hidden;
}

.step-track-fill {
  height: 100%;
  background: linear-gradient(90deg, #4facfe, #00f2fe);
  border-radius: 2px;
  transition: width 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* ===== 步骤面板 ===== */
.step-panel {
  padding: 8px 28px 0;
}

/* 方向感知的步骤切换过渡（前进） */
.step-slide-forward-enter-active,
.step-slide-forward-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.step-slide-forward-enter-from {
  opacity: 0;
  transform: translateX(30px);
}
.step-slide-forward-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 方向感知的步骤切换过渡（后退） */
.step-slide-backward-enter-active,
.step-slide-backward-leave-active {
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.step-slide-backward-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.step-slide-backward-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.step-panel-icon {
  font-size: 28px;
  line-height: 1;
  margin-bottom: 8px;
}

.step-panel-title {
  font-size: 17px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.step-panel-desc {
  font-size: 13px;
  color: #909399;
  margin-bottom: 18px;
}

.form-group-fields :deep(.el-form-item) {
  margin-bottom: 14px;
}

.form-group-fields :deep(.el-input__wrapper) {
  background: #f5f7fa;
  border: 1.5px solid transparent;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.04);
  border-radius: 10px;
  transition: all 0.3s ease;
  padding: 0 14px;
}

.form-group-fields :deep(.el-input__wrapper:hover) {
  background: #eef1f6;
  border-color: #e4e7ed;
}

.form-group-fields :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  border-color: #4facfe;
  box-shadow: 0 0 0 3px rgba(79,172,254,0.12), inset 0 1px 2px rgba(0,0,0,0.02);
}

.form-group-fields :deep(.el-input__inner) {
  height: 44px;
  font-size: 14px;
}

.form-group-fields :deep(.el-input__prefix) {
  margin-right: 8px;
}

.form-group-fields :deep(.el-input__prefix-inner) {
  color: #bfbfbf;
  transition: color 0.3s ease;
}

.form-group-fields :deep(.el-input__wrapper.is-focus .el-input__prefix-inner) {
  color: #4facfe;
}

/* 表单字段入场交错 */
.stagger-item {
  animation: staggerFadeIn 0.4s ease-out both;
  animation-delay: calc(var(--order) * 0.08s);
}

@keyframes staggerFadeIn {
  0% { opacity: 0; transform: translateY(12px); }
  100% { opacity: 1; transform: translateY(0); }
}

.form-group-fields .captcha-wrap,
.form-group-fields .code-wrap {
  display: flex;
  gap: 10px;
  align-items: center;
  width: 100%;
}

.form-group-fields .captcha-input-wrap,
.form-group-fields .code-input-wrap {
  flex: 1;
  min-width: 0;
}

.form-group-fields .captcha-img {
  width: 140px;
  height: 42px;
  border-radius: 10px;
  cursor: pointer;
  flex-shrink: 0;
  border: 1.5px solid #e8e8e8;
  transition: transform 0.2s ease, border-color 0.2s ease;
  object-fit: cover;
}

.form-group-fields .captcha-img:hover {
  transform: scale(1.04);
  border-color: #4facfe;
}

/* 发送验证码按钮 */
.form-group-fields .send-code-btn {
  width: 140px;
  flex-shrink: 0;
  height: 42px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  background: linear-gradient(135deg, #4facfe 0%, #00c6fb 100%);
  color: #fff;
  border: none;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.form-group-fields .send-code-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #00c6fb 0%, #4facfe 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.form-group-fields .send-code-btn:hover:not(.is-disabled) {
  color: #fff;
  box-shadow: 0 4px 14px rgba(79,172,254,0.4);
  transform: translateY(-1px);
}

.form-group-fields .send-code-btn:hover::before {
  opacity: 1;
}

.form-group-fields .send-code-btn span {
  position: relative;
  z-index: 1;
}

.form-group-fields .send-code-btn.is-disabled {
  background: #e8e8e8 !important;
  color: #bfbfbf !important;
  opacity: 1;
  box-shadow: none !important;
  transform: none !important;
}

/* 底部 */
.register-footer {
  padding: 20px 28px 28px;
}

.step-nav {
  display: flex;
  gap: 12px;
}

.step-prev-btn {
  flex: 0 0 110px;
  height: 48px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  border: 1.5px solid #d9d9d9;
  color: #606266;
  background: #fff;
  transition: all 0.3s ease;
}

.step-prev-btn:hover {
  border-color: #4facfe;
  color: #4facfe;
  background: #f5faff;
}

.step-next-btn {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
  border: none;
  letter-spacing: 3px;
  box-shadow: 0 4px 14px rgba(79,172,254,0.35);
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.step-next-btn:hover {
  color: #fff;
  box-shadow: 0 6px 22px rgba(79,172,254,0.5);
  transform: translateY(-1px);
}

.register-submit-btn {
  flex: 1;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
  border: none;
  letter-spacing: 4px;
  box-shadow: 0 4px 18px rgba(79,172,254,0.4);
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.register-submit-btn::after {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 60%);
  opacity: 0;
  transition: opacity 0.5s ease;
}

.register-submit-btn:hover {
  color: #fff;
  box-shadow: 0 8px 28px rgba(79,172,254,0.55);
  transform: translateY(-2px) scale(1.01);
}

.register-submit-btn:hover::after {
  opacity: 1;
}

.register-submit-btn:active {
  transform: translateY(0) scale(0.99);
}

.register-agreement {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  margin: 16px 0 0;
  line-height: 1.6;
}

.register-agreement a {
  color: #4facfe;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.register-agreement a:hover {
  color: #1a8bfe;
  text-decoration: underline;
}

/* 对话框入场动画 */
.register-dialog :deep(.el-overlay-dialog) {
  animation: dialogFadeIn 0.3s ease-out;
}

@keyframes dialogFadeIn {
  0% {
    opacity: 0;
    transform: scale(0.92) translateY(20px);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* ==== 过渡动画 ==== */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* ===== 响应式 ===== */
@media (max-width: 900px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    width: 100%;
    min-width: unset;
    padding: 32px 24px;
  }

  .form-wrapper {
    max-width: 380px;
  }
}

@media (max-width: 480px) {
  .form-panel {
    padding: 24px 16px;
  }

  .form-title {
    font-size: 24px;
  }

  .send-code-btn {
    width: 120px;
    font-size: 12px;
  }

  .register-form {
    padding: 16px 16px 0;
  }

  .register-actions {
    padding: 8px 16px 24px;
  }

  .register-steps {
    padding: 16px 16px 0;
  }

  .step-line {
    width: 30px;
  }
}

/* ===== 减少动效 ===== */
@media (prefers-reduced-motion: reduce) {
  .register-dialog :deep(.el-overlay-dialog) {
    animation: none;
  }
  .register-logo {
    animation: none;
  }
  .bubble {
    animation: none;
  }
  .step-panel {
    animation: none;
  }
  .stagger-item {
    animation: none;
  }
  .step-dot.active {
    animation: none;
  }
  .step-dot.done {
    animation: none;
  }
  .step-slide-forward-enter-active,
  .step-slide-forward-leave-active,
  .step-slide-backward-enter-active,
  .step-slide-backward-leave-active {
    transition-duration: 0.1s;
  }
  .step-slide-forward-enter-from,
  .step-slide-forward-leave-to,
  .step-slide-backward-enter-from,
  .step-slide-backward-leave-to {
    opacity: 1;
    transform: none;
  }
}
</style>