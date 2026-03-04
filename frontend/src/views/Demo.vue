<template>
  <div class="demo-page">
    <!-- 装饰性圆形 -->
    <div class="decorative-circle circle-1"></div>
    <div class="decorative-circle circle-2"></div>
    <div class="decorative-circle circle-3"></div>
    <div class="decorative-circle circle-4"></div>

    <el-card class="demo-card">
      <template #header>
        <div class="card-header">
          <div class="logo-wrapper">
            <span class="logo-icon">⚡</span>
          </div>
          <h1 class="title">ShortURL Pro</h1>
          <p class="subtitle">短链接生成器</p>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleGenerate"
      >
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input
            v-model="form.originalUrl"
            placeholder="请输入要缩短的长链接，如：https://www.example.com/very/long/path"
            size="large"
            clearable
          >
            <template #prefix>
              <el-icon class="input-icon"><Link /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="generate-btn"
            @click="handleGenerate"
          >
            <span v-if="!loading" class="btn-text">✨ 生成短链接</span>
            <span v-else>生成中...</span>
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 结果展示 -->
      <transition name="result-slide">
        <div v-if="result" class="result-wrapper">
          <el-divider>
            <span class="divider-text">✨ 生成成功</span>
          </el-divider>

          <div class="result-content">
            <div class="result-card">
              <div class="result-label">短链接</div>
              <div class="short-url-row">
                <el-link
                  type="primary"
                  :href="result.shortUrl"
                  target="_blank"
                  class="short-url-link"
                >
                  {{ result.shortUrl }}
                </el-link>
                <el-button
                  type="primary"
                  size="small"
                  class="copy-btn"
                  @click="copyShortUrl"
                >
                  <span v-if="!copied">📋 复制</span>
                  <span v-else>✓ 已复制</span>
                </el-button>
              </div>
            </div>

            <div class="result-details">
              <div class="detail-item">
                <span class="detail-label">原始链接</span>
                <el-text class="original-url">{{ result.originalUrl }}</el-text>
              </div>
              <div class="detail-item">
                <span class="detail-label">短码</span>
                <el-tag size="large" class="short-code-tag">{{ result.shortCode }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </transition>

      <div class="actions">
        <el-button type="primary" link class="action-link" @click="router.push('/login')">
          <span class="link-icon">🔐</span> 管理后台登录
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
import { Link } from '@element-plus/icons-vue'
import { ShortLinkApi, GenerateResponse } from '@/api/shortlink'

const router = useRouter()

const formRef = ref<FormInstance>()
const loading = ref(false)
const copied = ref(false)
const result = ref<GenerateResponse | null>(null)

const form = reactive({
  originalUrl: ''
})

const rules: FormRules = {
  originalUrl: [
    { required: true, message: '请输入原始链接', trigger: 'blur' },
    {
      type: 'url',
      message: '请输入合法的 URL（以 http:// 或 https:// 开头）',
      trigger: 'blur'
    }
  ]
}

// 从URL提取域名作为默认名称
function extractDomain(url: string): string {
  try {
    const urlObj = new URL(url)
    return urlObj.hostname
  } catch {
    return '短链接'
  }
}

async function handleGenerate() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const name = extractDomain(form.originalUrl)
      const res = await ShortLinkApi.generate({ name, originalUrl: form.originalUrl })
      const shortLink = res.data
      // 使用前端地址生成短链接，隐藏后端端口
      const FRONTEND_BASE = window.location.origin
      result.value = {
        shortCode: shortLink.shortCode,
        shortUrl: `${FRONTEND_BASE}/${shortLink.shortCode}`,
        originalUrl: shortLink.originalUrl
      }
      ElMessage.success('短链接生成成功')
    } catch (error) {
      console.error('生成失败:', error)
    } finally {
      loading.value = false
    }
  })
}

function copyShortUrl() {
  if (result.value) {
    navigator.clipboard.writeText(result.value.shortUrl)
    copied.value = true
    ElMessage.success('已复制到剪贴板')
    setTimeout(() => {
      copied.value = false
    }, 2000)
  }
}
</script>

<style scoped>
.demo-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

/* 装饰性圆形 */
.decorative-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}

.circle-1 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, rgba(90, 228, 168, 0.2), rgba(183, 148, 246, 0.15));
  top: -150px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 250px;
  height: 250px;
  background: linear-gradient(135deg, rgba(255, 126, 159, 0.2), rgba(255, 181, 197, 0.15));
  bottom: -80px;
  right: -80px;
  animation-delay: 2s;
}

.circle-3 {
  width: 180px;
  height: 180px;
  background: linear-gradient(135deg, rgba(183, 148, 246, 0.25), rgba(255, 126, 159, 0.15));
  top: 20%;
  right: 5%;
  animation-delay: 4s;
}

.circle-4 {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, rgba(90, 228, 168, 0.3), rgba(255, 126, 159, 0.2));
  bottom: 20%;
  left: 5%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-25px) rotate(5deg);
  }
}

/* 玻璃拟态卡片 */
.demo-card {
  width: 100%;
  max-width: 640px;
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
  width: 70px;
  height: 70px;
  margin: 0 auto 14px;
  background: linear-gradient(135deg, var(--color-secondary), var(--color-accent));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 25px rgba(90, 228, 168, 0.4);
  animation: pulse 2.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 8px 25px rgba(90, 228, 168, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 12px 35px rgba(90, 228, 168, 0.5);
  }
}

.logo-icon {
  font-size: 32px;
}

.title {
  font-family: 'Poppins', sans-serif;
  font-size: 26px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--color-secondary), var(--color-accent));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 14px;
  letter-spacing: 3px;
}

/* 输入框图标 */
.input-icon {
  font-size: 18px;
  color: var(--color-primary);
}

/* 生成按钮 */
.generate-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  border-radius: var(--radius-md) !important;
  background: linear-gradient(135deg, var(--color-secondary), var(--color-accent)) !important;
  border: none !important;
  box-shadow: 0 4px 20px rgba(90, 228, 168, 0.4) !important;
  transition: all 0.3s ease !important;
}

.generate-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 30px rgba(90, 228, 168, 0.5) !important;
}

.btn-text {
  position: relative;
}

/* 结果区域 */
.result-wrapper {
  padding: 10px 0;
}

.divider-text {
  color: var(--color-primary);
  font-weight: 500;
}

.result-content {
  animation: resultEnter 0.5s ease-out;
}

@keyframes resultEnter {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-card {
  background: linear-gradient(135deg, rgba(90, 228, 168, 0.1), rgba(183, 148, 246, 0.1));
  border-radius: var(--radius-md);
  padding: 16px;
  margin-bottom: 16px;
}

.result-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 10px;
  font-weight: 500;
}

.short-url-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.short-url-link {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-primary) !important;
}

.copy-btn {
  background: var(--gradient-btn) !important;
  border: none !important;
  transition: all 0.3s ease !important;
}

.copy-btn:hover {
  transform: scale(1.05);
}

.result-details {
  display: grid;
  gap: 12px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.detail-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  min-width: 60px;
  padding-top: 2px;
}

.original-url {
  word-break: break-all;
  color: var(--color-text-primary);
}

.short-code-tag {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent)) !important;
  border: none !important;
  color: white !important;
  font-weight: 600;
}

/* 结果动画 */
.result-slide-enter-active {
  animation: resultEnter 0.5s ease-out;
}

.result-slide-leave-active {
  animation: resultEnter 0.3s ease-in reverse;
}

/* 底部操作 */
.actions {
  text-align: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px dashed rgba(255, 126, 159, 0.3);
}

.action-link {
  font-size: 14px;
  color: var(--color-text-secondary) !important;
  transition: all 0.3s ease !important;
}

.action-link:hover {
  color: var(--color-primary) !important;
  transform: translateY(-2px);
}

.link-icon {
  margin-right: 4px;
}
</style>
