import request from "./request";

// ========== 饲料总库存接口 ==========

export const getFeedInventoryList = () => {
  return request.get("/api/inventory/feed");
};

export const getInventoryList = getFeedInventoryList;

export const updateAlertQuantity = (id, alertQuantity) => {
  return request.put(`/api/inventory/feed/${id}/alert`, null, {
    params: { alertQuantity },
  });
};

// ========== 饲料出库接口 ==========

export const addFeedStockOut = (data) => {
  return request.post("/api/feed/stock-out/create", data);
};
export const createStockOut = addFeedStockOut;

export const getFeedStockOutList = (queryDTO = {}) => {
  return request.post("/api/feed/stock-out/list", queryDTO);
};
export const listStockOut = getFeedStockOutList;

export const updateFeedStockOut = (id, data) => {
  return request.put(`/api/feed/stock-out/update/${id}`, data);
};
export const updateStockOut = updateFeedStockOut;

export const deleteFeedStockOut = (id) => {
  return request.delete(`/api/feed/stock-out/delete/${id}`);
};
export const deleteStockOut = deleteFeedStockOut;
