<template>
  <div class="seller-products">
    <div class="page-header">
      <h1>商品管理</h1>
      <el-button type="primary" @click="openAddModal">发布新商品</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索商品名称" style="width: 200px;" clearable />
      <el-select v-model="statusFilter" placeholder="选择状态" style="width: 120px;" clearable @change="handleSearch">
        <el-option label="全部" :value="undefined" />
        <el-option label="在售" :value="1" />
        <el-option label="下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="products" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="商品图片" width="100">
        <template #default="scope">
          <div v-if="scope.row?.mainImage" class="image-wrapper">
            <img :src="getImageUrl(scope.row.mainImage)" :alt="scope.row.name || ''" class="product-image" />
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="brandName" label="品牌" width="100" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.status === 1 ? '在售' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="160" :formatter="formatDate" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="viewProduct(scope.row)">详情</el-button>
          <el-button size="small" type="primary" link @click="editProduct(scope.row)">编辑</el-button>
          <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'" link
            @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
      style="margin-top: 20px; justify-content: flex-end;"
    />

    <el-dialog :title="isEdit ? '编辑商品' : '发布新商品'" v-model="showModal" width="900px" @closed="resetForm">
      <el-steps :active="currentStep" finish-status="success" style="margin-bottom: 30px;">
        <el-step title="设置SPU" />
        <el-step title="绑定属性" />
        <el-step title="设置SKU" />
      </el-steps>

      <el-form :model="productForm" :rules="formRules" ref="formRef" label-width="100px">

        <div v-show="currentStep === 0">
          <el-form-item label="商品名称" prop="name">
            <el-input v-model="productForm.name" placeholder="请输入商品名称" maxlength="100" show-word-limit />
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品分类" prop="categoryId">
                <el-cascader
                  v-model="productForm.categoryId"
                  :options="categoryTree"
                  :props="{ label: 'name', value: 'id', children: 'children', emitPath: false }"
                  placeholder="请选择分类"
                  clearable
                  filterable
                  expand-trigger="hover"
                  :show-all-levels="false"
                  style="width: 100%;"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="商品品牌">
                <el-select v-model="productForm.brandId" placeholder="请选择品牌" clearable style="width: 100%;">
                  <el-option v-for="brand in brandList" :key="brand.id" :label="brand.name" :value="brand.id" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="商品单位">
                <el-input v-model="productForm.unit" placeholder="如：件、个、台" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="关键词">
                <el-input v-model="productForm.keywords" placeholder="用逗号分隔关键词" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="商品描述">
            <el-input v-model="productForm.description" type="textarea" :rows="3" placeholder="请输入商品描述" maxlength="500" show-word-limit />
          </el-form-item>

          <el-form-item label="商品主图">
            <div class="main-image-container">
              <el-upload
                v-model:file-list="mainImageFileList"
                :auto-upload="false"
                list-type="picture-card"
                :on-change="handleMainImageChange"
                :on-remove="handleMainImageRemove"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="image-tip">支持jpg、png、webp格式，仅可上传一张主图</div>
            </div>
          </el-form-item>

          <el-form-item label="商品图片">
            <div class="image-upload-container">
              <el-upload
                v-model:file-list="imageFileList"
                :auto-upload="false"
                list-type="picture-card"
                :on-change="handleImageChange"
                :on-remove="handleImageRemove"
                :before-upload="beforeImageUpload"
                :limit="8"
                accept="image/*"
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <div class="image-tip">支持jpg、png、webp格式，最多上传8张图片</div>
            </div>
          </el-form-item>
        </div>

        <div v-show="currentStep === 1">
          <el-form-item label="当前分类">
            <span class="category-name">{{ flatCategories.find(c => c.id === productForm.categoryId)?.name || '-' }}</span>
          </el-form-item>

          <el-form-item label="基本属性">
            <div v-if="basicAttributes.length === 0" class="empty-attrs">该分类暂无基本属性</div>
            <div v-else class="attr-list">
              <div v-for="attr in basicAttributes" :key="attr.attrId" class="attr-item">
                <span class="attr-name">{{ attr.attrName }}:</span>
                <div class="basic-attr-selector">
                  <el-radio-group v-model="selectedBasicAttrs[attr.attrId]" class="attr-radio-group">
                    <el-radio v-for="val in attr.values" :key="val.valueId" :label="val.valueId">
                      {{ val.value }}
                    </el-radio>
                    <el-radio :label="-1">自定义</el-radio>
                  </el-radio-group>
                  <el-input 
                    v-if="selectedBasicAttrs[attr.attrId] === -1" 
                    v-model="customBasicAttrs[attr.attrId]" 
                    placeholder="请输入自定义值" 
                    class="custom-input"
                  />
                </div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="销售属性">
            <div v-if="salesAttributes.length === 0" class="empty-attrs">该分类暂无销售属性</div>
            <div v-else class="attr-list">
              <div v-for="attr in salesAttributes" :key="attr.attrId" class="attr-item">
                <span class="attr-name">{{ attr.attrName }}:</span>
                <div class="sale-attr-controls">
                  <el-button type="text" size="small" @click="selectAllSaleAttr(attr.attrId)" class="select-all-btn">
                    全选
                  </el-button>
                  <el-checkbox-group v-model="selectedSaleAttrValues[attr.attrId]" class="attr-checkbox-group">
                    <el-checkbox v-for="val in attr.values" :key="val.valueId" :label="val.valueId">
                      {{ val.value }}
                    </el-checkbox>
                  </el-checkbox-group>
                </div>
              </div>
            </div>
          </el-form-item>
        </div>

        <div v-show="currentStep === 2">
          <div v-if="skuTableData.length > 0">
            <el-form-item label="SKU列表">
              <div class="sku-actions">
                <span class="sku-count">共 {{ skuTableData.length }} 个SKU</span>
                <div class="sku-action-btns">
                  <el-button type="success" size="small" @click="batchAddAllSkuCombinations">批量添加</el-button>
                  <el-button type="primary" size="small" @click="openAddSingleSkuDialog">添加SKU</el-button>
                  <el-button type="primary" size="small" @click="batchModifySku">批量修改</el-button>
                  <el-button type="danger" size="small" @click="handleDeleteAllSku">全部删除</el-button>
                </div>
              </div>
              <el-table :data="skuTableData" border size="small" style="margin-top: 8px;">
                <el-table-column v-for="attr in skuAttrColumns" :key="attr.attrId" :label="attr.attrName" min-width="100">
                  <template #default="scope">
                    <span>{{ scope.row.attrNames?.[attr.attrId] || scope.row.specNames?.[attr.attrId] || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="价格" width="120">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.price" :min="0" :precision="2" size="small" controls-position="right" />
                  </template>
                </el-table-column>
                <el-table-column label="市场价" width="120">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.marketPrice" :min="0" :precision="2" size="small" controls-position="right" />
                  </template>
                </el-table-column>
                <el-table-column label="成本价" width="120">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.costPrice" :min="0" :precision="2" size="small" controls-position="right" />
                  </template>
                </el-table-column>
                <el-table-column label="库存" width="100">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.stock" :min="0" size="small" controls-position="right" />
                  </template>
                </el-table-column>
                <el-table-column label="预警库存" width="100">
                  <template #default="scope">
                    <el-input-number v-model="scope.row.warnStock" :min="0" size="small" controls-position="right" />
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
                      {{ scope.row.status === 1 ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="240" fixed="right">
                  <template #default="scope">
                    <el-button
                      :type="scope.row.status === 1 ? 'warning' : 'success'"
                      text
                      size="small"
                      @click="toggleSkuStatus(scope.row)"
                    >
                      {{ scope.row.status === 1 ? '禁用' : '启用' }}
                    </el-button>
                    <el-button type="primary" text size="small" @click="openSkuModifyDialog(scope.row)">修改</el-button>
                    <el-button type="danger" text size="small" @click="removeSkuRow(scope.$index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-form-item>
          </div>

          <div v-else class="empty-sku">
            <p>暂无SKU数据</p>
            <el-button type="success" @click="batchAddAllSkuCombinations">批量添加</el-button>
            <el-button type="primary" @click="openAddSingleSkuDialog">添加SKU</el-button>
            <el-button text @click="prevStep">返回上一步</el-button>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="showModal = false">取消</el-button>
        <el-button v-if="currentStep > 0" @click="prevStep">上一步</el-button>
        <el-button v-if="currentStep === 0 && isEdit" type="primary" @click="saveSpuEdit" :loading="submitting">修改</el-button>
        <el-button v-if="currentStep === 0 && !isEdit && !spuCreated" type="primary" @click="createSpu" :loading="submitting">添加</el-button>
        <el-button v-if="currentStep === 0 && !isEdit && spuCreated" type="primary" @click="saveSpuEdit" :loading="submitting">修改</el-button>
        <el-button v-if="currentStep === 0" type="primary" @click="nextStep">下一步</el-button>
        <el-button v-if="currentStep === 1" type="primary" @click="saveBasicAttrs" :loading="savingBasicAttrs">{{ basicAttrButtonText }}</el-button>
        <el-button v-if="currentStep === 1" type="primary" @click="saveSaleAttrs" :loading="savingSaleAttrs">{{ saleAttrButtonText }}</el-button>
        <el-button v-if="currentStep === 1" type="primary" @click="loadSkuAndNextStep" :loading="loadingSkuStep">下一步</el-button>
        <el-button v-if="currentStep === 2" type="primary" @click="submitProduct" :loading="submitting">完成</el-button>
      </template>
    </el-dialog>

    <el-dialog title="添加SKU" v-model="addSingleSkuDialogVisible" width="500px">
      <el-form label-width="100px">
        <div v-for="attr in salesAttrsForSkuCombinations" :key="attr.attrId" class="add-sku-attr">
          <el-form-item :label="attr.attrName">
            <el-select
              v-model="singleSkuForm.selectedAttrValues[attr.attrId]"
              placeholder="请选择"
              clearable
              style="width: 100%"
              @change="onSingleSkuAttrChange"
            >
              <el-option
                v-for="val in getAvailableValuesForAttr(attr.attrId)"
                :key="val.valueId"
                :label="val.value"
                :value="val.valueId"
              />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="价格">
          <el-input-number v-model="singleSkuForm.price" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="市场价">
          <el-input-number v-model="singleSkuForm.marketPrice" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="成本价">
          <el-input-number v-model="singleSkuForm.costPrice" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="singleSkuForm.stock" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="预警库存">
          <el-input-number v-model="singleSkuForm.warnStock" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="singleSkuForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addSingleSkuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddSingleSku">确认添加</el-button>
      </template>
    </el-dialog>

    <el-dialog title="商品详情" v-model="showDetailDialog" width="700px">
      <el-descriptions :column="2" border v-if="currentProduct">
        <el-descriptions-item label="商品ID">{{ currentProduct.id }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentProduct.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="品牌">{{ currentProduct.brandName }}</el-descriptions-item>
        <el-descriptions-item label="单位">{{ currentProduct.unit || '-' }}</el-descriptions-item>
        <el-descriptions-item label="关键词">{{ currentProduct.keywords || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销量">{{ currentProduct.sales }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentProduct.status === 1 ? 'success' : 'info'" size="small">
            {{ currentProduct.status === 1 ? '在售' : '下架' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDate(null, null, currentProduct.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="商品描述" :span="2">{{ currentProduct.description || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商品图片" :span="2">
          <div class="detail-images">
            <img v-for="(img, idx) in currentProduct.imageList" :key="idx" :src="getImageUrl(img)" class="detail-image" @error="handleImageError" />
          </div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog title="修改SKU" v-model="skuModifyDialogVisible" width="500px">
      <el-form v-if="currentModifySku" label-width="100px">
        <el-form-item label="价格">
          <el-input-number v-model="currentModifySku.price" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="市场价">
          <el-input-number v-model="currentModifySku.marketPrice" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="成本价">
          <el-input-number v-model="currentModifySku.costPrice" :min="0" :precision="2" controls-position="right" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="currentModifySku.stock" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="预警库存">
          <el-input-number v-model="currentModifySku.warnStock" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="currentModifySku.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuModifyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSkuModify">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getSpuPageBySeller, getSpuManageDetailForSeller, deleteSpu, addSpu, updateSpu, onShelfSpu, offShelfSpu } from '@/api/spu'
import { getCategoryList, getCategoryTree } from '@/api/category'
import { getBrandList } from '@/api/brand'
import { deleteSku, deleteSkuBySpu, enableSku, disableSku } from '@/api/sku'
import { getStoreSkuListWithAttributes, getCategoryAttributes } from '@/api/product'
import { batchBindSpuBasicAttr, batchBindSpuSaleAttr, getSpuAllAttrs, batchUpdateSpuBasicAttr, batchUpdateSpuSaleAttr } from '@/api/spuAttr'
import { updateSkuCombined, batchUpdateSku, createSkuWithAttr, batchCreateSkuWithAttr } from '@/api/skuAttr'
import { getSpuImageUrl } from '@/utils/resource'


interface SpuItem {
  id: number
  name: string
  categoryId: number
  brandId?: number
  categoryName?: string
  brandName?: string
  description?: string
  mainImage?: string
  images?: string
  unit?: string
  keywords?: string
  sales: number
  status: number
  createdAt: string
  imageList?: string[]
}

interface Category {
  id: number
  name: string
  parentId?: number
  children?: Category[]
}

interface Brand {
  id: number
  name: string
}

interface AttrValue {
  valueId: number
  value: string
  imageUrl?: string
}

interface CategoryAttribute {
  attrId: number
  attrName: string
  type: number
  values: AttrValue[]
}

interface SkuSpec {
  name: string
  values: string
}

interface SkuItem {
  id?: number
  specs: string[]
  price: number
  costPrice: number
  stock: number
  skuCode?: string
  warnStock?: number
  marketPrice: number
  status?: number
  attrNames: Record<number, string>
  attrValueIds: number[]
}

const token = localStorage.getItem('token') || ''
const userStore = useUserStore()
const sellerId = computed(() => userStore.userInfo?.id || 1)

const loading = ref(false)
const showModal = ref(false)
const showDetailDialog = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const savingBasicAttrs = ref(false)
const savingSaleAttrs = ref(false)
const loadingSkuStep = ref(false)
const currentStep = ref(0)
const savedSpuId = ref<number | undefined>()
const spuCreated = ref(false)
const formRef = ref<FormInstance>()

const products = ref<SpuItem[]>([])
const categoryTree = ref<Category[]>([])
const brandList = ref<Brand[]>([])
const currentProduct = ref<SpuItem | null>(null)
const imageFileList = ref<any[]>([])
const mainImageFileList = ref<any[]>([])

const basicAttributes = ref<CategoryAttribute[]>([])
const salesAttributes = ref<CategoryAttribute[]>([])
const selectedBasicAttrs = ref<Record<number, number>>({})
const customBasicAttrs = ref<Record<number, string>>({})
const selectedSaleAttrValues = ref<Record<number, number[]>>({})

const flatCategories = computed(() => {
  const result: Category[] = []
  const flatten = (cats: Category[]) => {
    cats.forEach(cat => {
      result.push(cat)
      if (cat.children && cat.children.length > 0) {
        flatten(cat.children)
      }
    })
  }
  flatten(categoryTree.value)
  return result
})

const hasExistingBasicAttrs = computed(() => Object.keys(basicAttrBindIds.value).length > 0)
const hasExistingSaleAttrs = computed(() => Object.keys(saleAttrBindIds.value).length > 0)

const basicAttrButtonText = computed(() => {
  if (isEdit.value) {
    return hasExistingBasicAttrs.value ? '修改基本属性' : '添加基本属性'
  }
  return hasExistingBasicAttrs.value ? '修改基本属性' : '添加基本属性'
})

const saleAttrButtonText = computed(() => {
  if (isEdit.value) {
    return hasExistingSaleAttrs.value ? '修改销售属性' : '添加销售属性'
  }
  return hasExistingSaleAttrs.value ? '修改销售属性' : '添加销售属性'
})

const searchKeyword = ref('')
const statusFilter = ref<number | undefined>(undefined)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

interface SkuSpecForm {
  name: string
  values: string
}

interface ProductFormData {
  id?: number
  name: string
  categoryId?: number
  brandId?: number
  unit?: string
  keywords?: string
  description?: string
  status: number
  mainImageName?: string
}

const productForm = reactive<ProductFormData>({
  name: '',
  categoryId: undefined,
  brandId: undefined,
  unit: '',
  keywords: '',
  description: '',
  status: 1
})

const skuSpecs = ref<SkuSpecForm[]>([])
const skuTableData = ref<SkuItem[]>([])
const skuAttrColumns = ref<Array<{ attrId: number; attrName: string }>>([])
const originalMainImage = ref('')
const originalImages = ref<string[]>([])
const originalCategoryId = ref<number | undefined>()
const deletedExistingImagePaths = ref<Set<string>>(new Set())
const simpleSku = reactive({
  price: 0,
  marketPrice: 0,
  stock: 0,
  warnStock: 0
})

const skuModifyDialogVisible = ref(false)
const currentModifySku = ref<SkuItem | null>(null)
const salesAttrsForSkuCombinations = ref<CategoryAttribute[]>([])
const basicAttrBindIds = ref<Record<number, number>>({})
const saleAttrBindIds = ref<Record<number, number>>({})
const addSingleSkuDialogVisible = ref(false)
const singleSkuForm = reactive({
  price: 0,
  costPrice: 0,
  marketPrice: 0,
  stock: 0,
  warnStock: 0,
  status: 1,
  selectedAttrValues: {} as Record<number, number | null>
})

const existingSkuCombinations = computed(() => {
  const set = new Set<string>()
  skuTableData.value.forEach(sku => {
    if (sku.attrValueIds && sku.attrValueIds.length > 0) {
      set.add(JSON.stringify([...sku.attrValueIds].sort()))
    }
  })
  return set
})

const formRules: FormRules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  brandId: [{ required: true, message: '请选择商品品牌', trigger: 'change' }]
}

const getImageUrl = (path?: string | null) => {
  if (!path) {
    return ''
  }
  const pathStr = String(path)
  if (pathStr.startsWith('data:') || pathStr.startsWith('http') || pathStr.startsWith('/api/')) {
    return pathStr
  }
  return getSpuImageUrl(pathStr)
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.style.display = 'none'
  const wrapper = img.parentElement
  if (wrapper) {
    wrapper.innerHTML = '<div class="placeholder-image"><span class="placeholder-text">图片失效</span></div>'
  }
}

const formatDate = (_row: any, _col: any, cellValue?: string) => {
  if (!cellValue) return '-'
  return cellValue.replace('T', ' ').substring(0, 19)
}



const getSkuValue = (specs: string[], index: number) => {
  return specs[index] || '-'
}

const toggleSkuStatus = async (sku: SkuItem) => {
  if (!sku.id) {
    sku.status = sku.status === 1 ? 0 : 1
    return
  }
  try {
    if (sku.status === 1) {
      await disableSku(sku.id)
      sku.status = 0
      ElMessage.success('已禁用')
    } else {
      await enableSku(sku.id)
      sku.status = 1
      ElMessage.success('已启用')
    }
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const openSkuModifyDialog = (sku: SkuItem) => {
  currentModifySku.value = { ...sku }
  skuModifyDialogVisible.value = true
}

const confirmSkuModify = async () => {
  if (!currentModifySku.value || !currentModifySku.value.id) {
    ElMessage.warning('SKU数据无效')
    return
  }

  savingSaleAttrs.value = true
  try {
    await updateSkuCombined({
      id: currentModifySku.value.id,
      price: currentModifySku.value.price,
      costPrice: currentModifySku.value.costPrice,
      marketPrice: currentModifySku.value.marketPrice,
      stock: currentModifySku.value.stock,
      warnStock: currentModifySku.value.warnStock,
      status: currentModifySku.value.status
    })

    const idx = skuTableData.value.findIndex(s => s.id === currentModifySku.value!.id)
    if (idx !== -1) {
      skuTableData.value[idx] = { ...currentModifySku.value }
    }

    skuModifyDialogVisible.value = false
    ElMessage.success('SKU修改成功')
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    submitting.value = false
  }
}

const batchModifySku = async () => {
  if (skuTableData.value.length === 0) {
    ElMessage.warning('没有SKU数据')
    return
  }

  try {
    await ElMessageBox.confirm(
      '将使用当前表格中的价格、库存、状态等数据批量更新所有SKU，是否继续？',
      '确认批量修改',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    const updateList = skuTableData.value
      .filter(sku => sku.id)
      .map(sku => ({
        id: sku.id!,
        price: sku.price,
        costPrice: sku.costPrice,
        marketPrice: sku.marketPrice,
        stock: sku.stock,
        warnStock: sku.warnStock,
        status: sku.status
      }))

    if (updateList.length === 0) {
      ElMessage.warning('没有可更新的SKU')
      return
    }

    const result = await batchUpdateSku(updateList)
    ElMessage.success(`批量修改完成，成功 ${result.successCount}/${result.totalCount} 个`)
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    submitting.value = false
  }
}

const batchAddAllSkuCombinations = async () => {
  const spuId = savedSpuId.value || productForm.id
  if (!spuId) {
    ElMessage.warning('请先保存商品信息')
    return
  }

  const attrs = salesAttrsForSkuCombinations.value.filter(attr => {
    const selected = selectedSaleAttrValues.value[attr.attrId] || []
    return selected.length > 0
  })

  if (attrs.length === 0) {
    ElMessage.warning('请先在第二步绑定销售属性')
    return
  }

  if (skuAttrColumns.value.length === 0) {
    skuAttrColumns.value = attrs.map(attr => ({
      attrId: attr.attrId,
      attrName: attr.attrName
    }))
  }

  const existingKeys = new Set<string>()
  skuTableData.value.forEach(sku => {
    if (sku.attrValueIds && sku.attrValueIds.length > 0) {
      existingKeys.add(JSON.stringify([...sku.attrValueIds].sort()))
    }
  })

  const combinations: Array<{ attrValueIds: number[]; attrNames: Record<number, string> }> = []

  const generate = (index: number, current: number[], names: Record<number, string>) => {
    if (index >= attrs.length) {
      const key = JSON.stringify([...current].sort())
      if (!existingKeys.has(key)) {
        combinations.push({ attrValueIds: [...current], attrNames: { ...names } })
      }
      return
    }

    const attr = attrs[index]
    if (!attr) return

    const values = selectedSaleAttrValues.value[attr.attrId] || []

    values.forEach(valueId => {
      const valueObj = attr.values.find(v => v.valueId === valueId)
      const newNames = { ...names, [attr.attrId]: valueObj?.value || '' }
      generate(index + 1, [...current, valueId], newNames)
    })
  }

  generate(0, [], {})

  if (combinations.length === 0) {
    ElMessage.warning('没有新的SKU组合可添加，所有组合已存在')
    return
  }

  submitting.value = true
  try {
    const createList = combinations.map(combo => ({
        spuId,
        price: 0,
        costPrice: 0,
        marketPrice: 0,
      stock: 0,
      warnStock: 0,
      status: 1,
      attrValueIds: combo.attrValueIds
    }))

    const result = await batchCreateSkuWithAttr(createList)

    const newSkus = combinations.map(combo => ({
      specs: [] as string[],
      price: 0,
      costPrice: 0,
      stock: 0,
      skuCode: '',
      warnStock: 0,
      marketPrice: 0,
      status: 1,
      attrNames: combo.attrNames,
      attrValueIds: combo.attrValueIds,
      id: undefined as number | undefined
    }))

    if (newSkus.length > 0) {
      const freshSkus = await getStoreSkuListWithAttributes(spuId)
      newSkus.forEach(newSku => {
        const key = JSON.stringify([...newSku.attrValueIds].sort())
        const matched = freshSkus.find(sku => {
          const saleAttrs = (sku as any).saleAttributes || []
          const ids = saleAttrs.map((sa: any) => sa.valueId)
          return JSON.stringify([...ids].sort()) === key
        })
        if (matched) {
          newSku.id = matched.id
        }
      })
    }

    skuTableData.value = [...skuTableData.value, ...newSkus]

    if (result.failedList && result.failedList.length > 0) {
      const failedMsgs = result.failedList.map(f => `${f.attrValueIds.join('-')}: ${f.reason}`).join('；')
      ElMessage.warning(`部分SKU添加失败：${failedMsgs}`)
    } else {
      ElMessage.success(`已添加 ${result.successCount} 个SKU组合`)
    }
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    submitting.value = false
  }
}

const getAvailableValuesForAttr = (attrId: number) => {
  const attr = salesAttrsForSkuCombinations.value.find(a => a.attrId === attrId)
  if (!attr) return []

  const selectedInOthers: Array<{ attrId: number; valueId: number }> = []
  for (const [otherId, valueId] of Object.entries(singleSkuForm.selectedAttrValues)) {
    if (Number(otherId) !== attrId && valueId != null) {
      selectedInOthers.push({ attrId: Number(otherId), valueId })
    }
  }

  if (selectedInOthers.length === 0) {
    return attr.values
  }

  const allAttrs = salesAttrsForSkuCombinations.value
  return attr.values.filter(val => {
    let canFormNew = false
    for (const combo of generateCombinationsForCheck(allAttrs, attrId, val.valueId, selectedInOthers)) {
      if (!existingSkuCombinations.value.has(combo)) {
        canFormNew = true
        break
      }
    }
    return canFormNew
  })
}

const generateCombinationsForCheck = (
  attrs: CategoryAttribute[],
  fixedAttrId: number,
  fixedValueId: number,
  selectedInOthers: Array<{ attrId: number; valueId: number }>
): string[] => {
  const remainingAttrs = attrs.filter(a => {
    if (a.attrId === fixedAttrId) return false
    return !selectedInOthers.some(s => s.attrId === a.attrId)
  })

  if (remainingAttrs.length === 0) {
    const ids = [fixedValueId, ...selectedInOthers.map(s => s.valueId)]
    return [JSON.stringify([...ids].sort())]
  }

  const results: string[] = []
  const seen = new Set<string>()

  const walk = (index: number, current: number[]) => {
    if (index >= remainingAttrs.length) {
      const ids = [fixedValueId, ...selectedInOthers.map(s => s.valueId), ...current]
      const key = JSON.stringify([...ids].sort())
      if (!seen.has(key)) {
        seen.add(key)
        results.push(key)
      }
      return
    }
    const attr = remainingAttrs[index]
    if (!attr) return
    for (const val of attr.values) {
      walk(index + 1, [...current, val.valueId])
    }
  }

  walk(0, [])
  return results
}

const onSingleSkuAttrChange = () => {
  for (const [attrId, valueId] of Object.entries(singleSkuForm.selectedAttrValues)) {
    if (valueId != null) {
      const available = getAvailableValuesForAttr(Number(attrId))
      const stillValid = available.some(v => v.valueId === valueId)
      if (!stillValid) {
        singleSkuForm.selectedAttrValues[Number(attrId)] = null
      }
    }
  }
}

const openAddSingleSkuDialog = () => {
  if (salesAttrsForSkuCombinations.value.length === 0) {
    ElMessage.warning('请先在第二步绑定销售属性')
    return
  }
  singleSkuForm.price = 0
  singleSkuForm.costPrice = 0
  singleSkuForm.marketPrice = 0
  singleSkuForm.stock = 0
  singleSkuForm.warnStock = 0
  singleSkuForm.status = 1
  singleSkuForm.selectedAttrValues = {}
  salesAttrsForSkuCombinations.value.forEach(attr => {
    singleSkuForm.selectedAttrValues[attr.attrId] = null
  })
  addSingleSkuDialogVisible.value = true
}

const confirmAddSingleSku = async () => {
  const spuId = savedSpuId.value || productForm.id
  if (!spuId) {
    ElMessage.warning('请先保存商品信息')
    return
  }

  const attrValueIds: number[] = []
  const attrNames: Record<number, string> = {}

  for (const attr of salesAttrsForSkuCombinations.value) {
    const valueId = singleSkuForm.selectedAttrValues[attr.attrId]
    if (!valueId) {
      ElMessage.warning(`请选择${attr.attrName}`)
      return
    }
    attrValueIds.push(valueId)
    const valObj = attr.values.find(v => v.valueId === valueId)
    attrNames[attr.attrId] = valObj?.value || ''
  }

  const existingKey = JSON.stringify([...attrValueIds].sort())
  const exists = skuTableData.value.some(sku => {
    if (sku.attrValueIds && sku.attrValueIds.length > 0) {
      return JSON.stringify([...sku.attrValueIds].sort()) === existingKey
    }
    return false
  })
  if (exists) {
    ElMessage.warning('该属性组合已存在，无法重复添加')
    return
  }

  if (skuAttrColumns.value.length === 0) {
    skuAttrColumns.value = salesAttrsForSkuCombinations.value.map(attr => ({
      attrId: attr.attrId,
      attrName: attr.attrName
    }))
  }

  submitting.value = true
  try {
    const result = await createSkuWithAttr({
      spuId,
      price: singleSkuForm.price,
      costPrice: singleSkuForm.costPrice,
      marketPrice: singleSkuForm.marketPrice,
      stock: singleSkuForm.stock,
      warnStock: singleSkuForm.warnStock,
      status: singleSkuForm.status,
      attrValueIds
    })

    const newSku: SkuItem = {
      id: result.skuId,
      specs: [],
      price: singleSkuForm.price,
      costPrice: singleSkuForm.costPrice,
      marketPrice: singleSkuForm.marketPrice,
      stock: singleSkuForm.stock,
      warnStock: singleSkuForm.warnStock,
      status: singleSkuForm.status,
      attrNames,
      attrValueIds
    }

    skuTableData.value = [...skuTableData.value, newSku]
    addSingleSkuDialogVisible.value = false
    ElMessage.success('SKU添加成功')
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    submitting.value = false
  }
}

const removeSkuRow = async (index: number) => {
  const sku = skuTableData.value[index]
  if (!sku) return

  try {
    await ElMessageBox.confirm('确定要删除这个SKU吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }

  if (sku.id) {
    try {
      await deleteSku(sku.id)
    } catch {
      // 拦截器已处理后端错误提示
      return
    }
  }

  skuTableData.value.splice(index, 1)
  ElMessage.success('删除成功')
}

const handleDeleteAllSku = async () => {
  const spuId = savedSpuId.value || productForm.id
  if (!spuId) {
    ElMessage.warning('请先保存商品信息')
    return
  }

  if (skuTableData.value.length === 0) {
    ElMessage.warning('没有SKU可删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除当前SPU下的所有 ${skuTableData.value.length} 个SKU吗？此操作不可恢复！`,
      '确认全部删除',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }

  submitting.value = true
  try {
    await deleteSkuBySpu(spuId)
    skuTableData.value = []
    ElMessage.success('全部删除成功')
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    submitting.value = false
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const result = await getSpuPageBySeller(sellerId.value, {
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value,
      page: pagination.page,
      pageSize: pagination.pageSize
    })
    products.value = result.list || []
    pagination.total = result.total || 0
  } catch (_) {
  } finally {
    loading.value = false
  }
}

const loadCategoriesAndBrands = async () => {
  try {
    const [treeData, brands] = await Promise.all([
      getCategoryTree(),
      getBrandList({ status: 1 })
    ])
    categoryTree.value = treeData || []
    brandList.value = brands || []
  } catch { /* ignore */ }
}

const selectAllSaleAttr = (attrId: number) => {
  const attr = salesAttributes.value.find(a => a.attrId === attrId)
  if (attr) {
    selectedSaleAttrValues.value[attrId] = attr.values.map(v => v.valueId)
  }
}

const handleCategoryChange = async (categoryId: number) => {
  if (!categoryId) {
    basicAttributes.value = []
    salesAttributes.value = []
    selectedBasicAttrs.value = {}
    customBasicAttrs.value = {}
    selectedSaleAttrValues.value = {}
    return
  }

  try {
    const attrs = await getCategoryAttributes(categoryId)
    const allBasic = (attrs || []).filter((a: any) => a.type === 2 || a.attrType === 2)
    const allSale = (attrs || []).filter((a: any) => a.type === 1 || a.attrType === 1)

    basicAttributes.value = allBasic.map((attr: any) => ({
      attrId: attr.attrId,
      attrName: attr.attrName,
      type: 2,
      values: (attr.values || []).map((v: any) => ({
        valueId: v.valueId,
        value: v.value,
        imageUrl: v.imageUrl
      }))
    }))
    salesAttributes.value = allSale.map((attr: any) => ({
      attrId: attr.attrId,
      attrName: attr.attrName,
      type: 1,
      values: (attr.values || []).map((v: any) => ({
        valueId: v.valueId,
        value: v.value,
        imageUrl: v.imageUrl
      }))
    }))

    // 重置选中状态
    selectedBasicAttrs.value = {}
    customBasicAttrs.value = {}
    selectedSaleAttrValues.value = {}
    basicAttributes.value.forEach(attr => {
      selectedBasicAttrs.value[attr.attrId] = 0
      customBasicAttrs.value[attr.attrId] = ''
    })
    salesAttributes.value.forEach(attr => {
      selectedSaleAttrValues.value[attr.attrId] = []
    })
  } catch {
  }
}

const buildCategoryTree = (categories: Category[]): Category[] => {
  const map = new Map<number, Category>()
  const roots: Category[] = []

  categories.forEach(cat => {
    map.set(cat.id, { ...cat, children: [] })
  })

  map.forEach(cat => {
    if (cat.parentId === 0 || !cat.parentId) {
      roots.push(cat)
    } else {
      const parent = map.get(cat.parentId)
      if (parent) {
        parent.children = parent.children || []
        parent.children.push(cat)
      }
    }
  })

  return roots
}

const createSpu = async () => {
  if (!productForm.name) {
    ElMessage.warning('请输入商品名称')
    return
  }
  if (!productForm.categoryId) {
    ElMessage.warning('请选择商品分类')
    return
  }

  submitting.value = true
  try {
    const spuData: any = {
      name: productForm.name,
      categoryId: productForm.categoryId,
      brandId: productForm.brandId || null,
      unit: productForm.unit || null,
      keywords: productForm.keywords || null,
      description: productForm.description || null,
      status: productForm.status
    }

    const allImages = [
      ...mainImageFileList.value,
      ...imageFileList.value
    ]
    const imageFiles = allImages
      .filter(f => f.raw)
      .map(f => f.raw) as File[]

    spuData.keepOldImages = false

    const result = await addSpu({
      spuDto: JSON.stringify(spuData),
      imageFiles: imageFiles.length > 0 ? imageFiles : undefined
    })
    savedSpuId.value = result.id
    productForm.id = result.id
    spuCreated.value = true
    originalCategoryId.value = productForm.categoryId
    ElMessage.success('SPU添加成功')
  } catch {
    // 拦截器已统一展示错误消息
  } finally {
    submitting.value = false
  }
}

const nextStep = async () => {
  if (currentStep.value === 0) {
    if (!productForm.name) {
      ElMessage.warning('请输入商品名称')
      return
    }
    if (!productForm.categoryId) {
      ElMessage.warning('请选择商品分类')
      return
    }

    if (!isEdit.value && !spuCreated.value) {
      ElMessage.warning('请先点击"添加"按钮保存商品信息')
      return
    }

    if (!isEdit.value) {
      productForm.id = savedSpuId.value
    }

    currentStep.value = 1

    try {
      const spuId = productForm.id!
      const categoryId = productForm.categoryId

      // 1. /attribute/category/{categoryId} 获取分类下全部属性（含所有可选值）
      // 2. /spu/attr/all/{spuId} 获取 SPU 已选择的属性
      const [catAttrs, spuAttrs] = await Promise.all([
        getCategoryAttributes(categoryId),
        getSpuAllAttrs(spuId)
      ])

      // ── 解析分类全部属性 → 按 type 拆分为基本属性 / 销售属性 ──
      const allBasic = (catAttrs || []).filter((a: any) => a.type === 2 || a.attrType === 2)
      const allSale = (catAttrs || []).filter((a: any) => a.type === 1 || a.attrType === 1)

      basicAttributes.value = allBasic.map((attr: any) => ({
        attrId: attr.attrId,
        attrName: attr.attrName,
        type: 2,
        values: (attr.values || []).map((v: any) => ({
          valueId: v.valueId,
          value: v.value,
          imageUrl: v.imageUrl
        }))
      }))
      salesAttributes.value = allSale.map((attr: any) => ({
        attrId: attr.attrId,
        attrName: attr.attrName,
        type: 1,
        values: (attr.values || []).map((v: any) => ({
          valueId: v.valueId,
          value: v.value,
          imageUrl: v.imageUrl
        }))
      }))

      // ── 重置选择状态 ──
      selectedBasicAttrs.value = {}
      customBasicAttrs.value = {}
      selectedSaleAttrValues.value = {}
      basicAttrBindIds.value = {}
      saleAttrBindIds.value = {}

      // ── 从 /spu/attr/all/{spuId} 填充选中状态 ──
      if (spuAttrs) {
        const bindIds: Record<number, number> = {}
        spuAttrs.basicAttrs?.forEach((attr: any) => {
          bindIds[attr.attrId] = attr.id
          if (attr.manualValue) {
            selectedBasicAttrs.value[attr.attrId] = -1
            customBasicAttrs.value[attr.attrId] = attr.manualValue
          } else if (attr.attrValueId) {
            selectedBasicAttrs.value[attr.attrId] = attr.attrValueId
          }
        })
        basicAttrBindIds.value = bindIds

        const saleBindIds: Record<number, number> = {}
        spuAttrs.saleAttrs?.forEach((attr: any) => {
          saleBindIds[attr.attrId] = attr.id
          if (attr.selectedValues && attr.selectedValues.length > 0) {
            selectedSaleAttrValues.value[attr.attrId] = attr.selectedValues.map((v: any) => v.valueId)
          } else {
            selectedSaleAttrValues.value[attr.attrId] = []
          }
        })
        saleAttrBindIds.value = saleBindIds

        // ── 构建销售属性列表（用于SKU组合生成）──
        salesAttrsForSkuCombinations.value = (spuAttrs.saleAttrs || [])
          .filter((attr: any) => attr.selectedValues && attr.selectedValues.length > 0)
          .map((attr: any) => {
            const fullSaleAttr = allSale.find((a: any) => a.attrId === attr.attrId)
            return {
              attrId: attr.attrId,
              attrName: attr.attrName,
              type: 2,
              values: (fullSaleAttr?.values || [])
                .filter((v: any) => attr.selectedValues.some((sv: any) => sv.valueId === v.valueId))
                .map((v: any) => ({
                  valueId: v.valueId,
                  value: v.value,
                  imageUrl: v.imageUrl,
                  sort: 0
                }))
            }
          })
      } else {
        basicAttributes.value.forEach((attr: any) => {
          if (selectedBasicAttrs.value[attr.attrId] === undefined) {
            selectedBasicAttrs.value[attr.attrId] = 0
            customBasicAttrs.value[attr.attrId] = ''
          }
        })
        salesAttributes.value.forEach((attr: any) => {
          if (selectedSaleAttrValues.value[attr.attrId] === undefined) {
            selectedSaleAttrValues.value[attr.attrId] = []
          }
        })
      }
    } catch {
    }
  }
}

const saveSpuEdit = async () => {
  if (!productForm.id) return

  if (productForm.categoryId !== originalCategoryId.value) {
    const action = await ElMessageBox.confirm(
      '修改商品分类将删除之前已绑定的属性和SKU，确认要修改分类吗？',
      '警告',
      {
        confirmButtonText: '确定修改',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).catch(() => 'dismiss')

    if (action !== 'confirm') return

    selectedBasicAttrs.value = {}
    customBasicAttrs.value = {}
    selectedSaleAttrValues.value = {}
    basicAttrBindIds.value = {}
    saleAttrBindIds.value = {}
    skuTableData.value = []
    skuAttrColumns.value = []
  }

  submitting.value = true
  try {
    const spuData: any = {
      id: productForm.id,
      name: productForm.name,
      categoryId: productForm.categoryId,
      brandId: productForm.brandId || null,
      unit: productForm.unit || null,
      keywords: productForm.keywords || null,
      description: productForm.description || null,
      status: productForm.status
    }

    const mainImg = mainImageFileList.value[0]

    if (mainImg) {
      spuData.mainImageName = mainImg.raw ? mainImg.raw.name : undefined
      spuData.mainImage = mainImg.raw ? undefined : (mainImg.response?.data || undefined)
    } else if (deletedExistingImagePaths.value.has(originalMainImage.value)) {
      spuData.mainImageName = ''
      spuData.mainImage = ''
    } else {
      spuData.mainImage = undefined
      spuData.mainImageName = undefined
    }

    const keptImages = originalImages.value.filter(img => !deletedExistingImagePaths.value.has(img))
    spuData.images = keptImages.length > 0 ? JSON.stringify(keptImages) : ''
    spuData.keepOldImages = false
    
    const newImageFiles: File[] = [
      ...(mainImg?.raw ? [mainImg.raw] : []),
      ...imageFileList.value.filter(f => f.raw).map(f => f.raw)
    ]
    
    await updateSpu({
      spuDto: JSON.stringify(spuData),
      imageFiles: newImageFiles.length > 0 ? newImageFiles : undefined
    })

    originalMainImage.value = spuData.mainImage || ''
    if (spuData.images) {
      try {
        originalImages.value = JSON.parse(spuData.images)
      } catch {
        originalImages.value = []
      }
    } else {
      originalImages.value = []
    }
    deletedExistingImagePaths.value = new Set()

    originalCategoryId.value = productForm.categoryId
    ElMessage.success('SPU修改成功')
  } catch {
    // 拦截器已统一展示错误消息
  } finally {
    submitting.value = false
  }
}

const saveBasicAttrs = async () => {
  if (!savedSpuId.value && !productForm.id) {
    ElMessage.warning('请先保存商品信息')
    return
  }
  const spuId = savedSpuId.value || productForm.id
  if (!spuId) return

  savingBasicAttrs.value = true
  try {
    const updateItems: Array<{ id: number; spuId: number; attrId: number; attrValueId?: number; manualValue?: string }> = []
    const bindItems: Array<{ spuId: number; attrId: number; attrValueId?: number; manualValue?: string }> = []

    for (const attrId of Object.keys(selectedBasicAttrs.value)) {
      const valueId = selectedBasicAttrs.value[Number(attrId)]
      const bindId = basicAttrBindIds.value[Number(attrId)]

      if (valueId === -1) {
        const customValue = customBasicAttrs.value[Number(attrId)]
        if (customValue && customValue.trim()) {
          if (bindId) {
            updateItems.push({ id: bindId, spuId, attrId: Number(attrId), manualValue: customValue.trim() })
          } else {
            bindItems.push({ spuId, attrId: Number(attrId), manualValue: customValue.trim() })
          }
        }
      } else if (valueId && valueId > 0) {
        if (bindId) {
          updateItems.push({ id: bindId, spuId, attrId: Number(attrId), attrValueId: valueId })
        } else {
          bindItems.push({ spuId, attrId: Number(attrId), attrValueId: valueId })
        }
      }
    }

    if (updateItems.length > 0) {
      await batchUpdateSpuBasicAttr(updateItems)
    }
    if (bindItems.length > 0) {
      await batchBindSpuBasicAttr(bindItems)
    }

    if (updateItems.length > 0 || bindItems.length > 0) {
      if (spuId) {
        const spuAttrs = await getSpuAllAttrs(spuId)
        if (spuAttrs) {
          const bindIds: Record<number, number> = {}
          spuAttrs.basicAttrs?.forEach((attr: any) => {
            bindIds[attr.attrId] = attr.id
            if (attr.manualValue) {
              selectedBasicAttrs.value[attr.attrId] = -1
              customBasicAttrs.value[attr.attrId] = attr.manualValue
            } else if (attr.attrValueId) {
              selectedBasicAttrs.value[attr.attrId] = attr.attrValueId
            }
          })
          basicAttrBindIds.value = bindIds
        }
      }
      ElMessage.success('基本属性保存成功')
    } else {
      ElMessage.warning('请选择基本属性')
    }
  } catch (error: any) {
    // 拦截器已处理后端错误提示
  } finally {
    savingBasicAttrs.value = false
  }
}

const saveSaleAttrs = async () => {
  if (!savedSpuId.value && !productForm.id) {
    ElMessage.warning('请先保存商品信息')
    return
  }
  const spuId = savedSpuId.value || productForm.id
  if (!spuId) return

  savingSaleAttrs.value = true
  try {
    const updateItems: Array<{ id: number; spuId: number; attrId: number; selectedValueIds: number[] }> = []
    const bindItems: Array<{ spuId: number; attrId: number; selectedValueIds: number[] }> = []

    for (const attrId of Object.keys(selectedSaleAttrValues.value)) {
      const valueIds = selectedSaleAttrValues.value[Number(attrId)]
      const bindId = saleAttrBindIds.value[Number(attrId)]
      if (valueIds && valueIds.length > 0) {
        if (bindId) {
          updateItems.push({ id: bindId, spuId, attrId: Number(attrId), selectedValueIds: valueIds })
        } else {
          bindItems.push({ spuId, attrId: Number(attrId), selectedValueIds: valueIds })
        }
      }
    }

    if (updateItems.length > 0) {
      await batchUpdateSpuSaleAttr(updateItems)
    }
    if (bindItems.length > 0) {
      await batchBindSpuSaleAttr(bindItems)
    }

    if (updateItems.length > 0 || bindItems.length > 0) {
      const spuAttrs = await getSpuAllAttrs(spuId)
      if (spuAttrs) {
        const saleBindIds: Record<number, number> = {}
        spuAttrs.saleAttrs?.forEach((attr: any) => {
          saleBindIds[attr.attrId] = attr.id
          if (attr.selectedValues && attr.selectedValues.length > 0) {
            selectedSaleAttrValues.value[attr.attrId] = attr.selectedValues.map((v: any) => v.valueId)
          }
        })
        saleAttrBindIds.value = saleBindIds

        salesAttrsForSkuCombinations.value = (spuAttrs.saleAttrs || []).map((attr: any) => ({
          attrId: attr.attrId,
          attrName: attr.attrName,
          type: 2,
          values: (attr.selectedValues || []).map((v: any) => ({
            valueId: v.valueId,
            value: v.value,
            imageUrl: v.imageUrl || null,
            sort: 0
          }))
        }))
      }
      ElMessage.success('销售属性保存成功')
    } else {
      ElMessage.warning('请选择销售属性')
    }
  } catch (error: any) {
    // 拦截器已处理后端错误提示
  } finally {
    savingSaleAttrs.value = false
  }
}

const loadSkuAndNextStep = async () => {
  const spuId = savedSpuId.value || productForm.id
  if (!spuId) {
    ElMessage.warning('请先保存商品信息')
    return
  }

  if (!hasExistingBasicAttrs.value) {
    ElMessage.warning('请先添加基本属性')
    return
  }
  if (!hasExistingSaleAttrs.value) {
    ElMessage.warning('请先添加销售属性')
    return
  }

  loadingSkuStep.value = true
  try {
    const skuList = await getStoreSkuListWithAttributes(spuId)

    if (skuList && skuList.length > 0) {
      const attrMap = new Map<number, { attrId: number; attrName: string; values: Set<string> }>()
      
      skuList.forEach((skuItem: any) => {
        const saleAttrs = skuItem.saleAttributes || []
        if (saleAttrs.length > 0) {
          saleAttrs.forEach((sa: any) => {
            if (!attrMap.has(sa.attrId)) {
              attrMap.set(sa.attrId, { attrId: sa.attrId, attrName: sa.attrName, values: new Set() })
            }
            attrMap.get(sa.attrId)!.values.add(sa.value)
          })
        }
      })
      
      if (attrMap.size > 0) {
        skuAttrColumns.value = Array.from(attrMap.values()).map(a => ({
          attrId: a.attrId,
          attrName: a.attrName
        }))
        
        skuTableData.value = skuList.map((skuItem: any) => {
          const saleAttrs = skuItem.saleAttributes || []
          const attrNames: Record<number, string> = {}
          const attrValueIds: number[] = []
          
          saleAttrs.forEach((sa: any) => {
            attrNames[sa.attrId] = sa.value
            attrValueIds.push(sa.valueId)
          })
          
          return {
            id: skuItem.id,
            skuCode: skuItem.skuCode || '',
            price: skuItem.price,
            costPrice: skuItem.costPrice || 0,
            stock: skuItem.stock,
            warnStock: skuItem.warnStock || 0,
            marketPrice: skuItem.marketPrice || 0,
            status: skuItem.status ?? 1,
            attrNames,
            attrValueIds,
            specs: []
          }
        })
      } else {
        const firstSku = skuList[0]
        if (firstSku) {
          const specs = typeof firstSku.specs === 'string' ? JSON.parse(firstSku.specs) : firstSku.specs || {}
          const specNames = Object.keys(specs)
          
          skuAttrColumns.value = specNames.map((name, idx) => ({
            attrId: idx + 1,
            attrName: name
          }))

          skuTableData.value = skuList.map((skuItem: any) => ({
            id: skuItem.id,
            skuCode: skuItem.skuCode || '',
            price: skuItem.price,
            costPrice: skuItem.costPrice || 0,
            stock: skuItem.stock,
            warnStock: skuItem.warnStock || 0,
            marketPrice: skuItem.marketPrice || 0,
            status: skuItem.status ?? 1,
            attrNames: typeof skuItem.specs === 'string' ? JSON.parse(skuItem.specs) : skuItem.specs || {},
            attrValueIds: [],
            specs: specNames.map(name => {
              const s = typeof skuItem.specs === 'string' ? JSON.parse(skuItem.specs) : skuItem.specs || {}
              return s[name] || ''
            })
          }))
        }
      }
    }

    currentStep.value = 2
    ElMessage.success('已加载SKU数据')
  } catch (error: any) {
    // 拦截器已处理后端错误提示
  } finally {
    loadingSkuStep.value = false
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const handleSearch = () => {
  pagination.page = 1
  loadProducts()
}

const resetSearch = () => {
  searchKeyword.value = ''
  statusFilter.value = undefined
  pagination.page = 1
  loadProducts()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  loadProducts()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  loadProducts()
}

const openAddModal = async () => {
  isEdit.value = false
  showModal.value = true
  await loadCategoriesAndBrands()
}

const viewProduct = async (row: SpuItem) => {
  try {
    const detail = await getSpuManageDetailForSeller(row.id)
    const images = parseImages(detail.images)
    if (detail.mainImage) {
      images.unshift(detail.mainImage)
    }
    currentProduct.value = {
      ...detail,
      imageList: images
    }
    showDetailDialog.value = true
  } catch {
  }
}

const editProduct = async (row: SpuItem) => {
  try {
    resetForm()
    
    const detail = await getSpuManageDetailForSeller(row.id)
    
    isEdit.value = true
    productForm.id = detail.id
    productForm.name = detail.name
    productForm.categoryId = detail.categoryId
    originalCategoryId.value = detail.categoryId
    productForm.brandId = detail.brandId
    productForm.unit = detail.unit
    productForm.keywords = detail.keywords
    productForm.description = detail.description
    productForm.status = detail.status

    const images = parseImages(detail.images)
    const mainImagePath = detail.mainImage

    originalMainImage.value = mainImagePath || ''
    originalImages.value = images

    if (mainImagePath) {
      const mainImageName = mainImagePath.split('/').pop() || ''
      mainImageFileList.value = [{
        uid: Date.now(),
        name: mainImageName,
        url: getImageUrl(mainImagePath),
        status: 'success',
        response: { data: mainImagePath }
      }]
      productForm.mainImageName = mainImageName
    }

    imageFileList.value = images.filter(Boolean).map((img, idx) => ({
      uid: Date.now() + idx + 1,
      name: img.split('/').pop() || `image-${idx}`,
      url: getImageUrl(img),
      status: 'success',
      response: { data: img }
    }))

    const categoryId = detail.categoryId
    // /attribute/category/{categoryId} 获取分类下全部属性（含所有可选值）
    // /spu/attr/all/{spuId} 获取 SPU 已选择的属性
    const [catAttrs, spuAttrs] = await Promise.all([
      getCategoryAttributes(categoryId),
      getSpuAllAttrs(detail.id)
    ])

    // ── 按 type 拆分基本属性 / 销售属性 ──
    const allBasic = (catAttrs || []).filter((a: any) => a.type === 2 || a.attrType === 2)
    const allSale = (catAttrs || []).filter((a: any) => a.type === 1 || a.attrType === 1)

    basicAttributes.value = allBasic.map((attr: any) => ({
      attrId: attr.attrId,
      attrName: attr.attrName,
      type: 2,
      values: (attr.values || []).map((v: any) => ({
        valueId: v.valueId,
        value: v.value,
        imageUrl: v.imageUrl
      }))
    }))
    salesAttributes.value = allSale.map((attr: any) => ({
      attrId: attr.attrId,
      attrName: attr.attrName,
      type: 1,
      values: (attr.values || []).map((v: any) => ({
        valueId: v.valueId,
        value: v.value,
        imageUrl: v.imageUrl
      }))
    }))

    // ── 填充选中状态（来自 /spu/attr/all/{spuId}）──
    if (spuAttrs) {
      const bindIds: Record<number, number> = {}
      spuAttrs.basicAttrs?.forEach((attr: any) => {
        bindIds[attr.attrId] = attr.id
        if (attr.manualValue) {
          selectedBasicAttrs.value[attr.attrId] = -1
          customBasicAttrs.value[attr.attrId] = attr.manualValue
        } else if (attr.attrValueId) {
          selectedBasicAttrs.value[attr.attrId] = attr.attrValueId
        }
      })
      basicAttrBindIds.value = bindIds

      const saleBindIds: Record<number, number> = {}
      spuAttrs.saleAttrs?.forEach((attr: any) => {
        saleBindIds[attr.attrId] = attr.id
        if (attr.selectedValues && attr.selectedValues.length > 0) {
          selectedSaleAttrValues.value[attr.attrId] = attr.selectedValues.map((v: any) => v.valueId)
        }
      })
      saleAttrBindIds.value = saleBindIds

      // ── 预构建销售属性（用于SKU组合生成）──
      salesAttrsForSkuCombinations.value = (spuAttrs.saleAttrs || [])
        .filter((attr: any) => attr.selectedValues && attr.selectedValues.length > 0)
        .map((attr: any) => {
          const fullSaleAttr = allSale.find((a: any) => a.attrId === attr.attrId)
          return {
            attrId: attr.attrId,
            attrName: attr.attrName,
            type: 2,
            values: (fullSaleAttr?.values || [])
              .filter((v: any) => attr.selectedValues.some((sv: any) => sv.valueId === v.valueId))
              .map((v: any) => ({
                valueId: v.valueId,
                value: v.value,
                imageUrl: v.imageUrl,
                sort: 0
              }))
          }
        })
    }

    await loadCategoriesAndBrands()
    showModal.value = true
  } catch {
    // 拦截器已统一展示错误消息
  }
}

const parseImages = (imagesStr?: string): string[] => {
  if (!imagesStr) return []
  try {
    const parsed = JSON.parse(imagesStr)
    return Array.isArray(parsed) ? parsed : [imagesStr]
  } catch {
    return imagesStr ? [imagesStr] : []
  }
}

const toggleStatus = async (row: SpuItem) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '上架' : '下架'

  try {
    if (newStatus === 1) {
      await onShelfSpu(row.id)
    } else {
      await offShelfSpu(row.id)
    }
    ElMessage.success(`商品已${action}`)
    loadProducts()
  } catch {
  }
}

const handleDelete = async (row: SpuItem) => {
  try {
    await ElMessageBox.confirm(`确定要删除商品"${row.name}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteSpu(row.id)
    ElMessage.success('商品已删除')
    loadProducts()
  } catch (error: any) {
    if (error !== 'cancel') {
    }
  }
}

const addSkuSpec = () => {
  skuSpecs.value.push({ name: '', values: '' })
}

const removeSkuSpec = (index: number) => {
  skuSpecs.value.splice(index, 1)
}

const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    return false
  }
  return true
}

const handleImageChange = (file: any, _fileList: any[]) => {
  if (file.raw) {
    file.url = URL.createObjectURL(file.raw)
  }
}

const handleImageRemove = (file: any, _fileList: any[]) => {
  if (file.url && file.url.startsWith('blob:')) {
    URL.revokeObjectURL(file.url)
  }
  if (!file.raw && file.response?.data) {
    deletedExistingImagePaths.value.add(file.response.data)
  }
}

const handleMainImageChange = (file: any, fileList: any[]) => {
  const isImage = file.raw?.type?.startsWith('image/')
  const isLt5M = file.raw && file.raw.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    mainImageFileList.value = fileList.filter((f: any) => f.uid !== file.uid)
    return
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB!')
    mainImageFileList.value = fileList.filter((f: any) => f.uid !== file.uid)
    return
  }

  if (file.raw) {
    file.url = URL.createObjectURL(file.raw)
  }
  if (fileList.length > 1) {
    mainImageFileList.value = [fileList[fileList.length - 1]]
  }
}

const handleMainImageRemove = (file: any, _fileList: any[]) => {
  if (file.url && file.url.startsWith('blob:')) {
    URL.revokeObjectURL(file.url)
  }
  if (!file.raw && file.response?.data) {
    deletedExistingImagePaths.value.add(file.response.data)
  }
}

const resetForm = () => {
  productForm.id = undefined
  productForm.name = ''
  productForm.categoryId = undefined
  productForm.brandId = undefined
  productForm.unit = ''
  productForm.keywords = ''
  productForm.description = ''
  productForm.status = 1
  productForm.mainImageName = ''
  imageFileList.value = []
  mainImageFileList.value = []
  skuSpecs.value = []
  skuTableData.value = []
  skuAttrColumns.value = []
  simpleSku.price = 0
  simpleSku.stock = 0
  simpleSku.warnStock = 0
  basicAttributes.value = []
  salesAttributes.value = []
  selectedBasicAttrs.value = {}
  customBasicAttrs.value = {}
  selectedSaleAttrValues.value = {}
  currentStep.value = 0
  savedSpuId.value = undefined
  spuCreated.value = false
  originalMainImage.value = ''
  originalImages.value = []
  deletedExistingImagePaths.value = new Set()
  salesAttrsForSkuCombinations.value = []
  basicAttrBindIds.value = {}
  saleAttrBindIds.value = {}
}

const submitProduct = async () => {
  submitting.value = true

  try {
    const spuId = savedSpuId.value || productForm.id

    if (!spuId) {
      ElMessage.error('商品信息不完整，请重新创建')
      return
    }

    if (skuTableData.value.length > 0) {
      const newSkus = skuTableData.value.filter(sku => !sku.id)

      if (newSkus.length > 0) {
        const createList = newSkus.map(sku => ({
          spuId,
          price: sku.price,
          costPrice: sku.costPrice,
          marketPrice: sku.marketPrice,
          stock: sku.stock,
          warnStock: sku.warnStock,
          status: sku.status,
          attrValueIds: sku.attrValueIds || []
        }))
        await batchCreateSkuWithAttr(createList)
      }
    }

    ElMessage.success(isEdit.value ? '商品更新成功' : '商品发布成功')
    showModal.value = false
    resetForm()
    loadProducts()
  } catch {
    // 拦截器已统一展示错误消息
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.seller-products {
  padding: 20px;
  max-width: 1400px;
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

.image-wrapper {
  width: 60px;
  height: 60px;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.placeholder-image {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 4px;
  border: 1px dashed #ddd;
}

.placeholder-text {
  font-size: 12px;
  color: #999;
  text-align: center;
  line-height: 1.2;
}

.empty-sku {
  color: #999;
  padding: 10px;
  text-align: center;
}

.empty-attrs {
  color: #999;
  padding: 10px;
  text-align: center;
}

.sale-attr-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.select-all-btn {
  align-self: flex-start;
  padding: 2px 8px;
  font-size: 12px;
  color: #409eff;
}

.attr-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attr-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.attr-name {
  font-weight: 500;
  min-width: 80px;
  color: #606266;
}

.attr-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.basic-attr-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attr-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.custom-input {
  width: 200px;
  margin-top: 4px;
}

.category-name {
  font-weight: 500;
  color: #409eff;
}

.sku-specs {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.sku-spec-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.image-upload-container {
  width: 100%;
}

.image-tip {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.detail-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.detail-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.sku-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sku-count {
  color: #666;
  font-size: 13px;
}

.sku-action-btns {
  display: flex;
  gap: 8px;
}

.add-sku-attr {
  margin-bottom: 12px;
}

.add-sku-attr .attr-label {
  display: block;
  margin-bottom: 4px;
  font-weight: 500;
  color: #333;
}
</style>
