<template>
  <main class="forbidden-main">
      <div class="forbidden-card">
        <!-- 锁定图标 -->
        <div class="icon-wrapper">
          <svg class="lock-icon" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="14" y="28" width="36" height="26" rx="4" stroke="currentColor" stroke-width="3.5" stroke-linejoin="round"/>
            <path d="M24 28V20a8 8 0 0 1 16 0v8" stroke="currentColor" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
            <circle cx="32" cy="40" r="3" fill="currentColor"/>
            <rect x="30" y="40" width="4" height="7" rx="1" fill="currentColor"/>
          </svg>
        </div>

        <!-- 状态码 -->
        <span class="status-code">403</span>

        <!-- 标题 -->
        <h1 class="title">权限不足</h1>

        <!-- 描述 -->
        <p class="description">
          抱歉，您没有访问此页面的权限。<br />
          请联系管理员获取相应权限，或返回首页继续浏览。
        </p>

        <!-- 操作按钮 -->
        <div class="actions">
          <el-button
            type="primary"
            size="large"
            class="action-btn"
            @click="goHome"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 12l2-2m0 0l7-7 7 7m-9 2v8m-4-6v6a1 1 0 001 1h3m8-8l2 2m-2-2V5a1 1 0 00-1-1h-2"/>
            </svg>
            返回首页
          </el-button>
          <el-button
            size="large"
            class="action-btn"
            @click="goLogin"
            v-if="!isLoggedIn"
          >
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4M10 17l5-5-5-5M13 12H3"/>
            </svg>
            重新登录
          </el-button>
        </div>
      </div>
    </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)

function goHome() {
  router.push('/home')
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.forbidden-main {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: #f8f9fa;
}

/* 卡片 */
.forbidden-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 480px;
  width: 100%;
}

/* 图标 */
.icon-wrapper {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  background: #fef2f2;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.lock-icon {
  width: 48px;
  height: 48px;
  color: #dc2626;
}

/* 状态码 */
.status-code {
  font-size: clamp(2.5rem, 6vw, 3.75rem);
  font-weight: 800;
  line-height: 1;
  color: #dc2626;
  letter-spacing: -0.03em;
  margin-bottom: 8px;
}

/* 标题 */
.title {
  font-size: clamp(1.25rem, 3vw, 1.5rem);
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px;
  text-wrap: balance;
}

/* 描述 */
.description {
  font-size: 1rem;
  line-height: 1.6;
  color: #6b7280;
  margin: 0 0 36px;
  max-width: 38ch;
}

/* 操作按钮 */
.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 140px;
}

/* 响应式：移动端按钮满宽 */
@media (max-width: 480px) {
  .actions {
    flex-direction: column;
    width: 100%;
  }

  .action-btn {
    width: 100%;
  }
}
</style>