<template>
  <div class="page-container">
    <div class="left-blank"></div>

    <div class="main-content">
      <div class="header-actions">
        <h2 class="page-title">生产厂家/供应商档案管理</h2>

        <div class="buttons">
          <el-button type="primary" icon="Plus" @click="openAddDialog" style="margin-right: 12px">
            新增厂家档案
          </el-button>

          <el-dropdown trigger="click" @command="handleExportCommand">
            <el-button type="warning" icon="Share">
              导出/分析筛选数据<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="copy_simple">简洁复制</el-dropdown-item>
                <el-dropdown-item command="copy_full">完整复制 </el-dropdown-item>
                <el-dropdown-item command="excel" divided>导出 Excel </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="filter-bar">
        <el-input
          v-model="filterParams.keyword"
          placeholder="搜索供应商名称..."
          style="width: 250px;"
          prefix-icon="Search"
          clearable
        />

        <el-select
          v-model="filterParams.category"
          placeholder="供应类别 (多选)"
          clearable
          multiple
          collapse-tags
          collapse-tags-indicator
          style="width: 280px;"
        >
          <el-option v-for="cat in SORT_ORDER" :key="cat" :label="cat" :value="cat" />
        </el-select>

        <el-button @click="resetFilter">重置筛选</el-button>

        <div class="filter-info" v-if="filteredList.length !== tableData.length">
          已筛选出 <b>{{ filteredList.length }}</b> 家供应商
        </div>
      </div>

      <div class="table-container">
        <el-table v-loading="loading" :data="filteredList" style="width: 100%" border stripe>
          <el-table-column label="操作" width="100" fixed="left">
            <template #default="scope">
              <div class="op-buttons-vertical">
                <el-button
                  type="warning"
                  size="small"
                  icon="DocumentCopy"
                  @click="handleCopyRow(scope.row)"
                >复制</el-button>

                <el-button type="success" size="small" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
              </div>
            </template>
          </el-table-column>

          <el-table-column prop="manufacturerName" label="供应商名称" min-width="180" />

          <el-table-column label="供应类别" width="150">
            <template #default="scope">
              <el-tag
                v-for="tag in (scope.row.manCategory ? scope.row.manCategory.split(',') : [])"
                :key="tag"
                :type="tag === '药品' ? 'success' : tag === '疫苗' ? 'warning' : tag === '饲料' ? '' : 'info'"
                style="margin-bottom: 2px; margin-right: 5px;"
              >
                {{ tag }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="manContactPerson" label="联系人" width="100" />
          <el-table-column prop="manContactPhone" label="联系电话" width="130" />
          <el-table-column prop="manContactEmail" label="联系邮箱" min-width="150" show-overflow-tooltip />
          <el-table-column prop="manContactAddress" label="联系地址" min-width="180" show-overflow-tooltip />
          <el-table-column prop="manRemark" label="备注" min-width="150" show-overflow-tooltip />

          <el-table-column prop="createTime" label="录入日期" width="160">
            <template #default="scope">
              <span>{{ scope.row.createTime ? scope.row.createTime.replace('T', ' ').split('.')[0] : '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" @close="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="厂家名称" prop="manufacturerName">
              <el-input v-model="formData.manufacturerName" placeholder="请输入供应商全称" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="供应类别" prop="categoryArray">
              <el-checkbox-group v-model="formData.categoryArray">
                <el-checkbox v-for="item in SORT_ORDER" :key="item" :label="item" />
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系人" prop="manContactPerson"><el-input v-model="formData.manContactPerson" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="manContactPhone"><el-input v-model="formData.manContactPhone" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="联系邮箱" prop="manContactEmail"><el-input v-model="formData.manContactEmail" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="联系地址" prop="manContactAddress"><el-input v-model="formData.manContactAddress" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注信息" prop="manRemark">
              <el-input type="textarea" v-model="formData.manRemark" :rows="3" placeholder="其他补充信息" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="submitForm">保 存 档 案</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from "vue";
import { useUserStore } from "@/stores/userStore";
import { ElMessage, ElMessageBox } from "element-plus";
import { getManufacturerList, addManufacturer, updateManufacturer, deleteManufacturer } from "@/api/manufacturerApi";
import { exportToExcel, formatRecordToText } from "@/utils/exportUtils.js";
import useClipboard from 'vue-clipboard3';

// ✨ AI 接入核心：引入 Store
import { useAIStore } from '@/stores/aiStore';

const aiStore = useAIStore();
const { toClipboard } = useClipboard();
const userStore = useUserStore();
const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref("新增厂家档案");
const formRef = ref(null);
const isEditMode = ref(false);
const SORT_ORDER = ['药品', '疫苗', '饲料', '其他'];

// --- AI 注入功能 ---

/**
 * 将供应商列表同步到 AI 全局上下文
 */
const syncVendorsToAI = () => {
  if (!tableData.value || tableData.value.length === 0) {
    aiStore.setPageContext('vendorStats', "目前系统暂无任何供应商或厂家档案记录。");
    return;
  }

  // 格式化数据，使 AI 能够清晰识别厂家与其供应品类及联系方式
  const summary = tableData.value.map((item, index) => {
    const cats = item.manCategory ? item.manCategory.replace(/,/g, '、') : '未标注品类';
    return `[供应商 ${index + 1}] 名称：${item.manufacturerName} | 供应类别：${cats} | 联系人：${item.manContactPerson} | 电话：${item.manContactPhone}${item.manRemark ? ' | 备注：' + item.manRemark : ''}`;
  }).join('\n');

  // 注入到 aiStore 的 vendorStats 模块中
  aiStore.setPageContext('vendorStats', summary);
};

// 监听表格数据变化，确保 AI 记忆实时更新
watch(tableData, () => {
  syncVendorsToAI();
}, { deep: true });

// --- 业务逻辑 ---

const filterParams = reactive({
  keyword: '',
  category: []
});

const vendorHeaderMap = {
  manufacturerName: "供应商名称",
  manCategory: "供应类别",
  manContactPerson: "联系人",
  manContactPhone: "联系电话",
  manContactEmail: "联系邮箱",
  manContactAddress: "联系地址",
  manRemark: "备注"
};

const filteredList = computed(() => {
  return tableData.value.filter(item => {
    const matchKeyword = !filterParams.keyword ||
      item.manufacturerName.toLowerCase().includes(filterParams.keyword.toLowerCase());

    let matchCategory = true;
    if (filterParams.category && filterParams.category.length > 0) {
      const itemCats = item.manCategory ? item.manCategory.split(',') : [];
      matchCategory = filterParams.category.some(c => itemCats.includes(c));
    }

    return matchKeyword && matchCategory;
  });
});

const resetFilter = () => {
  filterParams.keyword = '';
  filterParams.category = [];
};

const handleExportCommand = async (command) => {
  const activeData = filteredList.value;
  if (activeData.length === 0) return ElMessage.warning("当前筛选条件下无数据");

  if (command === 'excel') {
    exportToExcel(activeData, vendorHeaderMap, "供应商名录表");
    ElMessage.success("Excel 导出成功");
  } else if (command === 'copy_simple') {
    try {
      let text = `📋【供应商名录 - AI分析专用版】\n`;
      text += `生成时间：${new Date().toLocaleString()} (共${activeData.length}家)\n`;
      text += `--------------------------\n`;

      activeData.forEach((item, index) => {
        text += `${index + 1}. 【供应商名称】${item.manufacturerName}\n`;
        text += `   【联系信息】联系人：${item.manContactPerson} | 电话：${item.manContactPhone}\n`;
        const categoryDisplay = item.manCategory ? item.manCategory.replace(/,/g, '、') : '未标注品类';
        text += `   【供应品类】${categoryDisplay}\n`;
        if (item.manRemark) {
          text += `   【备注信息】${item.manRemark}\n`;
        }
        text += `--------------------------\n`;
      });

      await toClipboard(text);
      ElMessage.success("已按实际档案字段完成复制");
    } catch {
      ElMessage.error("复制失败");
    }
  } else if (command === 'copy_full') {
    try {
      let text = `📋【供应商完整档案详情】\n\n`;
      activeData.forEach((item) => {
        text += formatRecordToText(item, vendorHeaderMap, item.manufacturerName);
        text += "\n";
      });
      await toClipboard(text);
      ElMessage.success("完整数据已复制");
    } catch { ElMessage.error("复制失败"); }
  }
};

const handleCopyRow = async (row) => {
  try {
    const text = formatRecordToText(row, vendorHeaderMap, "供应商名片");
    await toClipboard(text);
    ElMessage.success("该行信息已复制");
  } catch { ElMessage.error("复制失败"); }
};

const formData = reactive({
  id: null, manufacturerName: "", categoryArray: [],
  manContactPerson: "", manContactPhone: "", manContactEmail: "",
  manContactAddress: "", manRemark: ""
});

const formRules = reactive({
  manufacturerName: [{ required: true, message: "请输入厂家名称", trigger: "blur" }],
  categoryArray: [{ type: 'array', required: true, message: "请选择供应类别", trigger: "change" }],
  manContactPerson: [{ required: true, message: "请输入联系人", trigger: "blur" }],
  manContactPhone: [{ required: true, message: "请输入有效电话", trigger: "blur" }]
});

const loadList = async () => {
  loading.value = true;
  try {
    const res = await getManufacturerList();
    tableData.value = res.data || [];
    // 加载完成后主动触发一次同步
    syncVendorsToAI();
  } catch { ElMessage.error("数据加载失败"); }
  finally { loading.value = false; }
};

const submitForm = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const sortedCats = [...formData.categoryArray].sort((a, b) => SORT_ORDER.indexOf(a) - SORT_ORDER.indexOf(b));
        const dataToSubmit = { ...formData, manCategory: sortedCats.join(',') };
        if (isEditMode.value) {
          await updateManufacturer(formData.id, dataToSubmit);
          ElMessage.success("修改成功");
        } else {
          await addManufacturer(dataToSubmit);
          ElMessage.success("新增成功");
        }
        dialogVisible.value = false;
        loadList();
      } catch { ElMessage.error("操作失败"); }
      finally { loading.value = false; }
    }
  });
};

const handleEdit = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) return ElMessage.error("仅管理员可操作");
  isEditMode.value = true;
  dialogTitle.value = "编辑厂家档案";
  Object.assign(formData, { ...row, categoryArray: row.manCategory ? row.manCategory.split(',') : [] });
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  if (!userStore.userInfo?.roles?.includes("ROLE_ADMIN")) return ElMessage.error("仅管理员可操作");
  ElMessageBox.confirm("此操作不可恢复，确定删除该供应商吗？", "警告", { type: 'error' }).then(async () => {
    try {
      await deleteManufacturer(row.id);
      ElMessage.success("删除成功");
      loadList();
    } catch { ElMessage.error("删除失败"); }
  });
};

const openAddDialog = () => { isEditMode.value = false; dialogTitle.value = "新增厂家档案"; resetForm(); dialogVisible.value = true; };
const resetForm = () => { if (formRef.value) formRef.value.resetFields(); Object.assign(formData, { id: null, manufacturerName: "", categoryArray: [] }); };

onMounted(loadList);
</script>

<style scoped>
.page-container { display: flex; width: 100%; height: 100%; }
.left-blank { flex: 1; }
.main-content {
  flex: 4; display: flex; flex-direction: column; padding: 20px;
  background-color: #f9f9f9; border-radius: 8px; margin: 20px;
}
.header-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.page-title { font-size: 1.5rem; color: #333; margin: 0; }

.filter-bar {
  display: flex; gap: 15px; margin-bottom: 20px; background: #fff;
  padding: 15px 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); align-items: center;
}
.filter-info { margin-left: auto; font-size: 13px; color: #909399; }

.op-buttons-vertical {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px 0;
}
.op-buttons-vertical .el-button {
  width: 74px;
  margin-left: 0 !important;
}

.table-container { flex: 1; overflow-x: auto; }
</style>
