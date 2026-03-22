package com.example.demo.repository;

import com.example.demo.entity.ManufacturerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 生产厂家/供应商信息 仓储接口 继承 JpaRepository 以获得标准 CRUD 功能
 */
@Repository
public interface ManufacturerInfoRepository extends JpaRepository<ManufacturerInfo, Long> {

    /**
     * 根据生产厂家名称查询厂家信息 用于： 1. 入库时校验厂家是否存在 2. 防止重复添加同名厂家
     *
     * * @param manufacturerName 厂家名称
     * @return 厂家实体的 Optional 封装
     */
    Optional<ManufacturerInfo> findByManufacturerName(String manufacturerName);
}
