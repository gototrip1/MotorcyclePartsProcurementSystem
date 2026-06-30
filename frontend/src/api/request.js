import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：携带 token
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = 'Bearer ' + userStore.token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截：拆解后端统一响应 Result{code,message,data,timestamp}
request.interceptors.response.use(
  (resp) => {
    const res = resp.data
    // 非标准结构（如直接返回文件流等）直接放行
    if (res === null || res === undefined || typeof res.code === 'undefined') {
      return res
    }
    if (res.code === 200) {
      return res.data // 成功：只把 data 抛给页面
    }
    if (res.code === 401) {
      // 未登录 / 登录过期
      useUserStore().logout()
      router.push('/login')
    }
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(res)
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      useUserStore().logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
