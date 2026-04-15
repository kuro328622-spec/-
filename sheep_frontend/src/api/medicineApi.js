import request from "./request.js";

// ========== 总库存管理接口 (Medicine Inventory) ==========

/**
 * 获取药品/疫苗总库存列表
 */
export const getInventoryList = () => {
  return request.get("/api/inventory/medicine");
};

/**
 * 修改库存警戒值
 * @param {Number|String} id 药品ID
 * @param {Number} alertQuantity 警戒数量
 */
export const updateAlertQuantity = (id, alertQuantity) => {
  return request.put(`/api/inventory/medicine/${id}/alert`, null, {
    params: { alertQuantity },
  });
};

/**
 * 删除药品库存档案
 * ✨ 修复点：参考入库管理，强制转换 ID 为 Number 类型，确保请求路径正确
 */
export const deleteMedicine = (id) => {
  return request.delete(`/api/inventory/medicine/${Number(id)}`);
};

// ========== 出库管理接口 (Stock Out) ==========

/**
 * 创建出库记录
 */
export const createStockOut = (data) => {
  return request.post("/api/stock/out/create", data);
};

/**
 * 更新出库记录
 */
export const updateStockOut = (id, data) => {
  return request.put(`/api/stock/out/update/${id}`, data);
};

/**
 * 删除出库记录
 */
export const deleteStockOut = (id) => {
  return request.delete(`/api/stock/out/delete/${Number(id)}`);
};

/**
 * 获取出库记录列表 (支持分页/筛选)
 */
export const listStockOut = (queryDTO) => {
  return request.post("/api/stock/out/list", queryDTO || {});
};
