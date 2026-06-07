<template>
  <div class="cart-container">
    <NavBar />
    
    <div class="main-content">
      <div class="cart-header">
        <h2>我的购物车</h2>
        <span v-if="cartItems.length > 0" class="item-count">共 {{ selectedCount }} 件商品已选择</span>
      </div>
      
      <div v-if="cartItems.length > 0" class="cart-content">
        <!-- 全选 -->
        <div class="select-all">
          <el-checkbox 
            v-model="selectAll" 
            @change="handleSelectAll"
            :disabled="cartItems.length === 0"
          >
            全选
          </el-checkbox>
          <el-button v-if="selectedIds.length > 0" type="text" @click="handleDeleteSelected">
            删除选中
          </el-button>
        </div>
        
        <!-- 购物车列表 -->
        <div class="cart-list">
          <div 
            v-for="item in cartItems" 
            :key="item.id" 
            class="cart-item"
            :class="{ selected: item.selected === 1 }"
          >
            <el-checkbox 
              :model-value="item.selected === 1" 
              @change="(val: boolean | number | any) => handleSelectItem(item.id, val)"
            />
            
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
              ¥{{ item.subtotal.toFixed(2) }}
            </div>
            
            <div class="item-actions">
              <el-button type="text" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
        </div>
        
        <!-- 结算栏 -->
        <div class="checkout-bar">
          <div class="checkout-left">
            <span>已选择 {{ selectedCount }} 件商品</span>
          </div>
          <div class="checkout-right">
            <div class="total-price">
              合计：<span class="price">¥{{ totalAmount.toFixed(2) }}</span>
            </div>
            <el-button 
              type="primary" 
              :disabled="selectedCount === 0"
              @click="handleCheckout"
            >
              去结算
            </el-button>
          </div>
        </div>
      </div>
      
      <!-- 空购物车 -->
      <div v-else class="empty-cart">
        <el-empty description="购物车是空的，去逛逛吧">
          <el-button type="primary" @click="goHome">去购物</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
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
    ElMessage.error('获取购物车失败');
  }
  finally {
    loading.value = false;
  }
};

// 全选/取消全选
const handleSelectAll = async () => {
  const selected = selectAll.value ? 1 : 0;
  try {
    await setCartSelectedAll(selected);
    cartItems.value.forEach(item => item.selected = selected);
  }
  catch (error) {
    ElMessage.error('操作失败');
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
      ElMessage.error('操作失败');
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
      ElMessage.error('更新数量失败');
    }
  }
};

// 删除单个商品
const handleDelete = async (id: number) => {
  const item = cartItems.value.find(i => i.id === id);
  if (item) {
    try {
      await removeCartItem(item.skuId);
      cartItems.value = cartItems.value.filter(item => item.id !== id);
      ElMessage.success('删除成功');
    }
    catch (error) {
      ElMessage.error('删除失败');
    }
  }
};

// 删除选中商品
const handleDeleteSelected = async () => {
  try {
    await clearSelectedCart();
    cartItems.value = cartItems.value.filter(item => item.selected === 0);
    ElMessage.success('删除成功');
  }
  catch (error) {
    ElMessage.error('删除失败');
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
.cart-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding-top: 60px;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.item-count {
  color: #666;
  font-size: 14px;
}

.cart-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.select-all {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.cart-list {
  padding: 10px 0;
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  transition: background-color 0.2s;
}

.cart-item.selected {
  background-color: #fff9f0;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item > * {
  flex-shrink: 0;
}

.cart-item .el-checkbox {
  margin-right: 15px;
}

.item-image {
  width: 100px;
  height: 100px;
  margin-right: 15px;
  cursor: pointer;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
  min-width: 200px;
}

.item-name {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #333;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-name:hover {
  color: #ff4400;
}

.item-specs {
  font-size: 13px;
  color: #999;
  margin-bottom: 8px;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: #ff4400;
}

.item-quantity {
  margin: 0 20px;
}

.item-total {
  width: 100px;
  text-align: right;
  font-size: 16px;
  font-weight: 600;
  color: #ff4400;
}

.item-actions {
  margin-left: 20px;
}

.item-actions .el-button {
  color: #999;
}

.item-actions .el-button:hover {
  color: #ff4400;
}

.checkout-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.checkout-left span {
  color: #666;
  font-size: 14px;
}

.checkout-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.total-price {
  font-size: 16px;
  color: #333;
}

.total-price .price {
  font-size: 24px;
  font-weight: 700;
  color: #ff4400;
}

.checkout-right .el-button {
  width: 120px;
  height: 40px;
  font-size: 16px;
}

.empty-cart {
  background: white;
  border-radius: 8px;
  padding: 60px 0;
  text-align: center;
}
</style>