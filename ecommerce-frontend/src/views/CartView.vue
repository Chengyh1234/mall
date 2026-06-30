<template>
  <div class="cart-container">
    <NavBar />

    <!-- 页面顶部 Hero -->
    <section class="cart-hero">
      <div class="hero-glow hero-glow-1" />
      <div class="hero-glow hero-glow-2" />
      <div class="hero-content">
        <h1 class="hero-title">我的购物车</h1>
        <p class="hero-subtitle">
          精选好物，静待启程
          <span v-if="cartItems.length > 0" class="hero-count">
            · 已选 {{ selectedCount }} 件
          </span>
        </p>
      </div>
    </section>

    <main class="main-content">
      <!-- 有商品状态 -->
      <div v-if="cartItems.length > 0" class="cart-layout">
        <!-- 左侧商品列表 -->
        <div class="cart-main">
          <!-- 全选工具栏 -->
          <div class="select-all-bar">
            <label class="custom-checkbox" :class="{ checked: selectAll, indeterminate: isIndeterminate }">
              <input
                type="checkbox"
                :checked="selectAll"
                :disabled="cartItems.length === 0"
                @change="handleSelectAll($event.target as HTMLInputElement)"
              />
              <span class="checkbox-mark">
                <svg v-if="selectAll" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                <span v-else-if="isIndeterminate" class="indeterminate-dot" />
              </span>
              <span class="checkbox-label">全选</span>
            </label>

            <button
              v-if="selectedIds.length > 0"
              class="text-action delete-selected"
              @click="handleDeleteSelected"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
              删除选中
            </button>
          </div>

          <!-- 购物车列表 -->
          <div class="cart-list">
            <div
              v-for="item in cartItems"
              :key="item.id"
              class="cart-item"
              :class="{ selected: item.selected === 1 }"
            >
              <label class="custom-checkbox item-checkbox" :class="{ checked: item.selected === 1 }">
                <input
                  type="checkbox"
                  :checked="item.selected === 1"
                  @change="handleSelectItem(item.id, ($event.target as HTMLInputElement).checked)"
                />
                <span class="checkbox-mark">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </span>
              </label>

              <div class="item-image" @click="goProductDetail(item.spuId)">
                <img :src="item.productImage" :alt="item.productName" />
              </div>

              <div class="item-info">
                <h3 class="item-name" @click="goProductDetail(item.spuId)">{{ item.productName }}</h3>
                <div class="item-specs">{{ item.skuSpecs }}</div>
                <div class="item-price">¥{{ item.price.toFixed(2) }}</div>
              </div>

              <div class="item-quantity">
                <el-input-number
                  :model-value="item.quantity"
                  @change="handleQuantityChange(item.id, $event)"
                  :min="1"
                  :max="item.stock"
                  size="small"
                />
              </div>

              <div class="item-total">
                <span class="total-label">小计</span>
                <span class="total-value">¥{{ item.subtotal.toFixed(2) }}</span>
              </div>

              <button class="icon-delete" @click="handleDelete(item.id)" title="删除">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧结算摘要 -->
        <aside class="cart-summary">
          <div class="summary-card">
            <h3 class="summary-title">订单摘要</h3>

            <div class="summary-row">
              <span>已选商品</span>
              <span class="summary-value">{{ selectedCount }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品总额</span>
              <span class="summary-value">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span>运费</span>
              <span class="summary-value free">免运费</span>
            </div>
            <div class="summary-row">
              <span>优惠</span>
              <span class="summary-value discount">-¥0.00</span>
            </div>

            <div class="summary-divider" />

            <div class="summary-total">
              <span>合计</span>
              <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
            </div>

            <button
              class="checkout-button"
              :disabled="selectedCount === 0"
              @click="handleCheckout"
            >
              去结算
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="5" y1="12" x2="19" y2="12" />
                <polyline points="12 5 19 12 12 19" />
              </svg>
            </button>

            <p class="summary-tip">结算前请确认商品规格与数量</p>
          </div>

          <!-- 信任标识 -->
          <div class="trust-badges">
            <div class="badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              </svg>
              <span>安全支付</span>
            </div>
            <div class="badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 6v6l4 2" />
              </svg>
              <span>极速发货</span>
            </div>
            <div class="badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" />
              </svg>
              <span>正品保障</span>
            </div>
          </div>
        </aside>
      </div>

      <!-- 空购物车 -->
      <div v-else class="empty-cart">
        <div class="empty-illustration">
          <div class="cart-outline">
            <svg viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="60" cy="60" r="56" stroke="#E7E5E4" stroke-width="1.5" />
              <path d="M38 45h8l10 32h28l8-24H48" stroke="#D6D3D1" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
              <circle cx="58" cy="85" r="4" fill="#D6D3D1" />
              <circle cx="84" cy="85" r="4" fill="#D6D3D1" />
            </svg>
          </div>
          <div class="floating-bubble bubble-1" />
          <div class="floating-bubble bubble-2" />
          <div class="floating-bubble bubble-3" />
        </div>
        <h2 class="empty-title">购物车还是空的</h2>
        <p class="empty-desc">发现心仪好物，从这里开始</p>
        <button class="empty-button" @click="goHome">
          去逛逛
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="5" y1="12" x2="19" y2="12" />
            <polyline points="12 5 19 12 12 19" />
          </svg>
        </button>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import NavBar from '@/components/NavBar.vue';
import {
  getCartList,
  updateCartQuantity,
  setCartSelected,
  setCartSelectedAll,
  removeCartItem,
  clearSelectedCart,
  type CartItemVo
} from '@/api/cart';
import { getSpuImageUrl } from '@/utils/resource';

const router = useRouter();

// 购物车项接口
interface CartItem {
  id: number;
  skuId: number;
  spuId: number;
  productName: string;
  productImage: string;
  skuSpecs: string;
  price: number;
  subtotal: number;
  quantity: number;
  stock: number;
  selected: number; // 1-选中 0-未选中
}

const cartItems = ref<CartItem[]>([]);
const loading = ref(false);

// 全选状态
const selectAll = computed({
  get: () => cartItems.value.length > 0 && cartItems.value.every(item => item.selected === 1),
  set: (val: boolean) => {
    const selected = val ? 1 : 0;
    cartItems.value.forEach(item => item.selected = selected);
  }
});

// 半选状态
const isIndeterminate = computed(() => {
  const selectedCount = cartItems.value.filter(item => item.selected === 1).length;
  return selectedCount > 0 && selectedCount < cartItems.value.length;
});

// 选中的ID列表
const selectedIds = computed(() => cartItems.value
  .filter(item => item.selected === 1)
  .map(item => item.id));

// 选中数量
const selectedCount = computed(() => selectedIds.value.length);

// 选中商品总金额
const totalAmount = computed(() => cartItems.value
  .filter(item => item.selected === 1)
  .reduce((sum, item) => sum + item.subtotal, 0));

// 获取购物车列表
const loadCartList = async () => {
  loading.value = true;
  try {
    const data: CartItemVo[] = await getCartList();
    cartItems.value = data.map((item: CartItemVo) => ({
      id: item.id,
      skuId: item.skuId,
      spuId: item.spuId,
      productName: item.productName,
      productImage: item.productImage ? getSpuImageUrl(item.productImage) : '/images/default-product.png',
      skuSpecs: item.skuSpecs || '默认规格',
      price: item.price,
      subtotal: item.subtotal,
      quantity: item.quantity,
      stock: item.stock,
      selected: item.selected
    }));
  } catch {
    /* 错误已由拦截器处理 */
  }
  finally {
    loading.value = false;
  }
};

// 全选/取消全选
const handleSelectAll = async (target?: HTMLInputElement) => {
  // 兼容自定义 checkbox 的 change 事件与原有逻辑
  const next = target ? target.checked : selectAll.value;
  const selected = next ? 1 : 0;
  try {
    await setCartSelectedAll(selected);
    cartItems.value.forEach(item => item.selected = selected);
  }
  catch (error) {
    /* 错误已由拦截器处理 */
  }
};

// 选择单个商品
const handleSelectItem = async (id: number, selected: boolean) => {
  const item = cartItems.value.find(i => i.id === id);
  if (item) {
    const newSelected = selected ? 1 : 0;
    try {
      await setCartSelected(item.skuId, newSelected);
      item.selected = newSelected;
    }
    catch (error) {
      /* 错误已由拦截器处理 */
    }
  }
};

// 修改数量
const handleQuantityChange = async (id: number, quantity: number) => {
  const item = cartItems.value.find(i => i.id === id);
  if (item && quantity !== null) {
    try {
      await updateCartQuantity(item.skuId, quantity);
      item.quantity = quantity;
      // 更新小计
      item.subtotal = item.price * quantity;
      ElMessage.success('数量更新成功');
    }
    catch (error) {
      /* 错误已由拦截器处理 */
    }
  }
};

// 删除单个商品
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要从购物车中移除该商品吗？', '删除商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'cart-confirm-dialog'
    });
    const item = cartItems.value.find(i => i.id === id);
    if (item) {
      await removeCartItem(item.skuId);
      cartItems.value = cartItems.value.filter(item => item.id !== id);
      ElMessage.success('删除成功');
    }
  } catch {
    // 用户取消或关闭确认框，不做处理
  }
};

// 删除选中商品
const handleDeleteSelected = async () => {
  if (selectedIds.value.length === 0) return;
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 件商品吗？`, '删除选中商品', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'cart-confirm-dialog'
    });
    await clearSelectedCart();
    cartItems.value = cartItems.value.filter(item => item.selected === 0);
    ElMessage.success('删除成功');
  } catch {
    // 用户取消或关闭确认框，不做处理
  }
};

// 去结算
const handleCheckout = () => {
  const selectedItems = cartItems.value.filter(item => item.selected === 1);
  if (selectedItems.length === 0) {
    ElMessage.warning('请选择商品');
    return;
  }
  // 将选中的商品信息传递到订单确认页
  const orderItems = selectedItems.map(item => ({
    cartId: item.id,
    spuId: item.spuId,
    skuId: item.skuId,
    name: item.productName,
    image: item.productImage,
    specs: item.skuSpecs,
    price: item.price,
    quantity: item.quantity
  }));
  sessionStorage.setItem('orderItems', JSON.stringify(orderItems));
  router.push('/order/confirm');
};

// 跳转到商品详情
const goProductDetail = (spuId: number) => {
  router.push(`/product/${spuId}`);
};

// 跳转到首页
const goHome = () => {
  router.push('/home');
};

onMounted(() => {
  loadCartList();
});
</script>

<style scoped>
/* ============================================
   购物车页面 — 高级暖白金色主题
   ============================================ */
.cart-container {
  min-height: 100vh;
  background: #FAFAF9;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  padding-top: 60px;
  color: #1C1917;
}

/* ---------- Hero 区域 ---------- */
.cart-hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FAF7F2 0%, #F5F0E8 100%);
  padding: 56px 20px 72px;
  text-align: center;
}

.hero-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  pointer-events: none;
}

.hero-glow-1 {
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(202, 138, 4, 0.25) 0%, transparent 70%);
  top: -80px;
  left: 10%;
  animation: float 8s ease-in-out infinite;
}

.hero-glow-2 {
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(251, 191, 36, 0.22) 0%, transparent 70%);
  bottom: -60px;
  right: 12%;
  animation: float 10s ease-in-out infinite reverse;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 1200px;
  margin: 0 auto;
}

.hero-title {
  margin: 0 0 12px;
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: #1C1917;
}

.hero-subtitle {
  margin: 0;
  font-size: 16px;
  color: #78716C;
  font-weight: 400;
}

.hero-count {
  color: #B45309;
  font-weight: 500;
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-18px) scale(1.05); }
}

/* ---------- 主体布局 ---------- */
.main-content {
  max-width: 1240px;
  margin: -36px auto 80px;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

.cart-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}

/* ---------- 左侧商品区 ---------- */
.cart-main {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 24px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 12px 24px -4px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.select-all-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #F5F5F4;
  background: rgba(255, 255, 255, 0.6);
}

.text-action {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: transparent;
  border: none;
  color: #78716C;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.text-action svg {
  width: 16px;
  height: 16px;
}

.text-action:hover {
  color: #B91C1C;
  background: #FEF2F2;
}

/* ---------- 自定义 Checkbox ---------- */
.custom-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.custom-checkbox input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.checkbox-mark {
  width: 22px;
  height: 22px;
  border-radius: 7px;
  border: 2px solid #D6D3D1;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
}

.checkbox-mark svg {
  width: 14px;
  height: 14px;
  color: #FFFFFF;
  transform: scale(0);
  transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.indeterminate-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #B45309;
}

.custom-checkbox:hover .checkbox-mark {
  border-color: #B45309;
  box-shadow: 0 0 0 4px rgba(180, 83, 9, 0.08);
}

.custom-checkbox.checked .checkbox-mark {
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  border-color: #B45309;
  box-shadow: 0 4px 10px rgba(180, 83, 9, 0.25);
}

.custom-checkbox.checked .checkbox-mark svg {
  transform: scale(1);
}

.custom-checkbox.indeterminate .checkbox-mark {
  border-color: #B45309;
  box-shadow: 0 0 0 4px rgba(180, 83, 9, 0.08);
}

.checkbox-label {
  font-size: 15px;
  color: #44403C;
  font-weight: 500;
}

/* ---------- 商品列表 ---------- */
.cart-list {
  padding: 8px 0;
}

.cart-item {
  display: grid;
  grid-template-columns: auto 110px 1fr auto auto auto;
  align-items: center;
  gap: 20px;
  padding: 22px 24px;
  margin: 0 10px;
  border-radius: 18px;
  border: 1px solid transparent;
  background: transparent;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.cart-item + .cart-item {
  border-top: 1px solid #F5F5F4;
}

.cart-item:hover {
  background: #FFFFFF;
  border-color: #E7E5E4;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

.cart-item.selected {
  background: #FFFBEB;
  border-color: #FCD34D;
  box-shadow: 0 0 0 1px #FCD34D, 0 12px 28px rgba(251, 191, 36, 0.12);
}

.item-checkbox {
  margin-right: 0;
}

/* 商品图片 */
.item-image {
  width: 100px;
  height: 100px;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  background: #F5F5F4;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.cart-item:hover .item-image img {
  transform: scale(1.06);
}

/* 商品信息 */
.item-info {
  min-width: 0;
}

.item-name {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1C1917;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.item-name:hover {
  color: #B45309;
}

.item-specs {
  display: inline-block;
  padding: 4px 10px;
  background: #F5F5F4;
  border-radius: 20px;
  font-size: 13px;
  color: #78716C;
  margin-bottom: 10px;
}

.item-price {
  font-size: 17px;
  font-weight: 700;
  color: #92400E;
}

/* 数量选择器 */
.item-quantity {
  display: flex;
  justify-content: center;
}

.item-quantity :deep(.el-input-number) {
  --el-input-number-width: 120px;
}

.item-quantity :deep(.el-input__wrapper) {
  background: #FFFFFF;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E7E5E4 inset;
  transition: box-shadow 0.2s;
}

.item-quantity :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #D97706 inset, 0 0 0 3px rgba(217, 119, 6, 0.12);
}

.item-quantity :deep(.el-input-number__decrease),
.item-quantity :deep(.el-input-number__increase) {
  background: transparent;
  border-color: #E7E5E4;
  color: #78716C;
  transition: all 0.2s;
}

.item-quantity :deep(.el-input-number__decrease:hover),
.item-quantity :deep(.el-input-number__increase:hover) {
  color: #B45309;
  background: #FFFBEB;
}

/* 小计 */
.item-total {
  text-align: right;
  min-width: 90px;
}

.total-label {
  display: block;
  font-size: 12px;
  color: #A8A29E;
  margin-bottom: 2px;
}

.total-value {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #B45309;
}

/* 删除按钮 */
.icon-delete {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #A8A29E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-delete svg {
  width: 18px;
  height: 18px;
}

.icon-delete:hover {
  background: #FEF2F2;
  color: #B91C1C;
  transform: rotate(4deg);
}

/* ---------- 右侧摘要 ---------- */
.cart-summary {
  position: sticky;
  top: 80px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 24px;
  padding: 26px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 16px 32px -4px rgba(0, 0, 0, 0.05);
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
  font-size: 30px;
  font-weight: 800;
  color: #B45309;
  letter-spacing: -0.5px;
}

.checkout-button {
  width: 100%;
  height: 52px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: #FFFFFF;
  font-size: 17px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 20px rgba(180, 83, 9, 0.28);
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.checkout-button svg {
  width: 18px;
  height: 18px;
  transition: transform 0.2s;
}

.checkout-button:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(180, 83, 9, 0.35);
}

.checkout-button:not(:disabled):hover svg {
  transform: translateX(3px);
}

.checkout-button:disabled {
  background: #E7E5E4;
  color: #A8A29E;
  cursor: not-allowed;
  box-shadow: none;
}

.summary-tip {
  margin: 14px 0 0;
  text-align: center;
  font-size: 12px;
  color: #A8A29E;
}

/* 信任标识 */
.trust-badges {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-top: 16px;
}

.badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 14px 8px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 16px;
  color: #78716C;
  font-size: 12px;
  transition: all 0.2s ease;
}

.badge svg {
  width: 20px;
  height: 20px;
  color: #B45309;
}

.badge:hover {
  background: #FFFFFF;
  border-color: #FCD34D;
  color: #1C1917;
  transform: translateY(-2px);
}

/* ---------- 空购物车 ---------- */
.empty-cart {
  max-width: 480px;
  margin: 40px auto 0;
  text-align: center;
  padding: 60px 30px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(231, 229, 228, 0.8);
  border-radius: 28px;
  box-shadow:
    0 4px 6px -1px rgba(0, 0, 0, 0.02),
    0 20px 40px -4px rgba(0, 0, 0, 0.05);
}

.empty-illustration {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 28px;
}

.cart-outline {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(251, 191, 36, 0.15);
}

.cart-outline svg {
  width: 80px;
  height: 80px;
}

.floating-bubble {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, #FCD34D 0%, #D97706 100%);
  opacity: 0.6;
  animation: bubble 3s ease-in-out infinite;
}

.bubble-1 {
  width: 18px;
  height: 18px;
  top: 10px;
  right: 20px;
  animation-delay: 0s;
}

.bubble-2 {
  width: 12px;
  height: 12px;
  bottom: 30px;
  left: 10px;
  animation-delay: 0.8s;
}

.bubble-3 {
  width: 10px;
  height: 10px;
  bottom: 10px;
  right: 40px;
  animation-delay: 1.5s;
}

@keyframes bubble {
  0%, 100% { transform: translateY(0) scale(1); opacity: 0.5; }
  50% { transform: translateY(-10px) scale(1.15); opacity: 0.8; }
}

.empty-title {
  margin: 0 0 10px;
  font-size: 24px;
  font-weight: 700;
  color: #1C1917;
}

.empty-desc {
  margin: 0 0 28px;
  font-size: 15px;
  color: #78716C;
}

.empty-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 14px 32px;
  border: none;
  border-radius: 14px;
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(180, 83, 9, 0.25);
  transition: all 0.25s ease;
}

.empty-button svg {
  width: 18px;
  height: 18px;
  transition: transform 0.2s;
}

.empty-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(180, 83, 9, 0.32);
}

.empty-button:hover svg {
  transform: translateX(3px);
}

/* ---------- 响应式 ---------- */
@media (max-width: 1024px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }

  .cart-summary {
    position: static;
    order: 2;
  }

  .trust-badges {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 28px;
  }

  .main-content {
    margin-top: -24px;
  }

  .cart-item {
    grid-template-columns: auto 90px 1fr;
    grid-template-rows: auto auto auto;
    gap: 12px;
    padding: 18px;
  }

  .item-checkbox {
    grid-row: 1 / 3;
    align-self: start;
  }

  .item-image {
    width: 90px;
    height: 90px;
    grid-row: 1 / 3;
  }

  .item-info {
    grid-column: 3;
    grid-row: 1;
  }

  .item-quantity {
    grid-column: 2 / 4;
    grid-row: 3;
    justify-content: flex-start;
  }

  .item-total {
    grid-column: 3;
    grid-row: 2;
    text-align: left;
  }

  .icon-delete {
    position: absolute;
    top: 18px;
    right: 18px;
  }

  .cart-item {
    position: relative;
  }

  .total-label {
    display: inline;
    margin-right: 6px;
  }

  .total-value {
    display: inline;
  }

  .trust-badges {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .badge {
    flex-direction: row;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .cart-main {
    border-radius: 18px;
  }

  .select-all-bar {
    padding: 16px;
  }

  .cart-item {
    margin: 0 6px;
    border-radius: 14px;
  }

  .summary-card {
    padding: 20px;
    border-radius: 18px;
  }

  .total-amount {
    font-size: 24px;
  }
}
</style>

<style>
/* 全局确认弹窗样式覆盖 */
.cart-confirm-dialog .el-message-box {
  border-radius: 20px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.12);
  border: 1px solid #E7E5E4;
}

.cart-confirm-dialog .el-message-box__header {
  padding: 24px 24px 12px;
}

.cart-confirm-dialog .el-message-box__title {
  font-size: 18px;
  font-weight: 700;
  color: #1C1917;
}

.cart-confirm-dialog .el-message-box__content {
  padding: 12px 24px;
  color: #57534E;
  font-size: 15px;
}

.cart-confirm-dialog .el-message-box__btns {
  padding: 16px 24px 24px;
}

.cart-confirm-dialog .el-button {
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 500;
}

.cart-confirm-dialog .el-button--primary {
  background: linear-gradient(135deg, #D97706 0%, #B45309 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(180, 83, 9, 0.25);
}

.cart-confirm-dialog .el-button--primary:hover {
  background: linear-gradient(135deg, #B45309 0%, #92400E 100%);
}
</style>
