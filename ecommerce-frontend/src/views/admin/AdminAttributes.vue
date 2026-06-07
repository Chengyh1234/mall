<template>
  <div class="admin-attributes">
    <!-- ===== 页面头部 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon-wrap">
          <el-icon class="header-icon"><List /></el-icon>
        </div>
        <div>
          <h1>属性管理</h1>
          <p class="header-desc">管理商品的销售属性和基本属性，支持属性值维护</p>
        </div>
      </div>
      <el-button type="primary" :icon="Plus" @click="showAddDialog">新增属性</el-button>
    </div>

    <!-- ===== 统计栏 ===== -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-value">{{ attributeList.length }}</span>
        <span class="stat-label">属性总数</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value" style="color: #409eff">{{ salesAttrsCount }}</span>
        <span class="stat-label">销售属性</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value" style="color: #67c23a">{{ basicAttrsCount }}</span>
        <span class="stat-label">基本属性</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value" style="color: #e6a23c">{{ totalValuesCount }}</span>
        <span class="stat-label">属性值总计</span>
      </div>
    </div>

    <!-- ===== 属性表格 ===== -->
    <div class="table-card">
      <el-table
        :data="attributeList"
        row-key="id"
        v-loading="loading"
        stripe
        class="attr-table"
        size="small"
      >
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="属性名称" min-width="140">
          <template #default="{ row }">
            <span class="attr-name-cell">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="属性类型" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.attrType === 1 ? 'primary' : 'success'"
              size="small"
              effect="light"
              round
            >
              {{ row.attrType === 1 ? '销售属性' : '基本属性' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="属性值数量" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain" round>
              {{ row.values?.length || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" link size="small" @click="showEditDialog(row)">
                <el-icon style="margin-right: 2px; vertical-align: -2px"><Edit /></el-icon>
                编辑
              </el-button>
              <el-divider direction="vertical" />
              <el-button type="success" link size="small" @click="showAddValueDialog(row)">
                <el-icon style="margin-right: 2px; vertical-align: -2px"><Plus /></el-icon>
                新增属性值
              </el-button>
              <el-divider direction="vertical" />
              <el-button type="danger" link size="small" @click="handleDelete(row)">
                <el-icon style="margin-right: 2px; vertical-align: -2px"><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>

        <!-- 展开行：属性值管理 -->
        <el-table-column type="expand" width="40">
          <template #default="{ row }">
            <div class="values-section">
              <div class="values-header">
                <el-icon color="#409eff"><Coin /></el-icon>
                <span><strong>{{ row.name }}</strong> 的属性值列表</span>
                <el-tag size="small" type="primary" effect="plain" round>
                  {{ row.values?.length || 0 }} 项
                </el-tag>
              </div>
              <el-table
                v-if="row.values && row.values.length > 0"
                :data="row.values"
                size="small"
                stripe
                class="value-table"
              >
                <el-table-column prop="id" label="ID" width="60" align="center" />
                <el-table-column prop="value" label="属性值" min-width="160">
                  <template #default="{ row: val }">
                    <span class="value-name-cell">{{ val.value }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="sort" label="排序" width="80" align="center" />
                <el-table-column label="操作" width="160">
                  <template #default="{ row: val }">
                    <el-button type="primary" link size="small" @click="showEditValueDialog(row, val)">
                      <el-icon style="margin-right: 2px; vertical-align: -2px"><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-divider direction="vertical" />
                    <el-button type="danger" link size="small" @click="handleDeleteValue(row, val)">
                      <el-icon style="margin-right: 2px; vertical-align: -2px"><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-else class="empty-values">
                <el-icon color="#c9cdd4" size="20"><Warning /></el-icon>
                <span>暂无属性值，点击「新增属性值」添加</span>
              </div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑属性弹窗 -->
    <el-dialog
      v-model="attrDialogVisible"
      :title="isEditingAttr ? '编辑属性' : '新增属性'"
      width="500px"
      :close-on-click-modal="false"
      class="form-dialog"
    >
      <div class="dialog-body">
        <el-form ref="attrFormRef" :model="attrForm" :rules="attrRules" label-width="100px">
          <el-form-item label="属性名称" prop="name">
            <el-input v-model="attrForm.name" placeholder="请输入属性名称，如：颜色、尺寸" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="属性类型" prop="attrType">
            <el-radio-group v-model="attrForm.attrType">
              <el-radio :value="1">
                <el-tag size="small" type="primary" effect="plain" round>销售属性</el-tag>
              </el-radio>
              <el-radio :value="2">
                <el-tag size="small" type="success" effect="plain" round>基本属性</el-tag>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="attrForm.sort" :min="0" controls-position="right" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="attrDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAttrSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑属性值弹窗 -->
    <el-dialog
      v-model="valueDialogVisible"
      :title="isEditingValue ? '编辑属性值' : '新增属性值'"
      width="500px"
      :close-on-click-modal="false"
      class="form-dialog"
    >
      <div class="dialog-body">
        <el-form ref="valueFormRef" :model="valueForm" :rules="valueRules" label-width="100px">
          <el-form-item label="所属属性">
            <el-tag type="primary" effect="light" round>
              {{ currentAttrName }}
            </el-tag>
          </el-form-item>
          <el-form-item label="属性值" prop="value">
            <el-input v-model="valueForm.value" placeholder="请输入属性值，如：红色、XL" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="valueForm.sort" :min="0" controls-position="right" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="valueDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleValueSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, List, Coin, Warning
} from '@element-plus/icons-vue'
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

// 当前正在操作的属性名称（用于弹窗显示）
const currentAttrName = ref('')

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
  editingValueId.value = val.id
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
      await deleteAttributeValue(val.id)
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
/* ===== 页面布局 ===== */
.admin-attributes {
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
  background: #f5f7fa;
}

/* ===== 页面头部 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.header-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  background: linear-gradient(135deg, #409eff20, #337ecc10);
  display: flex;
  align-items: center;
  justify-content: center;
}
.header-icon {
  font-size: 22px;
  color: #409eff;
}
.page-header h1 {
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: #1d2129;
}
.header-desc {
  margin: 0;
  font-size: 13px;
  color: #86909c;
}

/* ===== 统计栏 ===== */
.stats-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 18px 24px;
  margin-bottom: 20px;
}
.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  line-height: 1.2;
}
.stat-label {
  font-size: 12px;
  color: #86909c;
}
.stat-divider {
  width: 1px;
  height: 36px;
  background: #f0f0f0;
  flex-shrink: 0;
}

/* ===== 表格卡片 ===== */
.table-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.attr-table {
  border-radius: 6px;
}
.attr-table :deep(.el-table__header-wrapper th) {
  background: #fafafa;
  color: #4e5969;
  font-weight: 500;
  font-size: 13px;
}
.attr-table :deep(.el-table__body tr:hover > td) {
  background: #f5f9ff;
}
.attr-name-cell {
  color: #1d2129;
  font-weight: 500;
}

/* 操作按钮组 */
.action-btns {
  display: flex;
  align-items: center;
  gap: 0;
}
.action-btns :deep(.el-divider--vertical) {
  height: 12px;
  margin: 0 2px;
  background: #e5e6eb;
}

/* ===== 展开行：属性值 ===== */
.values-section {
  padding: 16px 24px;
  background: #fafcff;
  border-radius: 8px;
  margin: 8px 0;
}
.values-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 14px;
  color: #4e5969;
}
.values-header strong {
  color: #1d2129;
}

.value-table :deep(.el-table__header-wrapper th) {
  background: #f5f7fa;
  color: #4e5969;
  font-weight: 500;
  font-size: 12px;
}
.value-name-cell {
  color: #1d2129;
  font-weight: 500;
}

.empty-values {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #86909c;
  padding: 24px 0;
  font-size: 13px;
}

/* ===== 弹窗 ===== */
.form-dialog :deep(.el-dialog__header) {
  padding: 18px 24px 14px;
  border-bottom: 1px solid #f2f3f5;
}
.form-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}
.dialog-body {
  padding: 4px 0;
}
.dialog-body :deep(.el-form-item__label) {
  color: #4e5969;
  font-weight: 500;
}
.dialog-body :deep(.el-radio) {
  margin-right: 12px;
}
</style>