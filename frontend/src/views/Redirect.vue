<template>
  <div class="redirect-container">
    <el-result
      v-if="loading"
      icon="info"
      title="正在跳转..."
      sub-title="即将跳转到目标页面"
    />
    <el-result
      v-else-if="error"
      icon="error"
      title="跳转失败"
      :sub-title="errorMessage"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')

const baseURL = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

onMounted(async () => {
  const shortCode = route.params.shortCode as string

  if (!shortCode) {
    error.value = true
    errorMessage.value = '无效的短链接'
    loading.value = false
    return
  }

  try {
    // 直接发送请求获取原始URL
    const response = await axios.get(`${baseURL}/${shortCode}`, {
      timeout: 5000,
      validateStatus: (status) => status < 500
    })

    // 检查响应状态
    if (response.status === 302 || response.status === 301) {
      // 后端返回重定向，从响应头获取原始URL
      const location = response.headers.location
      if (location) {
        window.location.href = location
      } else {
        throw new Error('未找到重定向地址')
      }
    } else if (response.status === 200 && response.data.code === 200) {
      // 如果后端返回的是JSON格式的数据
      const originalUrl = response.data.data?.originalUrl
      if (originalUrl) {
        window.location.href = originalUrl
      } else {
        throw new Error('未找到原始链接')
      }
    } else if (response.status === 404) {
      error.value = true
      errorMessage.value = '短链接不存在'
    } else if (response.status === 403) {
      error.value = true
      errorMessage.value = '该链接已被禁用'
    } else {
      throw new Error(response.data.message || '跳转失败')
    }
  } catch (err: any) {
    error.value = true
    if (err.response?.status === 404) {
      errorMessage.value = '短链接不存在'
    } else if (err.response?.status === 403) {
      errorMessage.value = '该链接已被禁用'
    } else {
      errorMessage.value = err.message || '跳转失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
})

function goHome() {
  router.push('/demo')
}
</script>

<style scoped>
.redirect-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}
</style>
