import request from './request'

// 对应后端 UserController（基础路径 /api/users）
export const userApi = {
  page: (params) => request.get('/users/page', { params }),
  get: (id) => request.get(`/users/get/${id}`),
  create: (data) => request.post('/users/create', data),
  update: (id, data) => request.put(`/users/update/${id}`, data),
  remove: (id) => request.delete(`/users/delete/${id}`),
  // PATCH /users/assign-role/{id}?role=
  assignRole: (id, role) =>
    request.patch(`/users/assign-role/${id}`, null, { params: { role } }),
  // PATCH /users/enable/{id}
  enable: (id) => request.patch(`/users/enable/${id}`),
  // PATCH /users/disable/{id}
  disable: (id) => request.patch(`/users/disable/${id}`),
  // PATCH /users/reset-password/{id}?password=
  resetPassword: (id, password) =>
    request.patch(`/users/reset-password/${id}`, null, { params: { password } }),
  // PATCH /users/change-password  body: {id, oldPassword, newPassword}
  // 用户修改自己的密码，后端校验原密码
  changePassword: (data) => request.patch('/users/change-password', data)
}
