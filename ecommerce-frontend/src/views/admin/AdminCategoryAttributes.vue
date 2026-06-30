<template>
  <div class="admin-category-attributes" v-loading="loadingTree">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">分类属性绑定</h1>
          <p class="welcome-desc">为末级分类绑定销售属性与基本属性。SPU 正在使用的属性无法解绑。</p>
        </div>
        <div class="current-tag" v-if="selectedCategory">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z"/>
            <circle cx="7" cy="7" r="2"/>
          </svg>
          <span>{{ selectedCategory.name }}</span>
          <span class="current-tag__type" :class="selectedCategoryHasChildren ? 'type-parent' : 'type-leaf'">
            {{ selectedCategoryHasChildren ? '父分类' : '末级分类' }}
          </span>
        </div>
      </div>
    </section>

    <!-- 主内容：左树右表 -->
    <section class="main-content">
      <!-- 左侧分类树 -->
      <div class="category-panel">
        <div class="panel-header">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
          </svg>
          <span>选择分类</span>
        </div>
        <div class="category-search">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="filterText"
            type="text"
            placeholder="搜索分类..."
            @input="onFilterInput"
          />
        </div>
        <el-tree
          ref="treeRef"
          :data="categoryTree"
          :props="{ label: 'name', children: 'children' }"
          :filter-node-method="filterNode"
          node-key="id"
          highlight-current
          default-expand-all
          @current-change="handleCategoryChange"
          class="category-tree"
        >
          <template #default="{ data }">
            <div class="custom-tree-node">
              <svg
                class="node-icon"
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                :class="hasSubCategories(data) ? 'node-icon--folder' : 'node-icon--leaf'"
              >
                <path v-if="hasSubCategories(data)" d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
                <path v-else d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              </svg>
              <span class="node-label">{{ data.name }}</span>
              <span v-if="hasSubCategories(data)" class="child-count-badge">{{ data.children.length }}</span>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- 右侧属性面板 -->
      <div class="attr-panel">
        <div v-if="!selectedCategory" class="empty-state">
          <div class="empty-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <p>请在左侧选择一个分类</p>
          <span>仅末级分类（无子分类）可进行属性绑定</span>
        </div>

        <template v-else>
          <!-- 父分类提示 -->
          <div v-if="selectedCategoryHasChildren" class="notice-card notice-card--warning">
            <div class="notice-icon">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                <line x1="12" y1="9" x2="12" y2="13"/>
                <line x1="12" y1="17" x2="12.01" y2="17"/>
              </svg>
            </div>
            <div class="notice-content">
              <strong>「{{ selectedCategory.name }}」包含子分类</strong>
              <span>属性绑定仅支持末级分类，请选择一个末级分类进行操作。</span>
            </div>
          </div>

          <!-- 已绑定属性区 -->
          <div class="attr-section">
            <div class="section-header">
              <div class="section-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
                <h2>已绑定属性</h2>
                <span class="count-badge">{{ boundAttrs.length }}</span>
              </div>
            </div>

            <div v-if="boundAttrs.length > 0" class="bound-list">
              <div
                v-for="row in boundAttrs"
                :key="row.id"
                class="bound-card"
                :class="{ 'bound-card--locked': row.hasSpuUsage }"
              >
                <div class="bound-card__main">
                  <span class="bound-card__name">{{ row.attrName }}</span>
                  <span class="bound-type-tag" :class="row.attrType === 1 ? 'bound-type-tag--sales' : 'bound-type-tag--basic'">
                    {{ row.attrType === 1 ? '销售属性' : '基本属性' }}
                  </span>
                </div>
                <div class="bound-card__meta">
                  <span>排序 {{ row.sort }}</span>
                  <span v-if="row.hasSpuUsage" class="lock-hint">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                      <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                    </svg>
                    SPU 使用中
                  </span>
                </div>
                <div class="bound-card__actions">
                  <div class="sort-control">
                    <button class="sort-btn" title="升序" @click="handleSortChange(row, Math.max(0, row.sort - 1))">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="18 15 12 9 6 15"/>
                      </svg>
                    </button>
                    <span class="sort-value">{{ row.sort }}</span>
                    <button class="sort-btn" title="降序" @click="handleSortChange(row, row.sort + 1)">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="6 9 12 15 18 9"/>
                      </svg>
                    </button>
                  </div>
                  <button
                    class="action-btn action-btn--unbind"
                    :disabled="row.hasSpuUsage"
                    @click="handleUnbind(row)"
                  >
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                    解绑
                  </button>
                </div>
              </div>
            </div>

            <div v-else class="empty-section">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
              <span>暂未绑定属性</span>
            </div>
          </div>

          <!-- 可绑定属性区 -->
          <div v-if="!selectedCategoryHasChildren" class="attr-section">
            <div class="section-header">
              <div class="section-title">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                <h2>可绑定属性</h2>
                <span class="count-badge count-badge--green">{{ availableAttrs.length }}</span>
              </div>
              <button
                class="primary-btn primary-btn--sm"
                :disabled="availableAttrs.length === 0"
                @click="showBindDialog = true"
              >
                绑定属性
              </button>
            </div>

            <div v-if="availableAttrs.length > 0" class="available-list">
              <div
                v-for="attr in availableAttrs"
                :key="attr.id"
                class="available-card"
                @click="handleBind(attr)"
              >
                <div class="available-card__info">
                  <span class="available-card__name">{{ attr.name }}</span>
                  <span class="bound-type-tag" :class="attr.attrType === 1 ? 'bound-type-tag--sales' : 'bound-type-tag--basic'">
                    {{ attr.attrType === 1 ? '销售属性' : '基本属性' }}
                  </span>
                </div>
                <div class="available-card__action">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="12" y1="5" x2="12" y2="19"/>
                    <line x1="5" y1="12" x2="19" y2="12"/>
                  </svg>
                </div>
              </div>
            </div>

            <div v-else class="empty-section">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
              <span>所有属性均已绑定到该分类</span>
            </div>
          </div>
        </template>
      </div>
    </section>

    <!-- 绑定属性弹窗 -->
    <el-dialog
      v-model="showBindDialog"
      title="选择要绑定的属性"
      width="600px"
      :close-on-click-modal="false"
      class="bind-dialog"
      destroy-on-close
    >
      <div class="dialog-tip">
        选择下方属性绑定到「<strong>{{ selectedCategory?.name }}</strong>」
      </div>
      <div class="bind-options">
        <div
          v-for="attr in availableAttrs"
          :key="attr.id"
          class="bind-option"
          @click="handleBind(attr)"
        >
          <div class="bind-option__radio">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="16"/>
              <line x1="8" y1="12" x2="16" y2="12"/>
            </svg>
          </div>
          <div class="bind-option__info">
            <span class="bind-option__name">{{ attr.name }}</span>
            <span class="bound-type-tag" :class="attr.attrType === 1 ? 'bound-type-tag--sales' : 'bound-type-tag--basic'">
              {{ attr.attrType === 1 ? '销售属性' : '基本属性' }}
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="showBindDialog = false">关闭</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryTree, type Category } from '@/api/category'
import {
  getCategoryBoundAttrs,
  getCategoryAvailableAttrs,
  bindAttrToCategory,
  unbindCategoryAttr,
  updateCategoryAttrSort,
  type CategoryBoundAttr,
  type CategoryAvailableAttr
} from '@/api/attribute'

const filterText = ref('')
const treeRef = ref()
const categoryTree = ref<Category[]>([])
const selectedCategory = ref<Category | null>(null)

const loadingTree = ref(false)
const loadingBound = ref(false)
const loadingAvailable = ref(false)
const boundAttrs = ref<CategoryBoundAttr[]>([])
const availableAttrs = ref<CategoryAvailableAttr[]>([])
const showBindDialog = ref(false)

const hasSubCategories = (data: Category): boolean => {
  return !!(data.children && data.children.length > 0)
}

const selectedCategoryHasChildren = computed(() => {
  return selectedCategory.value ? hasSubCategories(selectedCategory.value) : false
})

const filterNode = (value: string, data: Category) => {
  if (!value) return true
  return data.name.includes(value)
}

let filterTimer: ReturnType<typeof setTimeout> | null = null
const onFilterInput = () => {
  if (filterTimer) clearTimeout(filterTimer)
  filterTimer = setTimeout(() => {
    treeRef.value?.filter(filterText.value)
  }, 150)
}

watch(filterText, () => {
  onFilterInput()
})

const loadCategoryTree = async () => {
  loadingTree.value = true
  try {
    categoryTree.value = (await getCategoryTree()) || []
  } catch {
    // 拦截器已处理
  } finally {
    loadingTree.value = false
  }
}

const handleCategoryChange = (data: Category | null) => {
  selectedCategory.value = data
  if (data && !hasSubCategories(data)) {
    loadBoundAttrs(data.id)
    loadAvailableAttrs(data.id)
  } else {
    boundAttrs.value = []
    availableAttrs.value = []
  }
}

const handleSortChange = async (row: CategoryBoundAttr, newSort: number) => {
  try {
    await updateCategoryAttrSort(row.id, newSort)
    ElMessage.success(`排序已更新为 ${newSort}`)
    row.sort = newSort
  } catch {
    if (selectedCategory.value) {
      await loadBoundAttrs(selectedCategory.value.id)
    }
  }
}

const loadBoundAttrs = async (categoryId: number) => {
  loadingBound.value = true
  try {
    boundAttrs.value = (await getCategoryBoundAttrs(categoryId)) || []
  } catch {
    boundAttrs.value = []
  } finally {
    loadingBound.value = false
  }
}

const loadAvailableAttrs = async (categoryId: number) => {
  loadingAvailable.value = true
  try {
    availableAttrs.value = (await getCategoryAvailableAttrs(categoryId)) || []
  } catch {
    availableAttrs.value = []
  } finally {
    loadingAvailable.value = false
  }
}

const handleBind = async (attr: CategoryAvailableAttr) => {
  if (!selectedCategory.value) return
  showBindDialog.value = false
  try {
    await bindAttrToCategory({
      categoryId: selectedCategory.value.id,
      attrId: attr.id
    })
    ElMessage.success(`属性「${attr.name}」绑定成功`)
    await Promise.all([
      loadBoundAttrs(selectedCategory.value.id),
      loadAvailableAttrs(selectedCategory.value.id)
    ])
  } catch {
    // 拦截器已处理
  }
}

const handleUnbind = async (row: CategoryBoundAttr) => {
  ElMessageBox.confirm(
    `确定从「${selectedCategory.value?.name}」中解绑属性「${row.attrName}」？`,
    '确认解绑',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await unbindCategoryAttr(row.id)
      ElMessage.success('解绑成功')
      if (selectedCategory.value) {
        await Promise.all([
          loadBoundAttrs(selectedCategory.value.id),
          loadAvailableAttrs(selectedCategory.value.id)
        ])
      }
    } catch {
      // 拦截器已处理
    }
  }).catch(() => {})
}

onMounted(() => {
  loadCategoryTree()
})
</script>

<style scoped>
.admin-category-attributes {
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

.current-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 12px;
  background: #F5F5F4;
  color: #1C1C1E;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.current-tag svg {
  color: #C8A464;
}

.current-tag__type {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 20px;
}

.current-tag__type.type-leaf {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.08);
}

.current-tag__type.type-parent {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.08);
}

/* ===========================
   主内容区
   =========================== */
.main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  height: calc(100vh - 200px);
  min-height: 480px;
  overflow: hidden;
}

/* ===========================
   左侧分类面板
   =========================== */
.category-panel {
  width: 280px;
  flex-shrink: 0;
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  padding: 20px;
  overflow-y: auto;
  height: 100%;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 700;
  color: #1C1C1E;
  margin-bottom: 14px;
}

.panel-header svg {
  color: #C8A464;
}

.category-search {
  position: relative;
  margin-bottom: 12px;
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
  width: 100%;
  height: 38px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  padding: 0 12px 0 36px;
  font-size: 13px;
  color: #1C1C1E;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.category-search input:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.1);
}

.category-tree {
  --el-tree-node-hover-bg-color: #FAFAF9;
  --el-tree-expand-icon-color: #A1A1AA;
}

.category-tree :deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.category-tree :deep(.el-tree-node__content:hover) {
  background: #FAFAF9;
}

.category-tree :deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background: #FDF8EE;
  color: #1C1C1E;
}

.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  padding-right: 6px;
}

.node-icon {
  flex-shrink: 0;
}

.node-icon--folder {
  color: #C8A464;
}

.node-icon--leaf {
  color: #ff4400;
}

.node-label {
  font-size: 13px;
  color: #1C1C1E;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 500;
}

.child-count-badge {
  margin-left: auto;
  font-size: 10px;
  font-weight: 700;
  color: #A1A1AA;
  background: #F0F0EE;
  padding: 1px 6px;
  border-radius: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* ===========================
   右侧属性面板
   =========================== */
.attr-panel {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  height: 100%;
  padding-right: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: #F5F5F4;
  color: #A1A1AA;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}

.empty-icon svg {
  width: 30px;
  height: 30px;
}

.empty-state p {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  color: #1C1C1E;
}

.empty-state span {
  font-size: 13px;
  color: #A1A1AA;
}

.notice-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  border-radius: 16px;
  padding: 16px 18px;
  margin-bottom: 16px;
}

.notice-card--warning {
  background: linear-gradient(135deg, #FFFBEB 0%, #FFF7ED 100%);
  border: 1px solid #FDE68A;
}

.notice-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(245, 158, 11, 0.12);
  color: #f59e0b;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notice-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.notice-content strong {
  font-size: 14px;
  font-weight: 600;
  color: #1C1C1E;
}

.notice-content span {
  font-size: 13px;
  color: #6B6B6E;
}

/* ===========================
   属性区块
   =========================== */
.attr-section {
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  padding: 20px 24px;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title h2 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1C1C1E;
}

.section-title svg {
  color: #C8A464;
}

.count-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 20px;
  color: #ff4400;
  background: rgba(255, 68, 0, 0.08);
}

.count-badge--green {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.08);
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  padding: 8px 16px;
  border: 1px solid #1C1C1E;
  background: #1C1C1E;
  color: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.primary-btn:hover:not(:disabled) {
  background: #2C2C2E;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.primary-btn--sm {
  padding: 6px 14px;
  font-size: 12px;
}

.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  padding: 8px 16px;
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

/* ===========================
   已绑定列表
   =========================== */
.bound-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bound-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  background: #FAFAF9;
  border: 1px solid #E8E8E6;
  border-radius: 14px;
  transition: all 0.2s ease;
}

.bound-card:hover {
  border-color: #CCC;
  background: #F5F5F4;
}

.bound-card--locked {
  background: #FEF2F2;
  border-color: #FECACA;
}

.bound-card__main {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.bound-card__name {
  font-size: 14px;
  font-weight: 600;
  color: #1C1C1E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bound-type-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 20px;
  white-space: nowrap;
}

.bound-type-tag--sales {
  color: #ff4400;
  background: rgba(255, 68, 0, 0.08);
}

.bound-type-tag--basic {
  color: #3B6E6E;
  background: rgba(59, 110, 110, 0.08);
}

.bound-card__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 500;
  white-space: nowrap;
}

.lock-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #ef4444;
  background: rgba(239, 68, 68, 0.06);
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.bound-card__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.sort-control {
  display: flex;
  align-items: center;
  gap: 2px;
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  border-radius: 8px;
  padding: 2px;
}

.sort-btn {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #6B6B6E;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.sort-btn:hover {
  background: #F0F0EE;
  color: #1C1C1E;
}

.sort-value {
  min-width: 24px;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
  color: #1C1C1E;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.action-btn:hover:not(:disabled) {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn--unbind {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.06);
}

.action-btn--unbind:hover:not(:disabled) {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.5);
}

/* ===========================
   可绑定列表
   =========================== */
.available-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 10px;
}

.available-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.available-card:hover {
  border-color: #C8A464;
  background: #FDFCFA;
  box-shadow: 0 2px 8px rgba(200, 164, 100, 0.12);
}

.available-card__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.available-card__name {
  font-size: 13px;
  font-weight: 600;
  color: #1C1C1E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.available-card__action {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F5F5F4;
  color: #C8A464;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.available-card:hover .available-card__action {
  background: #C8A464;
  color: #FFFFFF;
}

.empty-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #A1A1AA;
  font-size: 13px;
  padding: 28px 0;
  background: #FAFAF9;
  border-radius: 14px;
  border: 1px dashed #E8E8E6;
}

/* ===========================
   绑定弹窗
   =========================== */
.bind-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.bind-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.bind-dialog :deep(.el-dialog__body) {
  padding: 16px 24px 10px;
}

.bind-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 24px;
  border-top: none;
}

.dialog-tip {
  font-size: 13px;
  color: #6B6B6E;
  margin-bottom: 14px;
  padding: 10px 14px;
  background: #FAFAF9;
  border-radius: 10px;
}

.dialog-tip strong {
  color: #1C1C1E;
  font-weight: 600;
}

.bind-options {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 10px;
  max-height: 360px;
  overflow-y: auto;
}

.bind-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.bind-option:hover {
  border-color: #C8A464;
  background: #FDFCFA;
}

.bind-option__radio {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #C8A464;
  background: rgba(200, 164, 100, 0.1);
  flex-shrink: 0;
}

.bind-option__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.bind-option__name {
  font-size: 13px;
  font-weight: 600;
  color: #1C1C1E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
    height: auto;
    min-height: auto;
  }

  .category-panel {
    width: 100%;
    height: auto;
    max-height: 320px;
  }

  .attr-panel {
    height: auto;
    overflow: visible;
  }
}

@media (max-width: 768px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .bound-card {
    flex-wrap: wrap;
  }

  .bound-card__actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 8px;
    border-top: 1px solid #F0F0EE;
  }

  .available-list {
    grid-template-columns: 1fr;
  }

  .bind-options {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .category-search input,
  .category-tree :deep(.el-tree-node__content),
  .notice-card,
  .bound-card,
  .available-card,
  .bind-option,
  .action-btn,
  .sort-btn,
  .primary-btn,
  .secondary-btn {
    transition: none;
  }
}
</style>