import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "axios";

export const useAIStore = defineStore("ai", () => {
  const globalContext = ref({
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
  });

  const reportLoading = ref(false);
  const currentReport = ref("");

  // ✨ 新增：自动提问信号
  const autoQuestion = ref("");

  const setPageContext = (key, contextStr) => {
    if (key in globalContext.value) {
      globalContext.value[key] = contextStr;
    }
  };

  // ✨ 新增：触发自动提问
  const triggerAutoQuestion = (question) => {
    autoQuestion.value = question;
  };

  const allContext = computed(() => {
    const ctx = globalContext.value;
    const hasData = Object.values(ctx).some((val) => val && val.trim() !== "");

    if (!hasData) {
      return "目前系统尚未加载任何实时数据。请先浏览相关管理页面以完成数据同步。";
    }

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

  const generateAIReport = async (type, context) => {
    reportLoading.value = true;
    try {
      const response = await axios.post("http://localhost:8080/api/ai/report", {
        reportType: type,
        dataContext: context,
      });
      const content = response.data?.choices?.[0]?.message?.content || "报告生成失败";
      currentReport.value = content;
      return content;
    } catch (error) {
      console.error("AI 报告生成异常", error);
      throw error;
    } finally {
      reportLoading.value = false;
    }
  };

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
    currentReport.value = "";
  };

  return {
    globalContext,
    allContext,
    reportLoading,
    currentReport,
    autoQuestion,
    setPageContext,
    triggerAutoQuestion,
    generateAIReport,
    clearAllContext,
  };
});
