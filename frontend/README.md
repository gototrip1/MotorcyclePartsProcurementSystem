# 摩托车零部件采购管理系统 —— 前端

基于 **Vue 3 + Vite + Element Plus + Pinia + Vue Router + axios + ECharts** 的管理前端，
对接后端 Spring Boot（`/api/**`）。实现了登录鉴权、按角色的菜单/按钮权限、以及采购入库 /
领用出库 / 审核中心 / 库存 / 档案 / 统计等全部功能页面。

## 技术栈

| 项 | 选型 |
|---|---|
| 框架 | Vue 3（`<script setup>` Composition API） |
| 构建 | Vite 5 |
| UI | Element Plus 2 + @element-plus/icons-vue |
| 路由 | Vue Router 4（路由守卫做权限） |
| 状态 | Pinia 2（token / 角色 / 用户信息） |
| 请求 | axios（拦截器统一处理 `Result`） |
| 图表 | ECharts 5 |
| 时间 | dayjs |

## 快速开始

```bash
cd frontend
npm install      # 安装依赖
npm run dev      # 启动开发服务器（默认 http://localhost:5173）
npm run build    # 构建生产产物到 dist/
npm run preview  # 本地预览生产产物
```

开发期 Vite 已将 `/api` 代理到 `http://localhost:8080`（见 `vite.config.js`），
请先启动后端（`mvn spring-boot:run`）。

## 目录结构

```
frontend/
├── index.html
├── vite.config.js          # 开发代理 /api -> localhost:8080，@ 别名
├── package.json
└── src/
    ├── main.js             # 挂载 Vue / Element Plus / router / pinia
    ├── App.vue
    ├── api/                # 与后端 Controller 一一对应的接口封装
    │   ├── request.js      # axios 实例 + 拦截器（拆 Result、401 跳登录）
    │   ├── auth.js  supplier.js  part.js  purchaseOrder.js
    │   ├── stockOut.js  audit.js  inventory.js  user.js  statistics.js
    ├── stores/user.js      # Pinia：token / role / userInfo
    ├── router/index.js     # 路由表 + meta.roles + 全局守卫
    ├── layouts/MainLayout.vue   # 侧边菜单（按角色过滤）+ 顶栏 + 内容区
    ├── components/StatusTag.vue  # 通用状态标签
    ├── utils/
    │   ├── enums.js        # 与后端对齐的状态/分类映射
    │   └── format.js       # 日期/金额格式化
    ├── styles/global.css
    └── views/
        ├── Login.vue  Forbidden.vue
        ├── supplier/SupplierList.vue
        ├── part/PartList.vue
        ├── purchase/PurchaseOrderList.vue  purchase/PurchaseOrderEdit.vue
        ├── stockout/StockOutList.vue
        ├── audit/AuditCenter.vue
        ├── inventory/InventoryList.vue
        ├── user/UserList.vue
        └── statistics/Dashboard.vue
```

## 角色与权限

后端登录返回单个 `role` 字符串（`admin` / `purchase` / `requisition`），前端据此控制：

- **路由级**：`router/index.js` 中每个路由 `meta.roles`，全局守卫拦截无权访问 → `/403`。
- **菜单级**：`MainLayout.vue` 按当前角色过滤侧边菜单。
- **按钮级**：审核、锁价、删除、设安全库存等敏感按钮用 `v-if` + `userStore.isAdmin` 等控制。

| 角色 | 可见模块 |
|---|---|
| 采购专员 `purchase` | 供应商、零件、采购单、库存 |
| 领用人员 `requisition` | 领用单、库存 |
| 管理员 `admin` | 全部 + 审核中心、用户管理、统计报表 |

## 与后端对齐要点

- 统一响应 `Result{code,message,data,timestamp}`，拦截器只把 `data` 抛给页面，`code!=200` 走错误分支。
- 接口基础路径前缀 `/api`，各模块真实路径见 `src/api/*.js`（如采购单 `/api/orders`、领用单 `/api/stock-out-orders`）。
- 状态数字统一由 `utils/enums.js` 翻译为中文标签，页面中不散落魔法数字。
- **库存数量前端只读**：库存页不提供任何直接改数字的入口，库存变动一律由「审核中心」触发后端事务。

## 业务流程

- **采购入库**：采购员建单（采购中）→ 录物流 → 提交入库审核（待入库审核）→ 管理员在审核中心通过 → 库存 +。已入库后采购员可「标记已付款」。
- **领用出库**：领用人员建领用单（待审核）→ 管理员在审核中心通过 → 库存 −（库存不足后端报错，前端提示）。
