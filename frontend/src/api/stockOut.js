import request from './request'

// 对应后端 StockOutOrderController（基础路径 /api/stock-out-orders）
export const stockOutApi = {
  page: (params) => request.get('/stock-out-orders/page', { params }),
  // GET /stock-out-orders/get/{id} -> {order, details}
  get: (id) => request.get(`/stock-out-orders/get/${id}`),
  // GET /stock-out-orders/details/{id} -> List<StockOutDetail>
  details: (id) => request.get(`/stock-out-orders/details/${id}`),
  create: (data) => request.post('/stock-out-orders/create', data),
  update: (id, data) => request.put(`/stock-out-orders/update/${id}`, data),
  remove: (id) => request.delete(`/stock-out-orders/delete/${id}`),
  // PATCH /stock-out-orders/cancel/{id}
  cancel: (id) => request.patch(`/stock-out-orders/cancel/${id}`)
}
