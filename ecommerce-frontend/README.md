# Vue3 + Element Plus 电商平台前台商城

基于Vue3 + Vite + Element Plus + Axios开发的电商平台前台商城项目。

## 技术栈

- **前端框架**: Vue3 + 组合式API
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **请求库**: Axios
- **路由**: Vue Router 4.x
- **状态管理**: Pinia

## 项目结构

```
src/
├── api/                    # API接口
│   ├── user.js            # 用户相关接口
│   ├── product.js         # 商品相关接口
│   ├── cart.js            # 购物车相关接口
│   └── order.js           # 订单相关接口
├── assets/                # 静态资源
├── components/            # 公共组件
├── router/                # 路由配置
├── stores/                # Pinia状态管理
│   └── user.js           # 用户状态管理
├── utils/                 # 工具函数
│   └── request.js        # Axios封装
└── views/                 # 页面组件
    ├── LoginView.vue     # 登录页面
    ├── HomeView.vue      # 首页
    ├── ProfileView.vue   # 个人中心
    ├── ProductListView.vue # 商品列表
    ├── ProductDetailView.vue # 商品详情
    ├── CartView.vue      # 购物车
    ├── OrderListView.vue # 订单列表
    └── TestView.vue      # 测试页面
```

## 后端对接规范

### 接口地址
- 后端统一接口地址：`http://localhost:8888/api`
- Vite代理配置：`/api` -> `http://localhost:8888/api`

### 响应格式
```json
{
  "code": 200,
  "message": "提示信息",
  "data": "业务数据"
}
```

### 状态码
- `200`: 成功
- `401`: 登录失效/未授权
- `403`: 权限不足

## 功能特性

### 1. Axios封装
- 统一请求/响应拦截器
- 自动Token管理
- 统一错误处理
- 使用ElMessage提示

### 2. 权限与登录
- Token存储：localStorage
- 路由守卫：未登录自动拦截
- 自动携带Token
- 登录/退出功能

### 3. 页面功能
- **登录页面**: 表单验证、登录逻辑
- **首页**: 轮播图、热门商品推荐
- **个人中心**: 用户信息展示、订单管理
- **商品列表**: 商品展示、分类筛选
- **商品详情**: 商品详细信息
- **购物车**: 商品管理、结算
- **订单列表**: 订单状态管理

## 快速开始

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

## 配置说明

### Vite配置
- 代理配置：`/api` -> `http://localhost:8888/api`
- Element Plus自动导入
- TypeScript支持

### 环境变量
项目使用Vite环境变量，可在`.env`文件中配置。

## 开发说明

### 接口调用示例
```javascript
import { login } from '@/api/user'

// 登录
const handleLogin = async () => {
  try {
    const token = await login({
      username: 'admin',
      password: '123456'
    })
    console.log('登录成功:', token)
  } catch (error) {
    console.error('登录失败:', error)
  }
}
```

### 状态管理示例
```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 获取用户信息
const userInfo = computed(() => userStore.userInfo)

// 检查登录状态
const isLoggedIn = computed(() => userStore.isLoggedIn)
```

## 注意事项

1. 项目需要后端API支持，默认代理到`http://localhost:8888`
2. 登录功能需要后端提供`/api/user/login`接口
3. 所有需要权限的接口会自动携带Token
4. 路由守卫会自动拦截未登录的访问

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Edge >= 88
- Safari >= 14