<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2 class="page-title">药品 / 疫苗总库存管理</h2>

        <el-dropdown trigger="click" @command="handleExportCommand">
          <el-button type="warning" icon="Share">
            导出 / 复制筛选数据
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="copy_simple">简洁复制</el-dropdown-item>
              <el-dropdown-item command="copy_full">完整复制</el-dropdown-item>
              <el-dropdown-item command="excel" divided>导出 Excel</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索生产厂家或药品名称..."
          style="width: 250px"
          prefix-icon="Search"
          clearable
        />

        <el-select
          v-model="filterParams.stockStatus"
          placeholder="库存状态"
          clearable
          style="width: 180px"
        >
          <el-option label="全部库存记录" value="all" />
          <el-option label="⚠️ 库存不足（预警）" value="alert" />
          <el-option label="✅ 库存充足" value="normal" />
        </el-select>

        <el-button @click="resetFilter">重置筛选</el-button>

        <div class="filter-info" v-if="filteredList.length !== inventoryList.length">
          已筛选出 <b>{{ filteredList.length }}</b> 条记录
        </div>
      </div>

      <div class="table-container">
        <table class="inventory-table">
          <thead>
            <tr>
              <th>生产厂家</th>
              <th>药品名称</th>
              <th>类别</th>
              <th>成分类型</th>
              <th>单位</th>
              <th>库存数量</th>
              <th>单价</th>
              <th>警戒数量</th>
              <th style="width: 110px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in filteredList"
              :key="item.id"
              :class="{ 'alert-row': item.alert !== null && item.quantity < item.alert }"
            >
              <td>{{ item.manufacturer }}</td>
              <td>{{ item.name }}</td>
              <td>
                <span
                  :class="[
                    'custom-tag',
                    item.category === '药品' ? 'tag-success' : 'tag-warning'
                  ]"
                >
                  {{ item.category }}
                </span>
              </td>
              <td>{{ item.componentType }}</td>
              <td>{{ item.unitOfMeasure }}</td>
              <td><b>{{ item.quantity }}</b></td>
              <td>{{ item.unitPrice }}</td>
              <td>{{ item.alert ?? '未设置' }}</td>
              <td>
                <div class="op-buttons-vertical">
                  <el-button
                    type="warning"   size="small"
                    icon="DocumentCopy"
                    @click="handleCopyRow(item)"
                  >复制</el-button>

                  <el-button
                    type="primary"
                    size="small"
                    icon="Edit"
                    @click="openEditDialog(item)"
                  >修改预警</el-button>
                </div>
              </td>
            </tr>

            <tr v-if="filteredList.length === 0">
              <td colspan="9" style="padding: 40px; color: #999">
                暂无匹配的库存数据
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="修改库存预警值" width="400px">
      <el-form :model="editItem" label-width="120px">
        <el-form-item label="药品名称">
          <span>{{ editItem.name }}</span>
        </el-form-item>
        <el-form-item label="警戒数量">
          <el-input-number v-model.number="editItem.alert" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { getInventoryList, updateAlertQuantity } from "@/api/medicineApi.js";
import { ElMessage } from "element-plus";
import { exportToExcel, formatRecordToText } from "@/utils/exportUtils.js";
import useClipboard from "vue-clipboard3";

// 注意：由于你之前是 export default 风格，这里统一改为 script setup 风格以对齐之前的供应商页面
const { toClipboard } = useClipboard();

const inventoryList = ref([]);
const editDialogVisible = ref(false);
const editItem = ref({ id: null, name: "", alert: null });

// 1️⃣ 筛选参数
const filterParams = reactive({
  keyword: "",
  stockStatus: "all",
});

// 字段映射
const medicineHeaderMap = {
  manufacturer: "生产厂家",
  name: "药品名称",
  category: "类别",
  componentType: "成分类型",
  unitOfMeasure: "单位",
  quantity: "库存数量",
  unitPrice: "单价",
  alert: "警戒数量",
};

const fetchInventory = async () => {
  try {
    const res = await getInventoryList();
    inventoryList.value = res || [];
  } catch {
    ElMessage.error("库存数据加载失败");
  }
};

// 2️⃣ 计算属性：统一过滤逻辑
const filteredList = computed(() => {
  return inventoryList.value.filter((item) => {
    const matchKeyword =
      !filterParams.keyword ||
      item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
      item.manufacturer.toLowerCase().includes(filterParams.keyword.toLowerCase());

    let matchStatus = true;
    if (filterParams.stockStatus === "alert") {
      matchStatus = item.alert !== null && item.quantity < item.alert;
    } else if (filterParams.stockStatus === "normal") {
      matchStatus = item.alert === null || item.quantity >= item.alert;
    }

    return matchKeyword && matchStatus;
  });
});

const resetFilter = () => {
  filterParams.keyword = "";
  filterParams.stockStatus = "all";
};

// 3️⃣ 导出 / 复制
const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("筛选结果为空");

  if (command === "excel") {
    exportToExcel(activeData, medicineHeaderMap, "药品库存分析表");
    ElMessage.success("Excel 已生成");
  }  else if (command === 'copy_simple') {
    try {
      let text = `📋【药品/疫苗库存概览 - AI分析专用版】\n`;
      text += `生成时间：${new Date().toLocaleString()}\n`;
      text += `--------------------------\n`;

      activeData.forEach((item, index) => {
        // ✨ 修改点：明确【生产厂家】和【药品名称】标签
        // 注意：请确认你药品页面的厂家字段名是 manufacturer 还是 manufacturerName
        text += `${index + 1}. 【生产厂家】${item.manufacturer || '未标注'}\n`;
        text += `   【药品名称】${item.name}\n`;

        // 规格和类别对药品分析很重要，建议带上
        text += `   【规格属性】类别：${item.category || '药品'} | 库存单位：${item.unitOfMeasure}\n`;
        text += `   【当前库存】${item.quantity} ${item.unitOfMeasure}\n`;

        // 库存预警提示
        if (item.alert !== null && item.quantity < item.alert) {
          text += `   ⚠️ 状态：库存不足 (警戒线:${item.alert})\n`;
        }
        text += `--------------------------\n`;
      });

      await toClipboard(text);
      ElMessage.success("药品数据已按 AI 格式复制");
    } catch (err) {
      console.error(err);
      ElMessage.error("复制失败");
    }
  } else if (command === "copy_full") {
    try {
      let text = `📋【药品库存明细】\n\n`;
      activeData.forEach((item) => {
        text += formatRecordToText(item, medicineHeaderMap, item.name);
        text += "\n";
      });
      await toClipboard(text);
      ElMessage.success("完整版已复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

const handleCopyRow = async (item) => {
  try {
    const text = formatRecordToText(item, medicineHeaderMap, "库存明细");
    await toClipboard(text);
    ElMessage.success("已复制该行记录");
  } catch { ElMessage.error("复制失败"); }
};

const openEditDialog = (item) => {
  editItem.value = { ...item };
  editDialogVisible.value = true;
};

const submitEdit = async () => {
  try {
    await updateAlertQuantity(editItem.value.id, editItem.value.alert);
    const index = inventoryList.value.findIndex(i => i.id === editItem.value.id);
    if (index !== -1) {
      inventoryList.value[index].alert = editItem.value.alert;
    }
    ElMessage.success("更新成功");
    editDialogVisible.value = false;
  } catch {
    ElMessage.error("更新失败");
  }
};

onMounted(fetchInventory);
</script>

<style scoped>
.page-container { display: flex; width: 100%; height: 100%; }
.left-blank { flex: 1; }
.main-content {
  flex: 4; display: flex; flex-direction: column; padding: 20px;
  background-color: #f9f9f9; border-radius: 8px; margin: 20px;
}

.header-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.page-title { font-size: 1.5rem; color: #333; }

/* 🔍 筛选栏样式 */
.filter-bar {
  display: flex; gap: 15px; margin-bottom: 20px; background: #fff;
  padding: 15px 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); align-items: center;
}
.filter-info { margin-left: auto; font-size: 13px; color: #909399; }

/* 🛠️ 统一纵向按钮样式 */
.op-buttons-vertical {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 5px 0;
}
.op-buttons-vertical .el-button {
  width: 90px;
  margin-left: 0 !important;
}

/* 表格区域 */
.table-container { flex: 1; overflow-x: auto; }
.inventory-table { width: 100%; border-collapse: collapse; background-color: #fff; }
.inventory-table th, .inventory-table td {
  padding: 10px 8px; border: 1px solid #ebeef5; text-align: center; font-size: 14px;
}
.inventory-table thead th { background-color: #f5f7fa; font-weight: 600; }

.custom-tag { padding: 2px 10px; font-size: 12px; border-radius: 4px; border: 1px solid; }
.tag-success { background-color: #f0f9eb; border-color: #e1f3d8; color: #67c23a; }
.tag-warning { background-color: #fdf6ec; border-color: #faecd8; color: #e6a23c; }

/* 预警行高亮 */
.alert-row { background-color: #fef0f0 !important; }
.alert-row td { color: #f56c6c !important; font-weight: bold; }
</style>
