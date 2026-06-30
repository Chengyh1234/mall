<template>
  <div class="admin-categories">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">分类管理</h1>
          <p class="welcome-desc">管理商品多级分类结构，支持图标上传、排序与状态控制。</p>
        </div>
        <button class="primary-btn primary-btn--gold" @click="openAddDialog">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          新增分类
        </button>
      </div>
    </section>

    <!-- 统计卡 -->
    <section class="stats-row">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--total">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">全部分类</span>
          <span class="stat-card__value">{{ totalCount }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--level1">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">一级分类</span>
          <span class="stat-card__value">{{ level1Count }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--level2">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
            <circle cx="9" cy="7" r="4"/>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">二级分类</span>
          <span class="stat-card__value">{{ level2Count }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--level3">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">三级分类</span>
          <span class="stat-card__value">{{ level3Count }}</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--active">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-card__body">
          <span class="stat-card__label">已启用</span>
          <span class="stat-card__value">{{ activeCount }}</span>
        </div>
      </div>
    </section>

    <!-- 工具栏 -->
    <section class="list-card">
      <div class="list-card__header">
        <div class="list-card__title">
          <h2>分类结构</h2>
          <span>{{ filteredTree.length }} 个根分类</span>
        </div>
        <div class="list-card__toolbar">
          <div class="category-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input
              v-model="filterText"
              type="text"
              placeholder="搜索分类名称..."
              @input="onFilterInput"
            />
          </div>
          <button class="icon-btn" title="刷新" @click="loadCategories">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- 分类树列表 -->
      <div class="category-list" v-loading="loading">
        <div
          v-for="node in renderedTree"
          :key="node.id"
          class="category-node"
          :style="{ '--level': node.level || 1 }"
        >
          <div
            class="category-node__bar"
            :class="{
              'category-node__bar--disabled': node.status !== 1,
              'category-node__bar--parent': !!node.children?.length,
              'category-node__bar--leaf': !node.children?.length
            }"
          >
            <div
              class="category-node__main"
              :class="{
                'category-node__main--parent': !!node.children?.length,
                'category-node__main--leaf': !node.children?.length
              }"
              @click="toggleExpand(node.id)"
            >
              <div class="category-node__indent" :style="{ width: ((node.level || 1) - 1) * 28 + 'px' }" />

              <button
                v-if="node.children?.length"
                class="expand-btn"
                :class="{ expanded: expandedIds.has(node.id) }"
                @click.stop="toggleExpand(node.id)"
              >
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </button>
              <span v-else class="expand-placeholder" />

              <span class="level-badge" :class="'level-badge--' + (node.level || 1)">
                Lv.{{ node.level || 1 }}
              </span>

              <div class="category-icon" :class="{ 'category-icon--folder': !!node.children?.length }">
                <el-image
                  v-if="node.icon"
                  :src="getCategoryIconUrl(node.icon)"
                  fit="cover"
                  loading="lazy"
                  :preview-src-list="[getCategoryIconUrl(node.icon)]"
                  hide-on-click-modal
                />
                <svg
                  v-else
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path v-if="node.children?.length" d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                  <path v-else d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                </svg>
              </div>

              <span class="category-name">{{ node.name }}</span>

              <span v-if="!node.parentId || node.parentId === 0" class="root-tag">根分类</span>
              <span v-if="node.children?.length" class="child-count">{{ node.children.length }} 子类</span>
            </div>

            <div class="category-node__meta">
              <span class="parent-name" v-if="node.parentId">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="18 15 12 9 6 15"/>
                </svg>
                {{ getParentName(node.parentId) }}
              </span>
              <span class="sort-value">排序 {{ node.sort }}</span>
              <span class="time-value">{{ node.createdAt || '-' }}</span>
            </div>

            <div class="category-node__actions">
              <label class="toggle-switch" :class="{ checked: node.status === 1 }">
                <input
                  type="checkbox"
                  :checked="node.status === 1"
                  :disabled="switchingId === node.id"
                  @change="handleStatusChange(node, !node.status || node.status === 0)"
                />
                <span class="toggle-slider" />
                <span class="toggle-label">{{ node.status === 1 ? '启用' : '禁用' }}</span>
              </label>
              <button class="action-btn action-btn--edit" @click="openEditDialog(node)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                编辑
              </button>
              <button class="action-btn action-btn--add" @click="openAddChildDialog(node)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                子分类
              </button>
              <button class="action-btn action-btn--delete" @click="handleDelete(node)">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="renderedTree.length === 0 && !loading" class="empty-state">
        <div class="empty-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
        </div>
        <p>暂无分类数据</p>
        <span>点击右上角「新增分类」按钮开始创建</span>
      </div>
    </section>

    <!-- 新增/编辑分类弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑分类' : isAddingChild ? '新增子分类' : '新增分类'"
      width="560px"
      :close-on-click-modal="false"
      class="category-dialog"
      destroy-on-close
    >
      <div class="dialog-body">
        <el-form
          ref="formRef"
          :model="form"
          :rules="formRules"
          label-width="90px"
          label-position="left"
        >
          <el-form-item label="分类名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入分类名称"
              maxlength="20"
              show-word-limit
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
              style="width: 100%"
              check-strictly
            />
          </el-form-item>

          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" :max="9999" />
          </el-form-item>

          <el-form-item label="状态">
            <div class="radio-cards">
              <label
                class="radio-card"
                :class="{ active: form.statusBool }"
                @click="form.statusBool = true"
              >
                <input type="radio" :value="true" v-model="form.statusBool" />
                <span class="radio-card__dot" />
                <div>
                  <span class="radio-card__title">启用</span>
                  <span class="radio-card__desc">分类将在前台展示</span>
                </div>
              </label>
              <label
                class="radio-card"
                :class="{ active: !form.statusBool }"
                @click="form.statusBool = false"
              >
                <input type="radio" :value="false" v-model="form.statusBool" />
                <span class="radio-card__dot" />
                <div>
                  <span class="radio-card__title">禁用</span>
                  <span class="radio-card__desc">分类暂时隐藏</span>
                </div>
              </label>
            </div>
          </el-form-item>

          <el-form-item label="图标">
            <div class="icon-upload-area">
              <div class="upload-box" @click="triggerUpload">
                <input
                  ref="fileInputRef"
                  type="file"
                  accept="image/jpeg,image/png,image/gif,image/webp"
                  class="upload-input"
                  @change="handleIconChange"
                />
                <template v-if="iconPreviewUrl">
                  <img :src="iconPreviewUrl" alt="图标预览" class="upload-preview" />
                  <div class="upload-overlay">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.3"/>
                    </svg>
                    <span>更换</span>
                  </div>
                </template>
                <div v-else class="upload-placeholder">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19"/>
                    <line x1="5" y1="12" x2="19" y2="12"/>
                  </svg>
                  <span>上传图标</span>
                </div>
              </div>
              <div class="icon-info">
                <strong>分类图标</strong>
                <span>建议尺寸 128×128px，支持 JPG/PNG/WebP，最大 2MB</span>
                <button v-if="iconPreviewUrl" type="button" class="text-link text-link--danger" @click="removeIcon">
                  清除图标
                </button>
              </div>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="dialogVisible = false">取消</button>
          <button class="primary-btn" :disabled="saving" @click="handleSubmit">
            <span v-if="saving" class="btn-spinner" />
            {{ saving ? '保存中...' : (isEditing ? '保存修改' : '确认添加') }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import {
  getCategoryTree,
  addCategory,
  updateCategory,
  deleteCategory,
  getCategoryIconUrl,
  type Category
} from '@/api/category'

const loading = ref(false)
const saving = ref(false)
const switchingId = ref<number | null>(null)
const filterText = ref('')
const categoryTree = ref<Category[]>([])
const filteredTree = ref<Category[]>([])
const formRef = ref<FormInstance>()
const fileInputRef = ref<HTMLInputElement | null>(null)
const dialogVisible = ref(false)
const isEditing = ref(false)
const isAddingChild = ref(false)
const editingId = ref<number | null>(null)
const expandedIds = ref<Set<number>>(new Set())

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

// ===== 数据规范化（后端可能不返回 level/status，前端根据树结构补全） =====
function normalizeTree(nodes: Category[], parentLevel = 0): Category[] {
  return nodes.map(node => ({
    ...node,
    // 根据在树中的真实深度推断 level，不依赖后端字段
    level: parentLevel + 1,
    // 后端未返回状态时，默认视为启用
    status: node.status === 0 ? 0 : 1,
    children: node.children?.length ? normalizeTree(node.children, parentLevel + 1) : []
  }))
}

// ===== 统计 =====
// 基于扁平化后的节点计算，避免递归时遗漏/重复
const flatNodes = computed(() => {
  const list: Category[] = []
  const walk = (nodes: Category[]) => {
    for (const node of nodes) {
      list.push(node)
      if (node.children?.length) walk(node.children)
    }
  }
  walk(categoryTree.value)
  return list
})

const totalCount = computed(() => flatNodes.value.length)
const level1Count = computed(() => flatNodes.value.filter(n => n.level === 1).length)
const level2Count = computed(() => flatNodes.value.filter(n => n.level === 2).length)
const level3Count = computed(() => flatNodes.value.filter(n => n.level === 3).length)
const activeCount = computed(() => flatNodes.value.filter(n => n.status === 1).length)

// ===== 父级名称映射 =====
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

function getParentName(parentId: number): string {
  return parentMap.value[parentId] || `ID:${parentId}`
}

// ===== 树渲染 =====
const renderedTree = computed(() => {
  const result: Category[] = []
  const walk = (nodes: Category[]) => {
    for (const node of nodes) {
      result.push(node)
      if (node.children?.length && expandedIds.value.has(node.id)) {
        walk(node.children)
      }
    }
  }
  walk(filteredTree.value)
  return result
})

const toggleExpand = (id: number) => {
  const next = new Set(expandedIds.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedIds.value = next
}

// ===== 数据加载 =====
const loadCategories = async () => {
  loading.value = true
  try {
    const raw = (await getCategoryTree()) || []
    categoryTree.value = normalizeTree(raw)
    applyFilter()
  } catch {
    categoryTree.value = []
    filteredTree.value = []
  } finally {
    loading.value = false
  }
}

// ===== 搜索过滤 =====
let filterTimer: ReturnType<typeof setTimeout> | null = null
const onFilterInput = () => {
  if (filterTimer) clearTimeout(filterTimer)
  filterTimer = setTimeout(() => {
    applyFilter()
  }, 200)
}

const applyFilter = () => {
  const val = filterText.value.trim()
  if (!val) {
    filteredTree.value = categoryTree.value
    return
  }
  const filterTree = (nodes: Category[]): Category[] => {
    return nodes
      .filter(node => node.name.includes(val) || (node.children?.length && filterTree(node.children).length > 0))
      .map(node => ({
        ...node,
        children: node.children?.length ? filterTree(node.children) : []
      }))
  }
  filteredTree.value = filterTree(JSON.parse(JSON.stringify(categoryTree.value)))
  // 搜索时展开所有结果
  const allIds = new Set<number>()
  const collect = (nodes: Category[]) => {
    for (const node of nodes) {
      if (node.children?.length) {
        allIds.add(node.id)
        collect(node.children)
      }
    }
  }
  collect(filteredTree.value)
  expandedIds.value = allIds
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
  if (fileInputRef.value) fileInputRef.value.value = ''
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
  dialogVisible.value = true
}

const openEditDialog = (row: Category) => {
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
const triggerUpload = () => {
  fileInputRef.value?.click()
}

const handleIconChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 格式')
    if (fileInputRef.value) fileInputRef.value.value = ''
    return
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图标大小不能超过 2MB')
    if (fileInputRef.value) fileInputRef.value.value = ''
    return
  }

  if (iconPreviewUrl.value && iconPreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(iconPreviewUrl.value)
  }
  form.value.iconFile = file
  iconPreviewUrl.value = URL.createObjectURL(file)
}

const removeIcon = () => {
  if (iconPreviewUrl.value && iconPreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(iconPreviewUrl.value)
  }
  form.value.iconFile = null
  iconPreviewUrl.value = ''
  if (fileInputRef.value) fileInputRef.value.value = ''
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
    await ElMessageBox.confirm(
      `确定删除分类「${row.name}」吗？子分类将一并删除，且可能影响下挂商品。`,
      '确认删除',
      { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
    )
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 拦截器已处理
    }
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

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.admin-categories {
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
  grid-template-columns: repeat(5, minmax(0, 1fr));
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

.stat-card__icon--total   { background: #F5F5F4; color: #C8A464; }
.stat-card__icon--level1 { background: rgba(255, 68, 0, 0.08); color: #ff4400; }
.stat-card__icon--level2 { background: rgba(59, 110, 110, 0.08); color: #3B6E6E; }
.stat-card__icon--level3 { background: rgba(200, 164, 100, 0.12); color: #B89040; }
.stat-card__icon--active { background: rgba(34, 197, 94, 0.08); color: #22c55e; }

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

.category-search {
  position: relative;
}

.category-search svg {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #A1A1AA;
  pointer-events: none;
}

.category-search input {
  width: 220px;
  height: 36px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  padding: 0 12px 0 34px;
  font-size: 13px;
  color: #1C1C1E;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.category-search input:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.1);
}

/* ===========================
   分类树节点
   =========================== */
.category-list {
  display: flex;
  flex-direction: column;
  padding: 8px 0;
}

.category-node {
  --level: 1;
  position: relative;
}

/* 层级引导线：除根节点外，左侧绘制一条连接父级的细线 */
.category-node::before {
  content: '';
  position: absolute;
  left: calc(24px + ((var(--level) - 2) * 28px) + 10px);
  top: 0;
  width: 1px;
  height: 100%;
  background: #E8E8E6;
  pointer-events: none;
  z-index: 0;
}

.category-node__bar {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  margin: 0 12px;
  border-radius: 12px;
  transition: background 0.15s ease, box-shadow 0.15s ease;
  border-left: 3px solid transparent;
}

.category-node__bar:hover {
  background: #FAFAF9;
}

/* 父分类：左侧主题色竖线 + 略深背景 */
.category-node__bar--parent {
  border-left-color: #C8A464;
  background: #FDFCFA;
}

.category-node__bar--parent:hover {
  background: #F8F5EF;
}

/* 子分类：更轻盈 */
.category-node__bar--leaf {
  border-left-color: transparent;
}

.category-node__bar--disabled {
  opacity: 0.65;
}

.category-node__bar--disabled .category-icon,
.category-node__bar--disabled .category-name {
  opacity: 0.7;
}

.category-node__main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.category-node__indent {
  flex-shrink: 0;
}

.expand-btn,
.expand-placeholder {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.expand-btn {
  border-radius: 6px;
  border: none;
  background: transparent;
  color: #A1A1AA;
  cursor: pointer;
  transition: all 0.2s ease;
}

.expand-btn:hover {
  background: #F0F0EE;
  color: #6B6B6E;
}

.expand-btn.expanded svg {
  transform: rotate(90deg);
}

.expand-btn svg {
  transition: transform 0.2s ease;
}

.level-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 5px;
  flex-shrink: 0;
}

.level-badge--1 { color: #ff4400; background: rgba(255, 68, 0, 0.08); }
.level-badge--2 { color: #3B6E6E; background: rgba(59, 110, 110, 0.08); }
.level-badge--3 { color: #C8A464; background: rgba(200, 164, 100, 0.12); }

.category-icon {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  overflow: hidden;
  flex-shrink: 0;
  background: #F5F5F4;
  border: 1px solid #E8E8E6;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #A1A1AA;
}

/* 父分类图标：folder 主题色 */
.category-icon--folder {
  background: rgba(200, 164, 100, 0.1);
  border-color: rgba(200, 164, 100, 0.3);
  color: #C8A464;
}

.category-icon img,
.category-icon :deep(.el-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-name {
  font-size: 14px;
  font-weight: 500;
  color: #3B3B3E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 父分类名称更突出 */
.category-node__main--parent .category-name {
  font-weight: 700;
  color: #1C1C1E;
}

/* 子分类名称更柔和 */
.category-node__main--leaf .category-name {
  font-weight: 500;
  color: #4B4B4E;
}

.root-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: 20px;
  color: #A1A1AA;
  background: #F0F0EE;
  flex-shrink: 0;
}

.child-count {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: 20px;
  color: #C8A464;
  background: #FDF8EE;
  flex-shrink: 0;
}

.category-node__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
  white-space: nowrap;
  flex-shrink: 0;
  width: 260px;
}

.parent-name {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sort-value {
  min-width: 50px;
  text-align: right;
}

.time-value {
  min-width: 120px;
  text-align: right;
}

.category-node__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.toggle-switch {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.toggle-switch input {
  display: none;
}

.toggle-slider {
  width: 34px;
  height: 18px;
  border-radius: 18px;
  background: #E8E8E6;
  position: relative;
  transition: background 0.2s ease;
}

.toggle-slider::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease;
}

.toggle-switch.checked .toggle-slider {
  background: #22c55e;
}

.toggle-switch.checked .toggle-slider::after {
  transform: translateX(16px);
}

.toggle-label {
  font-size: 12px;
  font-weight: 600;
  color: #6B6B6E;
  min-width: 28px;
}

.toggle-switch.checked .toggle-label {
  color: #22c55e;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 10px;
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
   弹窗
   =========================== */
.category-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.category-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.category-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 10px;
}

.category-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
  border-top: none;
}

.category-dialog :deep(.el-form-item__label) {
  color: #6B6B6E;
  font-weight: 500;
}

.category-dialog :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #E8E8E6 inset;
  border-radius: 10px;
  padding: 0 12px;
}

.category-dialog :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #C8A464 inset;
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

.radio-cards {
  display: flex;
  gap: 10px;
}

.radio-card {
  flex: 1;
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
  border-color: #22c55e;
  background: rgba(34, 197, 94, 0.04);
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
  border-color: #22c55e;
}

.radio-card.active .radio-card__dot::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: #22c55e;
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

.icon-upload-area {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.upload-box {
  position: relative;
  width: 86px;
  height: 86px;
  border-radius: 14px;
  border: 2px dashed #E8E8E6;
  background: #FAFAF9;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.upload-box:hover {
  border-color: #C8A464;
  background: #FDFCFA;
}

.upload-input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
  z-index: 2;
}

.upload-preview {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  background: rgba(28, 28, 30, 0.55);
  color: #FFFFFF;
  opacity: 0;
  transition: opacity 0.2s ease;
  z-index: 3;
  font-size: 11px;
  font-weight: 600;
}

.upload-box:hover .upload-overlay {
  opacity: 1;
}

.upload-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #A1A1AA;
  z-index: 1;
}

.upload-placeholder svg {
  color: #C8A464;
}

.upload-placeholder span {
  font-size: 11px;
  font-weight: 500;
}

.icon-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-top: 4px;
}

.icon-info strong {
  font-size: 14px;
  font-weight: 600;
  color: #1C1C1E;
}

.icon-info span {
  font-size: 12px;
  color: #A1A1AA;
}

.text-link {
  font-size: 12px;
  font-weight: 600;
  color: #ff4400;
  background: transparent;
  border: none;
  cursor: pointer;
  text-align: left;
  padding: 0;
  transition: color 0.2s ease;
}

.text-link:hover {
  color: #e63d00;
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
@media (max-width: 1200px) {
  .category-node__bar {
    flex-wrap: wrap;
  }

  .category-node__meta {
    width: 100%;
    padding-left: calc(((var(--level) - 1) * 28px) + 56px);
    padding-top: 4px;
  }

  .category-node__actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #F0F0EE;
  }
}

@media (max-width: 1024px) {
  .stats-row {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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

  .category-search input {
    width: 100%;
  }

  .category-node__bar {
    padding: 12px 16px;
    margin: 0 8px;
  }

  .category-node__meta {
    padding-left: calc(((var(--level) - 1) * 28px) + 48px);
  }
}

@media (max-width: 480px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .category-node__actions {
    flex-wrap: wrap;
  }

  .radio-cards {
    flex-direction: column;
  }
}

@media (prefers-reduced-motion: reduce) {
  .primary-btn,
  .secondary-btn,
  .icon-btn,
  .stat-card,
  .category-node__bar,
  .expand-btn svg,
  .action-btn,
  .upload-box,
  .upload-overlay,
  .radio-card,
  .toggle-slider,
  .toggle-slider::after {
    transition: none;
  }

  .btn-spinner {
    animation: none;
  }
}
</style>