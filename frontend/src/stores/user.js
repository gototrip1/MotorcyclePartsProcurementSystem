import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

/**
 * 用户登录态。
 * 注意：后端登录返回的是单个 role 字符串（admin/purchase/requisition），
 * 这里统一保存为 role 字段，并提供基于角色的 getter 与守卫使用的判断方法。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  getters: {
    isLogin: (s) => !!s.token,
    isAdmin: (s) => s.role === 'admin',
    isPurchase: (s) => s.role === 'purchase',
    isRequisition: (s) => s.role === 'requisition',
    roleText: (s) => {
      const map = { admin: '管理员', purchase: '采购专员', requisition: '领用人员' }
      return map[s.role] || s.role
    }
  },
  actions: {
    async login(form) {
      // 后端返回 { token, id, username, realName, role, department }
      const data = await authApi.login(form)
      this.token = data.token
      this.role = data.role
      this.userInfo = {
        id: data.id,
        username: data.username,
        realName: data.realName,
        role: data.role,
        department: data.department
      }
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      return data
    },
    async logout(callApi = false) {
      if (callApi) {
        try {
          await authApi.logout()
        } catch (e) {
          /* 忽略登出接口异常 */
        }
      }
      this.token = ''
      this.role = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userInfo')
    },
    /** 当前用户是否拥有给定角色列表中的任一角色 */
    hasRole(roles) {
      if (!roles || roles.length === 0) return true
      return roles.includes(this.role)
    }
  }
})
