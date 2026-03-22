<template>
  <div class="app-container">
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card income">
          <div class="card-label">本期总收入</div>
          <div class="card-value">￥ {{ totalIncome.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card expense">
          <div class="card-label">本期总支出</div>
          <div class="card-value">￥ {{ totalExpense.toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card profit">
          <div class="card-label">本期净利润</div>
          <div class="card-value">￥ {{ (totalIncome - totalExpense).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="全部" clearable style="width: 100px">
            <el-option label="收入" value="收入" />
            <el-option label="支出" value="支出" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="queryParams.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查找</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" :icon="Plus" @click="openDialog('收入')">录入收入</el-button>
          <el-button type="danger" :icon="Minus" @click="openDialog('支出')">录入支出</el-button>

          <el-dropdown trigger="click" @command="handleExportCommand" style="margin-left: 12px">
            <el-button type="warning" :icon="Share">
              导出 / 复制筛选数据<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="copy_all">复制当前全部 (文本格式)</el-dropdown-item>
                <el-dropdown-item command="excel" divided>导出 Excel 表格</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table
        v-loading="loading"
        :data="filteredTableData"
        border
        stripe
        empty-text="暂无财务记录"
      >
        <el-table-column prop="recordDate" label="日期" width="120" sortable />
        <el-table-column prop="recordType" label="类型" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.recordType === '收入' ? 'success' : 'danger'">{{ scope.row.recordType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="明细内容" />
        <el-table-column prop="amount" label="金额">
          <template #default="scope">
            <span :class="scope.row.recordType === '收入' ? 'amt-in' : 'amt-out'">
              {{ scope.row.recordType === '收入' ? '+' : '-' }} {{ Number(scope.row.amount).toLocaleString(undefined, { minimumFractionDigits: 2 }) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="生成方式" width="120">
          <template #default="scope">
            <el-tag size="small" :effect="scope.row.isAuto === 1 ? 'dark' : 'plain'">
              {{ scope.row.isAuto === 1 ? '系统同步' : '手动录入' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="110" fixed="right">
          <template #default="scope">
            <div class="op-buttons-vertical">
              <el-button
                type="warning"
                size="small"
                icon="DocumentCopy"
                @click="handleCopyRow(scope.row)"
              >复制</el-button>

              <el-button v-if="scope.row.isAuto !== 1" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
              <el-tooltip v-else content="该记录由业务模块同步" placement="top">
                <el-button link type="info" :icon="Lock" disabled>锁定</el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="'新增' + currentAction" width="450px" @close="resetForm">
      <el-form :model="financeForm" label-width="90px">
        <el-form-item label="发生日期">
          <el-date-picker v-model="financeForm.date" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="currentAction + '项目'">
          <el-select v-model="financeForm.usage" placeholder="请选择" style="width: 100%">
            <template v-if="currentAction === '收入'">
              <el-option label="卖羊收入" value="卖羊收入" />
              <el-option label="政府补贴" value="政府补贴" />
              <el-option label="羊副产品销售" value="羊副产品销售" />
              <el-option label="其他收入" value="OTHER" />
            </template>
            <template v-else>
              <el-option label="员工工资" value="员工工资" />
              <el-option label="水电费用" value="水电费用" />
              <el-option label="检测检验费用" value="检测检验费用" />
              <el-option label="其他支出" value="OTHER" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item v-if="financeForm.usage === 'OTHER'" label="内容详情">
          <el-input v-model="financeForm.customUsage" placeholder="请输入具体明细" maxlength="30" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="financeForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :type="currentAction === '收入' ? 'primary' : 'danger'" :loading="loading" @click="submitFinance">确认保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Minus, Lock, Share, ArrowDown} from '@element-plus/icons-vue'
import { getFinancialList, addFinancialRecord, deleteFinancialRecord } from "@/api/financialApi"
import { useUserStore } from "@/stores/userStore"
import { exportToExcel } from "@/utils/exportUtils.js"
import useClipboard from 'vue-clipboard3'

const { toClipboard } = useClipboard()
const userStore = useUserStore()
const tableData = ref([])
const loading = ref(false)

// Excel 映射表
const financeHeaderMap = {
  recordDate: "日期",
  recordType: "类型",
  category: "明细内容",
  amount: "金额",
  isAuto: "系统同步(1是/0否)"
}

// 获取数据
const fetchRecords = async () => {
  loading.value = true
  try {
    const res = await getFinancialList()
    const data = res.data || res
    tableData.value = Array.isArray(data) ? data : []
  } catch  {
    ElMessage.error('无法加载财务数据')
  } finally { loading.value = false }
}

onMounted(() => fetchRecords())

// 统计看板
const totalIncome = computed(() => tableData.value.filter(i => i.recordType === '收入').reduce((s, n) => s + (Number(n.amount) || 0), 0))
const totalExpense = computed(() => tableData.value.filter(i => i.recordType === '支出').reduce((s, n) => s + (Number(n.amount) || 0), 0))

// 筛选查找逻辑
const queryParams = reactive({ type: '', dateRange: [] })
const filteredTableData = computed(() => {
  return tableData.value.filter(item => {
    const typeMatch = !queryParams.type || item.recordType === queryParams.type
    let dateMatch = true
    if (queryParams.dateRange?.length === 2) {
      dateMatch = item.recordDate >= queryParams.dateRange[0] && item.recordDate <= queryParams.dateRange[1]
    }
    return typeMatch && dateMatch
  })
})

const handleQuery = () => ElMessage.success('已应用筛选')
const resetQuery = () => { queryParams.type = ''; queryParams.dateRange = []; fetchRecords() }

// ✨ 复制与导出功能逻辑
const handleCopyRow = async (row) => {
  try {
    const text = `💰【单笔财务流水明细】\n` +
                 `--------------------------\n` +
                 `发生日期：${row.recordDate}\n` +
                 `记录类型：${row.recordType}\n` +
                 `明细内容：${row.category}\n` +
                 `金额明细：${row.recordType === '收入' ? '+' : '-'} ￥${Number(row.amount).toFixed(2)}\n` +
                 `记录来源：${row.isAuto === 1 ? '系统同步' : '手动录入'}\n` +
                 `--------------------------`;
    await toClipboard(text);
    ElMessage.success("明细已复制");
  } catch { ElMessage.error("复制失败"); }
};

const handleExportCommand = async (command) => {
  const activeData = filteredTableData.value;
  if (activeData.length === 0) return ElMessage.warning("当前筛选下无数据");

  if (command === 'excel') {
    exportToExcel(activeData, financeHeaderMap, "财务收支流水表");
    ElMessage.success("Excel 导出成功");
  } else if (command === 'copy_all') {
    try {
      let text = `📋【财务收支流水汇总】\n`;
      text += `统计区间：${queryParams.dateRange?.join(' 至 ') || '全部时间段'}\n`;
      text += `汇总统计：收入 +￥${totalIncome.value.toFixed(2)} | 支出 -￥${totalExpense.value.toFixed(2)}\n`;
      text += `--------------------------\n`;
      activeData.forEach((item, index) => {
        text += `${index + 1}. [${item.recordDate}] ${item.recordType} | ${item.category} | ￥${item.amount}\n`;
      });
      await toClipboard(text);
      ElMessage.success(`已复制 ${activeData.length} 条流水记录`);
    } catch { ElMessage.error("复制失败"); }
  }
};

// 录入对话框逻辑
const dialogVisible = ref(false)
const currentAction = ref('收入')
const financeForm = reactive({ date: new Date().toISOString().split('T')[0], usage: '', customUsage: '', amount: 0 })

const openDialog = (action) => {
  currentAction.value = action
  resetForm()
  dialogVisible.value = true
}

const resetForm = () => {
  financeForm.usage = ''
  financeForm.customUsage = ''
  financeForm.amount = 0
  financeForm.date = new Date().toISOString().split('T')[0]
}

const submitFinance = async () => {
  if (financeForm.amount <= 0 || !financeForm.usage) return ElMessage.warning('请填写完整信息')
  const payload = {
    recordDate: financeForm.date,
    recordType: currentAction.value,
    category: financeForm.usage === 'OTHER' ? financeForm.customUsage : financeForm.usage,
    amount: financeForm.amount,
    isAuto: 0
  }
  loading.value = true
  try {
    await addFinancialRecord(payload)
    ElMessage.success('录入成功')
    dialogVisible.value = false
    fetchRecords()
  } catch {
    ElMessage.error('录入失败')
  } finally { loading.value = false }
}

const handleDelete = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，仅管理员可删除记录")
    return
  }
  ElMessageBox.confirm(`确定删除该笔记录吗？\n内容：${row.category}`, '警示', { type: 'warning' })
  .then(async () => {
    try {
      await deleteFinancialRecord(row.id)
      ElMessage.success('已删除')
      fetchRecords()
    } catch { ElMessage.error('删除失败') }
  }).catch(() => {})
}
</script>

<style scoped>
.app-container { padding: 20px; background-color: #f5f7fa; min-height: 100vh; }
.stat-cards { margin-bottom: 20px; }
.stat-card { color: #fff; border: none; border-radius: 8px; }
.income { background: linear-gradient(135deg, #67C23A 0%, #95D475 100%); }
.expense { background: linear-gradient(135deg, #F56C6C 0%, #FAB6B6 100%); }
.profit { background: linear-gradient(135deg, #409EFF 0%, #79BBFF 100%); }
.card-label { font-size: 14px; opacity: 0.9; }
.card-value { font-size: 22px; font-weight: bold; margin-top: 5px; }
.filter-container { margin-bottom: 20px; }
.amt-in { color: #67C23A; font-weight: bold; }
.amt-out { color: #F56C6C; font-weight: bold; }

/* 🛠️ 纵向按钮样式 */
.op-buttons-vertical {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  padding: 4px 0;
}
.op-buttons-vertical .el-button {
  margin-left: 0 !important;
  width: 70px;
}
</style>
