<template>
  <div class="profile-container">
    <NavBar />

    <div class="profile-content">
      <!-- 侧边栏 -->
      <aside class="sidebar">
        <div class="sidebar-user">
          <div class="sidebar-avatar">
            <img :src="avatarUrl" alt="avatar" />
          </div>
          <div class="sidebar-user-info">
            <span class="sidebar-user-name">{{ userInfo?.username || '用户' }}</span>
          </div>
        </div>

        <nav class="sidebar-nav">
          <button
            v-for="item in menuItems"
            :key="item.key"
            :class="['nav-item', { active: activeSideMenu === item.key, danger: item.danger }]"
            @click="handleSideMenuSelect(item.key)"
          >
            <span class="nav-icon" v-html="item.icon"></span>
            <span class="nav-label">{{ item.label }}</span>
            <span v-if="item.badge && item.badge > 0" class="nav-badge">{{ item.badge }}</span>
          </button>
        </nav>

        <div class="sidebar-footer">
          <button class="nav-item danger" @click="handleLogout">
            <span class="nav-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            </span>
            <span class="nav-label">退出登录</span>
          </button>
        </div>
      </aside>

      <!-- 主内容区 -->
      <main class="main-content">
        <div v-loading="loading" class="content-box">
          <!-- ===== 概览看板 ===== -->
          <div v-if="activeSideMenu === 'overview'" class="overview-section animate-in">
            <div class="bento-grid">
              <!-- 欢迎卡片 -->
              <div class="bento-cell welcome-cell">
                <div class="welcome-card">
                  <div class="welcome-text">
                    <span class="welcome-eyebrow">{{ todayDate }}</span>
                    <h1 class="welcome-title">{{ timeGreeting }}，{{ userInfo?.username || '用户' }}</h1>
                    <p class="welcome-sub">欢迎回到你的个人中心</p>
                  </div>
                  <div class="welcome-avatar">
                    <img :src="avatarUrl" alt="avatar" />
                  </div>
                  <div class="welcome-pattern"></div>
                </div>
              </div>

              <!-- 账户概览 -->
              <div class="bento-cell account-cell">
                <div class="account-card">
                  <div class="account-header">
                    <span class="account-label">账户状态</span>
                    <span class="account-badge">正常</span>
                  </div>
                  <div class="account-meta">
                    <div class="meta-item">
                      <span class="meta-value" :class="{ active: profileData?.phone }">{{ profileData?.phone ? '已绑定' : '未绑定' }}</span>
                      <span class="meta-label">手机号</span>
                    </div>
                    <div class="meta-item">
                      <span class="meta-value" :class="{ active: profileData?.email }">{{ profileData?.email ? '已绑定' : '未绑定' }}</span>
                      <span class="meta-label">邮箱</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 订单状态卡片 -->
              <div v-for="(stat, idx) in orderStats" :key="idx" class="bento-cell stat-cell">
                <div class="stat-card-v2" @click="goToOrdersTab(stat.tab)">
                  <div class="stat-header">
                    <span class="stat-name">{{ stat.label }}</span>
                    <div class="stat-icon-v2">
                      <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" v-html="stat.icon"></svg>
                    </div>
                  </div>
                  <div class="stat-body">
                    <span class="stat-value" v-html="loadingStatusCount ? '<span class=shimmer>--</span>' : stat.value"></span>
                  </div>
                  <div class="stat-footer">
                    <span class="stat-hint">{{ stat.hint }}</span>
                    <span class="stat-arrow">→</span>
                  </div>
                </div>
              </div>

              <!-- 快捷入口 -->
              <div class="bento-cell quick-cell">
                <div class="quick-panel">
                  <h3 class="panel-title">快捷入口</h3>
                  <div class="quick-list">
                    <div v-for="(item, idx) in quickItems" :key="idx" class="quick-row" @click="handleSideMenuSelect(item.key)">
                      <div class="quick-row-icon">
                        <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" v-html="item.icon"></svg>
                      </div>
                      <div class="quick-row-text">
                        <span class="quick-row-label">{{ item.label }}</span>
                        <span class="quick-row-desc">{{ item.desc }}</span>
                      </div>
                      <span class="quick-row-arrow">→</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- ===== 个人资料 ===== -->
          <div v-else-if="activeSideMenu === 'info'" class="profile-section animate-in">
            <div v-loading="profileLoading" class="profile-card-v3">
              <div class="profile-cover">
                <div class="profile-cover-pattern"></div>
              </div>

              <div class="profile-head-v3">
                <div class="profile-avatar-area-v3">
                  <div class="avatar-frame-v3" @click="handleAvatarClick" title="点击更换头像">
                    <img :src="avatarUrl" alt="avatar" class="profile-avatar-img" />
                    <div class="avatar-overlay-v3">
                      <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
                    </div>
                  </div>
                  <input ref="fileInputRef" type="file" accept="image/jpeg,image/png,image/gif,image/webp" style="display:none" @change="handleAvatarChange" />
                  <button class="avatar-change-link" @click="handleAvatarClick">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
                    更换头像
                  </button>
                </div>

                <div class="profile-meta-v3">
                  <h2 class="profile-username-v3">{{ profileData?.username || '-' }}</h2>
                  <div class="profile-meta-row">
                    <span class="profile-id">ID: {{ profileData?.id || '-' }}</span>
                  </div>
                </div>

                <button class="profile-edit-btn-v3" @click="showEditDialog = true">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                  编辑资料
                </button>
              </div>

              <div class="profile-body-v3">
                <div class="info-section">
                  <h4 class="info-section-title">基本信息</h4>
                  <div class="info-grid-v3">
                    <div class="info-cell-v3">
                      <span class="cell-label-v3">用户名</span>
                      <span class="cell-value-v3">{{ profileData?.username || '-' }}</span>
                    </div>
                    <div class="info-cell-v3">
                      <span class="cell-label-v3">真实姓名</span>
                      <span class="cell-value-v3">{{ profileData?.realName || '未设置' }}</span>
                    </div>
                    <div class="info-cell-v3">
                      <span class="cell-label-v3">手机号</span>
                      <span class="cell-value-v3">{{ profileData?.phone || '未绑定' }}</span>
                    </div>
                    <div class="info-cell-v3">
                      <span class="cell-label-v3">邮箱</span>
                      <span class="cell-value-v3">{{ profileData?.email || '未绑定' }}</span>
                    </div>
                    <div class="info-cell-v3 full">
                      <span class="cell-label-v3">注册时间</span>
                      <span class="cell-value-v3 mono">{{ formatTime(profileData?.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 编辑资料弹窗 -->
            <el-dialog
              v-model="showEditDialog"
              width="440px"
              :show-close="false"
              destroy-on-close
              class="profile-edit-dialog-v3"
            >
              <div class="edit-dialog-header">
                <div class="edit-dialog-avatar" @click="handleAvatarClick">
                  <img :src="avatarUrl" alt="avatar" />
                  <div class="edit-avatar-overlay">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></svg>
                  </div>
                </div>
                <div class="edit-dialog-title">
                  <h4>编辑个人资料</h4>
                  <p>点击头像可更换</p>
                </div>
              </div>
              <el-form :model="editForm" label-position="top" class="edit-form-v3">
                <el-form-item label="用户名">
                  <el-input v-model="editForm.username" placeholder="输入新的用户名" maxlength="20" size="large" />
                </el-form-item>
              </el-form>
              <template #footer>
                <div class="edit-dialog-footer">
                  <button class="edit-cancel-btn" @click="showEditDialog = false">取消</button>
                  <button class="edit-save-btn" :disabled="savingProfile" @click="handleSaveProfile">
                    <span v-if="!savingProfile">保存</span>
                    <span v-else>保存中...</span>
                  </button>
                </div>
              </template>
            </el-dialog>
          </div>

          <!-- 我的订单 -->
          <div v-else-if="activeSideMenu === 'orders'" class="orders-section animate-in">
            <div class="order-header">
              <div class="order-header-top">
                <h3>我的订单</h3>
                <span class="order-total-count">共 {{ pagination.total }} 条</span>
              </div>
              <el-tabs v-model="activeOrderTab" type="card" class="order-tabs">
                <el-tab-pane label="全部" name="all"/>
                <el-tab-pane label="待付款" name="pending_pay"/>
                <el-tab-pane label="待发货" name="pending_ship"/>
                <el-tab-pane label="待收货" name="pending_receive"/>
                <el-tab-pane label="已完成" name="completed"/>
                <el-tab-pane label="已取消" name="cancelled"/>
                <el-tab-pane label="退款中" name="refunding"/>
                <el-tab-pane label="已退款" name="refunded"/>
                <el-tab-pane label="已拒绝" name="rejected"/>
              </el-tabs>
            </div>

            <div v-if="loading" class="order-loading">
              <el-skeleton :rows="3" animated class="order-skeleton" />
              <el-skeleton :rows="3" animated class="order-skeleton" />
              <el-skeleton :rows="3" animated class="order-skeleton" />
            </div>

            <div v-else-if="orders.length > 0" class="order-list">
              <div
                v-for="(order, idx) in orders"
                :key="order.id"
                :class="['order-card-v2', 'status-topline-' + order.status]"
                :style="{ '--i': idx }"
                @click="viewDetail(order)"
              >
                <div class="order-card-main">
                  <div class="order-card-top">
                    <div class="order-meta">
                      <span class="order-time">{{ formatDate(order.createdAt) }}</span>
                      <div class="order-no-wrap">
                        <span class="order-no" :title="order.orderNo">{{ order.orderNo }}</span>
                        <button class="order-copy-btn" title="复制订单号" @click="copyOrderNo(order.orderNo, $event)">
                          <svg viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></svg>
                        </button>
                      </div>
                    </div>
                    <span class="status-pill" :class="'pill-' + order.status">
                      <svg class="pill-icon" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" v-html="getStatusIcon(order.status)"></svg>
                      {{ getStatusText(order.status) }}
                    </span>
                  </div>

                  <div class="order-products">
                    <div class="product-main" v-if="order.items && order.items.length">
                      <img
                        class="product-main-img"
                        :src="orderItemImage(order.items[0]?.productImage || '')"
                        :alt="order.items[0]?.productName || ''"
                      />
                      <div class="product-main-info">
                        <div class="product-main-name">{{ order.items[0]?.productName }}</div>
                        <div class="product-main-specs" v-if="order.items[0]?.skuSpecs">
                          {{ order.items[0]?.skuSpecs }}
                        </div>
                        <div class="product-main-meta">
                          <span class="product-main-price">¥{{ (order.items[0]?.price || 0).toFixed(2) }}</span>
                          <span class="product-main-qty">×{{ order.items[0]?.quantity }}</span>
                        </div>
                      </div>
                    </div>
                    <div v-else class="order-summary-fallback">
                      共 {{ order.itemCount || '-' }} 件商品
                    </div>

                    <div class="product-side">
                      <div class="extra-thumbs" v-if="order.items && order.items.length > 1">
                        <img
                          v-for="(item, i) in order.items.slice(1, 4)"
                          :key="i"
                          class="extra-thumb"
                          :src="orderItemImage(item.productImage)"
                          :alt="item.productName"
                        />
                        <span v-if="order.items.length > 4" class="extra-more">+{{ order.items.length - 4 }}</span>
                      </div>
                      <div class="order-amounts-mini">
                        <div class="mini-row" v-if="order.discountAmount">
                          <span>优惠</span>
                          <span class="mini-discount">-¥{{ Number(order.discountAmount).toFixed(2) }}</span>
                        </div>
                        <div class="mini-row pay">
                          <span>实付</span>
                          <span class="mini-pay">¥{{ (order.payAmount || 0).toFixed(2) }}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="order-card-hints">
                    <div v-if="order.status === 1 && order.expireTime" class="status-hint hint-expire">
                      <el-icon class="hint-icon"><Clock /></el-icon>
                      剩余 {{ formatExpireTime(order.expireTime, countdownNow) }}
                    </div>
                    <div v-else-if="(order.status === 6 || order.status === 7) && order.refundAmount !== null" class="status-hint hint-refund">
                      退款 ¥{{ Number(order.refundAmount).toFixed(2) }}
                    </div>
                    <div v-else-if="order.status === 8 && order.rejectReason" class="status-hint hint-cancel">
                      拒绝原因：{{ order.rejectReason }}
                    </div>
                    <div v-else-if="order.deliveryCompany && order.deliveryNo" class="status-hint hint-delivery">
                      <el-icon class="hint-icon"><Van /></el-icon>
                      {{ order.deliveryCompany }} {{ order.deliveryNo }}
                    </div>
                    <div v-else-if="order.status === 5 && order.cancelReason" class="status-hint hint-cancel">
                      {{ order.cancelReason }}
                    </div>
                    <div v-else-if="order.status === 2" class="status-hint hint-pending">
                      等待卖家发货
                    </div>
                    <div v-if="order.remark" class="status-hint hint-remark">
                      备注：{{ order.remark }}
                    </div>
                  </div>
                </div>

                <div class="order-card-actions" @click.stop>
                  <template v-if="order.status === 1">
                    <button class="action-btn-v2 secondary" @click="showCancelDialog(order)">取消</button>
                    <button class="action-btn-v2 primary" @click="handlePay(order)">立即支付</button>
                  </template>
                  <template v-else-if="order.status === 2">
                    <button class="action-btn-v2 ghost" @click="handleRefund(order)">申请退款</button>
                  </template>
                  <template v-else-if="order.status === 3">
                    <button class="action-btn-v2 primary" @click="handleReceive(order)">确认收货</button>
                    <button class="action-btn-v2 ghost" @click="handleRefund(order)">申请退款</button>
                  </template>
                  <template v-else-if="order.status === 4">
                    <button class="action-btn-v2 ghost" @click="handleRefund(order)">申请退款</button>
                    <button class="action-btn-v2 ghost danger" @click="handleDelete(order)">删除</button>
                  </template>
                  <template v-else-if="order.status === 5">
                    <button class="action-btn-v2 ghost danger" @click="handleDelete(order)">删除</button>
                  </template>
                  <template v-else-if="order.status === 6">
                    <button class="action-btn-v2 ghost" @click="handleCancelRefund(order)">取消退款</button>
                    <span class="status-tip">退款审核中</span>
                  </template>
                  <template v-else-if="order.status === 7">
                    <span class="status-tip refund-done">已退款</span>
                  </template>
                  <template v-else-if="order.status === 8">
                    <button class="action-btn-v2 ghost" @click="handleCancelRefund(order)">取消退款</button>
                  </template>
                </div>
              </div>

              <!-- 分页 -->
              <div v-if="pagination.total > 0" class="pagination-wrap">
                <el-pagination
                  v-model:current-page="pagination.page"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[12, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  background
                  small
                  @size-change="loadOrders(true)"
                  @current-change="() => loadOrders()"
                />
              </div>
            </div>

            <div v-else class="empty-orders">
              <el-empty :image-size="140" description="暂无订单">
                <el-button type="primary" round @click="goHome">去购物</el-button>
              </el-empty>
            </div>
          </div>

          <!-- 收货地址 -->
          <div v-else-if="activeSideMenu === 'address'" class="address-section animate-in">
            <div class="address-section-header">
              <div class="address-section-title">
                <h3>收货地址</h3>
                <p>管理你的配送地址，让收货更便捷</p>
              </div>
              <button
                class="address-add-btn"
                @click="showAddAddressModalHandler"
                :disabled="addresses.length >= 10"
              >
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                添加地址
              </button>
            </div>
            <div v-if="addresses.length > 0" class="address-list">
              <div
                v-for="(addr, idx) in addresses"
                :key="addr.id"
                class="address-card"
                :class="{ 'is-default': addr.isDefault === 1 }"
                :style="{ '--i': idx }"
              >
                <div class="address-card-glow"></div>
                <div class="address-card-content">
                  <div class="address-main">
                    <div class="address-header">
                      <div class="address-user">
                        <span class="name">{{ addr.receiverName }}</span>
                        <span class="phone">{{ addr.receiverPhone }}</span>
                      </div>
                      <div class="address-badges">
                        <span v-if="addr.isDefault === 1" class="default-badge">
                          <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                          默认地址
                        </span>
                      </div>
                    </div>
                    <div class="address-detail">
                      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                      <span>{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}</span>
                    </div>
                  </div>
                  <div class="address-actions">
                    <button
                      v-if="addr.isDefault !== 1"
                      class="address-action-btn primary"
                      @click="confirmSetDefault(addr)"
                    >
                      <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                      设为默认
                    </button>
                    <button class="address-action-btn" @click="editAddress(addr)">
                      <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      编辑
                    </button>
                    <button class="address-action-btn danger" @click="confirmDeleteAddress(addr)">
                      <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/><line x1="10" y1="11" x2="10" y2="17"/><line x1="14" y1="11" x2="14" y2="17"/></svg>
                      删除
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-address">
              <div class="empty-address-icon">
                <svg viewBox="0 0 24 24" width="48" height="48" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
              </div>
              <h4>暂无收货地址</h4>
              <p>添加一个收货地址，开启便捷购物体验</p>
              <button class="address-add-btn" @click="showAddAddressModalHandler">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                添加地址
              </button>
            </div>
            <p v-if="addresses.length >= 10" class="address-limit">* 最多可添加 10 个收货地址</p>
          </div>

          <!-- 安全设置 -->
          <div v-else-if="activeSideMenu === 'security'" class="security-section animate-in">
            <div class="security-header">
              <h3>安全设置</h3>
              <p>保护你的账户信息与资金安全</p>
            </div>
            <div class="security-list">
              <div class="security-item" style="--i: 0">
                <div class="security-icon">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
                </div>
                <div class="security-info">
                  <h4>登录密码</h4>
                  <p>定期更换密码，保护账户安全</p>
                </div>
                <button class="security-action-btn" @click="showChangePasswordModal = true">
                  <span>修改</span>
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
                </button>
              </div>
              <div class="security-item" style="--i: 1">
                <div class="security-icon phone">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z"/></svg>
                </div>
                <div class="security-info">
                  <h4>手机号</h4>
                  <p>{{ userInfo?.phone ? maskPhone(userInfo.phone) : '未绑定手机号' }}</p>
                </div>
                <button class="security-action-btn" @click="showChangePhoneModal = true">
                  <span>{{ userInfo?.phone ? '更换' : '绑定' }}</span>
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
                </button>
              </div>
              <div class="security-item" style="--i: 2">
                <div class="security-icon email">
                  <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
                </div>
                <div class="security-info">
                  <h4>邮箱</h4>
                  <p>{{ userInfo?.email ? maskEmail(userInfo.email) : '未绑定邮箱' }}</p>
                </div>
                <button class="security-action-btn" @click="showChangeEmailModal = true">
                  <span>{{ userInfo?.email ? '更换' : '绑定' }}</span>
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
                </button>
              </div>
            </div>
          </div>

          <!-- 默认显示 -->
          <div v-else class="default-section">
            <h3>欢迎来到个人中心</h3>
            <p>请从左侧菜单选择要查看的内容</p>
          </div>
        </div>
      </main>
    </div>

    <!-- 添加/编辑地址弹窗 -->
    <el-dialog
      v-model="showAddAddressModal"
      width="90%"
      :show-close="false"
      class="address-edit-dialog"
      destroy-on-close
    >
      <div class="address-edit-header">
        <div class="address-edit-icon">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
        </div>
        <div class="address-edit-title">
          <h4>{{ editingAddress ? '编辑地址' : '添加地址' }}</h4>
          <p>{{ editingAddress ? '更新你的收货地址信息' : '填写新的收货地址' }}</p>
        </div>
        <button class="address-edit-close" @click="closeAddressModal">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        </button>
      </div>
      <div class="address-form-body">
        <div class="address-form-row">
          <div class="address-form-field elegant">
            <label class="address-field-label-elegant">收货人</label>
            <div class="address-field-input-elegant">
              <span class="addr-input-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              </span>
              <input
                v-model="addressForm.receiverName"
                type="text"
                placeholder="请输入收货人姓名"
                maxlength="20"
              >
            </div>
            <p v-if="!addressForm.receiverName && showAddressFormError" class="address-field-error-elegant">请填写收货人姓名</p>
          </div>

          <div class="address-form-field elegant">
            <label class="address-field-label-elegant">手机号</label>
            <div class="address-field-input-elegant">
              <span class="addr-input-icon">
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></svg>
              </span>
              <input
                v-model="addressForm.receiverPhone"
                type="tel"
                placeholder="请输入11位手机号"
                maxlength="11"
              >
            </div>
            <p v-if="showAddressFormError && !addressForm.receiverPhone" class="address-field-error-elegant">请填写手机号</p>
            <p v-else-if="showAddressFormError && !isValidPhone(addressForm.receiverPhone)" class="address-field-error-elegant">手机号格式不正确，请输入11位有效手机号</p>
          </div>
        </div>

        <div class="address-form-field elegant full">
          <label class="address-field-label-elegant">所在地区</label>
          <div class="address-field-input-elegant">
            <span class="addr-input-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
            </span>
            <el-cascader
              v-model="addressForm.region"
              :options="regionOptions"
              placeholder="请选择省 / 市 / 区"
              :props="{ checkStrictly: false }"
              class="address-region-cascader-elegant"
              popper-class="address-region-popper"
            />
          </div>
          <p v-if="!addressForm.region.length && showAddressFormError" class="address-field-error-elegant">请选择所在地区</p>
        </div>

        <div class="address-form-field elegant full">
          <label class="address-field-label-elegant">详细地址</label>
          <div class="address-field-input-elegant textarea">
            <span class="addr-input-icon">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 15s1-1 5-1 8 2 8 2V3s-4-2-8-2-5 1-5 1v15z"/><line x1="4" y1="4" x2="4" y2="20"/><path d="M10 11h6"/><path d="M13 8v6"/></svg>
            </span>
            <textarea
              v-model="addressForm.detailAddress"
              rows="2"
              placeholder="请输入街道、门牌号、楼层、房间号等详细地址"
              maxlength="100"
            />
          </div>
          <p v-if="!addressForm.detailAddress && showAddressFormError" class="address-field-error-elegant">请填写详细地址</p>
        </div>

        <div class="address-form-field inline">
          <label class="address-checkbox" @click="addressIsDefault = !addressIsDefault">
            <span class="address-custom-checkbox" :class="{ checked: addressIsDefault }">
              <svg v-if="addressIsDefault" viewBox="0 0 24 24" width="12" height="12" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            </span>
            <span class="address-checkbox-label">设为默认地址</span>
          </label>
          <p class="address-checkbox-tip">设置后下单时将优先使用该地址</p>
        </div>
      </div>
      <template #footer>
        <div class="address-edit-footer">
          <button class="address-edit-cancel" @click="closeAddressModal">取消</button>
          <button class="address-edit-save" :disabled="loading" @click="confirmSaveAddress">
            <span v-if="loading">保存中...</span>
            <span v-else>{{ editingAddress ? '修改' : '确认添加' }}</span>
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="showChangePasswordModal"
      width="420px"
      :show-close="false"
      class="security-dialog password-dialog"
      destroy-on-close
    >
      <div class="security-dialog-header">
        <div class="security-dialog-icon">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>
        </div>
        <div class="security-dialog-title">
          <h4>修改登录密码</h4>
          <p>建议使用字母、数字组合，提高安全性</p>
        </div>
      </div>
      <el-form :model="passwordForm" label-position="top" class="security-form">
        <el-form-item label="原密码">
          <el-input
            v-model="passwordForm.oldPassword"
            :type="showOldPassword ? 'text' : 'password'"
            placeholder="请输入原密码"
            size="large"
          >
            <template #suffix>
              <button
                type="button"
                class="password-toggle-btn"
                @click="showOldPassword = !showOldPassword"
              >
                <svg v-if="!showOldPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.06M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            :type="showNewPassword ? 'text' : 'password'"
            placeholder="请输入新密码"
            size="large"
          >
            <template #suffix>
              <button
                type="button"
                class="password-toggle-btn"
                @click="showNewPassword = !showNewPassword"
              >
                <svg v-if="!showNewPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.06M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            placeholder="请再次输入新密码"
            size="large"
          >
            <template #suffix>
              <button
                type="button"
                class="password-toggle-btn"
                @click="showConfirmPassword = !showConfirmPassword"
              >
                <svg v-if="!showConfirmPassword" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-5.95 5.06M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/>
                </svg>
              </button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="security-dialog-footer">
          <button class="security-cancel-btn" @click="showChangePasswordModal = false">取消</button>
          <button class="security-submit-btn" @click="changePassword">确认修改</button>
        </div>
      </template>
    </el-dialog>

    <!-- 取消订单弹窗 -->
    <el-dialog title="取消订单" v-model="cancelDialogVisible" width="420px" :close-on-click-modal="false">
      <div class="dialog-body">
        <p class="dialog-tip">请选择取消该订单的原因：</p>
        <el-select v-model="cancelReason" placeholder="请选择取消原因" class="full-select">
          <el-option label="不想买了" value="不想买了" />
          <el-option label="商品价格太贵" value="商品价格太贵" />
          <el-option label="等待时间太长" value="等待时间太长" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">返回</el-button>
        <el-button type="primary" @click="confirmCancel" :loading="submitting">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 删除订单确认弹窗 -->
    <el-dialog title="删除订单" v-model="deleteDialogVisible" width="400px">
      <div class="dialog-body">
        <p class="dialog-tip warn">⚠️ 删除后不可恢复，确认删除该订单吗？</p>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="submitting">确认删除</el-button>
      </template>
    </el-dialog>

    <!-- 申请退款弹窗 -->
    <el-dialog v-model="refundDialogVisible" width="480px" :close-on-click-modal="false" top="8vh" class="refund-dialog">
      <template #header>
        <div class="refund-dialog-header">
          <span class="refund-dialog-icon"><el-icon><WarningFilled /></el-icon></span>
          <div>
            <div class="refund-dialog-title">申请退款</div>
            <div class="refund-dialog-sub">提交后需等待管理员审核</div>
          </div>
        </div>
      </template>

      <div class="refund-body">
        <!-- 退款商品信息 -->
        <div class="refund-order-info" v-if="refundTarget">
          <div class="refund-order-no">{{ refundTarget.orderNo }}</div>
          <div class="refund-items-preview" v-if="refundTarget.items && refundTarget.items.length">
            <div class="refund-item-row" v-for="(item, i) in refundTarget.items.slice(0, 2)" :key="i">
              <img class="refund-item-img" :src="orderItemImage(item.productImage)" />
              <div class="refund-item-detail">
                <div class="refund-item-name">{{ item.productName }}</div>
                <div class="refund-item-specs" v-if="item.skuSpecs">{{ item.skuSpecs }}</div>
              </div>
              <div class="refund-item-qty">&times;{{ item.quantity }}</div>
            </div>
            <div class="refund-more-items" v-if="refundTarget.items.length > 2">
              等 {{ refundTarget.items.length }} 件商品
            </div>
          </div>
          <div class="refund-amount-row">
            <span class="refund-amount-label">订单金额</span>
            <span class="refund-amount-value">¥{{ (refundTarget.payAmount || 0).toFixed(2) }}</span>
          </div>
        </div>

        <!-- 退款原因选择 -->
        <div class="refund-reason-section">
          <label class="refund-reason-label">退款原因</label>
          <el-select v-model="refundReason" placeholder="请选择退款原因" class="refund-reason-select">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品与描述不符" value="商品与描述不符" />
            <el-option label="发错货" value="发错货" />
            <el-option label="不想要了" value="不想要了" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </div>
      </div>

      <template #footer>
        <div class="refund-footer">
          <button class="action-btn secondary" @click="refundDialogVisible = false">取消</button>
          <button class="action-btn primary" @click="confirmRefund" :disabled="submitting || !refundReason">
            <span v-if="submitting" class="btn-loading-icon"></span>
            {{ submitting ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="detailDialogVisible" direction="rtl" size="560px" :with-header="false" class="order-detail-drawer">
      <div v-loading="detailLoading" class="detail-drawer-body">
        <template v-if="orderDetail">
          <!-- 抽屉头部 -->
          <div class="detail-drawer-header">
            <div class="detail-header-top">
              <span class="detail-status-pill" :class="'sp-' + (orderDetail?.status || 0)">
                {{ orderDetail?.statusDesc || getStatusText(orderDetail?.status || 0) }}
              </span>
              <span class="detail-order-no">{{ orderDetail?.orderNo }}</span>
            </div>
            <div class="detail-header-time">{{ formatTime(orderDetail.createdAt) }}</div>
          </div>

          <!-- 状态时间线 -->
          <div class="detail-timeline">
            <div class="timeline-item" :class="{ active: orderDetail.status >= 1 }">
              <div class="timeline-dot"></div>
              <span>下单</span>
            </div>
            <div class="timeline-line" :class="{ active: orderDetail.status >= 2 && orderDetail.status !== 5 }"></div>
            <div class="timeline-item" :class="{ active: orderDetail.status >= 2 && orderDetail.status !== 5 }">
              <div class="timeline-dot"></div>
              <span>付款</span>
            </div>
            <div class="timeline-line" :class="{ active: orderDetail.status >= 3 && orderDetail.status !== 5 }"></div>
            <div class="timeline-item" :class="{ active: orderDetail.status >= 3 && orderDetail.status !== 5 }">
              <div class="timeline-dot"></div>
              <span>发货</span>
            </div>
            <div class="timeline-line" :class="{ active: orderDetail.status >= 4 }"></div>
            <div class="timeline-item" :class="{ active: orderDetail.status >= 4 }">
              <div class="timeline-dot"></div>
              <span>完成</span>
            </div>
          </div>

          <!-- 商品清单 -->
          <div class="detail-section">
            <div class="detail-sec-title">商品清单</div>
            <div class="detail-item-card" v-for="(item, i) in orderDetail.items || []" :key="i">
              <img class="detail-item-img" :src="orderItemImage(item.productImage)" />
              <div class="detail-item-info">
                <div class="detail-item-name">{{ item.productName }}</div>
                <div class="detail-item-specs">{{ item.skuSpecs }}</div>
              </div>
              <div class="detail-item-meta">
                <span class="detail-item-price">¥{{ item.price.toFixed(2) }}</span>
                <span class="detail-item-qty">×{{ item.quantity }}</span>
              </div>
              <div class="detail-item-subtotal">¥{{ item.totalAmount.toFixed(2) }}</div>
            </div>
          </div>

          <!-- 金额汇总 -->
          <div class="detail-section detail-amounts-v2">
            <div class="amt-row"><span>商品金额</span><span>¥{{ orderDetail.totalAmount.toFixed(2) }}</span></div>
            <div class="amt-row" v-if="orderDetail.discountAmount"><span>优惠</span><span class="amt-discount">-¥{{ orderDetail.discountAmount.toFixed(2) }}</span></div>
            <div class="amt-row" v-if="orderDetail.freightAmount"><span>运费</span><span>¥{{ orderDetail.freightAmount.toFixed(2) }}</span></div>
            <div class="amt-divider"></div>
            <div class="amt-row amt-pay"><span>实付金额</span><span>¥{{ orderDetail.payAmount.toFixed(2) }}</span></div>
          </div>

          <!-- 订单信息 -->
          <div class="detail-section">
            <div class="detail-sec-title">订单信息</div>
            <div class="detail-info-grid">
              <div class="info-cell">
                <span class="icl">订单编号</span>
                <span class="icv mono">{{ orderDetail.orderNo }}</span>
              </div>
              <div class="info-cell">
                <span class="icl">下单时间</span>
                <span class="icv">{{ formatTime(orderDetail.createdAt) }}</span>
              </div>
              <div class="info-cell" v-if="orderDetail.payTime">
                <span class="icl">支付时间</span>
                <span class="icv">{{ formatTime(orderDetail.payTime) }}</span>
              </div>
              <div class="info-cell" v-if="orderDetail.receiveTime">
                <span class="icl">收货时间</span>
                <span class="icv">{{ formatTime(orderDetail.receiveTime) }}</span>
              </div>
              <div class="info-cell" v-if="orderDetail.payTypeDesc">
                <span class="icl">支付方式</span>
                <span class="icv">{{ orderDetail.payTypeDesc }}</span>
              </div>
              <div class="info-cell" v-if="orderDetail.remark">
                <span class="icl">备注</span>
                <span class="icv">{{ orderDetail.remark }}</span>
              </div>
              <div class="info-cell" v-if="orderDetail.expireTime && orderDetail.status === 1">
                <span class="icl">支付截止</span>
                <span class="icv expire">{{ formatTime(orderDetail.expireTime) }}</span>
              </div>
              <div class="info-cell" v-if="orderDetail.cancelReason">
                <span class="icl">取消原因</span>
                <span class="icv muted">{{ orderDetail.cancelReason }}</span>
              </div>
            </div>
          </div>

          <!-- 收货信息 -->
          <div class="detail-section">
            <div class="detail-sec-title">收货信息</div>
            <div class="detail-info-grid">
              <div class="info-cell">
                <span class="icl">收货人</span>
                <span class="icv">{{ orderDetail.receiverName }}</span>
              </div>
              <div class="info-cell">
                <span class="icl">联系电话</span>
                <span class="icv">{{ orderDetail.receiverPhone }}</span>
              </div>
              <div class="info-cell full">
                <span class="icl">收货地址</span>
                <span class="icv">{{ orderDetail.receiverAddress }}</span>
              </div>
            </div>
          </div>

          <!-- 退款信息 -->
          <template v-if="orderDetail.status >= 6">
            <div class="detail-section">
              <div class="detail-sec-title">退款信息</div>
              <div class="detail-info-grid">
                <div class="info-cell" v-if="orderDetail.refundReason">
                  <span class="icl">退款原因</span>
                  <span class="icv">{{ orderDetail.refundReason }}</span>
                </div>
                <div class="info-cell" v-if="orderDetail.refundAmount">
                  <span class="icl">退款金额</span>
                  <span class="icv refund">¥{{ orderDetail.refundAmount.toFixed(2) }}</span>
                </div>
                <div class="info-cell" v-if="orderDetail.rejectReason">
                  <span class="icl">拒绝原因</span>
                  <span class="icv rejected">{{ orderDetail.rejectReason }}</span>
                </div>
                <div class="info-cell" v-if="orderDetail.rejectedAt">
                  <span class="icl">拒绝时间</span>
                  <span class="icv">{{ formatTime(orderDetail.rejectedAt) }}</span>
                </div>
              </div>
            </div>
          </template>

          <!-- 发货记录 -->
          <div class="detail-section" v-if="orderDetail.deliveries && orderDetail.deliveries.length">
            <div class="detail-sec-title">发货记录</div>
            <div class="detail-deliveries">
              <div class="del-timeline-row" v-for="(del, idx) in orderDetail.deliveries" :key="idx">
                <div class="del-timeline-dot"></div>
                <div class="del-timeline-line" v-if="idx < orderDetail.deliveries.length - 1"></div>
                <div class="del-content">
                  <span class="del-company">{{ del.deliveryCompany }}</span>
                  <span class="del-no">{{ del.deliveryNo }}</span>
                  <span class="del-time">{{ formatTime(del.deliveryTime) }}</span>
                </div>
              </div>
            </div>
          </div>
        </template>

        <el-empty v-else-if="!detailLoading" description="暂无订单详情" />
      </div>
    </el-drawer>

    <!-- 修改手机号弹窗 -->
    <el-dialog
      v-model="showChangePhoneModal"
      width="420px"
      :show-close="false"
      class="security-dialog phone-dialog"
      destroy-on-close
    >
      <div class="security-dialog-header">
        <div class="security-dialog-icon phone">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07 19.5 19.5 0 01-6-6 19.79 19.79 0 01-3.07-8.67A2 2 0 014.11 2h3a2 2 0 012 1.72 12.84 12.84 0 00.7 2.81 2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45 12.84 12.84 0 002.81.7A2 2 0 0122 16.92z"/></svg>
        </div>
        <div class="security-dialog-title">
          <h4>{{ userInfo?.phone ? '更换手机号' : '绑定手机号' }}</h4>
          <p>{{ userInfo?.phone ? `当前手机号：${maskPhone(userInfo.phone)}` : '绑定手机号可用于登录和找回密码' }}</p>
        </div>
      </div>
      <el-form :model="phoneForm" label-position="top" class="security-form">
        <el-form-item label="原密码">
          <el-input
            v-model="phoneForm.password"
            type="password"
            placeholder="请输入原密码"
            size="large"
          />
        </el-form-item>
        <el-form-item label="新手机号">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="security-dialog-footer">
          <button class="security-cancel-btn" @click="showChangePhoneModal = false">取消</button>
          <button class="security-submit-btn" @click="changePhone">确认{{ userInfo?.phone ? '更换' : '绑定' }}</button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改邮箱弹窗 -->
    <el-dialog
      v-model="showChangeEmailModal"
      width="420px"
      :show-close="false"
      class="security-dialog email-dialog"
      destroy-on-close
    >
      <div class="security-dialog-header">
        <div class="security-dialog-icon email">
          <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>
        </div>
        <div class="security-dialog-title">
          <h4>{{ userInfo?.email ? '更换邮箱' : '绑定邮箱' }}</h4>
          <p>{{ userInfo?.email ? `当前邮箱：${maskEmail(userInfo.email)}` : '绑定邮箱可用于接收订单通知和找回密码' }}</p>
        </div>
      </div>
      <el-form :model="emailForm" label-position="top" class="security-form">
        <el-form-item label="原密码">
          <el-input
            v-model="emailForm.password"
            type="password"
            placeholder="请输入原密码"
            size="large"
          />
        </el-form-item>
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.newEmail" placeholder="请输入新邮箱" size="large" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="security-dialog-footer">
          <button class="security-cancel-btn" @click="showChangeEmailModal = false">取消</button>
          <button class="security-submit-btn" @click="changeEmail">确认{{ userInfo?.email ? '更换' : '绑定' }}</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import NavBar from '@/components/NavBar.vue'
import {
  User,
  Document,
  ShoppingCart,
  Location,
  Lock,
  SwitchButton,
  Setting,
  Shop,
  Box,
  List,
  Folder,
  Camera,
  Edit,
  DataAnalysis,
  Timer,
  Coin,
  Van,
  CircleCheck,
  CircleClose,
  Delete,
  Clock,
  CreditCard,
  Tickets,
  SuccessFilled,
  WarningFilled,
  InfoFilled,
  MoreFilled,
  ArrowRight
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getAddressList, addAddress, updateAddress, deleteAddress as deleteAddressApi, setDefaultAddress, type Address } from '@/api/address'
import { updateUserProfile, getUserProfile, updatePassword, uploadAvatar, type UpdateProfileRequest, type UpdatePasswordRequest, type UserProfile } from '@/api/user'
import { getOrderListWithItems, payOrder, cancelOrder, confirmReceive, deleteOrder, applyRefund, cancelRefund, getOrderDetail, getOrderStatusCount, type Order, type OrderDetail, type OrderStatusCount } from '@/api/order'
import { getSpuImageUrl, getAvatarUrl } from '@/utils/resource'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeSideMenu = ref('overview')
const originalUserInfo = ref({})

// 时间问候
const timeGreeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 今日日期
const todayDate = computed(() =>
  new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long'
  })
)

// 快捷入口数据
const quickItems = [
  {
    key: 'orders',
    label: '全部订单',
    desc: '查看和管理你的订单',
    icon: '<path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/>'
  },
  {
    key: 'info',
    label: '个人资料',
    desc: '修改头像和昵称',
    icon: '<path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/>'
  },
  {
    key: 'address',
    label: '收货地址',
    desc: '管理配送地址',
    icon: '<path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/>'
  },
  {
    key: 'cart',
    label: '购物车',
    desc: '查看已加入的商品',
    icon: '<circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>'
  }
]

interface MenuItem {
  key: string
  label: string
  icon: string
  danger?: boolean
  badge?: number
}

// 侧边栏菜单项
const menuItems = computed<MenuItem[]>(() => [
  { key: 'overview', label: '我的看板', icon: '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>' },
  { key: 'orders', label: '我的订单', icon: '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>' },
  { key: 'cart', label: '我的购物车', icon: '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/></svg>' },
  { key: 'security', label: '安全设置', icon: '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></svg>' },
  { key: 'info', label: '个人资料', icon: '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>' },
  { key: 'address', label: '收货地址', icon: '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>' }
])

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 用户头像URL
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MCIgaGVpZ2h0PSI4MCIgdmlld0JveD0iMCAwIDgwIDgwIj48cmVjdCB3aWR0aD0iODAiIGhlaWdodD0iODAiIGZpbGw9IiNmMGM3YjMiLz48Y2lyY2xlIGN4PSI0MCIgY3k9IjMyIiByPSIxNiIgZmlsbD0iI2ZmZiIvPjxlbGxpcHNlIGN4PSI0MCIgY3k9IjcyIiByeD0iMjgiIHJ5PSIyMCIgZmlsbD0iI2ZmZiIvPjwvc3ZnPg=='
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) return avatar
  if (avatar.startsWith('/api/')) return avatar
  return getAvatarUrl(avatar)
})

// 个人资料（从 /user/profile 获取）
const profileData = ref<UserProfile | null>(null)
const profileLoading = ref(false)
const showEditDialog = ref(false)
const savingProfile = ref(false)

const editForm = reactive({
  username: ''
})

// 用户角色文本
const roleText = computed(() => {
  if (!userStore.userInfo?.roles || userStore.userInfo.roles.length === 0) return '普通用户'
  const role = userStore.userInfo.roles[0]
  if (!role) return '普通用户'
  if (role.code === 'SUPER_ADMIN') return '超级管理员'
  if (role.code === 'ADMIN' || role.code === 'ROLE_ADMIN') return '管理员'
  if (role.code === 'SELLER' || role.code === 'ROLE_SELLER') return '商家'
  if (role.code === 'OPERATOR' || role.code === 'ROLE_OPERATOR') return '运营'
  if (role.code === 'CUSTOMER_SERVICE' || role.code === 'ROLE_CUSTOMER_SERVICE') return '客服'
  return role.name || '普通用户'
})

// 加载个人资料
const loadProfile = async () => {
  profileLoading.value = true
  try {
    const data = await getUserProfile()
    profileData.value = data
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    profileLoading.value = false
  }
}

// 保存个人资料
const handleSaveProfile = async () => {
  if (!editForm.username.trim()) {
    ElMessage.warning('用户名不能为空')
    return
  }
  if (editForm.username === profileData.value?.username) {
    ElMessage.info('用户名未修改')
    return
  }

  savingProfile.value = true
  try {
    const updateData: UpdateProfileRequest = {
      username: editForm.username.trim()
    }
    await updateUserProfile(updateData)
    ElMessage.success('保存成功')
    showEditDialog.value = false
    await loadProfile()
    await userStore.fetchUserInfo(true)
  } catch {
    // 错误已在请求拦截器中处理，此处不再重复提示
  } finally {
    savingProfile.value = false
  }
}

// 打开编辑弹窗时填充表单
watch(showEditDialog, (val) => {
  if (val && profileData.value) {
    editForm.username = profileData.value.username || ''
  }
})

// 订单状态统计
const statusCount = reactive<OrderStatusCount>({
  pendingPayment: 0,
  pendingDelivery: 0,
  pendingReceipt: 0,
  refunding: 0
})
const loadingStatusCount = ref(false)

const loadStatusCount = async () => {
  loadingStatusCount.value = true
  try {
    const data = await getOrderStatusCount()
    Object.assign(statusCount, data)
  } catch {
    // 静默失败，保持 0
  } finally {
    loadingStatusCount.value = false
  }
}

// 看板订单状态卡片数据
const orderStats = computed(() => [
  {
    tab: 'pending_pay',
    label: '待付款',
    value: statusCount.pendingPayment,
    hint: '等待支付',
    icon: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>'
  },
  {
    tab: 'pending_ship',
    label: '待发货',
    value: statusCount.pendingDelivery,
    hint: '等待发货',
    icon: '<rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>'
  },
  {
    tab: 'pending_receive',
    label: '待收货',
    value: statusCount.pendingReceipt,
    hint: '等待收货',
    icon: '<path d="M21 16V8a2 2 0 00-1-1.73l-7-4a2 2 0 00-2 0l-7 4A2 2 0 002 8v8a2 2 0 001 1.73l7 4a2 2 0 002 0l7-4A2 2 0 0021 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>'
  },
  {
    tab: 'refunding',
    label: '退款中',
    value: statusCount.refunding,
    hint: '处理中',
    icon: '<line x1="12" y1="1" x2="12" y2="23"/><path d="M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6"/>'
  }
])

// 点击统计项跳转到我的订单对应标签
const goToOrdersTab = (tab: string) => {
  activeSideMenu.value = 'orders'
  activeOrderTab.value = tab
}

// 地址相关
const addresses = ref<Address[]>([])
const showAddAddressModal = ref(false)
const editingAddress = ref<Address | null>(null)

// 头像上传
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadingAvatar = ref(false)

const handleEditProfile = () => {
  activeSideMenu.value = 'info'
}

const handleAvatarClick = () => {
  fileInputRef.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    ElMessage.warning('仅支持 jpg、png、gif、webp 格式的图片')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }

  uploadingAvatar.value = true
  try {
    const res = await uploadAvatar(file)
    await userStore.fetchUserInfo(true)
    ElMessage.success('头像更新成功')
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    uploadingAvatar.value = false
    input.value = ''
  }
}

// 地址表单
const addressForm = reactive({
  id: null as number | null,
  receiverName: '',
  receiverPhone: '',
  region: [] as string[],
  detailAddress: '',
  isDefault: 0
})

// 省市区数据（模拟）
const regionOptions = ref([
  {
    value: '广东省',
    label: '广东省',
    children: [
      {
        value: '深圳市',
        label: '深圳市',
        children: [
          { value: '南山区', label: '南山区' },
          { value: '福田区', label: '福田区' },
          { value: '宝安区', label: '宝安区' }
        ]
      },
      {
        value: '广州市',
        label: '广州市',
        children: [
          { value: '天河区', label: '天河区' },
          { value: '越秀区', label: '越秀区' }
        ]
      }
    ]
  },
  {
    value: '北京市',
    label: '北京市',
    children: [
      {
        value: '北京市',
        label: '北京市',
        children: [
          { value: '朝阳区', label: '朝阳区' },
          { value: '海淀区', label: '海淀区' },
          { value: '西城区', label: '西城区' }
        ]
      }
    ]
  }
])

// 密码表单
const showChangePasswordModal = ref(false)
const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 修改手机号相关
const showChangePhoneModal = ref(false)
const phoneForm = reactive({
  password: '',
  newPhone: ''
})

// 修改邮箱相关
const showChangeEmailModal = ref(false)
const emailForm = reactive({
  password: '',
  newEmail: ''
})

// 订单相关
const orders = ref<Order[]>([])
const activeOrderTab = ref('all')
const cancelDialogVisible = ref(false)
const cancelReason = ref('')
const currentOrder = ref<Order | null>(null)

// 倒计时响应式更新
const countdownNow = ref(Date.now())
let countdownTimer: ReturnType<typeof setInterval> | null = null
const startCountdown = () => {
  stopCountdown()
  countdownNow.value = Date.now()
  countdownTimer = setInterval(() => { countdownNow.value = Date.now() }, 1000)
}
const stopCountdown = () => {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
}

// 分页状态
const pagination = reactive({
  page: 1,
  pageSize: 12,
  total: 0
})

// 删除弹窗
const deleteDialogVisible = ref(false)
const deleteTarget = ref<Order | null>(null)

// 退款弹窗
const refundDialogVisible = ref(false)
const refundReason = ref('')
const refundTarget = ref<Order | null>(null)

// 详情弹窗
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const orderDetail = ref<OrderDetail | null>(null)

const submitting = ref(false)

// 订单状态映射
const statusMap: Record<number, string> = {
  1: '待付款',
  2: '待发货',
  3: '待收货',
  4: '已完成',
  5: '已取消',
  6: '退款中',
  7: '已退款',
  8: '已拒绝'
}

const tabStatusMap: Record<string, number | undefined> = {
  all: undefined,
  pending_pay: 1,
  pending_ship: 2,
  pending_receive: 3,
  completed: 4,
  cancelled: 5,
  refunding: 6,
  refunded: 7,
  rejected: 8
}

// 获取用户信息（优化：优先使用缓存）
const fetchUserInfo = async () => {
  try {
    // 如果用户信息已经存在且不需要刷新，直接使用缓存
    if (userStore.userInfo && !userStore.needRefresh()) {
      originalUserInfo.value = { ...userStore.userInfo }
      return
    }
    
    loading.value = true
    await userStore.fetchUserInfo()
    originalUserInfo.value = { ...userStore.userInfo }
  } catch {
    // 如果有缓存数据，继续使用
    if (!userStore.userInfo) {
      ElMessage.error('获取用户信息失败')
    }
  } finally {
    loading.value = false
  }
}

// 获取地址列表
const loadAddresses = async () => {
  try {
    loading.value = true
    const data = await getAddressList()
    addresses.value = data || []
  } catch {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

// 获取订单列表
const loadOrders = async (resetPage = false) => {
  if (resetPage) pagination.page = 1
  loading.value = true
  try {
    const targetStatus = tabStatusMap[activeOrderTab.value]
    const params: { page: number; pageSize: number; status?: number } = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (targetStatus !== undefined) params.status = targetStatus
    const data = await getOrderListWithItems(params)
    const list = data?.list || []
    orders.value = list
    pagination.total = data?.total || orders.value.length || 0
  } catch {
    ElMessage.error('获取订单列表失败')
    orders.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 获取订单状态文本
const getStatusText = (status: number) => {
  return statusMap[status] || String(status)
}

// 获取订单状态对应的 el-tag type
const statusTag = (s: number) => {
  const m: Record<number, string> = { 1: 'warning', 2: 'info', 3: 'primary', 4: 'success', 5: 'danger', 6: 'warning', 7: 'success', 8: 'danger' }
  return m[s] || 'info'
}
// 获取退款状态对应的 el-tag type（复用 statusTag 逻辑）
const refundStatusTag = statusTag

// 侧边菜单选择处理
const handleSideMenuSelect = (index: string) => {
  if (index === 'logout') {
    handleLogout()
  } else if (index === 'cart') {
    router.push('/cart')
  } else {
    activeSideMenu.value = index
    if (index === 'overview') {
      loadStatusCount()
    } else if (index === 'address') {
      loadAddresses()
    } else if (index === 'orders') {
      loadOrders()
    } else if (index === 'info') {
      loadProfile()
    }
  }
}

// 更新用户信息
const updateUserInfo = async () => {
  if (!userInfo.value) return
  
  try {
    loading.value = true
    const updateData: UpdateProfileRequest = {}
    
    if (userInfo.value.email !== (originalUserInfo.value as any)?.email) {
      updateData.email = userInfo.value.email
    }
    if (userInfo.value.phone !== (originalUserInfo.value as any)?.phone) {
      updateData.phone = userInfo.value.phone
    }
    
    if (Object.keys(updateData).length > 0) {
      const response = await updateUserProfile(updateData)
      await userStore.fetchUserInfo()
      ElMessage.success('个人信息更新成功')
      originalUserInfo.value = { ...userStore.userInfo }
    } else {
      ElMessage.info('没有修改任何信息')
    }
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  userStore.setUserInfo({ ...originalUserInfo.value })
  ElMessage.info('已重置')
}

// 退出登录
const handleLogout = async () => {
  try {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
  }
}

// 地址默认复选框 (独立 boolean ref, 避免 reactive + el-checkbox true-value 响应问题)
const addressIsDefault = ref(false)
const showAddressFormError = ref(false)

// 打开添加地址弹窗
const showAddAddressModalHandler = () => {
  editingAddress.value = null
  addressForm.id = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.region = []
  addressForm.detailAddress = ''
  addressForm.isDefault = 0
  addressIsDefault.value = false
  showAddressFormError.value = false
  showAddAddressModal.value = true
}

// 编辑地址
const editAddress = (addr: Address) => {
  editingAddress.value = addr
  addressForm.id = addr.id
  addressForm.receiverName = addr.receiverName
  addressForm.receiverPhone = addr.receiverPhone
  addressForm.region = [addr.province, addr.city, addr.district]
  addressForm.detailAddress = addr.detailAddress
  addressForm.isDefault = addr.isDefault
  addressIsDefault.value = addr.isDefault === 1
  showAddressFormError.value = false
  showAddAddressModal.value = true
}

// 关闭地址弹窗
const closeAddressModal = () => {
  showAddAddressModal.value = false
  editingAddress.value = null
  showAddressFormError.value = false
}

// 手机号格式校验（大陆手机号：1开头的11位数字）
const isValidPhone = (phone: string): boolean => {
  return /^1\d{10}$/.test(phone)
}

// 校验地址表单
const validateAddressForm = (): boolean => {
  showAddressFormError.value = true
  return !!(
    addressForm.receiverName &&
    addressForm.receiverPhone && isValidPhone(addressForm.receiverPhone) &&
    addressForm.region.length &&
    addressForm.detailAddress
  )
}

// 确认保存地址（编辑 / 添加）
const confirmSaveAddress = () => {
  if (!validateAddressForm()) {
    ElMessage.warning('请填写完整信息')
    return
  }

  const title = editingAddress.value ? '确认修改地址' : '确认添加地址'
  const message = editingAddress.value
    ? '确定要修改当前地址信息吗？'
    : '确定添加新的收货地址吗？'
  const confirmText = editingAddress.value ? '确认修改' : '确认添加'

  ElMessageBox.confirm(message, title, {
    confirmButtonText: confirmText,
    cancelButtonText: '取消',
    type: editingAddress.value ? 'warning' : 'info',
    customClass: 'lux-message-box',
    confirmButtonClass: editingAddress.value ? 'lux-msg-confirm' : 'lux-msg-confirm',
    cancelButtonClass: 'lux-msg-cancel'
  })
    .then(() => {
      saveAddress()
    })
    .catch(() => {
      /* 用户取消 */
    })
}

// 保存地址
const saveAddress = async () => {
  try {
    loading.value = true
    const data = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.region[0] || '',
      city: addressForm.region[1] || '',
      district: addressForm.region[2] || '',
      detailAddress: addressForm.detailAddress,
      isDefault: addressIsDefault.value ? 1 : 0
    }

    if (editingAddress.value) {
      await updateAddress({
        ...data,
        id: editingAddress.value.id
      })
      ElMessage.success('地址更新成功')
    } else {
      await addAddress(data)
      ElMessage.success('地址添加成功')
    }

    closeAddressModal()
    loadAddresses()
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    loading.value = false
  }
}

// 设置默认地址
const setDefault = async (id: number) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    loadAddresses()
  } catch {
    /* 错误已由拦截器处理 */
  }
}

// 确认设置默认地址
const confirmSetDefault = (addr: Address) => {
  ElMessageBox.confirm(
    `确定将「${addr.province}${addr.city}${addr.district}${addr.detailAddress}」设为默认收货地址吗？`,
    '设为默认地址',
    {
      confirmButtonText: '确定设置',
      cancelButtonText: '取消',
      type: 'warning',
      customClass: 'lux-message-box',
      confirmButtonClass: 'lux-msg-confirm',
      cancelButtonClass: 'lux-msg-cancel'
    }
  )
    .then(() => {
      setDefault(addr.id)
    })
    .catch(() => {
      /* 用户取消 */
    })
}

// 删除地址
const deleteAddressHandler = async (id: number) => {
  try {
    await deleteAddressApi(id)
    ElMessage.success('地址已删除')
    loadAddresses()
  } catch {
    /* 错误已由拦截器处理 */
  }
}

// 确认删除地址
const confirmDeleteAddress = (addr: Address) => {
  ElMessageBox.confirm(
    `确定删除「${addr.receiverName}」的收货地址吗？删除后将无法恢复。`,
    '删除地址',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error',
      customClass: 'lux-message-box',
      confirmButtonClass: 'lux-msg-confirm-danger',
      cancelButtonClass: 'lux-msg-cancel'
    }
  )
    .then(() => {
      deleteAddressHandler(addr.id)
    })
    .catch(() => {
      /* 用户取消 */
    })
}

// 修改密码
const changePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度至少为6位')
    return
  }

  try {
    loading.value = true
    const requestData: UpdatePasswordRequest = {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    }
    await updatePassword(requestData)
    ElMessage.success('密码修改成功')
    showChangePasswordModal.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch {
    // 出现错误时清空已填写内容
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } finally {
    loading.value = false
  }
}

// 修改手机号
const changePhone = async () => {
  if (!phoneForm.password || !phoneForm.newPhone) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    loading.value = true
    await updateUserProfile({
      phone: phoneForm.newPhone,
      password: phoneForm.password
    })
    ElMessage.success('手机号修改成功')
    showChangePhoneModal.value = false
    phoneForm.password = ''
    phoneForm.newPhone = ''
    await userStore.fetchUserInfo()
  } catch {
    // 出现错误时清空已填写内容
    phoneForm.password = ''
    phoneForm.newPhone = ''
  } finally {
    loading.value = false
  }
}

// 修改邮箱
const changeEmail = async () => {
  if (!emailForm.password || !emailForm.newEmail) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    loading.value = true
    await updateUserProfile({
      email: emailForm.newEmail,
      password: emailForm.password
    })
    ElMessage.success('邮箱修改成功')
    showChangeEmailModal.value = false
    emailForm.password = ''
    emailForm.newEmail = ''
    await userStore.fetchUserInfo()
  } catch {
    // 出现错误时清空已填写内容
    emailForm.password = ''
    emailForm.newEmail = ''
  } finally {
    loading.value = false
  }
}

// 订单支付（增加确认弹窗）
const handlePay = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认支付订单 ${order.orderNo} 吗？`, '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'info'
    })
    submitting.value = true
    await payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '支付失败')
  } finally {
    submitting.value = false
  }
}

// 显示取消订单弹窗
const showCancelDialog = (order: Order) => {
  currentOrder.value = order
  cancelReason.value = ''
  cancelDialogVisible.value = true
}

// 确认取消订单
const confirmCancel = async () => {
  if (!cancelReason.value) {
    ElMessage.warning('请选择取消原因')
    return
  }
  if (!currentOrder.value) return
  try {
    submitting.value = true
    await cancelOrder(currentOrder.value.id)
    ElMessage.success('订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

// 确认收货（增加确认弹窗）
const handleReceive = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确认已收到订单 ${order.orderNo} 的商品吗？`, '确认收货', {
      confirmButtonText: '确认收货',
      cancelButtonText: '再想想',
      type: 'success'
    })
    submitting.value = true
    await confirmReceive(order.id)
    ElMessage.success('收货成功')
    loadOrders()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '收货失败')
  } finally {
    submitting.value = false
  }
}

// 删除订单
const handleDelete = (order: Order) => {
  deleteTarget.value = order
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!deleteTarget.value) return
  try {
    submitting.value = true
    await deleteOrder(deleteTarget.value.id)
    ElMessage.success('订单已删除')
    deleteDialogVisible.value = false
    loadOrders()
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

// 申请退款
const handleRefund = (order: Order) => {
  refundTarget.value = order
  refundReason.value = ''
  refundDialogVisible.value = true
}

const confirmRefund = async () => {
  if (!refundReason.value) {
    ElMessage.warning('请选择退款原因')
    return
  }
  if (!refundTarget.value) return
  try {
    submitting.value = true
    await applyRefund(refundTarget.value.id, refundReason.value)
    ElMessage.success('退款申请已提交，请等待审核')
    refundDialogVisible.value = false
    loadOrders()
  } catch {
    /* 错误已由拦截器处理 */
  } finally {
    submitting.value = false
  }
}

// 取消退款
const handleCancelRefund = async (order: Order) => {
  try {
    await ElMessageBox.confirm(`确定要取消订单 ${order.orderNo} 的退款申请吗？`, '取消退款', {
      confirmButtonText: '确定',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    submitting.value = true
    await cancelRefund(order.id)
    ElMessage.success('退款申请已取消，订单已恢复')
    loadOrders()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '取消退款失败')
  } finally {
    submitting.value = false
  }
}

// 查看订单详情
const viewDetail = async (order: Order) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  orderDetail.value = null
  try {
    const detail = await getOrderDetail(order.orderNo)
    orderDetail.value = detail
  } catch {
    ElMessage.error('获取订单详情失败')
    orderDetail.value = null
  } finally {
    detailLoading.value = false
  }
}

// 格式化时间
const formatTime = (t?: string) => {
  if (!t) return '-'
  try {
    const d = new Date(t)
    const pad = (n: number) => n.toString().padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return t
  }
}

// 拼接商品图片完整 URL
const orderItemImage = (img?: string) => {
  if (!img) return ''
  return img.startsWith('http') ? img : getSpuImageUrl(img)
}

// 格式化时间显示
const formatDate = (t?: string) => {
  if (!t) return ''
  const d = new Date(t)
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  return `${m}-${day} ${h}:${min}`
}

// 格式化过期时间倒计时
const formatExpireTime = (t?: string, now?: number) => {
  if (!t) return ''
  const expiry = new Date(t).getTime()
  const diff = expiry - (now || Date.now())
  if (diff <= 0) return '已过期'
  const m = Math.floor(diff / 60000)
  const s = Math.floor((diff % 60000) / 1000)
  return m > 0 ? `${m}分${s}秒` : `${s}秒`
}

// 手机号脱敏
const maskPhone = (phone: string) => {
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 邮箱脱敏
const maskEmail = (email: string) => {
  const [name = '', domain] = email.split('@')
  if (!domain) return email
  const masked = name.length > 2 ? name.slice(0, 2) + '***' : '***'
  return `${masked}@${domain}`
}

// 状态图标 SVG path
const getStatusIcon = (status: number) => {
  const icons: Record<number, string> = {
    1: '<circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>', // 待付款 - 时钟
    2: '<path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/>', // 待发货 - 包裹
    3: '<rect x="1" y="3" width="15" height="13"/><polygon points="16 8 20 8 23 11 23 16 16 16 16 8"/><circle cx="5.5" cy="18.5" r="2.5"/><circle cx="18.5" cy="18.5" r="2.5"/>', // 待收货 - 卡车
    4: '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>', // 已完成 - 对勾
    5: '<circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>', // 已取消 - 叉
    6: '<polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>', // 退款中 - 刷新
    7: '<path d="M17 1l4 4-4 4"/><path d="M3 11V9a4 4 0 0 1 4-4h14"/><path d="M7 23l-4-4 4-4"/><path d="M21 13v2a4 4 0 0 1-4 4H3"/>', // 已退款 - 退回
    8: '<circle cx="12" cy="12" r="10"/><line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>', // 已拒绝 - 禁止
  }
  return icons[status] || '<circle cx="12" cy="12" r="10"/>'
}

// 复制订单号
const copyOrderNo = async (no: string, e: Event) => {
  e.stopPropagation()
  try {
    await navigator.clipboard.writeText(no)
    ElMessage.success('订单号已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}

// 跳转到首页
const goHome = () => {
  router.push('/home')
}

// 监听订单标签切换
watch(activeOrderTab, () => {
  pagination.page = 1
  loadOrders()
})

// 组件挂载时获取数据（优化：仅在需要时刷新）
onMounted(() => {
  // 默认展示订单概览，加载状态统计数据
  loadStatusCount()
  startCountdown()
  // 预加载个人资料数据
  loadProfile()
  // 如果已有用户信息，直接使用；否则异步获取
  if (!userStore.userInfo) {
    fetchUserInfo()
  } else {
    // 使用缓存数据
    originalUserInfo.value = { ...userStore.userInfo }
    // 如果需要刷新，后台异步刷新（不阻塞UI）
    if (userStore.needRefresh()) {
      fetchUserInfo()
    }
  }
})

onUnmounted(() => {
  stopCountdown()
})
</script>

<style scoped>
/* 使用系统字体，不再加载外部 Google Fonts */

/* ===========================
   个人中心 — Studio Design System
   使用项目 Design Token
   =========================== */

/* ===== 容器布局 ===== */
.profile-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

.profile-content {
  flex: 1;
  display: flex;
  margin-top: var(--header-height, 60px);
  min-height: calc(100vh - var(--header-height, 60px));
}

/* ===== 侧边栏 — 浅色设计 ===== */
.sidebar {
  width: 240px;
  flex-shrink: 0;
  background: var(--surface);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  position: sticky;
  top: var(--header-height, 60px);
  height: calc(100vh - var(--header-height, 60px));
}

.sidebar-user {
  padding: var(--space-6) var(--space-5) var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-4);
  border-bottom: 1px solid var(--border-light);
}

.sidebar-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  overflow: hidden;
  flex-shrink: 0;
  border: 2px solid var(--color-brand-100);
}

.sidebar-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.sidebar-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.sidebar-user-name {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-3) var(--space-2);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 10px var(--space-3);
  border: none;
  background: transparent;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--ink-muted);
  transition: all var(--transition-fast);
  width: 100%;
  text-align: left;
  font-family: inherit;
}

.nav-item:hover {
  background: var(--surface-soft);
  color: var(--ink);
}

.nav-item.active {
  background: var(--color-brand-50);
  color: var(--color-brand-500);
  font-weight: 600;
}

.nav-item.danger {
  color: var(--color-danger);
}

.nav-item.danger:hover {
  background: #fef2f2;
  color: var(--color-danger);
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.nav-label {
  flex: 1;
}

.nav-badge {
  background: var(--color-brand-500);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: var(--radius-full);
  line-height: 1.6;
}

.sidebar-footer {
  padding: var(--space-2);
  border-top: 1px solid var(--border-light);
}

/* ===== 主内容区 ===== */
.main-content {
  flex: 1;
  padding: 0;
  background: transparent;
}

.content-box {
  background: transparent;
  min-height: calc(100vh - var(--header-height, 60px));
  padding: var(--space-8) var(--space-10);
}

/* ===== 入场动画 ===== */
.animate-in {
  animation: fadeInUp 0.3s ease-out both;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.35; }
  50% { opacity: 0.85; }
}

/* ===== 概览看板 ===== */
.overview-section {
  width: 100%;
}

/* Bento 网格布局 */
.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: var(--space-5);
  align-items: stretch;
}

.bento-cell {
  display: flex;
  flex-direction: column;
}

.welcome-cell { grid-column: span 8; }
.account-cell { grid-column: span 4; }
.stat-cell { grid-column: span 3; }
.quick-cell { grid-column: span 12; }

/* 通用卡片基底 */
.overview-section .welcome-card,
.overview-section .account-card,
.overview-section .stat-card-v2,
.overview-section .quick-panel {
  background: var(--surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
  height: 100%;
  transition: all var(--transition-base);
}

/* 欢迎卡片 — 克制的高级感 */
.welcome-card {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-6);
  overflow: hidden;
  background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%) !important;
}

.welcome-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 90% 10%, rgba(255, 68, 0, 0.04) 0%, transparent 40%),
    radial-gradient(circle at 10% 90%, rgba(0, 0, 0, 0.02) 0%, transparent 35%);
  pointer-events: none;
}

.welcome-text {
  display: flex;
  flex-direction: column;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.welcome-eyebrow {
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--ink-faint);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.welcome-title {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--ink);
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.welcome-sub {
  margin: 0;
  font-size: var(--text-base);
  color: var(--ink-muted);
  font-weight: 400;
}

.welcome-avatar {
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.welcome-avatar img {
  width: 84px;
  height: 84px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--border-light);
  display: block;
  box-shadow: var(--shadow-md);
}

/* 账户概览卡片 */
.account-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.account-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
}

.account-label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--ink-muted);
}

.account-badge {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--color-success);
  background: color-mix(in oklab, var(--color-success) 10%, white);
  padding: 4px 10px;
  border-radius: var(--radius-full);
}

.account-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-4);
  margin-top: auto;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-value {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--ink-muted);
}

.meta-value.active {
  color: var(--color-success);
}

.meta-label {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

/* 订单状态卡片 — 极简 */
.stat-card-v2 {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  cursor: pointer;
  position: relative;
}

.stat-card-v2::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: var(--border-light);
  transition: background var(--transition-base);
  border-radius: var(--radius-xl) var(--radius-xl) 0 0;
}

.stat-card-v2:hover {
  border-color: var(--color-brand-200);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-card-v2:hover::before {
  background: var(--color-brand-500);
}

.stat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--ink-muted);
}

.stat-icon-v2 {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-faint);
  background: var(--surface-soft);
  transition: all var(--transition-base);
}

.stat-card-v2:hover .stat-icon-v2 {
  color: var(--color-brand-500);
  background: var(--color-brand-50);
}

.stat-body {
  display: flex;
  align-items: baseline;
}

.stat-value {
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--ink);
  line-height: 1;
  font-variant-numeric: tabular-nums;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: var(--space-3);
  border-top: 1px solid var(--border-light);
}

.stat-hint {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.stat-arrow {
  font-size: var(--text-sm);
  color: var(--ink-faint);
  transition: all var(--transition-fast);
}

.stat-card-v2:hover .stat-arrow {
  color: var(--color-brand-500);
  transform: translateX(3px);
}

/* 加载骨架屏 */
.shimmer {
  display: inline-block;
  width: 40px;
  height: 36px;
  background: linear-gradient(90deg, var(--surface-muted) 25%, var(--surface-soft) 50%, var(--surface-muted) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* 快捷入口面板 */
.quick-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.panel-title {
  margin: 0;
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--ink);
}

.quick-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
}

.quick-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.quick-row:hover {
  background: var(--surface-soft);
  border-color: var(--border-light);
}

.quick-row-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-muted);
  background: var(--surface-muted);
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.quick-row:hover .quick-row-icon {
  color: var(--color-brand-500);
  background: var(--color-brand-50);
}

.quick-row-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.quick-row-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--ink);
}

.quick-row-desc {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.quick-row-arrow {
  font-size: var(--text-sm);
  color: var(--ink-faint);
  transition: transform var(--transition-fast);
}

.quick-row:hover .quick-row-arrow {
  color: var(--color-brand-500);
  transform: translateX(3px);
}

/* ===== 个人资料 ===== */
/* 个人资料 — 高级感浅色系主题变量 */
.profile-section {
  --lux-bg: #fafaf9;
  --lux-surface: #ffffff;
  --lux-surface-soft: #f5f5f4;
  --lux-surface-warm: #faf7f2;
  --lux-border: rgba(28, 25, 23, 0.08);
  --lux-border-strong: rgba(28, 25, 23, 0.16);
  --lux-gold: #ca8a04;
  --lux-gold-soft: #d4a017;
  --lux-gold-muted: #b7791f;
  --lux-gold-50: rgba(202, 138, 4, 0.08);
  --lux-gold-100: rgba(202, 138, 4, 0.14);
  --lux-ink: #1c1917;
  --lux-ink-soft: #44403c;
  --lux-ink-muted: #78716c;
  --lux-ink-faint: #a8a29e;
  --lux-radius: 24px;
  --lux-shadow: 0 20px 60px rgba(28, 25, 23, 0.08);
  --lux-shadow-hover: 0 28px 80px rgba(28, 25, 23, 0.12);
  --lux-font: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;

  padding: 0;
}

.profile-card-v3 {
  background: var(--lux-surface);
  border: 1px solid var(--lux-border);
  border-radius: var(--lux-radius);
  overflow: hidden;
  box-shadow: var(--lux-shadow);
  position: relative;
}

/* 顶部柔和光泽 */
.profile-card-v3::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(105deg, transparent 40%, rgba(202, 138, 4, 0.02) 50%, transparent 60%);
  pointer-events: none;
  z-index: 0;
}

.profile-cover {
  height: 150px;
  background:
    radial-gradient(circle at 15% 85%, rgba(202, 138, 4, 0.12) 0%, transparent 40%),
    radial-gradient(circle at 85% 15%, rgba(212, 160, 23, 0.10) 0%, transparent 35%),
    linear-gradient(135deg, #faf7f2 0%, #f5f0e8 50%, #faf8f3 100%);
  position: relative;
  overflow: hidden;
}

/* 高级感网格纹理 */
.profile-cover-pattern {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(202, 138, 4, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(202, 138, 4, 0.04) 1px, transparent 1px);
  background-size: 32px 32px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.5) 0%, transparent 100%);
}

/* 封面顶部金色细线 */
.profile-cover::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(202, 138, 4, 0.4), transparent);
  opacity: 0.8;
}

.profile-head-v3 {
  display: flex;
  align-items: flex-end;
  padding: 0 var(--space-10) var(--space-8);
  gap: var(--space-6);
  position: relative;
  z-index: 1;
}

.profile-avatar-area-v3 {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  margin-top: -56px;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.avatar-frame-v3 {
  position: relative;
  width: 112px;
  height: 112px;
  border-radius: 20px;
  cursor: pointer;
  flex-shrink: 0;
  overflow: hidden;
  border: 4px solid var(--lux-surface);
  box-shadow: 0 0 0 1.5px var(--lux-gold-soft), 0 12px 32px rgba(28, 25, 23, 0.12);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.avatar-frame-v3 img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.avatar-frame-v3:hover {
  transform: scale(1.03);
  box-shadow: 0 0 0 2px var(--lux-gold), 0 16px 40px rgba(202, 138, 4, 0.18);
}

.avatar-frame-v3:hover .avatar-overlay-v3 {
  opacity: 1;
}

.avatar-overlay-v3 {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  background: rgba(28, 25, 23, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  color: var(--lux-surface);
}

.avatar-change-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 12px;
  border-radius: var(--radius-full);
  border: 1px solid var(--lux-border);
  background: var(--lux-surface);
  color: var(--lux-gold);
  font-size: var(--text-xs);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(28, 25, 23, 0.04);
}

.avatar-change-link:hover {
  background: var(--lux-gold-50);
  border-color: rgba(202, 138, 4, 0.25);
  color: var(--lux-gold-soft);
}

.profile-meta-v3 {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding-bottom: 6px;
  flex: 1;
  min-width: 0;
}

.profile-username-v3 {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--lux-ink);
  line-height: 1.15;
  font-family: var(--lux-font);
  letter-spacing: -0.01em;
}

.profile-meta-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.profile-id {
  font-size: var(--text-xs);
  color: var(--lux-ink-muted);
  font-family: var(--font-mono);
  background: var(--lux-surface-soft);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  border: 1px solid var(--lux-border);
}

.profile-edit-btn-v3 {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 10px var(--space-5);
  border: 1px solid var(--lux-gold);
  background: var(--lux-surface);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--lux-gold);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
  margin-left: auto;
  margin-bottom: 6px;
  position: relative;
  overflow: hidden;
}

.profile-edit-btn-v3::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, var(--lux-gold-100), transparent);
  transform: translateX(-100%);
  transition: transform 0.6s ease;
}

.profile-edit-btn-v3:hover {
  background: var(--lux-gold-50);
  color: var(--lux-gold-soft);
  box-shadow: 0 4px 16px rgba(202, 138, 4, 0.15);
}

.profile-edit-btn-v3:hover::before {
  transform: translateX(100%);
}

.profile-body-v3 {
  padding: 0 var(--space-10) var(--space-10);
  position: relative;
  z-index: 1;
}

.info-section {
  background: var(--lux-surface-soft);
  border: 1px solid var(--lux-border);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  position: relative;
  overflow: hidden;
}

.info-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(202, 138, 4, 0.25), transparent);
  opacity: 0.6;
}

.info-section-title {
  margin: 0 0 var(--space-5) 0;
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--lux-gold);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-family: var(--lux-font);
}

.info-grid-v3 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

.info-cell-v3 {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: var(--space-4);
  background: var(--lux-surface);
  border: 1px solid var(--lux-border);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  position: relative;
}

.info-cell-v3::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, var(--lux-gold), transparent);
  opacity: 0;
  transition: opacity var(--transition-fast);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
}

.info-cell-v3:hover {
  border-color: rgba(202, 138, 4, 0.25);
  background: var(--lux-surface);
  transform: translateY(-2px);
  box-shadow: var(--lux-shadow-hover);
}

.info-cell-v3:hover::before {
  opacity: 0.5;
}

.info-cell-v3.full {
  grid-column: 1 / -1;
}

.cell-label-v3 {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--lux-ink-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-family: var(--lux-font);
}

.cell-value-v3 {
  font-size: var(--text-base);
  font-weight: 500;
  color: var(--lux-ink);
  line-height: 1.4;
  word-break: break-all;
  font-family: var(--lux-font);
}

.cell-value-v3.mono {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  color: var(--lux-ink-muted);
}

/* 编辑资料弹窗 — 高级感浅色系主题 */
.profile-edit-dialog-v3 :deep(.el-dialog) {
  border-radius: var(--lux-radius);
  overflow: hidden;
  background: var(--lux-surface);
  border: 1px solid var(--lux-border);
  box-shadow: var(--lux-shadow-hover);
}

.profile-edit-dialog-v3 :deep(.el-dialog__header) { display: none; }
.profile-edit-dialog-v3 :deep(.el-dialog__body) { padding: var(--space-6); position: relative; z-index: 1; }
.profile-edit-dialog-v3 :deep(.el-dialog__footer) { padding: 0 var(--space-6) var(--space-6); position: relative; z-index: 1; }

.edit-dialog-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.edit-dialog-avatar {
  position: relative;
  width: 76px;
  height: 76px;
  border-radius: 14px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid var(--lux-gold-soft);
  box-shadow: 0 8px 24px rgba(28, 25, 23, 0.1);
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.edit-dialog-avatar:hover {
  box-shadow: 0 8px 28px rgba(202, 138, 4, 0.2);
}

.edit-dialog-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.edit-avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 14px;
  background: rgba(28, 25, 23, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  color: var(--lux-surface);
}

.edit-dialog-avatar:hover .edit-avatar-overlay {
  opacity: 1;
}

.edit-dialog-title h4 {
  margin: 0 0 4px 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--lux-ink);
  font-family: var(--lux-font);
  letter-spacing: -0.01em;
}

.edit-dialog-title p {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--lux-ink-muted);
}

.edit-form-v3 :deep(.el-form-item__label) {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--lux-ink-muted);
  padding-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-family: var(--lux-font);
}

.edit-form-v3 :deep(.el-form-item) { margin-bottom: 0; }

.edit-form-v3 :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  background: var(--lux-surface-soft);
  box-shadow: 0 0 0 1px var(--lux-border) inset;
  padding: 2px 12px;
}

.edit-form-v3 :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--lux-gold) inset, 0 0 0 4px var(--lux-gold-50);
}

.edit-form-v3 :deep(.el-input__inner) {
  height: 42px;
  font-size: var(--text-sm);
  color: var(--lux-ink);
  background: transparent;
}

.edit-form-v3 :deep(.el-input__inner::placeholder) {
  color: var(--lux-ink-faint);
}

.edit-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.edit-cancel-btn {
  padding: 10px 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--lux-border);
  background: var(--lux-surface);
  color: var(--lux-ink-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.edit-cancel-btn:hover {
  border-color: var(--lux-border-strong);
  color: var(--lux-ink);
  background: var(--lux-surface-soft);
}

.edit-save-btn {
  padding: 10px 22px;
  border-radius: var(--radius-md);
  border: 1px solid var(--lux-gold);
  background: linear-gradient(135deg, var(--lux-gold-muted), var(--lux-gold));
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.edit-save-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--lux-gold), var(--lux-gold-soft));
  box-shadow: 0 4px 16px rgba(202, 138, 4, 0.2);
}

.edit-save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 加载闪烁占位 */
.shimmer {
  display: inline-block;
  min-width: 28px;
  animation: pulse 1.2s ease-in-out infinite;
  color: var(--border);
}

/* ===== 订单区域 ===== */
.order-header {
  margin-bottom: var(--space-6);
}

.order-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
}

.order-header-top h3 {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--ink);
}

.order-total-count {
  font-size: var(--text-xs);
  color: var(--ink-faint);
  background: var(--surface-soft);
  padding: 3px var(--space-3);
  border-radius: var(--radius-full);
  line-height: 1.8;
}

.order-tabs {
  margin-bottom: 0;
}

.order-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.order-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.order-tabs :deep(.el-tabs--card > .el-tabs__header .el-tabs__nav) {
  border: none;
}

.order-tabs :deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
  border: none;
  border-radius: var(--radius-md);
  margin: 0 2px;
  padding: 0 var(--space-4);
  height: 34px;
  line-height: 34px;
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--ink-muted);
  transition: all var(--transition-fast);
}

.order-tabs :deep(.el-tabs--card > .el-tabs__header .el-tabs__item.is-active) {
  background: var(--color-brand-50);
  color: var(--color-brand-500);
  font-weight: 600;
}

.order-tabs :deep(.el-tabs--card > .el-tabs__header .el-tabs__item:hover) {
  color: var(--ink);
}

.order-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.order-skeleton {
  padding: var(--space-6);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
  background: var(--surface);
}

/* ===== 订单列表 ===== */
.order-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: var(--space-5);
}

/* ===== 订单卡片 v2 ===== */
.order-card-v2 {
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  position: relative;
}

/* 顶部状态色细线 */
.order-card-v2::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: transparent;
  z-index: 1;
  transition: background var(--transition-base);
}

.order-card-v2.status-topline-1::before { background: var(--color-brand-500); }
.order-card-v2.status-topline-2::before { background: var(--color-accent); }
.order-card-v2.status-topline-3::before { background: var(--color-success); }
.order-card-v2.status-topline-4::before { background: var(--ink-muted); }
.order-card-v2.status-topline-5::before { background: var(--ink-faint); }
.order-card-v2.status-topline-6::before { background: var(--color-warning); }
.order-card-v2.status-topline-7::before { background: var(--color-success); }
.order-card-v2.status-topline-8::before { background: var(--color-danger); }

/* 左下角装饰（合并圆晕与小点） */
.order-card-v2::after {
  content: '';
  position: absolute;
  left: -18px;
  bottom: -18px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: radial-gradient(circle at center, var(--color-brand-100) 0%, var(--color-brand-50) 55%, transparent 70%);
  box-shadow: 28px 28px 0 0 var(--color-brand-300);
  opacity: 0.5;
  pointer-events: none;
  z-index: 0;
  transition: opacity var(--transition-base);
}

.order-card-v2:hover::after {
  opacity: 0.75;
}

.order-card-v2:hover {
  border-color: var(--color-brand-200);
  box-shadow: var(--shadow-md);
}

.order-card-main {
  flex: 1;
  padding: var(--space-5);
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.order-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-3);
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.order-time {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.order-no-wrap {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.order-no {
  font-size: var(--text-xs);
  color: var(--ink-muted);
  font-family: var(--font-mono);
  letter-spacing: 0.02em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-copy-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  color: var(--ink-faint);
  cursor: pointer;
  opacity: 0;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.order-card-v2:hover .order-copy-btn,
.order-copy-btn:focus {
  opacity: 1;
}

.order-copy-btn:hover {
  background: var(--surface-soft);
  color: var(--color-brand-500);
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 600;
  line-height: 1.6;
  flex-shrink: 0;
}

.pill-icon {
  flex-shrink: 0;
}

.status-pill.pill-1 { background: var(--color-brand-50); color: var(--color-brand-500); }
.status-pill.pill-2 { background: var(--color-accent-50); color: var(--color-accent); }
.status-pill.pill-3 { background: #e6f7e6; color: var(--color-success); }
.status-pill.pill-4 { background: var(--surface-muted); color: var(--ink-muted); }
.status-pill.pill-5 { background: var(--surface-muted); color: var(--ink-muted); }
.status-pill.pill-6 { background: var(--color-brand-50); color: var(--color-brand-500); }
.status-pill.pill-7 { background: #e6f7e6; color: var(--color-success); }
.status-pill.pill-8 { background: #fef2f2; color: var(--color-danger); }

.order-products {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--surface-soft);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.product-main {
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.product-main-img {
  width: 72px;
  height: 72px;
  border-radius: var(--radius-md);
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--border-light);
  background: var(--surface);
  transition: transform var(--transition-base);
}

.order-card-v2:hover .product-main-img {
  transform: scale(1.03);
}

.product-main-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  padding-top: 2px;
}

.product-main-name {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--ink);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-main-specs {
  font-size: var(--text-xs);
  color: var(--ink-faint);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-main-meta {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: auto;
}

.product-main-price {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--ink);
}

.product-main-qty {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.order-summary-fallback {
  font-size: var(--text-sm);
  color: var(--ink-muted);
  flex: 1;
  display: flex;
  align-items: center;
}

.product-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  justify-content: space-between;
  gap: var(--space-3);
  flex-shrink: 0;
  min-width: 80px;
}

.extra-thumbs {
  display: flex;
  align-items: center;
}

.extra-thumb {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  border: 2px solid var(--surface);
  margin-left: -10px;
  background: var(--surface);
  box-shadow: var(--shadow-sm);
}

.extra-thumb:first-child {
  margin-left: 0;
}

.extra-more {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-muted);
  color: var(--ink-muted);
  font-size: 10px;
  font-weight: 600;
  margin-left: -10px;
  border: 2px solid var(--surface);
}

.order-amounts-mini {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.mini-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.mini-row.pay {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--ink);
}

.mini-discount {
  color: var(--color-success);
}

.mini-pay {
  color: var(--color-brand-500);
}

.order-card-hints {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.status-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-xs);
  line-height: 1.4;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.hint-icon { font-size: 13px; }
.hint-expire { color: var(--color-warning); background: #fffbeb; }
.hint-refund { color: var(--color-success); background: #ecfdf5; }
.hint-delivery { color: var(--color-accent); background: var(--color-accent-50); }
.hint-cancel { color: var(--ink-muted); background: var(--surface-muted); }
.hint-pending { color: var(--color-accent); background: var(--color-accent-50); }
.hint-remark { color: var(--ink-muted); background: var(--surface-soft); }

.order-card-actions {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  padding: var(--space-3) var(--space-5);
  border-top: 1px solid var(--border-light);
  background: var(--surface-soft);
}

.action-btn-v2 {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 7px 14px;
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  font-weight: 500;
  line-height: 1.4;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all var(--transition-fast);
  white-space: nowrap;
  background: none;
  font-family: inherit;
}

.action-btn-v2:active { transform: scale(0.96); }
.action-btn-v2.primary { background: var(--color-brand-500); color: #fff; border-color: var(--color-brand-500); }
.action-btn-v2.primary:hover { background: var(--color-brand-600); border-color: var(--color-brand-600); }
.action-btn-v2.secondary { background: var(--surface); color: var(--ink-muted); border-color: var(--border); }
.action-btn-v2.secondary:hover { border-color: var(--color-brand-500); color: var(--color-brand-500); }
.action-btn-v2.ghost { background: transparent; color: var(--ink-muted); border-color: var(--border); }
.action-btn-v2.ghost:hover { border-color: var(--ink-muted); color: var(--ink); }
.action-btn-v2.ghost.danger { color: var(--color-danger); }
.action-btn-v2.ghost.danger:hover { border-color: var(--color-danger); color: var(--color-danger); }

.status-tip { font-size: var(--text-xs); color: var(--ink-faint); }
.status-tip.refund-done { color: var(--color-success); font-weight: 500; }

/* ===== 分页 ===== */
.pagination-wrap {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  margin-top: var(--space-2);
  padding: 4px 0;
}

.pagination-wrap :deep(.el-pagination) { font-weight: 400; }
.pagination-wrap :deep(.el-pagination .el-pager li) {
  border-radius: var(--radius-md);
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  font-size: var(--text-xs);
  margin: 0 2px;
}
.pagination-wrap :deep(.el-pagination button) { height: 32px; min-width: 32px; border-radius: var(--radius-md); }
.pagination-wrap :deep(.el-pagination .el-pagination__sizes) { font-size: var(--text-xs); }
.pagination-wrap :deep(.el-pagination .el-select .el-input__wrapper) { font-size: var(--text-xs); }

/* ===== 空状态 ===== */
.empty-orders, .empty-address {
  padding: var(--space-12) 0;
  text-align: center;
}

/* ===== 订单详情抽屉 ===== */
.order-detail-drawer :deep(.el-drawer) {
  border-radius: var(--radius-xl) 0 0 var(--radius-xl);
  overflow: hidden;
}

.order-detail-drawer :deep(.el-drawer__body) {
  padding: 0;
  overflow-y: auto;
}

.detail-drawer-body {
  min-height: 100%;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.detail-drawer-header {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  padding-bottom: var(--space-5);
  border-bottom: 1px solid var(--border-light);
}

.detail-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}

.detail-status-pill {
  display: inline-block;
  padding: 4px 12px;
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 600;
  line-height: 1.6;
}

.sp-1 { background: var(--color-brand-50); color: var(--color-brand-500); }
.sp-2 { background: var(--color-accent-50); color: var(--color-accent); }
.sp-3 { background: #ecfdf5; color: var(--color-success); }
.sp-4 { background: #ecfdf5; color: var(--color-success); }
.sp-5 { background: #fef2f2; color: var(--color-danger); }
.sp-6 { background: var(--color-brand-50); color: var(--color-brand-500); }
.sp-7 { background: #ecfdf5; color: var(--color-success); }
.sp-8 { background: #fef2f2; color: var(--color-danger); }

.detail-order-no {
  font-size: var(--text-xs);
  color: var(--ink-faint);
  font-family: var(--font-mono);
}

.detail-header-time {
  font-size: var(--text-sm);
  color: var(--ink-muted);
}

/* 状态时间线 */
.detail-timeline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-2);
  background: var(--surface-soft);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--ink-faint);
  transition: color var(--transition-fast);
}

.timeline-item.active {
  color: var(--ink);
  font-weight: 600;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--border);
  transition: all var(--transition-fast);
  position: relative;
}

.timeline-item.active .timeline-dot {
  background: var(--color-brand-500);
  box-shadow: 0 0 0 4px var(--color-brand-50);
}

.timeline-line {
  flex: 1;
  height: 2px;
  background: var(--border-light);
  margin: 0 4px;
  margin-bottom: 20px;
  transition: background var(--transition-fast);
}

.timeline-line.active {
  background: var(--color-brand-500);
}

/* 详情区块 */
.detail-section {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.detail-sec-title {
  font-size: var(--text-xs);
  font-weight: 700;
  color: var(--ink);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.detail-item-card {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  background: var(--surface-soft);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.detail-item-img {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid var(--border-light);
  background: var(--surface);
}

.detail-item-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item-name {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-item-specs {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.detail-item-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}

.detail-item-price {
  font-size: var(--text-xs);
  color: var(--ink-muted);
}

.detail-item-qty {
  font-size: var(--text-xs);
  color: var(--ink-faint);
}

.detail-item-subtotal {
  font-size: var(--text-sm);
  font-weight: 700;
  color: var(--ink);
  width: 80px;
  text-align: right;
  flex-shrink: 0;
}

/* 金额汇总 */
.detail-amounts-v2 {
  background: var(--surface-soft);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.detail-amounts-v2 .amt-row {
  display: flex;
  justify-content: space-between;
  font-size: var(--text-sm);
  color: var(--ink-muted);
  padding: 4px 0;
}

.amt-discount { color: var(--color-success); }
.amt-divider { height: 1px; background: var(--border-light); margin: 8px 0; }
.amt-pay { font-size: var(--text-base); font-weight: 700; color: var(--ink); }
.amt-pay span:last-child { color: var(--color-brand-500); }

/* 信息网格 */
.detail-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3) var(--space-4);
  padding: var(--space-4);
  background: var(--surface-soft);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
}

.detail-info-grid .info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-cell.full { grid-column: 1 / -1; }
.icl { font-size: var(--text-xs); color: var(--ink-faint); }
.icv { font-size: var(--text-sm); color: var(--ink); word-break: break-all; }
.icv.mono { font-family: var(--font-mono); letter-spacing: 0.02em; }
.icv.expire { color: var(--color-warning); }
.icv.muted { color: var(--ink-faint); }
.icv.refund { color: var(--color-success); font-weight: 600; }
.icv.rejected { color: var(--color-danger); }

/* 发货记录 */
.detail-deliveries { padding-left: 6px; }
.del-timeline-row { position: relative; padding-left: 22px; padding-bottom: var(--space-4); }
.del-timeline-row:last-child { padding-bottom: 0; }
.del-timeline-dot { position: absolute; left: 0; top: 3px; width: 10px; height: 10px; border-radius: 50%; background: var(--color-brand-500); border: 2px solid var(--color-brand-100); z-index: 1; }
.del-timeline-line { position: absolute; left: 4px; top: 15px; width: 2px; bottom: -2px; background: var(--border-light); }
.del-content { display: flex; flex-direction: column; gap: 4px; }
.del-company { font-size: var(--text-sm); font-weight: 500; color: var(--ink); }
.del-no { font-size: var(--text-xs); color: var(--color-brand-500); font-family: var(--font-mono); }
.del-time { font-size: var(--text-xs); color: var(--ink-faint); }

/* ===== 地址管理 ===== */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-5);
}

.section-header h3 {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--ink);
}

/* ===== 收货地址 ===== */
.address-section {
  --addr-gold: #ca8a04;
  --addr-gold-soft: #d4a017;
  --addr-gold-50: rgba(202, 138, 4, 0.08);
  --addr-gold-100: rgba(202, 138, 4, 0.14);
  --addr-ink: #1c1917;
  --addr-ink-muted: #78716c;
  --addr-ink-faint: #a8a29e;
  --addr-border: rgba(28, 25, 23, 0.08);
  --addr-border-strong: rgba(28, 25, 23, 0.16);
  --addr-surface: #ffffff;
  --addr-surface-soft: #f5f5f4;
  --addr-bg: #fafaf9;
  --addr-radius: 20px;
  --addr-shadow: 0 20px 60px rgba(28, 25, 23, 0.08);
  --addr-shadow-hover: 0 28px 80px rgba(28, 25, 23, 0.12);
}

.address-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  gap: var(--space-4);
}

.address-section-title h3 {
  margin: 0 0 4px 0;
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--addr-ink);
  letter-spacing: -0.01em;
}

.address-section-title p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--addr-ink-muted);
}

.address-add-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  padding: 10px 20px;
  border: 1px solid var(--addr-gold);
  background: var(--addr-surface);
  color: var(--addr-gold);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 8px rgba(202, 138, 4, 0.08);
}

.address-add-btn:hover:not(:disabled) {
  background: var(--addr-gold-50);
  box-shadow: 0 4px 16px rgba(202, 138, 4, 0.15);
}

.address-add-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: var(--space-5);
}

.address-card {
  position: relative;
  background: var(--addr-surface);
  border: 1px solid var(--addr-border);
  border-radius: var(--addr-radius);
  overflow: hidden;
  box-shadow: var(--addr-shadow);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.address-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 3px;
  background: linear-gradient(90deg, transparent, var(--addr-gold), transparent);
  opacity: 0;
  transition: opacity 0.35s ease;
}

.address-card.is-default {
  border-color: rgba(202, 138, 4, 0.35);
  box-shadow: var(--addr-shadow), 0 0 0 1px rgba(202, 138, 4, 0.08) inset;
}

.address-card.is-default::before {
  opacity: 1;
}

.address-card-glow {
  position: absolute;
  top: -40%;
  right: -20%;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(202, 138, 4, 0.08) 0%, transparent 70%);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.35s ease;
}

.address-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--addr-shadow-hover);
  border-color: rgba(202, 138, 4, 0.25);
}

.address-card:hover .address-card-glow {
  opacity: 1;
}

.address-card-content {
  position: relative;
  z-index: 1;
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
  min-height: 180px;
}

.address-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  flex: 1;
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
}

.address-user { display: flex; align-items: baseline; gap: var(--space-3); flex-wrap: wrap; }
.address-user .name { font-weight: 700; color: var(--addr-ink); font-size: var(--text-base); }
.address-user .phone { color: var(--addr-ink-muted); font-size: var(--text-sm); font-family: var(--font-mono); }

.address-badges { display: flex; gap: var(--space-2); flex-shrink: 0; }

.default-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  background: linear-gradient(135deg, var(--addr-gold-muted, #b7791f), var(--addr-gold));
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  border-radius: var(--radius-full);
  box-shadow: 0 2px 8px rgba(202, 138, 4, 0.2);
}

.address-detail {
  display: flex;
  align-items: flex-start;
  gap: var(--space-2);
  color: var(--addr-ink-muted);
  font-size: var(--text-sm);
  line-height: 1.6;
}

.address-detail svg {
  flex-shrink: 0;
  margin-top: 2px;
  color: var(--addr-gold);
}

.address-actions {
  display: flex;
  gap: var(--space-2);
  flex-wrap: wrap;
  padding-top: var(--space-4);
  border-top: 1px solid var(--addr-border);
}

.address-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border: 1px solid var(--addr-border);
  background: var(--addr-surface);
  color: var(--addr-ink-muted);
  border-radius: var(--radius-md);
  font-size: var(--text-xs);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.address-action-btn:hover {
  border-color: var(--addr-border-strong);
  color: var(--addr-ink);
  background: var(--addr-surface-soft);
}

.address-action-btn.primary {
  border-color: rgba(202, 138, 4, 0.3);
  color: var(--addr-gold);
  background: var(--addr-gold-50);
}

.address-action-btn.primary:hover {
  border-color: var(--addr-gold);
  background: var(--addr-gold-100);
  color: var(--addr-gold-soft);
}

.address-action-btn.danger {
  border-color: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  background: rgba(239, 68, 68, 0.04);
}

.address-action-btn.danger:hover {
  border-color: rgba(239, 68, 68, 0.4);
  background: rgba(239, 68, 68, 0.08);
  color: #dc2626;
}

.empty-address {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16) var(--space-6);
  background: var(--addr-surface);
  border: 1px dashed var(--addr-border);
  border-radius: var(--addr-radius);
  text-align: center;
  gap: var(--space-4);
}

.empty-address-icon {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: var(--addr-gold-50);
  color: var(--addr-gold);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
}

.empty-address h4 {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--addr-ink);
}

.empty-address p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--addr-ink-muted);
}

.address-limit {
  margin-top: var(--space-4);
  color: var(--addr-ink-faint);
  font-size: var(--text-xs);
  text-align: center;
}

/* 地址编辑弹窗 —— 外层容器样式已迁移到页面底部全局样式块，解决 teleport 后 scoped 变量失效问题 */

.address-edit-header {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-6);
  background: linear-gradient(135deg, var(--addr-bg) 0%, var(--addr-surface-soft) 100%);
  border-bottom: 1px solid var(--addr-border);
  position: relative;
}

.address-edit-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  background: var(--addr-gold-50);
  color: var(--addr-gold);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.address-edit-title { flex: 1; min-width: 0; }
.address-edit-title h4 {
  margin: 0 0 4px 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--addr-ink);
  letter-spacing: -0.01em;
}
.address-edit-title p {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--addr-ink-muted);
}

.address-edit-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--addr-border);
  background: var(--addr-surface);
  color: var(--addr-ink-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.address-edit-close:hover {
  background: var(--addr-surface-soft);
  color: var(--addr-ink);
  border-color: var(--addr-border-strong);
}

.address-form-body {
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* 横向表单布局 */
.address-form-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-5);
}

.address-form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.address-form-field.horizontal {
  display: grid;
  grid-template-columns: 80px 1fr;
  align-items: center;
  gap: 12px;
  padding: 0;
  background: transparent;
  border: none;
  border-radius: 0;
  box-shadow: none;
}

.address-form-field.elegant {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.address-form-field.elegant.full {
  grid-column: 1 / -1;
}

.address-field-label-elegant {
  font-size: 13px;
  font-weight: 600;
  color: var(--addr-ink);
  letter-spacing: 0.02em;
  padding-left: 2px;
}

.address-field-label-elegant::after {
  content: '*';
  color: var(--addr-gold);
  margin-left: 3px;
}

.address-field-input-elegant {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border: 1px solid #e7e5e4;
  border-radius: 14px;
  padding: 0 16px;
  min-height: 54px;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 1px 2px rgba(28, 25, 23, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.address-field-input-elegant:hover {
  border-color: #d6d3d1;
  box-shadow: 0 2px 8px rgba(28, 25, 23, 0.05);
}

.address-field-input-elegant:focus-within {
  border-color: var(--addr-gold);
  box-shadow:
    0 0 0 4px var(--addr-gold-50),
    0 4px 12px rgba(202, 138, 4, 0.08);
}

.address-field-input-elegant.textarea {
  align-items: flex-start;
  padding-top: 14px;
  padding-bottom: 14px;
}

.addr-input-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a8a29e;
  flex-shrink: 0;
  transition: color 0.25s ease;
}

.address-field-input-elegant:focus-within .addr-input-icon {
  color: var(--addr-gold);
}

.address-field-input-elegant input,
.address-field-input-elegant textarea {
  flex: 1;
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 15px;
  color: var(--addr-ink);
  line-height: 1.5;
  min-width: 0;
}

.address-field-input-elegant input {
  height: 54px;
}

.address-field-input-elegant textarea {
  padding: 0;
  resize: none;
  font-family: inherit;
}

.address-field-input-elegant input::placeholder,
.address-field-input-elegant textarea::placeholder {
  color: #a8a29e;
}

.address-region-cascader-elegant {
  flex: 1;
  width: 100%;
  min-width: 0;
}

.address-region-cascader-elegant :deep(.el-input__wrapper) {
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  border: none;
  padding: 0;
  height: auto;
}

.address-region-cascader-elegant :deep(.el-input__inner) {
  height: 54px;
  font-size: 15px;
  color: var(--addr-ink);
}

.address-region-cascader-elegant :deep(.el-input__inner::placeholder) {
  color: #a8a29e;
}

.address-field-error-elegant {
  margin: 0;
  padding-left: 2px;
  font-size: var(--text-xs);
  color: #ef4444;
  display: flex;
  align-items: center;
  gap: 6px;
}

.address-field-error-elegant::before {
  content: '';
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #ef4444;
}

.address-form-field.inline {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
  border-bottom: none;
  padding-top: var(--space-2);
}

.address-field-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--addr-gold);
}

.address-checkbox {
  display: inline-flex;
  align-items: center;
  gap: var(--space-3);
  cursor: pointer;
  user-select: none;
  padding: 10px 14px;
  border-radius: 12px;
  border: 1px solid var(--addr-border);
  background: var(--addr-surface);
  transition: all var(--transition-fast);
}

.address-checkbox:hover {
  border-color: var(--addr-gold);
  background: var(--addr-gold-50);
}

.address-custom-checkbox {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  border: 2px solid #d6d3d1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: all var(--transition-fast);
  flex-shrink: 0;
  box-shadow: inset 0 1px 2px rgba(28, 25, 23, 0.06);
}

.address-custom-checkbox.checked {
  background: var(--addr-gold);
  border-color: var(--addr-gold);
  box-shadow: 0 2px 6px rgba(202, 138, 4, 0.25);
}

.address-checkbox-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--addr-ink);
}

.address-checkbox-tip {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--addr-ink-faint);
}

.address-edit-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.address-edit-cancel {
  padding: 10px 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--addr-border);
  background: var(--addr-surface);
  color: var(--addr-ink-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.address-edit-cancel:hover {
  border-color: var(--addr-border-strong);
  color: var(--addr-ink);
  background: var(--addr-surface-soft);
}

.address-edit-save {
  padding: 10px 24px;
  border-radius: var(--radius-md);
  border: 1px solid var(--addr-gold);
  background: linear-gradient(135deg, var(--addr-gold-muted, #b7791f), var(--addr-gold));
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.address-edit-save:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--addr-gold), var(--addr-gold-soft));
  box-shadow: 0 4px 16px rgba(202, 138, 4, 0.2);
}

.address-edit-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== 安全设置 ===== */
.security-header {
  margin-bottom: var(--space-5);
}

.security-header h3 {
  margin: 0 0 6px 0;
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--ink);
}

.security-header p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--ink-faint);
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.security-item {
  display: flex;
  align-items: center;
  padding: var(--space-5) var(--space-6);
  background: var(--surface);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  gap: var(--space-5);
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.security-item:hover {
  border-color: var(--color-brand-200);
  box-shadow: var(--shadow-md);
}

.security-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--color-brand-50);
  color: var(--color-brand-500);
}

.security-icon.phone {
  background: var(--color-accent-50);
  color: var(--color-accent);
}

.security-icon.email {
  background: #e6f7e6;
  color: var(--color-success);
}

.security-info { flex: 1; min-width: 0; }
.security-info h4 { margin: 0 0 4px 0; color: var(--ink); font-size: var(--text-base); font-weight: 600; }
.security-info p { margin: 0; color: var(--ink-faint); font-size: var(--text-xs); }

.security-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--ink-muted);
  font-size: var(--text-xs);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}

.security-action-btn:hover {
  border-color: var(--color-brand-500);
  color: var(--color-brand-500);
  background: var(--color-brand-50);
}

/* ===== 安全设置弹窗 ===== */
.security-dialog :deep(.el-dialog) {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.security-dialog :deep(.el-dialog__header) { display: none; }
.security-dialog :deep(.el-dialog__body) { padding: var(--space-6); }
.security-dialog :deep(.el-dialog__footer) { padding: 0 var(--space-6) var(--space-6); }

.security-dialog-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.security-dialog-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--color-brand-50);
  color: var(--color-brand-500);
}

.security-dialog-icon.phone {
  background: var(--color-accent-50);
  color: var(--color-accent);
}

.security-dialog-icon.email {
  background: #e6f7e6;
  color: var(--color-success);
}

.security-dialog-title h4 {
  margin: 0 0 4px 0;
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--ink);
}

.security-dialog-title p {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--ink-faint);
  line-height: 1.5;
}

.security-form :deep(.el-form-item__label) {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--ink-muted);
  padding-bottom: 4px;
}

.security-form :deep(.el-form-item) { margin-bottom: var(--space-4); }
.security-form :deep(.el-form-item:last-child) { margin-bottom: 0; }

.security-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  box-shadow: 0 0 0 1px var(--border) inset;
  padding: 2px 12px;
}

.security-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-brand-500) inset;
}

.security-form :deep(.el-input__inner) {
  height: 40px;
  font-size: var(--text-sm);
}

.security-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.security-cancel-btn {
  padding: 10px 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--ink-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.security-cancel-btn:hover {
  border-color: var(--ink-muted);
  color: var(--ink);
}

.security-submit-btn {
  padding: 10px 20px;
  border-radius: var(--radius-md);
  border: 1px solid var(--color-brand-500);
  background: var(--color-brand-500);
  color: #fff;
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.security-submit-btn:hover {
  background: var(--color-brand-600);
  border-color: var(--color-brand-600);
}

/* ===== 通用弹窗 ===== */
.dialog-body { padding: var(--space-2) 0; }
.dialog-tip { font-size: var(--text-sm); color: var(--ink-muted); margin: 0 0 var(--space-4); }
.dialog-tip.warn { color: var(--color-warning); }
.full-select { width: 100%; }

/* 退款弹窗 */
.refund-dialog :deep(.el-dialog) {
  border-radius: var(--radius-xl);
  overflow: hidden;
}
.refund-dialog :deep(.el-dialog__header) { padding: var(--space-5) var(--space-6) 0; border-bottom: 1px solid var(--border-light); margin: 0; }
.refund-dialog :deep(.el-dialog__body) { padding: var(--space-4) var(--space-6); }
.refund-dialog :deep(.el-dialog__footer) { padding: 0 var(--space-6) var(--space-5); }
.refund-dialog-header { display: flex; align-items: flex-start; gap: var(--space-3); padding-bottom: var(--space-4); }
.refund-dialog-icon { display: inline-flex; align-items: center; justify-content: center; width: 36px; height: 36px; border-radius: 10px; background: var(--color-brand-50); color: var(--color-brand-500); font-size: 18px; flex-shrink: 0; }
.refund-dialog-title { font-size: var(--text-base); font-weight: 700; color: var(--ink); line-height: 1.4; }
.refund-dialog-sub { font-size: var(--text-xs); color: var(--ink-faint); margin-top: 2px; }
.refund-body { display: flex; flex-direction: column; gap: var(--space-4); }
.refund-order-info { background: var(--surface-soft); border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: var(--space-3); }
.refund-order-no { font-size: var(--text-xs); color: var(--ink-faint); font-family: var(--font-mono); margin-bottom: var(--space-2); letter-spacing: 0.02em; }
.refund-items-preview { display: flex; flex-direction: column; gap: 6px; }
.refund-item-row { display: flex; align-items: center; gap: 10px; }
.refund-item-img { width: 40px; height: 40px; border-radius: var(--radius-sm); object-fit: cover; flex-shrink: 0; border: 1px solid var(--border-light); background: var(--surface); }
.refund-item-detail { flex: 1; min-width: 0; }
.refund-item-name { font-size: var(--text-xs); color: var(--ink); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.refund-item-specs { font-size: 10px; color: var(--ink-faint); margin-top: 1px; }
.refund-item-qty { font-size: var(--text-xs); color: var(--ink-faint); flex-shrink: 0; }
.refund-more-items { font-size: 10px; color: var(--ink-faint); padding-left: 52px; }
.refund-amount-row { display: flex; align-items: center; justify-content: flex-end; gap: var(--space-2); margin-top: var(--space-2); padding-top: var(--space-2); border-top: 1px solid var(--border-light); }
.refund-amount-label { font-size: var(--text-xs); color: var(--ink-faint); }
.refund-amount-value { font-size: var(--text-base); font-weight: 700; color: var(--color-brand-500); }
.refund-reason-section { display: flex; flex-direction: column; gap: 6px; }
.refund-reason-label { font-size: var(--text-xs); font-weight: 500; color: var(--ink); }
.refund-reason-select { width: 100%; }
.refund-footer { display: flex; justify-content: flex-end; gap: var(--space-2); }

.btn-loading-icon {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-right: 4px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 密码切换按钮 */
.password-toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--ink-faint);
  transition: color var(--transition-fast);
}

.password-toggle-btn:hover { color: var(--color-brand-500); }
.password-toggle-btn:focus { outline: none; }
.eye-icon { width: 18px; height: 18px; }

/* 默认占位 */
.default-section {
  text-align: center;
  padding: var(--space-16) 0;
}

.default-section p {
  color: var(--ink-faint);
  font-size: var(--text-sm);
  margin-top: var(--space-2);
}

/* ===== 响应式 ===== */
@media (max-width: 767px) {
  .profile-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    border-right: none;
    flex-direction: row;
    flex-wrap: wrap;
    padding: var(--space-2);
    position: static;
    top: auto;
    height: auto;
  }

  .sidebar-user {
    width: 100%;
    padding: var(--space-3) var(--space-4);
  }

  .sidebar-nav {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 4px;
    padding: 4px 8px;
  }

  .nav-item {
    flex: 1;
    min-width: calc(33.33% - 4px);
    justify-content: center;
    padding: var(--space-2) var(--space-2);
    font-size: var(--text-xs);
    border-radius: var(--radius-sm);
  }

  .sidebar-footer {
    width: 100%;
    padding: 4px 8px;
  }

  .content-box {
    padding: var(--space-4);
    min-height: auto;
  }

  .welcome-cell,
  .account-cell,
  .stat-cell,
  .quick-cell {
    grid-column: span 12;
  }

  .bento-grid {
    gap: var(--space-4);
  }

  .welcome-card {
    padding: var(--space-5);
    flex-direction: column;
    text-align: center;
    gap: var(--space-4);
  }

  .welcome-title {
    font-size: var(--text-xl);
  }

  .welcome-avatar img {
    width: 64px;
    height: 64px;
  }

  .quick-list {
    grid-template-columns: 1fr;
  }

  .profile-head-v3 {
    padding: 0 var(--space-5) var(--space-6);
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .profile-meta-v3 {
    align-items: center;
  }

  .profile-meta-row {
    justify-content: center;
  }

  .profile-edit-btn-v3 {
    margin-left: 0;
    margin-top: var(--space-3);
  }

  .profile-body-v3 {
    padding: 0 var(--space-5) var(--space-6);
  }

  .info-grid-v3 {
    grid-template-columns: 1fr;
    gap: var(--space-3);
  }

  .profile-cover {
    height: 100px;
  }

  .address-list {
    grid-template-columns: 1fr;
  }

  .overview-section {
    max-width: 100%;
  }

  .order-list {
    grid-template-columns: 1fr;
  }

  .order-card-actions {
    justify-content: center;
  }

  .order-tabs :deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
    padding: 0 10px;
    font-size: var(--text-xs);
  }

  .detail-item-card {
    flex-wrap: wrap;
  }

  .detail-item-subtotal {
    width: 100%;
    text-align: left;
    margin-left: 68px;
  }

  .detail-timeline {
    padding: var(--space-3) var(--space-1);
  }

  .timeline-line {
    margin-bottom: 16px;
  }

  .default-section {
    padding: var(--space-10) 0;
  }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .content-box {
    padding: var(--space-6);
  }

  .welcome-cell,
  .account-cell,
  .quick-cell {
    grid-column: span 12;
  }

  .stat-cell {
    grid-column: span 6;
  }

  .address-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }

  .sidebar {
    width: 220px;
  }
}
</style>

<style>
/* 全局确认对话框样式 — 用于收货地址删除/设默认确认 */
.lux-message-box.el-message-box {
  border-radius: 20px;
  border: 1px solid rgba(28, 25, 23, 0.08);
  box-shadow: 0 28px 80px rgba(28, 25, 23, 0.12);
  padding-bottom: 24px;
}

.lux-message-box .el-message-box__header {
  padding: 24px 24px 12px;
}

.lux-message-box .el-message-box__title {
  font-size: 18px;
  font-weight: 700;
  color: #1c1917;
}

.lux-message-box .el-message-box__content {
  padding: 12px 24px;
  color: #78716c;
  font-size: 14px;
  line-height: 1.6;
}

.lux-message-box .el-message-box__btns {
  padding: 16px 24px 0;
  gap: 12px;
}

.lux-message-box .el-message-box__btns .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.lux-message-box .lux-msg-cancel {
  border: 1px solid rgba(28, 25, 23, 0.08);
  background: #fff;
  color: #78716c;
}

.lux-message-box .lux-msg-cancel:hover {
  border-color: rgba(28, 25, 23, 0.16);
  color: #1c1917;
  background: #f5f5f4;
}

.lux-message-box .lux-msg-confirm {
  border: 1px solid #ca8a04;
  background: linear-gradient(135deg, #b7791f, #ca8a04);
  color: #fff;
}

.lux-message-box .lux-msg-confirm:hover {
  background: linear-gradient(135deg, #ca8a04, #d4a017);
  box-shadow: 0 4px 16px rgba(202, 138, 4, 0.2);
}

.lux-message-box .lux-msg-confirm-danger {
  border: 1px solid #ef4444;
  background: linear-gradient(135deg, #dc2626, #ef4444);
  color: #fff;
}

.lux-message-box .lux-msg-confirm-danger:hover {
  background: linear-gradient(135deg, #ef4444, #f87171);
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.2);
}

/* ===== 地址编辑弹窗全局样式（teleport 到 body 后 scoped 样式无法作用到外层 .el-dialog） ===== */
.address-edit-dialog.el-dialog {
  /* 收货地址主题变量 */
  --addr-gold: #ca8a04;
  --addr-gold-soft: #d4a017;
  --addr-gold-50: rgba(202, 138, 4, 0.08);
  --addr-gold-100: rgba(202, 138, 4, 0.14);
  --addr-ink: #1c1917;
  --addr-ink-muted: #78716c;
  --addr-ink-faint: #a8a29e;
  --addr-border: rgba(28, 25, 23, 0.08);
  --addr-border-strong: rgba(28, 25, 23, 0.16);
  --addr-surface: #ffffff;
  --addr-surface-soft: #f5f5f4;
  --addr-bg: #fafaf9;
  --addr-radius: 20px;
  --addr-shadow: 0 20px 60px rgba(28, 25, 23, 0.08);
  --addr-shadow-hover: 0 28px 80px rgba(28, 25, 23, 0.12);

  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  max-width: 620px;
  width: 90% !important;
  margin: 0;
  border-radius: var(--addr-radius);
  overflow: hidden;
  background: var(--addr-surface);
  border: 1px solid var(--addr-border);
  box-shadow: var(--addr-shadow-hover);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

.address-edit-dialog .el-dialog__header {
  display: none;
}

.address-edit-dialog .el-dialog__body {
  padding: 0;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.address-edit-dialog .el-dialog__footer {
  padding: 16px 24px 24px;
  border-top: 1px solid var(--addr-border);
  margin: 0;
}
</style>
