import request from './request'

// 对应后端 SupplierController（基础路径 /api/suppliers）
export const supplierApi = {
  page: (params) => request.get('/suppliers/page', { params }),
  get: (id) => request.get(`/suppliers/get/${id}`),
  create: (data) => request.post('/suppliers/create', data),
  update: (id, data) => request.put(`/suppliers/update/${id}`, data),
  remove: (id) => request.delete(`/suppliers/delete/${id}`),
  search: (name) => request.get('/suppliers/search', { params: { name } }),
  // PATCH /suppliers/update-status/{id}?status=
  updateStatus: (id, status) =>
    request.patch(`/suppliers/update-status/${id}`, null, { params: { status } }),
  // PATCH /suppliers/update-credit-rating/{id}?creditRating=
  updateCreditRating: (id, creditRating) =>
    request.patch(`/suppliers/update-credit-rating/${id}`, null, { params: { creditRating } })
}
