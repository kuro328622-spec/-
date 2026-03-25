<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2 class="page-title">饲料总库存管理</h2>

        <div class="buttons">
          <el-dropdown trigger="click" @command="handleExportCommand">
            <el-button type="warning" icon="Share">
              导出/分析筛选数据<el-icon class="el-icon--right"><arrow-down /></el-icon>
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
          placeholder="搜索厂家或名称..."
          style="width: 180px;"
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

        <el-select
          v-model="filterParams.stockStatus"
          placeholder="库存状态"
          clearable
          style="width: 150px;"
        >
          <el-option label="全部库存记录" value="all" />
          <el-option label="⚠️ 库存不足 (预警)" value="alert" />
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
              <th>饲料名称</th>
              <th>类别</th>
              <th>成分类型</th>
              <th>库存数量</th>
              <th>单位</th>
              <th>加权平均单价</th>
              <th>警戒数量</th>
              <th style="width: 110px">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in filteredList"
              :key="item.id"
              :class="{'alert-row': item.alert !== null && item.quantity < item.alert}"
            >
              <td>{{ item.manufacturer }}</td>
              <td>{{ item.name }}</td>
              <td><span class="custom-tag tag-feed">{{ item.category }}</span></td>
              <td>
                <el-tag effect="plain" type="success" size="small">
                  {{ item.componentType || '未归类' }}
                </el-tag>
              </td>
              <td><b>{{ item.quantity }}</b></td>
              <td>{{ item.unitOfMeasure }}</td>
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

                  <el-button
                    type="danger"
                    size="small"
                    icon="Delete"
                    @click="handleDeleteRow(item)"
                  >删除</el-button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredList.length === 0">
              <td colspan="9" style="padding: 40px; color: #999;">暂无匹配的库存数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="修改库存预警值" width="400px">
      <el-form :model="editItem" label-width="120px">
        <el-form-item label="饲料名称"><span>{{ editItem.name }}</span></el-form-item>
        <el-form-item label="警戒数量">
          <el-input-number v-model.number="editItem.alert" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from "vue";
import { useAIStore } from "@/stores/aiStore"; // 1. 确保 Store 文件名正确
import { useUserStore } from "@/stores/userStore";
import { getInventoryList, updateAlertQuantity, deleteFeed } from "@/api/feedInventoryApi.js";
import { ElMessage, ElMessageBox } from "element-plus";
import { exportToExcel, formatRecordToText } from "@/utils/exportUtils.js";
import useClipboard from 'vue-clipboard3';

const { toClipboard } = useClipboard();
const aiStore = useAIStore();
const userStore = useUserStore();
const inventoryList = ref([]);
const editDialogVisible = ref(false);
const editItem = ref({ id: null, alert: null, name: "" });

const presetComponents = ['玉米', '豆粕', '鱼粉', '麸皮'];

const filterParams = reactive({
  keyword: '',
  stockStatus: 'all',
  category: '',
  componentType: ''
});

const feedHeaderMap = {
  manufacturer: "生产厂家",
  name: "饲料名称",
  category: "饲料类别",
  componentType: "成分类型",
  quantity: "当前库存",
  unitOfMeasure: "单位",
  unitPrice: "单价",
  alert: "警戒线"
};

// --- 修改后的数据同步方法：采用方案 B (全局注入) ---
const syncDataToAIContext = () => {
  if (inventoryList.value.length === 0) {
    aiStore.setPageContext('feedStock', '当前饲料库存为空。');
    return;
  }

  const inventorySummary = inventoryList.value.map((item, index) => {
    const totalValue = (item.quantity * (item.unitPrice || 0)).toFixed(2);
    const stockStatus = (item.alert !== null && item.quantity < item.alert) ? '⚠️库存不足' : '✅正常';

    return `[饲料档案 ${index + 1}] 名称: ${item.name} | 厂家: ${item.manufacturer} | 实时库存: ${item.quantity} ${item.unitOfMeasure} | 状态: ${stockStatus} | 预估占用资金: ${totalValue} 元`;
  }).join('\n');

  // 核心：使用 key 'feedStock' 存入，不再覆盖整个 pageContext
  aiStore.setPageContext('feedStock', inventorySummary);
  console.log("🚀 饲料总库存数据已同步至全局 AI 存储池");
};

watch(inventoryList, () => {
  syncDataToAIContext();
}, { deep: true });

const fetchInventory = async () => {
  try {
    const res = await getInventoryList();
    inventoryList.value = res || [];
    syncDataToAIContext();
  } catch { ElMessage.error("数据加载失败"); }
};

const handleDeleteRow = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) {
    ElMessage.error("权限不足，只有管理员可以删除！");
    return;
  }
  if (row.quantity > 0) {
    ElMessage.warning(`【${row.name}】尚有库存，无法删除！请清空库存后再操作。`);
    return;
  }
  ElMessageBox.confirm(
    `确定要永久删除饲料【${row.name}】的档案吗？此操作不可撤销。`,
    '安全警告',
    { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await deleteFeed(row.id);
      ElMessage.success("库存档案已删除");
      fetchInventory();
    } catch { ElMessage.error("删除失败"); }
  }).catch(() => {});
};

const filteredList = computed(() => {
  return inventoryList.value.filter(item => {
    const matchKeyword = !filterParams.keyword ||
      item.name.toLowerCase().includes(filterParams.keyword.toLowerCase()) ||
      item.manufacturer.toLowerCase().includes(filterParams.keyword.toLowerCase());

    let matchStatus = true;
    if (filterParams.stockStatus === 'alert') {
      matchStatus = item.alert !== null && item.quantity < item.alert;
    } else if (filterParams.stockStatus === 'normal') {
      matchStatus = item.alert === null || item.quantity >= item.alert;
    }

    const matchCategory = !filterParams.category || item.category === filterParams.category;

    let matchComponent = true;
    if (filterParams.componentType) {
      if (filterParams.componentType === 'OTHER') {
        matchComponent = !presetComponents.includes(item.componentType);
      } else {
        matchComponent = item.componentType === filterParams.componentType;
      }
    }

    return matchKeyword && matchStatus && matchCategory && matchComponent;
  });
});

const resetFilter = () => {
  filterParams.keyword = '';
  filterParams.stockStatus = 'all';
  filterParams.category = '';
  filterParams.componentType = '';
};

const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("筛选结果为空");

  if (command === 'excel') {
    exportToExcel(activeData, feedHeaderMap, "饲料库存分析表");
  } else if (command === 'copy_simple') {
    try {
      let text = `📋【饲料库存概览】\n--------------------------\n`;
      activeData.forEach((item, index) => {
        text += `${index + 1}. 【名称】${item.name} | 【当前库存】${item.quantity} ${item.unitOfMeasure}\n`;
      });
      await toClipboard(text);
      ElMessage.success("已复制简洁数据");
    } catch { ElMessage.error("复制失败"); }
  } else if (command === 'copy_full') {
    try {
      let text = `📋【饲料库存明细】\n\n`;
      activeData.forEach((item) => {
        text += formatRecordToText(item, feedHeaderMap, `名称:${item.name}`);
        text += "\n";
      });
      await toClipboard(text);
      ElMessage.success("全量明细已复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

const handleCopyRow = async (item) => {
  try {
    const text = formatRecordToText(item, feedHeaderMap, "单条明细");
    await toClipboard(text);
    ElMessage.success("已复制该条记录");
  } catch { ElMessage.error("复制失败"); }
};

const openEditDialog = (item) => {
  editItem.value = { ...item };
  editDialogVisible.value = true;
};

const submitEdit = async () => {
  try {
    await updateAlertQuantity(editItem.value.id, editItem.value.alert);
    ElMessage.success("更新成功");
    editDialogVisible.value = false;
    fetchInventory();
  } catch { ElMessage.error("操作失败"); }
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
.header-actions { margin-bottom: 15px; display: flex; justify-content: space-between; align-items: center; }
.filter-bar {
  display: flex; gap: 10px; margin-bottom: 20px; background: #fff;
  padding: 15px 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); align-items: center;
}
.filter-info { margin-left: auto; font-size: 13px; color: #909399; }
.op-buttons-vertical {
  display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 5px 0;
}
.op-buttons-vertical .el-button { width: 90px; margin-left: 0 !important; }
.page-title { font-size: 1.5rem; color: #333; }
.table-container { flex: 1; overflow-x: auto; }
.inventory-table { width: 100%; border-collapse: collapse; background-color: #fff; }
.inventory-table th, .inventory-table td {
  padding: 10px 8px; border: 1px solid #ebeef5; text-align: center; font-size: 14px;
}
.inventory-table thead th { background-color: #f5f7fa; font-weight: 600; }
.custom-tag {
  padding: 2px 10px; font-size: 12px; border-radius: 4px; border: 1px solid #adc6ff; color: #2f54eb; background: #f0f5ff;
}
.alert-row { background-color: #fef0f0 !important; }
.alert-row td { color: #f56c6c !important; font-weight: bold; }
</style>
