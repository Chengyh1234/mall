<template>
  <div class="profile-container">
    <NavBar />

    <div class="profile-content">
      <!-- 侧边栏菜单 -->
      <el-aside class="sidebar" width="250px">
        <el-menu
          :default-active="activeSideMenu"
          class="side-menu"
          @select="handleSideMenuSelect"
        >
          <!-- 普通用户菜单（所有登录用户都有） -->
          <el-menu-item index="overview">
            <el-icon><DataAnalysis /></el-icon>
            <span>我的看板</span>
          </el-menu-item>

          <el-menu-item index="orders">
            <el-icon><Document /></el-icon>
            <span>我的订单</span>
          </el-menu-item>
          
          <el-menu-item index="cart">
            <el-icon><ShoppingCart /></el-icon>
            <span>我的购物车</span>
          </el-menu-item>
          
          <el-sub-menu index="account">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>账号设置</span>
            </template>
            <el-menu-item index="security">
              <el-icon><Lock /></el-icon>
              <span>安全设置</span>
            </el-menu-item>
            <el-menu-item index="info">
              <el-icon><User /></el-icon>
              <span>个人资料</span>
            </el-menu-item>
            <el-menu-item index="address">
              <el-icon><Location /></el-icon>
              <span>收货地址</span>
            </el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="logout" class="logout-item">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <div v-loading="loading" class="content-box">
          <!-- 订单概览 -->
          <div v-if="activeSideMenu === 'overview'" class="overview-section">
            <!-- 欢迎横幅 -->
            <div class="welcome-banner">
              <div class="banner-content">
                <h1 class="welcome-title">嗨, {{ userInfo?.username || '用户' }}</h1>
                <p class="welcome-sub">欢迎回来，这是你的订单动态</p>
              </div>
              <div class="banner-avatar">
                <el-avatar :size="56" :src="avatarUrl" />
              </div>
            </div>

            <!-- 统计卡片网格 -->
            <div class="stat-grid">
              <div
                class="stat-card"
                :class="{ clickable: true }"
                @click="goToOrdersTab('pending_pay')"
              >
                <div class="stat-icon-wrap pending-pay">
                  <el-icon class="stat-icon"><Timer /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value" v-html="loadingStatusCount ? '<span class=shimmer>--</span>' : statusCount.pendingPayment"></span>
                  <span class="stat-label">待付款</span>
                </div>
                <div class="stat-footer">
                  <span>等待支付</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>

              <div
                class="stat-card"
                :class="{ clickable: true }"
                @click="goToOrdersTab('pending_ship')"
              >
                <div class="stat-icon-wrap pending-ship">
                  <el-icon class="stat-icon"><Van /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value" v-html="loadingStatusCount ? '<span class=shimmer>--</span>' : statusCount.pendingDelivery"></span>
                  <span class="stat-label">待发货</span>
                </div>
                <div class="stat-footer">
                  <span>等待商家发货</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>

              <div
                class="stat-card"
                :class="{ clickable: true }"
                @click="goToOrdersTab('pending_receive')"
              >
                <div class="stat-icon-wrap pending-receive">
                  <el-icon class="stat-icon"><Box /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value" v-html="loadingStatusCount ? '<span class=shimmer>--</span>' : statusCount.pendingReceipt"></span>
                  <span class="stat-label">待收货</span>
                </div>
                <div class="stat-footer">
                  <span>等待确认收货</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>

              <div
                class="stat-card"
                :class="{ clickable: true }"
                @click="goToOrdersTab('refunding')"
              >
                <div class="stat-icon-wrap refunding">
                  <el-icon class="stat-icon"><WarningFilled /></el-icon>
                </div>
                <div class="stat-info">
                  <span class="stat-value" v-html="loadingStatusCount ? '<span class=shimmer>--</span>' : statusCount.refunding"></span>
                  <span class="stat-label">退款中</span>
                </div>
                <div class="stat-footer">
                  <span>等待退款处理</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </div>

            <!-- 快捷入口 -->
            <div class="quick-links">
              <h3 class="section-title">快捷入口</h3>
              <div class="links-grid">
                <div class="quick-card" @click="handleSideMenuSelect('orders')">
                  <el-icon class="q-icon"><Document /></el-icon>
                  <span>全部订单</span>
                </div>
                <div class="quick-card" @click="handleSideMenuSelect('info')">
                  <el-icon class="q-icon"><User /></el-icon>
                  <span>个人资料</span>
                </div>
                <div class="quick-card" @click="handleSideMenuSelect('address')">
                  <el-icon class="q-icon"><Location /></el-icon>
                  <span>收货地址</span>
                </div>
                <div class="quick-card" @click="handleSideMenuSelect('cart')">
                  <el-icon class="q-icon"><ShoppingCart /></el-icon>
                  <span>购物车</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 个人资料 -->
          <div v-if="activeSideMenu === 'info'" class="profile-section">
            <div v-loading="profileLoading" class="profile-card">
              <!-- 头部：头像 + 基本信息 -->
              <div class="profile-head">
                <div class="profile-avatar-area">
                  <div class="avatar-frame" @click="handleAvatarClick" title="点击更换头像">
                    <el-avatar :size="96" :src="avatarUrl" class="profile-avatar" />
                    <div class="avatar-overlay">
                      <el-icon><Camera /></el-icon>
                      <span>更换头像</span>
                    </div>
                  </div>
                  <input
                    ref="fileInputRef"
                    type="file"
                    accept="image/jpeg,image/png,image/gif,image/webp"
                    style="display: none"
                    @change="handleAvatarChange"
                  />
                  <div class="avatar-name">
                    <h2 class="profile-username">{{ profileData?.username || '-' }}</h2>
                  </div>
                </div>
                <div class="profile-actions">
                  <el-button type="primary" plain :icon="Edit" @click="showEditDialog = true">编辑资料</el-button>
                </div>
              </div>

              <!-- 详细信息 -->
              <div class="profile-body">
                <div class="info-grid">
                  <div class="info-cell">
                    <span class="cell-label">用户ID</span>
                    <span class="cell-value">{{ profileData?.id || '-' }}</span>
                  </div>
                  <div class="info-cell">
                    <span class="cell-label">用户名</span>
                    <span class="cell-value">{{ profileData?.username || '-' }}</span>
                  </div>
                  <div class="info-cell">
                    <span class="cell-label">真实姓名</span>
                    <span class="cell-value">{{ profileData?.realName || '未设置' }}</span>
                  </div>
                  <div class="info-cell">
                    <span class="cell-label">手机号</span>
                    <span class="cell-value">{{ profileData?.phone || '未绑定' }}</span>
                  </div>
                  <div class="info-cell">
                    <span class="cell-label">邮箱</span>
                    <span class="cell-value">{{ profileData?.email || '未绑定' }}</span>
                  </div>
                  <div class="info-cell">
                    <span class="cell-label">注册时间</span>
                    <span class="cell-value mono">{{ formatTime(profileData?.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 编辑资料弹窗 -->
            <el-dialog v-model="showEditDialog" title="编辑个人资料" width="420px" destroy-on-close class="edit-dialog">
              <el-form :model="editForm" label-width="80px" class="edit-form">
                <el-form-item label="用户名">
                  <el-input v-model="editForm.username" placeholder="输入新的用户名" maxlength="20" />
                </el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="showEditDialog = false">取消</el-button>
                <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">保存</el-button>
              </template>
            </el-dialog>
          </div>

          <!-- 我的订单 -->
          <div v-else-if="activeSideMenu === 'orders'" class="orders-section">
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
                v-for="order in orders"
                :key="order.id"
                class="order-card"
                @click="viewDetail(order)"
              >
                <div class="order-card-header">
                  <div class="header-left">
                    <span class="order-time">{{ formatDate(order.createdAt) }}</span>
                    <span class="order-no">{{ order.orderNo }}</span>
                  </div>
                  <div class="header-right">
                    <span class="status-pill" :class="'pill-' + order.status">{{ getStatusText(order.status) }}</span>
                  </div>
                </div>

                <div class="order-card-body">
                  <div class="order-items" v-if="order.items && order.items.length">
                    <div class="order-item" v-for="(item, idx) in order.items" :key="idx">
                      <div class="item-img-wrap">
                        <img class="item-thumb" :src="orderItemImage(item.productImage || item.image)" />
                      </div>
                      <div class="item-info">
                        <div class="item-name">{{ item.productName || item.name }}</div>
                        <div class="item-specs" v-if="item.skuSpecs || item.specs">{{ item.skuSpecs || item.specs }}</div>
                      </div>
                      <div class="item-meta">
                        <span class="item-price">¥{{ (item.price || 0).toFixed(2) }}</span>
                        <span class="item-qty">&times;{{ item.quantity }}</span>
                      </div>
                      <div class="item-subtotal">¥{{ (item.totalAmount || item.price * item.quantity || 0).toFixed(2) }}</div>
                    </div>
                  </div>
                  <div v-else class="order-summary">共 {{ order.itemCount || '-' }} 件商品</div>
                  <div v-if="order.remark" class="order-remark">{{ order.remark }}</div>
                </div>

                <div class="order-card-footer">
                  <div class="footer-left">
                    <div v-if="order.status === 1 && order.expireTime" class="status-badge status-expire">
                      <el-icon class="badge-icon"><Clock /></el-icon>
                      剩余 {{ formatExpireTime(order.expireTime, countdownNow) }}
                    </div>
                    <div v-else-if="(order.status === 6 || order.status === 7) && order.refundAmount !== null" class="status-badge status-refund">
                      退款 ¥{{ Number(order.refundAmount).toFixed(2) }}
                    </div>
                    <div v-else-if="order.status === 8 && order.rejectReason" class="status-badge status-cancel">
                     拒绝原因：{{ order.rejectReason }}<span v-if="order.rejectedAt">（{{ formatDate(order.rejectedAt) }}）</span>
                    </div>
                    <div v-else-if="order.deliveryCompany && order.deliveryNo" class="status-badge status-delivery">
                      <el-icon class="badge-icon"><Van /></el-icon>
                      {{ order.deliveryCompany }} {{ order.deliveryNo }}
                    </div>
                    <div v-else-if="order.status === 5 && order.cancelReason" class="status-badge status-cancel">
                      {{ order.cancelReason }}
                    </div>
                    <div v-else-if="order.status === 2" class="status-badge status-pending-ship">
                      等待卖家发货
                    </div>
                  </div>

                  <div class="footer-right">
                    <div class="amount-summary">
                      <div class="amount-row">
                        <span class="amount-label">总额</span>
                        <span class="amount-value">¥{{ (order.totalAmount || 0).toFixed(2) }}</span>
                      </div>
                      <template v-if="order.discountAmount">
                        <div class="amount-row discount-row">
                          <span class="amount-label">优惠</span>
                          <span class="amount-value discount">-¥{{ Number(order.discountAmount).toFixed(2) }}</span>
                        </div>
                      </template>
                      <div class="amount-row pay-row">
                        <span class="amount-label">实付</span>
                        <span class="amount-value pay">¥{{ (order.payAmount || 0).toFixed(2) }}</span>
                      </div>
                    </div>
                    <div class="order-actions" @click.stop>
                      <template v-if="order.status === 1">
                        <button class="action-btn secondary" @click="showCancelDialog(order)">取消</button>
                        <button class="action-btn primary" @click="handlePay(order)">立即支付</button>
                      </template>
                      <template v-else-if="order.status === 2">
                        <button class="action-btn ghost" @click="handleRefund(order)">申请退款</button>
                      </template>
                      <template v-else-if="order.status === 3">
                        <button class="action-btn primary" @click="handleReceive(order)">确认收货</button>
                        <button class="action-btn ghost" @click="handleRefund(order)">申请退款</button>
                      </template>
                      <template v-else-if="order.status === 4">
                        <button class="action-btn ghost" @click="handleRefund(order)">申请退款</button>
                        <button class="action-btn ghost danger" @click="handleDelete(order)">删除</button>
                      </template>
                      <template v-else-if="order.status === 5">
                        <button class="action-btn ghost danger" @click="handleDelete(order)">删除</button>
                      </template>
                      <template v-else-if="order.status === 6">
                        <button class="action-btn ghost" @click="handleCancelRefund(order)">取消退款</button>
                        <span class="status-tip">退款审核中</span>
                      </template>
                      <template v-else-if="order.status === 7">
                        <span class="status-tip refund-done">已退款</span>
                      </template>
                      <template v-else-if="order.status === 8">
                        <button class="action-btn ghost" @click="handleCancelRefund(order)">取消退款</button>
                      </template>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 分页 -->
              <div v-if="pagination.total > 0" class="pagination-wrap">
                <el-pagination
                  v-model:current-page="pagination.page"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50]"
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
          <div v-else-if="activeSideMenu === 'address'" class="address-section">
            <div class="section-header">
              <h3>收货地址</h3>
              <el-button 
                type="primary" 
                size="small" 
                @click="showAddAddressModal = true"
                :disabled="addresses.length >= 10"
              >
                添加地址
              </el-button>
            </div>
            <div v-if="addresses.length > 0" class="address-list">
              <div 
                v-for="addr in addresses" 
                :key="addr.id" 
                class="address-card"
              >
                <div class="address-header">
                  <div class="address-user">
                    <span class="name">{{ addr.receiverName }}</span>
                    <span class="phone">{{ addr.receiverPhone }}</span>
                  </div>
                  <span v-if="addr.isDefault === 1" class="default-tag">默认</span>
                </div>
                <div class="address-detail">
                  {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
                </div>
                <div class="address-actions">
                  <el-button 
                    v-if="addr.isDefault !== 1" 
                    size="small" 
                    @click="setDefault(addr.id)"
                  >
                    设为默认
                  </el-button>
                  <el-button size="small" @click="editAddress(addr)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteAddressHandler(addr.id)">删除</el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-address">
              <el-empty description="暂无收货地址">
                <el-button type="primary" @click="showAddAddressModal = true">添加地址</el-button>
              </el-empty>
            </div>
            <p v-if="addresses.length >= 10" class="address-limit">* 最多可添加10个收货地址</p>
          </div>

          <!-- 安全设置 -->
          <div v-else-if="activeSideMenu === 'security'" class="security-section">
            <h3>安全设置</h3>
            <div class="security-list">
              <div class="security-item">
                <div class="security-icon">🔐</div>
                <div class="security-info">
                  <h4>修改密码</h4>
                  <p>定期更换密码，保护账户安全</p>
                </div>
                <el-button type="primary" size="small" @click="showChangePasswordModal = true">修改</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">📱</div>
                <div class="security-info">
                  <h4>修改手机号</h4>
                  <p>{{ userInfo?.phone || '未绑定' }}</p>
                </div>
                <el-button type="primary" size="small" @click="showChangePhoneModal = true">修改</el-button>
              </div>
              <div class="security-item">
                <div class="security-icon">📧</div>
                <div class="security-info">
                  <h4>修改邮箱</h4>
                  <p>{{ userInfo?.email || '未绑定' }}</p>
                </div>
                <el-button type="primary" size="small" @click="showChangeEmailModal = true">修改</el-button>
              </div>
            </div>
          </div>

          <!-- 默认显示 -->
          <div v-else class="default-section">
            <h3>欢迎来到个人中心</h3>
            <p>请从左侧菜单选择要查看的内容</p>
          </div>
        </div>
      </el-main>
    </div>

    <!-- 添加/编辑地址弹窗 -->
    <el-dialog 
      :title="editingAddress ? '编辑地址' : '添加地址'" 
      v-model="showAddAddressModal"
      width="500px"
    >
      <el-form :model="addressForm" label-width="80px" class="address-form">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="addressForm.receiverPhone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-cascader 
            v-model="addressForm.region" 
            :options="regionOptions" 
            placeholder="请选择省市区"
            :props="{ checkStrictly: false }"
          />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detailAddress" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault" :true-value="1" :false-value="0">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeAddressModal">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog title="修改密码" v-model="showChangePasswordModal" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input 
            v-model="passwordForm.oldPassword" 
            :type="showOldPassword ? 'text' : 'password'" 
            placeholder="请输入原密码"
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
            placeholder="请确认新密码"
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
        <el-button @click="showChangePasswordModal = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确定</el-button>
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
              <img class="refund-item-img" :src="orderItemImage(item.productImage || item.image)" />
              <div class="refund-item-detail">
                <div class="refund-item-name">{{ item.productName || item.name }}</div>
                <div class="refund-item-specs" v-if="item.skuSpecs || item.specs">{{ item.skuSpecs || item.specs }}</div>
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

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" width="740px" :close-on-click-modal="false" top="4vh" class="detail-dialog">
      <template #header>
        <div class="detail-dialog-header">
          <span class="detail-status-pill" :class="'sp-' + (orderDetail?.status || 0)">
            {{ orderDetail?.statusDesc || getStatusText(orderDetail?.status || 0) }}
          </span>
          <span class="detail-order-no">{{ orderDetail?.orderNo }}</span>
        </div>
      </template>

      <div v-loading="detailLoading" class="detail-body">
        <template v-if="orderDetail">
          <!-- ===== 商品列表 ===== -->
          <div class="detail-items-section">
            <div class="detail-sec-title">商品信息</div>
            <div class="detail-item-row" v-for="(item, i) in orderDetail.items || []" :key="i">
              <img class="detail-item-img" :src="orderItemImage(item.productImage)" />
              <div class="detail-item-info">
                <div class="detail-item-name">{{ item.productName }}</div>
                <div class="detail-item-specs">{{ item.skuSpecs }}</div>
              </div>
              <div class="detail-item-price">¥{{ item.price.toFixed(2) }}</div>
              <div class="detail-item-qty">&times;{{ item.quantity }}</div>
              <div class="detail-item-subtotal">¥{{ item.totalAmount.toFixed(2) }}</div>
            </div>
          </div>

          <!-- ===== 金额汇总 ===== -->
          <div class="detail-amounts">
            <div class="amt-row">
              <span>商品金额</span>
              <span>¥{{ orderDetail.totalAmount.toFixed(2) }}</span>
            </div>
            <div class="amt-row" v-if="orderDetail.discountAmount">
              <span>优惠</span>
              <span class="amt-discount">-¥{{ orderDetail.discountAmount.toFixed(2) }}</span>
            </div>
            <div class="amt-row" v-if="orderDetail.freightAmount">
              <span>运费</span>
              <span>¥{{ orderDetail.freightAmount.toFixed(2) }}</span>
            </div>
            <div class="amt-divider"></div>
            <div class="amt-row amt-pay">
              <span>实付金额</span>
              <span>¥{{ orderDetail.payAmount.toFixed(2) }}</span>
            </div>
          </div>

          <!-- ===== 订单信息 ===== -->
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

          <!-- ===== 收货信息 ===== -->
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

          <!-- ===== 退款信息 ===== -->
          <template v-if="orderDetail.status >= 6">
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
          </template>

          <!-- ===== 发货记录 ===== -->
          <div class="detail-sec-title" v-if="orderDetail.deliveries && orderDetail.deliveries.length">发货记录</div>
          <div class="detail-deliveries" v-if="orderDetail.deliveries && orderDetail.deliveries.length">
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
        </template>

        <el-empty v-else-if="!detailLoading" description="暂无订单详情" />
      </div>
    </el-dialog>

    <!-- 修改手机号弹窗 -->
    <el-dialog title="修改手机号" v-model="showChangePhoneModal" width="400px">
      <el-form :model="phoneForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input 
            v-model="phoneForm.password" 
            type="password" 
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新手机号">
          <el-input v-model="phoneForm.newPhone" placeholder="请输入新手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePhoneModal = false">取消</el-button>
        <el-button type="primary" @click="changePhone">确定</el-button>
      </template>
    </el-dialog>

    <!-- 修改邮箱弹窗 -->
    <el-dialog title="修改邮箱" v-model="showChangeEmailModal" width="400px">
      <el-form :model="emailForm" label-width="80px">
        <el-form-item label="原密码">
          <el-input 
            v-model="emailForm.password" 
            type="password" 
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.newEmail" placeholder="请输入新邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangeEmailModal = false">取消</el-button>
        <el-button type="primary" @click="changeEmail">确定</el-button>
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

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 用户头像URL
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return 'https://via.placeholder.com/80'
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
  pageSize: 10,
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
    const list = data?.list || data?.records || []
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

// 打开添加地址弹窗
const showAddAddressModalHandler = () => {
  editingAddress.value = null
  addressForm.id = null
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.region = []
  addressForm.detailAddress = ''
  addressForm.isDefault = 0
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
  showAddAddressModal.value = true
}

// 关闭地址弹窗
const closeAddressModal = () => {
  showAddAddressModal.value = false
  editingAddress.value = null
}

// 保存地址
const saveAddress = async () => {
  if (!addressForm.receiverName || !addressForm.receiverPhone || !addressForm.region.length || !addressForm.detailAddress) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    loading.value = true
    const data = {
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      province: addressForm.region[0] || '',
      city: addressForm.region[1] || '',
      district: addressForm.region[2] || '',
      detailAddress: addressForm.detailAddress,
      isDefault: addressForm.isDefault
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
    passwordForm.oldPassword = ''
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
    phoneForm.password = ''
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
    emailForm.password = ''
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
  } catch (e) {
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
.profile-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: 100%;
  margin: 0;
  padding: 0;
}

.profile-content {
  flex: 1;
  display: flex;
  width: 100%;
  max-width: 100%;
  margin: 60px 0 0 0;
  padding: 0;
}

.sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #eee;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.05);
  margin: 0;
  padding: 0;
  position: fixed;
  left: 0;
  top: 60px;
  height: calc(100vh - 60px);
  z-index: 10;
  overflow-y: auto;
}

.side-menu {
  border-right: none;
}

.user-info {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.avatar-wrapper {
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  transition: transform 0.2s;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

/* 头像弹出菜单 */
.avatar-popover-menu {
  padding: 4px 0;
}

.popover-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
  font-size: 14px;
  color: #333;
}

.popover-item:hover {
  background-color: #f5f5f5;
  color: #ff4400;
}

.popover-item .el-icon {
  font-size: 16px;
}

/* ===== 个人资料卡片 ===== */
.profile-section {
  padding: 0;
}

.profile-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.profile-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28px 32px;
  border-bottom: 1px solid #f0f1f5;
  flex-wrap: wrap;
  gap: 16px;
}

.profile-avatar-area {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-frame {
  position: relative;
  border-radius: 50%;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-frame:hover .avatar-overlay {
  opacity: 1;
}

.profile-avatar {
  display: block;
  border: 3px solid #f0f1f5;
  transition: filter 0.2s;
}

.avatar-frame:hover .profile-avatar {
  filter: brightness(0.7);
}

.avatar-overlay {
  position: absolute;
  top: 3px;
  left: 3px;
  width: calc(100% - 6px);
  height: calc(100% - 6px);
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
  color: #fff;
  font-size: 11px;
  gap: 3px;
  line-height: 1.2;
}

.avatar-overlay .el-icon {
  font-size: 18px;
}

.avatar-name {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.profile-username {
  margin: 0;
  font-size: 20px;
  font-weight: 650;
  color: #1a1a2e;
  line-height: 1.3;
}

.profile-role-badge {
  display: inline-flex;
  align-items: center;
  font-size: 12px;
  font-weight: 500;
  color: #4361ee;
  background: #eef1ff;
  padding: 2px 12px;
  border-radius: 20px;
  width: fit-content;
  line-height: 22px;
}

.profile-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.profile-body {
  padding: 28px 32px 32px;
}

.profile-body .info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px 40px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cell-label {
  font-size: 12px;
  font-weight: 500;
  color: #8e8ea0;
  letter-spacing: 0.3px;
  text-transform: uppercase;
}

.cell-value {
  font-size: 15px;
  color: #1a1a2e;
  line-height: 1.5;
}

.cell-value.mono {
  font-family: 'SF Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 14px;
  color: #555;
}

/* 编辑资料弹窗 */
.edit-form {
  padding: 8px 0;
}

.user-details {
  margin-top: 15px;
}

.user-details h3 {
  margin: 10px 0 5px 0;
  color: #333;
  font-size: 18px;
}

.user-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.logout-item {
  color: #f56c6c;
}

.logout-item .el-icon {
  color: #f56c6c;
}

.main-content {
  flex: 1;
  padding: 0;
  margin: 0 0 0 250px;
  background: #f5f5f5;
}

.content-box {
  background: white;
  min-height: calc(100vh - 60px);
  padding: 30px;
  margin: 0;
}

/* ===== 订单概览 ===== */
.overview-section {
  max-width: 720px;
}

/* 欢迎横幅 */
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 14px;
  padding: 28px 32px;
  margin-bottom: 28px;
  color: #fff;
}

.banner-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.welcome-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
  color: #fff;
}

.welcome-sub {
  margin: 0;
  font-size: 14px;
  opacity: 0.85;
  color: #fff;
}

.banner-avatar {
  flex-shrink: 0;
}

.banner-avatar .el-avatar {
  border: 2px solid rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.15);
}

/* 统计卡片网格 */
.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border: 1px solid #eaecf0;
  border-radius: 14px;
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: box-shadow 0.2s, transform 0.15s, border-color 0.2s;
  cursor: default;
}

.stat-card.clickable {
  cursor: pointer;
}

.stat-card.clickable:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: #cdd0d7;
  transform: translateY(-2px);
}

.stat-card.clickable:active {
  transform: scale(0.98);
}

.stat-icon-wrap {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-icon-wrap.pending-pay {
  background: #fff3e0;
  color: #e67e22;
}

.stat-icon-wrap.pending-ship {
  background: #e3f2fd;
  color: #1976d2;
}

.stat-icon-wrap.pending-receive {
  background: #e8f5e9;
  color: #388e3c;
}

.stat-icon-wrap.refunding {
  background: #fce4ec;
  color: #c62828;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 32px;
  font-weight: 750;
  color: #1a1a2e;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 14px;
  font-weight: 500;
  color: #555;
}

.stat-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #f0f1f5;
  padding-top: 10px;
  margin-top: auto;
}

.stat-footer .el-icon {
  font-size: 14px;
  transition: transform 0.2s;
}

.stat-card.clickable:hover .stat-footer .el-icon {
  transform: translateX(3px);
}

/* 加载闪烁占位 */
.shimmer {
  display: inline-block;
  min-width: 28px;
  animation: pulse 1.2s ease-in-out infinite;
  color: #ccc;
}
@keyframes pulse {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* 快捷入口 */
.quick-links {
  margin-bottom: 20px;
}

.section-title {
  margin: 0 0 14px 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.quick-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 18px 10px;
  background: #f8f9fb;
  border: 1px solid #eaecf0;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s, transform 0.15s;
  font-size: 13px;
  font-weight: 500;
  color: #444;
}

.quick-card:hover {
  background: #eef0f6;
  border-color: #cdd0d7;
  transform: translateY(-1px);
}

.quick-card:active {
  transform: scale(0.97);
}

.quick-card .q-icon {
  font-size: 22px;
  color: #667eea;
}

.address-section h3,
.security-section h3,
.default-section h3,
.orders-section h3,
.cart-section h3 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 24px;
}

/* ===== 订单区域 ===== */
.orders-section {
}

.order-header {
  margin-bottom: 24px;
}

.order-header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.order-header-top h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  letter-spacing: -0.01em;
}

.order-total-count {
  font-size: 13px;
  color: #8e8ea0;
  background: #f2f3f5;
  padding: 2px 12px;
  border-radius: 12px;
  line-height: 22px;
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

.order-loading {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-skeleton {
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #eaecf0;
}

/* ===== 订单卡片 ===== */
.order-list {
}

.order-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e0e3e8;
  cursor: pointer;
  transition: box-shadow 0.25s, border-color 0.25s, transform 0.2s;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.03);
}

.order-card:hover {
  border-color: #c8ccd3;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06), 0 2px 4px rgba(0, 0, 0, 0.04);
}

/* --- 卡片头部 --- */
.order-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 22px;
  background: #f4f5f8;
  border-bottom: 1px solid #e6e9ef;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.order-time {
  font-size: 13px;
  color: #8e8ea0;
  white-space: nowrap;
  flex-shrink: 0;
}

.order-no {
  font-size: 13px;
  color: #6b6b80;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.02em;
}

/* 状态徽章 */
.status-pill {
  display: inline-block;
  padding: 3px 14px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 22px;
}

.pill-1 {
  background: #fff5e6;
  color: #d4850b;
}

.pill-2 {
  background: #eef2ff;
  color: #4a6cf7;
}

.pill-3 {
  background: #e6f4ff;
  color: #1a7bc4;
}

.pill-4 {
  background: #e8f8e8;
  color: #2b8a3e;
}

.pill-5 {
  background: #fef0ef;
  color: #c2413a;
}

.pill-6 {
  background: #fff5e6;
  color: #d4850b;
}
.pill-7 {
  background: #e8f8e8;
  color: #2b8a3e;
}
.pill-8 {
  background: #fef0ef;
  color: #c2413a;
}

/* --- 卡片主体 --- */
.order-card-body {
  padding: 16px 22px;
  background: #fafbfc;
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 14px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #edf0f4;
  transition: background 0.15s;
}

.order-item:hover {
  background: #f0f2f6;
}

.item-img-wrap {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f2f5;
  border: 1px solid #eaecf0;
}

.item-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-specs {
  font-size: 12px;
  color: #8e8ea0;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  text-align: right;
  flex-shrink: 0;
  min-width: 70px;
}

.item-price {
  font-size: 15px;
  color: #1a1a2e;
  font-weight: 500;
}

.item-qty {
  font-size: 12px;
  color: #8e8ea0;
  margin-left: 3px;
}

.item-subtotal {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}

.order-summary {
  font-size: 13px;
  color: #8e8ea0;
}

.order-remark {
  margin-top: 8px;
  font-size: 12px;
  color: #6b6b80;
  padding: 6px 10px;
  background: #f8f9fb;
  border-radius: 6px;
  display: inline-block;
}

/* --- 卡片底部 --- */
.order-card-footer {
  border-top: 1px solid #e6e9ef;
  background: #f4f5f8;
  padding: 14px 22px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.footer-left {
  flex: 1;
  min-width: 0;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  line-height: 1.4;
  padding: 4px 10px;
  border-radius: 6px;
}

.badge-icon {
  font-size: 14px;
}

.status-expire {
  color: #d4850b;
  background: #fff9f0;
}

.status-refund {
  color: #2b8a3e;
  background: #f0faf0;
  font-weight: 500;
}

.status-delivery {
  color: #4a6cf7;
  background: #f0f3ff;
}

.status-cancel {
  color: #8e8ea0;
  background: #f5f5f6;
}

.status-pending-ship {
  color: #4a6cf7;
  background: #f0f3ff;
}

/* --- 右侧 --- */
.footer-right {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-shrink: 0;
}

.amount-summary {
  text-align: right;
}

.amount-row {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: flex-end;
  line-height: 1.6;
}

.amount-label {
  font-size: 12px;
  color: #8e8ea0;
}

.amount-value {
  font-size: 13px;
  color: #1a1a2e;
  font-weight: 500;
  min-width: 70px;
  text-align: right;
}

.discount-row .amount-value.discount {
  color: #2b8a3e;
}

.pay-row {
  margin-top: 1px;
}

.pay-row .amount-value.pay {
  font-size: 17px;
  font-weight: 700;
  color: #ff4400;
}

/* 操作按钮 */
.order-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 16px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s;
  white-space: nowrap;
  background: none;
  font-family: inherit;
}

.action-btn:active {
  transform: scale(0.97);
}

.action-btn.primary {
  background: #ff4400;
  color: #fff;
  border-color: #ff4400;
}

.action-btn.primary:hover {
  background: #e63d00;
  border-color: #e63d00;
}

.action-btn.secondary {
  background: #fff;
  color: #6b6b80;
  border-color: #d0d5dd;
}

.action-btn.secondary:hover {
  border-color: #ff4400;
  color: #ff4400;
}

.action-btn.ghost {
  background: transparent;
  color: #6b6b80;
  border-color: #d0d5dd;
}

.action-btn.ghost:hover {
  border-color: #6b6b80;
  color: #1a1a2e;
}

.action-btn.ghost.danger {
  color: #c2413a;
}

.action-btn.ghost.danger:hover {
  border-color: #c2413a;
  color: #c2413a;
}

.status-tip {
  font-size: 13px;
  color: #8e8ea0;
}

.status-tip.refund-done {
  color: #2b8a3e;
  font-weight: 500;
}

/* ===== 分页 ===== */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 8px 0 4px;
}

.pagination-wrap :deep(.el-pagination) {
  font-weight: 400;
}

.pagination-wrap :deep(.el-pagination .el-pager li) {
  border-radius: 6px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  font-size: 13px;
}

.pagination-wrap :deep(.el-pagination button) {
  height: 32px;
  min-width: 32px;
  border-radius: 6px;
}

.pagination-wrap :deep(.el-pagination .el-pagination__sizes) {
  font-size: 13px;
}

.pagination-wrap :deep(.el-pagination .el-select .el-input__wrapper) {
  font-size: 13px;
}

/* ===== 空状态 ===== */
.empty-orders {
  padding: 60px 0;
  text-align: center;
}

/* ===== 详情弹窗 ===== */
/* ===== 订单详情弹窗 ===== */
.detail-dialog :deep(.el-dialog__header) {
  padding: 20px 28px 0;
}
.detail-dialog :deep(.el-dialog__body) {
  padding: 16px 28px;
}
.detail-body {
  min-height: 200px;
}

/* 头部状态条 */
.detail-dialog-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.detail-status-pill {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.6;
}
.sp-1 { background: #fff7e6; color: #d4850b; }
.sp-2 { background: #f0f5ff; color: #2b6af0; }
.sp-3 { background: #e6f7ff; color: #0984ce; }
.sp-4 { background: #f0fff0; color: #389e0d; }
.sp-5 { background: #fff2f0; color: #cf1322; }
.sp-6 { background: #fff7e6; color: #d4850b; }
.sp-7 { background: #f0fff0; color: #389e0d; }
.sp-8 { background: #fff2f0; color: #cf1322; }
.detail-order-no {
  font-size: 13px;
  color: #8e8ea0;
  font-family: 'SF Mono', 'Consolas', monospace;
}

/* 商品列表行 */
.detail-items-section {
  margin-bottom: 16px;
}
.detail-sec-title {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 3px solid #ff4400;
}
.detail-item-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #f8f9fb;
  border-radius: 8px;
  margin-bottom: 6px;
}
.detail-item-row:last-child {
  margin-bottom: 0;
}
.detail-item-img {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #eaecf0;
  background: #fff;
}
.detail-item-info {
  flex: 1;
  min-width: 0;
}
.detail-item-name {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.detail-item-specs {
  font-size: 12px;
  color: #8e8ea0;
  margin-top: 2px;
}
.detail-item-price,
.detail-item-qty {
  font-size: 13px;
  color: #6b6b80;
  flex-shrink: 0;
}
.detail-item-subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a2e;
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}

/* 金额汇总 */
.detail-amounts {
  background: #f8f9fb;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 20px;
}
.amt-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #6b6b80;
  padding: 3px 0;
}
.amt-discount {
  color: #389e0d;
}
.amt-divider {
  height: 1px;
  background: #eaecf0;
  margin: 6px 0;
}
.amt-pay {
  font-size: 15px;
  font-weight: 700;
  color: #ff4400;
}

/* 信息网格 */
.detail-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 20px;
  margin-bottom: 20px;
}
.info-cell {
  display: flex;
  flex-direction: column;
  gap: 1px;
}
.info-cell.full {
  grid-column: 1 / -1;
}
.icl {
  font-size: 12px;
  color: #8e8ea0;
}
.icv {
  font-size: 13px;
  color: #1a1a2e;
  word-break: break-all;
}
.icv.mono {
  font-family: 'SF Mono', 'Consolas', monospace;
  letter-spacing: 0.02em;
}
.icv.expire {
  color: #d4850b;
}
.icv.muted {
  color: #8e8ea0;
}
.icv.refund {
  color: #389e0d;
  font-weight: 600;
}
.icv.rejected {
  color: #cf1322;
}

/* 发货时间线 */
.detail-deliveries {
  padding-left: 8px;
  margin-bottom: 4px;
}
.del-timeline-row {
  position: relative;
  padding-left: 24px;
  padding-bottom: 20px;
}
.del-timeline-row:last-child {
  padding-bottom: 0;
}
.del-timeline-dot {
  position: absolute;
  left: 0;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #409eff;
  border: 2px solid #d6e4ff;
  z-index: 1;
}
.del-timeline-line {
  position: absolute;
  left: 4px;
  top: 16px;
  width: 2px;
  bottom: -2px;
  background: #eaecf0;
}
.del-content {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.del-company {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
}
.del-no {
  font-size: 13px;
  color: #409eff;
  font-family: 'SF Mono', 'Consolas', monospace;
}
.del-time {
  font-size: 12px;
  color: #8e8ea0;
}

/* 地址管理样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.address-card {
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  transition: all 0.2s;
}

.address-card:hover {
  border-color: #ff4400;
  box-shadow: 0 2px 8px rgba(255, 68, 0, 0.1);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.address-user {
  display: flex;
  gap: 15px;
}

.address-user .name {
  font-weight: 600;
  color: #333;
}

.address-user .phone {
  color: #666;
  font-size: 14px;
}

.default-tag {
  padding: 2px 8px;
  background: #ff4400;
  color: white;
  font-size: 12px;
  border-radius: 4px;
}

.address-detail {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 10px;
}

.address-actions .el-button {
  padding: 4px 12px;
  font-size: 12px;
}

.empty-address {
  padding: 60px 0;
  text-align: center;
}

.address-limit {
  margin-top: 15px;
  color: #999;
  font-size: 13px;
}

/* 账户安全样式 */
.security-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.security-icon {
  font-size: 32px;
  margin-right: 20px;
}

.security-info {
  flex: 1;
}

.security-info h4 {
  margin: 0 0 5px 0;
  color: #333;
  font-size: 15px;
}

.security-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.default-section {
  text-align: center;
  padding: 100px 0;
}

.default-section p {
  color: #666;
  font-size: 16px;
  margin-top: 10px;
}

/* ===== 订单弹窗通用 ===== */
.dialog-body {
  padding: 8px 0;
}

.dialog-tip {
  font-size: 14px;
  color: #606266;
  margin: 0 0 16px;
}

.dialog-tip.warn {
  color: #e6a23c;
}

.full-select {
  width: 100%;
}

/* ===== 退款弹窗 ===== */
.refund-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 0;
}

.refund-dialog :deep(.el-dialog__body) {
  padding: 16px 24px;
}

.refund-dialog :deep(.el-dialog__footer) {
  padding: 0 24px 20px;
}

.refund-dialog-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.refund-dialog-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fff5e6;
  color: #d4850b;
  font-size: 18px;
  flex-shrink: 0;
}

.refund-dialog-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
  line-height: 1.4;
}

.refund-dialog-sub {
  font-size: 12px;
  color: #8e8ea0;
  margin-top: 2px;
}

.refund-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 订单信息卡片 */
.refund-order-info {
  background: #f8f9fb;
  border: 1px solid #edf0f4;
  border-radius: 10px;
  padding: 14px;
}

.refund-order-no {
  font-size: 12px;
  color: #6b6b80;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  margin-bottom: 10px;
  letter-spacing: 0.02em;
}

.refund-items-preview {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.refund-item-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.refund-item-img {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #eaecf0;
  background: #fff;
}

.refund-item-detail {
  flex: 1;
  min-width: 0;
}

.refund-item-name {
  font-size: 13px;
  color: #1a1a2e;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.refund-item-specs {
  font-size: 11px;
  color: #8e8ea0;
  margin-top: 1px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.refund-item-qty {
  font-size: 12px;
  color: #8e8ea0;
  flex-shrink: 0;
}

.refund-more-items {
  font-size: 12px;
  color: #8e8ea0;
  padding-left: 54px;
}

.refund-amount-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #edf0f4;
}

.refund-amount-label {
  font-size: 13px;
  color: #8e8ea0;
}

.refund-amount-value {
  font-size: 16px;
  font-weight: 700;
  color: #ff4400;
}

/* 退款原因 */
.refund-reason-section {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.refund-reason-label {
  font-size: 13px;
  font-weight: 500;
  color: #1a1a2e;
}

.refund-reason-select {
  width: 100%;
}

/* 底部按钮 */
.refund-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-loading-icon {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-right: 4px;
  vertical-align: middle;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== 详情弹窗 ===== */
.detail-wrap {
  min-height: 200px;
}

/* 状态横幅 */
.detail-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 22px;
  border-radius: 12px;
  margin-bottom: 22px;
}

.banner-1 { background: linear-gradient(135deg, #fff7e6, #fff1cc); }
.banner-2 { background: linear-gradient(135deg, #f0f5ff, #e6f0ff); }
.banner-3 { background: linear-gradient(135deg, #e6f7ff, #bae7ff); }
.banner-4 { background: linear-gradient(135deg, #f0fff0, #d9f7d9); }
.banner-5 { background: linear-gradient(135deg, #fff2f0, #ffd6d2); }
.banner-6 { background: linear-gradient(135deg, #fff7e6, #ffe7ba); }
.banner-7 { background: linear-gradient(135deg, #f0fff0, #d9f7d9); }

.banner-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.banner-icon {
  font-size: 32px;
  color: #409eff;
}

.banner-1 .banner-icon { color: #e6a23c; }
.banner-5 .banner-icon { color: #f56c6c; }
.banner-6 .banner-icon { color: #e6a23c; }
.banner-4 .banner-icon,
.banner-7 .banner-icon { color: #67c23a; }

.banner-text p {
  margin: 0;
}

.banner-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.banner-sub {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

.banner-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.banner-pay-type {
  font-size: 12px;
  color: #909399;
}

/* 区块标题（带图标） */
.detail-section {
  margin-bottom: 22px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.section-icon {
  font-size: 18px;
  color: #409eff;
}

.section-title h3 {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

/* 商品明细 */
.detail-items {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: #f8f9fb;
  border-radius: 10px;
  transition: background 0.15s;
}

.detail-item:hover {
  background: #f0f2f5;
}

.d-item-img-wrap {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f2f5;
  border: 1px solid #eaecf0;
}

.d-item-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.d-item-info {
  flex: 1;
  min-width: 0;
}

.d-item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  line-height: 1.4;
}

.d-item-specs {
  font-size: 12px;
  color: #909399;
  margin-top: 3px;
}

.d-item-price {
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.d-item-qty {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
  min-width: 24px;
}

.d-item-subtotal {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  width: 90px;
  text-align: right;
  flex-shrink: 0;
}

/* 信息网格 */
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: baseline;
  padding: 0 4px;
}

.info-row .label {
  width: 80px;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.info-row .value {
  font-size: 13px;
  color: #303133;
  flex: 1;
}

.info-row .value.mono {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
}

.info-row.total-row {
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  margin-top: 2px;
}

.info-row .value.pay-amount {
  font-size: 17px;
  font-weight: 700;
  color: #ff4400;
}

.info-row .value.discount {
  color: #67c23a;
}

/* 发货记录 */
.deliveries {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.delivery-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.delivery-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  margin-top: 4px;
  flex-shrink: 0;
  box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
}

.delivery-info-text {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 12px;
  font-size: 13px;
  color: #606266;
}

.delivery-info-text .company {
  font-weight: 500;
  color: #303133;
}

.delivery-info-text .tracking {
  font-family: 'SF Mono', 'Fira Code', 'Consolas', monospace;
  color: #606266;
}

.delivery-info-text .delivery-time {
  color: #909399;
  font-size: 12px;
}

/* 详情弹窗专用样式 */
.order-detail-dialog .el-dialog__header {
  padding: 18px 24px 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-detail-dialog .el-dialog__title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a2e;
}

.order-detail-dialog .el-dialog__body {
  padding: 20px 24px;
}

.order-detail-dialog .el-dialog__footer {
  padding: 12px 24px 18px;
}

/* 密码切换按钮样式 */
.password-toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: color 0.2s;
}

.password-toggle-btn:hover {
  color: #ff4400;
}

.password-toggle-btn:focus {
  outline: none;
}

.eye-icon {
  width: 18px;
  height: 18px;
}

/* 移动端适配 */
@media (max-width: 767px) {
  .profile-content {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #eee;
    position: relative;
    top: auto;
    left: auto;
    height: auto;
    z-index: auto;
    overflow-y: visible;
  }
  
  .main-content {
    margin-left: 0;
  }
  
  .content-box {
    padding: 20px;
    min-height: auto;
  }
  
  .user-info {
    padding: 20px 15px;
  }
  
  .address-list {
    grid-template-columns: 1fr;
  }
  
  .cart-item {
    flex-wrap: wrap;
    gap: 10px;
  }
  
  .item-info {
    min-width: auto;
    flex: 1;
  }
  
  .address-section h3,
  .security-section h3,
  .default-section h3,
  .orders-section h3,
  .cart-section h3 {
    font-size: 20px;
  }
  
  .default-section {
    padding: 50px 0;
  }
}

/* 平板端适配 */
@media (min-width: 768px) and (max-width: 1023px) {
  .content-box {
    padding: 25px;
  }
  
  .address-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

/* PC端适配 */
@media (min-width: 1024px) {
  .content-box {
    padding: 30px 40px;
  }
}
</style>