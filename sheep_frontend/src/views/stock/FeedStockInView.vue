<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2 class="page-title">饲料入库管理</h2>

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
          <el-button type="primary" icon="Plus" @click="openAddDialog">添加入库记录</el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索厂家或名称..."
          style="width: 170px;"
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
          已筛选出 <b>{{ filteredList.length }}</b> 条记录
        </div>
      </div>

      <div class="table-container">
        <el-table v-loading="loading" :data="filteredList" style="width: 100%" border :default-sort="{ prop: 'stockInTime', order: 'descending' }">
          <el-table-column label="操作" width="110" fixed="left">
            <template #default="scope">
              <div class="op-buttons-vertical">
                <el-button type="warning" size="small" icon="DocumentCopy" @click="handleCopyRow(scope.row)">复制</el-button>
                <el-button type="success" size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="manufacturer" label="生产厂家" width="120" />
          <el-table-column prop="name" label="名称" width="120" />
          <el-table-column prop="category" label="类别" width="90" />
          <el-table-column prop="componentType" label="成分类型" width="100" />
          <el-table-column prop="stockInQuantity" label="数量(kg)" width="90" />
          <el-table-column prop="unitPrice" label="单价(元)" width="90" />
          <el-table-column prop="freightFee" label="运费(元)" width="90" />
          <el-table-column prop="totalPrice" label="总价(元)" width="100" />
          <el-table-column prop="waterContent" label="水分(%)" width="80" />
          <el-table-column prop="mildew" label="霉变(%)" width="80" />
          <el-table-column prop="impurityContent" label="杂质(%)" width="80" />
          <el-table-column prop="usage" label="用途" min-width="150" show-overflow-tooltip />
          <el-table-column prop="nutrients" label="营养成分" min-width="150" show-overflow-tooltip />
          <el-table-column prop="productionDate" label="生产日期" width="110" />
          <el-table-column prop="expirationDate" label="到期日期" width="110" />
          <el-table-column prop="operator" label="操作员" width="100" />
          <el-table-column prop="stockInTime" label="入库时间" width="170" />
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="850px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="生产厂家" prop="manufacturer">
              <el-select v-model="formData.manufacturer" placeholder="厂家" :disabled="isEditMode" filterable allow-create default-first-option style="width: 100%">
                <el-option v-for="item in filteredManufacturers" :key="item.id || item.manufacturerName" :label="item.manufacturerName" :value="item.manufacturerName" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="名称" prop="name">
              <el-input v-model="formData.name" :disabled="isEditMode" placeholder="请输入名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="类别" prop="category">
              <el-select v-model="formData.category" :disabled="isEditMode" placeholder="请选择" style="width: 100%">
                <el-option label="全价料" value="全价料" />
                <el-option label="预混料" value="预混料" />
                <el-option label="浓缩料" value="浓缩料" />
                <el-option label="大宗原料" value="大宗原料" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="成分类型" prop="componentType">
              <el-select v-model="formData.componentType" placeholder="选择或输入" :disabled="isEditMode" filterable allow-create default-first-option style="width: 100%">
                <el-option label="玉米" value="玉米" /><el-option label="豆粕" value="豆粕" /><el-option label="鱼粉" value="鱼粉" /><el-option label="麸皮" value="麸皮" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位" prop="unitOfMeasure">
              <el-input v-model="formData.unitOfMeasure" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="生产日期" prop="productionDate">
              <el-date-picker v-model="formData.productionDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="expirationDate">
              <el-date-picker v-model="formData.expirationDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">收货与质检</el-divider>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库数量" prop="stockInQuantity">
              <el-input-number v-model="formData.stockInQuantity" :min="0.01" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价(元)" prop="unitPrice">
              <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="运费(元)" prop="freightFee">
              <el-input-number v-model="formData.freightFee" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="水分(%)" prop="waterContent">
              <el-input-number v-model="formData.waterContent" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="霉变(%)" prop="mildew">
              <el-input-number v-model="formData.mildew" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="杂质(%)" prop="impurityContent">
              <el-input-number v-model="formData.impurityContent" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="用途" prop="usage">
          <el-input v-model="formData.usage" type="textarea" :rows="2" placeholder="用途" />
        </el-form-item>
        <el-form-item label="营养成分" prop="nutrients">
          <el-input v-model="formData.nutrients" type="textarea" :rows="2" placeholder="营养说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="loading" @click="submitForm">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useUserStore } from "@/stores/userStore";
import { ElMessage, ElMessageBox } from "element-plus";
import dayjs from "dayjs";
import { getFeedStockInList, addFeedStockIn, updateFeedStockIn, deleteFeedStockIn } from "@/api/feedStockInApi";
import { getManufacturerList } from "@/api/manufacturerApi";
import { exportToExcel } from "@/utils/exportUtils.js";
import useClipboard from "vue-clipboard3";

const { toClipboard } = useClipboard();
const userStore = useUserStore();
const isAdmin = computed(() => userStore.userInfo?.roles?.includes("ROLE_ADMIN"));

const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref("添加入库记录");
const formRef = ref(null);
const isEditMode = ref(false);

// 预设品种列表，用于判断“其他”
const presetComponents = ['玉米', '豆粕', '鱼粉', '麸皮'];

const filterParams = reactive({
  keyword: "",
  dateRange: [],
  category: "",
  componentType: ""
});

const feedHeaderMap = {
  manufacturer: "生产厂家",
  name: "名称",
  category: "类别",
  stockInQuantity: "数量(kg)",
  unitPrice: "单价(元)",
  totalPrice: "总价(元)",
  stockInTime: "入库时间",
  operator: "操作员"
};

const allManufacturers = ref([]);
const filteredManufacturers = computed(() => {
  return allManufacturers.value.filter(item => item.manCategory && item.manCategory.includes("饲料"));
});

// 核心联动筛选逻辑
const filteredList = computed(() => {
  return tableData.value.filter(item => {
    // 关键字匹配
    const matchKeyword = !filterParams.keyword ||
      item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
      item.manufacturer.toLowerCase().includes(filterParams.keyword.toLowerCase());

    // 类别匹配
    const matchCategory = !filterParams.category || item.category === filterParams.category;

    // 成分类型匹配 (支持预设品种 + 其他自定义品种)
    let matchComponent = true;
    if (filterParams.componentType) {
      if (filterParams.componentType === 'OTHER') {
        // 选择“其他”，匹配所有不在预设数组里的值
        matchComponent = !presetComponents.includes(item.componentType);
      } else {
        // 选择具体品种，精确匹配
        matchComponent = item.componentType === filterParams.componentType;
      }
    }

    // 日期范围匹配
    let matchDate = true;
    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      const start = filterParams.dateRange[0];
      const end = filterParams.dateRange[1];
      const itemDate = item.stockInTime.substring(0, 10);
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

const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("没有可导出的数据");

  if (command === 'excel') {
    exportToExcel(activeData, feedHeaderMap, "饲料入库记录");
  } else if (command === 'copy_simple') {
    try {
      let text = `📋【饲料入库简报】\n\n`;
      activeData.forEach((item, index) => {
        text += `${index + 1}. 名称：${item.name} | 厂家：${item.manufacturer} | 数量：${item.stockInQuantity}kg | 入库时间：${item.stockInTime}\n`;
      });
      await toClipboard(text);
      ElMessage.success("简洁数据已复制");
    } catch { ElMessage.error("复制失败"); }
  } else if (command === 'copy_full') {
    try {
      let text = `📋【饲料入库全量数据导出】\n\n`;
      activeData.forEach((item, index) => {
        text += `[记录 #${index + 1}]\n` +
                `--------------------------\n` +
                `生产厂家：${item.manufacturer || ''}\n` +
                `饲料名称：${item.name || ''}\n` +
                `饲料类别：${item.category || ''}\n` +
                `成分类型：${item.componentType || ''}\n` +
                `--------------------------\n` +
                `入库数量：${item.stockInQuantity || 0} kg\n` +
                `入库单价：${item.unitPrice || 0} 元\n` +
                `总计金额：${item.totalPrice || 0} 元\n` +
                `--------------------------\n` +
                `水分含量：${item.waterContent || 0} %\n` +
                `霉变比例：${item.mildew || 0} %\n` +
                `杂质含量：${item.impurityContent || 0} %\n` +
                `营养成分：${item.nutrients || '无'}\n` +
                `--------------------------\n` +
                `入库日期：${item.stockInTime || ''}\n` +
                `--------------------------\n\n`;
      });
      await toClipboard(text);
      ElMessage.success("全量明细已按模板复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

const handleCopyRow = async (row) => {
  try {
    const text = `【饲料入库核心数据】\n` +
                 `--------------------------\n` +
                 `生产厂家：${row.manufacturer || ''}\n` +
                 `饲料名称：${row.name || ''}\n` +
                 `饲料类别：${row.category || ''}\n` +
                 `成分类型：${row.componentType || ''}\n` +
                 `--------------------------\n` +
                 `入库数量：${row.stockInQuantity || 0} kg\n` +
                 `入库单价：${row.unitPrice || 0} 元\n` +
                 `总计金额：${row.totalPrice || 0} 元\n` +
                 `--------------------------\n` +
                 `水分含量：${row.waterContent || 0} %\n` +
                 `霉变比例：${row.mildew || 0} %\n` +
                 `杂质含量：${row.impurityContent || 0} %\n` +
                 `营养成分：${row.nutrients || '无'}\n` +
                 `--------------------------\n` +
                 `入库时间：${row.stockInTime || ''}\n` +
                 `--------------------------`;
    await toClipboard(text);
    ElMessage.success("核心数据已复制");
  } catch { ElMessage.error("复制失败"); }
};

const formData = reactive({
  id: null, manufacturer: "", name: "", category: "", componentType: "", usage: "",
  productionDate: null, expirationDate: null, nutrients: "", waterContent: 0,
  mildew: 0, impurityContent: 0, unitOfMeasure: "kg", stockInQuantity: 0,
  unitPrice: 0, freightFee: 0, operator: ""
});

const formRules = {
  manufacturer: [{ required: true, message: "必填", trigger: "change" }],
  name: [{ required: true, message: "必填", trigger: "blur" }],
  category: [{ required: true, message: "必填", trigger: "change" }],
  componentType: [{ required: true, message: "必填", trigger: "change" }],
  productionDate: [{ required: true, message: "必填", trigger: "change" }],
  stockInQuantity: [{ required: true, message: "必填", trigger: "blur" }],
  unitPrice: [{ required: true, message: "必填", trigger: "blur" }]
};

const loadStockInList = async () => {
  loading.value = true;
  try {
    const res = await getFeedStockInList();
    tableData.value = res.data || res || [];
  } catch { ElMessage.error("列表加载失败"); }
  finally { loading.value = false; }
};

const loadManufacturers = async () => {
  try {
    const res = await getManufacturerList();
    allManufacturers.value = res.data || [];
  } catch (err) { console.error(err); }
};

const openAddDialog = () => {
  isEditMode.value = false; dialogTitle.value = "添加入库记录";
  resetForm(); formData.operator = userStore.userInfo?.username || 'admin';
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  if (!isAdmin.value) return ElMessage.error("无权限");
  isEditMode.value = true; dialogTitle.value = "编辑入库记录";
  Object.assign(formData, { ...row }); dialogVisible.value = true;
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const payload = { ...formData, mildew: String(formData.mildew || 0) };
        if (isEditMode.value) await updateFeedStockIn(formData.id, payload);
        else await addFeedStockIn(payload);
        ElMessage.success("操作成功"); dialogVisible.value = false; loadStockInList();
      } finally { loading.value = false; }
    }
  });
};

const handleDelete = (row) => {
  if (!isAdmin.value) return ElMessage.error("无权限");
  ElMessageBox.confirm("确定删除吗？", "警告", { type: "warning" }).then(async () => {
    await deleteFeedStockIn(row.id); ElMessage.success("删除成功"); loadStockInList();
  });
};

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields();
  Object.assign(formData, {
    id: null, manufacturer: "", name: "", category: "", componentType: "",
    usage: "", productionDate: dayjs().format('YYYY-MM-DD'), expirationDate: null,
    nutrients: "", waterContent: 0, mildew: 0, impurityContent: 0,
    stockInQuantity: 0, unitPrice: 0, freightFee: 0, unitOfMeasure: "kg", operator: ""
  });
};

onMounted(() => { loadStockInList(); loadManufacturers(); });
</script>

<style scoped>
.page-container { display: flex; width: 100%; height: 100%; }
.left-blank { flex: 1; }
.main-content {
  flex: 4; display: flex; flex-direction: column; padding: 20px;
  background-color: #f9f9f9; border-radius: 8px; margin: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
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
