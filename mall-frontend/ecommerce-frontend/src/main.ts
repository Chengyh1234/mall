import './assets/main.css'
import './assets/responsive.css'
import 'element-plus/dist/index.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)

app.mount('#app')

// 应用初始化时预加载用户信息（如果已登录）
const preloadUserInfo = async () => {
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const { useUserStore } = await import('@/stores/user')
      const userStore = useUserStore()
      // 静默预加载，不显示loading，失败也不报错
      await userStore.fetchUserInfo().catch(() => {})
    } catch { /* ignore */ }
  }
}

// 非阻塞式预加载
preloadUserInfo()