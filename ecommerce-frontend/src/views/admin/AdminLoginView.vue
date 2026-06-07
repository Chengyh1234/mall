<template>
  <div class="admin-login-container">
    <div class="admin-login-overlay"></div>
    <div class="admin-login-box">
      <div class="admin-login-sidebar">
        <div class="sidebar-brand">
          <div class="brand-icon">⚙️</div>
          <h2>运营管理中心</h2>
          <p>电商平台后台管理系统</p>
        </div>
        <div class="sidebar-features">
          <div class="feature-item">
            <span class="feature-icon">📦</span>
            <span>商品管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📋</span>
            <span>订单管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🏪</span>
            <span>店铺管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">👥</span>
            <span>用户管理</span>
          </div>
        </div>
      </div>
      <div class="admin-login-form">
        <div class="form-header">
          <h2>管理员登录</h2>
          <p>请输入您的管理员账号和密码</p>
        </div>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="管理员用户名"
              size="large"
              :prefix-icon="UserIcon"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="管理员密码"
              size="large"
              :prefix-icon="LockIcon"
              show-password
            />
          </el-form-item>
          <el-form-item prop="captcha">
            <div class="captcha-wrapper">
              <el-input
                v-model="loginForm.captcha"
                placeholder="验证码"
                size="large"
                :prefix-icon="KeyIcon"
                maxlength="4"
                class="captcha-input"
              />
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
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '验证中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
        <div class="form-footer">
          <span class="hint-text">该页面仅限管理员访问</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { adminLogin, getCaptcha } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const UserIcon = h(User)
const LockIcon = h(Lock)
const KeyIcon = h(Key)

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const captchaKey = ref('')
const captchaImage = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入管理员用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入管理员密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码为4位', trigger: 'blur' }
  ]
}

const loadCaptcha = async () => {
  try {
    const data = await getCaptcha()
    captchaKey.value = data.captchaKey
    captchaImage.value = data.captchaImage
  } catch {
    // 拦截器已处理
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const result = await adminLogin({
      account: loginForm.username,
      password: loginForm.password,
      captchaKey: captchaKey.value,
      captcha: loginForm.captcha
    })

    // 保存token
    userStore.setToken(result.token)

    // 处理角色信息
    let roles: Array<{ id: number | null; name: string; code: string; description: string | null; status: string | null; createdAt: string | null; updatedAt: string | null }> = []
    if (result.roles && result.roles.length > 0) {
      if (typeof result.roles[0] === 'string') {
        roles = result.roles.map((code: string) => ({
          id: null,
          name: code,
          code: code,
          description: null,
          status: null,
          createdAt: null,
          updatedAt: null
        }))
      } else {
        roles = result.roles as any
      }
    }

    userStore.setUserInfo({
      id: result.userId,
      username: result.username,
      roles: roles,
      permissions: result.permissions
    })

    // 验证是否为管理员角色
    if (!userStore.isAdmin) {
      // 非管理员登录，清除token并提示
      userStore.clearToken()
      ElMessage.error('该账号没有管理权限，请使用管理员账号登录')
      loading.value = false
      return
    }

    ElMessage.success('管理员登录成功')
    router.push('/admin')
  } catch {
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.admin-login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #1a1a2e;
  position: relative;
  overflow: hidden;
}

.admin-login-overlay {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(ellipse at 30% 50%, rgba(64, 158, 255, 0.08) 0%, transparent 60%),
              radial-gradient(ellipse at 70% 50%, rgba(103, 58, 183, 0.08) 0%, transparent 60%);
  pointer-events: none;
}

.admin-login-box {
  display: flex;
  width: 800px;
  min-height: 520px;
  background: #16213e;
  border-radius: 16px;
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.5);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.admin-login-sidebar {
  width: 320px;
  background: linear-gradient(135deg, #0f3460 0%, #1a1a2e 100%);
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-brand {
  margin-bottom: 48px;
}

.brand-icon {
  font-size: 40px;
  margin-bottom: 16px;
}

.sidebar-brand h2 {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.sidebar-brand p {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
  margin: 0;
}

.sidebar-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.04);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

.feature-icon {
  font-size: 18px;
}

.admin-login-form {
  flex: 1;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  color: #e0e0e0;
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.form-header p {
  color: rgba(255, 255, 255, 0.4);
  font-size: 14px;
  margin: 0;
}

.login-form {
  max-width: 360px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: none;
  border-radius: 8px;
  padding: 4px 16px;
  transition: all 0.3s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(64, 158, 255, 0.4);
  background: rgba(255, 255, 255, 0.08);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 1px rgba(64, 158, 255, 0.3);
}

.login-form :deep(.el-input__inner) {
  color: #e0e0e0;
  height: 44px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.3);
}

.login-form :deep(.el-input__prefix-inner) {
  color: rgba(255, 255, 255, 0.3);
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  border-radius: 8px;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  border: none;
  margin-top: 8px;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(64, 158, 255, 0.35);
}

.captcha-wrapper {
  display: flex;
  width: 100%;
  gap: 12px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-img {
  width: 120px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.form-footer {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.hint-text {
  color: rgba(255, 255, 255, 0.25);
  font-size: 12px;
}

@media screen and (max-width: 860px) {
  .admin-login-box {
    flex-direction: column;
    width: 90%;
    min-height: auto;
  }

  .admin-login-sidebar {
    width: 100%;
    padding: 32px 24px;
  }

  .admin-login-form {
    padding: 32px 24px;
  }

  .login-form {
    max-width: 100%;
  }
}
</style>