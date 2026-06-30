<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="title">{{ isEdit ? '编辑采购单' : '新建采购单' }}</span>
        <el-button :icon="Back" @click="goBack">返回列表</el-button>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="loading">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="订单号" prop="orderNumber">
              <el-input v-model="form.orderNumber" placeholder="留空则自动生成（PO+时间戳）" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplierId">
              <el-select v-model="supplierId" filterable placeholder="选择供应商以筛选零件" style="width: 100%" @change="onSupplierChange">
                <el-option v-for="s in supplierOptions" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="预计到货" prop="expectedDeliveryDate">
              <el-date-picker
                v-model="form.expectedDeliveryDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">采购明细</el-divider>

        <el-table :data="form.orderDetail" border>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="零件" min-width="220">
            <template #default="{ row }">
              <el-select
                v-model="row.partId"
                filterable
                placeholder="选择零件"
                style="width: 100%"
                @change="() => onPartChange(row)"
              >
                <el-option v-for="p in partOptions" :key="p.id" :label="`${p.name}（${p.partCode}）`" :value="p.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.quantity" :min="1" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="单价" width="150">
            <template #default="{ row }">
              <el-input-number v-model="row.unitPrice" :min="0" :precision="2" disabled style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column label="小计" width="130" align="right">
            <template #default="{ row }">{{ formatMoney((row.quantity || 0) * (row.unitPrice || 0)) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" @click="removeRow($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="detail-footer">
          <el-button :icon="Plus" @click="addRow">添加明细</el-button>
          <span class="total">合计金额：<b>{{ formatMoney(totalAmount) }}</b></span>
        </div>
      </el-form>

      <div class="actions">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Plus } from '@element-plus/icons-vue'
import { purchaseOrderApi } from '@/api/purchaseOrder'
import { supplierApi } from '@/api/supplier'
import { partApi } from '@/api/part'
import { formatMoney } from '@/utils/format'

// 订单号留空时前端自动生成：PO + 年月日时分秒 + 3 位随机数
function genOrderNumber() {
  const d = new Date()
  const p = (n, len = 2) => String(n).padStart(len, '0')
  const ts =
    d.getFullYear() +
    p(d.getMonth() + 1) +
    p(d.getDate()) +
    p(d.getHours()) +
    p(d.getMinutes()) +
    p(d.getSeconds())
  const rand = p(Math.floor(Math.random() * 1000), 3)
  return `PO${ts}${rand}`
}

const route = useRoute()
const router = useRouter()
const orderId = computed(() => route.params.id)
const isEdit = computed(() => !!orderId.value)

const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const supplierId = ref(null)
const supplierOptions = ref([])
const partOptions = ref([])

const form = reactive({
  id: null,
  orderNumber: '',
  expectedDeliveryDate: null,
  remark: '',
  orderDetail: [{ partId: null, quantity: 1, unitPrice: 0 }]
})

const rules = {
  // 订单号留空交后端生成，故不强制
}

const totalAmount = computed(() =>
  form.orderDetail.reduce((s, d) => s + (d.quantity || 0) * (d.unitPrice || 0), 0)
)

async function loadSuppliers() {
  supplierOptions.value = (await supplierApi.search('')) || []
}
async function loadParts(supId) {
  if (supId) {
    partOptions.value = (await partApi.listBySupplier(supId)) || []
  } else {
    partOptions.value = (await partApi.search('')) || []
  }
}
function onSupplierChange(val) {
  loadParts(val)
}
function onPartChange(row) {
  const p = partOptions.value.find((x) => x.id === row.partId)
  row.unitPrice = p ? Number(p.purchasePrice) : 0
}

function addRow() {
  form.orderDetail.push({ partId: null, quantity: 1, unitPrice: 0 })
}
function removeRow(idx) {
  form.orderDetail.splice(idx, 1)
}

async function loadDetail() {
  loading.value = true
  try {
    const res = await purchaseOrderApi.get(orderId.value)
    Object.assign(form, {
      id: res.id,
      orderNumber: res.orderNumber,
      expectedDeliveryDate: res.expectedDeliveryDate,
      remark: res.remark
    })
    const details = res.orderDetail || []
    form.orderDetail = details.length
      ? details.map((d) => ({
          id: d.id,
          partId: d.partId,
          quantity: d.quantity,
          unitPrice: Number(d.unitPrice)
        }))
      : [{ partId: null, quantity: 1, unitPrice: 0 }]
    // 推断供应商（取第一条明细零件的供应商）
    const firstPart = details[0]?.partDetail
    if (firstPart?.supplierId) {
      supplierId.value = firstPart.supplierId
      await loadParts(firstPart.supplierId)
    }
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadSuppliers()
  if (isEdit.value) {
    await loadDetail()
  } else {
    await loadParts(null)
  }
})

async function submit() {
  // 校验明细
  const valid = form.orderDetail.length > 0 && form.orderDetail.every((d) => d.partId && d.quantity > 0)
  if (!valid) {
    ElMessage.warning('请完整填写采购明细（零件、数量）')
    return
  }
  saving.value = true
  try {
    const payload = {
      // 后端订单号为必填，留空则前端自动生成
      orderNumber: form.orderNumber || genOrderNumber(),
      expectedDeliveryDate: form.expectedDeliveryDate,
      remark: form.remark,
      totalAmount: totalAmount.value,
      status: 1, // 新建即「采购中」
      orderDetail: form.orderDetail.map((d) => ({
        id: d.id,
        partId: d.partId,
        quantity: d.quantity,
        unitPrice: d.unitPrice
      }))
    }
    if (isEdit.value) {
      await purchaseOrderApi.update(form.id, payload)
    } else {
      await purchaseOrderApi.create(payload)
    }
    ElMessage.success('保存成功')
    goBack()
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/purchase')
}
</script>

<style scoped>
.title {
  font-weight: 600;
}
.detail-footer {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.detail-footer .total {
  font-size: 15px;
}
.detail-footer .total b {
  color: #f56c6c;
  font-size: 18px;
}
.actions {
  margin-top: 24px;
  text-align: center;
}
</style>
