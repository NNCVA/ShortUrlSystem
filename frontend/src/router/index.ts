import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/demo'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/demo',
    name: 'Demo',
    component: () => import('@/views/Demo.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/views/Admin.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/:shortCode',
    name: 'Redirect',
    component: () => import('@/views/Redirect.vue'),
    meta: { requiresAuth: false }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  // 需要认证的页面
  if (to.meta.requiresAuth && !authStore.isLogin) {
    next('/login')
    return
  }

  // 已登录用户访问登录页，跳转到管理页
  if (to.path === '/login' && authStore.isLogin) {
    next('/admin')
    return
  }

  next()
})

export default router
