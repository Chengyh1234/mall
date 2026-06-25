<template>
  <div class="order-confirm-container">
    <NavBar />
    
    <div class="main-content">
      <div class="order-steps">
        <el-steps :active="currentStep" align-center>
          <el-step title="确认订单" />
          <el-step title="支付订单" />
          <el-step title="完成订单" />
        </el-steps>
      </div>

      <!-- 确认订单 -->
      <div v-if="currentStep === 1" class="order-details">
        <h2>确认订单</h2>
        
        <!-- 收货地址 -->
        <div class="address-section">
          <h3>收货地址</h3>
          <div v-if="addresses.length > 0" class="address-list">
            <div 
              v-for="addr in addresses" 
              :key="addr.id" 
              class="address-item"
              :class="{ active: selectedAddressId === addr.id }"
              @click="selectAddress(addr.id)"
            >
              <el-radio v-model="selectedAddressId" :value="addr.id" />
              <div class="address-info">
                <div class="address-header">
                  <span class="name">{{ addr.receiverName }}</span>
                  <span class="phone">{{ addr.receiverPhone }}</span>
                  <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
                </div>
                <div class="address-detail">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-address">
            <el-empty description="暂无收货地址，请先添加">
              <el-button type="primary" @click="goToAddress">添加地址</el-button>
            </el-empty>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="product-info">
          <h3>商品信息</h3>
          <div class="product-list">
            <div 
              v-for="item in orderItems" 
              :key="item.cartId" 
              class="product-item"
            >
              <div class="item-image">
                <img :src="getSpuImageUrl(item.image)" :alt="item.name" />
              </div>
              <div class="item-info">
                <h4>{{ item.name }}</h4>
                <div class="item-specs">{{ item.specs }}</div>
                <div class="item-price-row">
                  <span class="price">¥{{ item.price.toFixed(2) }}</span>
                  <span class="quantity">x {{ item.quantity }}</span>
                </div>
              </div>
              <div class="item-total">
                ¥{{ (item.price * item.quantity).toFixed(2) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 订单摘要 -->
        <div class="order-summary">
          <h3>订单摘要</h3>
          <el-card>
            <div class="summary-item">
              <span>商品总价</span>
              <span>¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>运费</span>
              <span>¥{{ shippingFee.toFixed(2) }}</span>
            </div>
            <div class="summary-item">
              <span>优惠</span>
              <span class="discount">-¥{{ discount.toFixed(2) }}</span>
            </div>
            <div class="summary-item total">
              <span>应付总额</span>
              <span class="total-price">¥{{ payAmount.toFixed(2) }}</span>
            </div>
          </el-card>
        </div>

        <div class="action-buttons">
          <el-button @click="goBack">返回购物车</el-button>
          <el-button 
            type="primary" 
            :disabled="!selectedAddressId"
            @click="confirmOrder"
          >
            提交订单
          </el-button>
        </div>
      </div>

      <!-- 支付订单 -->
      <div v-else-if="currentStep === 2" class="payment-section">
        <h2>支付订单</h2>
        
        <div class="order-info-card">
          <div class="order-no">订单号：{{ orderIdList.join(', ') }}</div>
          <div class="order-amount">支付金额：<span class="amount">¥{{ payAmount.toFixed(2) }}</span></div>
        </div>

        <div class="payment-methods">
          <h3>选择支付方式</h3>
          <el-radio-group v-model="paymentMethod">
            <el-radio label="alipay">支付宝</el-radio>
            <el-radio label="wechat">微信支付</el-radio>
            <el-radio label="bank">银行卡</el-radio>
          </el-radio-group>
        </div>

        <div class="action-buttons">
          <el-button @click="goBack">返回修改</el-button>
          <el-button type="primary" @click="handlePayOrder">立即支付</el-button>
        </div>
      </div>

      <!-- 完成订单 -->
      <div v-else-if="currentStep === 3" class="success-section">
        <el-result
          icon="success"
          title="订单支付成功"
          sub-title="您的订单已成功支付，我们将尽快为您发货"
        >
          <template #extra>
            <div class="success-info">
              <p>订单号：{{ orderIdList.join(', ') }}</p>
              <p>支付金额：¥{{ payAmount.toFixed(2) }}</p>
            </div>
            <div class="success-buttons">
              <el-button type="primary" @click="goToHome">返回首页</el-button>
              <el-button @click="goToOrders">查看订单</el-button>
            </div>
          </template>
        </el-result>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import { getSpuImageUrl } from '@/utils/resource'
import { createOrderFromCart, createOrder, batchPay } from '@/api/order'
import { getAddressList } from '@/api/address'

const router = useRouter()

interface OrderItem {
  cartId: number
  spuId: number
  skuId: number
  name: string
  image: string
  specs: string
  price: number
  quantity: number
}

interface Address {
  id: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: number
}

const currentStep = ref(1)
const orderItems = ref<OrderItem[]>([])
const addresses = ref<Address[]>([])
const selectedAddressId = ref<number | null>(null)
const paymentMethod = ref('alipay')
const orderIdList = ref<number[]>([])
const loading = ref(false)
const isBuyNow = ref(false)

// 运费
const shippingFee = computed(() => {
  return totalAmount.value >= 99 ? 0 : 10
})

// 优惠
const discount = computed(() => 0)

// 商品总价
const totalAmount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// 应付金额
const payAmount = computed(() => {
  return totalAmount.value + shippingFee.value - discount.value
})

// 初始化
onMounted(() => {
  // 优先读取 buyNowItems（立即购买），再读取 orderItems（购物车结算）
  const buyNowStr = sessionStorage.getItem('buyNowItems')
  const cartStr = sessionStorage.getItem('orderItems')

  if (buyNowStr) {
    orderItems.value = JSON.parse(buyNowStr)
    sessionStorage.removeItem('buyNowItems')
    isBuyNow.value = true
  } else if (cartStr) {
    orderItems.value = JSON.parse(cartStr)
    sessionStorage.removeItem('orderItems')
  } else {
    ElMessage.warning('请先选择商品')
    router.push('/cart')
    return
  }
  
  // 获取收货地址
  loadAddresses()
})

// 加载收货地址
const loadAddresses = async () => {
  try {
    // 从API获取地址数据
    const data = await getAddressList()
    addresses.value = data || []
    
    // 默认选择第一个地址
    if (addresses.value.length > 0) {
      const defaultAddr = addresses.value.find(a => a.isDefault === 1)
      selectedAddressId.value = defaultAddr?.id ?? addresses.value[0]?.id ?? null
    }
  } catch {
    addresses.value = []
  }
}

// 选择地址
const selectAddress = (id: number) => {
  selectedAddressId.value = id
}

// 返回
const goBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  } else {
    router.push('/cart')
  }
}

// 提交订单
const confirmOrder = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  try {
    loading.value = true

    if (isBuyNow.value) {
      // 立即购买：直接创建订单
      const res = await createOrder({
        addressId: selectedAddressId.value,
        totalAmount: totalAmount.value,
        payAmount: payAmount.value,
        items: orderItems.value.map(item => ({
          skuId: item.skuId,
          quantity: item.quantity,
          productName: item.name,
          productImage: item.image,
          skuSpecs: item.specs,
          price: item.price
        }))
      })
      orderIdList.value = [res.orderId]
    } else {
      // 购物车结算
      const result = await createOrderFromCart({
        addressId: selectedAddressId.value,
        payType: 'alipay',
        buyerMessage: ''
      })
      orderIdList.value = result
    }

    ElMessage.success('订单创建成功')
    currentStep.value = 2
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    loading.value = false
  }
}

// 支付订单（调用批量支付接口）
const handlePayOrder = async () => {
  try {
    loading.value = true

    const res = await batchPay({
      orderIds: orderIdList.value,
      payType: paymentMethod.value
    })

    if (res.successCount === res.totalCount) {
      ElMessage.success('支付成功')
      currentStep.value = 3
    } else if (res.successCount > 0) {
      ElMessage.warning(`部分支付成功（${res.successCount}/${res.totalCount}），失败原因：${res.fail.map(f => f.reason).join('；')}`)
      currentStep.value = 3
    } else if (res.fail.length > 0) {
      ElMessage.error(res.fail[0].reason || '支付失败')
    } else {
      ElMessage.error('支付失败')
    }
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    loading.value = false
  }
}

// 返回首页
const goToHome = () => {
  router.push('/home')
}

// 查看订单
const goToOrders = () => {
  router.push('/orders')
}

// 跳转到地址管理
const goToAddress = () => {
  router.push('/profile#address')
}
</script>

<style scoped>
.order-confirm-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.order-steps {
  margin-bottom: 30px;
}

.order-details h2 {
  margin-bottom: 30px;
  color: #333;
  text-align: center;
  font-size: 24px;
}

.address-section,
.product-info,
.order-summary {
  margin-bottom: 30px;
}

.address-section h3,
.product-info h3,
.order-summary h3 {
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.address-item {
  display: flex;
  align-items: flex-start;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.address-item.active {
  border-color: #ff4400;
  background: #fff9f0;
}

.address-item .el-radio {
  margin-right: 15px;
  margin-top: 2px;
}

.address-info {
  flex: 1;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.address-header .name {
  font-weight: 600;
  color: #333;
}

.address-header .phone {
  color: #666;
  font-size: 14px;
}

.address-header .default-tag {
  padding: 2px 8px;
  background: #ff4400;
  color: white;
  font-size: 12px;
  border-radius: 4px;
}

.address-detail {
  color: #666;
  font-size: 14px;
}

.no-address {
  padding: 30px;
  background: white;
  border-radius: 8px;
}

.product-list {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.product-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.product-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 80px;
  height: 80px;
  margin-right: 15px;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 15px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-specs {
  color: #999;
  font-size: 13px;
  margin-bottom: 8px;
}

.item-price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price-row .price {
  color: #ff4400;
  font-weight: 600;
}

.item-price-row .quantity {
  color: #666;
  font-size: 14px;
}

.item-total {
  width: 80px;
  text-align: right;
  color: #ff4400;
  font-weight: 600;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  color: #666;
}

.summary-item.total {
  border-bottom: none;
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-top: 10px;
}

.total-price {
  color: #ff4400;
  font-size: 24px;
}

.discount {
  color: #67c23a;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
}

.action-buttons .el-button {
  min-width: 120px;
}

/* 支付页面样式 */
.payment-section {
  text-align: center;
  padding: 30px;
  background: white;
  border-radius: 8px;
}

.payment-section h2 {
  margin-bottom: 30px;
  color: #333;
}

.order-info-card {
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.order-no,
.order-amount {
  margin-bottom: 10px;
  color: #666;
}

.order-amount .amount {
  color: #ff4400;
  font-size: 24px;
  font-weight: 700;
}

.payment-methods {
  margin-bottom: 30px;
  text-align: left;
}

.payment-methods h3 {
  margin-bottom: 15px;
  color: #333;
}

.payment-methods .el-radio {
  display: block;
  margin-bottom: 15px;
  font-size: 16px;
}

/* 成功页面样式 */
.success-section {
  padding: 30px;
}

.success-info {
  margin-bottom: 20px;
  text-align: center;
  color: #666;
}

.success-info p {
  margin: 8px 0;
}

.success-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}
</style>