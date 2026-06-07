<template>
  <div class="test-container">
    <h1>电商平台测试页面</h1>
    <p>项目已成功启动！</p>
    
    <div class="features">
      <h2>已实现的功能：</h2>
      <ul>
        <li>Vue3 + Vite项目搭建</li>
        <li>Element Plus UI组件库集成</li>
        <li>Axios封装和请求拦截器</li>
        <li>Vue Router路由配置</li>
        <li>Pinia状态管理</li>
        <li>登录页面</li>
        <li>首页</li>
        <li>个人中心页面</li>
        <li>路由守卫（登录验证）</li>
        <li>Vite代理配置</li>
      </ul>
    </div>
    
    <div class="links">
      <h2>页面链接：</h2>
      <el-button type="primary" @click="goToLogin">登录页面</el-button>
      <el-button type="success" @click="goToHome">首页</el-button>
      <el-button type="info" @click="goToProfile">个人中心</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToLogin = () => {
  router.push('/login')
}

const goToHome = () => {
  router.push('/home')
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.test-container {
  max-width: 800px;
  margin: 50px auto;
  padding: 30px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  text-align: center;
}

h1 {
  color: #333;
  margin-bottom: 20px;
}

p {
  color: #666;
  margin-bottom: 30px;
  font-size: 18px;
}

.features {
  text-align: left;
  margin: 30px 0;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.features h2 {
  color: #333;
  margin-bottom: 15px;
}

.features ul {
  padding-left: 20px;
}

.features li {
  margin: 8px 0;
  color: #555;
}

.links {
  margin-top: 30px;
}

.links h2 {
  margin-bottom: 20px;
  color: #333;
}

.links .el-button {
  margin: 0 10px;
}
</style>