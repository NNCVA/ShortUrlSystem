<template>
  <div class="demo-container">
    <el-card class="demo-card">
      <template #header>
        <div class="card-header">
          <h2>ShortURL Pro</h2>
          <p>短链接生成演示页</p>
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
              <el-icon><Link /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            style="width: 100%"
            @click="handleGenerate"
          >
            生成短链接
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider v-if="result" />

      <div v-if="result" class="result">
        <el-result icon="success" title="生成成功">
          <template #sub-title>
            <div class="result-content">
              <p class="label">短链接：</p>
              <div class="short-url">
                <el-link type="primary" :href="result.shortUrl" target="_blank">
                  {{ result.shortUrl }}
                </el-link>
                <el-button type="primary" size="small" @click="copyShortUrl">
                  复制
                </el-button>
              </div>

              <p class="label" style="margin-top: 16px;">原始链接：</p>
              <el-text class="original-url">{{ result.originalUrl }}</el-text>

              <p class="label" style="margin-top: 16px;">短码：</p>
              <el-tag size="large">{{ result.shortCode }}</el-tag>
            </div>
          </template>
        </el-result>
      </div>

      <div class="actions">
        <el-button type="primary" link @click="router.push('/login')">
          管理后台登录
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="primary" link @click="router.push('/admin')">
          进入管理后台
        </el-button>
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

async function handleGenerate() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await ShortLinkApi.generate({ originalUrl: form.originalUrl })
      result.value = res.data
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
    ElMessage.success('已复制到剪贴板')
  }
}
</script>

<style scoped>
.demo-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.demo-card {
  width: 100%;
  max-width: 600px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.card-header p {
  margin: 8px 0 0;
  color: #999;
  font-size: 14px;
}

.result-content {
  text-align: left;
  padding: 0 40px;
}

.label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.short-url {
  display: flex;
  align-items: center;
  gap: 12px;
}

.original-url {
  word-break: break-all;
}

.actions {
  text-align: center;
  margin-top: 20px;
}
</style>
