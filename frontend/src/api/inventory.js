import request from './request'

// 对应后端 InventoryController（基础路径 /api/inventory）
export const inventoryApi = {
  page: (params) => request.get('/inventory/page', { params }),
  get: (id) => request.get(`/inventory/get/${id}`),
  // GET /inventory/warning 库存预警列表
  warning: () => request.get('/inventory/warning'),
  // GET /inventory/check 库存检查汇总
  check: () => request.get('/inventory/check'),
  // PATCH /inventory/update-safety-stock/{id}?safetyStock=
  updateSafetyStock: (id, safetyStock) =>
    request.patch(`/inventory/update-safety-stock/${id}`, null, { params: { safetyStock } })
  // 说明：/inventory/inbound、/inventory/outbound 为后端内部直改库存接口，
  // 前端不提供任何直接改库存入口（库存只读保护），故此处不暴露。
}
