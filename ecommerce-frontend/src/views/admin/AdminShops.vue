<template>
  <div class="admin-shops">
    <!-- 顶部 Bento -->
    <section class="bento-grid">
      <div class="bento-card welcome-card">
        <div class="welcome-content">
          <h1 class="welcome-title">店铺管理</h1>
          <p class="welcome-desc">管理全平台店铺与开店审核，控制营业状态与信息维护。</p>
        </div>
        <div class="tab-pills">
          <button
            :class="['tab-pill', { active: activeTab === 'shops' }]"
            @click="activeTab = 'shops'"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              <polyline points="9 22 9 12 15 12 15 22"/>
            </svg>
            店铺管理
          </button>
          <button
            :class="['tab-pill', { active: activeTab === 'audit' }]"
            @click="activeTab = 'audit'"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
            开店审核
            <span v-if="auditPagination.total > 0" class="tab-pill__badge">{{ auditPagination.total }}</span>
          </button>
        </div>
      </div>
    </section>

    <!-- 店铺管理 -->
    <template v-if="activeTab === 'shops'">
      <section class="list-card">
        <div class="filter-body">
          <div class="status-filter-section">
            <span class="status-filter-label">营业状态</span>
            <div class="status-pills">
              <button
                class="status-pill"
                :class="{ active: statusFilter === '' }"
                @click="statusFilter = ''; handleSearch()"
              >
                全部
              </button>
              <button
                class="status-pill"
                :class="{ active: statusFilter === 1 }"
                @click="statusFilter = 1; handleSearch()"
              >
                <span class="status-dot status-dot--active" />
                营业中
              </button>
              <button
                class="status-pill"
                :class="{ active: statusFilter === 0 }"
                @click="statusFilter = 0; handleSearch()"
              >
                <span class="status-dot status-dot--inactive" />
                已关闭
              </button>
            </div>
          </div>

          <div class="filter-divider" />

          <div class="filter-row">
            <div class="filter-field filter-field--grow">
              <label>店铺名称</label>
              <div class="filter-input-wrap">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                <input
                  v-model="searchKeyword"
                  type="text"
                  placeholder="搜索店铺名称..."
                  @keyup.enter="handleSearch"
                />
                <button v-if="searchKeyword" class="input-clear" @click="searchKeyword = ''; handleSearch()">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line x1="18" y1="6" x2="6" y2="18"/>
                    <line x1="6" y1="6" x2="18" y2="18"/>
                  </svg>
                </button>
              </div>
            </div>

            <div class="filter-field">
              <label>商家ID</label>
              <div class="filter-input-wrap">
                <input
                  v-model="searchSellerId"
                  type="text"
                  placeholder="精确匹配"
                  @keyup.enter="handleSearch"
                />
              </div>
            </div>

            <div class="filter-field">
              <label>联系电话</label>
              <div class="filter-input-wrap">
                <input
                  v-model="searchPhone"
                  type="text"
                  placeholder="模糊搜索"
                  @keyup.enter="handleSearch"
                />
              </div>
            </div>

            <div class="filter-actions">
              <button class="primary-btn primary-btn--gold" @click="handleSearch">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
                查询
              </button>
              <button class="secondary-btn" @click="handleResetShops">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="23 4 23 10 17 10"/>
                  <polyline points="1 20 1 14 7 14"/>
                  <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
                </svg>
                重置
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="list-card">
        <div class="list-card__header">
          <div class="list-card__title">
            <h2>店铺列表</h2>
            <span>共 {{ pagination.total }} 家</span>
          </div>
          <button class="icon-btn" title="刷新" @click="loadShops">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
          </button>
        </div>

        <div class="table-container" v-loading="loading">
          <el-table
            :data="shops"
            style="width: 100%"
            :header-cell-style="headerCellStyle"
            :cell-style="cellStyle"
            row-class-name="shop-row"
            empty-text="暂无店铺数据"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="id" label="ID" width="70" align="center" />
            <el-table-column label="店铺" min-width="180">
              <template #default="scope">
                <div class="shop-name-cell">
                  <div v-if="scope.row.logo" class="shop-logo">
                    <img :src="getImageUrl(scope.row.logo)" alt="" loading="lazy" />
                  </div>
                  <div v-else class="shop-logo shop-logo--placeholder">
                    {{ scope.row.name?.[0] || '店' }}
                  </div>
                  <div class="shop-name-info">
                    <span class="shop-name">{{ scope.row.name || '未命名' }}</span>
                    <span class="shop-seller">商家 #{{ scope.row.sellerId }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="联系电话" width="130" align="center">
              <template #default="scope">
                <span class="cell-muted">{{ scope.row.phone || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <span class="status-badge" :class="scope.row.status === 1 ? 'status-badge--active' : 'status-badge--inactive'">
                  <span class="status-dot" :class="scope.row.status === 1 ? 'status-dot--active' : 'status-dot--inactive'" />
                  {{ scope.row.status === 1 ? '营业中' : '已关闭' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="155" align="center">
              <template #default="scope">
                <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" align="center" fixed="right">
              <template #default="scope">
                <div class="action-group">
                  <button class="action-icon-btn action-icon-btn--view" title="查看" @click="showDetail(scope.row)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  </button>
                  <button class="action-icon-btn action-icon-btn--edit" title="编辑" @click="openEdit(scope.row)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                  <button
                    class="action-icon-btn"
                    :class="scope.row.status === 1 ? 'action-icon-btn--close' : 'action-icon-btn--open'"
                    :title="scope.row.status === 1 ? '关闭店铺' : '开启店铺'"
                    @click="toggleStatus(scope.row, scope.row.status !== 1)"
                  >
                    <svg v-if="scope.row.status === 1" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10"/>
                      <line x1="15" y1="9" x2="9" y2="15"/>
                      <line x1="9" y1="9" x2="15" y2="15"/>
                    </svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                  </button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="loadShops"
            @current-change="loadShops"
          />
        </div>
      </section>
    </template>

    <!-- 开店审核 -->
    <template v-if="activeTab === 'audit'">
      <section class="list-card">
        <div class="filter-body">
          <div class="status-filter-section">
            <span class="status-filter-label">申请状态</span>
            <div class="status-pills">
              <button
                class="status-pill"
                :class="{ active: auditStatusFilter === 2 }"
                @click="auditStatusFilter = 2; handleAuditSearch()"
              >
                <span class="status-dot status-dot--pending" />
                待审核
              </button>
              <button
                class="status-pill"
                :class="{ active: auditStatusFilter === 3 }"
                @click="auditStatusFilter = 3; handleAuditSearch()"
              >
                <span class="status-dot status-dot--rejected" />
                已驳回
              </button>
            </div>
          </div>
        </div>
      </section>

      <section class="list-card">
        <div class="list-card__header">
          <div class="list-card__title">
            <h2>{{ auditTitle }}</h2>
            <span>共 {{ auditPagination.total }} 条</span>
          </div>
          <button class="secondary-btn" :disabled="auditLoading" @click="loadApplyList">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="23 4 23 10 17 10"/>
              <polyline points="1 20 1 14 7 14"/>
              <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15"/>
            </svg>
            刷新
          </button>
        </div>

        <div class="table-container" v-loading="auditLoading">
          <el-table
            :data="applyList"
            style="width: 100%"
            :header-cell-style="headerCellStyle"
            :cell-style="cellStyle"
            row-class-name="shop-row"
            :empty-text="auditEmptyText"
          >
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="id" label="申请ID" width="80" align="center" />
            <el-table-column prop="name" label="店铺名称" min-width="160">
              <template #default="scope">
                <span class="cell-name">{{ scope.row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="sellerId" label="申请人ID" width="90" align="center" />
            <el-table-column prop="phone" label="联系电话" width="130" align="center">
              <template #default="scope">
                <span class="cell-muted">{{ scope.row.phone || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="address" label="店铺地址" min-width="180" show-overflow-tooltip>
              <template #default="scope">
                <span class="cell-muted">{{ scope.row.address || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110" align="center">
              <template #default="scope">
                <span class="status-badge" :class="getApplyStatusClass(scope.row.status)">
                  <span class="status-dot" :class="getApplyStatusDotClass(scope.row.status)" />
                  {{ getApplyStatusText(scope.row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="申请时间" width="155" align="center">
              <template #default="scope">
                <span class="time-text">{{ formatTime(scope.row.createdAt) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="210" align="center" fixed="right">
              <template #default="scope">
                <div class="action-group">
                  <template v-if="scope.row.status === 2">
                    <button class="action-btn action-btn--approve" :disabled="scope.row._approving" @click="handleApprove(scope.row)">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="20 6 9 17 4 12"/>
                      </svg>
                      通过
                    </button>
                    <button class="action-btn action-btn--reject" :disabled="scope.row._rejecting" @click="openRejectDialog(scope.row)">
                      <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="18" y1="6" x2="6" y2="18"/>
                        <line x1="6" y1="6" x2="18" y2="18"/>
                      </svg>
                      驳回
                    </button>
                  </template>
                  <button class="action-icon-btn action-icon-btn--view" title="详情" @click="showApplyDetail(scope.row)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <circle cx="12" cy="12" r="10"/>
                      <path d="M12 16v-4"/>
                      <path d="M12 8h.01"/>
                    </svg>
                  </button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination-bar">
          <el-pagination
            v-model:current-page="auditPagination.page"
            v-model:page-size="auditPagination.pageSize"
            :page-sizes="[10, 20, 50]"
            :total="auditPagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @size-change="loadApplyList"
            @current-change="loadApplyList"
          />
        </div>
      </section>
    </template>

    <!-- 店铺详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="'店铺详情 — ' + (currentShop?.name || '')"
      width="620px"
      destroy-on-close
      class="shop-dialog detail-dialog"
    >
      <div v-if="currentShop" class="detail-body">
        <div class="detail-hero">
          <div v-if="currentShop.logo" class="detail-hero-logo">
            <img :src="getImageUrl(currentShop.logo)" alt="" />
          </div>
          <div v-else class="detail-hero-logo detail-hero-logo--placeholder">
            {{ currentShop.name?.[0] || '店' }}
          </div>
          <div class="detail-hero-info">
            <div class="detail-hero-name">{{ currentShop.name || '未命名' }}</div>
            <div class="detail-hero-meta">
              <span class="status-badge" :class="currentShop.status === 1 ? 'status-badge--active' : 'status-badge--inactive'">
                <span class="status-dot" :class="currentShop.status === 1 ? 'status-dot--active' : 'status-dot--inactive'" />
                {{ currentShop.status === 1 ? '营业中' : '已关闭' }}
              </span>
              <span class="id-tag">ID #{{ currentShop.id }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3 class="detail-section-title">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
              <polyline points="10 9 9 9 8 9"/>
            </svg>
            基本信息
          </h3>
          <div class="detail-grid">
            <div class="detail-card">
              <span class="detail-card__label">店铺 ID</span>
              <span class="detail-card__value">#{{ currentShop.id }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">商家 ID</span>
              <span class="detail-card__value">#{{ currentShop.sellerId }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">联系电话</span>
              <span class="detail-card__value">{{ currentShop.phone || '—' }}</span>
            </div>
            <div class="detail-card">
              <span class="detail-card__label">排序权重</span>
              <span class="detail-card__value">{{ currentShop.sort ?? 0 }}</span>
            </div>
            <div class="detail-card detail-card--wide">
              <span class="detail-card__label">店铺地址</span>
              <span class="detail-card__value">{{ currentShop.address || '—' }}</span>
            </div>
            <div class="detail-card detail-card--wide">
              <span class="detail-card__label">店铺描述</span>
              <p class="detail-card__desc">{{ currentShop.description || '暂无描述' }}</p>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-skeleton :rows="6" animated />
      </div>
    </el-dialog>

    <!-- 编辑店铺弹窗 -->
    <el-dialog
      v-model="editVisible"
      title="编辑店铺信息"
      width="520px"
      destroy-on-close
      class="shop-dialog edit-dialog"
    >
      <el-form :model="editForm" label-width="90px" class="edit-form">
        <el-form-item label="店铺名称">
          <el-input v-model="editForm.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="editForm.address" placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入店铺描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="editVisible = false">取消</button>
          <button class="primary-btn primary-btn--gold" :disabled="saving" @click="handleSave">
            <svg v-if="saving" class="btn-spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
            保存修改
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 申请详情弹窗 -->
    <el-dialog
      v-model="applyDetailVisible"
      :title="'申请详情 — ' + (applyDetail?.name || '')"
      width="560px"
      destroy-on-close
      class="shop-dialog detail-dialog"
    >
      <div v-if="applyDetail" class="detail-body">
        <div class="detail-grid">
          <div class="detail-card">
            <span class="detail-card__label">申请 ID</span>
            <span class="detail-card__value">#{{ applyDetail.id }}</span>
          </div>
          <div class="detail-card">
            <span class="detail-card__label">店铺名称</span>
            <span class="detail-card__value">{{ applyDetail.name }}</span>
          </div>
          <div class="detail-card">
            <span class="detail-card__label">申请人 ID</span>
            <span class="detail-card__value">#{{ applyDetail.sellerId }}</span>
          </div>
          <div class="detail-card">
            <span class="detail-card__label">联系电话</span>
            <span class="detail-card__value">{{ applyDetail.phone || '—' }}</span>
          </div>
          <div class="detail-card">
            <span class="detail-card__label">申请状态</span>
            <span class="status-badge" :class="getApplyStatusClass(applyDetail.status)">
              <span class="status-dot" :class="getApplyStatusDotClass(applyDetail.status)" />
              {{ getApplyStatusText(applyDetail.status) }}
            </span>
          </div>
          <div class="detail-card">
            <span class="detail-card__label">申请时间</span>
            <span class="detail-card__value">{{ formatTime(applyDetail.createdAt) }}</span>
          </div>
          <div class="detail-card detail-card--wide">
            <span class="detail-card__label">店铺描述</span>
            <p class="detail-card__desc">{{ applyDetail.description || '暂无描述' }}</p>
          </div>
          <div class="detail-card detail-card--wide">
            <span class="detail-card__label">店铺地址</span>
            <span class="detail-card__value">{{ applyDetail.address || '—' }}</span>
          </div>
          <div v-if="applyDetail.status === 3 && applyDetail.rejectReason" class="detail-card detail-card--wide detail-card--danger">
            <span class="detail-card__label">驳回原因</span>
            <span class="detail-card__value detail-card__value--danger">{{ applyDetail.rejectReason }}</span>
          </div>
        </div>
      </div>
      <div v-else class="empty-detail">
        <el-skeleton :rows="6" animated />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="applyDetailVisible = false">关闭</button>
        </div>
      </template>
    </el-dialog>

    <!-- 驳回原因弹窗 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="驳回开店申请"
      width="480px"
      destroy-on-close
      class="shop-dialog reject-dialog"
    >
      <div class="reject-body">
        <div class="reject-warning">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
          <span>确定要驳回 <strong>{{ rejectingShop?.name }}</strong> 的开店申请吗？</span>
        </div>
        <p class="reject-hint">请填写驳回原因，以便申请人了解问题并修改后重新提交：</p>
        <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="4"
          placeholder="请输入驳回原因，如：店铺名称不合规、信息不完整等"
          maxlength="200"
          show-word-limit
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <button class="secondary-btn" @click="rejectDialogVisible = false">取消</button>
          <button class="primary-btn primary-btn--danger" :disabled="rejecting" @click="handleReject">
            <svg v-if="rejecting" class="btn-spinner" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
            </svg>
            确认驳回
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminStorePage, getAdminStoreDetail, updateStore, updateStoreStatus, type Store } from '@/api/shop'
import { getAdminApplyPage, approveStoreApply, rejectStoreApply } from '@/api/shop'
import { getStoreLogoUrl } from '@/utils/resource'

const activeTab = ref('shops')

// ====== 店铺管理 ======
const searchKeyword = ref('')
const statusFilter = ref<number | ''>('')
const searchSellerId = ref('')
const searchPhone = ref('')
const loading = ref(false)
const shops = ref<any[]>([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

const detailVisible = ref(false)
const currentShop = ref<Store | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const editingShop = ref<any>(null)
const editForm = reactive({ name: '', phone: '', address: '', description: '' })

const loadShops = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = { page: pagination.page, pageSize: pagination.pageSize }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (statusFilter.value !== '') params.status = statusFilter.value
    if (searchSellerId.value) params.sellerId = Number(searchSellerId.value)
    if (searchPhone.value) params.phone = searchPhone.value
    const result = await getAdminStorePage(params)
    shops.value = (result.list || []).map((item: Store) => ({ ...item, _statusLoading: false }))
    pagination.total = result.total || 0
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.page = 1; loadShops() }

const handleResetShops = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  searchSellerId.value = ''
  searchPhone.value = ''
  pagination.page = 1
  loadShops()
}

const toggleStatus = async (row: any, newStatus: boolean) => {
  const targetStatus = newStatus ? 1 : 0
  if (row.status === targetStatus) return
  row._statusLoading = true
  try {
    await updateStoreStatus(row.id, targetStatus)
    row.status = targetStatus
    ElMessage.success(targetStatus === 1 ? '店铺已开启营业' : '店铺已关闭')
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._statusLoading = false
  }
}

const showDetail = async (row: Store) => {
  currentShop.value = null
  detailVisible.value = true
  try {
    const detail = await getAdminStoreDetail(row.id)
    currentShop.value = detail
  } catch {
    // 拦截器已处理后端错误提示
  }
}

const openEdit = (row: any) => {
  editingShop.value = row
  editForm.name = row.name || ''
  editForm.phone = row.phone || ''
  editForm.address = row.address || ''
  editForm.description = row.description || ''
  editVisible.value = true
}

const handleSave = async () => {
  if (!editingShop.value?.id) { ElMessage.warning('未定位到店铺'); return }
  saving.value = true
  try {
    await updateStore({ id: editingShop.value.id, name: editForm.name, phone: editForm.phone, address: editForm.address, description: editForm.description })
    ElMessage.success('店铺信息已更新')
    editVisible.value = false
    loadShops()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    saving.value = false
  }
}

// ====== 开店审核 ======
const auditLoading = ref(false)
const applyList = ref<any[]>([])
const auditPagination = reactive({ page: 1, pageSize: 10, total: 0 })
const auditStatusFilter = ref<number>(2)

const auditTitle = computed(() => {
  switch (auditStatusFilter.value) {
    case 2: return '待审核申请'
    case 3: return '已驳回申请'
    default: return '开店申请'
  }
})

const auditEmptyText = computed(() => {
  switch (auditStatusFilter.value) {
    case 2: return '暂无待审核申请'
    case 3: return '暂无已驳回申请'
    default: return '暂无开店申请记录'
  }
})

const applyDetailVisible = ref(false)
const applyDetail = ref<any>(null)

const rejectDialogVisible = ref(false)
const rejectingShop = ref<any>(null)
const rejectReason = ref('')
const rejecting = ref(false)

const getApplyStatusText = (status?: number) => {
  switch (status) {
    case 2: return '待审核'
    case 3: return '已驳回'
    default: return '未知'
  }
}

const getApplyStatusClass = (status?: number) => {
  switch (status) {
    case 2: return 'status-badge--pending'
    case 3: return 'status-badge--rejected'
    default: return 'status-badge--inactive'
  }
}

const getApplyStatusDotClass = (status?: number) => {
  switch (status) {
    case 2: return 'status-dot--pending'
    case 3: return 'status-dot--rejected'
    default: return 'status-dot--inactive'
  }
}

const loadApplyList = async () => {
  auditLoading.value = true
  try {
    const params: Record<string, any> = {
      page: auditPagination.page,
      pageSize: auditPagination.pageSize,
      status: auditStatusFilter.value
    }
    const result = await getAdminApplyPage(params)
    applyList.value = (result.list || []).map((item: any) => ({ ...item, _approving: false, _rejecting: false }))
    auditPagination.total = result.total || 0
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    auditLoading.value = false
  }
}

const handleAuditSearch = () => { auditPagination.page = 1; loadApplyList() }

const handleApprove = async (row: any) => {
  row._approving = true
  try {
    await approveStoreApply(row.id)
    ElMessage.success('审核通过，店铺已开通')
    loadApplyList()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    row._approving = false
  }
}

const openRejectDialog = (row: any) => {
  rejectingShop.value = row
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) { ElMessage.warning('请输入驳回原因'); return }
  rejecting.value = true
  try {
    await rejectStoreApply(rejectingShop.value.id, rejectReason.value.trim())
    ElMessage.success('已驳回该开店申请')
    rejectDialogVisible.value = false
    loadApplyList()
  } catch {
    // 拦截器已处理后端错误提示
  } finally {
    rejecting.value = false
  }
}

const showApplyDetail = async (row: any) => {
  applyDetail.value = null
  applyDetailVisible.value = true
  try {
    const detail = await getAdminStoreDetail(row.id)
    applyDetail.value = detail
  } catch {
    // 拦截器已处理后端错误提示
  }
}

// ====== 工具函数 ======
const formatTime = (time: string | undefined) => {
  if (!time) return '—'
  try {
    const d = new Date(time)
    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch { return time }
}

const getImageUrl = (path?: string | null) => {
  if (!path) return ''
  return getStoreLogoUrl(String(path))
}

const headerCellStyle = () => ({
  background: '#F7F7F5',
  color: '#1C1C1E',
  fontWeight: 700,
  fontSize: '12px',
  borderBottom: '1px solid #E8E8E6',
  padding: '14px 0'
})

const cellStyle = () => ({
  borderBottom: '1px solid #F0F0EE',
  padding: '14px 0'
})

watch(activeTab, (tab) => {
  if (tab === 'audit' && applyList.value.length === 0) {
    loadApplyList()
  }
})

onMounted(() => { loadShops() })
</script>

<style scoped>
.admin-shops {
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
  gap: 16px;
}

.bento-card {
  background: #FFFFFF;
  border-radius: 20px;
  border: 1px solid #E8E8E6;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.welcome-card {
  grid-column: span 12;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 24px 28px;
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

.tab-pills {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #F5F5F4;
  padding: 4px;
  border-radius: 12px;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 10px;
  background: transparent;
  color: #6B6B6E;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.tab-pill:hover {
  color: #1C1C1E;
  background: rgba(255, 255, 255, 0.6);
}

.tab-pill.active {
  background: #FFFFFF;
  color: #3B6E6E;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.tab-pill__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #ef4444;
  color: #FFFFFF;
  font-size: 10px;
  font-weight: 700;
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
   筛选区
   =========================== */
.filter-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.status-filter-section {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.status-filter-label {
  font-size: 12px;
  font-weight: 700;
  color: #6B6B6E;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  padding-top: 9px;
  white-space: nowrap;
}

.status-pills {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: #4B4B4E;
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  padding: 8px 14px;
  border-radius: 22px;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
}

.status-pill:hover {
  border-color: #C8A464;
  color: #1C1C1E;
  background: #FDFCFA;
  box-shadow: 0 2px 6px rgba(200, 164, 100, 0.1);
}

.status-pill.active {
  background: rgba(200, 164, 100, 0.12);
  border-color: rgba(200, 164, 100, 0.5);
  color: #7A5C22;
  box-shadow: 0 2px 6px rgba(200, 164, 100, 0.12);
}

.filter-divider {
  height: 1px;
  background: linear-gradient(to right, #F0F0EE 0%, transparent 100%);
}

.filter-row {
  display: flex;
  align-items: flex-end;
  gap: 14px;
  flex-wrap: wrap;
}

.filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-field--grow {
  flex: 1;
  min-width: 180px;
}

.filter-field label {
  font-size: 11px;
  font-weight: 700;
  color: #6B6B6E;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.filter-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.filter-input-wrap > svg:first-child {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #A1A1AA;
  pointer-events: none;
}

.filter-input-wrap input {
  width: 100%;
  height: 36px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  padding: 0 32px 0 36px;
  font-size: 13px;
  color: #1C1C1E;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: #FFFFFF;
}

.filter-input-wrap input:focus {
  border-color: #C8A464;
  box-shadow: 0 0 0 3px rgba(200, 164, 100, 0.1);
}

.input-clear {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  border: none;
  border-radius: 50%;
  background: #E8E8E6;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
}

.input-clear:hover {
  background: #D4D4D2;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}

.primary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.primary-btn--gold {
  background: linear-gradient(135deg, #C8A464 0%, #B08B45 100%);
  color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(200, 164, 100, 0.25);
}

.primary-btn--gold:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(200, 164, 100, 0.35);
}

.primary-btn--danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #FFFFFF;
  box-shadow: 0 2px 8px rgba(239, 68, 68, 0.25);
}

.primary-btn--danger:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.35);
}

.primary-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  transform: none !important;
}

.secondary-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 36px;
  padding: 0 16px;
  border: 1px solid #E8E8E6;
  border-radius: 10px;
  background: #FFFFFF;
  color: #6B6B6E;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary-btn:hover {
  border-color: #CCC;
  color: #1C1C1E;
  background: #FAFAF9;
}

.secondary-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

/* ===========================
   表格
   =========================== */
.table-container {
  padding: 0 24px;
}

.shop-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.shop-logo {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #F0F0EE;
  flex-shrink: 0;
  background: #FAFAF9;
}

.shop-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.shop-logo--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #C8A464 0%, #B08B45 100%);
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 700;
}

.shop-name-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.shop-name {
  font-weight: 700;
  color: #1C1C1E;
  font-size: 14px;
}

.shop-seller {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 500;
}

.cell-name {
  font-weight: 700;
  color: #1C1C1E;
  font-size: 14px;
}

.cell-muted {
  color: #6B6B6E;
  font-size: 13px;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;
}

.status-badge--active {
  color: #15803d;
  background: rgba(34, 197, 94, 0.12);
}

.status-badge--inactive {
  color: #6B7280;
  background: #F3F4F6;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.status-dot--active { background: #22c55e; }
.status-dot--inactive { background: #9CA3AF; }
.status-dot--pending { background: #f59e0b; }
.status-dot--rejected { background: #ef4444; }

.status-badge--pending {
  color: #b45309;
  background: rgba(245, 158, 11, 0.12);
}

.status-badge--rejected {
  color: #b91c1c;
  background: rgba(239, 68, 68, 0.1);
}

.time-text {
  font-size: 12px;
  color: #909399;
}

.action-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.action-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 1px solid #E8E8E6;
  background: #FFFFFF;
  color: #6B6B6E;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-icon-btn:hover {
  transform: translateY(-1px);
}

.action-icon-btn--view {
  color: #3B6E6E;
  border-color: rgba(59, 110, 110, 0.3);
  background: rgba(59, 110, 110, 0.06);
}

.action-icon-btn--view:hover {
  background: rgba(59, 110, 110, 0.12);
  border-color: rgba(59, 110, 110, 0.5);
}

.action-icon-btn--edit {
  color: #C8A464;
  border-color: rgba(200, 164, 100, 0.35);
  background: rgba(200, 164, 100, 0.08);
}

.action-icon-btn--edit:hover {
  background: rgba(200, 164, 100, 0.14);
  border-color: rgba(200, 164, 100, 0.55);
}

.action-icon-btn--open {
  color: #22c55e;
  border-color: rgba(34, 197, 94, 0.35);
  background: rgba(34, 197, 94, 0.08);
}

.action-icon-btn--open:hover {
  background: rgba(34, 197, 94, 0.14);
  border-color: rgba(34, 197, 94, 0.55);
}

.action-icon-btn--close {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.06);
}

.action-icon-btn--close:hover {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.5);
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  height: 30px;
  padding: 0 12px;
  border-radius: 15px;
  border: 1px solid;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.action-btn--approve {
  color: #15803d;
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.35);
}

.action-btn--approve:hover {
  background: #22c55e;
  color: #FFFFFF;
  border-color: #22c55e;
}

.action-btn--reject {
  color: #b91c1c;
  background: rgba(239, 68, 68, 0.08);
  border-color: rgba(239, 68, 68, 0.3);
}

.action-btn--reject:hover {
  background: #ef4444;
  color: #FFFFFF;
  border-color: #ef4444;
}

:deep(.shop-row:hover > td) {
  background: #FAFAF9 !important;
}

/* ===========================
   分页
   =========================== */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 18px 24px;
  border-top: 1px solid #F0F0EE;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  background: #FFFFFF;
  border: 1px solid #E8E8E6;
  color: #6B6B6E;
  font-weight: 500;
  border-radius: 8px;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #3B6E6E;
  border-color: #3B6E6E;
  color: #FFFFFF;
}

/* ===========================
   弹窗
   =========================== */
.shop-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
  margin-right: 0;
}

.shop-dialog :deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 700;
  color: #1C1C1E;
}

.shop-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 24px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-hero {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  background: #FAFAF9;
  border-radius: 16px;
  border: 1px solid #F0F0EE;
}

.detail-hero-logo {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid #F0F0EE;
  flex-shrink: 0;
  background: #FFFFFF;
}

.detail-hero-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-hero-logo--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #C8A464 0%, #B08B45 100%);
  color: #FFFFFF;
  font-size: 24px;
  font-weight: 700;
}

.detail-hero-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.detail-hero-name {
  font-size: 18px;
  font-weight: 700;
  color: #1C1C1E;
}

.detail-hero-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.id-tag {
  font-size: 12px;
  color: #A1A1AA;
  font-weight: 600;
}

.detail-section {
  border-top: 1px solid #F0F0EE;
  padding-top: 20px;
}

.detail-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 700;
  color: #1C1C1E;
}

.detail-section-title svg {
  color: #C8A464;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-card {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 14px 16px;
  background: #FAFAF9;
  border-radius: 12px;
  border: 1px solid #F0F0EE;
}

.detail-card--wide {
  grid-column: 1 / -1;
}

.detail-card--danger {
  background: rgba(239, 68, 68, 0.06);
  border-color: rgba(239, 68, 68, 0.2);
}

.detail-card--danger .detail-card__label {
  color: #ef4444;
}

.detail-card__value--danger {
  color: #b91c1c;
}

.detail-card__label {
  font-size: 11px;
  color: #A1A1AA;
  font-weight: 600;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.detail-card__value {
  font-size: 13px;
  color: #1C1C1E;
  font-weight: 600;
  line-height: 1.5;
}

.detail-card__desc {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.6;
}

.empty-detail {
  padding: 40px 0;
}

.edit-form :deep(.el-form-item__label) {
  font-size: 12px;
  font-weight: 600;
  color: #6B6B6E;
}

.edit-form :deep(.el-input__wrapper),
.edit-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E8E8E6 inset;
}

.edit-form :deep(.el-input__wrapper.is-focus),
.edit-form :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #C8A464 inset, 0 0 0 3px rgba(200, 164, 100, 0.1);
}

.edit-form :deep(.el-textarea__inner) {
  padding: 10px 12px;
}

.reject-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reject-warning {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  background: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 12px;
  font-size: 14px;
  color: #991b1b;
  line-height: 1.5;
}

.reject-warning svg {
  flex-shrink: 0;
  margin-top: 1px;
  color: #ef4444;
}

.reject-warning strong {
  color: #ef4444;
}

.reject-hint {
  margin: 0;
  font-size: 13px;
  color: #6B6B6E;
  line-height: 1.5;
}

.btn-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===========================
   响应式
   =========================== */
@media (max-width: 900px) {
  .welcome-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .status-filter-section {
    flex-direction: column;
    gap: 10px;
  }

  .status-filter-label {
    padding-top: 0;
  }

  .filter-actions {
    margin-left: 0;
    width: 100%;
  }

  .filter-actions .primary-btn,
  .filter-actions .secondary-btn {
    flex: 1;
  }

  .filter-row {
    width: 100%;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .list-card__header {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-container {
    padding: 0 16px;
    overflow-x: auto;
  }

  .pagination-bar {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .tab-pills {
    width: 100%;
  }

  .tab-pill {
    flex: 1;
    justify-content: center;
  }

  .stat-card {
    padding: 14px 16px;
  }

  .filter-body {
    padding: 18px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .tab-pill,
  .primary-btn,
  .secondary-btn,
  .icon-btn,
  .action-icon-btn,
  .action-btn {
    transition: none;
  }

  .btn-spinner {
    animation: none;
  }
}
</style>
