import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 主框架下的业务路由（同时用于 MainLayout 侧边菜单渲染，故带 meta.title/icon）
export const businessRoutes = [
  {
    path: 'dashboard',
    name: 'Dashboard',
    component: () => import('@/views/statistics/Dashboard.vue'),
    meta: { title: '统计报表', icon: 'DataLine', roles: ['admin'] }
  },
  {
    path: 'suppliers',
    name: 'SupplierList',
    component: () => import('@/views/supplier/SupplierList.vue'),
    meta: { title: '供应商管理', icon: 'OfficeBuilding', roles: ['admin', 'purchase'] }
  },
  {
    path: 'parts',
    name: 'PartList',
    component: () => import('@/views/part/PartList.vue'),
    meta: { title: '零件管理', icon: 'Setting', roles: ['admin', 'purchase'] }
  },
  {
    path: 'purchase',
    name: 'PurchaseOrderList',
    component: () => import('@/views/purchase/PurchaseOrderList.vue'),
    meta: { title: '采购单', icon: 'ShoppingCart', roles: ['admin', 'purchase'] }
  },
  {
    path: 'purchase/edit/:id?',
    name: 'PurchaseOrderEdit',
    component: () => import('@/views/purchase/PurchaseOrderEdit.vue'),
    // 编辑页不在侧边菜单显示
    meta: { title: '采购单编辑', roles: ['admin', 'purchase'], hidden: true }
  },
  {
    path: 'stockout',
    name: 'StockOutList',
    component: () => import('@/views/stockout/StockOutList.vue'),
    meta: { title: '领用单', icon: 'Box', roles: ['admin', 'requisition'] }
  },
  {
    path: 'audit',
    name: 'AuditCenter',
    component: () => import('@/views/audit/AuditCenter.vue'),
    meta: { title: '审核中心', icon: 'Stamp', roles: ['admin'] }
  },
  {
    path: 'inventory',
    name: 'InventoryList',
    component: () => import('@/views/inventory/InventoryList.vue'),
    meta: { title: '库存管理', icon: 'Files', roles: ['admin', 'purchase', 'requisition'] }
  },
  {
    path: 'users',
    name: 'UserList',
    component: () => import('@/views/user/UserList.vue'),
    meta: { title: '用户管理', icon: 'User', roles: ['admin'] }
  }
]

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  { path: '/403', name: 'Forbidden', component: () => import('@/views/Forbidden.vue') },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: businessRoutes
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

/** 计算当前角色第一个有权访问的菜单路径，作为落地首页 */
function firstAllowedPath(userStore) {
  const route = businessRoutes.find(
    (r) => !r.meta.hidden && userStore.hasRole(r.meta.roles)
  )
  return route ? '/' + route.path : null
}

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：登录态 + 角色权限
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 已登录访问登录页 → 回到各自首页
  if (to.path === '/login') {
    return userStore.isLogin ? next(firstAllowedPath(userStore) || '/login') : next()
  }
  if (!userStore.isLogin) {
    return next('/login')
  }

  // 访问根路径 → 按角色跳转到第一个有权访问的页面（避免落到 admin 专属页被打 403）
  if (to.path === '/') {
    const target = firstAllowedPath(userStore)
    if (target) return next(target)
    // 登录态存在但角色无任何可访问页面（多为残留/损坏的登录态）→ 清理后回登录页
    userStore.logout()
    return next('/login')
  }

  // 其余路由按 meta.roles 鉴权
  const roles = to.meta?.roles
  if (roles && roles.length && !userStore.hasRole(roles)) {
    return next('/403')
  }
  next()
})

export default router
