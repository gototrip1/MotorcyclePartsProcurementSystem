<template>
  <div class="page-container">
    <el-card shadow="never" class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="领用单号">
          <el-input v-model="query.stockOutNumber" placeholder="单号" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(v, k) in STOCKOUT_STATUS" :key="k" :label="v.text" :value="Number(k)" />
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
        <span class="title">领用单列表</span>
        <el-button type="primary" :icon="Plus" @click="openCreate">新建领用单</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="stockOutNumber" label="领用单号" width="180" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="purpose" label="用途" width="100" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag :map="STOCKOUT_STATUS" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="领用时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.stockOutTime) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="cancel(row)">取消</el-button>
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

    <!-- 新建领用单弹窗 -->
    <el-dialog v-model="dialogVisible" title="新建领用单" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="领用单号" prop="stockOutNumber">
              <el-input v-model="form.stockOutNumber" placeholder="留空自动生成（SO+时间戳）" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="部门" prop="department">
              <el-input v-model="form.department" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用途" prop="purpose">
              <el-select v-model="form.purpose" style="width: 100%">
                <el-option v-for="o in PURPOSE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">领用明细</el-divider>
        <el-table :data="form.details" border size="small">
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="零件" min-width="240">
            <template #default="{ row }">
              <el-select v-model="row.partId" filterable placeholder="选择零件" style="width: 100%">
                <el-option v-for="p in partOptions" :key="p.id" :label="`${p.name}（${p.partCode}）`" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="领用数量" width="160">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div style="margin-top: 12px">
          <el-button :icon="Plus" @click="addRow">添加明细</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">提交出库申请</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="领用单详情" size="560px">
      <el-descriptions v-if="detail.order" :column="2" border>
        <el-descriptions-item label="单号">{{ detail.order.stockOutNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :map="STOCKOUT_STATUS" :value="detail.order.status" />
        </el-descriptions-item>
        <el-descriptions-item label="部门">{{ detail.order.department || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用途">{{ detail.order.purpose || '-' }}</el-descriptions-item>
        <el-descriptions-item label="领用时间" :span="2">{{ formatDateTime(detail.order.stockOutTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">领用明细</el-divider>
      <!-- get 接口返回的明细为 { detail, part } 视图对象 -->
      <el-table :data="detail.details" border size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="零件" min-width="160">
          <template #default="{ row }">{{ row.part?.name || partName(row.detail?.partId) }}</template>
        </el-table-column>
        <el-table-column label="数量" width="80" align="center">
          <template #default="{ row }">{{ row.detail?.quantity }}</template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { stockOutApi } from '@/api/stockOut'
import { partApi } from '@/api/part'
import { useUserStore } from '@/stores/user'
import { STOCKOUT_STATUS, PURPOSE_OPTIONS } from '@/utils/enums'
import { formatDateTime } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'

const userStore = useUserStore()

const query = reactive({ current: 1, size: 10, stockOutNumber: '', status: null, department: '', userId: null })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const partOptions = ref([])

function partName(id) {
  return partOptions.value.find((p) => p.id === id)?.name || id
}

// 领用单号留空时前端自动生成：SO + 年月日时分秒 + 3 位随机数
function genStockOutNumber() {
  const d = new Date()
  const p = (n, len = 2) => String(n).padStart(len, '0')
  const ts =
    d.getFullYear() +
    p(d.getMonth() + 1) +
    p(d.getDate()) +
    p(d.getHours()) +
    p(d.getMinutes()) +
    p(d.getSeconds())
  return `SO${ts}${p(Math.floor(Math.random() * 1000), 3)}`
}

async function loadParts() {
  partOptions.value = (await partApi.search('')) || []
}
async function loadData() {
  loading.value = true
  try {
    const res = await stockOutApi.page(query)
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
  Object.assign(query, { current: 1, stockOutNumber: '', status: null, department: '' })
  loadData()
}
onMounted(() => {
  loadParts()
  loadData()
})

// 新建
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()
const form = reactive({})
const defaultForm = () => ({
  stockOutNumber: '',
  department: userStore.userInfo.department || '',
  purpose: '生产',
  remark: '',
  details: [{ partId: null, quantity: 1 }]
})
const rules = {
  purpose: [{ required: true, message: '请选择用途', trigger: 'change' }]
}
function openCreate() {
  Object.assign(form, defaultForm())
  dialogVisible.value = true
  formRef.value?.clearValidate()
}
function addRow() {
  form.details.push({ partId: null, quantity: 1 })
}
function removeRow(idx) {
  form.details.splice(idx, 1)
}
async function submit() {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const ok = form.details.length > 0 && form.details.every((d) => d.partId && d.quantity > 0)
    if (!ok) {
      ElMessage.warning('请完整填写领用明细（零件、数量）')
      return
    }
    saving.value = true
    try {
      const payload = {
        // 后端领用单号为必填，留空则前端自动生成
        stockOutNumber: form.stockOutNumber || genStockOutNumber(),
        userId: userStore.userInfo.id,
        department: form.department,
        purpose: form.purpose,
        status: 1, // 待审核
        remark: form.remark,
        details: form.details.map((d) => ({ partId: d.partId, quantity: d.quantity }))
      }
      await stockOutApi.create(payload)
      ElMessage.success('已提交出库申请，等待管理员审核')
      dialogVisible.value = false
      loadData()
    } finally {
      saving.value = false
    }
  })
}

// 详情
const detailVisible = ref(false)
const detail = reactive({ order: null, details: [] })
async function openDetail(row) {
  const res = await stockOutApi.get(row.id)
  detail.order = res.order || row
  detail.details = res.details || []
  detailVisible.value = true
}

async function cancel(row) {
  await ElMessageBox.confirm('确认取消该领用单？', '提示', { type: 'warning' })
  await stockOutApi.cancel(row.id)
  ElMessage.success('已取消')
  loadData()
}
</script>

<style scoped>
.title {
  font-weight: 600;
}
</style>
