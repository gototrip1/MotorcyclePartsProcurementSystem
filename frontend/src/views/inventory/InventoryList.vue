<template>
  <div class="page-container">
    <!-- 库存概览 -->
    <el-row :gutter="16" class="overview">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ summary.totalItems ?? '-' }}</div>
          <div class="stat-label">库存品类总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value" style="color: #67c23a">{{ summary.normalItems ?? '-' }}</div>
          <div class="stat-label">库存充足</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value" style="color: #e6a23c">{{ summary.warningItems ?? '-' }}</div>
          <div class="stat-label">库存预警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <el-button type="warning" plain :icon="Warning" @click="showWarningOnly = !showWarningOnly">
            {{ showWarningOnly ? '查看全部' : '仅看预警' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="零件名称">
          <el-input v-model="query.partName" placeholder="名称模糊查询" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="仓库位置">
          <el-input v-model="query.warehouseLocation" placeholder="仓库位置" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="title">库存列表</span>
        <el-tag type="info" effect="plain">库存数量只读，仅由审核通过的入/出库自动变更</el-tag>
      </div>

      <el-table v-loading="loading" :data="displayData" border stripe :row-class-name="warnRow">
        <el-table-column label="零件" min-width="160">
          <template #default="{ row }">{{ row.partDetail?.name || '-' }}</template>
        </el-table-column>
        <el-table-column label="编码" width="120">
          <template #default="{ row }">{{ row.partDetail?.partCode || '-' }}</template>
        </el-table-column>
        <el-table-column prop="currentQuantity" label="当前数量" width="100" align="center" />
        <el-table-column prop="safetyStock" label="安全库存" width="100" align="center" />
        <el-table-column label="预警值" width="90" align="center">
          <template #default="{ row }">{{ row.partDetail?.stockWarningValue ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="库存状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="stockInfo(row).tag">{{ stockInfo(row).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warehouseLocation" label="仓库位置" width="120" />
        <el-table-column label="最近入库" width="160">
          <template #default="{ row }">{{ formatDateTime(row.lastInboundTime) }}</template>
        </el-table-column>
        <el-table-column label="最近出库" width="160">
          <template #default="{ row }">{{ formatDateTime(row.lastOutboundTime) }}</template>
        </el-table-column>
        <el-table-column v-if="userStore.isAdmin" label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSafety(row)">设安全库存</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!showWarningOnly" class="pagination-bar">
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

    <!-- 安全库存设置（管理员，7.4）-->
    <el-dialog v-model="safetyVisible" title="设置安全库存" width="380px">
      <el-form label-width="100px">
        <el-form-item label="零件">{{ safetyForm.partName }}</el-form-item>
        <el-form-item label="安全库存">
          <el-input-number v-model="safetyForm.safetyStock" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="safetyVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitSafety">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Warning } from '@element-plus/icons-vue'
import { inventoryApi } from '@/api/inventory'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'

const userStore = useUserStore()

const query = reactive({ current: 1, size: 10, partName: '', warehouseLocation: '' })
const tableData = ref([])
const warningData = ref([])
const total = ref(0)
const loading = ref(false)
const summary = reactive({ totalItems: null, warningItems: null, normalItems: null })
const showWarningOnly = ref(false)

const displayData = computed(() => (showWarningOnly.value ? warningData.value : tableData.value))

// 库存状态判定（7.3）：当前 <= 预警值 红色「严重不足」；<= 安全库存 橙色「不足」；否则「充足」
function stockInfo(row) {
  const qty = row.currentQuantity ?? 0
  const warn = row.partDetail?.stockWarningValue
  const safety = row.safetyStock
  if (warn != null && qty <= warn) return { text: '严重不足', tag: 'danger' }
  if (safety != null && qty <= safety) return { text: '不足', tag: 'warning' }
  return { text: '充足', tag: 'success' }
}
function warnRow({ row }) {
  const t = stockInfo(row).tag
  if (t === 'danger') return 'row-danger'
  if (t === 'warning') return 'row-warning'
  return ''
}

async function loadData() {
  loading.value = true
  try {
    const res = await inventoryApi.page(query)
    tableData.value = res.records || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
async function loadWarning() {
  warningData.value = (await inventoryApi.warning()) || []
}
async function loadSummary() {
  try {
    const res = await inventoryApi.check()
    summary.totalItems = res.totalItems
    summary.warningItems = res.warningItems
    summary.normalItems = res.normalItems
  } catch (e) {
    /* ignore */
  }
}
function onSearch() {
  query.current = 1
  loadData()
}
function onReset() {
  Object.assign(query, { current: 1, partName: '', warehouseLocation: '' })
  loadData()
}

watch(showWarningOnly, (v) => {
  if (v) loadWarning()
})

onMounted(() => {
  loadData()
  loadWarning()
  loadSummary()
})

// 安全库存设置
const safetyVisible = ref(false)
const saving = ref(false)
const safetyForm = reactive({ id: null, partName: '', safetyStock: 0 })
function openSafety(row) {
  Object.assign(safetyForm, {
    id: row.id,
    partName: row.partDetail?.name || '',
    safetyStock: row.safetyStock ?? 0
  })
  safetyVisible.value = true
}
async function submitSafety() {
  saving.value = true
  try {
    await inventoryApi.updateSafetyStock(safetyForm.id, safetyForm.safetyStock)
    ElMessage.success('已更新安全库存')
    safetyVisible.value = false
    loadData()
    loadSummary()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.overview {
  margin-bottom: 16px;
}
.title {
  font-weight: 600;
}
</style>
