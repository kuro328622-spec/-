package com.example.demo.service;

import com.example.demo.entity.ManufacturerInfo;
import java.util.List;
import java.util.Optional;

/**
 * 生产厂家/供应商业务逻辑接口
 */
public interface ManufacturerInfoService {

    /**
     * 保存或更新生产厂家信息（仅管理员）
     */
    ManufacturerInfo save(ManufacturerInfo manufacturer);

    /**
     * 查询所有厂家名录
     */
    List<ManufacturerInfo> findAll();

    /**
     * 根据ID查找厂家
     */
    Optional<ManufacturerInfo> findById(Long id);

    /**
     * 根据厂家名称查找（用于入库时的自动匹配）
     */
    Optional<ManufacturerInfo> findByName(String name);

    /**
     * 根据ID删除厂家记录（仅管理员）
     */
    void deleteById(Long id);

    /**
     * 更新厂家信息（仅管理员）
     */
    ManufacturerInfo update(Long id, ManufacturerInfo newInfo);
}
