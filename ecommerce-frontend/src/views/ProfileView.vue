<template>
  <div class="profile-container">
    <NavBar />

    <div class="profile-content">
      <!-- 侧边栏菜单 -->
      <el-aside class="sidebar" width="250px">
        <el-menu
          :default-active="activeSideMenu"
          class="side-menu"
          @select="handleSideMenuSelect"
        >
          <div class="user-info">
            <el-popover
              placement="right-start"
              :width="160"
              trigger="click"
            >
              <template #reference>
                <div class="avatar-wrapper" title="点击查看更多">
                  <el-avatar :size="80" :src="avatarUrl" />
                </div>
              </template>
              <div class="avatar-popover-menu">
                <div class="popover-item" @click="handleEditProfile">
                  <el-icon><Edit /></el-icon>
                  <span>编辑个人信息</span>
                </div>
              </div>
            </el-popover>
            <div class="user-details">
              <h3>{{ userInfo?.username || '用户' }}</h3>
            </div>
          </div>
          
          <!-- 普通用户菜单（所有登录用户都有） -->
          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          
          <el-menu-item index="cart">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的购物车</span>
          </el-menu-item>
          
          <el-sub-menu index="account">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>账号设置</span>
            </template>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="logout" class="logout-item">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div v-loading="loading" class="content-box">
          <!-- 个人资料 -->
          <div v-if="activeSideMenu === 'info'" class="info-section">
            <h3>个人资料</h3>
            <!-- 头像修改 -->
            <div class="avatar-upload-section">
              <div class="avatar-upload-wrapper" @click="handleAvatarClick" title="点击更换头像">
                <el-avatar :size="80" :src="avatarUrl" />
                <div class="avatar-upload-overlay">
                  <el-icon><Camera /></el-icon>
                  <span>更换头像</span>
                </div>
              </div>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                style="display: none"
                @change="handleAvatarChange"
              />
            </div>
            <el-form v-if="userInfo" :model="userInfo" label-width="120px" class="info-form">
              <el-form-item label="用户ID">
                <el-input v-model="userInfo.id" disabled />
              </el-form-item>
              
              <el-form-item label="用户名">
                <el-input v-model="userInfo.username" disabled />
              </el-form-item>
              
              <el-form-item label="账号状态">
                <el-input :value="userInfo.status === 1 ? '正常' : '禁用'" disabled />
              </el-form-item>
              
              <el-form-item label="最后登录时间">
                <el-input v-model="userInfo.lastLoginTime" disabled />
              </el-form-item>
              
              <el-form-item label="注册时间">
                <el-input v-model="userInfo.createdAt" disabled />
              </el-form-item>
            </el-form>
          </div>

          <!-- 我的订单 -->
          <div v-else-if="activeSideMenu === 'orders'" class="orders-section">
            <div class="order-header">
              <h3>我的订单</h3>
              <el-tabs v-model="activeOrderTab" type="card" class="order-tabs">
                <el-tab-pane label="全部" name="all">全部</el-tab-pane>
                <el-tab-pane label="待付款" name="pending_pay">待付款</el-tab-pane>
                <el-tab-pane label="待发货" name="pending_ship">待发货</el-tab-pane>
                <el-tab-pane label="待收货" name="pending_receive">待收货</el-tab-pane>
                <el-tab-pane label="已完成" name="completed">已完成</el-tab-pane>
                <el-tab-pane label="已取消" name="cancelled">已取消</el-tab-pane>
              </el-tabs>
            </div>
            
            <div v-if="orders.length > 0" class="order-list">
              <div 
                v-for="order in orders" 
                :key="order.id" 
                class="order-card"
              >
                <div class="order-header-bar">
                  <span class="order-no">订单号：{{ order.orderNo }}</span>
                  <span class="order-status" :class="getStatusClass(order.status)">
                    {{ getStatusText(order.status) }}
                  </span>
                </div>
                
                <div class="order-address">
                  <div class="address-info">
                    <span class="address-label">收货信息：</span>
                    <span>{{ order.receiverName }} {{ order.receiverPhone }}</span>
                    <span class="address-detail">{{ order.receiverAddress }}</span>
                  </div>
                </div>
                
                <div class="order-footer">
                  <div class="order-total">
                    合计：
                    <span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
                  </div>
                  <div class="order-actions">
                    <template v-if="order.status === 1">
                      <el-button type="primary" @click="handlePay(order.id)">立即支付</el-button>
                      <el-button type="text" @click="showCancelDialog(order)">取消订单</el-button>
                    </template>
                    <template v-else-if="order.status === 3">
                      <el-button type="primary" @click="handleReceive(order.id)">确认收货</el-button>
                    </template>
                    <template v-else-if="order.status === 2">
                      <span class="waiting-text">等待卖家发货...</span>
                    </template>
                    <template v-else-if="order.status === 5">
                      <span class="cancelled-text">{{ order.cancelReason || '订单已取消' }}</span>
                    </template>
                    <template v-else-if="order.status === 4">
                      <span class="completed-text">订单已完成</span>
                    </template>
                  </div>
                </div>

                <div v-if="order.status === 3 && order.deliveryCompany" class="shipping-info">
                  <div class="shipping-header">
                    <span class="shipping-icon">📦</span>
                    <span>物流信息</span>
                  </div>
                  <div class="shipping-detail">
                    <span>快递公司：{{ order.deliveryCompany }}</span>
                    <span>运单号：{{ order.deliveryNo }}</span>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-orders">
              <el-empty description="暂无订单">
                <el-button type="primary" @click="goHome">去购物</el-button>
              </el-empty>
            </div>
          </div>

          <!-- 收货地址 -->
          <div v-else-if="activeSideMenu === 'address'" class="address-section">
            <div class="section-header">
              <h3>收货地址</h3>
              <el-button 
                type="primary" 
                size="small" 
                @click="showAddAddressModal = true"
                :disabled="addresses.length >= 10"
              >
                添加地址
              </el-button>
            </div>
            <div v-if="addresses.length > 0" class="address-list">
              <div 
                v-for="addr in addresses" 
                :key="addr.id" 
                class="address-card"
              >
                <div class="address-header">
                  <div class="address-user">
                    <span class="name">{{ addr.receiverName }}</span>
                    <span class="phone">{{ addr.receiverPhone }}</span>
                  </div>
                  <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
                </div>
                <div class="address-detail">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                </div>
                <div class="address-actions">
                  <el-button 
                    v-if="addr.isDefault !== 1" 
                    size="small" 
                    @click="setDefault(addr.id)"
                  >
                    设为默认
                  </el-button>
                  <el-button size="small" @click="editAddress(addr)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteAddressHandler(addr.id)">删除</el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-address">
              <el-empty description="暂无收货地址">
                <el-button type="primary" @click="showAddAddressModal = true">添加地址</el-button>
              </el-empty>
            </div>
            <p v-if="addresses.length >= 10" class="address-limit">* 最多可添加10个收货地址</p>
          </div>

          <!-- 安全设置 -->
          <div v-else-if="activeSideMenu === 'security'" class="security-section">
            <h3>安全设置</h3>
            <div class="security-list">
              <div class="security-item">
                <div class="security-icon">🔐</div>
                <div class="security-info">
                  <h4>修改密码</h4>
                  <p>定期更换密码，保护账户安全</p>
                </div>
                <el-button type="primary" size="small" @click="showChangePasswordModal = true">修改</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">📱</div>
                <div class="security-info">
                  <h4>修改手机号</h4>
                  <p>{{ userInfo?.phone || '未绑定' }}</p>
                </div>
                <el-button type="primary" size="small" @click="showChangePhoneModal = true">修改</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">📧</div>
                <div class="security-info">
                  <h4>修改邮箱</h4>
                  <p>{{ userInfo?.email || '未绑定' }}</p>
                </div>
                <el-button type="primary" size="small" @click="showChangeEmailModal = true">修改</el-button>
              </div>
            </div>
          </div>

          <!-- 默认显示 -->
          <div v-else class="default-section">
            <h3>欢迎来到个人中心</h3>
            <p>请从左侧菜单选择要查看的内容</p>
          </div>
        </div>
      </el-main>
    </div>

    <!-- 添加/编辑地址弹窗 -->
    <el-dialog 
      :title="editingAddress ? '编辑地址' : '添加地址'" 
      v-model="showAddAddressModal"
      width="500px"
    >
      <el-form :model="addressForm" label-width="80px" class="address-form">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-cascader 
            v-model="addressForm.region" 
            :options="regionOptions" 
            placeholder="请选择省市区"
            :props="{ checkStrictly: false }"
          />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detailAddress" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault" :true-value="1" :false-value="0">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeAddressModal">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog title="修改密码" v-model="showChangePasswordModal" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input 
            v-model="passwordForm.oldPassword" 
            :type="showOldPassword ? 'text' : 'password'" 
            placeholder="请输入原密码"
          >
            <template #suffix>
              <button 
                type="button" 
                class="password-toggle-btn"
                @click="showOldPassword = !showOldPassword"
              >
                <svg v-if="!showOldPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.06M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input 
            v-model="passwordForm.newPassword" 
            :type="showNewPassword ? 'text' : 'password'" 
            placeholder="请输入新密码"
          >
            <template #suffix>
              <button 
                type="button" 
                class="password-toggle-btn"
                @click="showNewPassword = !showNewPassword"
              >
                <svg v-if="!showNewPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.06M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            :type="showConfirmPassword ? 'text' : 'password'" 
            placeholder="请确认新密码"
          >
            <template #suffix>
              <button 
                type="button" 
                class="password-toggle-btn"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <svg v-if="!showConfirmPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.06M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePasswordModal = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确定</el-button>
      </template>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog title="取消订单" v-model="cancelDialogVisible">
      <div class="cancel-reason">
        <span>取消原因：</span>
        <el-select v-model="cancelReason" placeholder="请选择取消原因">
          <el-option label="不想买了" value="不想买了" />
          <el-option label="商品价格太贵" value="商品价格太贵" />
          <el-option label="等待时间太长" value="等待时间太长" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 修改手机号弹窗 -->
    <el-dialog title="修改手机号" v-model="showChangePhoneModal" width="400px">
      <el-form :model="phoneForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input 
            v-model="phoneForm.password" 
            type="password" 
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新手机号">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePhoneModal = false">取消</el-button>
        <el-button type="primary" @click="changePhone">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改邮箱弹窗 -->
    <el-dialog title="修改邮箱" v-model="showChangeEmailModal" width="400px">
      <el-form :model="emailForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input 
            v-model="emailForm.password" 
            type="password" 
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.newEmail" placeholder="请输入新邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeEmailModal = false">取消</el-button>
        <el-button type="primary" @click="changeEmail">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import {
  User,
  Document,
  ShoppingCart,
  Location,
  Lock,
  SwitchButton,
  Setting,
  Shop,
  Box,
  List,
  Folder,
  Camera,
  Edit
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAddressList, addAddress, updateAddress, deleteAddress as deleteAddressApi, setDefaultAddress, type Address } from '@/api/address'
import { updateUserProfile, updatePassword, uploadAvatar, type UpdateProfileRequest, type UpdatePasswordRequest } from '@/api/user'
import { getOrderList, payOrder, cancelOrder, confirmReceive, type Order } from '@/api/order'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeSideMenu = ref('orders')
const originalUserInfo = ref({})

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 用户头像URL
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return 'https://via.placeholder.com/80'
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
  if (avatar.startsWith('/api/')) return avatar
  // 通过文件预览接口展示：relativePath=文件名 subDir=avatars
  return `/api/file/preview?relativePath=${avatar}&subDir=avatars`
})

// 地址相关
const addresses = ref<Address[]>([])
const showAddAddressModal = ref(false)
const editingAddress = ref<Address | null>(null)

// 头像上传
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadingAvatar = ref(false)

const handleEditProfile = () => {
  activeSideMenu.value = 'info'
}

const handleAvatarClick = () => {
  fileInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 jpg、png、gif、webp 格式的图片')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const res = await uploadAvatar(file)
    await userStore.fetchUserInfo(true)
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

// 地址表单
const addressForm = reactive({
  id: null as number | null,
  receiverName: '',
  receiverPhone: '',
  region: [] as string[],
  detailAddress: '',
  isDefault: 0
})

// 省市区数据（模拟）
const regionOptions = ref([
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '宝安区', label: '宝安区' }
        ]
      },
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' }
        ]
      }
    ]
  },
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '西城区', label: '西城区' }
        ]
      }
    ]
  }
])

// 密码表单
const showChangePasswordModal = ref(false)
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改手机号相关
const showChangePhoneModal = ref(false)
const phoneForm = reactive({
  password: '',
  newPhone: ''
})

// 修改邮箱相关
const showChangeEmailModal = ref(false)
const emailForm = reactive({
  password: '',
  newEmail: ''
})

// 订单相关
const orders = ref<Order[]>([])
const activeOrderTab = ref('all')
const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const currentOrder = ref<Order | null>(null)

// 订单状态映射
const statusMap: Record<number, string> = {
  1: '待付款',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款'
}

const tabStatusMap: Record<string, number> = {
  all: 0,
  pending_pay: 1,
  pending_ship: 2,
  pending_receive: 3,
  completed: 4,
  cancelled: 5
}

// 获取用户信息（优化：优先使用缓存）
const fetchUserInfo = async () => {
  try {
    // 如果用户信息已经存在且不需要刷新，直接使用缓存
    if (userStore.userInfo && !userStore.needRefresh()) {
      originalUserInfo.value = { ...userStore.userInfo }
      return
    }
    
    loading.value = true
    await userStore.fetchUserInfo()
    originalUserInfo.value = { ...userStore.userInfo }
  } catch {
    // 如果有缓存数据，继续使用
    if (!userStore.userInfo) {
      ElMessage.error('获取用户信息失败')
    }
  } finally {
    loading.value = false
  }
}

// 获取地址列表
const loadAddresses = async () => {
  try {
    loading.value = true
    const data = await getAddressList()
    addresses.value = data || []
  } catch {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

// 获取订单列表
const loadOrders = async (status?: number) => {
  loading.value = true
  try {
    const params: { page?: number; pageSize?: number; status?: number } = {
      page: 1,
      pageSize: 20
    }
    if (status !== undefined && status !== 0) {
      params.status = status
    }
    const data = await getOrderList(params)
    orders.value = data || []
  } catch {
    ElMessage.error('获取订单列表失败')
    orders.value = []
  } finally {
    loading.value = false
  }
}

// 获取订单状态样式
const getStatusClass = (status: number) => {
  const classMap: Record<number, string> = {
    1: 'status-pending',
    2: 'status-processing',
    3: 'status-processing',
    4: 'status-success',
    5: 'status-cancelled',
    6: 'status-warning',
    7: 'status-info'
  }
  return classMap[status] || ''
}

// 获取订单状态文本
const getStatusText = (status: number) => {
  return statusMap[status] || String(status)
}

// 侧边菜单选择处理
const handleSideMenuSelect = (index: string) => {
  if (index === 'logout') {
    handleLogout()
  } else if (index === 'cart') {
    router.push('/cart')
  } else {
    activeSideMenu.value = index
    if (index === 'address') {
      loadAddresses()
    } else if (index === 'orders') {
      loadOrders()
    }
  }
}

// 更新用户信息
const updateUserInfo = async () => {
  if (!userInfo.value) return
  
  try {
    loading.value = true
    const updateData: UpdateProfileRequest = {}
    
    if (userInfo.value.email !== (originalUserInfo.value as any)?.email) {
      updateData.email = userInfo.value.email
    }
    if (userInfo.value.phone !== (originalUserInfo.value as any)?.phone) {
      updateData.phone = userInfo.value.phone
    }
    
    if (Object.keys(updateData).length > 0) {
      const response = await updateUserProfile(updateData)
      await userStore.fetchUserInfo()
      ElMessage.success('个人信息更新成功')
      originalUserInfo.value = { ...userStore.userInfo }
    } else {
      ElMessage.info('没有修改任何信息')
    }
  } catch {
    ElMessage.error('更新失败')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  userStore.setUserInfo({ ...originalUserInfo.value })
  ElMessage.info('已重置')
}

// 退出登录
const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
  }
}

// 打开添加地址弹窗
const showAddAddressModalHandler = () => {
  editingAddress.value = null
  addressForm.id = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.region = []
  addressForm.detailAddress = ''
  addressForm.isDefault = 0
  showAddAddressModal.value = true
}

// 编辑地址
const editAddress = (addr: Address) => {
  editingAddress.value = addr
  addressForm.id = addr.id
  addressForm.receiverName = addr.receiverName
  addressForm.receiverPhone = addr.receiverPhone
  addressForm.region = [addr.province, addr.city, addr.district]
  addressForm.detailAddress = addr.detailAddress
  addressForm.isDefault = addr.isDefault
  showAddAddressModal.value = true
}

// 关闭地址弹窗
const closeAddressModal = () => {
  showAddAddressModal.value = false
  editingAddress.value = null
}

// 保存地址
const saveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.region.length || !addressForm.detailAddress) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    loading.value = true
    const data = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.region[0] || '',
      city: addressForm.region[1] || '',
      district: addressForm.region[2] || '',
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault
    }

    if (editingAddress.value) {
      await updateAddress({
        ...data,
        id: editingAddress.value.id
      })
      ElMessage.success('地址更新成功')
    } else {
      await addAddress(data)
      ElMessage.success('地址添加成功')
    }
    
    closeAddressModal()
    loadAddresses()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}

// 设置默认地址
const setDefault = async (id: number) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch {
    ElMessage.error('设置失败')
  }
}

// 删除地址
const deleteAddressHandler = async (id: number) => {
  try {
    await deleteAddressApi(id)
    ElMessage.success('地址已删除')
    loadAddresses()
  } catch {
    ElMessage.error('删除失败')
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少为6位')
    return
  }

  try {
    loading.value = true
    const requestData: UpdatePasswordRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }
    await updatePassword(requestData)
    ElMessage.success('密码修改成功')
    showChangePasswordModal.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch {
    passwordForm.oldPassword = ''
  } finally {
    loading.value = false
  }
}

// 修改手机号
const changePhone = async () => {
  if (!phoneForm.password || !phoneForm.newPhone) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    loading.value = true
    await updateUserProfile({ 
      phone: phoneForm.newPhone,
      password: phoneForm.password
    })
    ElMessage.success('手机号修改成功')
    showChangePhoneModal.value = false
    phoneForm.password = ''
    phoneForm.newPhone = ''
    await userStore.fetchUserInfo()
  } catch {
    phoneForm.password = ''
  } finally {
    loading.value = false
  }
}

// 修改邮箱
const changeEmail = async () => {
  if (!emailForm.password || !emailForm.newEmail) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  try {
    loading.value = true
    await updateUserProfile({ 
      email: emailForm.newEmail,
      password: emailForm.password
    })
    ElMessage.success('邮箱修改成功')
    showChangeEmailModal.value = false
    emailForm.password = ''
    emailForm.newEmail = ''
    await userStore.fetchUserInfo()
  } catch {
    emailForm.password = ''
  } finally {
    loading.value = false
  }
}

// 订单支付
const handlePay = async (orderId: number) => {
  try {
    await payOrder(orderId)
    ElMessage.success('支付成功')
    loadOrders()
  } catch {
    ElMessage.error('支付失败')
  }
}

// 显示取消订单弹窗
const showCancelDialog = (order: Order) => {
  currentOrder.value = order
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请选择取消原因')
    return
  }
  
  if (!currentOrder.value) return
  
  try {
    await cancelOrder(currentOrder.value.id)
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch {
    ElMessage.error('取消订单失败')
  }
}

// 确认收货
const handleReceive = async (orderId: number) => {
  try {
    await confirmReceive(orderId)
    ElMessage.success('收货成功')
    loadOrders()
  } catch {
    ElMessage.error('收货失败')
  }
}

// 跳转到首页
const goHome = () => {
  router.push('/home');
};

// 监听订单标签切换
watch(activeOrderTab, () => {
  const targetStatus = tabStatusMap[activeOrderTab.value] || 0
  loadOrders(targetStatus === 0 ? undefined : targetStatus)
})

// 组件挂载时获取数据（优化：仅在需要时刷新）
onMounted(() => {
  // 默认展示我的订单，加载订单数据
  loadOrders()
  // 如果已有用户信息，直接使用；否则异步获取
  if (!userStore.userInfo) {
    fetchUserInfo()
  } else {
    // 使用缓存数据
    originalUserInfo.value = { ...userStore.userInfo }
    // 如果需要刷新，后台异步刷新（不阻塞UI）
    if (userStore.needRefresh()) {
      fetchUserInfo()
    }
  }
})
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
  margin: 0;
  padding: 0;
}

.profile-content {
  flex: 1;
  display: flex;
  width: 100%;
  max-width: 100%;
  margin: 60px 0 0 0;
  padding: 0;
}

.sidebar {
  background: white;
  border-right: 1px solid #eee;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.05);
  margin: 0;
  padding: 0;
}

.side-menu {
  border-right: none;
}

.user-info {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.avatar-wrapper {
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  transition: transform 0.2s;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

/* 头像弹出菜单 */
.avatar-popover-menu {
  padding: 4px 0;
}

.popover-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  font-size: 14px;
  color: #333;
}

.popover-item:hover {
  background-color: #f5f5f5;
  color: #ff4400;
}

.popover-item .el-icon {
  font-size: 16px;
}

/* 个人资料页头像上传 */
.avatar-upload-section {
  text-align: center;
  margin-bottom: 24px;
}

.avatar-upload-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-upload-wrapper:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: #fff;
  font-size: 12px;
  gap: 4px;
}

.avatar-upload-overlay .el-icon {
  font-size: 20px;
}

.user-details {
  margin-top: 15px;
}

.user-details h3 {
  margin: 10px 0 5px 0;
  color: #333;
  font-size: 18px;
}

.user-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.logout-item {
  color: #f56c6c;
}

.logout-item .el-icon {
  color: #f56c6c;
}

.main-content {
  flex: 1;
  padding: 0;
  margin: 0;
  background: #f5f5f5;
}

.content-box {
  background: white;
  min-height: calc(100vh - 60px);
  padding: 30px;
  margin: 0;
}

.info-section h3,
.address-section h3,
.security-section h3,
.default-section h3,
.orders-section h3,
.cart-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 24px;
}

.info-form {
  max-width: 600px;
}

/* 订单样式 */
.order-header {
  margin-bottom: 20px;
}

.order-tabs {
  margin-top: 15px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  background: #fafafa;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
}

.order-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: white;
  border-bottom: 1px solid #eee;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-status {
  font-size: 14px;
  font-weight: 600;
}

.status-pending {
  color: #ff4400;
}

.status-processing {
  color: #409eff;
}

.status-success {
  color: #67c23a;
}

.status-cancelled {
  color: #999;
}

.status-warning {
  color: #e6a23c;
}

.status-info {
  color: #909399;
}

.order-address {
  padding: 15px 20px;
}

.address-info {
  font-size: 14px;
  color: #666;
}

.address-label {
  font-weight: 500;
  color: #333;
}

.address-detail {
  display: block;
  margin-top: 5px;
  color: #666;
}

.completed-text {
  color: #67c23a;
  font-size: 14px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.order-total .total-price {
  color: #ff4400;
  font-size: 18px;
  font-weight: 700;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.waiting-text {
  color: #999;
  font-size: 14px;
}

.cancelled-text {
  color: #999;
  font-size: 14px;
}

.shipping-info {
  padding: 15px 20px;
  background: #fff9f0;
  border-top: 1px solid #eee;
}

.shipping-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #333;
}

.shipping-icon {
  font-size: 18px;
}

.shipping-detail {
  display: flex;
  gap: 20px;
  font-size: 14px;
  color: #666;
}

.empty-orders {
  padding: 60px 0;
  text-align: center;
}

/* 地址管理样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.address-card {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.2s;
}

.address-card:hover {
  border-color: #ff4400;
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.1);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.address-user {
  display: flex;
  gap: 15px;
}

.address-user .name {
  font-weight: 600;
  color: #333;
}

.address-user .phone {
  color: #666;
  font-size: 14px;
}

.default-tag {
  padding: 2px 8px;
  background: #ff4400;
  color: white;
  font-size: 12px;
  border-radius: 4px;
}

.address-detail {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.address-actions .el-button {
  padding: 4px 12px;
  font-size: 12px;
}

.empty-address {
  padding: 60px 0;
  text-align: center;
}

.address-limit {
  margin-top: 15px;
  color: #999;
  font-size: 13px;
}

/* 账户安全样式 */
.security-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.security-icon {
  font-size: 32px;
  margin-right: 20px;
}

.security-info {
  flex: 1;
}

.security-info h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 15px;
}

.security-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.default-section {
  text-align: center;
  padding: 100px 0;
}

.default-section p {
  color: #666;
  font-size: 16px;
  margin-top: 10px;
}

/* 取消订单弹窗样式 */
.cancel-reason {
  display: flex;
  align-items: center;
  gap: 15px;
}

.cancel-reason span {
  font-weight: 500;
}

.cancel-reason .el-select {
  width: 200px;
}

/* 密码切换按钮样式 */
.password-toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: color 0.2s;
}

.password-toggle-btn:hover {
  color: #ff4400;
}

.password-toggle-btn:focus {
  outline: none;
}

.eye-icon {
  width: 18px;
  height: 18px;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .profile-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #eee;
  }
  
  .content-box {
    padding: 20px;
    min-height: auto;
  }
  
  .user-info {
    padding: 20px 15px;
  }
  
  .address-list {
    grid-template-columns: 1fr;
  }
  
  .cart-item {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .item-info {
    min-width: auto;
    flex: 1;
  }
  
  .info-section h3,
  .address-section h3,
  .security-section h3,
  .default-section h3,
  .orders-section h3,
  .cart-section h3 {
    font-size: 20px;
  }
  
  .default-section {
    padding: 50px 0;
  }
}

/* 平板端适配 */
@media (min-width: 768px) and (max-width: 1023px) {
  .content-box {
    padding: 25px;
  }
  
  .address-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

/* PC端适配 */
@media (min-width: 1024px) {
  .content-box {
    padding: 30px 40px;
  }
}
</style>