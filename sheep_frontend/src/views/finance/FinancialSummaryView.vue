<template>
  <div class="app-container" v-loading="loading" element-loading-text="正在聚合智慧财务数据...">
    <!-- 1. 顶部驾驶舱：核心指标卡片 -->
    <el-row :gutter="20" class="stat-dashboard">
      <el-col :span="6">
        <el-card shadow="always" class="glass-card income">
          <div class="card-body">
            <div class="label">总收入汇总</div>
            <div class="value">￥{{ summary.totalIncome.toLocaleString() }}</div>
            <div class="trend">笔数: {{ summary.incomeCount }} 笔</div>
          </div>
          <el-icon class="bg-icon"><TrendCharts /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="always" class="glass-card expense">
          <div class="card-body">
            <div class="label">总支出汇总</div>
            <div class="value">￥{{ summary.totalExpense.toLocaleString() }}</div>
            <div class="trend">最大项: {{ summary.topExpenseCategory || '无' }}</div>
          </div>
          <el-icon class="bg-icon"><PieChart /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="always" class="glass-card profit">
          <div class="card-body">
            <div class="label">累计净利润</div>
            <div class="value" :class="{ 'neg': summary.profit < 0 }">￥{{ summary.profit.toLocaleString() }}</div>
            <div class="trend">利润率: {{ summary.profitMargin }}%</div>
          </div>
          <el-icon class="bg-icon"><Money /></el-icon>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="always" class="glass-card efficiency">
          <div class="card-body">
            <div class="label">日均运营成本</div>
            <div class="value">￥{{ summary.dailyExpense.toLocaleString(undefined, {maximumFractionDigits:0}) }}</div>
            <div class="trend">周期: {{ summary.days }} 天</div>
          </div>
          <el-icon class="bg-icon"><Timer /></el-icon>
        </el-card>
      </el-col>
    </el-row>

    <!-- 2. 操作中心 -->
    <el-card class="action-card">
      <div class="action-flex">
        <div class="filter-area">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleFilter"
            style="width: 280px"
          />
          <el-button type="primary" :icon="Search" @click="fetchData" style="margin-left: 10px">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
        <div class="btn-group">
          <el-button type="primary" color="#626aef" :icon="MagicStick" @click="showAIDrawer = true">AI 智慧诊断助手</el-button>
          <el-button type="success" :icon="Document" @click="handleOpenReportPreview">生成专业财务报表</el-button>
          <el-button type="warning" :icon="Download" @click="exportDialogVisible = true">导出明细 (Excel)</el-button>
        </div>
      </div>
    </el-card>

    <!-- 3. 图表分析矩阵 (V7 专业版：4 个图表) -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><div class="c-header"><el-icon><TrendCharts /></el-icon><span>收支趋势动态图</span></div></template>
          <div id="lineChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><div class="c-header"><el-icon><PieChart /></el-icon><span>支出分类构成占比</span></div></template>
          <div id="pieChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><div class="c-header"><el-icon><Histogram /></el-icon><span>支出科目排行分析 (真实排行)</span></div></template>
          <div id="rankChart" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header><div class="c-header"><el-icon><DataLine /></el-icon><span>累计利润增长曲线 (经济效益分析)</span></div></template>
          <div id="profitCurveChart" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ✨ AI 智慧诊断抽屉 -->
    <el-drawer v-model="showAIDrawer" title="🤖 AI 养殖经营智慧助手" size="40%" @open="handleAIDiagnose">
      <div v-loading="aiStore.reportLoading" class="ai-chat-container">
        <div v-if="aiStore.currentReport" class="markdown-body" v-html="renderedAIReport"></div>
        <el-empty v-else description="正在深度扫描数据并提供智慧建议..." />
      </div>
    </el-drawer>

    <!-- 📊 专业报表预览弹窗 -->
    <el-dialog v-model="reportPreviewVisible" title="专业财务分析报告预览" width="800px" top="5vh">
      <div id="finalReport" class="report-preview-body">
        <div class="report-cover">
          <h1>规模化羊场财务分析报告</h1>
          <p>生成时间: {{ new Date().toLocaleString() }}</p>
          <p>统计周期: {{ dateRange && dateRange.length === 2 ? dateRange.join(' 至 ') : '全量历史数据' }}</p>
        </div>

        <div class="report-section">
          <h3>一、核心财务指标</h3>
          <table class="report-table">
            <tr><td>总收入</td><td>￥{{ summary.totalIncome }}</td><td>总支出</td><td>￥{{ summary.totalExpense }}</td></tr>
            <tr><td>净利润</td><td>￥{{ summary.profit }}</td><td>利润率</td><td>{{ summary.profitMargin }}%</td></tr>
            <tr><td>日均成本</td><td>￥{{ summary.dailyExpense.toFixed(2) }}</td><td>最大支出项</td><td>{{ summary.topExpenseCategory }}</td></tr>
          </table>
        </div>
        <div class="report-section">
          <h3>二、可视化图表分析</h3>
          <div class="report-charts-grid">
              <div class="report-chart-item">
                <p>收支趋势分析图</p>
                <img :src="chartImages.line" v-if="chartImages.line" />
              </div>
              <div class="report-chart-item">
                <p>支出分类构成图</p>
                <img :src="chartImages.pie" v-if="chartImages.pie" />
              </div>

              <div class="report-chart-item">
                <p>支出科目排行图</p>
                <img :src="chartImages.rank" v-if="chartImages.rank" />
              </div>
              <div class="report-chart-item">
                <p>累计利润增长曲线</p>
                <img :src="chartImages.profitCurve" v-if="chartImages.profitCurve" />
              </div>
          </div>
        </div>
        <div class="report-section">
          <h3>三、AI 智慧诊断结论</h3>
          <div class="markdown-body" v-html="renderedAIReport"></div>
        </div>
      </div>
      <template #footer>
        <el-button type="success" :icon="Printer" @click="handlePrintReport">打印 / 导出 PDF</el-button>
        <el-button @click="reportPreviewVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 导出 Excel 对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出明细数据 (Excel)" width="400px">
       <el-form label-width="80px">
          <el-form-item label="导出模式">
             <el-select v-model="exportMode" style="width: 100%">
                <el-option label="全部记录" value="all" />
                <el-option label="自定义日期" value="custom" />
             </el-select>
          </el-form-item>
          <el-form-item label="日期范围" v-if="exportMode === 'custom'">
             <el-date-picker v-model="exportCustomRange" type="daterange" style="width: 100%" value-format="YYYY-MM-DD" />
          </el-form-item>
       </el-form>
       <template #footer>
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleExportExcel">开始导出</el-button>
       </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, TrendCharts, PieChart, Download, MagicStick, Printer, Document, Money, Timer, Histogram, DataLine } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { getFinancialList } from "@/api/financialApi"
import { useAIStore } from '@/stores/aiStore'
import { marked } from 'marked'

// --- 响应式状态 ---
const aiStore = useAIStore()
const rawData = ref([])
const dateRange = ref([])
const loading = ref(false)
const showAIDrawer = ref(false)
const reportPreviewVisible = ref(false)
const exportDialogVisible = ref(false)
const exportMode = ref('all')
const exportCustomRange = ref([])

const summary = reactive({
  totalIncome: 0, totalExpense: 0, profit: 0, count: 0, incomeCount: 0,
  topExpenseCategory: '', profitMargin: 0, dailyExpense: 0, days: 0
})

const chartImages = reactive({
  line: '',
  pie: '',
  rank: '',
  profitCurve: ''
})

// 图表实例
let lineChart = null; let pieChart = null; let rankChart = null; let profitCurveChart = null;

// --- AI 诊断 ---
const renderedAIReport = computed(() => aiStore.currentReport ? marked(aiStore.currentReport) : '')

const handleAIDiagnose = async () => {
  if (aiStore.currentReport) return
  const context = `财务汇总：收入￥${summary.totalIncome}, 支出￥${summary.totalExpense}, 利润￥${summary.profit}, 利润率${summary.profitMargin}%, 最大支出项${summary.topExpenseCategory}。`
  aiStore.setPageContext('financeLogs', context)
  await aiStore.generateAIReport("养殖场经营深度智慧报告", aiStore.allContext)
}

// --- 报表预览与导出 ---
const handleOpenReportPreview = () => {
  // 转换 4 个图表为 Base64 图片
  if (lineChart) chartImages.line = lineChart.getDataURL({ pixelRatio: 2, backgroundColor: '#fff' })
  if (pieChart) chartImages.pie = pieChart.getDataURL({ pixelRatio: 2, backgroundColor: '#fff' })
  if (rankChart) chartImages.rank = rankChart.getDataURL({ pixelRatio: 2, backgroundColor: '#fff' })
  if (profitCurveChart) chartImages.profitCurve = profitCurveChart.getDataURL({ pixelRatio: 2, backgroundColor: '#fff' })

  if (!aiStore.currentReport) {
    ElMessage.info("正在同步 AI 诊断结论...请耐心等待")
    handleAIDiagnose()
  }
  reportPreviewVisible.value = true
}

const handlePrintReport = () => {
  const content = document.getElementById('finalReport').innerHTML
  const printWin = window.open('', '_blank')

  // 1. 写入内容
  printWin.document.write(`
    <html>
      <head>
        <title>专业财务分析报告</title>
        <style>
          body { font-family: sans-serif; padding: 40px; color: #333; line-height: 1.6; }
          .report-cover { text-align: center; border-bottom: 3px solid #626aef; margin-bottom: 40px; padding-bottom: 20px; }
          .report-section { margin-bottom: 30px; }
          h3 { color: #626aef; border-left: 5px solid #626aef; padding-left: 15px; margin-bottom: 15px; }
          .report-table { width: 100%; border-collapse: collapse; margin-bottom: 20px;}
          .report-table td { border: 1px solid #eee; padding: 12px; background: #fafafa; }
          .report-charts-grid { display: flex; gap: 20px; flex-wrap: wrap; justify-content: space-between; }
          .report-chart-item { width: 45%; text-align: center; border: 1px solid #eee; padding: 10px; border-radius: 8px; page-break-inside: avoid; }
          .report-chart-item img { width: 100%; height: auto; display: block; margin: 10px auto; }
          /* 核心：强制打印背景和图形 */
          * { -webkit-print-color-adjust: exact !important; print-color-adjust: exact !important; }
        </style>
      </head>
      <body>${content}</body>
    </html>
  `)

  printWin.document.close()

  // 2. 核心修复：监听图片加载完成
  const imgs = printWin.document.getElementsByTagName('img')
  const promises = Array.from(imgs).map(img => {
    if (img.complete) return Promise.resolve()
    return new Promise(resolve => {
      img.onload = resolve
      img.onerror = resolve // 报错也继续，防止卡死
    })
  })

  // 3. 所有图片加载完后再调用打印
  Promise.all(promises).then(() => {
    // 额外给 500ms 渲染缓冲
    setTimeout(() => {
      printWin.print()
      printWin.onafterprint = () => printWin.close()
    }, 500)
  })
}

// --- 业务数据逻辑 ---
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFinancialList()
    rawData.value = res.data || res
    handleFilter()
  } catch { ElMessage.error("数据聚合失败") }
  finally { loading.value = false }
}

const handleFilter = () => {
  let filtered = rawData.value
  if (dateRange.value && dateRange.value.length === 2) {
    const [start, end] = dateRange.value
    filtered = rawData.value.filter(i => i.recordDate >= start && i.recordDate <= end)
    summary.days = Math.max(1, Math.ceil((new Date(end) - new Date(start)) / 86400000) + 1)
  } else { summary.days = 30 }

  const inc = filtered.filter(i => i.recordType === '收入')
  const exp = filtered.filter(i => i.recordType === '支出')

  summary.totalIncome = inc.reduce((s, n) => s + Number(n.amount), 0)
  summary.totalExpense = exp.reduce((s, n) => s + Number(n.amount), 0)
  summary.profit = summary.totalIncome - summary.totalExpense
  summary.incomeCount = inc.length
  summary.count = filtered.length
  summary.profitMargin = summary.totalExpense > 0 ? ((summary.profit / summary.totalExpense) * 100).toFixed(1) : 0
  summary.dailyExpense = summary.totalExpense / summary.days

  const categoryMap = {}; exp.forEach(i => categoryMap[i.category] = (categoryMap[i.category] || 0) + Number(i.amount))
  const pieData = Object.keys(categoryMap).map(name => ({ name, value: categoryMap[name] }))
  summary.topExpenseCategory = pieData.sort((a,b) => b.value - a.value)[0]?.name || '无'

  renderCharts(filtered, pieData, categoryMap)
}

const renderCharts = (data, pieData, categoryMap) => {
  nextTick(() => {
    const lineDom = document.getElementById('lineChart'); const pieDom = document.getElementById('pieChart');
    const rankDom = document.getElementById('rankChart'); const curveDom = document.getElementById('profitCurveChart');
    if (!lineDom) return;

    if (!lineChart) lineChart = echarts.init(lineDom);
    if (!pieChart) pieChart = echarts.init(pieDom);
    if (!rankChart) rankChart = echarts.init(rankDom);
    if (!profitCurveChart) profitCurveChart = echarts.init(curveDom);

    // 1. 趋势图
    const dates = [...new Set(data.map(i => i.recordDate))].sort()
    lineChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [
        { name: '收入', type: 'line', smooth: true, data: dates.map(d => data.filter(i => i.recordDate === d && i.recordType === '收入').reduce((s,n) => s+Number(n.amount), 0)), itemStyle: {color: '#67C23A'} },
        { name: '支出', type: 'line', smooth: true, data: dates.map(d => data.filter(i => i.recordDate === d && i.recordType === '支出').reduce((s,n) => s+Number(n.amount), 0)), itemStyle: {color: '#F56C6C'} }
      ]
    })

    // 2. 占比图
    pieChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: pieData }]
    })

    // 3. ✨ 支出科目排行榜 (取代雷达图)
    const sortedRank = Object.keys(categoryMap).sort((a,b) => categoryMap[a] - categoryMap[b])
    rankChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: sortedRank },
      series: [{
        name: '支出金额',
        type: 'bar',
        data: sortedRank.map(cat => categoryMap[cat]),
        itemStyle: { color: '#626aef', borderRadius: [0, 5, 5, 0] },
        label: { show: true, position: 'right', formatter: '￥{c}' }
      }]
    })

    // 4. ✨ 累计利润增长曲线 (经济效益深度分析)
    let cumulative = 0
    const profitData = dates.map(d => {
      const dayIncome = data.filter(i => i.recordDate === d && i.recordType === '收入').reduce((s,n) => s+Number(n.amount), 0)
      const dayExpense = data.filter(i => i.recordDate === d && i.recordType === '支出').reduce((s,n) => s+Number(n.amount), 0)
      cumulative += (dayIncome - dayExpense)
      return cumulative
    })
    profitCurveChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: dates },
      yAxis: { type: 'value' },
      series: [{
        name: '累计净利润',
        type: 'line',
        data: profitData,
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{offset: 0, color: 'rgba(64, 158, 255, 0.3)'}, {offset: 1, color: 'rgba(64, 158, 255, 0)'}]) },
        itemStyle: { color: '#409EFF' },
        smooth: true
      }]
    })
  })
}

const handleExportExcel = () => {
  const ws = XLSX.utils.json_to_sheet(rawData.value.map(i => ({ '日期': i.recordDate, '类型': i.recordType, '科目': i.category, '金额': i.amount })))
  const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb, ws, "财务明细");
  XLSX.writeFile(wb, "羊场财务明细.xlsx");
  exportDialogVisible.value = false;
}

const resetFilter = () => { dateRange.value = []; fetchData() }
onMounted(() => { fetchData(); window.addEventListener('resize', () => { lineChart?.resize(); pieChart?.resize(); rankChart?.resize(); profitCurveChart?.resize(); }) })
onBeforeUnmount(() => { lineChart?.dispose(); pieChart?.dispose(); rankChart?.dispose(); profitCurveChart?.dispose(); })
</script>

<style scoped>
.app-container { padding: 20px; background: #f0f2f5; min-height: 100vh; }
.stat-dashboard { margin-bottom: 20px; }
.glass-card { border-radius: 15px; border: none; color: #fff; position: relative; overflow: hidden; height: 120px; }
.glass-card.income { background: linear-gradient(135deg, #67C23A 0%, #95d475 100%); }
.glass-card.expense { background: linear-gradient(135deg, #F56C6C 0%, #f89898 100%); }
.glass-card.profit { background: linear-gradient(135deg, #409EFF 0%, #79bbff 100%); }
.glass-card.efficiency { background: linear-gradient(135deg, #E6A23C 0%, #eebe77 100%); }

.card-body { position: relative; z-index: 2; }
.label { font-size: 14px; opacity: 0.8; margin-bottom: 5px; }
.value { font-size: 26px; font-weight: bold; margin-bottom: 5px; }
.trend { font-size: 12px; opacity: 0.7; }
.bg-icon { position: absolute; right: -10px; bottom: -10px; font-size: 80px; opacity: 0.15; color: #fff; }

.action-card { margin-bottom: 20px; border-radius: 10px; }
.action-flex { display: flex; justify-content: space-between; align-items: center; }
.chart-card { border-radius: 10px; }
.c-header { display: flex; align-items: center; gap: 8px; font-weight: bold; }
.chart-box { height: 350px; width: 100%; }

.report-preview-body { padding: 20px; background: #fff; }
.report-cover { text-align: center; margin-bottom: 30px; }
.report-table { width: 100%; border: 1px solid #eee; }
.report-charts-grid { display: flex; gap: 20px; margin-top: 20px; }
.report-chart-item { flex: 1; border: 1px solid #eee; padding: 10px; text-align: center; }
.report-chart-item img { width: 100%; }

.ai-chat-container { padding: 20px; line-height: 1.8; }
.neg { color: #ffd1d1; }
</style>
