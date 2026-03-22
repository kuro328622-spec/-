import request from "@/utils/request";

// 获取财政收支列表（支持日期、类型筛选）
export const getFinancialList = (params) => {
  return request({
    // 修改处：去掉了开头的 /api
    url: "/financial/list",
    method: "get",
    params, // 包含 startDate, endDate, type 等筛选条件
  });
};

// 添加收支记录
export const addFinancialRecord = (data) => {
  return request({
    // 修改处：去掉了开头的 /api
    url: "/financial/add",
    method: "post",
    data,
  });
};

// 删除记录
export const deleteFinancialRecord = (id) => {
  return request({
    // 修改处：去掉了开头的 /api
    url: `/financial/delete/${id}`,
    method: "delete",
  });
};

// 导出 Excel
export const exportFinancialExcel = (params) => {
  return request({
    // 修改处：去掉了开头的 /api
    url: "/financial/export",
    method: "get",
    params,
    responseType: "blob",
  });
};
