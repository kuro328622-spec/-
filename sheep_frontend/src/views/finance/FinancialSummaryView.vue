<template>
  <div class="app-container" v-loading="loading" element-loading-text="正在聚合财务数据...">
    <el-card class="filter-card" style="margin-bottom: 20px;">
      <el-form :inline="true">
        <el-form-item label="统计日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleFilter"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="warning" :icon="Download" @click="exportDialogVisible = true">导出报表</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon>
              <span style="margin-left: 8px">收支趋势分析</span>
            </div>
          </template>
          <div id="lineChart" class="chart-box"></div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><PieChart /></el-icon>
              <span style="margin-left: 8px">支出分类构成占比</span>
            </div>
          </template>
          <div id="pieChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header><span class="card-header">当前筛选时段财务概览</span></template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="总收入">
              <span class="text-success">￥ {{ summary.totalIncome.toLocaleString(undefined, {minimumFractionDigits: 2}) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="总支出">
              <span class="text-danger">￥ {{ summary.totalExpense.toLocaleString(undefined, {minimumFractionDigits: 2}) }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="净利润">
              <span :class="summary.profit >= 0 ? 'text-success' : 'text-danger'">
                ￥ {{ summary.profit.toLocaleString(undefined, {minimumFractionDigits: 2}) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="记录总数">{{ summary.count }} 笔</el-descriptions-item>
            <el-descriptions-item label="最大支出项">
              <el-tag type="danger" effect="plain">{{ summary.topExpenseCategory || '无' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="分析状态">
              <el-tag type="success" size="small">实时更新</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="exportDialogVisible" title="导出财务报表" width="500px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="时间跨度">
          <el-radio-group v-model="exportMode">
            <el-radio label="all">全部记录</el-radio>
            <el-radio label="year">按年</el-radio>
            <el-radio label="quarter">按季</el-radio>
            <el-radio label="month">按月</el-radio>
            <el-radio label="custom">自定义日期</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择年份" v-if="['year', 'quarter', 'month'].includes(exportMode)">
          <el-date-picker v-model="exportYear" type="year" value-format="YYYY" placeholder="选择年份" style="width: 100%"/>
        </el-form-item>

        <el-form-item label="选择季度" v-if="exportMode === 'quarter'">
          <el-select v-model="exportQuarter" placeholder="请选择季度" style="width: 100%">
            <el-option label="第一季度 (1-3月)" value="1" />
            <el-option label="第二季度 (4-6月)" value="2" />
            <el-option label="第三季度 (7-9月)" value="3" />
            <el-option label="第四季度 (10-12月)" value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="选择月份" v-if="exportMode === 'month'">
          <el-select v-model="exportMonth" placeholder="请选择月份" style="width: 100%">
            <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m.toString().padStart(2, '0')" />
          </el-select>
        </el-form-item>

        <el-form-item label="日期范围" v-if="exportMode === 'custom'">
          <el-date-picker
            v-model="exportCustomRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" :icon="Download" @click="handleExport">开始导出 Excel</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 1. 删除了未使用的 nextTick 导入
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, TrendCharts, PieChart, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { getFinancialList } from "@/api/financialApi"

// --- 响应式数据 ---
const rawData = ref([])
const dateRange = ref([])
const loading = ref(false)
const summary = reactive({
  totalIncome: 0,
  totalExpense: 0,
  profit: 0,
  count: 0,
  topExpenseCategory: ''
})

// 导出变量
const exportDialogVisible = ref(false)
const exportMode = ref('all')
const exportYear = ref(new Date().getFullYear().toString())
const exportQuarter = ref('1')
const exportMonth = ref('01')
const exportCustomRange = ref([])

// 图表实例
let lineChartInstance = null
let pieChartInstance = null

// --- 方法 ---

const fetchData = async () => {
  loading.value = true // 这里使用了 loading，解决了报错
  try {
    const res = await getFinancialList()
    rawData.value = res.data || res
    handleFilter()
  } catch  {
    ElMessage.error("数据加载失败")
  } finally {
    loading.value = false
  }
}

const handleFilter = () => {
  let filtered = rawData.value
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    filtered = rawData.value.filter(i => i.recordDate >= start && i.recordDate <= end)
  }

  summary.totalIncome = filtered.filter(i => i.recordType === '收入').reduce((s, n) => s + Number(n.amount), 0)
  summary.totalExpense = filtered.filter(i => i.recordType === '支出').reduce((s, n) => s + Number(n.amount), 0)
  summary.profit = summary.totalIncome - summary.totalExpense
  summary.count = filtered.length

  const dailyStats = {}
  filtered.forEach(item => {
    if (!dailyStats[item.recordDate]) dailyStats[item.recordDate] = { in: 0, out: 0 }
    if (item.recordType === '收入') dailyStats[item.recordDate].in += Number(item.amount)
    else dailyStats[item.recordDate].out += Number(item.amount)
  })
  const sortedDates = Object.keys(dailyStats).sort()

  const categoryStats = {}
  filtered.filter(i => i.recordType === '支出').forEach(item => {
    categoryStats[item.category] = (categoryStats[item.category] || 0) + Number(item.amount)
  })
  const pieData = Object.keys(categoryStats).map(name => ({ name, value: categoryStats[name] }))
  summary.topExpenseCategory = pieData.length > 0 ? pieData.sort((a,b) => b.value - a.value)[0].name : '无'

  renderCharts(sortedDates, dailyStats, pieData)
}

// 💡 修复了 Switch Case 中的词法声明报错
const handleExport = () => {
  let exportData = []
  let fileName = `财务报表`

  switch (exportMode.value) {
    case 'all':
      exportData = rawData.value
      fileName += '_全部'
      break
    case 'year':
      exportData = rawData.value.filter(i => i.recordDate.startsWith(exportYear.value))
      fileName += `_${exportYear.value}年`
      break
    case 'quarter': {
      // 增加大括号解决 Unexpected lexical declaration 报错
      const qMap = { '1': ['01','02','03'], '2': ['04','05','06'], '3': ['07','08','09'], '4': ['10','11','12'] }
      exportData = rawData.value.filter(i => {
        const m = i.recordDate.split('-')[1]
        return i.recordDate.startsWith(exportYear.value) && qMap[exportQuarter.value].includes(m)
      })
      fileName += `_${exportYear.value}第${exportQuarter.value}季`
      break
    }
    case 'month': {
      const prefix = `${exportYear.value}-${exportMonth.value}`
      exportData = rawData.value.filter(i => i.recordDate.startsWith(prefix))
      fileName += `_${prefix}`
      break
    }
    case 'custom': {
      if (!exportCustomRange.value || exportCustomRange.value.length < 2) {
        return ElMessage.warning("请选择起止日期")
      }
      const [start, end] = exportCustomRange.value
      exportData = rawData.value.filter(i => i.recordDate >= start && i.recordDate <= end)
      fileName += `_自定义_${start}_至_${end}`
      break
    }
  }

  if (exportData.length === 0) return ElMessage.warning("时段内无数据")

  const wsData = exportData.map(item => ({
    '日期': item.recordDate,
    '类型': item.recordType,
    '科目': item.category,
    '金额': Number(item.amount).toFixed(2),
    '来源': item.isAuto === 1 ? '系统同步' : '手动录入'
  }))

  const ws = XLSX.utils.json_to_sheet(wsData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, "Sheet1")
  XLSX.writeFile(wb, `${fileName}.xlsx`)
  exportDialogVisible.value = false
}

// 图表渲染
const renderCharts = (dates, stats, pieData) => {
  const lineDom = document.getElementById('lineChart')
  const pieDom = document.getElementById('pieChart')
  if (!lineDom || !pieDom) return

  if (!lineChartInstance) lineChartInstance = echarts.init(lineDom)
  if (!pieChartInstance) pieChartInstance = echarts.init(pieDom)

  lineChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['收入', '支出'], bottom: 0 },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      { name: '收入', type: 'line', smooth: true, data: dates.map(d => stats[d].in), itemStyle: {color: '#67C23A'} },
      { name: '支出', type: 'line', smooth: true, data: dates.map(d => stats[d].out), itemStyle: {color: '#F56C6C'} }
    ]
  })

  pieChartInstance.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie', radius: ['40%', '70%'],
      data: pieData,
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
}

const resetFilter = () => {
  dateRange.value = []
  handleFilter()
}

const handleResize = () => {
  lineChartInstance?.resize()
  pieChartInstance?.resize()
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  lineChartInstance?.dispose()
  pieChartInstance?.dispose()
})
</script>

<style scoped>
.app-container { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.chart-box { height: 400px; width: 100%; }
.card-header { font-weight: bold; display: flex; align-items: center; }
.text-success { color: #67C23A; font-weight: bold; }
.text-danger { color: #F56C6C; font-weight: bold; }
.filter-card :deep(.el-card__body) { padding-bottom: 0; }
</style>
