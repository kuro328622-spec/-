<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2 class="page-title">药品 / 疫苗总库存管理</h2>

        <div class="button-group">
          <el-dropdown trigger="click" @command="handleExportCommand">
            <el-button type="warning" icon="Share">
              导出 / 复制筛选数据
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
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

        <el-button @click="resetFilter" icon="Refresh">重置筛选</el-button>

        <div class="filter-info" v-if="filteredList.length !== inventoryList.length">
          已筛选出 <b>{{ filteredList.length }}</b> 条记录
        </div>
      </div>

      <div class="table-container" v-loading="loading">
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
              <th style="width: 120px">操作</th>
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
                  <el-button type="warning" size="small" icon="DocumentCopy" @click="handleCopyRow(item)">复制</el-button>
                  <el-button type="primary" size="small" icon="Edit" @click="openEditDialog(item)">修改预警</el-button>
                  <el-button
                    type="danger"
                    size="small"
                    icon="Delete"
                    @click="handleDeleteRow(item)"
                  >删除</el-button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredList.length === 0 && !loading">
              <td colspan="9" style="padding: 40px; color: #999">暂无匹配数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="修改库存预警值" width="400px">
      <el-form :model="editItem" label-width="120px">
        <el-form-item label="药品名称"><span>{{ editItem.name }}</span></el-form-item>
        <el-form-item label="警戒数量">
          <el-input-number v-model.number="editItem.alert" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { useUserStore } from "@/stores/userStore";
import { useAIStore } from "@/stores/aiStore";
import { getInventoryList, updateAlertQuantity, deleteMedicine } from "@/api/medicineApi.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { exportToExcel, formatRecordToText } from "@/utils/exportUtils.js";
import { ArrowDown } from "@element-plus/icons-vue";
import useClipboard from "vue-clipboard3";

// 状态定义
const userStore = useUserStore();
const aiStore = useAIStore();
const { toClipboard } = useClipboard();
const inventoryList = ref([]);
const loading = ref(false);
const editDialogVisible = ref(false);
const editItem = ref({ id: null, name: "", alert: null });
const filterParams = reactive({ keyword: "", stockStatus: "all" });

const medicineHeaderMap = {
  manufacturer: "生产厂家", name: "药品名称", category: "类别",
  componentType: "成分类型", unitOfMeasure: "单位", quantity: "库存数量",
  unitPrice: "单价", alert: "警戒数量"
};

// --- 修改后的数据同步方法：采用方案 B (全局注入) ---
const syncDataToAIContext = () => {
  if (inventoryList.value.length === 0) {
    aiStore.setPageContext('medicineStock', '当前暂无药品/疫苗库存数据。');
    return;
  }

  // 整理业务数据，去除冗余 ID
  const inventorySummary = inventoryList.value.map((item, index) => {
    const totalValue = (item.quantity * (item.unitPrice || 0)).toFixed(2);
    const stockStatus = (item.alert !== null && item.quantity < item.alert) ? '⚠️库存不足' : '✅正常';

    return `[药品清单 ${index + 1}] 名称: ${item.name} | 类别: ${item.category} | 厂家: ${item.manufacturer} | 实时库存: ${item.quantity}${item.unitOfMeasure} | 状态: ${stockStatus} | 预估占用资金: ${totalValue}元`;
  }).join('\n');

  // 核心修改：将数据存入 aiStore 的 medicineStock 模块
  aiStore.setPageContext('medicineStock', inventorySummary);
  console.log("🚀 药品/疫苗总库存数据已同步至全局 AI 存储池");
};

// 监听库存变化，自动更新 AI 的上下文
watch(inventoryList, () => {
  syncDataToAIContext();
}, { deep: true });

// 获取数据
const fetchInventory = async () => {
  loading.value = true;
  try {
    const res = await getInventoryList();
    inventoryList.value = res.data || res || [];
    // 同步给 AI
    syncDataToAIContext();
  } catch {
    ElMessage.error("数据加载失败，请检查后端接口");
  } finally {
    loading.value = false;
  }
};

// 过滤后的列表
const filteredList = computed(() => {
  return inventoryList.value.filter(item => {
    const matchK = !filterParams.keyword ||
                   item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
                   item.manufacturer.toLowerCase().includes(filterParams.keyword.toLowerCase());

    let matchS = true;
    if (filterParams.stockStatus === "alert") {
      matchS = item.alert !== null && item.quantity < item.alert;
    } else if (filterParams.stockStatus === "normal") {
      matchS = item.alert === null || item.quantity >= item.alert;
    }
    return matchK && matchS;
  });
});

// 删除逻辑
const handleDeleteRow = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，无法执行此操作！");
    return;
  }

  if (row.quantity > 0) {
    ElMessage.warning("该药品尚有库存，无法直接删除档案，请先办理出库！");
    return;
  }

  ElMessageBox.confirm(`此操作将永久删除【${row.name}】的库存档案, 是否继续?`, '警告', {
    confirmButtonText: "确定", cancelButtonText: "取消", type: 'warning'
  }).then(async () => {
    loading.value = true;
    try {
      await deleteMedicine(Number(row.id));
      ElMessage.success("库存档案删除成功！");
      fetchInventory();
    } catch {
      ElMessage.error("删除失败 (403)");
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    ElMessage.info("已取消删除");
  });
};

const openEditDialog = (item) => {
  editItem.value = { ...item };
  editDialogVisible.value = true;
};

const submitEdit = async () => {
  if (editItem.value.alert === null) return ElMessage.warning("请输入有效的警戒数量");
  loading.value = true;
  try {
    await updateAlertQuantity(editItem.value.id, editItem.value.alert);
    ElMessage.success("库存预警值更新成功");
    editDialogVisible.value = false;
    fetchInventory();
  } catch {
    ElMessage.error("更新失败");
  } finally {
    loading.value = false;
  }
};

const handleCopyRow = async (item) => {
  try {
    await toClipboard(formatRecordToText(item, medicineHeaderMap, "库存详情"));
    ElMessage.success("数据已复制到剪贴板");
  } catch { ElMessage.error("复制失败"); }
};

const handleExportCommand = (c) => {
  if (c === 'excel') exportToExcel(filteredList.value, medicineHeaderMap, "药品库存表");
};

const resetFilter = () => {
  Object.assign(filterParams, { keyword: "", stockStatus: "all" });
};

onMounted(fetchInventory);
</script>

<style scoped>
.page-container { display: flex; width: 100%; height: 100%; }
.left-blank { flex: 1; }
.main-content {
  flex: 4; display: flex; flex-direction: column; padding: 20px;
  background-color: #f9f9f9; margin: 20px; border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
.header-actions { display: flex; justify-content: space-between; margin-bottom: 15px; }
.filter-bar {
  display: flex; gap: 15px; margin-bottom: 20px; background: #fff;
  padding: 15px; border-radius: 8px; align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.op-buttons-vertical { display: flex; flex-direction: column; gap: 4px; }
.op-buttons-vertical .el-button { width: 90px; margin-left: 0 !important; }
.table-container { flex: 1; background: #fff; border-radius: 4px; overflow: hidden; }
.inventory-table { width: 100%; border-collapse: collapse; }
.inventory-table th, .inventory-table td { padding: 12px 8px; border: 1px solid #ebeef5; text-align: center; font-size: 14px; }
.inventory-table thead th { background: #f5f7fa; color: #606266; font-weight: bold; }
.custom-tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.tag-success { background: #f0f9eb; color: #67c23a; border: 1px solid #e1f3d8; }
.tag-warning { background: #fdf6ec; color: #e6a23c; border: 1px solid #faecd8; }
.alert-row { background-color: #fef0f0 !important; color: #f56c6c; font-weight: bold; }
</style>
