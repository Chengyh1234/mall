<template>
  <div class="admin-category-attributes">
    <!-- ===== 页面头部 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon-wrap">
          <el-icon class="header-icon"><Link /></el-icon>
        </div>
        <div>
          <h1>分类属性绑定</h1>
          <p class="header-desc">为分类绑定销售属性或基本属性，仅末级分类（无子分类）可绑定属性</p>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- ===== 左侧分类树 ===== -->
      <div class="category-panel">
        <div class="panel-header">
          <el-icon style="margin-right: 6px"><FolderOpened /></el-icon>
          选择分类
        </div>
        <el-input
          v-model="filterText"
          placeholder="搜索分类..."
          size="small"
          clearable
          class="category-filter"
          :prefix-icon="Search"
        />
        <el-tree
          ref="treeRef"
          :data="categoryTree"
          :props="{ label: 'name', children: 'children' }"
          :filter-node-method="(value: string, data: any) => data.name.includes(value)"
          node-key="id"
          highlight-current
          default-expand-all
          @current-change="handleCategoryChange"
        >
          <template #default="{ data }">
            <div class="custom-tree-node">
              <el-icon :class="getNodeIcon(data).class" :style="getNodeIcon(data).style">
                <component :is="getNodeIcon(data).icon" />
              </el-icon>
              <span class="node-label">{{ data.name }}</span>
              <span v-if="hasSubCategories(data)" class="child-count-badge">{{ data.children.length }} 子类</span>
            </div>
          </template>
        </el-tree>
      </div>

      <!-- ===== 右侧属性面板 ===== -->
      <div class="attr-panel">
        <!-- 未选中 -->
        <div v-if="!selectedCategory" class="empty-state">
          <el-icon class="empty-icon"><FolderOpened /></el-icon>
          <p class="empty-text">请在左侧选择一个末级分类</p>
          <p class="empty-hint">仅末级分类（无子分类）可进行属性绑定操作</p>
        </div>

        <template v-else>
          <!-- 非末级分类警告 -->
          <div v-if="selectedCategoryHasChildren" class="warning-banner">
            <el-icon class="warning-icon"><WarningFilled /></el-icon>
            <div class="warning-content">
              <strong>「{{ selectedCategory.name }}」包含子分类</strong>
              <p>属性绑定仅支持末级分类（无子分类），请选择一个末级分类进行操作</p>
            </div>
          </div>

          <!-- 当前分类信息卡片 -->
          <div class="current-category-card">
            <div class="card-accents">
              <div class="accent-bar"></div>
              <div class="accent-bar"></div>
              <div class="accent-bar"></div>
            </div>
            <div class="card-body">
              <el-icon class="card-icon" :color="selectedCategoryHasChildren ? '#d9d9d9' : '#409eff'">
                <component :is="selectedCategoryHasChildren ? FolderOpened : Document" />
              </el-icon>
              <div class="card-info">
                <span class="card-label">当前分类</span>
                <span class="card-name">{{ selectedCategory.name }}</span>
              </div>
              <el-tag v-if="selectedCategoryHasChildren" type="warning" size="small" effect="dark" round>
                父分类
              </el-tag>
              <el-tag v-else type="success" size="small" effect="dark" round>
                末级分类
              </el-tag>
            </div>
          </div>

          <!-- 已绑定属性 -->
          <div class="attr-section">
            <div class="section-header">
              <div class="section-title">
                <el-icon color="#409eff"><Link /></el-icon>
                <h3>已绑定属性</h3>
                <el-tag size="small" type="primary" effect="plain" round>{{ boundAttrs.length }}</el-tag>
              </div>
            </div>
            <el-table
              :data="boundAttrs"
              v-loading="loadingBound"
              size="small"
              stripe
              empty-text="暂未绑定属性"
              class="attr-table"
            >
              <el-table-column prop="attrName" label="属性名称" min-width="140">
                <template #default="{ row }">
                  <span class="attr-name-cell">{{ row.attrName }}</span>
                </template>
              </el-table-column>
              <el-table-column label="属性类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.attrType === 1 ? 'primary' : 'success'" size="small" effect="light" round>
                    {{ row.attrType === 1 ? '销售属性' : '基本属性' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="排序" width="110" align="center">
                <template #default="{ row }">
                  <el-input-number
                    v-model="row.sort"
                    :min="0"
                    :max="999"
                    size="small"
                    controls-position="right"
                    style="width: 80px"
                    @change="(val: number) => handleSortChange(row, val)"
                  />
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tooltip
                    v-if="row.hasSpuUsage"
                    content="该分类下已有 SPU 使用了此属性，无法解绑"
                    placement="top"
                  >
                    <el-tag type="warning" size="small" effect="plain">
                      <el-icon style="margin-right: 3px; vertical-align: -2px"><Lock /></el-icon>
                      使用中
                    </el-tag>
                  </el-tooltip>
                  <el-tag v-else type="success" size="small" effect="plain" round>可解绑</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="110" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button
                    type="danger"
                    link
                    size="small"
                    :disabled="row.hasSpuUsage"
                    @click="handleUnbind(row)"
                  >
                    <el-icon style="margin-right: 2px; vertical-align: -2px"><Remove /></el-icon>
                    解绑
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- 可绑定属性 -->
          <div class="attr-section" v-if="!selectedCategoryHasChildren">
            <div class="section-header">
              <div class="section-title">
                <el-icon color="#67c23a"><Plus /></el-icon>
                <h3>可绑定属性</h3>
                <el-tag size="small" type="success" effect="plain" round>{{ availableAttrs.length }}</el-tag>
              </div>
              <el-button type="primary" size="small" :icon="Plus" @click="showBindDialog = true" :disabled="availableAttrs.length === 0">
                绑定属性
              </el-button>
            </div>
            <el-table
              :data="availableAttrs"
              v-loading="loadingAvailable"
              size="small"
              stripe
              empty-text="所有属性均已绑定到该分类"
              class="attr-table"
            >
              <el-table-column prop="name" label="属性名称" min-width="140">
                <template #default="{ row }">
                  <span class="attr-name-cell">{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="属性类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.attrType === 1 ? 'primary' : 'success'" size="small" effect="light" round>
                    {{ row.attrType === 1 ? '销售属性' : '基本属性' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="90" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleBind(row)">
                    <el-icon style="margin-right: 2px; vertical-align: -2px"><Plus /></el-icon>
                    绑定
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </div>
    </div>

    <!-- 绑定属性弹窗 -->
    <el-dialog v-model="showBindDialog" title="选择要绑定的属性" width="640px" :close-on-click-modal="false" class="bind-dialog">
      <div class="dialog-tip">
        选择下方属性绑定到「<strong>{{ selectedCategory?.name }}</strong>」
      </div>
      <el-table
        :data="availableAttrs"
        v-loading="loadingAvailable"
        stripe
        @row-click="handleBindRowClick"
        class="attr-table"
        highlight-current-row
      >
        <el-table-column prop="name" label="属性名称" min-width="140" />
        <el-table-column label="属性类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.attrType === 1 ? 'primary' : 'success'" size="small" effect="light" round>
              {{ row.attrType === 1 ? '销售属性' : '基本属性' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleBind(row)">
              <el-icon style="margin-right: 2px; vertical-align: -2px"><Plus /></el-icon>
              绑定
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="showBindDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Link, FolderOpened, Document, WarningFilled, Lock,
  Plus, Remove, Search
} from '@element-plus/icons-vue'
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

const loadingBound = ref(false)
const loadingAvailable = ref(false)
const boundAttrs = ref<CategoryBoundAttr[]>([])
const availableAttrs = ref<CategoryAvailableAttr[]>([])
const showBindDialog = ref(false)

// 检查分类是否有子分类
const hasSubCategories = (data: Category): boolean => {
  return !!(data.children && data.children.length > 0)
}

const selectedCategoryHasChildren = computed(() => {
  return selectedCategory.value ? hasSubCategories(selectedCategory.value) : false
})

// 获取树节点图标
const getNodeIcon = (data: Category) => {
  if (hasSubCategories(data)) {
    return { icon: FolderOpened, class: 'node-icon-folder', style: { color: '#e6a23c' } }
  }
  return { icon: Document, class: 'node-icon-leaf', style: { color: '#409eff' } }
}

// 分类树搜索过滤
watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

// 加载分类树
const loadCategoryTree = async () => {
  try {
    categoryTree.value = (await getCategoryTree()) || []
  } catch {
    // 拦截器已处理
  }
}

// 选择分类
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

// 修改绑定排序
const handleSortChange = async (row: CategoryBoundAttr, newSort: number) => {
  try {
    await updateCategoryAttrSort(row.id, newSort)
    ElMessage.success(`排序已更新为 ${newSort}`)
  } catch {
    // 失败时回滚
    await loadBoundAttrs(selectedCategory.value!.id)
  }
}

// 加载已绑定属性
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

// 加载可绑定属性
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

// 绑定属性
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

// 弹窗内点击行
const handleBindRowClick = (row: CategoryAvailableAttr) => {
  handleBind(row)
}

// 解绑属性
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
/* ===== 页面布局 ===== */
.admin-category-attributes {
  padding: 24px;
  max-width: 1440px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
  background: #f5f7fa;
}

/* ===== 页面头部 ===== */
.page-header {
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

/* ===== 主内容区 ===== */
.main-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  height: calc(100vh - 180px);
  overflow: hidden;
}

/* ===== 左侧分类面板 ===== */
.category-panel {
  width: 260px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 16px;
  overflow-y: auto;
  height: 100%;
}
.panel-header {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f2f3f5;
  display: flex;
  align-items: center;
}
.category-filter {
  margin-bottom: 10px;
}

/* 自定义树节点 */
.custom-tree-node {
  display: flex;
  align-items: center;
  gap: 5px;
  flex: 1;
  min-width: 0;
  padding: 2px 0;
}
.node-icon-folder,
.node-icon-leaf {
  font-size: 15px;
  flex-shrink: 0;
}
.node-label {
  font-size: 13px;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.child-count-badge {
  margin-left: auto;
  font-size: 11px;
  color: #86909c;
  background: #f2f3f5;
  padding: 0 6px;
  border-radius: 8px;
  white-space: nowrap;
  flex-shrink: 0;
}

/* el-tree 内部样式覆盖 */
:deep(.el-tree-node__content) {
  height: 34px;
  border-radius: 6px;
  transition: background 0.2s;
}
:deep(.el-tree-node__content:hover) {
  background: #f5f7fa;
}
:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background: #e8f4ff;
}

/* ===== 右侧属性面板 ===== */
.attr-panel {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  height: 100%;
}

/* 空状态 */
.empty-state {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 60px 20px;
  text-align: center;
}
.empty-icon {
  font-size: 48px;
  color: #c9cdd4;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 15px;
  color: #4e5969;
  margin: 0 0 6px 0;
}
.empty-hint {
  font-size: 13px;
  color: #86909c;
  margin: 0;
}

/* 警告横幅 */
.warning-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  background: linear-gradient(135deg, #fef7e6, #fff7e6);
  border: 1px solid #ffe7a3;
  border-radius: 10px;
  padding: 14px 18px;
  margin-bottom: 16px;
}
.warning-icon {
  font-size: 20px;
  color: #e6a23c;
  margin-top: 1px;
  flex-shrink: 0;
}
.warning-content strong {
  font-size: 14px;
  color: #1d2129;
}
.warning-content p {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #86909c;
}

/* 当前分类信息卡片 */
.current-category-card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
  overflow: hidden;
}
.card-accents {
  display: flex;
  height: 4px;
}
.card-accents .accent-bar {
  flex: 1;
}
.card-accents .accent-bar:nth-child(1) { background: #409eff; }
.card-accents .accent-bar:nth-child(2) { background: #337ecc; }
.card-accents .accent-bar:nth-child(3) { background: #2b6cb0; }
.card-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
}
.card-icon {
  font-size: 28px;
}
.card-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.card-label {
  font-size: 12px;
  color: #86909c;
}
.card-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

/* 属性区块 */
.attr-section {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  padding: 18px 20px;
  margin-bottom: 16px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.section-title h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}

/* 表格 */
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

/* 绑定弹窗 */
.bind-dialog :deep(.el-dialog__header) {
  padding-bottom: 12px;
  border-bottom: 1px solid #f2f3f5;
}
.dialog-tip {
  font-size: 13px;
  color: #86909c;
  margin-bottom: 14px;
  padding: 10px 14px;
  background: #f5f7fa;
  border-radius: 6px;
}
.dialog-tip strong {
  color: #1d2129;
}
</style>