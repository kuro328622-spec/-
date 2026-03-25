import { defineStore } from "pinia";
import { ref, computed } from "vue";

export const useAIStore = defineStore("ai", () => {
  // 1. 定义全局状态对象
  // 这里的 key 必须与页面组件中 setPageContext 传入的 key 保持严格一致
  const globalContext = ref({
    // 饲料模块
    feedStock: "", // 饲料实时库存
    feedInLogs: "", // 饲料入库流水
    feedOutLogs: "", // 饲料出库流水

    // 药品疫苗模块
    medicineStock: "", // 药品/疫苗实时库存
    medicineInLogs: "", // 药品/疫苗入库流水
    medicineOutLogs: "", // 药品/疫苗出库流水

    // 财务模块
    financeLogs: "", // ✨ 新增：收支明细/财务流水

    // 基础档案
    pigFiles: "", // 羊群/猪只档案
    vendorStats: "", // 供应商/厂家信息
    other: "", // 其他临时上下文
  });

  /**
   * 2. 设置指定模块的上下文数据
   * @param {string} key - 模块名称
   * @param {string} contextStr - 格式化后的字符串数据
   */
  const setPageContext = (key, contextStr) => {
    if (key in globalContext.value) {
      globalContext.value[key] = contextStr;
      console.log(
        `%c[AI Context] ✅ ${key} 模块已成功同步至全局`,
        "color: #42b983; font-weight: bold;",
      );
    } else {
      console.error(
        `%c[AI Context] ❌ 尝试更新未定义的模块: ${key}。请检查 aiStore.js 中的 globalContext 定义。`,
        "color: #f56c6c; font-weight: bold;",
      );
    }
  };

  /**
   * 3. 汇总属性：AI 助手将读取这个拼接好的全量信息
   */
  const allContext = computed(() => {
    const ctx = globalContext.value;

    // 检查是否至少有一个模块有数据
    const hasData = Object.values(ctx).some((val) => val && val.trim() !== "");

    if (!hasData) {
      return "目前系统尚未加载任何实时数据。请先浏览相关管理页面以完成数据同步。";
    }

    // 构建结构化报告（增加财务模块编号）
    return `
# 养殖场全局实时数据汇总

${ctx.feedStock ? "## 1. 饲料库存状态\n" + ctx.feedStock + "\n" : ""}
${ctx.feedInLogs ? "## 2. 饲料最近入库动态\n" + ctx.feedInLogs + "\n" : ""}
${ctx.feedOutLogs ? "## 3. 饲料最近出库动态\n" + ctx.feedOutLogs + "\n" : ""}

${ctx.medicineStock ? "## 4. 药品与疫苗库存\n" + ctx.medicineStock + "\n" : ""}
${ctx.medicineInLogs ? "## 5. 药品最近入库动态\n" + ctx.medicineInLogs + "\n" : ""}
${ctx.medicineOutLogs ? "## 6. 药品最近出库动态\n" + ctx.medicineOutLogs + "\n" : ""}

${ctx.vendorStats ? "## 7. 供应商及厂家档案\n" + ctx.vendorStats + "\n" : ""}

${ctx.pigFiles ? "## 8. 档案与生长状态\n" + ctx.pigFiles + "\n" : ""}

${ctx.financeLogs ? "## 9. 收支明细与财务流水\n" + ctx.financeLogs + "\n" : ""}

${ctx.other ? "## 其他补充信息\n" + ctx.other : ""}
`.trim();
  });

  /**
   * 4. 清除方法：在用户退出登录时调用
   */
  const clearAllContext = () => {
    globalContext.value = {
      feedStock: "",
      feedInLogs: "",
      feedOutLogs: "",
      medicineStock: "",
      medicineInLogs: "",
      medicineOutLogs: "",
      financeLogs: "",
      pigFiles: "",
      vendorStats: "",
      other: "",
    };
    console.log("[AI Context] 全局上下文已清空");
  };

  return {
    globalContext,
    allContext,
    setPageContext,
    clearAllContext,
  };
});
