<template>
  <div class="page-container">
    <!-- 概览卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ overview.supplierCount ?? '-' }}</div>
          <div class="stat-label">供应商数量</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ overview.partCount ?? '-' }}</div>
          <div class="stat-label">零件数量</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ overview.userCount ?? '-' }}</div>
          <div class="stat-label">用户数量</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value">{{ overview.inventoryCount ?? '-' }}</div>
          <div class="stat-label">库存品类</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value" style="color: #67c23a">{{ monthPurchase.orderCount ?? '-' }}</div>
          <div class="stat-label">本月采购单数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value" style="color: #e6a23c">{{ formatMoney(monthPurchase.totalAmount) }}</div>
          <div class="stat-label">本月采购金额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value" style="color: #909399">{{ monthPurchase.completedCount ?? '-' }}</div>
          <div class="stat-label">本月已完成</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never" class="stat-card">
          <div class="stat-value" style="color: #f56c6c">{{ inventoryStat.warning ?? '-' }}</div>
          <div class="stat-label">库存预警数</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="16">
        <el-card shadow="never">
          <div class="card-header">
            <span class="title">采购月度趋势</span>
            <el-date-picker
              v-model="trendRange"
              type="monthrange"
              value-format="YYYY-MM-DD"
              start-placeholder="开始月"
              end-placeholder="结束月"
              size="small"
              style="width: 240px"
              @change="loadTrend"
            />
          </div>
          <div ref="trendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="card-header"><span class="title">库存状态分布</span></div>
          <div ref="invRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="never">
          <div class="card-header"><span class="title">供应商信用评级分布</span></div>
          <div ref="supplierRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <div class="card-header"><span class="title">零件分类分布</span></div>
          <div ref="partRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { statisticsApi } from '@/api/statistics'
import { formatMoney } from '@/utils/format'

const overview = reactive({ supplierCount: null, partCount: null, userCount: null, inventoryCount: null })
const monthPurchase = reactive({ orderCount: null, totalAmount: null, completedCount: null })
const inventoryStat = reactive({ warning: null, normal: null })

const trendRef = ref()
const invRef = ref()
const supplierRef = ref()
const partRef = ref()
let trendChart, invChart, supplierChart, partChart
const trendRange = ref([])

function initChart(el) {
  return echarts.init(el)
}

async function loadOverview() {
  const res = await statisticsApi.dashboard()
  overview.supplierCount = res.supplierCount
  overview.partCount = res.partCount
  overview.userCount = res.userCount
  overview.inventoryCount = res.inventoryCount
  if (res.monthPurchase) {
    monthPurchase.orderCount = res.monthPurchase.orderCount
    monthPurchase.totalAmount = res.monthPurchase.totalAmount
    monthPurchase.completedCount = res.monthPurchase.completedCount
  }
}

async function loadInventory() {
  const res = await statisticsApi.inventory()
  const dist = res.statusDistribution || {}
  inventoryStat.warning = dist.warning ?? 0
  inventoryStat.normal = dist.normal ?? 0
  invChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: dist.normal ?? 0, name: '充足', itemStyle: { color: '#67c23a' } },
          { value: dist.warning ?? 0, name: '预警', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  })
}

async function loadSupplier() {
  const res = await statisticsApi.suppliers()
  const byRating = res.byCreditRating || {}
  supplierChart.setOption({
    tooltip: {},
    xAxis: { type: 'category', data: Object.keys(byRating) },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: Object.values(byRating), itemStyle: { color: '#409eff' }, barWidth: '50%' }]
  })
}

async function loadPart() {
  const res = await statisticsApi.parts()
  const byCategory = res.byCategory || {}
  partChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: '65%',
        data: Object.entries(byCategory).map(([name, value]) => ({ name, value }))
      }
    ]
  })
}

async function loadTrend() {
  const start = trendRange.value?.[0] || null
  const end = trendRange.value?.[1] || null
  const res = (await statisticsApi.monthlyTrend(start, end)) || []
  const months = res.map((r) => r.month)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '采购金额'], bottom: 0 },
    xAxis: { type: 'category', data: months },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '金额' }
    ],
    series: [
      { name: '订单数', type: 'bar', data: res.map((r) => r.orderCount), itemStyle: { color: '#409eff' } },
      { name: '采购金额', type: 'line', yAxisIndex: 1, data: res.map((r) => r.totalAmount), itemStyle: { color: '#e6a23c' } }
    ]
  })
}

function onResize() {
  trendChart?.resize()
  invChart?.resize()
  supplierChart?.resize()
  partChart?.resize()
}

onMounted(async () => {
  await nextTick()
  trendChart = initChart(trendRef.value)
  invChart = initChart(invRef.value)
  supplierChart = initChart(supplierRef.value)
  partChart = initChart(partRef.value)
  window.addEventListener('resize', onResize)
  await Promise.all([loadOverview(), loadInventory(), loadSupplier(), loadPart(), loadTrend()])
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  invChart?.dispose()
  supplierChart?.dispose()
  partChart?.dispose()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.title {
  font-weight: 600;
}
</style>
