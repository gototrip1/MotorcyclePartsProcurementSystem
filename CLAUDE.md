# CLAUDE.md

本文件为 Claude Code（及其他协作者）提供本项目的整体说明，包含三种角色职责、系统功能、以及前后端实现方式。

> 重要：本项目最终确定为**纯内部进销存系统**（采购入库 + 领用出库），**不含对客户的销售**。
> 文档中描述的"目标设计"是定稿方向；当前代码部分内容尚未重构到位，差距见文末「目标设计 vs 当前代码现状」。

---

## 一、项目简介

**摩托车零部件采购管理系统** —— 管理摩托车零部件从"向供应商采购入库"到"内部车间领用出库"的全过程，核心是**库存的进与出都必须经过管理员审核**这一职责分离模型。

- 业务定位：内部进销存（采购入 + 领料出），无对外销售。
- 核心库表：`user`、`supplier`、`part`、`purchase_order` + `order_detail`、`inventory`、`stock_out_order` + `stock_out_detail`。
- 数据库设计与 ER 图见 [docs/database_schema.sql](docs/database_schema.sql)、[docs/database_er.drawio](docs/database_er.drawio)。

---

## 二、三个角色与职责

系统只有 **3 种角色**，围绕"谁能让库存变动"来划分权限。核心原则：

> **创建单据 ≠ 改库存。任何库存的增加或减少，都只能由【系统管理员】审核通过后触发。**
> 采购专员无权入库、领用人员无权出库 —— 这正是"审核"这一步存在的意义。

### 1. 采购专员（`purchase`）—— 只管进货，不领用物料
1. 新建供应商、零件资料，提交管理员审核；
2. 创建**采购单**，填采购数量、系统自动计算总价；录入物流信息、标记供应商发货；
3. 货到后提交**入库审批**——**无权直接入库、无权领用、不能出库减库存**；
4. 对账后标记订单**已付款**。

### 2. 领用人员（`requisition`，车间/生产）—— 只做领料，不采购
1. 创建**领用单（领料单）**，勾选需要领用的零件、填写领用数量；
2. 提交**出库申请**——**自己不能直接扣减库存**，需管理员审核。

### 3. 系统管理员（`admin`）—— 审核中枢，掌控库存变动大权
1. 审核供应商、零件档案，锁定采购单价；
2. 审核**采购入库**申请 → 通过后**库存增加**；
3. 审核**领用出库**申请 → 通过后**库存减少**；
4. 维护用户账号、配置库存预警参数。

---

## 三、系统功能

### 1. 基础档案管理
- **供应商管理**：增删改查；采购专员新建需管理员审核。
- **零件管理**：增删改查、按分类筛选；单价由管理员锁定。
- **用户管理**：管理员维护账号、分配上述 3 种角色。

### 2. 采购入库流程（采购专员发起，管理员审核入库）
- 创建采购单（明细：零件 + 数量 + 单价，小计/总价自动计算）；
- 录入物流（物流公司、运单号、发货时间、预计/实际到货）；
- 货到提交入库申请 → 管理员审核通过 → **库存 +**；
- 付款标记。

采购单状态流转（由现实事件驱动，靠人推动而非定时器）：

| 状态 | 触发者 | 含义 |
|---|---|---|
| 采购中 | 采购专员建单 | 起始态，自行处理物流/发货/等货，**无需审核** |
| 待入库审核 | 采购专员（货到提交） | 球交给管理员 |
| 已入库 | 管理员审核通过 | **库存 +** |
| 已取消 | — | — |

> 说明：到货时间不由系统计算。`expected_delivery_date`（预计交货）下单时与供应商约定填入，`actual_delivery_date`（实际到货）由人在货到时确认。"已付款"用单独字段标记，不混入 status。

### 3. 领用出库流程（领用人员发起，管理员审核出库）
- 创建领用单（明细：零件 + 领用数量），填领用人/部门/用途；
- 提交出库申请 → 管理员审核通过 → **库存 −**。

领用单状态流转：

| 状态 | 触发者 | 含义 |
|---|---|---|
| 待审核 | 领用人员提交 | 球在管理员手上 |
| 已出库 | 管理员审核通过 | **库存 −** |
| 已取消 | — | — |

### 4. 库存管理
- 每个零件一条库存记录（当前数量、安全库存、仓库位置、最近出入库时间）；
- 库存**只能由审核通过的入库/出库自动变更**，不允许手工直接改数字；
- **库存预警**：当前数量低于预警值/安全库存时给出提示。

### 5. 统计报表
- 采购统计、库存预警、订单/出入库明细查询等（见 `StatisticsController`）。

---

## 四、前后端实现

### 后端（已有，主体）

技术栈：

| 项 | 选型 |
|---|---|
| 框架 | Spring Boot 3.1.10 / Java 17 |
| ORM | MyBatis Plus 3.5.3.1 |
| 数据库 | MySQL 8.0.33（库名 `motorparts_db`） |
| 连接池 | HikariCP |
| API 文档 | SpringDoc OpenAPI（Swagger UI） |
| 工具 | Lombok、Hutool、FastJSON、Spring Validation |

分层架构（包路径 `com.motorparts`）：

```
controller/   REST 接口，@RestController + @RequestMapping("/api/xxx")，构造器注入
service/      业务接口
service/impl/ 业务实现（库存变动等事务逻辑在此）
mapper/       MyBatis Plus Mapper（继承 BaseMapper）
entity/       实体，继承 BaseEntity（含 deleted / create_time / update_time）
dto/          查询/组合返回对象
common/       Result、PageResult、enums（角色/状态等）、exception（全局异常）
config/       MyBatisPlus、Swagger、WebMvc(CORS)、DDL 初始化
init/         DatabaseInitializer 启动建表/初始化数据
```

关键约定：
- **统一响应** `Result<T>{code, message, data, timestamp}`；分页用 `PageResult`。成功 `Result.success(data)`，失败 `Result.error(...)`。
- **接口前缀**统一 `/api/`，RESTful 风格（如 `/api/parts/create`）。
- **跨域**：`WebMvcConfig` 已对 `/**` 全量放开（便于前后端分离开发）。
- **校验**：`@Validated` + `@Valid`，异常由 `GlobalExceptionHandler` 统一兜底。
- **枚举集中**在 `common/enums`（角色、订单状态、零件分类等）。
- **数据库初始化**：`application.yml` 中 `motorparts.init.*` 控制是否建表/灌样例数据；`ddl-auto: none`，表结构以 [docs/database_schema.sql](docs/database_schema.sql) 为准。
- **Swagger UI**：启动后访问 `http://localhost:8080/swagger-ui.html` 调试接口。

### 前端（前后端分离，待建）

技术选型（已确定）：

| 项 | 选型 |
|---|---|
| 框架 | **Vue 3**（Composition API） |
| 包管理 | **npm** |
| UI 组件库 | **Element Plus** |
| 构建工具 | Vite（Vue 3 推荐） |
| 通信 | axios 调用后端 `/api/**` |

> 说明：原 `ecommerce-demo/` 静态商品页（HTML/JS）已**全部删除**，不再使用。管理前端将以独立的 Vue 3 工程从零搭建。

实现约定：
- 独立前端工程（如 `frontend/` 目录），用 npm 管理依赖、Vite 启动开发服务器；
- 页面用 Element Plus 组件搭建（表格、表单、弹窗、菜单等）；
- 用 axios 统一封装请求，对接后端 `/api/` 接口，统一处理 `Result{code,message,data}`；
- 按 3 种角色做**菜单/按钮级权限控制**：采购专员看采购模块、领用人员看领用模块、管理员看审核与全部模块；
- 复用后端的状态枚举（采购单/领用单状态）做前端流转展示与按钮可用性控制；
- 开发期 axios 直连本地 8080（后端已全量放开 CORS），上线可由 Nginx 反向代理或 `npm run build` 后由后端/网关托管静态产物。

常用命令（前端工程建立后）：

```bash
npm install        # 安装依赖
npm run dev        # 启动开发服务器（Vite）
npm run build      # 构建生产产物
```

---

## 五、构建与运行

前置：JDK 17、Maven、MySQL 8（建库 `motorparts_db`，账号见 `application.yml`，默认 `root/123123`）。

```bash
# 编译打包
mvn clean package

# 运行
mvn spring-boot:run
# 或
java -jar target/MotorcyclePartsProcurementSystem-1.0.0.jar
```

启动后：API 根路径 `http://localhost:8080/api/`，文档 `http://localhost:8080/swagger-ui.html`。

---

## 六、数据库设计要点（定稿）

- 共约 8 张表：`user`、`supplier`、`part`、`purchase_order`、`order_detail`、`inventory`、`stock_out_order`、`stock_out_detail`。
- 采购单与领用单**各自独立成表**（不合并为单一 order + order_type），保持语义清晰、互不污染。
- **物流信息并入 `purchase_order`**（物流公司/运单号/发货时间/到货时间等列），不单独建 `logistics` 表（系统已无对客户发货场景）。
- 所有表含逻辑删除 `deleted` 与 `create_time` / `update_time`。
- 外键关系：`supplier 1—N part`、`part 1—1 inventory`、`user 1—N purchase_order`、`purchase_order 1—N order_detail N—1 part`、`user(领用人) 1—N stock_out_order 1—N stock_out_detail N—1 part`。

---

## 七、目标设计 vs 当前代码现状（重构清单）

以下为本系统**定稿设计**与**当前代码**的差距，后续重构需对齐：

| 项 | 目标设计 | 当前代码现状 |
|---|---|---|
| 角色 | 3 种：`admin` / `purchase` / `requisition` | `UserRoleEnum` 仍为 4 种：`admin/purchase/warehouse/sales` |
| 客户 | **已去除** customer | 仍存在 `Customer` 实体/Mapper/Service/Controller 及 `customer` 表 |
| 物流 | 字段**并入** `purchase_order` | 仍是独立 `Logistics` 实体/表与 `LogisticsController` |
| 领用出库 | 新增 `stock_out_order` + `stock_out_detail` 及对应代码 | **尚未实现** |
| 采购单状态 | 采购中 / 待入库审核 / 已入库 / 已取消 + 独立 `paid` 标记 | `OrderStatusEnum` 为 待审核/已审核/采购中/已入库/已取消 |
| 库存变动 | 仅由管理员审核通过的入/出库自动触发 | 需核对 service 层是否已严格收口 |

> 进行上述任一重构时，请同步更新 [docs/database_schema.sql](docs/database_schema.sql) 与 [docs/database_er.drawio](docs/database_er.drawio)，保持三者一致。
