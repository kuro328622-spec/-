package com.example.demo.service;

import com.example.demo.entity.DrugVaccineStockIn;
import java.util.List;
import java.util.Optional;

/**
 * 药品/疫苗入库业务逻辑接口
 */
public interface DrugVaccineStockInService {

    /**
     * 保存或更新一条入库记录（新增入库）
     *
     * @param stockIn 入库记录实体
     * @return 保存或更新后的入库记录
     */
    DrugVaccineStockIn save(DrugVaccineStockIn stockIn);

    /**
     * 查询所有入库记录
     *
     * @return 入库记录列表
     */
    List<DrugVaccineStockIn> findAll();

    /**
     * 根据ID查找入库记录
     *
     * @param id 记录ID
     * @return 包含入库记录的 Optional 对象，如果找不到则为 empty
     */
    Optional<DrugVaccineStockIn> findById(Long id);

    /**
     * 根据ID删除入库记录
     *
     * @param id 记录ID
     */
    void deleteById(Long id);

    /**
     * 编辑入库记录（同步库存）
     *
     * @param id 要编辑的记录ID
     * @param newStockIn 编辑后的入库记录实体
     * @return 编辑后的入库记录
     */
    DrugVaccineStockIn update(Long id, DrugVaccineStockIn newStockIn);
}
