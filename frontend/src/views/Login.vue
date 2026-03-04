<template>
  <div class="login-page">
    <!-- 装饰性圆形 -->
    <div class="decorative-circle circle-1"></div>
    <div class="decorative-circle circle-2"></div>
    <div class="decorative-circle circle-3"></div>

    <!-- 登录卡片 -->
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <div class="logo-wrapper">
            <span class="logo-icon">🔗</span>
          </div>
          <h1 class="title">ShortURL Pro</h1>
          <p class="subtitle">短链接管理系统</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            <span v-if="!loading">立即登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <div class="tips">
        <p>默认账号：admin / admin123456</p>
      </div>

      <div class="footer-links">
        <el-button type="primary" link @click="router.push('/')">
          返回首页
        </el-button>
        <el-divider direction="vertical" />

      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为 6-30 个字符', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await authStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/admin')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 装饰性圆形 */
.decorative-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, rgba(255, 126, 159, 0.3), rgba(255, 181, 197, 0.2));
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(90, 228, 168, 0.3), rgba(183, 148, 246, 0.2));
  bottom: -50px;
  right: -50px;
  animation-delay: 2s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, rgba(183, 148, 246, 0.3), rgba(255, 126, 159, 0.2));
  top: 50%;
  right: 10%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

/* 玻璃拟态卡片 */
.login-card {
  width: 100%;
  max-width: 440px;
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  animation: cardEnter 0.6s ease-out;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.card-header {
  text-align: center;
  padding: 10px 0 20px;
}

.logo-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(255, 126, 159, 0.4);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8px 25px rgba(255, 126, 159, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 12px 35px rgba(255, 126, 159, 0.5);
  }
}

.logo-icon {
  font-size: 36px;
}

.title {
  font-family: 'Poppins', sans-serif;
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  letter-spacing: 2px;
}

/* 表单样式 */
.el-form-item {
  margin-bottom: 24px;
}

.el-form-item >>> .el-form-item__label {
  color: var(--color-text-primary);
  font-weight: 500;
  margin-bottom: 8px;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: var(--radius-md) !important;
  background: var(--gradient-btn) !important;
  border: none !important;
  transition: all 0.3s ease !important;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s ease;
}

.login-btn:hover::before {
  left: 100%;
}

.login-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(255, 126, 159, 0.5) !important;
}

/* 提示信息 */
.tips {
  text-align: center;
  color: var(--color-text-light);
  font-size: 13px;
  margin-top: 20px;
  padding: 12px;
  background: rgba(255, 126, 159, 0.1);
  border-radius: var(--radius-sm);
}

.tips p {
  margin: 4px 0;
}

/* 底部链接 */
.footer-links {
  text-align: center;
  margin-top: 20px;
}

.footer-links .el-button {
  font-size: 14px;
  color: white;
  transition: color 0.3s ease;
}

.footer-links .el-button:hover {
  color: rgba(255, 255, 255, 0.8);
}
</style>
