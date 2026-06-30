# 摩托车零部件采购管理系统

## 项目介绍

本项目是一个**摩托车零部件内部进销存管理系统**，采用 SpringBoot 构建单体后端服务。系统专注于企业内部的采购入库和领用出库管理，覆盖从供应商准入、零部件采购、库存管理到领用审批的全链路业务流程。

**核心定位**：纯内部进销存系统（采购入库 + 领用出库，无对外销售）

**核心价值**：
- 打通供应商、采购、库存、领用四大业务域，数据联动减少信息孤岛
- 内置完整的采购订单生命周期（创建 → 审核 → 入库）
- 支持领用申请 → 管理员审核 → 出库的规范化流程
- 提供库存预警、采购统计等经营辅助能力
- 开箱即用：启动后自动初始化数据库表结构和模拟数据

**适用场景**：摩托车/汽车零部件生产企业的内部采购与领用管理、实训教学、毕业设计等。

### 主要功能

- **供应商管理**：管理供应商信息，包括信用评级、合作状态等
- **零部件管理**：管理摩托车零部件信息，包括分类、采购价格、库存预警值等
- **采购订单**：管理采购订单全流程（采购中 → 待入库审核 → 已入库 → 已取消），含物流跟踪信息
- **库存管理**：管理库存数量，提供库存预警功能（库存变更仅由审核通过的出入库触发）
- **领用管理**：领用人员发起领用申请，管理员审核后自动扣减库存
- **统计分析**：提供采购统计、库存预警等分析报表

### 用户角色

系统仅包含 3 种角色：

| 角色 | 英文标识 | 说明 |
|------|---------|------|
| 系统管理员 | admin | 全局管理权限，可审核入库和出库 |
| 采购专员 | purchase | 负责创建采购订单、跟踪采购进度 |
| 领用人员 | requisition | 发起领用申请（生产/维修/报废等） |

## 技术栈

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.1.10 |
| MyBatis Plus | 3.5.3.1 |
| MySQL | 8.0.33 |
| Lombok | 1.18.30 |
| SpringDoc OpenAPI | 2.0.2 |
| Hutool | 5.8.25 |

## 快速开始（后端启动指南）

### 一、环境要求

| 依赖 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 项目基于 Spring Boot 3.x，必须使用 JDK 17 及以上 |
| MySQL | 8.0+ | 用于业务数据持久化 |
| Maven | 3.6+ | 项目构建工具 |

确保本地已安装并配置好以上环境，且 MySQL 服务处于运行状态。

### 二、配置数据库

1. 创建数据库（系统启动时会自动建表，但需先手动创建空库）：

```sql
CREATE DATABASE motorparts CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

2. 修改数据库连接配置

打开 `src/main/resources/application.yml`，将 `spring.datasource` 下的信息替换为你的 MySQL 实例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/motorparts?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123123
```

> 提示：若 MySQL 端口或密码不同，请同步修改 `url` 和 `password`。

### 三、启动后端服务

在项目根目录下依次执行以下命令：

```bash
# 1. 进入项目目录（根据实际情况调整）
cd MotorcyclePartsProcurementSystem

# 2. 编译并安装依赖
mvn clean compile

# 3. 启动 SpringBoot 服务
mvn spring-boot:run
```

看到类似 `Started MotorpartsApplication in x.x seconds` 的日志，即表示后端启动成功。

**常用启动方式补充**：

- **IDE 启动**：在 IDEA/Eclipse 中直接运行 `com.motorparts.MotorpartsApplication` 的 `main` 方法，适合开发调试。
- **打包启动**：
  ```bash
  mvn clean package
  java -jar target/MotorcyclePartsProcurementSystem-1.0.0.jar
  ```

### 四、验证启动结果

| 入口 | 地址 | 说明 |
|------|------|------|
| 系统首页 | http://localhost:8080 | 后端服务根地址 |
| Swagger API 文档 | http://localhost:8080/swagger-ui/index.html | 可在线调试所有接口 |

首次启动时，系统会自动完成以下初始化：
1. 创建全部数据库表结构
2. 插入模拟数据（5 个用户、4 个供应商、5 个零部件等）

## 项目结构

```
src/main/java/com/motorparts/
├── MotorpartsApplication.java          # 启动类
├── config/                              # 配置类
│   ├── MyBatisPlusConfig.java
│   ├── WebMvcConfig.java
│   └── SwaggerConfig.java
├── controller/                          # 控制器层
│   ├── SupplierController.java
│   ├── PartController.java
│   ├── PurchaseOrderController.java
│   ├── InventoryController.java
│   ├── StockOutController.java         # 领用单控制器
│   └── StatisticsController.java
├── service/                              # 服务层
│   ├── SupplierService.java
│   ├── PartService.java
│   ├── PurchaseOrderService.java
│   ├── InventoryService.java
│   ├── StockOutService.java            # 领用单服务
│   └── impl/                             # 实现类
├── mapper/                               # Mapper接口
├── entity/                               # 实体类
├── dto/                                  # 数据传输对象
├── common/                               # 通用类
│   ├── Result.java
│   ├── PageResult.java
│   ├── enums/
│   └── exception/
└── init/                                 # 数据初始化
    └── DatabaseInitializer.java
```

## 数据初始化

项目启动时会自动执行数据初始化，包括：

- 创建数据库表结构
- 插入模拟数据

### 初始化配置

在 `application.yml` 中配置：

```yaml
motorparts:
  init:
    enabled: true          # 是否启用数据初始化
    insert-sample-data: true  # 是否插入模拟数据
```

### 模拟数据说明

| 数据类型 | 数量 | 说明 |
|----------|------|------|
| 用户 | 5 条 | 1 管理员 + 2 采购专员 + 2 领用人员 |
| 供应商 | 4 条 | 包含隆鑫、钱江、大长江、飞跃等 |
| 零部件 | 5 条 | 涵盖发动机类、电气类、制动类、传动类 |
| 库存 | 5 条 | 与零部件一一对应（含预警示例） |
| 采购订单 | 4 条 | 各状态示例（采购中/待入库/已入库/已取消） |
| 订单明细 | 6 条 | 与采购订单关联 |
| 领用单 | 3 条 | 已出库/待审核示例 |
| 领用明细 | 4 条 | 与领用单关联 |

## API 接口测试

### 使用 Swagger UI 测试

1. 启动项目后访问：http://localhost:8080/swagger-ui/index.html
2. 在页面上可以看到所有 API 接口
3. 点击任意接口可以查看详情和测试

### 使用 Postman 测试

1. 导入 API 文档或手动添加接口
2. 基础 URL：`http://localhost:8080/api`
3. 示例请求：

**创建供应商**
```http
POST http://localhost:8080/api/suppliers
Content-Type: application/json

{
  "supplierCode": "SUP005",
  "name": "测试供应商",
  "contactPerson": "张三",
  "phone": "13800138000",
  "email": "test@example.com",
  "address": "上海市某区某路1号",
  "creditRating": "A",
  "status": 1
}
```

**分页查询供应商**
```http
GET http://localhost:8080/api/suppliers?current=1&size=10
```

## 数据库设计

### 核心设计说明

本系统为**纯内部进销存系统**，遵循以下设计原则：

1. **库存变更仅由审核通过触发**：库存数量只能由管理员审核通过的入库/出库操作自动变更，防止人为篡改
2. **物流信息内置**：物流信息（物流公司、运单号、发货时间）直接并入采购订单表，不单独建表
3. **无客户表**：系统仅处理内部采购和领用，不涉及对外销售

### 表结构

| 表名 | 说明 | 关联关系 |
|------|------|----------|
| user | 用户/员工表 | - |
| supplier | 供应商表 | - |
| part | 产品/零部件表 | supplier_id -> supplier |
| purchase_order | 采购订单表（含物流信息） | created_by -> user |
| order_detail | 订单明细表 | order_id -> purchase_order, part_id -> part |
| inventory | 库存表 | part_id -> part |
| stock_out_order | 领用单（出库单） | user_id -> user |
| stock_out_detail | 领用明细表 | stock_out_id -> stock_out_order, part_id -> part |

### 表关系图

```
user(管理员/采购专员) 1 → N purchase_order(采购单)
user(领用人员)       1 → N stock_out_order(领用单)
supplier(供应商)     1 → N part(零部件)
part(零部件)         1 → 1 inventory(库存)
purchase_order       1 → N order_detail → N 1 part
stock_out_order      1 → N stock_out_detail → N 1 part
```

### 主要字段约定

- 所有表都包含 `deleted`（逻辑删除）、`create_time`、`update_time` 字段
- 金额字段使用 DECIMAL 类型确保精度
- 时间字段使用 DATETIME 类型

### 订单状态说明

**采购订单 (purchase_order.status)**：
| 状态值 | 说明 |
|--------|------|
| 1 | 采购中（等待供应商发货） |
| 2 | 待入库审核（货已到，需管理员审核入库） |
| 3 | 已入库（审核通过，库存已增加） |
| 4 | 已取消 |

**领用单 (stock_out_order.status)**：
| 状态值 | 说明 |
|--------|------|
| 1 | 待审核（等待管理员审核出库） |
| 2 | 已出库（审核通过，库存已扣减） |
| 3 | 已取消 |

### 数据库视图

系统提供以下视图辅助查询：

| 视图名 | 说明 |
|--------|------|
| inventory_warning_view | 库存预警视图（显示库存状态：严重不足/不足/充足） |
| order_statistics_view | 采购订单统计视图（按日期统计订单数、金额） |
| order_detail_full_view | 订单明细全信息视图（含零部件和供应商信息） |

## 注意事项

1. **首次运行**：首次运行时会自动创建表结构和插入模拟数据
2. **重新初始化**：如需重新插入数据，可设置 `motorparts.init.recreate-tables: true`
3. **API 测试**：建议使用 Swagger UI 进行 API 测试
4. **密码说明**：用户密码已加密存储（BCrypt），示例密码为占位符

## 默认用户

| 用户名 | 密码 | 角色 | 部门 |
|--------|------|------|------|
| admin | admin123 | 系统管理员 | 管理部 |
| purchase1 | admin123 | 采购专员 | 采购部 |
| purchase2 | admin123 | 采购专员 | 采购部 |
| worker1 | admin123 | 领用人员 | 生产车间 |
| worker2 | admin123 | 领用人员 | 生产车间 |

## 文档

- [API 接口文档](docs/API_DOCUMENTATION.md)
- [数据库表结构](database_schema.sql)
- [数据来源说明](docs/数据来源说明.txt)

## 许可证

本项目仅供学习和参考使用。

## 联系方式

如有问题，请联系项目维护者。