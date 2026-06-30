<template>
  <div class="order-confirm-container">
    <NavBar />

    <!-- 页面顶部 Hero -->
    <section class="order-hero">
      <div class="hero-glow hero-glow-1" />
      <div class="hero-glow hero-glow-2" />
      <div class="hero-content">
        <h1 class="hero-title">{{ stepTitle }}</h1>
        <p class="hero-subtitle">{{ stepSubtitle }}</p>
      </div>
    </section>

    <main class="main-content">
      <!-- 自定义步骤条 -->
      <div class="custom-steps">
        <div
          v-for="(step, index) in steps"
          :key="step.key"
          class="step-item"
          :class="{ active: currentStep >= step.key, current: currentStep === step.key }"
        >
          <div class="step-node">
            <span v-if="currentStep > step.key" class="step-check">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </span>
            <span v-else class="step-number">{{ index + 1 }}</span>
          </div>
          <div class="step-label">{{ step.title }}</div>
          <div v-if="index < steps.length - 1" class="step-line" />
        </div>
      </div>

      <!-- 确认订单 -->
      <div v-if="currentStep === 1" class="order-layout">
        <div class="order-main">
          <!-- 收货地址 -->
          <section class="order-section">
            <div class="section-header">
              <div class="section-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
              </div>
              <h3 class="section-title">收货地址</h3>
            </div>

            <div v-if="addresses.length > 0" class="address-list">
              <div
                v-for="addr in addresses"
                :key="addr.id"
                class="address-card"
                :class="{ active: selectedAddressId === addr.id }"
                @click="selectAddress(addr.id)"
              >
                <div class="address-radio">
                  <div class="radio-inner" :class="{ checked: selectedAddressId === addr.id }" />
                </div>
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
                <div v-if="selectedAddressId === addr.id" class="selected-check">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </div>
              </div>
            </div>

            <div v-else class="empty-state">
              <div class="empty-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                  <circle cx="12" cy="10" r="3" />
                </svg>
              </div>
              <p class="empty-text">暂无收货地址，请先添加</p>
              <button class="primary-button small" @click="goToAddress">
                添加地址
              </button>
            </div>
          </section>

          <!-- 商品信息 -->
          <section class="order-section">
            <div class="section-header">
              <div class="section-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z" />
                  <line x1="3" y1="6" x2="21" y2="6" />
                  <path d="M16 10a4 4 0 0 1-8 0" />
                </svg>
              </div>
              <h3 class="section-title">商品信息</h3>
            </div>

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
                  <h4 class="item-name">{{ item.name }}</h4>
                  <div class="item-specs">{{ item.specs }}</div>
                  <div class="item-price-row">
                    <span class="price">¥{{ item.price.toFixed(2) }}</span>
                    <span class="quantity">x {{ item.quantity }}</span>
                  </div>
                </div>
                <div class="item-total">
                  <span class="total-label">小计</span>
                  <span class="total-value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
                </div>
              </div>
            </div>
          </section>
        </div>

        <!-- 订单摘要 -->
        <aside class="order-summary">
          <div class="summary-card">
            <h3 class="summary-title">订单摘要</h3>

            <div class="summary-row">
              <span>商品总价</span>
              <span class="summary-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span>运费</span>
              <span class="summary-value" :class="{ free: shippingFee === 0 }">
                {{ shippingFee === 0 ? '免运费' : `¥${shippingFee.toFixed(2)}` }}
              </span>
            </div>
            <div class="summary-row">
              <span>优惠</span>
              <span class="summary-value discount">-¥{{ discount.toFixed(2) }}</span>
            </div>

            <div class="summary-divider" />

            <div class="summary-total">
              <span>应付总额</span>
              <span class="total-amount">¥{{ payAmount.toFixed(2) }}</span>
            </div>

            <div class="action-group">
              <button class="secondary-button" @click="goBack">
                返回购物车
              </button>
              <button
                class="primary-button"
                :disabled="!selectedAddressId"
                @click="confirmOrder"
              >
                提交订单
              </button>
            </div>

            <p class="summary-tip">提交订单即表示同意相关购买协议</p>
          </div>
        </aside>
      </div>

      <!-- 支付订单 -->
      <div v-else-if="currentStep === 2" class="payment-layout">
        <div class="payment-card">
          <div class="payment-header">
            <div class="payment-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="1" y="4" width="22" height="16" rx="2" ry="2" />
                <line x1="1" y1="10" x2="23" y2="10" />
              </svg>
            </div>
            <div class="payment-meta">
              <div class="payment-label">支付金额</div>
              <div class="payment-amount">¥{{ payAmount.toFixed(2) }}</div>
            </div>
          </div>

          <div class="order-no-row">
            <span>订单号</span>
            <span class="order-no-value">{{ orderIdList.join(', ') }}</span>
          </div>
        </div>

        <div class="payment-methods">
          <h3 class="section-title">选择支付方式</h3>
          <div class="method-list">
            <div
              v-for="method in paymentMethods"
              :key="method.key"
              class="method-card"
              :class="{ active: paymentMethod === method.key }"
              @click="paymentMethod = method.key"
            >
              <div class="method-icon" v-html="method.icon" />
              <div class="method-info">
                <div class="method-name">{{ method.name }}</div>
                <div class="method-desc">{{ method.desc }}</div>
              </div>
              <div class="method-radio">
                <div class="radio-inner" :class="{ checked: paymentMethod === method.key }" />
              </div>
            </div>
          </div>
        </div>

        <div class="action-group centered">
          <button class="secondary-button" @click="goBack">
            返回修改
          </button>
          <button class="primary-button" @click="handlePayOrder">
            立即支付
          </button>
        </div>
      </div>

      <!-- 完成订单 -->
      <div v-else-if="currentStep === 3" class="success-layout">
        <div class="success-card">
          <div class="success-animation">
            <div class="success-circle">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </div>
            <div class="confetti confetti-1" />
            <div class="confetti confetti-2" />
            <div class="confetti confetti-3" />
            <div class="confetti confetti-4" />
            <div class="confetti confetti-5" />
          </div>

          <h2 class="success-title">订单支付成功</h2>
          <p class="success-subtitle">您的订单已成功支付，我们将尽快为您发货</p>

          <div class="success-info">
            <div class="info-row">
              <span>订单号</span>
              <span class="info-value">{{ orderIdList.join(', ') }}</span>
            </div>
            <div class="info-row">
              <span>支付金额</span>
              <span class="info-value amount">¥{{ payAmount.toFixed(2) }}</span>
            </div>
            <div class="info-row">
              <span>支付方式</span>
              <span class="info-value">{{ currentPaymentMethodName }}</span>
            </div>
          </div>

          <div class="action-group centered">
            <button class="secondary-button" @click="goToOrders">
              查看订单
            </button>
            <button class="primary-button" @click="goToHome">
              返回首页
            </button>
          </div>
        </div>
      </div>
    </main>
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

const steps = [
  { key: 1, title: '确认订单' },
  { key: 2, title: '支付订单' },
  { key: 3, title: '完成订单' }
]

const stepTitle = computed(() => {
  const titles: Record<number, string> = {
    1: '确认订单',
    2: '支付订单',
    3: '订单完成'
  }
  return titles[currentStep.value] || '确认订单'
})

const stepSubtitle = computed(() => {
  const subtitles: Record<number, string> = {
    1: '核对商品与收货信息，确认无误后提交',
    2: '选择支付方式，完成订单付款',
    3: '支付成功，感谢您的购买'
  }
  return subtitles[currentStep.value] || ''
})

const currentPaymentMethodName = computed(() => {
  const method = paymentMethods.find(m => m.key === paymentMethod.value)
  return method?.name || '支付宝'
})

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

const paymentMethods = [
  {
    key: 'alipay',
    name: '支付宝',
    desc: '数亿用户都在用，安全快捷',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M8 12h8"/><path d="M12 8v8"/></svg>`
  },
  {
    key: 'wechat',
    name: '微信支付',
    desc: '微信一键支付，便捷无忧',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 11c.3 0 .6 0 .9.1a5 5 0 0 1 2.6 8.8c-.5.3-1.1.5-1.7.6h-.3c-3 0-5.5-2.2-5.5-5 0-2.7 2.5-5 5.5-5z"/><path d="M4 10c0-3.3 3.1-6 7-6s7 2.7 7 6-3.1 6-7 6c-.6 0-1.2-.1-1.8-.2l-2.6 1.4.6-2.2C5.1 14.3 4 12.3 4 10z"/></svg>`
  },
  {
    key: 'bank',
    name: '银行卡',
    desc: '支持主流银行借记卡/信用卡',
    icon: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="10" width="20" height="12" rx="2"/><path d="M2 10l10-6 10 6"/></svg>`
  }
]

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
      ElMessage.warning(`部分支付成功（${res.successCount}/${res.totalCount}），失败原因：${res.fail.map((f: { reason: string }) => f.reason).join('；')}`)
      currentStep.value = 3
    } else if (res.fail.length > 0) {
      ElMessage.error(res.fail[0]?.reason ?? '支付失败')
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
/* ============================================
   订单确认/支付/完成页面 — 高级暖白金色主题
   ============================================ */
.order-confirm-container {
  min-height: 100vh;
  background: #FAFAF9;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  padding-top: 60px;
  color: #1C1917;
}

/* ---------- Hero 区域 ---------- */
.order-hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FAF7F2 0%, #F5F0E8 100%);
  padding: 48px 20px 64px;
  text-align: center;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.4;
  pointer-events: none;
}

.hero-glow-1 {
  width: 240px;
  height: 240px;
  background: radial-gradient(circle, rgba(202, 138, 4, 0.22) 0%, transparent 70%);
  top: -70px;
  left: 15%;
  animation: float 8s ease-in-out infinite;
}

.hero-glow-2 {
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.2) 0%, transparent 70%);
  bottom: -50px;
  right: 18%;
  animation: float 10s ease-in-out infinite reverse;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-title {
  margin: 0 0 10px;
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #1C1917;
}

.hero-subtitle {
  margin: 0;
  font-size: 15px;
  color: #78716C;
  font-weight: 400;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-16px) scale(1.05); }
}

/* ---------- 主体布局 ---------- */
.main-content {
  max-width: 1100px;
  margin: -32px auto 80px;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

/* ---------- 自定义步骤条 ---------- */
.custom-steps {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 0;
  margin-bottom: 36px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 20px;
  padding: 24px 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 12px 24px -4px rgba(0, 0, 0, 0.04);
}

.step-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  max-width: 180px;
}

.step-node {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #F5F5F4;
  border: 2px solid #E7E5E4;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  color: #A8A29E;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  margin-bottom: 10px;
  z-index: 2;
}

.step-check svg {
  width: 20px;
  height: 20px;
}

.step-label {
  font-size: 14px;
  font-weight: 500;
  color: #A8A29E;
  transition: all 0.3s;
  text-align: center;
}

.step-line {
  position: absolute;
  top: 20px;
  left: calc(50% + 30px);
  width: calc(100% - 60px);
  height: 2px;
  background: #E7E5E4;
  transition: background 0.4s;
  z-index: 1;
}

.step-item.active .step-node {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border-color: #FCD34D;
  color: #B45309;
  box-shadow: 0 0 0 4px rgba(252, 211, 77, 0.2);
}

.step-item.active .step-label {
  color: #B45309;
  font-weight: 600;
}

.step-item.current .step-node {
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  border-color: #B45309;
  color: #FFFFFF;
  box-shadow: 0 4px 14px rgba(180, 83, 9, 0.3);
}

.step-item.current .step-label {
  color: #B45309;
  font-weight: 700;
}

.step-item.active .step-line {
  background: linear-gradient(90deg, #FCD34D 0%, #D97706 100%);
}

/* ---------- 确认订单布局 ---------- */
.order-layout {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
  align-items: start;
}

.order-main {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-section {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 12px 24px -4px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.section-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #B45309;
  box-shadow: 0 2px 8px rgba(251, 191, 36, 0.12);
}

.section-icon svg {
  width: 20px;
  height: 20px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1C1917;
}

/* ---------- 地址卡片 ---------- */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.address-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 18px 20px;
  background: #FFFFFF;
  border: 2px solid #F5F5F4;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.address-card:hover {
  border-color: #FCD34D;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.address-card.active {
  border-color: #D97706;
  background: #FFFBEB;
  box-shadow: 0 0 0 1px #D97706, 0 12px 28px rgba(251, 191, 36, 0.12);
}

.address-radio {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #D6D3D1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
  transition: all 0.2s;
}

.address-card:hover .address-radio {
  border-color: #B45309;
}

.radio-inner {
  width: 11px;
  height: 11px;
  border-radius: 50%;
  background: transparent;
  transition: all 0.2s;
}

.radio-inner.checked {
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  box-shadow: 0 2px 6px rgba(180, 83, 9, 0.25);
}

.address-info {
  flex: 1;
  min-width: 0;
}

.address-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 8px;
}

.address-header .name {
  font-weight: 700;
  color: #1C1917;
  font-size: 16px;
}

.address-header .phone {
  color: #78716C;
  font-size: 14px;
}

.default-tag {
  padding: 2px 8px;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: 20px;
}

.address-detail {
  color: #57534E;
  font-size: 14px;
  line-height: 1.6;
}

.selected-check {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(180, 83, 9, 0.25);
}

.selected-check svg {
  width: 14px;
  height: 14px;
}

/* ---------- 空状态 ---------- */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  background: #FFFFFF;
  border-radius: 16px;
}

.empty-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #F5F5F4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #A8A29E;
}

.empty-icon svg {
  width: 34px;
  height: 34px;
}

.empty-text {
  margin: 0 0 18px;
  color: #78716C;
  font-size: 15px;
}

/* ---------- 商品列表 ---------- */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #F5F5F4;
  transition: all 0.2s;
}

.product-item:hover {
  border-color: #FCD34D;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
}

.item-image {
  width: 84px;
  height: 84px;
  border-radius: 14px;
  overflow: hidden;
  background: #F5F5F4;
  flex-shrink: 0;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.product-item:hover .item-image img {
  transform: scale(1.06);
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1C1917;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-specs {
  display: inline-block;
  padding: 4px 10px;
  background: #F5F5F4;
  border-radius: 20px;
  font-size: 12px;
  color: #78716C;
  margin-bottom: 8px;
}

.item-price-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-price-row .price {
  color: #B45309;
  font-weight: 700;
  font-size: 15px;
}

.item-price-row .quantity {
  color: #A8A29E;
  font-size: 13px;
}

.item-total {
  text-align: right;
  min-width: 80px;
}

.total-label {
  display: block;
  font-size: 11px;
  color: #A8A29E;
  margin-bottom: 2px;
}

.total-value {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: #B45309;
}

/* ---------- 订单摘要 ---------- */
.order-summary {
  position: sticky;
  top: 80px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 24px;
  padding: 26px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 16px 32px -4px rgba(0, 0, 0, 0.05);
}

.summary-title {
  margin: 0 0 22px;
  font-size: 18px;
  font-weight: 700;
  color: #1C1917;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  font-size: 15px;
  color: #57534E;
}

.summary-value {
  font-weight: 600;
  color: #1C1917;
}

.summary-value.free {
  color: #15803D;
}

.summary-value.discount {
  color: #B45309;
}

.summary-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #E7E5E4, transparent);
  margin: 20px 0;
}

.summary-total {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 22px;
  font-size: 16px;
  color: #57534E;
}

.total-amount {
  font-size: 28px;
  font-weight: 800;
  color: #B45309;
  letter-spacing: -0.5px;
}

.action-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-group.centered {
  flex-direction: row;
  justify-content: center;
  flex-wrap: wrap;
}

.primary-button {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 20px rgba(180, 83, 9, 0.25);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.primary-button.small {
  width: auto;
  padding: 0 24px;
  height: 40px;
  font-size: 14px;
  display: inline-flex;
}

.primary-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(180, 83, 9, 0.32);
}

.primary-button:disabled {
  background: #E7E5E4;
  color: #A8A29E;
  cursor: not-allowed;
  box-shadow: none;
}

.secondary-button {
  width: 100%;
  height: 48px;
  border: 1.5px solid #E7E5E4;
  border-radius: 14px;
  background: #FFFFFF;
  color: #57534E;
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.secondary-button:hover {
  border-color: #D97706;
  color: #B45309;
  background: #FFFBEB;
}

.action-group.centered .primary-button,
.action-group.centered .secondary-button {
  width: auto;
  min-width: 140px;
  padding: 0 24px;
}

.summary-tip {
  margin: 14px 0 0;
  text-align: center;
  font-size: 12px;
  color: #A8A29E;
}

/* ---------- 支付订单 ---------- */
.payment-layout {
  max-width: 640px;
  margin: 0 auto;
}

.payment-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 24px;
  padding: 28px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 16px 32px -4px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  text-align: center;
}

.payment-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.payment-icon {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #B45309;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.15);
}

.payment-icon svg {
  width: 28px;
  height: 28px;
}

.payment-label {
  font-size: 14px;
  color: #78716C;
  margin-bottom: 4px;
}

.payment-amount {
  font-size: 32px;
  font-weight: 800;
  color: #B45309;
  letter-spacing: -0.5px;
}

.order-no-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding-top: 20px;
  border-top: 1px dashed #E7E5E4;
  color: #78716C;
  font-size: 14px;
}

.order-no-value {
  color: #1C1917;
  font-weight: 600;
  font-family: 'Inter', monospace;
}

.payment-methods {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 12px 24px -4px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.payment-methods .section-title {
  margin-bottom: 18px;
}

.method-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.method-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: #FFFFFF;
  border: 2px solid #F5F5F4;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.method-card:hover {
  border-color: #FCD34D;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.method-card.active {
  border-color: #D97706;
  background: #FFFBEB;
  box-shadow: 0 0 0 1px #D97706, 0 12px 28px rgba(251, 191, 36, 0.12);
}

.method-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #B45309;
  flex-shrink: 0;
}

.method-icon svg {
  width: 24px;
  height: 24px;
}

.method-info {
  flex: 1;
  min-width: 0;
}

.method-name {
  font-size: 15px;
  font-weight: 700;
  color: #1C1917;
  margin-bottom: 4px;
}

.method-desc {
  font-size: 13px;
  color: #78716C;
}

.method-radio {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #D6D3D1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}

.method-card.active .method-radio {
  border-color: #B45309;
}

/* ---------- 完成订单 ---------- */
.success-layout {
  max-width: 560px;
  margin: 0 auto;
}

.success-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 28px;
  padding: 48px 36px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.02), 0 24px 48px -4px rgba(0, 0, 0, 0.06);
  text-align: center;
}

.success-animation {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 28px;
}

.success-circle {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 32px rgba(180, 83, 9, 0.3);
  animation: scaleIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.success-circle svg {
  width: 48px;
  height: 48px;
  animation: checkDraw 0.4s ease 0.2s both;
}

.confetti {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FCD34D 0%, #D97706 100%);
  animation: confettiPop 0.8s ease 0.4s both;
}

.confetti-1 { top: -10px; left: 50%; transform: translateX(-50%); }
.confetti-2 { top: 20%; right: -10px; }
.confetti-3 { bottom: 10%; right: 5px; }
.confetti-4 { bottom: 0; left: 10px; }
.confetti-5 { top: 30%; left: -10px; }

@keyframes scaleIn {
  0% { transform: scale(0); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

@keyframes checkDraw {
  0% { stroke-dashoffset: 30; opacity: 0; }
  100% { stroke-dashoffset: 0; opacity: 1; }
}

@keyframes confettiPop {
  0% { transform: translate(0, 0) scale(0); opacity: 1; }
  100% { transform: translate(var(--tx, 30px), var(--ty, -30px)) scale(1); opacity: 0; }
}

.confetti-1 { --tx: 0px; --ty: -40px; }
.confetti-2 { --tx: 35px; --ty: -10px; }
.confetti-3 { --tx: 25px; --ty: 25px; }
.confetti-4 { --tx: -20px; --ty: 30px; }
.confetti-5 { --tx: -35px; --ty: -15px; }

.success-title {
  margin: 0 0 10px;
  font-size: 26px;
  font-weight: 800;
  color: #1C1917;
}

.success-subtitle {
  margin: 0 0 28px;
  font-size: 15px;
  color: #78716C;
}

.success-info {
  background: #FFFBEB;
  border: 1px solid #FEF3C7;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 28px;
  text-align: left;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
  color: #57534E;
  border-bottom: 1px dashed #FCD34D;
}

.info-row:last-child {
  border-bottom: none;
}

.info-value {
  font-weight: 600;
  color: #1C1917;
}

.info-value.amount {
  font-size: 18px;
  color: #B45309;
}

/* ---------- 响应式 ---------- */
@media (max-width: 1024px) {
  .order-layout {
    grid-template-columns: 1fr;
  }

  .order-summary {
    position: static;
    order: 2;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 26px;
  }

  .main-content {
    margin-top: -24px;
  }

  .custom-steps {
    padding: 20px 16px;
    border-radius: 16px;
  }

  .step-label {
    font-size: 12px;
  }

  .step-node {
    width: 34px;
    height: 34px;
    font-size: 13px;
  }

  .order-section {
    padding: 20px;
    border-radius: 18px;
  }

  .product-item {
    flex-wrap: wrap;
  }

  .item-total {
    width: 100%;
    text-align: left;
    margin-top: 10px;
    padding-left: 100px;
  }

  .action-group.centered {
    flex-direction: column;
  }

  .action-group.centered .primary-button,
  .action-group.centered .secondary-button {
    width: 100%;
  }

  .success-card {
    padding: 36px 24px;
  }
}

@media (max-width: 480px) {
  .address-card {
    padding: 16px;
  }

  .item-image {
    width: 72px;
    height: 72px;
  }

  .payment-card,
  .payment-methods {
    padding: 20px;
    border-radius: 18px;
  }

  .payment-amount {
    font-size: 26px;
  }
}
</style>
