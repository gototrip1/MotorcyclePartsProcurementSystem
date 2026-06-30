<template>
  <div class="page-container">
    <!-- 查询条件 -->
    <el-card shadow="never" class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="供应商名称">
          <el-input v-model="query.name" placeholder="名称模糊查询" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(v, k) in SUPPLIER_STATUS" :key="k" :label="v.text" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="信用评级">
          <el-select v-model="query.creditRating" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="o in CREDIT_RATING_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="title">供应商列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建供应商</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="supplierCode" label="编码" width="130" />
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="contactPerson" label="联系人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="creditRating" label="信用评级" width="90" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :map="SUPPLIER_STATUS" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <!-- 管理员审核：审核中(3) 可通过/驳回 -->
            <template v-if="userStore.isAdmin && row.status === 3">
              <el-button link type="success" @click="audit(row, 1)">通过</el-button>
              <el-button link type="danger" @click="audit(row, 2)">驳回</el-button>
            </template>
            <!-- 管理员对合作中/已终止做启停 -->
            <template v-if="userStore.isAdmin && row.status !== 3">
              <el-button v-if="row.status === 1" link type="warning" @click="changeStatus(row, 2)">终止</el-button>
              <el-button v-if="row.status === 2" link type="success" @click="changeStatus(row, 1)">启用</el-button>
            </template>
            <el-button v-if="userStore.isAdmin" link type="danger" @click="remove(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑供应商' : '新建供应商'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="form.supplierCode" placeholder="如 SUP001" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="信用评级" prop="creditRating">
          <el-select v-model="form.creditRating" style="width: 100%">
            <el-option v-for="o in CREDIT_RATING_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { supplierApi } from '@/api/supplier'
import { useUserStore } from '@/stores/user'
import { SUPPLIER_STATUS, CREDIT_RATING_OPTIONS } from '@/utils/enums'
import StatusTag from '@/components/StatusTag.vue'

const userStore = useUserStore()

const query = reactive({ current: 1, size: 10, name: '', status: null, creditRating: null })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await supplierApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    /* 已统一提示 */
  } finally {
    loading.value = false
  }
}
function onSearch() {
  query.current = 1
  loadData()
}
function onReset() {
  Object.assign(query, { current: 1, name: '', status: null, creditRating: null })
  loadData()
}
onMounted(loadData)

// 弹窗
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  supplierCode: '',
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  creditRating: 'B'
})
const rules = {
  supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    supplierCode: '',
    name: '',
    contactPerson: '',
    phone: '',
    email: '',
    address: '',
    creditRating: 'B'
  })
}
function openCreate() {
  resetForm()
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
function openEdit(row) {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      // 仅提交业务字段：剔除 createTime/updateTime/deleted 等服务端托管字段，
      // 否则后端日期反序列化（要求 yyyy-MM-dd'T'HH:mm:ss.SSS）会因格式不符而 500。
      const payload = {
        supplierCode: form.supplierCode,
        name: form.name,
        contactPerson: form.contactPerson,
        phone: form.phone,
        email: form.email,
        address: form.address,
        creditRating: form.creditRating,
        status: form.status
      }
      if (form.id) {
        await supplierApi.update(form.id, payload)
      } else {
        // 新建供应商默认进入审核中（status=3），交由管理员审核
        await supplierApi.create({ ...payload, status: userStore.isAdmin ? 1 : 3 })
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } catch (e) {
      /* 已统一提示 */
    } finally {
      saving.value = false
    }
  })
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除供应商「${row.name}」？`, '提示', { type: 'warning' })
  await supplierApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 审核：pass=1 通过(合作中)，pass=2 驳回(已终止)
async function audit(row, status) {
  const txt = status === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确认${txt}供应商「${row.name}」？`, '审核', { type: 'warning' })
  await supplierApi.updateStatus(row.id, status)
  ElMessage.success(`已${txt}`)
  loadData()
}

async function changeStatus(row, status) {
  const txt = status === 1 ? '启用' : '终止'
  await ElMessageBox.confirm(`确认${txt}该供应商？`, '提示', { type: 'warning' })
  await supplierApi.updateStatus(row.id, status)
  ElMessage.success(`已${txt}`)
  loadData()
}
</script>

<style scoped>
.title {
  font-weight: 600;
}
</style>
