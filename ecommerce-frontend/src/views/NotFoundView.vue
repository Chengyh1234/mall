<template>
  <main class="not-found-main">
    <div class="not-found-card">
      <!-- 404 图标 -->
      <div class="icon-wrapper">
        <svg class="search-icon" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="28" cy="28" r="16" stroke="currentColor" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M40 40l10 10" stroke="currentColor" stroke-width="3.5" stroke-linecap="round" stroke-linejoin="round"/>
          <line x1="22" y1="28" x2="34" y2="28" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
        </svg>
      </div>

      <!-- 状态码 -->
      <span class="status-code">404</span>

      <!-- 标题 -->
      <h1 class="title">页面未找到</h1>

      <!-- 描述：展示后端返回的具体错误信息 -->
      <p class="description">
        {{ errorMessage }}
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
          @click="goBack"
        >
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          返回上一页
        </el-button>
      </div>
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const errorMessage = computed(() => {
  return (route.query.message as string) || '抱歉，您请求的资源不存在。'
})

function goHome() {
  router.push('/home')
}

function goBack() {
  router.back()
}
</script>

<style scoped>
.not-found-main {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  background: #f8f9fa;
}

/* 卡片 */
.not-found-card {
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
  background: #fff7ed;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
}

.search-icon {
  width: 48px;
  height: 48px;
  color: #ea580c;
}

/* 状态码 */
.status-code {
  font-size: clamp(2.5rem, 6vw, 3.75rem);
  font-weight: 800;
  line-height: 1;
  color: #ea580c;
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