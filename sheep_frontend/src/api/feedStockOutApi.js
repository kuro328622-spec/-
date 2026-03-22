// 核心修复：引入你已经在 src/api/request.js 中植入了跳转逻辑的那个 request
import request from "./request.js";

/**
 * 获取饲料出库列表
 * @param {Object} queryDTO 查询条件
 */
export const getFeedStockOutList = (queryDTO = {}) => {
  // 确保路径与后端一致。
  // 因为 request.js 的 baseURL 是 http://localhost:8080/api
  // 所以这里拼接后就是 http://localhost:8080/api/feed/stock-out/list
  return request.post("/feed/stock-out/list", queryDTO);
};

/**
 * 添加饲料出库记录
 */
export const addFeedStockOut = (data) => {
  return request.post("/feed/stock-out/create", data);
};

/**
 * 更新饲料出库记录
 */
export const updateFeedStockOut = (id, data) => {
  return request.put(`/feed/stock-out/update/${id}`, data);
};

/**
 * 删除饲料出库记录
 */
export const deleteFeedStockOut = (id) => {
  return request.delete(`/feed/stock-out/delete/${id}`);
};
