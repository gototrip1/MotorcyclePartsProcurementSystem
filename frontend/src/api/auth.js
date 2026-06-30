import request from './request'

// 对应后端 AuthController（基础路径 /api/auth）
export const authApi = {
  // POST /api/auth/login  body: {username, password}
  // 返回 {token, id, username, realName, role, department}
  login: (data) => request.post('/auth/login', data),
  // POST /api/auth/logout
  logout: () => request.post('/auth/logout')
}
