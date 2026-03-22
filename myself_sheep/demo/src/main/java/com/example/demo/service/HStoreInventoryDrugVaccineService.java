package com.example.demo.service;

import com.example.demo.entity.HStoreInventoryDrugVaccine;
import com.example.demo.entity.DrugVaccineStockIn;

import java.math.BigDecimal;
import java.util.List;

/**
 * 总库存 Service 接口
 */
public interface HStoreInventoryDrugVaccineService {

    /**
     * 查询全部库存
     */
    List<HStoreInventoryDrugVaccine> findAll();

    /**
     * 更新警戒数量
     */
    HStoreInventoryDrugVaccine updateAlertQuantity(Long id, BigDecimal alertQuantity);

    /**
     * 出库操作：根据厂家、名称及类别减少库存 修复点：增加 category 参数，确保在同名同厂家情况下精准定位记录
     *
     * @param manufacturer 生产厂家
     * @param name 药品名称
     * @param category 类别（如：疫苗、药品）
     * @param quantityChange 扣减数量
     * @return 更新后的库存记录
     */
    HStoreInventoryDrugVaccine reduceStock(String manufacturer, String name, String category, BigDecimal quantityChange);

    /**
     * 【核心修复点】入库操作：同步库存 将返回值由 void 改为 BigDecimal，以便返回最新的库存总量给入库单显示。
     */
    BigDecimal syncInventoryFromStockIn(DrugVaccineStockIn stockIn);

    /**
     * 删除入库记录时同步库存
     */
    void syncInventoryOnDelete(DrugVaccineStockIn stockIn);

    /**
     * 同步库存更新（基于旧/新入库记录）
     */
    void syncInventoryOnUpdate(DrugVaccineStockIn oldStockIn, DrugVaccineStockIn newStockIn);
}
