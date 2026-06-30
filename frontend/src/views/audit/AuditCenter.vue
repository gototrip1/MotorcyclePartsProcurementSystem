<template>
  <div class="page-container">
    <el-card shadow="never">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 待入库审核：采购单 status=2 -->
        <el-tab-pane label="待入库审核" name="inbound">
          <el-table v-loading="inboundLoading" :data="inboundList" border stripe>
            <el-table-column prop="orderNumber" label="订单号" width="180" />
            <el-table-column label="总金额" width="130" align="right">
              <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column label="预计到货" width="130">
              <template #default="{ row }">{{ formatDate(row.expectedDeliveryDate) }}</template>
            </el-table-column>
            <el-table-column prop="logisticsCompany" label="物流公司" width="140" />
            <el-table-column prop="trackingNumber" label="运单号" width="150" />
            <el-table-column label="操作" min-width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewOrder(row)">明细</el-button>
                <el-button link type="success" @click="approveInbound(row)">通过入库</el-button>
                <el-button link type="danger" @click="rejectInbound(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="inboundQuery.current"
              :page-size="inboundQuery.size"
              :total="inboundTotal"
              layout="total, prev, pager, next"
              @current-change="loadInbound"
            />
          </div>
        </el-tab-pane>

        <!-- 待出库审核：领用单 status=1 -->
        <el-tab-pane label="待出库审核" name="outbound">
          <el-table v-loading="outboundLoading" :data="outboundList" border stripe>
            <el-table-column prop="stockOutNumber" label="领用单号" width="190" />
            <el-table-column prop="department" label="部门" width="130" />
            <el-table-column prop="purpose" label="用途" width="110" />
            <el-table-column label="领用时间" width="170">
              <template #default="{ row }">{{ formatDateTime(row.stockOutTime) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作" min-width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewStockOut(row)">明细</el-button>
                <el-button link type="success" @click="approveOutbound(row)">通过出库</el-button>
                <el-button link type="danger" @click="rejectOutbound(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-bar">
            <el-pagination
              v-model:current-page="outboundQuery.current"
              :page-size="outboundQuery.size"
              :total="outboundTotal"
              layout="total, prev, pager, next"
              @current-change="loadOutbound"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 明细抽屉 -->
    <el-drawer v-model="detailVisible" :title="detailTitle" size="600px">
      <el-table :data="detailRows" border size="small">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="零件" min-width="180">
          <template #default="{ row }">{{ row.partDetail?.name || partName(row.partId) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" align="center" />
        <el-table-column v-if="activeTab === 'inbound'" label="单价" width="120" align="right">
          <template #default="{ row }">{{ formatMoney(row.unitPrice) }}</template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { purchaseOrderApi } from '@/api/purchaseOrder'
import { stockOutApi } from '@/api/stockOut'
import { auditApi } from '@/api/audit'
import { partApi } from '@/api/part'
import { formatMoney, formatDate, formatDateTime } from '@/utils/format'

const activeTab = ref('inbound')
const partOptions = ref([])
function partName(id) {
  return partOptions.value.find((p) => p.id === id)?.name || id
}

// ---------- 待入库 ----------
const inboundList = ref([])
const inboundTotal = ref(0)
const inboundLoading = ref(false)
const inboundQuery = reactive({ current: 1, size: 10, status: 2 })
async function loadInbound() {
  inboundLoading.value = true
  try {
    const res = await purchaseOrderApi.page(inboundQuery)
    inboundList.value = res.records || []
    inboundTotal.value = res.total || 0
  } finally {
    inboundLoading.value = false
  }
}
async function approveInbound(row) {
  await ElMessageBox.confirm(`确认通过入库？通过后将增加库存。\n订单号：${row.orderNumber}`, '入库审核', { type: 'warning' })
  await auditApi.approveInbound(row.id)
  ElMessage.success('入库审核通过，库存已增加')
  loadInbound()
}
async function rejectInbound(row) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回入库', {
    inputValidator: (v) => (v ? true : '驳回原因不能为空')
  })
  await auditApi.rejectInbound(row.id, value)
  ElMessage.success('已驳回')
  loadInbound()
}

// ---------- 待出库 ----------
const outboundList = ref([])
const outboundTotal = ref(0)
const outboundLoading = ref(false)
const outboundQuery = reactive({ current: 1, size: 10, status: 1 })
async function loadOutbound() {
  outboundLoading.value = true
  try {
    const res = await stockOutApi.page(outboundQuery)
    outboundList.value = res.records || []
    outboundTotal.value = res.total || 0
  } finally {
    outboundLoading.value = false
  }
}
async function approveOutbound(row) {
  await ElMessageBox.confirm(`确认通过出库？通过后将扣减库存（库存不足将失败）。\n单号：${row.stockOutNumber}`, '出库审核', { type: 'warning' })
  try {
    await auditApi.approveOutbound(row.id)
    ElMessage.success('出库审核通过，库存已扣减')
    loadOutbound()
  } catch (e) {
    // 库存不足等业务错误由拦截器统一提示
  }
}
async function rejectOutbound(row) {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回出库', {
    inputValidator: (v) => (v ? true : '驳回原因不能为空')
  })
  await auditApi.rejectOutbound(row.id, value)
  ElMessage.success('已驳回')
  loadOutbound()
}

// ---------- 明细抽屉 ----------
const detailVisible = ref(false)
const detailTitle = ref('')
const detailRows = ref([])
async function viewOrder(row) {
  const res = await purchaseOrderApi.details(row.id)
  detailRows.value = res.details || []
  detailTitle.value = `采购单明细 - ${row.orderNumber}`
  detailVisible.value = true
}
async function viewStockOut(row) {
  const res = await stockOutApi.get(row.id)
  // 领用单 get 返回的明细为 { detail, part } 视图对象，归一化为扁平结构以复用同一表格
  detailRows.value = (res.details || []).map((v) => ({
    partId: v.detail?.partId,
    partDetail: v.part,
    quantity: v.detail?.quantity
  }))
  detailTitle.value = `领用单明细 - ${row.stockOutNumber}`
  detailVisible.value = true
}

function onTabChange(name) {
  name === 'inbound' ? loadInbound() : loadOutbound()
}

onMounted(async () => {
  partOptions.value = (await partApi.search('')) || []
  loadInbound()
  loadOutbound()
})
</script>

<style scoped></style>
