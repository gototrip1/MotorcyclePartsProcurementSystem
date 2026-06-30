<template>
  <div class="page-container">
    <el-card shadow="never" class="search-bar">
      <el-form :inline="true" :model="query">
        <el-form-item label="订单号">
          <el-input v-model="query.orderNumber" placeholder="订单号" clearable @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="(v, k) in ORDER_STATUS" :key="k" :label="v.text" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="onSearch">查询</el-button>
          <el-button :icon="Refresh" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="title">采购单列表</span>
        <el-button v-if="canEdit" type="primary" :icon="Plus" @click="goCreate">新建采购单</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" border stripe>
        <el-table-column prop="orderNumber" label="订单号" width="170" />
        <el-table-column label="总金额" width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <StatusTag :map="ORDER_STATUS" :value="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="付款" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :map="PAID_STATUS" :value="row.paid" />
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.orderTime) }}</template>
        </el-table-column>
        <el-table-column label="预计到货" width="120">
          <template #default="{ row }">{{ formatDate(row.expectedDeliveryDate) }}</template>
        </el-table-column>
        <el-table-column prop="logisticsCompany" label="物流公司" width="120" show-overflow-tooltip />
        <el-table-column label="操作" min-width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row)">详情</el-button>
            <!-- 采购中：录物流 / 提交入库 / 取消（采购员或管理员）-->
            <template v-if="canEdit && row.status === 1">
              <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
              <el-button link type="warning" @click="openLogistics(row)">录物流</el-button>
              <el-button link type="success" @click="submitInbound(row)">提交入库</el-button>
              <el-button link type="danger" @click="cancelOrder(row)">取消</el-button>
            </template>
            <!-- 已入库：标记已付款 -->
            <el-button
              v-if="canEdit && row.status === 3 && row.paid !== 1"
              link
              type="success"
              @click="markPaid(row)"
            >
              标记已付款
            </el-button>
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

    <!-- 录物流弹窗 -->
    <el-dialog v-model="logisticsVisible" title="录入物流信息" width="480px">
      <el-form label-width="100px">
        <el-form-item label="物流公司">
          <el-input v-model="logisticsForm.logisticsCompany" />
        </el-form-item>
        <el-form-item label="运单号">
          <el-input v-model="logisticsForm.trackingNumber" />
        </el-form-item>
        <el-form-item label="发货时间">
          <el-date-picker
            v-model="logisticsForm.shipTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预计到货">
          <el-date-picker
            v-model="logisticsForm.expectedDeliveryDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="logisticsVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitLogistics">保存</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="采购单详情" size="640px">
      <el-descriptions v-if="detail.order" :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.order.orderNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :map="ORDER_STATUS" :value="detail.order.status" />
        </el-descriptions-item>
        <el-descriptions-item label="总金额">{{ formatMoney(detail.order.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="付款状态">
          <StatusTag :map="PAID_STATUS" :value="detail.order.paid" />
        </el-descriptions-item>
        <el-descriptions-item label="物流公司">{{ detail.order.logisticsCompany || '-' }}</el-descriptions-item>
        <el-descriptions-item label="运单号">{{ detail.order.trackingNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发货时间">{{ formatDateTime(detail.order.shipTime) }}</el-descriptions-item>
        <el-descriptions-item label="预计到货">{{ formatDate(detail.order.expectedDeliveryDate) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.order.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">采购明细</el-divider>
      <el-table :data="detail.details" border size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="零件" min-width="160">
          <template #default="{ row }">{{ row.partDetail?.name || partName(row.partId) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" align="center" />
        <el-table-column label="单价" width="110" align="right">
          <template #default="{ row }">{{ formatMoney(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.subtotal) }}</template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { purchaseOrderApi } from '@/api/purchaseOrder'
import { partApi } from '@/api/part'
import { useUserStore } from '@/stores/user'
import { ORDER_STATUS, PAID_STATUS } from '@/utils/enums'
import { formatMoney, formatDate, formatDateTime } from '@/utils/format'
import StatusTag from '@/components/StatusTag.vue'

const router = useRouter()
const userStore = useUserStore()
const canEdit = computed(() => userStore.isAdmin || userStore.isPurchase)

const query = reactive({ current: 1, size: 10, orderNumber: '', status: null, startDate: null, endDate: null })
const dateRange = ref([])
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const partOptions = ref([])

function partName(id) {
  return partOptions.value.find((p) => p.id === id)?.name || id
}
async function loadParts() {
  partOptions.value = (await partApi.search('')) || []
}

async function loadData() {
  loading.value = true
  query.startDate = dateRange.value?.[0] || null
  query.endDate = dateRange.value?.[1] || null
  try {
    const res = await purchaseOrderApi.page(query)
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
  Object.assign(query, { current: 1, orderNumber: '', status: null })
  dateRange.value = []
  loadData()
}
// 列表页被 keep-alive 缓存，使用 onActivated（首次进入与从编辑页返回都会触发）刷新数据
onActivated(() => {
  loadData()
  loadParts()
})

function goCreate() {
  router.push('/purchase/edit')
}
function goEdit(row) {
  router.push(`/purchase/edit/${row.id}`)
}

// 详情
const detailVisible = ref(false)
const detail = reactive({ order: null, details: [] })
async function goDetail(row) {
  const res = await purchaseOrderApi.details(row.id)
  detail.order = res.order
  detail.details = res.details || []
  detailVisible.value = true
}

// 物流
const logisticsVisible = ref(false)
const saving = ref(false)
const logisticsForm = reactive({
  id: null,
  logisticsCompany: '',
  trackingNumber: '',
  shipTime: null,
  expectedDeliveryDate: null
})
function openLogistics(row) {
  Object.assign(logisticsForm, {
    id: row.id,
    logisticsCompany: row.logisticsCompany || '',
    trackingNumber: row.trackingNumber || '',
    shipTime: row.shipTime || null,
    expectedDeliveryDate: row.expectedDeliveryDate || null
  })
  logisticsVisible.value = true
}
async function submitLogistics() {
  saving.value = true
  try {
    const { id, ...params } = logisticsForm
    await purchaseOrderApi.updateLogistics(id, params)
    ElMessage.success('物流信息已保存')
    logisticsVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function submitInbound(row) {
  await ElMessageBox.confirm('确认提交入库审核？提交后等待管理员审核入库。', '提示', { type: 'warning' })
  await purchaseOrderApi.submitInbound(row.id)
  ElMessage.success('已提交入库审核')
  loadData()
}
async function markPaid(row) {
  await ElMessageBox.confirm('确认将该订单标记为已付款？', '提示', { type: 'warning' })
  await purchaseOrderApi.markPaid(row.id)
  ElMessage.success('已标记付款')
  loadData()
}
async function cancelOrder(row) {
  await ElMessageBox.confirm('确认取消该采购单？', '提示', { type: 'warning' })
  await purchaseOrderApi.updateStatus(row.id, 4)
  ElMessage.success('已取消')
  loadData()
}
</script>

<style scoped>
.title {
  font-weight: 600;
}
</style>
