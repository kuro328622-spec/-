import request from "./request.js";

// ========== 总库存接口 ==========

export const getInventoryList = () => {
  // 补齐 /api
  return request.get("/api/inventory/medicine");
};

export const updateAlertQuantity = (id, alertQuantity) => {
  return request.put(`/api/inventory/medicine/${id}/alert`, null, {
    params: { alertQuantity },
  });
};

// ========== 出库接口 ==========

export const createStockOut = (data) => {
  // 补齐 /api
  return request.post("/api/stock/out/create", data);
};

export const updateStockOut = (id, data) => {
  return request.put(`/api/stock/out/update/${id}`, data);
};

export const deleteStockOut = (id) => {
  return request.delete(`/api/stock/out/delete/${id}`);
};

export const listStockOut = (queryDTO) => {
  return request.post("/api/stock/out/list", queryDTO || {});
};
