<template>
  <div class="admin-container">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>ShortURL Pro 管理后台</h2>
          <div class="user-info">
            <span>欢迎，{{ authStore.userInfo?.username }}</span>
            <el-button type="danger" link @click="handleLogout">
              退出登录
            </el-button>
          </div>
        </div>
      </el-header>

      <el-main>
        <el-card>
          <template #header>
            <div class="toolbar">
              <el-input
                v-model="searchName"
                placeholder="搜索名称..."
                prefix-icon="Search"
                clearable
                style="width: 200px"
                @input="handleSearch"
              />
              <el-button type="primary" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增短链接
              </el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="tableData"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="name" label="名称" min-width="120" />
            <el-table-column prop="originalUrl" label="原始链接" min-width="200">
              <template #default="{ row }">
                <el-text truncated style="max-width: 200px">
                  {{ row.originalUrl }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="shortCode" label="短码" width="100">
              <template #default="{ row }">
                <el-tag>{{ row.shortCode }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'">
                  {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="clickCount" label="点击数" width="80" />
            <el-table-column prop="createdAt" label="创建时间" width="160" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
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
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            style="margin-top: 20px; justify-content: center"
            @size-change="fetchList"
            @current-change="fetchList"
          />
        </el-card>
      </el-main>
    </el-container>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑短链接' : '新增短链接'"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入短链接名称" />
        </el-form-item>
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input
            v-model="form.originalUrl"
            placeholder="请输入完整的原始链接（http:// 或 https:// 开头）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
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

const router = useRouter()
const authStore = useAuthStore()

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
      name: searchName.value || undefined,
      page: page.value,
      pageSize: pageSize.value
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('获取列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
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
  background: #f5f7fa;
}

.el-container {
  min-height: 100vh;
}

.el-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.el-main {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
