<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2>药品/疫苗入库管理</h2>
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
          <el-button
            type="primary"
            icon="Plus"
            @click="openAddDialog"
          >
            添加入库记录
          </el-button>
        </div>
      </div>

      <div class="filter-bar" style="display: flex; gap: 10px; margin-bottom: 20px; background: #fff; padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); align-items: center;">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索厂家或名称..."
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
          start-placeholder="开始"
          end-placeholder="结束"
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
        <el-table
          v-loading="loading"
          :data="filteredList"
          style="width: 100%"
          border
          :default-sort="{ prop: 'stockInTime', order: 'descending' }"
        >
          <el-table-column
            label="操作"
            width="120"
            fixed="left"
          >
            <template #default="scope">
              <div style="display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 5px 0;">
                <el-button
                  type="warning"
                  size="small"
                  icon="DocumentCopy"
                  @click="handleCopyRow(scope.row)"
                  style="width: 80px; margin-left: 0;"
                >
                  复制
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  icon="Edit"
                  @click="handleEdit(scope.row)"
                  style="width: 80px; margin-left: 0;"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  icon="Delete"
                  @click="handleDelete(scope.row)"
                  style="width: 80px; margin-left: 0;"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column
            prop="manufacturer"
            label="生产厂家"
            min-width="120"
          />
          <el-table-column
            prop="name"
            label="名称"
            min-width="120"
          />
          <el-table-column
            prop="category"
            label="类别"
            width="80"
          >
            <template #default="scope">
              <el-tag :type="scope.row.category === '药品' ? 'success' : 'warning'">
                {{ scope.row.category }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="componentType"
            label="成分类型"
            min-width="100"
          />
          <el-table-column
            prop="usage"
            label="用途"
            min-width="150"
            show-overflow-tooltip
          />

          <el-table-column
            prop="productionDate"
            label="生产日期"
            width="120"
          >
            <template #default="scope">
              <span>{{ scope.row.productionDate ? scope.row.productionDate.split(' ')[0].split('T')[0] : '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="expirationDate"
            label="有效期至"
            width="120"
          >
            <template #default="scope">
              <span>{{ scope.row.expirationDate ? scope.row.expirationDate.split(' ')[0].split('T')[0] : '-' }}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="productionBatch"
            label="批号"
            min-width="120"
          />
          <el-table-column
            prop="unitOfMeasure"
            label="单位"
            width="80"
          />
          <el-table-column
            prop="stockInQuantity"
            label="数量"
            width="80"
          />
          <el-table-column
            prop="unitPrice"
            label="单价(元)"
            width="100"
          />
          <el-table-column
            prop="freightFee"
            label="运费(元)"
            width="100"
          />
          <el-table-column
            prop="totalPrice"
            label="总价(元)"
            width="100"
          />
          <el-table-column
            prop="convertedUnitPrice"
            label="折合单价"
            width="100"
          />
          <el-table-column
            prop="operator"
            label="操作员"
            width="80"
          />
          <el-table-column
            prop="stockInTime"
            label="入库日期"
            width="180"
          >
            <template #default="scope">
              <span>{{ scope.row.stockInTime ? scope.row.stockInTime.replace('T', ' ').split('.')[0] : '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="totalInventory"
            label="总库存"
            width="80"
          />
        </el-table>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item
          label="类别"
          prop="category"
        >
          <el-select
            v-model="formData.category"
            placeholder="请选择类别"
            :disabled="isEditMode"
            @change="handleCategoryChange"
          >
            <el-option
              label="药品"
              value="药品"
            />
            <el-option
              label="疫苗"
              value="疫苗"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          label="生产厂家"
          prop="manufacturer"
        >
          <el-select
            v-model="formData.manufacturer"
            placeholder="请选择厂家或手动输入新厂家"
            :disabled="isEditMode || !formData.category"
            filterable
            allow-create
            default-first-option
            style="width: 100%"
          >
            <el-option
              v-for="item in filteredManufacturers"
              :key="item.id || item.manufacturerName"
              :label="item.manufacturerName"
              :value="item.manufacturerName"
            />
          </el-select>
        </el-form-item>

        <el-form-item
          label="名称"
          prop="name"
        >
          <el-input
            v-model="formData.name"
            placeholder="请输入药品/疫苗名称"
            :disabled="isEditMode"
          />
        </el-form-item>
        <el-form-item
          label="成分类型"
          prop="component_type"
        >
          <el-input
            v-model="formData.component_type"
            placeholder="请输入成分类型"
          />
        </el-form-item>
        <el-form-item
          label="用途"
          prop="usage"
        >
          <el-input
            type="textarea"
            v-model="formData.usage"
            placeholder="请输入用途"
            rows="2"
          />
        </el-form-item>
        <el-form-item
          label="生产日期"
          prop="production_date"
        >
          <el-date-picker
            v-model="formData.production_date"
            type="date"
            placeholder="选择生产日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item
          label="有效期至"
          prop="expiration_date"
        >
          <el-date-picker
            v-model="formData.expiration_date"
            type="date"
            placeholder="选择有效期至"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item
          label="批号"
          prop="production_batch"
        >
          <el-input
            v-model="formData.production_batch"
            placeholder="请输入生产批号"
          />
        </el-form-item>
        <el-form-item
          label="单位"
          prop="unit_of_measure"
        >
          <el-input
            v-model="formData.unit_of_measure"
            placeholder="如：瓶、盒、支"
            :disabled="isEditMode"
          />
        </el-form-item>
        <el-form-item
          label="数量"
          prop="stock_in_quantity"
        >
          <el-input-number
            v-model="formData.stock_in_quantity"
            :min="1"
            placeholder="请输入数量"
          />
        </el-form-item>
        <el-form-item
          label="单价(元)"
          prop="unit_price"
        >
          <el-input-number
            v-model="formData.unit_price"
            :min="0"
            :step="0.01"
            placeholder="请输入单价"
          />
        </el-form-item>

        <el-form-item
          label="运费(元)"
          prop="freight_fee"
        >
          <el-input-number
            v-model="formData.freight_fee"
            :min="0"
            :step="0.01"
            placeholder="请输入运费"
          />
        </el-form-item>

        <el-form-item
          label="总价(元)"
          prop="total_price"
        >
          <el-input-number
            v-model="formData.total_price"
            :min="0"
            :step="0.01"
            disabled
          />
        </el-form-item>

        <el-form-item
          label="折合单价"
          prop="converted_unit_price"
        >
          <el-input-number
            v-model="formData.converted_unit_price"
            :min="0"
            :step="0.01"
            disabled
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitForm"
          >提交</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from "vue";
import { useUserStore } from "@/stores/userStore";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getDrugVaccineStockInList,
  addDrugVaccineStockIn,
  updateDrugVaccineStockIn,
  deleteDrugVaccineStockIn,
} from "@/api/drugVaccineStockInApi";
import { getManufacturerList } from "@/api/manufacturerApi";
// 修改点 4: 引入导出和剪贴板工具
import { exportToExcel } from "@/utils/exportUtils.js";
import useClipboard from "vue-clipboard3";
// 引入 AI 存储桶
import { useAIStore } from '@/stores/aiStore';
const aiStore = useAIStore();
const { toClipboard } = useClipboard();
const userStore = useUserStore();

const tableData = ref([]);
const loading = ref(false);

const dialogVisible = ref(false);
const dialogTitle = ref("添加入库记录");
const formRef = ref(null);
const isEditMode = ref(false);

// 修改点 5: 添加筛选参数和 Excel 映射
const filterParams = reactive({
  keyword: "",
  dateRange: [],
  category: ""
});

const drugHeaderMap = {
  manufacturer: "生产厂家",
  name: "名称",
  category: "类别",
  stockInQuantity: "数量",
  unitOfMeasure: "单位",
  unitPrice: "单价",
  totalPrice: "总价",
  stockInTime: "入库时间",
  operator: "操作员"
};

// 修改点 6: 添加联动筛选计算属性
const filteredList = computed(() => {
  return tableData.value.filter(item => {
    const matchKeyword = !filterParams.keyword ||
      item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
      item.manufacturer.toLowerCase().includes(filterParams.keyword.toLowerCase());

    const matchCategory = !filterParams.category || item.category === filterParams.category;

    let matchDate = true;
    if (filterParams.dateRange && filterParams.dateRange.length === 2) {
      const start = filterParams.dateRange[0];
      const end = filterParams.dateRange[1];
      const itemDate = item.stockInTime.substring(0, 10);
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

// 修改点 7: 添加导出/全量复制函数
const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("没有可操作的数据");

  if (command === 'excel') {
    exportToExcel(activeData, drugHeaderMap, "药品疫苗入库记录");
  } else if (command === 'copy_simple') {
    try {
      let text = `📋【药品入库简报】\n\n`;
      activeData.forEach((item, index) => {
        text += `${index + 1}. 【厂家】${item.manufacturer} | 【名称】${item.name} | 数量：${item.stockInQuantity}${item.unitOfMeasure} | 入库时间：${item.stockInTime.split('T')[0]}\n`;
      });
      await toClipboard(text);
      ElMessage.success("简洁数据已复制");
    } catch { ElMessage.error("复制失败"); }
  } else if (command === 'copy_full') {
    try {
      let text = `📋【药品/疫苗入库全量分析数据】\n\n`;
      activeData.forEach((item, index) => {
        text += `[记录 #${index + 1}]\n` +
                `--------------------------\n` +
                `生产厂家：${item.manufacturer}\n` +
                `产品名称：${item.name}\n` +
                `产品类别：${item.category}\n` +
                `成分/类型：${item.componentType || '未标注'}\n` +
                `入库明细：${item.stockInQuantity} ${item.unitOfMeasure} (单价:${item.unitPrice}元)\n` +
                `费用合计：总价 ${item.totalPrice} 元 (含运费 ${item.freightFee} 元)\n` +
                `质检溯源：批号 ${item.productionBatch} | 有效期至 ${item.expirationDate?.split('T')[0]}\n` +
                `入库时间：${item.stockInTime?.replace('T', ' ')}\n` +
                `--------------------------\n\n`;
      });
      await toClipboard(text);
      ElMessage.success("全量数据已按模板复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

// 修改点 8: 添加单行复制函数
const handleCopyRow = async (row) => {
  try {
    const text = `【药品入库核心数据】\n` +
                 `--------------------------\n` +
                 `生产厂家：${row.manufacturer}\n` +
                 `产品名称：${row.name}\n` +
                 `规格数量：${row.stockInQuantity} ${row.unitOfMeasure}\n` +
                 `财务金额：单价 ${row.unitPrice} / 总价 ${row.totalPrice}\n` +
                 `有效期至：${row.expirationDate?.split('T')[0]}\n` +
                 `入库时间：${row.stockInTime?.replace('T', ' ')}`;
    await toClipboard(text);
    ElMessage.success("单条核心数据已复制");
  } catch { ElMessage.error("复制失败"); }
};

// 厂家数据逻辑 - 完全保留
const allManufacturers = ref([]);
const filteredManufacturers = computed(() => {
  if (!formData.category) return [];
  return allManufacturers.value.filter(item =>
    item.manCategory && item.manCategory.includes(formData.category)
  );
});

const handleCategoryChange = () => {
  formData.manufacturer = "";
};

const formData = reactive({
  id: null,
  manufacturer: "",
  name: "",
  category: "",
  component_type: "",
  usage: "",
  production_date: "",
  expiration_date: "",
  production_batch: "",
  unit_of_measure: "",
  stock_in_quantity: null,
  unit_price: null,
  total_price: null,
  freight_fee: null,
  converted_unit_price: null,
  operator: userStore.userInfo?.username,
});

const formRules = reactive({
  manufacturer: [
    { required: true, message: "请选择或手动输入生产厂家", trigger: "change" },
  ],
  name: [{ required: true, message: "请输入名称", trigger: "blur" }],
  category: [{ required: true, message: "请选择类别", trigger: "change" }],
  production_date: [
    { required: true, message: "请选择生产日期", trigger: "change" },
  ],
  expiration_date: [
    { required: true, message: "请选择有效期至", trigger: "change" },
  ],
  production_batch: [
    { required: true, message: "请输入批号", trigger: "blur" },
  ],
  unit_of_measure: [{ required: true, message: "请输入单位", trigger: "blur" }],
  stock_in_quantity: [
    { required: true, message: "请输入数量", trigger: "blur" },
  ],
  unit_price: [{ required: true, message: "请输入单价", trigger: "blur" }],
  total_price: [{ required: true, message: "总价不能为空", trigger: "blur" }],
  freight_fee: [{ required: true, message: "请输入运费", trigger: "blur" }],
});

// 完全保留你的 watch 监听逻辑
watch(
  [
    () => formData.stock_in_quantity,
    () => formData.unit_price,
    () => formData.freight_fee,
  ],
  () => {
    if (formData.stock_in_quantity && formData.unit_price) {
      formData.total_price =
        (formData.stock_in_quantity * formData.unit_price).toFixed(2) - 0;
    }
    if (
      formData.total_price !== null &&
      formData.freight_fee !== null &&
      formData.stock_in_quantity
    ) {
      formData.converted_unit_price =
        (
          (formData.total_price + formData.freight_fee) /
          formData.stock_in_quantity
        ).toFixed(2) - 0;
    }
  }
);

const loadManufacturers = async () => {
  try {
    const res = await getManufacturerList();
    allManufacturers.value = res.data || [];
  } catch (error) {
    console.error("厂家加载失败:", error);
  }
};

const openAddDialog = () => {
  isEditMode.value = false;
  dialogTitle.value = "添加入库记录";
  resetForm();
  dialogVisible.value = true;
};

// 提交函数：保留了所有 dataToSubmit 的字段映射
const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const dataToSubmit = {
          manufacturer: formData.manufacturer,
          name: formData.name,
          category: formData.category,
          componentType: formData.component_type,
          usage: formData.usage,
          productionDate: formData.production_date,
          expirationDate: formData.expiration_date,
          productionBatch: formData.production_batch,
          unitOfMeasure: formData.unit_of_measure,
          stockInQuantity: formData.stock_in_quantity,
          unitPrice: formData.unit_price,
          totalPrice: formData.total_price,
          freightFee: formData.freight_fee,
          convertedUnitPrice: formData.converted_unit_price,
          operator: formData.operator,
        };

        if (isEditMode.value) {
          await updateDrugVaccineStockIn(formData.id, dataToSubmit);
          ElMessage.success("修改成功！");
        } else {
          await addDrugVaccineStockIn(dataToSubmit);
          ElMessage.success("添加成功！");
        }
        dialogVisible.value = false;
        loadStockInList();
      } catch (error) {
        console.error("提交失败:", error);
        ElMessage.error("操作失败，请检查后端服务或网络连接！");
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.error("数据不完善，请检查并填写完整！");
    }
  });
};

const resetForm = () => {
  if (!formRef.value) return;
  formRef.value.resetFields();
  Object.assign(formData, {
    id: null,
    manufacturer: "",
    name: "",
    category: "",
    component_type: "",
    usage: "",
    production_date: "",
    expiration_date: "",
    production_batch: "",
    unit_of_measure: "",
    stock_in_quantity: null,
    unit_price: null,
    total_price: null,
    freight_fee: null,
    converted_unit_price: null,
    operator: userStore.userInfo?.username,
  });
};

// 编辑函数：保留了你原始的所有 Date 处理逻辑
const handleEdit = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，无法执行此操作！");
    return;
  }

  isEditMode.value = true;
  dialogTitle.value = "编辑入库记录";

  Object.assign(formData, {
    id: row.id,
    manufacturer: row.manufacturer,
    name: row.name,
    category: row.category,
    component_type: row.componentType,
    usage: row.usage,
    production_date: row.productionDate
      ? new Date(row.productionDate).toISOString().split("T")[0]
      : "",
    expiration_date: row.expirationDate
      ? new Date(row.expirationDate).toISOString().split("T")[0]
      : "",
    production_batch: row.productionBatch,
    unit_of_measure: row.unitOfMeasure,
    stock_in_quantity: row.stockInQuantity,
    unit_price: row.unitPrice,
    total_price: row.totalPrice,
    freight_fee: row.freightFee,
    converted_unit_price: row.convertedUnitPrice,
    operator: row.operator,
  });

  dialogVisible.value = true;
};

const handleDelete = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，无法执行此操作！");
    return;
  }

  ElMessageBox.confirm("此操作将永久删除该记录, 是否继续?", "警告", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(async () => {
      loading.value = true;
      try {
        await deleteDrugVaccineStockIn(Number(row.id));
        ElMessage.success("删除成功！");
        loadStockInList();
      } catch (error) {
        console.error("删除失败:", error);
        ElMessage.error("删除失败！");
      } finally {
        loading.value = false;
      }
    })
    .catch(() => {
      ElMessage.info("已取消删除");
    });
};

const loadStockInList = async () => {
  loading.value = true;
  try {
    const res = await getDrugVaccineStockInList();
    tableData.value = res.data;
  } catch (error) {
    console.error("获取入库列表失败:", error);
    tableData.value = [];
    ElMessage.error("获取数据失败，请联系管理员！");
  } finally {
    loading.value = false;
  }
};

// --- AI 注入逻辑开始 ---
const syncMedicineLogsToAI = () => {
  if (!tableData.value || tableData.value.length === 0) {
    aiStore.setPageContext('medicineInLogs', "目前系统暂无任何药品或疫苗的入库流水记录。");
    return;
  }

  // 提取最近 15 条完整记录
  const recentLogs = tableData.value.slice(0, 15);
  const summary = recentLogs.map((item, index) => {
    // 格式化日期显示
    const stockDate = item.stockInTime ? item.stockInTime.replace('T', ' ').split('.')[0] : '未知';
    const prodDate = item.productionDate ? item.productionDate.split('T')[0] : '未标注';
    const expDate = item.expirationDate ? item.expirationDate.split('T')[0] : '未标注';

    // 拼接全字段详细信息
    return `[记录${index + 1}]
    - 入库时间: ${stockDate}
    - 厂家: ${item.manufacturer} | 品名: ${item.name} | 类别: ${item.category}
    - 规格: 数量 ${item.stockInQuantity}${item.unitOfMeasure} | 批号: ${item.productionBatch}
    - 质量: 生产日期 ${prodDate} | 有效期至 ${expDate}
    - 详情: 成分类型 ${item.componentType || '通用'} | 用途: ${item.usage || '未填写'}
    - 财务: 单价 ${item.unitPrice}元 | 总价 ${item.totalPrice}元 | 运费 ${item.freightFee || 0}元
    - 操作员: ${item.operator}`;
  }).join('\n\n'); // 用双换行分隔，方便 AI 阅读

  aiStore.setPageContext('medicineInLogs', summary);
};

// 开启深度监听：一旦表格数据变动（增删改），立即更新 AI 的记忆
watch(tableData, () => {
  syncMedicineLogsToAI();
}, { deep: true });
// --- AI 注入逻辑结束 ---

onMounted(async () => {
  await loadStockInList(); // 确保这一行带 await，先拿数据
  loadManufacturers();

  // ✨ 在这里调用同步函数
  syncMedicineLogsToAI();
});
</script>

<style scoped>
/* 样式部分保持原样 */
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

.el-tooltip__content {
  max-width: 300px;
  word-wrap: break-word;
}
</style>
