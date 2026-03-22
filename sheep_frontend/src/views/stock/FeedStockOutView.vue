<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2 class="page-title">饲料出库管理</h2>

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
          <el-button type="primary" icon="Plus" @click="openAddDialog">添加出库记录</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索名称或厂家..."
          style="width: 160px;"
          prefix-icon="Search"
          clearable
        />

        <el-select v-model="filterParams.category" placeholder="全部类别" clearable style="width: 110px">
          <el-option label="全价料" value="全价料" />
          <el-option label="预混料" value="预混料" />
          <el-option label="浓缩料" value="浓缩料" />
          <el-option label="大宗原料" value="大宗原料" />
        </el-select>

        <el-select v-model="filterParams.componentType" placeholder="全部成分" clearable style="width: 110px">
          <el-option label="玉米" value="玉米" />
          <el-option label="豆粕" value="豆粕" />
          <el-option label="鱼粉" value="鱼粉" />
          <el-option label="麸皮" value="麸皮" />
          <el-option label="其他" value="OTHER" />
        </el-select>

        <el-date-picker
          v-model="filterParams.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始"
          end-placeholder="结束"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 230px;"
        />

        <el-button @click="resetFilter" icon="Refresh">重置</el-button>

        <div class="filter-info" v-if="filteredList.length !== tableData.length">
          已筛选出 <b>{{ filteredList.length }}</b> 条
        </div>
      </div>

      <div class="table-container">
        <el-table v-loading="loading" :data="filteredList" style="width: 100%" border :default-sort="{ prop: 'deliveryTime', order: 'descending' }">
          <el-table-column label="操作" width="110" fixed="left">
            <template #default="scope">
              <div class="op-buttons-vertical">
                <el-button type="warning" size="small" icon="DocumentCopy" @click="handleCopyRow(scope.row)">复制</el-button>
                <el-button type="success" size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="outboundNo" label="出库单号" width="160" />

          <el-table-column prop="name" label="名称/厂家" min-width="180">
            <template #default="scope">
              <div style="font-weight: bold">{{ scope.row.name }}</div>
              <div style="font-size: 12px; color: #909399">{{ scope.row.manufacturer }}</div>
            </template>
          </el-table-column>

          <el-table-column prop="category" label="类别" width="100">
             <template #default="scope">
              <el-tag type="info">{{ scope.row.category }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="componentType" label="成分类型" width="120">
            <template #default="scope">
              <el-tag effect="plain">{{ scope.row.componentType }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="num" label="出库数量(kg)" width="110" />
          <el-table-column prop="outPurposes" label="出库用途" min-width="150" show-overflow-tooltip />
          <el-table-column prop="outStaff" label="操作员" width="100" />

          <el-table-column prop="deliveryTime" label="出库时间" width="170">
            <template #default="scope">
              <span>{{ formatDate(scope.row.deliveryTime) }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="选择库存饲料" v-if="!isEditMode">
          <el-select v-model="selectedInventoryId" placeholder="请选择库存中的饲料" style="width: 100%" filterable @change="handleInventoryChange">
            <el-option v-for="item in inventoryList" :key="item.id" :label="`${item.name} | ${item.componentType} - ${item.manufacturer} (余: ${item.quantity})`" :value="item.id">
              <span style="float: left">{{ item.name }} ({{ item.componentType }})</span>
              <span style="float: right; color: #8492a6; font-size: 12px">库存: {{ item.quantity }} | {{ item.manufacturer }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="出库单号" prop="outboundNo">
          <el-input v-model="formData.outboundNo" disabled />
        </el-form-item>
        <el-form-item label="饲料信息">
          <el-row :gutter="10">
            <el-col :span="12"><el-input v-model="formData.name" placeholder="名称" disabled /></el-col>
            <el-col :span="12"><el-input v-model="formData.componentType" placeholder="成分类型" disabled /></el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="formData.manufacturer" disabled />
        </el-form-item>
        <el-form-item label="出库数量" prop="num">
          <el-input-number v-model="formData.num" :min="0.01" :max="maxAvailable" :precision="2" style="width: 200px" />
          <span v-if="!isEditMode && selectedInventoryId" style="margin-left: 10px; color: #F56C6C; font-size: 12px">(当前最大可用: {{ maxAvailable }})</span>
        </el-form-item>
        <el-form-item label="出库用途" prop="outPurposes">
          <el-input v-model="formData.outPurposes" placeholder="例如：1号猪舍领用" />
        </el-form-item>
        <el-form-item label="出库时间" prop="deliveryTime">
          <el-date-picker v-model="formData.deliveryTime" type="datetime" placeholder="选择出库时间" style="width: 100%" format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="备注" prop="notes">
          <el-input type="textarea" v-model="formData.notes" rows="2" placeholder="填写相关备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submitForm">确认出库</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import {
  getFeedStockOutList,
  addFeedStockOut,
  updateFeedStockOut,
  deleteFeedStockOut,
  getFeedInventoryList
} from '@/api/feedInventoryApi'
import { exportToExcel } from "@/utils/exportUtils.js";
import useClipboard from "vue-clipboard3";

const { toClipboard } = useClipboard();
const userStore = useUserStore()

const tableData = ref([])
const inventoryList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('饲料出库')
const formRef = ref(null)
const isEditMode = ref(false)
const selectedInventoryId = ref(null)
const maxAvailable = ref(999999)

// ✨ 预设成分列表
const presetComponents = ['玉米', '豆粕', '鱼粉', '麸皮'];

// ✨ 增强筛选参数
const filterParams = reactive({
  keyword: "",
  dateRange: [],
  category: "",
  componentType: ""
});

const outHeaderMap = {
  outboundNo: "出库单号",
  name: "名称",
  manufacturer: "生产厂家",
  category: "类别",
  num: "数量",
  outPurposes: "用途",
  deliveryTime: "出库时间",
  outStaff: "操作员"
};

const formData = reactive({
  id: null, manufacturer: '', name: '', category: '', componentType: '',
  outboundNo: '', deliveryTime: null, outPurposes: '', num: 1,
  outStaff: '', notes: '', productionBatch: ''
})

const formRules = reactive({
  outboundNo: [{ required: true, message: '单号不能为空', trigger: 'blur' }],
  num: [{ required: true, message: '请输入出库数量', trigger: 'blur' }],
  outPurposes: [{ required: true, message: '请输入用途', trigger: 'blur' }],
  deliveryTime: [{ required: true, message: '请选择时间', trigger: 'change' }]
})

const formatDate = (val) => val ? dayjs(val).format('YYYY-MM-DD HH:mm:ss') : ''

// ✨ 四项联动实时筛选逻辑
const filteredList = computed(() => {
  return tableData.value.filter(item => {
    // 关键字匹配 (名称或厂家)
    const matchKeyword = !filterParams.keyword ||
      item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
      item.manufacturer.toLowerCase().includes(filterParams.keyword.toLowerCase());

    // 类别匹配
    const matchCategory = !filterParams.category || item.category === filterParams.category;

    // 成分类型匹配 (含其他逻辑)
    let matchComponent = true;
    if (filterParams.componentType) {
      if (filterParams.componentType === 'OTHER') {
        matchComponent = !presetComponents.includes(item.componentType);
      } else {
        matchComponent = item.componentType === filterParams.componentType;
      }
    }

    // 日期范围匹配
    let matchDate = true;
    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      const start = filterParams.dateRange[0];
      const end = filterParams.dateRange[1];
      const itemDate = item.deliveryTime.substring(0, 10);
      matchDate = itemDate >= start && itemDate <= end;
    }
    return matchKeyword && matchCategory && matchComponent && matchDate;
  });
});

const resetFilter = () => {
  filterParams.keyword = "";
  filterParams.dateRange = [];
  filterParams.category = "";
  filterParams.componentType = "";
};

const handleCopyRow = async (row) => {
  try {
    const text = `【饲料出库核心明细】\n` +
                 `--------------------------\n` +
                 `出库单号：${row.outboundNo}\n` +
                 `生产厂家：${row.manufacturer}\n` +
                 `饲料名称：${row.name}\n` +
                 `饲料类别：${row.category}\n` +
                 `成分类型：${row.componentType}\n` +
                 `--------------------------\n` +
                 `出库数量：${row.num} kg\n` +
                 `出库用途：${row.outPurposes}\n` +
                 `出库日期：${formatDate(row.deliveryTime)}\n` +
                 `--------------------------`;
    await toClipboard(text);
    ElMessage.success("单条数据已复制");
  } catch { ElMessage.error("复制失败"); }
};

const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("没有可导出的数据");

  if (command === 'excel') {
    exportToExcel(activeData, outHeaderMap, "饲料出库记录");
  } else if (command === 'copy_simple') {
    try {
      let text = `📋【饲料出库简报】\n\n`;
      activeData.forEach((item, index) => {
        text += `${index + 1}. 名称：${item.name} | 数量：${item.num}kg | 用途：${item.outPurposes}\n`;
      });
      await toClipboard(text);
      ElMessage.success("简报已复制");
    } catch { ElMessage.error("复制失败"); }
  } else if (command === 'copy_full') {
    try {
      let text = `📋【饲料出库全量筛选导出】\n\n`;
      activeData.forEach((item, index) => {
        text += `[记录 #${index + 1}]\n` +
                `--------------------------\n` +
                `单号：${item.outboundNo}\n` +
                `名称：${item.name} | 厂家：${item.manufacturer}\n` +
                `类别：${item.category} | 成分：${item.componentType}\n` +
                `数量：${item.num} kg | 用途：${item.outPurposes}\n` +
                `时间：${formatDate(item.deliveryTime)}\n` +
                `--------------------------\n\n`;
      });
      await toClipboard(text);
      ElMessage.success("全量明细已复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

const loadStockOutList = async () => {
  loading.value = true
  try {
    const res = await getFeedStockOutList()
    tableData.value = res.data || res
  } catch { ElMessage.error('获取出库列表失败') }
  finally { loading.value = false }
}

const loadInventoryData = async () => {
  try {
    const res = await getFeedInventoryList()
    inventoryList.value = (res.data || res).filter(item => item.quantity > 0)
  } catch (err) { console.error('库存获取失败:', err) }
}

const handleInventoryChange = (id) => {
  const item = inventoryList.value.find(i => i.id === id)
  if (item) {
    formData.name = item.name; formData.manufacturer = item.manufacturer;
    formData.category = item.category; formData.componentType = item.componentType;
    formData.productionBatch = item.productionBatch || '';
    maxAvailable.value = item.quantity; formData.num = 1;
  }
}

const openAddDialog = () => {
  isEditMode.value = false; dialogTitle.value = '添加出库记录';
  resetForm(); formData.outboundNo = 'SL-CK' + dayjs().format('YYYYMMDDHHmmss');
  formData.deliveryTime = new Date(); formData.outStaff = userStore.userInfo?.username || 'admin';
  loadInventoryData(); dialogVisible.value = true;
}

const handleEdit = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) return ElMessage.error("权限不足");
  isEditMode.value = true; dialogTitle.value = '编辑出库记录';
  Object.assign(formData, { ...row });
  formData.deliveryTime = row.deliveryTime ? new Date(row.deliveryTime) : null;
  dialogVisible.value = true;
}

const handleDelete = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) return ElMessage.error("权限不足");
  ElMessageBox.confirm('确定删除该记录？', '警告', { type: 'warning' }).then(async () => {
    await deleteFeedStockOut(row.id); ElMessage.success('删除成功'); loadStockOutList();
  })
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const submitData = {
        ...formData,
        num: Number(formData.num),
        deliveryTime: dayjs(formData.deliveryTime).format("YYYY-MM-DD HH:mm:ss")
      }
      if (isEditMode.value) await updateFeedStockOut(formData.id, submitData);
      else await addFeedStockOut(submitData);
      ElMessage.success('操作成功'); dialogVisible.value = false; loadStockOutList();
    } catch (err) {
      ElMessage.error('失败：' + (err.response?.data?.message || '校验未通过'))
    } finally { loading.value = false }
  })
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
  selectedInventoryId.value = null; maxAvailable.value = 999999;
  Object.assign(formData, {
    id: null, manufacturer: '', name: '', category: '', componentType: '',
    outboundNo: '', deliveryTime: null, outPurposes: '', num: 1,
    outStaff: userStore.userInfo?.username || '', notes: '', productionBatch: ''
  })
}

onMounted(() => { loadStockOutList() })
</script>

<style scoped>
.page-container { display: flex; width: 100%; height: 100%; }
.left-blank { flex: 1; }
.main-content {
  flex: 4; display: flex; flex-direction: column; padding: 20px;
  background-color: #f9f9f9; border-radius: 8px; margin: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.header-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.filter-bar {
  display: flex; gap: 10px; margin-bottom: 20px; background: #fff;
  padding: 15px 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); align-items: center;
}
.filter-info { margin-left: auto; font-size: 13px; color: #909399; }
.op-buttons-vertical {
  display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 5px 0;
}
.op-buttons-vertical .el-button { width: 80px; margin-left: 0 !important; }
.table-container { flex: 1; overflow-x: auto; }
.dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
</style>
