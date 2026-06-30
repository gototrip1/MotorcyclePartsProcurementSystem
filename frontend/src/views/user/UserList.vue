<template>
  <div class="page-container">
    <el-card shadow="never" class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="账号">
          <el-input v-model="query.username" placeholder="账号模糊查询" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="query.department" placeholder="部门" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="title">用户列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建用户</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="username" label="账号" width="130" />
        <el-table-column prop="realName" label="姓名" width="110" />
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <el-tag>{{ USER_ROLE[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(v) => toggleStatus(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openRole(row)">分配角色</el-button>
            <el-button link type="info" @click="openReset(row)">重置密码</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadData"
          @size-change="onSearch"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新建用户'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item v-if="!form.id" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="form.department" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleVisible" title="分配角色" width="380px">
      <el-form label-width="80px">
        <el-form-item label="用户">{{ roleForm.realName }}（{{ roleForm.username }}）</el-form-item>
        <el-form-item label="角色">
          <el-select v-model="roleForm.role" style="width: 100%">
            <el-option v-for="o in USER_ROLE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitRole">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="380px">
      <el-form label-width="90px">
        <el-form-item label="用户">{{ resetForm.username }}</el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { USER_ROLE, USER_ROLE_OPTIONS } from '@/utils/enums'

const query = reactive({ current: 1, size: 10, username: '', role: null, department: '' })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await userApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.current = 1
  loadData()
}
function onReset() {
  Object.assign(query, { current: 1, username: '', role: null, department: '' })
  loadData()
}
onMounted(loadData)

// 新建/编辑
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({})
const defaultForm = () => ({
  id: null,
  username: '',
  password: '',
  realName: '',
  role: 'purchase',
  department: '',
  phone: '',
  email: '',
  status: 1
})
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}
function openCreate() {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
function openEdit(row) {
  Object.assign(form, defaultForm(), row, { password: '' })
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      // 剔除服务端托管字段（createTime/updateTime/deleted），避免后端日期反序列化报 500
      const { createTime, updateTime, deleted, ...rest } = form
      if (form.id) {
        // 编辑时若未填新密码，则不提交 password，避免把密码改空
        const payload = { ...rest }
        if (!payload.password) delete payload.password
        await userApi.update(form.id, payload)
      } else {
        await userApi.create(rest)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } finally {
      saving.value = false
    }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' })
  await userApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

async function toggleStatus(row, enabled) {
  try {
    enabled ? await userApi.enable(row.id) : await userApi.disable(row.id)
    ElMessage.success(enabled ? '已启用' : '已禁用')
    loadData()
  } catch (e) {
    loadData()
  }
}

// 分配角色
const roleVisible = ref(false)
const roleForm = reactive({ id: null, username: '', realName: '', role: '' })
function openRole(row) {
  Object.assign(roleForm, { id: row.id, username: row.username, realName: row.realName, role: row.role })
  roleVisible.value = true
}
async function submitRole() {
  saving.value = true
  try {
    await userApi.assignRole(roleForm.id, roleForm.role)
    ElMessage.success('角色已更新')
    roleVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

// 重置密码
const resetVisible = ref(false)
const resetForm = reactive({ id: null, username: '', password: '' })
function openReset(row) {
  Object.assign(resetForm, { id: row.id, username: row.username, password: '' })
  resetVisible.value = true
}
async function submitReset() {
  if (!resetForm.password) {
    ElMessage.warning('请输入新密码')
    return
  }
  saving.value = true
  try {
    await userApi.resetPassword(resetForm.id, resetForm.password)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.title {
  font-weight: 600;
}
</style>
