<template>
  <div class="page-container">
    <el-card shadow="never" class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="零件名称">
          <el-input v-model="query.name" placeholder="名称模糊查询" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="query.category" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="o in PART_CATEGORY_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-select
            v-model="query.supplierId"
            placeholder="全部"
            clearable
            filterable
            style="width: 180px"
          >
            <el-option v-for="s in supplierOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="title">零件列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建零件</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="partCode" label="编码" width="120" />
        <el-table-column prop="name" label="名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="unit" label="单位" width="70" align="center" />
        <el-table-column label="采购单价" width="110" align="right">
          <template #default="{ row }">{{ formatMoney(row.purchasePrice) }}</template>
        </el-table-column>
        <el-table-column label="供应商" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ supplierName(row.supplierId) }}</template>
        </el-table-column>
        <el-table-column prop="stockWarningValue" label="预警值" width="80" align="center" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="userStore.isAdmin" link type="warning" @click="openPrice(row)">锁价</el-button>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑零件' : '新建零件'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="零件编码" prop="partCode">
              <el-input v-model="form.partCode" placeholder="如 P0001" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="型号" prop="model">
              <el-input v-model="form.model" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="form.specification" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-select v-model="form.category" style="width: 100%">
                <el-option v-for="o in PART_CATEGORY_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-select v-model="form.unit" style="width: 100%" allow-create filterable>
                <el-option v-for="o in UNIT_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="form.supplierId" filterable style="width: 100%">
                <el-option v-for="s in supplierOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购单价" prop="purchasePrice">
              <!-- 采购员只读，仅管理员可改（或通过锁价按钮） -->
              <el-input-number
                v-model="form.purchasePrice"
                :min="0"
                :precision="2"
                :step="1"
                :disabled="!userStore.isAdmin"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="建议零售价" prop="suggestedRetailPrice">
              <el-input-number
                v-model="form.suggestedRetailPrice"
                :min="0"
                :precision="2"
                :step="1"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存预警值" prop="stockWarningValue">
              <el-input-number v-model="form.stockWarningValue" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="描述" prop="description">
              <el-input v-model="form.description" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 锁价弹窗 -->
    <el-dialog v-model="priceVisible" title="锁定采购单价" width="380px">
      <el-form label-width="90px">
        <el-form-item label="零件">{{ priceForm.name }}</el-form-item>
        <el-form-item label="采购单价">
          <el-input-number v-model="priceForm.purchasePrice" :min="0" :precision="2" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="priceVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPrice">确定锁价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { partApi } from '@/api/part'
import { supplierApi } from '@/api/supplier'
import { useUserStore } from '@/stores/user'
import { PART_CATEGORY_OPTIONS, UNIT_OPTIONS } from '@/utils/enums'
import { formatMoney } from '@/utils/format'

const userStore = useUserStore()

const query = reactive({ current: 1, size: 10, name: '', category: null, supplierId: null })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const supplierOptions = ref([])

function supplierName(id) {
  return supplierOptions.value.find((s) => s.id === id)?.name || '-'
}

async function loadSuppliers() {
  try {
    supplierOptions.value = (await supplierApi.search('')) || []
  } catch (e) {
    /* ignore */
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await partApi.page(query)
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
  Object.assign(query, { current: 1, name: '', category: null, supplierId: null })
  loadData()
}
onMounted(() => {
  loadSuppliers()
  loadData()
})

// 弹窗
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({})
const defaultForm = () => ({
  id: null,
  partCode: '',
  name: '',
  model: '',
  specification: '',
  unit: '个',
  category: '',
  supplierId: null,
  purchasePrice: 0,
  suggestedRetailPrice: 0,
  stockWarningValue: 10,
  description: ''
})
const rules = {
  partCode: [{ required: true, message: '请输入零件编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  purchasePrice: [{ required: true, message: '请输入采购单价', trigger: 'blur' }],
  supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }]
}

function openCreate() {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
function openEdit(row) {
  Object.assign(form, defaultForm(), row)
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      // 剔除服务端托管字段（createTime/updateTime/deleted），避免后端日期反序列化报 500
      const { createTime, updateTime, deleted, ...payload } = form
      form.id ? await partApi.update(form.id, payload) : await partApi.create(payload)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } finally {
      saving.value = false
    }
  })
}
async function remove(row) {
  await ElMessageBox.confirm(`确认删除零件「${row.name}」？`, '提示', { type: 'warning' })
  await partApi.remove(row.id)
  ElMessage.success('删除成功')
  loadData()
}

// 锁价
const priceVisible = ref(false)
const priceForm = reactive({ id: null, name: '', purchasePrice: 0 })
function openPrice(row) {
  Object.assign(priceForm, { id: row.id, name: row.name, purchasePrice: row.purchasePrice })
  priceVisible.value = true
}
async function submitPrice() {
  saving.value = true
  try {
    await partApi.updatePrice(priceForm.id, priceForm.purchasePrice)
    ElMessage.success('锁价成功')
    priceVisible.value = false
    loadData()
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
