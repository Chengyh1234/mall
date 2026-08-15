<template>
  <div class="cs-orders">
    <div class="page-header">
      <h1>订单查询</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索订单号/用户名/手机号" style="width: 300px;" />
      <el-select v-model="statusFilter" placeholder="选择状态">
        <el-option label="全部" value="" />
        <el-option label="待付款" value="pending" />
        <el-option label="待发货" value="unshipped" />
        <el-option label="已发货" value="shipped" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <el-table :data="orders" border>
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="buyerName" label="买家" />
      <el-table-column prop="buyerPhone" label="联系电话" />
      <el-table-column prop="sellerName" label="卖家" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column prop="price" label="金额" />
      <el-table-column prop="status" label="状态">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="viewOrder(scope.row)">查看详情</el-button>
          <el-button size="small" type="primary" @click="contactBuyer(scope.row)">联系买家</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const searchKeyword = ref('')
const statusFilter = ref('')

const orders = ref<any[]>([])

const getStatusType = (status: string) => {
  const types: Record<string, string> = {
    pending: 'warning',
    unshipped: 'primary',
    shipped: 'info',
    completed: 'success',
    cancelled: 'danger'
  }
  return types[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    pending: '待付款',
    unshipped: '待发货',
    shipped: '已发货',
    completed: '已完成',
    cancelled: '已取消'
  }
  return texts[status] || status
}

const handleSearch = () => {
  ElMessage.info('搜索功能开发中')
}

const viewOrder = (row: any) => {
  ElMessage.info(`查看订单: ${row.orderNo}`)
}

const contactBuyer = (row: any) => {
  ElMessage.info(`联系买家: ${row.buyerName}`)
}
</script>

<style scoped>
.cs-orders {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
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
