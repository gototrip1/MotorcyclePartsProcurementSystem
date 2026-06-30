# 摩托车零部件采购管理系统 API 接口文档

## 基本信息

| 项目 | 说明 |
|------|------|
| **API 文档地址** | http://localhost:8080/swagger-ui/index.html |
| **基础路径** | http://localhost:8080/api |
| **响应格式** | JSON |
| **认证方式** | Bearer Token（登录后获取 token，放入 Authorization 头） |
| **字符编码** | UTF-8 |

---

## 目录

1. [通用说明](#通用说明)
2. [认证管理](#1-认证管理)
3. [供应商管理](#2-供应商管理)
4. [零部件管理](#3-零部件管理)
5. [采购订单管理](#4-采购订单管理)
6. [库存管理](#5-库存管理)
7. [领用单管理](#6-领用单管理)
8. [审核管理](#7-审核管理)
9. [用户管理](#8-用户管理)
10. [统计分析](#9-统计分析)

---

## 通用说明

### 统一响应格式

所有 API 接口均遵循以下统一响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1746508800000
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200 表示成功，401 表示未登录/登录过期 |
| message | String | 操作结果描述信息 |
| data | Object/Array | 返回的数据对象，失败时为 null |
| timestamp | Long | 响应时间戳（毫秒） |

### 分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [ ... ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "timestamp": 1746508800000
}
```

**分页字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| records | Array | 当前页的数据列表 |
| total | Long | 总记录数 |
| size | Long | 每页记录数 |
| current | Long | 当前页码（从 1 开始） |
| pages | Long | 总页数 |

### 认证说明

除登录接口外，其他接口均需在请求头中携带 token：

```
Authorization: Bearer <token>
```

token 通过登录接口获取，登录成功后前端会自动存储并在后续请求中携带。

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录或登录过期 |
| 500 | 操作失败 |
| 601 | 数据已存在（如订单编号、零件编码重复） |
| 602 | 数据不存在 |
| 603 | 库存不足 |
| 604 | 订单状态异常 |
| 605 | 供应商状态异常 |

### 枚举值说明

**采购订单状态 (purchase_order.status)**

| 值 | 说明 | 标签颜色 |
|----|------|---------|
| 1 | 采购中（等待供应商发货） | info |
| 2 | 待入库审核（货已到，需管理员审核） | warning |
| 3 | 已入库（审核通过，库存已增加） | success |
| 4 | 已取消 | danger |

**领用单状态 (stock_out_order.status)**

| 值 | 说明 | 标签颜色 |
|----|------|---------|
| 1 | 待审核（等待管理员审核出库） | warning |
| 2 | 已出库（审核通过，库存已扣减） | success |
| 3 | 已取消 | danger |

**供应商状态 (supplier.status)**

| 值 | 说明 | 标签颜色 |
|----|------|---------|
| 1 | 合作中 | success |
| 2 | 已终止 | danger |
| 3 | 审核中 | warning |

**用户角色 (user.role)**

| 值 | 说明 |
|----|------|
| admin | 系统管理员（全局管理权限，可审核入库和出库） |
| purchase | 采购专员（负责创建采购订单、跟踪采购进度） |
| requisition | 领用人员（发起领用申请） |

**用户状态 (user.status)**

| 值 | 说明 | 标签颜色 |
|----|------|---------|
| 1 | 正常 | success |
| 2 | 禁用 | danger |

**供应商信用评级 (supplier.creditRating)**

| 值 | 说明 |
|----|------|
| A | 优秀 |
| B | 良好 |
| C | 一般 |
| D | 较差 |

**零部件分类 (part.category)**

| 值 | 说明 |
|----|------|
| 发动机类 | 发动机类配件 |
| 车架类 | 车架类配件 |
| 电气类 | 电气类配件 |
| 制动类 | 制动类配件 |
| 传动类 | 传动类配件 |
| 外观件 | 外观类配件 |

**领用用途 (stock_out_order.purpose)**

| 值 | 说明 |
|----|------|
| 生产 | 生产领用 |
| 维修 | 维修领用 |
| 报废 | 报废处理 |
| 其他 | 其他用途 |

**付款状态 (purchase_order.paid)**

| 值 | 说明 | 标签颜色 |
|----|------|---------|
| 0 | 未付款 | info |
| 1 | 已付款 | success |

---

## 1. 认证管理

**基础路径**: `/api/auth`

### 1.1 用户登录

用户登录认证，成功后返回 token 和用户信息。

**请求**

```
POST /api/auth/login
Content-Type: application/json
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | **是** | 用户名 |
| password | String | **是** | 密码 |

**请求示例：**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "id": 1,
    "username": "admin",
    "realName": "系统管理员",
    "role": "admin",
    "department": "管理部"
  },
  "timestamp": 1746508800000
}
```

---

### 1.2 用户退出

退出登录，清除当前用户的认证状态。

**请求**

```
POST /api/auth/logout
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1746508800000
}
```

---

## 2. 供应商管理

**基础路径**: `/api/suppliers`

### 2.1 创建供应商

添加新的供应商信息。

**请求**

```
POST /api/suppliers/create
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| supplierCode | String | **是** | 供应商编码（唯一标识） |
| name | String | **是** | 供应商名称 |
| contactPerson | String | 否 | 联系人 |
| phone | String | 否 | 联系电话 |
| email | String | 否 | 邮箱地址 |
| address | String | 否 | 地址 |
| creditRating | String | 否 | 信用评级（A/B/C/D），默认 B |
| status | Integer | 否 | 合作状态，默认 1（合作中） |

**请求示例：**

```json
{
  "supplierCode": "SUP005",
  "name": "重庆隆鑫发动机有限公司",
  "contactPerson": "陈经理",
  "phone": "023-68001001",
  "email": "sup005@longxin.com",
  "address": "重庆市九龙坡区工业园1号",
  "creditRating": "A",
  "status": 1
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5,
    "supplierCode": "SUP005",
    "name": "重庆隆鑫发动机有限公司",
    "contactPerson": "陈经理",
    "phone": "023-68001001",
    "email": "sup005@longxin.com",
    "address": "重庆市九龙坡区工业园1号",
    "creditRating": "A",
    "status": 1,
    "deleted": 0,
    "createTime": "2026-06-30T10:00:00",
    "updateTime": "2026-06-30T10:00:00"
  },
  "timestamp": 1746508800000
}
```

---

### 2.2 更新供应商

根据 ID 更新供应商信息。

**请求**

```
PUT /api/suppliers/update/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | **是** | 供应商 ID |

**请求参数：** 同创建供应商

---

### 2.3 删除供应商

逻辑删除供应商信息。

**请求**

```
DELETE /api/suppliers/delete/{id}
Authorization: Bearer <token>
```

---

### 2.4 获取单个供应商

根据 ID 获取供应商详情。

**请求**

```
GET /api/suppliers/get/{id}
Authorization: Bearer <token>
```

---

### 2.5 分页查询供应商

分页查询供应商列表，支持多条件筛选。

**请求**

```
GET /api/suppliers/page?current=1&size=10&name=&status=&creditRating=
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码，默认 1 |
| size | Long | 否 | 每页大小，默认 10 |
| name | String | 否 | 供应商名称（模糊匹配） |
| status | Integer | 否 | 合作状态（精确匹配） |
| creditRating | String | 否 | 信用评级（精确匹配） |

---

### 2.6 搜索供应商

根据名称关键字搜索供应商列表。

**请求**

```
GET /api/suppliers/search?name=隆鑫
Authorization: Bearer <token>
```

---

### 2.7 更新供应商状态

更新供应商的合作状态。

**请求**

```
PATCH /api/suppliers/update-status/{id}?status=2
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | **是** | 新状态（1-合作中，2-已终止，3-审核中） |

---

### 2.8 更新供应商信用评级

更新供应商的信用评级。

**请求**

```
PATCH /api/suppliers/update-credit-rating/{id}?creditRating=B
Authorization: Bearer <token>
```

---

## 3. 零部件管理

**基础路径**: `/api/parts`

### 3.1 创建零部件

添加新的零部件信息。

**请求**

```
POST /api/parts/create
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| partCode | String | **是** | 零件编码（唯一标识） |
| name | String | **是** | 零件名称 |
| model | String | 否 | 型号 |
| specification | String | 否 | 规格 |
| unit | String | 否 | 单位（个/套/件/台/箱），默认"个" |
| purchasePrice | BigDecimal | **是** | 采购单价（不能小于 0） |
| suggestedRetailPrice | BigDecimal | 否 | 建议零售价 |
| stockWarningValue | Integer | 否 | 库存预警值，默认 10 |
| supplierId | Long | 否 | 供应商 ID |
| category | String | 否 | 分类（发动机类/车架类/电气类/制动类/传动类/外观件） |
| description | String | 否 | 零件描述 |

**请求示例：**

```json
{
  "partCode": "P006",
  "name": "活塞",
  "model": "CG125",
  "specification": "标准缸径 56.5mm",
  "unit": "个",
  "purchasePrice": 45.00,
  "suggestedRetailPrice": 78.00,
  "stockWarningValue": 20,
  "supplierId": 1,
  "category": "发动机类",
  "description": "通用 125cc 发动机活塞"
}
```

---

### 3.2 更新零部件

根据 ID 更新零部件信息。

**请求**

```
PUT /api/parts/update/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

---

### 3.3 删除零部件

逻辑删除零部件信息。

**请求**

```
DELETE /api/parts/delete/{id}
Authorization: Bearer <token>
```

---

### 3.4 获取单个零部件

根据 ID 获取零部件详情。

**请求**

```
GET /api/parts/get/{id}
Authorization: Bearer <token>
```

---

### 3.5 分页查询零部件

分页查询零部件列表，支持多条件筛选。

**请求**

```
GET /api/parts/page?current=1&size=10&name=&category=&supplierId=
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码，默认 1 |
| size | Long | 否 | 每页大小，默认 10 |
| name | String | 否 | 零件名称（模糊匹配） |
| category | String | 否 | 分类（精确匹配） |
| supplierId | Long | 否 | 供应商 ID（精确匹配） |

---

### 3.6 搜索零部件

根据名称关键字搜索零部件列表。

**请求**

```
GET /api/parts/search?name=活塞
Authorization: Bearer <token>
```

---

### 3.7 更新采购单价

快速更新零部件的采购单价（仅管理员可操作）。

**请求**

```
PATCH /api/parts/update-price/{id}?purchasePrice=50.00
Authorization: Bearer <token>
```

---

### 3.8 获取供应商的产品列表

获取指定供应商供应的所有零部件。

**请求**

```
GET /api/parts/supplier/{supplierId}
Authorization: Bearer <token>
```

---

## 4. 采购订单管理

**基础路径**: `/api/orders`

> **说明**：物流信息（物流公司、运单号、发货时间）已内置在采购订单表中，不再单独建物流表。

### 4.1 创建采购订单

创建新的采购订单，包含订单基本信息和订单明细。

**请求**

```
POST /api/orders/create
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderNumber | String | **是** | 订单编号（唯一标识） |
| totalAmount | BigDecimal | 否 | 订单总金额（不传则自动根据明细计算） |
| status | Integer | 否 | 订单状态，默认 1（采购中） |
| paid | Integer | 否 | 付款标志，默认 0（未付款） |
| orderTime | LocalDateTime | 否 | 下单时间，默认当前时间 |
| expectedDeliveryDate | LocalDate | 否 | 预计交货日期 |
| remark | String | 否 | 备注 |
| orderDetail | Array | **是** | 订单明细列表，至少包含一个明细 |

**orderDetail 数组中的对象参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| partId | Long | **是** | 零部件 ID |
| quantity | Integer | **是** | 采购数量，最小值为 1 |
| unitPrice | BigDecimal | **是** | 采购单价 |
| remark | String | 否 | 明细备注 |

**请求示例：**

```json
{
  "orderNumber": "PO20260630001",
  "expectedDeliveryDate": "2026-07-05",
  "remark": "紧急采购",
  "orderDetail": [
    {
      "partId": 1,
      "quantity": 20,
      "unitPrice": 120.00,
      "remark": "活塞采购"
    },
    {
      "partId": 2,
      "quantity": 100,
      "unitPrice": 8.00
    }
  ]
}
```

---

### 4.2 更新采购订单

根据 ID 更新采购订单信息。

**请求**

```
PUT /api/orders/update/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

---

### 4.3 删除采购订单

逻辑删除采购订单（仅状态为 1-采购中或 4-已取消时可删除）。

**请求**

```
DELETE /api/orders/delete/{id}
Authorization: Bearer <token>
```

---

### 4.4 获取单个订单

根据 ID 获取采购订单详情。

**请求**

```
GET /api/orders/get/{id}
Authorization: Bearer <token>
```

---

### 4.5 获取订单详情（分离结构）

根据 ID 获取订单详情，返回订单和明细分离的结构。

**请求**

```
GET /api/orders/details/{id}
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "order": {
      "id": 1,
      "orderNumber": "PO20260601001",
      "totalAmount": 3200.00,
      "status": 3,
      "paid": 1,
      "orderTime": "2026-06-01T09:30:00",
      "expectedDeliveryDate": "2026-06-05",
      "actualDeliveryDate": "2026-06-04",
      "logisticsCompany": "顺丰物流",
      "trackingNumber": "SF1234567890",
      "shipTime": "2026-06-02T08:00:00",
      "createdBy": 2,
      "remark": "已入库并付款"
    },
    "details": [
      {
        "id": 1,
        "orderId": 1,
        "partId": 1,
        "quantity": 20,
        "unitPrice": 120.00,
        "subtotal": 2400.00,
        "remark": "活塞采购"
      }
    ]
  },
  "timestamp": 1746508800000
}
```

---

### 4.6 分页查询订单

分页查询采购订单列表，支持多条件筛选。

**请求**

```
GET /api/orders/page?current=1&size=10&orderNumber=&status=&startDate=&endDate=
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码，默认 1 |
| size | Long | 否 | 每页大小，默认 10 |
| orderNumber | String | 否 | 订单编号（模糊匹配） |
| status | Integer | 否 | 订单状态 |
| startDate | String | 否 | 开始日期，格式：yyyy-MM-dd |
| endDate | String | 否 | 结束日期，格式：yyyy-MM-dd |

---

### 4.7 更新订单状态

更新采购订单的状态。

**请求**

```
PATCH /api/orders/update-status/{id}?status=2
Authorization: Bearer <token>
```

---

### 4.8 更新物流信息

更新采购订单的物流信息（物流公司、运单号、发货时间、预计交货日期）。

**请求**

```
PATCH /api/orders/update-logistics/{id}?logisticsCompany=顺丰物流&trackingNumber=SF123456&shipTime=2026-06-30 10:00:00&expectedDeliveryDate=2026-07-05
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| logisticsCompany | String | 否 | 物流公司 |
| trackingNumber | String | 否 | 运单号 |
| shipTime | String | 否 | 发货时间，格式：yyyy-MM-dd HH:mm:ss |
| expectedDeliveryDate | String | 否 | 预计交货日期，格式：yyyy-MM-dd |

---

### 4.9 提交入库审核

采购专员确认货已到，将订单状态从 1（采购中）变更为 2（待入库审核）。

**请求**

```
PATCH /api/orders/submit-inbound/{id}
Authorization: Bearer <token>
```

---

### 4.10 标记已付款

将订单标记为已付款。

**请求**

```
PATCH /api/orders/mark-paid/{id}
Authorization: Bearer <token>
```

---

### 4.11 获取采购统计

获取指定时间范围内的采购订单统计数据。

**请求**

```
GET /api/orders/statistics?startDate=2026-01-01&endDate=2026-06-30
Authorization: Bearer <token>
```

---

### 4.12 搜索订单明细

根据零部件名称和时间范围搜索订单明细。

**请求**

```
GET /api/orders/search-details?partName=活塞&startDate=2026-01-01&endDate=2026-06-30
Authorization: Bearer <token>
```

---

## 5. 库存管理

**基础路径**: `/api/inventory`

> **重要说明**：库存变更仅由管理员审核通过的入库/出库操作触发。前端不提供直接修改库存数量的入口，保证库存数据的安全性和可追溯性。

### 5.1 获取库存记录

根据 ID 获取库存详情（包含零部件信息）。

**请求**

```
GET /api/inventory/get/{id}
Authorization: Bearer <token>
```

---

### 5.2 分页查询库存

分页查询库存列表，支持多条件筛选。

**请求**

```
GET /api/inventory/page?current=1&size=10&partName=&warehouseLocation=
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码，默认 1 |
| size | Long | 否 | 每页大小，默认 10 |
| partName | String | 否 | 零件名称（模糊匹配） |
| warehouseLocation | String | 否 | 仓库位置（模糊匹配） |

---

### 5.3 获取库存预警列表

获取当前库存数量低于库存预警值的记录列表。

**请求**

```
GET /api/inventory/warning
Authorization: Bearer <token>
```

---

### 5.4 库存盘点

获取库存盘点统计数据和所有库存明细。

**请求**

```
GET /api/inventory/check
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalItems": 5,
    "warningItems": 2,
    "normalItems": 3,
    "inventoryList": [ ... ]
  },
  "timestamp": 1746508800000
}
```

---

### 5.5 更新安全库存

更新指定库存记录的安全库存量。

**请求**

```
PATCH /api/inventory/update-safety-stock/{id}?safetyStock=20
Authorization: Bearer <token>
```

---

## 6. 领用单管理

**基础路径**: `/api/stock-out-orders`

### 6.1 创建领用单

领用人员发起领用申请。

**请求**

```
POST /api/stock-out-orders/create
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| stockOutNumber | String | **是** | 领用单号（唯一标识） |
| userId | Long | **是** | 领用人 ID（通常为当前登录用户） |
| department | String | 否 | 领用部门 |
| purpose | String | 否 | 用途（生产/维修/报废/其他），默认"生产" |
| remark | String | 否 | 备注 |
| stockOutDetail | Array | **是** | 领用明细列表 |

**stockOutDetail 数组中的对象参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| partId | Long | **是** | 零部件 ID |
| quantity | Integer | **是** | 领用数量 |
| remark | String | 否 | 明细备注 |

**请求示例：**

```json
{
  "stockOutNumber": "SO20260630001",
  "userId": 4,
  "department": "生产车间",
  "purpose": "生产",
  "remark": "生产线领用",
  "stockOutDetail": [
    {
      "partId": 1,
      "quantity": 5
    },
    {
      "partId": 3,
      "quantity": 10
    }
  ]
}
```

---

### 6.2 更新领用单

根据 ID 更新领用单信息（仅状态为 1-待审核时可更新）。

**请求**

```
PUT /api/stock-out-orders/update/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

---

### 6.3 删除领用单

逻辑删除领用单（仅状态为 1-待审核时可删除）。

**请求**

```
DELETE /api/stock-out-orders/delete/{id}
Authorization: Bearer <token>
```

---

### 6.4 获取领用单详情

根据 ID 获取领用单详情（包含领用明细）。

**请求**

```
GET /api/stock-out-orders/get/{id}
Authorization: Bearer <token>
```

---

### 6.5 获取领用明细

根据领用单 ID 获取明细列表。

**请求**

```
GET /api/stock-out-orders/details/{id}
Authorization: Bearer <token>
```

---

### 6.6 分页查询领用单

分页查询领用单列表，支持多条件筛选。

**请求**

```
GET /api/stock-out-orders/page?current=1&size=10&stockOutNumber=&status=&userId=
Authorization: Bearer <token>
```

---

### 6.7 取消领用单

将领用单状态变更为 3-已取消（仅状态为 1-待审核时可取消）。

**请求**

```
PATCH /api/stock-out-orders/cancel/{id}
Authorization: Bearer <token>
```

---

## 7. 审核管理

**基础路径**: `/api/audit`

> **说明**：审核操作仅管理员（admin 角色）可执行。审核通过后会自动变更库存数量。

### 7.1 入库审核通过

审核通过采购订单入库，订单状态变为 3-已入库，同时库存数量增加。

**请求**

```
POST /audit/inbound/approve/{orderId}
Authorization: Bearer <token>
```

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | **是** | 采购订单 ID（需处于 2-待入库审核状态） |

**响应示例：**

```json
{
  "code": 200,
  "message": "入库审核通过，库存已增加",
  "data": null,
  "timestamp": 1746508800000
}
```

---

### 7.2 入库审核拒绝

拒绝采购订单入库，订单状态保持不变，需提供拒绝原因。

**请求**

```
POST /audit/inbound/reject/{orderId}?reason=货物质量问题
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| reason | String | **是** | 拒绝原因 |

---

### 7.3 出库审核通过

审核通过领用单出库，领用单状态变为 2-已出库，同时库存数量减少。

**请求**

```
POST /audit/outbound/approve/{stockOutId}
Authorization: Bearer <token>
```

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| stockOutId | Long | **是** | 领用单 ID（需处于 1-待审核状态） |

**注意**：如果库存不足，审核会失败，返回 603 状态码。

---

### 7.4 出库审核拒绝

拒绝领用单出库，领用单状态保持不变。

**请求**

```
POST /audit/outbound/reject/{stockOutId}?reason=库存不足，暂缓出库
Authorization: Bearer <token>
```

---

## 8. 用户管理

**基础路径**: `/api/users`

### 8.1 创建用户

添加新用户（仅管理员可操作）。

**请求**

```
POST /api/users/create
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | **是** | 用户名（唯一） |
| password | String | **是** | 密码（会自动加密存储） |
| realName | String | 否 | 真实姓名 |
| role | String | **是** | 角色（admin/purchase/requisition） |
| department | String | 否 | 部门 |
| phone | String | 否 | 电话 |
| email | String | 否 | 邮箱 |
| status | Integer | 否 | 状态，默认 1（正常） |

---

### 8.2 更新用户

根据 ID 更新用户信息。

**请求**

```
PUT /api/users/update/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

---

### 8.3 删除用户

逻辑删除用户。

**请求**

```
DELETE /api/users/delete/{id}
Authorization: Bearer <token>
```

---

### 8.4 获取单个用户

根据 ID 获取用户详情。

**请求**

```
GET /api/users/get/{id}
Authorization: Bearer <token>
```

---

### 8.5 分页查询用户

分页查询用户列表。

**请求**

```
GET /api/users/page?current=1&size=10&username=&role=&status=
Authorization: Bearer <token>
```

---

### 8.6 分配角色

更新用户的角色（仅管理员可操作）。

**请求**

```
PATCH /api/users/assign-role/{id}?role=purchase
Authorization: Bearer <token>
```

---

### 8.7 启用用户

将用户状态设为 1-正常。

**请求**

```
PATCH /api/users/enable/{id}
Authorization: Bearer <token>
```

---

### 8.8 禁用用户

将用户状态设为 2-禁用（禁用后该用户无法登录）。

**请求**

```
PATCH /api/users/disable/{id}
Authorization: Bearer <token>
```

---

### 8.9 重置密码

重置用户密码（仅管理员可操作）。

**请求**

```
PATCH /api/users/reset-password/{id}?password=newPassword123
Authorization: Bearer <token>
```

---

### 8.10 修改密码

用户修改自己的密码，需校验原密码。

**请求**

```
PATCH /api/users/change-password
Content-Type: application/json
Authorization: Bearer <token>
```

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | **是** | 当前用户 ID |
| oldPassword | String | **是** | 原密码 |
| newPassword | String | **是** | 新密码 |

---

## 9. 统计分析

**基础路径**: `/api/statistics`

### 9.1 获取仪表盘统计数据

获取系统首页概览统计数据。

**请求**

```
GET /api/statistics/dashboard
Authorization: Bearer <token>
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "supplierCount": 4,
    "partCount": 5,
    "userCount": 5,
    "inventoryCount": 5,
    "pendingInboundCount": 1,
    "pendingOutboundCount": 2,
    "warningInventoryCount": 2
  },
  "timestamp": 1746508800000
}
```

**响应字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| supplierCount | Long | 供应商总数 |
| partCount | Long | 零部件总数 |
| userCount | Long | 用户总数 |
| inventoryCount | Long | 库存记录数 |
| pendingInboundCount | Long | 待入库审核订单数（status=2） |
| pendingOutboundCount | Long | 待出库审核领用单数（status=1） |
| warningInventoryCount | Long | 库存预警数量 |

---

### 9.2 获取供应商统计

获取供应商的分类统计数据。

**请求**

```
GET /api/statistics/suppliers
Authorization: Bearer <token>
```

---

### 9.3 获取零部件统计

获取零部件的分类统计数据。

**请求**

```
GET /api/statistics/parts
Authorization: Bearer <token>
```

---

### 9.4 获取库存状态统计

获取库存预警相关的统计数据。

**请求**

```
GET /api/statistics/inventory
Authorization: Bearer <token>
```

---

### 9.5 获取月度采购趋势

获取指定时间范围内的月度采购趋势数据。

**请求**

```
GET /api/statistics/monthly-trend?startDateParam=2026-01-01&endDateParam=2026-06-30
Authorization: Bearer <token>
```

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDateParam | String | 否 | 开始日期，格式：yyyy-MM-dd |
| endDateParam | String | 否 | 结束日期，格式：yyyy-MM-dd |

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "month": "2026-01",
      "orderCount": 10,
      "totalAmount": 50000
    },
    {
      "month": "2026-02",
      "orderCount": 15,
      "totalAmount": 75000
    }
  ],
  "timestamp": 1746508800000
}
```

---

## 错误码详细说明

| 状态码 | 说明 | 可能原因 |
|--------|------|----------|
| 200 | 操作成功 | - |
| 400 | 参数错误 | 缺少必填参数、参数格式不正确 |
| 401 | 未登录或登录过期 | token 无效或已过期 |
| 500 | 操作失败 | 服务器内部错误 |
| 601 | 数据已存在 | 重复插入相同编码的记录 |
| 602 | 数据不存在 | 尝试操作不存在的记录 |
| 603 | 库存不足 | 出库数量超过当前库存 |
| 604 | 订单状态异常 | 订单状态不允许当前操作 |
| 605 | 供应商状态异常 | 供应商已终止合作 |

---

## 数据类型说明

| 类型 | 说明 | 示例 |
|------|------|------|
| Long | 64 位整数 | 1, 100, 123456789 |
| Integer | 32 位整数 | 1, 100, 1000 |
| BigDecimal | 高精度小数（金额） | 500.00, 1234.56 |
| String | 字符串 | "发动机", "SF123456" |
| LocalDate | 日期 | 2026-06-30 |
| LocalDateTime | 日期时间 | 2026-06-30T10:00:00 |

---

## 时间格式说明

| 格式 | 模式 | 示例 |
|------|------|------|
| 日期 | yyyy-MM-dd | 2026-06-30 |
| 日期时间 | yyyy-MM-dd HH:mm:ss | 2026-06-30 10:00:00 |
| ISO 日期时间 | yyyy-MM-dd'T'HH:mm:ss | 2026-06-30T10:00:00 |

---

*文档更新时间：2026-06-30*