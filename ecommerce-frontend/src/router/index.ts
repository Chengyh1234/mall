import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore, UserRole } from '@/stores/user'

// 页面组件（懒加载）
const Login = () => import('@/views/LoginView.vue')
const Home = () => import('@/views/HomeView.vue')
const ProductList = () => import('@/views/ProductListView.vue')
const ProductDetail = () => import('@/views/ProductDetailView.vue')
const Cart = () => import('@/views/CartView.vue')
const Profile = () => import('@/views/ProfileView.vue')
const OrderList = () => import('@/views/OrderListView.vue')
const OrderConfirm = () => import('@/views/OrderConfirmView.vue')
const Test = () => import('@/views/TestView.vue')

// 卖家页面
const SellerLayout = () => import('@/views/seller/SellerLayout.vue')
const SellerHome = () => import('@/views/seller/SellerHome.vue')
const SellerProducts = () => import('@/views/seller/SellerProducts.vue')
const SellerOrders = () => import('@/views/seller/SellerOrders.vue')
const SellerProfile = () => import('@/views/seller/SellerProfile.vue')

// 管理员页面
const AdminLogin = () => import('@/views/admin/AdminLoginView.vue')
const AdminLayout = () => import('@/views/admin/AdminLayout.vue')
const AdminHome = () => import('@/views/admin/AdminHome.vue')
const AdminUsers = () => import('@/views/admin/AdminUsers.vue')
const AdminProducts = () => import('@/views/admin/AdminProducts.vue')
const AdminOrders = () => import('@/views/admin/AdminOrders.vue')
const AdminShops = () => import('@/views/admin/AdminShops.vue')
const BannerManage = () => import('@/views/admin/BannerManage.vue')
const AdminAttributes = () => import('@/views/admin/AdminAttributes.vue')
const AdminCategoryAttributes = () => import('@/views/admin/AdminCategoryAttributes.vue')
const AdminCategories = () => import('@/views/admin/AdminCategories.vue')

// 客服页面
const CsHome = () => import('@/views/cs/CsHome.vue')
const CsOrders = () => import('@/views/cs/CsOrders.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'login',
    component: Login,
    meta: { requiresAuth: false }
  },
  // 隐藏路径：管理员登录入口（不对外公开，无导航链接）
  {
    path: '/admin/login',
    name: 'admin-login',
    component: AdminLogin,
    meta: { requiresAuth: false }
  },
  {
    path: '/home',
    name: 'home',
    component: Home,
    meta: { requiresAuth: false }
  },
  {
    path: '/products',
    name: 'products',
    component: ProductList,
    meta: { requiresAuth: false }
  },
  {
    path: '/product/:id',
    name: 'product-detail',
    component: ProductDetail,
    meta: { requiresAuth: false }
  },
  {
    path: '/cart',
    name: 'cart',
    component: Cart,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'profile',
    component: Profile,
    meta: { requiresAuth: true, roles: [UserRole.USER] },
    beforeEnter: (to, from, next) => {
      const userStore = useUserStore()
      if (userStore.isLoggedIn && !userStore.hasRole(UserRole.USER)) {
        next('/login')
      } else {
        next()
      }
    }
  },
  {
    path: '/orders',
    name: 'orders',
    component: OrderList,
    meta: { requiresAuth: true }
  },
  {
    path: '/test',
    name: 'test',
    component: Test,
    meta: { requiresAuth: false }
  },
  {
    path: '/order/confirm',
    name: 'order-confirm',
    component: OrderConfirm,
    meta: { requiresAuth: true }
  },
  // 卖家路由（嵌套布局）
  {
    path: '/seller',
    component: SellerLayout,
    children: [
      {
        path: '',
        name: 'seller',
        component: SellerHome,
        meta: { requiresAuth: true, roles: [UserRole.SELLER] }
      },
      {
        path: 'products',
        name: 'seller-products',
        component: SellerProducts,
        meta: { requiresAuth: true, roles: [UserRole.SELLER] }
      },
      {
        path: 'orders',
        name: 'seller-orders',
        component: SellerOrders,
        meta: { requiresAuth: true, roles: [UserRole.SELLER] }
      },
      {
        path: 'profile',
        name: 'seller-profile',
        component: SellerProfile,
        meta: { requiresAuth: true, roles: [UserRole.SELLER] }
      }
    ]
  },
  // 管理员路由（嵌套布局）
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'admin',
        component: AdminHome,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'users',
        name: 'admin-users',
        component: AdminUsers,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'products',
        name: 'admin-products',
        component: AdminProducts,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'orders',
        name: 'admin-orders',
        component: AdminOrders,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'shops',
        name: 'admin-shops',
        component: AdminShops,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'banners',
        name: 'admin-banners',
        component: BannerManage,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'attributes',
        name: 'admin-attributes',
        component: AdminAttributes,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'category-attributes',
        name: 'admin-category-attributes',
        component: AdminCategoryAttributes,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      },
      {
        path: 'categories',
        name: 'admin-categories',
        component: AdminCategories,
        meta: { requiresAuth: true, roles: [UserRole.ADMIN, UserRole.OPERATOR] }
      }
    ]
  },
  // 客服路由
  {
    path: '/cs',
    name: 'cs',
    component: CsHome,
    meta: { requiresAuth: true, roles: [UserRole.CUSTOMER_SERVICE] }
  },
  {
    path: '/cs/orders',
    name: 'cs-orders',
    component: CsOrders,
    meta: { requiresAuth: true, roles: [UserRole.CUSTOMER_SERVICE] }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach(async (to, from) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn
  const routeMeta = to.meta as {
    requiresAuth?: boolean
    roles?: string[]
  }

  // 如果需要认证但未登录，重定向到登录页
  if (routeMeta?.requiresAuth && !isLoggedIn) {
    return '/login'
  }

  // 如果已登录访问登录页，根据角色重定向
  if (to.name === 'login' && isLoggedIn) {
    // 确保用户信息已加载以判断角色
    if (!userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch {
        return '/home'
      }
    }
    return userStore.isAdmin ? '/admin' : '/home'
  }

  // 管理员访问首页时重定向到管理后台
  if ((to.name === 'home' || to.path === '/') && isLoggedIn && userStore.isAdmin) {
    return '/admin'
  }

  // 角色权限检查
  if (routeMeta?.roles && routeMeta.roles.length > 0) {
    // 已登录但用户信息尚未加载（如刷新页面），先获取用户信息再判断权限
    if (isLoggedIn && !userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch {
        return '/home'
      }
    }

    const hasRequiredRole = routeMeta.roles.some(role => {
      if (role === UserRole.SELLER || role === UserRole.ROLE_SELLER) {
        return userStore.isSeller
      }
      return userStore.hasRole(role)
    })
    
    if (!hasRequiredRole) {
      return '/home'
    }
  }
})

export default router
