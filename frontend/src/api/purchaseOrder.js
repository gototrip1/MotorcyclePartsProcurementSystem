import request from './request'

// 对应后端 PurchaseOrderController（基础路径 /api/orders）
export const purchaseOrderApi = {
  page: (params) => request.get('/orders/page', { params }),
  get: (id) => request.get(`/orders/get/${id}`),
  // GET /orders/details/{id} -> {order, details}
  details: (id) => request.get(`/orders/details/${id}`),
  create: (data) => request.post('/orders/create', data),
  update: (id, data) => request.put(`/orders/update/${id}`, data),
  remove: (id) => request.delete(`/orders/delete/${id}`),
  // PATCH /orders/update-status/{id}?status=
  updateStatus: (id, status) =>
    request.patch(`/orders/update-status/${id}`, null, { params: { status } }),
  // PATCH /orders/update-logistics/{id}?logisticsCompany=&trackingNumber=&shipTime=&expectedDeliveryDate=
  updateLogistics: (id, params) =>
    request.patch(`/orders/update-logistics/${id}`, null, { params }),
  // PATCH /orders/submit-inbound/{id} 状态 1->2
  submitInbound: (id) => request.patch(`/orders/submit-inbound/${id}`),
  // PATCH /orders/mark-paid/{id}
  markPaid: (id) => request.patch(`/orders/mark-paid/${id}`),
  // GET /orders/statistics?startDate=&endDate=
  statistics: (params) => request.get('/orders/statistics', { params }),
  // GET /orders/search-details?partName=&startDate=&endDate=
  searchDetails: (params) => request.get('/orders/search-details', { params })
}
