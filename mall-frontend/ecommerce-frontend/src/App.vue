<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
</script>

<template>
  <ElConfigProvider :locale="zhCn">
    <RouterView />
  </ElConfigProvider>
</template>

<style>
html {
  scrollbar-gutter: stable;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  font-size: 14px;
  color: #333;
  background-color: #f5f5f5;
  min-width: 320px;
}

#app {
  min-height: 100vh;
  width: 100%;
}

/* 响应式容器 */
.container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* PC端样式 */
@media (min-width: 1024px) {
  .container {
    padding: 0 40px;
  }
}

/* 平板端样式 */
@media (max-width: 1023px) and (min-width: 768px) {
  .container {
    padding: 0 30px;
  }
}

/* 移动端样式 */
@media (max-width: 767px) {
  .container {
    padding: 0 15px;
  }
}
</style>
