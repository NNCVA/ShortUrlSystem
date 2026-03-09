import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'

// 生产环境使用空字符串（相对路径，通过Nginx代理），开发环境使用配置的后端地址
const baseURL = import.meta.env.PROD ? '' : (import.meta.env.VITE_API_BASE || 'http://localhost:8080')

const http: AxiosInstance = axios.create({
  baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：注入 Token
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误
http.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response.data
  },
  async (error: AxiosError) => {
    const authStore = useAuthStore()
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // 判断是否为 401 且不是刷新 token 的请求
    if (error.response?.status === 401 && originalRequest && !originalRequest.url?.includes('/auth/refresh')) {
      // 如果还没有重试过，尝试刷新 token
      if (!originalRequest._retry) {
        originalRequest._retry = true
        try {
          const refreshed = await authStore.refreshTokenFunc()
          if (refreshed) {
            // 刷新成功，重新设置 Authorization 头
            originalRequest.headers.Authorization = `Bearer ${authStore.token}`
            // 重新发送请求
            return http(originalRequest)
          }
        } catch {
          // 刷新失败，继续执行后面的登出逻辑
        }
      }
      // 刷新失败或已经重试过，跳转登录页
      ElMessage.error('登录已过期，请重新登录')
      authStore.logout()
      router.push('/login')
      return Promise.reject(error)
    }

    // 非 401 错误或其他情况
    if (error.response) {
      const status = error.response.status
      if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('资源不存在')
      } else {
        ElMessage.error((error.response.data as { message?: string })?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default http
