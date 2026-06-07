<template>
  <div class="admin-users">
    <div class="page-header">
      <h1>用户管理</h1>
      <el-button type="primary">添加用户</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索用户名" style="width: 300px;" />
      <el-select v-model="roleFilter" placeholder="选择角色">
        <el-option label="全部" value="" />
        <el-option label="普通用户" value="ROLE_USER" />
        <el-option label="卖家" value="ROLE_SELLER" />
        <el-option label="运营管理员" value="ROLE_OPERATOR" />
        <el-option label="超级用户" value="ROLE_ADMIN" />
        <el-option label="客服" value="ROLE_CUSTOMER_SERVICE" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="users" border>
      <el-table-column prop="id" label="ID" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="roles" label="角色">
        <template #default="scope">
          <el-tag v-for="role in scope.row.roles" :key="role" type="info" size="small">
            {{ getRoleText(role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small">编辑</el-button>
          <el-button :type="scope.row.status === 1 ? 'danger' : 'success'" size="small" 
            @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UserRole } from '@/stores/user'

const searchKeyword = ref('')
const roleFilter = ref('')

const users = ref<any[]>([])

const getRoleText = (role: string) => {
  const texts: Record<string, string> = {
    [UserRole.USER]: '普通用户',
    [UserRole.SELLER]: '卖家',
    [UserRole.OPERATOR]: '运营管理员',
    [UserRole.ADMIN]: '超级用户',
    [UserRole.CUSTOMER_SERVICE]: '客服'
  }
  return texts[role] || role
}

const handleSearch = () => {
  ElMessage.info('搜索功能开发中')
}

const toggleStatus = (row: any) => {
  row.status = row.status === 1 ? 0 : 1
  ElMessage.success(row.status === 1 ? '用户已启用' : '用户已禁用')
}
</script>

<style scoped>
.admin-users {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.search-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  align-items: center;
}
</style>
