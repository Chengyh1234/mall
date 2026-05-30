# 电商商城 API 接口文档

---

## 📚 目录结构

- [一、认证模块 (Auth)](#一认证模块-auth)
- [二、用户模块 (User)](#二用户模块-user)
- [三、收货地址模块 (Address)](#三收货地址模块-address)
- [四、购物车模块 (Cart)](#四购物车模块-cart)
- [五、订单模块 (Order)](#五订单模块-order)
- [六、发货记录模块 (Delivery)](#六发货记录模块-delivery)
- [七、物流公司模块 (Logistics)](#七物流公司模块-logistics)
- [八、商品模块 (Spu)](#八商品模块-spu)
- [九、SKU模块 (Sku)](#九sku模块-sku)
- [十、品牌模块 (Brand)](#十品牌模块-brand)
- [十一、分类模块 (Category)](#十一分类模块-category)
- [十二、属性模块 (Attribute)](#十二属性模块-attribute)
- [十三、商家SPU属性管理模块 (SpuAttr)](#十三商家spu属性管理模块-spuattr)
- [十四、商家SKU属性管理模块 (SkuAttr)](#十四商家sku属性管理模块-skuattr)
- [十五、文件模块 (File)](#十五文件模块-file)
- [十六、店铺模块 (Store)](#十六店铺模块-store)
- [十七、店铺管理员模块 (StoreAdmin)](#十七店铺管理员模块-storeadmin)
- [十八、商家仪表盘模块 (StoreDashboard)](#十八商家仪表盘模块-storedashboard)

---

## 🔐 权限说明

| 权限标识 | 说明 | 适用角色 |
|---------|------|---------|
| `isAuthenticated()` | 需要登录 | 登录用户 |
| `hasAuthority('xxx')` | 需要精确权限 | 指定权限 |
| `hasRole('xxx')` | 需要角色 | 指定角色 |
| `SUPER_ADMIN` | 超级管理员 | 系统最高权限 |
| `ADMIN` | 管理员 | 系统管理 |
| `SELLER` | 商家 | 商品管理 |

---

## 📋 通用状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或token无效 |
| 403 | 权限不足 |
| 500 | 服务器内部错误 |

---

## 一、认证模块 (Auth)

### 1.1 用户登录

**接口路径：** `/auth/login`
**HTTP方法：** POST
**权限：** 公开
**功能说明：** 用户登录获取Token

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名/手机号/邮箱 | `user@example.com` |
| password | String | 是 | 密码 | `123456` |

#### 请求示例
```json
{
    "username": "user@example.com",
    "password": "123456"
}
```

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| token | String | JWT Token | `eyJhbGciOiJIUzI1NiJ9...` |
| userId | Long | 用户ID | `1` |
| username | String | 用户名 | `user` |
| roles | List | 用户角色列表 | `["USER"]` |
| permissions | List | 用户权限列表 | `["order:list"]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "登录成功",
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "userId": 1,
        "username": "user",
        "roles": ["USER"],
        "permissions": ["order:list", "cart:add"]
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "用户名或密码错误",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户名或密码错误 | 登录凭证不正确 |
| 未登录 | 用户未登录或Token已过期 |

---

### 1.2 用户注册

**接口路径：** `/auth/register`
**HTTP方法：** POST
**权限：** 公开
**功能说明：** 新用户注册

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| username | String | 是 | 用户名 | `newuser` |
| password | String | 是 | 密码（6-20位） | `123456` |
| phone | String | 否 | 手机号 | `13800138000` |
| email | String | 否 | 邮箱 | `user@example.com` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 用户ID | `1` |
| username | String | 用户名 | `newuser` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "注册成功",
    "data": {
        "id": 1,
        "username": "newuser"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "用户名已存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户名已存在 | 该用户名已被注册 |

---

### 1.3 用户登出

**接口路径：** `/auth/logout`
**HTTP方法：** POST
**权限：** 需认证 (isAuthenticated)
**功能说明：** 用户登出

#### 请求参数

无

#### 响应示例
```json
{
    "code": 200,
    "msg": "登出成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "未登录",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 未登录 | 用户未登录或Token已过期 |

---

### 1.4 获取当前用户信息

**接口路径：** `/auth/info`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取当前登录用户信息

#### 请求参数

无（从Token中解析用户信息）

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 用户ID | `1` |
| username | String | 用户名 | `user` |
| phone | String | 手机号 | `13800138000` |
| email | String | 邮箱 | `user@example.com` |
| avatar | String | 头像URL | `/uploads/avatar/1.jpg` |
| roles | List | 角色列表 | `["USER"]` |
| permissions | List | 权限列表 | `["cart:add", "order:list"]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "username": "user",
        "phone": "13800138000",
        "email": "user@example.com",
        "avatar": "/uploads/avatar/1.jpg",
        "roles": ["USER"],
        "permissions": ["cart:add", "order:list"]
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "未登录",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 未登录 | 用户未登录或Token已过期 |

---

## 二、用户模块 (User)

### 2.1 获取当前用户信息

**接口路径：** `/user/profile`
**HTTP方法：** GET
**权限：** isAuthenticated()
**功能说明：** 获取当前登录用户的详细信息

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 用户ID | `1` |
| username | String | 用户名 | `user1` |
| email | String | 邮箱 | `user1@test.com` |
| phone | String | 手机号 | `13800138003` |
| avatar | String | 头像URL | `/uploads/avatars/xxx.jpg` |
| realName | String | 真实姓名 | `王五` |
| status | Integer | 状态（1-正常 0-禁用） | `1` |
| lastLoginTime | DateTime | 最后登录时间 | `2026-05-15 16:53:43` |
| createdAt | DateTime | 注册时间 | `2026-04-30 22:35:35` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 4,
        "username": "user1",
        "email": "user1@test.com",
        "phone": "13800138003",
        "avatar": "/uploads/avatars/xxx.jpg",
        "realName": "王五",
        "status": 1,
        "lastLoginTime": "2026-05-15 16:53:43",
        "createdAt": "2026-04-30 22:35:35"
    }
}
```

---

### 2.2 更新用户信息

**接口路径：** `/user/profile`
**HTTP方法：** PUT
**权限：** isAuthenticated()
**功能说明：** 更新当前用户的真实姓名、邮箱、手机号（邮箱和手机号会校验唯一性）。更新邮箱或手机号时需要验证密码。

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| realName | String | 否 | 真实姓名 | `王五` |
| email | String | 否 | 邮箱（更新时需要密码验证） | `user1@test.com` |
| phone | String | 否 | 手机号（更新时需要密码验证） | `13800138003` |
| password | String | 否 | 当前密码（更新邮箱或手机号时必填） | `123456` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": {
        "id": 4,
        "username": "user1",
        "email": "user1@test.com",
        "phone": "13800138003",
        "realName": "王五",
        "avatar": "/uploads/avatars/xxx.jpg",
        "status": 1
    }
}
```

#### 错误响应示例

**邮箱格式错误：**
```json
{
    "code": 500,
    "msg": "邮箱格式不正确",
    "data": null
}
```

**手机号格式错误：**
```json
{
    "code": 500,
    "msg": "手机号格式不正确，必须是11位数字",
    "data": null
}
```

**更新敏感信息未输入密码：**
```json
{
    "code": 500,
    "msg": "更新邮箱或手机号需要输入密码进行验证",
    "data": null
}
```

**密码验证失败：**
```json
{
    "code": 500,
    "msg": "密码验证失败",
    "data": null
}
```

**邮箱已被使用：**
```json
{
    "code": 500,
    "msg": "该邮箱已被其他用户使用",
    "data": null
}
```

**手机号已被使用：**
```json
{
    "code": 500,
    "msg": "该手机号已被其他用户使用",
    "data": null
}
```

---

### 2.3 修改密码

**接口路径：** `/user/password`
**HTTP方法：** PUT
**权限：** isAuthenticated()
**功能说明：** 修改当前用户的登录密码（需提供旧密码）

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| oldPassword | String | 是 | 旧密码 | `123456` |
| newPassword | String | 是 | 新密码（最少6位） | `654321` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "密码修改成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "新密码长度不能少于6位",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 新密码长度不能少于6位 | 新密码不符合长度要求 |
| 密码修改失败 | 密码修改操作失败 |

---

### 2.4 上传头像

**接口路径：** `/user/avatar`
**HTTP方法：** POST
**权限：** isAuthenticated()
**Content-Type：** multipart/form-data
**功能说明：** 上传并更新当前用户的头像

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| file | File | 是 | 头像图片文件（支持jpg/png/gif/webp） | |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| avatar | String | 头像相对路径 | `2026/05/15/uuid_avatar.jpg` |
| avatarUrl | String | 头像完整URL | `/uploads/avatars/2026/05/15/uuid_avatar.jpg` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "头像更新成功",
    "data": {
        "avatar": "2026/05/15/uuid_avatar.jpg",
        "avatarUrl": "/uploads/avatars/2026/05/15/uuid_avatar.jpg"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "头像上传失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 头像上传失败 | 头像文件上传过程中发生错误 |

---

## 三、收货地址模块 (Address)

### 3.1 添加收货地址

**接口路径：** `/address/add`
**HTTP方法：** POST
**权限：** 需认证 (isAuthenticated)
**功能说明：** 添加新的收货地址

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| receiverName | String | 是 | 收货人姓名 | `张三` |
| receiverPhone | String | 是 | 收货人电话 | `13800138000` |
| province | String | 是 | 省份 | `广东省` |
| city | String | 是 | 城市 | `深圳市` |
| district | String | 是 | 区县 | `南山区` |
| detailAddress | String | 是 | 详细地址 | `科技园路88号` |
| zipCode | String | 否 | 邮编 | `518000` |
| isDefault | Integer | 否 | 是否默认(1-是 0-否) | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 地址ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "收货人不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 收货人不能为空 | 收货人姓名为空 |
| 收货人电话不能为空 | 收货人电话为空 |
| 省/市/区不能为空 | 地址省市区信息不完整 |
| 详细地址不能为空 | 详细地址为空 |
| 地址数量已达上限 | 收货地址已达最大数量限制 |

---

### 3.2 更新收货地址

**接口路径：** `/address/update`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 更新收货地址

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 地址ID | `1` |
| receiverName | String | 否 | 收货人姓名 | `张三` |
| receiverPhone | String | 否 | 收货人电话 | `13800138000` |
| province | String | 否 | 省份 | `广东省` |
| city | String | 否 | 城市 | `深圳市` |
| district | String | 否 | 区县 | `南山区` |
| detailAddress | String | 否 | 详细地址 | `科技园路88号` |
| zipCode | String | 否 | 邮编 | `518000` |
| isDefault | Integer | 否 | 是否默认(1-是 0-否) | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": {
        "id": 1
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "地址ID不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 地址ID不能为空 | 未提供地址ID |
| 更新失败，地址不存在或不属于当前用户 | 地址不存在或无权操作 |

---

### 3.3 删除收货地址

**接口路径：** `/address/delete/{addressId}`
**HTTP方法：** DELETE
**权限：** 需认证 (isAuthenticated)
**功能说明：** 删除收货地址

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| addressId | Long | 是 | 地址ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "删除失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 品牌ID不能为空 | 品牌ID未填写 |
| 删除失败 | 品牌删除失败（品牌不存在） |

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "删除失败，地址不存在或不属于当前用户",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 删除失败，地址不存在或不属于当前用户 | 地址不存在或无权删除 |

---

### 3.4 获取地址详情

**接口路径：** `/address/detail/{addressId}`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取收货地址详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| addressId | Long | 是 | 地址ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 地址ID | `1` |
| userId | Long | 用户ID | `1` |
| receiverName | String | 收货人姓名 | `张三` |
| receiverPhone | String | 收货人电话 | `13800138000` |
| province | String | 省份 | `广东省` |
| city | String | 城市 | `深圳市` |
| district | String | 区县 | `南山区` |
| detailAddress | String | 详细地址 | `科技园路88号` |
| zipCode | String | 邮编 | `518000` |
| isDefault | Integer | 是否默认 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "userId": 1,
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "province": "广东省",
        "city": "深圳市",
        "district": "南山区",
        "detailAddress": "科技园路88号",
        "zipCode": "518000",
        "isDefault": 1
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "地址不存在或不属于当前用户",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 地址不存在或不属于当前用户 | 地址ID无效或无权访问 |

---

### 3.5 获取地址列表

**接口路径：** `/address/list`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取当前用户的所有收货地址

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 |
|--------|------|------|
| List | Array | 地址列表 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "receiverName": "张三",
            "receiverPhone": "13800138000",
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detailAddress": "科技园路88号",
            "isDefault": 1
        }
    ]
}
```

---

### 3.6 获取默认地址

**接口路径：** `/address/default`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取当前用户的默认收货地址

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "province": "广东省",
        "city": "深圳市",
        "district": "南山区",
        "detailAddress": "科技园路88号",
        "isDefault": 1
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "默认地址不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 默认地址不存在 | 当前用户未设置默认地址 |

---

### 3.7 设置默认地址

**接口路径：** `/address/set-default/{addressId}`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 将指定地址设为默认地址

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| addressId | Long | 是 | 地址ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "设置成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "设置失败，地址不存在或不属于当前用户",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 设置失败，地址不存在或不属于当前用户 | 地址ID无效或无权操作 |

---

## 四、购物车模块 (Cart)

### 3.1 添加商品到购物车

**接口路径：** `/cart/add`
**HTTP方法：** POST
**权限：** 需认证 (isAuthenticated)
**功能说明：** 添加商品到购物车

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `1001` |
| quantity | Integer | 否 | 数量（默认1） | `2` |
| productImage | String | 否 | 商品主图路径（前端传入，作为快照保存；不传则使用SKU默认图片） | `/images/2026/05/15/abc.jpg` |
| skuSpecs | String | 否 | SKU规格描述快照 | `颜色:黑色 / 尺寸:XL` |
| notes | String | 否 | 备注 | `送礼物` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| cartItemId | Long | 购物车项ID | `1` |
| skuId | Long | SKU ID | `1001` |
| quantity | Integer | 数量 | `2` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "cartItemId": 1,
        "skuId": 1001,
        "quantity": 2
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "SKU ID不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| SKU ID不能为空 | 未提供SKU ID |
| 数量必须大于0 | 数量参数值无效 |
| 添加失败，商品可能已下架或库存不足 | 商品状态异常或库存不足 |

---

### 3.2 更新商品数量

**接口路径：** `/cart/quantity/{skuId}`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 更新购物车中商品的数量

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `1001` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| quantity | Integer | 是 | 数量 | `3` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "更新失败，库存不足或商品不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 更新失败，库存不足或商品不存在 | 库存不足或商品已下架 |

---

### 3.3 设置选中状态

**接口路径：** `/cart/selected/{skuId}`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 设置购物车中商品的选中状态

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `1001` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| selected | Integer | 是 | 选中状态(1-选中 0-未选) | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "设置成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "选中状态值不正确",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 选中状态值不正确 | 选中状态参数值无效 |
| 设置失败，商品不存在 | 购物车中无该商品 |

---

### 3.4 全选/取消全选

**接口路径：** `/cart/selected-all`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 设置购物车中所有商品的选中状态

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| selected | Integer | 是 | 选中状态(1-全选 0-取消全选) | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "设置成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "选中状态值不正确",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 选中状态值不正确 | 选中状态参数值无效 |
| 设置失败 | 全选/取消全选操作失败 |

---

### 3.5 移除商品

**接口路径：** `/cart/remove/{skuId}`
**HTTP方法：** DELETE
**权限：** 需认证 (isAuthenticated)
**功能说明：** 从购物车中移除商品

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `1001` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "移除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "移除失败，商品不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 移除失败，商品不存在 | 购物车中无该商品 |

---

### 3.6 清空购物车

**接口路径：** `/cart/clear`
**HTTP方法：** DELETE
**权限：** 需认证 (isAuthenticated)
**功能说明：** 清空购物车所有商品

#### 响应示例
```json
{
    "code": 200,
    "msg": "清空成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "清空失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 清空失败 | 清空购物车操作失败 |

---

### 3.7 清空已选商品

**接口路径：** `/cart/clear-selected`
**HTTP方法：** DELETE
**权限：** 需认证 (isAuthenticated)
**功能说明：** 清空购物车中已选中的商品（结算后调用）

#### 响应示例
```json
{
    "code": 200,
    "msg": "清空成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "清空失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 清空失败 | 清空已选商品操作失败 |

---

### 3.8 获取购物车列表

**接口路径：** `/cart/list`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取购物车列表（包含商品详情和实时库存）

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 购物车项列表 | |

#### CartItemVo 返回字段

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 购物车项ID | `1` |
| skuId | Long | SKU ID | `1001` |
| quantity | Integer | 数量 | `2` |
| selected | Integer | 是否选中 | `1` |
| productName | String | 商品名称（快照） | `小米手机14` |
| productImage | String | 商品图片（快照） | `/uploads/product/xm14.jpg` |
| skuSpecs | String | SKU规格（快照JSON） | `{"颜色":"黑色"}` |
| price | BigDecimal | 单价（快照） | `4999.00` |
| subtotal | BigDecimal | 小计金额 | `9998.00` |
| notes | String | 备注 | `送礼物` |
| expireTime | String | 失效时间 | `null` |
| stock | Integer | SKU库存（实时） | `100` |
| skuStatus | Integer | SKU状态（实时） | `1` |
| spuId | Long | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "skuId": 1001,
            "quantity": 2,
            "selected": 1,
            "productName": "小米手机14",
            "productImage": "/uploads/product/xm14.jpg",
            "skuSpecs": "{\"颜色\":\"黑色\",\"内存\":\"256GB\"}",
            "price": 4999.00,
            "subtotal": 9998.00,
            "notes": "送礼物",
            "expireTime": null,
            "stock": 100,
            "skuStatus": 1,
            "spuId": 1
        }
    ]
}
```

---

### 3.9 获取已选商品

**接口路径：** `/cart/selected`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取已选中的商品（用于结算）

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 |
|--------|------|------|
| List | Array | 已选中的购物车项列表 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "skuId": 1001,
            "quantity": 2,
            "productName": "小米手机14",
            "price": 4999.00,
            "subtotal": 9998.00,
            "stock": 100
        }
    ]
}
```

---

### 3.10 获取购物车概要

**接口路径：** `/cart/summary`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取购物车概要（总数量、总金额、选中数量）

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| totalCount | Integer | 商品种类数 | `3` |
| selectedCount | Integer | 已选种类数 | `2` |
| totalAmount | BigDecimal | 总金额 | `12997.00` |
| selectedAmount | BigDecimal | 已选商品金额 | `9998.00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "totalCount": 3,
        "selectedCount": 2,
        "totalAmount": 12997.00,
        "selectedAmount": 9998.00
    }
}
```

---

## 五、订单模块 (Order)

### 4.1 创建订单

**接口路径：** `/order/create`
**HTTP方法：** POST
**权限：** 需认证 (isAuthenticated)
**功能说明：** 直接创建订单（不走购物车）

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| addressId | Long | 否 | 收货地址ID（二选一） | `1` |
| receiverName | String | 否 | 收货人姓名（二选一） | `张三` |
| receiverPhone | String | 否 | 收货人电话（二选一） | `13800138000` |
| receiverAddress | String | 否 | 收货地址（二选一） | `深圳市南山区...` |
| totalAmount | BigDecimal | 是 | 订单总金额 | `9998.00` |
| discountAmount | BigDecimal | 否 | 优惠金额 | `100.00` |
| freightAmount | BigDecimal | 否 | 运费 | `0.00` |
| payAmount | BigDecimal | 是 | 实付金额 | `9898.00` |
| remark | String | 否 | 订单备注 | `尽快发货` |
| items | List | 是 | 订单项列表 | |

#### OrderItemDto 参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `1001` |
| quantity | Integer | 是 | 数量 | `2` |
| productName | String | 是 | 商品名称 | `小米手机14` |
| productImage | String | 否 | 商品图片 | `/uploads/...` |
| skuSpecs | String | 否 | SKU规格 | `{"颜色":"黑色"}` |
| price | BigDecimal | 是 | 单价 | `4999.00` |
| giftFlag | Integer | 否 | 是否赠品(1-是 0-否) | `0` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| orderId | Long | 订单ID | `1` |
| orderNo | String | 订单号 | `ORD202605150001` |
| payAmount | BigDecimal | 实付金额 | `9898.00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "订单创建成功",
    "data": {
        "orderId": 1,
        "orderNo": "ORD202605150001",
        "payAmount": 9898.00
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单商品不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单商品不能为空 | 请求中没有包含订单项 |
| 收货信息不能为空 | 收货地址或收货信息缺失 |
| 收货地址不能为空 | 收货地址ID无效或未提供 |

---

### 4.2 从购物车结算创建订单

**接口路径：** `/order/create-from-cart`
**HTTP方法：** POST
**权限：** 需认证 (isAuthenticated)
**功能说明：** 从购物车已选中商品创建订单

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| addressId | Long | 是 | 收货地址ID | `1` |
| payType | String | 是 | 支付方式(alipay/wechat) | `alipay` |
| buyerMessage | String | 否 | 买家留言 | `尽快发货` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| orderId | Long | 订单ID | `1` |
| orderNo | String | 订单号 | `ORD202605150001` |
| totalAmount | BigDecimal | 订单总金额 | `9998.00` |
| payAmount | BigDecimal | 实付金额 | `9898.00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "订单创建成功",
    "data": {
        "orderId": 1,
        "orderNo": "ORD202605150001",
        "totalAmount": 9998.00,
        "payAmount": 9898.00
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "收货地址不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 收货地址不能为空 | 未提供收货地址ID |
| 购物车中没有选中的商品 | 购物车已选商品为空 |

---

### 4.3 获取订单详情

**接口路径：** `/order/detail/{orderNo}`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取订单详情（包含订单项和发货记录）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单号 | `ORD202605150001` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| order | Object | 订单主信息 | |
| items | Array | 订单明细列表 | |
| deliveries | Array | 发货记录列表 | |
| statusDesc | String | 订单状态描述 | `待发货` |
| payStatusDesc | String | 支付状态描述 | `已支付` |
| payTypeDesc | String | 支付方式描述 | `支付宝` |

#### Order 订单实体返回字段

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 订单ID | `1` |
| orderNo | String | 订单号 | `ORD202605150001` |
| userId | Long | 用户ID | `1` |
| totalAmount | BigDecimal | 总金额 | `9998.00` |
| discountAmount | BigDecimal | 优惠金额 | `100.00` |
| freightAmount | BigDecimal | 运费 | `0.00` |
| payAmount | BigDecimal | 实付金额 | `9898.00` |
| payType | String | 支付方式 | `alipay` |
| status | Integer | 订单状态 | `2` |
| payStatus | Integer | 支付状态 | `2` |
| receiverName | String | 收货人 | `张三` |
| receiverPhone | String | 收货电话 | `13800138000` |
| receiverAddress | String | 收货地址 | `深圳市南山区...` |
| remark | String | 备注 | `尽快发货` |
| createdAt | DateTime | 创建时间 | `2026-05-15 10:00:00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "order": {},
        "items": [],
        "deliveries": []
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单号无效或订单不存在 |
| 无权查看此订单 | 当前用户无权查看该订单 |

---

### 4.4 获取订单列表

**接口路径：** `/order/list`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取当前用户的订单列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 否 | 订单状态筛选 | `1` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 订单列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 总数量 | `50` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "orderNo": "ORD202605150001",
                "totalAmount": 9998.00,
                "payAmount": 9898.00,
                "status": 2,
                "statusDesc": "待发货",
                "itemCount": 2,
                "createdAt": "2026-05-15 10:00:00"
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 50
    }
}
```

---

### 4.5 商家分页查询店铺订单列表

**接口路径：** `/order/seller/list`
**HTTP方法：** GET
**权限：** SELLER / STORE_ADMIN / ADMIN / SUPER_ADMIN
**功能说明：** 商家分页查询名下店铺的订单。数据关联链路：orders → order_items → spu.seller_id，支持按状态筛选。

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 否 | 订单状态筛选 | `2` |
| page | Integer | 否 | 页码，默认第1页 | `1` |
| pageSize | Integer | 否 | 每页数量，默认10条，最大500条 | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array\<Order\> | 订单列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 订单总数 | `25` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "orderNo": "ORD202605150001",
                "userId": 5,
                "totalAmount": 9998.00,
                "payAmount": 9898.00,
                "status": 2,
                "payStatus": 1,
                "createdAt": "2026-05-15 10:00:00"
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 25
    }
}
```

#### 错误返回示例
```json
{
    "code": 403,
    "msg": "权限不足",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 权限不足 | 当前用户不具备商家角色 |

---

### 4.6 商家查询订单详情

**接口路径：** `/order/seller/detail/{orderNo}`
**HTTP方法：** GET
**权限：** SELLER / STORE_ADMIN / ADMIN / SUPER_ADMIN
**功能说明：** 商家根据订单号查询订单详情（含订单项和发货记录）。校验订单是否属于当前商家，仅返回本店铺的订单。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderNo | String | 是 | 订单号 | `ORD202605150001` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| order | Object | 订单主信息 | |
| items | Array | 订单明细列表 | |
| deliveries | Array | 发货记录列表 | |
| statusDesc | String | 订单状态描述 | `待发货` |
| payStatusDesc | String | 支付状态描述 | `已支付` |
| payTypeDesc | String | 支付方式描述 | `支付宝` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "order": {
            "id": 1,
            "orderNo": "ORD202605150001",
            "userId": 5,
            "totalAmount": 9998.00,
            "payAmount": 9898.00,
            "status": 2,
            "payStatus": 1,
            "payType": "alipay",
            "receiverName": "张三",
            "receiverPhone": "13800138000",
            "receiverAddress": "深圳市南山区...",
            "createdAt": "2026-05-15 10:00:00"
        },
        "items": [],
        "deliveries": [],
        "statusDesc": "待发货",
        "payStatusDesc": "已支付",
        "payTypeDesc": "支付宝"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在或不属于您的店铺",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在或不属于您的店铺 | 订单号无效或该订单不属于当前商家店铺 |
| 权限不足 | 当前用户不具备商家角色 |

---

### 4.7 获取订单明细

**接口路径：** `/order/items/{orderId}`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取订单的商品明细

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "skuId": 1001,
            "productName": "小米手机14",
            "productImage": "/uploads/product/xm14.jpg",
            "skuSpecs": "{\"颜色\":\"黑色\",\"内存\":\"256GB\"}",
            "price": 4999.00,
            "quantity": 2,
            "subtotal": 9998.00
        }
    ]
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单ID无效或不存在 |

---

### 4.8 取消订单

**接口路径：** `/order/cancel/{orderId}`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 取消订单（仅限未支付的订单）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "订单已取消",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单ID无效或不存在 |
| 无权操作此订单 | 当前用户无权操作该订单 |
| 订单取消失败，订单状态不允许 | 当前订单状态不允许取消 |

---

### 4.9 支付订单

**接口路径：** `/order/pay/{orderId}`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 模拟支付订单

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| payType | String | 否 | 支付方式(alipay/wechat，默认alipay) | `alipay` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "支付成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单ID无效或不存在 |
| 无权操作此订单 | 当前用户无权操作该订单 |
| 支付失败，订单状态不允许 | 当前订单状态不允许支付 |

---

### 4.10 发货

**接口路径：** `/order/deliver/{orderId}`
**HTTP方法：** PUT
**权限：** hasAuthority('order:deliver') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 订单发货（创建发货记录）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deliveryCompany | String | 是 | 物流公司代码 | `SF` |
| deliveryNo | String | 是 | 物流单号 | `SF1234567890` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "发货成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单ID无效或不存在 |
| 无权操作此订单 | 当前用户无权操作该订单 |
| 发货失败，订单状态不允许 | 当前订单状态不允许发货 |

---

### 4.11 确认收货

**接口路径：** `/order/confirm/{orderId}`
**HTTP方法：** PUT
**权限：** 需认证 (isAuthenticated)
**功能说明：** 确认收货

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "确认收货成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单ID无效或不存在 |
| 无权操作此订单 | 当前用户无权操作该订单 |
| 确认收货失败，订单状态不允许 | 当前订单状态不允许确认收货 |

---

### 4.12 删除订单

**接口路径：** `/order/delete/{orderId}`
**HTTP方法：** DELETE
**权限：** 需认证 (isAuthenticated)
**功能说明：** 删除订单（仅限已取消或已完成的订单）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单不存在 | 订单ID无效或不存在 |
| 无权操作此订单 | 当前用户无权操作该订单 |
| 订单删除失败 | 订单删除操作失败 |

---

### 4.13 获取订单状态描述

**接口路径：** `/order/status-desc/{status}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取订单状态的中文描述

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 是 | 订单状态码 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "statusDesc": "待付款"
    }
}
```

**订单状态码：**

| 状态码 | 说明 |
|--------|------|
| 1 | 待付款 |
| 2 | 待发货 |
| 3 | 待收货 |
| 4 | 已完成 |
| 5 | 已取消 |

---

### 4.14 获取支付状态描述

**接口路径：** `/order/pay-status-desc/{payStatus}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取支付状态的中文描述

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| payStatus | Integer | 是 | 支付状态码 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "payStatusDesc": "未支付"
    }
}
```

**支付状态码：**

| 状态码 | 说明 |
|--------|------|
| 0 | 未支付 |
| 1 | 已支付 |
| 2 | 已退款 |

---

## 六、发货记录模块 (Delivery)

### 5.1 创建发货记录

**接口路径：** `/delivery/create`
**HTTP方法：** POST
**权限：** hasAuthority('order:delivery:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 创建发货记录（支持一个订单多次发货）

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |
| deliveryCompany | String | 是 | 物流公司代码 | `SF` |
| deliveryNo | String | 是 | 物流单号 | `SF1234567890` |
| sender | String | 否 | 发货人 | `张三` |
| senderId | Long | 否 | 发货人ID | `1` |
| packageCount | Integer | 否 | 包裹数量（默认1） | `1` |
| weight | BigDecimal | 否 | 包裹重量(kg) | `2.5` |
| receiverName | String | 否 | 收货人姓名 | `李四` |
| receiverPhone | String | 否 | 收货人电话 | `13900139000` |
| remark | String | 否 | 备注 | `小心轻放` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 发货记录ID | `1` |
| orderId | Long | 订单ID | `1` |
| deliveryNo | String | 物流单号 | `SF1234567890` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "发货记录创建成功",
    "data": {
        "id": 1,
        "orderId": 1,
        "deliveryNo": "SF1234567890"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "订单ID不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 订单ID不能为空 | 未提供订单ID |
| 物流公司不能为空 | 未提供物流公司信息 |
| 物流单号不能为空 | 未提供物流单号 |
| 发货记录创建失败 | 创建发货记录失败 |

---

### 5.2 签收发货记录

**接口路径：** `/delivery/sign`
**HTTP方法：** PUT
**权限：** hasAuthority('order:delivery:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 标记发货记录为已签收

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deliveryNo | String | 是 | 物流单号 | `SF1234567890` |
| signer | String | 否 | 签收人 | `张三` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "签收成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "签收失败，发货记录不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 签收失败，发货记录不存在 | 物流单号无效或发货记录不存在 |

---

### 5.3 标记物流异常

**接口路径：** `/delivery/exception`
**HTTP方法：** PUT
**权限：** hasAuthority('order:delivery:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 标记发货记录为物流异常

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deliveryNo | String | 是 | 物流单号 | `SF1234567890` |
| exceptionReason | String | 是 | 异常原因 | `收件人不在家` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "异常已标记",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "处理失败，发货记录不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 处理失败，发货记录不存在 | 物流单号无效或发货记录不存在 |

---

### 5.4 获取订单发货记录

**接口路径：** `/delivery/list/{orderId}`
**HTTP方法：** GET
**权限：** 需认证 (isAuthenticated)
**功能说明：** 获取指定订单的所有发货记录

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| orderId | Long | 是 | 订单ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 发货记录列表 | |

#### OrderDelivery 发货记录实体返回字段

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 发货记录ID | `1` |
| orderId | Long | 订单ID | `1` |
| deliveryCompany | String | 物流公司 | `顺丰速运` |
| deliveryNo | String | 物流单号 | `SF1234567890` |
| deliveryStatus | Integer | 发货状态 | `1` |
| sender | String | 发货人 | `张三` |
| deliveryTime | DateTime | 发货时间 | `2026-05-15 14:00:00` |
| packageCount | Integer | 包裹数量 | `1` |
| weight | BigDecimal | 重量(kg) | `2.5` |
| receiverName | String | 收货人 | `李四` |
| receiverPhone | String | 收货电话 | `13900139000` |
| signTime | DateTime | 签收时间 | `2026-05-16 10:00:00` |
| signer | String | 签收人 | `张三` |
| exceptionReason | String | 异常原因 | `收件人不在家` |
| remark | String | 备注 | `小心轻放` |
| createdAt | DateTime | 创建时间 | `2026-05-15 14:00:00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [...]
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "无权查看此订单的发货记录",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 无权查看此订单的发货记录 | 当前用户无权查看该订单的发货记录 |

---

### 5.5 根据物流单号查询

**接口路径：** `/delivery/detail/{deliveryNo}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据物流单号查询发货记录

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deliveryNo | String | 是 | 物流单号 | `SF1234567890` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "orderId": 1,
        "deliveryCompany": "顺丰速运",
        "deliveryNo": "SF1234567890",
        "deliveryStatus": 1,
        "deliveryTime": "2026-05-15 14:00:00"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "发货记录不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 发货记录不存在 | 物流单号无效或发货记录不存在 |

---

### 5.6 根据状态查询

**接口路径：** `/delivery/status/{deliveryStatus}`
**HTTP方法：** GET
**权限：** hasAuthority('order:delivery:query') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 根据发货状态查询发货记录

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deliveryStatus | Integer | 是 | 发货状态码 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "orderId": 1,
            "deliveryCompany": "顺丰速运",
            "deliveryNo": "SF1234567890",
            "deliveryStatus": 1
        }
    ]
}
```

---

### 5.7 获取发货状态描述

**接口路径：** `/delivery/status-desc/{deliveryStatus}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取发货状态的中文描述

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| deliveryStatus | Integer | 是 | 发货状态码 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "statusDesc": "已发货"
    }
}
```

**发货状态码：**

| 状态码 | 说明 |
|--------|------|
| 1 | 已发货 |
| 2 | 已签收 |
| 3 | 物流异常 |

---

## 七、物流公司模块 (Logistics)

### 6.1 获取启用的物流公司列表

**接口路径：** `/logistics/list`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取所有启用的物流公司列表（前端下单选择物流公司用）

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 物流公司列表 | |

#### LogisticsCompany 实体返回字段

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 物流公司ID | `1` |
| name | String | 物流公司名称 | `顺丰速运` |
| code | String | 物流公司代码 | `SF` |
| logo | String | Logo URL | `/uploads/logistics/sf.png` |
| website | String | 官网 | `https://www.sf-express.com` |
| phone | String | 客服电话 | `95338` |
| sort | Integer | 排序 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "顺丰速运",
            "code": "SF",
            "logo": "/uploads/logistics/sf.png",
            "website": "https://www.sf-express.com",
            "phone": "95338",
            "sort": 1
        }
    ]
}
```

---

### 6.2 获取物流公司详情

**接口路径：** `/logistics/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据ID获取物流公司详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 物流公司ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "name": "顺丰速运",
        "code": "SF",
        "logo": "/uploads/logistics/sf.png",
        "website": "https://www.sf-express.com",
        "phone": "95338",
        "sort": 1,
        "status": 1
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "物流公司不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 物流公司不存在 | 物流公司ID无效或不存在 |

---

### 6.3 根据代码获取物流公司

**接口路径：** `/logistics/code/{code}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据物流公司代码获取物流公司

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| code | String | 是 | 物流公司代码 | `SF` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "name": "顺丰速运",
        "code": "SF"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "物流公司不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 物流公司不存在 | 物流公司代码无效或不存在 |

---

### 6.4 分页获取物流公司列表

**接口路径：** `/logistics/page`
**HTTP方法：** GET
**权限：** hasAuthority('system:logistics:query') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 分页获取物流公司列表（管理后台用）

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | 否 | 搜索关键字 | `顺丰` |
| status | Integer | 否 | 状态筛选 | `1` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 物流公司列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 总数量 | `10` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "name": "顺丰速运",
                "code": "SF",
                "status": 1,
                "sort": 1
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 10
    }
}
```

---

### 6.5 新增物流公司

**接口路径：** `/logistics/add`
**HTTP方法：** POST
**权限：** hasAuthority('system:logistics:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 新增物流公司

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 物流公司名称 | `顺丰速运` |
| code | String | 是 | 物流公司代码（唯一） | `SF` |
| logo | String | 否 | Logo URL | `/uploads/logistics/sf.png` |
| website | String | 否 | 官网 | `https://www.sf-express.com` |
| phone | String | 否 | 客服电话 | `95338` |
| sort | Integer | 否 | 排序（默认0） | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 物流公司ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "物流公司名称不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 物流公司名称不能为空 | 未提供物流公司名称 |
| 物流公司代码不能为空 | 未提供物流公司代码 |
| 添加失败，物流公司代码可能已存在 | 物流公司代码已被使用 |

---

### 6.6 更新物流公司

**接口路径：** `/logistics/update`
**HTTP方法：** PUT
**权限：** hasAuthority('system:logistics:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 更新物流公司信息

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 物流公司ID | `1` |
| name | String | 否 | 物流公司名称 | `顺丰速运` |
| code | String | 否 | 物流公司代码 | `SF` |
| logo | String | 否 | Logo URL | `/uploads/logistics/sf.png` |
| website | String | 否 | 官网 | `https://www.sf-express.com` |
| phone | String | 否 | 客服电话 | `95338` |
| sort | Integer | 否 | 排序 | `2` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "物流公司ID不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 物流公司ID不能为空 | 未提供物流公司ID |
| 更新失败，物流公司不存在或代码已存在 | 物流公司不存在或代码冲突 |

---

### 6.7 删除物流公司

**接口路径：** `/logistics/delete/{id}`
**HTTP方法：** DELETE
**权限：** hasAuthority('system:logistics:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 删除物流公司

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 物流公司ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "删除失败，物流公司不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 删除失败，物流公司不存在 | 物流公司ID无效或不存在 |

---

### 6.8 启用/禁用物流公司

**接口路径：** `/logistics/status/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('system:logistics:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 设置物流公司的启用/禁用状态

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 物流公司ID | `1` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 是 | 状态(1-启用 0-禁用) | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "设置成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "状态值不正确",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 状态值不正确 | 状态参数值无效 |
| 更新失败，物流公司不存在 | 物流公司ID无效或不存在 |

---

## 八、商品模块 (Spu)

### 7.1 新增商品

**接口路径：** `/spu/add`
**HTTP方法：** POST
**权限：** hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**Content-Type：** multipart/form-data
**功能说明：** 新增商品（自动绑定当前商家ID）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuDto | String | 是 | 商品信息JSON字符串 | `{"name":"小米手机14",...}` |
| imageFiles | File | 否 | 上传的图片文件列表 | |

#### spuDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| storeId | Long | 是 | 店铺ID | `1` |
| name | String | 是 | 商品名称 | `小米手机14` |
| categoryId | Long | 是 | 分类ID | `1` |
| brandId | Long | 否 | 品牌ID | `1` |
| description | String | 否 | 商品描述 | `高性能旗舰手机` |
| unit | String | 否 | 单位 | `台` |
| keywords | String | 否 | 关键词（逗号分隔） | `手机,小米,旗舰` |
| sales | Integer | 否 | 销量（默认0） | `0` |
| status | Integer | 否 | 状态（1-上架 0-下架，默认1） | `1` |
| mainImage | String | 否 | 主图路径（已有图片） | `2026/05/05/uuid_main.jpg` |
| mainImageName | String | 否 | 指定上传文件中哪张作为主图 | `main.jpg` |
| images | String | 否 | 图片集路径JSON数组 | `["2026/05/05/a.jpg"]` |
| keepOldImages | Boolean | 否 | 是否保留旧图片（默认false） | `false` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 商品ID | `1` |
| mainImage | String | 主图路径 | `2026/05/15/uuid_main.jpg` |
| images | String | 图片集JSON | `["2026/05/15/a.jpg"]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1,
        "mainImage": "2026/05/15/uuid_main.jpg",
        "images": "[\"2026/05/15/a.jpg\",\"2026/05/15/b.jpg\"]"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "用户未登录",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 该商家下已存在同名商品：xxx | 同一商家下不允许出现完全相同的商品名称 |
| 添加失败 | 商品保存失败 |

---

### 7.2 更新商品

**接口路径：** `/spu/update`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**Content-Type：** multipart/form-data
**功能说明：** 更新商品信息（商家只能修改自己的商品）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuDto | String | 是 | 商品信息JSON字符串 | `{"id":1,"name":"小米手机14",...}` |
| imageFiles | File | 否 | 上传的图片文件列表 | |

#### spuDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 商品ID | `1` |
| storeId | Long | 是 | 店铺ID | `1` |
| name | String | 否 | 商品名称 | `小米手机14` |
| categoryId | Long | 否 | 分类ID | `1` |
| brandId | Long | 否 | 品牌ID | `1` |
| description | String | 否 | 商品描述 | `高性能旗舰手机` |
| unit | String | 否 | 单位 | `台` |
| keywords | String | 否 | 关键词 | `手机,小米` |
| sales | Integer | 否 | 销量 | `100` |
| status | Integer | 否 | 状态 | `1` |
| mainImage | String | 否 | 主图路径 | `2026/05/05/uuid_main.jpg` |
| mainImageName | String | 否 | 指定上传文件中哪张作为主图 | `main.jpg` |
| images | String | 否 | 图片集路径 | `["2026/05/05/a.jpg"]` |
| keepOldImages | Boolean | 否 | 是否保留旧图片 | `true` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": {
        "id": 1,
        "mainImage": "2026/05/15/uuid_main.jpg",
        "images": "[\"2026/05/15/a.jpg\",\"2026/05/15/b.jpg\"]"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "商品不存在",
    "data": null
}
```

**错误信息说明：** (更新商品)

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 商品不存在 | 指定ID的商品不存在 |
| 该商家下已存在同名商品：xxx | 同一商家下不允许出现完全相同的商品名称（修改名称时触发） |
| 无权修改此商品 | 当前用户不是商品所有者且不是管理员 |
| 更新失败 | 商品更新失败 |

---

### 7.3 删除商品（逻辑删除）

**接口路径：** `/spu/delete/{id}`
**HTTP方法：** DELETE
**权限：** hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** 逻辑删除商品（设置 is_deleted=1，商品进入回收站，图片保留以支持恢复）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "商品不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 商品不存在 | 指定ID的商品不存在 |
| 无权删除此商品 | 当前用户不是商品所有者且不是管理员 |
| 删除失败 | 商品删除失败 |

---

### 7.4 上架商品

**接口路径：** `/spu/on-shelf/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** 上架商品（设置 status=1，商品在店铺中正常展示）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "上架成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "商品不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 商品不存在 | 指定ID的商品不存在 |
| 无权操作此商品 | 当前用户不是商品所有者且不是管理员 |
| 上架失败 | 商品上架失败 |

---

### 7.5 下架商品

**接口路径：** `/spu/off-shelf/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('product:offShelf') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** 下架商品（设置 status=0，商品在店铺中不可见，但管理后台仍可见）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "下架成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "商品不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 商品不存在 | 指定ID的商品不存在 |
| 无权操作此商品 | 当前用户不是商品所有者且不是管理员 |
| 下架失败 | 商品下架失败 |

---

### 7.6 恢复商品

**接口路径：** `/spu/restore/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** 恢复被逻辑删除的商品（设置 is_deleted=0，从回收站恢复到之前状态）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "恢复成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "恢复失败，商品不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 无权操作 | 当前用户不是管理员或商家 |
| 恢复失败，商品不存在 | 指定ID的商品未找到 |

---

### 7.7 获取商品详情

**接口路径：** `/spu/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取商品详情（含商家信息，用于前端展示商品详情及所属商家）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 商品ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| spu | Object | 商品SPU信息 | 见下方spu对象 |
| spu.id | Long | 商品ID | `1` |
| spu.sellerId | Long | 商家ID | `123` |
| spu.name | String | 商品名称 | `小米手机14` |
| spu.categoryId | Long | 分类ID | `1` |
| spu.brandId | Long | 品牌ID | `1` |
| spu.description | String | 商品描述 | `高性能旗舰手机` |
| spu.mainImage | String | 主图 | `/uploads/product/xm14.jpg` |
| spu.images | String | 图片集JSON | `["/uploads/xm14_1.jpg"]` |
| spu.unit | String | 单位 | `台` |
| spu.keywords | String | 关键词 | `手机,小米,旗舰` |
| spu.sales | Integer | 销量 | `1000` |
| spu.status | Integer | 状态 | `1` |
| spu.createdAt | DateTime | 创建时间 | `2026-05-15 10:00:00` |
| sellerId | Long | 商家ID（冗余字段） | `123` |
| sellerUsername | String | 商家用户名 | `seller01` |
| sellerAvatar | String | 商家头像 | `/uploads/avatars/seller.jpg` |
| sellerRealName | String | 商家真实姓名 | `张三` |
| sellerPhone | String | 商家手机号 | `13800138001` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "spu": {
            "id": 1,
            "sellerId": 123,
            "name": "小米手机14",
            "categoryId": 1,
            "brandId": 1,
            "description": "高性能旗舰手机",
            "mainImage": "/uploads/product/xm14.jpg",
            "images": "[\"/uploads/xm14_1.jpg\",\"2026/05/15/b.jpg\"]",
            "unit": "台",
            "keywords": "手机,小米,旗舰",
            "sales": 1000,
            "status": 1,
            "createdAt": "2026-05-15 10:00:00"
        },
        "sellerId": 123,
        "sellerUsername": "seller01",
        "sellerAvatar": "/uploads/avatars/seller.jpg",
        "sellerRealName": "张三",
        "sellerPhone": "13800138001"
    }
}
```

---

### 7.8 获取商品列表

**接口路径：** `/spu/list`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取商品列表（不分页）

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | 否 | 分类ID筛选 | `1` |
| brandId | Long | 否 | 品牌ID筛选 | `1` |
| status | Integer | 否 | 状态筛选 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "小米手机14",
            "mainImage": "/uploads/product/xm14.jpg",
            "price": 4999.00,
            "sales": 1000,
            "status": 1
        }
    ]
}
```

---

### 7.9 根据商家ID获取商品列表

**接口路径：** `/spu/list-by-seller/{sellerId}`
**HTTP方法：** GET
**权限：** hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')
**功能说明：** 根据商家ID获取商品列表（不分页），仅限商家及以上角色访问

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| sellerId | Long | 是 | 商家ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "小米手机14",
            "mainImage": "/uploads/images/spu/2026/05/15/uuid_xm14.jpg",
            "price": 4999.00,
            "sales": 1000,
            "status": 1,
            "sellerId": 1
        }
    ]
}
```

---

### 7.10 根据商家ID分页获取商品列表

**接口路径：** `/spu/page-by-seller/{sellerId}`
**HTTP方法：** GET
**权限：** hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')
**功能说明：** 根据商家ID分页获取商品列表，支持按状态筛选和商品名称搜索，仅限商家及以上角色访问

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| sellerId | Long | 是 | 商家ID | `1` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 否 | 状态筛选（1-上架 0-下架，不传则查询全部） | `1` |
| keyword | String | 否 | 搜索关键字（按商品名称模糊搜索） | `小米` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

外部字段：

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 商品列表（每个元素含 categoryName、brandName） | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 总记录数 | `50` |
| sellerId | Long | 商家ID | `2` |

list 数组内每个元素的字段：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 商品ID |
| name | String | 商品名称 |
| categoryId | Long | 分类ID |
| categoryName | String | 分类名称（新增） — 非数据库字段，批量查分类表回填 |
| brandId | Long | 品牌ID |
| brandName | String | 品牌名称（新增） — 非数据库字段，批量查品牌表回填 |
| sellerId | Long | 商家ID |
| storeId | Long | 店铺ID |
| description | String | 商品描述 |
| mainImage | String | 主图路径 |
| images | String | 图片集（JSON数组） |
| unit | String | 单位 |
| keywords | String | 关键词 |
| sales | Integer | 销量 |
| status | Integer | 状态（1-上架 0-下架） |
| isDeleted | Boolean | 逻辑删除 |
| createdAt | String | 创建时间 |
| updatedAt | String | 更新时间 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "name": "小米手机14",
                "categoryId": 5,
                "categoryName": "智能手机",
                "brandId": 3,
                "brandName": "小米",
                "sellerId": 1,
                "storeId": 1,
                "description": "旗舰手机",
                "mainImage": "/uploads/images/spu/2026/05/15/uuid_xm14.jpg",
                "images": "[\"/uploads/images/spu/2026/05/15/uuid_1.jpg\"]",
                "unit": "台",
                "keywords": "小米,手机,旗舰",
                "sales": 1000,
                "status": 1,
                "isDeleted": false,
                "createdAt": "2026-05-15 10:00:00",
                "updatedAt": "2026-05-15 10:00:00"
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 50,
        "sellerId": 1
    }
}
```

---

### 7.11 分页获取商品列表

**接口路径：** `/spu/page`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 分页获取商品列表（支持分类及其子分类、多字段模糊搜索、品牌筛选）
**缓存策略：** 接口使用Redis缓存，缓存时间30分钟。商品增删改操作会自动清除缓存，保证数据一致性。

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | 否 | 分类ID（包含子分类） | `1` |
| brandId | Long | 否 | 品牌ID | `1` |
| keyword | String | 否 | 搜索关键字 | `小米` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 商品列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 总数量（搜索时有） | `50` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "name": "小米手机14",
                "mainImage": "/uploads/product/xm14.jpg",
                "price": 4999.00,
                "sales": 1000,
                "status": 1
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 50
    }
}
```

---

## 九、SKU模块 (Sku)

### 8.1 新增SKU

**接口路径：** `/sku/add`
**HTTP方法：** POST
**权限：** hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**Content-Type：** multipart/form-data
**功能说明：** 新增SKU

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuDto | String | 是 | SKU信息JSON字符串 | `{"spuId":1,"price":4999.00,...}` |
| imageFile | File | 否 | SKU图片文件 | |

#### skuDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 否 | SKU ID（修改时必填） | `1` |
| spuId | Long | 是 | SPU ID | `1` |
| price | BigDecimal | 是 | 价格 | `4999.00` |
| marketPrice | BigDecimal | 否 | 市场价 | `5999.00` |
| costPrice | BigDecimal | 否 | 成本价 | `4000.00` |
| stock | Integer | 是 | 库存 | `100` |
| warnStock | Integer | 否 | 预警库存 | `10` |
| weight | BigDecimal | 否 | 重量(kg) | `0.5` |
| status | Integer | 否 | 状态（1-启用 0-禁用） | `1` |
| image | String | 否 | 图片路径（已有图片） | `/uploads/sku/xm14.jpg` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | SKU ID | `1` |
| spuId | Long | SPU ID | `1` |
| image | String | 图片路径 | `/uploads/sku/xm14.jpg` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1,
        "spuId": 1,
        "image": "/uploads/sku/xm14.jpg"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "SPU ID不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| SPU ID不能为空 | 请求参数中spuId为空 |
| 价格不能为空且必须大于0 | 请求参数中price为空或小于等于0 |
| 图片上传失败 | 图片上传过程中发生错误 |
| 添加失败 | SKU保存失败 |

---

### 8.2 批量新增SKU

**接口路径：** `/sku/batch-add`
**HTTP方法：** POST
**权限：** hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 批量新增SKU

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |
| skus | String | 是 | SKU列表JSON字符串 | `[{"price":4999.00,...},...]` |

#### skus 子对象 (skuDto)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| price | BigDecimal | 是 | 价格 | `4999.00` |
| marketPrice | BigDecimal | 否 | 市场价 | `5999.00` |
| costPrice | BigDecimal | 否 | 成本价 | `4000.00` |
| stock | Integer | 是 | 库存 | `100` |
| warnStock | Integer | 否 | 预警库存 | `10` |
| specs | String | 否 | 规格JSON | `{"颜色":"黑色","内存":"256GB"}` |
| weight | BigDecimal | 否 | 重量(kg) | `0.5` |
| status | Integer | 否 | 状态（1-启用 0-禁用） | `1` |
| image | String | 否 | 图片路径 | `/uploads/sku/xm14.jpg` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量添加成功",
    "data": null
}
```

---

### 8.3 更新SKU

**接口路径：** `/sku/update`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 更新SKU信息

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | SKU ID | `1` |

| price | BigDecimal | 否 | 价格 | `4999.00` |
| marketPrice | BigDecimal | 否 | 市场价 | `5999.00` |
| costPrice | BigDecimal | 否 | 成本价 | `4000.00` |
| stock | Integer | 否 | 库存 | `100` |
| warnStock | Integer | 否 | 预警库存 | `10` |
| specs | String | 否 | 规格JSON | `{"颜色":"黑色"}` |
| weight | BigDecimal | 否 | 重量(kg) | `0.5` |
| status | Integer | 否 | 状态 | `1` |
| image | String | 否 | 图片路径 | `/uploads/sku/xm14.jpg` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

---

### 8.4 删除SKU

**接口路径：** `/sku/delete/{id}`
**HTTP方法：** DELETE
**权限：** hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** **逻辑删除SKU**（设置 is_deleted=1），**保留SKU对应的销售属性**（sku_sale_attr_values表记录保持不变）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | SKU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

### 8.5 删除SPU下所有SKU

**接口路径：** `/sku/delete-by-spu/{spuId}`
**HTTP方法：** DELETE
**权限：** hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** **逻辑删除SPU下所有SKU**（设置 is_deleted=1），**保留各SKU对应的销售属性**（sku_sale_attr_values表记录保持不变）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

### 8.6 启用SKU

**接口路径：** `/sku/enable/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** 启用SKU（设置 status=1）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | SKU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "启用成功",
    "data": null
}
```

---

### 8.7 禁用SKU

**接口路径：** `/sku/disable/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER') or hasRole('STORE_ADMIN')
**功能说明：** 禁用SKU（设置 status=0）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | SKU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "禁用成功",
    "data": null
}
```

---

### 8.8 更新库存

**接口路径：** `/sku/update-stock`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 更新SKU库存

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | SKU ID | `1` |
| stock | Integer | 是 | 新库存数量 | `200` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

---

### 8.9 扣减库存

**接口路径：** `/sku/decrease-stock`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 扣减SKU库存（用于订单创建）

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | SKU ID | `1` |
| quantity | Integer | 是 | 扣减数量 | `2` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "扣减成功",
    "data": null
}
```

---

### 8.10 获取SKU详情

**接口路径：** `/sku/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取SKU详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | SKU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "spuId": 1,

        "price": 4999.00,
        "marketPrice": 5999.00,
        "stock": 100,
        "specs": "{\"颜色\":\"黑色\",\"内存\":\"256GB\"}",
        "status": 1
    }
}
```

---

### 8.11 获取SKU列表

**接口路径：** `/sku/list`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取SKU列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 否 | SPU ID筛选 | `1` |
| status | Integer | 否 | 状态筛选 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "spuId": 1,
    
            "price": 4999.00,
            "stock": 100
        }
    ]
}
```

---

### 8.12 分页获取SKU列表

**接口路径：** `/sku/page`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 分页获取SKU列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 否 | SPU ID筛选 | `1` |
| keyword | String | 否 | 搜索关键字 | `XM14` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | SKU列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 总数量 | `50` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "spuId": 1,
        
                "price": 4999.00,
                "stock": 100
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 50
    }
}
```

---

### 8.13 获取SPU最低价

**接口路径：** `/sku/min-price/{spuId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取SPU的最低SKU价格

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "minPrice": 3999.00
    }
}
```

---

### 8.14 获取SPU总库存

**接口路径：** `/sku/total-stock/{spuId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取SPU下所有SKU的总库存

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "totalStock": 500
    }
}
```

---

### 8.15 获取SKU列表（含属性）

**接口路径：** `/sku/list-with-attributes`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取SKU列表，包含销售属性信息

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "spuId": 1,
    
            "price": 4999.00,
            "stock": 100,
            "saleAttributes": [
                {"attrId": 1, "attrName": "颜色", "valueId": 10, "value": "黑色", "imageUrl": null}
            ]
        }
    ]
}
```

---

### 8.16 获取SKU详情（含属性）

**接口路径：** `/sku/detail-with-attributes/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取SKU详情，包含完整的销售属性信息

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | SKU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "spuId": 1,

        "price": 4999.00,
        "marketPrice": 5999.00,
        "stock": 100,
        "specs": "{\"颜色\":\"黑色\",\"内存\":\"256GB\"}",
        "saleAttributes": [
            {"attrId": 1, "attrName": "颜色", "valueId": 10, "value": "黑色"}
        ]
    }
}
```

---

## 十、品牌模块 (Brand)

### 9.1 新增品牌

**接口路径：** `/brand/add`
**HTTP方法：** POST
**权限：** hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**Content-Type：** multipart/form-data
**功能说明：** 新增品牌（支持Logo上传）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| brandDto | String | 是 | 品牌信息JSON字符串 | `{"name":"小米",...}` |
| logoFile | File | 否 | Logo图片文件 | |

#### brandDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 品牌名称 | `小米` |
| description | String | 否 | 品牌描述 | `小米科技有限责任公司` |
| website | String | 否 | 官网地址 | `https://www.mi.com` |
| sort | Integer | 否 | 排序（默认0） | `1` |
| status | Integer | 否 | 状态（1-启用 0-禁用，默认1） | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 品牌ID | `1` |
| logo | String | Logo路径 | `2026/05/15/uuid_logo.png` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "新增成功",
    "data": {
        "id": 1,
        "logo": "2026/05/15/uuid_logo.png"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "品牌名称不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 品牌名称不能为空 | 品牌名称未填写 |
| Logo上传失败 | Logo图片上传失败 |
| 新增失败 | 品牌新增失败 |

---

### 9.2 更新品牌

**接口路径：** `/brand/update`
**HTTP方法：** PUT
**权限：** hasAuthority('product:edit') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**Content-Type：** multipart/form-data
**功能说明：** 更新品牌信息（支持Logo上传）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| brandDto | String | 是 | 品牌信息JSON字符串 | `{"id":1,"name":"小米",...}` |
| logoFile | File | 否 | Logo图片文件 | |

#### brandDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 品牌ID | `1` |
| name | String | 否 | 品牌名称 | `小米` |
| description | String | 否 | 品牌描述 | `小米科技有限责任公司` |
| website | String | 否 | 官网地址 | `https://www.mi.com` |
| sort | Integer | 否 | 排序 | `2` |
| status | Integer | 否 | 状态 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": {
        "id": 1,
        "logo": "2026/05/15/uuid_logo.png"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "品牌不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 品牌ID不能为空 | 品牌ID未填写 |
| 品牌名称不能为空 | 品牌名称未填写 |
| 品牌不存在 | 指定ID的品牌不存在 |
| Logo上传失败 | Logo图片上传失败 |
| 更新失败 | 品牌更新失败 |

---

### 9.3 删除品牌

**接口路径：** `/brand/delete/{id}`
**HTTP方法：** DELETE
**权限：** hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 删除品牌（软删除）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 品牌ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "删除失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 品牌ID不能为空 | 品牌ID未填写 |
| 删除失败 | 品牌删除失败（品牌不存在） |

---

### 9.4 获取品牌详情

**接口路径：** `/brand/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据ID获取品牌详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 品牌ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 品牌ID | `1` |
| name | String | 品牌名称 | `小米` |
| logo | String | Logo路径 | `/uploads/logos/xiaomi.png` |
| description | String | 品牌描述 | `小米科技有限责任公司` |
| website | String | 官网地址 | `https://www.mi.com` |
| sort | Integer | 排序 | `1` |
| status | Integer | 状态 | `1` |
| createdAt | DateTime | 创建时间 | `2026-05-15 10:00:00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "name": "小米",
        "logo": "/uploads/logos/xiaomi.png",
        "description": "小米科技有限责任公司",
        "website": "https://www.mi.com",
        "sort": 1,
        "status": 1,
        "createdAt": "2026-05-15 10:00:00"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "品牌不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 品牌ID不能为空 | 品牌ID未填写 |
| 品牌不存在 | 指定ID的品牌不存在 |

---

### 9.5 获取品牌列表

**接口路径：** `/brand/list`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取品牌列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 否 | 品牌ID筛选 | `1` |
| name | String | 否 | 品牌名称 | `小米` |
| status | Integer | 否 | 状态筛选 | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 品牌列表 | |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "小米",
            "logo": "/uploads/logos/xiaomi.png",
            "sort": 1,
            "status": 1
        }
    ]
}
```

---

### 9.6 分页获取品牌列表

**接口路径：** `/brand/page`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 分页获取品牌列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 否 | 品牌ID筛选 | `1` |
| name | String | 否 | 品牌名称 | `小米` |
| status | Integer | 否 | 状态筛选 | `1` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 品牌列表 | |
| total | Long | 总数量 | `50` |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| pages | Long | 总页数 | `5` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "name": "小米",
                "logo": "/uploads/logos/xiaomi.png",
                "sort": 1,
                "status": 1
            }
        ],
        "total": 50,
        "page": 1,
        "pageSize": 10,
        "pages": 5
    }
}
```

---

### 9.7 根据状态获取品牌

**接口路径：** `/brand/status/{status}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据状态获取品牌列表

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 是 | 状态（1-启用 0-禁用） | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "小米",
            "logo": "/uploads/logos/xiaomi.png",
            "status": 1
        }
    ]
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "状态不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 状态不能为空 | 状态参数未填写 |

---

### 9.8 搜索品牌

**接口路径：** `/brand/search`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据品牌名称模糊搜索

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 品牌名称（支持模糊匹配） | `小米` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "小米",
            "logo": "/uploads/logos/xiaomi.png"
        }
    ]
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "品牌名称不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 品牌名称不能为空 | 搜索关键词未填写 |

---

### 9.9 按排序获取品牌

**接口路径：** `/brand/sort`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 按排序号升序获取品牌列表

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "小米",
            "logo": "/uploads/logos/xiaomi.png",
            "sort": 1
        },
        {
            "id": 2,
            "name": "华为",
            "logo": "/uploads/logos/huawei.png",
            "sort": 2
        }
    ]
}
```

---

## 十一、分类模块 (Category)

### 10.1 新增分类

**接口路径：** `/category/add`
**HTTP方法：** POST
**权限：** hasAuthority('product:category') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**Content-Type：** multipart/form-data
**功能说明：** 新增分类（支持图标上传）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryDto | String | 是 | 分类信息JSON字符串 | `{"name":"手机",...}` |
| iconFile | File | 否 | 图标图片文件 | |

#### categoryDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 分类名称 | `手机` |
| parentId | Long | 否 | 父分类ID（默认为0） | `0` |
| level | Integer | 否 | 分类级别 | `1` |
| sort | Integer | 否 | 排序（默认0） | `1` |
| status | Integer | 否 | 状态（1-启用 0-禁用，默认1） | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 分类ID | `1` |
| level | Integer | 分类级别 | `1` |
| icon | String | 图标路径 | `2026/05/15/uuid_icon.png` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1,
        "level": 1,
        "icon": "2026/05/15/uuid_icon.png"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "分类名称不能为空",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 分类名称不能为空 | 分类名称未填写 |
| 图标上传失败 | 分类图标上传失败 |
| 添加失败 | 分类新增失败 |

---

### 10.2 更新分类

**接口路径：** `/category/update`
**HTTP方法：** PUT
**权限：** hasAuthority('product:category') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**Content-Type：** multipart/form-data
**功能说明：** 更新分类信息（支持图标上传）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryDto | String | 是 | 分类信息JSON字符串 | `{"id":1,"name":"手机",...}` |
| iconFile | File | 否 | 图标图片文件 | |

#### categoryDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 分类ID | `1` |
| name | String | 否 | 分类名称 | `手机` |
| parentId | Long | 否 | 父分类ID | `0` |
| sort | Integer | 否 | 排序 | `2` |
| status | Integer | 否 | 状态 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": {
        "id": 1,
        "icon": "2026/05/15/uuid_icon.png"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "分类不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 分类ID不能为空 | 分类ID未填写 |
| 分类不存在 | 指定ID的分类不存在 |
| 图标上传失败 | 分类图标上传失败 |
| 更新失败 | 分类更新失败 |

---

### 10.3 删除分类

**接口路径：** `/category/delete/{id}`
**HTTP方法：** DELETE
**权限：** hasAuthority('product:category') or hasRole('SUPER_ADMIN') or hasRole('ADMIN')
**功能说明：** 删除分类（级联删除子分类，同时删除图标）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 分类ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "删除失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 删除失败 | 分类删除失败 |

---

### 10.4 获取分类详情

**接口路径：** `/category/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据ID获取分类详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 分类ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 分类ID | `1` |
| name | String | 分类名称 | `手机` |
| parentId | Long | 父分类ID | `0` |
| level | Integer | 分类级别 | `1` |
| sort | Integer | 排序 | `1` |
| icon | String | 图标路径 | `/uploads/icons/phone.png` |
| status | Integer | 状态 | `1` |
| createdAt | DateTime | 创建时间 | `2026-05-15 10:00:00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "name": "手机",
        "parentId": 0,
        "level": 1,
        "sort": 1,
        "icon": "/uploads/icons/phone.png",
        "status": 1,
        "createdAt": "2026-05-15 10:00:00"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "分类不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 分类不存在 | 指定ID的分类不存在 |

---

### 10.5 获取分类列表

**接口路径：** `/category/list`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取分类列表（支持多条件筛选）

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 否 | 分类名称（模糊搜索） | `手机` |
| parentId | Long | 否 | 父分类ID | `0` |
| level | Integer | 否 | 分类级别 | `1` |
| status | Integer | 否 | 状态筛选 | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 分类列表 | |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "手机",
            "parentId": 0,
            "level": 1,
            "sort": 1,
            "icon": "/uploads/icons/phone.png",
            "status": 1
        }
    ]
}
```

---

### 10.6 分页获取分类列表

**接口路径：** `/category/page`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 分页获取分类列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 否 | 分类名称（模糊搜索） | `手机` |
| parentId | Long | 否 | 父分类ID | `0` |
| level | Integer | 否 | 分类级别 | `1` |
| status | Integer | 否 | 状态筛选 | `1` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 分类列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "name": "手机",
                "parentId": 0,
                "level": 1,
                "sort": 1,
                "status": 1
            }
        ],
        "page": 1,
        "pageSize": 10
    }
}
```

---

### 10.7 获取分类树形结构

**接口路径：** `/category/tree`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取分类树形结构

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| parentId | Long | 否 | 父分类ID（不传则从根节点开始） | `0` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 树形结构列表 | |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "手机",
            "parentId": 0,
            "level": 1,
            "children": [
                {
                    "id": 2,
                    "name": "智能手机",
                    "parentId": 1,
                    "level": 2,
                    "children": []
                }
            ]
        }
    ]
}
```

---

### 10.8 获取一级分类

**接口路径：** `/category/level1`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取所有一级分类列表

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "手机",
            "parentId": 0,
            "level": 1,
            "icon": "/uploads/icons/phone.png"
        }
    ]
}
```

---

### 10.9 获取子分类

**接口路径：** `/category/children/{parentId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定父分类下的子分类列表

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| parentId | Long | 是 | 父分类ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 2,
            "name": "智能手机",
            "parentId": 1,
            "level": 2,
            "icon": "/uploads/icons/smartphone.png"
        }
    ]
}
```

---

## 十二、属性模块 (Attribute)

### 11.1 获取分类属性

**接口路径：** `/attribute/category/{categoryId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定分类下的所有属性

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | 是 | 分类ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 属性列表 | |

#### Attribute 实体返回字段

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 属性ID | `1` |
| name | String | 属性名称 | `颜色` |
| categoryId | Long | 分类ID | `1` |
| type | Integer | 属性类型 | `1` |
| inputType | String | 输入类型 | `select` |
| required | Integer | 是否必填 | `1` |
| sort | Integer | 排序 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "颜色",
            "categoryId": 1,
            "type": 1,
            "inputType": "select",
            "required": 1,
            "sort": 1
        }
    ]
}
```

---

### 11.2 获取销售属性

**接口路径：** `/attribute/sales/{categoryId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定分类下的销售属性及其属性值

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | 是 | 分类ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| List | Array | 销售属性列表（含属性值） | |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "attrId": 1,
            "attrName": "颜色",
            "type": 1,
            "values": [
                {"valueId": 10, "value": "黑色"},
                {"valueId": 11, "value": "白色"},
                {"valueId": 12, "value": "金色"}
            ]
        }
    ]
}
```

---

### 11.3 获取基本属性

**接口路径：** `/attribute/basic/{categoryId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定分类下的基本属性及其属性值

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | 是 | 分类ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "attrId": 5,
            "attrName": "品牌",
            "type": 2,
            "values": [
                {"valueId": 20, "value": "小米"},
                {"valueId": 21, "value": "华为"}
            ]
        }
    ]
}
```

---

### 11.4 获取指定类型属性

**接口路径：** `/attribute/category/{categoryId}/type/{type}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定分类下指定类型的属性及其属性值

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| categoryId | Long | 是 | 分类ID | `1` |
| type | Integer | 是 | 属性类型（1-销售属性 2-基本属性） | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "attrId": 1,
            "attrName": "颜色",
            "type": 1,
            "values": [
                {"valueId": 10, "value": "黑色"}
            ]
        }
    ]
}
```

**属性类型说明：**

| 类型 | 说明 |
|------|------|
| 1 | 销售属性（如颜色、尺码） |
| 2 | 基本属性（如品牌、材质） |

---

### 11.5 获取SPU基本属性

**接口路径：** `/attribute/spu/{spuId}/basic`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定SPU的基本属性值

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "attrId": 5,
            "attrName": "品牌",
            "valueId": 20,
            "value": "小米"
        }
    ]
}
```

---

### 11.6 获取SPU销售属性

**接口路径：** `/attribute/spu/{spuId}/sales`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定SPU的销售属性选择

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "attrId": 1,
            "attrName": "颜色",
            "valueId": 10,
            "value": "黑色"
        }
    ]
}
```

---

### 11.7 获取SPU所有属性

**接口路径：** `/attribute/spu/{spuId}/all`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定SPU的所有属性（基本属性+销售属性）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| basicAttributes | Array | 基本属性列表 | |
| saleAttributes | Array | 销售属性列表 | |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "basicAttributes": [
            {
                "attrId": 5,
                "attrName": "品牌",
                "valueId": 20,
                "value": "小米"
            }
        ],
        "saleAttributes": [
            {
                "attrId": 1,
                "attrName": "颜色",
                "valueId": 10,
                "value": "黑色"
            }
        ]
    }
}

```

---

## 十三、商家SPU属性管理模块 (SpuAttr)

### 14.1 绑定SPU基本属性

**接口路径：** `/spu/attr/basic/bind`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 商家为SPU绑定基本属性值，支持单选/多选（通过attrValueId）或手动输入（通过manualValue）

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |
| attrId | Long | 是 | 基本属性ID | `4` |
| attrValueId | Long | 否 | 属性值ID（单选/多选时使用） | `10` |
| manualValue | String | 否 | 手动输入值（手动输入时使用） | `高通骁龙8 Gen 3` |

**注意：** attrValueId 和 manualValue 至少填写一个

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 5,
        "message": "基本属性绑定成功"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "属性不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| SPU不存在 | 指定的商品SPU不存在 |
| 属性不存在 | 指定的属性ID不存在 |
| 该属性不是基本属性，无法绑定 | 属性类型不是基本属性（attr_type != 2） |
| 属性值ID和手动输入值至少填写一个 | attrValueId和manualValue都为空 |
| 属性值不存在 | 指定的属性值ID不存在 |
| 属性值不属于该属性 | 属性值不属于指定的属性 |
| 该基本属性已绑定 | 该SPU已绑定此基本属性，请先解绑或更新 |
| 无权操作该SPU | 当前商家无权操作该商品 |

---

### 13.2 批量绑定SPU基本属性

**接口路径：** `/spu/attr/basic/batch-bind`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 批量绑定SPU基本属性。遍历传入的绑定列表逐个绑定，某个绑定失败不会影响其他绑定的执行，最终返回成功/总数

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| - | Array | 是 | 绑定信息列表 | 见下 |

列表项字段：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuId | Long | 是 | SPU ID |
| attrId | Long | 是 | 属性ID |
| attrValueId | Long | 否 | 属性值ID |
| manualValue | String | 否 | 手动输入值 |

#### 请求示例
```json
[
    {
        "spuId": 3,
        "attrId": 4,
        "attrValueId": 10
    },
    {
        "spuId": 3,
        "attrId": 5,
        "manualValue": "纯棉"
    }
]
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量绑定完成",
    "data": {
        "successCount": 2,
        "totalCount": 2
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "批量绑定完成",
    "data": {
        "successCount": 1,
        "totalCount": 2
    }
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| SPU不存在 | 指定的商品SPU不存在 |
| 属性不存在 | 指定的属性ID不存在 |
| 无权操作该SPU | 当前商家无权操作该商品 |

> 注意：批量绑定不会因为单个绑定失败而全部回滚，失败的项会被跳过，最终返回成功绑定的数量

---

### 13.3 更新SPU基本属性绑定

**接口路径：** `/spu/attr/basic/update/{id}`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 更新SPU基本属性绑定。若传入 `attrValueId`，对应的 `manualValue` 会被自动清空。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 绑定记录ID | `5` |

#### 请求参数 (Body)

同 13.1

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "绑定记录不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 绑定记录不存在 | 指定的绑定记录ID不存在 |
| 属性值ID和手动输入值至少填写一个 | attrValueId和manualValue都为空 |
| 属性值不存在 | 指定的属性值ID不存在 |
| 属性值不属于该属性 | 属性值不属于该属性 |
| 无权操作该SPU | 当前商家无权操作该商品 |
| 更新失败 | 更新操作失败 |

---

### 13.4 批量更新SPU基本属性绑定

**接口路径：** `/spu/attr/basic/batch-update`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 批量更新SPU基本属性绑定。先校验所有更新项，全部校验通过后才执行更新，任一校验失败则全部回滚。若传入 `attrValueId`，对应的 `manualValue` 会被自动清空。

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 绑定记录ID |
| spuId | Long | 是 | SPU ID |
| attrId | Long | 是 | 属性ID |
| attrValueId | Long | 否 | 属性值ID |
| manualValue | String | 否 | 手动输入值 |

#### 请求示例
```json
[
    {
        "id": 5,
        "spuId": 3,
        "attrId": 4,
        "attrValueId": 10
    },
    {
        "id": 6,
        "spuId": 3,
        "attrId": 5,
        "manualValue": "纯棉"
    }
]
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量更新完成",
    "data": {
        "successCount": 2,
        "totalCount": 2
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "绑定记录不存在，ID: 999",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 更新列表不能为空 | 请求体为空 |
| 绑定记录不存在 | 指定的绑定记录ID不存在 |
| 属性值ID和手动输入值至少填写一个 | attrValueId和manualValue都为空 |
| 属性值不存在 | 指定的属性值ID不存在 |
| 属性值不属于该属性 | 属性值不属于该属性 |
| 无权操作该SPU | 当前商家无权操作该商品 |

> 注意：此接口与批量绑定不同，任一校验失败则**全部回滚**，不会部分成功

---

### 13.5 删除SPU基本属性绑定

**接口路径：** `/spu/attr/basic/delete/{id}`
**HTTP方法：** DELETE
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 删除SPU基本属性绑定

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 绑定记录ID | `5` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "绑定记录不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 绑定记录不存在 | 指定的绑定记录ID不存在 |
| 无权操作该SPU | 当前商家无权操作该商品 |
| 删除失败 | 删除操作失败 |

---

### 13.6 获取SPU基本属性列表

**接口路径：** `/spu/attr/basic/list/{spuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 获取SPU的基本属性列表

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "spuId": 3,
            "attrId": 4,
            "attrValueId": 10,
            "manualValue": null,
            "createdAt": "2026-05-12 14:07:25",
            "updatedAt": "2026-05-12 14:07:25"
        }
    ]
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "获取SPU基本属性列表失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 获取SPU基本属性列表失败 | 获取基本属性列表过程中发生异常 |

---

### 13.7 绑定SPU销售属性

**接口路径：** `/spu/attr/sale/bind`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 商家为SPU绑定销售属性及可选值（如颜色：[黑,白,红]）

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |
| attrId | Long | 是 | 销售属性ID | `1` |
| selectedValueIds | Array | 是 | 选中的属性值ID列表 | `[1, 2, 3, 4]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 4,
        "message": "销售属性绑定成功"
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "属性不存在",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| SPU不存在 | 指定的商品SPU不存在 |
| 属性不存在 | 指定的属性ID不存在 |
| 该属性不是销售属性，无法绑定 | 属性类型不是销售属性（attr_type != 1） |
| 属性值ID列表不能为空 | selectedValueIds列表为空 |
| 属性值不存在 | 指定的属性值ID不存在 |
| 属性值不属于该属性 | 属性值不属于指定的属性 |
| 该销售属性已绑定 | 该SPU已绑定此销售属性，请先解绑或更新 |
| 无权操作该SPU | 当前商家无权操作该商品 |

---

### 13.8 批量绑定SPU销售属性

**接口路径：** `/spu/attr/sale/batch-bind`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 批量绑定SPU销售属性

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| - | Array | 是 | 绑定信息列表 | 见下 |

列表项字段：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuId | Long | 是 | SPU ID |
| attrId | Long | 是 | 属性ID |
| selectedValueIds | Array | 是 | 属性值ID列表 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量绑定完成",
    "data": {
        "successCount": 2,
        "totalCount": 2
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "批量绑定完成",
    "data": {
        "successCount": 1,
        "totalCount": 2
    }
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| SPU不存在 | 指定的商品SPU不存在 |
| 属性不存在 | 指定的属性ID不存在 |
| 无权操作该SPU | 当前商家无权操作该商品 |

> 注意：批量绑定不会因为单个绑定失败而全部回滚，失败的项会被跳过，最终返回成功绑定的数量

---

### 13.9 更新SPU销售属性绑定

**接口路径：** `/spu/attr/sale/update/{id}`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 更新SPU销售属性绑定。如果移除了某个已被SKU绑定的属性值，将更新失败并提示先删除相关SKU

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 绑定记录ID | `4` |

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 否 | 记录ID（更新时建议传入，用于批量更新） |
| spuId | Long | 是 | SPU ID |
| attrId | Long | 是 | 销售属性ID |
| selectedValueIds | Long[] | 是 | 启用的属性值ID列表 |

#### 请求示例
```json
{
    "spuId": 3,
    "attrId": 1,
    "selectedValueIds": [1, 2]
}
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "属性值【黑色】已被SKU绑定，无法移除，请先删除相关SKU",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 绑定记录不存在 | 指定的绑定记录ID不存在 |
| 属性值ID列表不能为空 | selectedValueIds列表为空 |
| 属性值不存在 | 指定的属性值ID不存在 |
| 属性值不属于该属性 | 属性值不属于该属性 |
| 无权操作该SPU | 当前商家无权操作该商品 |
| 属性值【XXX】已被SKU绑定，无法移除，请先删除相关SKU | 要移除的属性值已被SKU使用，需先删除相关SKU再操作 |
| 更新失败 | 更新操作失败 |

---

### 13.10 批量更新SPU销售属性绑定

**接口路径：** `/spu/attr/sale/batch-update`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 批量更新SPU销售属性绑定。先校验所有变更，如果任何一项存在"被移除的属性值已被SKU绑定"的情况，则全部失败回滚并提示

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 绑定记录ID |
| spuId | Long | 是 | SPU ID |
| attrId | Long | 是 | 销售属性ID |
| selectedValueIds | Long[] | 是 | 启用的属性值ID列表 |

#### 请求示例
```json
[
    {
        "id": 1,
        "spuId": 3,
        "attrId": 1,
        "selectedValueIds": [1, 2]
    },
    {
        "id": 2,
        "spuId": 3,
        "attrId": 2,
        "selectedValueIds": [4, 5]
    }
]
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量更新完成",
    "data": {
        "successCount": 2,
        "totalCount": 2
    }
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "属性值【黑色】已被SKU绑定，无法移除，请先删除相关SKU",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 用户未登录 | 当前用户未登录或登录已过期 |
| 更新列表不能为空 | 请求体为空 |
| 绑定记录不存在 | 指定的绑定记录ID不存在 |
| 属性值ID列表不能为空 | selectedValueIds列表为空 |
| 属性值不存在 | 指定的属性值ID不存在 |
| 属性值不属于该属性 | 属性值不属于该属性 |
| 无权操作该SPU | 当前商家无权操作该商品 |
| 属性值【XXX】已被SKU绑定，无法移除，请先删除相关SKU | 要移除的属性值已被SKU使用，需先删除相关SKU再操作 |

---

### 13.11 删除SPU销售属性绑定

**接口路径：** `/spu/attr/sale/delete/{id}`
**HTTP方法：** DELETE
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 删除SPU销售属性绑定

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 绑定记录ID | `4` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

### 13.12 获取SPU销售属性列表

**接口路径：** `/spu/attr/sale/list/{spuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 获取SPU的销售属性列表（包含属性值详情）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "attrId": 1,
            "attrName": "颜色",
            "selectedValues": [
                {
                    "valueId": 1,
                    "value": "黑色",
                    "imageUrl": "/uploads/attr/black.jpg"
                },
                {
                    "valueId": 2,
                    "value": "白色",
                    "imageUrl": "/uploads/attr/white.jpg"
                },
                {
                    "valueId": 3,
                    "value": "红色",
                    "imageUrl": "/uploads/attr/red.jpg"
                }
            ]
        },
        {
            "id": 2,
            "attrId": 2,
            "attrName": "内存",
            "selectedValues": [
                {
                    "valueId": 4,
                    "value": "128GB",
                    "imageUrl": null
                },
                {
                    "valueId": 5,
                    "value": "256GB",
                    "imageUrl": null
                }
            ]
        }
    ]
}
```

#### 错误返回示例
```json
{
    "code": 500,
    "msg": "获取SPU销售属性列表失败",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 获取SPU销售属性列表失败 | 获取销售属性列表过程中发生异常 |

---

### 13.13 获取SPU所有属性

**接口路径：** `/spu/attr/all/{spuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 获取SPU的所有属性（基本属性+销售属性）完整信息

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "spuId": 3,
        "spuName": "小米14 Pro",
        "basicAttrs": [
            {
                "id": 1,
                "attrId": 4,
                "attrName": "电池容量",
                "attrType": 2,
                "attrValueId": 10,
                "attrValue": "5000mAh",
                "manualValue": null,
                "imageUrl": null
            }
        ],
        "saleAttrs": [
            {
                "id": 1,
                "attrId": 1,
                "attrName": "颜色",
                "selectedValues": [
                    {
                        "valueId": 1,
                        "value": "黑色",
                        "imageUrl": "/images/phone/black.png"
                    },
                    {
                        "valueId": 2,
                        "value": "白色",
                        "imageUrl": "/images/phone/white.png"
                    }
                ]
            }
        ]
    }
}
```

---

### 13.14 获取SPU可绑定的属性列表

**接口路径：** `/spu/attr/available/{spuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 根据SPU的分类，获取分类下所有可用属性，并标记哪些已绑定。用于商家在编辑SPU时选择属性。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "spuId": 3,
        "categoryId": 1,
        "basicAttrs": [
            {
                "attrId": 3,
                "attrName": "电池容量",
                "attrType": 2,
                "bound": true,
                "boundId": 10,
                "values": [
                    {"valueId": 7, "value": "5000mAh", "imageUrl": null, "sort": 1},
                    {"valueId": 8, "value": "6000mAh", "imageUrl": null, "sort": 2}
                ],
                "currentValue": {
                    "valueId": 7,
                    "value": "5000mAh",
                    "imageUrl": null
                }
            },
            {
                "attrId": 4,
                "attrName": "处理器",
                "attrType": 2,
                "bound": false,
                "boundId": null,
                "values": [
                    {"valueId": 9, "value": "骁龙8 Gen 3", "imageUrl": null, "sort": 1},
                    {"valueId": 10, "value": "天玑9300", "imageUrl": null, "sort": 2}
                ],
                "currentValue": null
            }
        ],
        "saleAttrs": [
            {
                "attrId": 1,
                "attrName": "颜色",
                "attrType": 1,
                "bound": true,
                "boundId": 20,
                "values": [
                    {"valueId": 1, "value": "黑色", "imageUrl": "/images/black.png", "sort": 1},
                    {"valueId": 2, "value": "白色", "imageUrl": "/images/white.png", "sort": 2},
                    {"valueId": 3, "value": "蓝色", "imageUrl": "/images/blue.png", "sort": 3}
                ],
                "currentValue": {
                    "valueIds": [1, 2]
                }
            },
            {
                "attrId": 2,
                "attrName": "内存",
                "attrType": 1,
                "bound": false,
                "boundId": null,
                "values": [
                    {"valueId": 4, "value": "8GB", "imageUrl": null, "sort": 1},
                    {"valueId": 5, "value": "12GB", "imageUrl": null, "sort": 2},
                    {"valueId": 6, "value": "16GB", "imageUrl": null, "sort": 3}
                ],
                "currentValue": null
            }
        ]
    }
}
```

---

### 13.15 一次性绑定SPU所有属性

**接口路径：** `/spu/attr/bind-all`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 一次性为SPU绑定所有属性（基本属性+销售属性）。会先清除该SPU原有的绑定，再批量绑定新的属性。

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |
| basicAttrs | Array | 否 | 基本属性绑定列表 | 见下 |
| saleAttrs | Array | 否 | 销售属性绑定列表 | 见下 |

##### basicAttrs 列表项字段：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| attrId | Long | 是 | 属性ID | `3` |
| attrValueId | Long | 否 | 属性值ID（与manualValue二选一） | `7` |
| manualValue | String | 否 | 手动输入值（与attrValueId二选一） | `自定义处理器` |

##### saleAttrs 列表项字段：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| attrId | Long | 是 | 属性ID | `1` |
| selectedValueIds | Array | 是 | 选中的属性值ID列表 | `[1, 2]` |

#### 请求示例
```json
{
    "spuId": 3,
    "basicAttrs": [
        {
            "attrId": 3,
            "attrValueId": 7
        },
        {
            "attrId": 4,
            "manualValue": "高通骁龙8 Gen 3"
        }
    ],
    "saleAttrs": [
        {
            "attrId": 1,
            "selectedValueIds": [1, 2]
        },
        {
            "attrId": 2,
            "selectedValueIds": [4, 5]
        }
    ]
}
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "属性绑定完成",
    "data": {
        "deletedBasicCount": 2,
        "deletedSaleCount": 1,
        "boundBasicCount": 2,
        "boundSaleCount": 2,
        "spuId": 3
    }
}
```

---

## 十四、商家SKU属性管理模块 (SkuAttr)

### 14.1 绑定SKU销售属性

**接口路径：** `/sku/attr/bind`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 商家为SKU绑定销售属性值（如颜色、内存、存储等）。注意：此操作会覆盖该SKU原有的销售属性绑定

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `23` |
| attrValueIds | Array | 是 | 属性值ID列表 | `[1, 5, 7]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "bindCount": 3,
        "message": "销售属性绑定成功"
    }
}
```

---

### 14.2 批量绑定SKU销售属性

**接口路径：** `/sku/attr/batch-bind`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 批量绑定SKU销售属性

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| - | Array | 是 | 绑定信息列表 | 见下 |

列表项字段：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuId | Long | 是 | SKU ID |
| attrValueIds | Array | 是 | 属性值ID列表 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量绑定完成",
    "data": {
        "totalBindCount": 6,
        "skuCount": 2
    }
}
```

---

### 15.3 更新SKU销售属性绑定

**接口路径：** `/sku/attr/update/{skuId}`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 更新SKU销售属性绑定（先删除原有绑定，再添加新的绑定）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `23` |

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| - | Array | 是 | 属性值ID列表 | `[1, 5, 7]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

---

### 14.4 删除SKU销售属性绑定

**接口路径：** `/sku/attr/delete/{id}`
**HTTP方法：** DELETE
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 删除SKU销售属性绑定

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 绑定记录ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

### 14.5 删除SKU所有销售属性绑定

**接口路径：** `/sku/attr/delete-all/{skuId}`
**HTTP方法：** DELETE
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 删除SKU的所有销售属性绑定

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `23` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": {
        "deletedCount": 3
    }
}
```

---

### 14.6 获取SKU销售属性列表

**接口路径：** `/sku/attr/list/{skuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 获取SKU的销售属性列表

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `23` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "skuId": 23,
            "attrValueId": 1,
            "createdAt": "2026-05-12 14:07:25",
            "updatedAt": "2026-05-12 14:07:25"
        }
    ]
}
```

---

### 14.7 获取SKU销售属性详情

**接口路径：** `/sku/attr/detail/{skuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 获取SKU的销售属性详情（包含完整的属性名称、属性值等信息）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| skuId | Long | 是 | SKU ID | `23` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "skuId": 23,

        "spuId": 3,
        "spuName": "小米14 Pro",
        "price": 4299.00,
        "stock": 100,
        "image": "/uploads/images/sku/2026/05/12/xxx.jpg",
        "saleAttrs": [
            {
                "id": 1,
                "attrId": 1,
                "attrName": "颜色",
                "attrValueId": 1,
                "attrValue": "黑色",
                "imageUrl": "/images/phone/black.png"
            },
            {
                "id": 2,
                "attrId": 2,
                "attrName": "运行内存",
                "attrValueId": 5,
                "attrValue": "12G",
                "imageUrl": null
            },
            {
                "id": 3,
                "attrId": 3,
                "attrName": "存储容量",
                "attrValueId": 7,
                "attrValue": "256GB",
                "imageUrl": null
            }
        ]
    }
}
```

---

### 14.8 获取SKU可绑定的销售属性列表

**接口路径：** `/sku/attr/available/{spuId}`
**HTTP方法：** GET
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 根据SPU获取其下SKU可选择的销售属性及可选值。用于创建SKU时选择销售属性值。

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "spuId": 3,
        "spuName": "小米14 Pro",
        "saleAttrs": [
            {
                "attrId": 1,
                "attrName": "颜色",
                "attrType": 1,
                "values": [
                    {"valueId": 1, "value": "黑色", "imageUrl": "/images/phone/black.png", "sort": 1},
                    {"valueId": 2, "value": "白色", "imageUrl": "/images/phone/white.png", "sort": 2},
                    {"valueId": 3, "value": "蓝色", "imageUrl": "/images/phone/blue.png", "sort": 3}
                ]
            },
            {
                "attrId": 2,
                "attrName": "内存",
                "attrType": 1,
                "values": [
                    {"valueId": 4, "value": "8GB", "imageUrl": null, "sort": 1},
                    {"valueId": 5, "value": "12GB", "imageUrl": null, "sort": 2},
                    {"valueId": 6, "value": "16GB", "imageUrl": null, "sort": 3}
                ]
            }
        ]
    }
}
```

---

### 14.9 校验SKU属性组合

**接口路径：** `/sku/attr/validate/{spuId}`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')`
**功能说明：** 校验SKU属性组合是否合法（检查属性值是否都属于该SPU选择的销售属性）

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `3` |

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| - | Array | 是 | 属性值ID列表 | `[1, 5, 7]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "valid": true,
        "message": "属性组合合法"
    }
}
```

---

### 14.10 创建SKU并绑定销售属性

**接口路径：** `/sku/attr/create`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 一步完成单个SKU的新增 + 销售属性绑定。属性值组合不能与已有SKU重复。

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |
| price | BigDecimal | **是** | 价格 | `5999.00` |
| marketPrice | BigDecimal | **是** | 市场价 | `6999.00` |
| costPrice | BigDecimal | **是** | 成本价 | `4000.00` |
| stock | Integer | 否 | 库存（默认0） | `100` |
| warnStock | Integer | 否 | 预警库存（默认10） | `10` |
| image | String | 否 | 图片路径 | `images/sku1.jpg` |
| weight | BigDecimal | 否 | 重量(kg) | `0.5` |
| status | Integer | 否 | 状态（默认1：启用） | `1` |
| attrValueIds | Array | 是 | 销售属性值ID列表 | `[101, 201, 301]` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "创建并绑定成功",
    "data": {
        "skuId": 1
    }
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 销售价格、市场价和成本价不能为空 | 三个价格字段均需传入 |
| 无权操作该SPU | 当前商家不是该SPU的拥有者 |
| SKU属性组合不合法 | 属性值不属于该SPU的销售属性范围 |
| 属性值组合 [101-201-301] 对应的SKU已存在 | 相同属性组合的SKU已存在 |

---

### 14.11 批量创建SKU并绑定销售属性（同SPU）

**接口路径：** `/sku/attr/batch-create`
**HTTP方法：** POST
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 批量创建SKU并绑定销售属性。所有SKU必须在同一个SPU下，且每个SKU的属性值组合不能与已有SKU重复。

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | SKU创建信息列表（所有元素使用同一个spuId） |

列表项字段（同14.10 请求参数）：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| spuId | Long | 是 | SPU ID | `1` |
| price | BigDecimal | **是** | 价格 | `5999.00` |
| marketPrice | BigDecimal | **是** | 市场价 | `6999.00` |
| costPrice | BigDecimal | **是** | 成本价 | `4000.00` |
| stock | Integer | 否 | 库存（默认0） | `100` |
| warnStock | Integer | 否 | 预警库存（默认10） | `10` |
| image | String | 否 | 图片路径 | `images/sku1.jpg` |
| weight | BigDecimal | 否 | 重量(kg) | `0.5` |
| status | Integer | 否 | 状态（默认1：启用） | `1` |
| attrValueIds | Array | 是 | 销售属性值ID列表 | `[101, 201, 301]` |

#### 请求示例
```json
[
    {
        "spuId": 1,
        "price": 5999.00,
        "marketPrice": 6999.00,
        "costPrice": 4000.00,
        "stock": 100,
        "attrValueIds": [101, 201, 301]
    },
    {
        "spuId": 1,
        "price": 6999.00,
        "marketPrice": 7999.00,
        "costPrice": 5000.00,
        "stock": 50,
        "attrValueIds": [102, 202, 302]
    }
]
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量创建并绑定成功",
    "data": {
        "createdCount": 2,
        "details": {
            "11": 3,
            "12": 3
        }
    }
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 销售价格、市场价和成本价不能为空 | 三个价格字段均需传入 |
| 批量创建的所有SKU必须属于同一个SPU | 请求中存在不同的spuId |
| 无权操作该SPU | 当前商家不是该SPU的拥有者 |
| SKU属性组合不合法 | 属性值不属于该SPU的销售属性范围 |
| 属性值组合 [101-201-301] 对应的SKU已存在 | 相同属性组合的SKU已存在 |

---

### 14.12 更新SKU基本信息（不修改销售属性）

**接口路径：** `/sku/attr/update-combined`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 更新SKU基本信息（价格、库存、状态等），**不修改销售属性绑定**。修改SKU信息时，销售价格、市场价和成本价三个字段均需传入。

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | SKU ID | `1` |
| price | BigDecimal | **是** | 价格 | `5499.00` |
| marketPrice | BigDecimal | **是** | 市场价 | `6499.00` |
| costPrice | BigDecimal | **是** | 成本价 | `4000.00` |
| stock | Integer | 否 | 库存 | `200` |
| warnStock | Integer | 否 | 预警库存 | `20` |
| image | String | 否 | 图片路径 | `images/sku_new.jpg` |
| weight | BigDecimal | 否 | 重量(kg) | `0.45` |
| status | Integer | 否 | 状态 | `1` |

#### 请求示例
```json
{
    "id": 1,
    "price": 5499.00,
    "marketPrice": 6499.00,
    "costPrice": 4000.00,
    "stock": 200,
    "status": 1
}
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 销售价格、市场价和成本价不能为空 | 三个价格字段均需传入 |
| SKU不存在 | 指定的SKU ID不存在 |
| 无权操作该SKU | 当前商家不是该SKU所属SPU的拥有者 |

---

### 14.13 批量更新SKU基本信息（不修改销售属性）

**接口路径：** `/sku/attr/batch-update`
**HTTP方法：** PUT
**权限：** `hasRole('SELLER') or hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('STORE_ADMIN')`
**功能说明：** 批量更新SKU基本信息，**不修改销售属性**。修改SKU信息时，销售价格、市场价和成本价三个字段均需传入。单个SKU更新失败不影响其他SKU。

#### 请求参数 (Body)

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| - | Array | 是 | SKU更新信息列表 |

列表项字段（同14.12 请求参数）：

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | SKU ID | `1` |
| price | BigDecimal | **是** | 价格 | `5499.00` |
| marketPrice | BigDecimal | **是** | 市场价 | `6499.00` |
| costPrice | BigDecimal | **是** | 成本价 | `4000.00` |
| stock | Integer | 否 | 库存 | `200` |
| warnStock | Integer | 否 | 预警库存 | `20` |
| image | String | 否 | 图片路径 | `images/sku_new.jpg` |
| weight | BigDecimal | 否 | 重量(kg) | `0.45` |
| status | Integer | 否 | 状态 | `1` |

#### 请求示例
```json
[
    {
        "id": 1,
        "price": 5499.00,
        "marketPrice": 6499.00,
        "costPrice": 4000.00,
        "stock": 200
    },
    {
        "id": 2,
        "price": 6499.00,
        "marketPrice": 7499.00,
        "costPrice": 4500.00,
        "status": 0
    }
]
```

#### 响应示例
```json
{
    "code": 200,
    "msg": "批量更新完成",
    "data": {
        "successCount": 2,
        "totalCount": 2
    }
}
```

**错误信息说明：**

| 错误信息 | 说明 |
|---------|------|
| 销售价格、市场价和成本价不能为空 | 三个价格字段均需传入 |
| SKU不存在 | 指定的SKU ID不存在 |
| 无权操作该SKU | 当前商家不是该SKU所属SPU的拥有者 |

---

## 十五、文件模块 (File)

### 15.1 上传通用文件

**接口路径：** `/file/upload`
**HTTP方法：** POST
**权限：** hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**Content-Type：** multipart/form-data
**功能说明：** 上传通用文件

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| file | File | 是 | 上传的文件 | |
| subDir | String | 否 | 子目录（默认"common"） | `images` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| relativePath | String | 相对路径 | `2026/05/15/uuid_file.pdf` |
| fullPath | String | 完整路径 | `/uploads/common/2026/05/15/uuid_file.pdf` |
| fileName | String | 文件名 | `document.pdf` |
| fileSize | Long | 文件大小(字节) | `1024000` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "上传成功",
    "data": {
        "relativePath": "2026/05/15/uuid_file.pdf",
        "fullPath": "/uploads/common/2026/05/15/uuid_file.pdf",
        "fileName": "document.pdf",
        "fileSize": 1024000
    }
}
```

---

### 12.2 上传图片

**接口路径：** `/file/upload/image`
**HTTP方法：** POST
**权限：** hasAuthority('product:add') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**Content-Type：** multipart/form-data
**功能说明：** 上传图片文件（仅支持jpg、png、gif、webp格式）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| file | File | 是 | 上传的图片文件 | |
| subDir | String | 否 | 子目录（默认"images"） | `products` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "上传成功",
    "data": {
        "relativePath": "2026/05/15/uuid_image.jpg",
        "fullPath": "/uploads/images/2026/05/15/uuid_image.jpg",
        "fileName": "product.jpg",
        "fileSize": 512000
    }
}
```

---

### 13.3 删除文件

**接口路径：** `/file/delete`
**HTTP方法：** DELETE
**权限：** hasAuthority('product:delete') or hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('SELLER')
**功能说明：** 删除指定文件

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| relativePath | String | 是 | 文件相对路径 | `2026/05/15/uuid_file.jpg` |
| subDir | String | 否 | 子目录（默认"images"） | `images` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

### 12.4 下载文件

**接口路径：** `/file/download`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 下载指定文件

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| relativePath | String | 是 | 文件相对路径 | `2026/05/15/uuid_file.pdf` |
| subDir | String | 否 | 子目录（默认"images"） | `common` |

#### 响应说明

- 返回文件内容
- Content-Type: application/octet-stream
- Header中包含Content-Disposition: attachment

---

### 13.5 预览图片

**接口路径：** `/file/preview`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 预览图片（直接显示图片内容）

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| relativePath | String | 是 | 图片相对路径 | `2026/05/15/uuid_image.jpg` |
| subDir | String | 否 | 子目录（默认"images"） | `products` |

#### 响应说明

- Content-Type 根据图片格式返回（image/jpeg、image/png、image/gif、image/webp）
- 直接在浏览器中显示图片

---

### 15.6 获取文件URL

**接口路径：** `/file/url`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取文件的访问URL

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| relativePath | String | 是 | 文件相对路径 | `2026/05/15/uuid_image.jpg` |
| subDir | String | 否 | 子目录（默认"images"） | `products` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| url | String | 相对URL | `/uploads/images/2026/05/15/uuid_image.jpg` |
| fullUrl | String | 完整URL | `/uploads/images/2026/05/15/uuid_image.jpg` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "url": "/uploads/images/2026/05/15/uuid_image.jpg",
        "fullUrl": "/uploads/images/2026/05/15/uuid_image.jpg"
    }
}
```

---

## 十六、店铺模块 (Store)

### 16.1 新增店铺

**接口路径：** `/store/add`
**HTTP方法：** POST
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**Content-Type：** multipart/form-data
**功能说明：** 新增店铺（商家创建自己的店铺）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| storeDto | String | 是 | 店铺信息JSON字符串 | `{"name":"我的店铺",...}` |
| logoFile | File | 否 | 店铺Logo图片文件 | |
| bannerFile | File | 否 | 店铺横幅图片文件 | |

#### storeDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| name | String | 是 | 店铺名称 | `我的店铺` |
| description | String | 否 | 店铺描述 | `主营手机电脑` |
| phone | String | 否 | 联系电话 | `13800138000` |
| address | String | 否 | 店铺地址 | `深圳市南山区` |
| businessLicense | String | 否 | 营业执照 | `/uploads/license.jpg` |
| status | Integer | 否 | 状态（1-正常 2-审核中，默认1） | `1` |
| sort | Integer | 否 | 排序（默认0） | `0` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 店铺ID | `1` |
| logo | String | Logo路径 | `2026/05/15/uuid_logo.jpg` |
| banner | String | 横幅路径 | `2026/05/15/uuid_banner.jpg` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1,
        "logo": "2026/05/15/uuid_logo.jpg",
        "banner": "2026/05/15/uuid_banner.jpg"
    }
}
```

---

### 16.2 更新店铺信息

**接口路径：** `/store/update`
**HTTP方法：** PUT
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**Content-Type：** multipart/form-data
**功能说明：** 更新店铺信息（商家只能修改自己的店铺）

#### 请求参数 (Form Data)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| storeDto | String | 是 | 店铺信息JSON字符串 | `{"id":1,"name":"新店名",...}` |
| logoFile | File | 否 | 店铺Logo图片文件 | |
| bannerFile | File | 否 | 店铺横幅图片文件 | |

#### storeDto 参数详情

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 店铺ID | `1` |
| name | String | 否 | 店铺名称 | `新店名` |
| description | String | 否 | 店铺描述 | `主营手机电脑` |
| phone | String | 否 | 联系电话 | `13800138000` |
| address | String | 否 | 店铺地址 | `深圳市南山区` |
| businessLicense | String | 否 | 营业执照 | `/uploads/license.jpg` |
| sort | Integer | 否 | 排序 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": {
        "id": 1,
        "logo": "2026/05/15/uuid_logo.jpg",
        "banner": "2026/05/15/uuid_banner.jpg"
    }
}
```

---

### 13.3 获取店铺详情

**接口路径：** `/store/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据ID获取店铺详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 店铺ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 店铺ID | `1` |
| name | String | 店铺名称 | `我的店铺` |
| sellerId | Long | 商家用户ID | `2` |
| logo | String | 店铺Logo | `/uploads/logos/xiaomi.png` |
| banner | String | 店铺横幅 | `/uploads/banners/store.jpg` |
| description | String | 店铺描述 | `主营手机电脑` |
| phone | String | 联系电话 | `13800138000` |
| address | String | 店铺地址 | `深圳市南山区` |
| businessLicense | String | 营业执照 | `/uploads/license.jpg` |
| status | Integer | 状态 | `1` |
| sort | Integer | 排序 | `0` |
| createdAt | DateTime | 创建时间 | `2026-05-15 10:00:00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "name": "我的店铺",
        "sellerId": 2,
        "logo": "/uploads/logos/xiaomi.png",
        "banner": "/uploads/banners/store.jpg",
        "description": "主营手机电脑",
        "phone": "13800138000",
        "address": "深圳市南山区",
        "status": 1,
        "sort": 0,
        "createdAt": "2026-05-15 10:00:00"
    }
}
```

---

### 16.4 获取当前用户的店铺

**接口路径：** `/store/my-store`
**HTTP方法：** GET
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')
**功能说明：** 获取当前登录商家对应的店铺

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "name": "我的店铺",
        "sellerId": 2,
        "status": 1
    }
}
```

---

### 16.5 获取店铺列表

**接口路径：** `/store/list`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取店铺列表（不分页）

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 否 | 状态筛选 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "我的店铺",
            "logo": "/uploads/logos/xiaomi.png",
            "status": 1
        }
    ]
}
```

---

### 16.6 分页获取店铺列表

**接口路径：** `/store/page`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 分页获取店铺列表

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| keyword | String | 否 | 搜索关键字（店铺名称） | `手机` |
| status | Integer | 否 | 状态筛选 | `1` |
| page | Integer | 否 | 页码（默认1） | `1` |
| pageSize | Integer | 否 | 每页数量（默认10） | `10` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| list | Array | 店铺列表 | |
| page | Integer | 当前页码 | `1` |
| pageSize | Integer | 每页数量 | `10` |
| total | Integer | 总数量 | `50` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "list": [
            {
                "id": 1,
                "name": "我的店铺",
                "logo": "/uploads/logos/xiaomi.png",
                "status": 1
            }
        ],
        "page": 1,
        "pageSize": 10,
        "total": 50
    }
}
```

---

### 16.7 更新店铺状态

**接口路径：** `/store/status/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**功能说明：** 更新店铺状态

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 店铺ID | `1` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 是 | 状态（1-正常 0-禁用 2-审核中 3-审核失败） | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

**店铺状态码：**

| 状态码 | 说明 |
|--------|------|
| 1 | 正常 |
| 0 | 禁用 |
| 2 | 审核中 |
| 3 | 审核失败 |

---

## 十七、店铺管理员模块 (StoreAdmin)

### 18.1 新增店铺管理员

**接口路径：** `/store-admin/add`
**HTTP方法：** POST
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**功能说明：** 为店铺添加管理员（店长可添加）

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| storeId | Long | 是 | 店铺ID | `1` |
| userId | Long | 是 | 管理员用户ID | `3` |
| role | Integer | 否 | 角色（1-店长 2-管理员 3-客服 4-财务，默认2） | `2` |
| status | Integer | 否 | 状态（1-启用 0-禁用，默认1） | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 管理员ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "添加成功",
    "data": {
        "id": 1
    }
}
```

---

### 17.2 更新店铺管理员

**接口路径：** `/store-admin/update`
**HTTP方法：** PUT
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**功能说明：** 更新店铺管理员信息

#### 请求参数 (Request Body)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | **是** | 管理员ID | `1` |
| role | Integer | 否 | 角色 | `3` |
| status | Integer | 否 | 状态 | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

---

### 17.3 删除店铺管理员

**接口路径：** `/store-admin/delete/{id}`
**HTTP方法：** DELETE
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**功能说明：** 删除店铺管理员

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 管理员ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "删除成功",
    "data": null
}
```

---

### 17.4 获取管理员详情

**接口路径：** `/store-admin/detail/{id}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 根据ID获取管理员详情

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 管理员ID | `1` |

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | Long | 管理员ID | `1` |
| storeId | Long | 店铺ID | `1` |
| userId | Long | 用户ID | `3` |
| role | Integer | 角色 | `2` |
| roleDesc | String | 角色描述 | `管理员` |
| status | Integer | 状态 | `1` |
| createdAt | DateTime | 创建时间 | `2026-05-15 10:00:00` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "storeId": 1,
        "userId": 3,
        "role": 2,
        "roleDesc": "管理员",
        "status": 1,
        "createdAt": "2026-05-15 10:00:00"
    }
}
```

---

### 17.5 获取店铺管理员列表

**接口路径：** `/store-admin/list/{storeId}`
**HTTP方法：** GET
**权限：** 公开
**功能说明：** 获取指定店铺的所有管理员

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| storeId | Long | 是 | 店铺ID | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": [
        {
            "id": 1,
            "storeId": 1,
            "userId": 2,
            "role": 1,
            "roleDesc": "店长",
            "status": 1
        },
        {
            "id": 2,
            "storeId": 1,
            "userId": 3,
            "role": 3,
            "roleDesc": "客服",
            "status": 1
        }
    ]
}
```

---

### 18.6 获取当前用户的管理员信息

**接口路径：** `/store-admin/my-admin`
**HTTP方法：** GET
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')
**功能说明：** 获取当前登录用户作为管理员的店铺信息

#### 响应示例
```json
{
    "code": 200,
    "msg": "操作成功",
    "data": {
        "id": 1,
        "storeId": 1,
        "userId": 2,
        "role": 1,
        "roleDesc": "店长",
        "status": 1
    }
}
```

---

### 18.7 更新管理员状态

**接口路径：** `/store-admin/status/{id}`
**HTTP方法：** PUT
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN')
**功能说明：** 更新管理员状态

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| id | Long | 是 | 管理员ID | `1` |

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| status | Integer | 是 | 状态（1-启用 0-禁用） | `1` |

#### 响应示例
```json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```

**管理员角色码：**

| 角色码 | 说明 |
|--------|------|
| 1 | 店长 |
| 2 | 管理员 |
| 3 | 客服 |
| 4 | 财务 |

---

## 十八、商家仪表盘模块 (StoreDashboard)

### 18.1 获取销售 KPI 总览

**接口路径：** `/store-admin/dashboard/sales/kpi`
**HTTP方法：** GET
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')
**功能说明：** 返回今日、近7天、本月、本年四个时间维度的销售总额，用于仪表盘核心指标卡片

#### 请求参数

无

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 |
|--------|------|------|
| today | BigDecimal | 今日销售额（元），统计当天00:00:00至今的已完成订单 |
| last7Days | BigDecimal | 近7天销售额（元），含今天 |
| thisMonth | BigDecimal | 本月销售额（元），当月1日至今 |
| thisYear | BigDecimal | 本年销售额（元），当年1月1日至今 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "today": 1580.00,
        "last7Days": 12500.00,
        "thisMonth": 42000.00,
        "thisYear": 185000.00
    }
}
```

---

### 18.2 获取销售趋势（折线图）

**接口路径：** `/store-admin/dashboard/sales/trend`
**HTTP方法：** GET
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')
**功能说明：** 返回最近7天每天的销售额明细，dates 和 values 一一对应，无销售数据的日期值为 0，用于折线图展示

#### 请求参数

无

#### 响应参数 (Response)

| 参数名 | 类型 | 说明 |
|--------|------|------|
| dates | List\<String\> | 日期数组（格式 YYYY-MM-DD），固定长度 7 |
| values | List\<BigDecimal\> | 对应日期的销售额数组，固定长度 7 |

#### 响应示例
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "dates": ["2026-05-22", "2026-05-23", "2026-05-24", "2026-05-25", "2026-05-26", "2026-05-27", "2026-05-28"],
        "values": [1200.00, 0.00, 3500.00, 2100.00, 0.00, 1800.00, 1580.00]
    }
}
```

---

### 18.3 获取商品销售排行

**接口路径：** `/store-admin/dashboard/sales/product-ranking`
**HTTP方法：** GET
**权限：** hasAuthority('store:manage') or hasRole('SUPER_ADMIN') or hasRole('SELLER')
**功能说明：** 返回指定时间段内各商品的销售汇总，按销售额降序排列，计算每个商品的销售额占比和销量占比，可用于条形图（销售额对比）和南丁格尔玫瑰图

#### 请求参数 (Query)

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| period | String | 否 | 时间段，可选 `today`、`last7Days`、`thisMonth`、`thisYear`，默认 `last7Days` | `last7Days` |

#### 响应参数 (Response)

数组元素：

| 参数名 | 类型 | 说明 |
|--------|------|------|
| name | String | 商品名称 |
| salesAmount | BigDecimal | 总销售额（元） |
| salesCount | Integer | 总销售数量（件） |
| percentOfTotal | BigDecimal | 该商品销售额占总销售额的百分比（保留一位小数，如 27.1 代表 27.1%） |
| countPercentOfTotal | BigDecimal | 该商品销售数量占总销售数量的百分比（保留一位小数，如 15.3 代表 15.3%） |

#### 响应示例
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "name": "2026夏季新款连衣裙",
            "salesAmount": 4580.00,
            "salesCount": 23,
            "percentOfTotal": 36.6,
            "countPercentOfTotal": 28.8
        },
        {
            "name": "真皮手提通勤包",
            "salesAmount": 3200.00,
            "salesCount": 8,
            "percentOfTotal": 25.6,
            "countPercentOfTotal": 10.0
        },
        {
            "name": "纯棉T恤基础款",
            "salesAmount": 2100.00,
            "salesCount": 42,
            "percentOfTotal": 16.8,
            "countPercentOfTotal": 52.5
        }
    ]
}
```

---

## 📝 附录：权限标识汇总

| 权限标识 | 说明 | 适用角色 |
|---------|------|---------|
| `isAuthenticated()` | 需要登录 | 任何登录用户 |
| `order:deliver` | 发货权限 | 商家/管理员 |
| `order:delivery:add` | 创建发货记录 | 商家/管理员 |
| `order:delivery:edit` | 编辑发货记录 | 商家/管理员 |
| `order:delivery:query` | 查询发货记录 | 商家/管理员 |
| `system:logistics:*` | 物流公司管理 | 管理员 |
| `product:add` | 添加商品/SKU | 商家/管理员 |
| `product:edit` | 编辑商品/SKU | 商家/管理员 |
| `product:delete` | 删除商品/SKU | 商家/管理员 |
| `product:category` | 分类管理 | 管理员 |
| `store:manage` | 店铺管理 | 商家/管理员 |
| `store:product` | 商品管理 | 商家/管理员 |
| `store:order` | 订单管理 | 商家/管理员 |
| `store:customer` | 客服管理 | 商家/管理员 |
| `store:finance` | 财务管理 | 商家/管理员 |
| `SUPER_ADMIN` | 超级管理员 | 系统最高权限 |
| `ADMIN` | 管理员 | 系统管理 |
| `SELLER` | 商家 | 商品管理 |

---

## 🔄 权限验证流程

```
请求到达
    ↓
检查 @PreAuthorize 注解
    ↓
isAuthenticated() → 检查是否已登录
hasAuthority() → 检查精确权限
hasRole() → 检查角色（会自动添加 ROLE_ 前缀）
    ↓
验证通过 → 执行方法
验证失败 → 返回 403 Forbidden
```

---

*文档生成时间：2026年5月*
