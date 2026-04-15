import request from "./request.js";

/**
 * 获取药品/疫苗出库列表
 */
export const getDrugVaccineStockOutList = (queryDTO = {}) => {
  // 补齐 /api
  return request.post("/api/stock/out/list", queryDTO);
};

/**
 * 添加药品/疫苗出库记录
 */
export const addDrugVaccineStockOut = (data) => {
  return request.post("/api/stock/out/create", data);
};

/**
 * 更新药品/疫苗出库记录
 */
export const updateDrugVaccineStockOut = (id, data) => {
  return request.put(`/api/stock/out/update/${id}`, data);
};

/**
 * 删除药品/疫苗出库记录
 */
export const deleteDrugVaccineStockOut = (id) => {
  return request.delete(`/api/stock/out/delete/${id}`);
};
