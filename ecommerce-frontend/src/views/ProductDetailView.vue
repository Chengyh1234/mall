<template>
  <div class="product-detail-container">
    <NavBar />

    <div class="main-content">
      <el-page-header @back="goBack" class="page-header">
        <template #content>
          <span class="page-title">商品详情</span>
        </template>
      </el-page-header>

      <div v-loading="loading" class="product-detail">
        <div class="product-left">
          <div class="product-gallery">
            <div class="main-image">
              <img :src="currentImage" :alt="spu?.name" class="img" />
            </div>
            <div class="thumbnail-list">
              <div
                v-for="(img, index) in imageList"
                :key="index"
                :class="['thumbnail', { active: currentImage === img }]"
                @click="currentImage = img"
              >
                <img :src="img" :alt="`图片 ${index + 1}`" />
              </div>
            </div>
          </div>

          <div v-if="basicAttributes.length > 0" class="basic-attr-section">
            <div class="section-title">基本属性</div>
            <div class="basic-attr-grid">
              <div v-for="attr in basicAttributes" :key="attr.attrId" class="basic-attr-item">
                <span class="attr-label">{{ attr.attrName }}：</span>
                <span class="attr-text">{{ attr.value }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="product-right">
          <div class="product-info">
            <h1 class="product-title">{{ spu?.name }}</h1>

            <div v-if="spu?.shopName" class="seller-info">
              <el-icon><Shop /></el-icon>
              <span class="shop-name">{{ spu.shopName }}</span>
            </div>

            <div class="price-section">
              <span class="price-label">售价</span>
              <span class="current-price">¥{{ currentSku?.price?.toLocaleString() || '0' }}</span>
              <span v-if="currentSku?.originalPrice" class="original-price">¥{{ currentSku?.originalPrice?.toLocaleString() }}</span>
            </div>

            <div class="info-item">
              <span class="label">销量</span>
              <span class="value">{{ currentSku?.sales || spu?.sales || 0 }}件</span>
            </div>

            <div class="info-item">
              <span class="label">库存</span>
              <span :class="['value', { 'low-stock': currentSku?.stock && currentSku?.stock < 10 }]">
                {{ currentSku?.stock || 0 }}件
                <span v-if="currentSku?.stock && currentSku?.stock < 10" class="stock-warning">库存紧张</span>
              </span>
            </div>

            <div class="spec-section">
              <div class="section-title">销售属性</div>
              <div v-for="spec in salesAttributes" :key="spec.attrId" class="spec-group">
                <span class="spec-name">{{ spec.attrName }}</span>
                <div class="spec-values">
                  <el-button
                    v-for="value in spec.values"
                    :key="value.valueId"
                    :type="isSpecSelected(spec.attrId, value.valueId) ? 'primary' : 'default'"
                    :class="['spec-btn', { active: isSpecSelected(spec.attrId, value.valueId), disabled: !isSpecAvailable(spec.attrId, value.valueId) }]"
                    @click="selectSpec(spec.attrId, value.valueId)"
                    :disabled="!isSpecAvailable(spec.attrId, value.valueId)"
                  >
                    {{ value.value }}
                  </el-button>
                </div>
              </div>
            </div>

            <div class="quantity-section">
              <span class="spec-name">数量</span>
              <el-input-number
                v-model="quantity"
                :min="1"
                :max="currentSku?.stock || 999"
                :step="1"
                class="quantity-input"
              />
            </div>

            <div class="action-buttons">
              <el-button type="success" size="large" class="action-btn add-cart-btn" @click="addToCart" :disabled="isCurrentSkuUnavailable">
                加入购物车
              </el-button>
              <el-button type="primary" size="large" class="action-btn buy-btn" @click="buyNow" :disabled="isCurrentSkuUnavailable">
                立即购买
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="product-description">
        <el-divider content-position="left">商品详情</el-divider>
        <div class="description-content">
          <p>{{ spu?.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop } from '@element-plus/icons-vue'
import NavBar from '@/components/NavBar.vue'
import { getProductDetail, getSkuListWithAttributes, getSpuBasicAttributes, type SpuDetail, type Sku, type SpuSalesAttribute, type SpuBasicAttribute } from '@/api/product'
import { addToCart as addToCartApi } from '@/api/cart'
import { getSpuImageUrl } from '@/utils/resource'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const spu = ref<SpuDetail | null>(null)
const skus = ref<Sku[]>([])
const salesAttributes = ref<SpuSalesAttribute[]>([])
const basicAttributes = ref<SpuBasicAttribute[]>([])
const quantity = ref(1)
const selectedSpecs = ref<Record<number, number>>({})
const currentImage = ref<string>('')

const imageList = computed(() => {
  const images: string[] = []
  if (spu.value?.mainImage) {
    images.push(getSpuImageUrl(spu.value.mainImage))
  }
  if (spu.value?.images) {
    try {
      const imgArray = JSON.parse(spu.value.images)
      imgArray.forEach((img: string) => {
        if (img) {
          images.push(getSpuImageUrl(img))
        }
      })
    } catch { /* ignore */ }
  }
  if (images.length === 0) {
    images.push('https://via.placeholder.com/400x400/f5f5f5/999999?text=商品图片')
  }
  return images
})

const currentSku = computed(() => {
  if (skus.value.length === 0) {
    return null
  }

  const selectedCount = Object.keys(selectedSpecs.value).length
  const attrCount = salesAttributes.value.length

  if (selectedCount !== attrCount) {
    return skus.value[0] || null
  }

  for (const sku of skus.value) {
    if (matchSku(sku, selectedSpecs.value)) {
      return sku
    }
  }

  return skus.value[0] || null
})

const isCurrentSkuUnavailable = computed(() => {
  if (!currentSku.value) return true
  return currentSku.value.status === 0 || (currentSku.value.stock != null && currentSku.value.stock <= 0)
})

const matchSku = (sku: Sku, specs: Record<number, number>) => {
  try {
    const saleAttrs = sku.saleAttributes || []
    for (const [attrId, valueId] of Object.entries(specs)) {
      const found = saleAttrs.find(attr => attr.attrId === Number(attrId) && attr.valueId === valueId)
      if (!found) {
        return false
      }
    }
    return true
  } catch (e) {
    return false
  }
}

const isSpecSelected = (attrId: number, valueId: number) => {
  return selectedSpecs.value[attrId] === valueId
}

const isSpecAvailable = (attrId: number, valueId: number) => {
  if (skus.value.length === 0) {
    return true
  }

  const tempSelected = { ...selectedSpecs.value, [attrId]: valueId }
  const selectedKeys = Object.keys(tempSelected)

  for (const sku of skus.value) {
    if (sku.stock && sku.stock > 0 && sku.status !== 0) {
      let match = true
      const saleAttrs = sku.saleAttributes || []
      for (const key of selectedKeys) {
        const id = Number(key)
        const value = tempSelected[id]
        const found = saleAttrs.find(attr => attr.attrId === id && attr.valueId === value)
        if (!found) {
          match = false
          break
        }
      }
      if (match) {
        return true
      }
    }
  }
  return false
}

const selectSpec = (attrId: number, valueId: number) => {
  if (selectedSpecs.value[attrId] === valueId) {
    delete selectedSpecs.value[attrId]
  } else {
    selectedSpecs.value[attrId] = valueId
  }
}

const initSelectedSpecs = () => {
  if (salesAttributes.value.length === 0 || skus.value.length === 0) {
    return
  }

  const firstAvailableSku = skus.value.find(sku => sku.status !== 0 && sku.stock > 0) || skus.value[0]
  if (firstAvailableSku && firstAvailableSku.saleAttributes) {
    for (const attr of firstAvailableSku.saleAttributes) {
      selectedSpecs.value[attr.attrId] = attr.valueId
    }
  }
}

const loadProductDetail = async () => {
  const productIdParam = route.params.id
  const productId = Array.isArray(productIdParam) ? productIdParam[0] : productIdParam
  if (!productId) {
    ElMessage.error('商品ID不存在')
    router.push('/home')
    return
  }

  loading.value = true
  try {
    const [spuData, skuData, basicAttrData] = await Promise.all([
      getProductDetail(productId),
      getSkuListWithAttributes(Number(productId)),
      getSpuBasicAttributes(Number(productId))
    ])
    spu.value = spuData.spu
    skus.value = skuData

    if (basicAttrData && Array.isArray(basicAttrData)) {
      basicAttributes.value = basicAttrData
    } else if (basicAttrData && basicAttrData.data) {
      basicAttributes.value = basicAttrData.data
    }

    const attrs: Record<number, SpuSalesAttribute> = {}
    for (const sku of skuData) {
      if (sku.saleAttributes) {
        for (const attr of sku.saleAttributes) {
          if (!attrs[attr.attrId]) {
            attrs[attr.attrId] = {
              attrId: attr.attrId,
              attrName: attr.attrName,
              values: []
            }
          }
          const currentAttr = attrs[attr.attrId]
          if (currentAttr) {
            const exists = currentAttr.values.find(v => v.valueId === attr.valueId)
            if (!exists) {
              currentAttr.values.push({
                valueId: attr.valueId,
                imageUrl: attr.imageUrl,
                value: attr.value
              })
            }
          }
        }
      }
    }
    salesAttributes.value = Object.values(attrs)

    const firstImage = imageList.value[0]
    currentImage.value = firstImage !== undefined ? firstImage : ''
    initSelectedSpecs()
  } catch {
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const buildSkuSpecsStr = () => {
  if (!currentSku.value) return undefined

  if (currentSku.value.specs) {
    const raw = currentSku.value.specs
    if (typeof raw === 'string') {
      try {
        const parsed = JSON.parse(raw)
        return Object.entries(parsed).map(([k, v]) => `${k}:${v}`).join(' ')
      } catch {
        return raw
      }
    }
    return Object.entries(raw as Record<string, string>).map(([k, v]) => `${k}:${v}`).join(' ')
  }

  const saleAttrs = currentSku.value.saleAttributes
  if (saleAttrs && saleAttrs.length > 0) {
    return saleAttrs.map((attr: any) => `${attr.attrName}:${attr.value}`).join(' ')
  }

  return undefined
}

const addToCart = async () => {
  if (!spu.value || !currentSku.value) {
    ElMessage.error('请先选择商品规格')
    return
  }

  if (quantity.value > (currentSku.value.stock || 0)) {
    ElMessage.error('库存不足')
    return
  }

  try {
    const specsStr = buildSkuSpecsStr()

    await addToCartApi({
      skuId: currentSku.value.id,
      quantity: quantity.value,
      productImage: currentSku.value.image || spu.value?.mainImage,
      skuSpecs: specsStr
    })
    ElMessage.success('已加入购物车')
    router.push('/cart')
  } catch {
    ElMessage.error('添加购物车失败')
  }
}

const buyNow = () => {
  if (!spu.value || !currentSku.value) {
    ElMessage.error('请先选择商品规格')
    return
  }

  if (quantity.value > (currentSku.value.stock || 0)) {
    ElMessage.error('库存不足')
    return
  }

  router.push({
    name: 'order-confirm',
    query: {
      productId: spu.value.id,
      skuId: currentSku.value.id,
      quantity: quantity.value
    }
  })
}

onMounted(() => {
  loadProductDetail()
})
</script>

<style scoped>
.product-detail-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.main-content {
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 20px 20px;
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
}

.product-detail {
  display: flex;
  gap: 40px;
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.product-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.product-gallery {
  flex-shrink: 0;
}

.main-image {
  width: 450px;
  height: 450px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.main-image .img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumbnail-list {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.thumbnail {
  width: 80px;
  height: 80px;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.thumbnail:hover {
  border-color: #ddd;
}

.thumbnail.active {
  border-color: #409eff;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.basic-attr-section {
  background: #fafafa;
  border-radius: 8px;
  padding: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
  display: inline-block;
}

.basic-attr-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px 30px;
}

.basic-attr-item {
  display: flex;
  font-size: 14px;
  padding: 8px 0;
}

.attr-label {
  color: #999;
  min-width: 80px;
}

.attr-text {
  color: #333;
}

.product-right {
  flex: 1;
}

.product-info {
  position: sticky;
  top: 20px;
}

.product-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.seller-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  color: #1890ff;
}

.seller-info .el-icon {
  font-size: 20px;
}

.shop-name {
  font-size: 15px;
  font-weight: 500;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff0f0 100%);
  border-radius: 8px;
  margin-bottom: 20px;
}

.price-label {
  font-size: 14px;
  color: #666;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: #e74c3c;
}

.original-price {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item .label {
  font-size: 14px;
  color: #666;
  min-width: 60px;
}

.info-item .value {
  font-size: 14px;
  color: #333;
}

.low-stock {
  color: #e74c3c;
}

.stock-warning {
  margin-left: 8px;
  font-size: 12px;
  padding: 2px 8px;
  background: #fff3cd;
  color: #856404;
  border-radius: 4px;
}

.spec-section {
  margin-top: 20px;
}

.spec-group {
  margin-bottom: 16px;
}

.spec-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 10px;
  display: block;
}

.spec-values {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.spec-btn {
  border: 1px solid #ddd;
  background: white;
  padding: 8px 18px;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.spec-btn:hover:not(.disabled) {
  border-color: #409eff;
  color: #409eff;
}

.spec-btn.active {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

.spec-btn.disabled {
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  border-color: #eee;
}

.quantity-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.quantity-input {
  width: 140px;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 28px;
}

.action-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  font-weight: 500;
}

.add-cart-btn {
  background: #67c23a;
  border-color: #67c23a;
}

.buy-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border-color: #ee5a5a;
}

.product-description {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.description-content {
  margin-top: 20px;
  color: #666;
  line-height: 1.8;
}

@media (max-width: 992px) {
  .product-detail {
    flex-direction: column;
  }

  .product-gallery {
    width: 100%;
  }

  .main-image {
    width: 100%;
    height: auto;
    aspect-ratio: 1;
  }

  .product-info {
    position: static;
  }
}

@media (max-width: 576px) {
  .main-content {
    padding: 70px 10px 10px;
  }

  .product-detail {
    padding: 16px;
  }

  .basic-attr-grid {
    grid-template-columns: 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }
}
</style>