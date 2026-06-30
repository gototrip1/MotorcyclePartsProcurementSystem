// 与后端枚举严格对齐的状态/分类映射，前端统一用于标签展示与按钮可用性控制。
// 切勿在页面里散落魔法数字。

// 采购单状态（OrderStatusEnum）
export const ORDER_STATUS = {
  1: { text: '采购中', tag: 'info' },
  2: { text: '待入库审核', tag: 'warning' },
  3: { text: '已入库', tag: 'success' },
  4: { text: '已取消', tag: 'danger' }
}

// 领用单状态（StockOutStatusEnum）
export const STOCKOUT_STATUS = {
  1: { text: '待审核', tag: 'warning' },
  2: { text: '已出库', tag: 'success' },
  3: { text: '已取消', tag: 'danger' }
}

// 供应商状态（SupplierStatusEnum）
export const SUPPLIER_STATUS = {
  1: { text: '合作中', tag: 'success' },
  2: { text: '已终止', tag: 'danger' },
  3: { text: '审核中', tag: 'warning' }
}

// 付款标记（PurchaseOrder.paid）
export const PAID_STATUS = {
  0: { text: '未付款', tag: 'info' },
  1: { text: '已付款', tag: 'success' }
}

// 用户状态（User.status）
export const USER_STATUS = {
  1: { text: '正常', tag: 'success' },
  2: { text: '禁用', tag: 'danger' }
}

// 用户角色（UserRoleEnum）
export const USER_ROLE = {
  admin: '管理员',
  purchase: '采购专员',
  requisition: '领用人员'
}

export const USER_ROLE_OPTIONS = [
  { value: 'admin', label: '管理员' },
  { value: 'purchase', label: '采购专员' },
  { value: 'requisition', label: '领用人员' }
]

// 供应商信用评级
export const CREDIT_RATING_OPTIONS = [
  { value: 'A', label: 'A（优秀）' },
  { value: 'B', label: 'B（良好）' },
  { value: 'C', label: 'C（一般）' },
  { value: 'D', label: 'D（较差）' }
]

// 零件分类（PartCategoryEnum）
export const PART_CATEGORY_OPTIONS = [
  { value: '发动机类', label: '发动机类' },
  { value: '车架类', label: '车架类' },
  { value: '电气类', label: '电气类' },
  { value: '制动类', label: '制动类' },
  { value: '传动类', label: '传动类' },
  { value: '外观件', label: '外观件' }
]

// 领用用途
export const PURPOSE_OPTIONS = [
  { value: '生产', label: '生产' },
  { value: '维修', label: '维修' },
  { value: '报废', label: '报废' },
  { value: '其他', label: '其他' }
]

// 计量单位
export const UNIT_OPTIONS = [
  { value: '个', label: '个' },
  { value: '套', label: '套' },
  { value: '件', label: '件' },
  { value: '台', label: '台' },
  { value: '箱', label: '箱' }
]
