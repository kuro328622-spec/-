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
     * ✨ 新增：根据ID删除库存档案
     */
    void deleteById(Long id);

    /**
     * 出库操作：根据厂家、名称及类别减少库存
     *
     * @param manufacturer 生产厂家
     * @param name 药品名称
     * @param category 类别
     * @param quantityChange 扣减数量
     */
    HStoreInventoryDrugVaccine reduceStock(String manufacturer, String name, String category, BigDecimal quantityChange);

    /**
     * 入库操作：同步库存
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
