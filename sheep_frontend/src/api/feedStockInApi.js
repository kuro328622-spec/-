import request from "./request";

/**
 * 添加饲料入库记录
 */
export function addFeedStockIn(data) {
  return request.post("/api/feed/stock-in", data);
}

/**
 * 获取所有饲料入库记录
 */
export function getFeedStockInList() {
  return request.get("/api/feed/stock-in");
}

/**
 * 根据ID更新饲料入库记录
 */
export function updateFeedStockIn(id, data) {
  return request.put(`/api/feed/stock-in/${id}`, data);
}

/**
 * 根据ID删除饲料入库记录
 */
export function deleteFeedStockIn(id) {
  return request.delete(`/api/feed/stock-in/${id}`);
}
