<template>
  <div class="admin-categories">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>分类管理</h1>
        <span class="header-subtitle">管理商品分类结构，支持多级分类与图标上传</span>
      </div>
      <div class="header-actions">
        <el-input
          v-model="filterText"
          placeholder="搜索分类名称..."
          prefix-icon="Search"
          clearable
          class="search-input"
        />
        <el-button type="primary" size="large" @click="openAddDialog">
          <el-icon style="margin-right: 6px;"><Plus /></el-icon>
          新增分类
        </el-button>
      </div>
    </div>

    <!-- 分类统计 -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-value">{{ totalCount }}</span>
        <span class="stat-label">全部分类</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value">{{ level1Count }}</span>
        <span class="stat-label">一级分类</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value">{{ level2Count }}</span>
        <span class="stat-label">二级分类</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value">{{ level3Count }}</span>
        <span class="stat-label">三级分类</span>
      </div>
      <div class="stat-divider" />
      <div class="stat-item">
        <span class="stat-value">{{ activeCount }}</span>
        <span class="stat-label">已启用</span>
      </div>
    </div>

    <!-- 分类树表格 -->
    <div class="table-container">
      <el-table
        :data="flatCategoryList"
        row-key="id"
        v-loading="loading"
        stripe
        default-expand-all
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        empty-text="暂无分类数据"
        class="category-table"
      >
        <el-table-column label="分类名称" min-width="280">
          <template #default="{ row }">
            <div class="category-name-cell" :style="{ paddingLeft: ((row.level || 1) - 1) * 30 + 'px' }">
              <!-- 层级标签 -->
              <span class="level-badge" :class="'level-' + (row.level || 1)">
                Lv.{{ row.level || 1 }}
              </span>
              <!-- 分类图标 -->
              <el-image
                v-if="row.icon"
                :src="getCategoryIconUrl(row.icon)"
                class="cat-icon"
                fit="cover"
                :preview-src-list="[getCategoryIconUrl(row.icon)]"
                preview-teleported
              >
                <template #error>
                  <span class="icon-emoji">{{ getLevelIcon(row.level || 1) }}</span>
                </template>
              </el-image>
              <span v-else class="icon-emoji">{{ getLevelIcon(row.level || 1) }}</span>
              <!-- 名称 -->
              <span class="name-text">{{ row.name }}</span>
              <!-- 根标记 -->
              <el-tag
                v-if="!row.parentId || row.parentId === 0"
                size="small"
                type="info"
                effect="plain"
                class="root-tag"
              >根分类</el-tag>
              <!-- 子分类数量 -->
              <el-tag
                v-if="row.children?.length"
                size="small"
                type=""
                effect="plain"
                class="child-count"
              >{{ row.children.length }}个子类</el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所属父级" width="160">
          <template #default="{ row }">
            <span v-if="row.parentId" class="parent-text">
              <el-icon style="margin-right: 4px; vertical-align: middle;"><ArrowUpBold /></el-icon>
              {{ getParentName(row.parentId) }}
            </span>
            <span v-else class="parent-text root-parent">— 根节点 —</span>
          </template>
        </el-table-column>

        <el-table-column label="图标" width="70" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.icon"
              :src="getCategoryIconUrl(row.icon)"
              style="width: 30px; height: 30px; border-radius: 6px; border: 1px solid #f0f0f0;"
              fit="cover"
              :preview-src-list="[getCategoryIconUrl(row.icon)]"
              preview-teleported
            >
              <template #error>
                <div class="icon-placeholder">📁</div>
              </template>
            </el-image>
            <div v-else class="icon-placeholder">📁</div>
          </template>
        </el-table-column>

        <el-table-column prop="sort" label="排序" width="70" align="center" />

        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              :loading="switchingId === row.id"
              @change="(val: boolean) => handleStatusChange(row, val)"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
              size="small"
            />
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="165">
          <template #default="{ row }">
            <span class="time-text">{{ row.createdAt || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button type="primary" link size="small" @click="openEditDialog(row)">
                <el-icon style="margin-right: 4px;"><Edit /></el-icon>编辑
              </el-button>
              <el-button type="success" link size="small" @click="openAddChildDialog(row)">
                <el-icon style="margin-right: 4px;"><Plus /></el-icon>子分类
              </el-button>
              <el-popconfirm
                title="确定删除该分类？子分类将一并删除"
                width="220"
                confirm-button-text="确定删除"
                cancel-button-text="取消"
                @confirm="handleDelete(row)"
              >
                <template #reference>
                  <el-button type="danger" link size="small">
                    <el-icon style="margin-right: 4px;"><Delete /></el-icon>删除
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑分类弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑分类' : isAddingChild ? '新增子分类' : '新增分类'"
      width="580px"
      :close-on-click-modal="false"
      class="category-dialog"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
        label-position="left"
        class="category-form"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入分类名称"
            maxlength="20"
            show-word-limit
            size="large"
          />
        </el-form-item>

        <el-form-item label="父级分类" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="categoryTree"
            :props="{
              label: 'name',
              value: 'id',
              children: 'children',
              disabled: (data: Category) => isEditing && data.id === editingId
            }"
            placeholder="选择父级分类（不选则为根分类）"
            clearable
            filterable
            size="large"
            style="width: 100%"
            check-strictly
          />
        </el-form-item>

        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" size="large" />
        </el-form-item>

        <el-form-item label="状态">
          <el-switch v-model="form.statusBool" active-text="启用" inactive-text="禁用" size="large" />
        </el-form-item>

        <el-form-item label="图标">
          <div class="icon-upload-area">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/jpeg,image/png,image/gif,image/webp"
              :on-change="handleIconChange"
            >
              <template #trigger>
                <div class="icon-upload-box">
                  <el-image
                    v-if="iconPreviewUrl"
                    :src="iconPreviewUrl"
                    style="width: 100%; height: 100%; border-radius: 8px;"
                    fit="cover"
                  />
                  <div v-else class="icon-upload-placeholder">
                    <el-icon :size="24"><Plus /></el-icon>
                    <span>上传图标</span>
                  </div>
                </div>
              </template>
            </el-upload>
            <div class="icon-info">
              <p class="icon-info-title">分类图标</p>
              <p class="icon-info-desc">建议尺寸 128×128px，支持 JPG/PNG/WebP</p>
              <el-button
                v-if="iconPreviewUrl"
                type="danger"
                link
                size="small"
                @click="removeIcon"
              >
                清除图标
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" size="large" :loading="saving" @click="handleSubmit">
            {{ isEditing ? '保存修改' : '确认添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, type FormInstance, type UploadInstance, type UploadRawFile } from 'element-plus'
import { Plus, Edit, Delete, Search, ArrowUpBold } from '@element-plus/icons-vue'
import {
  getCategoryTree,
  addCategory,
  updateCategory,
  deleteCategory,
  getCategoryIconUrl,
  type Category
} from '@/api/category'

// ===== 状态 =====
const loading = ref(false)
const saving = ref(false)
const switchingId = ref<number | null>(null)
const filterText = ref('')
const categoryTree = ref<Category[]>([])
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
const dialogVisible = ref(false)
const isEditing = ref(false)
const isAddingChild = ref(false)
const editingId = ref<number | null>(null)

// ===== 表单 =====
const form = ref({
  name: '',
  parentId: undefined as number | undefined,
  sort: 0,
  statusBool: true,
  iconFile: null as File | null
})
const iconPreviewUrl = ref('')

const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 20, message: '名称长度不能超过20个字符', trigger: 'blur' }
  ]
}

// ===== 计算 =====
// 扁平化树为列表（树表格使用）
const flatCategoryList = computed(() => {
  return categoryTree.value
})

// 统计数据
const totalCount = computed(() => countNodes(categoryTree.value))
const level1Count = computed(() => categoryTree.value.length)
const level2Count = computed(() => countByLevel(categoryTree.value, 2))
const level3Count = computed(() => countByLevel(categoryTree.value, 3))
const activeCount = computed(() => countActive(categoryTree.value))

function countNodes(nodes: Category[]): number {
  let count = 0
  for (const node of nodes) {
    count++
    if (node.children?.length) {
      count += countNodes(node.children)
    }
  }
  return count
}

function countByLevel(nodes: Category[], targetLevel: number): number {
  let count = 0
  for (const node of nodes) {
    if (node.children?.length) {
      count += countByLevel(node.children, targetLevel)
    }
    if ((node.level || 1) === targetLevel) {
      count++
    }
  }
  // 只算指定级别的
  return count
}

function countActive(nodes: Category[]): number {
  let count = 0
  for (const node of nodes) {
    if (node.status === 1) count++
    if (node.children?.length) {
      count += countActive(node.children)
    }
  }
  return count
}

// ===== 树形辅助 =====
// 构建 id -> name 映射（用于查找父级名称）
const parentMap = computed(() => {
  const map: Record<number, string> = {}
  function walk(nodes: Category[]) {
    for (const node of nodes) {
      map[node.id] = node.name
      if (node.children?.length) walk(node.children)
    }
  }
  walk(categoryTree.value)
  return map
})

// 获取父级名称
function getParentName(parentId: number): string {
  return parentMap.value[parentId] || `ID:${parentId}`
}

// 获取层级图标
function getLevelIcon(level: number): string {
  const icons = ['📂', '📁', '📄', '📃']
  return icons[Math.min(level - 1, icons.length - 1)] || '📁'
}

// ===== 加载数据 =====
const loadCategories = async () => {
  loading.value = true
  try {
    categoryTree.value = (await getCategoryTree()) || []
  } catch {
    categoryTree.value = []
  } finally {
    loading.value = false
  }
}

// ===== 弹窗操作 =====
const resetForm = () => {
  form.value = {
    name: '',
    parentId: undefined,
    sort: 0,
    statusBool: true,
    iconFile: null
  }
  iconPreviewUrl.value = ''
  isEditing.value = false
  isAddingChild.value = false
  editingId.value = null
}

const openAddDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const openAddChildDialog = (parent: Category) => {
  resetForm()
  isAddingChild.value = true
  form.value.parentId = parent.id
  form.value.sort = 0
  dialogVisible.value = true
}

const openEditDialog = async (row: Category) => {
  resetForm()
  isEditing.value = true
  editingId.value = row.id
  form.value.name = row.name
  form.value.parentId = row.parentId || undefined
  form.value.sort = row.sort ?? 0
  form.value.statusBool = row.status === 1
  if (row.icon) {
    iconPreviewUrl.value = getCategoryIconUrl(row.icon)
  }
  dialogVisible.value = true
}

// ===== 图标上传 =====
const handleIconChange = (file: { raw: UploadRawFile; name: string }) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 格式')
    return false
  }
  if (file.raw.size > 2 * 1024 * 1024) {
    ElMessage.warning('图标大小不能超过 2MB')
    return false
  }
  form.value.iconFile = file.raw
  iconPreviewUrl.value = URL.createObjectURL(file.raw)
}

const removeIcon = () => {
  form.value.iconFile = null
  iconPreviewUrl.value = ''
}

// ===== 提交 =====
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateCategory({
        id: editingId.value,
        name: form.value.name,
        parentId: form.value.parentId,
        sort: form.value.sort,
        status: form.value.statusBool ? 1 : 0,
        iconFile: form.value.iconFile || undefined
      })
      ElMessage.success('分类更新成功')
    } else {
      // 计算 level：如果有 parentId，找父级的 level + 1
      let level = 1
      if (form.value.parentId) {
        level = findLevel(categoryTree.value, form.value.parentId) + 1
      }
      await addCategory({
        name: form.value.name,
        parentId: form.value.parentId,
        level,
        sort: form.value.sort,
        status: form.value.statusBool ? 1 : 0,
        iconFile: form.value.iconFile || undefined
      })
      ElMessage.success('分类添加成功')
    }
    dialogVisible.value = false
    await loadCategories()
  } catch {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

// 在树中查找指定 ID 的节点的 level
function findLevel(nodes: Category[], id: number): number {
  for (const node of nodes) {
    if (node.id === id) return node.level || 1
    if (node.children?.length) {
      const found = findLevel(node.children, id)
      if (found > 0) return found
    }
  }
  return 0
}

// ===== 删除 =====
const handleDelete = async (row: Category) => {
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch {
    // 拦截器已处理
  }
}

// ===== 状态切换 =====
const handleStatusChange = async (row: Category, val: boolean) => {
  switchingId.value = row.id
  try {
    await updateCategory({
      id: row.id,
      status: val ? 1 : 0
    })
    ElMessage.success(val ? '已启用' : '已禁用')
    await loadCategories()
  } catch {
    // 拦截器已处理
  } finally {
    switchingId.value = null
  }
}

// ===== 搜索过滤 =====
watch(filterText, (val) => {
  if (!val) {
    loadCategories()
    return
  }
  // 简单本地过滤
  const filterTree = (nodes: Category[]): Category[] => {
    return nodes
      .filter(node => node.name.includes(val) || (node.children?.length && filterTree(node.children).length > 0))
      .map(node => ({
        ...node,
        children: node.children?.length ? filterTree(node.children) : []
      }))
  }
  categoryTree.value = filterTree(JSON.parse(JSON.stringify(categoryTree.value)))
})

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
/* ===== 页面布局 ===== */
.admin-categories {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

/* ===== 页面头部 ===== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left h1 {
  font-size: 26px;
  margin: 0 0 6px 0;
  color: #1a1a2e;
  font-weight: 700;
}

.header-subtitle {
  font-size: 14px;
  color: #888;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

/* ===== 统计栏 ===== */
.stats-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 12px;
  padding: 16px 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  padding: 4px 0;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.4;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 36px;
  background: #eee;
  flex-shrink: 0;
}

/* ===== 表格容器 ===== */
.table-container {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.category-table :deep(.el-table__header th) {
  background: #f8f9fc;
  color: #555;
  font-weight: 600;
  font-size: 13px;
}

.category-table :deep(.el-table__row) {
  transition: background-color 0.2s;
}

.category-table :deep(.el-table__row:hover) {
  background-color: #f0f7ff !important;
}

/* 不同层级行背景色微差 */
.category-table :deep(.el-table__row) .level-1 {
  --row-bg: transparent;
}
.category-table :deep(.el-table__row) .level-2 {
  --row-bg: transparent;
}
.category-table :deep(.el-table__row) .level-3 {
  --row-bg: transparent;
}

/* ===== 分类名称列 ===== */
.category-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

/* 层级标签 */
.level-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  height: 20px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  padding: 0 5px;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.level-badge.level-1 {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.level-badge.level-2 {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.level-badge.level-3 {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.cat-icon {
  width: 22px;
  height: 22px;
  border-radius: 4px;
  flex-shrink: 0;
}

.icon-emoji {
  font-size: 18px;
  line-height: 1;
  flex-shrink: 0;
}

.name-text {
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
}

.root-tag {
  margin-left: 2px;
  flex-shrink: 0;
}

.child-count {
  margin-left: 2px;
  flex-shrink: 0;
  background: #f0f5ff;
  border-color: #adc6ff;
  color: #2f54eb;
}

/* ===== 所属父级列 ===== */
.parent-text {
  font-size: 13px;
  color: #666;
  display: inline-flex;
  align-items: center;
}

.parent-text.root-parent {
  color: #bbb;
  font-style: italic;
}

/* ===== 其他列 ===== */
.time-text {
  color: #999;
  font-size: 13px;
}

.icon-placeholder {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  background: #f5f5f5;
  border-radius: 6px;
}

/* ===== 操作按钮 ===== */
.action-btns {
  display: flex;
  align-items: center;
  gap: 2px;
}

.action-btns .el-button {
  font-size: 13px;
}

/* ===== 弹窗 ===== */
.category-dialog :deep(.el-dialog__header) {
  padding: 24px 24px 0;
  font-size: 18px;
}

.category-dialog :deep(.el-dialog__body) {
  padding: 20px 24px;
}

.category-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.category-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #444;
}

/* 图标上传区域 */
.icon-upload-area {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.icon-upload-box {
  width: 80px;
  height: 80px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.icon-upload-box:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.icon-upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #bbb;
  font-size: 12px;
}

.icon-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 4px;
}

.icon-info-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.icon-info-desc {
  margin: 0;
  font-size: 12px;
  color: #999;
}
</style>