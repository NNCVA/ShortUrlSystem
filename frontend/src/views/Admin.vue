<template>
  <div class="admin-container">
    <!-- 顶部导航 -->
    <header class="admin-header">
      <div class="header-content">
        <div class="logo-section">
          <span class="logo-icon">🔗</span>
          <h2 class="system-title">ShortURL Pro</h2>
          <span class="badge">管理后台</span>
        </div>
        <div class="user-section">
          <div class="user-avatar">
            {{ authStore.userInfo?.username?.charAt(0).toUpperCase() }}
          </div>
          <span class="welcome-text">欢迎，{{ authStore.userInfo?.username }}</span>
          <el-button type="danger" size="small" class="logout-btn" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="admin-main">
      <el-card class="table-card">
        <template #header>
          <div class="toolbar">
            <div class="search-wrapper">
              <el-input
                v-model="searchName"
                placeholder="搜索名称..."
                prefix-icon="Search"
                clearable
                class="search-input"
                @input="handleSearch"
              />
            </div>
            <el-button type="primary" class="add-btn" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增短链接
            </el-button>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="tableData"
          stripe
          class="custom-table"
        >
          <el-table-column label="ID" width="60">
            <template #default="{ $index }">
              <span class="row-index">{{ (page - 1) * pageSize + $index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="名称" min-width="100">
            <template #default="{ row }">
              <span class="link-name">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="originalUrl" label="原始链接" min-width="150">
            <template #default="{ row }">
              <el-text truncated style="max-width: 300px" class="original-url">
                {{ row.originalUrl }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column prop="shortCode" label="短码" width="120">
            <template #default="{ row }">
              <el-tag class="short-code-tag">{{ row.shortCode }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90">
            <template #default="{ row }">
              <el-tag
                :class="['status-tag', row.status === 'ENABLED' ? 'status-enabled' : 'status-disabled']"
              >
                {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="clickCount" label="点击" width="80">
            <template #default="{ row }">
              <span class="click-count">{{ row.clickCount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180" :formatter="formatDateTime" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button type="primary" link @click="handleEdit(row)">
                  编辑
                </el-button>
                <el-button
                  :type="row.status === 'ENABLED' ? 'warning' : 'success'"
                  link
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
                </el-button>
                <el-button type="danger" link @click="handleDelete(row)">
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            class="custom-pagination"
            @size-change="fetchList"
            @current-change="fetchList"
          />
        </div>
      </el-card>
    </main>

    <!-- 新增/编辑弹窗（马卡龙风格） -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑短链接' : '新增短链接'"
      width="500px"
      class="macaron-dialog"
      :close-on-click-modal="false"
      draggable
      :show-close="false"
    >
      <!-- 糖果装饰 -->
      <div class="candy-decor">🍬 🌸 ✨</div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="macaron-form"
        size="default"
      >
        <!-- 短链接名称 -->
        <div class="field-group">
          <div class="field-label">
            <span class="required">*</span>
            短链接名称
            <span class="label-badge">{{ form.name.length }}/50</span>
          </div>
          <div class="input-wrapper">
            <span class="input-icon">📁</span>
            <el-input
              v-model="form.name"
              placeholder="请输入便于识别的短链接名称（如：官网首页）"
              class="macaron-input"
              :maxlength="50"
            />
          </div>
        </div>

        <!-- 原始链接 -->
        <div class="field-group">
          <div class="field-label">
            <span class="required">*</span>
            原始链接
          </div>
          <div class="input-wrapper">
            <span class="input-icon">🔗</span>
            <el-input
              v-model="form.originalUrl"
              placeholder="请输入完整的原始链接（http:// 或 https:// 开头）"
              class="macaron-input url-input"
            />
          </div>
        </div>

        <!-- 信息提示卡片 -->
        <div class="info-tip">
          <span class="tip-icon">ℹ️</span>
          <span class="tip-text">支持 http/https 协议，建议检查链接可访问性后再提交</span>
        </div>
      </el-form>

      <!-- 装饰分隔线 -->
      <div class="macaron-divider"></div>

      <!-- 底部按钮区 -->
      <template #footer>
        <div class="macaron-footer">
          <el-button
            class="macaron-cancel"
            @click="dialogVisible = false"
          >
            取消
          </el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            class="macaron-confirm"
            @click="handleSubmit"
          >
            {{ isEdit ? '保存修改' : '创建短链接' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, FormInstance, FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { ShortLinkApi, ShortLink, CreateShortLinkRequest } from '@/api/shortlink'

// 防抖函数
const debounce = <T extends (...args: any[]) => any>(fn: T, delay: number) => {
  let timer: ReturnType<typeof setTimeout> | null = null
  return (...args: Parameters<T>) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

const router = useRouter()
const authStore = useAuthStore()

// 格式化时间函数：yyyy-mm-dd hh-mm-ss
const formatDateTime = (_row: ShortLink, _column: any, cellValue: string) => {
  if (!cellValue) return ''
  const date = new Date(cellValue)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const loading = ref(false)
const tableData = ref<ShortLink[]>([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchName = ref('')

// 弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>(0)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<CreateShortLinkRequest>({
  name: '',
  originalUrl: ''
})

const rules: FormRules = {
  name: [
    { required: true, message: '请输入短链接名称', trigger: 'blur' },
    { max: 50, message: '名称最多 50 个字符', trigger: 'blur' }
  ],
  originalUrl: [
    { required: true, message: '请输入原始链接', trigger: 'blur' },
    {
      type: 'url',
      message: '请输入合法的 URL（以 http:// 或 https:// 开头）',
      trigger: 'blur'
    }
  ]
}

onMounted(() => {
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await ShortLinkApi.list({
      keyword: searchName.value || undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data.items
    total.value = res.data.total
  } catch (error) {
    console.error('获取列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 防抖搜索
const debouncedSearch = debounce(() => {
  page.value = 1
  fetchList()
}, 300)

function handleSearch() {
  debouncedSearch()
}

function handleAdd() {
  isEdit.value = false
  form.name = ''
  form.originalUrl = ''
  dialogVisible.value = true
}

function handleEdit(row: ShortLink) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
  form.originalUrl = row.originalUrl
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await ShortLinkApi.update(editId.value, form)
        ElMessage.success('更新成功')
      } else {
        await ShortLinkApi.create(form)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      fetchList()
    } catch (error) {
      console.error('操作失败:', error)
    } finally {
      submitLoading.value = false
    }
  })
}

async function handleToggleStatus(row: ShortLink) {
  const action = row.status === 'ENABLED' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}该短链接吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    if (row.status === 'ENABLED') {
      await ShortLinkApi.disable(row.id)
    } else {
      await ShortLinkApi.enable(row.id)
    }
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
    }
  }
}

async function handleDelete(row: ShortLink) {
  try {
    await ElMessageBox.confirm('确定要删除该短链接吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await ShortLinkApi.remove(row.id)
    ElMessage.success('删除成功')
    fetchList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(255, 126, 159, 0.05) 0%, rgba(90, 228, 168, 0.05) 100%);
}

/* 顶部导航 */
.admin-header {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-accent) 100%);
  padding: 0 30px;
  box-shadow: 0 4px 20px rgba(255, 126, 159, 0.3);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 64px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 28px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.system-title {
  font-family: 'Poppins', sans-serif;
  font-size: 22px;
  font-weight: 700;
  color: white;
  margin: 0;
}

.badge {
  background: rgba(255, 255, 255, 0.25);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 16px;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.welcome-text {
  color: white;
  font-weight: 500;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.5) !important;
  color: white !important;
  transition: all 0.3s ease !important;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.35) !important;
  transform: translateY(-2px);
}

/* 主内容区 */
.admin-main {
  padding: 30px;
}

/* 表格卡片 */
.table-card {
  background: var(--glass-bg);
  backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--glass-shadow);
  animation: cardEnter 0.5s ease-out;
}

@keyframes cardEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.search-wrapper {
  flex: 1;
  max-width: 300px;
}

.search-input {
  border-radius: var(--radius-md) !important;
}

.add-btn {
  background: var(--gradient-btn) !important;
  border: none !important;
  border-radius: var(--radius-md) !important;
  box-shadow: 0 4px 15px rgba(255, 126, 159, 0.4) !important;
  transition: all 0.3s ease !important;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 126, 159, 0.5) !important;
}

/* 表格样式 */
.custom-table {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.row-index {
  color: var(--color-text-light);
  font-weight: 500;
}

.link-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.original-url {
  color: var(--color-text-secondary);
}

.short-code-tag {
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent)) !important;
  border: none !important;
  color: white !important;
  font-weight: 600;
}

.status-tag {
  border: none !important;
  font-weight: 500;
}

.status-enabled {
  background: linear-gradient(135deg, #5AE4A8, #4FD1C5) !important;
  color: white !important;
}

.status-disabled {
  background: linear-gradient(135deg, #FC8181, #F56565) !important;
  color: white !important;
}

.click-count {
  font-weight: 700;
  color: var(--color-primary);
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons .el-button {
  padding: 4px 8px;
}

/* 编辑按钮 - 白色 */
.action-buttons .el-button--primary,
.action-buttons .el-button[type="primary"] {
  color: white !important;
}

/* 启用/禁用按钮 - 薄荷绿 */
.action-buttons .el-button[type="success"] {
  color: var(--color-secondary);
}

/* 禁用按钮 - 珊瑚粉 */
.action-buttons .el-button[type="warning"] {
  color: var(--color-primary);
}

/* 删除按钮 - 薰衣草紫 */
.action-buttons .el-button[type="danger"] {
  color: var(--color-accent);
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.custom-pagination {
  border-radius: var(--radius-md);
  overflow: hidden;
}

.custom-pagination .el-pager li.is-active {
  background: var(--gradient-btn) !important;
}

/* ========== 马卡龙弹窗样式 ========== */
.macaron-dialog.el-dialog {
  border-radius: 28px !important;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.82) !important;
  backdrop-filter: blur(20px);
  border: 1.5px solid rgba(255, 255, 255, 0.9) !important;
  box-shadow: 0 20px 60px rgba(196, 181, 253, 0.35), 0 6px 20px rgba(249, 168, 201, 0.25) !important;
}

.macaron-dialog.el-dialog__header {
  background: linear-gradient(115deg, #F9A8C9 0%, #C4B5FD 55%, #93C5FD 100%) !important;
  padding: 22px 28px 20px !important;
  margin: 0 !important;
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.macaron-dialog.el-dialog__title {
  color: white !important;
  font-family: 'Noto Sans SC', sans-serif !important;
  font-weight: 700 !important;
  font-size: 18px !important;
  letter-spacing: 0.06em;
  text-shadow: 0 1px 6px rgba(130, 80, 160, 0.18);
}

.macaron-dialog.el-dialog__headerbtn {
  position: absolute;
  top: 14px;
  right: 16px;
  display: none;
}

.macaron-dialog.el-dialog__headerbtn .el-dialog__close {
  color: white !important;
  font-size: 15px !important;
  font-weight: 700;
  transition: all 0.2s ease;
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.macaron-dialog.el-dialog__headerbtn .el-dialog__close:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: rotate(90deg);
}

.macaron-dialog.el-dialog__body {
  padding: 28px 28px 10px !important;
  background: transparent !important;
}

/* 糖果装饰 */
.candy-decor {
  position: absolute;
  top: -28px;
  right: 24px;
  font-size: 20px;
  letter-spacing: 4px;
  pointer-events: none;
  z-index: 1;
}

/* 头部装饰圆圈 */
.macaron-dialog.el-dialog__header::after {
  content: '';
  position: absolute;
  right: -30px;
  top: -30px;
  width: 110px;
  height: 110px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 50%;
  pointer-events: none;
}

/* 表单容器 */
.macaron-form {
  margin: 0;
}

/* 表单分组 */
.field-group {
  margin-bottom: 22px;
}

/* 表单标签 */
.field-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #4B3F6B;
  letter-spacing: 0.04em;
  margin-bottom: 10px;
}

.field-label .required {
  color: #F9A8C9;
  font-size: 16px;
  line-height: 1;
}

.field-label .label-badge {
  background: linear-gradient(135deg, #fce7f3, #ede9fe);
  color: #7C6FAA;
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 20px;
  margin-left: auto;
}

/* 输入框容器 */
.input-wrapper {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  pointer-events: none;
  opacity: 0.7;
  z-index: 1;
}

/* 马卡龙输入框 */
.macaron-input {
  width: 100%;
  padding: 13px 14px 13px 42px !important;
  background: linear-gradient(135deg, rgba(252, 231, 243, 0.5), rgba(237, 233, 254, 0.5)) !important;
  border: 1.8px solid rgba(196, 181, 253, 0.35) !important;
  border-radius: 14px !important;
  font-family: inherit !important;
  font-size: 14px !important;
  color: #4B3F6B !important;
  outline: none !important;
  transition: border-color 0.25s, box-shadow 0.25s, background 0.25s !important;
  caret-color: #C4B5FD;
  height: auto !important;
  line-height: 1.5 !important;
}

.macaron-input::placeholder {
  color: #B8AED4;
  font-size: 13px;
}

.macaron-input:hover {
  border-color: rgba(196, 181, 253, 0.6) !important;
}

.macaron-input:focus {
  border-color: #C4B5FD !important;
  background: linear-gradient(135deg, rgba(253, 242, 248, 0.8), rgba(245, 243, 255, 0.8)) !important;
  box-shadow: 0 0 0 4px rgba(196, 181, 253, 0.18), 0 2px 12px rgba(196, 181, 253, 0.15) !important;
}

.url-input {
  padding-right: 14px !important;
}

/* 输入框内部前缀 */
.macaron-input :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.macaron-input :deep(.el-input__inner) {
  color: #4B3F6B !important;
}

.macaron-input :deep(.el-input__inner)::placeholder {
  color: #B8AED4 !important;
}

/* 信息提示卡片 */
.info-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: linear-gradient(135deg, rgba(186, 230, 253, 0.35), rgba(167, 243, 208, 0.25));
  border: 1.5px solid rgba(147, 197, 253, 0.4);
  border-radius: 12px;
  padding: 11px 14px;
  margin-bottom: 8px;
}

.info-tip .tip-icon {
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 1px;
}

.info-tip .tip-text {
  font-size: 12.5px;
  color: #4A7FA5;
  line-height: 1.55;
  font-weight: 400;
}

/* 装饰分隔线 */
.macaron-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(196, 181, 253, 0.3) 30%, rgba(249, 168, 201, 0.3) 70%, transparent);
  margin: 0 28px;
}

/* 底部按钮区 */
.macaron-footer {
  padding: 18px 28px 24px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}

/* 取消按钮 */
.macaron-cancel {
  padding: 11px 24px !important;
  background: rgba(237, 233, 254, 0.6) !important;
  border: 1.5px solid rgba(196, 181, 253, 0.4) !important;
  border-radius: 999px !important;
  color: #7C6FAA !important;
  font-family: inherit !important;
  font-size: 14px !important;
  font-weight: 500 !important;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s !important;
  letter-spacing: 0.03em;
}

.macaron-cancel:hover {
  background: rgba(237, 233, 254, 0.9) !important;
  transform: translateY(-1px);
}

/* 确认按钮 */
.macaron-confirm {
  padding: 11px 28px !important;
  background: linear-gradient(135deg, #F9A8C9 0%, #C4B5FD 100%) !important;
  border: none !important;
  border-radius: 999px !important;
  color: #fff !important;
  font-family: inherit !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(196, 181, 253, 0.45), 0 2px 6px rgba(249, 168, 201, 0.3) !important;
  transition: transform 0.2s, box-shadow 0.2s, opacity 0.2s !important;
  letter-spacing: 0.05em;
  position: relative;
  overflow: hidden;
}

.macaron-confirm::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25), transparent);
  border-radius: inherit;
}

.macaron-confirm:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(196, 181, 253, 0.5) !important;
}

.macaron-confirm:active:not(:disabled) {
  transform: translateY(0);
}

.macaron-confirm:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* 响应式适配 */
@media (max-width: 576px) {
  .macaron-dialog.el-dialog {
    width: 92vw !important;
    margin: 0 auto;
  }

  .macaron-footer {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>
