import request from './request'

// 对应后端 PartController（基础路径 /api/parts）
export const partApi = {
  page: (params) => request.get('/parts/page', { params }),
  get: (id) => request.get(`/parts/get/${id}`),
  create: (data) => request.post('/parts/create', data),
  update: (id, data) => request.put(`/parts/update/${id}`, data),
  remove: (id) => request.delete(`/parts/delete/${id}`),
  search: (name) => request.get('/parts/search', { params: { name } }),
  // PATCH /parts/update-price/{id}?purchasePrice=
  updatePrice: (id, purchasePrice) =>
    request.patch(`/parts/update-price/${id}`, null, { params: { purchasePrice } }),
  // GET /parts/supplier/{supplierId}
  listBySupplier: (supplierId) => request.get(`/parts/supplier/${supplierId}`)
}
