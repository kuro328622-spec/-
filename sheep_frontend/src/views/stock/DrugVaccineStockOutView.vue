<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2>药品/疫苗出库管理</h2>
        <div class="top-btns">
          <el-dropdown trigger="click" @command="handleExportCommand" style="margin-right: 12px">
            <el-button type="warning" icon="Share">
              导出 / 复制筛选数据<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="copy_simple">简洁复制</el-dropdown-item>
                <el-dropdown-item command="copy_full">完整复制</el-dropdown-item>
                <el-dropdown-item command="excel" divided>导出 Excel</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="primary" icon="Plus" @click="openAddDialog">
            添加出库记录
          </el-button>
        </div>
      </div>

      <div class="filter-bar" style="display: flex; gap: 10px; margin-bottom: 20px; background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); align-items: center;">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索名称或单号..."
          style="width: 170px;"
          prefix-icon="Search"
          clearable
        />

        <el-select v-model="filterParams.category" placeholder="全部类别" clearable style="width: 110px">
          <el-option label="药品" value="药品" />
          <el-option label="疫苗" value="疫苗" />
        </el-select>

        <el-date-picker
          v-model="filterParams.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 230px;"
        />

        <el-button @click="resetFilter" icon="Refresh">重置</el-button>

        <div class="filter-info" v-if="filteredList.length !== tableData.length" style="margin-left: auto; font-size: 13px; color: #909399;">
          已筛选出 <b>{{ filteredList.length }}</b> 条记录
        </div>
      </div>

      <div class="table-container">
        <el-table v-loading="loading" :data="filteredList" style="width: 100%" border>
          <el-table-column label="操作" width="120" fixed="left">
            <template #default="scope">
              <div style="display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 5px 0;">
                <el-button type="warning" size="small" icon="DocumentCopy" @click="handleCopyRow(scope.row)" style="width: 80px; margin-left: 0;">复制</el-button>
                <el-button type="success" size="small" icon="Edit" @click="handleEdit(scope.row)" style="width: 80px; margin-left: 0;">编辑</el-button>
                <el-button type="danger" size="small" icon="Delete" @click="handleDelete(scope.row)" style="width: 80px; margin-left: 0;">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="outboundNo" label="出库单号" min-width="150" />
          <el-table-column prop="name" label="名称" min-width="150">
            <template #default="scope">
              <span>{{ scope.row.name }}</span>
              <div style="font-size: 12px; color: #909399">{{ scope.row.manufacturer }}</div>
            </template>
          </el-table-column>

          <el-table-column prop="category" label="类别" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.category === '药品' ? 'success' : 'warning'">
                {{ scope.row.category }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="num" label="出库数量" width="100" />
          <el-table-column prop="outStaff" label="操作员" width="100" />

          <el-table-column prop="deliveryTime" label="出库日期" width="180">
            <template #default="scope">
              <span>{{ formatDate(scope.row.deliveryTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="选择库存物品" v-if="!isEditMode">
          <el-select
            v-model="selectedInventoryId"
            placeholder="请输入药品/疫苗名称搜索"
            style="width: 100%"
            filterable
            @change="handleInventoryChange"
          >
            <el-option
              v-for="item in inventoryList"
              :key="item.id"
              :label="`${item.name} [${item.category}] - 厂家: ${item.manufacturer} (余: ${item.quantity || item.totalInventory})`"
              :value="item.id"
            >
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                <el-tag size="small" :type="item.category === '药品' ? 'success' : 'warning'">{{ item.category }}</el-tag>
                / {{ item.manufacturer }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="出库单号" prop="outboundNo">
          <el-input v-model="formData.outboundNo" disabled />
        </el-form-item>

        <el-form-item label="物品名称" prop="name">
          <el-input v-model="formData.name" disabled>
             <template #append v-if="formData.category">
                {{ formData.category }}
             </template>
          </el-input>
        </el-form-item>

        <el-form-item label="生产厂家" prop="manufacturer">
          <el-input v-model="formData.manufacturer" disabled />
        </el-form-item>

        <el-form-item label="出库数量" prop="num">
          <el-input-number
            v-model="formData.num"
            :min="1"
            :max="maxAvailable"
            :precision="0"
            step-strictly
            style="width: 200px"
          />
          <span v-if="!isEditMode && selectedInventoryId" style="margin-left: 10px; color: #F56C6C; font-size: 12px">
            (最大库存: {{ maxAvailable }})
          </span>
        </el-form-item>

        <el-form-item label="出库用途" prop="outPurposes">
          <el-input v-model="formData.outPurposes" placeholder="请输入出库用途" />
        </el-form-item>

        <el-form-item label="出库日期" prop="deliveryTime">
          <el-date-picker
            v-model="formData.deliveryTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>

        <el-form-item label="备注" prop="notes">
          <el-input type="textarea" v-model="formData.notes" rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue' // 增加了 computed
import { useUserStore } from '@/stores/userStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

import {
  getDrugVaccineStockOutList,
  addDrugVaccineStockOut,
  updateDrugVaccineStockOut,
  deleteDrugVaccineStockOut
} from '@/api/drugVaccineStockOutApi'

import * as MedicineApi from '@/api/medicineApi.js'
// 修改点 4: 引入导出工具
import { exportToExcel } from '@/utils/exportUtils.js'
import useClipboard from 'vue-clipboard3'
// ✨ 1. 引入 AI Store
import { useAIStore } from '@/stores/aiStore';
const aiStore = useAIStore();
const { toClipboard } = useClipboard()
const getInventory = MedicineApi.getInventoryList || MedicineApi.getMedicineList

const userStore = useUserStore()
const tableData = ref([])
const inventoryList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加出库记录')
const formRef = ref(null)
const isEditMode = ref(false)
const selectedInventoryId = ref(null)
const maxAvailable = ref(999999)

// 修改点 5: 添加筛选参数
const filterParams = reactive({
  keyword: "",
  dateRange: [],
  category: ""
})

// Excel 导出映射
const outboundHeaderMap = {
  outboundNo: "出库单号",
  name: "物品名称",
  category: "类别",
  manufacturer: "生产厂家",
  num: "出库数量",
  outPurposes: "出库用途",
  outStaff: "操作员",
  deliveryTime: "出库日期",
  notes: "备注"
}

// 修改点 6: 联动筛选逻辑
const filteredList = computed(() => {
  return tableData.value.filter(item => {
    const matchKeyword = !filterParams.keyword ||
      item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
      item.outboundNo.toLowerCase().includes(filterParams.keyword.toLowerCase());

    const matchCategory = !filterParams.category || item.category === filterParams.category;

    let matchDate = true;
    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      const start = filterParams.dateRange[0];
      const end = filterParams.dateRange[1];
      const itemDate = dayjs(item.deliveryTime).format('YYYY-MM-DD');
      matchDate = itemDate >= start && itemDate <= end;
    }
    return matchKeyword && matchCategory && matchDate;
  });
});

const resetFilter = () => {
  filterParams.keyword = "";
  filterParams.dateRange = [];
  filterParams.category = "";
};

// 修改点 7: 导出与批量复制方法
const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("没有可操作的数据");

  if (command === 'excel') {
    exportToExcel(activeData, outboundHeaderMap, "药品疫苗出库记录");
  } else if (command === 'copy_simple') {
    try {
      let text = `📋【药品出库简报】\n\n`;
      activeData.forEach((item, index) => {
        text += `${index + 1}. 【单号】${item.outboundNo} | 【名称】${item.name} | 数量：${item.num} | 日期：${dayjs(item.deliveryTime).format('YYYY-MM-DD')}\n`;
      });
      await toClipboard(text);
      ElMessage.success("简洁数据已复制");
    } catch { ElMessage.error("复制失败"); }
  } else if (command === 'copy_full') {
    try {
      let text = `📋【药品/疫苗出库全量分析数据】\n\n`;
      activeData.forEach((item, index) => {
        text += `[出库记录 #${index + 1}]\n` +
                `--------------------------\n` +
                `出库单号：${item.outboundNo}\n` +
                `物品名称：${item.name} (${item.category})\n` +
                `生产厂家：${item.manufacturer}\n` +
                `出库数量：${item.num}\n` +
                `出库用途：${item.outPurposes || '未填写'}\n` +
                `操作员：${item.outStaff}\n` +
                `出库时间：${dayjs(item.deliveryTime).format('YYYY-MM-DD HH:mm:ss')}\n` +
                `备注信息：${item.notes || '无'}\n` +
                `--------------------------\n\n`;
      });
      await toClipboard(text);
      ElMessage.success("全量数据已按模板复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

// 修改点 8: 单行复制方法
const handleCopyRow = async (row) => {
  try {
    const text = `【出库核心数据】\n` +
                 `--------------------------\n` +
                 `单号：${row.outboundNo}\n` +
                 `名称：${row.name}\n` +
                 `数量：${row.num}\n` +
                 `用途：${row.outPurposes}\n` +
                 `日期：${dayjs(row.deliveryTime).format('YYYY-MM-DD HH:mm:ss')}`;
    await toClipboard(text);
    ElMessage.success("单条核心数据已复制");
  } catch { ElMessage.error("复制失败"); }
};

// --- 以下原有逻辑保持不动 ---

const formData = reactive({
  id: null,
  inventoryId: null,
  manufacturer: '',
  name: '',
  category: '',
  outboundNo: '',
  deliveryTime: null,
  outPurposes: '',
  num: 1,
  outStaff: '',
  notes: ''
})

const formRules = reactive({
  outboundNo: [{ required: true, message: '单号缺失', trigger: 'blur' }],
  name: [{ required: true, message: '请选择库存品种', trigger: 'change' }],
  num: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  outPurposes: [{ required: true, message: '请输入用途', trigger: 'blur' }],
  deliveryTime: [{ required: true, message: '请选择日期', trigger: 'change' }]
})

const formatDate = (val) => val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : ''

const loadStockOutList = async () => {
  loading.value = true
  try {
    const res = await getDrugVaccineStockOutList()
    if (Array.isArray(res)) {
      tableData.value = res
    } else if (res && Array.isArray(res.data)) {
      tableData.value = res.data
    } else {
      tableData.value = []
    }
  } catch (err) {
    console.error('获取出库列表失败:', err)
    ElMessage.error('获取出库列表失败')
  } finally {
    loading.value = false
  }
}

const loadInventoryData = async () => {
  try {
    const res = await getInventory()
    console.log("出库页面获取到的原始数据：", res) // 调试用

    // 自动兼容两种返回结构：直接返回数组 或 返回带 data 的对象
    let rawData = Array.isArray(res) ? res : (res.data || [])

    // 关键点：确保字段名匹配。如果总库存页面显示正常，说明字段是 quantity
    inventoryList.value = rawData.filter(item => {
      // 这里的 quantity 必须和总库存 table 里的 prop 对应
      const stockNum = item.quantity ?? item.totalInventory ?? 0
      return stockNum > 0
    })

    console.log("过滤后可用于出库的列表：", inventoryList.value)
  } catch (err) {
    console.error('库存获取失败:', err)
    ElMessage.error('无法加载库存列表，请检查网络或联系管理员')
  }
}

const handleInventoryChange = (id) => {
  const item = inventoryList.value.find(i => i.id === id)
  if (item) {
    formData.inventoryId = item.id
    formData.name = item.name
    formData.manufacturer = item.manufacturer
    formData.category = item.category
    maxAvailable.value = item.quantity || item.totalInventory
    formData.num = 1
  }
}

const openAddDialog = () => {
  isEditMode.value = false
  dialogTitle.value = '添加出库记录'
  resetForm()
  formData.outboundNo = 'CK' + dayjs().format('YYYYMMDDHHmmss')
  formData.deliveryTime = new Date()
  formData.outStaff = userStore.userInfo?.username || 'admin'
  loadInventoryData()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，仅管理员可编辑！");
    return;
  }
  isEditMode.value = true
  dialogTitle.value = '编辑出库记录'
  Object.assign(formData, { ...row })
  formData.deliveryTime = row.deliveryTime ? new Date(row.deliveryTime) : null
  dialogVisible.value = true
}

const handleDelete = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，仅管理员可删除！");
    return;
  }
  ElMessageBox.confirm('确定要删除这条出库记录并退回库存吗？', '确认删除', { type: 'warning' })
    .then(async () => {
      try {
        await deleteDrugVaccineStockOut(row.id)
        ElMessage.success('删除成功，库存已返还')
        loadStockOutList()
      } catch (e) {
        console.error('删除失败:', e)
      }
    })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true

    const submitData = {
      ...formData,
      outStaff: userStore.userInfo?.username || formData.outStaff || 'admin',
      deliveryTime: dayjs(formData.deliveryTime).format("YYYY-MM-DD HH:mm:ss")
    }

    try {
      if (isEditMode.value) {
        await updateDrugVaccineStockOut(formData.id, submitData)
        ElMessage.success('记录更新成功')
      } else {
        await addDrugVaccineStockOut(submitData)
        ElMessage.success('出库申请已成功提交')
      }
      dialogVisible.value = false
      loadStockOutList()
    } catch (err) {
      console.error('提交失败:', err)
      const errorMsg = err.response?.data?.message || '操作失败'
      ElMessage.error('提交失败：' + errorMsg)
    } finally {
      loading.value = false
    }
  })
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  selectedInventoryId.value = null
  maxAvailable.value = 999999
  Object.assign(formData, {
    id: null,
    inventoryId: null,
    manufacturer: '',
    name: '',
    category: '',
    outboundNo: '',
    deliveryTime: null,
    outPurposes: '',
    num: 1,
    outStaff: userStore.userInfo?.username || '',
    notes: ''
  })
}

// ✨ 2. AI 出库数据同步逻辑
const syncOutboundLogsToAI = () => {
  if (!tableData.value || tableData.value.length === 0) {
    aiStore.setPageContext('medicineOutLogs', "暂无药品疫苗出库记录。");
    return;
  }

  // 取最近 15 条出库流水
  const recentLogs = tableData.value.slice(0, 15);
  const summary = recentLogs.map((item, index) => {
    const date = item.deliveryTime ? dayjs(item.deliveryTime).format('YYYY-MM-DD HH:mm') : '未知时间';
    return `[出库记录${index + 1}]
    - 时间: ${date}
    - 单号: ${item.outboundNo}
    - 品名: ${item.name} (${item.category})
    - 厂家: ${item.manufacturer}
    - 数量: ${item.num}
    - 用途: ${item.outPurposes || '未注明'}
    - 备注: ${item.notes || '无'}
    - 操作员: ${item.outStaff}`;
  }).join('\n\n');

  aiStore.setPageContext('medicineOutLogs', summary);
};

// 开启深度监听，确保出库、删除、编辑后 AI 记忆同步
watch(tableData, () => {
  syncOutboundLogsToAI();
}, { deep: true });

onMounted(async () => {
  await loadStockOutList(); // 确保用了 await，拿到数据再同步
  // ✨ 3. 初始化同步
  syncOutboundLogsToAI();
});
</script>

<style scoped>
.page-container {
  display: flex;
  width: 100%;
  height: 100%;
}

.left-blank {
  flex: 1;
}

.main-content {
  flex: 4;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 20px;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions h2 {
  margin: 0;
  color: #333;
}

.table-container {
  flex: 1;
  overflow-x: auto;
}

.el-table {
  --el-table-header-text-color: #606266;
  --el-table-row-hover-bg-color: #f5f7fa;
  width: 100%;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 新增样式：让顶部按钮看起来更整齐 */
.top-btns {
  display: flex;
  align-items: center;
}
</style>
