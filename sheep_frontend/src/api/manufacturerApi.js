// 1. 引入统一的请求实例
import request from "@/utils/request";

/**
 * 添加供应商/生产厂家记录
 */
export function addManufacturer(data) {
  // 对应后端 @PostMapping("/manufacturer")
  return request.post("/manufacturer", data);
}

/**
 * 获取所有供应商记录
 */
export function getManufacturerList() {
  // 对应后端 @GetMapping("/manufacturer")
  return request.get("/manufacturer");
}

/**
 * 根据ID更新供应商记录
 */
export function updateManufacturer(id, data) {
  // 对应后端 @PutMapping("/manufacturer/{id}")
  return request.put(`/manufacturer/${id}`, data);
}

/**
 * 根据ID删除供应商记录
 */
export function deleteManufacturer(id) {
  // 对应后端 @DeleteMapping("/manufacturer/{id}")
  return request.delete(`/manufacturer/${id}`);
}
