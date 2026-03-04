import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { AuthApi, LoginRequest, UserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    localStorage.getItem('userInfo')
      ? JSON.parse(localStorage.getItem('userInfo')!)
      : null
  )

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  async function login(loginData: LoginRequest) {
    const res = await AuthApi.login(loginData)
    token.value = res.data.token
    userInfo.value = res.data.userInfo
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  async function logout() {
    try {
      await AuthApi.logout()
    } catch {
      // 忽略登出错误
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    try {
      const res = await AuthApi.me()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch {
      logout()
    }
  }

  return {
    token,
    userInfo,
    isLogin,
    isAdmin,
    login,
    logout,
    fetchUserInfo
  }
})
