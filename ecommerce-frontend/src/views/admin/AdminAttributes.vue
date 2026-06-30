<template>
  <div class="admin-attributes" v-loading="loading">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">属性管理</h1>
          <p class="welcome-desc">管理商品的销售属性和基本属性，维护属性值与分类绑定关系。</p>
        </div>
        <button class="primary-btn primary-btn--gold" @click="showAddDialog">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新增属性
        </button>
      </div>
    </section>

    <!-- 统计卡 -->
    <section class="stats-row">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 11l3 3L22 4"/>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">属性总数</span>
          <span class="stat-card__value">{{ attributeList.length }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--sales">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="9" cy="21" r="1"/>
            <circle cx="20" cy="21" r="1"/>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">销售属性</span>
          <span class="stat-card__value">{{ salesAttrsCount }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--basic">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <line x1="4" y1="21" x2="4" y2="14"/>
            <line x1="4" y1="10" x2="4" y2="3"/>
            <line x1="12" y1="21" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12" y2="3"/>
            <line x1="20" y1="21" x2="20" y2="16"/>
            <line x1="20" y1="12" x2="20" y2="3"/>
            <line x1="1" y1="14" x2="7" y2="14"/>
            <line x1="9" y1="8" x2="15" y2="8"/>
            <line x1="17" y1="16" x2="23" y2="16"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">基本属性</span>
          <span class="stat-card__value">{{ basicAttrsCount }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--values">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="8" y1="12" x2="16" y2="12"/>
            <line x1="12" y1="8" x2="12" y2="16"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">属性值总计</span>
          <span class="stat-card__value">{{ totalValuesCount }}</span>
        </div>
      </div>
    </section>

    <!-- 筛选与列表 -->
    <section class="list-card">
      <div class="list-card__header">
        <div class="list-card__title">
          <h2>属性列表</h2>
          <span>共 {{ filteredAttributeList.length }} 条记录</span>
        </div>
        <div class="list-card__toolbar">
          <div class="filter-tabs">
            <button
              v-for="f in typeTabs"
              :key="String(f.value)"
              class="filter-tab"
              :class="{ active: typeFilter === f.value }"
              @click="setTypeFilter(f.value)"
            >{{ f.label }}</button>
          </div>
          <button class="icon-btn" title="刷新" @click="loadAttributes">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="attribute-list">
        <div
          v-for="attr in filteredAttributeList"
          :key="attr.id"
          class="attribute-item"
          :class="{ 'attribute-item--expanded': expandedIds.has(attr.id) }"
        >
          <div class="attribute-item__summary" @click="toggleExpand(attr.id)">
            <div class="attr-main">
              <div class="attr-title-row">
                <strong>{{ attr.name }}</strong>
                <span class="attr-type-tag" :class="attr.attrType === 1 ? 'attr-type-tag--sales' : 'attr-type-tag--basic'">
                  {{ attr.attrType === 1 ? '销售属性' : '基本属性' }}
                </span>
              </div>
              <div class="attr-meta-row">
                <span>排序 {{ attr.sort }}</span>
                <span class="dot-separator" />
                <span>{{ attr.values?.length || 0 }} 个属性值</span>
                <span class="dot-separator" />
                <span class="attr-id">#{{ attr.id }}</span>
              </div>
            </div>

            <div class="attribute-item__actions" @click.stop>
              <button class="action-btn action-btn--edit" @click="showEditDialog(attr)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                编辑
              </button>
              <button class="action-btn action-btn--add" @click="showAddValueDialog(attr)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                属性值
              </button>
              <button class="action-btn action-btn--delete" @click="handleDelete(attr)">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
                删除
              </button>
            </div>

            <div class="expand-chevron" :class="{ expanded: expandedIds.has(attr.id) }">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </div>
          </div>

          <!-- 展开：属性值列表 -->
          <div v-show="expandedIds.has(attr.id)" class="attribute-item__detail">
            <div class="detail-header">
              <div class="detail-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polygon points="12 2 2 7 12 12 22 7 12 2"/>
                  <polyline points="2 17 12 22 22 17"/>
                  <polyline points="2 12 12 17 22 12"/>
                </svg>
                <strong>{{ attr.name }}</strong>
                <span>属性值列表</span>
              </div>
              <button class="text-link" @click.stop="showAddValueDialog(attr)">+ 新增属性值</button>
            </div>

            <div v-if="attr.values && attr.values.length > 0" class="value-list">
              <div
                v-for="val in attr.values"
                :key="val.valueId"
                class="value-chip"
              >
                <span class="value-chip__name">{{ val.value }}</span>
                <span class="value-chip__sort">排序 {{ val.sort }}</span>
                <div class="value-chip__actions">
                  <button class="value-action" title="编辑" @click.stop="showEditValueDialog(attr, val)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                  <button class="value-action value-action--danger" title="删除" @click.stop="handleDeleteValue(attr, val)">
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>

            <div v-else class="empty-values">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <line x1="12" y1="8" x2="12" y2="12"/>
                <line x1="12" y1="16" x2="12.01" y2="16"/>
              </svg>
              <span>暂无属性值，点击新增属性值</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredAttributeList.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 11l3 3L22 4"/>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
          </svg>
        </div>
        <p>暂无属性</p>
        <span>点击右上角「新增属性」按钮开始配置</span>
      </div>
    </section>

    <!-- 新增/编辑属性弹窗 -->
    <el-dialog
      v-model="attrDialogVisible"
      :title="isEditingAttr ? '编辑属性' : '新增属性'"
      width="520px"
      :close-on-click-modal="false"
      class="attr-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <el-form ref="attrFormRef" :model="attrForm" :rules="attrRules" label-width="90px" label-position="left">
          <el-form-item label="属性名称" prop="name">
            <el-input v-model="attrForm.name" placeholder="如：颜色、尺寸" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="属性类型" prop="attrType">
            <div class="radio-cards">
              <label
                class="radio-card"
                :class="{ active: attrForm.attrType === 1 }"
                @click="attrForm.attrType = 1"
              >
                <input type="radio" :value="1" v-model="attrForm.attrType" />
                <span class="radio-card__dot" />
                <span class="radio-card__title">销售属性</span>
                <span class="radio-card__desc">用于商品 SKU 规格，如颜色/尺码</span>
              </label>
              <label
                class="radio-card"
                :class="{ active: attrForm.attrType === 2 }"
                @click="attrForm.attrType = 2"
              >
                <input type="radio" :value="2" v-model="attrForm.attrType" />
                <span class="radio-card__dot" />
                <span class="radio-card__title">基本属性</span>
                <span class="radio-card__desc">用于商品详情参数，如材质/产地</span>
              </label>
            </div>
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="attrForm.sort" :min="0" controls-position="right" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="attrDialogVisible = false">取消</button>
          <button class="primary-btn" :disabled="saving" @click="handleAttrSubmit">
            <span v-if="saving" class="btn-spinner" />
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 新增/编辑属性值弹窗 -->
    <el-dialog
      v-model="valueDialogVisible"
      :title="isEditingValue ? '编辑属性值' : '新增属性值'"
      width="520px"
      :close-on-click-modal="false"
      class="attr-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <el-form ref="valueFormRef" :model="valueForm" :rules="valueRules" label-width="90px" label-position="left">
          <el-form-item label="所属属性">
            <span class="form-readonly-tag">{{ currentAttrName }}</span>
          </el-form-item>
          <el-form-item label="属性值" prop="value">
            <el-input v-model="valueForm.value" placeholder="如：红色、XL" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="valueForm.sort" :min="0" controls-position="right" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="valueDialogVisible = false">取消</button>
          <button class="primary-btn" :disabled="saving" @click="handleValueSubmit">
            <span v-if="saving" class="btn-spinner" />
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAttributeList,
  addAttribute,
  updateAttribute,
  deleteAttribute,
  addAttributeValue,
  updateAttributeValue,
  deleteAttributeValue,
  type AttributeItem,
  type AttributeForm,
  type AttributeValueForm
} from '@/api/attribute'

const loading = ref(false)
const saving = ref(false)
const attributeList = ref<AttributeItem[]>([])
const typeFilter = ref<number | undefined>(undefined)
const expandedIds = ref<Set<number>>(new Set())

const typeTabs = [
  { label: '全部', value: undefined },
  { label: '销售属性', value: 1 },
  { label: '基本属性', value: 2 }
]

// 统计计算
const salesAttrsCount = computed(() =>
  attributeList.value.filter(a => a.attrType === 1).length
)
const basicAttrsCount = computed(() =>
  attributeList.value.filter(a => a.attrType === 2).length
)
const totalValuesCount = computed(() =>
  attributeList.value.reduce((sum, a) => sum + (a.values?.length || 0), 0)
)

const filteredAttributeList = computed(() => {
  if (typeFilter.value === undefined) return attributeList.value
  return attributeList.value.filter(a => a.attrType === typeFilter.value)
})

const setTypeFilter = (value?: number) => {
  typeFilter.value = value
}

const toggleExpand = (id: number) => {
  const next = new Set(expandedIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedIds.value = next
}

// 加载属性列表
const loadAttributes = async () => {
  loading.value = true
  try {
    attributeList.value = (await getAttributeList()) || []
  } catch {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

// ===== 属性 CRUD =====
const attrDialogVisible = ref(false)
const isEditingAttr = ref(false)
const editingAttrId = ref<number | null>(null)
const attrFormRef = ref()
const attrForm = reactive<AttributeForm>({
  name: '',
  attrType: 1,
  sort: 0
})
const attrRules = {
  name: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  attrType: [{ required: true, message: '请选择属性类型', trigger: 'change' }]
}

const resetAttrForm = () => {
  attrForm.name = ''
  attrForm.attrType = 1
  attrForm.sort = 0
}

const showAddDialog = () => {
  isEditingAttr.value = false
  editingAttrId.value = null
  currentAttrName.value = ''
  resetAttrForm()
  attrDialogVisible.value = true
}

const showEditDialog = (row: AttributeItem) => {
  isEditingAttr.value = true
  editingAttrId.value = row.id
  currentAttrName.value = row.name
  attrForm.name = row.name
  attrForm.attrType = row.attrType
  attrForm.sort = row.sort
  attrDialogVisible.value = true
}

const handleAttrSubmit = async () => {
  const valid = await attrFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEditingAttr.value && editingAttrId.value) {
      await updateAttribute(editingAttrId.value, attrForm)
      ElMessage.success('修改成功')
    } else {
      await addAttribute(attrForm)
      ElMessage.success('新增成功')
    }
    attrDialogVisible.value = false
    await loadAttributes()
  } catch {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const handleDelete = (row: AttributeItem) => {
  ElMessageBox.confirm(
    `确定删除属性「${row.name}」？如果该属性下有属性值则无法删除。`,
    '确认删除',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await deleteAttribute(row.id)
      ElMessage.success('删除成功')
      expandedIds.value.delete(row.id)
      await loadAttributes()
    } catch {
      // 拦截器已处理
    }
  }).catch(() => {})
}

// ===== 属性值 CRUD =====
const valueDialogVisible = ref(false)
const isEditingValue = ref(false)
const editingValueId = ref<number | null>(null)
const currentAttrId = ref<number | null>(null)
const currentAttrName = ref('')
const valueFormRef = ref()
const valueForm = reactive<AttributeValueForm>({
  attrId: 0,
  value: '',
  sort: 0
})
const valueRules = {
  value: [{ required: true, message: '请输入属性值名称', trigger: 'blur' }]
}

const resetValueForm = () => {
  valueForm.attrId = 0
  valueForm.value = ''
  valueForm.sort = 0
}

const showAddValueDialog = (row: AttributeItem) => {
  isEditingValue.value = false
  editingValueId.value = null
  currentAttrId.value = row.id
  currentAttrName.value = row.name
  resetValueForm()
  valueForm.attrId = row.id
  valueDialogVisible.value = true
}

const showEditValueDialog = (attr: AttributeItem, val: AttributeItem['values'][0]) => {
  isEditingValue.value = true
  editingValueId.value = val.valueId
  currentAttrId.value = attr.id
  currentAttrName.value = attr.name
  valueForm.attrId = attr.id
  valueForm.value = val.value
  valueForm.sort = val.sort
  valueDialogVisible.value = true
}

const handleValueSubmit = async () => {
  const valid = await valueFormRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (isEditingValue.value && editingValueId.value) {
      await updateAttributeValue(editingValueId.value, valueForm)
      ElMessage.success('修改成功')
    } else {
      await addAttributeValue(valueForm)
      ElMessage.success('新增成功')
    }
    valueDialogVisible.value = false
    await loadAttributes()
  } catch {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const handleDeleteValue = async (attr: AttributeItem, val: AttributeItem['values'][0]) => {
  ElMessageBox.confirm(
    `确定删除属性值「${val.value}」？如果该属性值已被商品引用则无法删除。`,
    '确认删除',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await deleteAttributeValue(val.valueId)
      ElMessage.success('删除成功')
      await loadAttributes()
    } catch {
      // 拦截器已处理
    }
  }).catch(() => {})
}

onMounted(() => {
  loadAttributes()
})
</script>

<style scoped>
.admin-attributes {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 24px;
  color: #1C1C1E;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* ===========================
   Bento 顶部
   =========================== */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 20px;
}

.welcome-card {
  grid-column: span 12;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border-radius: 20px;
  padding: 24px 28px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.welcome-desc {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 12px;
  padding: 10px 20px;
  border: 1px solid #1C1C1E;
  background: #1C1C1E;
  color: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.primary-btn:hover:not(:disabled) {
  background: #2C2C2E;
  border-color: #2C2C2E;
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.primary-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.primary-btn--gold {
  background: linear-gradient(135deg, #C8A464 0%, #ff8f1a 100%);
  border: none;
  color: #FFFFFF;
}

.primary-btn--gold:hover:not(:disabled) {
  background: linear-gradient(135deg, #D4B87A 0%, #ff9f2e 100%);
  box-shadow: 0 8px 20px rgba(200, 164, 100, 0.28);
}

.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 12px;
  padding: 10px 20px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.icon-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

/* ===========================
   统计卡
   =========================== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #FFFFFF;
  border-radius: 18px;
  padding: 20px 24px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.05);
}

.stat-card__icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-card__icon svg {
  width: 22px;
  height: 22px;
}

.stat-card__icon--total  { background: #F5F5F4; color: #C8A464; }
.stat-card__icon--sales  { background: rgba(255, 68, 0, 0.08); color: #ff4400; }
.stat-card__icon--basic  { background: rgba(59, 110, 110, 0.08); color: #3B6E6E; }
.stat-card__icon--values { background: rgba(99, 102, 241, 0.08); color: #6366F1; }

.stat-card__body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-card__label {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.stat-card__value {
  font-size: 22px;
  font-weight: 700;
  color: #1C1C1E;
  line-height: 1;
}

/* ===========================
   列表卡片
   =========================== */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.list-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-bottom: 1px solid #F0F0EE;
  flex-wrap: wrap;
}

.list-card__title {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.list-card__title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.list-card__title span {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.list-card__toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-tabs {
  display: flex;
  gap: 2px;
  background: #F0F0EE;
  border-radius: 10px;
  padding: 3px;
}

.filter-tab {
  font-size: 12px;
  font-weight: 600;
  padding: 6px 14px;
  border: none;
  border-radius: 7px;
  background: transparent;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.filter-tab:hover {
  color: #1C1C1E;
}

.filter-tab.active {
  background: #FFFFFF;
  color: #1C1C1E;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* ===========================
   属性项
   =========================== */
.attribute-list {
  display: flex;
  flex-direction: column;
}

.attribute-item {
  border-bottom: 1px solid #F0F0EE;
  transition: background 0.15s ease;
}

.attribute-item:last-child {
  border-bottom: none;
}

.attribute-item:hover {
  background: #FAFAF9;
}

.attribute-item--expanded {
  background: #FAFAF9;
}

.attribute-item__summary {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 24px;
  cursor: pointer;
}

.attr-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.attr-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.attr-title-row strong {
  font-size: 15px;
  font-weight: 600;
  color: #1C1C1E;
}

.attr-type-tag {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 20px;
}

.attr-type-tag--sales {
  color: #ff4400;
  background: rgba(255, 68, 0, 0.08);
}

.attr-type-tag--basic {
  color: #3B6E6E;
  background: rgba(59, 110, 110, 0.08);
}

.attr-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
}

.dot-separator {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #D4D4D4;
}

.attr-id {
  font-size: 11px;
  color: #C8A464;
  background: #FDF8EE;
  padding: 1px 6px;
  border-radius: 4px;
}

.attribute-item__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.expand-chevron {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #A1A1AA;
  transition: transform 0.25s ease, background 0.2s ease;
  flex-shrink: 0;
}

.attribute-item__summary:hover .expand-chevron {
  background: #F0F0EE;
  color: #6B6B6E;
}

.expand-chevron.expanded {
  transform: rotate(180deg);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 7px 12px;
  border-radius: 8px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.action-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

.action-btn--edit {
  color: #3B6E6E;
  border-color: rgba(59, 110, 110, 0.3);
  background: rgba(59, 110, 110, 0.06);
}

.action-btn--edit:hover {
  background: rgba(59, 110, 110, 0.12);
  border-color: rgba(59, 110, 110, 0.5);
}

.action-btn--add {
  color: #C8A464;
  border-color: rgba(200, 164, 100, 0.35);
  background: rgba(200, 164, 100, 0.08);
}

.action-btn--add:hover {
  background: rgba(200, 164, 100, 0.14);
  border-color: rgba(200, 164, 100, 0.55);
}

.action-btn--delete {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.06);
}

.action-btn--delete:hover {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.5);
}

/* ===========================
   展开详情
   =========================== */
.attribute-item__detail {
  padding: 0 24px 20px;
  animation: slideDown 0.2s ease both;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-6px); }
  to { opacity: 1; transform: translateY(0); }
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6B6B6E;
  font-size: 13px;
}

.detail-title strong {
  color: #1C1C1E;
  font-weight: 600;
}

.detail-title svg {
  color: #C8A464;
}

.text-link {
  font-size: 12px;
  font-weight: 600;
  color: #ff4400;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 0.2s ease;
}

.text-link:hover {
  color: #e63d00;
}

.value-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.value-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  font-size: 13px;
  transition: all 0.2s ease;
}

.value-chip:hover {
  border-color: #CCC;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.value-chip__name {
  font-weight: 600;
  color: #1C1C1E;
}

.value-chip__sort {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 500;
}

.value-chip__actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 4px;
}

.value-action {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  border: none;
  background: transparent;
  color: #A1A1AA;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.value-action:hover {
  background: #F0F0EE;
  color: #6B6B6E;
}

.value-action--danger:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.empty-values {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #A1A1AA;
  font-size: 13px;
  padding: 20px 0;
  background: #FFFFFF;
  border-radius: 12px;
  border: 1px dashed #E8E8E6;
}

/* ===========================
   空状态
   =========================== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 20px;
  text-align: center;
  color: #6B6B6E;
}

.empty-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: #F5F5F4;
  color: #A1A1AA;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.empty-icon svg {
  width: 28px;
  height: 28px;
}

.empty-state p {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 600;
  color: #1C1C1E;
}

.empty-state span {
  font-size: 12px;
  color: #A1A1AA;
}

/* ===========================
   弹窗表单
   =========================== */
.attr-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.attr-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.attr-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 10px;
}

.attr-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
  border-top: none;
}

.attr-dialog :deep(.el-form-item__label) {
  color: #6B6B6E;
  font-weight: 500;
}

.attr-dialog :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E8E8E6 inset;
  border-radius: 10px;
  padding: 0 12px;
}

.attr-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #C8A464 inset;
}

.attr-dialog :deep(.el-input-number__decrease),
.attr-dialog :deep(.el-input-number__increase) {
  border-color: #E8E8E6;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-readonly-tag {
  display: inline-flex;
  align-items: center;
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #ff4400;
  background: rgba(255, 68, 0, 0.08);
}

/* 单选卡片 */
.radio-cards {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.radio-card {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s ease;
}

.radio-card:hover {
  border-color: #CCC;
  background: #FAFAF9;
}

.radio-card.active {
  border-color: #ff4400;
  background: rgba(255, 68, 0, 0.04);
}

.radio-card input {
  display: none;
}

.radio-card__dot {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #D4D4D4;
  flex-shrink: 0;
  margin-top: 2px;
  position: relative;
  transition: border-color 0.2s ease;
}

.radio-card.active .radio-card__dot {
  border-color: #ff4400;
}

.radio-card.active .radio-card__dot::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: #ff4400;
}

.radio-card__title {
  font-size: 14px;
  font-weight: 600;
  color: #1C1C1E;
  display: block;
  margin-bottom: 2px;
}

.radio-card__desc {
  font-size: 12px;
  color: #A1A1AA;
  line-height: 1.4;
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #FFFFFF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .attribute-item__summary {
    flex-wrap: wrap;
  }

  .attribute-item__actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #F0F0EE;
  }
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stat-card {
    flex-direction: column;
    align-items: flex-start;
    padding: 16px;
  }

  .list-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .list-card__toolbar {
    width: 100%;
    justify-content: space-between;
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .attribute-item__actions,
  .attribute-item__summary {
    flex-direction: column;
    align-items: stretch;
  }

  .attribute-item__actions {
    gap: 6px;
  }

  .action-btn {
    justify-content: center;
  }

  .expand-chevron {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .primary-btn,
  .secondary-btn,
  .icon-btn,
  .stat-card,
  .attribute-item,
  .action-btn,
  .value-chip,
  .value-action,
  .radio-card,
  .expand-chevron {
    transition: none;
  }

  .attribute-item__detail {
    animation: none;
  }

  .btn-spinner {
    animation: none;
  }
}
</style>