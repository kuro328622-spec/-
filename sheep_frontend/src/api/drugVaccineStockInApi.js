// 1. 引入统一的请求实例（关键：这个实例带自动跳转逻辑）
import request from "@/utils/request";

/**
 * 添加药品/疫苗入库记录
 */
export function addDrugVaccineStockIn(data) {
  return request.post("/drug-vaccine/stock-in", data);
}

/**
 * 获取所有药品/疫苗入库记录
 */
export function getDrugVaccineStockInList() {
  return request.get("/drug-vaccine/stock-in");
}

/**
 * 根据ID更新药品/疫苗入库记录
 */
export function updateDrugVaccineStockIn(id, data) {
  return request.put(`/drug-vaccine/stock-in/${id}`, data);
}

/**
 * 根据ID删除药品/疫苗入库记录
 */
export function deleteDrugVaccineStockIn(id) {
  return request.delete(`/drug-vaccine/stock-in/${id}`);
}
