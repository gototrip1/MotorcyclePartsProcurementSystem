# 摩托车零部件采购管理系统 API 文档

## 版本信息
- **版本**：1.0.0
- **基础URL**：`http://localhost:8080/api`
- **API文档**：`http://localhost:8080/swagger-ui.html`

## 概述

本文档描述了摩托车零部件采购管理系统的RESTful API接口。

## 通用说明

### 统一响应格式

所有接口响应都采用以下JSON格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... },
  "timestamp": 1609459200000
}
```

### 响应码说明

| 响应码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 600 | 业务异常 |
| 601 | 数据已存在 |
| 602 | 数据不存在 |
| 603 | 库存不足 |
| 604 | 订单状态异常 |

### 分页参数

分页查询使用以下参数：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码，默认1 |
| size | Long | 否 | 每页大小，默认10 |

## 接口列表

### 客户管理接口

| 方法 | URL | 描述 |
|------|-----|------|
| POST | `/api/customers/create` | 创建客户 |
| PUT | `/api/customers/update/{id}` | 更新客户 |
| DELETE | `/api/customers/delete/{id}` | 删除客户 |
| GET | `/api/customers/get/{id}` | 获取单个客户 |
| GET | `/api/customers/page` | 分页查询客户 |
| GET | `/api/customers/search` | 搜索客户 |
| PATCH | `/api/customers/update-type/{id}` | 更新客户类型 |
| PATCH | `/api/customers/update-discount/{id}` | 更新折扣等级 |

### 库存管理接口

| 方法 | URL | 描述 |
|------|-----|------|
| GET | `/api/inventory/get/{id}` | 获取库存记录 |
| GET | `/api/inventory/page` | 分页查询库存 |
| POST | `/api/inventory/inbound` | 入库操作 |
| POST | `/api/inventory/outbound` | 出库操作 |
| GET | `/api/inventory/warning` | 获取库存预警列表 |
| PATCH | `/api/inventory/update-safety-stock/{id}` | 更新安全库存 |
| GET | `/api/inventory/check` | 库存盘点 |

### 物流管理接口

| 方法 | URL | 描述 |
|------|-----|------|
| POST | `/api/logistics/create` | 创建物流记录 |
| PUT | `/api/logistics/update/{id}` | 更新物流信息 |
| DELETE | `/api/logistics/delete/{id}` | 删除物流记录 |
| GET | `/api/logistics/get/{id}` | 获取物流记录 |
| GET | `/api/logistics/page` | 分页查询物流信息 |
| GET | `/api/logistics/order/{orderId}` | 根据订单获取物流信息 |
| PATCH | `/api/logistics/update-status/{id}` | 更新物流状态 |
| PATCH | `/api/logistics/update-shipping/{id}` | 更新发货信息 |
| PATCH | `/api/logistics/update-receiving/{id}` | 更新收货信息 |

### 产品/零部件管理接口

| 方法 | URL | 描述 |
|------|-----|------|
| POST | `/api/parts/create` | 创建零部件 |
| PUT | `/api/parts/update/{id}` | 更新零部件 |
| DELETE | `/api/parts/delete/{id}` | 删除零部件 |
| GET | `/api/parts/get/{id}` | 获取单个零部件 |
| GET | `/api/parts/page` | 分页查询零部件 |
| GET | `/api/parts/search` | 搜索零部件 |
| PATCH | `/api/parts/update-price/{id}` | 更新采购单价 |
| GET | `/api/parts/supplier/{supplierId}` | 获取供应商的产品列表 |

### 采购订单管理接口

| 方法 | URL | 描述 |
|------|-----|------|
| POST | `/api/orders/create` | 创建采购订单 |
| PUT | `/api/orders/update/{id}` | 更新采购订单 |
| DELETE | `/api/orders/delete/{id}` | 删除采购订单 |
| GET | `/api/orders/get/{id}` | 获取单个订单 |
| GET | `/api/orders/details/{id}` | 获取订单详情 |
| GET | `/api/orders/page` | 分页查询订单 |
| PATCH | `/api/orders/update-status/{id}` | 更新订单状态 |
| GET | `/api/orders/statistics` | 获取采购统计 |

### 供应商管理接口

| 方法 | URL | 描述 |
|------|-----|------|
| POST | `/api/suppliers/create` | 创建供应商 |
| PUT | `/api/suppliers/update/{id}` | 更新供应商 |
| DELETE | `/api/suppliers/delete/{id}` | 删除供应商 |
| GET | `/api/suppliers/get/{id}` | 获取单个供应商 |
| GET | `/api/suppliers/page` | 分页查询供应商 |
| GET | `/api/suppliers/search` | 搜索供应商 |
| PATCH | `/api/suppliers/update-status/{id}` | 更新供应商状态 |
| PATCH | `/api/suppliers/update-credit-rating/{id}` | 更新信用评级 |

### 统计分析接口

| 方法 | URL | 描述 |
|------|-----|------|
| GET | `/api/statistics/dashboard` | 获取仪表盘统计数据 |
| GET | `/api/statistics/suppliers` | 获取供应商统计 |
| GET | `/api/statistics/parts` | 获取产品分类统计 |
| GET | `/api/statistics/inventory` | 获取库存预警统计 |
| GET | `/api/statistics/monthly-trend` | 获取月度采购趋势 |

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 100,
    "pages": 10,
    "records": [ ... ]
  },
  "timestamp": 1609459200000
}
```

---

## 供应商管理接口

### 1. 创建供应商

**请求**
```
POST /api/suppliers
Content-Type: application/json

{
  "supplierCode": "SUP00001",
  "name": "博世汽车配件有限公司",
  "contactPerson": "张三",
  "phone": "13800138000",
  "email": "bosch@example.com",
  "address": "上海市嘉定区某路1号",
  "creditRating": "A",
  "status": 1
}
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "supplierCode": "SUP00001",
    "name": "博世汽车配件有限公司",
    ...
  }
}
```

### 2. 更新供应商

**请求**
```
PUT /api/suppliers/{id}
Content-Type: application/json

{
  "name": "博世汽车配件（上海）有限公司",
  "creditRating": "A+",
  "status": 1
}
```

### 3. 删除供应商

**请求**
```
DELETE /api/suppliers/{id}
```

### 4. 获取单个供应商

**请求**
```
GET /api/suppliers/{id}
```

### 5. 分页查询供应商

**请求**
```
GET /api/suppliers?current=1&size=10&name=博世&status=1&creditRating=A
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| name | String | 否 | 供应商名称（模糊查询） |
| status | Integer | 否 | 合作状态 |
| creditRating | String | 否 | 信用评级 |

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "current": 1,
    "size": 10,
    "total": 5,
    "pages": 1,
    "records": [...]
  }
}
```

### 6. 搜索供应商

**请求**
```
GET /api/suppliers/search?name=博世
```

### 7. 更新供应商状态

**请求**
```
PATCH /api/suppliers/{id}/status?status=2
```

### 8. 更新信用评级

**请求**
```
PATCH /api/suppliers/{id}/credit-rating?creditRating=A
```

---

## 产品/零部件管理接口

### 1. 创建零部件

**请求**
```
POST /api/parts
Content-Type: application/json

{
  "partCode": "PC2001",
  "name": "活塞环组件",
  "model": "标准型",
  "specification": "四冲程通用",
  "unit": "套",
  "purchasePrice": 158.00,
  "suggestedRetailPrice": 220.00,
  "stockWarningValue": 20,
  "supplierId": 1,
  "category": "发动机类",
  "description": "高品质活塞环组件"
}
```

### 2. 更新零部件

**请求**
```
PUT /api/parts/{id}
Content-Type: application/json

{
  "purchasePrice": 168.00,
  "stockWarningValue": 15
}
```

### 3. 删除零部件

**请求**
```
DELETE /api/parts/{id}
```

### 4. 获取单个零部件

**请求**
```
GET /api/parts/{id}
```

### 5. 分页查询零部件

**请求**
```
GET /api/parts?current=1&size=10&name=活塞&category=发动机类&supplierId=1
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| name | String | 否 | 零件名称（模糊查询） |
| category | String | 否 | 分类 |
| supplierId | Long | 否 | 供应商ID |

### 6. 搜索零部件

**请求**
```
GET /api/parts/search?name=活塞
```

### 7. 更新采购单价

**请求**
```
PATCH /api/parts/{id}/price?purchasePrice=168.00
```

### 8. 获取供应商的产品列表

**请求**
```
GET /api/parts/supplier/{supplierId}
```

---

## 采购订单管理接口

### 1. 创建采购订单

**请求**
```
POST /api/orders
Content-Type: application/json

{
  "orderNumber": "PO20240101001",
  "supplierId": 1,
  "expectedDeliveryDate": "2024-01-15",
  "remark": "紧急采购",
  "status": 1
}
```

### 2. 更新采购订单

**请求**
```
PUT /api/orders/{id}
Content-Type: application/json

{
  "remark": "已确认，稍后发货"
}
```

### 3. 删除采购订单

**请求**
```
DELETE /api/orders/{id}
```

### 4. 获取单个订单

**请求**
```
GET /api/orders/{id}
```

### 5. 获取订单详情（包含明细）

**请求**
```
GET /api/orders/{id}/details
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "order": {
      "id": 1,
      "orderNumber": "PO20240101001",
      ...
    },
    "details": [
      {
        "id": 1,
        "orderId": 1,
        "partId": 1,
        "quantity": 10,
        "unitPrice": 158.00,
        "subtotal": 1580.00
      }
    ]
  }
}
```

### 6. 分页查询订单

**请求**
```
GET /api/orders?current=1&size=10&orderNumber=PO2024&supplierId=1&status=1&startDate=2024-01-01&endDate=2024-12-31
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| orderNumber | String | 否 | 订单编号（模糊查询） |
| supplierId | Long | 否 | 供应商ID |
| status | Integer | 否 | 订单状态 |
| startDate | LocalDate | 否 | 开始日期 |
| endDate | LocalDate | 否 | 结束日期 |

**订单状态说明：**
| 状态码 | 说明 |
|--------|------|
| 1 | 待审核 |
| 2 | 已审核 |
| 3 | 采购中 |
| 4 | 已入库 |
| 5 | 已取消 |

### 7. 更新订单状态

**请求**
```
PATCH /api/orders/{id}/status?status=2
```

### 8. 获取采购统计

**请求**
```
GET /api/orders/statistics?startDate=2024-01-01&endDate=2024-12-31
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalOrders": 150,
    "totalAmount": 2580000,
    "completedOrders": 120,
    "pendingOrders": 30
  }
}
```

---

## 库存管理接口

### 1. 获取库存记录

**请求**
```
GET /api/inventory/{id}
```

### 2. 分页查询库存

**请求**
```
GET /api/inventory?current=1&size=10&partName=活塞&warehouseLocation=A区
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| partName | String | 否 | 零件名称 |
| warehouseLocation | String | 否 | 仓库位置 |

### 3. 入库操作

**请求**
```
POST /api/inventory/inbound?partId=1&quantity=100&warehouseLocation=A区-1号库
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| partId | Long | 是 | 零件ID |
| quantity | Integer | 是 | 入库数量 |
| warehouseLocation | String | 否 | 仓库位置 |

### 4. 出库操作

**请求**
```
POST /api/inventory/outbound?partId=1&quantity=5
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| partId | Long | 是 | 零件ID |
| quantity | Integer | 是 | 出库数量 |

### 5. 获取库存预警列表

**请求**
```
GET /api/inventory/warning
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "partId": 1,
      "currentQuantity": 5,
      "safetyStock": 20,
      ...
    }
  ]
}
```

### 6. 更新安全库存

**请求**
```
PATCH /api/inventory/{id}/safety-stock?safetyStock=30
```

### 7. 库存盘点

**请求**
```
GET /api/inventory/check
```

---

## 客户管理接口

### 1. 创建客户

**请求**
```
POST /api/customers
Content-Type: application/json

{
  "customerCode": "CUS00001",
  "name": "北京摩托车配件城",
  "contactPerson": "李经理",
  "phone": "13900139000",
  "email": "bjmotor@example.com",
  "address": "北京市朝阳区某路1号",
  "customerType": 1,
  "discountLevel": 2
}
```

### 2. 更新客户

**请求**
```
PUT /api/customers/{id}
Content-Type: application/json

{
  "discountLevel": 3
}
```

### 3. 删除客户

**请求**
```
DELETE /api/customers/{id}
```

### 4. 获取单个客户

**请求**
```
GET /api/customers/{id}
```

### 5. 分页查询客户

**请求**
```
GET /api/customers?current=1&size=10&name=北京&customerType=1&discountLevel=2
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| name | String | 否 | 客户名称（模糊查询） |
| customerType | Integer | 否 | 客户类型 |
| discountLevel | Integer | 否 | 折扣等级 |

**客户类型说明：**
| 类型码 | 说明 |
|--------|------|
| 1 | 经销商 |
| 2 | 零售店 |
| 3 | 个人用户 |

**折扣等级说明：**
| 等级码 | 说明 |
|--------|------|
| 1 | 无折扣 |
| 2 | 银牌（9.5折） |
| 3 | 金牌（9折） |
| 4 | 钻石（8.5折） |

### 6. 搜索客户

**请求**
```
GET /api/customers/search?name=北京
```

### 7. 更新客户类型

**请求**
```
PATCH /api/customers/{id}/type?customerType=2
```

### 8. 更新折扣等级

**请求**
```
PATCH /api/customers/{id}/discount-level?discountLevel=3
```

---

## 物流管理接口

### 1. 创建物流记录

**请求**
```
POST /api/logistics
Content-Type: application/json

{
  "orderId": 1,
  "logisticsCompany": "顺丰速运",
  "trackingNumber": "SF1234567890",
  "status": 1
}
```

### 2. 更新物流信息

**请求**
```
PUT /api/logistics/{id}
Content-Type: application/json

{
  "remark": "物流已发出"
}
```

### 3. 删除物流记录

**请求**
```
DELETE /api/logistics/{id}
```

### 4. 获取物流记录

**请求**
```
GET /api/logistics/{id}
```

### 5. 分页查询物流

**请求**
```
GET /api/logistics?current=1&size=10&orderId=1&trackingNumber=SF&status=2
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| current | Long | 否 | 当前页码 |
| size | Long | 否 | 每页大小 |
| orderId | Long | 否 | 订单ID |
| trackingNumber | String | 否 | 运单号（模糊查询） |
| status | Integer | 否 | 物流状态 |

**物流状态说明：**
| 状态码 | 说明 |
|--------|------|
| 1 | 待发货 |
| 2 | 运输中 |
| 3 | 已签收 |
| 4 | 异常 |

### 6. 根据订单获取物流

**请求**
```
GET /api/logistics/order/{orderId}
```

### 7. 更新物流状态

**请求**
```
PATCH /api/logistics/{id}/status?status=2
```

### 8. 更新发货信息

**请求**
```
PATCH /api/logistics/{id}/shipping?shipTime=2024-01-10 10:00:00&trackingNumber=SF1234567890&logisticsCompany=顺丰速运
```

### 9. 更新收货信息

**请求**
```
PATCH /api/logistics/{id}/receiving?actualArrivalTime=2024-01-15 14:30:00&receiver=王收货
```

---

## 统计分析接口

### 1. 获取仪表盘统计

**请求**
```
GET /api/statistics/dashboard
```

**响应**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "supplierCount": 30,
    "partCount": 60,
    "customerCount": 40,
    "userCount": 12,
    "inventoryCount": 60,
    "monthPurchase": {
      "orderCount": 15,
      "totalAmount": 258000,
      "completedCount": 12
    }
  }
}
```

### 2. 获取供应商统计

**请求**
```
GET /api/statistics/suppliers
```

### 3. 获取产品分类统计

**请求**
```
GET /api/statistics/parts
```

### 4. 获取库存预警统计

**请求**
```
GET /api/statistics/inventory
```

### 5. 获取月度采购趋势

**请求**
```
GET /api/statistics/monthly-trend?startDate=2024-01-01&endDate=2024-12-31
```

---

## 数据字典

### 供应商合作状态
| 值 | 说明 |
|----|------|
| 1 | 合作中 |
| 2 | 已终止 |
| 3 | 审核中 |

### 供应商信用评级
| 值 | 说明 |
|----|------|
| A | 优秀 |
| B | 良好 |
| C | 一般 |
| D | 较差 |

### 订单状态
| 值 | 说明 |
|----|------|
| 1 | 待审核 |
| 2 | 已审核 |
| 3 | 采购中 |
| 4 | 已入库 |
| 5 | 已取消 |

### 物流状态
| 值 | 说明 |
|----|------|
| 1 | 待发货 |
| 2 | 运输中 |
| 3 | 已签收 |
| 4 | 异常 |

### 客户类型
| 值 | 说明 |
|----|------|
| 1 | 经销商 |
| 2 | 零售店 |
| 3 | 个人用户 |

### 折扣等级
| 值 | 说明 |
|----|------|
| 1 | 无折扣 |
| 2 | 银牌（9.5折） |
| 3 | 金牌（9折） |
| 4 | 钻石（8.5折） |

### 用户角色
| 值 | 说明 |
|----|------|
| admin | 管理员 |
| purchase | 采购员 |
| warehouse | 仓管员 |
| sales | 销售员 |

### 零件分类
| 值 | 说明 |
|----|------|
| 发动机类 | 发动机相关配件 |
| 车架类 | 车架、减震器等 |
| 电气类 | 电器元件 |
| 制动类 | 刹车系统配件 |
| 传动类 | 传动系统配件 |
| 外观件 | 外观装饰件 |

---

## 注意事项

1. 所有POST、PUT、PATCH请求的Content-Type为application/json
2. 日期格式使用yyyy-MM-dd，时间格式使用yyyy-MM-dd HH:mm:ss
3. 所有删除操作均为逻辑删除
4. 分页查询的默认页码为1，每页大小为10
5. 模糊查询使用LIKE %%方式