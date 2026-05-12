# 商城后端 API 接口文档

---

## 目录
1. [商品SKU管理](#1-商品sku管理)
   - 1.14 [获取SPU的SKU列表（包含销售属性）](#114-获取spu的sku列表包含销售属性)
   - 1.15 [获取SKU详情（包含销售属性）](#115-获取sku详情包含销售属性)
2. [属性管理](#2-属性管理)
3. [分类管理](#3-分类管理)
4. [商品SPU管理](#4-商品spu管理)
5. [品牌管理](#5-品牌管理)
6. [文件管理](#6-文件管理)

---

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功，其他表示失败 |
| message | String | 提示信息 |
| data | Object | 返回的数据 |

---

## 1. 商品SKU管理

### 1.1 新增SKU（支持图片上传）

- **路径**: `POST /sku/add`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuDto | String (JSON) | 是 | SKU信息JSON字符串 |
| imageFile | MultipartFile | 否 | SKU图片文件 |

**skuDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spuId | Long | 是 | 所属SPU ID |
| skuCode | String | 否 | SKU编码 |
| price | BigDecimal | 是 | 销售价格 |
| marketPrice | BigDecimal | 否 | 市场价 |
| costPrice | BigDecimal | 否 | 成本价 |
| stock | Integer | 否 | 库存数量，默认0 |
| warnStock | Integer | 否 | 预警库存，默认10 |
| specs | String | 否 | 规格属性JSON |
| weight | BigDecimal | 否 | 重量 |
| status | Integer | 否 | 状态，默认1(启用) |
| image | String | 否 | 图片路径（不上传文件时使用） |

**返回数据**:

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "skuCode": "SKU001",
    "image": "2026/05/08/uuid_filename.jpg"
  }
}
```

---

### 1.2 批量新增SKU

- **路径**: `POST /sku/batch-add`
- **Content-Type**: `application/x-www-form-urlencoded`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuId | Long | 是 | 所属SPU ID |
| skus | String (JSON Array) | 是 | SKU列表JSON字符串 |

**返回数据**:

```json
{
  "code": 200,
  "message": "批量添加成功",
  "data": null
}
```

---

### 1.3 删除SKU（逻辑删除）

- **路径**: `DELETE /sku/delete/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | SKU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 1.4 删除SPU下所有SKU

- **路径**: `DELETE /sku/delete-by-spu/{spuId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| spuId | Long | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 1.5 更新SKU信息

- **路径**: `PUT /sku/update`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| skuDto | String (JSON) | 是 | SKU信息JSON字符串 |
| imageFile | MultipartFile | 否 | 新图片文件 |

**skuDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | SKU ID |
| skuCode | String | 否 | SKU编码 |
| price | BigDecimal | 否 | 销售价格 |
| marketPrice | BigDecimal | 否 | 市场价 |
| costPrice | BigDecimal | 否 | 成本价 |
| stock | Integer | 否 | 库存数量 |
| warnStock | Integer | 否 | 预警库存 |
| specs | String | 否 | 规格属性JSON |
| weight | BigDecimal | 否 | 重量 |
| status | Integer | 否 | 状态 |
| image | String | 否 | 图片路径 |

**返回数据**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "image": "2026/05/08/uuid_filename.jpg"
  }
}
```

---

### 1.6 更新库存

- **路径**: `PUT /sku/update-stock`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | SKU ID |
| stock | Integer | 是 | 新库存数量 |

**返回数据**:

```json
{
  "code": 200,
  "message": "库存更新成功",
  "data": null
}
```

---

### 1.7 扣减库存

- **路径**: `PUT /sku/decrease-stock`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | SKU ID |
| quantity | Integer | 是 | 扣减数量 |

**返回数据**:

```json
{
  "code": 200,
  "message": "扣减成功",
  "data": null
}
```

---

### 1.8 获取SKU详情

- **路径**: `GET /sku/detail/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | SKU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "spuId": 10,
    "skuCode": "SKU001",
    "price": 99.99,
    "marketPrice": 129.99,
    "costPrice": 69.99,
    "stock": 100,
    "warnStock": 10,
    "specs": "{\"color\":\"红色\",\"size\":\"L\"}",
    "weight": 0.5,
    "image": "2026/05/08/uuid_filename.jpg",
    "status": 1,
    "createdAt": "2026-05-08T10:00:00",
    "updatedAt": "2026-05-08T10:00:00"
  }
}
```

---

### 1.9 获取SPU的SKU列表

- **路径**: `GET /sku/list`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuId | Long | 是 | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "spuId": 10,
      "skuCode": "SKU001",
      "price": 99.99,
      "stock": 100,
      "specs": "{\"color\":\"红色\",\"size\":\"L\"}"
    }
  ]
}
```

---

### 1.10 根据SKU编码获取SKU

- **路径**: `GET /sku/by-code/{skuCode}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| skuCode | String | SKU编码 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "skuCode": "SKU001",
    ...
  }
}
```

---

### 1.11 分页获取SKU列表

- **路径**: `GET /sku/page`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuId | Long | 否 | SPU ID筛选 |
| status | Integer | 否 | 状态筛选 |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "page": 1,
    "pageSize": 10,
    "total": 100
  }
}
```

---

### 1.12 获取SPU最低价格

- **路径**: `GET /sku/min-price/{spuId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| spuId | Long | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": 99.99
}
```

---

### 1.13 获取SPU库存总量

- **路径**: `GET /sku/total-stock/{spuId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| spuId | Long | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": 500
}
```

---

### 1.14 获取SPU的SKU列表（包含销售属性）

- **路径**: `GET /sku/list-with-attributes`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuId | Long | 是 | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "spuId": 10,
      "skuCode": "SKU001",
      "price": 99.99,
      "marketPrice": 129.99,
      "costPrice": 69.99,
      "stock": 100,
      "warnStock": 10,
      "specs": "{\"color\":\"红色\",\"size\":\"L\"}",
      "weight": 0.5,
      "image": "2026/05/08/uuid_filename.jpg",
      "status": 1,
      "saleAttributes": [
        {
          "attrId": 1,
          "attrName": "颜色",
          "valueId": 1,
          "value": "红色",
          "imageUrl": "/images/color/red.png"
        },
        {
          "attrId": 2,
          "attrName": "尺码",
          "valueId": 5,
          "value": "L",
          "imageUrl": null
        }
      ]
    }
  ]
}
```

**返回字段说明**:

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | SKU ID |
| spuId | Long | 所属SPU ID |
| skuCode | String | SKU编码 |
| price | BigDecimal | 销售价格 |
| marketPrice | BigDecimal | 市场价 |
| costPrice | BigDecimal | 成本价 |
| stock | Integer | 库存数量 |
| warnStock | Integer | 预警库存 |
| specs | String | 规格属性JSON |
| weight | BigDecimal | 重量 |
| image | String | 图片路径 |
| status | Integer | 状态 |
| saleAttributes | Array | 销售属性列表 |
| saleAttributes[].attrId | Long | 属性ID |
| saleAttributes[].attrName | String | 属性名称 |
| saleAttributes[].valueId | Long | 属性值ID |
| saleAttributes[].value | String | 属性值 |
| saleAttributes[].imageUrl | String | 属性值图片URL |

---

### 1.15 获取SKU详情（包含销售属性）

- **路径**: `GET /sku/detail-with-attributes/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | SKU ID |

**返回数据**: 与 1.14 接口返回的单个 SKU 对象结构相同

---

## 2. 属性管理

属性管理模块提供商品属性的查询功能，包括销售属性（用于SKU选择）和基本属性（用于SPU详情展示）。

### 2.1 获取分类下所有属性

- **路径**: `GET /attribute/category/{categoryId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| categoryId | Long | 分类ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "颜色",
      "attrType": 1,
      "sort": 1,
      "createdAt": "2026-05-12T14:07:25"
    },
    {
      "id": 2,
      "name": "运行内存",
      "attrType": 1,
      "sort": 2,
      "createdAt": "2026-05-12T14:07:25"
    }
  ]
}
```

---

### 2.2 获取分类下销售属性及属性值

- **路径**: `GET /attribute/sales/{categoryId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| categoryId | Long | 分类ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "attrId": 1,
      "attrName": "颜色",
      "attrType": 1,
      "sort": 1,
      "values": [
        {
          "valueId": 1,
          "value": "黑色",
          "imageUrl": "/images/phone/black.png",
          "sort": 1
        },
        {
          "valueId": 2,
          "value": "白色",
          "imageUrl": "/images/phone/white.png",
          "sort": 2
        }
      ]
    },
    {
      "attrId": 2,
      "attrName": "运行内存",
      "attrType": 1,
      "sort": 2,
      "values": [
        {
          "valueId": 5,
          "value": "12G",
          "imageUrl": null,
          "sort": 1
        },
        {
          "valueId": 6,
          "value": "16G",
          "imageUrl": null,
          "sort": 2
        }
      ]
    }
  ]
}
```

---

### 2.3 获取分类下基本属性及属性值

- **路径**: `GET /attribute/basic/{categoryId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| categoryId | Long | 分类ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "attrId": 4,
      "attrName": "电池容量",
      "attrType": 2,
      "sort": 4,
      "values": [
        {
          "valueId": 10,
          "value": "5000mAh",
          "imageUrl": null,
          "sort": 1
        }
      ]
    },
    {
      "attrId": 5,
      "attrName": "处理器",
      "attrType": 2,
      "sort": 5,
      "values": []
    }
  ]
}
```

---

### 2.4 获取分类下指定类型属性及属性值

- **路径**: `GET /attribute/category/{categoryId}/type/{type}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| categoryId | Long | 分类ID |
| type | Integer | 属性类型（1=销售属性，2=基本属性） |

**返回数据**: 与 2.2 或 2.3 相同，根据 type 返回对应类型的属性

---

### 2.5 获取SPU的基本属性值

- **路径**: `GET /attribute/spu/{spuId}/basic`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| spuId | Long | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "attrId": 4,
      "attrName": "电池容量",
      "attrType": 2,
      "value": "5000mAh",
      "imageUrl": null
    },
    {
      "attrId": 5,
      "attrName": "处理器",
      "attrType": 2,
      "value": "高通骁龙8 Gen 3",
      "imageUrl": null
    }
  ]
}
```

---

### 2.6 获取SPU的销售属性选择

- **路径**: `GET /attribute/spu/{spuId}/sales`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| spuId | Long | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "attrId": 1,
      "attrName": "颜色",
      "values": [
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
    },
    {
      "attrId": 2,
      "attrName": "运行内存",
      "values": [
        {
          "valueId": 5,
          "value": "12G",
          "imageUrl": null
        },
        {
          "valueId": 6,
          "value": "16G",
          "imageUrl": null
        }
      ]
    }
  ]
}
```

---

### 2.7 获取SPU的所有属性（基本属性+销售属性）

- **路径**: `GET /attribute/spu/{spuId}/all`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| spuId | Long | SPU ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "basicAttributes": [
      {
        "attrId": 4,
        "attrName": "电池容量",
        "attrType": 2,
        "value": "5000mAh"
      },
      {
        "attrId": 5,
        "attrName": "处理器",
        "attrType": 2,
        "value": "高通骁龙8 Gen 3"
      }
    ],
    "saleAttributes": [
      {
        "attrId": 1,
        "attrName": "颜色",
        "values": [
          {"valueId": 1, "value": "黑色", "imageUrl": "/images/phone/black.png"},
          {"valueId": 2, "value": "白色", "imageUrl": "/images/phone/white.png"}
        ]
      }
    ]
  }
}
```

---

## 3. 分类管理

### 3.1 新增分类（支持图标上传）

- **路径**: `POST /category/add`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryDto | String (JSON) | 是 | 分类信息JSON字符串 |
| iconFile | MultipartFile | 否 | 分类图标文件 |

**categoryDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 分类名称 |
| parentId | Long | 否 | 父分类ID，默认0 |
| level | Integer | 否 | 分类级别 |
| sort | Integer | 否 | 排序号，默认0 |
| status | Integer | 否 | 状态，默认1 |

**返回数据**:

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "level": 1,
    "icon": "2026/05/08/uuid_filename.png"
  }
}
```

---

### 3.2 删除分类（级联删除子分类）

- **路径**: `DELETE /category/delete/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 分类ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 3.3 更新分类信息

- **路径**: `PUT /category/update`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| categoryDto | String (JSON) | 是 | 分类信息JSON字符串 |
| iconFile | MultipartFile | 否 | 新图标文件 |

**categoryDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 分类ID |
| name | String | 否 | 分类名称 |
| parentId | Long | 否 | 父分类ID |
| sort | Integer | 否 | 排序号 |
| status | Integer | 否 | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "icon": "2026/05/08/uuid_filename.png"
  }
}
```

---

### 3.4 获取分类详情

- **路径**: `GET /category/detail/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 分类ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "服装",
    "parentId": 0,
    "level": 1,
    "sort": 1,
    "icon": "2026/05/08/uuid_filename.png",
    "status": 1,
    "createdAt": "2026-05-08T10:00:00",
    "updatedAt": "2026-05-08T10:00:00"
  }
}
```

---

### 3.5 获取分类列表

- **路径**: `GET /category/list`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 分类名称模糊搜索 |
| parentId | Long | 否 | 父分类ID |
| level | Integer | 否 | 分类级别 |
| status | Integer | 否 | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### 3.6 分页获取分类列表

- **路径**: `GET /category/page`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 分类名称模糊搜索 |
| parentId | Long | 否 | 父分类ID |
| level | Integer | 否 | 分类级别 |
| status | Integer | 否 | 状态 |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "page": 1,
    "pageSize": 10
  }
}
```

---

### 3.7 获取分类树形结构

- **路径**: `GET /category/tree`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | Long | 否 | 父分类ID，不传则从根节点开始 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "服装",
      "level": 1,
      "children": [
        {
          "id": 2,
          "name": "男装",
          "level": 2,
          "children": []
        }
      ]
    }
  ]
}
```

---

### 3.8 获取一级分类列表

- **路径**: `GET /category/level1`

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### 3.9 获取子分类列表

- **路径**: `GET /category/children/{parentId}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| parentId | Long | 父分类ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

## 4. 商品SPU管理

### 4.1 新增商品（支持图片上传）

- **路径**: `POST /spu/add`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuDto | String (JSON) | 是 | 商品信息JSON字符串 |
| mainImageFile | MultipartFile | 否 | 主图文件 |
| imageFiles | MultipartFile[] | 否 | 商品图片集文件数组 |

**spuDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 商品名称 |
| categoryId | Long | 是 | 分类ID |
| brandId | Long | 否 | 品牌ID |
| description | String | 否 | 商品描述 |
| unit | String | 否 | 单位 |
| keywords | String | 否 | 关键词 |
| status | Integer | 否 | 状态，默认1 |

**返回数据**:

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "mainImage": "2026/05/08/uuid_filename.jpg"
  }
}
```

---

### 4.2 删除商品

- **路径**: `DELETE /spu/delete/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 商品ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 4.3 更新商品信息

- **路径**: `PUT /spu/update`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| spuDto | String (JSON) | 是 | 商品信息JSON字符串 |
| mainImageFile | MultipartFile | 否 | 新主图文件 |
| imageFiles | MultipartFile[] | 否 | 新商品图片集文件数组 |

**spuDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 商品ID |
| name | String | 否 | 商品名称 |
| categoryId | Long | 否 | 分类ID |
| brandId | Long | 否 | 品牌ID |
| description | String | 否 | 商品描述 |
| unit | String | 否 | 单位 |
| keywords | String | 否 | 关键词 |
| status | Integer | 否 | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "mainImage": "2026/05/08/uuid_filename.jpg"
  }
}
```

---

### 4.4 获取商品详情

- **路径**: `GET /spu/detail/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 商品ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "小米14 Pro",
    "categoryId": 27,
    "brandId": 3,
    "description": "小米旗舰手机",
    "mainImage": "2026/05/08/uuid_filename.jpg",
    "images": "[\"2026/05/08/uuid1.jpg\",\"2026/05/08/uuid2.jpg\"]",
    "unit": "台",
    "keywords": "小米,14,手机",
    "sales": 1000,
    "status": 1,
    "createdAt": "2026-05-08T10:00:00",
    "updatedAt": "2026-05-08T10:00:00"
  }
}
```

---

### 4.5 获取商品列表

- **路径**: `GET /spu/list`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 商品名称模糊搜索 |
| categoryId | Long | 否 | 分类ID |
| brandId | Long | 否 | 品牌ID |
| status | Integer | 否 | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### 4.6 分页获取商品列表（支持搜索）

- **路径**: `GET /spu/page`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 商品名称模糊搜索 |
| categoryId | Long | 否 | 分类ID |
| brandId | Long | 否 | 品牌ID |
| status | Integer | 否 | 状态 |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "page": 1,
    "pageSize": 10,
    "total": 100
  }
}
```

---

## 5. 品牌管理

### 5.1 新增品牌（支持Logo上传）

- **路径**: `POST /brand/add`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| brandDto | String (JSON) | 是 | 品牌信息JSON字符串 |
| logoFile | MultipartFile | 否 | 品牌Logo文件 |

**brandDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 品牌名称 |
| description | String | 否 | 品牌描述 |
| website | String | 否 | 品牌官网 |
| sort | Integer | 否 | 排序号，默认0 |
| status | Integer | 否 | 状态，默认1 |

**返回数据**:

```json
{
  "code": 200,
  "message": "添加成功",
  "data": {
    "id": 1,
    "logo": "2026/05/08/uuid_filename.png"
  }
}
```

---

### 5.2 删除品牌

- **路径**: `DELETE /brand/delete/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 品牌ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 5.3 更新品牌信息

- **路径**: `PUT /brand/update`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| brandDto | String (JSON) | 是 | 品牌信息JSON字符串 |
| logoFile | MultipartFile | 否 | 新Logo文件 |

**brandDto JSON结构**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 品牌ID |
| name | String | 否 | 品牌名称 |
| description | String | 否 | 品牌描述 |
| website | String | 否 | 品牌官网 |
| sort | Integer | 否 | 排序号 |
| status | Integer | 否 | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "logo": "2026/05/08/uuid_filename.png"
  }
}
```

---

### 5.4 获取品牌详情

- **路径**: `GET /brand/detail/{id}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 品牌ID |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "华为",
    "logo": "2026/05/08/uuid_filename.png",
    "description": "华为技术有限公司",
    "website": "https://www.huawei.com",
    "sort": 1,
    "status": 1,
    "createdAt": "2026-05-08T10:00:00",
    "updatedAt": "2026-05-08T10:00:00"
  }
}
```

---

### 5.5 获取品牌列表

- **路径**: `GET /brand/list`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 品牌名称模糊搜索 |
| status | Integer | 否 | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### 5.6 分页获取品牌列表

- **路径**: `GET /brand/page`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 品牌名称模糊搜索 |
| status | Integer | 否 | 状态 |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [...],
    "page": 1,
    "pageSize": 10,
    "total": 100
  }
}
```

---

### 5.7 根据状态获取品牌列表

- **路径**: `GET /brand/status/{status}`

**路径参数**:

| 参数名 | 类型 | 说明 |
|--------|------|------|
| status | Integer | 状态 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### 5.8 搜索品牌

- **路径**: `GET /brand/search`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 是 | 搜索关键词 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

### 5.9 按排序获取品牌

- **路径**: `GET /brand/sort`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 否 | 状态筛选 |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": [...]
}
```

---

## 6. 文件管理

### 6.1 上传通用文件

- **路径**: `POST /file/upload`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 文件 |

**返回数据**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "2026/05/08/uuid_filename.jpg",
    "filename": "uuid_filename.jpg",
    "size": 102400
  }
}
```

---

### 6.2 上传图片文件

- **路径**: `POST /file/upload/image`
- **Content-Type**: `multipart/form-data`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | MultipartFile | 是 | 图片文件 |

**返回数据**:

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "2026/05/08/uuid_filename.jpg",
    "filename": "uuid_filename.jpg",
    "size": 102400
  }
}
```

---

### 6.3 删除文件

- **路径**: `DELETE /file/delete`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| url | String | 是 | 文件URL |

**返回数据**:

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

### 6.4 下载文件

- **路径**: `GET /file/download`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| url | String | 是 | 文件URL |

**返回数据**: 文件流

---

### 6.5 预览图片

- **路径**: `GET /file/preview`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| url | String | 是 | 图片URL |

**返回数据**: 图片流

---

### 6.6 获取文件URL

- **路径**: `GET /file/url`

**前端传入参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| url | String | 是 | 文件URL |

**返回数据**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "http://localhost:8080/2026/05/08/uuid_filename.jpg"
  }
}
```

---

**文档结束**
